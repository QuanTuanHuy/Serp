/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableScheduling
public class TmsOrderApplication {

	private static final Logger log = LoggerFactory.getLogger(TmsOrderApplication.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(TmsOrderApplication.class, args);
		printApplicationInfo(context.getEnvironment());
	}

	private static void printApplicationInfo(Environment env) {
		String protocol = "http";
		if (env.getProperty("server.ssl.key-store") != null) {
			protocol = "https";
		}
		String serverPort = env.getProperty("server.port");
		String contextPath = env.getProperty("server.servlet.context-path");
		if (contextPath == null || contextPath.isBlank()) {
			contextPath = "/";
		}
		String hostAddress = "localhost";
		try {
			hostAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			log.warn("The host name could not be determined, using `localhost` as fallback");
		}

		System.out.println("\n----------------------------------------------------------\n\t" +
				"Application is running! Access URLs:\n\t" +
				"Local: \t\t" + protocol + "://localhost:" + serverPort + contextPath + "\n\t" +
				"External: \t" + protocol + "://" + hostAddress + ":" + serverPort + contextPath + "\n" +
				"----------------------------------------------------------");
	}

}
