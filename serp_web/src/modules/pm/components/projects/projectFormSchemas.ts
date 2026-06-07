/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project form schemas
 */

import { z } from 'zod';

const dateRangeSchema = <T extends z.ZodRawShape>(shape: T) =>
  z.object(shape).superRefine((value, context) => {
    const dates = value as {
      startDate?: string;
      targetDate?: string;
    };

    if (
      dates.startDate &&
      dates.targetDate &&
      dates.targetDate < dates.startDate
    ) {
      context.addIssue({
        code: z.ZodIssueCode.custom,
        message: 'Target date must be on or after the start date.',
        path: ['targetDate'],
      });
    }
  });

const projectTextFields = {
  name: z
    .string()
    .trim()
    .min(1, 'Project name is required')
    .max(120, 'Project name is too long'),
  description: z
    .string()
    .trim()
    .max(500, 'Description is too long')
    .optional()
    .or(z.literal('')),
};

export const createProjectSchema = dateRangeSchema({
  templateType: z
    .enum(['BLANK', 'KANBAN', 'SCRUM'])
    .nullable()
    .refine((value) => value !== null, {
      message: 'Choose one of the available templates.',
    }),
  ...projectTextFields,
  key: z
    .string()
    .trim()
    .min(2, 'Project key is required')
    .max(10, 'Project key must be 10 characters or fewer')
    .regex(
      /^[A-Z][A-Z0-9]{1,9}$/,
      'Use 2-10 uppercase letters or numbers, starting with a letter'
    ),
  leadId: z.string().min(1, 'Project lead is required'),
  categoryId: z.string().optional().or(z.literal('')),
  visibility: z.enum(['PRIVATE', 'TEAM', 'ORGANIZATION']),
  startDate: z.string().optional().or(z.literal('')),
  targetDate: z.string().optional().or(z.literal('')),
});

export const editProjectSchema = dateRangeSchema({
  ...projectTextFields,
  key: z
    .string()
    .trim()
    .min(2, 'Project key is required')
    .max(12, 'Project key must be 12 characters or fewer')
    .regex(
      /^[A-Z0-9]+(?:-[A-Z0-9]+)*$/,
      'Use uppercase letters, numbers, and hyphens only'
    ),
  leadId: z.string().min(1, 'Project lead is required'),
  category: z.string().min(1, 'Category is required'),
  visibility: z.enum(['PRIVATE', 'TEAM', 'ORGANIZATION']),
  startDate: z.string().min(1, 'Start date is required'),
  targetDate: z.string().min(1, 'Target date is required'),
  status: z.enum(['ACTIVE', 'COMPLETED', 'ARCHIVED']),
});

export type PMProjectCreateFormValues = z.infer<typeof createProjectSchema>;
export type PMProjectEditFormValues = z.infer<typeof editProjectSchema>;
