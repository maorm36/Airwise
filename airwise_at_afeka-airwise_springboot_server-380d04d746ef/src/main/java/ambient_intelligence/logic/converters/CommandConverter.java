package ambient_intelligence.logic.converters;

import org.springframework.stereotype.Component;

import ambient_intelligence.data.CommandEntity;
import ambient_intelligence.logic.boundaries.CommandBoundary;
import ambient_intelligence.logic.boundaries.CommandId;
import ambient_intelligence.utils.AirwiseConfig;

@Component
public class CommandConverter {

	public CommandBoundary toBoundary(CommandEntity entity) {

		CommandBoundary rv = new CommandBoundary();

		CommandId commandId = new CommandId();
		String systemID = entity.getId().split(AirwiseConfig.getIdSeparator())[0];
		String id = entity.getId().split(AirwiseConfig.getIdSeparator())[1];

		commandId.setSystemID(systemID);
		commandId.setCommandId(id);

		rv.setId(commandId);
		rv.setCommand(entity.getCommand());
		rv.setInvocationTimestamp(entity.getInvocationTimestamp());
		rv.setCommandAttributes(entity.getCommandAttributes());
		rv.setInvokedBy(entity.getInvokedBy());
		rv.setTargetObject(entity.getTargetObject());

		return rv;
	}

	public CommandEntity toEntity(CommandBoundary boundary) {
		CommandEntity rv = new CommandEntity();
		
		rv.setId(boundary.getId());
		rv.setCommand(boundary.getCommand());
		rv.setInvocationTimestamp(boundary.getInvocationTimestamp());
		rv.setCommandAttributes(boundary.getCommandAttributes());
		rv.setInvokedBy(boundary.getInvokedBy());
		rv.setTargetObject(boundary.getTargetObject());

		return rv;
	}

}
