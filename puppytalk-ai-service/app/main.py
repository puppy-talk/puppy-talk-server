"""
PuppyTalk AI Service - FastAPI Application
"""

import time
from contextlib import asynccontextmanager
from typing import Dict, Any

from fastapi import FastAPI, Request, Response, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.middleware.trustedhost import TrustedHostMiddleware
from fastapi.responses import JSONResponse
import structlog

from app.core.config import get_settings
from app.utils.logger import configure_logging, log_application_start, log_application_shutdown
from app.api.v1.api import api_router
from app.models.responses import ErrorResponse, ErrorDetail

# Configure logging
configure_logging()
logger = structlog.get_logger(__name__)
settings = get_settings()


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Application lifespan events"""
    # Startup
    log_application_start(version="1.0.0", environment=settings.environment)
    logger.info("PuppyTalk AI Service starting up")
    
    yield
    
    # Shutdown
    logger.info("PuppyTalk AI Service shutting down")
    log_application_shutdown()


# Create FastAPI application
app = FastAPI(
    title="PuppyTalk AI Service",
    description="AI service for PuppyTalk pet chat application using Grok API",
    version="1.0.0",
    openapi_url="/api/v1/openapi.json" if settings.debug else None,
    docs_url="/docs" if settings.debug else None,
    redoc_url="/redoc" if settings.debug else None,
    lifespan=lifespan
)


# Add middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.allowed_hosts if hasattr(settings, 'allowed_hosts') else ["*"],
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "DELETE"],
    allow_headers=["*"],
)

if not settings.debug:
    app.add_middleware(
        TrustedHostMiddleware,
        allowed_hosts=settings.allowed_hosts if hasattr(settings, 'allowed_hosts') else ["localhost", "127.0.0.1"]
    )


# Request/Response logging middleware
@app.middleware("http")
async def log_requests(request: Request, call_next):
    """Log all requests and responses"""
    start_time = time.time()
    
    # Generate request ID for tracing
    request_id = f"req_{int(time.time() * 1000000)}"
    
    # Add request ID to request state
    request.state.request_id = request_id
    
    # Log request
    logger.info(
        "Request started",
        request_id=request_id,
        method=request.method,
        url=str(request.url),
        client_ip=request.client.host if request.client else "unknown",
        user_agent=request.headers.get("user-agent", "unknown")
    )
    
    try:
        # Process request
        response = await call_next(request)
        
        # Calculate duration
        duration_ms = (time.time() - start_time) * 1000
        
        # Log response
        logger.info(
            "Request completed",
            request_id=request_id,
            method=request.method,
            url=str(request.url),
            status_code=response.status_code,
            duration_ms=round(duration_ms, 2)
        )
        
        # Add request ID to response headers
        response.headers["X-Request-ID"] = request_id
        
        return response
        
    except Exception as e:
        duration_ms = (time.time() - start_time) * 1000
        
        logger.error(
            "Request failed",
            request_id=request_id,
            method=request.method,
            url=str(request.url),
            error=str(e),
            duration_ms=round(duration_ms, 2),
            exc_info=True
        )
        
        # Return error response
        error_response = ErrorResponse(
            error=ErrorDetail(
                error_code="INTERNAL_ERROR",
                message="Internal server error occurred"
            ),
            request_id=request_id
        )
        
        return JSONResponse(
            status_code=500,
            content=error_response.dict(),
            headers={"X-Request-ID": request_id}
        )


# Global exception handler
@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception) -> JSONResponse:
    """Handle uncaught exceptions"""
    request_id = getattr(request.state, 'request_id', 'unknown')
    
    logger.error(
        "Unhandled exception",
        request_id=request_id,
        error=str(exc),
        exc_info=True
    )
    
    error_response = ErrorResponse(
        error=ErrorDetail(
            error_code="INTERNAL_ERROR",
            message="An unexpected error occurred"
        ),
        request_id=request_id
    )
    
    return JSONResponse(
        status_code=500,
        content=error_response.dict(),
        headers={"X-Request-ID": request_id}
    )


# HTTP exception handler
@app.exception_handler(HTTPException)
async def http_exception_handler(request: Request, exc: HTTPException) -> JSONResponse:
    """Handle HTTP exceptions"""
    request_id = getattr(request.state, 'request_id', 'unknown')
    
    logger.warning(
        "HTTP exception",
        request_id=request_id,
        status_code=exc.status_code,
        detail=exc.detail
    )
    
    # Handle detail as dict (from our endpoints) or string (from FastAPI)
    if isinstance(exc.detail, dict):
        return JSONResponse(
            status_code=exc.status_code,
            content=exc.detail,
            headers={"X-Request-ID": request_id}
        )
    else:
        error_response = ErrorResponse(
            error=ErrorDetail(
                error_code="HTTP_ERROR",
                message=str(exc.detail)
            ),
            request_id=request_id
        )
        
        return JSONResponse(
            status_code=exc.status_code,
            content=error_response.dict(),
            headers={"X-Request-ID": request_id}
        )


# Include API routes
app.include_router(api_router, prefix="/api/v1")


# Root endpoint
@app.get("/", include_in_schema=False)
async def root() -> Dict[str, Any]:
    """Root endpoint with service information"""
    return {
        "service": "PuppyTalk AI Service",
        "version": "1.0.0",
        "status": "running",
        "docs_url": "/docs" if settings.debug else None,
        "health_url": "/api/v1/health"
    }


# Service information endpoint
@app.get("/info", include_in_schema=False)
async def service_info() -> Dict[str, Any]:
    """Service information endpoint"""
    return {
        "name": "PuppyTalk AI Service",
        "version": "1.0.0",
        "description": "AI service for PuppyTalk pet chat application using Grok API",
        "environment": settings.environment,
        "debug": settings.debug,
        "grok_model": settings.grok_model
    }


if __name__ == "__main__":
    import uvicorn
    
    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=8000,
        reload=settings.debug,
        log_config=None  # Use our custom logging configuration
    )