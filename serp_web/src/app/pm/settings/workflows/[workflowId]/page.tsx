/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM workflow editor route
 */

import { PMWorkflowEditorPage } from '@/modules/pm';

export default async function Page({
  params,
}: {
  params: Promise<{ workflowId: string }>;
}) {
  const { workflowId } = await params;

  return <PMWorkflowEditorPage workflowId={Number(workflowId)} />;
}
