"""
Response models for PuppyTalk AI Service
"""

from typing import Optional, Dict, Any, List
from pydantic import BaseModel, Field
from datetime import datetime
from enum import Enum


class ResponseStatus(str, Enum):
    """Response status enumeration"""
    SUCCESS = "success"
    ERROR = "error"
    PARTIAL = "partial"


class ChatResponse(BaseModel):
    """Chat completion response"""
    success: bool = True
    message_id: Optional[str] = None
    content: str = Field(..., description="Generated pet message")
    
    # Generation metadata
    model: str = Field(..., description="Model used for generation")
    tokens_used: Optional[int] = Field(None, description="Number of tokens used")
    generation_time_ms: Optional[int] = Field(None, description="Generation time in milliseconds")
    
    # Context preservation
    conversation_id: Optional[str] = None
    timestamp: datetime = Field(default_factory=datetime.utcnow)
    
    # Debug information (only in debug mode)
    debug_info: Optional[Dict[str, Any]] = None


class InactivityNotificationResponse(BaseModel):
    """Inactivity notification response"""
    success: bool = True
    notification_message: str = Field(..., description="Generated notification message")
    
    # Metadata
    generation_time_ms: Optional[int] = None
    timestamp: datetime = Field(default_factory=datetime.utcnow)
    
    # Notification context
    suggested_send_time: Optional[datetime] = None
    priority: Optional[str] = Field(None, regex="^(low|normal|high)$")


class ErrorDetail(BaseModel):
    """Error detail information"""
    error_code: str
    message: str
    details: Optional[Dict[str, Any]] = None
    timestamp: datetime = Field(default_factory=datetime.utcnow)


class ErrorResponse(BaseModel):
    """Standardized error response"""
    success: bool = False
    error: ErrorDetail
    trace_id: Optional[str] = None
    request_id: Optional[str] = None


class HealthCheckResponse(BaseModel):
    """Health check response"""
    status: str = Field(..., description="Service status")
    timestamp: datetime = Field(default_factory=datetime.utcnow)
    version: str = Field(..., description="Service version")
    
    # Service dependencies status
    dependencies: Dict[str, str] = Field(default_factory=dict)
    




class BatchChatResponse(BaseModel):
    """Batch chat processing response"""
    success: bool = True
    processed_count: int = 0
    successful_count: int = 0
    failed_count: int = 0
    
    # Individual responses
    responses: List[ChatResponse] = Field(default_factory=list)
    errors: List[ErrorResponse] = Field(default_factory=list)
    
    # Processing metadata
    total_processing_time_ms: Optional[int] = None
    timestamp: datetime = Field(default_factory=datetime.utcnow)