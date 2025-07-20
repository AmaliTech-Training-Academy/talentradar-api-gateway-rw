package com.talentradar.apigatewayrw.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class ApiGatewayHealthIndicator implements HealthIndicator {

    private static final String DOWN_FILE_PATH = "/tmp/service_down";
    
    @Override
    public Health health() {
        File serviceDownFile = new File(DOWN_FILE_PATH);
        
        if (serviceDownFile.exists()) {
            return Health.down()
                    .withDetail("message", "Service is down or in maintenance")
                    .build();
        } else {
            return Health.up()
                    .withDetail("message", "Service is running")
                    .build();
        }
    }
}
