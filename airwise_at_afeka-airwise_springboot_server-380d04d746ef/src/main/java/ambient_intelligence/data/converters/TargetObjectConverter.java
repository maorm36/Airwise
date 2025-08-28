/*
package ambient_intelligence.data.converters;

import ambient_intelligence.logic.boundaries.ObjectId;
import ambient_intelligence.logic.boundaries.TargetObject;
import ambient_intelligence.utils.AirwiseConfig;
import jakarta.persistence.AttributeConverter;

public class TargetObjectConverter implements AttributeConverter<TargetObject, String> {

	@Override
	public String convertToDatabaseColumn(TargetObject attribute) {
		if (attribute == null || attribute.getId() == null || attribute.getId().getSystemId() == null
				|| attribute.getId().getId() == null || attribute.getId().getSystemId().isBlank()
				|| attribute.getId().getId().isBlank()) {
			return null;
		}

		return attribute.getId().getSystemId() + AirwiseConfig.getIdSeparator() + attribute.getId().getId();
	}

	@Override
	public TargetObject convertToEntityAttribute(String dbData) {

		if (dbData == null || dbData.isBlank()) {
			return null;
		}

		String[] objectIdparts = dbData.split(AirwiseConfig.getIdSeparator());
		if (objectIdparts.length != 2) {
			throw new IllegalArgumentException("Invalid data of ObjectId: " + dbData);
		}

		TargetObject targetObject = new TargetObject(new ObjectId(objectIdparts[1], objectIdparts[0]));
		return targetObject;
	}
}
*/