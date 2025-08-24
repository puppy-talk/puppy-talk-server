"""
Configuration management for PuppyTalk AI Service
"""

from functools import lru_cache
from typing import List, Optional
from pydantic import Field
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """Application settings with environment variable support"""
    
    # Application settings
    app_name: str = "PuppyTalk AI Service"
    app_version: str = "1.0.0"
    debug: bool = False
    environment: str = Field(default="development", env="ENVIRONMENT")
    
    # API settings
    api_prefix: str = "/api/v1"
    host: str = "0.0.0.0"
    port: int = 8001
    
    # Grok API settings
    grok_api_key: str = Field(..., env="GROK_API_KEY")
    grok_base_url: str = "https://api.x.ai/v1"
    grok_model: str = "grok-beta"
    grok_timeout: int = 30
    grok_max_retries: int = 3
    
    
    # Security settings
    secret_key: str = Field(..., env="SECRET_KEY")
    access_token_expire_minutes: int = 30
    allowed_hosts: List[str] = ["*"]
    
    # CORS settings
    cors_origins: List[str] = [
        "http://localhost:3000",
        "http://localhost:8080",
        "http://127.0.0.1:3000",
        "http://127.0.0.1:8080"
    ]
    
    # Logging settings
    log_level: str = "INFO"
    log_format: str = "json"
    
    
    # Java service integration
    java_service_url: str = "http://localhost:8080"
    java_service_timeout: int = 5
    
    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"
        case_sensitive = False


@lru_cache()
def get_settings() -> Settings:
    """Get cached settings instance"""
    return Settings()