/*
package ambient_intelligence.data.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import java.util.Map;

public class MapToJsonConverter implements AttributeConverter<Map<String, Object>, String> {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String convertToDatabaseColumn(Map<String, Object> attribute) {

		try {

			return objectMapper.writeValueAsString(attribute);

		} catch (Exception e) {
			throw new RuntimeException("Error converting Map to JSON as String", e);
		}

	}

	
	@Override
	public Map<String, Object> convertToEntityAttribute(String dbData) {

		try {

			return objectMapper.readValue(dbData, Map.class);

		} catch (Exception e) {
			throw new RuntimeException("Error converting JSON(as String) to Map", e);
		}

	}

}
*/