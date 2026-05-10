/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project timeline page
 */

interface PMProjectTimelinePageProps {
  params: Promise<{ projectId: string }>;
}

export default async function PMProjectTimelinePage({
  params,
}: PMProjectTimelinePageProps) {
  const { projectId } = await params;

  return (
    <div className='rounded-lg border bg-card p-8 text-card-foreground shadow-sm'>
      <p className='text-muted-foreground'>
        Project timeline view for{' '}
        <span className='font-medium'>{projectId}</span> will appear here.
      </p>
    </div>
  );
}
