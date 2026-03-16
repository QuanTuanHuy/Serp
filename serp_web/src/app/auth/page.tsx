/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Standalone authentication page
 */

import { AuthLayout } from '@/modules/account';

interface AuthPageProps {
  searchParams: Promise<{
    mode?: string | string[];
  }>;
}

export default async function AuthPage({ searchParams }: AuthPageProps) {
  const { mode } = await searchParams;
  const normalizedMode = Array.isArray(mode) ? mode[0] : mode;
  const defaultMode = normalizedMode === 'register' ? 'register' : 'login';

  return <AuthLayout defaultMode={defaultMode} />;
}
