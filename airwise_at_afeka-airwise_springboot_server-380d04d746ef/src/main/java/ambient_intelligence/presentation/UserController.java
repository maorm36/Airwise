package ambient_intelligence.presentation;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import ambient_intelligence.logic.UsersService;
import ambient_intelligence.logic.boundaries.NewUserBoundary;
import ambient_intelligence.logic.boundaries.UserBoundary;
import ambient_intelligence.logic.exceptions.ForbiddenException;
import ambient_intelligence.utils.AirwiseConfig;

@RestController
@RequestMapping(path = {"/ambient-intelligence/users"})
public class UserController {

	private final UsersService userService;

	public UserController(UsersService userService) {
		this.userService = userService;
	}	
	    
    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    public UserBoundary createUser(@RequestBody NewUserBoundary newUser) {
    	
    	UserBoundary userBoundary = new UserBoundary(newUser, AirwiseConfig.getSystemID());
    	
        return this.userService.createUser(userBoundary);
        
    }

    @GetMapping(path = {"/login/{systemID}/{userEmail}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public UserBoundary loginUser(
            @PathVariable("systemID") String systemID,
            @PathVariable("userEmail") String userEmail) {

    	return this.userService.login(systemID, userEmail)
    			.orElseThrow(()-> 
    				new ForbiddenException("Login FAILED.")
    			);
    }
    
    @PutMapping(path = {"/{systemID}/{userEmail}"}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    public void updateUser( @PathVariable("systemID") String systemID,
    		 @PathVariable("userEmail") String userEmail,
    		@RequestBody UserBoundary updatedUser) {
		
    	this.userService.updateUser(systemID, userEmail, updatedUser);
    	
    }
}
