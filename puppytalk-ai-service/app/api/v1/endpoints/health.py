"""
Health check endpoints for PuppyTalk AI Service
"""

import time
from typing import Dict, Any
from fastapi import APIRouter, Depends
import structlog

from app.core.config import get_settings
from app.models.responses import HealthCheckResponse
from app.services.grok_service import GrokService

logger = structlog.get_logger(__name__)
router = APIRouter()
settings = get_settings()


def get_grok_service() -> GrokService:
    """Dependency to get Grok service instance"""
    return GrokService()


@router.get(
    "/health",
    response_model=HealthCheckResponse,
    summary="Service health check",
    description="Check the health status of the AI service and its dependencies"
)
async def health_check(
    grok_service: GrokService = Depends(get_grok_service)
) -> HealthCheckResponse:
    """
    Perform basic health check
    
    Returns:
        Health status with dependency information
    """
    logger.info("Health check requested")
    
    try:
        # Check Grok API health
        grok_healthy = await grok_service.client.health_check()
        
        dependencies = {
            "grok_api": "healthy" if grok_healthy else "unhealthy"
        }
        
        # Determine overall status
        status = "healthy" if grok_healthy else "degraded"
        
        response = HealthCheckResponse(
            status=status,
            version="1.0.0",
            dependencies=dependencies
        )
        
        logger.info(
            "Health check completed",
            status=status,
            grok_api_healthy=grok_healthy
        )
        
        return response
        
    except Exception as e:
        logger.error("Health check failed", error=str(e))
        
        return HealthCheckResponse(
            status="unhealthy",
            version="1.0.0",
            dependencies={"grok_api": "unknown"}
        )


@router.get(
    "/health/liveness",
    summary="Liveness probe",
    description="Simple liveness check for container orchestration"
)
async def liveness_probe() -> Dict[str, str]:
    """
    Simple liveness probe for Kubernetes/Docker
    
    Returns:
        Basic status indication
    """
    return {"status": "alive", "timestamp": str(time.time())}


@router.get(
    "/health/readiness", 
    summary="Readiness probe",
    description="Readiness check including external dependencies"
)
async def readiness_probe(
    grok_service: GrokService = Depends(get_grok_service)
) -> Dict[str, Any]:
    """
    Readiness probe for container orchestration
    
    Returns:
        Readiness status with dependency checks
    """
    try:
        # Check if we can connect to Grok API
        grok_ready = await grok_service.client.health_check()
        
        return {
            "status": "ready" if grok_ready else "not_ready",
            "grok_api": "ready" if grok_ready else "not_ready",
            "timestamp": str(time.time())
        }
        
    except Exception as e:
        logger.error("Readiness check failed", error=str(e))
        return {
            "status": "not_ready",
            "error": str(e),
            "timestamp": str(time.time())
        }




