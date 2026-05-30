/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project people route
 */

import { PMProjectPeoplePage } from '@/modules/pm/pages/PMProjectPeoplePage';

export default async function ProjectPeopleRoute({
  params,
}: {
  params: Promise<{ projectId: string }>;
}) {
  const { projectId } = await params;
  return <PMProjectPeoplePage projectId={projectId} />;
}
