/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project summary page
 */

import { PMProjectSummaryPage as PMProjectSummaryModulePage } from '@/modules/pm/pages/PMProjectSummaryPage';

interface PMProjectSummaryPageProps {
  params: Promise<{ projectId: string }>;
}

export default async function PMProjectSummaryRoutePage({
  params,
}: PMProjectSummaryPageProps) {
  const { projectId } = await params;

  return <PMProjectSummaryModulePage projectId={projectId} />;
}
