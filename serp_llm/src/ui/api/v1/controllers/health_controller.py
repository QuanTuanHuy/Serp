"""
Author: QuanTuanHuy
Description: Part of Serp Project - Health Check Controller
"""

from fastapi import APIRouter
from datetime import datetime

router = APIRouter(tags=["Health"])


@router.get("/health")
async def health_check():
    """Health check endpoint"""
    return {
        "status": "healthy",
        "service": "serp_llm",
        "timestamp": datetime.now(),
    }


@router.get("/")
async def root():
    """Root endpoint"""
    return {
        "service": "SERP LLM Service",
        "version": "0.1.0",
        "description": "AI Assistant for SERP ERP System",
    }
