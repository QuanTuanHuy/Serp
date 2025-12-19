package serp.project.logistics.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EnumPatternValidator.class) // Liên kết với class logic
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR,
        ElementType.PARAMETER, ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumValidator {
    Class<? extends Enum<?>> enumClass(); // Enum cần check là gì

    String message() default "Value is not valid"; // Message mặc định

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
