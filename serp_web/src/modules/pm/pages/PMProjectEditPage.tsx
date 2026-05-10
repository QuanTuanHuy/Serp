/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project edit page
 */

'use client';

import { useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import { ArrowLeft, Settings2 } from 'lucide-react';
import { toast } from 'sonner';
import {
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';
import { getPMProjectDetailMockById } from '../mocks/projectDetail';
import {
  PMProjectEditForm,
  type PMProjectEditFormValues,
} from '../components/projects/PMProjectEditForm';

interface PMProjectEditPageProps {
  projectId: string;
}

export function PMProjectEditPage({ projectId }: PMProjectEditPageProps) {
  const router = useRouter();
  const [isDirty, setIsDirty] = useState(false);

  const project = useMemo(
    () => getPMProjectDetailMockById(projectId),
    [projectId]
  );

  const navigateWithDirtyCheck = (href: string) => {
    if (
      isDirty &&
      !window.confirm('You have unsaved changes. Leave this page?')
    ) {
      return;
    }

    router.push(href);
  };

  const handleSave = async (values: PMProjectEditFormValues) => {
    toast.success(`Saved changes for ${values.name}.`);
    router.push(`/pm/projects/${projectId}/summary`);
  };

  if (!project) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Project not found</CardTitle>
        </CardHeader>
        <CardContent className='space-y-4'>
          <p className='text-sm text-muted-foreground'>
            The project <span className='font-medium'>{projectId}</span> is not
            available in the current PM mock adapter.
          </p>
          <Button type='button' onClick={() => router.push('/pm/projects')}>
            Back to projects
          </Button>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between'>
        <div className='space-y-3'>
          <Button
            type='button'
            variant='ghost'
            className='w-fit px-0 text-muted-foreground hover:bg-transparent'
            onClick={() =>
              navigateWithDirtyCheck(`/pm/projects/${projectId}/summary`)
            }
          >
            <ArrowLeft className='mr-2 h-4 w-4' />
            Back to project
          </Button>

          <div className='flex items-center gap-3'>
            <div className='flex h-11 w-11 items-center justify-center rounded-2xl bg-primary/10 text-primary'>
              <Settings2 className='h-5 w-5' />
            </div>
            <div>
              <h1 className='text-3xl font-bold tracking-tight'>
                Edit project
              </h1>
              <p className='text-sm text-muted-foreground'>
                Update metadata, ownership, schedule, and guarded project-level
                settings for{' '}
                <span className='font-medium text-foreground'>
                  {project.name}
                </span>
                .
              </p>
            </div>
          </div>
        </div>
      </div>

      <PMProjectEditForm
        project={project}
        onSubmit={handleSave}
        onCancel={() =>
          navigateWithDirtyCheck(`/pm/projects/${projectId}/summary`)
        }
        onDirtyChange={setIsDirty}
      />
    </div>
  );
}
