package com.habesha.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Habesha Community Spring Boot application.  This class
 * bootstraps the application context and scans for components under the
 * {@code com.habesha.community} package.  Running this class will start an
 * embedded web server and expose the REST API endpoints defined in the
 * controllers.  Use the {@code application.properties} file for
 * configuration such as database connection details and external API keys.
 */
@SpringBootApplication
public class HabeshaCommunityApplication {

    public static void main(String[] args) {
        SpringApplication.run(HabeshaCommunityApplication.class, args);
    }
}