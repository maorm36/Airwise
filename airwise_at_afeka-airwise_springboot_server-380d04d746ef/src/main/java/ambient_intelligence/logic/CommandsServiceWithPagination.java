package ambient_intelligence.logic;

import java.util.List;

import ambient_intelligence.logic.boundaries.CommandBoundary;

public interface CommandsServiceWithPagination extends CommandsService {
	
	public List<CommandBoundary> getAllCommandsHistory(String userSystemID, String userEmail, int size, int page);
	
}
