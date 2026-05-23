/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project edit form
 */

'use client';

import { useEffect, useMemo, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  CalendarDays,
  FolderKanban,
  Lock,
  Shield,
  TriangleAlert,
  UserRound,
} from 'lucide-react';
import {
  Alert,
  AlertDescription,
  AlertTitle,
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
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
import { PM_PROJECT_LIST_MOCKS } from '../../mocks/projectList';
import type { PMProjectDetail } from '../../types/project-detail.types';
import type { PMProjectStatus } from '../../types/project-list.types';
import {
  PM_PROJECT_VISIBILITY_OPTIONS,
  getPMProjectTemplateDefinition,
  normalizePMProjectKey,
} from '../../utils/projectForm';
import { PMProjectTemplateBadge } from './PMProjectTemplateBadge';
import {
  editProjectSchema,
  type PMProjectEditFormValues,
} from './projectFormSchemas';

const EDITABLE_STATUS_OPTIONS: { value: PMProjectStatus; label: string }[] = [
  { value: 'ACTIVE', label: 'Active' },
  { value: 'COMPLETED', label: 'Completed' },
  { value: 'ARCHIVED', label: 'Archived' },
];

export type { PMProjectEditFormValues };

interface PMProjectEditFormProps {
  project: PMProjectDetail;
  onSubmit: (values: PMProjectEditFormValues) => Promise<void> | void;
  onCancel: () => void;
  onDirtyChange?: (isDirty: boolean) => void;
}

function getStatusLabel(status: PMProjectStatus) {
  return (
    EDITABLE_STATUS_OPTIONS.find((option) => option.value === status)?.label ||
    status
  );
}

export function PMProjectEditForm({
  project,
  onSubmit,
  onCancel,
  onDirtyChange,
}: PMProjectEditFormProps) {
  const form = useForm<PMProjectEditFormValues>({
    resolver: zodResolver(editProjectSchema),
    defaultValues: {
      name: project.name,
      key: project.key,
      description: project.description,
      leadId: project.lead.id,
      category: project.category,
      visibility: project.visibility,
      startDate: project.startDate,
      targetDate: project.targetDate,
      status: project.status,
    },
  });

  const [hasManualKeyEdit, setHasManualKeyEdit] = useState(false);

  const projectName = form.watch('name');
  const selectedLeadId = form.watch('leadId');
  const visibility = form.watch('visibility');
  const startDate = form.watch('startDate');
  const targetDate = form.watch('targetDate');
  const status = form.watch('status');

  const categoryOptions = useMemo(
    () => [...new Set(PM_PROJECT_LIST_MOCKS.map((item) => item.category))],
    []
  );

  const leadOptions = useMemo(
    () =>
      PM_PROJECT_LIST_MOCKS.map((item) => item.lead).filter(
        (lead, index, array) =>
          array.findIndex((candidate) => candidate.id === lead.id) === index
      ),
    []
  );

  const selectedLead =
    leadOptions.find((lead) => lead.id === selectedLeadId) || project.lead;
  const selectedVisibility =
    PM_PROJECT_VISIBILITY_OPTIONS.find(
      (option) => option.value === visibility
    ) || PM_PROJECT_VISIBILITY_OPTIONS[1];
  const templateDefinition = getPMProjectTemplateDefinition(
    project.templateType
  );

  useEffect(() => {
    onDirtyChange?.(form.formState.isDirty);
  }, [form.formState.isDirty, onDirtyChange]);

  useEffect(() => {
    const handleBeforeUnload = (event: BeforeUnloadEvent) => {
      if (!form.formState.isDirty || form.formState.isSubmitting) {
        return;
      }

      event.preventDefault();
      event.returnValue = '';
    };

    window.addEventListener('beforeunload', handleBeforeUnload);

    return () => {
      window.removeEventListener('beforeunload', handleBeforeUnload);
    };
  }, [form.formState.isDirty, form.formState.isSubmitting]);

  const handleKeyChange = (value: string) => {
    setHasManualKeyEdit(true);
    form.setValue('key', normalizePMProjectKey(value), {
      shouldDirty: true,
      shouldValidate: form.formState.isSubmitted,
    });
  };

  const handleResetKey = () => {
    setHasManualKeyEdit(false);
    form.setValue('key', project.key, {
      shouldDirty: true,
      shouldValidate: true,
    });
  };

  const handleArchiveProject = () => {
    if (typeof window === 'undefined') {
      return;
    }

    if (!window.confirm('Archive this project and mark it as archived?')) {
      return;
    }

    form.setValue('status', 'ARCHIVED', {
      shouldDirty: true,
      shouldValidate: true,
    });
  };

  const handleSubmit = async (values: PMProjectEditFormValues) => {
    await onSubmit({
      ...values,
      key: normalizePMProjectKey(values.key),
    });
  };

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(handleSubmit)} className='space-y-6'>
        <Card>
          <CardHeader>
            <CardTitle>Basic info</CardTitle>
            <CardDescription>
              Update the core metadata used across PM project surfaces.
            </CardDescription>
          </CardHeader>
          <CardContent className='grid gap-6 md:grid-cols-2'>
            <FormField
              control={form.control}
              name='name'
              render={({ field }) => (
                <FormItem className='md:col-span-2'>
                  <FormLabel>Project name</FormLabel>
                  <FormControl>
                    <Input
                      placeholder='e.g. Digital Onboarding Hub'
                      {...field}
                    />
                  </FormControl>
                  <FormDescription>
                    This title appears in the projects list, detail header, and
                    future board surfaces.
                  </FormDescription>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name='key'
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Project key</FormLabel>
                  <FormControl>
                    <Input
                      placeholder='DOH'
                      {...field}
                      onChange={(event) => handleKeyChange(event.target.value)}
                    />
                  </FormControl>
                  <FormDescription>
                    Keep the project key stable because it becomes part of board
                    and issue references.
                  </FormDescription>
                  <FormMessage />
                </FormItem>
              )}
            />

            <div className='space-y-2'>
              <div className='rounded-xl border border-dashed bg-muted/30 p-4'>
                <p className='text-sm font-medium'>Original key</p>
                <p className='mt-1 text-lg font-semibold tracking-wide'>
                  {project.key}
                </p>
                <p className='mt-2 text-sm text-muted-foreground'>
                  Reset if the edited key drifts too far from the current naming
                  convention.
                </p>
              </div>
              <Button
                type='button'
                variant='outline'
                className='w-full'
                onClick={handleResetKey}
                disabled={!hasManualKeyEdit}
              >
                Reset to original key
              </Button>
            </div>

            <FormField
              control={form.control}
              name='description'
              render={({ field }) => (
                <FormItem className='md:col-span-2'>
                  <FormLabel>Description</FormLabel>
                  <FormControl>
                    <Textarea
                      rows={4}
                      placeholder='Summarize the software scope, delivery objective, or ownership context.'
                      {...field}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name='category'
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Category</FormLabel>
                  <Select onValueChange={field.onChange} value={field.value}>
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder='Select a category' />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {categoryOptions.map((category) => (
                        <SelectItem key={category} value={category}>
                          {category}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />

            <div className='rounded-xl border bg-muted/20 p-4'>
              <p className='text-sm font-medium'>Project identity</p>
              <p className='mt-2 text-sm text-muted-foreground'>
                The project mark is currently derived from the project name and
                template until a dedicated icon or color API is available.
              </p>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Ownership and access</CardTitle>
            <CardDescription>
              Manage who owns the project and how broadly the workspace is
              exposed.
            </CardDescription>
          </CardHeader>
          <CardContent className='grid gap-6 md:grid-cols-2'>
            <FormField
              control={form.control}
              name='leadId'
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Project lead</FormLabel>
                  <Select onValueChange={field.onChange} value={field.value}>
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder='Select a project lead' />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {leadOptions.map((lead) => (
                        <SelectItem key={lead.id} value={lead.id}>
                          {lead.name}
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
              name='visibility'
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Visibility</FormLabel>
                  <Select onValueChange={field.onChange} value={field.value}>
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder='Select visibility' />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {PM_PROJECT_VISIBILITY_OPTIONS.map((option) => (
                        <SelectItem key={option.value} value={option.value}>
                          {option.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormDescription>
                    {selectedVisibility.description}
                  </FormDescription>
                  <FormMessage />
                </FormItem>
              )}
            />

            <div className='rounded-xl border bg-muted/20 p-4 md:col-span-2'>
              <div className='flex items-center gap-2 text-sm font-medium'>
                <UserRound className='h-4 w-4 text-primary' />
                Ownership summary
              </div>
              <p className='mt-2 text-sm text-muted-foreground'>
                {selectedLead.name} owns delivery coordination for this project.
                Access is currently set to{' '}
                {selectedVisibility.label.toLowerCase()}.
              </p>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Delivery settings</CardTitle>
            <CardDescription>
              Update the project schedule and lifecycle state.
            </CardDescription>
          </CardHeader>
          <CardContent className='grid gap-6 md:grid-cols-2'>
            <FormField
              control={form.control}
              name='startDate'
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Start date</FormLabel>
                  <FormControl>
                    <Input type='date' {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name='targetDate'
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Target date</FormLabel>
                  <FormControl>
                    <Input type='date' {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name='status'
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Status</FormLabel>
                  <Select onValueChange={field.onChange} value={field.value}>
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder='Select status' />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {EDITABLE_STATUS_OPTIONS.map((option) => (
                        <SelectItem key={option.value} value={option.value}>
                          {option.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />

            <div className='rounded-xl border bg-muted/20 p-4'>
              <div className='flex items-center gap-2 text-sm font-medium'>
                <CalendarDays className='h-4 w-4 text-primary' />
                Delivery summary
              </div>
              <p className='mt-2 text-sm text-muted-foreground'>
                {startDate && targetDate
                  ? `${startDate} to ${targetDate}`
                  : 'Start and target dates will appear here.'}
              </p>
              <p className='mt-1 text-sm text-muted-foreground'>
                Current lifecycle: {getStatusLabel(status)}.
              </p>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Template origin summary</CardTitle>
            <CardDescription>
              The original template is fixed after project creation and is shown
              here as a reference only.
            </CardDescription>
          </CardHeader>
          <CardContent className='space-y-5'>
            {templateDefinition ? (
              <>
                <div className='flex flex-wrap items-center gap-3'>
                  <PMProjectTemplateBadge
                    templateType={templateDefinition.type}
                  />
                  <div>
                    <p className='font-semibold'>{templateDefinition.title}</p>
                    <p className='text-sm text-muted-foreground'>
                      {templateDefinition.description}
                    </p>
                  </div>
                </div>

                <div className='grid gap-4 md:grid-cols-3'>
                  <div className='rounded-xl border bg-muted/25 p-4'>
                    <div className='flex items-center gap-2 text-sm font-medium'>
                      <FolderKanban className='h-4 w-4 text-primary' />
                      Board behavior
                    </div>
                    <p className='mt-2 text-sm text-muted-foreground'>
                      {templateDefinition.boardBehavior}
                    </p>
                  </div>

                  <div className='rounded-xl border bg-muted/25 p-4'>
                    <div className='flex items-center gap-2 text-sm font-medium'>
                      <Lock className='h-4 w-4 text-primary' />
                      Template origin
                    </div>
                    <p className='mt-2 text-sm text-muted-foreground'>
                      Template identity is locked after create. Adjust behavior
                      through project settings, not by switching the origin.
                    </p>
                  </div>

                  <div className='rounded-xl border bg-muted/25 p-4'>
                    <div className='flex items-center gap-2 text-sm font-medium'>
                      <Shield className='h-4 w-4 text-primary' />
                      Workflow preset
                    </div>
                    <p className='mt-2 text-sm text-muted-foreground'>
                      {templateDefinition.workflowSummary}
                    </p>
                  </div>
                </div>

                <div className='rounded-xl border bg-muted/20 p-4'>
                  <p className='text-sm font-medium'>Preset summary</p>
                  <ul className='mt-3 space-y-2 text-sm text-muted-foreground'>
                    {templateDefinition.presetSummary.map((item) => (
                      <li key={item} className='flex items-start gap-2'>
                        <span className='mt-1 h-1.5 w-1.5 shrink-0 rounded-full bg-primary/70' />
                        <span>{item}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              </>
            ) : null}
          </CardContent>
        </Card>

        <Card className='border-destructive/25'>
          <CardHeader>
            <CardTitle>Danger zone</CardTitle>
            <CardDescription>
              Use destructive actions carefully. These flows are currently wired
              as frontend-only placeholders until the PM API is connected.
            </CardDescription>
          </CardHeader>
          <CardContent className='space-y-4'>
            <Alert>
              <TriangleAlert className='h-4 w-4' />
              <AlertTitle>Unsaved changes warning</AlertTitle>
              <AlertDescription>
                Closing the browser tab while the form is dirty will trigger a
                native warning before the page unloads.
              </AlertDescription>
            </Alert>

            <div className='flex flex-col gap-3 sm:flex-row'>
              <Button
                type='button'
                variant='outline'
                onClick={handleArchiveProject}
              >
                Archive project
              </Button>
              <Button type='button' variant='destructive' disabled>
                Delete project
              </Button>
            </div>
          </CardContent>
        </Card>

        <div className='flex flex-col-reverse gap-3 sm:flex-row sm:justify-end'>
          <Button type='button' variant='outline' onClick={onCancel}>
            Cancel
          </Button>
          <Button type='submit' disabled={form.formState.isSubmitting}>
            {form.formState.isSubmitting ? 'Saving changes...' : 'Save changes'}
          </Button>
        </div>
      </form>
    </Form>
  );
}
