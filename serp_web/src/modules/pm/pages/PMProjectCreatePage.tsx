/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project create page
 */

'use client';

import { useRouter } from 'next/navigation';
import { ArrowLeft, Sparkles } from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/shared/components/ui';
import {
  PMProjectCreateForm,
  type PMProjectCreateFormValues,
} from '../components/projects/PMProjectCreateForm';

function createProjectIdFromKey(projectKey: string) {
  const base = projectKey.toLowerCase().replace(/[^a-z0-9-]/g, '-');
  return `${base || 'pm-project'}-${Date.now().toString().slice(-6)}`;
}

export function PMProjectCreatePage() {
  const router = useRouter();

  const handleCreateProject = async (values: PMProjectCreateFormValues) => {
    const projectId = createProjectIdFromKey(values.key);

    toast.success(`Project ${values.name} is ready for setup.`);
    router.push(`/pm/projects/${projectId}/summary`);
  };

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between'>
        <div className='space-y-3'>
          <Button
            type='button'
            variant='ghost'
            className='w-fit px-0 text-muted-foreground hover:bg-transparent'
            onClick={() => router.push('/pm/projects')}
          >
            <ArrowLeft className='mr-2 h-4 w-4' />
            Back to projects
          </Button>

          <div className='flex items-center gap-3'>
            <div className='flex h-11 w-11 items-center justify-center rounded-2xl bg-primary/10 text-primary'>
              <Sparkles className='h-5 w-5' />
            </div>
            <div>
              <h1 className='text-3xl font-bold tracking-tight'>
                Create project
              </h1>
              <p className='text-sm text-muted-foreground'>
                Launch a company software workspace from Blank, Kanban, or Scrum
                defaults.
              </p>
            </div>
          </div>
        </div>
      </div>

      <PMProjectCreateForm
        onSubmit={handleCreateProject}
        onCancel={() => router.push('/pm/projects')}
      />
    </div>
  );
}
