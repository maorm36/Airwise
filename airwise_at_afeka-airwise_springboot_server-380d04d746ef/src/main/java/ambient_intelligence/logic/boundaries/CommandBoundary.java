package ambient_intelligence.logic.boundaries;

import java.util.Map;

public class CommandBoundary {
	
	private CommandId id;
	
	private String command;
	
	private TargetObject targetObject;
	
	private String invocationTimestamp;
	
	private InvokedBy invokedBy;
	
	private Map<String, Object> commandAttributes;

	
	public CommandBoundary() {
	}

	public CommandId getId() {
		return id;
	}

	
	public void setId(CommandId id) {
		this.id = id;
	}

	
	public String getCommand() {
		return command;
	}

	
	public void setCommand(String command) {
		this.command = command;
	}

	
	public TargetObject getTargetObject() {
		return targetObject;
	}

	
	public void setTargetObject(TargetObject targetObject) {
		this.targetObject = targetObject;
	}

	
	public String getInvocationTimestamp() {
		return invocationTimestamp;
	}

	
	public void setInvocationTimestamp(String invocationTimestamp) {
		this.invocationTimestamp = invocationTimestamp;
	}

	
	public InvokedBy getInvokedBy() {
		return invokedBy;
	}

	
	public void setInvokedBy(InvokedBy invokedBy) {
		this.invokedBy = invokedBy;
	}

	
	public Map<String, Object> getCommandAttributes() {
		return commandAttributes;
	}

	
	public void setCommandAttributes(Map<String, Object> commandAttributes) {
		this.commandAttributes = commandAttributes;
	}

	
	public String toString() {
		return "CommandBoundary [id=" + id + ", command=" + command + ", targetObject=" + targetObject
				+ ", invocationTimestamp=" + invocationTimestamp + ", invokedBy=" + invokedBy + ", commandAttributes="
				+ commandAttributes + "]";
	}
}
