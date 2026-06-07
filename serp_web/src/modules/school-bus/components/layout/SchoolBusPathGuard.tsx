'use client';

/**
 * SchoolBusPathGuard
 *
 * Defense-in-depth route guard for the School Bus module.
 * This is a SECOND layer of protection AFTER the module-level RouteGuard
 * (which is driven by the menu display API).
 *
 * Purpose:
 * - Block direct URL access for roles that have no business visiting a path.
 * - E.g. Parent typing /school-bus/dispatch directly.
 *
 * Note: This is UX-only. Backend APIs still enforce data authorization.
 * The primary guard is RouteGuard (menu display API). This is an extra layer.
 */

import React from 'react';
import { usePathname } from 'next/navigation';
import { LockKeyhole } from 'lucide-react';
import { Button } from '@/shared/components/ui';
import Link from 'next/link';
import { useSchoolBusAccess, canAccessSchoolBusRoute } from '../../security/schoolBusAccess';

interface SchoolBusPathGuardProps {
  children: React.ReactNode;
}

export function SchoolBusPathGuard({ children }: SchoolBusPathGuardProps) {
  const pathname = usePathname();
  const access = useSchoolBusAccess();

  // While roles are loading (user is null), allow rendering (RouteGuard handles auth)
  if (!access.roles.length) {
    return <>{children}</>;
  }

  const allowed = canAccessSchoolBusRoute(access.roles, pathname);

  if (!allowed) {
    return (
      <div className='flex min-h-[60vh] flex-col items-center justify-center gap-6 text-center px-4'>
        <div className='flex h-16 w-16 items-center justify-center rounded-2xl bg-red-50 ring-1 ring-red-100'>
          <LockKeyhole className='h-8 w-8 text-[#C81E3A]' />
        </div>
        <div className='space-y-2'>
          <h2 className='text-xl font-bold text-slate-900'>Access Restricted</h2>
          <p className='text-sm text-slate-500 max-w-sm'>
            Your current role does not have permission to view this page.
            Contact your administrator if you believe this is a mistake.
          </p>
        </div>
        <Button asChild className='rounded-full bg-[#C81E3A] hover:bg-[#A6142D] text-white'>
          <Link href={access.landingPath}>Go to my workspace</Link>
        </Button>
      </div>
    );
  }

  return <>{children}</>;
}
