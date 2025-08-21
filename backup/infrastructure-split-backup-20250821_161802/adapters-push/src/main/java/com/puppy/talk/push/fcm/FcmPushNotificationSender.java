package com.puppy.talk.push.fcm;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.puppy.talk.push.PushNotificationException;
import com.puppy.talk.push.PushNotificationSender;
import com.puppy.talk.push.PushNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Firebase Cloud Messaging 푸시 알림 전송 구현체
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FcmPushNotificationSender implements PushNotificationSender {
    
    private final FirebaseMessaging firebaseMessaging;
    
    @Override
    public void send(PushNotification notification) {
        if (!isAvailable()) {
            log.warn("FCM service is not available, skipping notification: {}", notification.identity());
            return;
        }
        
        try {
            Message message = buildMessage(notification);
            String response = firebaseMessaging.send(message);
            
            log.info("Successfully sent FCM message: {} for notification: {}", 
                response, notification.identity());
                
        } catch (Exception e) {
            log.error("Failed to send FCM message for notification: {} - {}", 
                notification.identity(), e.getMessage(), e);
            throw new PushNotificationException("Failed to send push notification", e);
        }
    }
    
    @Override
    public void sendBatch(Iterable<PushNotification> notifications) {
        List<Message> messages = new ArrayList<>();
        
        for (PushNotification notification : notifications) {
            try {
                Message message = buildMessage(notification);
                messages.add(message);
            } catch (Exception e) {
                log.error("Failed to build FCM message for notification: {} - {}", 
                    notification.identity(), e.getMessage());
            }
        }
        
        if (messages.isEmpty()) {
            log.warn("No valid messages to send in batch");
            return;
        }
        
        try {
            // FCM은 배치 전송을 지원하지만, 여기서는 개별 전송으로 구현
            // 실제 프로덕션에서는 sendAll()을 사용할 수 있습니다
            for (Message message : messages) {
                firebaseMessaging.send(message);
            }
            
            log.info("Successfully sent {} FCM messages in batch", messages.size());
            
        } catch (Exception e) {
            log.error("Failed to send FCM batch messages: {}", e.getMessage(), e);
            throw new PushNotificationException("Failed to send batch push notifications", e);
        }
    }
    
    @Override
    public boolean isAvailable() {
        try {
            return firebaseMessaging != null;
        } catch (Exception e) {
            log.debug("FCM service is not available: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public String getServiceName() {
        return "Firebase Cloud Messaging (FCM)";
    }
    
    /**
     * PushNotification을 FCM Message로 변환합니다.
     */
    private Message buildMessage(PushNotification pushNotification) {
        Message.Builder builder = Message.builder()
            .setToken(pushNotification.deviceToken())
            .setNotification(Notification.builder()
                .setTitle(pushNotification.title())
                .setBody(pushNotification.message())
                .build());
        
        // 추가 데이터가 있는 경우 포함
        if (pushNotification.data() != null && !pushNotification.data().trim().isEmpty()) {
            try {
                // JSON 문자열을 Map으로 변환 (간단한 구현)
                Map<String, String> dataMap = parseDataString(pushNotification.data());
                if (!dataMap.isEmpty()) {
                    builder.putAllData(dataMap);
                }
            } catch (Exception e) {
                log.warn("Failed to parse notification data: {} - {}", 
                    pushNotification.data(), e.getMessage());
            }
        }
        
        // 알림 타입을 데이터에 추가
        builder.putData("notificationType", pushNotification.notificationType().name());
        builder.putData("userId", pushNotification.userId().id().toString());
        
        return builder.build();
    }
    
    /**
     * JSON 형태의 데이터 문자열을 Map으로 파싱합니다.
     * 간단한 구현으로, 실제로는 Jackson ObjectMapper 등을 사용하는 것이 좋습니다.
     */
    private Map<String, String> parseDataString(String dataString) {
        Map<String, String> dataMap = new java.util.HashMap<>();
        
        if (dataString.startsWith("{") && dataString.endsWith("}")) {
            // 간단한 JSON 파싱 (프로덕션에서는 ObjectMapper 사용 권장)
            String content = dataString.substring(1, dataString.length() - 1);
            String[] pairs = content.split(",");
            
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim().replaceAll("\"", "");
                    String value = keyValue[1].trim().replaceAll("\"", "");
                    dataMap.put(key, value);
                }
            }
        } else {
            // 단순 key=value 형태로 간주
            dataMap.put("extra", dataString);
        }
        
        return dataMap;
    }
}