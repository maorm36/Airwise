export class ObjectBoundary {
  constructor({ type, alias, active, createdBy, objectDetails, objectId = null }) {
    this.type = type;
    this.alias = alias;
    this.active = active;
    this.createdBy = createdBy;
    this.objectDetails = objectDetails;
    this.objectId = objectId;
  }
}