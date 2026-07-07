package serp.project.school_bus_service.shared.i18n;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import serp.project.school_bus_service.shared.exception.AppErrorCode;

import java.util.Locale;

/**
 * Application-level message resolver backed by Spring's {@link MessageSource}.
 *
 * <p>Usage patterns:
 * <pre>
 * // Domain-specific ErrorInfo (no args)
 * throw new AppException(AppErrorCode.Bus.INVALID_STATUS,
 *         messageCommon.getMessage(AppErrorCode.Bus.INVALID_STATUS));
 *
 * // Domain-specific ErrorInfo with args
 * throw new AppException(AppErrorCode.Bus.INVALID_STATUS,
 *         messageCommon.getMessage(AppErrorCode.Bus.INVALID_STATUS, status, allowed));
 *
 * // Raw key (for backward-compat or dynamic keys)
 * messageCommon.getMessage("bus.plateNumber.conflict", plateNumber);
 * </pre>
 */
@Component
public class MessageCommon {

    private static final Locale SCHOOL_BUS_LOCALE = Locale.forLanguageTag("vi-VN");

    private final MessageSource messageSource;

    public MessageCommon(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Resolves the i18n message for a domain {@link AppErrorCode.ErrorInfo}.
     * Falls back to {@code errorInfo.defaultMessage()} if the key is not found.
     */
    public String getMessage(AppErrorCode.ErrorInfo errorInfo) {
        return messageSource.getMessage(errorInfo.code(), null, errorInfo.defaultMessage(), SCHOOL_BUS_LOCALE);
    }

    /**
     * Resolves a parameterized i18n message for a domain {@link AppErrorCode.ErrorInfo}.
     * Arguments map to {@code {0}}, {@code {1}}, … placeholders in the message template.
     */
    public String getMessage(AppErrorCode.ErrorInfo errorInfo, Object... args) {
        return messageSource.getMessage(errorInfo.code(), args, errorInfo.defaultMessage(), SCHOOL_BUS_LOCALE);
    }

    /**
     * Resolves a domain-specific message by raw key with no arguments.
     */
    public String getMessage(String key) {
        return messageSource.getMessage(key, null, key, SCHOOL_BUS_LOCALE);
    }

    /**
     * Resolves a domain-specific message by raw key with positional arguments.
     */
    public String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, key, SCHOOL_BUS_LOCALE);
    }
}
