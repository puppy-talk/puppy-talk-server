package com.puppy.talk.pet;

import com.puppy.talk.dto.PetRegistrationResult;
import com.puppy.talk.chat.ChatRoomRepository;
import com.puppy.talk.user.UserRepository;
import com.puppy.talk.user.UserNotFoundException;
import com.puppy.talk.chat.ChatRoom;
import com.puppy.talk.user.UserIdentity;
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
        if (userRepository.findByIdentity(userId).isEmpty()) {
            throw new UserNotFoundException(userId);
        }

        if (personaRepository.findByIdentity(personaId).isEmpty()) {
            throw new PersonaNotFoundException(personaId);
        }

        Pet pet = new Pet(
            null,
            userId,
            personaId,
            name,
            breed,
            age,
            profileImageUrl
        );

        Pet savedPet = petRepository.save(pet);

        String roomTitle = savedPet.name() + "와의 채팅방";
        ChatRoom newChatRoom = ChatRoom.of(
            savedPet.identity(),
            roomTitle,
            null
        );

        ChatRoom savedChatRoom = chatRoomRepository.save(newChatRoom);

        return new PetRegistrationResult(savedPet, savedChatRoom);
    }
}