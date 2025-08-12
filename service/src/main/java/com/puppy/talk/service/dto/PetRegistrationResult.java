package com.puppy.talk.service.dto;

import com.puppy.talk.model.chat.ChatRoom;
import com.puppy.talk.model.pet.Pet;

public record PetRegistrationResult(Pet pet, ChatRoom chatRoom) {
}