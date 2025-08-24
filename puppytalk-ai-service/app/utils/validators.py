"""
Input validation utilities for PuppyTalk AI Service
"""

from typing import List, Optional
from app.models.requests import ChatRequest, InactivityNotificationRequest


def validate_chat_request(request: ChatRequest) -> List[str]:
    """
    Validate chat request for business logic constraints
    
    Args:
        request: Chat request to validate
        
    Returns:
        List of validation error messages (empty if valid)
    """
    errors = []
    
    # Validate user message
    if not request.user_message.strip():
        errors.append("User message cannot be empty")
    
    if len(request.user_message) > 1000:
        errors.append("User message exceeds maximum length of 1000 characters")
    
    # Validate pet persona
    if not request.pet_persona.name.strip():
        errors.append("Pet name cannot be empty")
    
    if len(request.pet_persona.personality_traits) > 5:
        errors.append("Pet can have maximum 5 personality traits")
    
    # Validate conversation history
    if len(request.conversation_history) > 20:
        errors.append("Conversation history cannot exceed 20 messages")
    
    # Validate generation parameters
    if request.max_tokens and (request.max_tokens < 10 or request.max_tokens > 500):
        errors.append("max_tokens must be between 10 and 500")
    
    if request.temperature and (request.temperature < 0.0 or request.temperature > 2.0):
        errors.append("temperature must be between 0.0 and 2.0")
    
    return errors


def validate_notification_request(request: InactivityNotificationRequest) -> List[str]:
    """
    Validate inactivity notification request
    
    Args:
        request: Notification request to validate
        
    Returns:
        List of validation error messages (empty if valid)
    """
    errors = []
    
    # Validate pet persona
    if not request.pet_persona.name.strip():
        errors.append("Pet name cannot be empty")
    
    # Validate hours since last activity
    if request.hours_since_last_activity < 2:
        errors.append("Minimum 2 hours since last activity required")
    
    if request.hours_since_last_activity > 72:
        errors.append("Maximum 72 hours since last activity allowed")
    
    # Validate time of day if provided
    valid_times = {"morning", "afternoon", "evening", "night"}
    if request.time_of_day and request.time_of_day not in valid_times:
        errors.append(f"time_of_day must be one of: {', '.join(valid_times)}")
    
    # Validate last messages
    if len(request.last_messages) > 10:
        errors.append("Last messages cannot exceed 10 items")
    
    return errors


def validate_pet_name(name: str) -> bool:
    """
    Validate pet name format
    
    Args:
        name: Pet name to validate
        
    Returns:
        True if valid, False otherwise
    """
    if not name or not name.strip():
        return False
    
    if len(name.strip()) > 50:
        return False
    
    # Allow letters, numbers, spaces, and common pet name characters
    allowed_chars = set("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 -'.")
    return all(c in allowed_chars for c in name)


def validate_personality_traits(traits: List[str]) -> bool:
    """
    Validate personality traits list
    
    Args:
        traits: List of personality traits
        
    Returns:
        True if valid, False otherwise
    """
    if len(traits) > 5:
        return False
    
    for trait in traits:
        if not trait or not trait.strip():
            return False
        if len(trait.strip()) > 50:
            return False
    
    return True



def validate_generation_params(max_tokens: Optional[int], temperature: Optional[float]) -> bool:
    """
    Validate AI generation parameters
    
    Args:
        max_tokens: Maximum tokens to generate
        temperature: Sampling temperature
        
    Returns:
        True if valid, False otherwise
    """
    if max_tokens is not None:
        if max_tokens < 10 or max_tokens > 500:
            return False
    
    if temperature is not None:
        if temperature < 0.0 or temperature > 2.0:
            return False
    
    return True