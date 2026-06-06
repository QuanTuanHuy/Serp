package serp.project.school_bus_service.shared;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.postgresql.util.PGobject;

@Converter(autoApply = false)
public class JsonbConverter implements AttributeConverter<String, Object> {

    @Override
    public Object convertToDatabaseColumn(String attribute) {
        try {
            if (attribute == null) {
                return null;
            }
            PGobject po = new PGobject();
            po.setType("jsonb");
            po.setValue(attribute);
            return po;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to convert String to jsonb PGobject", e);
        }
    }

    @Override
    public String convertToEntityAttribute(Object dbData) {
        if (dbData == null) {
            return null;
        }
        return dbData.toString();
    }
}
