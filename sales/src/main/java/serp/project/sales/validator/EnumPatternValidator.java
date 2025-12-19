package serp.project.logistics.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.stream.Stream;

public class EnumPatternValidator implements ConstraintValidator<EnumValidator, CharSequence> {
    private List<String> acceptedValues;

    @Override
    public void initialize(EnumValidator annotation) {
        // Lấy danh sách các giá trị Enum hợp lệ và chuyển thành String
        acceptedValues = Stream.of(annotation.enumClass().getEnumConstants())
                .map(Enum::name)
                .toList();
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        // Nếu null, để cho @NotNull xử lý (nếu có), ở đây return true
        if (value == null) {
            return true;
        }
        // Kiểm tra giá trị gửi lên có nằm trong list Enum không
        return acceptedValues.contains(value.toString());
    }
}
