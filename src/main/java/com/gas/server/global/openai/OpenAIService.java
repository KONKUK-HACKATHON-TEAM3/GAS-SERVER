package com.gas.server.global.openai;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAIService {

    private final OpenAIClient openAIClient;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    private static final String SYSTEM_PROMPT = """
            당신은 가족 간 일상 공유 서비스의 태그 생성 전문가입니다.
            가족들이 서로의 꾸밈없는 일상을 공유하는 사진과 텍스트를 분석하여 따뜻하고 구체적인 태그를 생성해주세요.
            
            태그 생성 가이드라인:
            1. 2~4개 사이의 한글 태그를 생성
            2. 구체적이고 직관적인 내용 우선 (예: #찜닭, #저녁식사, #카페, #산책)
            3. 감정이나 분위기도 포함 가능 (예: #피곤한, #하루, #행복한, #순간)
            4. 가족 관련 키워드가 있다면 우선 포함
            5. 일반적인 태그보다 구체적인 상황 설명을 선호
            
            좋은 예시: "#찜닭, #저녁, #맛있다, #피곤이싹"
            나쁜 예시: "#음식, #일상, #기록, #추억"
            
            태그는 콤마로 구분하여 반환하고, 각 태그는 앞에 #을 달아서 10자 이내로 작성해주세요.
            """;

    public String generateTags(String imageUrl, String text) {
        try {
            String userPrompt = buildUserPrompt(text);

            List<OpenAIRequest.Message> messages = List.of(
                    OpenAIRequest.Message.system(SYSTEM_PROMPT),
                    OpenAIRequest.Message.userWithImage(userPrompt, imageUrl)
            );

            OpenAIRequest request = OpenAIRequest.of(model, messages);
            OpenAIResponse response = openAIClient.generateCompletion(
                    "Bearer " + apiKey,
                    request
            );

            String content = response.getContent();
            if (StringUtils.hasText(content)) {
                return cleanTags(content);
            }
            return generateContextualTags(text);
        } catch (Exception e) {
            log.error("Failed to generate tags from OpenAI", e);
            return generateContextualTags(text);
        }
    }

    public String generateTagsForVideo(String text) {
        // 영상이고 텍스트가 없으면 태그 생성 안 함
        if (!StringUtils.hasText(text)) {
            return null;
        }

        try {
            List<OpenAIRequest.Message> messages = List.of(
                    OpenAIRequest.Message.system(SYSTEM_PROMPT),
                    OpenAIRequest.Message.user("다음 영상 설명을 보고 태그를 생성해주세요: " + text)
            );

            OpenAIRequest request = OpenAIRequest.of(model, messages);
            OpenAIResponse response = openAIClient.generateCompletion(
                    "Bearer " + apiKey,
                    request
            );

            String content = response.getContent();
            if (StringUtils.hasText(content)) {
                return cleanTags(content);
            }
            return generateContextualTags(text);
        } catch (Exception e) {
            log.error("Failed to generate tags for video", e);
            return generateContextualTags(text);
        }
    }

    private String buildUserPrompt(String text) {
        if (StringUtils.hasText(text)) {
            return String.format(
                    "이미지와 함께 작성된 텍스트: '%s'\n이 내용을 분석하여 가족과 공유하기 좋은 태그를 '#태그1, #태그2' 형식으로 생성해주세요.",
                    text
            );
        } else {
            return "이미지를 분석하여 가족과 공유하기 좋은 구체적인 태그를 '#태그1, #태그2' 형식으로 생성해주세요.";
        }
    }

    private String cleanTags(String content) {
        // GPT가 이미 "#태그, #태그" 형식으로 반환하므로 간단한 정리만
        return content.trim()
                .replaceAll("\\s+", " ")  // 연속된 공백 제거
                .replaceAll("[\"']", "");  // 따옴표 제거
    }

    private String generateContextualTags(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }

        List<String> tags = new ArrayList<>();
        String lowerText = text.toLowerCase();

        // 해커톤 관련
        if (containsAny(lowerText, "해커톤", "hackathon", "콩쿠르", "건국대", "링크", "연결")) {
            tags.add("#해커톤");
            if (containsAny(lowerText, "우승", "1등", "대상", "수상")) {
                tags.add("#대상가즈아");
            } else {
                tags.add("#화이팅");
            }
            if (containsAny(lowerText, "링크", "연결", "소통")) {
                tags.add("#연결");
            }
        }

        // 음식 관련 (더 구체적으로)
        if (containsAny(lowerText, "찜닭", "닭갈비", "닭")) {
            tags.add("#찜닭");
            if (containsAny(lowerText, "매운", "매워", "불닭")) {
                tags.add("#매운맛");
            }
        }
        if (containsAny(lowerText, "피자", "도미노", "피자헛")) {
            tags.add("#피자");
            tags.add("#치즈가득");
        }
        if (containsAny(lowerText, "치킨", "통닭", "bbq", "교촌")) {
            tags.add("#치킨");
            if (containsAny(lowerText, "맥주", "콜라")) {
                tags.add("#치맥");
            }
        }
        if (containsAny(lowerText, "야식", "새벽", "밤")) {
            tags.add("#야식");
            tags.add("#죄송합니다");
        }
        if (containsAny(lowerText, "아침", "모닝")) {
            tags.add("#아침식사");
            tags.add("#굿모닝");
        }
        if (containsAny(lowerText, "맛있", "야미", "yummy", "존맛")) {
            tags.add("#야미");
        }

        // 감정/상태 (더 다양하게)
        if (containsAny(lowerText, "피곤", "힘들", "지친", "졸려", "피로")) {
            tags.add("#피곤모드");
            if (containsAny(lowerText, "극복", "이겨", "파이팅")) {
                tags.add("#그래도화이팅");
            }
        }
        if (containsAny(lowerText, "행복", "좋아", "최고", "굿")) {
            tags.add("#행복");
            tags.add("#좋은하루");
        }
        if (containsAny(lowerText, "슬프", "우울", "그립", "보고싶")) {
            tags.add("#보고싶어요");
        }
        if (containsAny(lowerText, "스트레스", "답답", "짜증")) {
            tags.add("#스트레스");
            tags.add("#힐링필요");
        }

        // 일상 활동 (구체화)
        if (containsAny(lowerText, "공부", "과제", "시험", "레포트")) {
            tags.add("#공부");
            if (containsAny(lowerText, "시험", "중간", "기말")) {
                tags.add("#시험기간");
            } else {
                tags.add("#열공모드");
            }
        }
        if (containsAny(lowerText, "운동", "헬스", "러닝", "조깅")) {
            tags.add("#운동");
            tags.add("#건강한하루");
        }
        if (containsAny(lowerText, "영화", "드라마", "넷플릭스", "유튜브")) {
            tags.add("#휴식");
            tags.add("#넷플릭스");
        }
        if (containsAny(lowerText, "게임", "롤", "오버워치", "피파")) {
            tags.add("#게임");
            tags.add("#힐링타임");
        }
        if (containsAny(lowerText, "카페", "커피", "아메리카노", "라떼")) {
            tags.add("#카페");
            tags.add("#커피타임");
        }

        // 날씨/계절
        if (containsAny(lowerText, "비", "rain", "우산")) {
            tags.add("#비오는날");
        }
        if (containsAny(lowerText, "눈", "snow", "겨울")) {
            tags.add("#눈");
            tags.add("#겨울감성");
        }
        if (containsAny(lowerText, "봄", "벚꽃", "꽃")) {
            tags.add("#봄");
            tags.add("#꽃구경");
        }

        // 태그가 없으면 시간대별 기본 태그
        if (tags.isEmpty()) {
            if (containsAny(lowerText, "오늘", "today", "일상")) {
                tags.add("#오늘의일상");
            } else if (containsAny(lowerText, "주말", "토요일", "일요일")) {
                tags.add("#주말");
                tags.add("#휴식");
            } else {
                tags.add("#일상");
                tags.add("#데일리");
            }
        }

        // 최대 4개까지만 반환, # 붙여서
        List<String> finalTags = tags.subList(0, Math.min(tags.size(), 4));
        return String.join(", ", finalTags);
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}