"""
Author: QuanTuanHuy
Description: Part of Serp Project - Dependency Injection
"""

from functools import lru_cache
from fastapi import Depends

from src.infrastructure.db.repositories import (
    AIModuleAdapter,
    AICapabilityAdapter,
    ConversationAdapter,
    MessageAdapter,
)
from src.infrastructure.llm import OpenAIClient

from src.core.service import (
    AIModuleService,
    AICapabilityService,
    ConversationService,
    MessageService,
)

from src.core.usecase import ChatUseCase


# Repository Adapters

@lru_cache
def get_ai_module_adapter() -> AIModuleAdapter:
    """Get AI Module repository adapter (singleton)"""
    return AIModuleAdapter()


@lru_cache
def get_ai_capability_adapter() -> AICapabilityAdapter:
    """Get AI Capability repository adapter (singleton)"""
    return AICapabilityAdapter()


@lru_cache
def get_conversation_adapter() -> ConversationAdapter:
    """Get Conversation repository adapter (singleton)"""
    return ConversationAdapter()


@lru_cache
def get_message_adapter() -> MessageAdapter:
    """Get Message repository adapter (singleton)"""
    return MessageAdapter()


# Client Adapters

@lru_cache
def get_llm_client() -> OpenAIClient:
    """Get LLM client (singleton)"""
    return OpenAIClient()


# Services

def get_ai_module_service(
    ai_module_adapter: AIModuleAdapter = Depends(get_ai_module_adapter)
) -> AIModuleService:
    """Get AI Module service"""
    return AIModuleService(ai_module_port=ai_module_adapter)


def get_ai_capability_service(
    ai_capability_adapter: AICapabilityAdapter = Depends(get_ai_capability_adapter)
) -> AICapabilityService:
    """Get AI Capability service"""
    return AICapabilityService(ai_capability_port=ai_capability_adapter)


def get_conversation_service(
    conversation_adapter: ConversationAdapter = Depends(get_conversation_adapter)
) -> ConversationService:
    """Get Conversation service"""
    return ConversationService(conversation_port=conversation_adapter)


def get_message_service(
    message_adapter: MessageAdapter = Depends(get_message_adapter)
) -> MessageService:
    """Get Message service"""
    return MessageService(message_port=message_adapter)


# Use Cases

def get_chat_usecase(
    ai_module_service: AIModuleService = Depends(get_ai_module_service),
    ai_capability_service: AICapabilityService = Depends(get_ai_capability_service),
    conversation_service: ConversationService = Depends(get_conversation_service),
    message_service: MessageService = Depends(get_message_service),
    llm_client: OpenAIClient = Depends(get_llm_client),
) -> ChatUseCase:
    """Get Chat use case"""
    return ChatUseCase(
        ai_module_service=ai_module_service,
        ai_capability_service=ai_capability_service,
        conversation_service=conversation_service,
        message_service=message_service,
        llm_client=llm_client,
    )


# Auth Dependencies

async def get_current_user_id() -> int:
    """
    Get current user ID from header
    
    TODO: Replace with JWT token extraction in production
    """
    return 1


async def get_current_tenant_id() -> int:
    """
    Get current tenant ID from header
    
    TODO: Replace with JWT token extraction in production
    """
    return 1
