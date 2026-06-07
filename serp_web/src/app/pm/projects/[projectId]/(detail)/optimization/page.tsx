/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project optimization route
 */

import { PMProjectOptimizationPage } from '@/modules/pm';

interface PMProjectOptimizationRoutePageProps {
  params: Promise<{ projectId: string }>;
}

export default async function PMProjectOptimizationRoutePage({
  params,
}: PMProjectOptimizationRoutePageProps) {
  const { projectId } = await params;

  return <PMProjectOptimizationPage projectId={projectId} />;
}
