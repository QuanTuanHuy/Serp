/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project summary page
 */

interface PMProjectSummaryPageProps {
  params: Promise<{ projectId: string }>;
}

export default async function PMProjectSummaryPage({
  params,
}: PMProjectSummaryPageProps) {
  const { projectId } = await params;

  return (
    <div className='rounded-lg border bg-card p-8 text-card-foreground shadow-sm'>
      <p className='text-muted-foreground'>
        Project summary view for{' '}
        <span className='font-medium'>{projectId}</span> will appear here.
      </p>
    </div>
  );
}
