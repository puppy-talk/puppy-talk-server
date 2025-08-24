"""
Structured logging utilities for PuppyTalk AI Service
"""

import logging
import sys
from typing import Any, Dict, Optional
import structlog
from datetime import datetime

from app.core.config import get_settings


def configure_logging() -> None:
    """Configure structured logging for the application"""
    settings = get_settings()
    
    # Configure structlog
    structlog.configure(
        processors=[
            structlog.stdlib.filter_by_level,
            structlog.stdlib.add_logger_name,
            structlog.stdlib.add_log_level,
            structlog.stdlib.PositionalArgumentsFormatter(),
            structlog.processors.TimeStamper(fmt="ISO"),
            structlog.processors.StackInfoRenderer(),
            structlog.processors.format_exc_info,
            structlog.processors.UnicodeDecoder(),
            structlog.processors.JSONRenderer() if not settings.debug else structlog.dev.ConsoleRenderer(colors=True),
        ],
        context_class=dict,
        logger_factory=structlog.stdlib.LoggerFactory(),
        wrapper_class=structlog.stdlib.BoundLogger,
        cache_logger_on_first_use=True,
    )
    
    # Configure standard library logging
    logging.basicConfig(
        format="%(message)s",
        stream=sys.stdout,
        level=logging.INFO if not settings.debug else logging.DEBUG,
    )
    
    # Set third-party library log levels
    logging.getLogger("httpx").setLevel(logging.WARNING)
    logging.getLogger("httpcore").setLevel(logging.WARNING)
    logging.getLogger("asyncio").setLevel(logging.WARNING)


def get_logger(name: str) -> structlog.BoundLogger:
    """
    Get a structured logger instance
    
    Args:
        name: Logger name (usually __name__)
        
    Returns:
        Configured structured logger
    """
    return structlog.get_logger(name)


class RequestLogger:
    """Request-specific logger with correlation ID"""
    
    def __init__(self, request_id: str, logger: Optional[structlog.BoundLogger] = None):
        self.request_id = request_id
        self.logger = logger or structlog.get_logger()
        self.bound_logger = self.logger.bind(request_id=request_id)
    
    def info(self, msg: str, **kwargs) -> None:
        """Log info level message with request context"""
        self.bound_logger.info(msg, **kwargs)
    
    def warning(self, msg: str, **kwargs) -> None:
        """Log warning level message with request context"""
        self.bound_logger.warning(msg, **kwargs)
    
    def error(self, msg: str, **kwargs) -> None:
        """Log error level message with request context"""
        self.bound_logger.error(msg, **kwargs)
    
    def debug(self, msg: str, **kwargs) -> None:
        """Log debug level message with request context"""
        self.bound_logger.debug(msg, **kwargs)




def log_application_start(version: str, environment: str) -> None:
    """Log application startup information"""
    logger = structlog.get_logger("startup")
    logger.info(
        "Application starting",
        version=version,
        environment=environment,
        timestamp=datetime.utcnow().isoformat()
    )


def log_application_shutdown() -> None:
    """Log application shutdown information"""
    logger = structlog.get_logger("shutdown")
    logger.info(
        "Application shutting down",
        timestamp=datetime.utcnow().isoformat()
    )


def mask_sensitive_data(data: Dict[str, Any]) -> Dict[str, Any]:
    """
    Mask sensitive data in logs
    
    Args:
        data: Dictionary that may contain sensitive data
        
    Returns:
        Dictionary with sensitive data masked
    """
    sensitive_keys = {
        "api_key", "password", "token", "secret", "key", 
        "authorization", "auth", "credentials"
    }
    
    masked_data = {}
    
    for key, value in data.items():
        key_lower = key.lower()
        
        if any(sensitive_key in key_lower for sensitive_key in sensitive_keys):
            if isinstance(value, str) and len(value) > 4:
                masked_data[key] = value[:4] + "*" * (len(value) - 4)
            else:
                masked_data[key] = "***"
        else:
            masked_data[key] = value
    
    return masked_data