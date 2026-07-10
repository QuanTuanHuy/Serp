package serp.project.tms_payment_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import serp.project.tms_payment_service.scheduler.OrderStatusQueryScheduler;
import serp.project.tms_payment_service.scheduler.RefundStatusChecker;
import serp.project.tms_payment_service.scheduler.WebhookRetryScheduler;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:tms_payment_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=none"
})
class PaymentServiceApplicationTests {

	@MockBean
	private OrderStatusQueryScheduler orderStatusQueryScheduler;

	@MockBean
	private RefundStatusChecker refundStatusChecker;

	@MockBean
	private WebhookRetryScheduler webhookRetryScheduler;

	@Test
	void contextLoads() {
	}

}
