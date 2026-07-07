'use client';

import * as React from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import { useFieldArray, useForm } from 'react-hook-form';
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
  TRIP_OPTION_OPTIONS,
} from '../constants';
import { schoolBusUi } from '../theme';
import { SchoolBusSelect } from './ui/SchoolBusSelect';
import { SchoolBusCheckbox } from './ui/SchoolBusCheckbox';
import { SchoolBusDatePicker } from './ui/SchoolBusDatePicker';
import type {
  SchoolBusDepot,
  SchoolBusPickupPoint,
  SchoolBusRejectRequest,
  SchoolBusRoute,
  SchoolBusRouteAssignmentRequest,
  SchoolBusRouteUpsertRequest,
  SchoolBusStudent,
  SchoolBusTransportRequestDetail,
  SchoolBusTransportRequestUpsertRequest,
  SchoolBusDropdownOption,
} from '../types';
import {
  useGetActiveSchoolPickupPointsQuery,
  useGetSchoolBusSubscriptionsQuery,
  useGetStudentsQuery,
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
    <div
      className={cn(
        'space-y-4 rounded-2xl border border-slate-100 bg-white p-5 shadow-sm',
        className
      )}
    >
      <div className='flex items-start justify-between gap-4 border-b border-slate-100 pb-3.5'>
        <div>
          <h4 className='text-sm font-semibold text-slate-900'>{title}</h4>
          {description && (
            <p className='mt-0.5 text-xs text-slate-500'>{description}</p>
          )}
        </div>
        {action && <div className='shrink-0'>{action}</div>}
      </div>
      <div className='pt-1'>{children}</div>
    </div>
  );
}

/** Blocking: missing required field -> cannot save.
 *  Needs-config: all required fields present but approval blocker exists (missing window / coords).
 *  Ready: fully configured, can be approved. */
type RowReadiness =
  | 'missing-student'
  | 'missing-tripopt'
  | 'missing-pickup'
  | 'missing-dropoff'
  | 'missing-target'
  | 'ready';

function getRowReadiness(
  sv: any,
  needsTarget: boolean,
  requiresRouting: boolean
): RowReadiness {
  // -- Blocking checks ------------------------------------------------
  if (!sv.studentId || Number(sv.studentId) === 0) return 'missing-student';

  if (!requiresRouting) {
    if (
      needsTarget &&
      (!sv.targetSubscriptionId || sv.targetSubscriptionId === '__none__')
    )
      return 'missing-target';
    return 'ready';
  }

  const opt = (sv.tripOption || '').toUpperCase();
  if (!opt || opt === '__NONE__') return 'missing-tripopt';

  const needsPickup = opt === 'MORNING' || opt === 'ROUND_TRIP';
  const needsDropoff = opt === 'AFTERNOON' || opt === 'ROUND_TRIP';

  if (needsPickup && (!sv.pickupPointId || sv.pickupPointId === '__none__'))
    return 'missing-pickup';
  if (needsDropoff && (!sv.dropoffPointId || sv.dropoffPointId === '__none__'))
    return 'missing-dropoff';
  if (
    needsTarget &&
    (!sv.targetSubscriptionId || sv.targetSubscriptionId === '__none__')
  )
    return 'missing-target';

  return 'ready';
}

function RowStatusBadge({ readiness }: { readiness: RowReadiness }) {
  const configs: Record<string, { label: string; className: string }> = {
    'missing-student': {
      label: 'Thiếu thông tin bắt buộc',
      className: 'bg-red-50 text-red-700 ring-red-200',
    },
    'missing-tripopt': {
      label: 'Thiếu thông tin bắt buộc',
      className: 'bg-red-50 text-red-700 ring-red-200',
    },
    'missing-pickup': {
      label: 'Thiếu thông tin bắt buộc',
      className: 'bg-red-50 text-red-700 ring-red-200',
    },
    'missing-dropoff': {
      label: 'Thiếu thông tin bắt buộc',
      className: 'bg-red-50 text-red-700 ring-red-200',
    },
    'missing-target': {
      label: 'Thiếu thông tin bắt buộc',
      className: 'bg-red-50 text-red-700 ring-red-200',
    },
    ready: {
      label: 'Hoàn tất',
      className: 'bg-emerald-50 text-emerald-700 ring-emerald-200',
    },
  };
  const cfg = configs[readiness] || configs.ready;
  return (
    <span
      className={cn(
        'inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-[9px] font-semibold ring-1',
        cfg.className
      )}
    >
      {cfg.label}
    </span>
  );
}

const transportRequestSchema = z
  .object({
    parentProfileId: z.coerce.number().min(1, 'Vui lòng chọn phụ huynh'),
    schoolId: z.coerce.number().min(1, 'Vui lòng chọn trường học'),
    requestType: z.string().min(1, 'Vui lòng chọn loại yêu cầu'),
    effectiveFrom: z.string().min(1, 'Vui lòng chọn ngày hiệu lực'),
    effectiveTo: z.string().optional(),
    notes: z.string().optional(),
    changeReason: z.string().optional(),
    students: z
      .array(
        z.object({
          studentId: z.coerce.number().min(1, 'Vui lòng chọn học sinh'),
          pickupPointId: z.string().optional(),
          dropoffPointId: z.string().optional(),
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
  })
  .superRefine((data, ctx) => {
    const reqType = data.requestType;
    const isNewOrChangeOrRenew =
      reqType === 'NEW_SERVICE' ||
      reqType === 'CHANGE_SERVICE' ||
      reqType === 'RENEW_SERVICE';
    const requiresTarget = reqType !== 'NEW_SERVICE';

    data.students.forEach((student, index) => {
      if (!student.studentId || Number(student.studentId) === 0) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: 'Vui lòng chọn học sinh',
          path: ['students', index, 'studentId'],
        });
        return;
      }

      if (requiresTarget) {
        if (
          !student.targetSubscriptionId ||
          student.targetSubscriptionId === '' ||
          student.targetSubscriptionId === '__none__'
        ) {
          ctx.addIssue({
            code: z.ZodIssueCode.custom,
            message: `Vui lòng chọn đăng ký dịch vụ cần áp dụng cho loại yêu cầu ${reqType}`,
            path: ['students', index, 'targetSubscriptionId'],
          });
        }
      }

      if (isNewOrChangeOrRenew) {
        if (
          !student.tripOption ||
          student.tripOption === '' ||
          student.tripOption === '__none__'
        ) {
          ctx.addIssue({
            code: z.ZodIssueCode.custom,
            message: 'Vui lòng chọn phương án đi xe',
            path: ['students', index, 'tripOption'],
          });
        }

        const opt = student.tripOption || '';
        const needsPickup = opt === 'MORNING' || opt === 'ROUND_TRIP';
        const needsDropoff = opt === 'AFTERNOON' || opt === 'ROUND_TRIP';

        if (
          needsPickup &&
          (!student.pickupPointId ||
            student.pickupPointId === '' ||
            student.pickupPointId === '__none__')
        ) {
          ctx.addIssue({
            code: z.ZodIssueCode.custom,
            message: 'Vui lòng chọn điểm đón',
            path: ['students', index, 'pickupPointId'],
          });
        }

        if (
          needsDropoff &&
          (!student.dropoffPointId ||
            student.dropoffPointId === '' ||
            student.dropoffPointId === '__none__')
        ) {
          ctx.addIssue({
            code: z.ZodIssueCode.custom,
            message: 'Vui lòng chọn điểm trả',
            path: ['students', index, 'dropoffPointId'],
          });
        }

        const hasDays =
          student.monday ||
          student.tuesday ||
          student.wednesday ||
          student.thursday ||
          student.friday ||
          student.saturday ||
          student.sunday;
        if (!hasDays) {
          ctx.addIssue({
            code: z.ZodIssueCode.custom,
            message: 'Vui lòng chọn ít nhất một ngày hoạt động',
            path: ['students', index, 'monday'],
          });
        }
      }
    });
  });

const routeSchema = z.object({
  schoolId: z.coerce.number().min(1, 'Vui lòng chọn trường học'),
  routeDirection: z.enum(['OUTBOUND', 'RETURN']),
  startLocationType: z.enum(['SCHOOL', 'DEPOT']),
  startDepotId: z.string().optional(),
  endLocationType: z.enum(['SCHOOL', 'DEPOT']),
  endDepotId: z.string().optional(),
  routeName: z.string().min(1, 'Vui lòng nhập tên tuyến'),
  serviceDate: z.string().min(1, 'Vui lòng chọn ngày phục vụ'),
  planningNotes: z.string().optional(),
  isActive: z.boolean().default(true),
});

const rejectSchema = z.object({
  reason: z.string().min(1, 'Vui lòng nhập lý do'),
});

const assignmentSchema = z.object({
  busId: z.coerce.number().min(1, 'Vui lòng chọn xe bus'),
  driverId: z.coerce.number().min(1, 'Vui lòng chọn tài xế'),
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
  parents: SchoolBusDropdownOption[];
  schools: SchoolBusDropdownOption[];
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
  pickupCapablePoints: any[];
  dropoffCapablePoints: any[];
  pickupRequired: boolean;
  dropoffRequired: boolean;
}

function PickupDropoffPointsFields({
  index,
  form,
  schoolId,
  pickupCapablePoints,
  dropoffCapablePoints,
  pickupRequired,
  dropoffRequired,
}: PickupDropoffPointsFieldsProps) {
  const makePickupOptions = React.useMemo(
    () =>
      pickupCapablePoints.map((pp) => {
        const name: string =
          pp.pickupPointName || `Điểm #${pp.pickupPointId}`;
        return { value: String(pp.pickupPointId), label: name };
      }),
    [pickupCapablePoints]
  );

  const makeDropoffOptions = React.useMemo(
    () =>
      dropoffCapablePoints.map((pp) => {
        const name: string =
          pp.pickupPointName || `Điểm #${pp.pickupPointId}`;
        return { value: String(pp.pickupPointId), label: name };
      }),
    [dropoffCapablePoints]
  );

  return (
    <>
      <div className='min-w-0 space-y-1'>
        <SelectField
          form={form}
          name={`students.${index}.pickupPointId`}
          label={pickupRequired ? 'Điểm đón *' : 'Điểm đón/trả'}
          allowEmpty
          emptyValue='__none__'
          emptyLabel='Chưa có điểm đón/trả'
          options={makePickupOptions}
        />
      </div>

      <div className='min-w-0 space-y-1'>
        <SelectField
          form={form}
          name={`students.${index}.dropoffPointId`}
          label={dropoffRequired ? 'Điểm trả *' : 'Điểm trả'}
          allowEmpty
          emptyValue='__none__'
          emptyLabel='Chưa có điểm trả'
          options={makeDropoffOptions}
        />
      </div>
    </>
  );
}

const EMPTY_STUDENT = {
  studentId: 0,
  pickupPointId: '',
  dropoffPointId: '',
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
  students: initialStudents,
  onSubmit,
  isLoading = false,
  onCancel,
  submitLabel = 'Lưu yêu cầu',
  isParentRole = false,
  currentParentId,
}: TransportRequestFormProps) {
  const mapStudentDefaults = React.useCallback(
    (items: any[]) =>
      items.map((item) => ({
        studentId: item.studentId,
        pickupPointId: item.pickupPointId ? String(item.pickupPointId) : '',
        dropoffPointId: item.dropoffPointId ? String(item.dropoffPointId) : '',
        tripOption: item.tripOption || '',
        monday: item.monday || true,
        tuesday: item.tuesday || true,
        wednesday: item.wednesday || true,
        thursday: item.thursday || true,
        friday: item.friday || true,
        saturday: item.saturday || false,
        sunday: item.sunday || false,
        targetSubscriptionId: item.targetSubscriptionId
          ? String(item.targetSubscriptionId)
          : '',
        studentNote: item.studentNote || '',
      })),
    []
  );

  const form = useForm<TransportRequestFormValues>({
    resolver: zodResolver(transportRequestSchema) as any,
    defaultValues: {
      parentProfileId:
        initialData?.request.parentProfileId ||
        (isParentRole ? (currentParentId || 0) : (parents[0]?.id || 0)),
      schoolId: initialData?.request.schoolId || schools[0]?.id || 0,
      requestType:
        initialData?.request.requestType || REQUEST_TYPE_OPTIONS[0].value,
      effectiveFrom: initialData?.request.effectiveFrom || '',
      effectiveTo: initialData?.request.effectiveTo || '',
      notes: initialData?.request.notes || '',
      changeReason: initialData?.request.changeReason || '',
      students: initialData?.students
        ? mapStudentDefaults(initialData.students)
        : [EMPTY_STUDENT],
      isActive: initialData?.request.isActive || true,
    },
  });

  // Reset form ONLY when initialData changes (edit mode data arrives or is cleared).
  // Do NOT include parents/schools in deps - they are option lists only.
  // Including them caused re-reset after async API data loaded, destroying user input.
  React.useEffect(() => {
    form.reset({
      parentProfileId:
        initialData?.request.parentProfileId ||
        (isParentRole ? (currentParentId || 0) : 0),
      schoolId: initialData?.request.schoolId || 0,
      requestType:
        initialData?.request.requestType || REQUEST_TYPE_OPTIONS[0].value,
      effectiveFrom: initialData?.request.effectiveFrom || '',
      effectiveTo: initialData?.request.effectiveTo || '',
      notes: initialData?.request.notes || '',
      changeReason: initialData?.request.changeReason || '',
      students: initialData?.students
        ? mapStudentDefaults(initialData.students)
        : [EMPTY_STUDENT],
      isActive: initialData?.request.isActive || true,
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

  // Dynamically load students based on selected school and parent (to support pagination limits)
  const { data: dynamicStudentsData, isFetching: isFetchingStudents } =
    useGetStudentsQuery(
      {
        ...SCHOOL_BUS_OPTION_QUERY,
        schoolId: Number(schoolId) || undefined,
        parentProfileId: Number(parentProfileId) || undefined,
        sortBy: 'fullName',
      },
      {
        // Skip query if school or parent isn't selected yet
        skip:
          !schoolId ||
          Number(schoolId) === 0 ||
          !parentProfileId ||
          Number(parentProfileId) === 0,
      }
    );

  const students = React.useMemo(() => {
    if (
      !schoolId ||
      Number(schoolId) === 0 ||
      !parentProfileId ||
      Number(parentProfileId) === 0
    ) {
      return isParentRole ? initialStudents : [];
    }
    return getPageItems(dynamicStudentsData?.data);
  }, [
    dynamicStudentsData,
    schoolId,
    parentProfileId,
    isParentRole,
    initialStudents,
  ]);

  const filteredStudents = React.useMemo(() => {
    return students.filter(
      (student) =>
        student.schoolId === Number(schoolId) &&
        student.parentProfileId === Number(parentProfileId)
    );
  }, [students, schoolId, parentProfileId]);

  // Reactively set parentProfileId for Parent role once currentParentId resolves
  React.useEffect(() => {
    if (isParentRole && currentParentId && !form.getValues('parentProfileId')) {
      form.setValue('parentProfileId', currentParentId);
    }
  }, [currentParentId, isParentRole, form]);

  // When schoolId or parentProfileId changes, reset the students field array to avoid invalid/mismatched rows
  const prevSchoolParentRef = React.useRef({ schoolId, parentProfileId });
  React.useEffect(() => {
    const prev = prevSchoolParentRef.current;
    if (
      (prev.schoolId !== schoolId ||
        prev.parentProfileId !== parentProfileId) &&
      prev.schoolId !== 0 &&
      prev.parentProfileId !== 0
    ) {
      form.setValue('students', [EMPTY_STUDENT], { shouldDirty: true });
      setActiveStudentIndex(0);
    }
    prevSchoolParentRef.current = { schoolId, parentProfileId };
  }, [schoolId, parentProfileId, form]);

  // -- Load linked pickup points reactively per school ---------------
  const { data: linkedPPData } = useGetActiveSchoolPickupPointsQuery(
    Number(schoolId),
    { skip: !schoolId || Number(schoolId) === 0 }
  );
  const linkedPickupPoints = React.useMemo(
    () => linkedPPData?.data || [],
    [linkedPPData]
  );
  // Map to SchoolBusPickupPoint-compatible format for the map component
  const mapPickupPoints = React.useMemo<SchoolBusPickupPoint[]>(
    () =>
      linkedPickupPoints
        .filter(
          (lp) =>
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

  // -- Load subscriptions reactively -------------------
  const { data: subscriptionsData } = useGetSchoolBusSubscriptionsQuery(
    { ...SCHOOL_BUS_OPTION_QUERY, schoolId: Number(schoolId) } as any,
    { skip: !schoolId || Number(schoolId) === 0 }
  );
  const filteredSubscriptions = React.useMemo(
    () => getPageItems(subscriptionsData?.data),
    [subscriptionsData]
  );

  // -- Reset school-dependent fields when school changes -------------
  const prevSchoolIdRef = React.useRef(schoolId);
  React.useEffect(() => {
    if (
      prevSchoolIdRef.current !== schoolId &&
      prevSchoolIdRef.current !== undefined
    ) {
      const currentStudents = form.getValues('students');
      currentStudents.forEach((_, idx) => {
        form.setValue(`students.${idx}.studentId`, 0);
        form.setValue(`students.${idx}.pickupPointId`, '');
        form.setValue(`students.${idx}.dropoffPointId`, '');
        form.setValue(`students.${idx}.targetSubscriptionId`, '');
      });
    }
    prevSchoolIdRef.current = schoolId;
  }, [schoolId, form]);

  const allStudentValues = form.watch('students');

  // \u2500\u2500 Auto-fill default pickup/dropoff when studentId changes \u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500
  // Compares against a stable ref to detect which rows changed their student selection.
  // Only fills in pickup/dropoffPointId when the field is still empty \u2014 never overwrites.
  const prevStudentIdRef = React.useRef<Record<number, string>>({});

  React.useEffect(() => {
    allStudentValues.forEach((sv, idx) => {
      const rawId = sv.studentId;
      const currentKey = String(rawId || '');
      const prevKey = String(prevStudentIdRef.current[idx] || '');

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
      const st =
        students.find((s) => s.id === Number(rawId)) ||
        (() => {
          const found = initialData?.students?.find(
            (s) => s.studentId === Number(rawId)
          );
          if (!found) return undefined;
          return {
            id: found.studentId,
            fullName: found.studentName,
            schoolId: initialData?.request?.schoolId || 0,
            parentProfileId: initialData?.request?.parentProfileId || 0,
            pickupPointId: found.pickupPointId || undefined,
            defaultDropoffPointId: found.dropoffPointId || undefined,
          } as SchoolBusStudent;
        })();
      if (!st) return;

      // Auto-fill pickup from student default (entity field: pickup_point_id)
      const defaultPickup = st.pickupPointId || null;
      const currentPickup = sv.pickupPointId;
      if (
        defaultPickup &&
        (!currentPickup || currentPickup === '' || currentPickup === '__none__')
      ) {
        form.setValue(`students.${idx}.pickupPointId`, String(defaultPickup), {
          shouldDirty: true,
          shouldValidate: false,
        });
      }

      // Auto-fill dropoff from student default (entity field: default_dropoff_point_id)
      const defaultDropoff = st.defaultDropoffPointId || null;
      const currentDropoff = sv.dropoffPointId;
      if (
        defaultDropoff &&
        (!currentDropoff ||
          currentDropoff === '' ||
          currentDropoff === '__none__')
      ) {
        form.setValue(
          `students.${idx}.dropoffPointId`,
          String(defaultDropoff),
          {
            shouldDirty: true,
            shouldValidate: false,
          }
        );
      }
    });
    // Rerun when any studentId in any row changes, or when student list (option data) updates
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    JSON.stringify(allStudentValues.map((s) => String(s.studentId || ''))),
    students,
  ]);

  const selectedPickupPointId = form.watch(
    `students.${activeStudentIndex}.pickupPointId`
  );
  const selectedDropoffPointId = form.watch(
    `students.${activeStudentIndex}.dropoffPointId`
  );
  const activeStudentId = form.watch(
    `students.${activeStudentIndex}.studentId`
  );
  const activeTripOption = form.watch(
    `students.${activeStudentIndex}.tripOption`
  );
  const selectedSchool = React.useMemo(
    () => schools.find((school) => school.id === Number(schoolId)),
    [schoolId, schools]
  );
  const selectedSchoolLat = selectedSchool
    ? 'latitude' in selectedSchool
      ? selectedSchool.latitude
      : (selectedSchool as any).metadata?.latitude
    : undefined;
  const selectedSchoolLng = selectedSchool
    ? 'longitude' in selectedSchool
      ? selectedSchool.longitude
      : (selectedSchool as any).metadata?.longitude
    : undefined;
  const activeStudent = React.useMemo(
    () =>
      filteredStudents.find(
        (student) => student.id === Number(activeStudentId)
      ),
    [activeStudentId, filteredStudents]
  );

  const [fitAllKey, setFitAllKey] = React.useState(0);
  const [fitSelectedKey, setFitSelectedKey] = React.useState(0);

  const handleFitAll = () => setFitAllKey((k) => k + 1);
  const handleFitSelected = () => setFitSelectedKey((k) => k + 1);

  // -- Filter linked pickup points by usage type per trip option -----
  const pickupCapablePoints = React.useMemo(
    () =>
      linkedPickupPoints.filter((lp) => {
        const ut = lp.pickupPointUsageType?.toUpperCase();
        return !ut || ut === 'PICKUP_ONLY' || ut === 'PICKUP_DROPOFF';
      }),
    [linkedPickupPoints]
  );
  const dropoffCapablePoints = React.useMemo(
    () =>
      linkedPickupPoints.filter((lp) => {
        const ut = lp.pickupPointUsageType?.toUpperCase();
        return !ut || ut === 'DROPOFF_ONLY' || ut === 'PICKUP_DROPOFF';
      }),
    [linkedPickupPoints]
  );

  // -- Build student markers for the map -----------------------------
  const studentMarkers = React.useMemo<StudentMapMarker[]>(() => {
    const markers: StudentMapMarker[] = [];
    allStudentValues.forEach((sv, idx) => {
      const st = filteredStudents.find((s) => s.id === Number(sv.studentId));
      const name = st?.fullName || `Học sinh ${idx + 1}`;
      if (sv.pickupPointId) {
        const pp = linkedPickupPoints.find(
          (lp) => lp.pickupPointId === Number(sv.pickupPointId)
        );
        if (
          pp?.pickupPointLatitude != null &&
          pp?.pickupPointLongitude != null
        ) {
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
        const dp = linkedPickupPoints.find(
          (lp) => lp.pickupPointId === Number(sv.dropoffPointId)
        );
        if (
          dp?.pickupPointLatitude != null &&
          dp?.pickupPointLongitude != null
        ) {
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

  const canFitAll = Boolean(
    selectedSchool || mapPickupPoints.length > 0 || studentMarkers.length > 0
  );
  const canFitSelected = Boolean(
    selectedSchool?.id || selectedPickupPointId || selectedDropoffPointId
  );

  // -- Smart point selection handler from map click ------------------
  const handleMapPointSelect = React.useCallback(
    (pickupPointId: number) => {
      const opt = (activeTripOption || '').toUpperCase();
      if (opt === 'AFTERNOON') {
        form.setValue(
          `students.${activeStudentIndex}.dropoffPointId`,
          String(pickupPointId),
          { shouldDirty: true }
        );
      } else {
        // MORNING, ROUND_TRIP, or default - set pickup
        form.setValue(
          `students.${activeStudentIndex}.pickupPointId`,
          String(pickupPointId),
          { shouldDirty: true }
        );
      }
    },
    [form, activeStudentIndex, activeTripOption]
  );

  // -- Single source of truth: compute readiness for each student row --
  // MUST NOT be wrapped in useMemo - form.watch('students') can return the same
  // array reference even after RHF mutates internal state in-place, causing a
  // stale memo hit. Direct computation on each render is always fresh.
  const rowReadinessList: RowReadiness[] = allStudentValues.map((sv) =>
    getRowReadiness(sv, needsTarget, requiresRouting)
  );

  // Derive validation summary from rowReadinessList (same render cycle, guaranteed consistent)
  let _readyCount = 0;
  const _blockingMessages: string[] = [];
  const _approvalBlockerMessages: string[] = [];

  allStudentValues.forEach((sv, idx) => {
    const studentName =
      students.find((s) => s.id === Number(sv.studentId))?.fullName ||
      `Học sinh ${idx + 1}`;
    const state = rowReadinessList[idx];

    switch (state) {
      case 'missing-student':
        _blockingMessages.push(`${studentName}: Vui lòng chọn học sinh`);
        break;
      case 'missing-tripopt':
        _blockingMessages.push(
          `${studentName}: Vui lòng chọn phương án đi xe (sáng / chiều / hai chiều)`
        );
        break;
      case 'missing-pickup':
        _blockingMessages.push(
          `${studentName}: Vui lòng chọn điểm đón cho phương án đi xe đã chọn`
        );
        break;
      case 'missing-dropoff':
        _blockingMessages.push(
          `${studentName}: Vui lòng chọn điểm trả cho phương án đi xe đã chọn`
        );
        break;
      case 'missing-target':
        _blockingMessages.push(
          `${studentName}: Vui lòng chọn đăng ký dịch vụ cần áp dụng`
        );
        break;
      case 'ready':
        _readyCount++;
        break;
    }
  });

  const validationSummary = {
    blockingMessages: _blockingMessages,
    approvalBlockerMessages: _approvalBlockerMessages,
    isAllReady: _blockingMessages.length === 0,
    readyCount: _readyCount,
    needsConfigCount: 0,
    totalCount: allStudentValues.length,
  };

  return (
    <Form {...form}>
      <form
        onSubmit={form.handleSubmit(async (values) => {
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
              pickupPointId: item.pickupPointId
                ? Number(item.pickupPointId)
                : null,
              dropoffPointId: item.dropoffPointId
                ? Number(item.dropoffPointId)
                : null,
              tripOption: item.tripOption || null,
              monday: item.monday,
              tuesday: item.tuesday,
              wednesday: item.wednesday,
              thursday: item.thursday,
              friday: item.friday,
              saturday: item.saturday,
              sunday: item.sunday,
              targetSubscriptionId: item.targetSubscriptionId
                ? Number(item.targetSubscriptionId)
                : null,
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
              title='Thông tin yêu cầu'
              description='Ngày hiệu lực xác định thời gian yêu cầu được dùng cho lập kế hoạch.'
            >
              <div
                className={cn(
                  'grid gap-4',
                  isParentRole
                    ? 'grid-cols-1 md:grid-cols-2'
                    : 'grid-cols-1 md:grid-cols-3'
                )}
              >
                {/* Parent dropdown - hidden for SCHOOL_BUS_PARENT role (backend resolves from token) */}
                {!isParentRole && (
                  <SelectField
                    form={form}
                    name='parentProfileId'
                    label='Phụ huynh'
                    options={parents.map((parent) => ({
                      value: String(parent.id),
                      label: parent.label,
                    }))}
                  />
                )}
                <SelectField
                  form={form}
                  name='schoolId'
                  label='Trường học'
                  options={schools.map((school) => ({
                    value: String(school.id),
                    label: school.label,
                  }))}
                />
                <SelectField
                  form={form}
                  name='requestType'
                  label='Loại yêu cầu'
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
                  label='Hiệu lực từ'
                  type='date'
                />
                <TextField
                  form={form}
                  name='effectiveTo'
                  label='Hiệu lực đến'
                  type='date'
                />
              </div>

              <div className='mt-4 max-w-xl'>
                <FormField
                  control={form.control}
                  name='notes'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Ghi chú</FormLabel>
                      <FormControl>
                        <Textarea
                          {...field}
                          value={field.value || ''}
                          rows={2}
                          className='text-sm'
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
            </SchoolBusFormSection>

            <SchoolBusFormSection
              title={`Học sinh yêu cầu (${validationSummary.readyCount} / ${validationSummary.totalCount} đã hoàn tất)`}
              description='Thêm học sinh và khai báo ngày hoạt động, điểm đón/trả tương ứng.'
              action={
                <Button
                  type='button'
                  variant='outline'
                  size='sm'
                  className='rounded-lg h-8 border-slate-200 text-slate-800 hover:bg-slate-50 shadow-none'
                  onClick={() => append(EMPTY_STUDENT)}
                >
                  <Plus className='h-3.5 w-3.5 mr-1.5' /> Thêm học sinh
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
                        <span className='font-semibold text-slate-800 text-xs'>
                          Thông tin học sinh
                        </span>
                        <RowStatusBadge
                          readiness={
                            rowReadinessList[index] || 'missing-student'
                          }
                        />
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
                        Xóa
                      </Button>
                    </div>

                    <div className='grid gap-4 md:grid-cols-2'>
                      <SelectField
                        form={form}
                        name={`students.${index}.studentId`}
                        label='Học sinh *'
                        emptyText={
                          isFetchingStudents
                            ? 'Đang tải học sinh...'
                            : !schoolId || Number(schoolId) === 0
                              ? 'Vui lòng chọn trường học trước'
                              : !parentProfileId ||
                                  Number(parentProfileId) === 0
                                ? 'Vui lòng chọn phụ huynh trước'
                                : 'Không tìm thấy học sinh phù hợp với trường và phụ huynh này'
                        }
                        options={filteredStudents.map((student) => ({
                          value: String(student.id),
                          label: student.fullName,
                        }))}
                      />
                      <SelectField
                        form={form}
                        name={`students.${index}.tripOption`}
                        label={
                          requiresRouting ? 'Phương án đi xe *' : 'Phương án đi xe'
                        }
                        allowEmpty
                        emptyValue='__none__'
                        emptyLabel='Chọn loại chuyến'
                        options={TRIP_OPTION_OPTIONS.map((o) => ({
                          value: o.value,
                          label: o.label,
                        }))}
                      />
                    </div>

                    <div className='grid gap-4 md:grid-cols-1'>
                      {(() => {
                        const rowOpt = (
                          form.watch(`students.${index}.tripOption`) || ''
                        ).toUpperCase();
                        // Only mark required when tripOption is explicitly set to a value needing that direction
                        const pickupRequired =
                          rowOpt === 'MORNING' || rowOpt === 'ROUND_TRIP';
                        const dropoffRequired =
                          rowOpt === 'AFTERNOON' || rowOpt === 'ROUND_TRIP';
                        return (
                          <PickupDropoffPointsFields
                            index={index}
                            form={form}
                            schoolId={Number(schoolId)}
                            pickupCapablePoints={pickupCapablePoints}
                            dropoffCapablePoints={dropoffCapablePoints}
                            pickupRequired={pickupRequired}
                            dropoffRequired={dropoffRequired}
                          />
                        );
                      })()}
                    </div>

                    {needsTarget ? (
                      <div className='grid gap-4 md:grid-cols-1'>
                        <SelectField
                          form={form}
                          name={`students.${index}.targetSubscriptionId`}
                          label='Đăng ký dịch vụ đối tượng *'
                          allowEmpty
                          emptyValue='__none__'
                          emptyLabel='Chưa có đối tượng'
                          options={filteredSubscriptions.map((s) => ({
                            value: String(s.id),
                            label: `${s.subscriptionCode} - ${s.studentName}`,
                          }))}
                        />
                      </div>
                    ) : null}

                    <div>
                      <div className='space-y-1.5'>
                        <FormLabel className='block text-[11px] font-semibold text-slate-700 uppercase tracking-wider'>
                          Ngày trong tuần
                        </FormLabel>
                        <div className='flex flex-wrap gap-1.5'>
                          {[
                            { field: 'monday' as const, label: 'T2' },
                            { field: 'tuesday' as const, label: 'T3' },
                            { field: 'wednesday' as const, label: 'T4' },
                            { field: 'thursday' as const, label: 'T5' },
                            { field: 'friday' as const, label: 'T6' },
                            { field: 'saturday' as const, label: 'T7' },
                            { field: 'sunday' as const, label: 'CN' },
                          ].map(({ field, label }) => (
                            <FormField
                              key={field}
                              control={form.control}
                              name={`students.${index}.${field}`}
                              render={({ field: dayField }) => (
                                <label
                                  className={cn(
                                    'inline-flex select-none items-center gap-1.5 rounded-lg border px-2 py-1 text-xs font-medium cursor-pointer transition-colors',
                                    'border-slate-200 bg-white text-slate-700 hover:border-slate-300 hover:bg-slate-50',
                                    dayField.value &&
                                      'border-[#C81E3A] bg-[#FDECEF]/60 text-[#C81E3A] font-semibold'
                                  )}
                                >
                                  <SchoolBusCheckbox
                                    checked={Boolean(dayField.value)}
                                    onCheckedChange={dayField.onChange}
                                    className='h-3.5 w-3.5'
                                  />
                                  {label}
                                </label>
                              )}
                            />
                          ))}
                        </div>
                      </div>
                    </div>

                    <FormField
                      control={form.control}
                      name={`students.${index}.studentNote`}
                      render={({ field: noteField }) => (
                        <FormItem>
                          <FormLabel>Ghi chú học sinh</FormLabel>
                          <FormControl>
                            <Input
                              {...noteField}
                              value={noteField.value || ''}
                              placeholder='Ghi chú tùy chọn cho học sinh này'
                              className='text-xs'
                            />
                          </FormControl>
                        </FormItem>
                      )}
                    />
                  </div>
                ))}
              </div>

              {/* Validation Summary */}
              <div
                className={cn(
                  'rounded-xl p-4 text-xs border mt-4 space-y-3',
                  validationSummary.blockingMessages.length > 0
                    ? 'bg-red-50/60 border-red-200 text-red-900'
                    : validationSummary.approvalBlockerMessages.length > 0
                      ? 'bg-amber-50/60 border-amber-200 text-amber-900'
                      : 'bg-emerald-50/50 border-emerald-100 text-emerald-800'
                )}
              >
                <div className='flex items-center justify-between font-semibold border-b pb-2 border-slate-100/60'>
                  <span>Tóm tắt kiểm tra</span>
                  <span>
                    {validationSummary.readyCount} /{' '}
                    {validationSummary.totalCount}
                    {validationSummary.readyCount ===
                      validationSummary.totalCount &&
                    validationSummary.isAllReady
                      ? ' - Đã sẵn sàng'
                      : validationSummary.needsConfigCount > 0
                        ? ' sẵn sàng - ' +
                          validationSummary.needsConfigCount +
                          ' cần bổ sung cấu hình'
                        : ' sẵn sàng'}
                  </span>
                </div>

                {/* Blocking issues - prevent save */}
                {validationSummary.blockingMessages.length > 0 && (
                  <div className='space-y-1'>
                    <p className='font-bold text-red-700 uppercase text-[10px] tracking-wider'>
                      Thiếu thông tin bắt buộc nên chưa thể gửi:
                    </p>
                    <ul className='list-disc list-inside space-y-1 text-[11px] font-medium'>
                      {validationSummary.blockingMessages.map((msg, i) => (
                        <li key={i}>{msg}</li>
                      ))}
                    </ul>
                  </div>
                )}

                {/* Approval blockers - can save, but cannot approve */}
                {validationSummary.approvalBlockerMessages.length > 0 && (
                  <div className='space-y-1 pt-1'>
                    <p className='font-bold text-amber-700 uppercase text-[10px] tracking-wider'>
                      Cần bổ sung cấu hình - có thể lưu nhưng chưa thể duyệt:
                    </p>
                    <ul className='list-disc list-inside space-y-1 text-[11px] font-medium text-amber-800'>
                      {validationSummary.approvalBlockerMessages.map(
                        (msg, i) => (
                          <li key={i}>{msg}</li>
                        )
                      )}
                    </ul>
                  </div>
                )}

                {/* All clear */}
                {validationSummary.isAllReady &&
                  validationSummary.blockingMessages.length === 0 &&
                  validationSummary.approvalBlockerMessages.length === 0 && (
                    <p className='text-[11px] font-medium text-emerald-700'>
                      Tất cả học sinh đã đủ thông tin và sẵn sàng để lập kế hoạch.
                    </p>
                  )}
              </div>
            </SchoolBusFormSection>
          </div>

          {/* Right panel: Map & Active Context */}
          <div className='space-y-4 lg:sticky lg:top-6 lg:self-start'>
            <div className='space-y-1.5 px-1'>
              <h4 className='text-sm font-semibold text-slate-900'>
                Bản đồ điểm đón/trả
              </h4>
              <p className='text-xs text-slate-500 leading-relaxed'>
                Chọn một học sinh ở bên trái, sau đó bấm vào điểm trên bản đồ để
                gán điểm đó.
                {activeTripOption?.toUpperCase() === 'AFTERNOON'
                  ? ' Bấm để đặt điểm trả.'
                  : ' Bấm để đặt điểm đón.'}
              </p>
            </div>
            <SchoolBusMapWorkspace
              defaultPreset='map-focus'
              mapHeightClassName='h-[500px]'
              onFitAll={handleFitAll}
              onFitRoute={handleFitSelected}
              canFitAll={canFitAll}
              canFitRoute={canFitSelected}
              fitRouteLabel='Thu phóng tuyến đang chọn'
              map={
                <OperationsMap
                  schools={selectedSchool ? [selectedSchool as any] : []}
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
                      Thông tin học sinh đang chọn
                    </p>
                    {activeStudentIndex !== null &&
                    activeStudentIndex >= 0 &&
                    activeStudentIndex < allStudentValues.length ? (
                      <div className='space-y-3.5 bg-slate-50/40 p-4 rounded-xl border border-slate-100'>
                        <div>
                          <span className='font-bold text-slate-400 block uppercase tracking-wider text-[8px] mb-1'>
                            Học sinh đang chọn
                          </span>
                          {activeStudent ? (
                            <span className='inline-flex items-center gap-1.5 font-semibold text-slate-800 text-xs bg-white px-2.5 py-1 rounded-md border border-slate-200/50 shadow-sm'>
                              <User className='h-3.5 w-3.5 text-slate-400' />
                              {activeStudent.fullName}
                            </span>
                          ) : (
                            <span className='inline-flex items-center text-amber-600 italic font-medium bg-amber-50 px-2 py-0.5 rounded border border-amber-100/50'>
                              Chưa chọn
                            </span>
                          )}
                        </div>
                        <div>
                          <span className='font-bold text-slate-400 block uppercase tracking-wider text-[8px] mb-1'>
                            Phương án đi xe
                          </span>
                          <span className='inline-flex items-center font-semibold text-slate-700 text-xs bg-white px-2.5 py-1 rounded-md border border-slate-200/50 shadow-sm'>
                            {activeTripOption || 'Hai chiều (mặc định)'}
                          </span>
                        </div>
                        <div>
                          <span className='font-bold text-slate-400 block uppercase tracking-wider text-[8px] mb-1'>
                            Điểm đón
                          </span>
                          {selectedPickupPointId &&
                          selectedPickupPointId !== '__none__' ? (
                            <span className='inline-flex items-center gap-1.5 font-semibold text-sky-700 text-xs bg-sky-50 px-2.5 py-1 rounded-md border border-sky-100'>
                              <span className='h-1.5 w-1.5 rounded-full bg-sky-500 animate-pulse' />
                              {linkedPickupPoints.find(
                                (lp) =>
                                  String(lp.pickupPointId) ===
                                  selectedPickupPointId
                              )?.pickupPointName || 'Đã chọn'}
                            </span>
                          ) : (
                            <span className='inline-flex items-center gap-1 text-amber-600 italic font-medium bg-amber-50 px-2 py-1 rounded-md border border-amber-100/50'>
                              Chưa chọn
                            </span>
                          )}
                        </div>
                        <div>
                          <span className='font-bold text-slate-400 block uppercase tracking-wider text-[8px] mb-1'>
                            Điểm trả
                          </span>
                          {selectedDropoffPointId &&
                          selectedDropoffPointId !== '__none__' ? (
                            <span className='inline-flex items-center gap-1.5 font-semibold text-emerald-700 text-xs bg-emerald-50 px-2.5 py-1 rounded-md border border-emerald-100'>
                              <span className='h-1.5 w-1.5 rounded-full bg-emerald-500 animate-pulse' />
                              {linkedPickupPoints.find(
                                (lp) =>
                                  String(lp.pickupPointId) ===
                                  selectedDropoffPointId
                              )?.pickupPointName || 'Đã chọn'}
                            </span>
                          ) : (
                            <span className='inline-flex items-center gap-1 text-slate-400 italic font-medium bg-white px-2.5 py-1 rounded-md border border-slate-200/50 shadow-sm'>
                              Giống điểm đón
                            </span>
                          )}
                        </div>
                        <div className='pt-2.5 border-t border-slate-100'>
                          <p className='text-[10px] text-slate-400 italic leading-relaxed'>
                            Bấm vào điểm trên bản đồ để gán cho dòng này làm{' '}
                            {activeTripOption === 'AFTERNOON'
                              ? 'điểm trả'
                              : 'Điểm đón'}
                            .
                          </p>
                        </div>
                      </div>
                    ) : (
                      <div className='py-8 text-center text-slate-400 italic bg-slate-50/50 rounded-xl border border-slate-100 border-dashed'>
                        Chọn một học sinh ở bên trái để gán điểm bằng
                        bản đồ.
                      </div>
                    )}
                  </div>
                </div>
              }
            />

            {!selectedSchoolLat || !selectedSchoolLng ? (
              <div className='rounded-xl border border-amber-200 bg-amber-50/50 px-4 py-3 text-xs text-amber-800 leading-relaxed shadow-sm'>
                Cảnh báo: trường học chưa có tọa độ, nên bản đồ chỉ hiển thị
                các điểm đón/trả đã có tọa độ.
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
              Hủy
            </Button>
          ) : null}
          <Button
            type='submit'
            className='rounded-full bg-[#C81E3A] hover:bg-[#B31B34] text-white font-semibold shadow-sm'
            disabled={isLoading || !validationSummary.isAllReady}
          >
            {isLoading ? 'Đang lưu...' : submitLabel}
          </Button>
        </div>
      </form>
    </Form>
  );
}

interface RoutePlanFormProps {
  initialData?: SchoolBusRoute | null;
  schools: SchoolBusDropdownOption[];
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
  submitLabel = 'Lưu tuyến',
}: RoutePlanFormProps) {
  const form = useForm<RouteFormValues>({
    resolver: zodResolver(routeSchema) as any,
    defaultValues: {
      schoolId: initialData?.schoolId || schools[0]?.id || 0,
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
      planningNotes: initialData?.planningNotes || '',
      isActive: initialData?.isActive || true,
    },
  });

  React.useEffect(() => {
    form.reset({
      schoolId: initialData?.schoolId || schools[0]?.id || 0,
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
      planningNotes: initialData?.planningNotes || '',
      isActive: initialData?.isActive || true,
    });
  }, [form, initialData, schools]);

  const schoolId = Number(form.watch('schoolId') || 0);
  const routeDirection = form.watch('routeDirection');
  const startLocationType = form.watch('startLocationType');
  const endLocationType = form.watch('endLocationType');

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
            label='Trường học'
            options={schools.map((school) => ({
              value: String(school.id),
              label: school.label,
            }))}
          />
          {initialData?.routeCode ? (
            <ReadOnlyField label='Mã tuyến' value={initialData.routeCode} />
          ) : null}
          <TextField form={form} name='routeName' label='Tên tuyến' />
          <TextField
            form={form}
            name='serviceDate'
            label='Ngày phục vụ'
            type='date'
          />
          <SelectField
            form={form}
            name='routeDirection'
            label='Chiều tuyến'
            options={ROUTE_DIRECTION_OPTIONS.map((option) => ({
              value: option.value,
              label: option.label,
            }))}
          />
        </div>

        <div className={schoolBusUi.subtlePanel}>
          <div className='mb-4'>
            <p className='text-sm font-semibold text-slate-950'>
              Điểm đầu và điểm cuối cố định
            </p>
            <p className='mt-1 text-xs text-slate-500'>
              Tuyến cần xác định rõ xe bắt đầu và kết thúc ở đâu.
            </p>
          </div>
          <div className='grid gap-4 md:grid-cols-2'>
            <SelectField
              form={form}
              name='startLocationType'
              label='Vị trí bắt đầu'
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
                label='Bãi xe bắt đầu'
                options={depots.map((depot) => ({
                  value: String(depot.id),
                  label: depot.name,
                }))}
              />
            ) : (
              <ReadOnlyField
                label='Trường bắt đầu'
                value={(() => {
                  const found = schools.find(
                    (school) => school.id === schoolId
                  );
                  return found ? found.label : 'Chọn trường';
                })()}
              />
            )}

            <SelectField
              form={form}
              name='endLocationType'
              label='Vị trí kết thúc'
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
                label='Bãi xe kết thúc'
                options={depots.map((depot) => ({
                  value: String(depot.id),
                  label: depot.name,
                }))}
              />
            ) : (
              <ReadOnlyField
                label='Trường kết thúc'
                value={(() => {
                  const found = schools.find(
                    (school) => school.id === schoolId
                  );
                  return found ? found.label : 'Chọn trường';
                })()}
              />
            )}
          </div>
          {depots.length === 0 &&
          (startLocationType === 'DEPOT' || endLocationType === 'DEPOT') ? (
            <p className='mt-3 text-xs font-medium text-amber-700'>
              Chưa có bãi xe. Hãy tạo bãi xe trong mục Trường học / Bãi xe trước khi
              lưu tuyến này.
            </p>
          ) : null}
        </div>

        <FormField
          control={form.control}
          name='planningNotes'
          render={({ field }) => (
            <FormItem>
              <FormLabel>Ghi chú lập tuyến</FormLabel>
              <FormControl>
                <Textarea {...field} value={field.value || ''} />
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
              Hủy
            </Button>
          ) : null}
          <Button
            type='submit'
            className={schoolBusUi.primaryButton}
            disabled={isLoading}
          >
            {isLoading ? 'Đang lưu...' : submitLabel}
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
      title='Từ chối yêu cầu xe bus'
      description='Nhập lý do từ chối trước khi đóng yêu cầu này.'
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
                <FormLabel>Lý do</FormLabel>
                <FormControl>
                  <Textarea {...field} value={field.value || ''} />
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
              Hủy
            </Button>
            <Button
              type='submit'
              className={schoolBusUi.dangerButton}
              disabled={isLoading}
            >
              {isLoading ? 'Đang từ chối...' : 'Từ chối yêu cầu'}
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
  buses: SchoolBusDropdownOption[];
  drivers: SchoolBusDropdownOption[];
  attendants: SchoolBusDropdownOption[];
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
      busId: initialData?.busId || buses[0]?.id || 0,
      driverId: initialData?.driverId || drivers[0]?.id || 0,
      attendantId: initialData?.attendantId
        ? String(initialData.attendantId)
        : '',
      assignmentNote: '',
      reason: '',
      isActive: true,
    },
  });

  React.useEffect(() => {
    form.reset({
      busId: initialData?.busId || buses[0]?.id || 0,
      driverId: initialData?.driverId || drivers[0]?.id || 0,
      attendantId: initialData?.attendantId
        ? String(initialData.attendantId)
        : '',
      assignmentNote: '',
      reason: '',
      isActive: true,
    });
  }, [form, initialData, buses, drivers]);

  return (
    <SchoolBusFormDialog
      open={open}
      onOpenChange={onOpenChange}
      title='Phân công nguồn lực cho tuyến'
      description='Chọn tài xế và phụ xe nếu có cho tuyến này.'
    >
      <Form {...form}>
        <form
          onSubmit={form.handleSubmit(async (values) =>
            onSubmit({
              busId: values.busId,
              driverId: values.driverId,
              attendantId: values.attendantId
                ? Number(values.attendantId)
                : null,
              assignmentNote: values.assignmentNote || null,
              reason: values.reason || null,
              isActive: values.isActive,
            })
          )}
          className='space-y-4'
        >
          <div className='grid gap-4 md:grid-cols-2'>
            <SelectField
              form={form}
              name='busId'
              label='Xe'
              options={buses.map((bus) => ({
                value: String(bus.id),
                label: bus.label,
              }))}
            />
            <SelectField
              form={form}
              name='driverId'
              label='Tài xế'
              options={drivers.map((driver) => ({
                value: String(driver.id),
                label: driver.label,
              }))}
            />
            <SelectField
              form={form}
              name='attendantId'
              label='Phụ xe'
              allowEmpty
              emptyValue='__none__'
              emptyLabel='Chưa có phụ xe'
              options={attendants.map((attendant) => ({
                value: String(attendant.id),
                label: attendant.label,
              }))}
            />
          </div>

          <div className='grid gap-4 md:grid-cols-2'>
            <TextField
              form={form}
              name='assignmentNote'
              label='Ghi chú phân công'
            />
            <TextField form={form} name='reason' label='Lý do thay đổi' />
          </div>

          <div className='flex justify-end gap-2 border-t pt-4'>
            <Button
              type='button'
              variant='outline'
              className={schoolBusUi.outlineButton}
              onClick={() => onOpenChange(false)}
            >
              Hủy
            </Button>
            <Button
              type='submit'
              className={schoolBusUi.primaryButton}
              disabled={isLoading}
            >
              {isLoading ? 'Đang phân công...' : 'Phân công tuyến'}
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
              <Input
                {...field}
                type={type}
                value={(field.value as string) || ''}
              />
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
  emptyText,
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
  emptyText?: string;
}) {
  const selectOptions = React.useMemo(() => {
    // Pass through description so SchoolBusSelect renders it as a second line in the dropdown
    const list = options.map((opt) => ({
      label: opt.label,
      value: opt.value,
      description: opt.description,
    }));
    if (allowEmpty) {
      list.unshift({
        label: emptyLabel,
        value: emptyValue,
        description: undefined,
      });
    }
    return list;
  }, [options, allowEmpty, emptyLabel, emptyValue]);

  const isSearchable = searchable || options.length > 6;

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
              value={String(field.value || (allowEmpty ? emptyValue : ''))}
              onChange={(value) => {
                const val = value === emptyValue ? '' : value;
                field.onChange(val);
                if (onChange) {
                  onChange(val);
                }
              }}
              placeholder={placeholder || `Chọn ${label.toLowerCase()}`}
              options={selectOptions}
              searchable={isSearchable}
              emptyText={emptyText}
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
