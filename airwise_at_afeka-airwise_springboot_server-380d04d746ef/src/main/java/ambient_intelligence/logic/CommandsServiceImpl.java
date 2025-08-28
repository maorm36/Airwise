package ambient_intelligence.logic;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import ambient_intelligence.dal.CommandCrud;
import ambient_intelligence.dal.ObjectCrud;
import ambient_intelligence.dal.UserCrud;
import ambient_intelligence.data.ActionType;
import ambient_intelligence.data.CommandEntity;
import ambient_intelligence.data.ObjectEntity;
import ambient_intelligence.data.RepeatPattern;
import ambient_intelligence.data.UserEntity;
import ambient_intelligence.data.UserRole;
import ambient_intelligence.external_api.RestClientACService;
import ambient_intelligence.logic.boundaries.ACResponse;
import ambient_intelligence.logic.boundaries.ACState;
import ambient_intelligence.logic.boundaries.CommandBoundary;
import ambient_intelligence.logic.boundaries.CommandId;
import ambient_intelligence.logic.boundaries.CreatedBy;
import ambient_intelligence.logic.boundaries.ObjectBoundary;
import ambient_intelligence.logic.boundaries.ObjectId;
import ambient_intelligence.logic.boundaries.PowerConsumptionLog;
import ambient_intelligence.logic.boundaries.TargetObject;
import ambient_intelligence.logic.boundaries.UserId;
import ambient_intelligence.logic.converters.CommandConverter;
import ambient_intelligence.logic.converters.ObjectConverter;
import ambient_intelligence.logic.exceptions.ExternalApiException;
import ambient_intelligence.logic.exceptions.InvalidRequestInputException;
import ambient_intelligence.logic.exceptions.ObjectNotFoundException;
import ambient_intelligence.logic.exceptions.UnauthorizedException;
import ambient_intelligence.logic.security.AuthorizationService;
import ambient_intelligence.utils.AirWiseValidator;
import ambient_intelligence.utils.AirwiseConfig;
import ambient_intelligence.utils.DateUtils;
import ambient_intelligence.utils.EmailRequest;
import ambient_intelligence.utils.EmailService;
import ambient_intelligence.utils.ValueParser;

@Service
public class CommandsServiceImpl implements CommandsServiceWithPagination {

	private final CommandCrud commandCrud;
	private final ObjectCrud objectCrud;
	private final UserCrud userCrud;
	private final CommandConverter commandConverter;
	private final ObjectConverter objectConverter;
	private final AirWiseValidator validator;
	private final AuthorizationService authz;
	private final RestClientACService restClient;
	private final EmailService emailService;
	private final ObjectsService objectsService;
    private Log log = LogFactory.getLog(CommandsServiceImpl.class);
    
    @Autowired
    private ObjectMapper objectMapper;


	private static final String SYSTEM_OPERATOR_EMAIL = "SystemOperator@airwise.com";

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public CommandsServiceImpl(CommandCrud commandCrud, ObjectCrud objectCrud, UserCrud userCrud,
			CommandConverter commandConverter, AirWiseValidator validator, AuthorizationService authz,
			RestClientACService restClient, EmailService emailService, ObjectsService objectsService,
			ObjectConverter objectConverter) {

		this.commandCrud = commandCrud;
		this.objectCrud = objectCrud;
		this.userCrud = userCrud;
		this.commandConverter = commandConverter;
		this.validator = validator;
		this.authz = authz;
		this.restClient = restClient;
		this.emailService = emailService;
		this.objectsService = objectsService;
		this.objectConverter = objectConverter;
	}

	@Override
	@Transactional(readOnly = false)
	public List<Object> invokeCommand(CommandBoundary command) {

		if (!this.authz.ensureRole(command.getInvokedBy().getUserId().getSystemID(),
				command.getInvokedBy().getUserId().getEmail(), UserRole.END_USER)) {
			throw new UnauthorizedException("Unauthorized action.");
		}

		try {
			this.validator.isValidCommandRequest(command);
		} catch (Exception e) {
			throw new InvalidRequestInputException(e.getMessage());
		}

		String targetObjId = command.getTargetObject().getId().getSystemID() + AirwiseConfig.getIdSeparator()
				+ command.getTargetObject().getId().getObjectId();

		ObjectEntity target = objectCrud.findByIdAndActiveTrue(targetObjId)
				.orElseThrow(() -> new ObjectNotFoundException("TargetObject not found"));

		CommandId commandId = new CommandId(UUID.randomUUID().toString(), AirwiseConfig.getSystemID());

		command.setId(commandId);

		CommandEntity commandEntity = this.commandConverter.toEntity(command);

		commandEntity.setInvocationTimestamp(DateUtils.getCurrentFormattedDate());

		this.commandCrud.save(commandEntity);

		switch (command.getCommand()) {
		case "VERIFY_AC_BY_SERIAL_THEN_ADD" -> handleVerifyAcBySerialThenAdd(commandEntity, target);
		case "UPDATE_AC_STATE" -> handleUpdateACState(commandEntity, target);
		case "SCHEDULE_TASK" -> handleScheduleTask(commandEntity, target);
		case "ROOM_ACS_CONTROL" -> handleRoomAcsControl(commandEntity, target);
		case "DELETE_ENTITY_WITH_CHILDREN" -> handleDeleteEntityWithChildren(commandEntity, target);
		default -> throw new InvalidRequestInputException("Unknown command: " + command.getCommand());
		}

		return List.of(this.commandConverter.toBoundary(commandEntity));

	}

	@Override
	@Transactional(readOnly = true)
	public List<CommandBoundary> getAllCommandsHistory(String userSystemID, String userEmail, int size, int page) {

		this.validator.isValidPaginationInputs(size, page);

		// validate the user requesting the data and his Role.
		if (!this.authz.ensureRole(userSystemID, userEmail, UserRole.ADMIN)) {
			throw new UnauthorizedException("Unauthorized action.");
		}

		return this.commandCrud.findAll(PageRequest.of(page, size)).stream().map(this.commandConverter::toBoundary)
				.toList();
	}

	@Override
	@Transactional(readOnly = false)
	public void deleteAllCommands(String userSystemID, String userEmail) {

		// validate the user making the action and his Role.
		if (!this.authz.ensureRole(userSystemID, userEmail, UserRole.ADMIN)) {
			throw new UnauthorizedException("Unauthorized action.");
		}

		this.commandCrud.deleteAll();

	}

	// HANDLINDG COMMANDS
	private void handleVerifyAcBySerialThenAdd(CommandEntity command, ObjectEntity room) {

		if (!"Room".equalsIgnoreCase(room.getType())) {
			throw new InvalidRequestInputException("VERIFY_AC_BY_SERIAL_THEN_ADD can only be invoked on Room objects.");
		}

		// Get AC details from command attributes
		String serial = ValueParser.toString(command.getCommandAttributes().get("serial"));
		if (serial == null || serial.isBlank()) {
			throw new InvalidRequestInputException("AC serial number is missing in command attributes.");
		}

		String manufacturer = ValueParser.toString(command.getCommandAttributes().get("manufacturer"));
		if (manufacturer == null || manufacturer.isBlank()) {
			throw new InvalidRequestInputException("AC manufacturer is missing in command attributes.");
		}

		int wattsOfDevice = ValueParser.toInt(command.getCommandAttributes().get("wattsOfDevice"));
		if (wattsOfDevice <= 0) {
			throw new InvalidRequestInputException("AC Watts Of Device is invalid in command attributes.");
		}

		List<ObjectEntity> acsOfRoom = room.getChilds();

		if (acsOfRoom != null && !acsOfRoom.isEmpty()) {

			for (ObjectEntity acInRoom : acsOfRoom) {

				if ("AirConditioner".equalsIgnoreCase(acInRoom.getType()) && acInRoom.isActive()) {
					String serialOfAc = ValueParser.toString(acInRoom.getObjectDetails().get("serial"));
					if (serialOfAc.equals(serial)) {
						throw new InvalidRequestInputException(
								"This AC serial number is already exists in the selected Room.");
					}
				}

			}
		}

		// Call external API to fetch the latest state
		ACResponse response;
		try {
			response = restClient.getACStateBySerial(serial);
		} catch (Exception e) {
			throw new ExternalApiException("Failed to verify AC: " + e.getMessage(), e);
		}

		if (response == null || response.getAcState() == null || response.getCode() < 200 || response.getCode() > 299) {
			throw new ObjectNotFoundException("AC not found in external system.");
		}

		ObjectBoundary newAc = new ObjectBoundary();

		newAc.setType("AirConditioner");
		newAc.setAlias(serial);
		newAc.setStatus(ActionType.TURN_OFF.toString());
		newAc.setActive(true);

		// fetch or create SystemOperator user as creator (OPERATOR)
		String systemId = AirwiseConfig.getSystemID();
		String userKey = systemId + AirwiseConfig.getIdSeparator() + SYSTEM_OPERATOR_EMAIL;
		UserEntity operator = userCrud.findById(userKey).orElseGet(() -> {
			UserEntity newOp = new UserEntity();
			newOp.setUserId(userKey);
			newOp.setRole(UserRole.OPERATOR);
			newOp.setUsername("InternalSystemOperator");
			newOp.setAvatar("InternalSystemOperator");
			return userCrud.save(newOp);
		});

		CreatedBy createdBy = new CreatedBy();
		createdBy.setUserId(new UserId(systemId, SYSTEM_OPERATOR_EMAIL));
		newAc.setCreatedBy(createdBy);

		Map<String, Object> details = new HashMap<>();
		details.put("serial", serial);
		details.put("manufacturer", manufacturer);
		details.put("watts", wattsOfDevice);

		// get room defaults
		double temperature = ValueParser.toDouble(room.getObjectDetails().get("temperature"));
		String mode = ValueParser.toString(room.getObjectDetails().get("mode"));
		String fanSpeed = ValueParser.toString(room.getObjectDetails().get("fanSpeed"));

		details.put("power", false);
		details.put("temperature", temperature);
		details.put("mode", mode);
		details.put("fanSpeed", fanSpeed);

		this.validator.checkValidAcUpdateStateRequest(details);

		newAc.setObjectDetails(details);

		newAc = this.objectsService.create(newAc);

		// Update local new AC state with latest state
		ACState acState = response.getAcState();
		Map<String, Object> updatedDetails = newAc.getObjectDetails();
		updatedDetails.put("power", acState.getPower());
		updatedDetails.put("temperature", acState.getTemperature());
		updatedDetails.put("mode", acState.getMode());
		updatedDetails.put("fanSpeed", acState.getFanSpeed());
		newAc.setObjectDetails(updatedDetails);

		ObjectEntity newAcEntity = this.objectConverter.toEntity(newAc);

		this.objectCrud.save(newAcEntity);

		// bind to the room

		ObjectBoundary roomBoundary = this.objectConverter.toBoundary(room);

		this.objectsService.bindObjects(systemId, roomBoundary.getId().getObjectId(), systemId,
				newAc.getId().getObjectId(), systemId, SYSTEM_OPERATOR_EMAIL);

		try {
			createNotification(command.getInvokedBy().getUserId(), "AC Verified And Added to Room",
					"AC '" + newAc.getAlias()
							+ "' verified and added to room successfully. Latest state of AC has been updated.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Transactional(readOnly = false)
	private void handleUpdateACState(CommandEntity command, ObjectEntity target) {

		Map<String, Object> attrs = command.getCommandAttributes();

		String serial = target.getAlias(); // AC serial saved in Alias

		this.validator.checkValidAcUpdateStateRequest(attrs);

		boolean power = ValueParser.toBoolean(attrs.get("power"));
		double temp = ValueParser.toDouble(attrs.get("temperature"));
		String mode = ValueParser.toString(attrs.get("mode"));
		String fanSpeed = ValueParser.toString(attrs.get("fanSpeed"));

		ACResponse response = restClient.setACState(serial, power, temp, mode, fanSpeed);

		if (response.getCode() >= 200 && response.getCode() < 300) {

			target.setStatus(power ? ActionType.TURN_ON.toString() : ActionType.TURN_OFF.toString());

			Map<String, Object> saveattrs = target.getObjectDetails();
			saveattrs.put("power", power);
			saveattrs.put("temperature", temp);
			saveattrs.put("mode", mode);
			saveattrs.put("fanSpeed", fanSpeed);

			String alreadyStarted = ValueParser.toString(target.getObjectDetails().get("startDateTime"));

			if (power && alreadyStarted == null) {
				log.info("AC on: " + DateUtils.getCurrentFormattedDate());
				saveattrs.put("startDateTime", DateUtils.getCurrentFormattedDate());

			}
			

			target.setObjectDetails(saveattrs);
			
			this.objectCrud.save(target);

			if (!power) {
				try {
					String endDateTime = DateUtils.getCurrentFormattedDate();
					log.info("AC off: " + endDateTime);
					String startDateTime = ValueParser.toString(saveattrs.get("startDateTime"));
					
					saveattrs.put("startDateTime", null);
					saveattrs.put("endDateTime", null);
					
					target.setObjectDetails(saveattrs);
					this.objectCrud.save(target);

					this.logPowerConsumption(command, target, startDateTime, endDateTime);					

				} catch (Exception e) {
					// log failed powerConsumptionLog
					e.printStackTrace();
				}
			}

			// Log or notify
			createNotification(target.getCreatedBy().getUserId(), "AC State Updated", response.getMessage());

		} else {
			throw new ExternalApiException("Error:" + response.getMessage());
		}
	}
	
	@Transactional(readOnly = false)
	private void handleScheduleTask(CommandEntity command, ObjectEntity scheduledTask) {

		Map<String, Object> attrs = command.getCommandAttributes();

		// Validate fields: taskName, action, repeat, times, and optionally AC settings
		this.validator.validateScheduleTaskAttributes(attrs);

		String taskName = ValueParser.toString(attrs.get("taskName"));
		String startTime = ValueParser.toString(attrs.get("startTime"));
		String endTime = ValueParser.toString(attrs.get("endTime"));

		ActionType action = ActionType.fromString(ValueParser.toString(attrs.get("action")));
		RepeatPattern repeat = RepeatPattern.fromString(ValueParser.toString(attrs.get("repeat")));
		boolean useCurrentPrefs = ValueParser.toBoolean(attrs.getOrDefault("useCurrentPreferences", true));

		// === Validate parent: must be AC ===
		ObjectEntity ac = scheduledTask.getParent();
		if (ac == null || !"AirConditioner".equalsIgnoreCase(ac.getType())) {
			throw new InvalidRequestInputException("Scheduled task must be linked to an AirConditioner.");
		}

		// === Determine AC preferences ===
		double temperature;
		String mode;
		String fanSpeed;

		if (useCurrentPrefs || action == ActionType.TURN_OFF) {
			Map<String, Object> acDetails = ac.getObjectDetails();
			temperature = ValueParser.toDouble(acDetails.get("temperature"));
			mode = ValueParser.toString(acDetails.get("mode"));
			fanSpeed = ValueParser.toString(acDetails.get("fanSpeed"));
		} else {
			temperature = ValueParser.toDouble(attrs.get("temperature"));
			mode = ValueParser.toString(attrs.get("mode"));
			fanSpeed = ValueParser.toString(attrs.get("fanSpeed"));

			boolean power = action == ActionType.TURN_ON ? true : false;

			Map<String, Object> prefs = Map.of("power", power, "temperature", temperature, "mode", mode, "fanSpeed",
					fanSpeed);
			this.validator.checkValidAcUpdateStateRequest(prefs);
		}

		// === Update the Scheduled Task object ===
		scheduledTask.setStatus("SCHEDULED");
		scheduledTask.setActive(true);

		Map<String, Object> taskDetails = new HashMap<>();
		taskDetails.put("taskName", taskName);
		taskDetails.put("action", action.toString());
		taskDetails.put("startTime", startTime);
		if (endTime != null)
			taskDetails.put("endTime", endTime);
		taskDetails.put("repeat", repeat.toString());
		taskDetails.put("temperature", temperature);
		taskDetails.put("mode", mode);
		taskDetails.put("fanSpeed", fanSpeed);
		scheduledTask.setObjectDetails(taskDetails);

		objectCrud.save(scheduledTask);

		// === Notify the user ===
		createNotification(command.getInvokedBy().getUserId(), "Scheduled Task Confirmed",
				"Your task '" + taskName + "' has been scheduled to start at " + startTime);
	}

	@Transactional(readOnly = false)
	private void handleRoomAcsControl(CommandEntity command, ObjectEntity room) {

		List<ObjectEntity> children = room.getChilds();
		if (children == null || children.isEmpty()) {
			throw new InvalidRequestInputException("No ACs in this room.");
		}

		for (ObjectEntity ac : children) {
			CommandEntity clonedCmd = cloneCommand(command, ac);
			handleUpdateACState(clonedCmd, ac);
		}

		createNotification(command.getInvokedBy().getUserId(), "Group Control", "Group AC command dispatched.");
	}

	@Transactional(readOnly = false)
	private CommandEntity cloneCommand(CommandEntity original, ObjectEntity newTarget) {
		CommandEntity copy = new CommandEntity();
		copy.setCommand(original.getCommand());
		copy.setCommandAttributes(original.getCommandAttributes());
		copy.setInvokedBy(original.getInvokedBy());
		copy.setTargetObject(new TargetObject(new ObjectId(newTarget.getId(), AirwiseConfig.getSystemID())));
		copy.setInvocationTimestamp(DateUtils.getCurrentFormattedDate());
		return copy;
	}

	@Transactional(readOnly = false)
	private void createNotification(UserId userId, String title, String message) {

		String tenantEmail = userId.getEmail();

		Pageable pagingTenant = PageRequest.of(0, 1, Direction.DESC, "creationTimestamp", "id");
		List<ObjectEntity> tenants = this.objectCrud.findByAliasAndActiveTrue(tenantEmail, pagingTenant);
		if (tenants == null || tenants.isEmpty()) {
			return;
		}

		ObjectEntity tenant = tenants.getFirst();

		ObjectBoundary tenantBnd = this.objectConverter.toBoundary(tenant);

		ObjectBoundary notification = new ObjectBoundary();

		notification.setType("Notification");
		notification.setAlias("info-notification-" + tenantBnd.getId().getObjectId());
		notification.setStatus("info");
		notification.setActive(true);

		// fetch or create SystemOperator user as creator (OPERATOR)
		String systemId = AirwiseConfig.getSystemID();
		String userKey = systemId + AirwiseConfig.getIdSeparator() + SYSTEM_OPERATOR_EMAIL;
		UserEntity operator = userCrud.findById(userKey).orElseGet(() -> {
			UserEntity newOp = new UserEntity();
			newOp.setUserId(userKey);
			newOp.setRole(UserRole.OPERATOR);
			newOp.setUsername("InternalSystemOperator");
			return userCrud.save(newOp);
		});

		CreatedBy createdBy = new CreatedBy();
		createdBy.setUserId(new UserId(systemId, SYSTEM_OPERATOR_EMAIL));
		notification.setCreatedBy(createdBy);

		Map<String, Object> details = new HashMap<>();
		details.put("message", message);
		notification.setObjectDetails(details);

		notification = this.objectsService.create(notification);

		try {
			this.emailService.sendEmail(new EmailRequest("noreply@airwise.com", userId.getEmail(), title, message));
		} catch (Exception e) {
			// optionally log
		}
	}

	@Transactional(readOnly = false)
	public void updateAcStateViaCommand(CommandEntity command, ObjectEntity ac) {
		this.handleUpdateACState(command, ac);
	}

	
	@Transactional(readOnly = false)
	private void logPowerConsumption(CommandEntity command, ObjectEntity ac, String startDateTimeStr, String endDateTimeStr) {
	    String userEmail = command.getInvokedBy().getUserId().getEmail();
	    String userId = AirwiseConfig.getSystemID() + AirwiseConfig.getIdSeparator() + userEmail;

	    UserEntity user = this.userCrud.findById(userId)
	            .orElseThrow(() -> new ObjectNotFoundException("User not found for email: " + userEmail));

	    Pageable pagingTenant = PageRequest.of(0, 1, Direction.DESC, "creationTimestamp", "id");
	    List<ObjectEntity> tenants = this.objectCrud.findByAliasAndActiveTrue(userEmail, pagingTenant);
	    if (tenants == null || tenants.isEmpty()) {
	        log.warn("No tenant found for email: " + userEmail);
	        return;
	    }

	    ObjectEntity tenant = tenants.getFirst();

	    ObjectEntity room = ac.getParent();
	    if (room == null) {
	        throw new ObjectNotFoundException("No room parent for AC Id: " + ac.getId());
	    }

	    ObjectEntity site = room.getParent();
	    if (site == null) {
	        throw new ObjectNotFoundException("No site parent for room Id: " + room.getId());
	    }

	    ObjectBoundary tenantBoundary = objectConverter.toBoundary(tenant);

	    Pageable paging = PageRequest.of(0, 1, Direction.DESC, "creationTimestamp", "id");
	    List<ObjectEntity> settingsOfTenantList = this.objectCrud
	            .findByAliasAndActiveTrue("Settings-" + tenantBoundary.getId().getObjectId(), paging);

	    if (settingsOfTenantList.isEmpty()) {
	        throw new ObjectNotFoundException("No Settings for tenant: " + tenant.getId());
	    }

	    ObjectEntity settingsOfTenant = settingsOfTenantList.getFirst();

	    double costPerKwh = ValueParser.toDouble(settingsOfTenant.getObjectDetails().getOrDefault("costPerKwh", 0));
	    double vatRate = ValueParser.toDouble(settingsOfTenant.getObjectDetails().getOrDefault("vatRate", 0)) / 100;
	    double wattsOfDevice = ValueParser.toDouble(ac.getObjectDetails().get("watts"));

	    log.info("costPerKwh: {} " + costPerKwh);
	    log.info("wattsOfDevice: {} " + wattsOfDevice);
	    log.info("vatRate: {} " + vatRate);
	    log.info("startDateTimeStr: {} " + startDateTimeStr);
	    log.info("endDateTimeStr: {} "+ endDateTimeStr);

	    if (wattsOfDevice <= 0 || costPerKwh <= 0 || startDateTimeStr == null || endDateTimeStr == null) {
	        log.warn("Invalid inputs for logging power consumption");
	        return;
	    }

	    OffsetDateTime start = OffsetDateTime.parse(startDateTimeStr, DateUtils.FORMATTER);
	    OffsetDateTime end = OffsetDateTime.parse(endDateTimeStr, DateUtils.FORMATTER);

	    if (end.isBefore(start)) {
	        log.warn("End time is before start time");
	        return;
	    }

	    // Init or get existing logs
	    Map<String, Object> siteDetails = site.getObjectDetails();
	    if (siteDetails == null) {
	        siteDetails = new HashMap<>();
	    }

	    Object powerLogsObj = siteDetails.get("powerConsumptionLogs");
	    Map<String, PowerConsumptionLog> pclMap = new HashMap<>();

	    ObjectMapper mapper = new ObjectMapper();

	    if (powerLogsObj instanceof List) {
	        @SuppressWarnings("unchecked")
	        List<Object> rawList = (List<Object>) powerLogsObj;

	        for (Object raw : rawList) {
	            try {
	                PowerConsumptionLog pcl = mapper.convertValue(raw, PowerConsumptionLog.class);
	                pclMap.put(pcl.getDate(), pcl);
	            } catch (Exception e) {
	                log.warn("Failed to deserialize PowerConsumptionLog entry: {}" +  e.getMessage());
	            }
	        }
	    }

	    OffsetDateTime current = start;
	    while (!current.toLocalDate().isAfter(end.toLocalDate())) {
	        LocalDate day = current.toLocalDate();
	        OffsetDateTime dayStart = day.atStartOfDay().atOffset(start.getOffset());
	        OffsetDateTime dayEnd = dayStart.plusDays(1);

	        OffsetDateTime sliceStart = current.isAfter(dayStart) ? current : dayStart;
	        OffsetDateTime sliceEnd = end.isBefore(dayEnd) ? end : dayEnd;

	        double minutes = (double) Duration.between(sliceStart, sliceEnd).toMinutes();
	        if (minutes <= 0) {
	            current = dayEnd;
	            continue;
	        }

	        double kwh = wattsOfDevice * minutes / 1000;
	        double cost = (kwh * (costPerKwh / 60)) * (1 + vatRate);

	        String dateKey = day.format(DATE_FORMAT);
	        PowerConsumptionLog pcl = pclMap.getOrDefault(dateKey, new PowerConsumptionLog(dateKey, 0, 0, 0));

	        pcl.setRuntime(pcl.getRuntime() + minutes);
	        pcl.setKwh(pcl.getKwh() + kwh);
	        pcl.setCost(pcl.getCost() + cost);

	        pclMap.put(dateKey, pcl);

	        log.info("Added/Updated PowerConsumptionLog for day {}: runtime={} min, kWh={}, cost={}" + dateKey + " " + minutes + " " + kwh + " " + cost);

	        current = dayEnd;
	    }

	    List<PowerConsumptionLog> sortedLogs = new ArrayList<>(pclMap.values());
	    sortedLogs.sort((a, b) -> b.getDate().compareTo(a.getDate())); // newest first

	    siteDetails.put("powerConsumptionLogs", sortedLogs);
	    site.setObjectDetails(siteDetails);

	    objectCrud.save(site);

	    log.info("Power consumption logs successfully saved to site ID: {} " + site.getId());
	}


	@Transactional(readOnly = false)
	private void handleDeleteEntityWithChildren(CommandEntity command, ObjectEntity target) {

		switch (target.getType()) {
		case "Site" -> deleteSite(target);
		case "Room" -> deleteRoom(target);
		case "AirConditioner" -> deleteAC(target);
		case "Task" -> deleteTask(target);
		default -> throw new InvalidRequestInputException("Unknown entity type: " + target.getType());
		}
	}

	// DELETE Entity methods
	private void deleteSite(ObjectEntity site) {

		// Get all Rooms belonging to this Site
		List<ObjectEntity> rooms = site.getChilds();
		if (rooms != null) {
			rooms = rooms.stream().filter(child -> "Room".equalsIgnoreCase(child.getType()))
					.collect(Collectors.toList());

			// Delete all Rooms (which will cascade to ACs and Tasks)
			for (ObjectEntity room : rooms) {
				deleteRoom(room);
			}
		}

		// Soft delete the Site itself
		site.setActive(false);
		objectCrud.save(site);
	}

	private void deleteRoom(ObjectEntity room) {
		// Get all ACs belonging to this Room
		List<ObjectEntity> acs = room.getChilds();
		if (acs != null) {
			acs = acs.stream().filter(child -> "AirConditioner".equalsIgnoreCase(child.getType()))
					.collect(Collectors.toList());

			// Delete all ACs (which will cascade to Tasks)
			for (ObjectEntity ac : acs) {
				deleteAC(ac);
			}
		}

		// Soft delete the Room itself
		room.setActive(false);
		objectCrud.save(room);
	}

	private void deleteAC(ObjectEntity ac) {
		// Get all Tasks belonging to this AC
		List<ObjectEntity> tasks = ac.getChilds();
		if (tasks != null) {
			tasks = tasks.stream().filter(child -> "Task".equalsIgnoreCase(child.getType()))
					.collect(Collectors.toList());

			// Delete all Tasks
			for (ObjectEntity task : tasks) {
				deleteTask(task);
			}
		}

		// Soft delete the AC itself
		ac.setActive(false);
		objectCrud.save(ac);
	}

	private void deleteTask(ObjectEntity task) {
		// Soft delete the Task
		task.setActive(false);
		objectCrud.save(task);
	}

	// DEPRECATED METHODS BELOW

	@Override
	@Deprecated
	public List<CommandBoundary> getAllCommandsHistory() {

		throw new InvalidRequestInputException("This operation is deprecated and will be removed in the near future.");

	}

	@Override
	@Deprecated
	public void deleteAllCommands() {

		throw new InvalidRequestInputException("This operation is deprecated and will be removed in the near future.");

	}

}
