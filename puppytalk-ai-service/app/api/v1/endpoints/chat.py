"""
Chat API endpoints for AI message generation
"""

import time
import uuid
from typing import Dict, Any
from fastapi import APIRouter, HTTPException, Depends, BackgroundTasks
import structlog

from app.core.config import get_settings
from app.core.exceptions import MessageGenerationError, GrokAPIError, ValidationError
from app.models.requests import ChatRequest, InactivityNotificationRequest
from app.models.responses import ChatResponse, InactivityNotificationResponse, ErrorResponse, ErrorDetail
from app.services.grok_service import GrokService
from app.utils.validators import validate_chat_request, validate_notification_request

logger = structlog.get_logger(__name__)
router = APIRouter()
settings = get_settings()

# Constants
MAX_RESPONSE_TIME_MS = 30000  # 30 seconds
MIN_RESPONSE_TIME_MS = 100    # 100ms


def get_grok_service() -> GrokService:
    """Dependency to get Grok service instance"""
    return GrokService()


@router.post(
    "/chat/generate",
    response_model=ChatResponse,
    summary="Generate AI pet response",
    description="Generate a response message from AI pet based on user input and conversation context"
)
async def generate_chat_response(
    request: ChatRequest,
    background_tasks: BackgroundTasks,
    grok_service: GrokService = Depends(get_grok_service)
) -> ChatResponse:
    """
    Generate AI pet response for user message
    
    Args:
        request: Chat generation request
        background_tasks: Background tasks for async operations
        grok_service: Grok service instance
        
    Returns:
        Generated chat response
        
    Raises:
        HTTPException: If generation fails
    """
    start_time = time.time()
    request_id = str(uuid.uuid4())
    
    logger.info(
        "Chat generation request received",
        request_id=request_id,
        user_id=request.user_id,
        pet_id=request.pet_id,
        chat_room_id=request.chat_room_id,
        message_length=len(request.user_message),
        history_length=len(request.conversation_history)
    )
    
    try:
        # Validate request
        validation_errors = validate_chat_request(request)
        if validation_errors:
            raise ValidationError(
                message=f"Request validation failed: {', '.join(validation_errors)}"
            )
        
        # Generate pet response using Grok API
        pet_response = await grok_service.generate_pet_response(
            pet_name=request.pet_persona.name,
            pet_persona=request.pet_persona.type.value,
            personality_traits=request.pet_persona.personality_traits,
            conversation_history=request.conversation_history,
            user_message=request.user_message,
            max_tokens=request.max_tokens or 150,
            temperature=request.temperature or 0.8
        )
        
        generation_time = int((time.time() - start_time) * 1000)
        
        # Validate response time
        if generation_time > MAX_RESPONSE_TIME_MS:
            logger.warning(
                "Slow response time detected",
                request_id=request_id,
                generation_time_ms=generation_time
            )
        
        # Create response
        response = ChatResponse(
            success=True,
            message_id=str(uuid.uuid4()),
            content=pet_response,
            model=settings.grok_model,
            generation_time_ms=generation_time,
            conversation_id=f"{request.chat_room_id}_{int(time.time())}"
        )
        
        # Add debug info if in debug mode
        if settings.debug:
            response.debug_info = {
                "request_id": request_id,
                "processing_steps": [
                    "validation_completed",
                    "grok_api_called",
                    "response_generated"
                ],
                "grok_parameters": {
                    "model": settings.grok_model,
                    "max_tokens": request.max_tokens,
                    "temperature": request.temperature
                }
            }
        
        logger.info(
            "Chat generation completed successfully",
            request_id=request_id,
            generation_time_ms=generation_time,
            response_length=len(pet_response)
        )
        
        # Schedule background tasks
        background_tasks.add_task(
            log_generation_metrics,
            request_id=request_id,
            user_id=request.user_id,
            generation_time_ms=generation_time,
            success=True,
            model=settings.grok_model,
            response_length=len(pet_response)
        )
        
        return response
        
    except ValidationError as e:
        logger.warning(
            "Chat generation validation error",
            request_id=request_id,
            error=str(e)
        )
        raise HTTPException(
            status_code=400,
            detail=ErrorDetail(
                error_code=e.error_code,
                message=e.message,
                details=e.details
            ).dict()
        )
        
    except MessageGenerationError as e:
        logger.error(
            "Chat generation failed",
            request_id=request_id,
            error=str(e),
            details=e.details
        )
        raise HTTPException(
            status_code=500,
            detail=ErrorDetail(
                error_code=e.error_code,
                message=e.message,
                details=e.details
            ).dict()
        )
        
    except GrokAPIError as e:
        logger.error(
            "Grok API error during chat generation",
            request_id=request_id,
            status_code=e.status_code,
            error=str(e)
        )
        raise HTTPException(
            status_code=502,
            detail=ErrorDetail(
                error_code=e.error_code,
                message="AI service temporarily unavailable",
                details={"upstream_error": e.message}
            ).dict()
        )
        
    except Exception as e:
        logger.error(
            "Unexpected error during chat generation",
            request_id=request_id,
            error=str(e),
            exc_info=True
        )
        raise HTTPException(
            status_code=500,
            detail=ErrorDetail(
                error_code="INTERNAL_ERROR",
                message="Internal server error occurred"
            ).dict()
        )


@router.post(
    "/chat/inactivity-notification",
    response_model=InactivityNotificationResponse,
    summary="Generate inactivity notification",
    description="Generate a notification message when user has been inactive for a specified period"
)
async def generate_inactivity_notification(
    request: InactivityNotificationRequest,
    background_tasks: BackgroundTasks,
    grok_service: GrokService = Depends(get_grok_service)
) -> InactivityNotificationResponse:
    """
    Generate inactivity notification message
    
    Args:
        request: Inactivity notification request
        background_tasks: Background tasks for async operations
        grok_service: Grok service instance
        
    Returns:
        Generated notification response
        
    Raises:
        HTTPException: If generation fails
    """
    start_time = time.time()
    request_id = str(uuid.uuid4())
    
    logger.info(
        "Inactivity notification request received",
        request_id=request_id,
        user_id=request.user_id,
        pet_id=request.pet_id,
        hours_since_last_activity=request.hours_since_last_activity,
        time_of_day=request.time_of_day
    )
    
    try:
        # Validate request
        validation_errors = validate_notification_request(request)
        if validation_errors:
            raise ValidationError(
                message=f"Request validation failed: {', '.join(validation_errors)}"
            )
        
        # Generate notification message
        notification_message = await grok_service.generate_inactivity_notification(
            pet_name=request.pet_persona.name,
            pet_persona=request.pet_persona.type.value,
            personality_traits=request.pet_persona.personality_traits,
            hours_since_last_activity=request.hours_since_last_activity,
            time_of_day=request.time_of_day,
            last_messages=request.last_messages
        )
        
        generation_time = int((time.time() - start_time) * 1000)
        
        # Validate response time
        if generation_time > MAX_RESPONSE_TIME_MS:
            logger.warning(
                "Slow notification generation detected",
                request_id=request_id,
                generation_time_ms=generation_time
            )
        
        response = InactivityNotificationResponse(
            success=True,
            notification_message=notification_message,
            generation_time_ms=generation_time,
            priority="normal"  # Default priority
        )
        
        logger.info(
            "Inactivity notification generated successfully",
            request_id=request_id,
            generation_time_ms=generation_time,
            message_length=len(notification_message)
        )
        
        # Schedule background tasks
        background_tasks.add_task(
            log_notification_metrics,
            request_id=request_id,
            user_id=request.user_id,
            generation_time_ms=generation_time,
            success=True,
            message_length=len(notification_message)
        )
        
        return response
        
    except ValidationError as e:
        logger.warning(
            "Notification generation validation error",
            request_id=request_id,
            error=str(e)
        )
        raise HTTPException(
            status_code=400,
            detail=ErrorDetail(
                error_code=e.error_code,
                message=e.message,
                details=e.details
            ).dict()
        )
        
    except MessageGenerationError as e:
        logger.error(
            "Notification generation failed",
            request_id=request_id,
            error=str(e)
        )
        raise HTTPException(
            status_code=500,
            detail=ErrorDetail(
                error_code=e.error_code,
                message=e.message,
                details=e.details
            ).dict()
        )
        
    except Exception as e:
        logger.error(
            "Unexpected error during notification generation",
            request_id=request_id,
            error=str(e),
            exc_info=True
        )
        raise HTTPException(
            status_code=500,
            detail=ErrorDetail(
                error_code="INTERNAL_ERROR",
                message="Internal server error occurred"
            ).dict()
        )


async def log_generation_metrics(
    request_id: str,
    user_id: int,
    generation_time_ms: int,
    success: bool,
    model: str,
    response_length: int
) -> None:
    """Background task to log generation metrics with enhanced information"""
    try:
        logger.info(
            "Generation metrics logged",
            request_id=request_id,
            user_id=user_id,
            generation_time_ms=generation_time_ms,
            success=success,
            model=model,
            response_length=response_length,
            performance_category="fast" if generation_time_ms < 1000 else "normal" if generation_time_ms < 5000 else "slow"
        )
    except Exception as e:
        logger.error(
            "Failed to log generation metrics",
            request_id=request_id,
            error=str(e)
        )


async def log_notification_metrics(
    request_id: str,
    user_id: int,
    generation_time_ms: int,
    success: bool,
    message_length: int
) -> None:
    """Background task to log notification metrics with enhanced information"""
    try:
        logger.info(
            "Notification metrics logged",
            request_id=request_id,
            user_id=user_id,
            generation_time_ms=generation_time_ms,
            success=success,
            message_length=message_length,
            performance_category="fast" if generation_time_ms < 1000 else "normal" if generation_time_ms < 5000 else "slow"
        )
    except Exception as e:
        logger.error(
            "Failed to log notification metrics",
            request_id=request_id,
            error=str(e)
        )