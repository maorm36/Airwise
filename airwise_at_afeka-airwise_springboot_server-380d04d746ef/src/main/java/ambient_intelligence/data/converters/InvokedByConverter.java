/*
package ambient_intelligence.data.converters;

import ambient_intelligence.logic.boundaries.InvokedBy;
import ambient_intelligence.logic.boundaries.UserId;
import ambient_intelligence.utils.AirwiseConfig;
import jakarta.persistence.AttributeConverter;

public class InvokedByConverter implements AttributeConverter<InvokedBy, String> {

	@Override
	public String convertToDatabaseColumn(InvokedBy attribute) {
		
		if (attribute == null || attribute.getUserId() == null ||
				attribute.getUserId().getEmail() == null || attribute.getUserId().getEmail().isBlank()
				|| attribute.getUserId().getSystemId() == null 
				|| attribute.getUserId().getSystemId().isBlank()) {
            return null;
        }

        return attribute.getUserId().getSystemId() + AirwiseConfig.getIdSeparator() + attribute.getUserId().getEmail();
	}


	@Override
	public InvokedBy convertToEntityAttribute(String dbData) {
		
		if (dbData == null || dbData.isBlank()) {
            return null;
        }
		
		 String[] userIdparts = dbData.split(AirwiseConfig.getIdSeparator());
        if (userIdparts.length != 2) {
            throw new IllegalArgumentException("Invalid data of UserId: " + dbData);
        }

        InvokedBy invokedBy = new InvokedBy();
        invokedBy.setUserId(new UserId(userIdparts[0], userIdparts[1]));

        return invokedBy;
	}
}
*/