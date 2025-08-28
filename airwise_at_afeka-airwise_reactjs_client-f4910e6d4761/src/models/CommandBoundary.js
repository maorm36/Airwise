export class CommandBoundary {

    constructor({ commandId, command, targetObject, invocationTimestamp, invokedBy, commandAttributes }) {
      this.commandId = commandId;
      this.command = command;
      this.targetObject = targetObject;
      this.invocationTimestamp = invocationTimestamp;
      this.invokedBy = invokedBy;
      this.commandAttributes = commandAttributes;
    }
  }
  