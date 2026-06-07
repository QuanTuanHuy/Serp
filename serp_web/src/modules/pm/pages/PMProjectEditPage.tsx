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
import {
  useGetPmProjectByIdQuery,
  useUpdatePmProjectMutation,
  useArchivePmProjectMutation,
  useUnarchivePmProjectMutation,
} from '../api/projectApi';
import type { PMProjectDetail } from '../types/project-detail.types';
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

  const {
    data: projectResponse,
    isLoading: isProjectLoading,
    error: projectError,
  } = useGetPmProjectByIdQuery(projectId);

  const [updatePmProject] = useUpdatePmProjectMutation();
  const [archivePmProject] = useArchivePmProjectMutation();
  const [unarchivePmProject] = useUnarchivePmProjectMutation();

  const mappedProject = useMemo(() => {
    if (!projectResponse) {
      return null;
    }

    const mapped: PMProjectDetail & { categoryName?: string } = {
      id: String(projectResponse.id),
      name: projectResponse.name,
      key: projectResponse.key,
      description: projectResponse.description || '',
      category: projectResponse.category
        ? String(projectResponse.category.id)
        : 'ALL',
      categoryName: projectResponse.category?.name || 'Uncategorized',
      status: projectResponse.isArchived ? 'ARCHIVED' : 'ACTIVE',
      lead: {
        id: String(projectResponse.leadUserId),
        name:
          projectResponse.leadUserName || `User #${projectResponse.leadUserId}`,
      },
      visibility: 'ORGANIZATION',
      startDate: new Date(projectResponse.createdAt || Date.now())
        .toISOString()
        .split('T')[0],
      targetDate: new Date(projectResponse.updatedAt || Date.now())
        .toISOString()
        .split('T')[0],
      updatedAt: projectResponse.updatedAt || new Date().toISOString(),
      createdAt: projectResponse.createdAt || new Date().toISOString(),
    };
    return mapped;
  }, [projectResponse]);

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
    try {
      await updatePmProject({
        id: projectId,
        body: {
          name: values.name,
          key: values.key,
          description: values.description,
          leadUserId: Number(values.leadId),
          categoryId:
            values.category === 'ALL' ? undefined : Number(values.category),
        },
      }).unwrap();

      const isCurrentlyArchived = projectResponse?.isArchived || false;
      const targetArchivedState = values.status === 'ARCHIVED';

      if (targetArchivedState && !isCurrentlyArchived) {
        await archivePmProject(projectId).unwrap();
      } else if (!targetArchivedState && isCurrentlyArchived) {
        await unarchivePmProject(projectId).unwrap();
      }

      toast.success(`Saved changes for ${values.name}.`);
      router.push(`/pm/projects/${projectId}/summary`);
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to save changes.');
    }
  };

  if (isProjectLoading) {
    return (
      <Card className='border border-border/70 bg-card/60 shadow-sm'>
        <CardContent className='py-16 text-center text-muted-foreground'>
          Loading project details...
        </CardContent>
      </Card>
    );
  }

  if (projectError || !mappedProject) {
    return (
      <Card className='border border-border/70 bg-card/60 shadow-sm'>
        <CardHeader>
          <CardTitle>Project not found</CardTitle>
        </CardHeader>
        <CardContent className='space-y-4'>
          <p className='text-sm text-muted-foreground'>
            The project <span className='font-medium'>{projectId}</span> is not
            available or failed to load.
          </p>
          <Button
            type='button'
            onClick={() => router.push('/pm/projects')}
            className='rounded-xl'
          >
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
                  {mappedProject.name}
                </span>
                .
              </p>
            </div>
          </div>
        </div>
      </div>

      <PMProjectEditForm
        project={mappedProject}
        onSubmit={handleSave}
        onCancel={() =>
          navigateWithDirtyCheck(`/pm/projects/${projectId}/summary`)
        }
        onDirtyChange={setIsDirty}
      />
    </div>
  );
}
