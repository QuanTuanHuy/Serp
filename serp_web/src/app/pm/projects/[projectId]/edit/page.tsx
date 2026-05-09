/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project edit page
 */

interface PMProjectEditPageProps {
  params: Promise<{ projectId: string }>;
}

export default async function PMProjectEditPage({
  params,
}: PMProjectEditPageProps) {
  const { projectId } = await params;

  return (
    <div className='rounded-lg border bg-card p-8 text-card-foreground shadow-sm'>
      <div className='space-y-2'>
        <h2 className='text-xl font-semibold'>Edit project</h2>
        <p className='text-muted-foreground'>
          Project metadata editor for{' '}
          <span className='font-medium'>{projectId}</span> will be implemented
          here.
        </p>
      </div>
    </div>
  );
}
