"""
Author: QuanTuanHuy
Description: Part of Serp Project - Dependency Injection
"""

from functools import lru_cache
from fastapi import Depends, Request

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
from src.kernel.utils.auth_utils import auth_utils


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

async def get_current_user_id(request: Request) -> int:
    """
    Get current user ID from JWT token in request.
    
    Raises:
        HTTPException: If user is not authenticated
    """
    return auth_utils.require_authentication(request)


async def get_current_tenant_id(request: Request) -> int:
    """
    Get current tenant ID from JWT token in request.
    
    Returns:
        Tenant ID or raises exception if not found
    """
    tenant_id = auth_utils.get_current_tenant_id(request)
    if tenant_id is None:
        from fastapi import HTTPException, status
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Tenant ID not found in token"
        )
    return tenant_id


async def get_optional_user_id(request: Request) -> int | None:
    """
    Get current user ID from JWT token (optional).
    
    Returns:
        User ID or None if not authenticated
    """
    return auth_utils.get_current_user_id(request)


async def get_optional_tenant_id(request: Request) -> int | None:
    """
    Get current tenant ID from JWT token (optional).
    
    Returns:
        Tenant ID or None if not found
    """
    return auth_utils.get_current_tenant_id(request)


# Role-based access dependencies

def require_role(*roles: str):
    """
    Dependency to require specific roles.
    
    Usage:
        @router.get("/admin", dependencies=[Depends(require_role("ADMIN", "SUPER_ADMIN"))])
    """
    async def _check_role(request: Request):
        auth_utils.require_role(request, *roles)
    
    return _check_role


def require_system_admin():
    """
    Dependency to require system admin role.
    
    Usage:
        @router.get("/admin", dependencies=[Depends(require_system_admin())])
    """
    async def _check_admin(request: Request):
        from fastapi import HTTPException, status
        if not auth_utils.is_system_admin(request):
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="System admin access required"
            )
    
    return _check_admin


def require_organization_access(organization_id: int):
    """
    Dependency to require organization access.
    
    Usage:
        @router.get("/org/{org_id}", dependencies=[Depends(require_organization_access(org_id))])
    """
    async def _check_org_access(request: Request):
        auth_utils.require_organization_access(request, organization_id)
    
    return _check_org_access

