package ambient_intelligence.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ambient_intelligence.dal.ObjectCrud;
import ambient_intelligence.data.ObjectEntity;
import ambient_intelligence.data.UserRole;
import ambient_intelligence.logic.boundaries.ObjectBoundary;
import ambient_intelligence.logic.boundaries.ObjectId;
import ambient_intelligence.logic.converters.ObjectConverter;
import ambient_intelligence.logic.exceptions.InvalidRequestInputException;
import ambient_intelligence.logic.exceptions.ObjectNotFoundException;
import ambient_intelligence.logic.exceptions.UnauthorizedException;
import ambient_intelligence.logic.security.AuthorizationService;
import ambient_intelligence.utils.AirWiseValidator;
import ambient_intelligence.utils.AirwiseConfig;
import ambient_intelligence.utils.DateUtils;

@Service
public class ObjectsServiceImpl implements ObjectsServicePagination {

	private final ObjectCrud objectsCrud;
	private final ObjectConverter objectConverter;
	private final AirWiseValidator validator;
	private final AuthorizationService authz;
	private Log log = LogFactory.getLog(ObjectsServiceImpl.class);

	public ObjectsServiceImpl(ObjectCrud objectsCrud, ObjectConverter objectConverter, AirWiseValidator validator,
			AuthorizationService authz) {
		this.objectsCrud = objectsCrud;
		this.objectConverter = objectConverter;
		this.validator = validator;
		this.authz = authz;
	}

	@Override
	@Transactional(readOnly = false)
	public ObjectBoundary create(ObjectBoundary object) {

		// Role validation: Only OPERATOR can create objects
		if (!this.authz.ensureRole(object.getCreatedBy().getUserId().getSystemID(),
				object.getCreatedBy().getUserId().getEmail(), UserRole.OPERATOR)) {
			throw new UnauthorizedException("Unauthorized action.");
		}

		// Input validation
		try {
			this.validator.isValidObjBoundaryRequest(object);
		} catch (Exception e) {
			throw new InvalidRequestInputException(e.getMessage());
		}

		ObjectId objectId = new ObjectId();
		objectId.setObjectId(UUID.randomUUID().toString());
		objectId.setSystemID(AirwiseConfig.getSystemID());
		object.setId(objectId);

		object.setCreationTimestamp(DateUtils.getCurrentFormattedDate());

		ObjectEntity entity = objectConverter.toEntity(object);
		ObjectEntity savedEntity = this.objectsCrud.save(entity);

		return objectConverter.toBoundary(savedEntity);
	}

	@Override
	@Transactional(readOnly = false)
	public void updateObject(String systemID, String objectId, ObjectBoundary update, String userSystemID,
			String userEmail) {

		if (!this.authz.ensureRole(userSystemID, userEmail, UserRole.OPERATOR)) {
			throw new UnauthorizedException("Unauthorized action.");
		}

		// Validate systemID
		if (!this.validator.isValidSystemId(systemID)) {
			throw new InvalidRequestInputException("The SystemId is invalid");
		}

		ObjectId objId = new ObjectId(objectId, systemID);
		if (!this.validator.checkValidObjectId(objId)) {
			throw new InvalidRequestInputException("The objectId is invalid");
		}

		String completeId = systemID + AirwiseConfig.getIdSeparator() + objectId;
		ObjectEntity existing = this.objectsCrud.findById(completeId)
				.orElseThrow(() -> new ObjectNotFoundException("ObjectId not found: " + objectId));

		if (update.getAlias() != null && !update.getAlias().isBlank()) {
			existing.setAlias(update.getAlias());
		}
		
		if (update.getObjectDetails() != null) {
			existing.setObjectDetails(update.getObjectDetails());
		}
		
		if (update.getStatus() != null && !update.getStatus().isBlank()) {
			existing.setStatus(update.getStatus());
		}
		
		if (update.getType() != null && !update.getType().isBlank()) {
			existing.setType(update.getType());
		}

		existing.setActive(update.isActive());

		this.objectsCrud.save(existing);

	}

	@Override
	@Transactional(readOnly = true)
	public Optional<ObjectBoundary> getSpecificObject(String systemID, String objectId, String userSystemID,
			String userEmail) {

		ObjectId objId = new ObjectId(objectId, systemID);

		if (!this.validator.checkValidObjectId(objId)) {
			throw new InvalidRequestInputException("The objectId is invalid");
		}
		if (!validator.isValidSystemId(userSystemID)) {
			throw new InvalidRequestInputException("Invalid userSystemID");
		}
		if (!validator.isValidEmail(userEmail)) {
			throw new InvalidRequestInputException("Invalid userEmail");
		}

		String completeId = systemID + AirwiseConfig.getIdSeparator() + objectId;
		Optional<ObjectBoundary> result;

		if (this.authz.ensureRole(userSystemID, userEmail, UserRole.OPERATOR)) {

			// OPERATOR can access any object
			Optional<ObjectEntity> entity = this.objectsCrud.findById(completeId);

			if (entity.isEmpty()) {
				throw new ObjectNotFoundException("ObjectId not found: " + objectId);
			}

			result = entity.map(objectConverter::toBoundary);

		} else if (this.authz.ensureRole(userSystemID, userEmail, UserRole.END_USER)) {

			// END_USER can only access active objects
			Optional<ObjectEntity> entity = this.objectsCrud.findByIdAndActiveTrue(completeId);

			if (entity.isEmpty()) {
				throw new ObjectNotFoundException("ObjectId not found: " + objectId);
			}

			result = entity.map(objectConverter::toBoundary);

		} else {
			throw new UnauthorizedException("Not authorized");
		}

		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public List<ObjectBoundary> getAllObjects(String userSystemID, String userEmail, int size, int page) {

		this.validator.isValidPaginationInputs(size, page);

		Pageable pageable = PageRequest.of(page, size, Direction.DESC, "creationTimestamp", "id");
		List<ObjectEntity> entities;

		if (this.authz.ensureRole(userSystemID, userEmail, UserRole.OPERATOR)) {

			entities = objectsCrud.findAll(pageable).getContent();

		} else if (this.authz.ensureRole(userSystemID, userEmail, UserRole.END_USER)) {

			entities = objectsCrud.findAllByActiveTrue(pageable);

			if (entities.isEmpty()) {
				throw new ObjectNotFoundException("No objects found");
			}

		} else {
			throw new UnauthorizedException("Not authorized");
		}

		return entities.stream().map(objectConverter::toBoundary).toList();
	}

	@Override
	@Transactional(readOnly = false)
	public void bindObjects(String parentSystemID, String parentObjectId, String childSystemID, String childObjectId,
			String userSystemID, String userEmail) {

		if (!this.authz.ensureRole(userSystemID, userEmail, UserRole.OPERATOR)) {
			throw new UnauthorizedException("Not authorized");
		}

		ObjectId objIdChild = new ObjectId(childObjectId, childSystemID);

		if (!this.validator.checkValidObjectId(objIdChild)) {
			throw new InvalidRequestInputException("The objectIdChild is invalid");
		}

		String completeIdChild = childSystemID + AirwiseConfig.getIdSeparator() + childObjectId;

		ObjectId objIdParent = new ObjectId(parentObjectId, parentSystemID);
		if (!this.validator.checkValidObjectId(objIdParent)) {
			throw new InvalidRequestInputException("The objectIdParent is invalid");
		}
		String completeIdParent = parentSystemID + AirwiseConfig.getIdSeparator() + parentObjectId;

		ObjectEntity parent = this.objectsCrud.findById(completeIdParent).orElseThrow(() -> new ObjectNotFoundException(
				"Parent object not found with systemID: " + parentSystemID + " and objectId: " + parentObjectId));

		ObjectEntity child = this.objectsCrud.findById(completeIdChild).orElseThrow(() -> new ObjectNotFoundException(
				"Child object not found with systemID: " + childSystemID + " and objectId: " + childObjectId));

		if (parent.getChilds() == null)
			parent.setChilds(new ArrayList<>());

		child.setParent(parent);
		List<ObjectEntity> list = parent.getChilds();
		list.add(child);
		parent.setChilds(list);

		this.objectsCrud.save(parent);
		this.objectsCrud.save(child);

	}
	
	@Override
	public List<ObjectBoundary> getParents(String childSystemID, String childObjectId, String userSystemID,
			String userEmail, int size, int page) {

		this.validator.isValidPaginationInputs(size, page);

		ObjectId objIdChild = new ObjectId(childObjectId, childSystemID);
		if (!this.validator.checkValidObjectId(objIdChild)) {
			throw new InvalidRequestInputException("The objectIdChild is invalid");
		}

		Pageable pageable = PageRequest.of(page, size, Direction.DESC, "creationTimestamp", "id");

		String completeIdChild = childSystemID + AirwiseConfig.getIdSeparator() + childObjectId;

		List<ObjectEntity> parentEntities;

		if (this.authz.ensureRole(userSystemID, userEmail, UserRole.OPERATOR)) {

			parentEntities = this.objectsCrud.findAllById(completeIdChild, pageable);

		} else if (this.authz.ensureRole(userSystemID, userEmail, UserRole.END_USER)) {

			parentEntities = this.objectsCrud.findAllByIdAndActiveTrue(completeIdChild, pageable);

		} else {
			throw new UnauthorizedException("Not authorized");
		}

		if (parentEntities == null || parentEntities.isEmpty()) {
			throw new ObjectNotFoundException("No parents found for child object ID: " + childObjectId);
		}
				
		return List.of(parentEntities.get(0).getParent()).stream().map(this.objectConverter::toBoundary).toList();
		
		// return parentEntities.stream().map(this.objectConverter::toBoundary).toList();

	}

	@Override
	@Transactional(readOnly = true)
	public List<ObjectBoundary> getChildren(String parentSystemID, String parentObjectId, String userSystemID,
			String userEmail, int size, int page) {

		this.validator.isValidPaginationInputs(size, page);

		ObjectId objIdParent = new ObjectId(parentObjectId, parentSystemID);

		if (!this.validator.checkValidObjectId(objIdParent)) {
			throw new InvalidRequestInputException("The objectIdParent is invalid");
		}

		String completeIdParent = parentSystemID + AirwiseConfig.getIdSeparator() + parentObjectId;

		Pageable pageable = PageRequest.of(page, size, Direction.DESC, "creationTimestamp", "id");
		List<ObjectEntity> childEntities;

		if (this.authz.ensureRole(userSystemID, userEmail, UserRole.OPERATOR)) {

			ObjectEntity parent = this.objectsCrud.findById(completeIdParent)
					.orElseThrow(() -> new ObjectNotFoundException("parentObjectId not found: " + parentObjectId));

			childEntities = this.objectsCrud.findAllByParent_Id(parent.getId(), pageable);

		} else if (this.authz.ensureRole(userSystemID, userEmail, UserRole.END_USER)) {

			ObjectEntity parent = this.objectsCrud.findById(completeIdParent)
					.orElseThrow(() -> new ObjectNotFoundException("parentObjectId not found: " + parentObjectId));

			childEntities = this.objectsCrud.findAllByParent_IdAndActiveTrue(parent.getId(), pageable);

		} else {
			throw new UnauthorizedException("Not authorized");
		}

		if (childEntities == null || childEntities.isEmpty()) {
			throw new ObjectNotFoundException("No children found for parent object ID: " + parentObjectId);
		}

		return childEntities.stream().map(this.objectConverter::toBoundary).toList();
	}

	@Override
	@Transactional(readOnly = false)
	public void deleteAllObjects(String userSystemID, String userEmail) {

		// validate the user making the action and his Role.
		if (!this.authz.ensureRole(userSystemID, userEmail, UserRole.ADMIN)) {
			throw new UnauthorizedException("Unauthorized Action");
		}

		this.objectsCrud.deleteAll();

	}

	@Override
	@Transactional(readOnly = true)
	public List<ObjectBoundary> searchByExactAlias(String alias, String userSystemID, String userEmail, int size,
			int page) {

		this.validator.isValidPaginationInputs(size, page);

		// Input validation
		if (alias == null || alias.isEmpty()) {
			throw new InvalidRequestInputException("Alias cannot be null or empty");
		}

		Pageable pageable = PageRequest.of(page, size, Direction.DESC, "creationTimestamp");
		List<ObjectEntity> entities;

		if (this.authz.ensureRole(userSystemID, userEmail, UserRole.OPERATOR)) {

			entities = objectsCrud.findByAlias(alias, pageable);

		} else if (this.authz.ensureRole(userSystemID, userEmail, UserRole.END_USER)) {

			entities = objectsCrud.findByAliasAndActiveTrue(alias, pageable);

			if (entities.isEmpty()) {
				throw new ObjectNotFoundException("No objects found with alias: " + alias);
			}

		} else {
			throw new UnauthorizedException("not authorized to search objects");
		}

		return entities.stream().map(objectConverter::toBoundary).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<ObjectBoundary> searchByAliasPattern(String pattern, String userSystemID, String userEmail, int size,
			int page) {

		this.validator.isValidPaginationInputs(size, page);

		// Input validation
		if (pattern == null || pattern.isEmpty()) {
			throw new InvalidRequestInputException("Pattern cannot be null or empty");
		}

		Pageable pageable = PageRequest.of(page, size, Direction.DESC, "creationTimestamp");
		List<ObjectEntity> entities;

		if (this.authz.ensureRole(userSystemID, userEmail, UserRole.OPERATOR)) {

			entities = objectsCrud.findByAliasLike("*" + pattern + "*", pageable);

		} else if (this.authz.ensureRole(userSystemID, userEmail, UserRole.END_USER)) {

			entities = objectsCrud.findByAliasLikeAndActiveTrue("*" + pattern + "*", pageable);

			if (entities.isEmpty()) {
				throw new ObjectNotFoundException("No objects found matching pattern: " + pattern);
			}

		} else {
			throw new UnauthorizedException("not authorized to search objects");
		}

		return entities.stream().map(objectConverter::toBoundary).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<ObjectBoundary> searchByType(String type, String userSystemID, String userEmail, int size, int page) {

		this.validator.isValidPaginationInputs(size, page);

		// Input validation
		if (type == null || type.isEmpty()) {
			throw new InvalidRequestInputException("Type cannot be null or empty");
		}

		Pageable pageable = PageRequest.of(page, size, Direction.DESC, "creationTimestamp");
		List<ObjectEntity> entities;

		if (this.authz.ensureRole(userSystemID, userEmail, UserRole.OPERATOR)) {

			entities = objectsCrud.findByType(type, pageable);

		} else if (this.authz.ensureRole(userSystemID, userEmail, UserRole.END_USER)) {

			entities = objectsCrud.findByTypeAndActiveTrue(type, pageable);

			if (entities.isEmpty()) {
				throw new ObjectNotFoundException("No objects found with type: " + type);
			}
		} else {
			throw new UnauthorizedException("not authorized to search objects");
		}

		return entities.stream().map(objectConverter::toBoundary).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<ObjectBoundary> searchByStatus(String status, String userSystemID, String userEmail, int size,
			int page) {

		this.validator.isValidPaginationInputs(size, page);

		// Input validation
		if (status == null || status.isEmpty()) {
			throw new InvalidRequestInputException("Status cannot be null or empty");
		}

		Pageable pageable = PageRequest.of(page, size, Direction.DESC, "creationTimestamp");
		List<ObjectEntity> entities;

		if (this.authz.ensureRole(userSystemID, userEmail, UserRole.OPERATOR)) {

			entities = objectsCrud.findByStatus(status, pageable);

		} else if (this.authz.ensureRole(userSystemID, userEmail, UserRole.END_USER)) {

			entities = objectsCrud.findByStatusAndActiveTrue(status, pageable);

			if (entities.isEmpty()) {
				throw new ObjectNotFoundException("No objects found with status: " + status);
			}

		} else {
			throw new UnauthorizedException("not authorized to search objects");
		}

		return entities.stream().map(objectConverter::toBoundary).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<ObjectBoundary> searchByTypeAndStatus(String type, String status, String userSystemID, String userEmail,
			int size, int page) {

		this.validator.isValidPaginationInputs(size, page);

		// Input validation
		if (type == null || type.isEmpty()) {
			throw new InvalidRequestInputException("Type cannot be null or empty");
		}
		if (status == null || status.isEmpty()) {
			throw new InvalidRequestInputException("Status cannot be null or empty");
		}

		Pageable pageable = PageRequest.of(page, size, Direction.DESC, "creationTimestamp");
		List<ObjectEntity> entities;

		if (this.authz.ensureRole(userSystemID, userEmail, UserRole.OPERATOR)) {

			entities = objectsCrud.findByTypeAndStatus(type, status, pageable);

		} else if (this.authz.ensureRole(userSystemID, userEmail, UserRole.END_USER)) {

			entities = objectsCrud.findByTypeAndStatusAndActiveTrue(type, status, pageable);

			if (entities.isEmpty()) {
				throw new ObjectNotFoundException("No objects found with type: " + type + " and status: " + status);
			}

		} else {
			throw new UnauthorizedException("not authorized to search objects");
		}

		return entities.stream().map(objectConverter::toBoundary).toList();
	}

	// START DEPRECATED METHODS

	@Override
	@Deprecated
	public void updateObject(String systemID, String objectId, ObjectBoundary update) {

		throw new InvalidRequestInputException("This operation is deprecated and will be removed in the near future.");

	}

	@Override
	@Deprecated
	public Optional<ObjectBoundary> getSpecificObject(String systemID, String objectId) {

		throw new InvalidRequestInputException("This operation is deprecated and will be removed in the near future.");

	}

	@Override
	@Deprecated
	public void deleteAllObjects() {

		throw new InvalidRequestInputException(
				"this operation is deprecated and it will be removed in the near future.");

	}

	@Override
	@Deprecated
	public void bindObjects(String parentSystemID, String parentObjectId, String childSystemID, String childObjectId) {

		throw new InvalidRequestInputException(
				"this operation is deprecated and it will be removed in the near future.");

	}

	@Override
	@Deprecated
	public List<ObjectBoundary> getChildren(String parentSystemID, String parentObjectId) {

		throw new InvalidRequestInputException(
				"this operation is deprecated and it will be removed in the near future.");

	}

	@Override
	@Deprecated
	public List<ObjectBoundary> getParents(String childSystemID, String childObjectId) {

		throw new InvalidRequestInputException(
				"this operation is deprecated and it will be removed in the near future.");

	}

	@Override
	@Deprecated
	public List<ObjectBoundary> getAllObjects() {

		throw new InvalidRequestInputException(
				"This operation is deprecated and it will be removed in the near future.");

	}

	// END DEPRECATED METHODS

}
