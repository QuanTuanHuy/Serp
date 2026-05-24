/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project components management page
 */

'use client';

import { useDeferredValue, useMemo, useState } from 'react';
import Link from 'next/link';
import {
  AlertCircle,
  Loader2,
  MoreHorizontal,
  Plus,
  Search,
} from 'lucide-react';
import { toast } from 'sonner';
import { getErrorMessage } from '@/lib/store/api';
import { selectOrganizationId } from '@/modules/account/store';
import { useGetOrganizationUsersQuery } from '@/modules/settings/services/users/usersApi';
import {
  Alert,
  AlertDescription,
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertTitle,
  Avatar,
  AvatarFallback,
  Button,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
  Input,
  Skeleton,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui';
import { useAppSelector } from '@/shared/hooks';
import {
  getAssigneeLabel,
  PMProjectComponentDialog,
  toComponentLeadUserId,
  type ComponentFormValues,
} from '../components/projects/PMProjectComponentDialog';
import {
  useCreatePmProjectComponentMutation,
  useDeletePmProjectComponentMutation,
  useGetPmProjectComponentsQuery,
  useUpdatePmProjectComponentMutation,
} from '../api/projectApi';
import type {
  PMCreateProjectComponentRequest,
  PMProjectComponentApi,
} from '../types/api';

interface PMProjectComponentsPageProps {
  projectId: string;
}

export function PMProjectComponentsPage({
  projectId,
}: PMProjectComponentsPageProps) {
  const numericProjectId = Number(projectId);
  const organizationId = useAppSelector(selectOrganizationId);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingComponent, setEditingComponent] =
    useState<PMProjectComponentApi | null>(null);
  const [deletingComponent, setDeletingComponent] =
    useState<PMProjectComponentApi | null>(null);
  const deferredSearch = useDeferredValue(search.trim());

  const componentsQuery = useGetPmProjectComponentsQuery(
    {
      projectId: numericProjectId,
      params: {
        search: deferredSearch || undefined,
        page: 0,
        pageSize: 100,
        sortBy: 'name',
        sortDirection: 'asc',
      },
    },
    { skip: !Number.isFinite(numericProjectId) }
  );

  const { data: usersResponse, isLoading: isUsersLoading } =
    useGetOrganizationUsersQuery(
      {
        organizationId: organizationId as number,
        page: 0,
        pageSize: 100,
        status: 'ACTIVE',
        sortBy: 'firstName',
        sortDir: 'ASC',
      },
      { skip: !organizationId }
    );

  const [createComponent, createState] = useCreatePmProjectComponentMutation();
  const [updateComponent, updateState] = useUpdatePmProjectComponentMutation();
  const [deleteComponent, deleteState] = useDeletePmProjectComponentMutation();

  const usersById = useMemo(() => {
    const map = new Map<number, string>();
    for (const user of usersResponse?.data.items || []) {
      const name =
        `${user.firstName || ''} ${user.lastName || ''}`.trim() ||
        user.email ||
        `User #${user.id}`;
      map.set(Number(user.id), name);
    }
    return map;
  }, [usersResponse]);

  const userOptions = useMemo(
    () =>
      [...(usersResponse?.data.items || [])]
        .map((user) => {
          const label =
            `${user.firstName || ''} ${user.lastName || ''}`.trim() ||
            user.email ||
            `User #${user.id}`;
          return {
            value: String(user.id),
            label,
          };
        })
        .sort((left, right) => left.label.localeCompare(right.label)),
    [usersResponse]
  );

  const components = componentsQuery.data?.data.items ?? [];
  const totalItems = componentsQuery.data?.data.totalItems ?? 0;
  const isInitialLoading = componentsQuery.isLoading && components.length === 0;
  const isSaving = createState.isLoading || updateState.isLoading;

  const openCreateDialog = () => {
    setEditingComponent(null);
    setDialogOpen(true);
  };

  const openEditDialog = (component: PMProjectComponentApi) => {
    setEditingComponent(component);
    setDialogOpen(true);
  };

  const handleSubmit = async (values: ComponentFormValues) => {
    const body: PMCreateProjectComponentRequest = {
      name: values.name.trim(),
      description: values.description?.trim() || undefined,
      leadUserId: toComponentLeadUserId(values.leadUserId),
      assigneeType: values.assigneeType,
    };

    try {
      if (editingComponent) {
        await updateComponent({
          projectId: numericProjectId,
          componentId: editingComponent.id,
          body,
        }).unwrap();
        toast.success(`Component ${body.name} updated.`);
      } else {
        await createComponent({
          projectId: numericProjectId,
          body,
        }).unwrap();
        toast.success(`Component ${body.name} created.`);
      }
      setDialogOpen(false);
      setEditingComponent(null);
    } catch (error) {
      toast.error(
        editingComponent
          ? 'Failed to update component'
          : 'Failed to create component',
        {
          description: getErrorMessage(error),
        }
      );
    }
  };

  const handleDelete = async () => {
    if (!deletingComponent) {
      return;
    }

    try {
      await deleteComponent({
        projectId: numericProjectId,
        componentId: deletingComponent.id,
      }).unwrap();
      toast.success(`Component ${deletingComponent.name} deleted.`);
      setDeletingComponent(null);
    } catch (error) {
      toast.error('Failed to delete component', {
        description: getErrorMessage(error),
      });
    }
  };

  if (!Number.isFinite(numericProjectId)) {
    return (
      <Alert variant='destructive'>
        <AlertCircle className='h-4 w-4' />
        <AlertTitle>Invalid project</AlertTitle>
        <AlertDescription>
          Project id must be a numeric identifier.
        </AlertDescription>
      </Alert>
    );
  }

  return (
    <div className='space-y-5'>
      <div className='flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between'>
        <div className='max-w-2xl space-y-2'></div>
        <div className='flex flex-col items-start gap-3 sm:flex-row sm:items-center lg:justify-end'>
          <p className='text-sm text-muted-foreground'>
            This project has{' '}
            <span className='font-medium text-foreground'>{totalItems}</span>{' '}
            {totalItems === 1 ? 'component' : 'components'}
          </p>
          <Button type='button' onClick={openCreateDialog}>
            Create component
          </Button>
        </div>
      </div>

      <div className='relative max-w-xs'>
        <Search className='pointer-events-none absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
        <Input
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          placeholder='Search components'
          className='pl-9'
        />
      </div>

      {componentsQuery.error ? (
        <Alert variant='destructive'>
          <AlertCircle className='h-4 w-4' />
          <AlertTitle>Components unavailable</AlertTitle>
          <AlertDescription>
            {getErrorMessage(componentsQuery.error)}
          </AlertDescription>
        </Alert>
      ) : null}

      <div className='overflow-hidden rounded-lg border bg-background'>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className='w-[20%]'>Component</TableHead>
              <TableHead>Description</TableHead>
              <TableHead className='w-[20%]'>Component lead</TableHead>
              <TableHead className='w-[16%]'>Default assignee</TableHead>
              <TableHead className='w-[12%]'>Issues</TableHead>
              <TableHead className='w-12 text-right'>More actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isInitialLoading ? (
              Array.from({ length: 4 }).map((_, index) => (
                <TableRow key={index}>
                  <TableCell>
                    <Skeleton className='h-5 w-36' />
                  </TableCell>
                  <TableCell>
                    <Skeleton className='h-5 w-full max-w-md' />
                  </TableCell>
                  <TableCell>
                    <Skeleton className='h-6 w-32' />
                  </TableCell>
                  <TableCell>
                    <Skeleton className='h-5 w-28' />
                  </TableCell>
                  <TableCell>
                    <Skeleton className='h-5 w-16' />
                  </TableCell>
                  <TableCell />
                </TableRow>
              ))
            ) : components.length ? (
              components.map((component) => (
                <TableRow key={component.id}>
                  <TableCell className='font-medium'>
                    {component.name}
                  </TableCell>
                  <TableCell className='max-w-md whitespace-normal text-muted-foreground'>
                    {component.description || '-'}
                  </TableCell>
                  <TableCell>
                    <ComponentLeadValue
                      leadUserId={component.leadUserId}
                      usersById={usersById}
                      loading={isUsersLoading}
                    />
                  </TableCell>
                  <TableCell>
                    {getAssigneeLabel(component.assigneeType)}
                  </TableCell>
                  <TableCell>
                    <Link
                      href={`/pm/projects/${numericProjectId}/list?componentIds=${component.id}`}
                      className='font-medium text-primary hover:underline'
                    >
                      {component.issueCount}{' '}
                      {component.issueCount === 1 ? 'issue' : 'issues'}
                    </Link>
                  </TableCell>
                  <TableCell className='text-right'>
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button
                          type='button'
                          variant='ghost'
                          size='icon'
                          className='h-8 w-8'
                          aria-label={`Actions for ${component.name}`}
                        >
                          <MoreHorizontal className='h-4 w-4' />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align='end'>
                        <DropdownMenuItem
                          onSelect={() => openEditDialog(component)}
                        >
                          Edit
                        </DropdownMenuItem>
                        <DropdownMenuItem
                          variant='destructive'
                          onSelect={() => setDeletingComponent(component)}
                        >
                          Delete
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </TableCell>
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={6} className='h-40 text-center'>
                  <div className='space-y-2'>
                    <p className='font-medium'>
                      {deferredSearch
                        ? 'No components match your search'
                        : 'No components yet'}
                    </p>
                    <p className='text-sm text-muted-foreground'>
                      {deferredSearch
                        ? 'Try another component name.'
                        : 'Create a component to group related work items.'}
                    </p>
                    {!deferredSearch ? (
                      <Button type='button' onClick={openCreateDialog}>
                        Create component
                      </Button>
                    ) : null}
                  </div>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>

      {componentsQuery.isFetching && !isInitialLoading ? (
        <p className='flex items-center gap-2 text-xs text-muted-foreground'>
          <Loader2 className='h-3.5 w-3.5 animate-spin' />
          Refreshing components...
        </p>
      ) : null}

      <PMProjectComponentDialog
        open={dialogOpen}
        component={editingComponent}
        users={userOptions}
        saving={isSaving}
        onOpenChange={(open) => {
          setDialogOpen(open);
          if (!open) {
            setEditingComponent(null);
          }
        }}
        onSubmit={handleSubmit}
      />

      <AlertDialog
        open={Boolean(deletingComponent)}
        onOpenChange={(open) => {
          if (!open) {
            setDeletingComponent(null);
          }
        }}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete component?</AlertDialogTitle>
            <AlertDialogDescription>
              This removes {deletingComponent?.name} from the project component
              list and clears existing work item component links.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={deleteState.isLoading}>
              Cancel
            </AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDelete}
              disabled={deleteState.isLoading}
            >
              {deleteState.isLoading ? 'Deleting...' : 'Delete'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}

function ComponentLeadValue({
  leadUserId,
  usersById,
  loading,
}: {
  leadUserId?: number | null;
  usersById: Map<number, string>;
  loading: boolean;
}) {
  if (!leadUserId) {
    return <span className='text-muted-foreground'>None</span>;
  }

  const name = usersById.get(leadUserId) || `User #${leadUserId}`;

  return (
    <span className='inline-flex min-w-0 items-center gap-2'>
      <Avatar className='h-6 w-6'>
        <AvatarFallback className='text-[10px]'>
          {getInitials(name)}
        </AvatarFallback>
      </Avatar>
      <span className='truncate'>
        {loading && !usersById.has(leadUserId) ? `User #${leadUserId}` : name}
      </span>
    </span>
  );
}

function getInitials(name: string): string {
  const parts = name.trim().split(/\s+/).filter(Boolean).slice(0, 2);
  if (!parts.length) {
    return 'U';
  }
  return parts.map((part) => part[0]?.toUpperCase()).join('');
}
