'use client';

import * as React from 'react';
import {
  AlertTriangle,
  ArrowLeft,
  Calendar,
  CalendarClock,
  Clock,
  Edit2,
  GraduationCap,
  Link2,
  MapPin,
  MapPinCheck,
  Plus,
  Search,
  Table2,
  Trash2,
  Unlink,
} from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useGetSchoolByIdQuery,
  useUpdateSchoolMutation,
  useCreateSchoolScheduleMutation,
  useUpdateSchoolScheduleMutation,
  useDeleteSchoolScheduleMutation,
  useLinkSchoolPickupPointMutation,
  useUnlinkSchoolPickupPointMutation,
  useCreateSchoolPickupPointWindowMutation,
  useUpdateSchoolPickupPointWindowMutation,
  useDeleteSchoolPickupPointWindowMutation,
  useGetSchoolSchedulesQuery,
  useGetSchoolPickupPointsQuery,
  useGetSchoolPickupPointWindowsQuery,
  useGetPickupPointsQuery,
} from '../api/schoolBusApi';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { SchoolBusDeleteDialog } from '../components/SchoolBusDeleteDialog';
import { SchoolFormDialog } from '../components/SchoolBusMasterDataForms';
import { ScheduleFormDialog } from '../components/forms/ScheduleFormDialog';
import { LinkPickupPointDialog } from '../components/forms/LinkPickupPointDialog';
import { WindowFormDialog } from '../components/forms/WindowFormDialog';
import { OperationsMap } from '../components/map/OperationsMap';
import { SchoolBusMapLegend } from '../components/map/SchoolBusMapLegend';
import { SchoolBusMapWorkspace } from '../components/map/SchoolBusMapWorkspace';
import { MapMarkerVisibilityProvider } from '../components/map/MapMarkerVisibilityContext';
import { formatDate, getPageItems, SCHOOL_BUS_OPTION_QUERY } from '../utils';
import type {
  SchoolBusSchool,
  SchoolBusSchedule,
  SchoolBusSchoolPickupPoint,
  SchoolBusSchoolPickupPointWindow,
  SchoolBusPickupPoint,
  SchoolScheduleSummary,
} from '../types';

interface SchoolBusSchoolDetailPageProps {
  schoolId: number;
  onClose: () => void;
}

const formatDaysCompact = (days: string[] | undefined | null) => {
  if (!days || days.length === 0) return 'Mon–Fri';
  const dayMap: Record<string, string> = {
    MONDAY: 'Mon',
    TUESDAY: 'Tue',
    WEDNESDAY: 'Wed',
    THURSDAY: 'Thu',
    FRIDAY: 'Fri',
    SATURDAY: 'Sat',
    SUNDAY: 'Sun',
  };
  return days.map((d) => dayMap[d.toUpperCase()] || d).join(', ');
};

const formatUsageType = (type: string | undefined | null) => {
  if (type === 'PICKUP' || type === 'PICKUP_ONLY') return 'Pickup only';
  if (type === 'DROPOFF' || type === 'DROPOFF_ONLY') return 'Drop-off only';
  if (type === 'PICKUP_DROPOFF') return 'Pickup & drop-off';
  return type || 'N/A';
};

const getUsageTypeBadgeClasses = (type: string | undefined | null) => {
  if (type === 'PICKUP' || type === 'PICKUP_ONLY')
    return 'bg-blue-50 text-blue-700 border border-blue-200';
  if (type === 'DROPOFF' || type === 'DROPOFF_ONLY')
    return 'bg-emerald-50 text-emerald-700 border border-emerald-200';
  return 'bg-indigo-50 text-indigo-700 border border-indigo-200';
};

export function SchoolBusSchoolDetailPage({
  schoolId,
  onClose,
}: SchoolBusSchoolDetailPageProps) {
  const [activeTab, setActiveTab] = React.useState<'general' | 'schedules' | 'pickups'>('general');

  // --- RTK queries & mutations ---
  const { data: schoolDetailData, isLoading: loadingSchool, refetch: refetchSchool } =
    useGetSchoolByIdQuery(schoolId);
  const school = schoolDetailData?.data;

  const { data: schedulesData, refetch: refetchSchedules } = useGetSchoolSchedulesQuery({
    schoolId,
    page: 0,
    size: 100,
  });
  const schedules = getPageItems(schedulesData?.data);

  const { data: linkedPickupData, refetch: refetchLinkedPickups } = useGetSchoolPickupPointsQuery({
    schoolId,
    page: 0,
    size: 100,
  });
  const linkedPickupPoints = getPageItems(linkedPickupData?.data);

  const { data: allPickupPointsData } = useGetPickupPointsQuery({
    page: 0,
    size: 1000,
    sortBy: 'name',
    sortDirection: 'ASC',
  });
  const allPickupPoints = getPageItems(allPickupPointsData?.data);

  const [updateSchool, { isLoading: updatingSchool }] = useUpdateSchoolMutation();
  const [createSchedule, { isLoading: creatingSchedule }] = useCreateSchoolScheduleMutation();
  const [updateSchedule, { isLoading: updatingSchedule }] = useUpdateSchoolScheduleMutation();
  const [deleteSchedule, { isLoading: deletingSchedule }] = useDeleteSchoolScheduleMutation();
  const [linkPickupPoint, { isLoading: linkingPickup }] = useLinkSchoolPickupPointMutation();
  const [unlinkPickupPoint, { isLoading: unlinkingPickup }] = useUnlinkSchoolPickupPointMutation();
  const [createWindow, { isLoading: creatingWindow }] = useCreateSchoolPickupPointWindowMutation();
  const [updateWindow, { isLoading: updatingWindow }] = useUpdateSchoolPickupPointWindowMutation();
  const [deleteWindow, { isLoading: deletingWindow }] = useDeleteSchoolPickupPointWindowMutation();

  // --- Dialog states ---
  const [schoolDialogOpen, setSchoolDialogOpen] = React.useState(false);
  const [scheduleDialogOpen, setScheduleDialogOpen] = React.useState(false);
  const [linkDialogOpen, setLinkDialogOpen] = React.useState(false);
  const [windowDialogOpen, setWindowDialogOpen] = React.useState(false);
  const [editingSchedule, setEditingSchedule] = React.useState<SchoolBusSchedule | null>(null);
  const [editingWindow, setEditingWindow] = React.useState<SchoolBusSchoolPickupPointWindow | null>(null);
  const [expandedSppId, setExpandedSppId] = React.useState<number | null>(null);
  const [deleteTarget, setDeleteTarget] = React.useState<{
    type: 'schedule' | 'link' | 'window';
    id: number;
    title: string;
    description: string;
  } | null>(null);

  // --- Filters ---
  const [scheduleShiftFilter, setScheduleShiftFilter] = React.useState<'ALL' | 'MORNING' | 'AFTERNOON'>('ALL');
  const [scheduleDayFilter, setScheduleDayFilter] = React.useState<string>('ALL');
  const [scheduleViewMode, setScheduleViewMode] = React.useState<'table' | 'calendar'>('table');

  const [pickupSearch, setPickupSearch] = React.useState('');
  const [pickupUsageFilter, setPickupUsageFilter] = React.useState<'ALL' | 'PICKUP' | 'DROPOFF' | 'PICKUP_DROPOFF'>('ALL');
  const [pickupCoordsFilter, setPickupCoordsFilter] = React.useState<'ALL' | 'CONFIGURED' | 'MISSING'>('ALL');

  // --- Map fit control key ---
  const [fitSelectedKey, setFitSelectedKey] = React.useState(0);
  const [selectedPickupPointId, setSelectedPickupPointId] = React.useState<number | null>(null);

  // --- Handlers ---
  const refetchAll = React.useCallback(async () => {
    await Promise.all([refetchSchool(), refetchSchedules(), refetchLinkedPickups()]);
  }, [refetchSchool, refetchSchedules, refetchLinkedPickups]);

  const handleEditSchool = async (values: any) => {
    try {
      const response = await updateSchool({ id: schoolId, body: values }).unwrap();
      toast.success(response.message || 'School details updated');
      setSchoolDialogOpen(false);
      refetchSchool();
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to update school');
    }
  };

  const handleSaveSchedule = async (values: any) => {
    try {
      const response = editingSchedule
        ? await updateSchedule({ id: editingSchedule.id, body: values }).unwrap()
        : await createSchedule({ schoolId, body: values }).unwrap();
      toast.success(response.message || 'Schedule saved successfully');
      setScheduleDialogOpen(false);
      setEditingSchedule(null);
      refetchAll();
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to save schedule');
    }
  };

  const handleLinkPickupPoint = async (values: any) => {
    try {
      const response = await linkPickupPoint({ schoolId, body: values }).unwrap();
      toast.success(response.message || 'Pickup point linked successfully');
      setLinkDialogOpen(false);
      refetchAll();
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to link pickup point');
    }
  };

  const handleSaveWindow = async (values: any) => {
    if (!expandedSppId) return;
    const body = { ...values, schoolPickupPointId: expandedSppId };
    try {
      const response = editingWindow
        ? await updateWindow({ windowId: editingWindow.id, body }).unwrap()
        : await createWindow(body).unwrap();
      toast.success(response.message || 'Window saved successfully');
      setWindowDialogOpen(false);
      setEditingWindow(null);
      refetchAll();
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to save window');
    }
  };

  const handleConfirmDelete = async () => {
    if (!deleteTarget) return;
    try {
      if (deleteTarget.type === 'schedule') {
        const response = await deleteSchedule(deleteTarget.id).unwrap();
        toast.success(response.message || 'Schedule deleted');
      } else if (deleteTarget.type === 'link') {
        const response = await unlinkPickupPoint({ schoolId, pickupPointId: deleteTarget.id }).unwrap();
        toast.success(response.message || 'Pickup point unlinked');
      } else if (deleteTarget.type === 'window') {
        const response = await deleteWindow(deleteTarget.id).unwrap();
        toast.success(response.message || 'Window deleted');
      }
      setDeleteTarget(null);
      refetchAll();
    } catch (error: any) {
      toast.error(error?.data?.message || 'Delete operation failed');
    }
  };

  // --- Derived maps data ---
  const mapSchools = React.useMemo(() => {
    if (!school || typeof school.latitude !== 'number' || typeof school.longitude !== 'number') return [];
    return [school];
  }, [school]);

  const mapPickupPoints = React.useMemo(() => {
    return linkedPickupPoints
      .filter((lp) => typeof lp.pickupPointLatitude === 'number' && typeof lp.pickupPointLongitude === 'number')
      .map((lp) => ({
        id: lp.pickupPointId,
        name: lp.pickupPointName,
        code: lp.pickupPointName, // Fallback for code if needed
        address: lp.pickupPointAddress || '',
        latitude: lp.pickupPointLatitude,
        longitude: lp.pickupPointLongitude,
        usageType: lp.pickupPointUsageType || 'PICKUP_DROPOFF',
        isActive: true,
      })) as SchoolBusPickupPoint[];
  }, [linkedPickupPoints]);

  const hasMapData = mapSchools.length > 0 || mapPickupPoints.length > 0;

  // --- Schedules filter & calendars ---
  const filteredSchedules = React.useMemo(() => {
    return schedules.filter((s) => {
      const matchShift =
        scheduleShiftFilter === 'ALL' ||
        (s.shiftType === 'MORNING' && scheduleShiftFilter === 'MORNING') ||
        (s.shiftType === 'AFTERNOON' && scheduleShiftFilter === 'AFTERNOON');
      const matchDay =
        scheduleDayFilter === 'ALL' ||
        s.daysOfWeek?.map((d) => d.toUpperCase()).includes(scheduleDayFilter.toUpperCase());
      return matchShift && matchDay;
    });
  }, [schedules, scheduleShiftFilter, scheduleDayFilter]);

  const daysOfWeekList = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];

  // --- Pickups filter ---
  const filteredPickups = React.useMemo(() => {
    return linkedPickupPoints.filter((lp) => {
      const matchSearch =
        !pickupSearch ||
        lp.pickupPointName.toLowerCase().includes(pickupSearch.toLowerCase()) ||
        (lp.pickupPointAddress ?? '').toLowerCase().includes(pickupSearch.toLowerCase());
      const matchUsage =
        pickupUsageFilter === 'ALL' ||
        (pickupUsageFilter === 'PICKUP' && (lp.pickupPointUsageType === 'PICKUP' || lp.pickupPointUsageType === 'PICKUP_ONLY')) ||
        (pickupUsageFilter === 'DROPOFF' && (lp.pickupPointUsageType === 'DROPOFF' || lp.pickupPointUsageType === 'DROPOFF_ONLY')) ||
        (pickupUsageFilter === 'PICKUP_DROPOFF' && lp.pickupPointUsageType === 'PICKUP_DROPOFF');
      const hasCoords = typeof lp.pickupPointLatitude === 'number' && typeof lp.pickupPointLongitude === 'number';
      const matchCoords =
        pickupCoordsFilter === 'ALL' ||
        (pickupCoordsFilter === 'CONFIGURED' && hasCoords) ||
        (pickupCoordsFilter === 'MISSING' && !hasCoords);
      return matchSearch && matchUsage && matchCoords;
    });
  }, [linkedPickupPoints, pickupSearch, pickupUsageFilter, pickupCoordsFilter]);

  // Window details state helper
  const { data: windowsData } = useGetSchoolPickupPointWindowsQuery(expandedSppId!, {
    skip: !expandedSppId,
  });
  const expandedWindows = windowsData?.data ?? [];

  if (loadingSchool) {
    return (
      <div className="flex h-screen items-center justify-center bg-slate-50 text-slate-500">
        <div className="flex flex-col items-center gap-3">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-slate-300 border-t-[#C81E3A]" />
          <p className="text-sm font-semibold">Loading school workspace...</p>
        </div>
      </div>
    );
  }

  if (!school) {
    return (
      <div className="flex h-screen flex-col items-center justify-center gap-4 bg-slate-50 text-center">
        <GraduationCap className="h-12 w-12 text-slate-300 animate-bounce" />
        <div>
          <h3 className="text-lg font-bold text-slate-800">School Not Found</h3>
          <p className="text-sm text-slate-400">The requested school details could not be loaded.</p>
        </div>
        <Button onClick={onClose} variant="outline" className="rounded-full">
          <ArrowLeft className="mr-2 h-4 w-4" /> Go back
        </Button>
      </div>
    );
  }

  return (
    <>
      <SchoolBusPageShell
        title={school.name}
        description={school.address || 'Manage school profiles, associated schedules, and pickup points.'}
        breadcrumb={
          <SchoolBusBreadcrumb
            items={[
              { label: 'School Bus Ops', href: '/school-bus/dispatch' },
              { label: 'Schools', href: '/school-bus/schools' },
              { label: school.name, current: true },
            ]}
          />
        }
        actions={
          <div className="flex items-center gap-2">
            <Button onClick={onClose} variant="outline" className="rounded-full bg-white text-slate-600 hover:bg-slate-50 border-slate-200">
              <ArrowLeft className="mr-2 h-4 w-4" /> Back to directory
            </Button>
            <Button onClick={() => setSchoolDialogOpen(true)} variant="outline" className="rounded-full bg-white text-slate-600 hover:bg-slate-50 border-slate-200">
              <Edit2 className="mr-2 h-3.5 w-3.5" /> Edit Profile
            </Button>
            <Button
              className="rounded-full bg-[#C81E3A] hover:bg-[#A6172D] text-white"
              onClick={() => {
                if (activeTab === 'schedules') {
                  setEditingSchedule(null);
                  setScheduleDialogOpen(true);
                } else {
                  setLinkDialogOpen(true);
                }
              }}
            >
              <Plus className="mr-1.5 h-4 w-4" />
              {activeTab === 'schedules' ? 'Add Schedule' : 'Link Pickup'}
            </Button>
          </div>
        }
      >
        <div className="flex flex-col gap-6">
          {/* Summary metrics bar */}
          <div className="grid gap-3 grid-cols-2 lg:grid-cols-4">
            <SchoolBusMetricCard
              label="Schedules"
              value={school.scheduleCount ?? 0}
              icon={CalendarClock}
              tone="school"
              variant="compact"
            />
            <SchoolBusMetricCard
              label="Linked Pickups"
              value={school.pickupPointCount ?? 0}
              icon={MapPin}
              tone="pickup"
              variant="compact"
            />
            <SchoolBusMetricCard
              label="Active schedules"
              value={school.activeScheduleCount ?? 0}
              icon={Clock}
              tone="linked"
              variant="compact"
            />
            <SchoolBusMetricCard
              label="Coordinated points"
              value={school.pickupPointCount && school.anyLinkedPointMissingCoordinates ? 'Warning' : 'Configured'}
              icon={AlertTriangle}
              tone={school.anyLinkedPointMissingCoordinates ? 'warning' : 'success'}
              variant="compact"
            />
          </div>

          {/* Navigation Tabs */}
          <div className="flex border-b border-slate-200">
            <button
              onClick={() => setActiveTab('general')}
              className={cn(
                'px-5 py-3 text-sm font-semibold border-b-2 transition-colors',
                activeTab === 'general'
                  ? 'border-[#C81E3A] text-[#C81E3A]'
                  : 'border-transparent text-slate-500 hover:text-slate-700'
              )}
            >
              General Info
            </button>
            <button
              onClick={() => setActiveTab('schedules')}
              className={cn(
                'px-5 py-3 text-sm font-semibold border-b-2 transition-colors',
                activeTab === 'schedules'
                  ? 'border-[#C81E3A] text-[#C81E3A]'
                  : 'border-transparent text-slate-500 hover:text-slate-700'
              )}
            >
              Schedules ({school.scheduleCount ?? 0})
            </button>
            <button
              onClick={() => setActiveTab('pickups')}
              className={cn(
                'px-5 py-3 text-sm font-semibold border-b-2 transition-colors',
                activeTab === 'pickups'
                  ? 'border-[#C81E3A] text-[#C81E3A]'
                  : 'border-transparent text-slate-500 hover:text-slate-700'
              )}
            >
              Linked Pickups ({school.pickupPointCount ?? 0})
            </button>
          </div>

          {/* Tab contents */}
          <div className="bg-white rounded-[24px] border border-slate-200 overflow-hidden shadow-sm flex flex-col min-h-[500px]">
            {activeTab === 'general' && renderGeneralTab(school)}
            {activeTab === 'schedules' && renderSchedulesTab(school)}
            {activeTab === 'pickups' && renderPickupsTab(school)}
          </div>
        </div>
      </SchoolBusPageShell>

      {renderDialogs()}
    </>
  );

  // --- Sub-renderer: General Tab ---
  function renderGeneralTab(school: SchoolBusSchool) {
    return (
      <div className="flex flex-col lg:flex-row flex-1 min-h-0">
        {/* Left Column: Details & Warnings */}
        <div className="w-full lg:w-[380px] shrink-0 border-r border-slate-200 p-6 space-y-6">
          <div className="space-y-4">
            <h3 className="text-xs font-bold text-slate-400 uppercase tracking-widest">School Profile</h3>
            <div className="bg-slate-50/50 border border-slate-100 rounded-2xl p-5 space-y-4">
              <div className="flex flex-col gap-0.5">
                <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Identity Code</span>
                <span className="text-sm font-mono font-bold text-slate-800">{school.code || '—'}</span>
              </div>
              <div className="flex flex-col gap-0.5">
                <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Operational Status</span>
                <div>
                  <SchoolBusStatusBadge status={school.isActive ? 'ACTIVE' : 'INACTIVE'} />
                </div>
              </div>
              <div className="flex flex-col gap-0.5">
                <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Contact Phone</span>
                <span className="text-xs font-semibold text-slate-700">{school.contactPhone || '—'}</span>
              </div>
              <div className="flex flex-col gap-0.5">
                <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Contact Email</span>
                <span className="text-xs font-semibold text-slate-700">{school.contactEmail || '—'}</span>
              </div>
              <div className="flex flex-col gap-0.5">
                <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Address</span>
                <span className="text-xs leading-relaxed text-slate-600">{school.address || '—'}</span>
              </div>
              <div className="flex flex-col gap-0.5">
                <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Coordinates</span>
                {school.latitude != null ? (
                  <span className="text-xs font-mono font-medium text-slate-700">
                    {school.latitude.toFixed(6)}, {school.longitude?.toFixed(6)}
                  </span>
                ) : (
                  <span className="text-xs text-amber-600 font-semibold flex items-center gap-1">
                    <AlertTriangle className="h-3.5 w-3.5" /> Coords missing
                  </span>
                )}
              </div>
            </div>
          </div>

          {/* Warnings Panel */}
          {(!school.latitude || school.anyLinkedPointMissingCoordinates) && (
            <div className="rounded-2xl border border-amber-200 bg-amber-50/50 p-5 space-y-3">
              <h4 className="text-xs font-bold text-amber-800 flex items-center gap-1.5">
                <AlertTriangle className="h-4 w-4 shrink-0 text-amber-600" /> Network Warnings
              </h4>
              <div className="text-xs text-amber-700 space-y-1.5 leading-relaxed">
                {!school.latitude && (
                  <p>• The school itself is missing coordinates, preventing it from showing on map overlays.</p>
                )}
                {school.anyLinkedPointMissingCoordinates && (
                  <p>• One or more linked pickup points do not have coordinates set. Check Linked Pickups list.</p>
                )}
              </div>
            </div>
          )}
        </div>

        {/* Right Column: Operations Map */}
        <div className="flex-1 min-h-[400px] flex flex-col bg-slate-50 relative p-4">
          <MapMarkerVisibilityProvider>
            <SchoolBusMapWorkspace
              flat
              className="flex-1 rounded-2xl overflow-hidden border border-slate-200 shadow-sm"
              mapHeightClassName="h-full"
              panelClassName="w-[300px] p-0 flex flex-col h-full min-h-0 bg-white"
              map={
                hasMapData ? (
                  <OperationsMap
                    schools={mapSchools}
                    depots={[]}
                    pickupPoints={mapPickupPoints}
                    selectedSchoolId={schoolId}
                    selectedPickupPointId={selectedPickupPointId}
                    onSchoolSelect={() => {}}
                    onPickupPointSelect={(id) => setSelectedPickupPointId(id)}
                    fitSelectedKey={fitSelectedKey}
                    className="h-full w-full"
                  />
                ) : (
                  <div className="flex h-full flex-col items-center justify-center gap-4 bg-[#f8fafc] px-6 text-center">
                    <div className="flex h-16 w-16 items-center justify-center rounded-2xl border-2 border-dashed border-slate-200 bg-white shadow-sm">
                      <MapPin className="h-8 w-8 text-slate-300" />
                    </div>
                    <div>
                      <p className="text-sm font-semibold text-slate-600">No map markers yet</p>
                      <p className="mt-1 max-w-[240px] text-xs leading-5 text-slate-400">
                        Map markers will appear once coordinates are configured for the school or linked points.
                      </p>
                    </div>
                  </div>
                )
              }
            />
          </MapMarkerVisibilityProvider>
        </div>
      </div>
    );
  }

  // --- Sub-renderer: Schedules Tab ---
  function renderSchedulesTab(school: SchoolBusSchool) {
    return (
      <div className="flex flex-col lg:flex-row flex-1 min-h-0">
        {/* Left rail filter panel */}
        <div className="w-full lg:w-[280px] shrink-0 border-r border-slate-200 p-5 space-y-5 bg-slate-50/40">
          <h3 className="text-xs font-bold text-slate-400 uppercase tracking-widest">Schedules Manager</h3>

          <div className="space-y-4">
            {/* Shift Filter */}
            <div className="space-y-1.5">
              <label className="text-[10px] font-bold uppercase tracking-wider text-slate-400">Shift Type</label>
              <div className="grid grid-cols-3 gap-1 rounded-xl bg-slate-100 p-1">
                {(['ALL', 'MORNING', 'AFTERNOON'] as const).map((shift) => (
                  <button
                    key={shift}
                    onClick={() => setScheduleShiftFilter(shift)}
                    className={cn(
                      'rounded-lg py-1 text-[10px] font-bold text-center capitalize transition-all',
                      scheduleShiftFilter === shift
                        ? 'bg-white shadow-sm text-slate-900'
                        : 'text-slate-500 hover:text-slate-800'
                    )}
                  >
                    {shift === 'ALL' ? 'All' : shift.toLowerCase()}
                  </button>
                ))}
              </div>
            </div>

            {/* Day filter */}
            <div className="space-y-1.5">
              <label className="text-[10px] font-bold uppercase tracking-wider text-slate-400">Day of Week</label>
              <select
                value={scheduleDayFilter}
                onChange={(e) => setScheduleDayFilter(e.target.value)}
                className="w-full rounded-xl border border-slate-200 bg-white py-1.5 px-3 text-xs font-medium text-slate-700 focus:outline-none focus:border-slate-300 focus:ring-1 focus:ring-slate-200 shadow-sm"
              >
                <option value="ALL">All operational days</option>
                {daysOfWeekList.map((day) => (
                  <option key={day} value={day.toUpperCase()}>
                    {day}
                  </option>
                ))}
              </select>
            </div>

            {/* View switcher */}
            <div className="space-y-1.5">
              <label className="text-[10px] font-bold uppercase tracking-wider text-slate-400">Layout mode</label>
              <div className="grid grid-cols-2 gap-1 rounded-xl bg-slate-100 p-1">
                <button
                  onClick={() => setScheduleViewMode('table')}
                  className={cn(
                    'flex items-center justify-center gap-1.5 rounded-lg py-1.5 text-[10px] font-bold transition-all',
                    scheduleViewMode === 'table' ? 'bg-white shadow-sm text-slate-900' : 'text-slate-500'
                  )}
                >
                  <Table2 className="h-3 w-3" /> Table
                </button>
                <button
                  onClick={() => setScheduleViewMode('calendar')}
                  className={cn(
                    'flex items-center justify-center gap-1.5 rounded-lg py-1.5 text-[10px] font-bold transition-all',
                    scheduleViewMode === 'calendar' ? 'bg-white shadow-sm text-slate-900' : 'text-slate-500'
                  )}
                >
                  <Calendar className="h-3 w-3" /> Calendar
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* Right main workspace */}
        <div className="flex-1 overflow-auto p-6 min-h-0">
          {filteredSchedules.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-full gap-4 text-center py-20 bg-slate-50/50 rounded-2xl border border-dashed border-slate-200">
              <CalendarClock className="h-10 w-10 text-slate-300 animate-pulse" />
              <div>
                <p className="text-sm font-semibold text-slate-600">No schedules configured</p>
                <p className="text-xs text-slate-400 max-w-xs mt-1">
                  Create operational schedules to specify school arrival/departure times.
                </p>
              </div>
            </div>
          ) : scheduleViewMode === 'table' ? (
            <div className="overflow-x-auto border border-slate-100 rounded-2xl">
              <table className="w-full text-left border-collapse bg-white">
                <thead>
                  <tr className="border-b border-slate-200 bg-slate-50/80 text-[10px] font-bold text-slate-400 uppercase tracking-wider">
                    <th className="py-3 px-4 pl-6">Schedule</th>
                    <th className="py-3 px-4">Code</th>
                    <th className="py-3 px-4">Shift</th>
                    <th className="py-3 px-4">Days</th>
                    <th className="py-3 px-4">Time window</th>
                    <th className="py-3 px-4">Effective Range</th>
                    <th className="py-3 px-4">Status</th>
                    <th className="py-3 px-4 pr-6 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 text-xs">
                  {filteredSchedules.map((sch) => (
                    <tr key={sch.id} className="hover:bg-slate-50/30 transition-colors">
                      <td className="py-3.5 px-4 pl-6 font-bold text-slate-800">
                        <div className="flex items-center gap-1.5">
                          {sch.scheduleName}
                          {sch.isDefault && (
                            <span className="bg-red-50 text-[#C81E3A] px-1 py-0.5 rounded text-[9px] font-bold border border-red-100">
                              Default
                            </span>
                          )}
                        </div>
                      </td>
                      <td className="py-3.5 px-4 font-mono font-semibold text-[#C81E3A]">{sch.scheduleCode || '—'}</td>
                      <td className="py-3.5 px-4">
                        <span className={cn(
                          'px-2 py-0.5 rounded-full text-[10px] font-bold border',
                          sch.shiftType === 'MORNING'
                            ? 'bg-blue-50 text-blue-700 border-blue-100'
                            : 'bg-orange-50 text-orange-700 border-orange-100'
                        )}>
                          {sch.shiftType}
                        </span>
                      </td>
                      <td className="py-3.5 px-4 font-medium text-slate-600 truncate max-w-[140px]">{formatDaysCompact(sch.daysOfWeek)}</td>
                      <td className="py-3.5 px-4 font-bold text-slate-700">
                        {sch.arrivalDeadline || '—'} / {sch.departureTime || '—'}
                      </td>
                      <td className="py-3.5 px-4 font-medium text-slate-500">
                        {sch.effectiveFrom ? formatDate(sch.effectiveFrom) : '—'} to {sch.effectiveTo ? formatDate(sch.effectiveTo) : '—'}
                      </td>
                      <td className="py-3.5 px-4">
                        <SchoolBusStatusBadge status={sch.isActive ? 'ACTIVE' : 'INACTIVE'} />
                      </td>
                      <td className="py-3.5 px-4 pr-6 text-right">
                        <div className="flex justify-end gap-1.5">
                          <Button
                            size="icon"
                            variant="outline"
                            className="h-8 w-8 rounded-lg border-slate-200 text-slate-500 hover:bg-slate-50"
                            onClick={() => {
                              setEditingSchedule(sch);
                              setScheduleDialogOpen(true);
                            }}
                          >
                            <Edit2 className="h-3.5 w-3.5" />
                          </Button>
                          <Button
                            size="icon"
                            variant="outline"
                            className="h-8 w-8 rounded-lg border-slate-200 text-slate-500 hover:border-red-200 hover:bg-red-50 hover:text-[#C81E3A]"
                            onClick={() =>
                              setDeleteTarget({
                                type: 'schedule',
                                id: sch.id,
                                title: 'Delete Schedule',
                                description: 'Are you sure you want to delete this schedule? Associated routes will be affected.',
                              })
                            }
                          >
                            <Trash2 className="h-3.5 w-3.5" />
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            renderCalendarLayout()
          )}
        </div>
      </div>
    );
  }

  // Helper calendar renderer
  function renderCalendarLayout() {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-7 gap-3">
        {daysOfWeekList.map((day) => {
          const dayUpper = day.toUpperCase();
          const daySchedules = schedules.filter((s) =>
            s.daysOfWeek?.map((d) => d.toUpperCase()).includes(dayUpper)
          );

          return (
            <div key={day} className="flex flex-col rounded-2xl border border-slate-100 bg-slate-50/50 overflow-hidden min-h-[220px]">
              <div className="bg-slate-100/80 px-3 py-2 border-b border-slate-200 flex justify-between items-center shrink-0">
                <span className="text-xs font-bold text-slate-700">{day}</span>
                <span className="text-[10px] font-bold text-slate-400 bg-white border rounded-full px-1.5 py-0.5">
                  {daySchedules.length}
                </span>
              </div>
              <div className="flex-1 p-2 space-y-2 overflow-y-auto">
                {daySchedules.map((sch) => (
                  <div
                    key={sch.id}
                    className="p-2.5 bg-white border border-slate-200/60 rounded-xl hover:border-slate-300 transition-colors shadow-sm space-y-1.5 relative group"
                  >
                    <div className="flex justify-between items-start gap-1">
                      <span className="text-[10px] font-bold font-mono text-[#C81E3A]">{sch.scheduleCode}</span>
                      <span className={cn(
                        'px-1 py-0.5 rounded text-[8px] font-bold uppercase tracking-wide border',
                        sch.shiftType === 'MORNING'
                          ? 'bg-blue-50 text-blue-700 border-blue-100'
                          : 'bg-orange-50 text-orange-700 border-orange-100'
                      )}>
                        {sch.shiftType === 'MORNING' ? 'AM' : 'PM'}
                      </span>
                    </div>
                    <p className="text-xs font-bold text-slate-800 leading-snug line-clamp-1">{sch.scheduleName}</p>
                    <p className="text-[10px] font-semibold text-slate-500 font-mono flex items-center gap-1">
                      <Clock className="h-3 w-3 shrink-0" />
                      {sch.arrivalDeadline || '—'} / {sch.departureTime || '—'}
                    </p>
                    <div className="flex justify-end gap-1 opacity-0 group-hover:opacity-100 transition-opacity absolute top-1 right-1 bg-white pl-1 py-0.5">
                      <button
                        onClick={() => {
                          setEditingSchedule(sch);
                          setScheduleDialogOpen(true);
                        }}
                        className="text-slate-400 hover:text-slate-700 p-0.5"
                      >
                        <Edit2 className="h-3 w-3" />
                      </button>
                    </div>
                  </div>
                ))}
                {daySchedules.length === 0 && (
                  <div className="h-full flex items-center justify-center text-center p-3 text-[10px] text-slate-400">
                    No active slots
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>
    );
  }

  // --- Sub-renderer: Linked Pickups Tab ---
  function renderPickupsTab(school: SchoolBusSchool) {
    return (
      <div className="flex flex-col lg:flex-row flex-1 min-h-0">
        {/* Left rail filter panel */}
        <div className="w-full lg:w-[280px] shrink-0 border-r border-slate-200 p-5 space-y-5 bg-slate-50/40">
          <h3 className="text-xs font-bold text-slate-400 uppercase tracking-widest">Network Filter</h3>

          <div className="space-y-4">
            {/* Search */}
            <div className="space-y-1.5">
              <label className="text-[10px] font-bold uppercase tracking-wider text-slate-400">Search Point</label>
              <div className="relative">
                <Search className="absolute left-2.5 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-slate-400" />
                <input
                  type="text"
                  placeholder="Search name, code..."
                  value={pickupSearch}
                  onChange={(e) => setPickupSearch(e.target.value)}
                  className="w-full rounded-xl border border-slate-200 bg-white py-1.5 pl-8 pr-3 text-xs outline-none focus:border-slate-300 focus:ring-1 focus:ring-slate-200 shadow-sm"
                />
              </div>
            </div>

            {/* Usage Filter */}
            <div className="space-y-1.5">
              <label className="text-[10px] font-bold uppercase tracking-wider text-slate-400">Usage Type</label>
              <select
                value={pickupUsageFilter}
                onChange={(e) => setPickupUsageFilter(e.target.value as any)}
                className="w-full rounded-xl border border-slate-200 bg-white py-1.5 px-3 text-xs font-medium text-slate-700 focus:outline-none focus:border-slate-300 focus:ring-1 focus:ring-slate-200 shadow-sm"
              >
                <option value="ALL">All usage types</option>
                <option value="PICKUP">Pickup only</option>
                <option value="DROPOFF">Drop-off only</option>
                <option value="PICKUP_DROPOFF">Pickup & Drop-off</option>
              </select>
            </div>

            {/* Coordinate filter */}
            <div className="space-y-1.5">
              <label className="text-[10px] font-bold uppercase tracking-wider text-slate-400">Coordinates</label>
              <div className="grid grid-cols-3 gap-1 rounded-xl bg-slate-100 p-1">
                {(['ALL', 'CONFIGURED', 'MISSING'] as const).map((mode) => (
                  <button
                    key={mode}
                    onClick={() => setPickupCoordsFilter(mode)}
                    className={cn(
                      'rounded-lg py-1 text-[9px] font-bold text-center capitalize transition-all',
                      pickupCoordsFilter === mode
                        ? 'bg-white shadow-sm text-slate-900'
                        : 'text-slate-500 hover:text-slate-800'
                    )}
                  >
                    {mode === 'ALL' ? 'All' : mode.toLowerCase()}
                  </button>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* Right main workspace */}
        <div className="flex-1 overflow-auto p-6 min-h-0 space-y-4">
          {filteredPickups.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-full gap-4 text-center py-20 bg-slate-50/50 rounded-2xl border border-dashed border-slate-200">
              <Link2 className="h-10 w-10 text-slate-300 animate-pulse" />
              <div>
                <p className="text-sm font-semibold text-slate-600">No linked pickup points found</p>
                <p className="text-xs text-slate-400 max-w-xs mt-1">
                  Connect pickup points from the network database to establish routing paths.
                </p>
              </div>
            </div>
          ) : (
            <div className="space-y-3">
              {filteredPickups.map((lp) => {
                const isExpanded = expandedSppId === lp.id;
                const hasCoords = typeof lp.pickupPointLatitude === 'number' && typeof lp.pickupPointLongitude === 'number';

                return (
                  <div
                    key={lp.id}
                    className="border border-slate-200 hover:border-slate-300 rounded-[20px] bg-white transition-shadow shadow-sm overflow-hidden"
                  >
                    {/* Header info */}
                    <div className="flex items-center justify-between p-4 flex-wrap gap-3">
                      <div className="flex items-center gap-3">
                        <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-xl bg-slate-50 border border-slate-100 text-slate-500">
                          <MapPin className="h-4.5 w-4.5" />
                        </div>
                        <div>
                          <div className="flex items-center gap-2">
                            <h4 className="text-sm font-bold text-slate-900">{lp.pickupPointName}</h4>
                            {lp.isDefault && (
                              <span className="bg-indigo-50 text-indigo-700 px-2 py-0.5 rounded-full text-[9px] font-bold border border-indigo-100">
                                Default
                              </span>
                            )}
                          </div>
                          <p className="text-xs text-slate-400 mt-0.5 max-w-xl truncate">{lp.pickupPointAddress || 'No address'}</p>
                        </div>
                      </div>

                      {/* Detail metrics and badges */}
                      <div className="flex items-center gap-3">
                        <span className={cn('px-2.5 py-0.5 rounded-full text-[10px] font-bold', getUsageTypeBadgeClasses(lp.pickupPointUsageType))}>
                          {formatUsageType(lp.pickupPointUsageType)}
                        </span>

                        {hasCoords ? (
                          <span className="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-2 py-0.5 text-[10px] font-semibold text-emerald-700 ring-1 ring-inset ring-emerald-600/10">
                            <MapPinCheck className="h-3 w-3" /> Coords
                          </span>
                        ) : (
                          <span className="inline-flex items-center gap-1 rounded-full bg-amber-50 px-2 py-0.5 text-[10px] font-semibold text-amber-800 ring-1 ring-inset ring-amber-600/15">
                            <AlertTriangle className="h-3 w-3" /> No coords
                          </span>
                        )}

                        <div className="flex items-center gap-1 border-l border-slate-200 pl-3">
                          <Button
                            variant="outline"
                            size="sm"
                            className="h-8 rounded-lg text-slate-600 hover:bg-slate-50 text-xs font-semibold"
                            onClick={() => {
                              if (isExpanded) {
                                setExpandedSppId(null);
                              } else {
                                setExpandedSppId(lp.id);
                              }
                            }}
                          >
                            Windows ({isExpanded ? 'Collapse' : 'Manage'})
                          </Button>
                          <Button
                            variant="outline"
                            size="sm"
                            className="h-8 rounded-lg border-red-200 text-red-500 hover:bg-red-50 hover:text-[#C81E3A] text-xs font-semibold"
                            onClick={() =>
                              setDeleteTarget({
                                type: 'link',
                                id: lp.pickupPointId,
                                title: 'Unlink Pickup Point',
                                description: 'Unlink this pickup point? Associated time windows will also be deleted.',
                              })
                            }
                          >
                            <Unlink className="h-3.5 w-3.5" />
                          </Button>
                        </div>
                      </div>
                    </div>

                    {/* Expanded Window preview & list */}
                    {isExpanded && (
                      <div className="bg-slate-50/50 border-t border-slate-100 p-4 space-y-3">
                        <div className="flex justify-between items-center">
                          <span className="text-xs font-bold text-slate-700">Time Windows ({expandedWindows.length})</span>
                          <Button
                            onClick={() => {
                              setEditingWindow(null);
                              setWindowDialogOpen(true);
                            }}
                            size="sm"
                            className="h-7 rounded-lg text-[10px] font-bold bg-[#C81E3A] hover:bg-[#A6172D] text-white"
                          >
                            <Plus className="mr-1 h-3 w-3" /> Add window
                          </Button>
                        </div>

                        {expandedWindows.length === 0 ? (
                          <div className="text-center py-6 text-xs text-slate-400 bg-white border border-slate-150 rounded-xl">
                            No windows configured for this link. Add one to enable routing scheduling.
                          </div>
                        ) : (
                          <div className="overflow-x-auto border border-slate-200 rounded-xl bg-white">
                            <table className="w-full text-left border-collapse">
                              <thead>
                                <tr className="border-b border-slate-200 bg-slate-50 text-[10px] font-bold text-slate-400 uppercase tracking-wider">
                                  <th className="py-2.5 px-4 pl-5">Schedule</th>
                                  <th className="py-2.5 px-4">Direction</th>
                                  <th className="py-2.5 px-4">Time window</th>
                                  <th className="py-2.5 px-4">Est. distance</th>
                                  <th className="py-2.5 px-4">Est. duration</th>
                                  <th className="py-2.5 px-4 pr-5 text-right">Actions</th>
                                </tr>
                              </thead>
                              <tbody className="divide-y divide-slate-100 text-xs">
                                {expandedWindows.map((w) => (
                                  <tr key={w.id} className="hover:bg-slate-50/30">
                                    <td className="py-2.5 px-4 pl-5 font-bold text-slate-800">{w.scheduleName}</td>
                                    <td className="py-2.5 px-4 font-semibold text-slate-600">
                                      {w.direction === 'PICKUP_TO_SCHOOL' ? '🏫 Pickup → School' : '🏠 School → Drop-off'}
                                    </td>
                                    <td className="py-2.5 px-4 font-bold text-slate-700">
                                      {w.windowStart} – {w.windowEnd}
                                    </td>
                                    <td className="py-2.5 px-4 text-slate-500 font-medium">
                                      {w.estimatedDistanceToSchoolKm != null ? `${w.estimatedDistanceToSchoolKm} km` : '—'}
                                    </td>
                                    <td className="py-2.5 px-4 text-slate-500 font-medium">
                                      {w.estimatedDurationToSchoolMin != null ? `${w.estimatedDurationToSchoolMin} min` : '—'}
                                    </td>
                                    <td className="py-2.5 px-4 pr-5 text-right">
                                      <div className="flex justify-end gap-1">
                                        <button
                                          onClick={() => {
                                            setEditingWindow(w);
                                            setWindowDialogOpen(true);
                                          }}
                                          className="text-slate-400 hover:text-slate-700 p-1"
                                        >
                                          <Edit2 className="h-3 w-3" />
                                        </button>
                                        <button
                                          onClick={() =>
                                            setDeleteTarget({
                                              type: 'window',
                                              id: w.id,
                                              title: 'Delete Time Window',
                                              description: 'Are you sure you want to delete this window config?',
                                            })
                                          }
                                          className="text-slate-400 hover:text-[#C81E3A] p-1"
                                        >
                                          <Trash2 className="h-3 w-3" />
                                        </button>
                                      </div>
                                    </td>
                                  </tr>
                                ))}
                              </tbody>
                            </table>
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    );
  }

  // --- Shared Dialog Renderer ---
  function renderDialogs() {
    return (
      <>
        {/* School Form Dialog */}
        <SchoolFormDialog
          open={schoolDialogOpen}
          onOpenChange={(open) => {
            setSchoolDialogOpen(open);
          }}
          initialData={school}
          isLoading={updatingSchool}
          onSubmit={handleEditSchool}
        />

        {/* Schedule Form Dialog */}
        <ScheduleFormDialog
          open={scheduleDialogOpen}
          onOpenChange={(open) => {
            setScheduleDialogOpen(open);
            if (!open) setEditingSchedule(null);
          }}
          initialData={editingSchedule}
          isLoading={creatingSchedule || updatingSchedule}
          onSubmit={handleSaveSchedule}
        />

        {/* Link Pickup Point Dialog */}
        <LinkPickupPointDialog
          open={linkDialogOpen}
          onOpenChange={setLinkDialogOpen}
          pickupPoints={allPickupPoints}
          isLoading={linkingPickup}
          onSubmit={handleLinkPickupPoint}
        />

        {/* Window Form Dialog */}
        <WindowFormDialog
          open={windowDialogOpen}
          onOpenChange={(open) => {
            setWindowDialogOpen(open);
            if (!open) setEditingWindow(null);
          }}
          schedules={schedules}
          existingWindow={editingWindow}
          usageType={
            linkedPickupPoints.find((lp) => lp.id === expandedSppId)
              ?.pickupPointUsageType
          }
          isLoading={creatingWindow || updatingWindow}
          onSubmit={handleSaveWindow}
        />

        {/* Delete Confirmation Dialog */}
        <SchoolBusDeleteDialog
          open={Boolean(deleteTarget)}
          onOpenChange={(open) => {
            if (!open) setDeleteTarget(null);
          }}
          title={deleteTarget?.title || 'Delete confirmation'}
          description={deleteTarget?.description || 'This operation is permanent.'}
          isLoading={deletingSchedule || unlinkingPickup || deletingWindow}
          onConfirm={handleConfirmDelete}
        />
      </>
    );
  }
}
