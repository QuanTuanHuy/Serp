"""
Author: QuanTuanHuy
Description: Part of Serp Project - JWT Utilities
"""

import jwt
import requests
from typing import Optional, List, Dict, Any
from datetime import datetime
from loguru import logger
from functools import lru_cache

from src.config import settings


class JwtUtils:
    """
    Utility class for JWT token validation and extraction.
    Validates tokens against Keycloak JWKS endpoint.
    """
    
    def __init__(self):
        self._jwks_uri = f"{settings.keycloak_server_url}/realms/{settings.keycloak_realm}/protocol/openid-connect/certs"
        self._expected_issuer = f"{settings.keycloak_server_url}/realms/{settings.keycloak_realm}"
        self._expected_audience = settings.keycloak_client_id
        self._public_keys: Optional[Dict[str, Any]] = None
    
    @lru_cache(maxsize=1)
    def _fetch_public_keys(self) -> Dict[str, Any]:
        """
        Fetch public keys from Keycloak JWKS endpoint.
        Cached to avoid repeated requests.
        """
        try:
            response = requests.get(self._jwks_uri, timeout=10)
            response.raise_for_status()
            jwks = response.json()
            
            # Convert JWKS to dict keyed by kid
            keys = {}
            for key in jwks.get("keys", []):
                kid = key.get("kid")
                if kid:
                    keys[kid] = jwt.algorithms.RSAAlgorithm.from_jwk(key)
            
            logger.debug(f"Fetched {len(keys)} public keys from Keycloak")
            return keys
        except Exception as e:
            logger.error(f"Failed to fetch public keys from Keycloak: {e}")
            return {}
    
    def _get_public_key(self, kid: str) -> Optional[Any]:
        """Get public key by key ID"""
        if self._public_keys is None:
            self._public_keys = self._fetch_public_keys()
        
        return self._public_keys.get(kid)
    
    def validate_token(self, token: str) -> Dict[str, Any]:
        """
        Validate JWT token and return claims.
        
        Args:
            token: JWT token string
            
        Returns:
            Dict containing JWT claims
            
        Raises:
            Exception: If token is invalid
        """
        try:
            unverified_header = jwt.get_unverified_header(token)
            kid = unverified_header.get("kid")
            
            if not kid:
                logger.warning("No key ID found in JWT header, attempting validation without signature verification")
                return self._validate_token_without_signature(token)
            
            public_key = self._get_public_key(kid)
            if not public_key:
                logger.warning(f"Could not find public key for key ID: {kid}, refreshing keys")
                # Clear cache and try again
                self._fetch_public_keys.cache_clear()
                self._public_keys = None
                public_key = self._get_public_key(kid)
                
                if not public_key:
                    logger.warning("Still no public key found, attempting validation without signature")
                    return self._validate_token_without_signature(token)
            
            payload = jwt.decode(
                token,
                public_key,
                algorithms=["RS256"],
                # audience=self._expected_audience,
                issuer=self._expected_issuer,
                options={
                    "verify_signature": True,
                    "verify_exp": True,
                    "verify_iat": True,
                    "verify_aud": False,
                    "verify_iss": True,
                }
            )
            
            logger.debug(f"Successfully validated token with key ID: {kid}")
            return payload
            
        except jwt.ExpiredSignatureError:
            logger.error("JWT token has expired")
            raise Exception("JWT token has expired")
        except jwt.InvalidTokenError as e:
            logger.error(f"Invalid JWT token: {e}")
            raise Exception(f"Invalid JWT token: {e}")
        except Exception as e:
            logger.error(f"Error validating JWT token: {e}")
            raise Exception(f"Error validating JWT token: {e}")
    
    def _validate_token_without_signature(self, token: str) -> Dict[str, Any]:
        """
        Validate token without signature verification (fallback for development).
        Still validates expiration and basic claims.
        """
        try:
            payload = jwt.decode(
                token,
                options={
                    "verify_signature": False,
                    "verify_exp": True,
                    "verify_iat": True,
                }
            )
            
            issuer = payload.get("iss")
            if issuer and self._expected_issuer and issuer != self._expected_issuer:
                raise Exception(f"JWT token issuer mismatch. Expected: {self._expected_issuer}, Actual: {issuer}")
            
            # Manual audience check
            # audiences = payload.get("aud")
            # if audiences:
            #     if isinstance(audiences, str):
            #         audiences = [audiences]
            #     if self._expected_audience and self._expected_audience not in audiences:
            #         logger.warning(f"JWT token audience mismatch. Expected: {self._expected_audience}, Actual: {audiences}")
            
            logger.debug("Validated token without signature verification")
            return payload
            
        except jwt.ExpiredSignatureError:
            logger.error("JWT token has expired")
            raise Exception("JWT token has expired")
        except Exception as e:
            logger.error(f"Error validating JWT token without signature: {e}")
            raise Exception(f"Invalid JWT token: {e}")
    
    def get_user_id_from_token(self, token: str) -> Optional[int]:
        """Extract user ID from token"""
        try:
            claims = self.validate_token(token)
            uid = claims.get("uid")
            
            if uid is not None:
                return int(uid)
            
            logger.warning("No uid claim found in token")
            return None
        except Exception as e:
            logger.error(f"Error extracting user ID from token: {e}")
            return None
    
    def get_tenant_id_from_token(self, token: str) -> Optional[int]:
        """Extract tenant ID from token"""
        try:
            claims = self.validate_token(token)
            tid = claims.get("tid")
            
            if tid is not None:
                return int(tid)
            
            logger.warning("No tid claim found in token")
            return None
        except Exception as e:
            logger.error(f"Error extracting tenant ID from token: {e}")
            return None
    
    def get_subject_from_token(self, token: str) -> Optional[str]:
        """Extract subject from token"""
        try:
            claims = self.validate_token(token)
            return claims.get("sub")
        except Exception as e:
            logger.error(f"Error extracting subject from token: {e}")
            return None
    
    def get_email_from_token(self, token: str) -> Optional[str]:
        """Extract email from token"""
        try:
            claims = self.validate_token(token)
            return claims.get("email")
        except Exception as e:
            logger.error(f"Error extracting email from token: {e}")
            return None
    
    def get_preferred_username_from_token(self, token: str) -> Optional[str]:
        """Extract preferred username from token"""
        try:
            claims = self.validate_token(token)
            return claims.get("preferred_username")
        except Exception as e:
            logger.error(f"Error extracting preferred_username from token: {e}")
            return None
    
    def get_full_name_from_token(self, token: str) -> Optional[str]:
        """Extract full name from token"""
        try:
            claims = self.validate_token(token)
            return claims.get("name")
        except Exception as e:
            logger.error(f"Error extracting name from token: {e}")
            return None
    
    def get_roles_from_token(self, token: str) -> List[str]:
        """
        Extract all roles from token (realm roles + resource roles).
        
        Args:
            token: JWT token string
            
        Returns:
            List of role names
        """
        try:
            claims = self.validate_token(token)
            roles = []
            
            realm_access = claims.get("realm_access", {})
            if isinstance(realm_access, dict):
                realm_roles = realm_access.get("roles", [])
                if isinstance(realm_roles, list):
                    roles.extend(realm_roles)
            
            resource_access = claims.get("resource_access", {})
            if isinstance(resource_access, dict):
                for _, client_data in resource_access.items():
                    if isinstance(client_data, dict):
                        client_roles = client_data.get("roles", [])
                        if isinstance(client_roles, list):
                            roles.extend(client_roles)
            
            roles = [r for r in roles if r is not None]
            return list(set(roles))
            
        except Exception as e:
            logger.error(f"Error extracting roles from token: {e}")
            return []
    
    def get_realm_roles_from_token(self, token: str) -> List[str]:
        """Extract realm roles from token"""
        try:
            claims = self.validate_token(token)
            realm_access = claims.get("realm_access", {})
            
            if isinstance(realm_access, dict):
                roles = realm_access.get("roles", [])
                if isinstance(roles, list):
                    return roles
            
            return []
        except Exception as e:
            logger.error(f"Error extracting realm roles from token: {e}")
            return []
    
    def get_resource_roles_from_token(self, token: str, client_id: str) -> List[str]:
        """Extract resource roles for a specific client from token"""
        try:
            claims = self.validate_token(token)
            resource_access = claims.get("resource_access", {})
            
            if isinstance(resource_access, dict):
                client_access = resource_access.get(client_id, {})
                if isinstance(client_access, dict):
                    roles = client_access.get("roles", [])
                    if isinstance(roles, list):
                        return roles
            
            return []
        except Exception as e:
            logger.error(f"Error extracting resource roles for client {client_id}: {e}")
            return []
    
    def has_role(self, token: str, role_name: str) -> bool:
        """Check if token has a specific role (case-insensitive)"""
        roles = self.get_roles_from_token(token)
        role_name_upper = role_name.upper()
        return any(r.upper() == role_name_upper for r in roles)
    
    def has_realm_role(self, token: str, role_name: str) -> bool:
        """Check if token has a specific realm role"""
        realm_roles = self.get_realm_roles_from_token(token)
        return role_name in realm_roles
    
    def has_resource_role(self, token: str, client_id: str, role_name: str) -> bool:
        """Check if token has a specific resource role for a client"""
        resource_roles = self.get_resource_roles_from_token(token, client_id)
        return role_name in resource_roles
    
    def is_token_expired(self, token: str) -> bool:
        """Check if token is expired"""
        try:
            claims = jwt.decode(token, options={"verify_signature": False})
            exp = claims.get("exp")
            
            if exp is None:
                return True
            
            return datetime.fromtimestamp(exp) < datetime.now()
        except Exception as e:
            logger.error(f"Error checking token expiration: {e}")
            return True
    
    def is_bearer_token(self, token: str) -> bool:
        """Check if token is a Bearer token"""
        try:
            claims = jwt.decode(token, options={"verify_signature": False})
            typ = claims.get("typ")
            return typ == "Bearer"
        except Exception as e:
            logger.error(f"Error checking token type: {e}")
            return False
    
    def get_claims_from_token(self, token: str) -> Optional[Dict[str, Any]]:
        """Get all claims from token"""
        try:
            return self.validate_token(token)
        except Exception as e:
            logger.error(f"Error getting claims from token: {e}")
            return None
    
    def is_token_valid(self, token: str) -> bool:
        """Check if token is valid"""
        try:
            self.validate_token(token)
            return True
        except Exception:
            return False
    
    def is_email_verified_from_token(self, token: str) -> bool:
        """Check if email is verified in token"""
        try:
            claims = self.validate_token(token)
            email_verified = claims.get("email_verified")
            return bool(email_verified)
        except Exception as e:
            logger.error(f"Error checking email verification: {e}")
            return False
    
    def get_authorized_party_from_token(self, token: str) -> Optional[str]:
        """Extract authorized party (azp) from token"""
        try:
            claims = self.validate_token(token)
            return claims.get("azp")
        except Exception as e:
            logger.error(f"Error extracting azp from token: {e}")
            return None
    
    def get_session_id_from_token(self, token: str) -> Optional[str]:
        """Extract session ID (sid) from token"""
        try:
            claims = self.validate_token(token)
            return claims.get("sid")
        except Exception as e:
            logger.error(f"Error extracting sid from token: {e}")
            return None
