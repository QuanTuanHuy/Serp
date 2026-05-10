/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project detail redirect
 */

import { redirect } from 'next/navigation';

export default async function PMProjectDetailEntryPage({
  params,
}: {
  params: Promise<{ projectId: string }>;
}) {
  const { projectId } = await params;

  redirect(`/pm/projects/${projectId}/summary`);
}
