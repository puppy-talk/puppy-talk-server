package com.puppy.talk.service;

import com.puppy.talk.infrastructure.chat.ChatRoomRepository;
import com.puppy.talk.infrastructure.pet.PersonaRepository;
import com.puppy.talk.infrastructure.pet.PetRepository;
import com.puppy.talk.infrastructure.user.UserRepository;
import com.puppy.talk.exception.pet.PersonaNotFoundException;
import com.puppy.talk.exception.user.UserNotFoundException;
import com.puppy.talk.model.chat.ChatRoom;
import com.puppy.talk.model.chat.ChatRoomIdentity;
import com.puppy.talk.model.pet.Pet;
import com.puppy.talk.model.pet.PetIdentity;
import com.puppy.talk.model.pet.PersonaIdentity;
import com.puppy.talk.model.user.UserIdentity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PetRegistrationService {

    private final PetRepository petRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final PersonaRepository personaRepository;

    @Transactional
    public PetRegistrationResult registerPet(
        UserIdentity userId,
        PersonaIdentity personaId,
        String name,
        String breed,
        int age,
        String profileImageUrl
    ) {
        // 사용자 존재 확인
        if (!userRepository.findByIdentity(userId).isPresent()) {
            throw new UserNotFoundException(userId);
        }

        // 페르소나 존재 확인
        if (!personaRepository.findByIdentity(personaId).isPresent()) {
            throw new PersonaNotFoundException(personaId);
        }

        // 펫 생성 (identity는 null로 설정하여 새 ID 생성)
        Pet newPet = new Pet(
            null, // PetIdentity는 repository에서 생성
            userId,
            personaId,
            name,
            breed,
            age,
            profileImageUrl
        );

        // 펫 저장
        Pet savedPet = petRepository.save(newPet);

        // 채팅방 생성 (펫 이름을 기반으로 채팅방 이름 생성)
        String chatRoomName = savedPet.name() + "와의 채팅방";
        ChatRoom newChatRoom = new ChatRoom(
            null, // ChatRoomIdentity는 repository에서 생성
            savedPet.identity(),
            chatRoomName,
            null // 초기에는 메시지가 없음
        );

        ChatRoom savedChatRoom = chatRoomRepository.save(newChatRoom);

        return new PetRegistrationResult(savedPet, savedChatRoom);
    }

    public record PetRegistrationResult(Pet pet, ChatRoom chatRoom) {}
}