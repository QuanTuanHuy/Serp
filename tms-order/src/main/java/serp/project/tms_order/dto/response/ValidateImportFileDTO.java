package serp.project.tms_order.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ValidateImportFileDTO<T> {
    private LinkedHashMap<String, String> header;
    @JsonProperty("file_id")
    private UUID fileId;
    @JsonProperty("is_success")
    private boolean isSuccess;
    @JsonProperty("error_message")
    private String errorMessage;
    private int type;
    private List<T> data;
}

