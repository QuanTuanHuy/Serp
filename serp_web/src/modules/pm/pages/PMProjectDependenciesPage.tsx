/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project dependencies page
 */

'use client';

import { Card, CardContent } from '@/shared/components/ui';

interface PMProjectDependenciesPageProps {
  projectId: string;
}

export function PMProjectDependenciesPage({
  projectId,
}: PMProjectDependenciesPageProps) {
  const numericProjectId = Number(projectId);

  if (!Number.isFinite(numericProjectId)) {
    return (
      <Card className='shadow-sm'>
        <CardContent className='p-6 text-sm text-muted-foreground'>
          Invalid project id.
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className='shadow-sm'>
      <CardContent className='p-6 text-sm text-muted-foreground'>
        Dependencies view for project {projectId}.
      </CardContent>
    </Card>
  );
}
