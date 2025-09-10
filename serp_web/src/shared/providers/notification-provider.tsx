/**
 * Notification Provider - Toast notifications setup
 * (authors: QuanTuanHuy, Description: Part of Serp Project)
 */

'use client';

import { Toaster } from 'sonner';

export function NotificationProvider({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <>
      {children}
      <Toaster
        position='bottom-right'
        offset={20}
        closeButton={true}
        richColors={true}
        expand={true}
        visibleToasts={4}
        duration={4000}
        theme='system'
        dir='ltr'
      />
    </>
  );
}
