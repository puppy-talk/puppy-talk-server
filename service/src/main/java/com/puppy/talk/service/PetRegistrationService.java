package com.puppy.talk.service;

import com.puppy.talk.service.dto.PetRegistrationResult;
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