/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Component to protect routes that require authentication
 */

'use client';

import React from 'react';
import { useAuth } from '../hooks';
import { AuthLayout } from './AuthLayout';
import { RoleGuard } from './RoleGuard';

interface ProtectedRouteProps {
  children: React.ReactNode;
  fallback?: React.ReactNode;
  redirectTo?: string;

  roles?: string | string[];
  permissions?: string | string[];
  menuKey?: string;
  moduleKey?: string;
  featureKey?: string;
  requireAllRoles?: boolean;
  requireAllPermissions?: boolean;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  fallback,
  roles,
  permissions,
  menuKey,
  moduleKey,
  featureKey,
  requireAllRoles,
  requireAllPermissions,
}) => {
  const { isAuthenticated, isLoading, token } = useAuth();

  if (isLoading || (token && !isAuthenticated)) {
    return (
      <div className='min-h-screen flex items-center justify-center'>
        <div className='text-center space-y-4'>
          <div className='animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto'></div>
          <p className='text-muted-foreground'>Checking authentication...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return fallback || <AuthLayout />;
  }

  if (!roles && !permissions && !menuKey && !featureKey) {
    return <>{children}</>;
  }

  return (
    <RoleGuard
      roles={roles}
      permissions={permissions}
      menuKey={menuKey}
      moduleKey={moduleKey}
      featureKey={featureKey}
      requireAllRoles={requireAllRoles}
      requireAllPermissions={requireAllPermissions}
      fallback={fallback}
    >
      {children}
    </RoleGuard>
  );
};

export const withAuth = <P extends object>(
  Component: React.ComponentType<P>,
  options?: {
    fallback?: React.ReactNode;
    redirectTo?: string;
    roles?: string | string[];
    permissions?: string | string[];
    menuKey?: string;
    moduleKey?: string;
    featureKey?: string;
    requireAllRoles?: boolean;
    requireAllPermissions?: boolean;
  }
) => {
  const AuthenticatedComponent = (props: P) => (
    <ProtectedRoute
      fallback={options?.fallback}
      redirectTo={options?.redirectTo}
      roles={options?.roles}
      permissions={options?.permissions}
      menuKey={options?.menuKey}
      moduleKey={options?.moduleKey}
      featureKey={options?.featureKey}
      requireAllRoles={options?.requireAllRoles}
      requireAllPermissions={options?.requireAllPermissions}
    >
      <Component {...props} />
    </ProtectedRoute>
  );

  AuthenticatedComponent.displayName = `withAuth(${Component.displayName || Component.name})`;

  return AuthenticatedComponent;
};
