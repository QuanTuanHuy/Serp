/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.response;

public record PostOfficeGeocodeBatchResponse(
        int requestedBatch,
        int processed,
        int updated,
        int skipped
) {
}
