/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Standalone authentication page
 */

'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { AuthLayout } from '@/modules/account';
import { useAuth, useUser } from '@/modules/account';
import { SYSTEM_ADMIN_ROLES } from '@/shared';

export default function AuthPage() {
  const router = useRouter();
  const { isAuthenticated } = useAuth();
  const { user } = useUser();

  useEffect(() => {
    if (isAuthenticated && user) {
      if (user.roles.some((role) => SYSTEM_ADMIN_ROLES.includes(role))) {
        console.log('Redirecting to admin dashboard');
        router.push('/admin');
      } else {
        console.log('Redirecting to user dashboard');
        router.push('/');
      }
    }
  }, [isAuthenticated, user, router]);

  const handleAuthSuccess = () => {};

  return <AuthLayout onAuthSuccess={handleAuthSuccess} />;
}
