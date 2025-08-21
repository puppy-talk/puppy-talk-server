package com.puppy.talk.pet;

import com.puppy.talk.user.UserIdentity;

public record Pet(
    PetIdentity identity,
    UserIdentity userId,
    PersonaIdentity personaId,
    String name,
    String breed,
    int age,
    String profileImageUrl
) {

    public Pet {
        // identity can be null for new pets before saving
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
        if (personaId == null) {
            throw new IllegalArgumentException("PersonaId cannot be null");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (age < 0) {
            throw new IllegalArgumentException("Age cannot be negative");
        }
    }

    /**
     * 펫이 특정 사용자에게 속하는지 확인합니다.
     * 
     * @param userId 확인할 사용자 ID
     * @return 소유자가 맞으면 true, 아니면 false
     */
    public boolean belongsTo(UserIdentity userId) {
        return this.userId.equals(userId);
    }

    /**
     * 펫이 특정 페르소나를 가지고 있는지 확인합니다.
     * 
     * @param personaId 확인할 페르소나 ID
     * @return 페르소나가 맞으면 true, 아니면 false
     */
    public boolean hasPersona(PersonaIdentity personaId) {
        return this.personaId.equals(personaId);
    }

    /**
     * 채팅방 제목을 생성합니다.
     * 
     * @return "{펫이름}와의 채팅방" 형식의 제목
     */
    public String generateChatRoomTitle() {
        return name + "와의 채팅방";
    }

    /**
     * 펫의 나이 그룹을 반환합니다.
     * 
     * @return 나이 그룹 (PUPPY, YOUNG, ADULT, SENIOR)
     */
    public AgeGroup getAgeGroup() {
        if (age <= 1) return AgeGroup.PUPPY;
        if (age <= 3) return AgeGroup.YOUNG;
        if (age <= 7) return AgeGroup.ADULT;
        return AgeGroup.SENIOR;
    }

    /**
     * 펫의 나이 그룹
     */
    public enum AgeGroup {
        PUPPY("강아지"),
        YOUNG("어린이"),
        ADULT("성인"),
        SENIOR("노령");

        private final String description;

        AgeGroup(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}