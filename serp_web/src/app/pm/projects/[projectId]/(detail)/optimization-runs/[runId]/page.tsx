/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project optimization run route
 */

import { PMProjectOptimizationRunPage } from '@/modules/pm';

interface PMProjectOptimizationRunRoutePageProps {
  params: Promise<{ projectId: string; runId: string }>;
}

export default async function PMProjectOptimizationRunRoutePage({
  params,
}: PMProjectOptimizationRunRoutePageProps) {
  const { projectId, runId } = await params;

  return <PMProjectOptimizationRunPage projectId={projectId} runId={runId} />;
}
