/**
 * Dialog to create a CRM meeting request (async scheduling queue).
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project
 */

'use client';

import { useEffect, useMemo, useState } from 'react';
import Link from 'next/link';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';
import { getErrorMessage } from '@/lib/store/api';
import {
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Input,
  Label,
  Textarea,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import { useGetMyOrganizationQuery } from '@/modules/settings/services/organizations/organizationsApi';
import { useGetOrganizationUsersQuery } from '@/modules/settings/services/users/usersApi';
import { useGetMyModulesQuery } from '@/modules/account/services/moduleApi';
import {
  useCreateMeetingRequestMutation,
  useGetCustomersQuery,
  useGetTeamsQuery,
} from '../../api/crmApi';
import type {
  CreateMeetingRequestPayload,
  MeetingRequestType,
} from '../../types';
import { MEETING_REQUEST_TYPE_LABELS } from '../../types/meetingRequest';

const MEETING_TYPES = Object.keys(
  MEETING_REQUEST_TYPE_LABELS
) as MeetingRequestType[];

function toDatetimeLocalValue(d: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function defaultTimeWindow(): {
  earliestLocal: string;
  latestLocal: string;
  deadlineLocal: string;
} {
  const now = new Date();
  const earliest = new Date(now);
  earliest.setDate(earliest.getDate() + 1);
  earliest.setHours(9, 0, 0, 0);
  const latest = new Date(earliest);
  latest.setDate(latest.getDate() + 4);
  latest.setHours(17, 0, 0, 0);
  const deadline = new Date(latest);
  deadline.setHours(23, 59, 0, 0);
  return {
    earliestLocal: toDatetimeLocalValue(earliest),
    latestLocal: toDatetimeLocalValue(latest),
    deadlineLocal: toDatetimeLocalValue(deadline),
  };
}

const formSchema = z
  .object({
    teamId: z.string().min(1, 'Select a team'),
    meetingType: z.enum(
      MEETING_TYPES as [MeetingRequestType, ...MeetingRequestType[]]
    ),
    subject: z.string().max(255).optional(),
    description: z.string().max(2000).optional(),
    location: z.string().max(255).optional(),
    preferredTimeSlot: z
      .union([z.literal('MORNING'), z.literal('AFTERNOON'), z.literal('')])
      .optional(),
    preferredUserId: z.string().optional(),
    durationMinutes: z.string().optional(),
    earliestLocal: z.string().min(1, 'Required'),
    latestLocal: z.string().min(1, 'Required'),
    deadlineLocal: z.string().min(1, 'Required'),
    pickedAccountId: z.string().optional(),
  })
  .superRefine((data, ctx) => {
    const earliest = new Date(data.earliestLocal).getTime();
    const latest = new Date(data.latestLocal).getTime();
    const deadline = new Date(data.deadlineLocal).getTime();
    if (earliest > latest) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: 'Latest start must be after earliest start',
        path: ['latestLocal'],
      });
    }
    if (deadline < earliest) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: 'Deadline must be on or after earliest start',
        path: ['deadlineLocal'],
      });
    }
    const dm = data.durationMinutes?.trim();
    if (dm) {
      const n = Number.parseInt(dm, 10);
      if (Number.isNaN(n) || n < 15) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: 'Duration must be at least 15 minutes',
          path: ['durationMinutes'],
        });
      }
    }
  });

type FormValues = z.infer<typeof formSchema>;

export interface RequestMeetingDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  /** Fixed account (detail pages). When omitted, user picks via search. */
  accountId?: string;
  accountName?: string;
  opportunityId?: string;
  contactId?: string;
  onSuccess?: () => void;
}

export function RequestMeetingDialog({
  open,
  onOpenChange,
  accountId: fixedAccountId,
  accountName,
  opportunityId,
  contactId,
  onSuccess,
}: RequestMeetingDialogProps) {
  const defaults = useMemo(() => defaultTimeWindow(), []);
  const [accountSearch, setAccountSearch] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');

  useEffect(() => {
    const t = window.setTimeout(
      () => setDebouncedSearch(accountSearch.trim()),
      350
    );
    return () => window.clearTimeout(t);
  }, [accountSearch]);

  const { data: teamsPayload } = useGetTeamsQuery({
    filters: { status: 'ACTIVE' },
    pagination: { page: 1, limit: 100 },
  });
  const teams = teamsPayload?.data?.items ?? [];

  const { data: organization } = useGetMyOrganizationQuery(undefined, {
    skip: !open,
  });
  const organizationId = organization?.id;
  const { data: myModules } = useGetMyModulesQuery(undefined, {
    skip: !open || !organizationId,
  });
  const crmModuleId = myModules?.find((m) => m.moduleCode === 'CRM')?.moduleId;

  const { data: orgUsersResponse, isLoading: orgUsersLoading } =
    useGetOrganizationUsersQuery(
      {
        organizationId: organizationId as number,
        page: 0,
        pageSize: 100,
        status: 'ACTIVE',
        moduleId: crmModuleId,
      },
      { skip: !open || !organizationId }
    );
  const orgUsers = orgUsersResponse?.data?.items ?? [];

  const formatOrgUserLabel = (u: (typeof orgUsers)[number]) => {
    const name = [u.firstName, u.lastName].filter(Boolean).join(' ').trim();
    return name || u.email || `User #${u.id}`;
  };

  const { data: customersResult } = useGetCustomersQuery(
    {
      filters: { search: debouncedSearch || undefined },
      pagination: { page: 1, limit: 20 },
    },
    {
      skip: !open || !!fixedAccountId || debouncedSearch.length < 2,
    }
  );
  const customerHits = customersResult?.data?.data ?? [];

  const [createMeetingRequest, { isLoading }] =
    useCreateMeetingRequestMutation();

  const form = useForm<FormValues>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      teamId: '',
      meetingType: 'DISCOVERY',
      subject: '',
      description: '',
      location: '',
      preferredTimeSlot: '',
      preferredUserId: '',
      durationMinutes: '',
      earliestLocal: defaults.earliestLocal,
      latestLocal: defaults.latestLocal,
      deadlineLocal: defaults.deadlineLocal,
      pickedAccountId: fixedAccountId ?? '',
    },
  });

  const pickedAccountId = form.watch('pickedAccountId');
  const teamIdValue = form.watch('teamId');

  useEffect(() => {
    if (!open) return;
    const d = defaultTimeWindow();
    form.reset({
      teamId: '',
      meetingType: 'DISCOVERY',
      subject: '',
      description: '',
      location: '',
      preferredTimeSlot: '',
      preferredUserId: '',
      durationMinutes: '',
      earliestLocal: d.earliestLocal,
      latestLocal: d.latestLocal,
      deadlineLocal: d.deadlineLocal,
      pickedAccountId: fixedAccountId ?? '',
    });
    setAccountSearch('');
    setDebouncedSearch('');
  }, [open, fixedAccountId, form]);

  const onSubmit = form.handleSubmit(async (values) => {
    const resolvedAccountId = fixedAccountId || values.pickedAccountId;
    if (!resolvedAccountId) {
      toast.error('Select an account');
      return;
    }

    const earliestStart = new Date(values.earliestLocal).getTime();
    const latestStart = new Date(values.latestLocal).getTime();
    const requestedDeadline = new Date(values.deadlineLocal).getTime();

    const payload: CreateMeetingRequestPayload = {
      teamId: Number(values.teamId),
      accountId: Number(resolvedAccountId),
      meetingType: values.meetingType,
      earliestStart,
      latestStart,
      requestedDeadline,
      subject: values.subject?.trim() || undefined,
      description: values.description?.trim() || undefined,
      location: values.location?.trim() || undefined,
      ...(values.preferredTimeSlot === 'MORNING' ||
      values.preferredTimeSlot === 'AFTERNOON'
        ? { preferredTimeSlot: values.preferredTimeSlot }
        : {}),
      ...(opportunityId ? { opportunityId: Number(opportunityId) } : {}),
      ...(contactId ? { contactId: Number(contactId) } : {}),
      ...(values.preferredUserId?.trim()
        ? { preferredUserId: Number.parseInt(values.preferredUserId, 10) }
        : {}),
      ...(values.durationMinutes?.trim()
        ? {
            durationMinutes: Number.parseInt(values.durationMinutes, 10),
          }
        : {}),
    };

    try {
      const res = await createMeetingRequest(payload).unwrap();
      if (!res.success) {
        toast.error(res.message || 'Could not create meeting request');
        return;
      }
      toast.success(
        res.message ||
          'Meeting request submitted — scheduling will assign a rep and time.'
      );
      onOpenChange(false);
      onSuccess?.();
    } catch (err) {
      toast.error(getErrorMessage(err) || 'Failed to create meeting request');
    }
  });

  const needsAccountPick = !fixedAccountId;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-h-[90vh] overflow-y-auto sm:max-w-[400px] !max-w-3xl'>
        <DialogHeader>
          <DialogTitle>Request a meeting</DialogTitle>
          <DialogDescription>
            Submit a scheduling request. When a slot is found, a{' '}
            <strong>Meeting</strong> activity is created and linked here.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={onSubmit} className='space-y-4'>
          {fixedAccountId ? (
            <div className='rounded-md border bg-muted/40 px-3 py-2 text-sm'>
              <span className='text-muted-foreground'>Account: </span>
              {accountName ? (
                <span className='font-medium'>{accountName}</span>
              ) : (
                <Link
                  href={`/crm/accounts/${fixedAccountId}`}
                  className='font-medium text-primary underline-offset-4 hover:underline'
                >
                  #{fixedAccountId}
                </Link>
              )}
            </div>
          ) : (
            <div className='space-y-2'>
              <Label>Account</Label>
              <Input
                placeholder='Search accounts (type at least 2 characters)'
                value={accountSearch}
                onChange={(e) => {
                  const v = e.target.value;
                  setAccountSearch(v);
                  if (pickedAccountId) {
                    form.setValue('pickedAccountId', '', {
                      shouldValidate: true,
                    });
                  }
                }}
              />
              {debouncedSearch.length >= 2 &&
                customerHits.length > 0 &&
                !pickedAccountId && (
                  <div className='max-h-36 overflow-auto rounded-md border'>
                    {customerHits.map((c) => (
                      <button
                        key={c.id}
                        type='button'
                        className='block w-full px-3 py-2 text-left text-sm hover:bg-muted'
                        onClick={() => {
                          form.setValue('pickedAccountId', String(c.id), {
                            shouldValidate: true,
                          });
                          setAccountSearch(c.name || `Account ${c.id}`);
                        }}
                      >
                        {c.name || `Account ${c.id}`}
                      </button>
                    ))}
                  </div>
                )}
              {form.formState.errors.pickedAccountId && (
                <p className='text-sm text-destructive'>
                  {form.formState.errors.pickedAccountId.message}
                </p>
              )}
              {needsAccountPick && !pickedAccountId && (
                <p className='text-xs text-muted-foreground'>
                  Pick an account from search results.
                </p>
              )}
            </div>
          )}

          <div className='space-y-2'>
            <Label htmlFor='mr-team'>Team</Label>
            <Select
              value={teamIdValue ? String(teamIdValue) : undefined}
              onValueChange={(v) =>
                form.setValue('teamId', v, { shouldValidate: true })
              }
            >
              <SelectTrigger id='mr-team' className='w-full'>
                <SelectValue placeholder='Select team' />
              </SelectTrigger>
              <SelectContent position='popper' className='max-h-60 z-[100]'>
                {teams.map((t) => (
                  <SelectItem key={String(t.id)} value={String(t.id)}>
                    {t.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {form.formState.errors.teamId && (
              <p className='text-sm text-destructive'>
                {form.formState.errors.teamId.message}
              </p>
            )}
          </div>

          <div className='space-y-2'>
            <Label>Meeting type</Label>
            <Select
              value={form.watch('meetingType')}
              onValueChange={(v) =>
                form.setValue('meetingType', v as MeetingRequestType, {
                  shouldValidate: true,
                })
              }
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {MEETING_TYPES.map((mt) => (
                  <SelectItem key={mt} value={mt}>
                    {MEETING_REQUEST_TYPE_LABELS[mt]}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className='space-y-2'>
            <Label htmlFor='mr-subject'>Subject (optional)</Label>
            <Input
              id='mr-subject'
              {...form.register('subject')}
              placeholder='e.g. Product demo follow-up'
            />
          </div>

          <div className='grid gap-3 sm:grid-cols-3'>
            <div className='space-y-2'>
              <Label htmlFor='mr-earliest'>Earliest start</Label>
              <Input
                id='mr-earliest'
                type='datetime-local'
                {...form.register('earliestLocal')}
              />
              {form.formState.errors.earliestLocal && (
                <p className='text-xs text-destructive'>
                  {form.formState.errors.earliestLocal.message}
                </p>
              )}
            </div>
            <div className='space-y-2'>
              <Label htmlFor='mr-latest'>Latest start</Label>
              <Input
                id='mr-latest'
                type='datetime-local'
                {...form.register('latestLocal')}
              />
              {form.formState.errors.latestLocal && (
                <p className='text-xs text-destructive'>
                  {form.formState.errors.latestLocal.message}
                </p>
              )}
            </div>
            <div className='space-y-2'>
              <Label htmlFor='mr-deadline'>Scheduling deadline</Label>
              <Input
                id='mr-deadline'
                type='datetime-local'
                {...form.register('deadlineLocal')}
              />
              {form.formState.errors.deadlineLocal && (
                <p className='text-xs text-destructive'>
                  {form.formState.errors.deadlineLocal.message}
                </p>
              )}
            </div>
          </div>

          <div className='space-y-2'>
            <Label htmlFor='mr-slot'>Preferred part of day</Label>
            <Select
              value={
                form.watch('preferredTimeSlot') &&
                form.watch('preferredTimeSlot') !== ''
                  ? form.watch('preferredTimeSlot')
                  : '_none'
              }
              onValueChange={(v) =>
                form.setValue(
                  'preferredTimeSlot',
                  v === '_none' ? '' : (v as 'MORNING' | 'AFTERNOON'),
                  { shouldValidate: true }
                )
              }
            >
              <SelectTrigger id='mr-slot'>
                <SelectValue placeholder='No preference' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='_none'>No preference</SelectItem>
                <SelectItem value='MORNING'>Morning (08–12)</SelectItem>
                <SelectItem value='AFTERNOON'>Afternoon (13–17)</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className='grid gap-3 sm:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='mr-duration'>Duration (min, optional)</Label>
              <Input
                id='mr-duration'
                type='number'
                min={15}
                step={5}
                {...form.register('durationMinutes')}
              />
              {form.formState.errors.durationMinutes && (
                <p className='text-xs text-destructive'>
                  {form.formState.errors.durationMinutes.message}
                </p>
              )}
            </div>
            <div className='space-y-2'>
              <Label htmlFor='mr-pref-user'>
                Preferred assignee (optional)
              </Label>
              <Select
                disabled={orgUsersLoading || !organizationId}
                value={
                  form.watch('preferredUserId')?.trim()
                    ? form.watch('preferredUserId')!
                    : '_none'
                }
                onValueChange={(v) =>
                  form.setValue('preferredUserId', v === '_none' ? '' : v, {
                    shouldValidate: true,
                  })
                }
              >
                <SelectTrigger id='mr-pref-user'>
                  <SelectValue
                    placeholder={
                      orgUsersLoading ? 'Loading users…' : 'No preference'
                    }
                  />
                </SelectTrigger>
                <SelectContent position='popper' className='max-h-60'>
                  <SelectItem value='_none'>No preference</SelectItem>
                  {orgUsers.map((u) => (
                    <SelectItem key={u.id} value={String(u.id)}>
                      {formatOrgUserLabel(u)}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {!organizationId && open && (
                <p className='text-xs text-muted-foreground'>
                  Organization context is required to list users.
                </p>
              )}
            </div>
          </div>

          <div className='space-y-2'>
            <Label htmlFor='mr-loc'>Location / link (optional)</Label>
            <Input
              id='mr-loc'
              {...form.register('location')}
              placeholder='Teams link or office'
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='mr-desc'>Description (optional)</Label>
            <Textarea id='mr-desc' rows={3} {...form.register('description')} />
          </div>

          <DialogFooter className='gap-2 sm:gap-0'>
            <Button
              type='button'
              variant='outline'
              onClick={() => onOpenChange(false)}
            >
              Cancel
            </Button>
            <Button type='submit' disabled={isLoading}>
              {isLoading ? 'Submitting…' : 'Submit request'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
