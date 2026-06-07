/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project component form dialog
 */

'use client';

import { useEffect } from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import { Loader2 } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import {
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Form,
  FormControl,
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
import type {
  PMProjectComponentApi,
  PMProjectComponentAssigneeType,
} from '../../types/api';

const NO_LEAD_VALUE = '__none__';

export const ASSIGNEE_LABELS: Record<PMProjectComponentAssigneeType, string> = {
  PROJECT_DEFAULT: 'Project default',
  COMPONENT_LEAD: 'Component lead',
  PROJECT_LEAD: 'Project lead',
  UNASSIGNED: 'Unassigned',
};

const componentFormSchema = z.object({
  name: z
    .string()
    .trim()
    .min(1, 'Name is required')
    .max(255, 'Name must be 255 characters or fewer'),
  description: z
    .string()
    .max(2000, 'Description must be 2000 characters or fewer')
    .optional()
    .or(z.literal('')),
  leadUserId: z.string().optional(),
  assigneeType: z.enum([
    'PROJECT_DEFAULT',
    'COMPONENT_LEAD',
    'PROJECT_LEAD',
    'UNASSIGNED',
  ]),
});

export type ComponentFormValues = z.infer<typeof componentFormSchema>;

interface PMProjectComponentDialogProps {
  open: boolean;
  component: PMProjectComponentApi | null;
  users: Array<{ value: string; label: string }>;
  saving: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (values: ComponentFormValues) => Promise<void>;
}

export function PMProjectComponentDialog({
  open,
  component,
  users,
  saving,
  onOpenChange,
  onSubmit,
}: PMProjectComponentDialogProps) {
  const form = useForm<ComponentFormValues>({
    resolver: zodResolver(componentFormSchema),
    defaultValues: getDefaultValues(component),
  });

  useEffect(() => {
    if (open) {
      form.reset(getDefaultValues(component));
    }
  }, [component, form, open]);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-xl'>
        <DialogHeader>
          <DialogTitle>
            {component ? 'Edit project component' : 'Create project component'}
          </DialogTitle>
          <DialogDescription>
            Required fields are marked with an asterisk.
          </DialogDescription>
        </DialogHeader>

        <Form {...form}>
          <form className='space-y-5' onSubmit={form.handleSubmit(onSubmit)}>
            <FormField
              control={form.control}
              name='name'
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Name *</FormLabel>
                  <FormControl>
                    <Input autoFocus {...field} />
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
                    <Textarea rows={3} {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name='leadUserId'
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Component lead</FormLabel>
                  <Select
                    value={field.value || NO_LEAD_VALUE}
                    onValueChange={field.onChange}
                  >
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder='Select person' />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      <SelectItem value={NO_LEAD_VALUE}>
                        Select person
                      </SelectItem>
                      {users.map((user) => (
                        <SelectItem key={user.value} value={user.value}>
                          {user.label}
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
              name='assigneeType'
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Default assignee</FormLabel>
                  <Select value={field.value} onValueChange={field.onChange}>
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {Object.entries(ASSIGNEE_LABELS).map(([value, label]) => (
                        <SelectItem key={value} value={value}>
                          {label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />

            <DialogFooter>
              <Button
                type='button'
                variant='outline'
                onClick={() => onOpenChange(false)}
                disabled={saving}
              >
                Cancel
              </Button>
              <Button type='submit' disabled={saving}>
                {saving ? (
                  <span className='flex items-center gap-2'>
                    <Loader2 className='h-4 w-4 animate-spin' />
                    Saving...
                  </span>
                ) : (
                  'Save'
                )}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}

export function getAssigneeLabel(value: string): string {
  return ASSIGNEE_LABELS[normalizeAssigneeType(value) || 'PROJECT_DEFAULT'];
}

export function toComponentLeadUserId(value?: string): number | null {
  return value && value !== NO_LEAD_VALUE ? Number(value) : null;
}

function getDefaultValues(
  component: PMProjectComponentApi | null
): ComponentFormValues {
  return {
    name: component?.name || '',
    description: component?.description || '',
    leadUserId: component?.leadUserId
      ? String(component.leadUserId)
      : NO_LEAD_VALUE,
    assigneeType:
      normalizeAssigneeType(component?.assigneeType) || 'PROJECT_DEFAULT',
  };
}

function normalizeAssigneeType(
  value?: string | null
): PMProjectComponentAssigneeType | null {
  if (
    value === 'PROJECT_DEFAULT' ||
    value === 'COMPONENT_LEAD' ||
    value === 'PROJECT_LEAD' ||
    value === 'UNASSIGNED'
  ) {
    return value;
  }
  return null;
}
