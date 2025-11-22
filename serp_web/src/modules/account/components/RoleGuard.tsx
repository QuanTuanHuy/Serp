/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Component to protect routes/components based on user roles and permissions
 */

'use client';

import React from 'react';
import { useAuth, usePermissions, useUser } from '../hooks';
import { AccessDenied } from './AccessDenied';

export interface RoleGuardProps {
  children: React.ReactNode;

  // Role-based access
  roles?: string | string[];
  requireAllRoles?: boolean;

  // Permission-based access
  permissions?: string | string[];
  requireAllPermissions?: boolean;

  // Module/Menu/Feature visibility
  menuKey?: string;
  moduleKey?: string;
  featureKey?: string;

  // Organization/Tenant based
  organizationId?: number;

  // Custom validation function
  customValidator?: (user: any, userPermissions: any) => boolean;

  // Fallback components
  fallback?: React.ReactNode;
  loading?: React.ReactNode;

  // Behavior options
  hideOnNoAccess?: boolean;
  redirectTo?: string;
}

export const RoleGuard: React.FC<RoleGuardProps> = ({
  children,
  roles,
  requireAllRoles = false,
  permissions,
  requireAllPermissions = false,
  menuKey,
  moduleKey,
  featureKey,
  organizationId,
  customValidator,
  fallback,
  loading,
  hideOnNoAccess = false,
}) => {
  const { isAuthenticated, isLoading } = useAuth();
  const { user } = useUser();
  const {
    userPermissions,
    canAccess,
    isLoading: permissionsLoading,
  } = usePermissions();

  console.log('RoleGuard Debug:', {
    isAuthenticated,
    isLoading,
    user,
    userPermissions,
  });

  // Show loading state
  if (isLoading || permissionsLoading) {
    return (
      loading || (
        <div className='animate-pulse'>
          <div className='h-4 bg-gray-200 rounded w-3/4'></div>
        </div>
      )
    );
  }

  // Not authenticated
  if (!isAuthenticated || !user) {
    if (hideOnNoAccess) return null;
    return (
      fallback || (
        <AccessDenied reason='authentication' variant='minimal' size='sm' />
      )
    );
  }

  // Check organization access
  if (organizationId && user.organizationId !== organizationId) {
    if (hideOnNoAccess) return null;
    return (
      fallback || (
        <AccessDenied reason='organization' variant='minimal' size='sm' />
      )
    );
  }

  // Use the simplified canAccess function
  const hasAccess = canAccess({
    roles: roles ? (Array.isArray(roles) ? roles : [roles]) : undefined,
    permissions: permissions
      ? Array.isArray(permissions)
        ? permissions
        : [permissions]
      : undefined,
    requireAllRoles,
    requireAllPermissions,
    menuKey,
    moduleKey,
    featureKey,
  });

  // Custom validation
  if (customValidator && !customValidator(user, userPermissions)) {
    if (hideOnNoAccess) return null;
    return (
      fallback || <AccessDenied reason='custom' variant='minimal' size='sm' />
    );
  }

  // Final access check
  if (!hasAccess) {
    if (hideOnNoAccess) return null;

    // Determine the most appropriate reason based on what was checked
    let reason: 'role' | 'permission' | 'authorization' = 'authorization';
    let requiredRoles: string[] | undefined;
    let requiredPermissions: string[] | undefined;

    if (roles) {
      reason = 'role';
      requiredRoles = Array.isArray(roles) ? roles : [roles];
    } else if (permissions) {
      reason = 'permission';
      requiredPermissions = Array.isArray(permissions)
        ? permissions
        : [permissions];
    }

    return (
      fallback || (
        <AccessDenied
          reason={reason}
          variant='detailed'
          size='sm'
          requiredRoles={requiredRoles}
          requiredPermissions={requiredPermissions}
        />
      )
    );
  }

  return <>{children}</>;
};

// HOC version for easier usage
export const withRoleGuard = <P extends object>(
  Component: React.ComponentType<P>,
  guardProps: Omit<RoleGuardProps, 'children'>
) => {
  const GuardedComponent = (props: P) => (
    <RoleGuard {...guardProps}>
      <Component {...props} />
    </RoleGuard>
  );

  GuardedComponent.displayName = `withRoleGuard(${Component.displayName || Component.name})`;
  return GuardedComponent;
};
