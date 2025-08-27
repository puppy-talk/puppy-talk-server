package com.puppytalk.pet;

import com.puppytalk.support.EntityId;

public class PetId extends EntityId {
    
    private PetId(Long value) {
        super(value);
    }
    
    /**
     * 하나의 매개변수를 받아 타입 변환 (데이터베이스에서 조회된 값용)
     */
    public static PetId from(Long value) {
        return new PetId(value);
    }
    
    /**
     * 항상 새로운 인스턴스를 생성해 반환 (신규 생성용)
     */
    public static PetId create() {
        return new PetId(null);
    }
}