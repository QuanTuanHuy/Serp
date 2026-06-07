'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';

/**
 * Legacy route — standalone route creation is no longer supported.
 * Redirects to the session-based Route Planning Workspace.
 */
export default function Page() {
  const router = useRouter();
  useEffect(() => {
    router.replace('/school-bus/dispatch/planning');
  }, [router]);
  return null;
}
