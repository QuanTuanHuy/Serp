/*
Author: QuanTuanHuy
Description: Part of Serp Project - Logistics module auth guard
*/

'use client';

import { ReactNode } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components';
import { ShieldAlert } from 'lucide-react';

interface LogisticsAuthGuardProps {
  children: ReactNode;
}

// Simple permission check - replace with actual hook when available
const usePermissions = () => {
  return {
    hasAnyRole: (roles: string[]) => true, // Allow access for now
    isLoading: false,
  };
};

export const LogisticsAuthGuard = ({ children }: LogisticsAuthGuardProps) => {
  const { hasAnyRole, isLoading } = usePermissions();

  // Check if user has any Logistics module role
  const hasAccess = hasAnyRole([
    'LOGISTICS_ADMIN',
    'LOGISTICS_MANAGER',
    'LOGISTICS_STAFF',
  ]);

  if (isLoading) {
    return (
      <div className='flex items-center justify-center min-h-screen'>
        <div className='animate-spin rounded-full h-8 w-8 border-b-2 border-primary'></div>
      </div>
    );
  }

  if (!hasAccess) {
    return (
      <div className='flex items-center justify-center min-h-screen p-6'>
        <Card className='max-w-md w-full'>
          <CardHeader>
            <div className='flex items-center gap-2'>
              <ShieldAlert className='h-5 w-5 text-destructive' />
              <CardTitle>Không có quyền truy cập</CardTitle>
            </div>
          </CardHeader>
          <CardContent>
            <p className='text-sm text-muted-foreground'>
              Bạn không có quyền truy cập module Logistics. Vui lòng liên hệ
              quản trị viên để được cấp quyền.
            </p>
          </CardContent>
        </Card>
      </div>
    );
  }

  return <>{children}</>;
};
