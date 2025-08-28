import { ObjectBoundary } from './ObjectBoundary';
import { AcConstants } from '../Components/Utils/AcConstants';
import { AcState } from './AcState';
import { ScheduleConstants } from '../Components/Utils/ScheduleConstants';

export class TaskBoundary extends ObjectBoundary {

  constructor({
    name,
    action,
    startTime,
    endTime,
    repeat,
    useCurrentPreferences,
    selectedAC,
    temp,
    mode,
    fanSpeed,
    alias,
    active = true,
    createdBy,
    objectId = null,
    status = ScheduleConstants.STATUS.ACTIVE,
    tenantId = null
  }) {

    TaskBoundary.validateRequiredFields({ name, action, startTime, selectedAC });

    const objectDetails = TaskBoundary.buildObjectDetails({
      name,
      action,
      startTime,
      endTime,
      repeat,
      useCurrentPreferences,
      selectedAC,
      temp,
      mode,
      fanSpeed
    });

    // Use provided alias or generate from tenantId
    const taskAlias = alias || (tenantId ? `tasks-${tenantId}` : name);

    super({
      type: 'Task',
      alias: taskAlias,
      active,
      createdBy: createdBy || selectedAC?.createdBy,
      objectDetails,
      objectId,
      status,
    });

    this.selectedAC = selectedAC;
  }

  static validateRequiredFields({ name, action, startTime, selectedAC }) {
    if (!name) throw new Error('Task name is required');
    if (!action) throw new Error('Task action is required');
    if (!startTime) throw new Error('Start time is required');
    if (!selectedAC) throw new Error('Selected AC is required');
    if (!selectedAC.id) throw new Error('Selected AC must have an ID');
  }

  static buildObjectDetails({
    name,
    action,
    startTime,
    endTime,
    repeat,
    useCurrentPreferences,
    selectedAC,
    temp,
    mode,
    fanSpeed
  }) {
    const details = {
      name,
      action,
      startTime,
      endTime,
      repeat,
      acId: selectedAC.id.objectId,
      useCurrentPreferences,
      targetAC: {
        systemID: selectedAC.id.systemID,
        objectId: selectedAC.id.objectId,
        alias: selectedAC.alias
      }
    };

    // Add AC state only if action is not TURN_OFF
    // Remove redundancy - only store in acState
    if (action !== AcConstants.ACTIONS.TURN_OFF) {
      details.acState = new AcState({
        serial: selectedAC.serial,
        power: true,
        temperature: temp,
        mode: mode,
        fanSpeed: fanSpeed,
        manufacturer: selectedAC.manufacturer || 'Unknown',
        motion: selectedAC.motion || false
      });
    }

    return details;
  }

  // Method to get task details for scheduling API
  getTaskDetails() {
    const details = {
      taskName: this.objectDetails.name, // Server expects 'taskName' not 'name'
      action: this.objectDetails.action,
      startTime: this.objectDetails.startTime,
      repeat: this.objectDetails.repeat,
      useCurrentPreferences: this.objectDetails.useCurrentPreferences
    };

    // Only add endTime if it exists
    if (this.objectDetails.endTime) {
      details.endTime = this.objectDetails.endTime;
    }

    // Add AC preferences directly if not using current preferences
    if (!this.objectDetails.useCurrentPreferences && this.objectDetails.acState) {
      details.temperature = this.objectDetails.acState.temperature;
      details.mode = this.objectDetails.acState.mode;
      details.fanSpeed = this.objectDetails.acState.fanSpeed;
    }

    return details;
  }

  getTaskData() {
    return {
      details: {
        ...this.objectDetails
      }
    };
  }

  getCreateTaskPayload(operator, tenant) {
    return {
      operatorSystemID: operator.userId.systemID,
      operatorEmail: operator.userId.email,
      task: this.getTaskData(),
      tenantId: tenant.id.objectId
    };
  }

  getScheduleTaskPayload(user, createdTaskId) {
    return {
      acSystemID: createdTaskId.systemID,
      acObjectId: createdTaskId.objectId,
      taskDetails: this.getTaskDetails(),
      operatorSystemID: user.userId.systemID,
      operatorEmail: user.userId.email
    };
  }

  static fromFormData(formData, selectedAC, user, tenant = null) {
    return new TaskBoundary({
      name: formData.taskName,
      action: formData.action,
      startTime: formData.startTime,
      endTime: formData.endTime,
      repeat: formData.repeat,
      useCurrentPreferences: formData.useCurrentPreferences,
      selectedAC: selectedAC,
      temp: formData.temp,
      mode: formData.mode,
      fanSpeed: formData.fanSpeed,
      createdBy: user,
      active: true,
      tenantId: tenant?.id?.objectId
    });
  }

  // Getters for easy access
  get taskName() {
    return this.objectDetails.name;
  }

  get taskAction() {
    return this.objectDetails.action;
  }

  get targetAC() {
    return this.objectDetails.targetAC;
  }

  get startTime() {
    return this.objectDetails.startTime;
  }

  get endTime() {
    return this.objectDetails.endTime;
  }

  get repeat() {
    return this.objectDetails.repeat;
  }

  get acPreferences() {
    return this.objectDetails.acState;
  }

  updateDetails(updates) {
    this.objectDetails = {
      ...this.objectDetails,
      ...updates,
    };
  }
}