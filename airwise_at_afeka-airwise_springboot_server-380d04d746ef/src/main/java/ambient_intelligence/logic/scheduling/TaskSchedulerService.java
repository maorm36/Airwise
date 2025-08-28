package ambient_intelligence.logic.scheduling;

import ambient_intelligence.data.*;
import ambient_intelligence.dal.ObjectCrud;
import ambient_intelligence.dal.UserCrud;
import ambient_intelligence.logic.CommandsServiceImpl;
import ambient_intelligence.logic.boundaries.InvokedBy;
import ambient_intelligence.logic.boundaries.ObjectId;
import ambient_intelligence.logic.boundaries.TargetObject;
import ambient_intelligence.logic.exceptions.InvalidRequestInputException;
import ambient_intelligence.logic.exceptions.ObjectNotFoundException;
import ambient_intelligence.utils.AirwiseConfig;
import ambient_intelligence.utils.DateUtils;
import ambient_intelligence.utils.ValueParser;

import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class TaskSchedulerService {

    private final ObjectCrud objectCrud;
    private final CommandsServiceImpl commandsService;
    private final UserCrud userCrud;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public TaskSchedulerService(ObjectCrud objectCrud, CommandsServiceImpl commandsService, UserCrud userCrud) {
        this.objectCrud = objectCrud;
        this.commandsService = commandsService;
        this.userCrud = userCrud;
    }

    @Scheduled(cron = "0 * * * * *") // every minute
    public void runScheduledTasks() {
    	List<ObjectEntity> allTasks = this.objectCrud.findByTypeAndStatusAndActiveTrue("Task", "SCHEDULED", Pageable.unpaged());

        for (ObjectEntity task : allTasks) {
            Map<String, Object> details = task.getObjectDetails();
            if (details == null || !details.containsKey("startTime") || !details.containsKey("action"))
                continue;
            
            String action = ValueParser.toString(details.get("action"));
            if(action.equals(ActionType.TURN_ON.toString()) && !details.containsKey("endTime")) {
            	continue;
            }

            String repeatStr = String.valueOf(details.get("repeat"));
            RepeatPattern repeatPattern;
            try {
                repeatPattern = RepeatPattern.fromString(repeatStr);
            } catch (Exception e) {
                continue; // unknown pattern
            }

            
            LocalTime now = LocalTime.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
            boolean isEndTime = false;
            
            // parse scheduled time
            if(action.equals(ActionType.TURN_ON.toString())) {   
            	
            	// Here is a TURN_ON action, so we need start time AND end time
            	String startTimeStr = String.valueOf(details.get("startTime"));
            	String endTimeStr = String.valueOf(details.get("endTime"));

                LocalTime scheduledStartTime;
                try {
                	scheduledStartTime = LocalTime.parse(startTimeStr, TIME_FORMAT);
                } catch (Exception e) {
                    continue; // invalid time format
                }
                
                LocalTime scheduledEndTime;
                try {
                	scheduledEndTime = LocalTime.parse(endTimeStr, TIME_FORMAT);
                } catch (Exception e) {
                    continue; // invalid time format
                }
                
            	
                if (!now.equals(scheduledStartTime.truncatedTo(java.time.temporal.ChronoUnit.MINUTES))
                		&& !now.equals(scheduledEndTime.truncatedTo(java.time.temporal.ChronoUnit.MINUTES))) {                	
                	continue;
                }
                
                if(now.equals(scheduledEndTime.truncatedTo(java.time.temporal.ChronoUnit.MINUTES))) {
                	isEndTime = true;
                }

            } else {
            	
            	// Here is a TURN_OFF action, so we need just start time
            	String startTimeStr = String.valueOf(details.get("startTime"));

                LocalTime scheduledTime;
                try {
                    scheduledTime = LocalTime.parse(startTimeStr, TIME_FORMAT);
                } catch (Exception e) {
                    continue; // invalid time format
                }
                
            	
                if (!now.equals(scheduledTime.truncatedTo(java.time.temporal.ChronoUnit.MINUTES)))
                    continue;
            }
            
            if (!shouldRunToday(repeatStr))
                continue;

            try {
                runTask(task, repeatPattern, isEndTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }
    }
    
    @Scheduled(cron = "0 0 0 * * *") // Runs at midnight every day
    public void resetRecurringTaskStatuses() {
        List<ObjectEntity> executedTasks = objectCrud
                .findByTypeAndStatusAndActiveTrue("Task", "EXECUTED", Pageable.unpaged());

        for (ObjectEntity task : executedTasks) {
            Map<String, Object> details = task.getObjectDetails();
            if (details == null || !details.containsKey("repeat")) continue;

            String repeatStr = String.valueOf(details.get("repeat"));

            RepeatPattern repeatPattern;
            try {
                repeatPattern = RepeatPattern.fromString(repeatStr);
            } catch (IllegalArgumentException e) {
                continue;
            }

            // Only reset recurring patterns
            if ( repeatPattern != RepeatPattern.ONCE ) {
                task.setStatus("SCHEDULED");
                objectCrud.save(task);
            }
        }
    }


    private boolean shouldRunToday(String repeat) {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        return switch (RepeatPattern.fromString(repeat)) {
            case ONCE -> true;
            case EVERY_DAY -> true;
            case EVERY_WEEKDAY -> (dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.FRIDAY);
            case WEEKENDS -> (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);
        };
    }

    private void runTask(ObjectEntity task, RepeatPattern repeatPattern, boolean endTime) {
    	
        ObjectEntity ac = task.getParent();
        
        if (ac == null || !"AirConditioner".equalsIgnoreCase(ac.getType())) {
            throw new InvalidRequestInputException("Scheduled task must be linked to an AirConditioner.");
        }

        Map<String, Object> prefs = task.getObjectDetails();
        if (prefs == null) return;

        String actionStr = ValueParser.toString(prefs.get("action"));
        boolean power = "TURN_ON".equalsIgnoreCase(actionStr);
        if(power && endTime) {
        	power = false;
        }

        double temperature = ValueParser.toDouble(prefs.get("temperature"));
        String mode = ValueParser.toString(prefs.get("mode"));
        String fanSpeed = ValueParser.toString(prefs.get("fanSpeed"));

        Map<String, Object> attrs = Map.of(
                "power", power,
                "temperature", temperature,
                "mode", mode,
                "fanSpeed", fanSpeed
        );

        // Walk up the hierarchy to find the Tenant (User entity is linked to tenant alias)
        ObjectEntity room = ac.getParent();
        ObjectEntity site = room != null ? room.getParent() : null;
        ObjectEntity tenant = site != null ? site.getParent() : null;

        if (tenant == null || tenant.getCreatedBy() == null) {
            throw new InvalidRequestInputException("Tenant information is missing for the scheduled task.");
        }

        String userId = AirwiseConfig.getSystemID() + AirwiseConfig.getIdSeparator() + tenant.getAlias();
        UserEntity user = this.userCrud.findById(userId).orElseThrow(() ->
        	new ObjectNotFoundException("User not found for email: " + tenant.getAlias())
        );
        
        
        CommandEntity fakeCmd = new CommandEntity();
        fakeCmd.setCommand("UPDATE_AC_STATE");
        fakeCmd.setCommandAttributes(new HashMap<>(attrs));
        fakeCmd.setTargetObject(new TargetObject(new ObjectId(ac.getId(), AirwiseConfig.getSystemID())));
        fakeCmd.setInvocationTimestamp(DateUtils.getCurrentFormattedDate());
        InvokedBy invkBy = new InvokedBy();
        invkBy.setUserId(tenant.getCreatedBy().getUserId());
        fakeCmd.setInvokedBy(invkBy);

        this.commandsService.updateAcStateViaCommand(fakeCmd, ac);
         if(!power) {
        	 task.setStatus("EXECUTED");        	 
         }
         task.getObjectDetails().put("lastExecution", DateUtils.getCurrentFormattedDate());
	
	     objectCrud.save(task);
    }
}
