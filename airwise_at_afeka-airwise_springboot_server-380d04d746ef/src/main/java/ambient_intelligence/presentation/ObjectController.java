package ambient_intelligence.presentation;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ambient_intelligence.logic.ObjectsServicePagination;
import ambient_intelligence.logic.boundaries.ObjectBoundary;
import ambient_intelligence.logic.boundaries.ObjectChildIdBoundary;
import ambient_intelligence.logic.exceptions.ObjectNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/ambient-intelligence/objects")
public class ObjectController {

	private final ObjectsServicePagination objectsService;

	public ObjectController(ObjectsServicePagination objectsService) {
		this.objectsService = objectsService;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ObjectBoundary createObject(@RequestBody ObjectBoundary objectBoundary) {
		return this.objectsService.create(objectBoundary);
	}

	@PutMapping(path = "/{systemID}/{objectId}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void updateObject(@PathVariable("systemID") String systemID, @PathVariable("objectId") String objectId,
			@RequestBody ObjectBoundary objectBoundary, @RequestParam("userSystemID") String userSystemID,
			@RequestParam("userEmail") String userEmail) {

		this.objectsService.updateObject(systemID, objectId, objectBoundary, userSystemID, userEmail);
	}

	@GetMapping(path = "/{systemID}/{objectId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ObjectBoundary getSpecificObject(@PathVariable("systemID") String systemID,
			@PathVariable("objectId") String objectId, @RequestParam("userSystemID") String userSystemID,
			@RequestParam("userEmail") String userEmail) {

		return this.objectsService.getSpecificObject(systemID, objectId, userSystemID, userEmail)
				.orElseThrow(() -> new ObjectNotFoundException("Could not find object with id: " + objectId));

	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ObjectBoundary> getAllObjects(@RequestParam("userSystemID") String userSystemID,
			@RequestParam("userEmail") String userEmail, @RequestParam(value = "size", defaultValue = "10") int size,
			@RequestParam(value = "page", defaultValue = "0") int page) {

		return this.objectsService.getAllObjects(userSystemID, userEmail, size, page);

	}

	@PutMapping(path = "/{parentSystemID}/{parentObjectId}/children", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void bindParentObjAndChildObj(@PathVariable("parentSystemID") String parentSystemID,
			@PathVariable("parentObjectId") String parentObjectId,
			@RequestBody ObjectChildIdBoundary objectChildIdBoundary, @RequestParam("userSystemID") String userSystemID,
			@RequestParam("userEmail") String userEmail) {

		this.objectsService.bindObjects(parentSystemID, parentObjectId,
				objectChildIdBoundary.getChildId().getSystemID(), objectChildIdBoundary.getChildId().getObjectId(),
				userSystemID, userEmail);

	}

	@GetMapping(path = "/{parentSystemID}/{parentObjectId}/children", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ObjectBoundary> getAllChildrenOfObject(@PathVariable("parentSystemID") String parentSystemID,
			@PathVariable("parentObjectId") String parentObjectId, @RequestParam("userSystemID") String userSystemID,
			@RequestParam("userEmail") String userEmail, @RequestParam(value = "size", defaultValue = "10") int size,
			@RequestParam(value = "page", defaultValue = "0") int page) {

		return this.objectsService.getChildren(parentSystemID, parentObjectId, userSystemID, userEmail, size, page);

	}
	

	@GetMapping(path = "/search/byAlias/{alias}", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ObjectBoundary> searchByExactAlias(@PathVariable("alias") String alias,
			@RequestParam("userSystemID") String userSystemID, @RequestParam("userEmail") String userEmail,
			@RequestParam(value = "size", defaultValue = "10") int size,
			@RequestParam(value = "page", defaultValue = "0") int page) {

		return this.objectsService.searchByExactAlias(alias, userSystemID, userEmail, size, page);

	}

	@GetMapping(path = "/search/byAliasPattern/{pattern}", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ObjectBoundary> searchByAliasPattern(@PathVariable("pattern") String pattern,
			@RequestParam("userSystemID") String userSystemID, @RequestParam("userEmail") String userEmail,
			@RequestParam(value = "size", defaultValue = "10") int size,
			@RequestParam(value = "page", defaultValue = "0") int page) {

		return this.objectsService.searchByAliasPattern(pattern, userSystemID, userEmail, size, page);

	}

	@GetMapping(path = "/search/byType/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ObjectBoundary> searchByType(@PathVariable("type") String type,
			@RequestParam("userSystemID") String userSystemID, @RequestParam("userEmail") String userEmail,
			@RequestParam(value = "size", defaultValue = "10") int size,
			@RequestParam(value = "page", defaultValue = "0") int page) {

		return this.objectsService.searchByType(type, userSystemID, userEmail, size, page);

	}

	@GetMapping(path = "/search/byStatus/{status}", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ObjectBoundary> searchByStatus(@PathVariable("status") String status,
			@RequestParam("userSystemID") String userSystemID, @RequestParam("userEmail") String userEmail,
			@RequestParam(value = "size", defaultValue = "10") int size,
			@RequestParam(value = "page", defaultValue = "0") int page) {

		return this.objectsService.searchByStatus(status, userSystemID, userEmail, size, page);

	}

	@GetMapping(path = "/search/byTypeAndStatus/{type}/{status}", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ObjectBoundary> searchByTypeAndStatus(@PathVariable("type") String type,
			@PathVariable("status") String status, @RequestParam("userSystemID") String userSystemID,
			@RequestParam("userEmail") String userEmail, @RequestParam(value = "size", defaultValue = "10") int size,
			@RequestParam(value = "page", defaultValue = "0") int page) {

		return this.objectsService.searchByTypeAndStatus(type, status, userSystemID, userEmail, size, page);

	}
}