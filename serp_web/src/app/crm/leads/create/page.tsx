// CRM Lead Create Page (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { useRouter } from 'next/navigation';
import { CreateLeadPage } from '@/modules/crm/pages';

export default function CreateLeadRoute() {
  const router = useRouter();

  return (
    <CreateLeadPage
      onSuccess={(leadId) => router.push(`/crm/leads/${leadId}`)}
      onCancel={() => router.back()}
    />
  );
}
