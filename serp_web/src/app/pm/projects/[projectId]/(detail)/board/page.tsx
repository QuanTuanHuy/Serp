/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project board page
 */

interface PMProjectBoardPageProps {
  params: Promise<{ projectId: string }>;
}

export default async function PMProjectBoardPage({
  params,
}: PMProjectBoardPageProps) {
  const { projectId } = await params;

  return (
    <div className='rounded-lg border bg-card p-8 text-card-foreground shadow-sm'>
      <p className='text-muted-foreground'>
        Project board view for <span className='font-medium'>{projectId}</span>{' '}
        will appear here.
      </p>
    </div>
  );
}
