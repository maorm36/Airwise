package ambient_intelligence.logic.scheduling;

import ambient_intelligence.data.*;
import ambient_intelligence.dal.ObjectCrud;
import ambient_intelligence.dal.UserCrud;
import ambient_intelligence.logic.boundaries.ACResponse;
import ambient_intelligence.logic.boundaries.ACState;
import ambient_intelligence.logic.boundaries.CreatedBy;
import ambient_intelligence.logic.boundaries.ObjectBoundary;
import ambient_intelligence.logic.boundaries.UserBoundary;
import ambient_intelligence.external_api.RestClientACService;
import ambient_intelligence.logic.boundaries.UserId;
import ambient_intelligence.logic.ObjectsServiceImpl;
import ambient_intelligence.logic.converters.ObjectConverter;
import ambient_intelligence.logic.converters.UserConverter;
import ambient_intelligence.utils.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class InHomeSecurityMonitor {

	private static final String SYSTEM_OPERATOR_EMAIL = "SystemOperator@airwise.com";


    private final ObjectCrud objectCrud;
    private final UserCrud userCrud;
    private final RestClientACService externalAcRestClient;
    private final ObjectsServiceImpl objectsService;
    private final EmailService emailService;
    private final UserConverter userConverter;
    private final ObjectConverter objectConverter;
    private Log log = LogFactory.getLog(InHomeSecurityMonitor.class);
    
    public InHomeSecurityMonitor(ObjectCrud objectCrud,
                                  UserCrud userCrud,
                                  RestClientACService externalAcRestClient,
                                  ObjectsServiceImpl objectsService,
                                  EmailService emailService, UserConverter userConverter,
                                  ObjectConverter objectConverter ) {
        this.objectCrud = objectCrud;
        this.userCrud = userCrud;
        this.externalAcRestClient = externalAcRestClient;
        this.objectsService = objectsService;
        this.emailService = emailService;
        this.userConverter = userConverter;
        this.objectConverter = objectConverter;
    }

    @Scheduled(cron = "0 * * * * *") // every minute
    public void monitorInHomeSecurity() {
    	
    	log.info("monitorInHomeSecurity start");
    	
        List<UserEntity> endUsers = userCrud.findAll();
        
        for (UserEntity user : endUsers) {
        	
        	if(!user.getRole().toString().equals(UserRole.END_USER.toString()))
        		continue;
        	
        	
        	UserBoundary userBoundary = this.userConverter.toBoundary(user);
        	
            String userEmail = userBoundary.getUserId().getEmail();
            
            Pageable paging = PageRequest.of(0, 1, Direction.DESC, "creationTimestamp", "id");
            List<ObjectEntity> tenants = objectCrud.findByAliasAndActiveTrue(userEmail, paging);
            
            if(tenants == null || tenants.isEmpty()) continue;
                        
            ObjectEntity tenant = tenants.getFirst();
            
            if (tenant == null || !tenant.getType().equals("Tenant")) continue;
                      
            List<ObjectEntity> sites = this.objectCrud.findAllByParent_IdAndActiveTrue(tenant.getId(), Pageable.unpaged());
            if(sites != null && !sites.isEmpty()) {
                for (ObjectEntity site : sites) {
                	
                	  if(site.getObjectDetails() != null) {                		  
                		  boolean inSite = ValueParser.toBoolean(site.getObjectDetails().get("inSite"));
                		  if(inSite) continue;
                		  
                		  List<ObjectEntity> rooms = this.objectCrud.findAllByParent_IdAndActiveTrue(site.getId(), Pageable.unpaged());
                		  if(rooms != null && !rooms.isEmpty()) {
                			                  			  
                			  for (ObjectEntity room : rooms) {
                				  
                				  List<ObjectEntity> acs = this.objectCrud.findAllByParent_IdAndActiveTrue(room.getId(), Pageable.unpaged());
                				  if(acs != null && !acs.isEmpty()) {
	                				  for (ObjectEntity ac : acs) {
	                					  if ("AirConditioner".equalsIgnoreCase(ac.getType())) {
	                						  this.checkMotionDetection(userBoundary, site, ac);
	                					  }
	                				  }
                				  }
                			  }
                			  
                		  }
                		  
                	  }
                }
            }
            
            log.info("monitorInHomeSecurity ends");
        }
    }
    
    
    private void checkMotionDetection(UserBoundary userBoundary, ObjectEntity site, ObjectEntity ac) {
    	try {
        	
    		log.info("send request api for ac: " + ac.getAlias());
        	
        	ACResponse response = externalAcRestClient.getACStateBySerial(ac.getAlias());
        	
        	log.info("response of api : " + response.getCode());
        	
            if (response != null && response.getCode() >= 200 && response.getCode() < 300) {
            	
            	ACState state = response.getAcState();
            	
            	log.info("state of ac : " + state.toString());
            	
            	               		
        		if(state.isMotion()) {            
        			log.info("create security notfication: " + userBoundary.getUserId().getEmail());
        			this.createNotification(userBoundary.getUserId(),
        					"Security Alert: Motion Detected",
        					"Motion detected in your Site: " + site.getAlias() + ", while marked as 'Away'. Please check immediately.");
        		}
            	
            }
            
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }


    @Transactional
    private void createNotification(UserId userId, String title, String message) {
    	
    	String tenantEmail = userId.getEmail();
		
    	Pageable pagingTenant = PageRequest.of(0, 1, Direction.DESC, "creationTimestamp", "id");
		List<ObjectEntity> tenants = this.objectCrud.findByAliasAndActiveTrue(tenantEmail, pagingTenant);
		if(tenants == null || tenants.isEmpty()) {
			return;
		}
		
		ObjectEntity tenant = tenants.getFirst();
		
        ObjectBoundary tenantBnd = this.objectConverter.toBoundary(tenant);
		
		Pageable pagingNotifs = PageRequest.of(0, 1, Direction.DESC, "creationTimestamp", "id");
		List<ObjectEntity> notifs = this.objectCrud.findByAliasAndActiveTrue("alert-notification-"+tenantBnd.getId().getObjectId(), pagingNotifs);
		if(notifs != null && !notifs.isEmpty()) {
			ObjectEntity notf = notifs.getFirst();	
			long minutesDiff = DateUtils.getMinutesDiffFromNow(notf.getCreationTimestamp());
			if(minutesDiff < 30) return;
		}
		
        ObjectBoundary notification = new ObjectBoundary();
        notification.setType("Notification");
        notification.setAlias("alert-notification-"+tenantBnd.getId().getObjectId());
        notification.setStatus("warning");
        notification.setActive(true);

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
        notification.setCreatedBy(createdBy);

        Map<String, Object> details = new HashMap<>();
        details.put("title", title);
        details.put("message", message);
        notification.setObjectDetails(details);

        notification = this.objectsService.create(notification);
        
        try {
        	this.log.info("trying to send email of security alert");
            this.emailService.sendEmail(
                    new EmailRequest("noreply@airwise.com", tenantEmail, title, message));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
