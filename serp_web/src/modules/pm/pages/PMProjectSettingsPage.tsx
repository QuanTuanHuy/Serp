/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project settings page
 */

'use client';

import { useEffect, useMemo, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import {
  Archive,
  ArrowLeft,
  FolderKanban,
  PenLine,
  RotateCcw,
  Save,
  ShieldCheck,
  Users,
} from 'lucide-react';
import { toast } from 'sonner';

import { getErrorMessage } from '@/lib/store/api';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Checkbox,
  Skeleton,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui';

import {
  useArchivePmProjectMutation,
  useGetPmProjectPermissionsQuery,
  useGetPmProjectRolesQuery,
  useGetPmProjectSettingsOverviewQuery,
  useReplacePmProjectPermissionGrantsMutation,
  useUnarchivePmProjectMutation,
} from '../api';
import type {
  PMProjectPermissionDefinitionApi,
  PMProjectPermissionGrantApi,
} from '../types/api';

interface PMProjectSettingsPageProps {
  projectId: string;
}

export function PMProjectSettingsPage({
  projectId,
}: PMProjectSettingsPageProps) {
  const router = useRouter();
  const numericProjectId = Number(projectId);
  const canLoadProjectScopedData = Number.isFinite(numericProjectId);
  const [permissionGrantDraft, setPermissionGrantDraft] = useState<
    PMProjectPermissionGrantApi[]
  >([]);
  const {
    data: overview,
    isLoading: isOverviewLoading,
    error: overviewError,
  } = useGetPmProjectSettingsOverviewQuery(projectId);
  const permissionsQuery = useGetPmProjectPermissionsQuery(numericProjectId, {
    skip: !canLoadProjectScopedData,
  });
  const rolesQuery = useGetPmProjectRolesQuery({
    page: 0,
    pageSize: 100,
  });
  const [archivePmProject, archiveState] = useArchivePmProjectMutation();
  const [unarchivePmProject, unarchiveState] = useUnarchivePmProjectMutation();
  const [replacePermissionGrants, replacePermissionGrantsState] =
    useReplacePmProjectPermissionGrantsMutation();

  useEffect(() => {
    setPermissionGrantDraft(permissionsQuery.data?.grants ?? []);
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
    granteeType: 'PROJECT_LEAD' | 'PROJECT_ROLE',
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

      <ProjectPermissionSection
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

interface ProjectPermissionSectionProps {
  permissions: PMProjectPermissionDefinitionApi[];
  grants: PMProjectPermissionGrantApi[];
  roles: Array<{ id: number; name: string; isSystem: boolean }>;
  schemeName?: string;
  isLoading: boolean;
  errorMessage?: string;
  isSaving: boolean;
  isDirty: boolean;
  onToggleGrant: (
    permissionKey: string,
    granteeType: 'PROJECT_LEAD' | 'PROJECT_ROLE',
    granteeRef?: string | null
  ) => void;
  onReset: () => void;
  onSave: () => void;
}

function ProjectPermissionSection({
  permissions,
  grants,
  roles,
  schemeName,
  isLoading,
  errorMessage,
  isSaving,
  isDirty,
  onToggleGrant,
  onReset,
  onSave,
}: ProjectPermissionSectionProps) {
  const permissionGroups = useMemo(
    () => groupPermissionsByCategory(permissions),
    [permissions]
  );
  const unmanagedGrants = useMemo(
    () =>
      grants.filter(
        (grant) =>
          grant.granteeType !== 'PROJECT_LEAD' &&
          grant.granteeType !== 'PROJECT_ROLE'
      ),
    [grants]
  );

  return (
    <Card className='shadow-sm'>
      <CardHeader className='gap-3 border-b md:flex-row md:items-center md:justify-between'>
        <div className='min-w-0'>
          <CardTitle className='flex items-center gap-2 text-base'>
            <ShieldCheck className='h-4 w-4' />
            Project permissions
          </CardTitle>
          <p className='mt-1 truncate text-sm text-muted-foreground'>
            {schemeName || 'Permission scheme'}
          </p>
        </div>
        <div className='flex flex-wrap gap-2'>
          <Button
            type='button'
            variant='outline'
            size='sm'
            onClick={onReset}
            disabled={!isDirty || isSaving}
          >
            <RotateCcw className='mr-2 h-4 w-4' />
            Reset
          </Button>
          <Button
            type='button'
            size='sm'
            onClick={onSave}
            disabled={!isDirty || isSaving}
          >
            <Save className='mr-2 h-4 w-4' />
            Save
          </Button>
        </div>
      </CardHeader>
      <CardContent className='p-0'>
        {isLoading ? (
          <div className='space-y-3 p-4'>
            {Array.from({ length: 5 }).map((_, index) => (
              <Skeleton key={index} className='h-12 w-full' />
            ))}
          </div>
        ) : errorMessage ? (
          <div className='p-6 text-sm text-muted-foreground'>
            Project permissions unavailable: {errorMessage}
          </div>
        ) : (
          <div className='overflow-x-auto'>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className='min-w-72'>Permission</TableHead>
                  <TableHead className='w-32 text-center'>
                    Project lead
                  </TableHead>
                  {roles.map((role) => (
                    <TableHead key={role.id} className='w-36 text-center'>
                      <span className='line-clamp-2'>{role.name}</span>
                    </TableHead>
                  ))}
                </TableRow>
              </TableHeader>
              <TableBody>
                {permissionGroups.map((group) => (
                  <PermissionGroupRows
                    key={group.category}
                    category={group.category}
                    permissions={group.permissions}
                    grants={grants}
                    roles={roles}
                    onToggleGrant={onToggleGrant}
                  />
                ))}
                {permissionGroups.length === 0 ? (
                  <TableRow>
                    <TableCell
                      colSpan={roles.length + 2}
                      className='h-24 text-center text-sm text-muted-foreground'
                    >
                      No permission definitions found.
                    </TableCell>
                  </TableRow>
                ) : null}
              </TableBody>
            </Table>
          </div>
        )}
        {unmanagedGrants.length > 0 ? (
          <div className='border-t p-4'>
            <p className='text-xs font-semibold uppercase text-muted-foreground'>
              Other grants
            </p>
            <div className='mt-2 flex flex-wrap gap-2'>
              {unmanagedGrants.map((grant) => (
                <Badge key={grantKey(grant)} variant='outline'>
                  {grant.permissionKey}: {grant.granteeType}
                  {grant.granteeRef ? ` ${grant.granteeRef}` : ''}
                </Badge>
              ))}
            </div>
          </div>
        ) : null}
      </CardContent>
    </Card>
  );
}

function PermissionGroupRows({
  category,
  permissions,
  grants,
  roles,
  onToggleGrant,
}: {
  category: string;
  permissions: PMProjectPermissionDefinitionApi[];
  grants: PMProjectPermissionGrantApi[];
  roles: Array<{ id: number; name: string; isSystem: boolean }>;
  onToggleGrant: ProjectPermissionSectionProps['onToggleGrant'];
}) {
  return (
    <>
      <TableRow className='bg-muted/50 hover:bg-muted/50'>
        <TableCell
          colSpan={roles.length + 2}
          className='py-2 text-xs font-semibold uppercase text-muted-foreground'
        >
          {formatPermissionCategory(category)}
        </TableCell>
      </TableRow>
      {permissions.map((permission) => (
        <TableRow key={permission.permissionKey}>
          <TableCell>
            <div className='space-y-1'>
              <p className='font-medium'>{permission.name}</p>
              <p className='text-xs text-muted-foreground'>
                {permission.description || permission.permissionKey}
              </p>
            </div>
          </TableCell>
          <TableCell className='text-center'>
            <Checkbox
              checked={grants.some((grant) =>
                isGrantMatch(
                  grant,
                  permission.permissionKey,
                  'PROJECT_LEAD',
                  null
                )
              )}
              onCheckedChange={() =>
                onToggleGrant(permission.permissionKey, 'PROJECT_LEAD', null)
              }
              aria-label={`${permission.name} for project lead`}
            />
          </TableCell>
          {roles.map((role) => (
            <TableCell key={role.id} className='text-center'>
              <Checkbox
                checked={grants.some((grant) =>
                  isGrantMatch(
                    grant,
                    permission.permissionKey,
                    'PROJECT_ROLE',
                    role.name
                  )
                )}
                onCheckedChange={() =>
                  onToggleGrant(
                    permission.permissionKey,
                    'PROJECT_ROLE',
                    role.name
                  )
                }
                aria-label={`${permission.name} for ${role.name}`}
              />
            </TableCell>
          ))}
        </TableRow>
      ))}
    </>
  );
}

function groupPermissionsByCategory(
  permissions: PMProjectPermissionDefinitionApi[]
) {
  const groups = new Map<string, PMProjectPermissionDefinitionApi[]>();
  for (const permission of permissions) {
    const category = permission.category || 'OTHER';
    groups.set(category, [...(groups.get(category) ?? []), permission]);
  }
  return Array.from(groups.entries()).map(([category, items]) => ({
    category,
    permissions: items,
  }));
}

function isGrantMatch(
  grant: PMProjectPermissionGrantApi,
  permissionKey: string,
  granteeType: string,
  granteeRef?: string | null
) {
  return (
    grant.permissionKey === permissionKey &&
    grant.granteeType === granteeType &&
    normalizeGrantRef(grant.granteeRef) === normalizeGrantRef(granteeRef)
  );
}

function buildGrantStateKey(grants: PMProjectPermissionGrantApi[]) {
  return grants.map(grantKey).sort().join('|');
}

function grantKey(grant: PMProjectPermissionGrantApi) {
  return [
    grant.permissionKey,
    grant.granteeType,
    normalizeGrantRef(grant.granteeRef) ?? '',
    grant.customFieldId ?? '',
  ].join(':');
}

function normalizeGrantRef(value?: string | null) {
  const normalized = value?.trim();
  return normalized ? normalized.toLowerCase() : null;
}

function formatPermissionCategory(category: string) {
  return category.toLowerCase().replaceAll('_', ' ');
}
