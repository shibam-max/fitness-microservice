package com.fitness.activityservice.service;

import com.fitness.activityservice.config.WebClientConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserValidationService {

    private final WebClient userServiceWebClient;

    public boolean validateUser(String userId) {
        log.info("Calling user service for {}", userId);
        try {
            Boolean result = userServiceWebClient.get()
                    .uri("/api/users/{userId}/validate", userId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            return result != null && result;
    } catch(WebClientException e){
        log.error("Error calling user service: {}", e.getMessage());
        log.warn("User-service validation failed, but allowing request to proceed");
        }
        return true;
    }
}
