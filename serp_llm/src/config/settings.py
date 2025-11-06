# Author: QuanTuanHuy
# Description: Part of Serp Project - Application Settings

from typing import Literal
from pydantic import Field, field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """Application settings loaded from environment variables"""
    
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=False,
        extra="ignore"
    )
    
    # Server Configuration
    host: str = Field(default="0.0.0.0", description="Server host")
    port: int = Field(default=8089, description="Server port")
    environment: Literal["development", "staging", "production"] = Field(
        default="development", 
        description="Environment"
    )
    debug: bool = Field(default=False, description="Debug mode")
    
    # Database Configuration
    db_host: str = Field(default="localhost", description="Database host")
    db_port: int = Field(default=5432, description="Database port")
    db_name: str = Field(default="serp_llm", description="Database name")
    db_user: str = Field(default="serp", description="Database user")
    db_password: str = Field(default="serp", description="Database password")
    database_url: str = Field(
        default="postgresql+asyncpg://serp:serp@localhost:5432/serp_llm",
        description="Full database URL"
    )
    
    # Redis Configuration
    redis_host: str = Field(default="localhost", description="Redis host")
    redis_port: int = Field(default=6379, description="Redis port")
    redis_db: int = Field(default=2, description="Redis database number")
    redis_password: str = Field(default="", description="Redis password")
    redis_ttl: int = Field(default=3600, description="Redis TTL in seconds")
    
    # Kafka Configuration
    kafka_bootstrap_servers: str = Field(
        default="localhost:9092", 
        description="Kafka bootstrap servers"
    )
    kafka_group_id: str = Field(
        default="serp_llm_group", 
        description="Kafka consumer group ID"
    )
    kafka_auto_offset_reset: Literal["earliest", "latest"] = Field(
        default="earliest",
        description="Kafka auto offset reset"
    )
    
    # OpenAI Configuration
    openai_api_key: str = Field(default="", description="Gemini API Key")
    openai_org_id: str = Field(default="", description="OpenAI Organization ID (not used for Gemini)")
    openai_base_url: str = Field(
        default="https://generativelanguage.googleapis.com/v1beta/openai/",
        description="Gemini OpenAI-compatible API base URL"
    )
    
    # LLM Defaults
    default_model: str = Field(
        default="gemini-2.0-flash",
        description="Default LLM model (gemini-2.0-flash, gemini-2.5-pro, gemini-2.5-flash)"
    )
    default_max_tokens: int = Field(
        default=8192,
        description="Default max tokens"
    )
    default_temperature: float = Field(
        default=0.7,
        description="Default temperature"
    )
    embedding_model: str = Field(
        default="text-embedding-004",
        description="Gemini embedding model"
    )
    embedding_dimension: int = Field(
        default=1536,
        description="Embedding dimension"
    )
    
    # Keycloak Configuration
    keycloak_server_url: str = Field(
        default="http://localhost:8180",
        description="Keycloak server URL"
    )
    keycloak_realm: str = Field(default="serp", description="Keycloak realm")
    keycloak_client_id: str = Field(
        default="serp-llm",
        description="Keycloak client ID"
    )
    keycloak_client_secret: str = Field(
        default="",
        description="Keycloak client secret"
    )
    
    # Service URLs
    account_service_url: str = Field(
        default="http://localhost:8081",
        description="Account service URL"
    )
    crm_service_url: str = Field(
        default="http://localhost:8086",
        description="CRM service URL"
    )
    ptm_task_service_url: str = Field(
        default="http://localhost:8083",
        description="PTM Task service URL"
    )
    ptm_schedule_service_url: str = Field(
        default="http://localhost:8084",
        description="PTM Schedule service URL"
    )
    
    log_level: Literal["DEBUG", "INFO", "WARNING", "ERROR", "CRITICAL"] = Field(
        default="INFO",
        description="Log level"
    )
    log_format: Literal["json", "text"] = Field(
        default="json",
        description="Log format"
    )
    
    @field_validator("log_level", mode="before")
    @classmethod
    def normalize_log_level(cls, v: str) -> str:
        """Convert log level to uppercase"""
        if isinstance(v, str):
            return v.upper()
        return v
    
    @property
    def redis_url(self) -> str:
        """Construct Redis URL"""
        if self.redis_password:
            return f"redis://:{self.redis_password}@{self.redis_host}:{self.redis_port}/{self.redis_db}"
        return f"redis://{self.redis_host}:{self.redis_port}/{self.redis_db}"
    
    @property
    def is_production(self) -> bool:
        """Check if running in production"""
        return self.environment == "production"
    
    @property
    def is_development(self) -> bool:
        """Check if running in development"""
        return self.environment == "development"
    
    @property
    def uvicorn_log_level(self) -> str:
        """Get log level in lowercase for uvicorn CLI"""
        return self.log_level.lower()


# Global settings instance
settings = Settings()
