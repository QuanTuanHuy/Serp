"""
Author: QuanTuanHuy
Description: Part of Serp Project - Authentication Utilities
"""

from typing import Optional, List
from fastapi import Request, HTTPException, status
from loguru import logger

from src.kernel.utils.jwt_utils import JwtUtils


class AuthUtils:
    """
    Utility class for authentication and authorization.
    Extracts user information from JWT tokens in requests.
    """
    
    def __init__(self, jwt_utils: Optional[JwtUtils] = None):
        self.jwt_utils = jwt_utils or JwtUtils()
    
    def extract_token_from_request(self, request: Request) -> Optional[str]:
        """
        Extract JWT token from Authorization header.
        
        Args:
            request: FastAPI request object
            
        Returns:
            Token string without 'Bearer ' prefix, or None if not found
        """
        auth_header = request.headers.get("Authorization")
        
        if not auth_header:
            return None
        
        if not auth_header.startswith("Bearer "):
            logger.warning("Authorization header does not start with 'Bearer '")
            return None
        
        token = auth_header[7:]
        return token
    
    def get_current_user_id(self, request: Request) -> Optional[int]:
        """
        Get current user ID from request.
        
        Args:
            request: FastAPI request object
            
        Returns:
            User ID or None if not found
        """
        token = self.extract_token_from_request(request)
        if not token:
            logger.warning("No token found in request")
            return None
        
        return self.jwt_utils.get_user_id_from_token(token)
    
    def get_current_tenant_id(self, request: Request) -> Optional[int]:
        """
        Get current tenant ID from request.
        
        Args:
            request: FastAPI request object
            
        Returns:
            Tenant ID or None if not found
        """
        token = self.extract_token_from_request(request)
        if not token:
            logger.warning("No token found in request")
            return None
        
        return self.jwt_utils.get_tenant_id_from_token(token)
    
    def get_current_user_email(self, request: Request) -> Optional[str]:
        """Get current user email from request"""
        token = self.extract_token_from_request(request)
        if not token:
            return None
        
        return self.jwt_utils.get_email_from_token(token)
    
    def get_current_user_full_name(self, request: Request) -> Optional[str]:
        """Get current user full name from request"""
        token = self.extract_token_from_request(request)
        if not token:
            return None
        
        return self.jwt_utils.get_full_name_from_token(token)
    
    def get_realm_roles(self, request: Request) -> List[str]:
        """Get realm roles from request"""
        token = self.extract_token_from_request(request)
        if not token:
            return []
        
        return self.jwt_utils.get_realm_roles_from_token(token)
    
    def get_client_roles(self, request: Request, client_id: str) -> List[str]:
        """Get client roles from request"""
        token = self.extract_token_from_request(request)
        if not token:
            return []
        
        return self.jwt_utils.get_resource_roles_from_token(token, client_id)
    
    def get_all_roles(self, request: Request) -> List[str]:
        """Get all roles (realm + resource) from request"""
        token = self.extract_token_from_request(request)
        if not token:
            return []
        
        return self.jwt_utils.get_roles_from_token(token)
    
    def has_realm_role(self, request: Request, role_name: str) -> bool:
        """Check if user has a specific realm role"""
        realm_roles = self.get_realm_roles(request)
        return role_name in realm_roles or role_name.upper() in [r.upper() for r in realm_roles]
    
    def has_client_role(self, request: Request, client_id: str, role_name: str) -> bool:
        """Check if user has a specific client role"""
        client_roles = self.get_client_roles(request, client_id)
        return role_name in client_roles or role_name.upper() in [r.upper() for r in client_roles]
    
    def has_any_role(self, request: Request, *role_names: str) -> bool:
        """Check if user has any of the specified roles"""
        all_roles = self.get_all_roles(request)
        all_roles_upper = [r.upper() for r in all_roles]
        
        for role_name in role_names:
            if role_name in all_roles or role_name.upper() in all_roles_upper:
                return True
        
        return False
    
    def is_system_admin(self, request: Request) -> bool:
        """Check if user is a system admin"""
        return self.has_any_role(request, "SUPER_ADMIN", "SYSTEM_MODERATOR")
    
    def can_access_organization(self, request: Request, organization_id: int) -> bool:
        """
        Validate tenant access: Check if user's tenantId matches the organizationId.
        System admins bypass this check.
        
        Args:
            request: FastAPI request object
            organization_id: Organization ID to check access for
            
        Returns:
            True if user has access, False otherwise
        """
        if self.is_system_admin(request):
            return True
        
        current_tenant_id = self.get_current_tenant_id(request)
        if current_tenant_id is None:
            logger.warning("User has no tenantId in JWT")
            return False
        
        has_access = current_tenant_id == organization_id
        if not has_access:
            logger.warning(
                f"User tenantId {current_tenant_id} does not match organizationId {organization_id}"
            )
        
        return has_access
    
    def validate_token(self, request: Request) -> bool:
        """
        Validate token from request.
        
        Args:
            request: FastAPI request object
            
        Returns:
            True if token is valid, False otherwise
        """
        token = self.extract_token_from_request(request)
        if not token:
            return False
        
        return self.jwt_utils.is_token_valid(token)
    
    def require_authentication(self, request: Request) -> int:
        """
        Require authentication and return user ID.
        
        Args:
            request: FastAPI request object
            
        Returns:
            User ID
            
        Raises:
            HTTPException: If authentication fails
        """
        token = self.extract_token_from_request(request)
        if not token:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Missing authentication token",
                headers={"WWW-Authenticate": "Bearer"},
            )
        
        user_id = self.jwt_utils.get_user_id_from_token(token)
        if user_id is None:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid authentication token",
                headers={"WWW-Authenticate": "Bearer"},
            )
        
        return user_id
    
    def require_role(self, request: Request, *required_roles: str) -> None:
        """
        Require user to have at least one of the specified roles.
        
        Args:
            request: FastAPI request object
            required_roles: Role names to check
            
        Raises:
            HTTPException: If user doesn't have required role
        """
        if not self.has_any_role(request, *required_roles):
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail=f"Insufficient permissions. Required roles: {', '.join(required_roles)}",
            )
    
    def require_organization_access(self, request: Request, organization_id: int) -> None:
        """
        Require user to have access to organization.
        
        Args:
            request: FastAPI request object
            organization_id: Organization ID to check access for
            
        Raises:
            HTTPException: If user doesn't have access
        """
        if not self.can_access_organization(request, organization_id):
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="You don't have access to this organization",
            )


# Global instance
auth_utils = AuthUtils()
