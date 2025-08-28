package ambient_intelligence.logic.converters;

import org.springframework.stereotype.Component;

import ambient_intelligence.data.ObjectEntity;
import ambient_intelligence.logic.boundaries.ObjectBoundary;
import ambient_intelligence.logic.boundaries.ObjectId;
import ambient_intelligence.utils.AirwiseConfig;

@Component
public class ObjectConverter {

	public ObjectConverter() {
	}

	public ObjectBoundary toBoundary(ObjectEntity entity) {

		ObjectBoundary rv = new ObjectBoundary();

		ObjectId objectId = new ObjectId();

		String systemID = entity.getId().split(AirwiseConfig.getIdSeparator())[0];
		String id = entity.getId().split(AirwiseConfig.getIdSeparator())[1];

		objectId.setSystemID(systemID);
		objectId.setObjectId(id);

		rv.setId(objectId);
		rv.setActive(entity.isActive());
		rv.setAlias(entity.getAlias());
		rv.setCreatedBy(entity.getCreatedBy());
		rv.setCreationTimestamp(entity.getCreationTimestamp());
		rv.setObjectDetails(entity.getObjectDetails());
		rv.setStatus(entity.getStatus());
		rv.setType(entity.getType());

		return rv;
	}

	public ObjectEntity toEntity(ObjectBoundary boundary) {

		ObjectEntity rv = new ObjectEntity();

		rv.setActive(boundary.isActive());
		rv.setAlias(boundary.getAlias());
		rv.setCreatedBy(boundary.getCreatedBy());
		rv.setCreationTimestamp(boundary.getCreationTimestamp());
		rv.setId(boundary.getId());
		rv.setObjectDetails(boundary.getObjectDetails());
		rv.setStatus(boundary.getStatus());
		rv.setType(boundary.getType());

		return rv;
	}

}