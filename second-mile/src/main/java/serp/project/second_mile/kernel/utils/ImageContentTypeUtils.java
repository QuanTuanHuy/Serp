/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.kernel.utils;

import serp.project.second_mile.exception.AppException;
import serp.project.second_mile.exception.ErrorCode;

import java.util.Locale;

public final class ImageContentTypeUtils {

    private ImageContentTypeUtils() {
    }

    public static String normalizeImageContentType(String contentType) {
        String normalized = contentType == null
                ? ""
                : contentType.trim().toLowerCase(Locale.ROOT);

        if (!normalized.startsWith("image/")) {
            throw new AppException(ErrorCode.FILE_IMAGE_TYPE_INVALID);
        }

        return normalized;
    }
}
