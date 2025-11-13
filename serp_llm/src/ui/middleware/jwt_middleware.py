"""
Author: QuanTuanHuy
Description: Part of Serp Project - JWT Authentication Middleware
"""

from typing import Optional, List
from fastapi import Request, HTTPException, status
from fastapi.responses import JSONResponse
from starlette.middleware.base import BaseHTTPMiddleware
from loguru import logger

from src.kernel.utils.jwt_utils import JwtUtils
from src.kernel.utils.response_utils import response_utils
from src.config import settings


class JWTAuthMiddleware(BaseHTTPMiddleware):
    """
    Middleware to validate JWT tokens from Keycloak.
    Attaches user information to request state.
    """
    
    # Paths that don't require authentication
    EXCLUDED_PATHS = [
        "/",
        "/health",
        "/docs",
        "/redoc",
        "/openapi.json",
    ]
    
    def __init__(self, app, jwt_utils: Optional[JwtUtils] = None):
        super().__init__(app)
        self.jwt_utils = jwt_utils or JwtUtils()
    
    async def dispatch(self, request: Request, call_next):
        """Process request and validate JWT token"""
        
        if self._is_excluded_path(request.url.path):
            return await call_next(request)
        
        auth_header = request.headers.get("Authorization")
        
        if not auth_header:
            logger.warning(f"Missing Authorization header for {request.url.path}")
            error_response = response_utils.unauthorized(
                message="Missing authentication token"
            )
            return JSONResponse(
                status_code=status.HTTP_401_UNAUTHORIZED,
                content=error_response.model_dump(),
                headers={"WWW-Authenticate": "Bearer"},
            )
        
        if not auth_header.startswith("Bearer "):
            logger.warning(f"Invalid Authorization header format for {request.url.path}")
            error_response = response_utils.unauthorized(
                message="Invalid authentication header format"
            )
            return JSONResponse(
                status_code=status.HTTP_401_UNAUTHORIZED,
                content=error_response.model_dump(),
                headers={"WWW-Authenticate": "Bearer"},
            )
        
        token = auth_header[7:]
        
        try:
            claims = self.jwt_utils.validate_token(token)
            
            user_id = claims.get("uid")
            tenant_id = claims.get("tid")
            email = claims.get("email")
            username = claims.get("preferred_username")
            full_name = claims.get("name")
            
            realm_roles = []
            realm_access = claims.get("realm_access", {})
            if isinstance(realm_access, dict):
                realm_roles = realm_access.get("roles", [])
            
            all_roles = []
            all_roles.extend(realm_roles)
            
            resource_access = claims.get("resource_access", {})
            if isinstance(resource_access, dict):
                for _, client_data in resource_access.items():
                    if isinstance(client_data, dict):
                        client_roles = client_data.get("roles", [])
                        if isinstance(client_roles, list):
                            all_roles.extend(client_roles)
            
            all_roles = list(set(all_roles))
            
            request.state.user_id = int(user_id) if user_id else None
            request.state.tenant_id = int(tenant_id) if tenant_id else None
            request.state.email = email
            request.state.username = username
            request.state.full_name = full_name
            request.state.roles = all_roles
            request.state.realm_roles = realm_roles
            request.state.jwt_claims = claims
            request.state.token = token
            
            logger.debug(
                f"Authenticated user: {user_id} (tenant: {tenant_id}) for {request.url.path}"
            )
            
        except Exception as e:
            logger.error(f"JWT validation failed for {request.url.path}: {e}")
            error_response = response_utils.unauthorized(
                message=f"Invalid authentication token: {str(e)}"
            )
            return JSONResponse(
                status_code=status.HTTP_401_UNAUTHORIZED,
                content=error_response.model_dump(),
                headers={"WWW-Authenticate": "Bearer"},
            )
        
        response = await call_next(request)
        return response
    
    def _is_excluded_path(self, path: str) -> bool:
        """Check if path is excluded from authentication"""
        if path in self.EXCLUDED_PATHS:
            return True
        
        if path.startswith("/docs") or path.startswith("/redoc"):
            return True
        
        return False


class RoleBasedAccessMiddleware(BaseHTTPMiddleware):
    """
    Middleware to check role-based access control.
    Requires JWTAuthMiddleware to run first.
    """
    
    def __init__(self, app, required_roles: Optional[List[str]] = None):
        super().__init__(app)
        self.required_roles = required_roles or []
    
    async def dispatch(self, request: Request, call_next):
        """Check if user has required roles"""
        
        if not self.required_roles:
            return await call_next(request)
        
        # Get user roles from request state (set by JWTAuthMiddleware)
        user_roles = getattr(request.state, "roles", [])
        
        if not user_roles:
            logger.warning(f"No roles found in request state for {request.url.path}")
            error_response = response_utils.forbidden(
                message="Insufficient permissions"
            )
            return JSONResponse(
                status_code=status.HTTP_403_FORBIDDEN,
                content=error_response.model_dump(),
            )
        
        user_roles_upper = [r.upper() for r in user_roles]
        required_roles_upper = [r.upper() for r in self.required_roles]
        
        has_access = any(role in user_roles_upper for role in required_roles_upper)
        
        if not has_access:
            logger.warning(
                f"User does not have required roles for {request.url.path}. "
                f"Required: {self.required_roles}, User has: {user_roles}"
            )
            error_response = response_utils.forbidden(
                message=f"Insufficient permissions. Required roles: {', '.join(self.required_roles)}"
            )
            return JSONResponse(
                status_code=status.HTTP_403_FORBIDDEN,
                content=error_response.model_dump(),
            )
        
        response = await call_next(request)
        return response
