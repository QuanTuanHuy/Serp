package serp.project.school_bus_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class SchoolBusServiceApplication {

    public static void main(String[] args) {
        // PostgreSQL rejects the legacy JVM timezone id "Asia/Saigon".
        // Force a canonical timezone before datasource initialization.
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SpringApplication.run(SchoolBusServiceApplication.class, args);
    }
}
