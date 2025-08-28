package ambient_intelligence.data;

import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import ambient_intelligence.logic.boundaries.CommandId;
import ambient_intelligence.logic.boundaries.InvokedBy;
import ambient_intelligence.logic.boundaries.TargetObject;
import ambient_intelligence.utils.AirwiseConfig;

@Document(collection = "COMMANDS")
public class CommandEntity {
	
	@Id
	private String id;
	private String command;
	private TargetObject targetObject;
	private String invocationTimestamp;
	private InvokedBy invokedBy;
	private Map<String, Object> commandAttributes;

	public CommandEntity() {
	}

	public String getId() {
		return id;
	}

	public void setId(CommandId id) {
		this.id = AirwiseConfig.getSystemID() + AirwiseConfig.getIdSeparator() + id.getCommandId();
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

}
