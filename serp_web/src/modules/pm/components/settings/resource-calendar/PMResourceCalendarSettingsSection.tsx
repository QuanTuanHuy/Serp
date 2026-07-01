/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM resource calendar settings section
 */

'use client';

import { useMemo, useState } from 'react';
import { CalendarClock, Plus, Users } from 'lucide-react';
import { toast } from 'sonner';

import { getErrorMessage } from '@/lib/store/api';
import { Button } from '@/shared/components/ui/button';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { ConfirmDialog } from '@/shared/components/ui/confirm-dialog';
import { Skeleton } from '@/shared/components/ui/skeleton';
import { selectOrganizationId } from '@/modules/account/store';
import { useGetOrganizationUsersQuery } from '@/modules/settings/services/users/usersApi';
import { useGetMyModulesQuery } from '@/modules/account/services/moduleApi';
import { useAppSelector } from '@/shared/hooks';
import type { UserProfile } from '@/modules/admin/types';

import {
  useCreatePmResourceCalendarExceptionMutation,
  useCreatePmResourceCalendarProfileMutation,
  useDeletePmResourceCalendarExceptionMutation,
  useDeletePmResourceCalendarProfileMutation,
  useReplacePmResourceCalendarAssignmentsMutation,
  useReplacePmResourceCalendarBlocksMutation,
  useUpdatePmResourceCalendarExceptionMutation,
  useUpdatePmResourceCalendarProfileMutation,
} from '../../../api';
import type {
  PMCreateResourceCalendarExceptionRequest,
  PMCreateResourceCalendarProfileRequest,
  PMReplaceResourceCalendarAssignmentsRequest,
  PMReplaceResourceCalendarBlocksRequest,
  PMResourceCalendarExceptionApi,
  PMResourceCalendarProfileApi,
  PMResourceCalendarSettingsOverviewApi,
} from '../../../types/api';
import { MiniStat } from '../settings-tables';
import { PMResourceCalendarAssignmentPanel } from './PMResourceCalendarAssignmentPanel';
import { PMResourceCalendarExceptionPanel } from './PMResourceCalendarExceptionPanel';
import {
  PMResourceCalendarProfileDialog,
  type ResourceCalendarProfileDialogState,
} from './PMResourceCalendarProfileDialog';
import { PMResourceCalendarProfileTable } from './PMResourceCalendarProfileTable';

type DeleteTarget =
  | { kind: 'profile'; item: PMResourceCalendarProfileApi }
  | { kind: 'exception'; item: PMResourceCalendarExceptionApi };

export function PMResourceCalendarSettingsSection({
  overview,
  isLoading,
  errorMessage,
}: {
  overview: PMResourceCalendarSettingsOverviewApi | undefined;
  isLoading: boolean;
  errorMessage?: string;
}) {
  const [profileDialog, setProfileDialog] =
    useState<ResourceCalendarProfileDialogState | null>(null);
  const [selectedProfileId, setSelectedProfileId] = useState<number | null>(
    null
  );
  const [deleteTarget, setDeleteTarget] = useState<DeleteTarget | null>(null);

  const organizationId = useAppSelector(selectOrganizationId);
  const { data: myModules } = useGetMyModulesQuery(undefined, {
    skip: !organizationId,
  });
  const pmModuleId = myModules?.find((m) => m.moduleCode === 'PM')?.moduleId;

  const usersQuery = useGetOrganizationUsersQuery(
    {
      organizationId: organizationId as number,
      page: 0,
      pageSize: 100,
      status: 'ACTIVE',
      moduleId: pmModuleId,
    },
    {
      skip: !organizationId,
    }
  );

  const userMap = useMemo(() => {
    const map = new Map<number, UserProfile>();
    (usersQuery.data?.data.items ?? []).forEach((user) => {
      map.set(user.id, user);
    });
    return map;
  }, [usersQuery.data]);

  const [createProfile, createProfileState] =
    useCreatePmResourceCalendarProfileMutation();
  const [updateProfile, updateProfileState] =
    useUpdatePmResourceCalendarProfileMutation();
  const [deleteProfile, deleteProfileState] =
    useDeletePmResourceCalendarProfileMutation();
  const [replaceBlocks, replaceBlocksState] =
    useReplacePmResourceCalendarBlocksMutation();
  const [replaceAssignments, replaceAssignmentsState] =
    useReplacePmResourceCalendarAssignmentsMutation();
  const [createException, createExceptionState] =
    useCreatePmResourceCalendarExceptionMutation();
  const [updateException, updateExceptionState] =
    useUpdatePmResourceCalendarExceptionMutation();
  const [deleteException, deleteExceptionState] =
    useDeletePmResourceCalendarExceptionMutation();

  const profiles = overview?.profiles ?? [];
  const assignments = overview?.assignments ?? [];
  const exceptions = overview?.upcomingExceptions ?? [];
  const profileCount = profiles.length;
  const assignedUserCount = useMemo(
    () => new Set(assignments.map((assignment) => assignment.userId)).size,
    [assignments]
  );

  const isProfileSaving =
    createProfileState.isLoading ||
    updateProfileState.isLoading ||
    replaceBlocksState.isLoading;
  const isExceptionSaving =
    createExceptionState.isLoading || updateExceptionState.isLoading;
  const isDeleting =
    deleteProfileState.isLoading || deleteExceptionState.isLoading;

  const handleProfileSubmit = async (
    mode: ResourceCalendarProfileDialogState['mode'],
    id: number | undefined,
    profile: PMCreateResourceCalendarProfileRequest,
    blocks: PMReplaceResourceCalendarBlocksRequest
  ) => {
    try {
      const saved =
        mode === 'create'
          ? await createProfile(profile).unwrap()
          : id
            ? await updateProfile({ id, body: profile }).unwrap()
            : null;
      const profileId = saved?.id ?? id;
      if (!profileId) {
        return;
      }
      await replaceBlocks({ id: profileId, body: blocks }).unwrap();
      toast.success(
        mode === 'create'
          ? 'Resource calendar created.'
          : 'Resource calendar updated.'
      );
      setProfileDialog(null);
      setSelectedProfileId(profileId);
    } catch (error) {
      toast.error('Unable to save resource calendar', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleAssignmentsSubmit = async (
    profileId: number,
    body: PMReplaceResourceCalendarAssignmentsRequest
  ) => {
    try {
      await replaceAssignments({ id: profileId, body }).unwrap();
      toast.success('Assignments updated.');
    } catch (error) {
      toast.error('Unable to save assignments', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleExceptionSubmit = async (
    mode: 'create' | 'edit',
    id: number | undefined,
    body: PMCreateResourceCalendarExceptionRequest
  ) => {
    try {
      if (mode === 'create') {
        await createException(body).unwrap();
        toast.success('Exception created.');
      } else if (id) {
        await updateException({ id, body }).unwrap();
        toast.success('Exception updated.');
      }
    } catch (error) {
      toast.error('Unable to save exception', {
        description: getErrorMessage(error),
      });
      throw error;
    }
  };

  const handleConfirmDelete = async () => {
    if (!deleteTarget) {
      return;
    }

    try {
      if (deleteTarget.kind === 'profile') {
        await deleteProfile(deleteTarget.item.id).unwrap();
        toast.success('Resource calendar deleted.');
      } else {
        await deleteException(deleteTarget.item.id).unwrap();
        toast.success('Exception deleted.');
      }
      setDeleteTarget(null);
    } catch (error) {
      toast.error('Unable to delete resource calendar record', {
        description: getErrorMessage(error),
      });
    }
  };

  if (isLoading) {
    return (
      <div className='space-y-4'>
        <div className='grid gap-3 md:grid-cols-4'>
          {Array.from({ length: 4 }).map((_, index) => (
            <Skeleton key={index} className='h-20 rounded-md' />
          ))}
        </div>
        <Skeleton className='h-72 rounded-md' />
      </div>
    );
  }

  if (errorMessage) {
    return (
      <Card className='border-border/60 bg-background/90 shadow-sm'>
        <CardContent className='p-6 text-sm text-muted-foreground'>
          Unable to load resource calendars: {errorMessage}
        </CardContent>
      </Card>
    );
  }

  return (
    <div className='space-y-4'>
      <div className='flex flex-col gap-3 border-b border-border/60 pb-4 md:flex-row md:items-start md:justify-between'>
        <div className='space-y-1'>
          <h1 className='text-2xl font-semibold tracking-tight'>
            Resource calendars
          </h1>
          <p className='text-sm text-muted-foreground'>
            Configure workspace-level working calendars and capacity for PM
            optimization.
          </p>
        </div>
        <Button
          type='button'
          onClick={() => setProfileDialog({ mode: 'create' })}
        >
          <Plus className='mr-2 h-4 w-4' />
          Add calendar
        </Button>
      </div>

      <div className='grid gap-3 md:grid-cols-4'>
        <MiniStat title='Profiles' value={profileCount} />
        <MiniStat title='Assigned users' value={assignedUserCount} />
        <MiniStat title='Exceptions' value={exceptions.length} />
        <MiniStat
          title='Unassigned users'
          value={overview?.unassignedUserIds.length ?? 0}
        />
      </div>

      <Card className='border-border/60 bg-background/90 shadow-sm'>
        <CardHeader className='flex flex-row items-center justify-between border-b py-4'>
          <CardTitle className='flex items-center gap-2 text-sm'>
            <CalendarClock className='h-4 w-4 text-muted-foreground' />
            Calendar profiles
          </CardTitle>
        </CardHeader>
        <CardContent className='p-0'>
          <PMResourceCalendarProfileTable
            profiles={profiles}
            onEdit={(profile) =>
              setProfileDialog({ mode: 'edit', item: profile })
            }
            onDelete={(profile) =>
              setDeleteTarget({ kind: 'profile', item: profile })
            }
          />
        </CardContent>
      </Card>

      <div className='grid gap-4 xl:grid-cols-[minmax(0,1fr)_minmax(420px,0.9fr)]'>
        <PMResourceCalendarAssignmentPanel
          profiles={profiles}
          assignments={assignments}
          selectedProfileId={selectedProfileId}
          isSubmitting={replaceAssignmentsState.isLoading}
          onProfileChange={setSelectedProfileId}
          onSubmit={handleAssignmentsSubmit}
        />
        <Card className='border-border/60 bg-background/90 shadow-sm'>
          <CardHeader className='border-b py-4'>
            <CardTitle className='flex items-center gap-2 text-sm'>
              <Users className='h-4 w-4 text-muted-foreground' />
              Coverage
            </CardTitle>
          </CardHeader>
          <CardContent className='space-y-2 p-4 text-sm text-muted-foreground'>
            <p>{assignedUserCount} assigned user(s) use calendar profiles.</p>
            <p>
              Materialized window:{' '}
              {overview?.materializedWindowStart
                ? formatDate(overview.materializedWindowStart)
                : '-'}{' '}
              to{' '}
              {overview?.materializedWindowEnd
                ? formatDate(overview.materializedWindowEnd)
                : '-'}
            </p>
          </CardContent>
        </Card>
      </div>

      <PMResourceCalendarExceptionPanel
        exceptions={exceptions}
        isSubmitting={isExceptionSaving}
        onSubmit={handleExceptionSubmit}
        onDelete={(exception) =>
          setDeleteTarget({ kind: 'exception', item: exception })
        }
      />

      <PMResourceCalendarProfileDialog
        state={profileDialog}
        isSubmitting={isProfileSaving}
        onOpenChange={(open) => {
          if (!open) {
            setProfileDialog(null);
          }
        }}
        onSubmit={handleProfileSubmit}
      />
      <ConfirmDialog
        open={deleteTarget !== null}
        onOpenChange={(open) => {
          if (!open) {
            setDeleteTarget(null);
          }
        }}
        title='Delete resource calendar record'
        description={
          deleteTarget
            ? `Delete "${deleteTarget.kind === 'profile' ? deleteTarget.item.name : deleteTarget.item.reason || `Exception ${deleteTarget.item.id}`}"?`
            : undefined
        }
        confirmText='Delete'
        variant='destructive'
        isLoading={isDeleting}
        onConfirm={handleConfirmDelete}
      />
    </div>
  );
}

function formatDate(value: number) {
  return new Intl.DateTimeFormat('en', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  }).format(new Date(value));
}
