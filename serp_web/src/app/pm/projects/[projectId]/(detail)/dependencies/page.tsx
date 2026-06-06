/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project dependencies route
 */

import { PMProjectDependenciesPage } from '@/modules/pm';

interface PMProjectDependenciesRoutePageProps {
  params: Promise<{ projectId: string }>;
}

export default async function PMProjectDependenciesRoutePage({
  params,
}: PMProjectDependenciesRoutePageProps) {
  const { projectId } = await params;

  return <PMProjectDependenciesPage projectId={projectId} />;
}
