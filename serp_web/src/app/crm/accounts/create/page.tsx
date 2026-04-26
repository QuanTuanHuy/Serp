// CRM Account Create Page (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { useRouter } from 'next/navigation';
import { CreateAccountPage } from '@/modules/crm/pages';

export default function CreateAccountRoute() {
  const router = useRouter();

  return (
    <CreateAccountPage
      onSuccess={(accountId) => router.push(`/crm/accounts/${accountId}`)}
      onCancel={() => router.back()}
    />
  );
}
