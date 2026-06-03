'use client';

import * as React from 'react';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import {
  AlertTriangle,
  CalendarClock,
  ChevronDown,
  ChevronRight,
  Clock,
  Eye,
  GraduationCap,
  Link2,
  Map,
  MapPin,
  MapPinCheck,
  Pencil,
  Plus,
  Search,
  Table2,
  Trash2,
  Unlink,
} from 'lucide-react';
import { toast } from 'sonner';
import { Button, Tooltip, TooltipTrigger, TooltipContent, TooltipProvider } from '@/shared/components/ui';
import { SchoolBusSchoolDetailPage } from './SchoolBusSchoolDetailPage';
import { cn } from '@/shared/utils';
import {
  useCreatePickupPointMutation,
  useCreateSchoolMutation,
  useCreateSchoolScheduleMutation,
  useCreateSchoolPickupPointWindowMutation,
  useDeletePickupPointMutation,
  useDeleteSchoolMutation,
  useDeleteSchoolScheduleMutation,
  useDeleteSchoolPickupPointWindowMutation,
  useGetAllActiveSchoolPickupLinksQuery,
  useGetDepotsQuery,
  useGetPickupPointsQuery,
  useGetSchoolPickupPointsQuery,
  useGetSchoolPickupPointWindowsQuery,
  useGetSchoolSchedulesQuery,
  useGetSchoolsQuery,
  useLinkSchoolPickupPointMutation,
  useUnlinkSchoolPickupPointMutation,
  useUpdatePickupPointMutation,
  useUpdateSchoolMutation,
  useUpdateSchoolScheduleMutation,
  useUpdateSchoolPickupPointWindowMutation,
} from '../api/schoolBusApi';
import { SchoolBusDeleteDialog } from '../components/SchoolBusDeleteDialog';
import {
  PickupPointFormDialog,
  SchoolFormDialog,
} from '../components/SchoolBusMasterDataForms';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusPaginationBar } from '../components/SchoolBusPaginationBar';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusScrollableTable } from '../components/SchoolBusScrollableTable';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { SchoolBusDataTable } from '../components/ui/SchoolBusDataTable';
import type { SchoolBusTableColumn } from '../components/ui/SchoolBusDataTable';
import { SchoolBusSelect } from '../components/ui/SchoolBusSelect';
import { MapToolbar } from '../components/map/MapToolbar';
import { MapMarkerVisibilityProvider } from '../components/map/MapMarkerVisibilityContext';
import { OperationsMap } from '../components/map/OperationsMap';
import { SchoolBusMapLegend } from '../components/map/SchoolBusMapLegend';
import { SchoolBusMapWorkspace } from '../components/map/SchoolBusMapWorkspace';
import { ScheduleFormDialog } from '../components/forms/ScheduleFormDialog';
import { LinkPickupPointDialog } from '../components/forms/LinkPickupPointDialog';
import { WindowFormDialog } from '../components/forms/WindowFormDialog';
import { useSchoolBusPagination } from '../hooks/useSchoolBusPagination';
import type { SchoolBusDepot, SchoolBusPickupPoint, SchoolBusSchedule, SchoolBusSchool, SchoolBusSchoolPickupPointWindow } from '../types';
import { formatDate, getPageItems, SCHOOL_BUS_OPTION_QUERY } from '../utils';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';

type DeleteTarget =
  | { type: 'school'; entity: SchoolBusSchool }
  | { type: 'pickup'; entity: SchoolBusPickupPoint }
  | null;

type ViewMode = 'directory' | 'network';

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
  return days.map(d => dayMap[d.toUpperCase()] || d).join(', ');
};

const formatUsageType = (type: string | undefined | null) => {
  if (type === 'PICKUP' || type === 'PICKUP_ONLY') return 'Pickup only';
  if (type === 'DROPOFF' || type === 'DROPOFF_ONLY') return 'Drop-off only';
  if (type === 'PICKUP_DROPOFF') return 'Pickup & drop-off';
  return type || 'N/A';
};

const getUsageTypeBadgeClasses = (type: string | undefined | null) => {
  if (type === 'PICKUP' || type === 'PICKUP_ONLY') return 'bg-blue-50 text-blue-700 border border-blue-200';
  if (type === 'DROPOFF' || type === 'DROPOFF_ONLY') return 'bg-emerald-50 text-emerald-700 border border-emerald-200';
  return 'bg-indigo-50 text-indigo-700 border border-indigo-200';
};

// ── Shared InfoRow ────────────────────────────────────────────────────────────

// ── DetailField Helper ────────────────────────────────────────────────────────
function DetailField({ label, value, warn = false }: { label: string; value: React.ReactNode; warn?: boolean }) {
  return (
    <div className='flex flex-col gap-0.5'>
      <span className='text-[10px] font-semibold text-slate-400 uppercase tracking-wider'>{label}</span>
      <div className={cn('text-xs font-medium text-slate-700', warn && 'text-amber-600 font-semibold')}>
        {value}
      </div>
    </div>
  );
}

// ── Network Map Detail Panel ──────────────────────────────────────────────────

interface DetailPanelProps {
  selectedSchool: SchoolBusSchool | null;
  selectedPickup: SchoolBusPickupPoint | null;
  linkedPickupCount: number;
  scheduleCount: number;
  onEditSchool: (s: SchoolBusSchool) => void;
  onEditPickup: (p: SchoolBusPickupPoint) => void;
  onLinkPickup: () => void;
  onManageSchedules: () => void;
}

function NetworkDetailPanel({
  selectedSchool,
  selectedPickup,
  linkedPickupCount,
  scheduleCount,
  onEditSchool,
  onEditPickup,
  onLinkPickup,
  onManageSchedules,
}: DetailPanelProps) {
  if (!selectedSchool && !selectedPickup) {
    return (
      <div className='flex h-full flex-col items-center justify-center gap-3 px-6 py-12 text-center bg-slate-50/20'>
        <div className='flex h-12 w-12 items-center justify-center rounded-2xl border border-slate-200 bg-white shadow-sm text-slate-400'>
          <MapPin className='h-5 w-5' />
        </div>
        <div className='space-y-1.5'>
          <p className='text-sm font-semibold text-slate-800'>Select school or pickup</p>
          <p className='max-w-[240px] text-xs leading-5 text-slate-400'>
            Click a marker on the map or a list item to inspect network details.
          </p>
        </div>
      </div>
    );
  }

  if (selectedSchool) {
    return (
      <div className='flex h-full flex-col overflow-hidden min-h-0 bg-white'>
        {/* Header */}
        <div className='flex items-start gap-3 p-5 border-b border-slate-100 bg-white shrink-0'>
          <div className='flex h-9 w-9 shrink-0 items-center justify-center rounded-2xl bg-red-50 border border-red-100'>
            <GraduationCap className='h-4.5 w-4.5 text-[#C81E3A]' />
          </div>
          <div className='min-w-0 flex-1'>
            <p className='text-[10px] font-bold uppercase tracking-widest text-[#C81E3A]'>School</p>
            <h4 className='mt-0.5 text-sm font-bold leading-snug text-slate-900 truncate' title={selectedSchool.name}>
              {selectedSchool.name}
            </h4>
          </div>
        </div>

        {/* Details (Scrollable Body) */}
        <div className='flex-1 overflow-y-auto p-5 space-y-4 min-h-0'>
          {/* Identity Section */}
          <div className='space-y-1.5'>
            <p className='text-[10px] font-bold uppercase tracking-wider text-slate-400 px-1'>Identity</p>
            <div className='bg-slate-50/60 border border-slate-100 rounded-2xl p-4 space-y-3'>
              <DetailField label='Code' value={selectedSchool.code ?? '—'} />
              <DetailField
                label='Status'
                value={
                  selectedSchool.isActive ? (
                    <span className='inline-flex items-center rounded-full bg-emerald-50 px-2.5 py-0.5 text-[10px] font-semibold text-emerald-700 ring-1 ring-inset ring-emerald-600/10'>
                      Active
                    </span>
                  ) : (
                    <span className='inline-flex items-center rounded-full bg-slate-100 px-2.5 py-0.5 text-[10px] font-semibold text-slate-600 ring-1 ring-inset ring-slate-500/10'>
                      Inactive
                    </span>
                  )
                }
              />
            </div>
          </div>

          {/* Location Section */}
          <div className='space-y-1.5'>
            <p className='text-[10px] font-bold uppercase tracking-wider text-slate-400 px-1'>Location</p>
            <div className='bg-slate-50/60 border border-slate-100 rounded-2xl p-4 space-y-3'>
              <DetailField label='Address' value={selectedSchool.address ?? '—'} />
              <DetailField
                label='Coordinates'
                value={
                  typeof selectedSchool.latitude === 'number' && typeof selectedSchool.longitude === 'number'
                    ? `${selectedSchool.latitude.toFixed(5)}, ${selectedSchool.longitude.toFixed(5)}`
                    : '⚠ Not set'
                }
                warn={typeof selectedSchool.latitude !== 'number'}
              />
            </div>
          </div>

          {/* Operations Section */}
          <div className='space-y-1.5'>
            <p className='text-[10px] font-bold uppercase tracking-wider text-slate-400 px-1'>Operations</p>
            <div className='bg-slate-50/60 border border-slate-100 rounded-2xl p-4 space-y-3'>
              <DetailField label='Contact' value={selectedSchool.contactPhone ?? '—'} />
              <DetailField label='Linked pickups' value={String(linkedPickupCount)} />
              <DetailField label='Schedules' value={String(scheduleCount)} />
            </div>
          </div>
        </div>

        {/* Actions (Sticky Footer) */}
        <div className='shrink-0 flex flex-col gap-2 border-t border-slate-100 p-5 bg-white'>
          <Button
            size='sm'
            variant='outline'
            className='w-full justify-start border-slate-200 text-slate-700 hover:bg-slate-50 hover:text-slate-900 hover:border-slate-300'
            onClick={() => onEditSchool(selectedSchool)}
          >
            <Pencil className='mr-2.5 h-3.5 w-3.5 text-slate-400' /> Edit school
          </Button>
          <Button
            size='sm'
            variant='outline'
            className='w-full justify-start border-red-200 text-[#C81E3A] bg-red-50/10 hover:bg-red-50 hover:border-red-300 hover:text-[#99182D]'
            onClick={onLinkPickup}
          >
            <Link2 className='mr-2.5 h-3.5 w-3.5 text-[#C81E3A]/70' /> Link pickup point
          </Button>
          <Button
            size='sm'
            variant='outline'
            className='w-full justify-start border-slate-200 text-slate-700 hover:bg-slate-50 hover:text-slate-900 hover:border-slate-300'
            onClick={onManageSchedules}
          >
            <CalendarClock className='mr-2.5 h-3.5 w-3.5 text-slate-400' /> Manage schedules
          </Button>
        </div>
      </div>
    );
  }

  if (selectedPickup) {
    const isPickup = selectedPickup.usageType === 'PICKUP' || selectedPickup.usageType === 'PICKUP_ONLY';
    const isDropoff = selectedPickup.usageType === 'DROPOFF' || selectedPickup.usageType === 'DROPOFF_ONLY';
    const PickupIcon = isDropoff ? MapPinCheck : MapPin;
    
    const iconColor = isDropoff
      ? 'text-emerald-600 bg-emerald-50 border-emerald-100'
      : isPickup
        ? 'text-blue-600 bg-blue-50 border-blue-100'
        : 'text-indigo-600 bg-indigo-50 border-indigo-100';

    const labelColor = isDropoff
      ? 'text-[#10B981]'
      : isPickup
        ? 'text-blue-600'
        : 'text-indigo-600';

    const labelText = isDropoff
      ? 'Drop-off Point'
      : isPickup
        ? 'Pickup Point'
        : 'Pickup & Drop-off';

    return (
      <div className='flex h-full flex-col overflow-hidden min-h-0 bg-white'>
        {/* Header */}
        <div className='flex items-start gap-3 p-5 border-b border-slate-100 bg-white shrink-0'>
          <div className={cn('flex h-9 w-9 shrink-0 items-center justify-center rounded-2xl border', iconColor)}>
            <PickupIcon className='h-4.5 w-4.5' />
          </div>
          <div className='min-w-0 flex-1'>
            <p className={cn('text-[10px] font-bold uppercase tracking-widest', labelColor)}>{labelText}</p>
            <h4 className='mt-0.5 text-sm font-bold leading-snug text-slate-900 truncate' title={selectedPickup.name}>
              {selectedPickup.name}
            </h4>
          </div>
        </div>

        {/* Details (Scrollable Body) */}
        <div className='flex-1 overflow-y-auto p-5 space-y-4 min-h-0'>
          {/* Identity Section */}
          <div className='space-y-1.5'>
            <p className='text-[10px] font-bold uppercase tracking-wider text-slate-400 px-1'>Identity</p>
            <div className='bg-slate-50/60 border border-slate-100 rounded-2xl p-4 space-y-3'>
              <DetailField label='Usage type' value={formatUsageType(selectedPickup.usageType)} />
              <DetailField label='Zone code' value={selectedPickup.zoneCode ?? '—'} />
            </div>
          </div>

          {/* Location Section */}
          <div className='space-y-1.5'>
            <p className='text-[10px] font-bold uppercase tracking-wider text-slate-400 px-1'>Location</p>
            <div className='bg-slate-50/60 border border-slate-100 rounded-2xl p-4 space-y-3'>
              <DetailField label='Address' value={selectedPickup.address ?? '—'} />
              <DetailField
                label='Coordinates'
                value={
                  typeof selectedPickup.latitude === 'number' && typeof selectedPickup.longitude === 'number'
                    ? `${selectedPickup.latitude.toFixed(5)}, ${selectedPickup.longitude.toFixed(5)}`
                    : '⚠ Not set'
                }
                warn={typeof selectedPickup.latitude !== 'number'}
              />
            </div>
          </div>
        </div>

        {/* Actions (Sticky Footer) */}
        <div className='shrink-0 flex flex-col gap-2 border-t border-slate-100 p-5 bg-white'>
          <Button
            size='sm'
            variant='outline'
            className='w-full justify-start border-slate-200 text-slate-700 hover:bg-slate-50 hover:text-slate-900 hover:border-slate-300'
            onClick={() => onEditPickup(selectedPickup)}
          >
            <Pencil className='mr-2.5 h-3.5 w-3.5 text-slate-400' /> Edit pickup point
          </Button>
          <Button
            size='sm'
            variant='outline'
            className='w-full justify-start border-red-200 text-[#C81E3A] bg-red-50/10 hover:bg-red-50 hover:border-red-300 hover:text-[#99182D]'
            onClick={onLinkPickup}
          >
            <Link2 className='mr-2.5 h-3.5 w-3.5 text-[#C81E3A]/70' /> Link to school
          </Button>
        </div>
      </div>
    );
  }

  return null;
}

// ── Main Page ─────────────────────────────────────────────────────────────────

export function SchoolBusSchoolsPage() {
  const [selectedDetailSchoolId, setSelectedDetailSchoolId] = React.useState<number | null>(null);
  const [viewMode, setViewMode] = React.useState<ViewMode>('network');
  const [activeTab, setActiveTab] = React.useState('schools');
  const [networkFilter, setNetworkFilter] = React.useState('');

  // ─── Directory search & filter states ────────────────────────────
  const [schoolSearchValue, setSchoolSearchValue] = React.useState('');
  const [schoolStatusFilter, setSchoolStatusFilter] = React.useState<'ALL' | 'ACTIVE' | 'INACTIVE'>('ALL');
  const [schoolCoordsFilter, setSchoolCoordsFilter] = React.useState<'ALL' | 'MISSING'>('ALL');

  const [pickupSearchValue, setPickupSearchValue] = React.useState('');
  const [pickupUsageFilter, setPickupUsageFilter] = React.useState<'ALL' | 'PICKUP' | 'DROPOFF' | 'PICKUP_DROPOFF'>('ALL');
  const [pickupCoordsFilter, setPickupCoordsFilter] = React.useState<'ALL' | 'MISSING'>('ALL');

  const [scheduleSearch, setScheduleSearch] = React.useState('');
  const [linkedSearch, setLinkedSearch] = React.useState('');

  const schoolsPagination = useSchoolBusPagination({ page: 0, size: 10, sortBy: 'name', sortDirection: 'ASC' });
  const pickupPagination = useSchoolBusPagination({ page: 0, size: 10, sortBy: 'name', sortDirection: 'ASC' });

  const { data, isLoading: loadingSchools } = useGetSchoolsQuery(schoolsPagination.params);
  const { data: allSchoolsData } = useGetSchoolsQuery({ ...SCHOOL_BUS_OPTION_QUERY, sortBy: 'name' });
  const { data: pickupPointsData, isLoading: loadingPickups } = useGetPickupPointsQuery(pickupPagination.params);
  const { data: depotsData } = useGetDepotsQuery({ ...SCHOOL_BUS_OPTION_QUERY, sortBy: 'name' });
  const { data: allLinksData } = useGetAllActiveSchoolPickupLinksQuery();

  const [createSchool, { isLoading: creatingSchool }] = useCreateSchoolMutation();
  const [updateSchool, { isLoading: updatingSchool }] = useUpdateSchoolMutation();
  const [deleteSchool, { isLoading: deletingSchool }] = useDeleteSchoolMutation();
  const [createPickupPoint, { isLoading: creatingPickup }] = useCreatePickupPointMutation();
  const [updatePickupPoint, { isLoading: updatingPickup }] = useUpdatePickupPointMutation();
  const [deletePickupPoint, { isLoading: deletingPickup }] = useDeletePickupPointMutation();
  const [createSchedule, { isLoading: creatingSchedule }] = useCreateSchoolScheduleMutation();
  const [updateSchedule, { isLoading: updatingSchedule }] = useUpdateSchoolScheduleMutation();
  const [deleteSchedule] = useDeleteSchoolScheduleMutation();
  const [linkPickupPoint, { isLoading: linkingPickup }] = useLinkSchoolPickupPointMutation();
  const [unlinkPickupPoint] = useUnlinkSchoolPickupPointMutation();
  const [createWindow, { isLoading: creatingWindow }] = useCreateSchoolPickupPointWindowMutation();
  const [updateWindow, { isLoading: updatingWindow }] = useUpdateSchoolPickupPointWindowMutation();
  const [deleteWindow] = useDeleteSchoolPickupPointWindowMutation();

  const schools = getPageItems(data?.data);
  const allSchools = getPageItems(allSchoolsData?.data);
  const pickupPoints = getPageItems(pickupPointsData?.data);
  const depots = getPageItems(depotsData?.data);
  const allLinks = allLinksData?.data ?? [];

  // ─── Selection state ─────────────────────────────────────────────
  const [selectedSchoolId, setSelectedSchoolId] = React.useState<number | null>(null);
  const [selectedPickupPointId, setSelectedPickupPointId] = React.useState<number | null>(null);

  // ─── Dialog state ────────────────────────────────────────────────
  const [schoolDialogOpen, setSchoolDialogOpen] = React.useState(false);
  const [pickupDialogOpen, setPickupDialogOpen] = React.useState(false);
  const [scheduleDialogOpen, setScheduleDialogOpen] = React.useState(false);
  const [linkDialogOpen, setLinkDialogOpen] = React.useState(false);
  const [windowDialogOpen, setWindowDialogOpen] = React.useState(false);
  const [editingSchool, setEditingSchool] = React.useState<SchoolBusSchool | null>(null);
  const [editingPickup, setEditingPickup] = React.useState<SchoolBusPickupPoint | null>(null);
  const [editingSchedule, setEditingSchedule] = React.useState<SchoolBusSchedule | null>(null);
  const [editingWindow, setEditingWindow] = React.useState<SchoolBusSchoolPickupPointWindow | null>(null);
  const [deleteTarget, setDeleteTarget] = React.useState<DeleteTarget>(null);
  const [expandedSppId, setExpandedSppId] = React.useState<number | null>(null);

  // ─── Map fit controls ────────────────────────────────────────────
  const [fitAllKey, setFitAllKey] = React.useState(0);
  const [fitSelectedKey, setFitSelectedKey] = React.useState(0);
  const handleFitAll = React.useCallback(() => setFitAllKey((k) => k + 1), []);
  const handleFitSelected = React.useCallback(() => setFitSelectedKey((k) => k + 1), []);

  React.useEffect(() => {
    if (!selectedSchoolId && schools.length > 0) setSelectedSchoolId(schools[0].id);
  }, [schools, selectedSchoolId]);

  // ─── Schedule & linked data ──────────────────────────────────────
  const { data: schedulesData, isLoading: loadingSchedules } = useGetSchoolSchedulesQuery(
    { schoolId: selectedSchoolId!, page: 0, size: 50 },
    { skip: !selectedSchoolId }
  );
  const { data: linkedPickupData } = useGetSchoolPickupPointsQuery(
    { schoolId: selectedSchoolId!, page: 0, size: 50 },
    { skip: !selectedSchoolId }
  );
  const schedules = getPageItems(schedulesData?.data);
  const linkedPickupPoints = getPageItems(linkedPickupData?.data);
  const allPickupPointsForLink = getPageItems(pickupPointsData?.data);

  // ─── Memoized directory filtered tables ──────────────────────────
  const filteredSchoolsTable = React.useMemo(() => {
    return schools.filter((school) => {
      const matchesStatus = schoolStatusFilter === 'ALL'
        ? true
        : schoolStatusFilter === 'ACTIVE'
          ? school.isActive
          : !school.isActive;
      const matchesCoords = schoolCoordsFilter === 'ALL'
        ? true
        : typeof school.latitude !== 'number' || typeof school.longitude !== 'number';
      return matchesStatus && matchesCoords;
    });
  }, [schools, schoolStatusFilter, schoolCoordsFilter]);

  const filteredPickupsTable = React.useMemo(() => {
    return pickupPoints.filter((pp) => {
      const matchesUsage = pickupUsageFilter === 'ALL'
        ? true
        : pickupUsageFilter === 'PICKUP'
          ? (pp.usageType === 'PICKUP' || pp.usageType === 'PICKUP_ONLY')
          : pickupUsageFilter === 'DROPOFF'
            ? (pp.usageType === 'DROPOFF' || pp.usageType === 'DROPOFF_ONLY')
            : pp.usageType === 'PICKUP_DROPOFF';
      const matchesCoords = pickupCoordsFilter === 'ALL'
        ? true
        : typeof pp.latitude !== 'number' || typeof pp.longitude !== 'number';
      return matchesUsage && matchesCoords;
    });
  }, [pickupPoints, pickupUsageFilter, pickupCoordsFilter]);

  const filteredSchedulesTable = React.useMemo(() => {
    if (!scheduleSearch) return schedules;
    const q = scheduleSearch.toLowerCase();
    return schedules.filter(
      (sch) =>
        sch.scheduleName.toLowerCase().includes(q) ||
        (sch.scheduleCode ?? '').toLowerCase().includes(q)
    );
  }, [schedules, scheduleSearch]);

  const filteredLinkedPickupsTable = React.useMemo(() => {
    if (!linkedSearch) return linkedPickupPoints;
    const q = linkedSearch.toLowerCase();
    return linkedPickupPoints.filter(
      (lp) =>
        lp.pickupPointName.toLowerCase().includes(q) ||
        (lp.pickupPointAddress ?? '').toLowerCase().includes(q)
    );
  }, [linkedPickupPoints, linkedSearch]);

  const { data: windowsData } = useGetSchoolPickupPointWindowsQuery(expandedSppId!, { skip: !expandedSppId });
  const windows = windowsData?.data ?? [];

  // ─── Derived stats ───────────────────────────────────────────────
  const missingCoordsCount =
    schools.filter((s) => typeof s.latitude !== 'number').length +
    pickupPoints.filter((p) => typeof p.latitude !== 'number').length;

  const canFitAll =
    schools.some((s) => typeof s.latitude === 'number') ||
    pickupPoints.some((p) => typeof p.latitude === 'number') ||
    depots.some((d) => typeof d.latitude === 'number');

  const selectedSchoolObj = schools.find((s) => s.id === selectedSchoolId) ?? allSchools.find((s) => s.id === selectedSchoolId) ?? null;
  const selectedPickupObj = pickupPoints.find((p) => p.id === selectedPickupPointId) ?? null;
  const canFitSelected =
    (selectedSchoolObj != null && typeof selectedSchoolObj.latitude === 'number') ||
    (selectedPickupObj != null && typeof selectedPickupObj.latitude === 'number');

  // ─── Handlers ────────────────────────────────────────────────────
  const handleSaveSchool = async (values: any) => {
    try {
      const response = editingSchool
        ? await updateSchool({ id: editingSchool.id, body: values }).unwrap()
        : await createSchool(values).unwrap();
      toast.success(response.message || 'School saved');
      setSchoolDialogOpen(false);
      setEditingSchool(null);
    } catch (error: any) { toast.error(error?.data?.message || 'Failed to save school'); }
  };

  const handleSavePickupPoint = async (values: any) => {
    try {
      const response = editingPickup
        ? await updatePickupPoint({ id: editingPickup.id, body: values }).unwrap()
        : await createPickupPoint(values).unwrap();
      toast.success(response.message || 'Pickup point saved');
      setPickupDialogOpen(false);
      setEditingPickup(null);
    } catch (error: any) { toast.error(error?.data?.message || 'Failed to save pickup point'); }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    try {
      if (deleteTarget.type === 'school') {
        const response = await deleteSchool(deleteTarget.entity.id).unwrap();
        toast.success(response.message || 'School deleted');
      } else {
        const response = await deletePickupPoint(deleteTarget.entity.id).unwrap();
        toast.success(response.message || 'Pickup point deleted');
      }
      setDeleteTarget(null);
    } catch (error: any) { toast.error(error?.data?.message || 'Delete failed'); }
  };

  const handleSaveSchedule = async (values: any) => {
    if (!selectedSchoolId) return;
    try {
      const response = editingSchedule
        ? await updateSchedule({ id: editingSchedule.id, body: values }).unwrap()
        : await createSchedule({ schoolId: selectedSchoolId, body: values }).unwrap();
      toast.success(response.message || 'Schedule saved');
      setScheduleDialogOpen(false);
      setEditingSchedule(null);
    } catch (error: any) { toast.error(error?.data?.message || 'Failed to save schedule'); }
  };

  const handleDeleteSchedule = async (id: number) => {
    try { const r = await deleteSchedule(id).unwrap(); toast.success(r.message || 'Schedule deleted'); }
    catch (error: any) { toast.error(error?.data?.message || 'Failed to delete schedule'); }
  };

  const handleLinkPickupPoint = async (values: any) => {
    if (!selectedSchoolId) return;
    try {
      const r = await linkPickupPoint({ schoolId: selectedSchoolId, body: values }).unwrap();
      toast.success(r.message || 'Pickup point linked');
      setLinkDialogOpen(false);
    } catch (error: any) { toast.error(error?.data?.message || 'Failed to link pickup point'); }
  };

  const handleUnlinkPickupPoint = async (pickupPointId: number) => {
    if (!selectedSchoolId) return;
    try {
      const r = await unlinkPickupPoint({ schoolId: selectedSchoolId, pickupPointId }).unwrap();
      toast.success(r.message || 'Pickup point unlinked');
    } catch (error: any) { toast.error(error?.data?.message || 'Failed to unlink pickup point'); }
  };

  const handleSaveWindow = async (values: any) => {
    if (!expandedSppId) return;
    const body = { ...values, schoolPickupPointId: expandedSppId };
    try {
      const r = editingWindow
        ? await updateWindow({ windowId: editingWindow.id, body }).unwrap()
        : await createWindow(body).unwrap();
      toast.success(r.message || 'Window saved');
      setWindowDialogOpen(false);
      setEditingWindow(null);
    } catch (error: any) { toast.error(error?.data?.message || 'Failed to save window'); }
  };

  const handleDeleteWindow = async (windowId: number) => {
    try { const r = await deleteWindow(windowId).unwrap(); toast.success(r.message || 'Window deleted'); }
    catch (error: any) { toast.error(error?.data?.message || 'Failed to delete window'); }
  };


  // ─── Network list filtering ──────────────────────────────────────
  const filteredSchools = React.useMemo(() => {
    if (!networkFilter) return allSchools;
    const q = networkFilter.toLowerCase();
    return allSchools.filter((s) => s.name.toLowerCase().includes(q) || (s.code ?? '').toLowerCase().includes(q));
  }, [allSchools, networkFilter]);

  const filteredPickups = React.useMemo(() => {
    if (!networkFilter) return pickupPoints;
    const q = networkFilter.toLowerCase();
    return pickupPoints.filter((p) => p.name.toLowerCase().includes(q) || (p.address ?? '').toLowerCase().includes(q));
  }, [pickupPoints, networkFilter]);

  // ─── View mode toggle ───────────────────────────────────────────
  const viewModeToggle = (
    <div className='inline-flex items-center rounded-full border border-slate-200 bg-slate-100 p-0.5'>
      <button
        type='button'
        className={cn(
          'flex items-center gap-1.5 rounded-full px-3 py-1.5 text-xs font-medium transition-colors',
          viewMode === 'directory' ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-500 hover:text-slate-700'
        )}
        onClick={() => setViewMode('directory')}
      >
        <Table2 className='h-3.5 w-3.5' /> Directory
      </button>
      <button
        type='button'
        className={cn(
          'flex items-center gap-1.5 rounded-full px-3 py-1.5 text-xs font-medium transition-colors',
          viewMode === 'network' ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-500 hover:text-slate-700'
        )}
        onClick={() => setViewMode('network')}
      >
        <Map className='h-3.5 w-3.5' /> Network Map
      </button>
    </div>
  );

  const DIRECTION_LABELS: Record<string, string> = {
    PICKUP_TO_SCHOOL: '🏫 Pickup → School',
    DROPOFF_FROM_SCHOOL: '🏠 School → Drop-off',
  };

  // ═══════════════════════════════════════════════════════════════════
  // NETWORK MAP VIEW
  // ═══════════════════════════════════════════════════════════════════
  if (viewMode === 'network') {
    return (
      <>
        <SchoolBusPageShell
          compact
          title='School Network Workspace'
          description='Inspect and manage schools, pickup points, and depot locations on the map.'
          breadcrumb={
            <SchoolBusBreadcrumb items={[
              { label: 'School Bus Ops', href: '/school-bus/dispatch' },
              { label: 'Schools', current: true },
            ]} />
          }
          actions={
            <div className='flex items-center gap-3'>
              {viewModeToggle}
              <Button variant='outline' className='rounded-full' onClick={() => { setEditingPickup(null); setPickupDialogOpen(true); }}>
                <Plus className='h-4 w-4' /> Add pickup
              </Button>
              <Button className='rounded-full' onClick={() => { setEditingSchool(null); setSchoolDialogOpen(true); }}>
                <Plus className='h-4 w-4' /> Add school
              </Button>
            </div>
          }
        >
          <div className='flex flex-col gap-6'>
          {/* Stats row */}
          <div className='grid gap-3 grid-cols-2 lg:grid-cols-4'>
            <SchoolBusMetricCard label='Schools' value={allSchools.length} icon={GraduationCap} tone='school' variant='compact' />
            <SchoolBusMetricCard label='Pickup points' value={pickupPoints.length} icon={MapPin} tone='pickup' variant='compact' />
            <SchoolBusMetricCard label='Linked pickups' value={allLinks.length} icon={Link2} tone='linked' variant='compact' />
            <SchoolBusMetricCard
              label='Missing coords'
              value={missingCoordsCount}
              icon={AlertTriangle}
              tone={missingCoordsCount > 0 ? 'warning' : 'success'}
              variant='compact'
            />
          </div>

          {/* 3-column workspace */}
          <div
            className='flex overflow-hidden rounded-[24px] border border-slate-200 bg-white shadow-[0_4px_20px_rgba(15,23,42,0.08)]'
            style={{ height: 'calc(100vh - 225px)', minHeight: '520px' }}
          >
            {/* ── Left panel: list ──────────────────────────────── */}
            <div className='flex w-[300px] shrink-0 flex-col overflow-hidden border-r border-slate-200 bg-white'>
              <div className='shrink-0 border-b border-slate-100 bg-gradient-to-r from-slate-50 to-white px-3.5 py-3'>
                <p className='text-[10px] font-bold uppercase tracking-widest text-slate-500'>Network Directory</p>
              </div>
              {/* Search */}
              <div className='shrink-0 border-b border-slate-100 px-3.5 py-2.5'>
                <div className='relative'>
                  <Search className='absolute left-2.5 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-slate-400' />
                  <input
                    type='text'
                    placeholder='Filter schools or pickup points...'
                    value={networkFilter}
                    onChange={(e) => setNetworkFilter(e.target.value)}
                    className='w-full rounded-lg border border-slate-200 bg-slate-50 py-1.5 pl-8 pr-3 text-xs outline-none focus:border-slate-300 focus:ring-1 focus:ring-slate-200'
                  />
                </div>
              </div>
              {/* Scrollable list */}
              <div className='flex-1 overflow-y-auto divide-y divide-slate-100/50'>
                {/* Schools group */}
                <div>
                  <div className='flex items-center justify-between px-3.5 pt-4 pb-2'>
                    <span className='text-[10px] font-bold uppercase tracking-widest text-slate-400'>Schools</span>
                    <span className='rounded-full bg-slate-100 px-1.5 py-0.5 text-[9px] font-bold text-slate-500'>
                      {filteredSchools.length}
                    </span>
                  </div>
                  <div className='pb-2'>
                    {filteredSchools.length === 0 ? (
                      <p className='px-3.5 py-3 text-xs text-slate-400 italic'>No schools found</p>
                    ) : (
                      filteredSchools.map((s) => {
                        const isSelected = selectedSchoolId === s.id && !selectedPickupPointId;
                        return (
                          <button
                            key={s.id}
                            type='button'
                            onClick={() => {
                              setSelectedSchoolId(s.id);
                              setSelectedPickupPointId(null);
                              // Fit map to selected school
                              setFitSelectedKey((k) => k + 1);
                            }}
                            className={cn(
                              'flex w-full flex-col gap-0.5 border-b border-slate-100/40 px-3.5 py-2.5 text-left transition-colors border-l-4',
                              isSelected
                                ? 'bg-red-50/20 font-semibold border-l-[#C81E3A] text-[#A61B31]'
                                : 'text-slate-700 border-l-transparent hover:bg-slate-50/50'
                            )}
                          >
                            <div className='flex items-center justify-between w-full gap-2'>
                              <span className='truncate text-xs font-semibold text-slate-900'>{s.name}</span>
                              {s.code && (
                                <span className='shrink-0 rounded bg-red-50 px-1 text-[9px] font-bold text-[#C81E3A] ring-1 ring-inset ring-red-600/10'>
                                  {s.code}
                                </span>
                              )}
                            </div>
                            <span className='truncate text-[10px] text-slate-400 mt-0.5'>{s.address || 'No address'}</span>
                          </button>
                        );
                      })
                    )}
                  </div>
                </div>

                {/* Pickup points group */}
                <div>
                  <div className='flex items-center justify-between px-3.5 pt-4 pb-2'>
                    <span className='text-[10px] font-bold uppercase tracking-widest text-slate-400'>Pickup Points</span>
                    <span className='rounded-full bg-slate-100 px-1.5 py-0.5 text-[9px] font-bold text-slate-500'>
                      {filteredPickups.length}
                    </span>
                  </div>
                  <div className='pb-2'>
                    {filteredPickups.length === 0 ? (
                      <p className='px-3.5 py-3 text-xs text-slate-400 italic'>No pickup points found</p>
                    ) : (
                      filteredPickups.map((p) => {
                        const isSelected = selectedPickupPointId === p.id;
                        const isPickup = p.usageType === 'PICKUP' || p.usageType === 'PICKUP_ONLY';
                        const isDropoff = p.usageType === 'DROPOFF' || p.usageType === 'DROPOFF_ONLY';
                        const leftBorderClass = isPickup
                          ? 'border-l-blue-600 text-blue-700'
                          : isDropoff
                            ? 'border-l-emerald-600 text-emerald-700'
                            : 'border-l-indigo-600 text-indigo-700';
                        const activeBg = isPickup
                          ? 'bg-blue-50/20'
                          : isDropoff
                            ? 'bg-emerald-50/20'
                            : 'bg-indigo-50/20';

                        return (
                          <button
                            key={p.id}
                            type='button'
                            onClick={() => {
                              setSelectedPickupPointId(p.id);
                              // Select school of this pickup if it exists in the linked list
                              const schoolLink = allLinks.find((l) => l.pickupPointId === p.id);
                              if (schoolLink) {
                                setSelectedSchoolId(schoolLink.schoolId);
                              }
                              // Fit map to selected pickup
                              setFitSelectedKey((k) => k + 1);
                            }}
                            className={cn(
                              'flex w-full flex-col gap-0.5 border-b border-slate-100/40 px-3.5 py-2.5 text-left transition-colors border-l-4',
                              isSelected
                                ? cn('font-semibold', activeBg, leftBorderClass)
                                : 'text-slate-700 border-l-transparent hover:bg-slate-50/50'
                            )}
                          >
                            <div className='flex items-center justify-between w-full gap-2'>
                              <span className='truncate text-xs font-semibold text-slate-900'>{p.name}</span>
                              {p.zoneCode && (
                                <span className='shrink-0 rounded bg-blue-50 px-1 text-[9px] font-bold text-blue-700 ring-1 ring-inset ring-blue-700/10'>
                                  {p.zoneCode}
                                </span>
                              )}
                            </div>
                            <span className='truncate text-[10px] text-slate-400 mt-0.5'>{p.address || 'No address'}</span>
                          </button>
                        );
                      })
                    )}
                  </div>
                </div>
              </div>
            </div>

            {/* ── Right side: Map + Detail Panel ────────────────── */}
            <SchoolBusMapWorkspace
              flat
              className='flex-1 min-h-0'
              mapHeightClassName='h-full'
              panelClassName='w-[340px] p-0 flex flex-col h-full min-h-0 bg-white'
              map={
                canFitAll ? (
                  <OperationsMap
                    schools={allSchools}
                    depots={depots}
                    pickupPoints={pickupPoints}
                    selectedSchoolId={selectedSchoolId}
                    selectedPickupPointId={selectedPickupPointId}
                    selectedDepotId={null}
                    onSchoolSelect={(id) => { setSelectedSchoolId(id); setSelectedPickupPointId(null); }}
                    onPickupPointSelect={(id) => { setSelectedPickupPointId(id); }}
                    onDepotSelect={() => {}}
                    fitAllKey={fitAllKey}
                    fitSelectedKey={fitSelectedKey}
                    className='h-full w-full'
                  />
                ) : (
                  <div className='flex h-full flex-col items-center justify-center gap-4 bg-[#f8fafc] px-6'>
                    <div className='flex h-16 w-16 items-center justify-center rounded-2xl border-2 border-dashed border-slate-200 bg-white'>
                      <MapPin className='h-8 w-8 text-slate-300' />
                    </div>
                    <div className='text-center'>
                      <p className='text-sm font-semibold text-slate-600'>No map data yet</p>
                      <p className='mt-1 max-w-[240px] text-xs leading-5 text-slate-400'>
                        Add coordinates to schools, depots, or pickup points to view them on the map.
                      </p>
                    </div>
                  </div>
                )
              }
              legend={<SchoolBusMapLegend showRouteLines={false} />}
              onFitAll={handleFitAll}
              onFitRoute={handleFitSelected}
              canFitAll={canFitAll}
              canFitRoute={canFitSelected}
              fitRouteLabel='Fit Selected'
              panel={
                <NetworkDetailPanel
                  selectedSchool={selectedSchoolObj}
                  selectedPickup={selectedPickupObj}
                  linkedPickupCount={linkedPickupPoints.length}
                  scheduleCount={schedules.length}
                  onEditSchool={(s) => { setEditingSchool(s); setSchoolDialogOpen(true); }}
                  onEditPickup={(p) => { setEditingPickup(p); setPickupDialogOpen(true); }}
                  onLinkPickup={() => setLinkDialogOpen(true)}
                  onManageSchedules={() => setViewMode('directory')}
                />
              }
            />
          </div>
          </div>
        </SchoolBusPageShell>

        {renderDialogs()}
      </>
    );
  }

  // ═══════════════════════════════════════════════════════════════════
  // DIRECTORY VIEW
  // ═══════════════════════════════════════════════════════════════════

  const schoolScopeSelector = (
    <div className='flex items-center gap-3 rounded-xl border border-slate-200 bg-white px-4 py-1.5 shadow-sm'>
      <label className='text-xs font-semibold text-slate-500 whitespace-nowrap uppercase tracking-wider'>School:</label>
      <SchoolBusSelect
        value={selectedSchoolId ?? ''}
        onChange={(val) => {
          setSelectedSchoolId(val ? Number(val) : null);
          setSelectedPickupPointId(null);
        }}
        options={allSchools.map((s) => ({ label: s.name, value: s.id }))}
        placeholder='Select school...'
        searchPlaceholder='Search school by name...'
        emptyText='No schools found'
        clearable
        searchable
        fullWidth
        className='flex-1 min-w-[200px]'
      />
    </div>
  );

  // Columns definition for schools
  const schoolColumns: SchoolBusTableColumn<SchoolBusSchool>[] = [
    {
      key: 'school',
      header: 'School',
      className: 'pl-6',
      headerClassName: 'pl-6',
      render: (school) => {
        const isSelected = selectedSchoolId === school.id;
        const hasCoords = typeof school.latitude === 'number' && typeof school.longitude === 'number';
        return (
          <div className='flex items-center gap-3'>
            <div className='flex h-8 w-8 shrink-0 items-center justify-center rounded-xl bg-red-50 text-[#C81E3A] border border-red-100/50'>
              <GraduationCap className='h-4.5 w-4.5' />
            </div>
            <div className='min-w-0'>
              <button
                type='button'
                className='text-left outline-none'
                onClick={() => {
                  setSelectedSchoolId(school.id);
                  setSelectedPickupPointId(null);
                }}
              >
                <div className='flex items-center gap-1.5'>
                  <p className={cn(
                    'font-bold text-sm transition-colors',
                    isSelected ? 'text-[#C81E3A]' : 'text-slate-900 hover:text-[#C81E3A]'
                  )}>{school.name}</p>
                  {!hasCoords && (
                    <Tooltip>
                      <TooltipTrigger asChild>
                        <AlertTriangle className='h-3.5 w-3.5 text-amber-500 cursor-help shrink-0' />
                      </TooltipTrigger>
                      <TooltipContent className='bg-slate-900 text-white p-2 rounded-lg text-xs max-w-[200px]'>
                        School profile is missing geocoding coordinates.
                      </TooltipContent>
                    </Tooltip>
                  )}
                </div>
                <p className='text-[11px] text-slate-400 mt-0.5 max-w-xs truncate' title={school.address || undefined}>
                  {school.address || 'No address'}
                </p>
              </button>
            </div>
          </div>
        );
      }
    },
    {
      key: 'network',
      header: 'Network',
      render: (school) => {
        const linkedCount = school.pickupPointCount ?? 0;
        const missingLinked = school.anyLinkedPointMissingCoordinates;
        return (
          <div className='flex items-center gap-2'>
            <span className={cn(
              'inline-flex items-center rounded-md px-2 py-0.5 text-xs font-semibold ring-1 ring-inset',
              missingLinked
                ? 'bg-amber-50 text-amber-800 ring-amber-600/15'
                : 'bg-slate-100 text-slate-700 ring-slate-600/10'
            )}>
              {linkedCount} linked pickups
              {missingLinked && (
                <Tooltip>
                  <TooltipTrigger asChild>
                    <AlertTriangle className='h-3 w-3 text-amber-600 ml-1 cursor-help' />
                  </TooltipTrigger>
                  <TooltipContent className='bg-slate-900 text-white p-2 rounded-lg text-xs max-w-[200px]'>
                    Warning: Some linked pickup points are missing coordinates.
                  </TooltipContent>
                </Tooltip>
              )}
            </span>
          </div>
        );
      }
    },
    {
      key: 'schedules',
      header: 'Schedules',
      render: (school) => {
        const schedules = school.schedules || [];
        if (schedules.length === 0) return <span className='text-slate-400 font-medium text-xs'>—</span>;

        const limit = 2;
        const visible = schedules.slice(0, limit);
        const extra = schedules.length - limit;

        return (
          <TooltipProvider>
            <div className='flex flex-col gap-1 items-start py-1'>
              {visible.map((sch) => (
                <Tooltip key={sch.id}>
                  <TooltipTrigger asChild>
                    <span className={cn(
                      'inline-flex items-center rounded px-1.5 py-0.5 text-[10px] font-bold border font-mono cursor-help',
                      sch.shift === 'MORNING'
                        ? 'bg-blue-50 text-blue-700 border-blue-100'
                        : 'bg-orange-50 text-orange-700 border-orange-100'
                    )}>
                      {sch.code}
                    </span>
                  </TooltipTrigger>
                  <TooltipContent className='p-3 max-w-[260px] bg-slate-900 text-white rounded-xl shadow-lg border border-slate-800 space-y-1 text-xs'>
                    <div className='flex items-center justify-between gap-2 border-b border-slate-800 pb-1.5 mb-1.5'>
                      <p className='font-bold'>{sch.name}</p>
                      <span className={cn(
                        'text-[9px] uppercase tracking-wide px-1.5 py-0.5 rounded border font-bold',
                        sch.isActive
                          ? 'bg-emerald-950 text-emerald-400 border-emerald-800'
                          : 'bg-slate-800 text-slate-400 border-slate-700'
                      )}>
                        {sch.isActive ? 'Active' : 'Inactive'}
                      </span>
                    </div>
                    <p className='text-slate-400 font-medium'>Shift: <span className='text-slate-200 font-semibold'>{sch.shift}</span></p>
                    <p className='text-slate-400 font-medium'>Days: <span className='text-slate-200 font-semibold'>{sch.days?.join(', ') || 'Mon-Fri'}</span></p>
                    <p className='text-slate-400 font-medium'>Window: <span className='text-slate-200 font-semibold'>{sch.arrivalDeadline || '—'} / {sch.departureTime || '—'}</span></p>
                    {sch.isDefault && (
                      <span className='inline-block bg-red-950 text-red-400 px-1.5 py-0.5 rounded text-[9px] font-bold border border-red-800 mt-1'>
                        Default Schedule
                      </span>
                    )}
                  </TooltipContent>
                </Tooltip>
              ))}
              {extra > 0 && (
                <span className='text-[10px] font-semibold text-slate-400 pl-1'>
                  +{extra} more
                </span>
              )}
            </div>
          </TooltipProvider>
        );
      }
    },
    {
      key: 'pickupPoints',
      header: 'Pickup points',
      render: (school) => {
        const points = school.pickupPoints || [];
        if (points.length === 0) return <span className='text-slate-400 font-medium text-xs'>—</span>;

        const limit = 2;
        const visible = points.slice(0, limit);
        const extra = points.length - limit;

        return (
          <TooltipProvider>
            <div className='flex flex-col gap-1 items-start py-1'>
              {visible.map((pt) => {
                const hasCoords = !!pt.hasCoordinates;
                const isPickup = pt.usageType === 'PICKUP' || pt.usageType === 'PICKUP_ONLY';
                const isDropoff = pt.usageType === 'DROPOFF' || pt.usageType === 'DROPOFF_ONLY';

                return (
                  <Tooltip key={pt.code}>
                    <TooltipTrigger asChild>
                      <span className={cn(
                        'inline-flex items-center gap-1 rounded px-1.5 py-0.5 text-[10px] font-bold border font-mono cursor-help',
                        !hasCoords
                          ? 'bg-amber-50 text-amber-800 border-amber-200'
                          : isPickup
                          ? 'bg-blue-50 text-blue-700 border-blue-150'
                          : isDropoff
                          ? 'bg-emerald-50 text-emerald-700 border-emerald-150'
                          : 'bg-indigo-50 text-indigo-700 border-indigo-150'
                      )}>
                        {!hasCoords && <AlertTriangle className='h-2.5 w-2.5 text-amber-600' />}
                        {pt.code}
                      </span>
                    </TooltipTrigger>
                    <TooltipContent className='p-3 max-w-[260px] bg-slate-900 text-white rounded-xl shadow-lg border border-slate-800 space-y-1 text-xs'>
                      <div className='flex items-center justify-between gap-2 border-b border-slate-800 pb-1.5 mb-1.5'>
                        <p className='font-bold'>{pt.name}</p>
                        {pt.isDefault && (
                          <span className='bg-indigo-950 text-indigo-400 px-1 py-0.5 rounded text-[8px] font-bold border border-indigo-800 font-sans'>
                            Default Link
                          </span>
                        )}
                      </div>
                      <p className='text-slate-400 font-medium'>Usage: <span className='text-slate-200 font-semibold'>{pt.usageType}</span></p>
                      <p className='text-slate-400 font-medium truncate'>Address: <span className='text-slate-200 font-semibold'>{pt.address || 'No address'}</span></p>
                      <p className='text-slate-400 font-medium'>Coordinates: {hasCoords ? (
                        <span className='text-emerald-400 font-semibold'>Configured</span>
                      ) : (
                        <span className='text-amber-500 font-semibold flex items-center gap-0.5 inline-flex'><AlertTriangle className='h-3 w-3 shrink-0' /> Missing</span>
                      )}</p>
                    </TooltipContent>
                  </Tooltip>
                );
              })}
              {extra > 0 && (
                <span className='text-[10px] font-semibold text-slate-400 pl-1'>
                  +{extra} more
                </span>
              )}
            </div>
          </TooltipProvider>
        );
      }
    },
    {
      key: 'code',
      header: 'Code',
      render: (school) => (
        school.code ? (
          <span className='inline-flex items-center rounded-md bg-red-50 px-2 py-0.5 text-xs font-semibold text-[#C81E3A] ring-1 ring-inset ring-red-600/10'>
            {school.code}
          </span>
        ) : (
          <span className='text-slate-400 font-medium'>—</span>
        )
      )
    },
    {
      key: 'contact',
      header: 'Contact',
      render: (school) => (
        <span className='text-xs font-medium text-slate-600'>{school.contactPhone || '—'}</span>
      )
    },
    {
      key: 'status',
      header: 'Status',
      render: (school) => <SchoolBusStatusBadge status={school.isActive ? 'ACTIVE' : 'INACTIVE'} />
    },
    {
      key: 'actions',
      header: 'Actions',
      className: 'pr-6 text-right',
      headerClassName: 'pr-6 text-right',
      render: (school) => (
        <div className='flex justify-end gap-1.5'>
          <Button
            size='icon'
            variant='outline'
            className='h-8 w-8 rounded-lg border-slate-200 text-slate-500 hover:bg-slate-50 hover:text-slate-700'
            onClick={() => {
              setSelectedDetailSchoolId(school.id);
            }}
          >
            <Eye className='h-3.5 w-3.5' />
          </Button>
          <Button
            size='icon'
            variant='outline'
            className='h-8 w-8 rounded-lg border-slate-200 text-slate-500 hover:bg-slate-50 hover:text-slate-700'
            onClick={() => {
              setEditingSchool(school);
              setSchoolDialogOpen(true);
            }}
          >
            <Pencil className='h-3.5 w-3.5' />
          </Button>
          <Button
            size='icon'
            variant='outline'
            className='h-8 w-8 rounded-lg border-slate-200 text-slate-500 hover:border-red-200 hover:bg-red-50 hover:text-red-600'
            onClick={() => setDeleteTarget({ type: 'school', entity: school })}
          >
            <Trash2 className='h-3.5 w-3.5' />
          </Button>
        </div>
      )
    }
  ];

  // Columns definition for pickup-points
  const pickupColumns: SchoolBusTableColumn<SchoolBusPickupPoint>[] = [
    {
      key: 'pickup',
      header: 'Pickup Point',
      className: 'pl-6',
      headerClassName: 'pl-6',
      render: (pp) => {
        const isDropoff = pp.usageType === 'DROPOFF' || pp.usageType === 'DROPOFF_ONLY';
        const isPickup = pp.usageType === 'PICKUP' || pp.usageType === 'PICKUP_ONLY';
        const cellIconWrapper = isDropoff
          ? 'bg-emerald-50 text-emerald-700 border-emerald-100/50'
          : isPickup
            ? 'bg-blue-50 text-blue-700 border-blue-100/50'
            : 'bg-indigo-50 text-indigo-700 border-indigo-100/50';
        const PickupIcon = isDropoff ? MapPinCheck : MapPin;
        const hasCoords = typeof pp.latitude === 'number' && typeof pp.longitude === 'number';

        return (
          <div className='flex items-center gap-3'>
            <div className={cn('flex h-8 w-8 shrink-0 items-center justify-center rounded-xl border', cellIconWrapper)}>
              <PickupIcon className='h-4.5 w-4.5' />
            </div>
            <div className='min-w-0'>
              <button
                type='button'
                className='text-left outline-none'
                onClick={() => {
                  setSelectedPickupPointId(pp.id);
                  setSelectedSchoolId(pp.schoolId ?? null);
                }}
              >
                <p className='font-bold text-slate-900 hover:text-[#C81E3A] text-sm transition-colors'>{pp.name}</p>
                <div className='flex flex-wrap items-center gap-2 mt-0.5'>
                  <span className='text-[11px] text-slate-400 truncate max-w-xs' title={pp.address || undefined}>
                    {pp.address || 'No address'}
                  </span>
                  {!hasCoords && (
                    <span className='inline-flex items-center gap-1 rounded-md bg-amber-50 px-1 py-0.2 text-[8px] font-bold text-amber-700 ring-1 ring-inset ring-amber-600/10'>
                      No coords
                    </span>
                  )}
                </div>
              </button>
            </div>
          </div>
        );
      }
    },
    {
      key: 'code',
      header: 'Code',
      render: (pp) => (
        pp.code ? (
          <span className='inline-flex items-center rounded-md bg-red-50 px-2 py-0.5 text-xs font-semibold text-[#C81E3A] ring-1 ring-inset ring-red-600/10'>
            {pp.code}
          </span>
        ) : (
          <span className='text-slate-400 font-medium'>—</span>
        )
      )
    },
    {
      key: 'zone',
      header: 'Zone',
      render: (pp) => (
        pp.zoneCode ? (
          <span className='inline-flex items-center rounded-md bg-blue-50 px-2 py-0.5 text-xs font-bold text-blue-700 ring-1 ring-inset ring-blue-700/10'>
            {pp.zoneCode}
          </span>
        ) : (
          <span className='text-slate-400 font-medium'>—</span>
        )
      )
    },
    {
      key: 'school',
      header: 'Linked School',
      render: (pp) => {
        const schoolName = allSchools.find((s) => s.id === pp.schoolId)?.name;
        return schoolName ? (
          <span className='text-xs font-semibold text-slate-700'>{schoolName}</span>
        ) : (
          <span className='text-xs text-slate-400 italic font-medium'>Unlinked</span>
        );
      }
    },
    {
      key: 'usage',
      header: 'Usage Type',
      render: (pp) => {
        if (pp.usageType === 'PICKUP' || pp.usageType === 'PICKUP_ONLY') {
          return (
            <span className='inline-flex items-center rounded-md bg-blue-50 px-2 py-0.5 text-[10px] font-bold text-blue-700 ring-1 ring-inset ring-blue-600/15'>
              Pickup
            </span>
          );
        } else if (pp.usageType === 'DROPOFF' || pp.usageType === 'DROPOFF_ONLY') {
          return (
            <span className='inline-flex items-center rounded-md bg-emerald-50 px-2 py-0.5 text-[10px] font-bold text-emerald-700 ring-1 ring-inset ring-emerald-600/15'>
              Drop-off
            </span>
          );
        } else {
          return (
            <span className='inline-flex items-center rounded-md bg-indigo-50 px-2 py-0.5 text-[10px] font-bold text-indigo-700 ring-1 ring-inset ring-indigo-600/15'>
              Pickup & Drop-off
            </span>
          );
        }
      }
    },
    {
      key: 'actions',
      header: 'Actions',
      className: 'pr-6 text-right',
      headerClassName: 'pr-6 text-right',
      render: (pp) => (
        <div className='flex justify-end gap-1.5'>
          <Button
            size='icon'
            variant='outline'
            className='h-8 w-8 rounded-lg border-slate-200 text-slate-500 hover:bg-slate-50 hover:text-slate-700'
            onClick={() => {
              setEditingPickup(pp);
              setPickupDialogOpen(true);
            }}
          >
            <Pencil className='h-3.5 w-3.5' />
          </Button>
          <Button
            size='icon'
            variant='outline'
            className='h-8 w-8 rounded-lg border-slate-200 text-slate-500 hover:border-red-200 hover:bg-red-50 hover:text-red-600'
            onClick={() => setDeleteTarget({ type: 'pickup', entity: pp })}
          >
            <Trash2 className='h-3.5 w-3.5' />
          </Button>
        </div>
      )
    }
  ];

  // Columns definition for schedules
  const scheduleColumns: SchoolBusTableColumn<SchoolBusSchedule>[] = [
    {
      key: 'name',
      header: 'Name',
      className: 'pl-6 font-bold text-slate-900 text-sm',
      headerClassName: 'pl-6',
      render: (schedule) => schedule.scheduleName
    },
    {
      key: 'code',
      header: 'Code',
      render: (schedule) => (
        schedule.scheduleCode ? (
          <span className='inline-flex items-center rounded-md bg-slate-100 px-2 py-0.5 text-xs font-semibold text-slate-700 ring-1 ring-inset ring-slate-600/10'>
            {schedule.scheduleCode}
          </span>
        ) : (
          <span className='text-slate-400 font-medium'>—</span>
        )
      )
    },
    {
      key: 'shift',
      header: 'Shift',
      className: 'text-xs font-medium text-slate-600',
      render: (schedule) => schedule.shiftType
    },
    {
      key: 'days',
      header: 'Days',
      render: (schedule) => renderDayChips(schedule.daysOfWeek || [])
    },
    {
      key: 'arrival',
      header: 'Arrival',
      className: 'text-xs font-semibold text-slate-700',
      render: (schedule) => schedule.arrivalDeadline || '—'
    },
    {
      key: 'departure',
      header: 'Departure',
      className: 'text-xs font-semibold text-slate-700',
      render: (schedule) => schedule.departureTime || '—'
    },
    {
      key: 'effective',
      header: 'Effective Window',
      className: 'text-xs text-slate-500',
      render: (schedule) => (
        `${formatDate(schedule.effectiveFrom)}${schedule.effectiveTo ? ` – ${formatDate(schedule.effectiveTo)}` : ' –'}`
      )
    },
    {
      key: 'actions',
      header: 'Actions',
      className: 'pr-6 text-right',
      headerClassName: 'pr-6 text-right',
      render: (schedule) => (
        <div className='flex justify-end gap-1.5'>
          <Button
            size='icon'
            variant='outline'
            className='h-8 w-8 rounded-lg border-slate-200 text-slate-500 hover:bg-slate-50 hover:text-slate-700'
            onClick={() => {
              setEditingSchedule(schedule);
              setScheduleDialogOpen(true);
            }}
          >
            <Pencil className='h-3.5 w-3.5' />
          </Button>
          <Button
            size='icon'
            variant='outline'
            className='h-8 w-8 rounded-lg border-slate-200 text-slate-500 hover:border-red-200 hover:bg-red-50 hover:text-red-600'
            onClick={() => handleDeleteSchedule(schedule.id)}
          >
            <Trash2 className='h-3.5 w-3.5' />
          </Button>
        </div>
      )
    }
  ];

  // Toolbars definition
  const schoolToolbar = (
    <div className='flex flex-wrap items-center justify-between gap-4 w-full'>
      <div className='flex flex-wrap items-center gap-3 flex-1 min-w-0'>
        <div className='relative w-full max-w-xs'>
          <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400' />
          <input
            type='text'
            placeholder='Search schools...'
            value={schoolSearchValue}
            onChange={(e) => {
              setSchoolSearchValue(e.target.value);
              schoolsPagination.setKeyword(e.target.value);
            }}
            className='w-full h-9 rounded-lg border border-slate-200 bg-white py-1.5 pl-9 pr-3 text-xs outline-none focus:border-slate-300 focus:ring-1 focus:ring-slate-200'
          />
        </div>
        <SchoolBusSelect
          value={schoolCoordsFilter}
          onChange={(val) => setSchoolCoordsFilter(val || 'ALL')}
          options={[
            { label: 'All coordinates', value: 'ALL' },
            { label: 'Missing coordinates', value: 'MISSING' },
          ]}
        />
        <SchoolBusSelect
          value={schoolStatusFilter}
          onChange={(val) => setSchoolStatusFilter(val || 'ALL')}
          options={[
            { label: 'All status', value: 'ALL' },
            { label: 'Active', value: 'ACTIVE' },
            { label: 'Inactive', value: 'INACTIVE' },
          ]}
        />
      </div>
      <Button
        variant='outline'
        className='rounded-full border-[#F6CDD5] text-[#A61B31] bg-[#FDECEF]/40 hover:bg-[#FDECEF] hover:text-[#99182D] hover:border-[#F6CDD5] h-9 px-4 text-xs font-semibold'
        onClick={() => { setEditingSchool(null); setSchoolDialogOpen(true); }}
      >
        <Plus className='h-4 w-4 mr-1.5' /> Add school
      </Button>
    </div>
  );

  const pickupToolbar = (
    <div className='flex flex-wrap items-center justify-between gap-4 w-full'>
      <div className='flex flex-wrap items-center gap-3 flex-1 min-w-0'>
        <div className='relative w-full max-w-xs'>
          <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400' />
          <input
            type='text'
            placeholder='Search pickup points...'
            value={pickupSearchValue}
            onChange={(e) => {
              setPickupSearchValue(e.target.value);
              pickupPagination.setKeyword(e.target.value);
            }}
            className='w-full h-9 rounded-lg border border-slate-200 bg-white py-1.5 pl-9 pr-3 text-xs outline-none focus:border-slate-300 focus:ring-1 focus:ring-slate-200'
          />
        </div>
        <SchoolBusSelect
          value={pickupCoordsFilter}
          onChange={(val) => setPickupCoordsFilter(val || 'ALL')}
          options={[
            { label: 'All coordinates', value: 'ALL' },
            { label: 'Missing coordinates', value: 'MISSING' },
          ]}
        />
        <SchoolBusSelect
          value={pickupUsageFilter}
          onChange={(val) => setPickupUsageFilter(val || 'ALL')}
          options={[
            { label: 'All usage types', value: 'ALL' },
            { label: 'Pickup only', value: 'PICKUP' },
            { label: 'Drop-off only', value: 'DROPOFF' },
            { label: 'Pickup & Drop-off', value: 'PICKUP_DROPOFF' },
          ]}
        />
      </div>
      <Button
        variant='outline'
        className='rounded-full border-[#F6CDD5] text-[#A61B31] bg-[#FDECEF]/40 hover:bg-[#FDECEF] hover:text-[#99182D] hover:border-[#F6CDD5] h-9 px-4 text-xs font-semibold'
        onClick={() => { setEditingPickup(null); setPickupDialogOpen(true); }}
      >
        <Plus className='h-4 w-4 mr-1.5' /> Add pickup point
      </Button>
    </div>
  );

  const scheduleToolbar = (
    <div className='flex flex-col gap-4 w-full sm:flex-row sm:items-center sm:justify-between'>
      <div className='flex flex-wrap items-center gap-3 flex-1 min-w-0'>
        {schoolScopeSelector}
        <div className='relative w-full max-w-xs'>
          <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400' />
          <input
            type='text'
            placeholder='Search schedules...'
            value={scheduleSearch}
            onChange={(e) => setScheduleSearch(e.target.value)}
            className='w-full h-9 rounded-lg border border-slate-200 bg-white py-1.5 pl-9 pr-3 text-xs outline-none focus:border-slate-300 focus:ring-1 focus:ring-slate-200'
          />
        </div>
      </div>
      {selectedSchoolId && (
        <Button
          variant='outline'
          className='rounded-full border-[#F6CDD5] text-[#A61B31] bg-[#FDECEF]/40 hover:bg-[#FDECEF] hover:text-[#99182D] hover:border-[#F6CDD5] h-9 px-4 text-xs font-semibold'
          onClick={() => { setEditingSchedule(null); setScheduleDialogOpen(true); }}
        >
          <Plus className='h-4 w-4 mr-1.5' /> Add schedule
        </Button>
      )}
    </div>
  );

  const linkedPickupToolbar = (
    <div className='flex flex-col gap-4 w-full sm:flex-row sm:items-center sm:justify-between'>
      <div className='flex flex-wrap items-center gap-3 flex-1 min-w-0'>
        {schoolScopeSelector}
        {selectedSchoolId && (
          <div className='relative w-full max-w-xs'>
            <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400' />
            <input
              type='text'
              placeholder='Search linked pickups...'
              value={linkedSearch}
              onChange={(e) => setLinkedSearch(e.target.value)}
              className='w-full h-9 rounded-lg border border-slate-200 bg-white py-1.5 pl-9 pr-3 text-xs outline-none focus:border-slate-300 focus:ring-1 focus:ring-slate-200'
            />
          </div>
        )}
      </div>
      {selectedSchoolId && (
        <Button
          variant='outline'
          className='rounded-full border-[#F6CDD5] text-[#A61B31] bg-[#FDECEF]/40 hover:bg-[#FDECEF] hover:text-[#99182D] hover:border-[#F6CDD5] h-9 px-4 text-xs font-semibold'
          onClick={() => setLinkDialogOpen(true)}
        >
          <Link2 className='h-4 w-4 mr-1.5' /> Link pickup point
        </Button>
      )}
    </div>
  );

  const selectedSchoolName = schools.find((s) => s.id === selectedSchoolId)?.name;

  const renderDayChips = (days: string | string[]) => {
    const daysArr = typeof days === 'string'
      ? days.split(',').map((d) => d.trim().toUpperCase())
      : (days || []).map((d) => d.toUpperCase());
    const allWeekdays = [
      { key: 'MONDAY', label: 'M' },
      { key: 'TUESDAY', label: 'T' },
      { key: 'WEDNESDAY', label: 'W' },
      { key: 'THURSDAY', label: 'T' },
      { key: 'FRIDAY', label: 'F' },
      { key: 'SATURDAY', label: 'S' },
      { key: 'SUNDAY', label: 'S' },
    ];
    return (
      <div className='flex gap-1'>
        {allWeekdays.map((day) => {
          const active = daysArr.includes(day.key);
          return (
            <span
              key={day.key}
              className={cn(
                'inline-flex h-5 w-5 items-center justify-center rounded-full text-[9px] font-bold transition-colors',
                active
                  ? 'bg-red-50 text-[#C81E3A] border border-[#F6CDD5]'
                  : 'bg-slate-50 text-slate-300 border border-slate-100/50'
              )}
              title={day.key}
            >
              {day.label}
            </span>
          );
        })}
      </div>
    );
  };

  const linkedPickupTabChildren = (
    <div className='flex flex-col min-h-0 bg-white'>
      {!selectedSchoolId ? (
        <div className='px-6 py-8'>
          <SchoolBusEmptyState
            title='No school selected'
            description='Select a school from the dropdown above to manage its linked pickup points.'
            icon={Link2}
            className='min-h-[220px]'
          />
        </div>
      ) : filteredLinkedPickupsTable.length === 0 ? (
        <div className='px-6 py-8'>
          <SchoolBusEmptyState
            title={linkedPickupPoints.length === 0 ? 'No pickup points linked yet' : 'No matching linked pickup points'}
            description={linkedPickupPoints.length === 0 ? 'Link a pickup point to this school to make request planning actionable.' : 'Try adjusting your search criteria.'}
            icon={Link2}
            className='min-h-[220px]'
          />
        </div>
      ) : (
        <div className='px-6 py-5 space-y-3 bg-slate-50/20 border-b border-slate-100 rounded-b-2xl'>
          {filteredLinkedPickupsTable.map((link) => {
            const isExpanded = expandedSppId === link.id;
            const linkWindows = isExpanded ? windows : [];
            return (
              <div key={link.id} className='rounded-2xl border border-slate-200/80 bg-white overflow-hidden shadow-sm hover:shadow-md/5 transition-all duration-200'>
                {/* Header */}
                <div
                  className='flex items-center justify-between px-5 py-4 cursor-pointer select-none transition hover:bg-slate-50/50'
                  onClick={() => setExpandedSppId(isExpanded ? null : link.id)}
                >
                  <div className='flex items-center gap-3 min-w-0'>
                    <div className='flex h-8 w-8 shrink-0 items-center justify-center rounded-xl bg-slate-100 border border-slate-200/40 text-slate-500'>
                      {isExpanded ? <ChevronDown className='h-4 w-4' /> : <ChevronRight className='h-4 w-4' />}
                    </div>
                    <div className='min-w-0'>
                      <p className='text-sm font-bold text-slate-800'>{link.pickupPointName}</p>
                      <p className='text-[11px] text-slate-400 mt-0.5 truncate max-w-md' title={link.pickupPointAddress || undefined}>
                        {link.pickupPointAddress || 'No address'}
                      </p>
                    </div>
                  </div>
                  <div className='flex items-center gap-3 shrink-0'>
                    <span className={cn(
                      'rounded-md px-2 py-0.5 text-[10px] font-bold ring-1 ring-inset',
                      link.pickupPointUsageType === 'PICKUP' || link.pickupPointUsageType === 'PICKUP_ONLY'
                        ? 'bg-blue-50 text-blue-700 ring-blue-600/15'
                        : link.pickupPointUsageType === 'DROPOFF' || link.pickupPointUsageType === 'DROPOFF_ONLY'
                          ? 'bg-emerald-50 text-emerald-700 ring-emerald-600/15'
                          : 'bg-indigo-50 text-indigo-700 ring-indigo-600/15'
                    )}>
                      {formatUsageType(link.pickupPointUsageType)}
                    </span>
                    <Button
                      size='sm'
                      variant='outline'
                      className='h-8 px-3 rounded-lg border-red-200 text-[#C81E3A] hover:bg-red-50 hover:text-[#99182D] hover:border-red-300 gap-1.5'
                      onClick={(e) => { e.stopPropagation(); handleUnlinkPickupPoint(link.pickupPointId); }}
                    >
                      <Unlink className='h-3.5 w-3.5' /> Unlink
                    </Button>
                  </div>
                </div>

                {/* Expanded content */}
                {isExpanded && (
                  <div className='border-t border-slate-100 bg-slate-50/20 p-5'>
                    <div className='flex items-center justify-between mb-4'>
                      <h5 className='text-xs font-bold text-slate-600 uppercase tracking-wider'>Operational Windows</h5>
                      <Button
                        size='sm'
                        className='h-7 rounded-lg bg-[#C81E3A] text-white hover:bg-[#B31B34] text-xs font-semibold px-3 gap-1'
                        onClick={() => { setEditingWindow(null); setWindowDialogOpen(true); }}
                      >
                        <Plus className='h-3.5 w-3.5' /> Add window
                      </Button>
                    </div>

                    {linkWindows.length === 0 ? (
                      <div className='rounded-xl border border-dashed border-slate-200 bg-white p-6 text-center'>
                        <Clock className='mx-auto h-6 w-6 text-slate-300 mb-2' />
                        <p className='text-xs font-semibold text-slate-600'>No timing windows configured</p>
                        <p className='text-[10px] text-slate-400 mt-1 max-w-[280px] mx-auto'>
                          Define time boundaries for school runs to restrict trip dispatching.
                        </p>
                      </div>
                    ) : (
                      <div className='overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm'>
                        <Table>
                          <TableHeader className='bg-slate-50/50 hover:bg-slate-50/50 border-b border-slate-100'>
                            <TableRow className='h-8 border-b border-slate-100'>
                              <TableHead className='pl-4 text-[10px] font-bold text-slate-400 uppercase tracking-wider'>Schedule</TableHead>
                              <TableHead className='text-[10px] font-bold text-slate-400 uppercase tracking-wider'>Direction</TableHead>
                              <TableHead className='text-[10px] font-bold text-slate-400 uppercase tracking-wider'>Time window</TableHead>
                              <TableHead className='text-[10px] font-bold text-slate-400 uppercase tracking-wider'>Transit Estimate</TableHead>
                              <TableHead className='pr-4 text-[10px] font-bold text-slate-400 uppercase tracking-wider text-right'>Actions</TableHead>
                            </TableRow>
                          </TableHeader>
                          <TableBody>
                            {linkWindows.map((w) => (
                              <TableRow key={w.id} className='text-xs hover:bg-slate-50/30 border-b border-slate-100 last:border-b-0'>
                                <TableCell className='pl-4 py-2.5 font-medium text-slate-900'>{w.scheduleName}</TableCell>
                                <TableCell className='py-2.5'>{DIRECTION_LABELS[w.direction] || w.direction}</TableCell>
                                <TableCell className='py-2.5 font-semibold text-slate-700'>{w.windowStart} – {w.windowEnd}</TableCell>
                                <TableCell className='py-2.5 text-slate-500'>
                                  {w.estimatedDistanceToSchoolKm != null ? `${w.estimatedDistanceToSchoolKm} km` : '-'}
                                  {w.estimatedDurationToSchoolMin != null ? ` / ${w.estimatedDurationToSchoolMin} min` : ''}
                                </TableCell>
                                <TableCell className='pr-4 py-2.5 text-right'>
                                  <div className='flex items-center justify-end gap-1'>
                                    <Button
                                      size='icon'
                                      variant='ghost'
                                      className='h-7 w-7 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-50'
                                      onClick={() => { setEditingWindow(w); setWindowDialogOpen(true); }}
                                    >
                                      <Pencil className='h-3 w-3' />
                                    </Button>
                                    <Button
                                      size='icon'
                                      variant='ghost'
                                      className='h-7 w-7 rounded-lg text-red-500 hover:text-red-600 hover:bg-red-50'
                                      onClick={() => handleDeleteWindow(w.id)}
                                    >
                                      <Trash2 className='h-3 w-3' />
                                    </Button>
                                  </div>
                                </TableCell>
                              </TableRow>
                            ))}
                          </TableBody>
                        </Table>
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
  );

  if (selectedDetailSchoolId != null) {
    return (
      <SchoolBusSchoolDetailPage
        schoolId={selectedDetailSchoolId}
        onClose={() => setSelectedDetailSchoolId(null)}
      />
    );
  }

  return (
    <>
      <SchoolBusPageShell
        title='Schools and pickup network'
        description='Manage school entities and the pickup-point network that powers request intake and routing.'
        breadcrumb={
          <SchoolBusBreadcrumb items={[
            { label: 'School Bus Ops', href: '/school-bus/dispatch' },
            { label: 'Schools', current: true },
          ]} />
        }
        actions={
          <div className='flex items-center gap-3'>
            {viewModeToggle}
            <Button variant='outline' className='rounded-full' onClick={() => { setEditingPickup(null); setPickupDialogOpen(true); }}>
              <Plus className='h-4 w-4' /> Add pickup point
            </Button>
            <Button className='rounded-full' onClick={() => { setEditingSchool(null); setSchoolDialogOpen(true); }}>
              <Plus className='h-4 w-4' /> Add school
            </Button>
          </div>
        }
      >
        <div className='flex flex-col gap-6'>
          {/* Stats */}
          <div className='grid gap-3 grid-cols-2 lg:grid-cols-4'>
            <SchoolBusMetricCard label='Schools' value={schools.length} icon={GraduationCap} tone='school' />
            <SchoolBusMetricCard label='Pickup points' value={pickupPoints.length} icon={MapPin} tone='pickup' />
            <SchoolBusMetricCard label='Linked pickups' value={allLinks.length} icon={Link2} tone='linked' />
            <SchoolBusMetricCard
              label='Missing coords'
              value={missingCoordsCount}
              icon={AlertTriangle}
              tone={missingCoordsCount > 0 ? 'warning' : 'success'}
            />
          </div>

          {/* Unified Table view */}
          <SchoolBusDataTable<any>
            tabs={[
              { key: 'schools', label: 'Schools', count: schools.length },
              { key: 'pickup-points', label: 'Pickup points', count: pickupPoints.length },
            ]}
            activeTab={activeTab}
            onTabChange={setActiveTab}
            toolbar={
              activeTab === 'schools'
                ? schoolToolbar
                : activeTab === 'pickup-points'
                  ? pickupToolbar
                  : activeTab === 'schedules'
                    ? scheduleToolbar
                    : linkedPickupToolbar
            }
            data={
              activeTab === 'schools'
                ? filteredSchoolsTable
                : activeTab === 'pickup-points'
                  ? filteredPickupsTable
                  : activeTab === 'schedules'
                    ? filteredSchedulesTable
                    : []
            }
            columns={
              activeTab === 'schools'
                ? schoolColumns
                : activeTab === 'pickup-points'
                  ? pickupColumns
                  : activeTab === 'schedules'
                    ? scheduleColumns
                    : []
            }
            isLoading={
              activeTab === 'schools'
                ? loadingSchools
                : activeTab === 'pickup-points'
                  ? loadingPickups
                  : activeTab === 'schedules'
                    ? loadingSchedules
                    : false
            }
            pagination={
              activeTab === 'schools' && data
                ? { page: data?.data, onPageChange: schoolsPagination.setPage }
                : activeTab === 'pickup-points' && pickupPointsData
                  ? { page: pickupPointsData?.data, onPageChange: pickupPagination.setPage }
                  : undefined
            }
          >
            {activeTab === 'linked-pickups' && linkedPickupTabChildren}
          </SchoolBusDataTable>
        </div>
      </SchoolBusPageShell>

      {renderDialogs()}
    </>
  );

  // ─── Shared dialogs renderer ──────────────────────────────────────
  function renderDialogs() {
    return (
      <>
        <SchoolFormDialog
          open={schoolDialogOpen}
          onOpenChange={(open) => { setSchoolDialogOpen(open); if (!open) setEditingSchool(null); }}
          initialData={editingSchool}
          isLoading={creatingSchool || updatingSchool}
          onSubmit={handleSaveSchool}
        />
        <PickupPointFormDialog
          open={pickupDialogOpen}
          onOpenChange={(open) => { setPickupDialogOpen(open); if (!open) setEditingPickup(null); }}
          initialData={editingPickup}
          schools={allSchools.length > 0 ? allSchools : schools}
          isLoading={creatingPickup || updatingPickup}
          onSubmit={handleSavePickupPoint}
        />
        <SchoolBusDeleteDialog
          open={Boolean(deleteTarget)}
          onOpenChange={(open) => { if (!open) setDeleteTarget(null); }}
          title={deleteTarget?.type === 'school' ? 'Delete school' : 'Delete pickup point'}
          description={deleteTarget?.type === 'school' ? 'This will soft-delete the school record.' : 'This will soft-delete the pickup point.'}
          isLoading={deletingSchool || deletingPickup}
          onConfirm={handleDelete}
        />
        <ScheduleFormDialog
          open={scheduleDialogOpen}
          onOpenChange={(open) => { setScheduleDialogOpen(open); if (!open) setEditingSchedule(null); }}
          initialData={editingSchedule}
          isLoading={creatingSchedule || updatingSchedule}
          onSubmit={handleSaveSchedule}
        />
        <LinkPickupPointDialog
          open={linkDialogOpen}
          onOpenChange={setLinkDialogOpen}
          pickupPoints={allPickupPointsForLink}
          isLoading={linkingPickup}
          onSubmit={handleLinkPickupPoint}
        />
        <WindowFormDialog
          open={windowDialogOpen}
          onOpenChange={(open) => { setWindowDialogOpen(open); if (!open) setEditingWindow(null); }}
          schedules={schedules}
          existingWindow={editingWindow}
          usageType={linkedPickupPoints.find((lp) => lp.id === expandedSppId)?.pickupPointUsageType}
          isLoading={creatingWindow || updatingWindow}
          onSubmit={handleSaveWindow}
        />
      </>
    );
  }
}
