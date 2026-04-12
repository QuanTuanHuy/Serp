/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.first_mile.enums.ImportHistoryStatus;
import serp.project.first_mile.enums.ImportType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportHistoryResponse {
    private Long id;

    @JsonProperty("file_id")
    private UUID fileId;

    @JsonProperty("file_name")
    private String fileName;

    private ImportHistoryStatus status;

    @JsonProperty("total_records")
    private Integer totalRecords;

    @JsonProperty("success_records")
    private Integer successRecords;

    @JsonProperty("failed_records")
    private Integer failedRecords;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("started_at")
    private LocalDateTime startedAt;

    @JsonProperty("finished_at")
    private LocalDateTime finishedAt;

    @JsonProperty("type")
    private ImportType type;
}
