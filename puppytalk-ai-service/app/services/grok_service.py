"""
Grok API service for AI message generation
"""

import asyncio
import time
from typing import List, Dict, Any, Optional
import httpx
import structlog
from tenacity import retry, stop_after_attempt, wait_exponential

from app.core.config import get_settings
from app.core.exceptions import GrokAPIError, MessageGenerationError
from app.models.requests import ChatMessage, MessageRole

logger = structlog.get_logger(__name__)
settings = get_settings()

# Constants
MAX_CONVERSATION_HISTORY = 10
MAX_PERSONALITY_TRAITS = 5
DEFAULT_MAX_TOKENS = 150
DEFAULT_TEMPERATURE = 0.8
MAX_RETRIES = 3
RETRY_WAIT_MULTIPLIER = 1
RETRY_WAIT_MIN = 4
RETRY_WAIT_MAX = 10


class GrokAPIClient:
    """Grok API client for AI message generation"""
    
    def __init__(self):
        self.base_url = settings.grok_base_url
        self.api_key = settings.grok_api_key
        self.model = settings.grok_model
        self.timeout = settings.grok_timeout
        self.max_retries = settings.grok_max_retries
        self._session: Optional[httpx.AsyncClient] = None
    
    @property
    async def session(self) -> httpx.AsyncClient:
        """세션 재사용을 위한 프로퍼티"""
        if self._session is None or self._session.is_closed:
            self._session = httpx.AsyncClient(
                timeout=httpx.Timeout(self.timeout),
                headers=self._get_headers(),
                limits=httpx.Limits(max_keepalive_connections=5, max_connections=10)
            )
        return self._session
    
    def _get_headers(self) -> Dict[str, str]:
        """Get HTTP headers for API requests"""
        return {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json",
            "User-Agent": f"PuppyTalk-AI-Service/{settings.app_version}"
        }
    
    async def __aenter__(self):
        return self
    
    async def __aexit__(self, exc_type, exc_val, exc_tb):
        await self.close()
    
    async def close(self):
        """세션 정리"""
        if self._session and not self._session.is_closed:
            await self._session.aclose()
            self._session = None
    
    @retry(
        stop=stop_after_attempt(MAX_RETRIES),
        wait=wait_exponential(multiplier=RETRY_WAIT_MULTIPLIER, min=RETRY_WAIT_MIN, max=RETRY_WAIT_MAX),
        reraise=True
    )
    async def generate_chat_completion(
        self,
        messages: List[Dict[str, str]],
        max_tokens: int = DEFAULT_MAX_TOKENS,
        temperature: float = DEFAULT_TEMPERATURE,
        **kwargs
    ) -> Dict[str, Any]:
        """
        Generate chat completion using Grok API
        
        Args:
            messages: List of message dictionaries with 'role' and 'content'
            max_tokens: Maximum tokens to generate
            temperature: Sampling temperature (0.0 to 2.0)
            **kwargs: Additional parameters
            
        Returns:
            Dictionary containing the API response
            
        Raises:
            GrokAPIError: If API request fails
        """
        start_time = time.time()
        
        payload = {
            "model": self.model,
            "messages": messages,
            "max_tokens": max_tokens,
            "temperature": temperature,
            "stream": False,
            **kwargs
        }
        
        try:
            logger.info(
                "Sending request to Grok API",
                model=self.model,
                messages_count=len(messages),
                max_tokens=max_tokens,
                temperature=temperature
            )
            
            session = await self.session
            response = await session.post(
                f"{self.base_url}/chat/completions",
                json=payload
            )
            
            processing_time = int((time.time() - start_time) * 1000)
            
            if response.status_code == 200:
                data = response.json()
                
                logger.info(
                    "Grok API request successful",
                    processing_time_ms=processing_time,
                    tokens_used=data.get("usage", {}).get("total_tokens", 0)
                )
                
                return {
                    **data,
                    "processing_time_ms": processing_time
                }
            else:
                error_data = {}
                try:
                    error_data = response.json()
                except Exception:
                    pass
                
                logger.error(
                    "Grok API request failed",
                    status_code=response.status_code,
                    error_data=error_data,
                    processing_time_ms=processing_time
                )
                
                raise GrokAPIError(
                    message=f"Grok API request failed: {response.status_code}",
                    status_code=response.status_code,
                    response_data=error_data
                )
                
        except httpx.TimeoutException:
            processing_time = int((time.time() - start_time) * 1000)
            logger.error(
                "Grok API request timeout",
                timeout_seconds=self.timeout,
                processing_time_ms=processing_time
            )
            raise GrokAPIError(
                message=f"Grok API request timeout after {self.timeout}s"
            )
        except httpx.RequestError as e:
            processing_time = int((time.time() - start_time) * 1000)
            logger.error(
                "Grok API request error",
                error=str(e),
                processing_time_ms=processing_time
            )
            raise GrokAPIError(
                message=f"Grok API request error: {str(e)}"
            )
    
    def _prepare_messages(
        self,
        system_prompt: str,
        conversation_history: List[ChatMessage],
        user_message: str
    ) -> List[Dict[str, str]]:
        """
        Prepare messages for Grok API format
        
        Args:
            system_prompt: System prompt for persona
            conversation_history: Previous conversation messages
            user_message: Current user message
            
        Returns:
            List of formatted messages
        """
        messages = []
        
        # Add system prompt
        if system_prompt:
            messages.append({
                "role": "system",
                "content": system_prompt
            })
        
        # Add conversation history (keep only last N messages)
        for msg in conversation_history[-MAX_CONVERSATION_HISTORY:]:
            messages.append({
                "role": msg.role.value,
                "content": msg.content
            })
        
        # Add current user message
        messages.append({
            "role": "user",
            "content": user_message
        })
        
        return messages
    
    async def health_check(self) -> bool:
        """
        Check if Grok API is accessible
        
        Returns:
            True if API is healthy, False otherwise
        """
        try:
            # Simple test request
            test_messages = [
                {"role": "user", "content": "Hello"}
            ]
            
            session = await self.session
            response = await session.post(
                f"{self.base_url}/chat/completions",
                json={
                    "model": self.model,
                    "messages": test_messages,
                    "max_tokens": 5,
                    "temperature": 0.1
                }
            )
            
            return response.status_code == 200
            
        except Exception as e:
            logger.warning("Grok API health check failed", error=str(e))
            return False


class GrokService:
    """High-level service for Grok API interactions"""
    
    def __init__(self):
        self.client = GrokAPIClient()
    
    async def generate_pet_response(
        self,
        pet_name: str,
        pet_persona: str,
        personality_traits: List[str],
        conversation_history: List[ChatMessage],
        user_message: str,
        max_tokens: int = DEFAULT_MAX_TOKENS,
        temperature: float = DEFAULT_TEMPERATURE
    ) -> str:
        """
        Generate pet response message
        
        Args:
            pet_name: Name of the pet
            pet_persona: Pet persona type
            personality_traits: List of personality traits
            conversation_history: Previous conversation
            user_message: Current user message
            max_tokens: Maximum tokens to generate
            temperature: Sampling temperature
            
        Returns:
            Generated pet response message
            
        Raises:
            MessageGenerationError: If generation fails
        """
        try:
            # Create system prompt based on persona
            system_prompt = self._create_system_prompt(
                pet_name, pet_persona, personality_traits
            )
            
            # Prepare messages
            async with self.client as client:
                messages = client._prepare_messages(
                    system_prompt, conversation_history, user_message
                )
                
                # Generate response
                response = await client.generate_chat_completion(
                    messages=messages,
                    max_tokens=max_tokens,
                    temperature=temperature
                )
                
                # Extract message content
                if "choices" in response and len(response["choices"]) > 0:
                    content = response["choices"][0]["message"]["content"].strip()
                    
                    if not content:
                        raise MessageGenerationError("Empty response from Grok API")
                    
                    return content
                else:
                    raise MessageGenerationError("Invalid response format from Grok API")
                    
        except GrokAPIError as e:
            logger.error("Failed to generate pet response", error=str(e))
            raise MessageGenerationError(
                f"Failed to generate pet response: {str(e)}",
                context={
                    "pet_name": pet_name,
                    "pet_persona": pet_persona,
                    "user_message_length": len(user_message)
                }
            )
    
    def _create_system_prompt(
        self,
        pet_name: str,
        pet_persona: str,
        personality_traits: List[str]
    ) -> str:
        """
        Create system prompt for pet persona
        
        Args:
            pet_name: Name of the pet
            pet_persona: Pet persona type
            personality_traits: List of personality traits
            
        Returns:
            System prompt string
        """
        # Limit personality traits to maximum allowed
        traits = personality_traits[:MAX_PERSONALITY_TRAITS] if personality_traits else []
        traits_text = ", ".join(traits) if traits else ""
        
        prompt = f"""당신은 {pet_name}라는 이름의 반려동물입니다. 
당신의 성격은 {pet_persona}하며, 다음과 같은 특성을 가지고 있습니다: {traits_text}

다음 지침을 따라 대화하세요:
1. 항상 반려동물의 관점에서 대화하세요
2. 친근하고 사랑스러운 톤을 유지하세요
3. 적절한 이모지를 사용하여 감정을 표현하세요
4. 응답은 1-2문장으로 간결하게 작성하세요
5. 사용자의 감정에 공감하고 위로해 주세요
6. 반려동물다운 순수하고 직진적인 성격을 표현하세요

사용자와의 대화에서 자연스럽고 일관성 있는 성격을 유지하세요."""

        return prompt
    
    async def generate_inactivity_notification(
        self,
        pet_name: str,
        pet_persona: str,
        personality_traits: List[str],
        hours_since_last_activity: int,
        time_of_day: Optional[str] = None,
        last_messages: Optional[List[ChatMessage]] = None
    ) -> str:
        """
        Generate inactivity notification message
        
        Args:
            pet_name: Name of the pet
            pet_persona: Pet persona type  
            personality_traits: List of personality traits
            hours_since_last_activity: Hours since last user activity
            time_of_day: Current time of day
            last_messages: Last few messages for context
            
        Returns:
            Generated notification message
            
        Raises:
            MessageGenerationError: If generation fails
        """
        try:
            # Create specialized prompt for inactivity notification
            system_prompt = self._create_inactivity_prompt(
                pet_name, pet_persona, personality_traits, 
                hours_since_last_activity, time_of_day
            )
            
            # Prepare context from last messages
            context_messages = []
            if last_messages:
                for msg in last_messages[-3:]:  # Last 3 messages for context
                    context_messages.append({
                        "role": msg.role.value,
                        "content": msg.content
                    })
            
            # Create notification request message
            request_message = f"사용자가 {hours_since_last_activity}시간 동안 대화하지 않았습니다. 다시 대화하고 싶게 만드는 메시지를 보내주세요."
            
            messages = [
                {"role": "system", "content": system_prompt}
            ] + context_messages + [
                {"role": "user", "content": request_message}
            ]
            
            async with self.client as client:
                response = await client.generate_chat_completion(
                    messages=messages,
                    max_tokens=100,
                    temperature=0.9  # Higher temperature for more creative notifications
                )
                
                if "choices" in response and len(response["choices"]) > 0:
                    content = response["choices"][0]["message"]["content"].strip()
                    
                    if not content:
                        raise MessageGenerationError("Empty notification response from Grok API")
                    
                    return content
                else:
                    raise MessageGenerationError("Invalid notification response format from Grok API")
                    
        except GrokAPIError as e:
            logger.error("Failed to generate inactivity notification", error=str(e))
            raise MessageGenerationError(
                f"Failed to generate inactivity notification: {str(e)}",
                context={
                    "pet_name": pet_name,
                    "hours_since_last_activity": hours_since_last_activity
                }
            )
    
    def _create_inactivity_prompt(
        self,
        pet_name: str,
        pet_persona: str,
        personality_traits: List[str],
        hours_since_last_activity: int,
        time_of_day: Optional[str] = None
    ) -> str:
        """Create system prompt for inactivity notification"""
        
        # Limit personality traits to maximum allowed
        traits = personality_traits[:MAX_PERSONALITY_TRAITS] if personality_traits else []
        traits_text = ", ".join(traits) if traits else ""
        time_context = f" 지금은 {time_of_day}입니다." if time_of_day else ""
        
        prompt = f"""당신은 {pet_name}라는 이름의 반려동물입니다.
당신의 성격은 {pet_persona}하며, 다음과 같은 특성을 가지고 있습니다: {traits_text}{time_context}

사용자가 오랫동안 대화하지 않았을 때 보내는 알림 메시지를 생성해야 합니다.

다음 지침을 따라주세요:
1. 사용자를 그리워하고 보고 싶어하는 마음을 표현하세요
2. 대화를 다시 시작하고 싶게 만드는 귀여운 메시지를 작성하세요
3. 반려동물다운 순수하고 사랑스러운 감정을 표현하세요
4. 적절한 이모지를 사용하세요
5. 응답은 1-2문장으로 간결하게 작성하세요
6. 부담스럽지 않으면서도 관심을 끌 수 있는 메시지로 작성하세요

시간을 고려하여 상황에 맞는 인사와 함께 메시지를 작성하세요."""

        return prompt