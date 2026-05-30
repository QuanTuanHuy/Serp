'use client';

import * as React from 'react';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import {
  AlertTriangle,
  Building2,
  CalendarClock,
  ChevronDown,
  ChevronRight,
  Clock,
  Link2,
  Map,
  MapPinned,
  Pencil,
  Plus,
  Search,
  Table2,
  Trash2,
  Unlink,
} from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/shared/components/ui';
import { Combobox } from '@/shared/components/ui/combobox';
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
import { SchoolBusTableTabs } from '../components/SchoolBusTableTabs';
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

// ── Shared InfoRow ────────────────────────────────────────────────────────────

function InfoRow({ label, value, warn = false }: { label: string; value: string; warn?: boolean }) {
  return (
    <div className='rounded-xl bg-slate-50/80 px-3 py-2 ring-1 ring-slate-100'>
      <p className='text-[10px] text-slate-400'>{label}</p>
      <p className={`mt-0.5 text-xs font-medium ${warn ? 'text-amber-600' : 'text-slate-700'}`}>
        {value}
      </p>
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
      <div className='flex h-full flex-col items-center justify-center gap-3 px-4 py-8 text-center'>
        <div className='flex h-12 w-12 items-center justify-center rounded-full bg-slate-100'>
          <MapPinned className='h-6 w-6 text-slate-400' />
        </div>
        <p className='text-sm font-medium text-slate-600'>Nothing selected</p>
        <p className='max-w-[200px] text-xs leading-5 text-slate-400'>
          Click a school or pickup point in the list or on the map to see details and actions.
        </p>
      </div>
    );
  }

  if (selectedSchool) {
    return (
      <div className='flex h-full flex-col gap-3 p-4'>
        {/* Header */}
        <div className='flex items-start gap-2.5'>
          <div className='mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-rose-50 ring-1 ring-rose-200'>
            <Building2 className='h-4 w-4 text-rose-600' />
          </div>
          <div className='min-w-0 flex-1'>
            <p className='text-[10px] font-bold uppercase tracking-widest text-rose-600'>School</p>
            <p className='mt-0.5 text-sm font-semibold leading-tight text-slate-900'>
              {selectedSchool.name}
            </p>
          </div>
        </div>

        {/* Details */}
        <div className='space-y-2 overflow-y-auto flex-1'>
          <InfoRow label='Code' value={selectedSchool.code ?? '—'} />
          <InfoRow label='Address' value={selectedSchool.address ?? '—'} />
          <InfoRow label='Contact' value={selectedSchool.contactPhone ?? '—'} />
          <InfoRow
            label='Coordinates'
            value={
              typeof selectedSchool.latitude === 'number' && typeof selectedSchool.longitude === 'number'
                ? `${selectedSchool.latitude.toFixed(5)}, ${selectedSchool.longitude.toFixed(5)}`
                : '⚠ Not set'
            }
            warn={typeof selectedSchool.latitude !== 'number'}
          />
          <InfoRow label='Status' value={selectedSchool.isActive ? 'Active' : 'Inactive'} />
          <InfoRow label='Linked pickups' value={String(linkedPickupCount)} />
          <InfoRow label='Schedules' value={String(scheduleCount)} />
        </div>

        {/* Actions */}
        <div className='shrink-0 flex flex-col gap-2 border-t border-slate-100 pt-3'>
          <Button size='sm' variant='outline' className='w-full justify-start' onClick={() => onEditSchool(selectedSchool)}>
            <Pencil className='mr-2 h-3.5 w-3.5' /> Edit school
          </Button>
          <Button size='sm' variant='outline' className='w-full justify-start' onClick={onLinkPickup}>
            <Link2 className='mr-2 h-3.5 w-3.5' /> Link pickup point
          </Button>
          <Button size='sm' variant='outline' className='w-full justify-start' onClick={onManageSchedules}>
            <CalendarClock className='mr-2 h-3.5 w-3.5' /> Manage schedules
          </Button>
        </div>
      </div>
    );
  }

  if (selectedPickup) {
    return (
      <div className='flex h-full flex-col gap-3 p-4'>
        {/* Header */}
        <div className='flex items-start gap-2.5'>
          <div className='mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-sky-50 ring-1 ring-sky-200'>
            <MapPinned className='h-4 w-4 text-sky-600' />
          </div>
          <div className='min-w-0 flex-1'>
            <p className='text-[10px] font-bold uppercase tracking-widest text-sky-600'>Pickup Point</p>
            <p className='mt-0.5 text-sm font-semibold leading-tight text-slate-900'>
              {selectedPickup.name}
            </p>
          </div>
        </div>

        {/* Details */}
        <div className='space-y-2 overflow-y-auto flex-1'>
          <InfoRow label='Usage type' value={selectedPickup.usageType ?? '—'} />
          <InfoRow label='Zone' value={selectedPickup.zoneCode ?? '—'} />
          <InfoRow label='Address' value={selectedPickup.address ?? '—'} />
          <InfoRow
            label='Coordinates'
            value={
              typeof selectedPickup.latitude === 'number' && typeof selectedPickup.longitude === 'number'
                ? `${selectedPickup.latitude.toFixed(5)}, ${selectedPickup.longitude.toFixed(5)}`
                : '⚠ Not set'
            }
            warn={typeof selectedPickup.latitude !== 'number'}
          />
        </div>

        {/* Actions */}
        <div className='shrink-0 flex flex-col gap-2 border-t border-slate-100 pt-3'>
          <Button size='sm' variant='outline' className='w-full justify-start' onClick={() => onEditPickup(selectedPickup)}>
            <Pencil className='mr-2 h-3.5 w-3.5' /> Edit pickup point
          </Button>
          <Button size='sm' variant='outline' className='w-full justify-start' onClick={onLinkPickup}>
            <Link2 className='mr-2 h-3.5 w-3.5' /> Link to school
          </Button>
        </div>
      </div>
    );
  }

  return null;
}

// ── Main Page ─────────────────────────────────────────────────────────────────

export function SchoolBusSchoolsPage() {
  const [viewMode, setViewMode] = React.useState<ViewMode>('network');
  const [networkFilter, setNetworkFilter] = React.useState('');

  const schoolsPagination = useSchoolBusPagination({ page: 0, size: 10, sortBy: 'name', sortDirection: 'ASC' });
  const pickupPagination = useSchoolBusPagination({ page: 0, size: 10, sortBy: 'name', sortDirection: 'ASC' });

  const { data, isLoading } = useGetSchoolsQuery(schoolsPagination.params);
  const { data: allSchoolsData } = useGetSchoolsQuery({ ...SCHOOL_BUS_OPTION_QUERY, sortBy: 'name' });
  const { data: pickupPointsData } = useGetPickupPointsQuery(pickupPagination.params);
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
  const { data: schedulesData } = useGetSchoolSchedulesQuery(
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

  // ─── School search for combobox ──────────────────────────────────
  const [schoolSearchKeyword, setSchoolSearchKeyword] = React.useState('');
  const [debouncedKeyword, setDebouncedKeyword] = React.useState('');
  React.useEffect(() => {
    const t = setTimeout(() => setDebouncedKeyword(schoolSearchKeyword), 300);
    return () => clearTimeout(t);
  }, [schoolSearchKeyword]);
  const { data: searchedSchoolsData, isFetching: searchingSchools } = useGetSchoolsQuery({
    ...SCHOOL_BUS_OPTION_QUERY, sortBy: 'name', keyword: debouncedKeyword || undefined,
  });
  const searchedSchools = getPageItems(searchedSchoolsData?.data);
  const schoolItems = React.useMemo(
    () => searchedSchools.map((s) => ({ value: s.id, label: s.name })),
    [searchedSchools]
  );

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
          {/* Stats row */}
          <div className='grid gap-3 grid-cols-2 lg:grid-cols-4'>
            <SchoolBusMetricCard label='Schools' value={allSchools.length} icon={Building2} tone='info' />
            <SchoolBusMetricCard label='Pickup points' value={pickupPoints.length} icon={MapPinned} tone='success' />
            <SchoolBusMetricCard label='Linked pickups' value={allLinks.length} icon={Link2} tone='default' />
            <SchoolBusMetricCard
              label='Missing coords'
              value={missingCoordsCount}
              icon={AlertTriangle}
              tone={missingCoordsCount > 0 ? 'warning' : 'success'}
              hint={missingCoordsCount > 0 ? 'Items not visible on map' : 'All items have coordinates'}
            />
          </div>

          {/* 3-column workspace */}
          <div
            className='flex overflow-hidden rounded-[24px] border border-slate-200 bg-white shadow-[0_4px_20px_rgba(15,23,42,0.08)]'
            style={{ height: 'calc(100vh - 260px)', minHeight: '520px' }}
          >
            {/* ── Left panel: list ──────────────────────────────── */}
            <div className='flex w-[280px] shrink-0 flex-col overflow-hidden border-r border-slate-200 bg-white'>
              <div className='shrink-0 border-b border-slate-100 bg-gradient-to-r from-rose-50 to-white px-3 py-2.5'>
                <p className='text-[10px] font-bold uppercase tracking-widest text-rose-600'>Network</p>
              </div>
              {/* Search */}
              <div className='shrink-0 border-b border-slate-100 px-3 py-2'>
                <div className='relative'>
                  <Search className='absolute left-2.5 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-slate-400' />
                  <input
                    type='text'
                    placeholder='Filter schools/pickups...'
                    value={networkFilter}
                    onChange={(e) => setNetworkFilter(e.target.value)}
                    className='w-full rounded-lg border border-slate-200 bg-slate-50 py-1.5 pl-8 pr-3 text-xs outline-none focus:border-rose-300 focus:ring-1 focus:ring-rose-200'
                  />
                </div>
              </div>
              {/* Scrollable list */}
              <div className='flex-1 overflow-y-auto'>
                {/* Schools */}
                <div className='border-b border-slate-100'>
                  <p className='px-3 pt-2.5 pb-1 text-[10px] font-bold uppercase tracking-widest text-slate-400'>
                    Schools ({filteredSchools.length})
                  </p>
                  {filteredSchools.map((s) => (
                    <button
                      key={s.id}
                      type='button'
                      className={cn(
                        'w-full px-3 py-2 text-left transition-colors hover:bg-rose-50/50',
                        selectedSchoolId === s.id && !selectedPickupPointId && 'bg-rose-50 border-l-2 border-rose-500'
                      )}
                      onClick={() => { setSelectedSchoolId(s.id); setSelectedPickupPointId(null); setFitSelectedKey((k) => k + 1); }}
                    >
                      <p className='text-xs font-medium text-slate-900 truncate'>{s.name}</p>
                      <p className='text-[10px] text-slate-400 truncate'>{s.address || 'No address'}</p>
                    </button>
                  ))}
                </div>
                {/* Pickup points */}
                <div>
                  <p className='px-3 pt-2.5 pb-1 text-[10px] font-bold uppercase tracking-widest text-slate-400'>
                    Pickup Points ({filteredPickups.length})
                  </p>
                  {filteredPickups.map((p) => (
                    <button
                      key={p.id}
                      type='button'
                      className={cn(
                        'w-full px-3 py-2 text-left transition-colors hover:bg-sky-50/50',
                        selectedPickupPointId === p.id && 'bg-sky-50 border-l-2 border-sky-500'
                      )}
                      onClick={() => { setSelectedPickupPointId(p.id); setSelectedSchoolId(p.schoolId ?? null); setFitSelectedKey((k) => k + 1); }}
                    >
                      <p className='text-xs font-medium text-slate-900 truncate'>{p.name}</p>
                      <p className='text-[10px] text-slate-400 truncate'>{p.address || 'No address'}</p>
                    </button>
                  ))}
                </div>
              </div>
            </div>

            {/* ── Right side: Map + Detail Panel ────────────────── */}
            <SchoolBusMapWorkspace
              className='flex-1 border-none rounded-none shadow-none'
              mapHeightClassName='h-full'
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
                      <MapPinned className='h-8 w-8 text-slate-300' />
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
        </SchoolBusPageShell>

        {renderDialogs()}
      </>
    );
  }

  // ═══════════════════════════════════════════════════════════════════
  // DIRECTORY VIEW
  // ═══════════════════════════════════════════════════════════════════

  const schoolScopeSelector = (
    <div className='flex items-center gap-3 rounded-xl border border-slate-200 bg-white px-4 py-3 shadow-sm'>
      <label className='text-sm font-medium text-slate-700 whitespace-nowrap'>School:</label>
      <Combobox
        value={selectedSchoolId ?? undefined}
        onChange={(val) => { setSelectedSchoolId(val != null ? Number(val) : null); setSelectedPickupPointId(null); }}
        items={schoolItems}
        placeholder='Search school by name...'
        emptyText='No schools found'
        loading={searchingSchools}
        clearable
        onSearch={setSchoolSearchKeyword}
        className='h-9 flex-1 min-w-0 rounded-lg'
      />
    </div>
  );

  const schoolsTabContent = (
    <div className='space-y-4'>
      <div>
        <h3 className='text-base font-semibold text-slate-950'>School directory</h3>
        <p className='mt-1 text-sm text-slate-500'>Create, update, and soft-delete school master data.</p>
      </div>
      {isLoading ? (
        <p className='text-sm text-muted-foreground'>Loading schools...</p>
      ) : schools.length === 0 ? (
        <SchoolBusEmptyState title='No schools registered yet' description='Add your first school to unlock routing and request setup.' icon={Building2} />
      ) : (
        <SchoolBusScrollableTable footer={<SchoolBusPaginationBar page={data?.data} onPageChange={schoolsPagination.setPage} />}>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>School</TableHead>
                <TableHead>Code</TableHead>
                <TableHead>Contact</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className='text-right'>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {schools.map((school) => (
                <TableRow key={school.id}>
                  <TableCell>
                    <button type='button' className='text-left' onClick={() => { setSelectedSchoolId(school.id); setSelectedPickupPointId(null); }}>
                      <p className='font-medium'>{school.name}</p>
                      <p className='text-xs text-muted-foreground'>{school.address || 'No address'}</p>
                    </button>
                  </TableCell>
                  <TableCell>{school.code || 'N/A'}</TableCell>
                  <TableCell>
                    <div className='text-sm'>
                      <p>{school.contactPhone || 'No phone'}</p>
                      <p className='text-xs text-muted-foreground'>{school.contactEmail || 'No email'}</p>
                    </div>
                  </TableCell>
                  <TableCell><SchoolBusStatusBadge status={school.isActive ? 'ACTIVE' : 'INACTIVE'} /></TableCell>
                  <TableCell className='text-right'>
                    <div className='flex justify-end gap-2'>
                      <Button size='icon' variant='outline' onClick={() => { setEditingSchool(school); setSchoolDialogOpen(true); }}><Pencil className='h-4 w-4' /></Button>
                      <Button size='icon' variant='outline' onClick={() => setDeleteTarget({ type: 'school', entity: school })}><Trash2 className='h-4 w-4' /></Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </SchoolBusScrollableTable>
      )}
    </div>
  );

  const pickupPointsTabContent = (
    <div className='space-y-4'>
      <div>
        <h3 className='text-base font-semibold text-slate-950'>Pickup point footprint</h3>
        <p className='mt-1 text-sm text-slate-500'>Boarding points grouped into the operational network.</p>
      </div>
      {pickupPoints.length === 0 ? (
        <SchoolBusEmptyState title='No pickup points available' description='Add pickup points to make request-to-route planning actionable.' icon={MapPinned} className='min-h-[220px]' />
      ) : (
        <SchoolBusScrollableTable footer={<SchoolBusPaginationBar page={pickupPointsData?.data} onPageChange={pickupPagination.setPage} />}>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Pickup point</TableHead>
                <TableHead>Zone</TableHead>
                <TableHead>Address</TableHead>
                <TableHead>Usage type</TableHead>
                <TableHead className='text-right'>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {pickupPoints.map((pp) => (
                <TableRow key={pp.id}>
                  <TableCell>
                    <button type='button' className='text-left font-medium' onClick={() => { setSelectedPickupPointId(pp.id); setSelectedSchoolId(pp.schoolId ?? null); }}>
                      {pp.name}
                    </button>
                  </TableCell>
                  <TableCell><span className='text-sm text-slate-600'>{pp.zoneCode || '-'}</span></TableCell>
                  <TableCell>{pp.address}</TableCell>
                  <TableCell><span className='text-xs font-mono text-rose-700'>{pp.usageType || 'N/A'}</span></TableCell>
                  <TableCell className='text-right'>
                    <div className='flex justify-end gap-2'>
                      <Button size='icon' variant='outline' onClick={() => { setEditingPickup(pp); setPickupDialogOpen(true); }}><Pencil className='h-4 w-4' /></Button>
                      <Button size='icon' variant='outline' onClick={() => setDeleteTarget({ type: 'pickup', entity: pp })}><Trash2 className='h-4 w-4' /></Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </SchoolBusScrollableTable>
      )}
    </div>
  );

  const selectedSchoolName = schools.find((s) => s.id === selectedSchoolId)?.name;

  const schedulesTabContent = (
    <div className='space-y-4'>
      {schoolScopeSelector}
      <div className='flex items-center justify-between'>
        <div>
          <h3 className='text-base font-semibold text-slate-950'>Schedules</h3>
          <p className='mt-1 text-sm text-slate-500'>
            {selectedSchoolName ? `Academic schedules for ${selectedSchoolName}` : 'Select a school above to view schedules'}
          </p>
        </div>
        {selectedSchoolId && (
          <Button variant='outline' className='rounded-full' onClick={() => { setEditingSchedule(null); setScheduleDialogOpen(true); }}>
            <Plus className='h-4 w-4' /> Add schedule
          </Button>
        )}
      </div>
      {!selectedSchoolId ? (
        <SchoolBusEmptyState title='No school selected' description='Use the dropdown above to pick a school.' icon={CalendarClock} className='min-h-[220px]' />
      ) : schedules.length === 0 ? (
        <SchoolBusEmptyState title='No schedules yet' description='Create a schedule to define shift times.' icon={CalendarClock} className='min-h-[220px]' />
      ) : (
        <SchoolBusScrollableTable>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Name</TableHead>
                <TableHead>Code</TableHead>
                <TableHead>Shift</TableHead>
                <TableHead>Day</TableHead>
                <TableHead>Arrival</TableHead>
                <TableHead>Departure</TableHead>
                <TableHead>Effective</TableHead>
                <TableHead className='text-right'>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {schedules.map((schedule) => (
                <TableRow key={schedule.id}>
                  <TableCell className='font-medium'>{schedule.scheduleName}</TableCell>
                  <TableCell><span className='text-xs font-mono text-rose-700'>{schedule.scheduleCode || '-'}</span></TableCell>
                  <TableCell>{schedule.shiftType}</TableCell>
                  <TableCell>{schedule.daysOfWeek?.length ? schedule.daysOfWeek.join(', ') : 'Mon–Fri'}</TableCell>
                  <TableCell>{schedule.arrivalDeadline || '-'}</TableCell>
                  <TableCell>{schedule.departureTime || '-'}</TableCell>
                  <TableCell>{formatDate(schedule.effectiveFrom)}{schedule.effectiveTo ? ` – ${formatDate(schedule.effectiveTo)}` : ' –'}</TableCell>
                  <TableCell className='text-right'>
                    <div className='flex justify-end gap-2'>
                      <Button size='icon' variant='outline' onClick={() => { setEditingSchedule(schedule); setScheduleDialogOpen(true); }}><Pencil className='h-4 w-4' /></Button>
                      <Button size='icon' variant='outline' onClick={() => handleDeleteSchedule(schedule.id)}><Trash2 className='h-4 w-4' /></Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </SchoolBusScrollableTable>
      )}
    </div>
  );

  const linkedPickupTabContent = (
    <div className='space-y-4'>
      {schoolScopeSelector}
      <div className='flex items-center justify-between'>
        <div>
          <h3 className='text-base font-semibold text-slate-950'>Linked pickup points</h3>
          <p className='mt-1 text-sm text-slate-500'>
            {selectedSchoolName ? `Pickup points for ${selectedSchoolName}. Click to manage windows.` : 'Select a school above'}
          </p>
        </div>
        {selectedSchoolId && (
          <Button variant='outline' className='rounded-full' onClick={() => setLinkDialogOpen(true)}>
            <Link2 className='h-4 w-4' /> Link pickup point
          </Button>
        )}
      </div>
      {!selectedSchoolId ? (
        <SchoolBusEmptyState title='No school selected' description='Use the dropdown above to pick a school.' icon={Link2} className='min-h-[220px]' />
      ) : linkedPickupPoints.length === 0 ? (
        <SchoolBusEmptyState title='No pickup points linked' description='Link a pickup point to this school.' icon={Link2} className='min-h-[220px]' />
      ) : (
        <div className='space-y-2'>
          {linkedPickupPoints.map((link) => {
            const isExpanded = expandedSppId === link.id;
            const linkWindows = isExpanded ? windows : [];
            return (
              <div key={link.id} className='rounded-2xl border border-slate-200 bg-white overflow-hidden shadow-sm'>
                <div className='flex items-center gap-3 px-4 py-3 cursor-pointer hover:bg-slate-50/80 transition-colors' onClick={() => setExpandedSppId(isExpanded ? null : link.id)}>
                  <span className='text-slate-400'>{isExpanded ? <ChevronDown className='h-4 w-4' /> : <ChevronRight className='h-4 w-4' />}</span>
                  <div className='flex-1 min-w-0'>
                    <p className='font-medium text-sm text-slate-900 truncate'>{link.pickupPointName}</p>
                    <p className='text-xs text-slate-500 truncate'>{link.pickupPointAddress || 'No address'}</p>
                  </div>
                  <span className='rounded-full bg-slate-100 px-2.5 py-0.5 text-xs font-medium text-slate-600'>{link.pickupPointUsageType || 'N/A'}</span>
                  <Button size='icon' variant='outline' className='h-8 w-8 rounded-lg' onClick={(e) => { e.stopPropagation(); handleUnlinkPickupPoint(link.pickupPointId); }}>
                    <Unlink className='h-3.5 w-3.5' />
                  </Button>
                </div>
                {isExpanded && (
                  <div className='border-t border-slate-100 bg-slate-50/50 px-4 py-3'>
                    <div className='flex items-center justify-between mb-3'>
                      <div className='flex items-center gap-2 text-sm font-medium text-slate-700'>
                        <Clock className='h-4 w-4 text-rose-500' /> Time windows
                      </div>
                      <Button size='sm' variant='outline' className='h-7 rounded-full text-xs' onClick={() => { setEditingWindow(null); setWindowDialogOpen(true); }}>
                        <Plus className='h-3 w-3' /> Add window
                      </Button>
                    </div>
                    {linkWindows.length === 0 ? (
                      <p className='text-xs text-slate-400 py-3 text-center'>No time windows configured.</p>
                    ) : (
                      <Table>
                        <TableHeader>
                          <TableRow className='text-xs'>
                            <TableHead>Schedule</TableHead>
                            <TableHead>Direction</TableHead>
                            <TableHead>Window</TableHead>
                            <TableHead>Distance</TableHead>
                            <TableHead className='text-right'>Actions</TableHead>
                          </TableRow>
                        </TableHeader>
                        <TableBody>
                          {linkWindows.map((w) => (
                            <TableRow key={w.id} className='text-xs'>
                              <TableCell className='font-medium'>{w.scheduleName}</TableCell>
                              <TableCell>{DIRECTION_LABELS[w.direction] || w.direction}</TableCell>
                              <TableCell>{w.windowStart} – {w.windowEnd}</TableCell>
                              <TableCell>
                                {w.estimatedDistanceToSchoolKm != null ? `${w.estimatedDistanceToSchoolKm} km` : '-'}
                                {w.estimatedDurationToSchoolMin != null ? ` / ${w.estimatedDurationToSchoolMin} min` : ''}
                              </TableCell>
                              <TableCell className='text-right'>
                                <div className='flex items-center justify-end gap-1'>
                                  <Button size='icon' variant='ghost' className='h-7 w-7' onClick={() => { setEditingWindow(w); setWindowDialogOpen(true); }}><Pencil className='h-3 w-3' /></Button>
                                  <Button size='icon' variant='ghost' className='h-7 w-7 text-rose-600 hover:text-rose-700' onClick={() => handleDeleteWindow(w.id)}><Trash2 className='h-3 w-3' /></Button>
                                </div>
                              </TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
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
        {/* Stats */}
        <div className='grid gap-3 grid-cols-2 lg:grid-cols-4'>
          <SchoolBusMetricCard label='Schools' value={schools.length} icon={Building2} tone='info' />
          <SchoolBusMetricCard label='Pickup points' value={pickupPoints.length} icon={MapPinned} tone='success' />
          <SchoolBusMetricCard label='Linked pickups' value={allLinks.length} icon={Link2} tone='default' />
          <SchoolBusMetricCard
            label='Missing coords'
            value={missingCoordsCount}
            icon={AlertTriangle}
            tone={missingCoordsCount > 0 ? 'warning' : 'success'}
          />
        </div>

        {/* Tabs */}
        <SchoolBusTableTabs
          defaultValue='schools'
          tabs={[
            { value: 'schools', label: 'Schools', count: schools.length, content: schoolsTabContent },
            { value: 'pickup-points', label: 'Pickup points', count: pickupPoints.length, content: pickupPointsTabContent },
            { value: 'schedules', label: 'Schedules', count: schedules.length, content: schedulesTabContent },
            { value: 'linked-pickups', label: 'Linked pickups', count: linkedPickupPoints.length, content: linkedPickupTabContent },
          ]}
        />
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
