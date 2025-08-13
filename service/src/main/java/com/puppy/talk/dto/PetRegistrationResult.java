package com.puppy.talk.dto;

import com.puppy.talk.chat.ChatRoom;
import com.puppy.talk.pet.Pet;

public record PetRegistrationResult(Pet pet, ChatRoom chatRoom) {
}