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
        response_data: Optional[Dict[str, Any]] = None
    ):
        self.status_code = status_code
        self.response_data = response_data or {}
        super().__init__(
            message=message,
            error_code="GROK_API_ERROR",
            details={
                "status_code": status_code,
                "response_data": response_data
            }
        )



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