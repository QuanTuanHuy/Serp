/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM create work item dialog
 */

'use client';

import { useEffect, useMemo, useState } from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import { Loader2, PlusSquare } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';
import { z } from 'zod';
import { getErrorMessage } from '@/lib/store/api';
import { selectOrganizationId } from '@/modules/account/store';
import { useGetOrganizationUsersQuery } from '@/modules/settings/services/users/usersApi';
import { useAppSelector } from '@/shared/hooks';
import {
  Alert,
  AlertDescription,
  AlertTitle,
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
  Input,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Textarea,
} from '@/shared/components/ui';
import { Combobox, type ComboboxItem } from '@/shared/components/ui/combobox';
import {
  useCreatePmWorkItemMutation,
  useGetPmProjectsQuery,
  useGetPmWorkItemCreateMetaQuery,
  useSearchPmWorkItemsQuery,
} from '../../api';
import type {
  PMCreateWorkItemRequest,
  PMProjectSummaryApi,
  PMWorkItemCustomField,
  PMWorkItemSearchApi,
} from '../../types/api';

const createWorkItemSchema = z.object({
  projectId: z.string().min(1, 'Project is required'),
  issueTypeId: z.string().min(1, 'Issue type is required'),
  summary: z
    .string()
    .trim()
    .min(1, 'Summary is required')
    .max(512, 'Summary must be 512 characters or fewer'),
  description: z.string().optional().or(z.literal('')),
  priorityId: z.string().optional().or(z.literal('')),
  assigneeId: z.string().optional().or(z.literal('')),
  parentId: z.string().optional().or(z.literal('')),
  dueDate: z.string().optional().or(z.literal('')),
  timeOriginalEstimate: z.string().optional().or(z.literal('')),
  securityLevelId: z.string().optional().or(z.literal('')),
});

type CreateWorkItemFormValues = z.infer<typeof createWorkItemSchema>;

interface CreateWorkItemDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  initialProjectId?: number;
}

function mapProjectToComboboxItem(
  project: PMProjectSummaryApi
): ComboboxItem & { archived: boolean } {
  return {
    value: String(project.id),
    label: `${project.name} (${project.key})`,
    archived: project.isArchived,
  };
}

function mapWorkItemToComboboxItem(item: PMWorkItemSearchApi): ComboboxItem {
  return {
    value: String(item.id),
    label: `${item.key} - ${item.summary}`,
  };
}

function buildCreateRequest(
  values: CreateWorkItemFormValues,
  customFields: PMWorkItemCustomField[]
): PMCreateWorkItemRequest {
  const customFieldPayload: Record<string, unknown> = {};

  customFields.forEach((field) => {
    if (field.defaultValues.length === 1) {
      customFieldPayload[field.fieldKey] = field.defaultValues[0].value;
      return;
    }

    if (field.defaultValues.length > 1) {
      customFieldPayload[field.fieldKey] = field.defaultValues.map(
        (defaultValue) => defaultValue.value
      );
    }
  });

  return {
    issueTypeId: Number(values.issueTypeId),
    summary: values.summary.trim(),
    description: values.description?.trim() || undefined,
    priorityId: values.priorityId ? Number(values.priorityId) : undefined,
    assigneeId: values.assigneeId ? Number(values.assigneeId) : undefined,
    parentId: values.parentId ? Number(values.parentId) : undefined,
    dueDate: values.dueDate ? new Date(values.dueDate).getTime() : undefined,
    timeOriginalEstimate: values.timeOriginalEstimate
      ? Number(values.timeOriginalEstimate)
      : undefined,
    securityLevelId: values.securityLevelId
      ? Number(values.securityLevelId)
      : undefined,
    customFields:
      Object.keys(customFieldPayload).length > 0
        ? customFieldPayload
        : undefined,
  };
}

export function CreateWorkItemDialog({
  open,
  onOpenChange,
  initialProjectId,
}: CreateWorkItemDialogProps) {
  const organizationId = useAppSelector(selectOrganizationId);
  const [projectSearch, setProjectSearch] = useState('');
  const [parentSearch, setParentSearch] = useState('');
  const [createPmWorkItem] = useCreatePmWorkItemMutation();

  const form = useForm<CreateWorkItemFormValues>({
    resolver: zodResolver(createWorkItemSchema),
    defaultValues: {
      projectId: initialProjectId ? String(initialProjectId) : '',
      issueTypeId: '',
      summary: '',
      description: '',
      priorityId: '',
      assigneeId: '',
      parentId: '',
      dueDate: '',
      timeOriginalEstimate: '',
      securityLevelId: '',
    },
  });

  const selectedProjectId = form.watch('projectId');
  const selectedIssueTypeId = form.watch('issueTypeId');

  useEffect(() => {
    if (open) {
      form.reset({
        projectId: initialProjectId ? String(initialProjectId) : '',
        issueTypeId: '',
        summary: '',
        description: '',
        priorityId: '',
        assigneeId: '',
        parentId: '',
        dueDate: '',
        timeOriginalEstimate: '',
        securityLevelId: '',
      });
      setProjectSearch('');
      setParentSearch('');
    }
  }, [form, initialProjectId, open]);

  const { data: projectResponse, isLoading: isProjectLoading } =
    useGetPmProjectsQuery({
      page: 0,
      pageSize: 50,
      search: projectSearch.trim() || undefined,
      sortBy: 'updatedAt',
      sortDirection: 'desc',
    });

  const projectItems = useMemo(
    () => (projectResponse?.data.items || []).map(mapProjectToComboboxItem),
    [projectResponse]
  );

  const { data: meta, isFetching: isMetaFetching } =
    useGetPmWorkItemCreateMetaQuery(
      {
        projectId: Number(selectedProjectId),
        issueTypeId: selectedIssueTypeId
          ? Number(selectedIssueTypeId)
          : undefined,
      },
      {
        skip: !selectedProjectId,
      }
    );

  const { data: usersResponse, isLoading: isUserLoading } =
    useGetOrganizationUsersQuery(
      {
        organizationId: organizationId as number,
        page: 0,
        pageSize: 100,
        status: 'ACTIVE',
      },
      { skip: !organizationId || !open }
    );

  const assigneeOptions = useMemo(
    () =>
      [...(usersResponse?.data.items || [])]
        .sort((left, right) => {
          const leftName =
            `${left.firstName || ''} ${left.lastName || ''}`.trim();
          const rightName =
            `${right.firstName || ''} ${right.lastName || ''}`.trim();
          return leftName.localeCompare(rightName);
        })
        .map((user) => ({
          id: String(user.id),
          label:
            `${user.firstName || ''} ${user.lastName || ''}`.trim() ||
            user.email ||
            `User #${user.id}`,
        })),
    [usersResponse]
  );

  const { data: parentResponse, isFetching: isParentFetching } =
    useSearchPmWorkItemsQuery(
      {
        projectId: Number(selectedProjectId),
        params: {
          keyword: parentSearch.trim() || undefined,
          page: 0,
          pageSize: 20,
          sortField: 'updatedAt',
          sortDirection: 'DESC',
        },
      },
      {
        skip: !selectedProjectId,
      }
    );

  const parentItems = useMemo(
    () =>
      (parentResponse?.data.items || [])
        .filter((item) => String(item.issueTypeId) !== selectedIssueTypeId)
        .map(mapWorkItemToComboboxItem),
    [parentResponse, selectedIssueTypeId]
  );

  useEffect(() => {
    if (!meta) {
      return;
    }

    const currentIssueTypeId = form.getValues('issueTypeId');
    const hasIssueType = meta.issueTypes.some(
      (issueType) => String(issueType.id) === currentIssueTypeId
    );

    if (!hasIssueType) {
      form.setValue('issueTypeId', String(meta.selectedIssueTypeId), {
        shouldValidate: true,
      });
    }

    form.setValue(
      'priorityId',
      meta.defaultPriorityId ? String(meta.defaultPriorityId) : '',
      {
        shouldDirty: false,
      }
    );
    form.setValue(
      'securityLevelId',
      meta.defaultSecurityLevelId ? String(meta.defaultSecurityLevelId) : '',
      {
        shouldDirty: false,
      }
    );
  }, [form, meta]);

  const handleProjectChange = (value: string | number | undefined) => {
    const nextProjectId = value ? String(value) : '';
    form.setValue('projectId', nextProjectId, {
      shouldDirty: true,
      shouldValidate: true,
    });
    form.setValue('issueTypeId', '', { shouldDirty: true });
    form.setValue('priorityId', '', { shouldDirty: true });
    form.setValue('securityLevelId', '', { shouldDirty: true });
    form.setValue('parentId', '', { shouldDirty: true });
    setParentSearch('');
  };

  const handleSubmit = async (values: CreateWorkItemFormValues) => {
    if (!meta) {
      toast.error('Create metadata is still loading.');
      return;
    }

    try {
      const created = await createPmWorkItem({
        projectId: Number(values.projectId),
        body: buildCreateRequest(values, meta.customFields),
      }).unwrap();

      toast.success(`Work item ${created.key} created successfully.`);
      onOpenChange(false);
    } catch (error) {
      toast.error('Failed to create work item', {
        description: getErrorMessage(error),
      });
    }
  };

  const metaBlockedReason = meta?.createBlockedReason?.trim();

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-h-[90vh] overflow-y-auto sm:max-w-2xl'>
        <DialogHeader>
          <DialogTitle className='flex items-center gap-2'>
            <PlusSquare className='h-5 w-5 text-primary' />
            Create work item
          </DialogTitle>
          <DialogDescription>
            Select project first. Issue type, initial status, and visible fields
            update from project configuration.
          </DialogDescription>
        </DialogHeader>

        <Form {...form}>
          <form
            onSubmit={form.handleSubmit(handleSubmit)}
            className='space-y-6'
          >
            <div className='space-y-6'>
              <FormField
                control={form.control}
                name='projectId'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Project</FormLabel>
                    <FormControl>
                      <Combobox
                        value={field.value || undefined}
                        onChange={handleProjectChange}
                        items={projectItems}
                        placeholder='Select project'
                        emptyText='No projects found'
                        loading={isProjectLoading}
                        onSearch={setProjectSearch}
                      />
                    </FormControl>
                    <FormDescription>
                      Choose any PM project to load create rules.
                    </FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name='issueTypeId'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Issue type</FormLabel>
                    <Select
                      onValueChange={field.onChange}
                      value={field.value || undefined}
                      disabled={!meta || isMetaFetching}
                    >
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder='Select issue type' />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {(meta?.issueTypes || []).map((issueType) => (
                          <SelectItem
                            key={issueType.id}
                            value={String(issueType.id)}
                          >
                            {issueType.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <div className='space-y-2'>
                <FormLabel>Initial status</FormLabel>
                <div className='flex h-10 items-center rounded-md border bg-muted/40 px-3 text-sm'>
                  {isMetaFetching ? (
                    <span className='flex items-center gap-2 text-muted-foreground'>
                      <Loader2 className='h-4 w-4 animate-spin' />
                      Resolving status...
                    </span>
                  ) : meta?.initialStatus ? (
                    meta.initialStatus.name
                  ) : (
                    <span className='text-muted-foreground'>
                      Select project first
                    </span>
                  )}
                </div>
                <p className='text-xs text-muted-foreground'>
                  Status comes from project workflow initial step.
                </p>
              </div>
            </div>

            {metaBlockedReason ? (
              <Alert variant='destructive'>
                <AlertTitle>Work item creation is blocked</AlertTitle>
                <AlertDescription>{metaBlockedReason}</AlertDescription>
              </Alert>
            ) : null}

            <div className='space-y-6'>
              <FormField
                control={form.control}
                name='summary'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Summary</FormLabel>
                    <FormControl>
                      <Input
                        placeholder='Summarize work item objective'
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name='description'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Description</FormLabel>
                    <FormControl>
                      <Textarea
                        rows={10}
                        placeholder='Add context, scope, acceptance notes, or delivery details.'
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name='priorityId'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Priority</FormLabel>
                    <Select
                      onValueChange={field.onChange}
                      value={field.value || undefined}
                      disabled={!meta || isMetaFetching}
                    >
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder='Select priority' />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {(meta?.priorities || []).map((priority) => (
                          <SelectItem
                            key={priority.id}
                            value={String(priority.id)}
                          >
                            {priority.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name='assigneeId'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Assignee</FormLabel>
                    <Select
                      onValueChange={field.onChange}
                      value={field.value || undefined}
                      disabled={isUserLoading || assigneeOptions.length === 0}
                    >
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder='Unassigned' />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {assigneeOptions.map((option) => (
                          <SelectItem key={option.id} value={option.id}>
                            {option.label}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name='parentId'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Parent</FormLabel>
                    <FormControl>
                      <Combobox
                        value={field.value || undefined}
                        onChange={(value) =>
                          field.onChange(value ? String(value) : '')
                        }
                        items={parentItems}
                        placeholder='Select parent issue'
                        emptyText='No work items found'
                        loading={isParentFetching}
                        onSearch={setParentSearch}
                        disabled={!selectedProjectId}
                      />
                    </FormControl>
                    <FormDescription>
                      Useful for sub-task or child issue creation.
                    </FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name='securityLevelId'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Security level</FormLabel>
                    <Select
                      onValueChange={field.onChange}
                      value={field.value || undefined}
                      disabled={!meta || meta.securityLevels.length === 0}
                    >
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder='Default visibility' />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {(meta?.securityLevels || []).map((level) => (
                          <SelectItem key={level.id} value={String(level.id)}>
                            {level.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name='dueDate'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Due date</FormLabel>
                    <FormControl>
                      <Input type='date' {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name='timeOriginalEstimate'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Original estimate</FormLabel>
                    <FormControl>
                      <Input
                        type='number'
                        min='0'
                        step='1'
                        placeholder='Minutes'
                        {...field}
                      />
                    </FormControl>
                    <FormDescription>
                      Send value in minutes to backend.
                    </FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            {meta?.customFields.length ? (
              <Alert>
                <AlertTitle>Custom fields detected</AlertTitle>
                <AlertDescription>
                  Current frontend sends backend default values for supported
                  custom fields:{' '}
                  {meta.customFields.map((field) => field.name).join(', ')}.
                </AlertDescription>
              </Alert>
            ) : null}

            <DialogFooter className='gap-2 sm:gap-0'>
              <Button
                type='button'
                variant='outline'
                onClick={() => onOpenChange(false)}
              >
                Cancel
              </Button>
              <Button
                type='submit'
                disabled={
                  form.formState.isSubmitting ||
                  !meta ||
                  isMetaFetching ||
                  !meta.createAllowed
                }
              >
                {form.formState.isSubmitting ? (
                  <span className='flex items-center gap-2'>
                    <Loader2 className='h-4 w-4 animate-spin' />
                    Creating...
                  </span>
                ) : (
                  'Create work item'
                )}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}
