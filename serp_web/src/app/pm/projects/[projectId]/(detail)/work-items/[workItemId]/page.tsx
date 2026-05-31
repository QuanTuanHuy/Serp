/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item detail route
 */

import { PMWorkItemDetailPage as PMWorkItemDetailModulePage } from '@/modules/pm/pages/PMWorkItemDetailPage';

interface PMWorkItemDetailRoutePageProps {
  params: Promise<{ projectId: string; workItemId: string }>;
}

export default async function PMWorkItemDetailRoutePage({
  params,
}: PMWorkItemDetailRoutePageProps) {
  const { projectId, workItemId } = await params;

  return (
    <PMWorkItemDetailModulePage
      projectId={projectId}
      workItemId={workItemId}
    />
  );
}
