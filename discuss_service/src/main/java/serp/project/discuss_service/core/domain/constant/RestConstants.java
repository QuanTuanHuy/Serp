/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.discuss_service.core.domain.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RestConstants {
    public static final String API_BASE = "/api/v1";

    public static final String MESSAGES = API_BASE + "/channels/{channelId}/messages";
    public static final String CHANNELS = API_BASE + "/channels";
    public static final String USERS = API_BASE + "/users";
    public static final String ATTACHMENTS = API_BASE + "/attachments";
}
