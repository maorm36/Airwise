package ambient_intelligence.utils;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import ambient_intelligence.data.AcMode;
import ambient_intelligence.data.ActionType;
import ambient_intelligence.data.FanSpeed;
import ambient_intelligence.data.RepeatPattern;
import ambient_intelligence.data.UserRole;
import ambient_intelligence.logic.boundaries.CommandBoundary;
import ambient_intelligence.logic.boundaries.ObjectBoundary;
import ambient_intelligence.logic.boundaries.ObjectId;
import ambient_intelligence.logic.boundaries.UserId;
import ambient_intelligence.logic.exceptions.InvalidRequestInputException;

@Component
public class AirWiseValidator {

	public AirWiseValidator() {
	}

	public boolean isValidSystemId(String systemID) {
		if (systemID == null || systemID.isBlank() || !systemID.equals(AirwiseConfig.getSystemID())) {
			return false;
		}

		return true;
	}

	public boolean checkValidObjectId(ObjectId objectId) {

		if (objectId.getSystemID() == null || objectId.getSystemID().isBlank()
				|| !objectId.getSystemID().equals(AirwiseConfig.getSystemID()) || objectId.getObjectId() == null
				|| objectId.getObjectId().isBlank()) {

			return false;

		}

		return true;
	}

	public boolean checkValidUserId(UserId userId) {

		if (userId.getSystemID() == null || userId.getSystemID().isBlank()
				|| !userId.getSystemID().equals(AirwiseConfig.getSystemID()) || userId.getEmail() == null
				|| userId.getEmail().isBlank() || !this.isValidEmail(userId.getEmail())) {
			return false;
		}

		return true;
	}

	public boolean isValidRole(String role) {

		if (role == null || role.isBlank()) {
			return false;
		}

		return UserRole.ADMIN.toString() == role || UserRole.OPERATOR.toString() == role
				|| UserRole.END_USER.toString() == role;

	}

	public boolean isValidEmail(String email) {

		Pattern emailPattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

		if (email == null || email.isBlank()) {
			return false;
		}

		return emailPattern.matcher(email).matches();
	}

	public void isValidCommandRequest(CommandBoundary command) throws Exception {

		if (!this.checkValidObjectId(command.getTargetObject().getId())) {
			throw new InvalidRequestInputException("Invalid input - TargetObject is invalid");
		}

		if (!this.checkValidUserId(command.getInvokedBy().getUserId())) {
			throw new InvalidRequestInputException("Invalid input - InvokedBy is invalid");
		}

		if (command.getCommand() == null || command.getCommand().isBlank()) {
			throw new InvalidRequestInputException("Invalid input - command is invalid");
		}

	}

	public void isValidObjBoundaryRequest(ObjectBoundary objectBoundary) throws Exception {
		if (objectBoundary == null) {
			throw new InvalidRequestInputException("Invalid input - ObjectBoundary cannot be null");
		}

		if (objectBoundary.getAlias() == null || objectBoundary.getAlias().isBlank()) {
			throw new InvalidRequestInputException("Invalid input - ObjectBoundary's Alias cannot be null");
		}

		if (objectBoundary.getStatus() == null || objectBoundary.getStatus().isBlank()) {
			throw new InvalidRequestInputException("Invalid input - ObjectBoundary's Status cannot be null");
		}

		if (objectBoundary.getType() == null || objectBoundary.getType().isBlank()) {
			throw new InvalidRequestInputException("Invalid input - ObjectBoundary's Type cannot be null");
		}

		if (objectBoundary.getCreatedBy() == null
				|| !this.checkValidUserId(objectBoundary.getCreatedBy().getUserId())) {
			throw new InvalidRequestInputException("Invalid input - ObjectBoundary's CreatedBy cannot be null");
		}

	}
	
	
	public void isValidPaginationInputs(int size, int page) {
		if(size <= 0) {			
			throw new InvalidRequestInputException("Invalid input - size param is invalid");
		}
		
		if(page < 0) {
			throw new InvalidRequestInputException("Invalid input - page param is invalid");
		}
	}
	
	public void checkValidAcUpdateStateRequest(Map<String, Object> attrs) {
	    
	    if (!attrs.containsKey("power") ||
	        !attrs.containsKey("temperature") ||
	        !attrs.containsKey("mode") || 
	        !attrs.containsKey("fanSpeed")) {
	        throw new InvalidRequestInputException("Fields required : [power, temperature, mode, fanSpeed] must be provided.");
	    }

        Object power = attrs.get("power");
        if (!(power instanceof Boolean)) {
            throw new InvalidRequestInputException("Invalid value for 'power': expected boolean.");
        }

        double temperature = ValueParser.toDouble(attrs.get("temperature"));
        if (temperature < 16 || temperature > 30) {
            throw new InvalidRequestInputException("Temperature must be between 16 and 30.");
        }

        Object mode = attrs.get("mode");
        if (!(mode instanceof String)) {
            throw new InvalidRequestInputException("Invalid value for 'mode': expected string.");
        }
        
        try {
            AcMode.valueOf(((String) mode).toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestInputException("Invalid AC mode: " + mode);
        }
    

    
        Object fanSpeed = attrs.get("fanSpeed");
        if (!(fanSpeed instanceof String)) {
            throw new InvalidRequestInputException("Invalid value for 'fanSpeed': expected string.");
        }
        
        try {
            FanSpeed.valueOf(((String) fanSpeed).toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestInputException("Invalid fan speed: " + fanSpeed);
        }
	    
	}
	
	public void validateScheduleTaskAttributes(Map<String, Object> attrs) {
		
	    // === Required base fields ===
	    if (attrs.get("taskName") == null ||
	        attrs.get("action") == null ||
	        attrs.get("startTime") == null ||
	        attrs.get("repeat") == null) {
	        throw new InvalidRequestInputException("Missing required fields: taskName, action, startTime, repeat.");
	    }
	    
	    // === Parse and validate action ===
	    Object actionObj = attrs.get("action");
	    if (!(actionObj instanceof String)) {
	        throw new InvalidRequestInputException("Action must be a string.");
	    }
	    String action = ((String) actionObj).toUpperCase();
	    try {
	        ActionType.valueOf(action);
	    } catch (IllegalArgumentException e) {
	        throw new InvalidRequestInputException("Invalid action: " + action + ". Must be one of: " + Arrays.toString(ActionType.values()));
	    }

	    // === Parse and validate repeat ===
	    Object repeatObj = attrs.get("repeat");
	    if (!(repeatObj instanceof String)) {
	        throw new InvalidRequestInputException("Repeat must be a string.");
	    }
	    String repeat = ((String) repeatObj).toUpperCase();
	    try {
	        RepeatPattern.valueOf(repeat);
	    } catch (IllegalArgumentException e) {
	        throw new InvalidRequestInputException("Invalid repeat pattern: " + repeat + ". Must be one of: " + Arrays.toString(RepeatPattern.values()));
	    }
	
	    // === Time validation ===
	    String startTimeStr = String.valueOf(attrs.get("startTime"));
	    String endTimeStr = attrs.get("endTime") != null ? String.valueOf(attrs.get("endTime")) : null;
	
	    LocalTime startTime;
	    try {
	        startTime = LocalTime.parse(startTimeStr);
	    } catch (Exception e) {
	        throw new InvalidRequestInputException("Invalid format for startTime. Expected HH:mm.");
	    }
	
	    if (action.equals("TURN_ON")) {
	        LocalTime endTime;
	        try {
	            endTime = LocalTime.parse(endTimeStr);
	        } catch (Exception e) {
	            throw new InvalidRequestInputException("Invalid format for endTime. Expected HH:mm.");
	        }
	
	        if (!endTime.isAfter(startTime)) {
	            throw new InvalidRequestInputException("End time must be after start time.");
	        }
	    }
	
	    // === AC preferences check ===
	    Boolean useCurrentPrefs = (Boolean) attrs.getOrDefault("useCurrentPreferences", true);
	    if (!useCurrentPrefs) {
	        if (attrs.get("temperature") == null ||
	            attrs.get("mode") == null ||
	            attrs.get("fanSpeed") == null) {
	            throw new InvalidRequestInputException("Missing custom preferences: temperature, mode, fanSpeed.");
	        }
	
	        double temp = ValueParser.toDouble(attrs.get("temperature"));
	        if (temp < 16 || temp > 30) {
	            throw new InvalidRequestInputException("Temperature must be a number between 16 and 30.");
	        }
	
	        Object mode = attrs.get("mode");

	        if (!(mode instanceof String)) {
	            throw new InvalidRequestInputException("Mode must be a string.");
	        }

	        try {
	            AcMode.valueOf(((String) mode).toUpperCase());
	        } catch (IllegalArgumentException e) {
	            throw new InvalidRequestInputException(
	                "Mode must be one of: " + Arrays.toString(AcMode.values())
	            );
	        }
	
	        Object fanSpeed = attrs.get("fanSpeed");

	        if (!(fanSpeed instanceof String)) {
	            throw new InvalidRequestInputException("FanSpeed must be a string.");
	        }

	        try {
	            FanSpeed.valueOf(((String) fanSpeed).toUpperCase());
	        } catch (IllegalArgumentException e) {
	            throw new InvalidRequestInputException(
	                "FanSpeed must be one of: " + Arrays.toString(FanSpeed.values())
	            );
	        }
	    }
	}



}
