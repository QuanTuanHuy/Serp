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
  const mapStudentDefaults = (items: typeof initialData extends undefined ? never : NonNullable<typeof initialData>['students']) =>
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
    }));

  const emptyStudent = {
    studentId: 0, pickupPointId: '', dropoffPointId: '', schoolScheduleId: '',
    tripOption: '', monday: true, tuesday: true, wednesday: true, thursday: true,
    friday: true, saturday: false, sunday: false, targetSubscriptionId: '', studentNote: '',
  };

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
        : [emptyStudent],
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
        : [emptyStudent],
      isActive: initialData?.request.isActive ?? true,
    });
  }, [form, initialData, parents, schools]);

  const { fields, append, remove } = useFieldArray({
    control: form.control,
    name: 'students',
  });
  const [activeStudentIndex, setActiveStudentIndex] = React.useState(0);

  const schoolId = form.watch('schoolId');
  const requestType = form.watch('requestType');
  const needsTarget = requestType !== 'NEW_SERVICE';
  const filteredStudents = React.useMemo(
    () => students.filter((student) => student.schoolId === Number(schoolId)),
    [students, schoolId]
  );

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
                onClick={() => append(emptyStudent)}
              >
                Add student
              </Button>
            </div>

            <div className='space-y-4'>
              {fields.map((field, index) => (
                <div
                  key={field.id}
                  className={`space-y-3 ${schoolBusUi.interactiveCard} ${
                    activeStudentIndex === index
                      ? 'border-rose-300 bg-rose-50/70'
                      : ''
                  }`}
                  onClick={() => setActiveStudentIndex(index)}
                >
                  <div className='grid gap-4 md:grid-cols-[1fr_1fr_auto]'>
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
                      label='School schedule'
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
                        label='Target subscription'
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
                          <div className='flex items-center gap-2 rounded-md border border-dashed border-slate-300 bg-slate-50 px-3 py-2'>
                            <span className='text-xs text-slate-400'>
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
                          <FormLabel className='block text-xs font-medium text-slate-700'>
                            Days of week
                            <span className='ml-1.5 font-normal text-slate-400'>
                              (based on selected schedule)
                            </span>
                          </FormLabel>
                          <div className='flex flex-wrap gap-2'>
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
                                      className={[
                                        'inline-flex select-none items-center gap-1.5 rounded-md border px-2.5 py-1 text-xs font-medium transition-colors',
                                        isAllowed
                                          ? 'cursor-pointer border-rose-200 bg-white text-slate-700 hover:border-rose-400 hover:bg-rose-50'
                                          : 'cursor-not-allowed border-slate-200 bg-slate-100 text-slate-400',
                                      ].join(' ')}
                                    >
                                      <input
                                        type='checkbox'
                                        checked={Boolean(dayField.value)}
                                        disabled={!isAllowed}
                                        onChange={(e) => {
                                          if (isAllowed) dayField.onChange(e.target.checked);
                                        }}
                                        className='accent-rose-500 disabled:opacity-0'
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
                          <Input {...noteField} value={noteField.value ?? ''} placeholder='Optional note for this student' />
                        </FormControl>
                      </FormItem>
                    )}
                  />
                </div>
              ))}
            </div>
          </div>

          <div className={`space-y-3 ${schoolBusUi.subtlePanel}`}>
            <div>
              <p className='text-sm font-semibold text-slate-950'>Pickup point map</p>
              <p className='mt-1 text-xs text-slate-500'>
                Select a student row, then click a marker to assign that point.
                {activeTripOption?.toUpperCase() === 'AFTERNOON'
                  ? ' Click sets drop-off point.'
                  : ' Click sets pickup point.'}
              </p>
            </div>
            <SchoolBusMapWorkspace
              defaultPreset='map-focus'
              mapHeightClassName='h-[380px]'
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
                  className='h-full w-full'
                />
              }
              legend={<SchoolBusMapLegend />}
              panel={
                <div className='space-y-3'>
                  <p className='text-sm font-semibold text-slate-950'>
                    Active row context
                  </p>
                  <p className='text-xs text-slate-500'>
                    Student:{' '}
                    {activeStudent?.fullName || 'Select student in the active row'}
                  </p>
                  <p className='text-xs text-slate-500'>
                    Trip option:{' '}
                    {activeTripOption || 'ROUND_TRIP (default)'}
                  </p>
                  <p className='text-xs text-slate-500'>
                    Pickup:{' '}
                    {selectedPickupPointId
                      ? linkedPickupPoints.find(
                          (lp) => lp.pickupPointId === Number(selectedPickupPointId)
                        )?.pickupPointName || 'Unknown'
                      : 'Not selected'}
                  </p>
                  <p className='text-xs text-slate-500'>
                    Drop-off:{' '}
                    {selectedDropoffPointId
                      ? linkedPickupPoints.find(
                          (lp) => lp.pickupPointId === Number(selectedDropoffPointId)
                        )?.pickupPointName || 'Unknown'
                      : 'Not selected'}
                  </p>
                  <p className='text-xs text-slate-500'>
                    Available points: {linkedPickupPoints.length}
                  </p>
                </div>
              }
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
  disabled = false,
  description,
  placeholder,
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
            disabled={disabled}
          >
            <FormControl>
              <SelectTrigger>
                <SelectValue
                  placeholder={placeholder || `Select ${label.toLowerCase()}`}
                />
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
          {description ? (
            <FormDescription>{description}</FormDescription>
          ) : null}
          <FormMessage />
        </FormItem>
      )}
    />
  );
}
