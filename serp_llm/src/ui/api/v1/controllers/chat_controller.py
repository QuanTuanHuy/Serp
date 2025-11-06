"""
Author: QuanTuanHuy
Description: Part of Serp Project - Chat API Controller
"""

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from loguru import logger

from src.infrastructure.db.database import get_db
from src.core.domain.dto import ChatRequest, ChatResponse
from src.core.usecase import ChatUseCase
from src.ui.api.v1.dependencies import (
    get_chat_usecase,
    get_current_user_id,
    get_current_tenant_id,
)


router = APIRouter(prefix="/chat", tags=["Chat"])


@router.post(
    "",
    response_model=ChatResponse,
    status_code=status.HTTP_200_OK,
    summary="Send chat message",
    description="Send a message and get AI assistant response"
)
async def chat(
    request: ChatRequest,
    db: AsyncSession = Depends(get_db),
    chat_usecase: ChatUseCase = Depends(get_chat_usecase),
    user_id: int = Depends(get_current_user_id),
    tenant_id: int = Depends(get_current_tenant_id),
) -> ChatResponse:
    """
    Chat with AI assistant
    """
    try:
        response = await chat_usecase.execute(
            db=db,
            user_id=user_id,
            tenant_id=tenant_id,
            request=request,
        )
        
        logger.info(
            f"Chat completed: user={user_id}, conversation={response.conversation_id}, "
            f"tokens={response.tokens_used}, time={response.processing_time_ms}ms"
        )
        
        return response
        
    except ValueError as e:
        logger.warning(f"Chat validation error: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )
    except PermissionError as e:
        logger.warning(f"Chat permission denied: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail=str(e)
        )
    except Exception as e:
        logger.error(f"Chat error: {type(e).__name__}: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to process chat request: {type(e).__name__}"
        )


@router.get(
    "/conversations",
    summary="List user conversations",
    description="Get list of user's conversations"
)
async def list_conversations(
    module_code: str = None,
    limit: int = 50,
    offset: int = 0,
    db: AsyncSession = Depends(get_db),
    user_id: int = Depends(get_current_user_id),
    tenant_id: int = Depends(get_current_tenant_id),
):
    """
    List user's conversations
    
    TODO: Implement conversation listing use case
    """
    raise HTTPException(
        status_code=status.HTTP_501_NOT_IMPLEMENTED,
        detail="Not implemented yet"
    )


@router.get(
    "/conversations/{conversation_id}/messages",
    summary="Get conversation messages",
    description="Get messages in a specific conversation"
)
async def get_conversation_messages(
    conversation_id: int,
    limit: int = 100,
    offset: int = 0,
    db: AsyncSession = Depends(get_db),
    user_id: int = Depends(get_current_user_id),
):
    """
    Get messages in a conversation
    
    TODO: Implement message retrieval use case
    """
    raise HTTPException(
        status_code=status.HTTP_501_NOT_IMPLEMENTED,
        detail="Not implemented yet"
    )
