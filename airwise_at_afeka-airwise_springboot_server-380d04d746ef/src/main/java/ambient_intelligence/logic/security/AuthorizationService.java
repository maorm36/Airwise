package ambient_intelligence.logic.security;


import java.util.Arrays;
import java.util.Optional;

import org.springframework.stereotype.Service;

import ambient_intelligence.dal.UserCrud;
import ambient_intelligence.data.UserEntity;
import ambient_intelligence.data.UserRole;
import ambient_intelligence.logic.exceptions.InvalidRequestInputException;
import ambient_intelligence.logic.exceptions.UnauthorizedException;
import ambient_intelligence.utils.AirWiseValidator;
import ambient_intelligence.utils.AirwiseConfig;

@Service
public class AuthorizationService {

    private final AirWiseValidator validator;
    private final UserCrud userCrud;

    public AuthorizationService(AirWiseValidator validator,
    		UserCrud userCrud) {
        this.validator = validator;
        this.userCrud = userCrud;
    }

    
    public boolean ensureRole(String systemId,
                           String email,
                           UserRole... allowedRoles) {

        if (!validator.isValidSystemId(systemId)) {
            throw new InvalidRequestInputException("Invalid input – systemID is invalid.");
        }
        if (!validator.isValidEmail(email)) {
            throw new InvalidRequestInputException("Invalid input – email is invalid.");
        }

        String userId = systemId + AirwiseConfig.getIdSeparator() + email;
        Optional<UserEntity> user = userCrud.findById(userId);
            
        if(!user.isPresent()) {
        	throw new UnauthorizedException("Unauthorized action.");
        }

       return Arrays.stream(allowedRoles)
                                .anyMatch(r -> r == user.get().getRole());
    }
}
