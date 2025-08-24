"""
API v1 router configuration
"""

from fastapi import APIRouter

from app.api.v1.endpoints import chat, health

api_router = APIRouter()

# Include endpoint routers
api_router.include_router(chat.router, prefix="/chat", tags=["chat"])
api_router.include_router(health.router, prefix="/health", tags=["health"])