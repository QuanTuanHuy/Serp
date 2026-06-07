/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project settings page
 */

'use client';

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
  useGetPmProjectSettingsOverviewQuery,
  useUnarchivePmProjectMutation,
} from '../api';

interface PMProjectSettingsPageProps {
  projectId: string;
}

export function PMProjectSettingsPage({
  projectId,
}: PMProjectSettingsPageProps) {
  const router = useRouter();
  const {
    data: overview,
    isLoading: isOverviewLoading,
    error: overviewError,
  } = useGetPmProjectSettingsOverviewQuery(projectId);
  const [archivePmProject, archiveState] = useArchivePmProjectMutation();
  const [unarchivePmProject, unarchiveState] = useUnarchivePmProjectMutation();

  const handleArchiveToggle = async () => {
    if (!overview?.project) {
      return;
    }

    try {
      if (overview.project.isArchived) {
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

  if (isOverviewLoading) {
    return (
      <div className='rounded-lg border bg-card p-6 text-sm text-muted-foreground shadow-sm'>
        Loading project settings...
      </div>
    );
  }

  if (overviewError || !overview) {
    return (
      <Card className='shadow-sm'>
        <CardContent className='space-y-4 p-6'>
          <p className='text-sm text-muted-foreground'>
            Project settings unavailable.
          </p>
          <Button
            type='button'
            variant='outline'
            onClick={() => router.push('/pm/projects')}
          >
            <ArrowLeft className='mr-2 h-4 w-4' />
            Back to projects
          </Button>
        </CardContent>
      </Card>
    );
  }

  const { project, people, components, schemes } = overview;
  const componentPreview = components.preview;

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
            <div className='flex h-11 w-11 items-center justify-center rounded-md bg-primary/10 text-primary'>
              <PenLine className='h-5 w-5' />
            </div>
            <div>
              <h1 className='text-3xl font-bold tracking-tight'>
                Project settings
              </h1>
              <p className='text-sm text-muted-foreground'>
                {project.name} - {project.key}
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
            variant={project.isArchived ? 'secondary' : 'outline'}
            onClick={handleArchiveToggle}
            disabled={archiveState.isLoading || unarchiveState.isLoading}
          >
            <Archive className='mr-2 h-4 w-4' />
            {project.isArchived ? 'Unarchive' : 'Archive'}
          </Button>
        </div>
      </div>

      <div className='grid gap-5 xl:grid-cols-[minmax(0,1.2fr)_minmax(0,0.8fr)]'>
        <Card className='shadow-sm'>
          <CardHeader className='border-b'>
            <CardTitle className='text-base'>Project profile</CardTitle>
          </CardHeader>
          <CardContent className='grid gap-3 p-4 md:grid-cols-2'>
            <SettingField label='Name' value={project.name} />
            <SettingField label='Key' value={project.key} />
            <SettingField
              label='Lead'
              value={project.leadUserName || 'Unassigned'}
            />
            <SettingField
              label='Category'
              value={project.category?.name || 'No category'}
            />
            <SettingField label='Project type' value={project.projectTypeKey} />
            <SettingField
              label='State'
              value={project.isArchived ? 'Archived' : 'Active'}
            />
            <div className='md:col-span-2'>
              <SettingField
                label='Description'
                value={project.description || '-'}
              />
            </div>
          </CardContent>
        </Card>

        <Card className='shadow-sm'>
          <CardHeader className='border-b'>
            <CardTitle className='text-base'>People & roles</CardTitle>
          </CardHeader>
          <CardContent className='space-y-3 p-4'>
            <SettingField
              label='Project lead'
              value={people.leadUserName || 'Unassigned'}
            />
            <div className='grid gap-3 sm:grid-cols-2'>
              <SettingField
                label='Members'
                value={String(people.memberCount)}
              />
              <SettingField
                label='Roles used'
                value={String(people.roleCount)}
              />
            </div>
            <Button
              type='button'
              variant='outline'
              className='w-full justify-start'
              onClick={() => router.push(`/pm/projects/${projectId}/people`)}
            >
              <Users className='mr-2 h-4 w-4' />
              Open people
            </Button>
          </CardContent>
        </Card>
      </div>

      <div className='grid gap-5 xl:grid-cols-[minmax(0,1fr)_360px]'>
        <Card className='shadow-sm'>
          <CardHeader className='border-b'>
            <CardTitle className='text-base'>Components</CardTitle>
          </CardHeader>
          <CardContent className='p-0'>
            <div className='flex items-center justify-between border-b px-4 py-3 text-sm text-muted-foreground'>
              <span>{components.totalCount} total</span>
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
            {componentPreview.length > 0 ? (
              <div className='divide-y'>
                {componentPreview.map((component) => (
                  <div
                    key={component.id}
                    className='grid gap-3 px-4 py-3 md:grid-cols-[minmax(0,1fr)_120px_160px]'
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
          </CardContent>
        </Card>

        <Card className='shadow-sm'>
          <CardHeader className='border-b'>
            <CardTitle className='text-base'>Configuration schemes</CardTitle>
          </CardHeader>
          <CardContent className='space-y-2 p-4'>
            {schemes.map((scheme) => (
              <div key={scheme.type} className='rounded-md border p-3'>
                <div className='flex items-start justify-between gap-3'>
                  <div className='min-w-0'>
                    <p className='text-sm font-medium'>{scheme.label}</p>
                    <p className='truncate text-sm text-muted-foreground'>
                      {scheme.schemeName ||
                        (typeof scheme.schemeId === 'number'
                          ? `#${scheme.schemeId}`
                          : '-')}
                    </p>
                  </div>
                  {scheme.globalSection ? (
                    <Button asChild variant='ghost' size='sm'>
                      <Link
                        href={`/pm/settings?section=${scheme.globalSection}`}
                      >
                        Open
                      </Link>
                    </Button>
                  ) : (
                    <Badge variant='secondary'>Read-only</Badge>
                  )}
                </div>
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
