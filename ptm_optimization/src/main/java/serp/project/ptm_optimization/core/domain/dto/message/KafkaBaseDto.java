package serp.project.ptm_optimization.core.domain.dto.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class KafkaBaseDto<T> {
    private String cmd;
    private String errorCode;
    private String errorMessage;
    private Long timestamp;
    private T data;
    private String source;
    private String replyTopic;
    private Object metaData;
}
