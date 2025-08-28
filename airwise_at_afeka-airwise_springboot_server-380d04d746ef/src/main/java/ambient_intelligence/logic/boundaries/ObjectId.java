package ambient_intelligence.logic.boundaries;


public class ObjectId {

	private String objectId;
	private String systemID;

	public ObjectId() {
	}

	public ObjectId(String objectId, String systemID) {
		this.objectId = objectId;
		this.systemID = systemID;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public String getSystemID() {
		return systemID;
	}

	public void setSystemID(String systemID) {
		this.systemID = systemID;
	}

	@Override
	public String toString() {
		return "ObjectId [Id=" + objectId + ", systemID=" + systemID + "]";
	}
}
