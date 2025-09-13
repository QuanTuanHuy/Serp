/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Standalone authentication page
 */

'use client';

import { AuthLayout } from '@/modules/account';
import { useRouter } from 'next/navigation';

export default function AuthPage() {
  const router = useRouter();

  const handleAuthSuccess = () => {
    router.push('/');
  };

  return <AuthLayout onAuthSuccess={handleAuthSuccess} />;
}
