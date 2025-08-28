package com.gas.server.global.fcm;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Slf4j
@Configuration
public class FcmConfig {

    @Value("${fcm.key.path}")
    private String fcmKeyPath;

    @Value("${fcm.key.scope}")
    private String fcmScope;

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource(fcmKeyPath);
            
            try (InputStream serviceAccount = resource.getInputStream()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount)
                                .createScoped(fcmScope))
                        .build();

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                    log.info("Firebase application initialized successfully");
                }
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }
}