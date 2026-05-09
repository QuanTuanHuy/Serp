/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project create form
 */

'use client';

import { useEffect, useMemo, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { CalendarDays, FolderKanban, Shield, UserRound } from 'lucide-react';
import { z } from 'zod';
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
import type { PMProjectVisibility } from '../../types/project-create.types';
import {
  PM_PROJECT_TEMPLATE_OPTIONS,
  PM_PROJECT_VISIBILITY_OPTIONS,
  generatePMProjectKey,
  getPMProjectTemplateDefinition,
  normalizePMProjectKey,
} from '../../utils/projectForm';
import { PMProjectTemplateBadge } from './PMProjectTemplateBadge';
import { PMProjectTemplatePicker } from './PMProjectTemplatePicker';

const createProjectSchema = z
  .object({
    templateType: z
      .enum(['BLANK', 'KANBAN', 'SCRUM'])
      .nullable()
      .refine((value) => value !== null, {
        message: 'Choose one of the available templates.',
      }),
    name: z
      .string()
      .trim()
      .min(1, 'Project name is required')
      .max(120, 'Project name is too long'),
    key: z
      .string()
      .trim()
      .min(2, 'Project key is required')
      .max(12, 'Project key must be 12 characters or fewer')
      .regex(
        /^[A-Z0-9]+(?:-[A-Z0-9]+)*$/,
        'Use uppercase letters, numbers, and hyphens only'
      ),
    description: z
      .string()
      .trim()
      .max(500, 'Description is too long')
      .optional()
      .or(z.literal('')),
    leadId: z.string().min(1, 'Project lead is required'),
    category: z.string().min(1, 'Category is required'),
    visibility: z.enum(['PRIVATE', 'TEAM', 'ORGANIZATION']),
    startDate: z.string().min(1, 'Start date is required'),
    targetDate: z.string().min(1, 'Target date is required'),
  })
  .superRefine((value, context) => {
    if (
      value.startDate &&
      value.targetDate &&
      value.targetDate < value.startDate
    ) {
      context.addIssue({
        code: z.ZodIssueCode.custom,
        message: 'Target date must be on or after the start date.',
        path: ['targetDate'],
      });
    }
  });

export type PMProjectCreateFormValues = z.infer<typeof createProjectSchema>;

interface PMProjectCreateFormProps {
  onSubmit: (values: PMProjectCreateFormValues) => Promise<void> | void;
  onCancel: () => void;
}

export function PMProjectCreateForm({
  onSubmit,
  onCancel,
}: PMProjectCreateFormProps) {
  const form = useForm<PMProjectCreateFormValues>({
    resolver: zodResolver(createProjectSchema),
    defaultValues: {
      templateType: null,
      name: '',
      key: '',
      description: '',
      leadId: '',
      category: '',
      visibility: 'TEAM',
      startDate: '',
      targetDate: '',
    },
  });

  const [hasManualKeyEdit, setHasManualKeyEdit] = useState(false);

  const projectName = form.watch('name');
  const selectedTemplateType = form.watch('templateType');
  const selectedLeadId = form.watch('leadId');
  const visibility = form.watch('visibility');
  const startDate = form.watch('startDate');
  const targetDate = form.watch('targetDate');
  const generatedKey = useMemo(
    () => generatePMProjectKey(projectName),
    [projectName]
  );

  const categoryOptions = useMemo(
    () => [
      ...new Set(PM_PROJECT_LIST_MOCKS.map((project) => project.category)),
    ],
    []
  );

  const leadOptions = useMemo(
    () =>
      PM_PROJECT_LIST_MOCKS.map((project) => project.lead).filter(
        (lead, index, array) =>
          array.findIndex((candidate) => candidate.id === lead.id) === index
      ),
    []
  );

  const selectedTemplate = getPMProjectTemplateDefinition(selectedTemplateType);

  const selectedLead =
    leadOptions.find((lead) => lead.id === selectedLeadId) || null;

  const selectedVisibility =
    PM_PROJECT_VISIBILITY_OPTIONS.find(
      (option) => option.value === visibility
    ) || PM_PROJECT_VISIBILITY_OPTIONS[1];

  useEffect(() => {
    if (!hasManualKeyEdit) {
      form.setValue('key', generatedKey, {
        shouldDirty: false,
        shouldValidate: form.formState.isSubmitted,
      });
    }
  }, [form, generatedKey, hasManualKeyEdit]);

  const handleKeyChange = (value: string) => {
    setHasManualKeyEdit(true);
    form.setValue('key', normalizePMProjectKey(value), {
      shouldDirty: true,
      shouldValidate: form.formState.isSubmitted,
    });
  };

  const handleUseGeneratedKey = () => {
    setHasManualKeyEdit(false);
    form.setValue('key', generatedKey, {
      shouldDirty: true,
      shouldValidate: true,
    });
  };

  const handleSubmit = async (values: PMProjectCreateFormValues) => {
    await onSubmit({
      ...values,
      key: normalizePMProjectKey(values.key),
    });
  };

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(handleSubmit)} className='space-y-6'>
        <div className='grid gap-6 xl:grid-cols-[minmax(0,0.95fr)_minmax(0,1.05fr)]'>
          <Card className='xl:sticky xl:top-6'>
            <CardHeader>
              <CardTitle>Template picker</CardTitle>
              <CardDescription>
                Choose the internal software template that should seed the
                project workflow.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <FormField
                control={form.control}
                name='templateType'
                render={({ field, fieldState }) => (
                  <FormItem>
                    <FormControl>
                      <PMProjectTemplatePicker
                        value={field.value}
                        onValueChange={field.onChange}
                        options={PM_PROJECT_TEMPLATE_OPTIONS}
                        error={fieldState.error?.message}
                      />
                    </FormControl>
                  </FormItem>
                )}
              />
            </CardContent>
          </Card>

          <div className='space-y-6'>
            <Card>
              <CardHeader>
                <CardTitle>Project details</CardTitle>
                <CardDescription>
                  Configure the core metadata before the workspace is created.
                </CardDescription>
              </CardHeader>
              <CardContent className='space-y-6'>
                <div className='grid gap-6 md:grid-cols-2'>
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
                          Used across the project list, summary header, and
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
                            onChange={(event) =>
                              handleKeyChange(event.target.value)
                            }
                          />
                        </FormControl>
                        <FormDescription>
                          Auto-generated from the project name and still
                          editable before create.
                        </FormDescription>
                        <FormMessage />
                      </FormItem>
                    )}
                  />

                  <div className='space-y-2'>
                    <div className='rounded-xl border border-dashed bg-muted/30 p-4'>
                      <p className='text-sm font-medium'>Suggested key</p>
                      <p className='mt-1 text-lg font-semibold tracking-wide'>
                        {generatedKey || 'Waiting for project name'}
                      </p>
                      <p className='mt-2 text-sm text-muted-foreground'>
                        Keep keys short and stable so they stay usable in board,
                        ticket, and backlog references.
                      </p>
                    </div>
                    <Button
                      type='button'
                      variant='outline'
                      className='w-full'
                      onClick={handleUseGeneratedKey}
                      disabled={!generatedKey}
                    >
                      Use generated key
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
                        <FormDescription>
                          Keep this concise so it reads cleanly in the projects
                          table and detail header.
                        </FormDescription>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                </div>

                <div className='grid gap-6 md:grid-cols-2'>
                  <FormField
                    control={form.control}
                    name='leadId'
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Project lead</FormLabel>
                        <Select
                          onValueChange={field.onChange}
                          value={field.value || undefined}
                        >
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
                    name='category'
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Category</FormLabel>
                        <Select
                          onValueChange={field.onChange}
                          value={field.value || undefined}
                        >
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

                  <FormField
                    control={form.control}
                    name='visibility'
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Visibility</FormLabel>
                        <Select
                          onValueChange={field.onChange}
                          value={field.value}
                        >
                          <FormControl>
                            <SelectTrigger>
                              <SelectValue placeholder='Select visibility' />
                            </SelectTrigger>
                          </FormControl>
                          <SelectContent>
                            {PM_PROJECT_VISIBILITY_OPTIONS.map((option) => (
                              <SelectItem
                                key={option.value}
                                value={option.value}
                              >
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

                  <div className='grid gap-6 sm:grid-cols-2 md:col-span-2'>
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
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Review preset summary</CardTitle>
                <CardDescription>
                  Confirm the workflow defaults that will be seeded from the
                  selected template.
                </CardDescription>
              </CardHeader>
              <CardContent className='space-y-5'>
                {selectedTemplate ? (
                  <>
                    <div className='flex flex-wrap items-center gap-3'>
                      <PMProjectTemplateBadge
                        templateType={selectedTemplate.type}
                      />
                      <div>
                        <p className='font-semibold'>
                          {selectedTemplate.title}
                        </p>
                        <p className='text-sm text-muted-foreground'>
                          {selectedTemplate.description}
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
                          {selectedTemplate.boardBehavior}
                        </p>
                      </div>

                      <div className='rounded-xl border bg-muted/25 p-4'>
                        <div className='flex items-center gap-2 text-sm font-medium'>
                          <UserRound className='h-4 w-4 text-primary' />
                          Ownership
                        </div>
                        <p className='mt-2 text-sm text-muted-foreground'>
                          {selectedLead?.name ||
                            'Lead will appear here once selected.'}
                        </p>
                      </div>

                      <div className='rounded-xl border bg-muted/25 p-4'>
                        <div className='flex items-center gap-2 text-sm font-medium'>
                          <CalendarDays className='h-4 w-4 text-primary' />
                          Delivery window
                        </div>
                        <p className='mt-2 text-sm text-muted-foreground'>
                          {startDate && targetDate
                            ? `${startDate} to ${targetDate}`
                            : 'Start and target dates will appear here.'}
                        </p>
                      </div>
                    </div>

                    <div className='rounded-xl border bg-muted/20 p-4'>
                      <p className='text-sm font-medium'>Workflow preset</p>
                      <p className='mt-2 text-sm text-muted-foreground'>
                        {selectedTemplate.workflowSummary}
                      </p>
                      <ul className='mt-3 space-y-2 text-sm text-muted-foreground'>
                        {selectedTemplate.presetSummary.map((item) => (
                          <li key={item} className='flex items-start gap-2'>
                            <span className='mt-1 h-1.5 w-1.5 shrink-0 rounded-full bg-primary/70' />
                            <span>{item}</span>
                          </li>
                        ))}
                      </ul>
                    </div>
                  </>
                ) : (
                  <Alert>
                    <Shield className='h-4 w-4' />
                    <AlertTitle>Select a template first</AlertTitle>
                    <AlertDescription>
                      The create summary will update once Blank, Kanban, or
                      Scrum has been chosen.
                    </AlertDescription>
                  </Alert>
                )}

                <Alert>
                  <Shield className='h-4 w-4' />
                  <AlertTitle>Current implementation note</AlertTitle>
                  <AlertDescription>
                    This flow is wired to the new PM route structure and uses a
                    local redirect on submit until the PM create API is
                    integrated.
                  </AlertDescription>
                </Alert>
              </CardContent>
            </Card>

            <div className='flex flex-col-reverse gap-3 sm:flex-row sm:justify-end'>
              <Button type='button' variant='outline' onClick={onCancel}>
                Cancel
              </Button>
              <Button type='submit' disabled={form.formState.isSubmitting}>
                {form.formState.isSubmitting
                  ? 'Creating project...'
                  : 'Create project'}
              </Button>
            </div>
          </div>
        </div>
      </form>
    </Form>
  );
}
