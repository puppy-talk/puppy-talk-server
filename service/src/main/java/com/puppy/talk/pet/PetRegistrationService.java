package com.puppy.talk.pet;

import com.puppy.talk.dto.PetRegistrationResult;
import com.puppy.talk.chat.ChatRoomRepository;
import com.puppy.talk.user.UserRepository;
import com.puppy.talk.user.UserNotFoundException;
import com.puppy.talk.chat.ChatRoom;
import com.puppy.talk.user.UserIdentity;
import com.puppy.talk.pet.command.PetCreateCommand;
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
    public PetRegistrationResult createPet(PetCreateCommand command) {
        if (userRepository.findByIdentity(command.userId()).isEmpty()) {
            throw new UserNotFoundException(command.userId());
        }

        if (personaRepository.findByIdentity(command.personaId()).isEmpty()) {
            throw new PersonaNotFoundException(command.personaId());
        }

        Pet pet = new Pet(
            null,
            command.userId(),
            command.personaId(),
            command.name(),
            command.breed(),
            command.age(),
            command.profileImageUrl()
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