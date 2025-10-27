/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Standalone authentication page
 */

'use client';

import { AuthLayout } from '@/modules/account';

export default function AuthPage() {
  const handleAuthSuccess = () => {};

  return <AuthLayout onAuthSuccess={handleAuthSuccess} />;
}
