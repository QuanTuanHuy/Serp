// CRM Opportunity Create Page (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { useRouter } from 'next/navigation';
import { CreateOpportunityPage } from '@/modules/crm/pages';

export default function CreateOpportunityRoute() {
  const router = useRouter();

  return (
    <CreateOpportunityPage
      onSuccess={(opportunityId) => router.push(`/crm/opportunities/${opportunityId}`)}
      onCancel={() => router.back()}
    />
  );
}
