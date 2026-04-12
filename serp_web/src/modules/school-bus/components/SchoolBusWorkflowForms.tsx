'use client';

import * as React from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  useFieldArray,
  useForm,
} from 'react-hook-form';
import { z } from 'zod';
import {
  Button,
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
import { REQUEST_TYPE_OPTIONS, SHIFT_TYPE_OPTIONS } from '../constants';
import { schoolBusUi } from '../theme';
import type {
  SchoolBusAttendant,
  SchoolBusBus,
  SchoolBusDriver,
  SchoolBusPickupPoint,
  SchoolBusParent,
  SchoolBusRejectRequest,
  SchoolBusRoute,
  SchoolBusRouteAssignmentRequest,
  SchoolBusRouteUpsertRequest,
  SchoolBusSchool,
  SchoolBusStudent,
  SchoolBusTransportRequestDetail,
  SchoolBusTransportRequestUpsertRequest,
} from '../types';
import { OperationsMap } from './map/OperationsMap';
import { SchoolBusFormDialog } from './SchoolBusFormDialog';

const transportRequestSchema = z.object({
  parentProfileId: z.coerce.number().min(1, 'Parent is required'),
  schoolId: z.coerce.number().min(1, 'School is required'),
  requestType: z.string().min(1, 'Request type is required'),
  effectiveFrom: z.string().min(1, 'Effective from is required'),
  effectiveTo: z.string().optional(),
  notes: z.string().optional(),
  students: z
    .array(
      z.object({
        studentId: z.coerce.number().min(1, 'Student is required'),
        pickupPointId: z.string().optional(),
      })
    )
    .min(1, 'At least one student is required'),
  isActive: z.boolean().default(true),
});

const routeSchema = z.object({
  schoolId: z.coerce.number().min(1, 'School is required'),
  routeName: z.string().min(1, 'Route name is required'),
  serviceDate: z.string().min(1, 'Service date is required'),
  shiftType: z.string().min(1, 'Shift type is required'),
  planningNotes: z.string().optional(),
  isActive: z.boolean().default(true),
});

const rejectSchema = z.object({
  reason: z.string().min(1, 'Reason is required'),
});

const assignmentSchema = z.object({
  busId: z.coerce.number().min(1, 'Bus is required'),
  driverId: z.coerce.number().min(1, 'Driver is required'),
  attendantId: z.string().optional(),
  isActive: z.boolean().default(true),
});

type TransportRequestFormValues = z.infer<typeof transportRequestSchema>;
type RouteFormValues = z.infer<typeof routeSchema>;
type RejectFormValues = z.infer<typeof rejectSchema>;
type AssignmentFormValues = z.infer<typeof assignmentSchema>;

interface TransportRequestFormProps {
  initialData?: SchoolBusTransportRequestDetail | null;
  parents: SchoolBusParent[];
  schools: SchoolBusSchool[];
  students: SchoolBusStudent[];
  pickupPoints: SchoolBusPickupPoint[];
  onSubmit: (values: SchoolBusTransportRequestUpsertRequest) => Promise<void>;
  isLoading?: boolean;
  onCancel?: () => void;
  submitLabel?: string;
}

export function TransportRequestForm({
  initialData,
  parents,
  schools,
  students,
  pickupPoints,
  onSubmit,
  isLoading = false,
  onCancel,
  submitLabel = 'Save request',
}: TransportRequestFormProps) {
  const form = useForm<TransportRequestFormValues>({
    resolver: zodResolver(transportRequestSchema) as any,
    defaultValues: {
      parentProfileId:
        initialData?.request.parentProfileId ?? parents[0]?.id ?? 0,
      schoolId: initialData?.request.schoolId ?? schools[0]?.id ?? 0,
      requestType:
        initialData?.request.requestType || REQUEST_TYPE_OPTIONS[0].value,
      effectiveFrom: initialData?.request.effectiveFrom || '',
      effectiveTo: initialData?.request.effectiveTo || '',
      notes: initialData?.request.notes || '',
      students:
        initialData?.students.map((item) => ({
          studentId: item.studentId,
          pickupPointId: item.pickupPointId ? String(item.pickupPointId) : '',
        })) || [{ studentId: 0, pickupPointId: '' }],
      isActive: initialData?.request.isActive ?? true,
    },
  });

  React.useEffect(() => {
    form.reset({
      parentProfileId:
        initialData?.request.parentProfileId ?? parents[0]?.id ?? 0,
      schoolId: initialData?.request.schoolId ?? schools[0]?.id ?? 0,
      requestType:
        initialData?.request.requestType || REQUEST_TYPE_OPTIONS[0].value,
      effectiveFrom: initialData?.request.effectiveFrom || '',
      effectiveTo: initialData?.request.effectiveTo || '',
      notes: initialData?.request.notes || '',
      students:
        initialData?.students.map((item) => ({
          studentId: item.studentId,
          pickupPointId: item.pickupPointId ? String(item.pickupPointId) : '',
        })) || [{ studentId: 0, pickupPointId: '' }],
      isActive: initialData?.request.isActive ?? true,
    });
  }, [form, initialData, parents, schools]);

  const { fields, append, remove } = useFieldArray({
    control: form.control,
    name: 'students',
  });
  const [activeStudentIndex, setActiveStudentIndex] = React.useState(0);

  const schoolId = form.watch('schoolId');
  const filteredStudents = React.useMemo(
    () => students.filter((student) => student.schoolId === Number(schoolId)),
    [students, schoolId]
  );
  const filteredPickupPoints = React.useMemo(
    () =>
      pickupPoints.filter(
        (pickupPoint) => pickupPoint.schoolId === Number(schoolId)
      ),
    [pickupPoints, schoolId]
  );
  const selectedPickupPointId = form.watch(`students.${activeStudentIndex}.pickupPointId`);
  const selectedSchool = React.useMemo(
    () => schools.find((school) => school.id === Number(schoolId)),
    [schoolId, schools]
  );

  return (
    <Form {...form}>
      <form
        onSubmit={form.handleSubmit(async (values) =>
          onSubmit({
            parentProfileId: values.parentProfileId,
            schoolId: values.schoolId,
            requestType: values.requestType,
            effectiveFrom: values.effectiveFrom,
            effectiveTo: values.effectiveTo || null,
            notes: values.notes || undefined,
            students: values.students.map((item) => ({
              studentId: item.studentId,
              pickupPointId: item.pickupPointId
                ? Number(item.pickupPointId)
                : null,
            })),
            isActive: values.isActive,
          })
        )}
        className='space-y-6'
      >
        <div className='grid gap-4 md:grid-cols-2'>
          <SelectField
            form={form}
            name='parentProfileId'
            label='Parent'
            options={parents.map((parent) => ({
              value: String(parent.id),
              label: parent.fullName,
            }))}
          />
          <SelectField
            form={form}
            name='schoolId'
            label='School'
            options={schools.map((school) => ({
              value: String(school.id),
              label: school.name,
            }))}
          />
          <SelectField
            form={form}
            name='requestType'
            label='Request type'
            options={REQUEST_TYPE_OPTIONS.map((option) => ({
              value: option.value,
              label: option.label,
            }))}
          />
          <TextField
            form={form}
            name='effectiveFrom'
            label='Effective from'
            type='date'
          />
          <TextField
            form={form}
            name='effectiveTo'
            label='Effective to'
            type='date'
          />
        </div>

        <FormField
          control={form.control}
          name='notes'
          render={({ field }) => (
            <FormItem>
              <FormLabel>Notes</FormLabel>
              <FormControl>
                <Textarea {...field} value={field.value ?? ''} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <div className='grid gap-6 xl:grid-cols-[1.1fr_0.9fr]'>
          <div className='space-y-3'>
            <div className='flex items-center justify-between'>
              <h3 className='text-sm font-semibold uppercase tracking-wide text-muted-foreground'>
                Requested students
              </h3>
              <Button
                type='button'
                variant='outline'
                className={schoolBusUi.outlineButton}
                onClick={() => append({ studentId: 0, pickupPointId: '' })}
              >
                Add student
              </Button>
            </div>

            <div className='space-y-4'>
              {fields.map((field, index) => (
                <div
                  key={field.id}
                  className={`grid gap-4 md:grid-cols-[1fr_1fr_auto] ${schoolBusUi.interactiveCard} ${
                    activeStudentIndex === index
                      ? 'border-rose-300 bg-rose-50/70'
                      : ''
                  }`}
                  onClick={() => setActiveStudentIndex(index)}
                >
                  <SelectField
                    form={form}
                    name={`students.${index}.studentId`}
                    label='Student'
                    options={filteredStudents.map((student) => ({
                      value: String(student.id),
                      label: student.fullName,
                    }))}
                  />
                  <SelectField
                    form={form}
                    name={`students.${index}.pickupPointId`}
                    label='Pickup point'
                    allowEmpty
                    emptyValue='__none__'
                    emptyLabel='No pickup point'
                    options={filteredPickupPoints.map((pickupPoint) => ({
                      value: String(pickupPoint.id),
                      label: pickupPoint.name,
                    }))}
                  />
                  <div className='flex items-end'>
                    <Button
                      type='button'
                      variant='ghost'
                      className={`w-full ${schoolBusUi.ghostButton}`}
                      disabled={fields.length === 1}
                      onClick={() => remove(index)}
                    >
                      Remove
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className={`space-y-3 ${schoolBusUi.subtlePanel}`}>
            <div>
              <p className='text-sm font-semibold text-slate-950'>Pickup point map</p>
              <p className='mt-1 text-xs text-slate-500'>
                Select a student row, then click a pickup marker on the map to bind that pickup point.
              </p>
            </div>
            <OperationsMap
              schools={selectedSchool ? [selectedSchool] : []}
              pickupPoints={filteredPickupPoints}
              selectedSchoolId={selectedSchool?.id}
              selectedPickupPointId={
                selectedPickupPointId ? Number(selectedPickupPointId) : null
              }
              onPickupPointSelect={(pickupPointId) =>
                form.setValue(
                  `students.${activeStudentIndex}.pickupPointId`,
                  String(pickupPointId),
                  { shouldDirty: true }
                )
              }
              className='h-[360px] w-full overflow-hidden rounded-[20px] border'
            />

            {!selectedSchool?.latitude || !selectedSchool?.longitude ? (
              <p className='text-xs text-amber-700'>
                Missing coordinates: this school is not pinned yet, so only pickup points with coordinates will appear on the map.
              </p>
            ) : null}
          </div>
        </div>

        <div className='flex justify-end gap-2 border-t pt-4'>
          {onCancel ? (
            <Button
              type='button'
              variant='outline'
              className={schoolBusUi.outlineButton}
              onClick={onCancel}
            >
              Cancel
            </Button>
          ) : null}
          <Button
            type='submit'
            className={schoolBusUi.primaryButton}
            disabled={isLoading}
          >
            {isLoading ? 'Saving...' : submitLabel}
          </Button>
        </div>
      </form>
    </Form>
  );
}

interface RoutePlanFormProps {
  initialData?: SchoolBusRoute | null;
  schools: SchoolBusSchool[];
  onSubmit: (values: SchoolBusRouteUpsertRequest) => Promise<void>;
  isLoading?: boolean;
  onCancel?: () => void;
  submitLabel?: string;
}

export function RoutePlanForm({
  initialData,
  schools,
  onSubmit,
  isLoading = false,
  onCancel,
  submitLabel = 'Save route',
}: RoutePlanFormProps) {
  const form = useForm<RouteFormValues>({
    resolver: zodResolver(routeSchema) as any,
    defaultValues: {
      schoolId: initialData?.schoolId ?? schools[0]?.id ?? 0,
      routeName: initialData?.routeName || '',
      serviceDate: initialData?.serviceDate || '',
      shiftType: initialData?.shiftType || SHIFT_TYPE_OPTIONS[0].value,
      planningNotes: initialData?.planningNotes || '',
      isActive: initialData?.isActive ?? true,
    },
  });

  React.useEffect(() => {
    form.reset({
      schoolId: initialData?.schoolId ?? schools[0]?.id ?? 0,
      routeName: initialData?.routeName || '',
      serviceDate: initialData?.serviceDate || '',
      shiftType: initialData?.shiftType || SHIFT_TYPE_OPTIONS[0].value,
      planningNotes: initialData?.planningNotes || '',
      isActive: initialData?.isActive ?? true,
    });
  }, [form, initialData, schools]);

  return (
    <Form {...form}>
      <form
        onSubmit={form.handleSubmit(async (values) =>
          onSubmit({
            schoolId: values.schoolId,
            routeName: values.routeName,
            serviceDate: values.serviceDate,
            shiftType: values.shiftType,
            planningNotes: values.planningNotes || undefined,
            isActive: values.isActive,
          })
        )}
        className='space-y-6'
      >
        <div className='grid gap-4 md:grid-cols-2'>
          <SelectField
            form={form}
            name='schoolId'
            label='School'
            options={schools.map((school) => ({
              value: String(school.id),
              label: school.name,
            }))}
          />
          {initialData?.routeCode ? (
            <ReadOnlyField label='Route code' value={initialData.routeCode} />
          ) : null}
          <TextField form={form} name='routeName' label='Route name' />
          <TextField
            form={form}
            name='serviceDate'
            label='Service date'
            type='date'
          />
          <SelectField
            form={form}
            name='shiftType'
            label='Shift type'
            options={SHIFT_TYPE_OPTIONS.map((option) => ({
              value: option.value,
              label: option.label,
            }))}
          />
        </div>

        <FormField
          control={form.control}
          name='planningNotes'
          render={({ field }) => (
            <FormItem>
              <FormLabel>Planning notes</FormLabel>
              <FormControl>
                <Textarea {...field} value={field.value ?? ''} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <div className='flex justify-end gap-2 border-t pt-4'>
          {onCancel ? (
            <Button
              type='button'
              variant='outline'
              className={schoolBusUi.outlineButton}
              onClick={onCancel}
            >
              Cancel
            </Button>
          ) : null}
          <Button
            type='submit'
            className={schoolBusUi.primaryButton}
            disabled={isLoading}
          >
            {isLoading ? 'Saving...' : submitLabel}
          </Button>
        </div>
      </form>
    </Form>
  );
}

interface RejectDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (values: SchoolBusRejectRequest) => Promise<void>;
  isLoading?: boolean;
}

export function RejectTransportRequestDialog({
  open,
  onOpenChange,
  onSubmit,
  isLoading = false,
}: RejectDialogProps) {
  const form = useForm<RejectFormValues>({
    resolver: zodResolver(rejectSchema) as any,
    defaultValues: { reason: '' },
  });

  React.useEffect(() => {
    if (open) {
      form.reset({ reason: '' });
    }
  }, [form, open]);

  return (
    <SchoolBusFormDialog
      open={open}
      onOpenChange={onOpenChange}
      title='Reject transport request'
      description='Capture the rejection reason before closing this request.'
    >
      <Form {...form}>
        <form
          onSubmit={form.handleSubmit(async (values) => onSubmit(values))}
          className='space-y-4'
        >
          <FormField
            control={form.control}
            name='reason'
            render={({ field }) => (
              <FormItem>
                <FormLabel>Reason</FormLabel>
                <FormControl>
                  <Textarea {...field} value={field.value ?? ''} />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />

          <div className='flex justify-end gap-2 border-t pt-4'>
            <Button
              type='button'
              variant='outline'
              className={schoolBusUi.outlineButton}
              onClick={() => onOpenChange(false)}
            >
              Cancel
            </Button>
            <Button
              type='submit'
              className={schoolBusUi.dangerButton}
              disabled={isLoading}
            >
              {isLoading ? 'Rejecting...' : 'Reject request'}
            </Button>
          </div>
        </form>
      </Form>
    </SchoolBusFormDialog>
  );
}

interface RouteAssignmentDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  initialData?: {
    busId?: number | null;
    driverId?: number | null;
    attendantId?: number | null;
  } | null;
  buses: SchoolBusBus[];
  drivers: SchoolBusDriver[];
  attendants: SchoolBusAttendant[];
  onSubmit: (values: SchoolBusRouteAssignmentRequest) => Promise<void>;
  isLoading?: boolean;
}

export function RouteAssignmentDialog({
  open,
  onOpenChange,
  initialData,
  buses,
  drivers,
  attendants,
  onSubmit,
  isLoading = false,
}: RouteAssignmentDialogProps) {
  const form = useForm<AssignmentFormValues>({
    resolver: zodResolver(assignmentSchema) as any,
    defaultValues: {
      busId: initialData?.busId ?? buses[0]?.id ?? 0,
      driverId: initialData?.driverId ?? drivers[0]?.id ?? 0,
      attendantId: initialData?.attendantId ? String(initialData.attendantId) : '',
      isActive: true,
    },
  });

  React.useEffect(() => {
    form.reset({
      busId: initialData?.busId ?? buses[0]?.id ?? 0,
      driverId: initialData?.driverId ?? drivers[0]?.id ?? 0,
      attendantId: initialData?.attendantId ? String(initialData.attendantId) : '',
      isActive: true,
    });
  }, [form, initialData, buses, drivers]);

  return (
    <SchoolBusFormDialog
      open={open}
      onOpenChange={onOpenChange}
      title='Assign route resources'
      description='Attach a bus, driver, and optional attendant to this route.'
    >
      <Form {...form}>
        <form
          onSubmit={form.handleSubmit(async (values) =>
            onSubmit({
              busId: values.busId,
              driverId: values.driverId,
              attendantId: values.attendantId ? Number(values.attendantId) : null,
              isActive: values.isActive,
            })
          )}
          className='space-y-4'
        >
          <div className='grid gap-4 md:grid-cols-2'>
            <SelectField
              form={form}
              name='busId'
              label='Bus'
              options={buses.map((bus) => ({
                value: String(bus.id),
                label: `${bus.plateNumber} (${bus.status})`,
              }))}
            />
            <SelectField
              form={form}
              name='driverId'
              label='Driver'
              options={drivers.map((driver) => ({
                value: String(driver.id),
                label: driver.fullName,
              }))}
            />
            <SelectField
              form={form}
              name='attendantId'
              label='Attendant'
              allowEmpty
              emptyValue='__none__'
              emptyLabel='No attendant'
              options={attendants.map((attendant) => ({
                value: String(attendant.id),
                label: attendant.fullName,
              }))}
            />
          </div>

          <div className='flex justify-end gap-2 border-t pt-4'>
            <Button
              type='button'
              variant='outline'
              className={schoolBusUi.outlineButton}
              onClick={() => onOpenChange(false)}
            >
              Cancel
            </Button>
            <Button
              type='submit'
              className={schoolBusUi.primaryButton}
              disabled={isLoading}
            >
              {isLoading ? 'Assigning...' : 'Assign route'}
            </Button>
          </div>
        </form>
      </Form>
    </SchoolBusFormDialog>
  );
}

function TextField({
  form,
  name,
  label,
  type = 'text',
}: {
  form: any;
  name: string;
  label: string;
  type?: string;
}) {
  return (
    <FormField
      control={form.control}
      name={name as any}
      render={({ field }) => (
        <FormItem>
          <FormLabel>{label}</FormLabel>
          <FormControl>
            <Input {...field} type={type} value={(field.value as string) ?? ''} />
          </FormControl>
          <FormMessage />
        </FormItem>
      )}
    />
  );
}

function ReadOnlyField({ label, value }: { label: string; value: string }) {
  return (
    <div className='space-y-2'>
      <FormLabel>{label}</FormLabel>
      <div className='rounded-xl border border-rose-100 bg-rose-50/70 px-3 py-2 text-sm font-semibold text-rose-700'>
        {value}
      </div>
    </div>
  );
}

function SelectField({
  form,
  name,
  label,
  options,
  allowEmpty = false,
  emptyLabel = 'None',
  emptyValue = '__none__',
}: {
  form: any;
  name: string;
  label: string;
  options: Array<{ value: string; label: string }>;
  allowEmpty?: boolean;
  emptyLabel?: string;
  emptyValue?: string;
}) {
  return (
    <FormField
      control={form.control}
      name={name as any}
      render={({ field }) => (
        <FormItem>
          <FormLabel>{label}</FormLabel>
          <Select
            onValueChange={(value) =>
              field.onChange(value === emptyValue ? '' : value)
            }
            value={String(field.value ?? (allowEmpty ? emptyValue : ''))}
          >
            <FormControl>
              <SelectTrigger>
                <SelectValue placeholder={`Select ${label.toLowerCase()}`} />
              </SelectTrigger>
            </FormControl>
            <SelectContent>
              {allowEmpty ? (
                <SelectItem value={emptyValue}>{emptyLabel}</SelectItem>
              ) : null}
              {options.map((option) => (
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
  );
}
