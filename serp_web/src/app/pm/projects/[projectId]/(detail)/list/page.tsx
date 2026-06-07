/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project work item list page
 */

import { PMWorkItemListTab } from '@/modules/pm/components/work-items';

interface PMProjectWorkItemListPageProps {
  params: Promise<{ projectId: string }>;
}

export default async function PMProjectWorkItemListPage({
  params,
}: PMProjectWorkItemListPageProps) {
  const { projectId } = await params;
  const numericProjectId = Number(projectId);

  if (!Number.isFinite(numericProjectId)) {
    return (
      <div className='rounded-lg border bg-card p-8 text-card-foreground shadow-sm'>
        <p className='text-muted-foreground'>Invalid project id.</p>
      </div>
    );
  }

  return <PMWorkItemListTab projectId={numericProjectId} />;
}
