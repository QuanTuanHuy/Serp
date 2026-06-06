/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project settings page
 */

'use client';

import { useEffect, useMemo, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { Archive, ArrowLeft, PenLine } from 'lucide-react';
import { toast } from 'sonner';

import { getErrorMessage } from '@/lib/store/api';
import { Button, Card, CardContent } from '@/shared/components/ui';

import {
  useArchivePmProjectMutation,
  useGetPmProjectPermissionsQuery,
  useGetPmProjectRolesQuery,
  useGetPmProjectSettingsOverviewQuery,
  useReplacePmProjectPermissionGrantsMutation,
  useUnarchivePmProjectMutation,
} from '../api';
import { PMProjectSettingsComponentsSection } from '../components/projects/settings/PMProjectSettingsComponentsSection';
import { PMProjectSettingsGeneralSection } from '../components/projects/settings/PMProjectSettingsGeneralSection';
import { PMProjectSettingsPeopleSection } from '../components/projects/settings/PMProjectSettingsPeopleSection';
import { PMProjectSettingsSchemesSection } from '../components/projects/settings/PMProjectSettingsSchemesSection';
import {
  DEFAULT_PROJECT_SETTINGS_SECTION,
  PMProjectSettingsSidebar,
  PROJECT_SETTINGS_SECTION_KEYS,
  type PMProjectSettingsSectionKey,
} from '../components/projects/settings/PMProjectSettingsSidebar';
import {
  buildGrantStateKey,
  isGrantMatch,
  PMProjectPermissionsSection,
  type PMEditablePermissionGranteeType,
} from '../components/projects/settings/PMProjectPermissionsSection';
import type { PMProjectPermissionGrantApi } from '../types/api';

interface PMProjectSettingsPageProps {
  projectId: string;
}

export function PMProjectSettingsPage({
  projectId,
}: PMProjectSettingsPageProps) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const numericProjectId = Number(projectId);
  const canLoadProjectScopedData = Number.isFinite(numericProjectId);
  const activeSection = normalizeSettingsSection(searchParams.get('section'));
  const canLoadPermissions =
    canLoadProjectScopedData && activeSection === 'permissions';
  const [permissionGrantDraft, setPermissionGrantDraft] = useState<
    PMProjectPermissionGrantApi[]
  >([]);
  const {
    data: overview,
    isLoading: isOverviewLoading,
    error: overviewError,
  } = useGetPmProjectSettingsOverviewQuery(projectId);
  const permissionsQuery = useGetPmProjectPermissionsQuery(numericProjectId, {
    skip: !canLoadPermissions,
  });
  const rolesQuery = useGetPmProjectRolesQuery(
    {
      page: 0,
      pageSize: 100,
    },
    {
      skip: !canLoadPermissions,
    }
  );
  const [archivePmProject, archiveState] = useArchivePmProjectMutation();
  const [unarchivePmProject, unarchiveState] = useUnarchivePmProjectMutation();
  const [replacePermissionGrants, replacePermissionGrantsState] =
    useReplacePmProjectPermissionGrantsMutation();

  useEffect(() => {
    if (permissionsQuery.data?.grants) {
      setPermissionGrantDraft(permissionsQuery.data.grants);
    }
  }, [permissionsQuery.data?.grants]);

  const permissionDraftKey = useMemo(
    () => buildGrantStateKey(permissionGrantDraft),
    [permissionGrantDraft]
  );
  const permissionServerKey = useMemo(
    () => buildGrantStateKey(permissionsQuery.data?.grants ?? []),
    [permissionsQuery.data?.grants]
  );
  const hasPermissionChanges = permissionDraftKey !== permissionServerKey;

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

  const handleTogglePermissionGrant = (
    permissionKey: string,
    granteeType: PMEditablePermissionGranteeType,
    granteeRef?: string | null
  ) => {
    setPermissionGrantDraft((current) => {
      const exists = current.some((grant) =>
        isGrantMatch(grant, permissionKey, granteeType, granteeRef)
      );

      if (exists) {
        return current.filter(
          (grant) =>
            !isGrantMatch(grant, permissionKey, granteeType, granteeRef)
        );
      }

      return [
        ...current,
        {
          permissionKey,
          granteeType,
          granteeRef: granteeRef ?? null,
          customFieldId: null,
        },
      ];
    });
  };

  const handleResetPermissionGrants = () => {
    setPermissionGrantDraft(permissionsQuery.data?.grants ?? []);
  };

  const handleSavePermissionGrants = async () => {
    try {
      await replacePermissionGrants({
        projectId: numericProjectId,
        body: {
          grants: permissionGrantDraft.map((grant) => ({
            permissionKey: grant.permissionKey,
            granteeType: grant.granteeType,
            granteeRef: grant.granteeRef ?? null,
            customFieldId: grant.customFieldId ?? null,
          })),
        },
      }).unwrap();
      toast.success('Project permissions updated.');
    } catch (error) {
      toast.error('Unable to update project permissions', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleSectionChange = (section: PMProjectSettingsSectionKey) => {
    const nextParams = new URLSearchParams(searchParams.toString());
    if (section === DEFAULT_PROJECT_SETTINGS_SECTION) {
      nextParams.delete('section');
    } else {
      nextParams.set('section', section);
    }

    const query = nextParams.toString();
    router.push(
      query
        ? `/pm/projects/${projectId}/settings?${query}`
        : `/pm/projects/${projectId}/settings`
    );
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

      <div className='grid gap-6 lg:grid-cols-[260px_minmax(0,1fr)]'>
        <PMProjectSettingsSidebar
          activeSection={activeSection}
          onSectionChange={handleSectionChange}
        />

        <main className='min-w-0 space-y-5'>
          {activeSection === 'general' ? (
            <PMProjectSettingsGeneralSection project={project} />
          ) : null}

          {activeSection === 'people' ? (
            <PMProjectSettingsPeopleSection
              people={people}
              onOpenPeople={() =>
                router.push(`/pm/projects/${projectId}/people`)
              }
            />
          ) : null}

          {activeSection === 'components' ? (
            <PMProjectSettingsComponentsSection
              components={components}
              onOpenComponents={() =>
                router.push(`/pm/projects/${projectId}/components`)
              }
            />
          ) : null}

          {activeSection === 'schemes' ? (
            <PMProjectSettingsSchemesSection schemes={schemes} />
          ) : null}

          {activeSection === 'permissions' ? (
            <PMProjectPermissionsSection
              permissions={permissionsQuery.data?.permissions ?? []}
              grants={permissionGrantDraft}
              roles={rolesQuery.data?.data.items ?? []}
              schemeName={permissionsQuery.data?.scheme.name}
              isLoading={permissionsQuery.isLoading || rolesQuery.isLoading}
              errorMessage={
                permissionsQuery.error
                  ? getErrorMessage(permissionsQuery.error)
                  : undefined
              }
              isSaving={replacePermissionGrantsState.isLoading}
              isDirty={hasPermissionChanges}
              onToggleGrant={handleTogglePermissionGrant}
              onReset={handleResetPermissionGrants}
              onSave={handleSavePermissionGrants}
            />
          ) : null}
        </main>
      </div>
    </div>
  );
}

function normalizeSettingsSection(
  value: string | null
): PMProjectSettingsSectionKey {
  if (
    value &&
    PROJECT_SETTINGS_SECTION_KEYS.includes(value as PMProjectSettingsSectionKey)
  ) {
    return value as PMProjectSettingsSectionKey;
  }

  return DEFAULT_PROJECT_SETTINGS_SECTION;
}
