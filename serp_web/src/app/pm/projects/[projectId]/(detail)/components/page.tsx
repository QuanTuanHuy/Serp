/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project components page
 */

interface PMProjectComponentsPageProps {
  params: Promise<{ projectId: string }>;
}

export default async function PMProjectComponentsPage({
  params,
}: PMProjectComponentsPageProps) {
  const { projectId } = await params;

  return (
    <div className='rounded-lg border bg-card p-8 text-card-foreground shadow-sm'>
      <p className='text-muted-foreground'>
        Project components view for{' '}
        <span className='font-medium'>{projectId}</span> will appear here.
      </p>
    </div>
  );
}
