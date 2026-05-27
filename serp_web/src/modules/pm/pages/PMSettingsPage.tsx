/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM settings shell page
 */

'use client';

import { FormEvent, useDeferredValue, useEffect, useMemo, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import {
  Bug,
  CheckSquare,
  ChevronDown,
  Copy,
  Edit,
  Layers3,
  MoreHorizontal,
  Plus,
  Search,
  Trash2,
  Workflow,
  Zap,
} from 'lucide-react';
import { toast } from 'sonner';
import { getErrorMessage } from '@/lib/store/api';
import { ConfirmDialog } from '@/shared/components/ui/confirm-dialog';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Checkbox,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
  Input,
  Label,
  ScrollArea,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Skeleton,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
  Textarea,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useCreatePmIssueTypeMutation,
  useCreatePmIssueTypeSchemeMutation,
  useDeletePmIssueTypeMutation,
  useDeletePmIssueTypeSchemeMutation,
  useGetPmIssueTypeSettingsOverviewQuery,
  useManagePmIssueTypeSchemeItemsMutation,
  useUpdatePmIssueTypeMutation,
  useUpdatePmIssueTypeSchemeMutation,
} from '../api';
import type {
  PMCreateIssueTypeRequest,
  PMManageIssueTypeSchemeItemsRequest,
  PMUpdateIssueTypeRequest,
  PMWorkTypeSchemeSettingsApi,
  PMWorkTypeSettingsApi,
} from '../types/api';

type PMSettingsSection = 'work-types' | 'work-type-schemes';
type WorkTypeDialogState =
  | { mode: 'create'; item?: undefined }
  | { mode: 'edit'; item: PMWorkTypeSettingsApi };
type SchemeDialogState =
  | { mode: 'create'; item?: undefined }
  | { mode: 'edit'; item: PMWorkTypeSchemeSettingsApi };
type DeleteTarget =
  | { kind: 'work-type'; item: PMWorkTypeSettingsApi }
  | { kind: 'scheme'; item: PMWorkTypeSchemeSettingsApi };

const SETTINGS_ITEMS: Array<{
  key: PMSettingsSection;
  title: string;
  description: string;
  group: string;
}> = [
  {
    key: 'work-types',
    title: 'Work types',
    description: 'Manage the work item type catalog.',
    group: 'Work types',
  },
  {
    key: 'work-type-schemes',
    title: 'Work type schemes',
    description: 'Control which work types are available to projects.',
    group: 'Work types',
  },
];

const HIERARCHY_OPTIONS = [
  { value: 0, label: '0 - Sub-task level' },
  { value: 1, label: '1 - Standard work item' },
  { value: 2, label: '2 - Epic level' },
];

function includesText(value: string | null | undefined, needle: string) {
  return value?.toLowerCase().includes(needle) ?? false;
}

function formatCount(value?: number | null) {
  return typeof value === 'number' ? value.toString() : '-';
}

function normalizeOptionalText(value: string) {
  const trimmed = value.trim();
  return trimmed.length > 0 ? trimmed : null;
}

function orderedSelectedWorkTypeIds(
  workTypes: PMWorkTypeSettingsApi[],
  selectedIds: number[]
) {
  const selected = new Set(selectedIds);
  return workTypes
    .filter((workType) => selected.has(workType.id))
    .map((workType) => workType.id);
}

function WorkTypeGlyph({ workType }: { workType: { typeKey?: string; hierarchyLevel?: number | null } }) {
  const key = workType.typeKey?.toLowerCase() || '';
  if (key.includes('bug')) {
    return <Bug className='h-4 w-4 text-red-500' />;
  }
  if (key.includes('epic') || workType.hierarchyLevel === 2) {
    return <Zap className='h-4 w-4 text-purple-500' />;
  }
  if (key.includes('story')) {
    return <CheckSquare className='h-4 w-4 text-blue-500' />;
  }
  return <Copy className='h-4 w-4 text-sky-500' />;
}

export function PMSettingsPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const rawSection = searchParams.get('section') as PMSettingsSection | null;
  const section = SETTINGS_ITEMS.some((item) => item.key === rawSection)
    ? rawSection!
    : 'work-types';
  const [search, setSearch] = useState('');
  const deferredSearch = useDeferredValue(search.trim().toLowerCase());
  const [workTypeDialog, setWorkTypeDialog] =
    useState<WorkTypeDialogState | null>(null);
  const [schemeDialog, setSchemeDialog] = useState<SchemeDialogState | null>(
    null
  );
  const [deleteTarget, setDeleteTarget] = useState<DeleteTarget | null>(null);

  const overviewQuery = useGetPmIssueTypeSettingsOverviewQuery();
  const [createWorkType, createWorkTypeState] = useCreatePmIssueTypeMutation();
  const [updateWorkType, updateWorkTypeState] = useUpdatePmIssueTypeMutation();
  const [deleteWorkType, deleteWorkTypeState] = useDeletePmIssueTypeMutation();
  const [createScheme, createSchemeState] =
    useCreatePmIssueTypeSchemeMutation();
  const [updateScheme, updateSchemeState] =
    useUpdatePmIssueTypeSchemeMutation();
  const [manageSchemeItems, manageSchemeItemsState] =
    useManagePmIssueTypeSchemeItemsMutation();
  const [deleteScheme, deleteSchemeState] =
    useDeletePmIssueTypeSchemeMutation();

  const workTypes = overviewQuery.data?.workTypes ?? [];
  const schemes = overviewQuery.data?.workTypeSchemes ?? [];

  const filteredWorkTypes = useMemo(() => {
    if (!deferredSearch) {
      return workTypes;
    }
    return workTypes.filter(
      (workType) =>
        includesText(workType.name, deferredSearch) ||
        includesText(workType.typeKey, deferredSearch) ||
        includesText(workType.description, deferredSearch)
    );
  }, [deferredSearch, workTypes]);

  const filteredSchemes = useMemo(() => {
    if (!deferredSearch) {
      return schemes;
    }
    return schemes.filter(
      (scheme) =>
        includesText(scheme.name, deferredSearch) ||
        includesText(scheme.description, deferredSearch) ||
        scheme.workTypes.some((workType) =>
          includesText(workType.name, deferredSearch)
        ) ||
        scheme.spaces.some((space) => includesText(space.name, deferredSearch))
    );
  }, [deferredSearch, schemes]);

  const stats = useMemo(
    () => ({
      workTypes: workTypes.length,
      customWorkTypes: workTypes.filter((item) => !item.isSystem).length,
      schemes: schemes.length,
      boundSchemes: schemes.filter((item) => item.spaces.length > 0).length,
    }),
    [schemes, workTypes]
  );

  const updateSection = (nextSection: PMSettingsSection) => {
    const nextParams = new URLSearchParams(searchParams.toString());
    nextParams.set('section', nextSection);
    router.replace(`/pm/settings?${nextParams.toString()}`, {
      scroll: false,
    });
  };

  const handleWorkTypeSubmit = async (
    mode: WorkTypeDialogState['mode'],
    id: number | undefined,
    values: PMCreateIssueTypeRequest
  ) => {
    try {
      if (mode === 'create') {
        await createWorkType(values).unwrap();
        toast.success('Work type created.');
      } else if (id) {
        const updateBody: PMUpdateIssueTypeRequest = {
          name: values.name,
          description: values.description,
          iconUrl: values.iconUrl,
          hierarchyLevel: values.hierarchyLevel,
        };
        await updateWorkType({ id, body: updateBody }).unwrap();
        toast.success('Work type updated.');
      }
      setWorkTypeDialog(null);
    } catch (error) {
      toast.error('Unable to save work type', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleSchemeSubmit = async (
    mode: SchemeDialogState['mode'],
    id: number | undefined,
    values: {
      name: string;
      description: string | null;
      defaultIssueTypeId: number;
      issueTypeIds: number[];
    }
  ) => {
    if (!values.issueTypeIds.includes(values.defaultIssueTypeId)) {
      toast.error('Default work type must be included in the scheme.');
      return;
    }

    try {
      const itemBody: PMManageIssueTypeSchemeItemsRequest = {
        issueTypeIds: orderedSelectedWorkTypeIds(workTypes, values.issueTypeIds),
      };

      if (mode === 'create') {
        const created = await createScheme({
          name: values.name,
          description: values.description,
          defaultIssueTypeId: values.defaultIssueTypeId,
        }).unwrap();
        await manageSchemeItems({ id: created.id, body: itemBody }).unwrap();
        toast.success('Work type scheme created.');
      } else if (id) {
        await updateScheme({
          id,
          body: {
            name: values.name,
            description: values.description,
            defaultIssueTypeId: values.defaultIssueTypeId,
          },
        }).unwrap();
        await manageSchemeItems({ id, body: itemBody }).unwrap();
        toast.success('Work type scheme updated.');
      }
      setSchemeDialog(null);
    } catch (error) {
      toast.error('Unable to save work type scheme', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleConfirmDelete = async () => {
    if (!deleteTarget) {
      return;
    }

    try {
      if (deleteTarget.kind === 'work-type') {
        await deleteWorkType(deleteTarget.item.id).unwrap();
        toast.success('Work type deleted.');
      } else {
        await deleteScheme(deleteTarget.item.id).unwrap();
        toast.success('Work type scheme deleted.');
      }
      setDeleteTarget(null);
    } catch (error) {
      toast.error('Unable to delete record', {
        description: getErrorMessage(error),
      });
    }
  };

  const isDeleting =
    deleteWorkTypeState.isLoading || deleteSchemeState.isLoading;
  const isSchemeSaving =
    createSchemeState.isLoading ||
    updateSchemeState.isLoading ||
    manageSchemeItemsState.isLoading;
  const activeItems = section === 'work-types' ? filteredWorkTypes : filteredSchemes;

  return (
    <div className='grid gap-6 lg:grid-cols-[280px_minmax(0,1fr)]'>
      <aside className='space-y-4'>
        <Card className='border-border/60 bg-background/90 shadow-sm'>
          <CardHeader className='border-b py-4'>
            <CardTitle className='text-sm'>Switch settings</CardTitle>
          </CardHeader>
          <CardContent className='p-2'>
            <div className='space-y-3 p-1'>
              <Button
                type='button'
                variant='outline'
                className='w-full justify-between'
              >
                <span className='flex items-center gap-2'>
                  <Layers3 className='h-4 w-4' />
                  Work items
                </span>
                <ChevronDown className='h-4 w-4 text-muted-foreground' />
              </Button>

              <div className='space-y-2'>
                <p className='px-2 text-xs font-semibold uppercase text-muted-foreground'>
                  Work types
                </p>
                {SETTINGS_ITEMS.map((item) => {
                  const active = item.key === section;
                  return (
                    <button
                      key={item.key}
                      type='button'
                      onClick={() => updateSection(item.key)}
                      className={cn(
                        'flex w-full items-start gap-3 rounded-md px-3 py-2 text-left transition-colors hover:bg-muted',
                        active && 'bg-primary/10 text-primary'
                      )}
                    >
                      <Workflow className='mt-0.5 h-4 w-4 shrink-0' />
                      <span className='min-w-0'>
                        <span className='block text-sm font-medium'>
                          {item.title}
                        </span>
                        <span className='block text-xs text-muted-foreground'>
                          {item.description}
                        </span>
                      </span>
                    </button>
                  );
                })}
              </div>
            </div>
          </CardContent>
        </Card>
      </aside>

      <main className='space-y-4'>
        <div className='flex flex-col gap-3 border-b border-border/60 pb-4 md:flex-row md:items-start md:justify-between'>
          <div className='space-y-1'>
            <h1 className='text-2xl font-semibold tracking-tight'>
              {section === 'work-types'
                ? 'Work types'
                : 'Work type schemes'}
            </h1>
            <p className='text-sm text-muted-foreground'>
              {section === 'work-types'
                ? 'Structure the work items that teams can create and track.'
                : 'Choose which work types are available to each project space.'}
            </p>
          </div>
          <Button
            type='button'
            onClick={() =>
              section === 'work-types'
                ? setWorkTypeDialog({ mode: 'create' })
                : setSchemeDialog({ mode: 'create' })
            }
          >
            <Plus className='mr-2 h-4 w-4' />
            {section === 'work-types'
              ? 'Add work type'
              : 'Add work type scheme'}
          </Button>
        </div>

        <div className='grid gap-3 md:grid-cols-4'>
          <MiniStat title='Work types' value={stats.workTypes} />
          <MiniStat title='Custom types' value={stats.customWorkTypes} />
          <MiniStat title='Schemes' value={stats.schemes} />
          <MiniStat title='Bound schemes' value={stats.boundSchemes} />
        </div>

        <Card className='border-border/60 bg-background/90 shadow-sm'>
          <CardHeader className='flex flex-col gap-3 border-b py-4 md:flex-row md:items-center md:justify-between'>
            <div className='relative w-full md:max-w-sm'>
              <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
              <Input
                value={search}
                onChange={(event) => setSearch(event.target.value)}
                placeholder={
                  section === 'work-types'
                    ? 'Filter work types'
                    : 'Filter work type schemes'
                }
                className='pl-9'
              />
            </div>
            <Badge variant='secondary'>{activeItems.length} shown</Badge>
          </CardHeader>
          <CardContent className='p-0'>
            {overviewQuery.isLoading ? (
              <div className='space-y-3 p-4'>
                {Array.from({ length: 5 }).map((_, index) => (
                  <Skeleton key={index} className='h-12 rounded-md' />
                ))}
              </div>
            ) : overviewQuery.error ? (
              <div className='p-6 text-sm text-muted-foreground'>
                Unable to load PM settings: {getErrorMessage(overviewQuery.error)}
              </div>
            ) : section === 'work-types' ? (
              <WorkTypesTable
                workTypes={filteredWorkTypes}
                onEdit={(item) => setWorkTypeDialog({ mode: 'edit', item })}
                onDelete={(item) => setDeleteTarget({ kind: 'work-type', item })}
              />
            ) : (
              <WorkTypeSchemesTable
                schemes={filteredSchemes}
                onEdit={(item) => setSchemeDialog({ mode: 'edit', item })}
                onDelete={(item) => setDeleteTarget({ kind: 'scheme', item })}
              />
            )}
          </CardContent>
        </Card>
      </main>

      <WorkTypeDialog
        state={workTypeDialog}
        isSubmitting={createWorkTypeState.isLoading || updateWorkTypeState.isLoading}
        onOpenChange={(open) => !open && setWorkTypeDialog(null)}
        onSubmit={handleWorkTypeSubmit}
      />
      <SchemeDialog
        state={schemeDialog}
        workTypes={workTypes}
        isSubmitting={isSchemeSaving}
        onOpenChange={(open) => !open && setSchemeDialog(null)}
        onSubmit={handleSchemeSubmit}
      />
      <ConfirmDialog
        open={deleteTarget !== null}
        onOpenChange={(open) => !open && setDeleteTarget(null)}
        title='Delete PM setting'
        description={
          deleteTarget
            ? `Delete "${deleteTarget.item.name}"? This may fail if it is still used by projects or work items.`
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

function WorkTypesTable({
  workTypes,
  onEdit,
  onDelete,
}: {
  workTypes: PMWorkTypeSettingsApi[];
  onEdit: (item: PMWorkTypeSettingsApi) => void;
  onDelete: (item: PMWorkTypeSettingsApi) => void;
}) {
  if (workTypes.length === 0) {
    return <EmptyState message='No work types match the current filter.' />;
  }

  return (
    <div className='overflow-x-auto'>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Name</TableHead>
            <TableHead>Hierarchy level</TableHead>
            <TableHead>Related schemes</TableHead>
            <TableHead className='text-right'>Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {workTypes.map((workType) => (
            <TableRow key={workType.id}>
              <TableCell>
                <div className='flex min-w-64 items-start gap-2'>
                  <WorkTypeGlyph workType={workType} />
                  <div className='min-w-0'>
                    <div className='flex flex-wrap items-center gap-2'>
                      <span className='font-medium'>{workType.name}</span>
                      {workType.isSystem && (
                        <Badge variant='outline'>System</Badge>
                      )}
                    </div>
                    <p className='mt-0.5 text-xs text-muted-foreground'>
                      {workType.description || workType.typeKey}
                    </p>
                  </div>
                </div>
              </TableCell>
              <TableCell>{formatCount(workType.hierarchyLevel)}</TableCell>
              <TableCell>
                <RelatedSchemeList schemes={workType.relatedSchemes} />
              </TableCell>
              <TableCell className='text-right'>
                <RowActionMenu
                  readOnly={workType.readOnly}
                  onEdit={() => onEdit(workType)}
                  onDelete={() => onDelete(workType)}
                />
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
}

function WorkTypeSchemesTable({
  schemes,
  onEdit,
  onDelete,
}: {
  schemes: PMWorkTypeSchemeSettingsApi[];
  onEdit: (item: PMWorkTypeSchemeSettingsApi) => void;
  onDelete: (item: PMWorkTypeSchemeSettingsApi) => void;
}) {
  if (schemes.length === 0) {
    return (
      <EmptyState message='No work type schemes match the current filter.' />
    );
  }

  return (
    <div className='overflow-x-auto'>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Name</TableHead>
            <TableHead>Options</TableHead>
            <TableHead>Spaces</TableHead>
            <TableHead className='text-right'>Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {schemes.map((scheme) => (
            <TableRow key={scheme.id}>
              <TableCell>
                <div className='min-w-72'>
                  <div className='flex flex-wrap items-center gap-2'>
                    <span className='font-medium'>{scheme.name}</span>
                    {scheme.isSystem && <Badge variant='outline'>System</Badge>}
                  </div>
                  <p className='mt-0.5 text-xs text-muted-foreground'>
                    {scheme.description || 'No description.'}
                  </p>
                </div>
              </TableCell>
              <TableCell>
                <div className='space-y-1'>
                  {scheme.workTypes.map((workType) => (
                    <div key={workType.id} className='flex items-center gap-2'>
                      <WorkTypeGlyph workType={workType} />
                      <span className='text-sm'>{workType.name}</span>
                      {workType.isDefault && (
                        <Badge variant='secondary'>Default</Badge>
                      )}
                    </div>
                  ))}
                </div>
              </TableCell>
              <TableCell>
                <div className='space-y-1'>
                  {scheme.spaces.length === 0 ? (
                    <span className='text-sm text-muted-foreground'>-</span>
                  ) : (
                    scheme.spaces.map((space) => (
                      <Badge key={space.id} variant='outline'>
                        {space.key} {space.name}
                      </Badge>
                    ))
                  )}
                </div>
              </TableCell>
              <TableCell className='text-right'>
                <RowActionMenu
                  readOnly={scheme.readOnly}
                  onEdit={() => onEdit(scheme)}
                  onDelete={() => onDelete(scheme)}
                />
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
}

function RelatedSchemeList({
  schemes,
}: {
  schemes: PMWorkTypeSettingsApi['relatedSchemes'];
}) {
  if (schemes.length === 0) {
    return <span className='text-sm text-muted-foreground'>-</span>;
  }

  return (
    <div className='flex flex-wrap gap-1.5'>
      {schemes.map((scheme) => (
        <Badge key={scheme.id} variant='outline'>
          {scheme.name}
        </Badge>
      ))}
    </div>
  );
}

function RowActionMenu({
  readOnly,
  onEdit,
  onDelete,
}: {
  readOnly: boolean;
  onEdit: () => void;
  onDelete: () => void;
}) {
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button type='button' variant='ghost' size='sm'>
          <MoreHorizontal className='h-4 w-4' />
          <span className='sr-only'>Open actions</span>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align='end'>
        <DropdownMenuItem disabled={readOnly} onClick={onEdit}>
          <Edit className='mr-2 h-4 w-4' />
          Edit
        </DropdownMenuItem>
        <DropdownMenuSeparator />
        <DropdownMenuItem
          disabled={readOnly}
          variant='destructive'
          onClick={onDelete}
        >
          <Trash2 className='mr-2 h-4 w-4' />
          Delete
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}

function WorkTypeDialog({
  state,
  isSubmitting,
  onOpenChange,
  onSubmit,
}: {
  state: WorkTypeDialogState | null;
  isSubmitting: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (
    mode: WorkTypeDialogState['mode'],
    id: number | undefined,
    values: PMCreateIssueTypeRequest
  ) => Promise<void>;
}) {
  const [typeKey, setTypeKey] = useState('');
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [iconUrl, setIconUrl] = useState('');
  const [hierarchyLevel, setHierarchyLevel] = useState('1');

  useEffect(() => {
    if (!state) {
      return;
    }
    setTypeKey(state.item?.typeKey ?? '');
    setName(state.item?.name ?? '');
    setDescription(state.item?.description ?? '');
    setIconUrl(state.item?.iconUrl ?? '');
    setHierarchyLevel(String(state.item?.hierarchyLevel ?? 1));
  }, [state]);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!state) {
      return;
    }
    const cleanName = name.trim();
    const cleanTypeKey = typeKey.trim();
    if (!cleanName || (state.mode === 'create' && !cleanTypeKey)) {
      toast.error('Work type key and name are required.');
      return;
    }

    await onSubmit(state.mode, state.item?.id, {
      typeKey: cleanTypeKey,
      name: cleanName,
      description: normalizeOptionalText(description),
      iconUrl: normalizeOptionalText(iconUrl),
      hierarchyLevel: Number(hierarchyLevel),
    });
  };

  return (
    <Dialog open={state !== null} onOpenChange={onOpenChange}>
      <DialogContent>
        <form onSubmit={handleSubmit} className='space-y-4'>
          <DialogHeader>
            <DialogTitle>
              {state?.mode === 'edit' ? 'Edit work type' : 'Add work type'}
            </DialogTitle>
            <DialogDescription>
              Configure the catalog item that teams use when creating work.
            </DialogDescription>
          </DialogHeader>

          <div className='grid gap-4'>
            <div className='space-y-2'>
              <Label htmlFor='work-type-key'>Key</Label>
              <Input
                id='work-type-key'
                value={typeKey}
                onChange={(event) => setTypeKey(event.target.value)}
                disabled={state?.mode === 'edit'}
                placeholder='task'
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='work-type-name'>Name</Label>
              <Input
                id='work-type-name'
                value={name}
                onChange={(event) => setName(event.target.value)}
                placeholder='Task'
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='work-type-description'>Description</Label>
              <Textarea
                id='work-type-description'
                value={description}
                onChange={(event) => setDescription(event.target.value)}
                rows={3}
              />
            </div>
            <div className='grid gap-4 sm:grid-cols-2'>
              <div className='space-y-2'>
                <Label htmlFor='work-type-icon'>Icon URL</Label>
                <Input
                  id='work-type-icon'
                  value={iconUrl}
                  onChange={(event) => setIconUrl(event.target.value)}
                  placeholder='https://...'
                />
              </div>
              <div className='space-y-2'>
                <Label>Hierarchy level</Label>
                <Select value={hierarchyLevel} onValueChange={setHierarchyLevel}>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {HIERARCHY_OPTIONS.map((option) => (
                      <SelectItem key={option.value} value={String(option.value)}>
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
          </div>

          <DialogFooter>
            <Button
              type='button'
              variant='outline'
              onClick={() => onOpenChange(false)}
              disabled={isSubmitting}
            >
              Cancel
            </Button>
            <Button type='submit' disabled={isSubmitting}>
              {isSubmitting ? 'Saving...' : 'Save'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

function SchemeDialog({
  state,
  workTypes,
  isSubmitting,
  onOpenChange,
  onSubmit,
}: {
  state: SchemeDialogState | null;
  workTypes: PMWorkTypeSettingsApi[];
  isSubmitting: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (
    mode: SchemeDialogState['mode'],
    id: number | undefined,
    values: {
      name: string;
      description: string | null;
      defaultIssueTypeId: number;
      issueTypeIds: number[];
    }
  ) => Promise<void>;
}) {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [defaultIssueTypeId, setDefaultIssueTypeId] = useState('');
  const [selectedIds, setSelectedIds] = useState<number[]>([]);

  useEffect(() => {
    if (!state) {
      return;
    }
    const initialIds =
      state.mode === 'edit'
        ? state.item.workTypes.map((workType) => workType.id)
        : workTypes.slice(0, 1).map((workType) => workType.id);
    const initialDefault =
      state.mode === 'edit'
        ? state.item.defaultIssueTypeId
        : initialIds[0] ?? workTypes[0]?.id;

    setName(state.item?.name ?? '');
    setDescription(state.item?.description ?? '');
    setSelectedIds(initialIds);
    setDefaultIssueTypeId(initialDefault ? String(initialDefault) : '');
  }, [state, workTypes]);

  const toggleWorkType = (id: number, checked: boolean) => {
    setSelectedIds((current) => {
      if (checked) {
        return current.includes(id) ? current : [...current, id];
      }
      return current.filter((currentId) => currentId !== id);
    });
    if (!checked && defaultIssueTypeId === String(id)) {
      setDefaultIssueTypeId('');
    }
  };

  const handleDefaultChange = (value: string) => {
    const id = Number(value);
    setDefaultIssueTypeId(value);
    setSelectedIds((current) => (current.includes(id) ? current : [...current, id]));
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!state) {
      return;
    }
    const cleanName = name.trim();
    const defaultId = Number(defaultIssueTypeId);
    if (!cleanName || !Number.isFinite(defaultId) || selectedIds.length === 0) {
      toast.error('Name, default work type, and at least one option are required.');
      return;
    }

    await onSubmit(state.mode, state.item?.id, {
      name: cleanName,
      description: normalizeOptionalText(description),
      defaultIssueTypeId: defaultId,
      issueTypeIds: selectedIds,
    });
  };

  return (
    <Dialog open={state !== null} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-2xl'>
        <form onSubmit={handleSubmit} className='space-y-4'>
          <DialogHeader>
            <DialogTitle>
              {state?.mode === 'edit'
                ? 'Edit work type scheme'
                : 'Add work type scheme'}
            </DialogTitle>
            <DialogDescription>
              Select the work types available to projects using this scheme.
            </DialogDescription>
          </DialogHeader>

          <div className='grid gap-4'>
            <div className='grid gap-4 sm:grid-cols-2'>
              <div className='space-y-2'>
                <Label htmlFor='scheme-name'>Name</Label>
                <Input
                  id='scheme-name'
                  value={name}
                  onChange={(event) => setName(event.target.value)}
                />
              </div>
              <div className='space-y-2'>
                <Label>Default work type</Label>
                <Select
                  value={defaultIssueTypeId}
                  onValueChange={handleDefaultChange}
                >
                  <SelectTrigger>
                    <SelectValue placeholder='Select default' />
                  </SelectTrigger>
                  <SelectContent>
                    {workTypes.map((workType) => (
                      <SelectItem key={workType.id} value={String(workType.id)}>
                        {workType.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
            <div className='space-y-2'>
              <Label htmlFor='scheme-description'>Description</Label>
              <Textarea
                id='scheme-description'
                value={description}
                onChange={(event) => setDescription(event.target.value)}
                rows={3}
              />
            </div>
            <div className='space-y-2'>
              <Label>Options</Label>
              <ScrollArea className='h-56 rounded-md border'>
                <div className='divide-y'>
                  {workTypes.map((workType) => {
                    const checked = selectedIds.includes(workType.id);
                    return (
                      <label
                        key={workType.id}
                        className='flex cursor-pointer items-start gap-3 px-3 py-2'
                      >
                        <Checkbox
                          checked={checked}
                          onCheckedChange={(value) =>
                            toggleWorkType(workType.id, value === true)
                          }
                        />
                        <span className='flex min-w-0 items-start gap-2'>
                          <WorkTypeGlyph workType={workType} />
                          <span className='min-w-0'>
                            <span className='block text-sm font-medium'>
                              {workType.name}
                            </span>
                            <span className='block text-xs text-muted-foreground'>
                              {workType.description || workType.typeKey}
                            </span>
                          </span>
                        </span>
                      </label>
                    );
                  })}
                </div>
              </ScrollArea>
            </div>
          </div>

          <DialogFooter>
            <Button
              type='button'
              variant='outline'
              onClick={() => onOpenChange(false)}
              disabled={isSubmitting}
            >
              Cancel
            </Button>
            <Button type='submit' disabled={isSubmitting || workTypes.length === 0}>
              {isSubmitting ? 'Saving...' : 'Save'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

function MiniStat({ title, value }: { title: string; value: number }) {
  return (
    <div className='rounded-md border bg-muted/20 px-3 py-3'>
      <p className='text-xs font-medium uppercase text-muted-foreground'>
        {title}
      </p>
      <div className='mt-1 text-2xl font-semibold'>{value}</div>
    </div>
  );
}

function EmptyState({ message }: { message: string }) {
  return (
    <div className='p-6 text-sm text-muted-foreground'>
      {message}
    </div>
  );
}
