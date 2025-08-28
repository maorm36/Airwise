package ambient_intelligence.logic;

import java.util.List;
import java.util.Optional;

import ambient_intelligence.logic.boundaries.ObjectBoundary;

public interface ObjectsService {

	public ObjectBoundary create(ObjectBoundary object);

	public void updateObject(String systemID, String objectId, ObjectBoundary update, String userSystemID, String userEmail);
	
	public Optional<ObjectBoundary> getSpecificObject(String systemID, String objectId, String userSystemID, String userEmail);
	
	public void bindObjects(String parentSystemID, String parentObjectId, String childSystemID, String childObjectId, String userSystemID, String userEmail);
	
	public void deleteAllObjects(String userSystemID, String userEmail);
	
	
	// DEPRECATED METHODS BELOW:
	
	@Deprecated
	public void updateObject(String systemID, String objectId, ObjectBoundary update);
	
	@Deprecated
	public Optional<ObjectBoundary> getSpecificObject(String systemID, String objectId);
	
	@Deprecated
	public List<ObjectBoundary> getAllObjects();
	
	@Deprecated
	public void bindObjects(String parentSystemID, String parentObjectId, String childSystemID, String childObjectId);
	
	@Deprecated
	public List<ObjectBoundary> getChildren(String parentSystemID, String parentObjectId);

	@Deprecated
	public List<ObjectBoundary> getParents(String childSystemID, String childObjectId);

	@Deprecated
	public void deleteAllObjects();
	
}
