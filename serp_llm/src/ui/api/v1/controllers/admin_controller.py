"""
Author: QuanTuanHuy
Description: Part of Serp Project - Admin API Controller (Example with Role-Based Access)
"""

from typing import Dict, Any
from fastapi import APIRouter, Depends, HTTPException, status, Request
from loguru import logger

from src.ui.api.v1.dependencies import (
    get_current_user_id,
    require_role,
    require_system_admin,
)
from src.kernel.utils.auth_utils import auth_utils
from src.kernel.utils.response_utils import response_utils
from src.core.domain.dto import GeneralResponse


router = APIRouter(prefix="/admin", tags=["Admin"])


@router.get(
    "/stats",
    response_model=GeneralResponse[Dict[str, Any]],
    summary="Get system statistics",
    description="Get system statistics (Admin only)",
    dependencies=[Depends(require_role("SUPER_ADMIN"))],
)
async def get_system_stats(
    user_id: int = Depends(get_current_user_id),
):
    """
    Get system statistics - requires SUPER_ADMIN role
    """
    logger.info(f"Admin user {user_id} accessed system stats")
    
    data = {
        "message": "System statistics endpoint",
        "user_id": user_id,
        "note": "This endpoint requires SUPER_ADMIN role"
    }
    
    return response_utils.success(
        data=data,
        message="System statistics retrieved successfully"
    )


@router.post(
    "/users/ban",
    response_model=GeneralResponse[Dict[str, Any]],
    summary="Ban a user",
    description="Ban a user from the system (Super Admin only)",
    dependencies=[Depends(require_system_admin())],
)
async def ban_user(
    target_user_id: int,
    reason: str,
    user_id: int = Depends(get_current_user_id),
):
    """
    Ban a user - requires SUPER_ADMIN or SYSTEM_MODERATOR role
    """
    logger.info(f"Super admin {user_id} banning user {target_user_id}: {reason}")
    
    data = {
        "target_user_id": target_user_id,
        "banned_by": user_id,
        "reason": reason,
        "note": "This endpoint requires SUPER_ADMIN or SYSTEM_MODERATOR role"
    }
    
    return response_utils.success(
        data=data,
        message=f"User {target_user_id} has been banned successfully"
    )


@router.get(
    "/user-info",
    response_model=GeneralResponse[Dict[str, Any]],
    summary="Get current user info from JWT",
    description="Get all information extracted from JWT token",
)
async def get_user_info(
    request: Request,
    user_id: int = Depends(get_current_user_id),
):
    """
    Get current user information from JWT token
    """
    data = {
        "user_id": user_id,
        "tenant_id": auth_utils.get_current_tenant_id(request),
        "email": auth_utils.get_current_user_email(request),
        "full_name": auth_utils.get_current_user_full_name(request),
        "realm_roles": auth_utils.get_realm_roles(request),
        "all_roles": auth_utils.get_all_roles(request),
        "is_system_admin": auth_utils.is_system_admin(request),
    }
    
    return response_utils.success(
        data=data,
        message="User information retrieved successfully"
    )


@router.get(
    "/roles/check",
    response_model=GeneralResponse[Dict[str, Any]],
    summary="Check if user has specific roles",
    description="Check if current user has specific roles",
)
async def check_roles(
    request: Request,
    roles: str,
    user_id: int = Depends(get_current_user_id),
):
    """
    Check if user has specific roles
    
    Args:
        roles: Comma-separated list of roles to check (e.g., "ADMIN,SUPER_ADMIN")
    """
    role_list = [r.strip() for r in roles.split(",")]
    
    data = {
        "user_id": user_id,
        "requested_roles": role_list,
        "user_roles": auth_utils.get_all_roles(request),
        "has_any_role": auth_utils.has_any_role(request, *role_list),
    }
    
    return response_utils.success(
        data=data,
        message="Role check completed successfully"
    )
