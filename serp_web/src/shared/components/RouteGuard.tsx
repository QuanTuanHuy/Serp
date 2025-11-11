/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Route-level protection component
 */

'use client';

import React from 'react';
import { Loader2 } from 'lucide-react';
import { AccessDenied } from '@/modules/account/components';
import { useModuleRouteGuard } from '@/shared/hooks';

interface RouteGuardProps {
  moduleCode: string;

  children: React.ReactNode;

  loadingComponent?: React.ReactNode;

  accessDeniedComponent?: React.ReactNode;

  loadingVariant?: 'fullscreen' | 'inline';
}

const DefaultLoadingFullscreen: React.FC = () => (
  <div className='flex items-center justify-center min-h-screen'>
    <div className='flex flex-col items-center gap-4'>
      <Loader2 className='h-12 w-12 animate-spin text-primary' />
      <p className='text-sm text-muted-foreground'>Checking permissions...</p>
    </div>
  </div>
);

const DefaultLoadingInline: React.FC = () => (
  <div className='flex items-center justify-center p-8'>
    <div className='flex items-center gap-3'>
      <Loader2 className='h-6 w-6 animate-spin text-primary' />
      <p className='text-sm text-muted-foreground'>Verifying access...</p>
    </div>
  </div>
);

/**
 * RouteGuard Component
 *
 * Protects routes by checking if user has menu access to the current path.
 * Shows loading spinner while checking, AccessDenied if no access.
 *
 * @example
 * ```tsx
 * // In PTMLayout
 * <RouteGuard moduleCode="PTM">
 *   {children}
 * </RouteGuard>
 * ```
 */
export const RouteGuard: React.FC<RouteGuardProps> = ({
  moduleCode,
  children,
  loadingComponent,
  accessDeniedComponent,
  loadingVariant = 'fullscreen',
}) => {
  const { hasAccess, isLoading } = useModuleRouteGuard(moduleCode);

  if (isLoading) {
    if (loadingComponent) {
      return <>{loadingComponent}</>;
    }

    return loadingVariant === 'fullscreen' ? (
      <DefaultLoadingFullscreen />
    ) : (
      <DefaultLoadingInline />
    );
  }

  if (!hasAccess) {
    if (accessDeniedComponent) {
      return <>{accessDeniedComponent}</>;
    }

    return (
      <AccessDenied
        reason='authorization'
        title='Access Denied'
        description="You don't have permission to access this page."
        variant='minimal'
        size='md'
        showBackButton={true}
        showHomeButton={true}
      />
    );
  }

  return <>{children}</>;
};
