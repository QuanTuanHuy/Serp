/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

import { EditOpportunityPage } from '@/modules/crm/pages';

interface Props {
  params: Promise<{
    opportunityId: string;
  }>;
}

export default async function Page({ params }: Props) {
  const { opportunityId } = await params;
  return <EditOpportunityPage opportunityId={opportunityId} />;
}
