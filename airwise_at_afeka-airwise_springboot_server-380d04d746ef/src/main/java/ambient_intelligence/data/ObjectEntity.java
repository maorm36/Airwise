package ambient_intelligence.data;

import java.util.List;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import ambient_intelligence.logic.boundaries.CreatedBy;
import ambient_intelligence.logic.boundaries.ObjectId;
import ambient_intelligence.utils.AirwiseConfig;

@Document(collection = "OBJECTS")
public class ObjectEntity {

	@Id
	private String id;
	private String type;
	private String alias;
	private String status;
	private boolean active;
	private String creationTimestamp;
	private CreatedBy createdBy;
	private Map<String, Object> objectDetails;
	
	@DBRef
	private ObjectEntity parent;
	@DBRef
	private List<ObjectEntity> childs;

	public ObjectEntity() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setId(ObjectId id) {
		this.id = AirwiseConfig.getSystemID() + AirwiseConfig.getIdSeparator() + id.getObjectId();
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getCreationTimestamp() {
		return creationTimestamp;
	}

	public void setCreationTimestamp(String creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}

	public CreatedBy getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(CreatedBy createdBy) {
		this.createdBy = createdBy;
	}

	public Map<String, Object> getObjectDetails() {
		return objectDetails;
	}

	public void setObjectDetails(Map<String, Object> objectDetails) {
		this.objectDetails = objectDetails;
	}
	
	public ObjectEntity getParent() {
		return parent;
	}

	public void setParent(ObjectEntity parent) {
		this.parent = parent;
	}
	
	public List<ObjectEntity> getChilds() {
		return childs;
	}

	public void setChilds(List<ObjectEntity> childs) {
		this.childs = childs;
	}

	@Override
	public String toString() {
		return "ObjectEntity [id=" + id + ", type=" + type + ", alias=" + alias + ", status=" + status + ", active="
				+ active + ", creationTimestamp=" + creationTimestamp + ", createdBy=" + createdBy + ", parent="
				+ parent + ", objectDetails=" + objectDetails + "]";
	}

}
