/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Legacy PM project kanban redirect
 */

import { redirect } from 'next/navigation';

export default async function PMProjectKanbanRedirectPage({
  params,
}: {
  params: Promise<{ projectId: string }>;
}) {
  const { projectId } = await params;

  redirect(`/pm/projects/${projectId}/board`);
}
