package com.gas.server.global.fcm;

import com.gas.server.domain.entity.MemberEntity;
import com.gas.server.domain.repository.MemberRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final MemberRepository memberRepository;

    @Async
    public void sendNewFeedNotification(Long uploaderId, String uploaderNickname) {
        try {
            List<MemberEntity> members = memberRepository.findAll();
            
            for (MemberEntity member : members) {
                if (!Objects.equals(member.getId(), uploaderId) && 
                    member.getFcmToken() != null && 
                    !member.getFcmToken().isEmpty()) {
                    
                    sendNotification(
                        member.getFcmToken(),
                        "가족의 새로운 소식이 도착했어요!",
                        uploaderNickname + "님이 새로운 스토리를 공유했어요."
                    );
                }
            }
        } catch (Exception e) {
            log.error("Failed to send new feed notifications: {}", e.getMessage());
        }
    }

    @Async
    public void sendRankingChangeNotification(Long completerId) {
        try {
            List<MemberEntity> members = memberRepository.findAll();
            
            for (MemberEntity member : members) {
                if (!Objects.equals(member.getId(), completerId) && 
                    member.getFcmToken() != null && 
                    !member.getFcmToken().isEmpty()) {
                    
                    sendNotification(
                        member.getFcmToken(),
                        "이번 주 1등이 바뀌었어요!",
                        "지금 바로 확인해 보세요."
                    );
                }
            }
        } catch (Exception e) {
            log.error("Failed to send ranking change notifications: {}", e.getMessage());
        }
    }

    private void sendNotification(String token, String title, String body) {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(notification)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.debug("Successfully sent notification to token {}: {}", token.substring(0, 10) + "...", response);
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send notification to token {}: {}", token.substring(0, 10) + "...", e.getMessage());
            
            if (e.getMessagingErrorCode() != null) {
                switch (e.getMessagingErrorCode()) {
                    case UNREGISTERED:
                    case INVALID_ARGUMENT:
                        log.info("Removing invalid FCM token: {}", token.substring(0, 10) + "...");
                        removeInvalidToken(token);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            log.error("Unexpected error while sending notification: {}", e.getMessage());
        }
    }

    @Transactional
    private void removeInvalidToken(String token) {
        try {
            memberRepository.findAll().stream()
                    .filter(member -> token.equals(member.getFcmToken()))
                    .forEach(member -> {
                        member.updateFcmToken(null);
                        memberRepository.save(member);
                    });
        } catch (Exception e) {
            log.error("Failed to remove invalid token: {}", e.getMessage());
        }
    }
}