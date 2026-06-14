'use client';

import { useEffect, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import { useSchoolBusAccess } from '@/modules/school-bus/security/schoolBusAccess';
import { useAppSelector, useModuleSidebar } from '@/shared/hooks';
import { selectUserProfile } from '@/modules/account/store';
import type { SidebarMenuItem } from '@/shared/hooks/useModuleSidebar';

function flattenMenuPaths(items: SidebarMenuItem[]): string[] {
  return items.flatMap((item) => [
    item.href,
    ...flattenMenuPaths(item.children ?? []),
  ]);
}

export default function SchoolBusPage() {
  const router = useRouter();
  const user = useAppSelector(selectUserProfile);
  const access = useSchoolBusAccess();
  const { menuItems, isLoading: menusLoading } = useModuleSidebar('SCHOOLBUS');
  const landingPath = useMemo(() => {
    const menuPaths = flattenMenuPaths(menuItems);

    if (menuPaths.includes(access.landingPath)) {
      return access.landingPath;
    }

    return menuPaths[0];
  }, [access.landingPath, menuItems]);

  useEffect(() => {
    if (user !== null && !menusLoading && landingPath) {
      router.replace(landingPath);
    }
  }, [landingPath, menusLoading, router, user]);

  return (
    <div className='flex h-full items-center justify-center'>
      <div className='text-center'>
        <div className='text-lg font-semibold'>
          Loading School Bus Module...
        </div>
        <div className='mt-2 text-sm text-muted-foreground'>
          Redirecting to your workspace...
        </div>
      </div>
    </div>
  );
}
