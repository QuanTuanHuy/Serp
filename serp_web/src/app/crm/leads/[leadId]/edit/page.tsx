/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

import { EditLeadPage } from '@/modules/crm/pages';

interface Props {
  params: Promise<{
    leadId: string;
  }>;
}

export default async function Page({ params }: Props) {
  const { leadId } = await params;
  return <EditLeadPage leadId={leadId} />;
}
