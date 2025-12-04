// Opportunity Detail Page Route (authors: QuanTuanHuy, Description: Part of Serp Project)

import { OpportunityDetailPage } from '@/modules/crm/pages/opportunities';

interface Props {
  params: Promise<{
    opportunityId: string;
  }>;
}

export default async function Page({ params }: Props) {
  const { opportunityId } = await params;
  return <OpportunityDetailPage opportunityId={opportunityId} />;
}
