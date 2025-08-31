"""
Custom exceptions for PuppyTalk AI Service
"""

from typing import Any, Dict, Optional


class AIServiceError(Exception):
    """Base exception for AI service errors"""
    
    def __init__(
        self, 
        message: str, 
        error_code: str = "AI_SERVICE_ERROR",
        details: Optional[Dict[str, Any]] = None
    ):
        self.message = message
        self.error_code = error_code
        self.details = details or {}
        super().__init__(self.message)


class GrokAPIError(AIServiceError):
    """Grok API related errors"""
    
    def __init__(
        self, 
        message: str, 
        status_code: Optional[int] = None,
        response_data: Optional[Dict[str, Any]] = None,
        retry_after: Optional[int] = None
    ):
        self.status_code = status_code
        self.response_data = response_data or {}
        self.retry_after = retry_after
        super().__init__(
            message=message,
            error_code="GROK_API_ERROR",
            details={
                "status_code": status_code,
                "response_data": response_data,
                "retry_after": retry_after
            }
        )
    
    @property
    def is_retryable(self) -> bool:
        """Check if this error is retryable"""
        if not self.status_code:
            return True  # Network errors are usually retryable
        
        # 429 (Too Many Requests), 5xx (Server Errors) are retryable
        return self.status_code == 429 or (500 <= self.status_code < 600)



class ValidationError(AIServiceError):
    """Input validation error"""
    
    def __init__(self, message: str, field: Optional[str] = None):
        super().__init__(
            message=message,
            error_code="VALIDATION_ERROR",
            details={"field": field} if field else {}
        )


class PersonaNotFoundError(AIServiceError):
    """Persona not found error"""
    
    def __init__(self, persona_id: str):
        super().__init__(
            message=f"Persona not found: {persona_id}",
            error_code="PERSONA_NOT_FOUND",
            details={"persona_id": persona_id}
        )


class MessageGenerationError(AIServiceError):
    """Message generation error"""
    
    def __init__(self, message: str, context: Optional[Dict[str, Any]] = None):
        super().__init__(
            message=message,
            error_code="MESSAGE_GENERATION_ERROR",
            details={"context": context} if context else {}
        )