"""
Author: QuanTuanHuy
Description: Part of Serp Project - Seed Data Script

This script seeds the database with initial AI modules and capabilities.
Run this after Alembic migration to populate the database with required data.

Usage:
    poetry run python scripts/seed_data.py
"""

import asyncio
import sys
from pathlib import Path

# Add src to Python path
sys.path.insert(0, str(Path(__file__).parent.parent))

from loguru import logger
from sqlalchemy.ext.asyncio import AsyncSession

from src.infrastructure.db.database import AsyncSessionLocal
from src.infrastructure.db.models import (
    AIModuleModel,
    AICapabilityModel,
)
from src.core.domain.enums import (
    ModuleCode,
    CapabilityType,
)


async def seed_modules(session: AsyncSession) -> dict[ModuleCode, int]:
    """
    Seed AI modules
    Returns: Dict mapping ModuleCode to module ID
    """
    logger.info("Seeding AI modules...")
    
    modules_data = [
        {
            "code": ModuleCode.CRM.value,
            "name": "Customer Relationship Management",
            "description": "AI assistant for CRM - helps manage customers, leads, opportunities, and sales activities",
            "icon": "users",
            "enabled": True,
            "config": {
                "default_model": "gemini-2.0-flash",
                "max_context_messages": 10,
                "enable_web_search": True,
                "enable_file_analysis": True,
                "color": "#3b82f6",
                "display_order": 1,
            },
        },
        {
            "code": ModuleCode.PTM.value,
            "name": "Personal Task Management",
            "description": "AI assistant for personal productivity - helps manage tasks, schedules, and optimization",
            "icon": "calendar-check",
            "enabled": True,
            "config": {
                "default_model": "gemini-2.0-flash",
                "max_context_messages": 10,
                "enable_task_suggestions": True,
                "enable_schedule_optimization": True,
                "color": "#10b981",
                "display_order": 2,
            },
        },
        {
            "code": ModuleCode.SALES.value,
            "name": "Sales Management",
            "description": "AI assistant for sales - helps with forecasting, pipeline management, and deal closure",
            "icon": "trending-up",
            "enabled": True,
            "config": {
                "default_model": "gemini-2.0-flash",
                "max_context_messages": 10,
                "enable_forecasting": True,
                "enable_pipeline_analysis": True,
                "color": "#8b5cf6",
                "display_order": 3,
            },
        },
    ]
    
    module_id_map = {}
    
    for module_data in modules_data:
        # Check if module already exists
        from sqlalchemy import select
        stmt = select(AIModuleModel).filter(AIModuleModel.code == module_data["code"])
        result = await session.execute(stmt)
        existing_module = result.scalars().first()
        
        if existing_module:
            logger.info(f"Module {module_data['code']} already exists, skipping...")
            module_id_map[ModuleCode(module_data["code"])] = existing_module.id
            continue
        
        module = AIModuleModel(**module_data)
        session.add(module)
        await session.flush()  # Get ID
        
        module_id_map[ModuleCode(module_data["code"])] = module.id
        logger.info(f"Created module: {module_data['code']} (ID: {module.id})")
    
    await session.commit()
    logger.info(f"✅ Seeded {len(module_id_map)} modules")
    
    return module_id_map


async def seed_capabilities(session: AsyncSession, module_id_map: dict[ModuleCode, int]):
    """
    Seed AI capabilities for each module
    """
    logger.info("Seeding AI capabilities...")
    
    crm_capabilities = [
        {
            "module_code": ModuleCode.CRM.value,
            "code": "chat",
            "capability_type": CapabilityType.CHAT.value,
            "name": "CRM Chat Assistant",
            "description": "Conversational AI for CRM queries and actions",
            "enabled": True,
            "system_prompt": """You are a helpful CRM assistant for the SERP ERP system.
You help users manage customers, leads, opportunities, and sales activities.

Your capabilities:
- Answer questions about customer data
- Help create and update customer records
- Provide insights on sales pipeline
- Suggest next best actions for leads
- Analyze customer trends and patterns

Always be professional, concise, and action-oriented.
If you need to access data, use the provided functions.
""",
            "default_model": "gemini-2.0-flash",
            "default_temperature": 0.7,
            "default_max_tokens": 2048,
            "available_functions": [
                {
                    "name": "search_customers",
                    "description": "Search for customers by name, email, or company",
                    "parameters": {
                        "type": "object",
                        "properties": {
                            "query": {"type": "string", "description": "Search query"},
                            "limit": {"type": "integer", "description": "Max results", "default": 10},
                        },
                        "required": ["query"],
                    },
                },
                {
                    "name": "get_customer_details",
                    "description": "Get detailed information about a specific customer",
                    "parameters": {
                        "type": "object",
                        "properties": {
                            "customer_id": {"type": "integer", "description": "Customer ID"},
                        },
                        "required": ["customer_id"],
                    },
                },
            ],
        },
    ]
    
    ptm_capabilities = [
        {
            "module_code": ModuleCode.PTM.value,
            "code": "chat",
            "capability_type": CapabilityType.CHAT.value,
            "name": "PTM Chat Assistant",
            "description": "Conversational AI for personal task management",
            "enabled": True,
            "system_prompt": """You are a personal productivity assistant for the SERP ERP system.
You help users manage their tasks, schedules, and optimize their time.

Your capabilities:
- Answer questions about tasks and schedules
- Help create and update tasks
- Provide scheduling suggestions
- Optimize task priorities
- Analyze productivity patterns

Be supportive, organized, and help users stay focused on their goals.
If you need to access data, use the provided functions.
""",
            "default_model": "gemini-2.0-flash",
            "default_temperature": 0.7,
            "default_max_tokens": 2048,
            "available_functions": [
                {
                    "name": "search_tasks",
                    "description": "Search for tasks by title, status, or priority",
                    "parameters": {
                        "type": "object",
                        "properties": {
                            "query": {"type": "string", "description": "Search query"},
                            "status": {"type": "string", "enum": ["TODO", "IN_PROGRESS", "DONE"]},
                            "limit": {"type": "integer", "default": 10},
                        },
                    },
                },
                {
                    "name": "create_task",
                    "description": "Create a new task",
                    "parameters": {
                        "type": "object",
                        "properties": {
                            "title": {"type": "string"},
                            "description": {"type": "string"},
                            "priority": {"type": "string", "enum": ["LOW", "MEDIUM", "HIGH"]},
                            "due_date": {"type": "string", "format": "date-time"},
                        },
                        "required": ["title"],
                    },
                },
            ],
        },
    ]
    
    sales_capabilities = [
        {
            "module_code": ModuleCode.SALES.value,
            "code": "chat",
            "capability_type": CapabilityType.CHAT.value,
            "name": "Sales Chat Assistant",
            "description": "Conversational AI for sales management",
            "enabled": True,
            "system_prompt": """You are a sales assistant for the SERP ERP system.
You help users manage their sales pipeline, forecast revenue, and close deals.

Your capabilities:
- Answer questions about sales data
- Provide pipeline insights
- Suggest deal closure strategies
- Analyze sales trends
- Forecast revenue

Be confident, data-driven, and focused on results.
If you need to access data, use the provided functions.
""",
            "default_model": "gemini-2.0-flash",
            "default_temperature": 0.7,
            "default_max_tokens": 2048,
            "available_functions": [
                {
                    "name": "get_sales_pipeline",
                    "description": "Get current sales pipeline overview",
                    "parameters": {
                        "type": "object",
                        "properties": {
                            "stage": {"type": "string", "description": "Filter by stage"},
                        },
                    },
                },
            ],
        },
    ]
    
    all_capabilities = crm_capabilities + ptm_capabilities + sales_capabilities
    
    created_count = 0
    for cap_data in all_capabilities:
        from sqlalchemy import select
        stmt = select(AICapabilityModel).filter(
            AICapabilityModel.module_code == cap_data["module_code"],
            AICapabilityModel.code == cap_data["code"],
        )
        result = await session.execute(stmt)
        existing_cap = result.scalars().first()
        
        if existing_cap:
            logger.info(f"Capability {cap_data['name']} already exists, skipping...")
            continue
        
        capability = AICapabilityModel(**cap_data)
        session.add(capability)
        created_count += 1
        logger.info(f"Created capability: {cap_data['name']}")
    
    await session.commit()
    logger.info(f"✅ Seeded {created_count} capabilities")


async def main():
    """
    Main seed function
    """
    logger.info("Starting database seed...")
    
    async with AsyncSessionLocal() as session:
        try:
            module_id_map = await seed_modules(session)
            
            await seed_capabilities(session, module_id_map)
            
            logger.success("✅ Database seeding completed successfully!")
            
        except Exception as e:
            logger.error(f"❌ Seed failed: {e}")
            await session.rollback()
            raise


if __name__ == "__main__":
    asyncio.run(main())
