"""
Author: QuanTuanHuy
Description: Part of Serp Project - Chat Use Case
"""

import time
from typing import Optional
from sqlalchemy.ext.asyncio import AsyncSession
from loguru import logger

from src.core.domain.dto import ChatRequest, ChatResponse
from src.core.domain.enums import ModuleCode, ContextType
from src.core.service import (
    AIModuleService,
    AICapabilityService,
    ConversationService,
    MessageService,
)
from src.core.port.client import ILLMClientPort


class ChatUseCase:
    """
    Use case for chat completion
    
    Orchestrates the entire chat flow:
    1. Validate module and capability
    2. Get or create conversation
    3. Save user message
    4. Build LLM context with history
    5. Call LLM API
    6. Save assistant response
    7. Return response
    """
    
    def __init__(
        self,
        ai_module_service: AIModuleService,
        ai_capability_service: AICapabilityService,
        conversation_service: ConversationService,
        message_service: MessageService,
        llm_client: ILLMClientPort,
    ):
        self.ai_module_service = ai_module_service
        self.ai_capability_service = ai_capability_service
        self.conversation_service = conversation_service
        self.message_service = message_service
        self.llm_client = llm_client
    
    async def execute(
        self,
        db: AsyncSession,
        user_id: int,
        tenant_id: int,
        request: ChatRequest,
    ) -> ChatResponse:
        """Execute chat completion"""
        
        start_time = time.time()
        
        module_code = ModuleCode(request.module_code)
        module = await self.ai_module_service.get_module_by_code(db, module_code)
        if not module:
            raise ValueError(f"Module '{request.module_code}' not found or disabled")
        
        has_access = await self.ai_module_service.validate_module_access(
            db, module_code, tenant_id
        )
        if not has_access:
            raise PermissionError(f"Tenant does not have access to module '{request.module_code}'")
        
        capability = await self.ai_capability_service.get_capability(
            db, module_code, request.capability_code
        )        
        if not capability:
            raise ValueError(
                f"Capability '{request.capability_code}' not found for module '{request.module_code}'"
            )
        
        context_type = ContextType(request.context_type) if request.context_type else None        
        conversation = await self.conversation_service.get_or_create_conversation(
            db=db,
            user_id=user_id,
            tenant_id=tenant_id,
            module_code=module_code,
            capability_code=capability.code,
            conversation_id=request.conversation_id,
            context_type=context_type,
            context_id=request.context_id,
        )
        
        user_message = await self.message_service.create_user_message(
            db=db,
            conversation_id=conversation.id,
            content=request.message,
            attachments=request.attachments,
        )
        
        await db.commit()
        
        context_dict = {}
        if context_type:
            context_dict["context_type"] = context_type.value
        if request.context_id:
            context_dict["context_id"] = request.context_id    
        system_message = self.ai_capability_service.build_system_message(
            capability, context_dict
        )
        
        history = await self.message_service.get_conversation_history(
            db=db,
            conversation_id=conversation.id,
            max_messages=20,
        )
        history_messages = self.message_service.format_messages_for_llm(
            [msg for msg in history if msg.id != user_message.id]
        )
        
        messages = []
        if system_message:
            messages.append({"role": "system", "content": system_message})
        messages.extend(history_messages)
        messages.append({"role": "user", "content": request.message})
        
        model_config = self.ai_capability_service.get_model_config(capability)        
        if request.temperature is not None:
            model_config["temperature"] = request.temperature
        if request.max_tokens is not None:
            model_config["max_tokens"] = request.max_tokens
        
        logger.info(
            f"Calling LLM for conversation {conversation.id} "
            f"with {len(messages)} messages, model={model_config['model']}"
        )
        
        llm_response = await self.llm_client.chat_completion(
            messages=messages,
            model=model_config["model"],
            temperature=model_config["temperature"],
            max_tokens=model_config["max_tokens"],
            stream=False,
        )
        
        processing_time_ms = int((time.time() - start_time) * 1000)
        
        assistant_message = await self.message_service.create_assistant_message(
            db=db,
            conversation_id=conversation.id,
            content=llm_response["content"],
            model_used=model_config["model"],
            tokens_used=llm_response.get("tokens_used", 0),
            processing_time_ms=processing_time_ms,
            sources=[],
        )
        
        await db.commit()
        
        return ChatResponse(
            conversation_id=conversation.id,
            message_id=assistant_message.id,
            content=assistant_message.content,
            model_used=assistant_message.model_used,
            tokens_used=assistant_message.tokens_used,
            processing_time_ms=processing_time_ms,
            sources=[],
        )
