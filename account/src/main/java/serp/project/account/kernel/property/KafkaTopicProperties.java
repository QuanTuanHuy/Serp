/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project
 */

package serp.project.account.kernel.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.kafka.topics")
public class KafkaTopicProperties {
    private String syncUserFirstMile = "SYNC_USER_FIRST_MILE";
}
