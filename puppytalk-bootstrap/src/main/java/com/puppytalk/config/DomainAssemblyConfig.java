package com.puppytalk.config;

import com.puppytalk.activity.ActivityDomainService;
import com.puppytalk.activity.UserActivityRepository;
import com.puppytalk.chat.ChatDomainService;
import com.puppytalk.chat.ChatRoomRepository;
import com.puppytalk.chat.MessageRepository;
import com.puppytalk.notification.NotificationDomainService;
import com.puppytalk.notification.NotificationRepository;
import com.puppytalk.pet.PetDomainService;
import com.puppytalk.pet.PetRepository;
import com.puppytalk.user.UserDomainService;
import com.puppytalk.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainAssemblyConfig {

    @Bean
    public PetDomainService petDomainService(PetRepository petRepository) {
        return new PetDomainService(petRepository);
    }

    @Bean
    public UserDomainService userDomainService(UserRepository userRepository) {
        return new UserDomainService(userRepository);
    }
    
    @Bean
    public ChatDomainService chatDomainService(ChatRoomRepository chatRoomRepository,
                                              MessageRepository messageRepository) {
        return new ChatDomainService(chatRoomRepository, messageRepository);
    }
    
    @Bean
    public ActivityDomainService activityDomainService(UserActivityRepository userActivityRepository) {
        return new ActivityDomainService(userActivityRepository);
    }
    
    @Bean
    public NotificationDomainService notificationDomainService(NotificationRepository notificationRepository) {
        return new NotificationDomainService(notificationRepository);
    }
}