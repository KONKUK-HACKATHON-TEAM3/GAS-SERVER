package com.gas.server.global.openai;

import com.gas.server.global.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "openAiClient",
        url = "https://api.openai.com",
        configuration = FeignConfig.class
)
public interface OpenAIClient {

    @PostMapping("/v1/chat/completions")
    OpenAIResponse generateCompletion(
            @RequestHeader("Authorization") String authorization,
            @RequestBody OpenAIRequest request
    );
}
