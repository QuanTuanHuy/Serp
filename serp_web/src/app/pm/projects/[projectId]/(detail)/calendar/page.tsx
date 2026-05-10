/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project calendar page
 */

interface PMProjectCalendarPageProps {
  params: Promise<{ projectId: string }>;
}

export default async function PMProjectCalendarPage({
  params,
}: PMProjectCalendarPageProps) {
  const { projectId } = await params;

  return (
    <div className='rounded-lg border bg-card p-8 text-card-foreground shadow-sm'>
      <p className='text-muted-foreground'>
        Project calendar view for{' '}
        <span className='font-medium'>{projectId}</span> will appear here.
      </p>
    </div>
  );
}
