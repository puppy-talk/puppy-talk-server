package com.puppytalk.pet;

/**
 * 최대 반려동물 개수 초과 예외
 * 
 * 사용자가 허용된 최대 반려동물 개수를 초과하여 생성하려고 할 때 발생합니다.
 */
public class MaxPetsExceededException extends RuntimeException {
    
    private final Long userId;
    private final int maxAllowed;
    private final int currentCount;
    
    public MaxPetsExceededException(Long userId, int maxAllowed, int currentCount) {
        super(String.format("User %d has exceeded maximum pets limit. Current: %d, Max allowed: %d", 
                userId, currentCount, maxAllowed));
        this.userId = userId;
        this.maxAllowed = maxAllowed;
        this.currentCount = currentCount;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public int getMaxAllowed() {
        return maxAllowed;
    }
    
    public int getCurrentCount() {
        return currentCount;
    }
}