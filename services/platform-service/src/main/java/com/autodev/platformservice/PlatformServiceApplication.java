package com.autodev.platformservice;

import com.autodev.platformservice.config.KeycloakProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(KeycloakProperties.class)
@SpringBootApplication
public class PlatformServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlatformServiceApplication.class, args);
    }

}
