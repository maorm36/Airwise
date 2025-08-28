package ambient_intelligence.logic.boundaries;

public class ObjectChildIdBoundary {
	
	private ObjectId childId; 

	public ObjectChildIdBoundary() {
	}

	public ObjectChildIdBoundary(ObjectId childId) {
		this.childId = childId;
	}
	
	public ObjectId getChildId() {
		return childId;
	}

	public void setChildId(ObjectId childId) {
		this.childId = childId;
	}

	@Override
	public String toString() {
		return "ObjectChildIdBoundary [objectId=" + childId.getObjectId() + ", systemId=" + childId.getSystemID() + "]";
	}

}
