/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project components page
 */

import { PMProjectComponentsPage as PMProjectComponentsModulePage } from '@/modules/pm/pages/PMProjectComponentsPage';

interface PMProjectComponentsPageProps {
  params: Promise<{ projectId: string }>;
}

export default async function PMProjectComponentsPage({
  params,
}: PMProjectComponentsPageProps) {
  const { projectId } = await params;

  return <PMProjectComponentsModulePage projectId={projectId} />;
}
