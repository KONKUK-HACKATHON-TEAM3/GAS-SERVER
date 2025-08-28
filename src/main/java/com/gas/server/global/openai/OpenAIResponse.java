package com.gas.server.global.openai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAIResponse(
        List<Choice> choices
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Choice(
            Message message
    ) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Message(
                String content
        ) {

        }
    }

    public String getContent() {
        if (choices != null && !choices.isEmpty() && choices.get(0).message() != null) {
            return choices.get(0).message().content();
        }
        return "";
    }
}
