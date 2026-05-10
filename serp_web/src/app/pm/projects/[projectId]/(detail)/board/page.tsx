/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project board page
 */

import { PMWorkItemBoard } from '@/modules/pm/components/work-items';

interface PMProjectBoardPageProps {
  params: Promise<{ projectId: string }>;
}

export default async function PMProjectBoardPage({
  params,
}: PMProjectBoardPageProps) {
  const { projectId } = await params;
  const numericProjectId = Number(projectId);

  if (!Number.isFinite(numericProjectId)) {
    return (
      <div className='rounded-lg border bg-card p-8 text-card-foreground shadow-sm'>
        <p className='text-muted-foreground'>Invalid project id.</p>
      </div>
    );
  }

  return <PMWorkItemBoard projectId={numericProjectId} />;
}
