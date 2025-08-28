package com.gas.server.global.config;

import com.gas.server.ServerApplication;
import feign.Retryer;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackageClasses = ServerApplication.class)
public class FeignConfig {

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(1000, 5000, 3);
    }
}
