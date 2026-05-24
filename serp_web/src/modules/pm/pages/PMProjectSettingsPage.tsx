/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project settings page
 */

'use client';

import { useMemo } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { Archive, ArrowLeft, FolderKanban, PenLine, Users } from 'lucide-react';
import { toast } from 'sonner';
import { getErrorMessage } from '@/lib/store/api';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';
import {
  useArchivePmProjectMutation,
  useGetPmProjectByIdQuery,
  useGetPmProjectComponentsQuery,
  useUnarchivePmProjectMutation,
} from '../api';

interface PMProjectSettingsPageProps {
  projectId: string;
}

export function PMProjectSettingsPage({
  projectId,
}: PMProjectSettingsPageProps) {
  const router = useRouter();
  const numericProjectId = Number(projectId);
  const {
    data: projectResponse,
    isLoading: isProjectLoading,
    error: projectError,
  } = useGetPmProjectByIdQuery(projectId);
  const { data: componentsResponse } = useGetPmProjectComponentsQuery(
    {
      projectId: numericProjectId,
      params: {
        page: 0,
        pageSize: 5,
        sortBy: 'name',
        sortDirection: 'asc',
      },
    },
    { skip: !Number.isFinite(numericProjectId) }
  );
  const [archivePmProject, archiveState] = useArchivePmProjectMutation();
  const [unarchivePmProject, unarchiveState] = useUnarchivePmProjectMutation();

  const schemeItems = useMemo(
    () => [
      { label: 'Issue type scheme', value: projectResponse?.issueTypeSchemeId },
      { label: 'Workflow scheme', value: projectResponse?.workflowSchemeId },
      {
        label: 'Field config scheme',
        value: projectResponse?.fieldConfigSchemeId,
      },
      {
        label: 'Screen scheme',
        value: projectResponse?.issueTypeScreenSchemeId,
      },
      {
        label: 'Permission scheme',
        value: projectResponse?.permissionSchemeId,
      },
      {
        label: 'Notification scheme',
        value: projectResponse?.notificationSchemeId,
      },
      { label: 'Priority scheme', value: projectResponse?.prioritySchemeId },
      {
        label: 'Security scheme',
        value: projectResponse?.issueSecuritySchemeId,
      },
    ],
    [projectResponse]
  );

  const handleArchiveToggle = async () => {
    if (!projectResponse) {
      return;
    }

    try {
      if (projectResponse.isArchived) {
        await unarchivePmProject(projectId).unwrap();
        toast.success('Project unarchived.');
      } else {
        await archivePmProject(projectId).unwrap();
        toast.success('Project archived.');
      }
    } catch (error) {
      toast.error('Unable to update project state', {
        description: getErrorMessage(error),
      });
    }
  };

  if (isProjectLoading) {
    return (
      <div className='rounded-lg border bg-card p-6 text-sm text-muted-foreground shadow-sm'>
        Loading project...
      </div>
    );
  }

  if (projectError || !projectResponse) {
    return (
      <Card className='shadow-sm'>
        <CardContent className='p-6 text-sm text-muted-foreground'>
          Project unavailable.
        </CardContent>
      </Card>
    );
  }

  const components = componentsResponse?.data.items ?? [];

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between'>
        <div className='space-y-3'>
          <Button
            type='button'
            variant='ghost'
            className='w-fit px-0 text-muted-foreground hover:bg-transparent'
            onClick={() => router.push(`/pm/projects/${projectId}/summary`)}
          >
            <ArrowLeft className='mr-2 h-4 w-4' />
            Back to project
          </Button>

          <div className='flex items-center gap-3'>
            <div className='flex h-11 w-11 items-center justify-center rounded-2xl bg-primary/10 text-primary'>
              <PenLine className='h-5 w-5' />
            </div>
            <div>
              <h1 className='text-3xl font-bold tracking-tight'>
                Project settings
              </h1>
              <p className='text-sm text-muted-foreground'>
                {projectResponse.name} · {projectResponse.key}
              </p>
            </div>
          </div>
        </div>

        <div className='flex flex-wrap gap-2'>
          <Button
            type='button'
            variant='outline'
            onClick={() => router.push(`/pm/projects/${projectId}/edit`)}
          >
            <PenLine className='mr-2 h-4 w-4' />
            Edit project
          </Button>
          <Button
            type='button'
            variant={projectResponse.isArchived ? 'secondary' : 'outline'}
            onClick={handleArchiveToggle}
            disabled={archiveState.isLoading || unarchiveState.isLoading}
          >
            <Archive className='mr-2 h-4 w-4' />
            {projectResponse.isArchived ? 'Unarchive' : 'Archive'}
          </Button>
        </div>
      </div>

      <div className='grid gap-5 xl:grid-cols-[minmax(0,1.2fr)_minmax(0,0.8fr)]'>
        <Card className='shadow-sm'>
          <CardHeader className='border-b'>
            <CardTitle className='text-base'>Details</CardTitle>
          </CardHeader>
          <CardContent className='grid gap-3 p-4 md:grid-cols-2'>
            <SettingField label='Name' value={projectResponse.name} />
            <SettingField label='Key' value={projectResponse.key} />
            <SettingField
              label='Lead'
              value={
                projectResponse.leadUserName ||
                (projectResponse.leadUserId
                  ? `User #${projectResponse.leadUserId}`
                  : '-')
              }
            />
            <SettingField
              label='Category'
              value={projectResponse.category?.name || '-'}
            />
            <SettingField
              label='Project type'
              value={projectResponse.projectTypeKey}
            />
            <SettingField
              label='State'
              value={projectResponse.isArchived ? 'Archived' : 'Active'}
            />
            <div className='md:col-span-2'>
              <SettingField
                label='Description'
                value={projectResponse.description || '-'}
              />
            </div>
          </CardContent>
        </Card>

        <Card className='shadow-sm'>
          <CardHeader className='border-b'>
            <CardTitle className='text-base'>People & roles</CardTitle>
          </CardHeader>
          <CardContent className='space-y-3 p-4'>
            <div className='flex items-center justify-between rounded-md border px-3 py-2'>
              <span className='text-sm text-muted-foreground'>Lead</span>
              <span className='text-sm font-medium'>
                {projectResponse.leadUserName ||
                  (projectResponse.leadUserId
                    ? `User #${projectResponse.leadUserId}`
                    : '-')}
              </span>
            </div>
            <Button
              type='button'
              variant='outline'
              className='w-full justify-start'
              onClick={() => router.push(`/pm/projects/${projectId}/edit`)}
            >
              <Users className='mr-2 h-4 w-4' />
              Edit ownership
            </Button>
          </CardContent>
        </Card>
      </div>

      <div className='grid gap-5 xl:grid-cols-[minmax(0,1fr)_320px]'>
        <Card className='shadow-sm'>
          <CardHeader className='border-b'>
            <CardTitle className='text-base'>Components</CardTitle>
          </CardHeader>
          <CardContent className='p-0'>
            <div className='flex items-center justify-between border-b px-4 py-3 text-sm text-muted-foreground'>
              <span>{components.length} shown</span>
              <Button
                type='button'
                variant='outline'
                size='sm'
                onClick={() =>
                  router.push(`/pm/projects/${projectId}/components`)
                }
              >
                <FolderKanban className='mr-2 h-4 w-4' />
                Open components
              </Button>
            </div>
            <div className='overflow-hidden'>
              {components.length > 0 ? (
                <div className='divide-y'>
                  {components.map((component) => (
                    <div
                      key={component.id}
                      className='grid gap-3 px-4 py-3 md:grid-cols-[minmax(0,1fr)_140px_140px]'
                    >
                      <div className='min-w-0'>
                        <p className='font-medium'>{component.name}</p>
                        <p className='truncate text-sm text-muted-foreground'>
                          {component.description || '-'}
                        </p>
                      </div>
                      <div className='text-sm text-muted-foreground'>
                        {component.issueCount} issues
                      </div>
                      <div className='text-sm text-muted-foreground'>
                        {component.assigneeType}
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className='px-4 py-6 text-sm text-muted-foreground'>
                  No components.
                </div>
              )}
            </div>
          </CardContent>
        </Card>

        <Card className='shadow-sm'>
          <CardHeader className='border-b'>
            <CardTitle className='text-base'>Bound schemes</CardTitle>
          </CardHeader>
          <CardContent className='space-y-2 p-4'>
            {schemeItems.map((item) => (
              <div
                key={item.label}
                className='flex items-center justify-between rounded-md border px-3 py-2'
              >
                <span className='text-sm text-muted-foreground'>
                  {item.label}
                </span>
                <Badge variant='secondary'>
                  {typeof item.value === 'number' ? `#${item.value}` : '-'}
                </Badge>
              </div>
            ))}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

function SettingField({ label, value }: { label: string; value: string }) {
  return (
    <div className='rounded-md border px-3 py-2'>
      <p className='text-xs font-medium uppercase tracking-wide text-muted-foreground'>
        {label}
      </p>
      <p className='mt-1 text-sm font-medium'>{value}</p>
    </div>
  );
}
