package ambient_intelligence.logic;

import java.util.List;

import ambient_intelligence.logic.boundaries.CommandBoundary;

public interface CommandsService {
	
	public List<Object> invokeCommand(CommandBoundary command);
	
	public void deleteAllCommands(String userSystemID, String userEmail);
	
	
	// DEPRECATED MTHODS BELOW:
	
	@Deprecated
	public List<CommandBoundary> getAllCommandsHistory();
	
	@Deprecated
	public void deleteAllCommands();

}
