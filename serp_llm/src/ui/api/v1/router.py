"""
Author: QuanTuanHuy
Description: Part of Serp Project - API V1 Router
"""

from fastapi import APIRouter
from src.ui.api.v1.controllers import chat_controller, health_controller, admin_controller


api_router = APIRouter()

api_router.include_router(health_controller.router)

api_v1_router = APIRouter(prefix="/api/v1")
api_v1_router.include_router(chat_controller.router)
api_v1_router.include_router(admin_controller.router)
