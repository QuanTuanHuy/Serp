/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.domain.constant;

public class KafkaTopic {
    public static final String EMAIL_REQUEST_TOPIC = "serp-mail.email-request-topic";

    public static final String EMAIL_STATUS_TOPIC = "serp-mail.email-status-topic";

    public static final String EMAIL_DLQ_TOPIC = "serp-mail.email-dlq-topic";

    public static final String BULK_EMAIL_REQUEST_TOPIC = "serp-mail.bulk-email-request-topic";

    public static final String EMAIL_TEMPLATE_EVENT_TOPIC = "serp-mail.email-template-event-topic";

    private KafkaTopic() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
