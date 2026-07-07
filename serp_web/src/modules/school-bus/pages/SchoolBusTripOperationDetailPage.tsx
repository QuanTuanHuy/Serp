'use client';

import * as React from 'react';
import Link from 'next/link';
import {
  CalendarDays,
  CheckCircle2,
  PlayCircle,
  XCircle,
  Warehouse,
  GraduationCap,
  BusFront,
  Users,
  MapPin,
  Route,
  Clock,
  ArrowLeft,
  Bell,
  Play,
  SkipForward,
  User,
  Search,
  CheckSquare,
} from 'lucide-react';
import { toast } from 'sonner';
import type { Client, StompSubscription } from '@stomp/stompjs';
import {
  schoolBusApi,
  useArriveTripStopMutation,
  useCancelTripMutation,
  useCompleteTripMutation,
  useDepartTripStopMutation,
  useGetTripAttendanceStudentsQuery,
  useGetTripOperationOverviewQuery,
  useGetTripRecentAttendanceQuery,
  useSkipTripStopMutation,
  useStartTripMutation,
  useStartBoardingTripStopMutation,
  useAbsentTripStudentMutation,
  useBoardTripStudentMutation,
  useDropoffTripStudentMutation,
  useNoShowTripStudentMutation,
  useNotServedTripStudentMutation,
  useBatchAttendanceTripStopMutation,
} from '../api/schoolBusApi';
import { useAppDispatch } from '@/lib/store';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import {
  Button,
  Input,
  Badge,
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  SheetDescription,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { formatDate, formatDateTime } from '../utils';
import type {
  ApiResponse,
  TripAttendanceStopItem,
  TripAttendanceStudentItem,
  SchoolBusTripOperationAction,
} from '../types';
import { TripMap } from '../components/map/TripMap';
import { MapMarkerVisibilityProvider } from '../components/map/MapMarkerVisibilityContext';
import { useSchoolBusAccess } from '../security/schoolBusAccess';
import { schoolBusUi } from '../theme';
import {
  directionLabel,
  operationEventLabel,
  tripStatusLabel,
  tripStopStatusLabel,
  tripStudentStatusLabel,
} from '../schoolBusLabels';

// -- Helpers ------------------------------------------------------------------

function stopTypeLabel(stop: TripAttendanceStopItem): string {
  const { stopPurpose, locationType } = stop;
  if (stopPurpose === 'START_TERMINAL') {
    return locationType === 'SCHOOL'
      ? 'Trường học - Điểm đầu tuyến'
      : 'Bãi xe - Điểm đầu tuyến';
  }
  if (stopPurpose === 'END_TERMINAL') {
    return locationType === 'SCHOOL'
      ? 'Trường học - Điểm cuối tuyến'
      : 'Bãi xe - Điểm cuối tuyến';
  }
  if (stopPurpose === 'PICKUP') return 'Điểm đón';
  if (stopPurpose === 'DROPOFF') return 'Điểm trả';
  return locationType || 'Điểm dừng';
}

const statusMap: Record<string, { label: string; className: string }> = {
  PLANNED: {
    label: tripStatusLabel.PLANNED,
    className: 'border-slate-200 bg-slate-50 text-slate-600 hover:bg-slate-50',
  },
  ASSIGNED: {
    label: tripStatusLabel.ASSIGNED,
    className: 'border-amber-200 bg-amber-50 text-amber-700 hover:bg-amber-50',
  },
  IN_PROGRESS: {
    label: tripStatusLabel.IN_PROGRESS,
    className: 'border-blue-200 bg-blue-55 text-blue-700 hover:bg-blue-55',
  },
  COMPLETED: {
    label: tripStatusLabel.COMPLETED,
    className:
      'border-emerald-250 bg-emerald-50 text-emerald-700 hover:bg-emerald-50',
  },
  CANCELLED: {
    label: tripStatusLabel.CANCELLED,
    className: 'border-red-200 bg-red-50 text-red-700 hover:bg-red-50',
  },
  // Stop statuses:
  PENDING: {
    label: tripStopStatusLabel.PENDING,
    className: 'border-slate-200 bg-slate-50 text-slate-500 hover:bg-slate-50',
  },
  ARRIVED: {
    label: tripStopStatusLabel.ARRIVED,
    className: 'border-blue-200 bg-blue-50 text-blue-600 hover:bg-blue-50',
  },
  BOARDING: {
    label: tripStopStatusLabel.BOARDING,
    className:
      'border-indigo-250 bg-indigo-50 text-indigo-700 hover:bg-indigo-50',
  },
  DEPARTED: {
    label: tripStopStatusLabel.DEPARTED,
    className:
      'border-emerald-250 bg-emerald-50 text-emerald-700 hover:bg-emerald-50',
  },
  SKIPPED: {
    label: tripStopStatusLabel.SKIPPED,
    className:
      'border-slate-200 bg-slate-100 text-slate-400 hover:bg-slate-100',
  },
  BOARDED: {
    label: tripStudentStatusLabel.BOARDED,
    className: 'border-blue-150 bg-blue-50 text-blue-650',
  },
  ABSENT: {
    label: tripStudentStatusLabel.ABSENT,
    className: 'border-red-200 bg-red-50 text-red-700',
  },
  DROPPED_OFF: {
    label: tripStudentStatusLabel.DROPPED_OFF,
    className: 'border-emerald-200 bg-emerald-50 text-emerald-650',
  },
  NO_SHOW: {
    label: tripStudentStatusLabel.NO_SHOW,
    className: 'border-red-250 bg-red-50 text-red-650',
  },
  NOT_SERVED: {
    label: tripStudentStatusLabel.NOT_SERVED,
    className: 'border-slate-200 bg-slate-50 text-slate-400',
  },
  // Event types
  TRIP_STARTED: {
    label: operationEventLabel.TRIP_STARTED,
    className: 'border-blue-250 bg-blue-50 text-blue-700',
  },
  TRIP_COMPLETED: {
    label: operationEventLabel.TRIP_COMPLETED,
    className: 'border-emerald-250 bg-emerald-50 text-emerald-700',
  },
  TRIP_CANCELLED: {
    label: operationEventLabel.TRIP_CANCELLED,
    className: 'border-red-250 bg-red-50 text-red-700',
  },
  STOP_ARRIVED: {
    label: operationEventLabel.STOP_ARRIVED,
    className: 'border-blue-200 bg-blue-50 text-blue-600',
  },
  STOP_BOARDING_STARTED: {
    label: operationEventLabel.STOP_BOARDING_STARTED,
    className: 'border-indigo-200 bg-indigo-50 text-indigo-700',
  },
  STOP_DEPARTED: {
    label: operationEventLabel.STOP_DEPARTED,
    className: 'border-emerald-200 bg-emerald-55 text-emerald-700',
  },
  STOP_SKIPPED: {
    label: operationEventLabel.STOP_SKIPPED,
    className: 'border-slate-200 bg-slate-100 text-slate-450',
  },
  STUDENT_BOARDED: {
    label: operationEventLabel.STUDENT_BOARDED,
    className: 'border-blue-150 bg-blue-50 text-blue-650',
  },
  STUDENT_ABSENT: {
    label: operationEventLabel.STUDENT_ABSENT,
    className: 'border-red-200 bg-red-50 text-red-700',
  },
  STUDENT_NO_SHOW: {
    label: operationEventLabel.STUDENT_NO_SHOW,
    className: 'border-red-250 bg-red-50 text-red-650',
  },
  STUDENT_DROPPED_OFF: {
    label: operationEventLabel.STUDENT_DROPPED_OFF,
    className: 'border-emerald-200 bg-emerald-50 text-emerald-650',
  },
  STUDENT_NOT_SERVED: {
    label: operationEventLabel.STUDENT_NOT_SERVED,
    className: 'border-slate-200 bg-slate-50 text-slate-400',
  },
};

const renderFriendlyBadge = (status: string) => {
  const normalized = (status || '').toUpperCase();
  const config = statusMap[normalized] || {
    label: normalized,
    className: 'border-slate-200 bg-slate-50 text-slate-600 hover:bg-slate-50',
  };
  return (
    <Badge
      className={cn(
        'rounded-full px-2 py-0.2 text-[10px] font-bold shadow-none border shrink-0 hover:bg-transparent',
        config.className
      )}
    >
      {config.label}
    </Badge>
  );
};

const getFriendlyDirection = (dir?: string | null) => {
  if (dir) return directionLabel[dir] || dir;
  return dir || '';
};

interface SchoolBusTripOperationDetailPageProps {
  tripId: number;
}

const ACTIVE_POLL_INTERVAL_MS = 10_000;
const IDLE_POLL_INTERVAL_MS = 25_000;
const ATTENDANCE_POLL_INTERVAL_MS = 30_000;
const DEFAULT_RATE_LIMIT_BACKOFF_MS = 60_000;

type PollingError = {
  status?: number;
  data?: {
    retryAfterSeconds?: number;
  };
};

function getRateLimitBackoffMs(error: PollingError | undefined): number | null {
  if (error?.status !== 429) return null;

  const retryAfterSeconds = error.data?.retryAfterSeconds;
  return retryAfterSeconds && retryAfterSeconds > 0
    ? retryAfterSeconds * 1000
    : DEFAULT_RATE_LIMIT_BACKOFF_MS;
}

export function SchoolBusTripOperationDetailPage({
  tripId,
}: SchoolBusTripOperationDetailPageProps) {
  const access = useSchoolBusAccess();
  const dispatch = useAppDispatch();
  // -- State ------------------------------------------------------------------
  const [selectedStopId, setSelectedStopId] = React.useState<number | null>(
    null
  );
  const [isAttendanceDrawerOpen, setIsAttendanceDrawerOpen] =
    React.useState(false);
  const [searchQuery, setSearchQuery] = React.useState('');
  const [showSkipForm, setShowSkipForm] = React.useState(false);
  const [skipReason, setSkipReason] = React.useState('');
  const [showCancelForm, setShowCancelForm] = React.useState(false);
  const [cancelReason, setCancelReason] = React.useState('');

  const [lastUpdated, setLastUpdated] = React.useState<string>('');
  const [isTabVisible, setIsTabVisible] = React.useState(true);
  const [pollInterval, setPollInterval] = React.useState(IDLE_POLL_INTERVAL_MS);
  const [isSyncDelayed, setIsSyncDelayed] = React.useState(false);

  // -- Queries ----------------------------------------------------------------
  const {
    data: overviewData,
    refetch: refetchOverview,
  } = useGetTripOperationOverviewQuery(tripId, {
    refetchOnMountOrArgChange: true,
  });
  const { data: eventsData, refetch: refetchEvents } =
    useGetTripRecentAttendanceQuery(
      { tripId, size: 24 },
      { refetchOnMountOrArgChange: true }
    );
  const {
    data: drawerStudentsData,
    isFetching: loadingDrawerStudents,
  } = useGetTripAttendanceStudentsQuery(
    { tripId, routeStopId: selectedStopId },
    { skip: !isAttendanceDrawerOpen, refetchOnMountOrArgChange: true }
  );

  // -- Derived ----------------------------------------------------------------
  const manifest = overviewData?.data || null;
  const summary = manifest?.summary || null;
  const restEvents = eventsData?.data || [];

  const drawerStudents = drawerStudentsData?.data || [];
  const tripStatus = manifest?.tripStatus || null;
  const tripIsActive = [
    'IN_PROGRESS',
    'BOARDING',
    'DROPOFF',
    'ARRIVED',
  ].includes(tripStatus || '');
  const tripIsCompleted = tripStatus === 'COMPLETED';
  const tripIsCancelled = tripStatus === 'CANCELLED';
  const isOutbound =
    manifest?.routeDirection === 'OUTBOUND';
  const tripCode = manifest?.tripCode || `Chuyến #${tripId}`;
  const routeCode = manifest?.routeCode || '';
  const routeName = manifest?.routeName || '';

  // -- Polling implementation -------------------------------------------------
  const isRefreshingRef = React.useRef(false);
  const lastAttendanceRefreshRef = React.useRef(Date.now());

  const refreshTripDetailData = React.useCallback(
    async (forceAttendance = false) => {
      if (isRefreshingRef.current) return;
      isRefreshingRef.current = true;
      try {
        const now = Date.now();
        const shouldRefreshAttendance =
          forceAttendance ||
          now - lastAttendanceRefreshRef.current >= ATTENDANCE_POLL_INTERVAL_MS;

        const overviewResult = await refetchOverview();
        const attendanceResult = shouldRefreshAttendance
          ? await refetchEvents()
          : undefined;

        if (attendanceResult && !attendanceResult.error) {
          lastAttendanceRefreshRef.current = now;
        }

        const manifestBackoff = getRateLimitBackoffMs(
          overviewResult.error as PollingError | undefined
        );
        const attendanceBackoff = getRateLimitBackoffMs(
          attendanceResult?.error as PollingError | undefined
        );
        const backoffMs = Math.max(
          manifestBackoff || 0,
          attendanceBackoff || 0
        );

        if (backoffMs > 0) {
          setIsSyncDelayed(true);
          setPollInterval(backoffMs);
          return;
        }

        if (!overviewResult.error) {
          setLastUpdated(new Date().toLocaleTimeString());
          setIsSyncDelayed(false);
          setPollInterval(
            tripIsActive ? ACTIVE_POLL_INTERVAL_MS : IDLE_POLL_INTERVAL_MS
          );
        }
      } finally {
        isRefreshingRef.current = false;
      }
    },
    [refetchOverview, refetchEvents, tripIsActive]
  );

  // Track page visibility
  React.useEffect(() => {
    const handleVisibilityChange = () => {
      const visible = document.visibilityState === 'visible';
      setIsTabVisible(visible);
      if (visible) {
        void refreshTripDetailData(true);
      }
    };
    document.addEventListener('visibilitychange', handleVisibilityChange);
    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, [refreshTripDetailData, tripIsCompleted, tripIsCancelled, tripIsActive]);

  // Polling effect
  React.useEffect(() => {
    if (tripIsCompleted || tripIsCancelled || !isTabVisible) return;

    const intervalId = setInterval(() => {
      void refreshTripDetailData();
    }, pollInterval);

    return () => clearInterval(intervalId);
  }, [
    refreshTripDetailData,
    isTabVisible,
    pollInterval,
    tripIsCompleted,
    tripIsCancelled,
  ]);

  React.useEffect(() => {
    if (overviewData) {
      setLastUpdated(new Date().toLocaleTimeString());
    }
  }, [overviewData]);

  React.useEffect(() => {
    if (!isSyncDelayed && !tripIsCompleted && !tripIsCancelled) {
      setPollInterval(
        tripIsActive ? ACTIVE_POLL_INTERVAL_MS : IDLE_POLL_INTERVAL_MS
      );
    }
  }, [isSyncDelayed, tripIsActive, tripIsCompleted, tripIsCancelled]);

  const sortedEvents = React.useMemo(() => {
    return [...restEvents].sort(
      (a: any, b: any) =>
        new Date(b.recordedAt).getTime() - new Date(a.recordedAt).getTime()
    );
  }, [restEvents]);

  // -- Auto-advance to current (first non-done) stop -------------------------
  React.useEffect(() => {
    if (!manifest?.stops?.length) return;
    const selected = manifest.stops.find(
      (s) => s.routeStopId === selectedStopId
    );
    if (
      !selected ||
      selected.stopStatus === 'DEPARTED' ||
      selected.stopStatus === 'SKIPPED'
    ) {
      const next = manifest.stops.find(
        (s) => s.stopStatus !== 'DEPARTED' && s.stopStatus !== 'SKIPPED'
      );
      setSelectedStopId(
        next?.routeStopId ||
          manifest.stops[manifest.stops.length - 1]?.routeStopId ||
          null
      );
    }
  }, [manifest, selectedStopId]);

  // -- Current & Next Stops Inferred ------------------------------------------
  const opSummary = React.useMemo(() => {
    if (!manifest?.stops?.length) return null;
    const stops = manifest.stops;
    const total = stops.length;
    const done =
      tripStatus === 'COMPLETED'
        ? total
        : stops.filter((s) => {
            if (s.stopPurpose === 'START_TERMINAL') {
              return s.stopStatus === 'DEPARTED';
            }
            if (s.stopPurpose === 'END_TERMINAL') {
              return (
                s.stopStatus === 'ARRIVED' ||
                s.stopStatus === 'DEPARTED' ||
                s.stopStatus === 'SKIPPED'
              );
            }
            return s.stopStatus === 'DEPARTED' || s.stopStatus === 'SKIPPED';
          }).length;

    let current = null;
    let next = null;

    if (tripStatus === 'COMPLETED') {
      current = stops[stops.length - 1];
    } else if (tripStatus === 'PLANNED' || tripStatus === 'ASSIGNED') {
      current = stops[0];
      if (stops.length > 1) next = stops[1];
    } else if (tripStatus === 'IN_PROGRESS') {
      const active = stops.find(
        (s) => s.stopStatus === 'ARRIVED' || s.stopStatus === 'BOARDING'
      );
      if (active) {
        current = active;
      } else {
        const firstNonDone = stops.find(
          (s) => s.stopStatus !== 'DEPARTED' && s.stopStatus !== 'SKIPPED'
        );
        current = firstNonDone || stops[stops.length || 1];
      }
      next = stops.find((s) => s.stopStatus === 'PENDING');
    }

    return { total, done, current, next };
  }, [manifest, tripStatus]);

  // -- Derived logic for complete trip and timeline sequence ------------------
  const sortedStops = React.useMemo(() => {
    if (!manifest?.stops) return [];
    return [...manifest.stops].sort((a, b) => a.stopOrder - b.stopOrder);
  }, [manifest?.stops]);

  const firstUnfinishedStop = React.useMemo(() => {
    return (
      sortedStops.find(
        (s) => s.stopStatus !== 'DEPARTED' && s.stopStatus !== 'SKIPPED'
      ) || null
    );
  }, [sortedStops]);

  const allStopsFinished = React.useMemo(() => {
    if (sortedStops.length === 0) return false;
    return sortedStops.every((s, idx) => {
      const isEndTerminal = idx === sortedStops.length - 1;
      if (isEndTerminal) {
        return (
          s.stopStatus === 'ARRIVED' ||
          s.stopStatus === 'BOARDING' ||
          s.stopStatus === 'DEPARTED'
        );
      } else {
        return s.stopStatus === 'DEPARTED' || s.stopStatus === 'SKIPPED';
      }
    });
  }, [sortedStops]);

  const hasPlannedStudents = React.useMemo(() => {
    return (summary?.planned || 0) > 0;
  }, [summary?.planned]);

  const canCompleteTrip =
    tripStatus === 'IN_PROGRESS' && allStopsFinished && !hasPlannedStudents;

  // -- Attendance Workspace Derived States ------------------------------------
  const selectedStop = React.useMemo(() => {
    if (!manifest?.stops) return null;
    return manifest.stops.find((s) => s.routeStopId === selectedStopId) || null;
  }, [manifest?.stops, selectedStopId]);

  const stopStatus = selectedStop?.stopStatus || null;
  const isStopActionable = tripIsActive && stopStatus === 'BOARDING';
  const isDepotStop = selectedStop?.locationType === 'DEPOT';
  const isPickupActionStop =
    selectedStop?.stopPurpose === 'PICKUP' ||
    (selectedStop?.stopPurpose === 'START_TERMINAL' &&
      selectedStop?.locationType === 'SCHOOL' &&
      !isOutbound);
  const isDropoffActionStop =
    selectedStop?.stopPurpose === 'DROPOFF' ||
    (selectedStop?.stopPurpose === 'END_TERMINAL' &&
      selectedStop?.locationType === 'SCHOOL' &&
      isOutbound);

  const studentsAtStop = React.useMemo<TripAttendanceStudentItem[]>(() => {
    if (!selectedStop) return [];
    let filtered = drawerStudents;

    if (searchQuery.trim() !== '') {
      const q = searchQuery.toLowerCase();
      filtered = filtered.filter(
        (s) =>
          (s.studentName || '').toLowerCase().includes(q) ||
          (s.studentCode || '').toLowerCase().includes(q)
      );
    }

    return filtered;
  }, [drawerStudents, selectedStop, searchQuery]);

  // -- Mutations --------------------------------------------------------------
  const [startTrip, { isLoading: starting }] = useStartTripMutation();
  const [completeTrip, { isLoading: completing }] = useCompleteTripMutation();
  const [cancelTrip, { isLoading: cancelling }] = useCancelTripMutation();
  const [arriveStop, { isLoading: arriving }] = useArriveTripStopMutation();
  const [departStop, { isLoading: departing }] = useDepartTripStopMutation();
  const [skipStop, { isLoading: skipping }] = useSkipTripStopMutation();
  const [startBoardingStop, { isLoading: boarding }] =
    useStartBoardingTripStopMutation();
  const [boardStudent, { isLoading: boardingStudent }] =
    useBoardTripStudentMutation();
  const [dropoffStudent, { isLoading: droppingOffStudent }] =
    useDropoffTripStudentMutation();
  const [absentStudent, { isLoading: markingAbsent }] =
    useAbsentTripStudentMutation();
  const [noShowStudent, { isLoading: markingNoShow }] =
    useNoShowTripStudentMutation();
  const [notServedStudent, { isLoading: markingNotServed }] =
    useNotServedTripStudentMutation();
  const [batchAttendance, { isLoading: batchingAttendance }] =
    useBatchAttendanceTripStopMutation();

  // -- Batch selection state --
  const [selectedStudentIds, setSelectedStudentIds] = React.useState<Set<number>>(new Set());

  const isActing =
    starting ||
    completing ||
    cancelling ||
    arriving ||
    departing ||
    skipping ||
    boarding ||
    boardingStudent ||
    droppingOffStudent ||
    markingAbsent ||
    markingNoShow ||
    markingNotServed ||
    batchingAttendance;

  const patchTripOperationOverview = React.useCallback(
    (action?: SchoolBusTripOperationAction | null) => {
      if (!action) return;

      dispatch(
        schoolBusApi.util.updateQueryData(
          'getTripOperationOverview',
          tripId,
          (draft) => {
            if (!draft?.data) return;
            if (action.tripStatus) {
              draft.data.tripStatus = action.tripStatus;
            }

            if (action.routeStopId == null) return;
            const stop = draft.data.stops?.find(
              (item) => item.routeStopId === action.routeStopId
            );
            if (!stop) return;

            if (action.stopStatus) {
              stop.stopStatus = action.stopStatus;
            }
            if (action.actualArrivalTime) {
              stop.actualArrivalTime = action.actualArrivalTime;
            }
            if (action.actualDepartureTime) {
              stop.actualDepartureTime = action.actualDepartureTime;
            }
          }
        )
      );
      setLastUpdated(new Date().toLocaleTimeString());
    },
    [dispatch, tripId]
  );

  const act = async <T,>(label: string, fn: () => Promise<T>) => {
    try {
      const result = await fn();
      toast.success(`Đã hoàn tất: ${label}`);
      return result;
    } catch (e: unknown) {
      const err = e as { status?: number; data?: { message?: string } };
      toast.error(
        err?.status === 429
          ? 'Hệ thống đang bận. Vui lòng chờ vài giây rồi thử lại.'
          : (err?.data?.message || `Không thể thực hiện: ${label}`)
      );
      return undefined;
    }
  };

  // -- Actions ----------------------------------------------------------------
  const extractAction = (
    response?: ApiResponse<SchoolBusTripOperationAction>
  ) => response?.data || null;

  const handleStart = () => {
    void act('Bắt đầu chuyến', () => startTrip(tripId).unwrap()).then((response) => {
      patchTripOperationOverview(extractAction(response));
    });
  };
  const handleComplete = () =>
    act('Hoàn thành chuyến', () => completeTrip({ id: tripId }).unwrap());
  const handleStartBoarding = (stopId: number) => {
    void act('Bắt đầu đón/trả', async () => {
      const response = await startBoardingStop({ tripId, routeStopId: stopId }).unwrap();
      setSelectedStopId(stopId);
      setIsAttendanceDrawerOpen(true);
      return response;
    }).then((response) => {
      patchTripOperationOverview(extractAction(response));
    });
  };
  const handleCancel = () => {
    if (!cancelReason.trim()) return;
    act('Hủy chuyến', () =>
      cancelTrip({ id: tripId, body: { reason: cancelReason } }).unwrap()
    );
    setShowCancelForm(false);
    setCancelReason('');
  };

  const handleArrive = (stopId: number) => {
    void act('Đến điểm dừng', () =>
      arriveStop({ tripId, routeStopId: stopId }).unwrap()
    ).then((response) => {
      patchTripOperationOverview(extractAction(response));
    });
  };
  const handleDepart = (stopId: number) => {
    void act('Rời điểm dừng', () =>
      departStop({ tripId, routeStopId: stopId }).unwrap()
    ).then((response) => {
      patchTripOperationOverview(extractAction(response));
    });
  };
  const handleSkip = (stopId: number) => {
    if (!skipReason.trim()) return;
    act('Bỏ qua điểm dừng', () =>
      skipStop({ tripId, routeStopId: stopId, reason: skipReason }).unwrap()
    );
    setShowSkipForm(false);
    setSkipReason('');
  };

  const handleBoard = (s: TripAttendanceStudentItem) => {
    if (!selectedStopId) return;
    act(`Ghi nhận lên xe ${s.studentName || ''}`, () =>
      boardStudent({
        tripId,
        body: { routeStopId: selectedStopId, studentId: s.studentId },
      }).unwrap()
    );
  };
  const handleDropoff = (s: TripAttendanceStudentItem) => {
    if (!selectedStopId) return;
    act(`Ghi nhận xuống xe ${s.studentName || ''}`, () =>
      dropoffStudent({
        tripId,
        body: { routeStopId: selectedStopId, studentId: s.studentId },
      }).unwrap()
    );
  };
  const handleAbsent = (s: TripAttendanceStudentItem) => {
    if (!selectedStopId) return;
    act(`Ghi nhận vắng mặt ${s.studentName || ''}`, () =>
      absentStudent({
        tripId,
        body: { routeStopId: selectedStopId, studentId: s.studentId },
      }).unwrap()
    );
  };
  const handleNoShow = (s: TripAttendanceStudentItem) => {
    if (!selectedStopId) return;
    act(`Ghi nhận không có mặt tại điểm đón ${s.studentName || ''}`, () =>
      noShowStudent({
        tripId,
        body: { routeStopId: selectedStopId, studentId: s.studentId },
      }).unwrap()
    );
  };
  const handleNotServed = (s: TripAttendanceStudentItem) => {
    if (!selectedStopId) return;
    act(`Ghi nhận chưa phục vụ ${s.studentName || ''}`, () =>
      notServedStudent({
        tripId,
        body: { routeStopId: selectedStopId, studentId: s.studentId },
      }).unwrap()
    );
  };

  // -- Batch selection helpers --
  const toggleStudentSelection = (studentId: number) => {
    setSelectedStudentIds((prev) => {
      const next = new Set(prev);
      if (next.has(studentId)) next.delete(studentId);
      else next.add(studentId);
      return next;
    });
  };

  const plannedStudentsAtStop = React.useMemo(() => {
    return studentsAtStop.filter((s) => (s.status || 'PLANNED').toUpperCase() === 'PLANNED');
  }, [studentsAtStop]);

  const toggleSelectAll = () => {
    const allIds = plannedStudentsAtStop.map((s) => s.studentId);
    const allSelected = allIds.every((id) => selectedStudentIds.has(id));
    if (allSelected) {
      setSelectedStudentIds(new Set());
    } else {
      setSelectedStudentIds(new Set(allIds));
    }
  };

  const handleBatchAction = (action: 'MARK_BOARDED' | 'MARK_ABSENT' | 'MARK_NO_SHOW') => {
    if (!selectedStopId || selectedStudentIds.size === 0) return;
    const label =
      action === 'MARK_BOARDED'
        ? 'lên xe'
        : action === 'MARK_ABSENT'
          ? 'vắng mặt'
          : 'không có mặt tại điểm đón';
    act(`Ghi nhận hàng loạt ${label} (${selectedStudentIds.size} học sinh)`, async () => {
      await batchAttendance({
        tripId,
        stopId: selectedStopId,
        body: { action, studentIds: Array.from(selectedStudentIds) },
      }).unwrap();
      setSelectedStudentIds(new Set());
    });
  };

  return (
    <MapMarkerVisibilityProvider>
      <SchoolBusPageShell
        title={tripCode}
        description={
          access.isParentOnly
            ? 'Theo dõi chuyến đi và tiến độ vận hành của học sinh.'
            : 'Theo dõi vận hành chuyến xe, vòng đời điểm dừng và nhật ký thao tác.'
        }
        breadcrumb={
          <SchoolBusBreadcrumb
            items={
              access.isParentOnly
                ? [
                    { label: 'Xe bus trường học', href: '/school-bus/dashboard' },
                    {
                      label: 'Theo dõi chuyến học sinh',
                      href: '/school-bus/trips',
                    },
                    { label: tripCode, current: true },
                  ]
                : [
                    { label: 'Điều phối xe buýt', href: '/school-bus/dispatch' },
                    { label: 'Vận hành chuyến', href: '/school-bus/trips' },
                    { label: tripCode, current: true },
                  ]
            }
          />
        }
      >
        <div className='flex flex-col gap-6'>
          {/* Back navigation & banners */}
          <div className='flex flex-col gap-3'>
            <div className='flex items-center justify-between'>
              <Button
                variant='outline'
                size='sm'
                className='rounded-full h-8 px-3 font-semibold'
                asChild
              >
                <Link href='/school-bus/trips'>
                  <ArrowLeft className='h-3.5 w-3.5 mr-1.5' />
                  {access.isParentOnly
                    ? 'Quay lại theo dõi chuyến'
                    : 'Quay lại vận hành chuyến'}
                </Link>
              </Button>
            </div>

            {tripIsCompleted && (
              <div className='flex items-center gap-2.5 bg-emerald-50 border border-emerald-100 text-emerald-800 px-4 py-3 rounded-2xl text-xs font-semibold shadow-xs'>
                <CheckCircle2 className='h-4.5 w-4.5 text-emerald-600 shrink-0' />
                <span>Chuyến đã hoàn thành - thao tác vận hành đã khóa.</span>
              </div>
            )}

            {tripIsCancelled && (
              <div className='flex items-center gap-2.5 bg-red-50 border border-red-100 text-red-800 px-4 py-3 rounded-2xl text-xs font-semibold shadow-xs'>
                <XCircle className='h-4.5 w-4.5 text-red-600 shrink-0' />
                <span>
                  Chuyến đã hủy. Lý do:{' '}
                  {(manifest as any)?.cancellationReason ||
                    'Chưa có'}
                </span>
              </div>
            )}

            {isSyncDelayed && (
              <div className='flex items-center gap-2.5 bg-amber-50 border border-amber-100 text-amber-800 px-4 py-3 rounded-2xl text-xs font-semibold shadow-xs'>
                <Clock className='h-4.5 w-4.5 text-amber-600 shrink-0' />
                <span>
                  Đồng bộ đang chậm. Hệ thống sẽ tự thử lại
                  {lastUpdated ? ` - cập nhật gần nhất: ${lastUpdated}` : '...'}
                </span>
              </div>
            )}
          </div>

          {/* Trip Summary Card */}
          {manifest && (
            <div className='bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-4'>
              <div className='flex items-center justify-between pb-3 border-b border-slate-100'>
                <div className='flex items-center gap-2.5 min-w-0'>
                  <div className='flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-blue-50 text-blue-700 border border-blue-100/50'>
                    <Route className='h-5 w-5' />
                  </div>
                  <div className='min-w-0'>
                    <h3 className='font-bold text-slate-900 text-sm truncate'>
                      {tripCode}
                    </h3>
                    <p className='text-xs text-slate-400 truncate mt-0.5'>
                      {routeCode} - {routeName}
                    </p>
                  </div>
                </div>
                <div className='flex items-center gap-2'>
                  {lastUpdated && (
                    <span className='text-[10px] font-medium text-slate-400 px-2 py-0.5 rounded-full border border-slate-200 bg-slate-50 shrink-0'>
                      Cập nhật: {lastUpdated}
                    </span>
                  )}
                  {renderFriendlyBadge(tripStatus || '')}
                  <Button
                    size='sm'
                    variant='outline'
                    className='h-7 rounded-lg text-[10px] font-bold border-slate-250 text-slate-700 hover:bg-slate-50 shadow-none'
                    onClick={() => {
                      setSelectedStopId(null);
                      setIsAttendanceDrawerOpen(true);
                    }}
                  >
                    {access.isParentOnly
                      ? 'Xem trạng thái học sinh'
                      : 'Xem danh sách điểm danh'}
                  </Button>
                </div>
              </div>

              <div className='grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-8 gap-y-4 gap-x-6 text-xs'>
                <div className='flex items-start gap-2 min-w-0'>
                  <CalendarDays className='h-4.5 w-4.5 text-slate-400 shrink-0 mt-0.5' />
                  <div className='flex flex-col min-w-0'>
                    <span className='text-slate-400 text-[10px] font-semibold uppercase tracking-wider'>
                      Ngày phục vụ
                    </span>
                    <span className='font-bold text-slate-800 truncate mt-0.5'>
                      {formatDate(manifest.serviceDate || '')}
                    </span>
                  </div>
                </div>

                <div className='flex items-start gap-2 min-w-0'>
                  <Route className='h-4.5 w-4.5 text-indigo-500 shrink-0 mt-0.5' />
                  <div className='flex flex-col min-w-0'>
                    <span className='text-slate-400 text-[10px] font-semibold uppercase tracking-wider'>
                      Chiều tuyến
                    </span>
                    <span className='font-bold text-slate-800 truncate mt-0.5'>
                      {getFriendlyDirection(manifest.routeDirection)}
                    </span>
                  </div>
                </div>

                <div className='flex items-start gap-2 min-w-0'>
                  <Route className='h-4.5 w-4.5 text-slate-400 shrink-0 mt-0.5' />
                  <div className='flex flex-col min-w-0'>
                    <span className='text-slate-400 text-[10px] font-semibold uppercase tracking-wider'>
                      Độ dài tuyến
                    </span>
                    <span className='font-bold text-slate-800 truncate mt-0.5'>
                      {manifest?.distanceKm != null
                        ? `${manifest.distanceKm} km`
                        : '-'}
                    </span>
                  </div>
                </div>

                <div className='flex items-start gap-2 min-w-0'>
                  <Clock className='h-4.5 w-4.5 text-slate-400 shrink-0 mt-0.5' />
                  <div className='flex flex-col min-w-0'>
                    <span className='text-slate-400 text-[10px] font-semibold uppercase tracking-wider'>
                      Thời lượng dự kiến
                    </span>
                    <span className='font-bold text-slate-800 truncate mt-0.5'>
                      {manifest?.durationMin != null
                        ? `${manifest.durationMin} phút`
                        : '-'}
                    </span>
                  </div>
                </div>

                <div className='flex items-start gap-2 min-w-0'>
                  <BusFront className='h-4.5 w-4.5 text-slate-400 shrink-0 mt-0.5' />
                  <div className='flex flex-col min-w-0'>
                    <span className='text-slate-400 text-[10px] font-semibold uppercase tracking-wider'>
                      Xe bus
                    </span>
                    {manifest.busPlateNumber ? (
                      <span className='font-mono font-bold text-slate-800 bg-slate-50 border border-slate-200/80 rounded px-1.5 py-0.2 mt-0.5 w-fit'>
                        {manifest.busPlateNumber}
                      </span>
                    ) : (
                      <span className='font-bold text-amber-600 mt-0.5'>
                        Thiếu xe
                      </span>
                    )}
                  </div>
                </div>

                <div className='flex items-start gap-2 min-w-0'>
                  <User className='h-4.5 w-4.5 text-slate-400 shrink-0 mt-0.5' />
                  <div className='flex flex-col min-w-0'>
                    <span className='text-slate-400 text-[10px] font-semibold uppercase tracking-wider'>
                      Tài xế
                    </span>
                    <span className='font-bold text-slate-800 truncate mt-0.5'>
                      {manifest.driverName || 'Chưa có tài xế assigned'}
                    </span>
                  </div>
                </div>

                <div className='flex items-start gap-2 min-w-0'>
                  <Users className='h-4.5 w-4.5 text-slate-400 shrink-0 mt-0.5' />
                  <div className='flex flex-col min-w-0'>
                    <span className='text-slate-400 text-[10px] font-semibold uppercase tracking-wider'>
                      Phụ xe
                    </span>
                    <span className='font-bold text-slate-800 truncate mt-0.5'>
                      {manifest.attendantName || '-'}
                    </span>
                  </div>
                </div>

                {/* Main operational control actions - only for users who can operate trips */}
                <div className='flex flex-col justify-center sm:col-span-2 lg:col-span-1 gap-2'>
                  {access.canOperateTrip &&
                    !tripIsCompleted &&
                    !tripIsCancelled && (
                      <>
                        {tripStatus === 'PLANNED' || tripStatus === 'ASSIGNED' ? (
                          <Button
                            size='sm'
                            className='bg-[#C81E3A] hover:bg-[#B31B34] text-white rounded-full font-bold shadow-none h-8 px-4 border-0 text-xs shrink-0 w-full'
                            onClick={handleStart}
                            disabled={isActing}
                          >
                            <PlayCircle className='mr-1.5 h-4 w-4' />
                            Bắt đầu chuyến
                          </Button>
                        ) : null}

                        {tripStatus === 'IN_PROGRESS' && (
                          <div
                            className='w-full'
                            title={
                              !canCompleteTrip
                                ? 'Cần hoàn tất các điểm dừng và xử lý trạng thái học sinh trước khi hoàn thành chuyến.'
                                : undefined
                            }
                          >
                            <Button
                              size='sm'
                              className='bg-[#C81E3A] hover:bg-[#B31B34] text-white rounded-full font-bold shadow-none h-8 px-4 border-0 text-xs shrink-0 w-full disabled:opacity-50 disabled:cursor-not-allowed'
                              onClick={handleComplete}
                              disabled={isActing || !canCompleteTrip}
                            >
                              <CheckCircle2 className='mr-1.5 h-4 w-4' />
                              Hoàn thành chuyến
                            </Button>
                            {!canCompleteTrip && (
                              <p className='text-[9px] text-red-500 mt-1 font-semibold text-center leading-tight'>
                                Cần hoàn tất các điểm dừng và xử lý trạng thái học sinh trước khi hoàn thành chuyến.
                              </p>
                            )}
                          </div>
                        )}

                        {showCancelForm ? (
                          <div className='flex flex-col gap-1.5'>
                            <Input
                              value={cancelReason}
                              onChange={(e) => setCancelReason(e.target.value)}
                              placeholder='Lý do hủy chuyến...'
                              className='h-7 text-xs rounded-lg px-2 bg-slate-50'
                            />
                            <div className='flex gap-1 justify-end'>
                              <Button
                                size='sm'
                                variant='ghost'
                                className='h-6 text-[10px] px-2 rounded-lg'
                                onClick={() => setShowCancelForm(false)}
                              >
                                Quay lại
                              </Button>
                              <Button
                                size='sm'
                                className='h-6 text-[10px] rounded-lg bg-red-650 hover:bg-red-700 text-white font-bold px-2'
                                onClick={handleCancel}
                                disabled={!cancelReason.trim() || isActing}
                              >
                                Xác nhận
                              </Button>
                            </div>
                          </div>
                        ) : (
                          <Button
                            size='sm'
                            variant='outline'
                            className='h-8 rounded-full border-red-250 text-red-650 hover:bg-red-50 text-xs font-semibold px-3 w-full'
                            onClick={() => setShowCancelForm(true)}
                            disabled={isActing}
                          >
                            <XCircle size={13} className='mr-1.5 shrink-0' />
                            Hủy chuyến
                          </Button>
                        )}
                      </>
                    )}
                  {/* Readonly badge for non-operators */}
                  {!access.canOperateTrip &&
                    !tripIsCompleted &&
                    !tripIsCancelled && (
                      <span className='inline-flex items-center justify-center rounded-full bg-slate-50 border border-slate-200 px-3 py-1 text-[10px] font-semibold text-slate-400'>
                        Chỉ xem
                      </span>
                    )}
                </div>
              </div>
            </div>
          )}

          {/* Stats & Summaries Row */}
          {(summary || opSummary) && (
            <div className='grid gap-4 md:grid-cols-3'>
              {/* Trip progress */}
              {opSummary && (
                <div className='bg-white border border-slate-200 rounded-2xl p-4 shadow-sm flex flex-col justify-between'>
                  <div>
                    <p className='text-[10px] font-extrabold text-slate-400 uppercase tracking-wider mb-2'>
                      Tiến độ chuyến
                    </p>
                    <div className='flex items-end justify-between mb-2'>
                      <span className='text-2xl font-extrabold text-slate-800'>
                        {opSummary.done}/{opSummary.total}
                      </span>
                      <span className='text-xs font-semibold text-slate-400'>
                        Điểm đã xử lý
                      </span>
                    </div>
                  </div>
                  <div className='w-full bg-slate-100 rounded-full h-2.5 overflow-hidden border border-slate-200/50'>
                    <div
                      className='bg-blue-600 h-full rounded-full transition-all duration-300'
                      style={{
                        width: `${(opSummary.done / opSummary.total) * 100}%`,
                      }}
                    />
                  </div>
                </div>
              )}

              {/* Stop tracking details */}
              {opSummary && (
                <div className='bg-white border border-slate-200 rounded-2xl p-4 shadow-sm flex flex-col justify-between text-xs font-medium text-slate-500'>
                  <div>
                    <p className='text-[10px] font-extrabold text-slate-400 uppercase tracking-wider mb-2'>
                      Trạng thái tuyến suy luận
                    </p>
                    <div className='space-y-1.5'>
                      <div className='flex items-center justify-between'>
                        <span>Vị trí hiện tại:</span>
                        <span className='font-bold text-slate-800 truncate max-w-[150px]'>
                          {opSummary.current
                            ? opSummary.current.displayName
                            : '-'}
                        </span>
                      </div>
                      <div className='flex items-center justify-between'>
                        <span>Điểm tiếp theo:</span>
                        <span className='font-bold text-slate-800 truncate max-w-[150px]'>
                          {opSummary.next ? opSummary.next.displayName : '-'}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {/* Students Summary & Open Attendance */}
              {summary && (
                <div className='bg-white border border-slate-200 rounded-2xl p-4 shadow-sm flex flex-col justify-between'>
                  <div>
                    <p className='text-[10px] font-extrabold text-slate-400 uppercase tracking-wider mb-3'>
                      {access.isParentOnly
                        ? 'Trạng thái di chuyển của học sinh'
                        : 'Điểm danh học sinh'}
                    </p>
                    <div className='grid grid-cols-6 gap-1 text-center divide-x divide-slate-100 mb-3'>
                      <div className='flex flex-col gap-1 min-w-0'>
                        <span className='text-base font-extrabold text-slate-800'>
                          {summary.totalStudents}
                        </span>
                        <span className='text-[9px] text-slate-400 font-semibold truncate'>
                          Tổng
                        </span>
                      </div>
                      <div className='flex flex-col gap-1 min-w-0'>
                        <span className='text-base font-extrabold text-amber-600'>
                          {summary.planned}
                        </span>
                        <span className='text-[9px] text-slate-400 font-semibold truncate'>
                          Chưa điểm danh
                        </span>
                      </div>
                      <div className='flex flex-col gap-1 min-w-0'>
                        <span className='text-base font-extrabold text-blue-600'>
                          {summary.boarded}
                        </span>
                        <span className='text-[9px] text-slate-400 font-semibold truncate'>
                          Đã lên xe
                        </span>
                      </div>
                      <div className='flex flex-col gap-1 min-w-0'>
                        <span className='text-base font-extrabold text-emerald-600'>
                          {summary.droppedOff}
                        </span>
                        <span className='text-[9px] text-slate-400 font-semibold truncate'>
                          Đã xuống
                        </span>
                      </div>
                      <div className='flex flex-col gap-1 min-w-0'>
                        <span className='text-base font-extrabold text-red-500'>
                          {summary.absent + summary.noShow}
                        </span>
                        <span className='text-[9px] text-slate-400 font-semibold truncate'>
                          Vắng/không có mặt
                        </span>
                      </div>
                      <div className='flex flex-col gap-1 min-w-0'>
                        <span className='text-base font-extrabold text-slate-400'>
                          {summary.notServed}
                        </span>
                        <span className='text-[9px] text-slate-400 font-semibold truncate'>
                          Chưa phục vụ
                        </span>
                      </div>
                    </div>
                  </div>
                  <Button
                    size='sm'
                    className='w-full bg-[#C81E3A] hover:bg-[#B31B34] text-white font-bold rounded-xl h-8 text-xs border-0 shadow-none'
                    onClick={() => {
                      setSelectedStopId(null);
                      setIsAttendanceDrawerOpen(true);
                    }}
                  >
                    {access.isParentOnly
                      ? 'Xem trạng thái di chuyển'
                      : 'Mở bảng điểm danh học sinh'}
                  </Button>
                </div>
              )}
            </div>
          )}

          {/* Map and Timeline */}
          <div className='grid gap-5 xl:grid-cols-[1fr_390px] items-start'>
            {/* Map container */}
            <div className='relative h-[480px] overflow-hidden rounded-2xl border border-slate-200 shadow-xs bg-slate-50'>
              <TripMap
                stops={manifest?.stops || []}
                tripStatus={tripStatus || ''}
                isOutbound={isOutbound}
                routeGeometry={manifest?.routeGeometry}
                className='h-full w-full'
              />
            </div>

            {/* Timeline stop operations */}
            <div className='flex flex-col gap-4 bg-white border border-slate-200 rounded-2xl p-5 shadow-sm max-h-[480px] overflow-y-auto'>
              <div>
                <p className='text-[10px] font-extrabold uppercase tracking-wider text-slate-400'>
                  {access.isParentOnly
                    ? 'Dòng thời gian theo dõi chuyến'
                    : 'Dòng thời gian vận hành điểm dừng'}
                </p>
                <p className='text-[10px] text-slate-450 mt-1 font-semibold leading-relaxed'>
                  {access.isParentOnly
                    ? 'Theo dõi tiến độ xe và thời điểm đến từng điểm theo thứ tự.'
                    : 'Thực hiện đến điểm, đón/trả và rời điểm theo đúng thứ tự tuyến.'}
                </p>
              </div>

              {manifest?.stops && manifest.stops.length > 0 ? (
                <div className='relative pl-3 space-y-5 before:absolute before:left-[15px] before:top-2 before:bottom-2 before:w-[1.5px] before:bg-slate-100/70'>
                  {sortedStops.map((stop) => {
                    const isCurrent =
                      opSummary?.current &&
                      stop.routeStopId === opSummary.current.routeStopId;
                    const isNext =
                      opSummary?.next &&
                      stop.routeStopId === opSummary.next.routeStopId;

                    const isPending = stop.stopStatus === 'PENDING';
                    const isArrived = stop.stopStatus === 'ARRIVED';
                    const isBoarding = stop.stopStatus === 'BOARDING';
                    const isDeparted = stop.stopStatus === 'DEPARTED';
                    const isSkipped = stop.stopStatus === 'SKIPPED';
                    const isFinished = isDeparted || isSkipped;

                    const stopType = stop.locationType || '';
                    const StopIcon =
                      stopType === 'SCHOOL'
                        ? GraduationCap
                        : stopType === 'DEPOT'
                          ? Warehouse
                          : MapPin;

                    // Control eligibility (sequential check)
                    const isNextActionableStop =
                      firstUnfinishedStop &&
                      stop.routeStopId === firstUnfinishedStop.routeStopId;

                    const isStartTerminal =
                      stop.stopPurpose === 'START_TERMINAL';
                    const isEndTerminal = stop.stopPurpose === 'END_TERMINAL';
                    const isServiceStop = !isStartTerminal && !isEndTerminal;
                    const stopIsPickupAction =
                      stop.stopPurpose === 'PICKUP' ||
                      (stop.stopPurpose === 'START_TERMINAL' &&
                        stop.locationType === 'SCHOOL' &&
                        !isOutbound);
                    const stopIsDropoffAction =
                      stop.stopPurpose === 'DROPOFF' ||
                      (stop.stopPurpose === 'END_TERMINAL' &&
                        stop.locationType === 'SCHOOL' &&
                        isOutbound);

                    const boardingPending = Math.max(
                      (stop.plannedBoardingCount || 0) -
                        (stop.actualBoardedCount || 0),
                      0
                    );
                    const dropoffPending = Math.max(
                      (stop.plannedDropoffCount || 0) -
                        (stop.actualDroppedCount || 0),
                      0
                    );
                    const hasStudents = (stop.studentCount || 0) > 0;
                    const pendingCount = stopIsPickupAction
                      ? boardingPending
                      : stopIsDropoffAction
                        ? dropoffPending
                        : 0;
                    const attendanceResolved = pendingCount === 0;

                    const showArrive = isPending && !isStartTerminal;

                    // For service stops with students, boarding/drop-off is needed
                    const canBoard = isServiceStop && hasStudents;
                    const showStartBoarding = isArrived && canBoard;

                    // Depart conditions:
                    // - Start terminal: immediately from ARRIVED or BOARDING state
                    // - Service stop with students: only from BOARDING state
                    // - Service stop without students: from ARRIVED state
                    // - End terminal never allows Depart Stop (only Complete Trip).
                    const showDepart =
                      (isStartTerminal && (isArrived || isBoarding)) ||
                      (!isStartTerminal &&
                        !isEndTerminal &&
                        ((isArrived && !canBoard) ||
                          (isBoarding && attendanceResolved)));

                    const canSkip = isServiceStop;
                    const showSkip = (isPending || isArrived) && canSkip;

                    // End terminal allows Complete Trip when arrived or boarding (during drop-off)
                    const showCompleteTrip =
                      isEndTerminal &&
                      (isArrived || isBoarding) &&
                      tripStatus === 'IN_PROGRESS';

                    return (
                      <div
                        key={stop.routeStopId}
                        className={cn(
                          'relative flex gap-4 text-xs font-semibold group transition-all',
                          isCurrent && 'text-blue-700',
                          isDeparted && 'text-slate-400 opacity-70',
                          isSkipped && 'text-slate-350 opacity-60'
                        )}
                      >
                        {/* Timeline dot */}
                        <div
                          className={cn(
                            'relative z-10 flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-white border shadow-2xs transition-all',
                            isCurrent
                              ? 'border-blue-600 ring-4 ring-blue-500/10'
                              : isDeparted
                                ? 'border-emerald-300 bg-emerald-50'
                                : isSkipped
                                  ? 'border-slate-200 bg-slate-50'
                                  : 'border-slate-250'
                          )}
                        >
                          <span
                            className={cn(
                              'h-2 w-2 rounded-full',
                              isCurrent
                                ? 'bg-blue-600 animate-pulse'
                                : isDeparted
                                  ? 'bg-emerald-500'
                                  : isSkipped
                                    ? 'bg-slate-300'
                                    : 'bg-slate-350'
                            )}
                          />
                        </div>

                        {/* Stop Details */}
                        <div className='flex-1 min-w-0 space-y-2'>
                          <div className='flex items-start justify-between gap-1.5'>
                            <div className='min-w-0 flex-1'>
                              <div className='flex items-center gap-1.5 min-w-0'>
                                <div
                                  className={cn(
                                    'flex h-4.5 w-4.5 items-center justify-center rounded shrink-0 border border-slate-100',
                                    stopType === 'SCHOOL'
                                      ? 'bg-red-50 text-red-500'
                                      : stopType === 'DEPOT'
                                        ? 'bg-orange-50 text-orange-500'
                                        : 'bg-slate-50 text-slate-500'
                                  )}
                                >
                                  <StopIcon className='h-3 w-3' />
                                </div>
                                <p className='truncate text-[11px] font-bold text-slate-800'>
                                  {stop.stopOrder}.{' '}
                                  {stop.displayName ||
                                    `Điểm dừng #${stop.routeStopId}`}
                                </p>
                              </div>
                              <p className='text-[9px] text-slate-400 font-semibold mt-0.5 pl-6'>
                                {stopTypeLabel(stop)}
                              </p>
                            </div>
                            <div className='flex flex-col items-end gap-1 shrink-0'>
                              {renderFriendlyBadge(stop.stopStatus)}
                              {stop.studentCount !== undefined &&
                                stop.studentCount !== null &&
                                stop.studentCount > 0 && (
                                  <span className='text-[8px] font-extrabold uppercase bg-slate-100 border border-slate-200 text-slate-650 px-1 py-0.2 rounded'>
                                    Học sinh: {stop.studentCount}
                                  </span>
                                )}
                            </div>
                          </div>

                          {/* Detailed timing info */}
                          <div className='text-[10px] text-slate-500 pl-6 space-y-0.5 font-medium'>
                            <div className='flex items-center gap-1.5'>
                              <span className='text-slate-400'>Dự kiến:</span>
                              <span className='text-slate-700 font-semibold'>
                                {stop.plannedArrivalTime || '-'}
                                {stop.plannedDepartureTime
                                  ? ` - ${stop.plannedDepartureTime}`
                                  : ''}
                              </span>
                            </div>
                            {(stop.actualArrivalTime ||
                              stop.actualDepartureTime) && (
                              <div className='flex items-center gap-1.5 text-blue-600'>
                                <span>Thực tế:</span>
                                <span className='font-semibold'>
                                  {stop.actualArrivalTime
                                    ? stop.actualArrivalTime
                                        .split('T')[1]
                                        ?.substring(0, 5) ||
                                      stop.actualArrivalTime
                                    : '-'}
                                  {stop.actualDepartureTime
                                    ? ` - ${stop.actualDepartureTime.split('T')[1]?.substring(0, 5) || stop.actualDepartureTime}`
                                    : ''}
                                </span>
                              </div>
                            )}
                          </div>

                          {stopType !== 'DEPOT' && (
                            <div className='pl-6 pt-1'>
                              <Button
                                size='sm'
                                variant='outline'
                                className='h-7 rounded-lg text-[10px] font-bold border-slate-205 text-slate-650 hover:bg-slate-50 shadow-none'
                                onClick={() => {
                                  setSelectedStopId(stop.routeStopId);
                                  setIsAttendanceDrawerOpen(true);
                                }}
                              >
                                {isBoarding &&
                                tripIsActive &&
                                access.canOperateTrip
                                  ? !isOutbound && isServiceStop
                                    ? 'Ghi nhận xuống xe'
                                    : 'Điểm danh'
                                  : access.isParentOnly
                                    ? 'Xem trạng thái học sinh'
                                    : 'Xem điểm danh'}
                              </Button>
                            </div>
                          )}

                          {/* Actual visits info */}
                          {(stop.actualBoardedCount > 0 ||
                            stop.actualDroppedCount > 0) && (
                            <p className='text-[10px] text-slate-500 pl-6 font-medium'>
                              Đã lên xe:{' '}
                              <span className='font-bold text-slate-700'>
                                {stop.actualBoardedCount}
                              </span>{' '}
                              - Đã xuống xe:{' '}
                              <span className='font-bold text-slate-700'>
                                {stop.actualDroppedCount}
                              </span>
                            </p>
                          )}

                          {/* Action Buttons - only for users who can operate trips */}
                          {tripIsActive &&
                            !isFinished &&
                            access.canOperateTrip && (
                              <div
                                className='flex flex-wrap items-center gap-1.5 pl-6 pt-1'
                                title={
                                  !isNextActionableStop
                                    ? 'Cần xử lý các điểm trước trước.'
                                    : undefined
                                }
                              >
                                {showArrive && (
                                  <Button
                                    size='sm'
                                    className='h-7 bg-[#C81E3A] hover:bg-[#B31B34] text-white rounded-lg px-2.5 text-[10px] font-bold shadow-none disabled:opacity-50 disabled:cursor-not-allowed border-0'
                                    onClick={() =>
                                      handleArrive(stop.routeStopId)
                                    }
                                    disabled={isActing || !isNextActionableStop}
                                  >
                                    {stop.stopPurpose === 'END_TERMINAL'
                                      ? stop.locationType === 'SCHOOL'
                                        ? 'Đến trường'
                                        : stop.locationType === 'DEPOT'
                                          ? 'Đến bãi xe'
                                          : 'Đến điểm dừng'
                                      : 'Đến điểm dừng'}
                                  </Button>
                                )}
                                {showStartBoarding && (
                                  <Button
                                    size='sm'
                                    className='h-7 bg-[#C81E3A] hover:bg-[#B31B34] text-white rounded-lg px-2.5 text-[10px] font-bold shadow-none disabled:opacity-50 disabled:cursor-not-allowed border-0'
                                    onClick={() =>
                                      handleStartBoarding(stop.routeStopId)
                                    }
                                    disabled={isActing || !isNextActionableStop}
                                  >
                                    {stop.stopPurpose === 'DROPOFF'
                                      ? 'Bắt đầu trả học sinh'
                                      : 'Bắt đầu đón học sinh'}
                                  </Button>
                                )}
                                {showDepart && (
                                  <Button
                                    size='sm'
                                    variant={
                                      showStartBoarding ? 'outline' : 'default'
                                    }
                                    className={cn(
                                      'h-7 rounded-lg text-[10px] font-bold shadow-none disabled:opacity-50 disabled:cursor-not-allowed',
                                      showStartBoarding
                                        ? 'border-slate-200 text-slate-600 hover:bg-slate-50'
                                        : 'bg-[#C81E3A] hover:bg-[#B31B34] text-white border-0'
                                    )}
                                    onClick={() =>
                                      handleDepart(stop.routeStopId)
                                    }
                                    disabled={isActing || !isNextActionableStop}
                                  >
                                    {isStartTerminal
                                      ? stop.locationType === 'SCHOOL'
                                        ? 'Rời trường'
                                        : stop.locationType === 'DEPOT'
                                          ? 'Rời bãi xe'
                                          : 'Rời điểm dừng'
                                      : 'Rời điểm dừng'}
                                  </Button>
                                )}
                                {showCompleteTrip && (
                                  <div
                                    title={
                                      !canCompleteTrip
                                        ? 'Cần hoàn tất các điểm dừng và xử lý trạng thái học sinh trước khi hoàn thành chuyến.'
                                        : undefined
                                    }
                                  >
                                    <Button
                                      size='sm'
                                      className='h-7 bg-[#C81E3A] hover:bg-[#B31B34] text-white rounded-lg px-2.5 text-[10px] font-bold shadow-none disabled:opacity-50 disabled:cursor-not-allowed border-0'
                                      onClick={handleComplete}
                                      disabled={isActing || !canCompleteTrip}
                                    >
                                      Hoàn thành chuyến
                                    </Button>
                                  </div>
                                )}
                                {showSkip && (
                                  <>
                                    {showSkipForm &&
                                    selectedStopId === stop.routeStopId ? (
                                      <div className='flex items-center gap-1'>
                                        <Input
                                          value={skipReason}
                                          onChange={(e) =>
                                            setSkipReason(e.target.value)
                                          }
                                          placeholder='Lý do bỏ qua...'
                                          className='h-7 w-32 rounded-lg border-slate-200 bg-white px-2 text-[10px] font-normal text-slate-650 placeholder:text-slate-400 shadow-none focus-visible:ring-1 focus-visible:ring-[#C81E3A]/20'
                                        />
                                        <Button
                                          size='sm'
                                          className='h-7 rounded-lg bg-[#C81E3A] px-2.5 text-[10px] font-semibold text-white shadow-none hover:bg-[#B31B34] disabled:opacity-50 disabled:cursor-not-allowed border-0'
                                          onClick={() =>
                                            handleSkip(stop.routeStopId)
                                          }
                                          disabled={
                                            !skipReason.trim() || isActing
                                          }
                                        >
                                          Xác nhận
                                        </Button>
                                        <Button
                                          size='sm'
                                          variant='ghost'
                                          className='h-7 rounded-lg px-2 text-[10px] font-semibold text-slate-600 shadow-none hover:bg-slate-50 hover:text-slate-700'
                                          onClick={() => setShowSkipForm(false)}
                                        >
                                          Quay lại
                                        </Button>
                                      </div>
                                    ) : (
                                      <Button
                                        size='sm'
                                        variant='outline'
                                        className='h-7 rounded-lg border-slate-200 text-slate-500 hover:bg-slate-50 text-[10px] font-semibold px-2.5 disabled:opacity-50 disabled:cursor-not-allowed'
                                        onClick={() => {
                                          setSelectedStopId(stop.routeStopId);
                                          setShowSkipForm(true);
                                        }}
                                        disabled={
                                          isActing || !isNextActionableStop
                                        }
                                      >
                                        <SkipForward
                                          size={11}
                                          className='mr-1 shrink-0'
                                        />
                                        Bỏ qua điểm
                                      </Button>
                                    )}
                                  </>
                                )}
                              </div>
                            )}
                        </div>
                      </div>
                    );
                  })}
                </div>
              ) : (
                <SchoolBusEmptyState
                  title='Chưa có điểm dừng được liên kết'
                  description='Thiếu dữ liệu về trình tự các điểm dừng.'
                  icon={MapPin}
                />
              )}
            </div>
          </div>

          {/* Activity Log Feed */}
          <div className='bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-4'>
            <p className='text-[10px] font-extrabold text-slate-400 uppercase tracking-wider'>
              Nhật ký hoạt động
            </p>
            {sortedEvents.length === 0 ? (
              <SchoolBusEmptyState
                title='Chưa ghi nhận sự kiện'
                description='Nhật ký điểm danh và thao tác vận hành sẽ hiển thị tại đây sau khi được xử lý.'
                icon={Bell}
              />
            ) : (
              <div className='grid gap-3 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 max-h-[300px] overflow-y-auto pr-1'>
                {sortedEvents.map((item) => {
                  const isBoard = ['BOARDED', 'BOARD'].includes(
                    (item.eventType || item.attendanceType || '').toUpperCase()
                  );
                  const isDrop = ['DROPPED_OFF', 'DROPOFF'].includes(
                    (item.eventType || item.attendanceType || '').toUpperCase()
                  );
                  const isAbsent = ['ABSENT', 'NO_SHOW'].includes(
                    (item.eventType || item.attendanceType || '').toUpperCase()
                  );

                  const cardBorder = isBoard
                    ? 'border-blue-100 bg-blue-50/10'
                    : isDrop
                      ? 'border-emerald-100 bg-emerald-50/10'
                      : isAbsent
                        ? 'border-red-100 bg-red-50/10'
                        : 'border-slate-150 bg-white';

                  return (
                    <div
                      key={item.id}
                      className={cn(
                        'rounded-xl border p-3.5 shadow-2xs space-y-2.5 transition-all hover:shadow-xs',
                        cardBorder
                      )}
                    >
                      <div className='flex items-start justify-between gap-2.5 min-w-0'>
                        <div className='min-w-0 flex-1'>
                          <p className='truncate text-xs font-bold text-slate-900'>
                            {item.studentName}
                          </p>
                          <p className='text-[10px] text-slate-450 mt-1 font-semibold flex items-center gap-1'>
                            <Clock className='h-3 w-3 text-slate-350' />
                            {formatDateTime(item.recordedAt)}
                          </p>
                        </div>
                        {renderFriendlyBadge(
                          item.eventType || item.attendanceType
                        )}
                      </div>
                      {item.notes && (
                        <p
                          className='text-[10px] text-slate-500 bg-slate-50 border border-slate-100 rounded px-1.5 py-0.5 mt-1 truncate'
                          title={item.notes}
                        >
                          Ghi chú: {item.notes}
                        </p>
                      )}
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>

        <Sheet
          open={isAttendanceDrawerOpen}
          onOpenChange={setIsAttendanceDrawerOpen}
        >
          <SheetContent
            side='right'
            className='school-bus-shell flex h-full w-[100vw] flex-col gap-6 overflow-y-auto bg-background p-6 text-foreground sm:max-w-[550px]'
          >
            <SheetHeader className='border-b border-slate-100 pb-4'>
              <div className='flex items-center justify-between'>
                <SheetTitle className='text-base font-extrabold text-slate-800'>
                  {access.isParentOnly
                    ? selectedStop
                      ? `Trạng thái qua điểm: ${selectedStop.displayName}`
                      : 'Danh sách trạng thái học sinh'
                    : selectedStop
                      ? `Điểm danh tại điểm: ${selectedStop.displayName}`
                      : 'Danh sách điểm danh chuyến'}
                </SheetTitle>
              </div>
              <SheetDescription className='text-xs text-slate-400 mt-1 font-semibold'>
                {selectedStop
                  ? `${stopTypeLabel(selectedStop)} - Trạng thái: ${tripStopStatusLabel[selectedStop.stopStatus] || selectedStop.stopStatus}`
                  : access.isParentOnly
                    ? `Tuyến: ${routeCode} - Chi tiết học sinh`
                    : `Tuyến: ${routeCode} - Chiều: ${getFriendlyDirection(manifest?.routeDirection)}`}
              </SheetDescription>
            </SheetHeader>

            {/* Stop Context Banner if stop selected */}
            {selectedStop && !access.isParentOnly && (
              <div className='space-y-3 shrink-0'>
                {tripStatus !== 'IN_PROGRESS' &&
                tripStatus !== 'COMPLETED' &&
                tripStatus !== 'CANCELLED' ? (
                  <div className='bg-amber-50 border border-amber-200 text-amber-850 px-4 py-3 rounded-xl text-[11px] font-semibold'>
                    Hãy bắt đầu chuyến trước khi ghi nhận điểm danh.
                  </div>
                ) : tripStatus === 'COMPLETED' || tripStatus === 'CANCELLED' ? (
                  <div className='bg-slate-50 border border-slate-200 text-slate-600 px-4 py-3 rounded-xl text-[11px] font-semibold'>
                    Chuyến đã hoàn thành hoặc đã hủy. Bản ghi điểm danh đã bị khóa.
                  </div>
                ) : selectedStop.stopStatus === 'PENDING' ? (
                  <div className='bg-amber-50 border border-amber-200 text-amber-850 px-4 py-3 rounded-xl text-[11px] font-semibold'>
                    Hãy ghi nhận xe đến điểm dừng trước khi điểm danh.
                  </div>
                ) : selectedStop.stopStatus === 'ARRIVED' ? (
                  <div className='bg-amber-50 border border-amber-200 text-amber-850 px-4 py-3 rounded-xl text-[11px] font-semibold'>
                    Hãy bắt đầu đón/trả tại điểm dừng này trước khi điểm danh.
                  </div>
                ) : selectedStop.stopStatus === 'DEPARTED' ||
                  selectedStop.stopStatus === 'SKIPPED' ? (
                  <div className='bg-slate-50 border border-slate-200 text-slate-600 px-4 py-3 rounded-xl text-[11px] font-semibold'>
                    Xe đã rời hoặc bỏ qua điểm dừng này. Bản ghi điểm danh đã bị khóa.
                  </div>
                ) : isStopActionable && isPickupActionStop ? (
                  <div className='bg-emerald-50 border border-emerald-250 text-emerald-800 px-4 py-3 rounded-xl text-[11px] font-semibold'>
                    Điểm dừng đang ở chế độ đón học sinh. Ghi nhận học sinh đã lên xe,
                    vắng mặt hoặc không có mặt tại điểm đón.
                  </div>
                ) : isStopActionable && isDropoffActionStop ? (
                  <div className='bg-emerald-50 border border-emerald-250 text-emerald-800 px-4 py-3 rounded-xl text-[11px] font-semibold'>
                    Điểm dừng đang ở chế độ trả học sinh. Ghi nhận học sinh đã xuống xe
                    hoặc chưa phục vụ.
                  </div>
                ) : null}
              </div>
            )}

            {/* Search Box */}
            <div className='relative shrink-0'>
              <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400' />
              <Input
                type='text'
                placeholder='Tìm học sinh theo tên hoặc mã...'
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className='h-9 pl-9 text-xs rounded-xl border-slate-200 focus:border-slate-350 focus:ring-1 focus:ring-slate-200/50'
              />
            </div>

            {/* Batch Action Bar */}
            {selectedStop &&
              tripIsActive &&
              isStopActionable &&
              access.canMarkAttendance &&
              !access.isParentOnly &&
              (isPickupActionStop || isDropoffActionStop) && (
              <div className='space-y-2 shrink-0'>
                <div className='flex items-center gap-2'>
                  <button
                    type='button'
                    onClick={toggleSelectAll}
                    className='flex items-center gap-1.5 text-[10px] font-bold text-slate-500 hover:text-slate-700 transition-colors'
                    disabled={plannedStudentsAtStop.length === 0 || isActing}
                  >
                    <CheckSquare className='h-3.5 w-3.5' />
                    {plannedStudentsAtStop.length > 0 &&
                    plannedStudentsAtStop.every((s) => selectedStudentIds.has(s.studentId))
                      ? 'Bỏ chọn tất cả'
                      : 'Chọn tất cả'}
                  </button>
                  {selectedStudentIds.size > 0 && (
                    <span className='text-[10px] text-slate-400 font-semibold'>
                      Đã chọn {selectedStudentIds.size}
                    </span>
                  )}
                </div>
                {selectedStudentIds.size > 0 && (
                  <div className='flex flex-wrap items-center gap-1.5'>
                    {isPickupActionStop && (
                      <>
                        <Button
                          size='sm'
                          className='h-7 bg-[#C81E3A] hover:bg-[#B31B34] text-white rounded-full px-3 text-[10px] font-bold shadow-none border-0'
                          onClick={() => handleBatchAction('MARK_BOARDED')}
                          disabled={isActing}
                        >
                          Lên xe ({selectedStudentIds.size})
                        </Button>
                        <Button
                          size='sm'
                          variant='outline'
                          className='h-7 rounded-full border-amber-250 px-3 text-[10px] text-amber-700 hover:bg-amber-50 font-bold shadow-none'
                          onClick={() => handleBatchAction('MARK_ABSENT')}
                          disabled={isActing}
                        >
                          Vắng mặt ({selectedStudentIds.size})
                        </Button>
                        <Button
                          size='sm'
                          variant='outline'
                          className='h-7 rounded-full border-red-250 px-3 text-[10px] text-red-650 hover:bg-red-50 font-bold shadow-none'
                          onClick={() => handleBatchAction('MARK_NO_SHOW')}
                          disabled={isActing}
                        >
                          Không có mặt ({selectedStudentIds.size})
                        </Button>
                      </>
                    )}
                  </div>
                )}
              </div>
            )}

            {/* Main Content Area */}
            <div className='flex-1 overflow-y-auto min-h-0 pr-1'>
              {loadingDrawerStudents ? (
                <div className='py-12 text-center text-slate-400 text-xs font-semibold'>
                  Đang tải danh sách học sinh...
                </div>
              ) : selectedStop ? (
                /* Stop Specific Student List */
                studentsAtStop.length === 0 ? (
                  <div className='py-12 text-center text-slate-400 text-xs font-semibold'>
                    Không có học sinh phù hợp với chiều điểm dừng và bộ lọc
                    search.
                  </div>
                ) : (
                  <div className='space-y-3'>
                    {studentsAtStop.map((student) => {
                      const stStatus = student.status || 'PLANNED';
                      const stNormalized = stStatus.toUpperCase();

                      const isPlanned = stNormalized === 'PLANNED';
                      const isBoarded = stNormalized === 'BOARDED';

                      const canBoard = isPickupActionStop && isPlanned;
                      const canDrop = isDropoffActionStop && isBoarded;
                      const canAbsent = isPickupActionStop && isPlanned;
                      const canNotServed = isDropoffActionStop && isBoarded;

                      return (
                        <div
                          key={student.tripStudentId}
                          className='bg-white border border-slate-150 rounded-xl p-4 shadow-2xs hover:shadow-xs transition-all flex flex-col sm:flex-row sm:items-center justify-between gap-4'
                        >
                          <div className='flex items-start gap-3 min-w-0 flex-1'>
                            {/* Batch selection checkbox */}
                            {tripIsActive && isStopActionable && access.canMarkAttendance && isPlanned && (isPickupActionStop || isDropoffActionStop) && (
                              <input
                                type='checkbox'
                                checked={selectedStudentIds.has(student.studentId)}
                                onChange={() => toggleStudentSelection(student.studentId)}
                                disabled={isActing}
                                className='mt-0.5 h-4 w-4 rounded border-slate-300 text-[#C81E3A] focus:ring-[#C81E3A]/30 cursor-pointer shrink-0 accent-[#C81E3A]'
                              />
                            )}
                          <div className='space-y-1 min-w-0'>
                            <div className='flex items-center gap-2 flex-wrap'>
                              <p className='font-bold text-slate-800 text-xs truncate'>
                                {student.studentName}
                              </p>
                              {renderFriendlyBadge(stStatus)}
                            </div>
                            <div className='flex flex-wrap items-center gap-x-2 gap-y-0.5 text-[10px] text-slate-400 font-semibold'>
                              <span>Mã: {student.studentCode || 'Chưa có'}</span>
                            </div>
                            {student.note && (
                              <p className='text-[10px] text-red-500 bg-red-50/50 border border-red-100/50 rounded px-2 py-0.5 mt-1 w-fit font-medium'>
                                Ghi chú: {student.note}
                              </p>
                            )}
                          </div>
                          </div>

                          {/* Attendance Actions */}
                          {tripIsActive &&
                            isStopActionable &&
                            access.canMarkAttendance && (
                              <div className='flex flex-wrap items-center gap-1.5 shrink-0 self-end sm:self-center'>
                                {canBoard && (
                                  <Button
                                    size='sm'
                                    className='h-7.5 bg-[#C81E3A] hover:bg-[#B31B34] text-white rounded-full px-3 text-[10px] font-bold shadow-none border-0'
                                    onClick={() => handleBoard(student)}
                                    disabled={isActing}
                                  >
                                    Lên xe
                                  </Button>
                                )}
                                {canDrop && (
                                  <Button
                                    size='sm'
                                    className={cn(
                                      schoolBusUi.primaryButton,
                                      'h-7.5 rounded-full px-3 text-[10px] font-bold shadow-none border-0'
                                    )}
                                    onClick={() => handleDropoff(student)}
                                    disabled={isActing}
                                  >
                                    Xuống xe
                                  </Button>
                                )}
                                {canAbsent && (
                                  <Button
                                    size='sm'
                                    variant='outline'
                                    className='h-7.5 rounded-full border-amber-250 px-3 text-[10px] text-amber-700 hover:bg-amber-50 font-bold shadow-none'
                                    onClick={() => handleAbsent(student)}
                                    disabled={isActing}
                                  >
                                    Vắng mặt
                                  </Button>
                                )}
                                {canBoard && (
                                  <Button
                                    size='sm'
                                    variant='outline'
                                    className='h-7.5 rounded-full border-red-250 px-3 text-[10px] text-red-650 hover:bg-red-50 font-bold shadow-none'
                                    onClick={() => handleNoShow(student)}
                                    disabled={isActing}
                                  >
                                    Không có mặt
                                  </Button>
                                )}
                                {canNotServed && (
                                  <Button
                                    size='sm'
                                    variant='outline'
                                    className='h-7.5 rounded-full border-slate-300 px-3 text-[10px] text-slate-650 hover:bg-slate-50 font-bold shadow-none'
                                    onClick={() => handleNotServed(student)}
                                    disabled={isActing}
                                  >
                                    Chưa phục vụ
                                  </Button>
                                )}
                              </div>
                            )}
                        </div>
                      );
                    })}
                  </div>
                )
              ) : (
                /* Grouped All Students View */
                (() => {
                  const students = drawerStudents;
                  const stops = manifest?.stops || [];

                  // Group students by stop (either pickup or dropoff depending on the stop type)
                  const filteredStudents = searchQuery.trim()
                    ? students.filter(
                        (s) =>
                          (s.studentName || '')
                            .toLowerCase()
                            .includes(searchQuery.toLowerCase()) ||
                          (s.studentCode || '')
                            .toLowerCase()
                            .includes(searchQuery.toLowerCase())
                      )
                    : students;

                  if (filteredStudents.length === 0) {
                    return (
                      <div className='py-12 text-center text-slate-400 text-xs font-semibold'>
                        Không tìm thấy bản ghi học sinh.
                      </div>
                    );
                  }

                  // Group by stopId
                  return (
                    <div className='space-y-6'>
                      {stops
                        .filter((stop) => stop.locationType !== 'DEPOT')
                        .map((stop) => {
                          const stopSts = filteredStudents.filter((s) =>
                            isOutbound
                              ? s.pickupStopId === stop.routeStopId
                              : s.dropoffStopId === stop.routeStopId
                          );

                          if (stopSts.length === 0) return null;

                          return (
                            <div key={stop.routeStopId} className='space-y-2.5'>
                              <div className='flex items-center justify-between border-b border-slate-100 pb-1.5'>
                                <h4 className='font-bold text-slate-800 text-xs'>
                                  {stop.stopOrder}. {stop.displayName}
                                </h4>
                                <span className='text-[9px] font-extrabold uppercase bg-slate-100 text-slate-500 px-2 py-0.5 rounded'>
                                  {stopTypeLabel(stop)}
                                </span>
                              </div>
                              <div className='grid gap-2'>
                                {stopSts.map((student) => (
                                  <div
                                    key={student.tripStudentId}
                                    className='bg-slate-50/50 border border-slate-100 rounded-xl p-3 flex items-center justify-between gap-3'
                                  >
                                    <div className='min-w-0'>
                                      <p className='font-bold text-slate-800 text-xs truncate'>
                                        {student.studentName}
                                      </p>
                                      <p className='text-[10px] text-slate-400 font-semibold mt-0.5'>
                                        Mã: {student.studentCode || 'Chưa có'}
                                      </p>
                                    </div>
                                    <div className='flex items-center gap-2 shrink-0'>
                                      {renderFriendlyBadge(student.status)}
                                      {/* Quick Mark Attendance link if stop is boarding and active */}
                                      {tripIsActive &&
                                        stop.stopStatus === 'BOARDING' &&
                                        access.canMarkAttendance && (
                                          <Button
                                            size='sm'
                                            variant='ghost'
                                            className='h-6 text-[10px] text-blue-650 hover:text-blue-700 font-extrabold px-1 rounded-md'
                                            onClick={() => {
                                              setSelectedStopId(
                                                stop.routeStopId
                                              );
                                            }}
                                          >
                                            Điểm danh
                                          </Button>
                                        )}
                                    </div>
                                  </div>
                                ))}
                              </div>
                            </div>
                          );
                        })}
                    </div>
                  );
                })()
              )}
            </div>

            {/* Sticky bottom panel */}
            {selectedStop && (
              <div className='border-t border-slate-100 pt-4 flex justify-between shrink-0'>
                <Button
                  size='sm'
                  variant='outline'
                  className='h-8.5 rounded-xl text-xs font-bold border-slate-200 text-slate-650 hover:bg-slate-50'
                  onClick={() => setSelectedStopId(null)}
                >
                  Hiển thị tất cả học sinh
                </Button>
                <Button
                  size='sm'
                  className='h-8.5 rounded-xl text-xs font-bold bg-slate-100 text-slate-650 border border-slate-200 hover:bg-slate-200'
                  onClick={() => setIsAttendanceDrawerOpen(false)}
                >
                  Đóng bảng
                </Button>
              </div>
            )}
          </SheetContent>
        </Sheet>
      </SchoolBusPageShell>
    </MapMarkerVisibilityProvider>
  );
}
