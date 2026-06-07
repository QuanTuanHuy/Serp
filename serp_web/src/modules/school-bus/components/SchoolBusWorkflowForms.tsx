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
  useGetSchoolPickupPointsCompatibilityQuery,
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

/** Blocking: missing required field → cannot save.
 *  Needs-config: all required fields present but approval blocker exists (missing window / coords).
 *  Ready: fully configured, can be approved. */
type RowReadiness =
  | 'missing-student'
  | 'missing-schedule'
  | 'missing-tripopt'
  | 'missing-pickup'
  | 'missing-dropoff'
  | 'missing-target'
  | 'needs-config'
  | 'ready';

interface CompatibilityEntry {
  pickupReadinessCode: string;
  dropoffReadinessCode: string;
}

function getRowReadiness(
  sv: any,
  needsTarget: boolean,
  requiresRouting: boolean,
  compatibilityMapBySchedule?: Map<string, CompatibilityEntry>
): RowReadiness {
  // ── Blocking checks ────────────────────────────────────────────────
  if (!sv.studentId || Number(sv.studentId) === 0) return 'missing-student';

  if (!requiresRouting) {
    if (needsTarget && (!sv.targetSubscriptionId || sv.targetSubscriptionId === '__none__')) return 'missing-target';
    return 'ready';
  }

  if (!sv.schoolScheduleId || sv.schoolScheduleId === '__none__') return 'missing-schedule';

  const opt = (sv.tripOption || '').toUpperCase();
  if (!opt || opt === '__NONE__') return 'missing-tripopt';

  const needsPickup  = opt === 'MORNING'   || opt === 'ROUND_TRIP';
  const needsDropoff = opt === 'AFTERNOON' || opt === 'ROUND_TRIP';

  if (needsPickup  && (!sv.pickupPointId  || sv.pickupPointId  === '__none__')) return 'missing-pickup';
  if (needsDropoff && (!sv.dropoffPointId || sv.dropoffPointId === '__none__')) return 'missing-dropoff';
  if (needsTarget  && (!sv.targetSubscriptionId || sv.targetSubscriptionId === '__none__')) return 'missing-target';

  // ── Approval-blocker check (needs-config) ─────────────────────────
  if (compatibilityMapBySchedule) {
    const schId = Number(sv.schoolScheduleId);
    if (schId) {
      if (needsPickup && sv.pickupPointId) {
        const comp = compatibilityMapBySchedule.get(`${schId}-${sv.pickupPointId}`);
        if (comp && comp.pickupReadinessCode !== 'READY' && comp.pickupReadinessCode !== 'NOT_CHECKED') {
          return 'needs-config';
        }
      }
      if (needsDropoff && sv.dropoffPointId) {
        const comp = compatibilityMapBySchedule.get(`${schId}-${sv.dropoffPointId}`);
        if (comp && comp.dropoffReadinessCode !== 'READY' && comp.dropoffReadinessCode !== 'NOT_CHECKED') {
          return 'needs-config';
        }
      }
    }
  }

  return 'ready';
}

function ReadinessBadge({ readiness }: { readiness: RowReadiness }) {
  const configs: Record<string, { label: string; className: string }> = {
    'missing-student': { label: 'Missing required fields', className: 'bg-red-50 text-red-700 ring-red-200' },
    'missing-schedule': { label: 'Missing required fields', className: 'bg-red-50 text-red-700 ring-red-200' },
    'missing-tripopt': { label: 'Missing required fields', className: 'bg-red-50 text-red-700 ring-red-200' },
    'missing-pickup': { label: 'Missing required fields', className: 'bg-red-50 text-red-700 ring-red-200' },
    'missing-dropoff': { label: 'Missing required fields', className: 'bg-red-50 text-red-700 ring-red-200' },
    'missing-target': { label: 'Missing required fields', className: 'bg-red-50 text-red-700 ring-red-200' },
    'needs-config': { label: 'Needs configuration', className: 'bg-amber-50 text-amber-700 ring-amber-200' },
    ready: { label: 'Ready', className: 'bg-emerald-50 text-emerald-700 ring-emerald-200' },
  };
  const cfg = configs[readiness] ?? configs.ready;
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
}).superRefine((data, ctx) => {
  const reqType = data.requestType;
  const isNewOrChangeOrRenew = reqType === 'NEW_SERVICE' || reqType === 'CHANGE_SERVICE' || reqType === 'RENEW_SERVICE';
  const requiresTarget = reqType !== 'NEW_SERVICE';

  data.students.forEach((student, index) => {
    if (!student.studentId || Number(student.studentId) === 0) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: 'Student is required',
        path: ['students', index, 'studentId'],
      });
      return;
    }

    if (requiresTarget) {
      if (!student.targetSubscriptionId || student.targetSubscriptionId === '' || student.targetSubscriptionId === '__none__') {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: `Target subscription is required for request type ${reqType}`,
          path: ['students', index, 'targetSubscriptionId'],
        });
      }
    }

    if (isNewOrChangeOrRenew) {
      if (!student.schoolScheduleId || student.schoolScheduleId === '' || student.schoolScheduleId === '__none__') {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: 'School schedule is required',
          path: ['students', index, 'schoolScheduleId'],
        });
      }

      if (!student.tripOption || student.tripOption === '' || student.tripOption === '__none__') {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: 'Trip option is required',
          path: ['students', index, 'tripOption'],
        });
      }

      const opt = student.tripOption || '';
      const needsPickup = opt === 'MORNING' || opt === 'ROUND_TRIP';
      const needsDropoff = opt === 'AFTERNOON' || opt === 'ROUND_TRIP';

      if (needsPickup && (!student.pickupPointId || student.pickupPointId === '' || student.pickupPointId === '__none__')) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: 'Pickup point is required',
          path: ['students', index, 'pickupPointId'],
        });
      }

      if (needsDropoff && (!student.dropoffPointId || student.dropoffPointId === '' || student.dropoffPointId === '__none__')) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: 'Drop-off point is required',
          path: ['students', index, 'dropoffPointId'],
        });
      }

      const hasDays = student.monday || student.tuesday || student.wednesday || student.thursday || student.friday || student.saturday || student.sunday;
      if (!hasDays) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: 'At least one day must be selected',
          path: ['students', index, 'monday'],
        });
      }
    }
  });
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
  /** When true: hides parent dropdown, auto-resolves parentProfileId from currentParentId. */
  isParentRole?: boolean;
  /** The parentProfileId of the currently logged-in parent. Required when isParentRole=true. */
  currentParentId?: number;
}

interface PickupDropoffPointsFieldsProps {
  index: number;
  form: any;
  schoolId: number;
  schoolScheduleId: number | null;
  pickupCapablePoints: any[];
  dropoffCapablePoints: any[];
  pickupRequired: boolean;
  dropoffRequired: boolean;
}

function PickupDropoffPointsFields({
  index,
  form,
  schoolId,
  schoolScheduleId,
  pickupCapablePoints,
  dropoffCapablePoints,
  pickupRequired,
  dropoffRequired,
}: PickupDropoffPointsFieldsProps) {
  const { data: compData } = useGetSchoolPickupPointsCompatibilityQuery(
    { schoolId, schoolScheduleId: schoolScheduleId! },
    { skip: !schoolId || !schoolScheduleId }
  );

  const compatibilityMap = React.useMemo(() => {
    const map = new Map<number, {
      pickupReadinessCode: string;
      pickupReadinessLabel: string;
      dropoffReadinessCode: string;
      dropoffReadinessLabel: string;
    }>();
    compData?.data?.forEach((item) => {
      map.set(item.pickupPointId, item);
    });
    return map;
  }, [compData]);

  // Watch currently selected values to show inline readiness badge
  const currentPickupId  = form.watch(`students.${index}.pickupPointId`);
  const currentDropoffId = form.watch(`students.${index}.dropoffPointId`);

  const selectedPickupComp  = (currentPickupId  && currentPickupId  !== '__none__') ? compatibilityMap.get(Number(currentPickupId))  : null;
  const selectedDropoffComp = (currentDropoffId && currentDropoffId !== '__none__') ? compatibilityMap.get(Number(currentDropoffId)) : null;

  /** Badge config for a readiness code: returns label + tailwind classes. */
  const readinessBadgeConfig = (code: string | undefined, direction: 'pickup' | 'dropoff'): { label: string; className: string } | null => {
    if (!schoolScheduleId || !code) return null;
    if (code === 'READY') return { label: 'Ready', className: 'bg-emerald-50 text-emerald-700 ring-emerald-200' };
    if (code === 'NOT_CHECKED') return null;
    if (code === (direction === 'pickup' ? 'MISSING_PICKUP_WINDOW' : 'MISSING_DROPOFF_WINDOW'))
      return { label: 'Missing window', className: 'bg-amber-50 text-amber-700 ring-amber-200' };
    if (code === 'MISSING_COORDINATES')
      return { label: 'Missing coordinates', className: 'bg-red-50 text-red-600 ring-red-200' };
    if (code === 'UNSUPPORTED_USAGE_TYPE')
      return { label: 'Unsupported usage', className: 'bg-red-50 text-red-600 ring-red-200' };
    return { label: 'Needs config', className: 'bg-amber-50 text-amber-700 ring-amber-200' };
  };

  const pickupBadge  = readinessBadgeConfig(selectedPickupComp?.pickupReadinessCode,  'pickup');
  const dropoffBadge = readinessBadgeConfig(selectedDropoffComp?.dropoffReadinessCode, 'dropoff');

  /** Option: name only as label (for truncation in trigger), readiness as description (second line in dropdown). */
  const makePickupOptions = React.useMemo(
    () => pickupCapablePoints.map((pp) => {
      const name: string = pp.pickupPointName ?? `Point #${pp.pickupPointId}`;
      const comp = compatibilityMap.get(pp.pickupPointId);
      let description: string | undefined;
      if (schoolScheduleId && comp) {
        if (comp.pickupReadinessCode === 'READY') description = 'Ready';
        else if (comp.pickupReadinessCode !== 'NOT_CHECKED') description = comp.pickupReadinessLabel;
      }
      return { value: String(pp.pickupPointId), label: name, description };
    }),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [pickupCapablePoints, compatibilityMap, schoolScheduleId]
  );

  const makeDropoffOptions = React.useMemo(
    () => dropoffCapablePoints.map((pp) => {
      const name: string = pp.pickupPointName ?? `Point #${pp.pickupPointId}`;
      const comp = compatibilityMap.get(pp.pickupPointId);
      let description: string | undefined;
      if (schoolScheduleId && comp) {
        if (comp.dropoffReadinessCode === 'READY') description = 'Ready';
        else if (comp.dropoffReadinessCode !== 'NOT_CHECKED') description = comp.dropoffReadinessLabel;
      }
      return { value: String(pp.pickupPointId), label: name, description };
    }),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [dropoffCapablePoints, compatibilityMap, schoolScheduleId]
  );

  return (
    <>
      <div className='min-w-0 space-y-1'>
        <SelectField
          form={form}
          name={`students.${index}.pickupPointId`}
          label={pickupRequired ? 'Pickup point *' : 'Pickup point'}
          allowEmpty
          emptyValue='__none__'
          emptyLabel='No pickup point'
          options={makePickupOptions}
        />
        {pickupBadge && (
          <span className={cn(
            'inline-flex items-center rounded-full px-2 py-0.5 text-[10px] font-medium ring-1 ml-0.5',
            pickupBadge.className
          )}>
            {pickupBadge.label}
          </span>
        )}
      </div>

      <div className='min-w-0 space-y-1'>
        <SelectField
          form={form}
          name={`students.${index}.dropoffPointId`}
          label={dropoffRequired ? 'Drop-off point *' : 'Drop-off point'}
          allowEmpty
          emptyValue='__none__'
          emptyLabel='No drop-off point'
          options={makeDropoffOptions}
        />
        {dropoffBadge && (
          <span className={cn(
            'inline-flex items-center rounded-full px-2 py-0.5 text-[10px] font-medium ring-1 ml-0.5',
            dropoffBadge.className
          )}>
            {dropoffBadge.label}
          </span>
        )}
      </div>
    </>
  );
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
  isParentRole = false,
  currentParentId,
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
        initialData?.request.parentProfileId ??
        (isParentRole ? (currentParentId ?? 0) : (parents[0]?.id ?? 0)),
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

  // Reset form ONLY when initialData changes (edit mode data arrives or is cleared).
  // Do NOT include parents/schools in deps — they are option lists only.
  // Including them caused re-reset after async API data loaded, destroying user input.
  React.useEffect(() => {
    form.reset({
      parentProfileId:
        initialData?.request.parentProfileId ??
        (isParentRole ? (currentParentId ?? 0) : 0),
      schoolId: initialData?.request.schoolId ?? 0,
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
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [initialData]);

  const { fields, append, remove } = useFieldArray({
    control: form.control,
    name: 'students',
  });
  const [activeStudentIndex, setActiveStudentIndex] = React.useState(0);

  const schoolId = form.watch('schoolId');
  const parentProfileId = form.watch('parentProfileId');
  const requestType = form.watch('requestType');
  const needsTarget = requestType !== 'NEW_SERVICE';
  // PAUSE / STOP / RESUME do not require routing data (schedule, tripOption, pickup, dropoff)
  const requiresRouting =
    requestType === 'NEW_SERVICE' ||
    requestType === 'CHANGE_SERVICE' ||
    requestType === 'RENEW_SERVICE';

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
  const uniqueScheduleIds = React.useMemo(() => {
    const ids = new Set<number>();
    allStudentValues.forEach((s) => {
      const idNum = Number(s.schoolScheduleId);
      if (idNum) ids.add(idNum);
    });
    return Array.from(ids);
  }, [allStudentValues]);

  const { data: comp1 } = useGetSchoolPickupPointsCompatibilityQuery(
    { schoolId: Number(schoolId), schoolScheduleId: uniqueScheduleIds[0]! },
    { skip: !schoolId || !uniqueScheduleIds[0] }
  );
  const { data: comp2 } = useGetSchoolPickupPointsCompatibilityQuery(
    { schoolId: Number(schoolId), schoolScheduleId: uniqueScheduleIds[1]! },
    { skip: !schoolId || !uniqueScheduleIds[1] }
  );
  const { data: comp3 } = useGetSchoolPickupPointsCompatibilityQuery(
    { schoolId: Number(schoolId), schoolScheduleId: uniqueScheduleIds[2]! },
    { skip: !schoolId || !uniqueScheduleIds[2] }
  );

  const compatibilityMapBySchedule = React.useMemo(() => {
    const map = new Map<string, CompatibilityEntry>();

    const addItems = (scheduleId: number, items: any[]) => {
      items.forEach((item) => {
        map.set(`${scheduleId}-${item.pickupPointId}`, {
          pickupReadinessCode: item.pickupReadinessCode ?? 'NOT_CHECKED',
          dropoffReadinessCode: item.dropoffReadinessCode ?? 'NOT_CHECKED',
        });
      });
    };

    if (comp1?.data && uniqueScheduleIds[0]) addItems(uniqueScheduleIds[0], comp1.data);
    if (comp2?.data && uniqueScheduleIds[1]) addItems(uniqueScheduleIds[1], comp2.data);
    if (comp3?.data && uniqueScheduleIds[2]) addItems(uniqueScheduleIds[2], comp3.data);

    return map;
  }, [comp1, comp2, comp3, uniqueScheduleIds]);

  // \u2500\u2500 Auto-fill default pickup/dropoff when studentId changes \u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500
  // Compares against a stable ref to detect which rows changed their student selection.
  // Only fills in pickup/dropoffPointId when the field is still empty \u2014 never overwrites.
  const prevStudentIdRef = React.useRef<Record<number, string>>({});

  React.useEffect(() => {
    allStudentValues.forEach((sv, idx) => {
      const rawId = sv.studentId;
      const currentKey = String(rawId ?? '');
      const prevKey = String(prevStudentIdRef.current[idx] ?? '');

      // A valid id is non-empty and not '0'
      const isValidId =
        rawId !== undefined &&
        rawId !== null &&
        String(rawId) !== '' &&
        String(rawId) !== '0' &&
        Number(rawId) !== 0;

      if (!isValidId || currentKey === prevKey) {
        prevStudentIdRef.current[idx] = currentKey;
        return;
      }

      prevStudentIdRef.current[idx] = currentKey;

      // Look up student in the full students list (not filteredStudents which may be empty)
      const st = students.find((s) => s.id === Number(rawId));
      if (!st) return;

      // Auto-fill pickup from student default (entity field: pickup_point_id)
      const defaultPickup = st.pickupPointId ?? null;
      const currentPickup = sv.pickupPointId;
      if (defaultPickup && (!currentPickup || currentPickup === '' || currentPickup === '__none__')) {
        form.setValue(`students.${idx}.pickupPointId`, String(defaultPickup), {
          shouldDirty: true,
          shouldValidate: false,
        });
      }

      // Auto-fill dropoff from student default (entity field: default_dropoff_point_id)
      const defaultDropoff = st.defaultDropoffPointId ?? null;
      const currentDropoff = sv.dropoffPointId;
      if (defaultDropoff && (!currentDropoff || currentDropoff === '' || currentDropoff === '__none__')) {
        form.setValue(`students.${idx}.dropoffPointId`, String(defaultDropoff), {
          shouldDirty: true,
          shouldValidate: false,
        });
      }
    });
  // Rerun when any studentId in any row changes, or when student list (option data) updates
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [JSON.stringify(allStudentValues.map((s) => String(s.studentId ?? ''))), students]);


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

  // ── Single source of truth: compute readiness for each student row ──
  // MUST NOT be wrapped in useMemo — form.watch('students') can return the same
  // array reference even after RHF mutates internal state in-place, causing a
  // stale memo hit. Direct computation on each render is always fresh.
  const rowReadinessList: RowReadiness[] = allStudentValues.map((sv) =>
    getRowReadiness(sv, needsTarget, requiresRouting, compatibilityMapBySchedule)
  );

  // Derive validation summary from rowReadinessList (same render cycle, guaranteed consistent)
  let _readyCount = 0;
  let _needsConfigCount = 0;
  const _blockingMessages: string[] = [];
  const _approvalBlockerMessages: string[] = [];

  allStudentValues.forEach((sv, idx) => {
    const studentName =
      students.find((s) => s.id === Number(sv.studentId))?.fullName || `Student ${idx + 1}`;
    const state = rowReadinessList[idx];

    switch (state) {
      case 'missing-student':
        _blockingMessages.push(`${studentName}: Student selection is required`);
        break;
      case 'missing-schedule':
        _blockingMessages.push(`${studentName}: School schedule is required`);
        break;
      case 'missing-tripopt':
        _blockingMessages.push(`${studentName}: Trip option is required (Morning / Afternoon / Round trip)`);
        break;
      case 'missing-pickup':
        _blockingMessages.push(`${studentName}: Pickup point is required for the selected trip option`);
        break;
      case 'missing-dropoff':
        _blockingMessages.push(`${studentName}: Drop-off point is required for the selected trip option`);
        break;
      case 'missing-target':
        _blockingMessages.push(`${studentName}: Target subscription is required`);
        break;
      case 'needs-config': {
        _needsConfigCount++;
        const schId = Number(sv.schoolScheduleId);
        const opt = (sv.tripOption || '').toUpperCase();
        const needsPickup  = opt === 'MORNING'   || opt === 'ROUND_TRIP';
        const needsDropoff = opt === 'AFTERNOON' || opt === 'ROUND_TRIP';
        if (needsPickup && sv.pickupPointId) {
          const comp = compatibilityMapBySchedule.get(`${schId}-${sv.pickupPointId}`);
          if (comp && comp.pickupReadinessCode !== 'READY' && comp.pickupReadinessCode !== 'NOT_CHECKED') {
            const label = comp.pickupReadinessCode === 'MISSING_PICKUP_WINDOW'
              ? 'Pickup window is not configured'
              : comp.pickupReadinessCode === 'MISSING_COORDINATES'
              ? 'Pickup point is missing coordinates'
              : 'Pickup point configuration is incomplete';
            _approvalBlockerMessages.push(`${studentName}: ${label} — cannot approve until fixed`);
          }
        }
        if (needsDropoff && sv.dropoffPointId) {
          const comp = compatibilityMapBySchedule.get(`${schId}-${sv.dropoffPointId}`);
          if (comp && comp.dropoffReadinessCode !== 'READY' && comp.dropoffReadinessCode !== 'NOT_CHECKED') {
            const label = comp.dropoffReadinessCode === 'MISSING_DROPOFF_WINDOW'
              ? 'Drop-off window is not configured'
              : comp.dropoffReadinessCode === 'MISSING_COORDINATES'
              ? 'Drop-off point is missing coordinates'
              : 'Drop-off point configuration is incomplete';
            _approvalBlockerMessages.push(`${studentName}: ${label} — cannot approve until fixed`);
          }
        }
        break;
      }
      case 'ready':
        _readyCount++;
        break;
    }
  });

  const validationSummary = {
    blockingMessages: _blockingMessages,
    approvalBlockerMessages: _approvalBlockerMessages,
    isAllReady: _blockingMessages.length === 0 && _needsConfigCount === 0,
    readyCount: _readyCount,
    needsConfigCount: _needsConfigCount,
    totalCount: allStudentValues.length,
  };


  return (
    <Form {...form}>
      <form
        onSubmit={form.handleSubmit(async (values) => {
          let hasDayErrors = false;
          values.students.forEach((item, index) => {
            if (item.schoolScheduleId) {
              const schedDays = getScheduleDays(String(item.schoolScheduleId));
              const daysToCheck = [
                { name: 'monday' as const, key: 'MONDAY' },
                { name: 'tuesday' as const, key: 'TUESDAY' },
                { name: 'wednesday' as const, key: 'WEDNESDAY' },
                { name: 'thursday' as const, key: 'THURSDAY' },
                { name: 'friday' as const, key: 'FRIDAY' },
                { name: 'saturday' as const, key: 'SATURDAY' },
                { name: 'sunday' as const, key: 'SUNDAY' },
              ];
              daysToCheck.forEach(({ name, key }) => {
                if (item[name] && !schedDays.has(key)) {
                  form.setError(`students.${index}.${name}`, {
                    type: 'manual',
                    message: `${key} is not supported by the selected schedule`,
                  });
                  hasDayErrors = true;
                }
              });
            }
          });
          if (hasDayErrors) return;

          return onSubmit({
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
          });
        })}
        className='space-y-6'
      >
        <div className='grid gap-6 grid-cols-1 lg:grid-cols-[1.15fr_0.85fr]'>
          {/* Left panel: Info & Students & Validation */}
          <div className='space-y-6 flex flex-col'>
            <SchoolBusFormSection
              title='Request Information'
              description='Effective dates define when this demand can be used for planning.'
            >
              <div className={cn('grid gap-4', isParentRole ? 'grid-cols-1 md:grid-cols-2' : 'grid-cols-1 md:grid-cols-3')}>
                {/* Parent dropdown — hidden for SCHOOL_BUS_PARENT role (backend resolves from token) */}
                {!isParentRole && (
                  <SelectField
                    form={form}
                    name='parentProfileId'
                    label='Parent'
                    options={parents.map((parent) => ({
                      value: String(parent.id),
                      label: parent.fullName,
                    }))}
                  />
                )}
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
                        <ReadinessBadge readiness={rowReadinessList[index] ?? 'missing-student'} />
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
                        label={requiresRouting ? 'Trip option *' : 'Trip option'}
                        allowEmpty
                        emptyValue='__none__'
                        emptyLabel='Select trip option'
                        options={TRIP_OPTION_OPTIONS.map((o) => ({
                          value: o.value,
                          label: o.label,
                        }))}
                      />
                    </div>

                    <div className='grid gap-4 md:grid-cols-2'>
                      {(() => {
                        const rowOpt = (form.watch(`students.${index}.tripOption`) || '').toUpperCase();
                        // Only mark required when tripOption is explicitly set to a value needing that direction
                        const pickupRequired = rowOpt === 'MORNING' || rowOpt === 'ROUND_TRIP';
                        const dropoffRequired = rowOpt === 'AFTERNOON' || rowOpt === 'ROUND_TRIP';
                        const rowScheduleId = form.watch(`students.${index}.schoolScheduleId`);
                        return (
                          <PickupDropoffPointsFields
                            index={index}
                            form={form}
                            schoolId={Number(schoolId)}
                            schoolScheduleId={rowScheduleId ? Number(rowScheduleId) : null}
                            pickupCapablePoints={pickupCapablePoints}
                            dropoffCapablePoints={dropoffCapablePoints}
                            pickupRequired={pickupRequired}
                            dropoffRequired={dropoffRequired}
                          />
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
                'rounded-xl p-4 text-xs border mt-4 space-y-3',
                validationSummary.blockingMessages.length > 0
                  ? 'bg-red-50/60 border-red-200 text-red-900'
                  : validationSummary.approvalBlockerMessages.length > 0
                  ? 'bg-amber-50/60 border-amber-200 text-amber-900'
                  : 'bg-emerald-50/50 border-emerald-100 text-emerald-800'
              )}>
                <div className='flex items-center justify-between font-semibold border-b pb-2 border-slate-100/60'>
                  <span>Validation summary</span>
                  <span>
                    {validationSummary.readyCount} / {validationSummary.totalCount}
                    {validationSummary.readyCount === validationSummary.totalCount && validationSummary.isAllReady
                      ? ' · All ready'
                      : validationSummary.needsConfigCount > 0
                      ? ' ready · ' + validationSummary.needsConfigCount + ' need config'
                      : ' ready'
                    }
                  </span>
                </div>

                {/* Blocking issues — prevent save */}
                {validationSummary.blockingMessages.length > 0 && (
                  <div className='space-y-1'>
                    <p className='font-bold text-red-700 uppercase text-[10px] tracking-wider'>
                      Missing required fields — prevent submission:
                    </p>
                    <ul className='list-disc list-inside space-y-1 text-[11px] font-medium'>
                      {validationSummary.blockingMessages.map((msg, i) => (
                        <li key={i}>{msg}</li>
                      ))}
                    </ul>
                  </div>
                )}

                {/* Approval blockers — can save, but cannot approve */}
                {validationSummary.approvalBlockerMessages.length > 0 && (
                  <div className='space-y-1 pt-1'>
                    <p className='font-bold text-amber-700 uppercase text-[10px] tracking-wider'>
                      Configuration required — can save but cannot approve:
                    </p>
                    <ul className='list-disc list-inside space-y-1 text-[11px] font-medium text-amber-800'>
                      {validationSummary.approvalBlockerMessages.map((msg, i) => (
                        <li key={i}>{msg}</li>
                      ))}
                    </ul>
                  </div>
                )}

                {/* All clear */}
                {validationSummary.isAllReady && validationSummary.blockingMessages.length === 0 && validationSummary.approvalBlockerMessages.length === 0 && (
                  <p className='text-[11px] font-medium text-emerald-700'>
                    ✓ All students are fully configured and ready for transport planning.
                  </p>
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
            disabled={isLoading || !validationSummary.isAllReady}
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
  // description field is forwarded as a second line inside dropdown options
  options: Array<{ value: string; label: string; description?: string }>;
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
    // Pass through description so SchoolBusSelect renders it as a second line in the dropdown
    const list = options.map((opt) => ({ label: opt.label, value: opt.value, description: opt.description }));
    if (allowEmpty) {
      list.unshift({ label: emptyLabel, value: emptyValue, description: undefined });
    }
    return list;
  }, [options, allowEmpty, emptyLabel, emptyValue]);

  const isSearchable = searchable ?? (options.length > 6);

  return (
    <FormField
      control={form.control}
      name={name as any}
      render={({ field }) => (
        // min-w-0 + overflow-hidden prevent the select from overflowing its grid cell
        <FormItem className={cn('min-w-0 overflow-hidden', className)}>
          <FormLabel>{label}</FormLabel>
          <FormControl>
            <SchoolBusSelect
              fullWidth
              size='md'
              className='h-11 rounded-xl w-full max-w-full text-slate-900 border-slate-200 shadow-sm'
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
