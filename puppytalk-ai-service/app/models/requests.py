"""
Request models for PuppyTalk AI Service
"""

from typing import List, Optional, Dict, Any
from pydantic import BaseModel, Field, validator
from enum import Enum


class MessageRole(str, Enum):
    """Message role enumeration"""
    USER = "user"
    ASSISTANT = "assistant"
    SYSTEM = "system"


class PersonaType(str, Enum):
    """Pet persona type enumeration"""
    FRIENDLY = "friendly"
    PLAYFUL = "playful"
    CALM = "calm"
    ENERGETIC = "energetic"
    WISE = "wise"
    MISCHIEVOUS = "mischievous"


class ChatMessage(BaseModel):
    """Individual chat message"""
    role: MessageRole
    content: str = Field(..., min_length=1, max_length=2000)
    timestamp: Optional[str] = None


class PetPersona(BaseModel):
    """Pet persona configuration"""
    type: PersonaType
    name: str = Field(..., min_length=1, max_length=50)
    breed: Optional[str] = Field(None, max_length=50)
    age: Optional[int] = Field(None, ge=0, le=30)
    personality_traits: List[str] = Field(default_factory=list, max_items=5)
    custom_instructions: Optional[str] = Field(None, max_length=500)


class ChatRequest(BaseModel):
    """Chat completion request"""
    user_id: int = Field(..., gt=0)
    pet_id: int = Field(..., gt=0)
    chat_room_id: int = Field(..., gt=0)
    
    # Current user message
    user_message: str = Field(..., min_length=1, max_length=1000)
    
    # Pet persona
    pet_persona: PetPersona
    
    # Conversation history (last N messages)
    conversation_history: List[ChatMessage] = Field(default_factory=list, max_items=20)
    
    # Additional context
    context: Optional[Dict[str, Any]] = Field(default_factory=dict)
    
    # Generation parameters
    max_tokens: Optional[int] = Field(150, ge=10, le=500)
    temperature: Optional[float] = Field(0.8, ge=0.0, le=2.0)
    
    @validator('conversation_history')
    def validate_conversation_history(cls, v):
        """Validate conversation history"""
        if len(v) > 20:
            # Keep only the most recent 20 messages
            return v[-20:]
        return v


class InactivityNotificationRequest(BaseModel):
    """Request for generating inactivity notification message"""
    user_id: int = Field(..., gt=0)
    pet_id: int = Field(..., gt=0)
    chat_room_id: int = Field(..., gt=0)
    
    # Pet persona
    pet_persona: PetPersona
    
    # Last conversation context
    last_messages: List[ChatMessage] = Field(default_factory=list, max_items=10)
    
    # Hours since last activity
    hours_since_last_activity: int = Field(..., ge=2, le=72)
    
    # Time context (morning, afternoon, evening, night)
    time_of_day: Optional[str] = Field(None, regex="^(morning|afternoon|evening|night)$")


class HealthCheckRequest(BaseModel):
    """Health check request"""
    service_name: Optional[str] = None