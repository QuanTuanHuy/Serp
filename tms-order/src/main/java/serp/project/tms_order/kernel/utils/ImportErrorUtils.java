/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.kernel.utils;

import serp.project.tms_order.exception.AppException;
import serp.project.tms_order.exception.ErrorCode;

import java.util.function.Function;

public final class ImportErrorUtils {

    private ImportErrorUtils() {
    }

    public static String resolveExceptionMessage(Throwable throwable, Function<String, String> messageResolver) {
        if (throwable == null) {
            return messageResolver.apply(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessageKey());
        }

        Throwable rootCause = throwable;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }

        if (rootCause instanceof AppException appException) {
            return messageResolver.apply(appException.getErrorCode().getMessageKey());
        }

        String rootMessage = rootCause.getMessage();
        if (!hasText(rootMessage)) {
            return messageResolver.apply(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessageKey());
        }

        if (rootMessage.startsWith("error.")) {
            return messageResolver.apply(rootMessage);
        }

        return rootMessage;
    }

    public static String truncateErrorMessage(String errorMessage, int maxLength) {
        if (!hasText(errorMessage)) {
            return null;
        }

        if (errorMessage.length() <= maxLength) {
            return errorMessage;
        }

        return errorMessage.substring(0, maxLength);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
