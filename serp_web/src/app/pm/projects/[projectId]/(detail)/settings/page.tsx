/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project settings route
 */

import { PMProjectSettingsPage } from '@/modules/pm';

interface PMProjectSettingsRoutePageProps {
  params: Promise<{ projectId: string }>;
}

export default async function PMProjectSettingsRoutePage({
  params,
}: PMProjectSettingsRoutePageProps) {
  const { projectId } = await params;

  return <PMProjectSettingsPage projectId={projectId} />;
}
