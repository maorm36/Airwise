/*
package ambient_intelligence.data.converters;

import ambient_intelligence.logic.boundaries.CreatedBy;
import ambient_intelligence.utils.AirwiseConfig;
import jakarta.persistence.AttributeConverter;

public class CreatedByConverter implements AttributeConverter<CreatedBy, String> {

	@Override
	public String convertToDatabaseColumn(CreatedBy attribute) {

		if (attribute == null || attribute.getUserId() == null || attribute.getUserId().getEmail().isBlank()
				|| attribute.getUserId().getSystemId() == null || attribute.getUserId().getSystemId().isBlank()) {
			return null;
		}

		return attribute.getUserId().getSystemId() + AirwiseConfig.getIdSeparator() + attribute.getUserId().getEmail();
	}

	@Override
	public CreatedBy convertToEntityAttribute(String dbData) {

		if (dbData == null || dbData.isBlank()) {
			return null;
		}

		String[] userIdparts = dbData.split(AirwiseConfig.getIdSeparator());
		if (userIdparts.length != 2) {
			throw new IllegalArgumentException("Invalid data of CreatedBy: " + dbData);
		}

		CreatedBy createdBy = new CreatedBy(userIdparts[0], userIdparts[1]);

		return createdBy;
	}
}
*/