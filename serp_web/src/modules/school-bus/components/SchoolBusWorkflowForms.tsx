'use client';

import * as React from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  useFieldArray,
  useForm,
} from 'react-hook-form';
import { z } from 'zod';
import { cn } from '@/shared/utils';
import { Plus, User } from 'lucide-react';
import {
  Button,
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
import {
  REQUEST_TYPE_OPTIONS,
  ROUTE_DIRECTION_OPTIONS,
  ROUTE_LOCATION_TYPE_OPTIONS,
  SHIFT_TYPE_OPTIONS,
  TRIP_OPTION_OPTIONS,
} from '../constants';
import { schoolBusUi } from '../theme';
import { SchoolBusSelect } from './ui/SchoolBusSelect';
import { SchoolBusCheckbox } from './ui/SchoolBusCheckbox';
import { SchoolBusDatePicker } from './ui/SchoolBusDatePicker';
import type {
  SchoolBusAttendant,
  SchoolBusBus,
  SchoolBusDepot,
  SchoolBusDriver,
  SchoolBusParent,
  SchoolBusPickupPoint,
  SchoolBusRejectRequest,
  SchoolBusRoute,
  SchoolBusRouteAssignmentRequest,
  SchoolBusRouteUpsertRequest,
  SchoolBusSchool,
  SchoolBusStudent,
  SchoolBusTransportRequestDetail,
  SchoolBusTransportRequestUpsertRequest,
} from '../types';
import {
  useGetActiveSchoolPickupPointsQuery,
  useGetActiveSchoolSchedulesQuery,
  useGetSubscriptionsQuery,
} from '../api/schoolBusApi';
import { getPageItems, SCHOOL_BUS_OPTION_QUERY } from '../utils';
import type { StudentMapMarker } from './map/OperationsMapClient';
import { OperationsMap } from './map/OperationsMap';
import { SchoolBusMapLegend } from './map/SchoolBusMapLegend';
import { SchoolBusMapWorkspace } from './map/SchoolBusMapWorkspace';
import { SchoolBusFormDialog } from './SchoolBusFormDialog';

interface SchoolBusFormSectionProps {
  title: string;
  description?: string;
  action?: React.ReactNode;
  children: React.ReactNode;
  className?: string;
}

export function SchoolBusFormSection({
  title,
  description,
  action,
  children,
  className,
}: SchoolBusFormSectionProps) {
  return (
    <div className={cn('space-y-4 rounded-2xl border border-slate-100 bg-white p-5 shadow-sm', className)}>
      <div className='flex items-start justify-between gap-4 border-b border-slate-100 pb-3.5'>
        <div>
          <h4 className='text-sm font-semibold text-slate-900'>{title}</h4>
          {description && <p className='mt-0.5 text-xs text-slate-500'>{description}</p>}
        </div>
        {action && <div className='shrink-0'>{action}</div>}
      </div>
      <div className='pt-1'>{children}</div>
    </div>
  );
}

function getRowReadiness(sv: any, needsTarget: boolean) {
  if (!sv.studentId || Number(sv.studentId) === 0) return 'missing-student';
  
  const opt = (sv.tripOption || '').toUpperCase();
  const needsPickup = opt === 'MORNING' || opt === 'ROUND_TRIP' || opt === '';
  const needsDropoff = opt === 'AFTERNOON' || opt === 'ROUND_TRIP';
  
  if (needsPickup && (!sv.pickupPointId || sv.pickupPointId === '__none__')) return 'missing-pickup';
  if (needsDropoff && (!sv.dropoffPointId || sv.dropoffPointId === '__none__')) return 'missing-dropoff';
  if (!sv.schoolScheduleId || sv.schoolScheduleId === '__none__') return 'missing-schedule';
  if (needsTarget && (!sv.targetSubscriptionId || sv.targetSubscriptionId === '__none__')) return 'missing-target';
  
  return 'ready';
}

function ReadinessBadge({ readiness }: { readiness: string }) {
  const configs: Record<string, { label: string; className: string }> = {
    'missing-student': { label: 'Missing student', className: 'bg-red-50 text-red-700 ring-red-200' },
    'missing-pickup': { label: 'Missing pickup', className: 'bg-amber-50 text-amber-700 ring-amber-200' },
    'missing-dropoff': { label: 'Missing drop-off', className: 'bg-amber-50 text-amber-700 ring-amber-200' },
    'missing-schedule': { label: 'Missing schedule', className: 'bg-amber-50 text-amber-700 ring-amber-200' },
    'missing-target': { label: 'Missing subscription', className: 'bg-amber-50 text-amber-700 ring-amber-200' },
    ready: { label: 'Ready', className: 'bg-emerald-50 text-emerald-700 ring-emerald-200' },
  };
  const cfg = configs[readiness] || configs.ready;
  return (
    <span className={cn('inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-[9px] font-semibold ring-1', cfg.className)}>
      {cfg.label}
    </span>
  );
}


const transportRequestSchema = z.object({
  parentProfileId: z.coerce.number().min(1, 'Parent is required'),
  schoolId: z.coerce.number().min(1, 'School is required'),
  requestType: z.string().min(1, 'Request type is required'),
  effectiveFrom: z.string().min(1, 'Effective from is required'),
  effectiveTo: z.string().optional(),
  notes: z.string().optional(),
  changeReason: z.string().optional(),
  students: z
    .array(
      z.object({
        studentId: z.coerce.number().min(1, 'Student is required'),
        pickupPointId: z.string().optional(),
        dropoffPointId: z.string().optional(),
        schoolScheduleId: z.string().optional(),
        tripOption: z.string().optional(),
        monday: z.boolean().default(true),
        tuesday: z.boolean().default(true),
        wednesday: z.boolean().default(true),
        thursday: z.boolean().default(true),
        friday: z.boolean().default(true),
        saturday: z.boolean().default(false),
        sunday: z.boolean().default(false),
        targetSubscriptionId: z.string().optional(),
        studentNote: z.string().optional(),
      })
    )
    .min(1, 'At least one student is required'),
  isActive: z.boolean().default(true),
});

const routeSchema = z.object({
  schoolId: z.coerce.number().min(1, 'School is required'),
  routeDirection: z.enum(['OUTBOUND', 'RETURN']),
  startLocationType: z.enum(['SCHOOL', 'DEPOT']),
  startDepotId: z.string().optional(),
  endLocationType: z.enum(['SCHOOL', 'DEPOT']),
  endDepotId: z.string().optional(),
  routeName: z.string().min(1, 'Route name is required'),
  serviceDate: z.string().min(1, 'Service date is required'),
  schoolScheduleId: z.string().min(1, 'School schedule is required'),
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
  assignmentNote: z.string().optional(),
  reason: z.string().optional(),
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
  onSubmit: (values: SchoolBusTransportRequestUpsertRequest) => Promise<void>;
  isLoading?: boolean;
  onCancel?: () => void;
  submitLabel?: string;
}

const EMPTY_STUDENT = {
  studentId: 0,
  pickupPointId: '',
  dropoffPointId: '',
  schoolScheduleId: '',
  tripOption: '',
  monday: true,
  tuesday: true,
  wednesday: true,
  thursday: true,
  friday: true,
  saturday: false,
  sunday: false,
  targetSubscriptionId: '',
  studentNote: '',
};

export function TransportRequestForm({
  initialData,
  parents,
  schools,
  students,
  onSubmit,
  isLoading = false,
  onCancel,
  submitLabel = 'Save request',
}: TransportRequestFormProps) {
  const mapStudentDefaults = React.useCallback((items: any[]) =>
    items.map((item) => ({
      studentId: item.studentId,
      pickupPointId: item.pickupPointId ? String(item.pickupPointId) : '',
      dropoffPointId: item.dropoffPointId ? String(item.dropoffPointId) : '',
      schoolScheduleId: item.schoolScheduleId ? String(item.schoolScheduleId) : '',
      tripOption: item.tripOption || '',
      monday: item.monday ?? true,
      tuesday: item.tuesday ?? true,
      wednesday: item.wednesday ?? true,
      thursday: item.thursday ?? true,
      friday: item.friday ?? true,
      saturday: item.saturday ?? false,
      sunday: item.sunday ?? false,
      targetSubscriptionId: item.targetSubscriptionId ? String(item.targetSubscriptionId) : '',
      studentNote: item.studentNote || '',
    })), []);

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
      changeReason: initialData?.request.changeReason || '',
      students: initialData?.students
        ? mapStudentDefaults(initialData.students)
        : [EMPTY_STUDENT],
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
      changeReason: initialData?.request.changeReason || '',
      students: initialData?.students
        ? mapStudentDefaults(initialData.students)
        : [EMPTY_STUDENT],
      isActive: initialData?.request.isActive ?? true,
    });
  }, [form, initialData, parents, schools, mapStudentDefaults]);

  const { fields, append, remove } = useFieldArray({
    control: form.control,
    name: 'students',
  });
  const [activeStudentIndex, setActiveStudentIndex] = React.useState(0);

  const schoolId = form.watch('schoolId');
  const parentProfileId = form.watch('parentProfileId');
  const requestType = form.watch('requestType');
  const needsTarget = requestType !== 'NEW_SERVICE';

  const filteredStudents = React.useMemo(() => {
    return students.filter(
      (student) =>
        student.schoolId === Number(schoolId) &&
        student.parentProfileId === Number(parentProfileId)
    );
  }, [students, schoolId, parentProfileId]);

  // When schoolId or parentProfileId changes, reset the students field array to avoid invalid/mismatched rows
  const prevSchoolParentRef = React.useRef({ schoolId, parentProfileId });
  React.useEffect(() => {
    const prev = prevSchoolParentRef.current;
    if (
      (prev.schoolId !== schoolId || prev.parentProfileId !== parentProfileId) &&
      prev.schoolId !== 0 &&
      prev.parentProfileId !== 0
    ) {
      form.setValue('students', [EMPTY_STUDENT], { shouldDirty: true });
      setActiveStudentIndex(0);
    }
    prevSchoolParentRef.current = { schoolId, parentProfileId };
  }, [schoolId, parentProfileId, form]);

  // ── Load linked pickup points reactively per school ───────────────
  const { data: linkedPPData } = useGetActiveSchoolPickupPointsQuery(
    Number(schoolId), { skip: !schoolId || Number(schoolId) === 0 }
  );
  const linkedPickupPoints = React.useMemo(
    () => linkedPPData?.data ?? [],
    [linkedPPData]
  );
  // Map to SchoolBusPickupPoint-compatible format for the map component
  const mapPickupPoints = React.useMemo<SchoolBusPickupPoint[]>(
    () => linkedPickupPoints
      .filter((lp) =>
        typeof lp.pickupPointLatitude === 'number' &&
        typeof lp.pickupPointLongitude === 'number'
      )
      .map((lp) => ({
        id: lp.pickupPointId,
        name: lp.pickupPointName,
        address: lp.pickupPointAddress || '',
        latitude: lp.pickupPointLatitude,
        longitude: lp.pickupPointLongitude,
        usageType: lp.pickupPointUsageType || null,
        isActive: true,
        isDeleted: false,
        createdAt: '',
        updatedAt: '',
      })),
    [linkedPickupPoints]
  );

  // ── Load schedules and subscriptions reactively ───────────────────
  const { data: schedulesData } = useGetActiveSchoolSchedulesQuery(
    Number(schoolId), { skip: !schoolId || Number(schoolId) === 0 }
  );
  const { data: subscriptionsData } = useGetSubscriptionsQuery(
    { ...SCHOOL_BUS_OPTION_QUERY, schoolId: Number(schoolId) } as any,
    { skip: !schoolId || Number(schoolId) === 0 }
  );
  const filteredSchedules = React.useMemo(
    () => schedulesData?.data ?? [],
    [schedulesData]
  );
  const filteredSubscriptions = React.useMemo(
    () => getPageItems(subscriptionsData?.data),
    [subscriptionsData]
  );

  // ── Reset school-dependent fields when school changes ─────────────
  const prevSchoolIdRef = React.useRef(schoolId);
  React.useEffect(() => {
    if (prevSchoolIdRef.current !== schoolId && prevSchoolIdRef.current !== undefined) {
      const currentStudents = form.getValues('students');
      currentStudents.forEach((_, idx) => {
        form.setValue(`students.${idx}.studentId`, 0);
        form.setValue(`students.${idx}.pickupPointId`, '');
        form.setValue(`students.${idx}.dropoffPointId`, '');
        form.setValue(`students.${idx}.schoolScheduleId`, '');
        form.setValue(`students.${idx}.targetSubscriptionId`, '');
      });
    }
    prevSchoolIdRef.current = schoolId;
  }, [schoolId, form]);

  // ── Per-row schedule-driven days helpers ──────────────────────────
  /** Returns the set of days enabled by the given schedule, uppercased. */
  const getScheduleDays = React.useCallback(
    (scheduleId: string): Set<string> => {
      if (!scheduleId || scheduleId === '__none__') return new Set();
      const sch = filteredSchedules.find((s) => String(s.id) === scheduleId);
      if (!sch?.daysOfWeek) return new Set();
      return new Set(sch.daysOfWeek.map((d) => d.toUpperCase()));
    },
    [filteredSchedules]
  );

  // ── Watch all student rows for the schedule auto-tick effect ──────
  const allStudentValues = form.watch('students');
  React.useEffect(() => {
    allStudentValues.forEach((sv, idx) => {
      const schedDays = getScheduleDays(sv.schoolScheduleId ?? '');
      if (schedDays.size === 0) return; // no schedule selected – keep as is
      const dayFields = [
        { field: 'monday' as const,    key: 'MONDAY'    },
        { field: 'tuesday' as const,   key: 'TUESDAY'   },
        { field: 'wednesday' as const, key: 'WEDNESDAY' },
        { field: 'thursday' as const,  key: 'THURSDAY'  },
        { field: 'friday' as const,    key: 'FRIDAY'    },
        { field: 'saturday' as const,  key: 'SATURDAY'  },
        { field: 'sunday' as const,    key: 'SUNDAY'    },
      ];
      dayFields.forEach(({ field, key }) => {
        const shouldBeEnabled = schedDays.has(key);
        if (!shouldBeEnabled && sv[field]) {
          form.setValue(`students.${idx}.${field}`, false, { shouldDirty: true });
        } else if (shouldBeEnabled && !sv[field]) {
          // Auto-tick if schedule has the day and user hasn't explicitly unticked
          // (We only auto-tick, never auto-untick beyond clearing non-schedule days)
          form.setValue(`students.${idx}.${field}`, true, { shouldDirty: true });
        }
      });
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [JSON.stringify(allStudentValues.map((s) => s.schoolScheduleId))]);


  // ── Derived state per active student row ──────────────────────────
  const selectedPickupPointId = form.watch(`students.${activeStudentIndex}.pickupPointId`);
  const selectedDropoffPointId = form.watch(`students.${activeStudentIndex}.dropoffPointId`);
  const activeStudentId = form.watch(`students.${activeStudentIndex}.studentId`);
  const activeTripOption = form.watch(`students.${activeStudentIndex}.tripOption`);
  const selectedSchool = React.useMemo(
    () => schools.find((school) => school.id === Number(schoolId)),
    [schoolId, schools]
  );
  const activeStudent = React.useMemo(
    () =>
      filteredStudents.find((student) => student.id === Number(activeStudentId)),
    [activeStudentId, filteredStudents]
  );

  const [fitAllKey, setFitAllKey] = React.useState(0);
  const [fitSelectedKey, setFitSelectedKey] = React.useState(0);

  const handleFitAll = () => setFitAllKey((k) => k + 1);
  const handleFitSelected = () => setFitSelectedKey((k) => k + 1);


  // ── Filter linked pickup points by usage type per trip option ─────
  const pickupCapablePoints = React.useMemo(
    () => linkedPickupPoints.filter((lp) => {
      const ut = lp.pickupPointUsageType?.toUpperCase();
      return !ut || ut === 'PICKUP_ONLY' || ut === 'PICKUP_DROPOFF';
    }),
    [linkedPickupPoints]
  );
  const dropoffCapablePoints = React.useMemo(
    () => linkedPickupPoints.filter((lp) => {
      const ut = lp.pickupPointUsageType?.toUpperCase();
      return !ut || ut === 'DROPOFF_ONLY' || ut === 'PICKUP_DROPOFF';
    }),
    [linkedPickupPoints]
  );

  // ── Build student markers for the map ─────────────────────────────
  const studentMarkers = React.useMemo<StudentMapMarker[]>(() => {
    const markers: StudentMapMarker[] = [];
    allStudentValues.forEach((sv, idx) => {
      const st = filteredStudents.find((s) => s.id === Number(sv.studentId));
      const name = st?.fullName || `Student ${idx + 1}`;
      if (sv.pickupPointId) {
        const pp = linkedPickupPoints.find((lp) => lp.pickupPointId === Number(sv.pickupPointId));
        if (pp?.pickupPointLatitude != null && pp?.pickupPointLongitude != null) {
          markers.push({
            key: `student-pickup-${idx}`,
            studentName: name,
            pointName: pp.pickupPointName,
            latitude: pp.pickupPointLatitude,
            longitude: pp.pickupPointLongitude,
            role: 'pickup',
          });
        }
      }
      if (sv.dropoffPointId && sv.dropoffPointId !== sv.pickupPointId) {
        const dp = linkedPickupPoints.find((lp) => lp.pickupPointId === Number(sv.dropoffPointId));
        if (dp?.pickupPointLatitude != null && dp?.pickupPointLongitude != null) {
          markers.push({
            key: `student-dropoff-${idx}`,
            studentName: name,
            pointName: dp.pickupPointName,
            latitude: dp.pickupPointLatitude,
            longitude: dp.pickupPointLongitude,
            role: 'dropoff',
          });
        }
      }
    });
    return markers;
  }, [allStudentValues, filteredStudents, linkedPickupPoints]);

  const canFitAll = Boolean(selectedSchool || mapPickupPoints.length > 0 || studentMarkers.length > 0);
  const canFitSelected = Boolean(selectedSchool?.id || selectedPickupPointId || selectedDropoffPointId);

  // ── Smart point selection handler from map click ──────────────────
  const handleMapPointSelect = React.useCallback((pickupPointId: number) => {
    const opt = (activeTripOption || '').toUpperCase();
    if (opt === 'AFTERNOON') {
      form.setValue(`students.${activeStudentIndex}.dropoffPointId`, String(pickupPointId), { shouldDirty: true });
    } else {
      // MORNING, ROUND_TRIP, or default — set pickup
      form.setValue(`students.${activeStudentIndex}.pickupPointId`, String(pickupPointId), { shouldDirty: true });
    }
  }, [form, activeStudentIndex, activeTripOption]);

  const validationSummary = React.useMemo(() => {
    let missingStudentCount = 0;
    let missingPickupCount = 0;
    let missingDropoffCount = 0;
    let missingScheduleCount = 0;
    let missingTargetCount = 0;
    let readyCount = 0;

    allStudentValues.forEach((sv) => {
      const state = getRowReadiness(sv, needsTarget);
      if (state === 'missing-student') missingStudentCount++;
      else if (state === 'missing-pickup') missingPickupCount++;
      else if (state === 'missing-dropoff') missingDropoffCount++;
      else if (state === 'missing-schedule') missingScheduleCount++;
      else if (state === 'missing-target') missingTargetCount++;
      else if (state === 'ready') readyCount++;
    });

    const messages: string[] = [];
    if (missingStudentCount > 0) messages.push(`${missingStudentCount} row(s) missing student selection`);
    if (missingPickupCount > 0) messages.push(`${missingPickupCount} student(s) missing pickup point`);
    if (missingDropoffCount > 0) messages.push(`${missingDropoffCount} student(s) missing drop-off point`);
    if (missingScheduleCount > 0) messages.push(`${missingScheduleCount} student(s) missing schedule`);
    if (missingTargetCount > 0) messages.push(`${missingTargetCount} student(s) missing target subscription`);

    const isAllReady = readyCount === allStudentValues.length;

    return {
      messages,
      isAllReady,
      readyCount,
      totalCount: allStudentValues.length,
    };
  }, [allStudentValues, needsTarget]);

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
            changeReason: values.changeReason || undefined,
            students: values.students.map((item) => ({
              studentId: item.studentId,
              pickupPointId: item.pickupPointId ? Number(item.pickupPointId) : null,
              dropoffPointId: item.dropoffPointId ? Number(item.dropoffPointId) : null,
              schoolScheduleId: item.schoolScheduleId ? Number(item.schoolScheduleId) : null,
              tripOption: item.tripOption || null,
              monday: item.monday,
              tuesday: item.tuesday,
              wednesday: item.wednesday,
              thursday: item.thursday,
              friday: item.friday,
              saturday: item.saturday,
              sunday: item.sunday,
              targetSubscriptionId: item.targetSubscriptionId ? Number(item.targetSubscriptionId) : null,
              studentNote: item.studentNote || null,
            })),
            isActive: values.isActive,
          })
        )}
        className='space-y-6'
      >
        <div className='grid gap-6 grid-cols-1 lg:grid-cols-[1.15fr_0.85fr]'>
          {/* Left panel: Info & Students & Validation */}
          <div className='space-y-6 flex flex-col'>
            <SchoolBusFormSection
              title='Request Information'
              description='Effective dates define when this demand can be used for planning.'
            >
              <div className='grid gap-4 grid-cols-1 md:grid-cols-3'>
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
              </div>

              <div className='grid gap-4 grid-cols-1 md:grid-cols-2 mt-4'>
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

              <div className='mt-4 max-w-xl'>
                <FormField
                  control={form.control}
                  name='notes'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Notes</FormLabel>
                      <FormControl>
                        <Textarea {...field} value={field.value ?? ''} rows={2} className='text-sm' />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
            </SchoolBusFormSection>

            <SchoolBusFormSection
              title={`Requested Students (${validationSummary.readyCount} / ${validationSummary.totalCount} ready)`}
              description='Add students and specify their schedules and locations.'
              action={
                <Button
                  type='button'
                  variant='outline'
                  size='sm'
                  className='rounded-lg h-8 border-slate-200 text-slate-800 hover:bg-slate-50 shadow-none'
                  onClick={() => append(EMPTY_STUDENT)}
                >
                  <Plus className='h-3.5 w-3.5 mr-1.5' /> Add student
                </Button>
              }
            >
              <div className='space-y-4 max-h-[600px] overflow-y-auto pr-1 mt-4'>
                {fields.map((field, index) => (
                  <div
                    key={field.id}
                    className={cn(
                      'p-4 border rounded-2xl space-y-4 transition-all cursor-pointer relative',
                      activeStudentIndex === index
                        ? 'border-slate-300 bg-slate-50/50 shadow-sm ring-1 ring-slate-300/20'
                        : 'border-slate-100 bg-white hover:border-slate-200'
                    )}
                    onClick={() => setActiveStudentIndex(index)}
                  >
                    <div className='flex items-center justify-between border-b border-slate-100/50 pb-2 mb-2'>
                      <div className='flex items-center gap-2'>
                        <span className='inline-flex h-5 w-5 items-center justify-center rounded-full bg-slate-200 text-[10px] font-bold text-slate-700'>
                          {index + 1}
                        </span>
                        <span className='font-semibold text-slate-800 text-xs'>Student details</span>
                        <ReadinessBadge readiness={getRowReadiness(allStudentValues[index] || {}, needsTarget)} />
                      </div>
                      <Button
                        type='button'
                        variant='ghost'
                        size='sm'
                        className='h-6 rounded-lg text-[11px] text-red-500 hover:text-red-700 hover:bg-red-50 px-2'
                        disabled={fields.length === 1}
                        onClick={(e) => {
                          e.stopPropagation();
                          remove(index);
                          if (activeStudentIndex === index) {
                            setActiveStudentIndex(Math.max(0, index - 1));
                          }
                        }}
                      >
                        Remove
                      </Button>
                    </div>

                    <div className='grid gap-4 md:grid-cols-2'>
                      <SelectField
                        form={form}
                        name={`students.${index}.studentId`}
                        label='Student *'
                        options={filteredStudents.map((student) => ({
                          value: String(student.id),
                          label: student.fullName,
                        }))}
                      />
                      <SelectField
                        form={form}
                        name={`students.${index}.tripOption`}
                        label='Trip option'
                        allowEmpty
                        emptyValue='__none__'
                        emptyLabel='Default (round trip)'
                        options={TRIP_OPTION_OPTIONS.map((o) => ({
                          value: o.value,
                          label: o.label,
                        }))}
                      />
                    </div>

                    <div className='grid gap-4 md:grid-cols-2'>
                      {(() => {
                        const rowOpt = (form.watch(`students.${index}.tripOption`) || '').toUpperCase();
                        const pickupRequired = rowOpt === 'MORNING' || rowOpt === 'ROUND_TRIP' || rowOpt === '';
                        const dropoffRequired = rowOpt === 'AFTERNOON' || rowOpt === 'ROUND_TRIP';
                        return (
                          <>
                            <SelectField
                              form={form}
                              name={`students.${index}.pickupPointId`}
                              label={pickupRequired ? 'Pickup point *' : 'Pickup point'}
                              allowEmpty
                              emptyValue='__none__'
                              emptyLabel='No pickup point'
                              options={pickupCapablePoints.map((pp) => ({
                                value: String(pp.pickupPointId),
                                label: pp.pickupPointName,
                              }))}
                            />
                            <SelectField
                              form={form}
                              name={`students.${index}.dropoffPointId`}
                              label={dropoffRequired ? 'Drop-off point *' : 'Drop-off point'}
                              allowEmpty
                              emptyValue='__none__'
                              emptyLabel='Same as pickup'
                              options={dropoffCapablePoints.map((pp) => ({
                                value: String(pp.pickupPointId),
                                label: pp.pickupPointName,
                              }))}
                            />
                          </>
                        );
                      })()}
                    </div>

                    <div className='grid gap-4 md:grid-cols-2'>
                      <SelectField
                        form={form}
                        name={`students.${index}.schoolScheduleId`}
                        label='School schedule *'
                        allowEmpty
                        emptyValue='__none__'
                        emptyLabel='No schedule'
                        options={filteredSchedules.map((s) => ({
                          value: String(s.id),
                          label: s.scheduleName || `Schedule #${s.id}`,
                        }))}
                      />
                      {needsTarget ? (
                        <SelectField
                          form={form}
                          name={`students.${index}.targetSubscriptionId`}
                          label='Target subscription *'
                          allowEmpty
                          emptyValue='__none__'
                          emptyLabel='No target'
                          options={filteredSubscriptions.map((s) => ({
                            value: String(s.id),
                            label: `${s.subscriptionCode} — ${s.studentName}`,
                          }))}
                        />
                      ) : null}
                    </div>

                    <div>
                      {(() => {
                        const rowScheduleId = form.watch(`students.${index}.schoolScheduleId`) ?? '';
                        const schedDays = getScheduleDays(rowScheduleId);
                        const scheduleSelected = schedDays.size > 0;

                        if (!scheduleSelected) {
                          return (
                            <div className='flex items-center gap-2 rounded-md border border-dashed border-slate-200 bg-slate-50 px-3 py-2'>
                              <span className='text-[11px] text-slate-400'>
                                📅 Select a school schedule above to configure days of week
                              </span>
                            </div>
                          );
                        }

                        const DAY_KEYS = [
                          { field: 'monday'    as const, key: 'MONDAY',    label: 'Mon' },
                          { field: 'tuesday'   as const, key: 'TUESDAY',   label: 'Tue' },
                          { field: 'wednesday' as const, key: 'WEDNESDAY', label: 'Wed' },
                          { field: 'thursday'  as const, key: 'THURSDAY',  label: 'Thu' },
                          { field: 'friday'    as const, key: 'FRIDAY',    label: 'Fri' },
                          { field: 'saturday'  as const, key: 'SATURDAY',  label: 'Sat' },
                          { field: 'sunday'    as const, key: 'SUNDAY',    label: 'Sun' },
                        ];

                        return (
                          <div className='space-y-1.5'>
                            <FormLabel className='block text-[11px] font-semibold text-slate-700 uppercase tracking-wider'>
                              Days of week (from schedule)
                            </FormLabel>
                            <div className='flex flex-wrap gap-1.5'>
                              {DAY_KEYS.map(({ field, key, label }) => {
                                const isAllowed = schedDays.has(key);
                                return (
                                  <FormField
                                    key={field}
                                    control={form.control}
                                    name={`students.${index}.${field}`}
                                    render={({ field: dayField }) => (
                                      <label
                                        title={!isAllowed ? 'Not part of this schedule' : undefined}
                                        className={cn(
                                          'inline-flex select-none items-center gap-1.5 rounded-lg border px-2 py-1 text-xs font-medium transition-colors',
                                          isAllowed
                                            ? 'cursor-pointer border-slate-200 bg-white text-slate-700 hover:border-slate-300 hover:bg-slate-50'
                                            : 'cursor-not-allowed border-slate-200 bg-slate-50 text-slate-400',
                                          isAllowed && dayField.value && 'border-[#C81E3A] bg-[#FDECEF]/60 text-[#C81E3A] font-semibold'
                                        )}
                                      >
                                        <SchoolBusCheckbox
                                          checked={Boolean(dayField.value)}
                                          disabled={!isAllowed}
                                          onCheckedChange={(checked) => {
                                            if (isAllowed) {
                                              dayField.onChange(checked);
                                            }
                                          }}
                                          className='h-3.5 w-3.5'
                                        />
                                        {label}
                                      </label>
                                    )}
                                  />
                                );
                              })}
                            </div>
                          </div>
                        );
                      })()}
                    </div>

                    <FormField
                      control={form.control}
                      name={`students.${index}.studentNote`}
                      render={({ field: noteField }) => (
                        <FormItem>
                          <FormLabel>Student note</FormLabel>
                          <FormControl>
                            <Input {...noteField} value={noteField.value ?? ''} placeholder='Optional note for this student' className='text-xs' />
                          </FormControl>
                        </FormItem>
                      )}
                    />
                  </div>
                ))}
              </div>

              {/* Validation Summary */}
              <div className={cn(
                'rounded-xl p-4 text-xs border mt-4',
                validationSummary.isAllReady
                  ? 'bg-emerald-50/50 border-emerald-100 text-emerald-800'
                  : 'bg-amber-50/50 border-amber-100 text-amber-800'
              )}>
                <div className='flex items-center justify-between font-semibold'>
                  <span>Validation summary:</span>
                  <span>{validationSummary.readyCount} / {validationSummary.totalCount} Ready</span>
                </div>
                {validationSummary.messages.length > 0 ? (
                  <ul className='list-disc list-inside mt-2 space-y-1 text-[11px] opacity-90'>
                    {validationSummary.messages.map((msg, i) => (
                      <li key={i}>{msg}</li>
                    ))}
                  </ul>
                ) : (
                  <p className='mt-1.5 text-[11px] opacity-90'>All requested students are ready for transport planning.</p>
                )}
              </div>
            </SchoolBusFormSection>
          </div>

          {/* Right panel: Map & Active Context */}
          <div className='space-y-4 lg:sticky lg:top-6 lg:self-start'>
            <div className='space-y-1.5 px-1'>
              <h4 className='text-sm font-semibold text-slate-900'>Pickup point map</h4>
              <p className='text-xs text-slate-500 leading-relaxed'>
                Select a student row on the left, then click a map marker to assign that point.
                {activeTripOption?.toUpperCase() === 'AFTERNOON'
                  ? ' Click sets drop-off point.'
                  : ' Click sets pickup point.'}
              </p>
            </div>
            <SchoolBusMapWorkspace
              defaultPreset='map-focus'
              mapHeightClassName='h-[500px]'
              onFitAll={handleFitAll}
              onFitRoute={handleFitSelected}
              canFitAll={canFitAll}
              canFitRoute={canFitSelected}
              fitRouteLabel='Fit Selected'
              map={
                <OperationsMap
                  schools={selectedSchool ? [selectedSchool] : []}
                  pickupPoints={mapPickupPoints}
                  studentMarkers={studentMarkers}
                  selectedSchoolId={selectedSchool?.id}
                  selectedPickupPointId={
                    selectedPickupPointId ? Number(selectedPickupPointId) : null
                  }
                  onPickupPointSelect={handleMapPointSelect}
                  fitAllKey={fitAllKey}
                  fitSelectedKey={fitSelectedKey}
                  className='h-full w-full'
                />
              }
              legend={<SchoolBusMapLegend />}
              panel={
                <div className='space-y-4 text-xs'>
                  <div>
                    <p className='text-sm font-semibold text-slate-900 border-b border-slate-100 pb-2 mb-3 flex items-center gap-1.5'>
                      <User className='h-4 w-4 text-slate-400' />
                      Active student row context
                    </p>
                    {activeStudentIndex !== null && activeStudentIndex >= 0 && activeStudentIndex < allStudentValues.length ? (
                      <div className='space-y-3.5 bg-slate-50/40 p-4 rounded-xl border border-slate-100'>
                        <div>
                          <span className='font-bold text-slate-400 block uppercase tracking-wider text-[8px] mb-1'>Selected student</span>
                          {activeStudent ? (
                            <span className='inline-flex items-center gap-1.5 font-semibold text-slate-800 text-xs bg-white px-2.5 py-1 rounded-md border border-slate-200/50 shadow-sm'>
                              <User className='h-3.5 w-3.5 text-slate-400' />
                              {activeStudent.fullName}
                            </span>
                          ) : (
                            <span className='inline-flex items-center text-amber-600 italic font-medium bg-amber-50 px-2 py-0.5 rounded border border-amber-100/50'>Not selected</span>
                          )}
                        </div>
                        <div>
                          <span className='font-bold text-slate-400 block uppercase tracking-wider text-[8px] mb-1'>Trip option</span>
                          <span className='inline-flex items-center font-semibold text-slate-700 text-xs bg-white px-2.5 py-1 rounded-md border border-slate-200/50 shadow-sm'>
                            {activeTripOption || 'ROUND_TRIP (Default)'}
                          </span>
                        </div>
                        <div>
                          <span className='font-bold text-slate-400 block uppercase tracking-wider text-[8px] mb-1'>Pickup point</span>
                          {selectedPickupPointId && selectedPickupPointId !== '__none__' ? (
                            <span className='inline-flex items-center gap-1.5 font-semibold text-sky-700 text-xs bg-sky-50 px-2.5 py-1 rounded-md border border-sky-100'>
                              <span className='h-1.5 w-1.5 rounded-full bg-sky-500 animate-pulse' />
                              {linkedPickupPoints.find((lp) => String(lp.pickupPointId) === selectedPickupPointId)?.pickupPointName || 'Selected'}
                            </span>
                          ) : (
                            <span className='inline-flex items-center gap-1 text-amber-600 italic font-medium bg-amber-50 px-2 py-1 rounded-md border border-amber-100/50'>
                              Not selected
                            </span>
                          )}
                        </div>
                        <div>
                          <span className='font-bold text-slate-400 block uppercase tracking-wider text-[8px] mb-1'>Drop-off point</span>
                          {selectedDropoffPointId && selectedDropoffPointId !== '__none__' ? (
                            <span className='inline-flex items-center gap-1.5 font-semibold text-emerald-700 text-xs bg-emerald-50 px-2.5 py-1 rounded-md border border-emerald-100'>
                              <span className='h-1.5 w-1.5 rounded-full bg-emerald-500 animate-pulse' />
                              {linkedPickupPoints.find((lp) => String(lp.pickupPointId) === selectedDropoffPointId)?.pickupPointName || 'Selected'}
                            </span>
                          ) : (
                            <span className='inline-flex items-center gap-1 text-slate-400 italic font-medium bg-white px-2.5 py-1 rounded-md border border-slate-200/50 shadow-sm'>
                              Same as pickup
                            </span>
                          )}
                        </div>
                        <div className='pt-2.5 border-t border-slate-100'>
                          <p className='text-[10px] text-slate-400 italic leading-relaxed'>
                            💡 Click a map marker to assign it to this row as {activeTripOption === 'AFTERNOON' ? 'Drop-off' : 'Pickup'}.
                          </p>
                        </div>
                      </div>
                    ) : (
                      <div className='py-8 text-center text-slate-400 italic bg-slate-50/50 rounded-xl border border-slate-100 border-dashed'>
                        Select a student row on the left to assign points using the map.
                      </div>
                    )}
                  </div>
                </div>
              }
            />

            {!selectedSchool?.latitude || !selectedSchool?.longitude ? (
              <div className='rounded-xl border border-amber-200 bg-amber-50/50 px-4 py-3 text-xs text-amber-800 leading-relaxed shadow-sm'>
                ⚠️ Missing coordinates: this school is not pinned yet, so only pickup points with coordinates will appear on the map.
              </div>
            ) : null}
          </div>
        </div>

        {/* Sticky bottom action bar */}
        <div className='flex justify-end gap-3 border-t bg-white sticky bottom-0 z-20 -mx-5 -mb-5 px-5 py-4 border-slate-200/80 rounded-b-2xl shadow-[0_-4px_12px_rgba(0,0,0,0.03)]'>
          {onCancel ? (
            <Button
              type='button'
              variant='outline'
              className='rounded-full border-slate-300 hover:bg-slate-50'
              onClick={onCancel}
            >
              Cancel
            </Button>
          ) : null}
          <Button
            type='submit'
            className='rounded-full bg-[#C81E3A] hover:bg-[#B31B34] text-white font-semibold shadow-sm'
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
  depots: SchoolBusDepot[];
  onSubmit: (values: SchoolBusRouteUpsertRequest) => Promise<void>;
  isLoading?: boolean;
  onCancel?: () => void;
  submitLabel?: string;
}

export function RoutePlanForm({
  initialData,
  schools,
  depots,
  onSubmit,
  isLoading = false,
  onCancel,
  submitLabel = 'Save route',
}: RoutePlanFormProps) {
  const form = useForm<RouteFormValues>({
    resolver: zodResolver(routeSchema) as any,
    defaultValues: {
      schoolId: initialData?.schoolId ?? schools[0]?.id ?? 0,
      routeDirection: initialData?.routeDirection || 'OUTBOUND',
      startLocationType: initialData?.startLocationType || 'SCHOOL',
      startDepotId:
        initialData?.startLocationType === 'DEPOT'
          ? String(initialData.startLocationId)
          : '',
      endLocationType: initialData?.endLocationType || 'SCHOOL',
      endDepotId:
        initialData?.endLocationType === 'DEPOT'
          ? String(initialData.endLocationId)
          : '',
      routeName: initialData?.routeName || '',
      serviceDate: initialData?.serviceDate || '',
      schoolScheduleId: initialData?.schoolScheduleId ? String(initialData.schoolScheduleId) : '',
      planningNotes: initialData?.planningNotes || '',
      isActive: initialData?.isActive ?? true,
    },
  });

  React.useEffect(() => {
    form.reset({
      schoolId: initialData?.schoolId ?? schools[0]?.id ?? 0,
      routeDirection: initialData?.routeDirection || 'OUTBOUND',
      startLocationType: initialData?.startLocationType || 'SCHOOL',
      startDepotId:
        initialData?.startLocationType === 'DEPOT'
          ? String(initialData.startLocationId)
          : '',
      endLocationType: initialData?.endLocationType || 'SCHOOL',
      endDepotId:
        initialData?.endLocationType === 'DEPOT'
          ? String(initialData.endLocationId)
          : '',
      routeName: initialData?.routeName || '',
      serviceDate: initialData?.serviceDate || '',
      schoolScheduleId: initialData?.schoolScheduleId ? String(initialData.schoolScheduleId) : '',
      planningNotes: initialData?.planningNotes || '',
      isActive: initialData?.isActive ?? true,
    });
  }, [form, initialData, schools]);

  const schoolId = Number(form.watch('schoolId') || 0);
  const routeDirection = form.watch('routeDirection');
  const startLocationType = form.watch('startLocationType');
  const endLocationType = form.watch('endLocationType');

  const { data: schedulesData } = useGetActiveSchoolSchedulesQuery(schoolId, {
    skip: !schoolId,
  });
  const schedules = schedulesData?.data ?? [];

  React.useEffect(() => {
    if (routeDirection === 'OUTBOUND') {
      form.setValue('endLocationType', 'SCHOOL', { shouldDirty: true });
      form.setValue('endDepotId', '', { shouldDirty: true });
      return;
    }

    form.setValue('startLocationType', 'SCHOOL', { shouldDirty: true });
    form.setValue('startDepotId', '', { shouldDirty: true });
  }, [form, routeDirection]);

  return (
    <Form {...form}>
      <form
        onSubmit={form.handleSubmit(async (values) =>
          onSubmit({
            schoolId: values.schoolId,
            routeDirection: values.routeDirection,
            startLocationType: values.startLocationType,
            startSchoolId:
              values.startLocationType === 'SCHOOL' ? values.schoolId : null,
            startDepotId:
              values.startLocationType === 'DEPOT' && values.startDepotId
                ? Number(values.startDepotId)
                : null,
            endLocationType: values.endLocationType,
            endSchoolId:
              values.endLocationType === 'SCHOOL' ? values.schoolId : null,
            endDepotId:
              values.endLocationType === 'DEPOT' && values.endDepotId
                ? Number(values.endDepotId)
                : null,
            routeName: values.routeName,
            serviceDate: values.serviceDate,
            schoolScheduleId: Number(values.schoolScheduleId),
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
            name='routeDirection'
            label='Route direction'
            options={ROUTE_DIRECTION_OPTIONS.map((option) => ({
              value: option.value,
              label: option.label,
            }))}
          />
          <SelectField
            form={form}
            name='schoolScheduleId'
            label='School schedule'
            options={schedules.map((schedule) => ({
              value: String(schedule.id),
              label: `${schedule.scheduleName} (${schedule.shiftType}${schedule.daysOfWeek ? ` · ${schedule.daysOfWeek.join(',')}` : ''})`,
            }))}
          />
        </div>

        <div className={schoolBusUi.subtlePanel}>
          <div className='mb-4'>
            <p className='text-sm font-semibold text-slate-950'>
              Fixed start and end points
            </p>
            <p className='mt-1 text-xs text-slate-500'>
              Routes must explicitly model where the bus starts and where it ends.
            </p>
          </div>
          <div className='grid gap-4 md:grid-cols-2'>
            <SelectField
              form={form}
              name='startLocationType'
              label='Start location'
              disabled={routeDirection === 'RETURN'}
              options={ROUTE_LOCATION_TYPE_OPTIONS.map((option) => ({
                value: option.value,
                label: option.label,
              }))}
            />
            {startLocationType === 'DEPOT' ? (
              <SelectField
                form={form}
                name='startDepotId'
                label='Start depot'
                options={depots.map((depot) => ({
                  value: String(depot.id),
                  label: depot.name,
                }))}
              />
            ) : (
              <ReadOnlyField
                label='Start school'
                value={
                  schools.find((school) => school.id === schoolId)?.name ||
                  'Select a school'
                }
              />
            )}

            <SelectField
              form={form}
              name='endLocationType'
              label='End location'
              disabled={routeDirection === 'OUTBOUND'}
              options={ROUTE_LOCATION_TYPE_OPTIONS.map((option) => ({
                value: option.value,
                label: option.label,
              }))}
            />
            {endLocationType === 'DEPOT' ? (
              <SelectField
                form={form}
                name='endDepotId'
                label='End depot'
                options={depots.map((depot) => ({
                  value: String(depot.id),
                  label: depot.name,
                }))}
              />
            ) : (
              <ReadOnlyField
                label='End school'
                value={
                  schools.find((school) => school.id === schoolId)?.name ||
                  'Select a school'
                }
              />
            )}
          </div>
          {depots.length === 0 &&
          (startLocationType === 'DEPOT' || endLocationType === 'DEPOT') ? (
            <p className='mt-3 text-xs font-medium text-amber-700'>
              No depots available. Create a depot from Schools / Depots before saving this route.
            </p>
          ) : null}
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
      assignmentNote: '',
      reason: '',
      isActive: true,
    },
  });

  React.useEffect(() => {
    form.reset({
      busId: initialData?.busId ?? buses[0]?.id ?? 0,
      driverId: initialData?.driverId ?? drivers[0]?.id ?? 0,
      attendantId: initialData?.attendantId ? String(initialData.attendantId) : '',
      assignmentNote: '',
      reason: '',
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

          <div className='grid gap-4 md:grid-cols-2'>
            <TextField
              form={form}
              name='assignmentNote'
              label='Assignment Note'
            />
            <TextField
              form={form}
              name='reason'
              label='Reason for change'
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
            {type === 'date' ? (
              <SchoolBusDatePicker
                fullWidth
                value={field.value}
                onChange={field.onChange}
              />
            ) : (
              <Input {...field} type={type} value={(field.value as string) ?? ''} />
            )}
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
      <div className='rounded-xl border border-slate-200 bg-slate-50/70 px-3 py-2 text-sm font-semibold text-slate-700'>
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
  disabled = false,
  description,
  placeholder,
  onChange,
  className,
  searchable,
}: {
  form: any;
  name: string;
  label: string;
  options: Array<{ value: string; label: string }>;
  allowEmpty?: boolean;
  emptyLabel?: string;
  emptyValue?: string;
  disabled?: boolean;
  description?: string;
  placeholder?: string;
  onChange?: (value: string) => void;
  className?: string;
  searchable?: boolean;
}) {
  const selectOptions = React.useMemo(() => {
    const list = options.map((opt) => ({ label: opt.label, value: opt.value }));
    if (allowEmpty) {
      list.unshift({ label: emptyLabel, value: emptyValue });
    }
    return list;
  }, [options, allowEmpty, emptyLabel, emptyValue]);

  const isSearchable = searchable ?? (options.length > 6);

  return (
    <FormField
      control={form.control}
      name={name as any}
      render={({ field }) => (
        <FormItem className={className}>
          <FormLabel>{label}</FormLabel>
          <FormControl>
            <SchoolBusSelect
              fullWidth
              size='md'
              className='h-11 rounded-xl w-full text-slate-900 border-slate-200 shadow-sm'
              disabled={disabled}
              value={String(field.value ?? (allowEmpty ? emptyValue : ''))}
              onChange={(value) => {
                const val = value === emptyValue ? '' : value;
                field.onChange(val);
                if (onChange) {
                  onChange(val);
                }
              }}
              placeholder={placeholder || `Select ${label.toLowerCase()}`}
              options={selectOptions}
              searchable={isSearchable}
            />
          </FormControl>
          {description ? (
            <FormDescription>{description}</FormDescription>
          ) : null}
          <FormMessage />
        </FormItem>
      )}
    />
  );
}
