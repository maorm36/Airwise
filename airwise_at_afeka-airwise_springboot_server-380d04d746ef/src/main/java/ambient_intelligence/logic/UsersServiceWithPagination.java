package ambient_intelligence.logic;

import java.util.List;

import ambient_intelligence.logic.boundaries.UserBoundary;

public interface UsersServiceWithPagination extends UsersService {
	
	public List<UserBoundary> getAllUsers(String userSystemID, String userEmail, int size, int page);

}
