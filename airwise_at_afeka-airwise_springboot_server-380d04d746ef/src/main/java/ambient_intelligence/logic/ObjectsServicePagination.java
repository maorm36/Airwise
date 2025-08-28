package ambient_intelligence.logic;

import java.util.List;
import ambient_intelligence.logic.boundaries.ObjectBoundary;

public interface ObjectsServicePagination extends ObjectsService {
	
	public List<ObjectBoundary> getAllObjects(String userSystemID, String userEmail, 
			int size, int page);
	
	public List<ObjectBoundary> getParents(String childSystemID, String childObjectId, String userSystemID, String userEmail,
			int size, int page);
	
	public List<ObjectBoundary> getChildren(String parentSystemID, String parentObjectId, String userSystemID, String userEmail,
			int size, int page);

	public List<ObjectBoundary> searchByExactAlias(String alias, String userSystemID, String userEmail, 
			int size, int page);

	public List<ObjectBoundary> searchByAliasPattern(String pattern, String userSystemID, String userEmail, 
			int size, int page);

	public List<ObjectBoundary> searchByType(String type, String userSystemID, String userEmail, 
			int size, int page);

	public List<ObjectBoundary> searchByStatus(String status, String userSystemID, String userEmail, 
			int size, int page);

	public List<ObjectBoundary> searchByTypeAndStatus(String type, String status, String userSystemID, String userEmail,
			int size, int page);
}
