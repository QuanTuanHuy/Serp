/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project edit page
 */

import { PMProjectEditPage } from '@/modules/pm/pages/PMProjectEditPage';

interface PMProjectEditPageProps {
  params: Promise<{ projectId: string }>;
}

export default async function Page({ params }: PMProjectEditPageProps) {
  const { projectId } = await params;

  return <PMProjectEditPage projectId={projectId} />;
}
