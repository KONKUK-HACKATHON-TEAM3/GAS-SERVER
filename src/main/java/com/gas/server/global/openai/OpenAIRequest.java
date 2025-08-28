package com.gas.server.global.openai;

import java.util.List;
import java.util.Map;

public record OpenAIRequest(
        String model,
        List<Message> messages
) {

    public record Message(
            String role,
            Object content
    ) {

        public static Message system(String content) {
            return new Message("system", content);
        }

        public static Message user(String text) {
            return new Message("user", text);
        }

        public static Message userWithImage(String text, String imageUrl) {
            List<Map<String, Object>> contentList = List.of(
                    Map.of("type", "text", "text", text),
                    Map.of("type", "image_url", "image_url", Map.of("url", imageUrl))
            );
            return new Message("user", contentList);
        }
    }

    public static OpenAIRequest of(String model, List<Message> messages) {
        return new OpenAIRequest(model, messages);
    }
}
