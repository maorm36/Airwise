package ambient_intelligence.logic;

import java.util.List;
import java.util.Optional;

import ambient_intelligence.logic.boundaries.UserBoundary;

public interface UsersService {
	
	public UserBoundary createUser(UserBoundary user);

	public void updateUser(String systemID, String userEmail, UserBoundary update);
	
	public Optional<UserBoundary> login(String systemID, String userEmail);
	
	public void deleteAllUsers(String userSystemID, String userEmail);
	
	
	// DEPRECATED METHODS BELOW
	
	@Deprecated
	public List<UserBoundary> getAllUsers();
	
	@Deprecated
	public void deleteAllUsers();
	
}
