package com.teamproj.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableCaching
@EnableScheduling
@EnableJpaAuditing
@SpringBootApplication
public class BackendApplication {
//    public static void main(String[] args) {
//        SpringApplication.run(BackendApplication.class, args);
//    }

    public static final String APPLICATION_LOCATIONS = "spring.config.location="
            + "/home/ec2-user/app/nonstop/application.properties,"
            + "/home/ec2-user/app/nonstop/real-application.yml";

    public static void main(String[] args) {
        new SpringApplicationBuilder(BackendApplication.class)
                .properties(APPLICATION_LOCATIONS)
                .run(args);
    }
}
