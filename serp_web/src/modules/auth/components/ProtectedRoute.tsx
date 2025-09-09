/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Component to protect routes that require authentication
 */

'use client';

import React from 'react';
import { useAuth } from '../hooks/useAuth';
import { AuthLayout } from './AuthLayout';

interface ProtectedRouteProps {
  children: React.ReactNode;
  fallback?: React.ReactNode;
  redirectTo?: string;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  fallback,
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

  return <>{children}</>;
};

export const withAuth = <P extends object>(
  Component: React.ComponentType<P>,
  options?: {
    fallback?: React.ReactNode;
    redirectTo?: string;
  }
) => {
  const AuthenticatedComponent = (props: P) => (
    <ProtectedRoute
      fallback={options?.fallback}
      redirectTo={options?.redirectTo}
    >
      <Component {...props} />
    </ProtectedRoute>
  );

  AuthenticatedComponent.displayName = `withAuth(${Component.displayName || Component.name})`;

  return AuthenticatedComponent;
};
