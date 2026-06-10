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
  ChevronDown,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react';
import { toast } from 'sonner';
import { useRouter, useSearchParams, useParams, usePathname } from 'next/navigation';
import { Popover, PopoverContent, PopoverTrigger } from '@/shared/components/ui/popover';
import { SchoolBusSelect } from '../components/ui/SchoolBusSelect';
import { Button, Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useGetSchoolByIdQuery,
  useUpdateSchoolMutation,
  useLinkSchoolPickupPointMutation,
  useUnlinkSchoolPickupPointMutation,
  useGetSchoolPickupPointsQuery,
  useGetPickupPointsQuery,
} from '../api/schoolBusApi';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { SchoolBusDeleteDialog } from '../components/SchoolBusDeleteDialog';
import { SchoolFormDialog } from '../components/SchoolBusMasterDataForms';
import { LinkPickupPointDialog } from '../components/forms/LinkPickupPointDialog';
import { OperationsMap } from '../components/map/OperationsMap';
import { SchoolBusMapLegend } from '../components/map/SchoolBusMapLegend';
import { SchoolBusMapWorkspace } from '../components/map/SchoolBusMapWorkspace';
import { MapMarkerVisibilityProvider } from '../components/map/MapMarkerVisibilityContext';
import { formatDate, getPageItems, SCHOOL_BUS_OPTION_QUERY } from '../utils';
import type {
  SchoolBusSchool,
  SchoolBusSchoolPickupPoint,
  SchoolBusPickupPoint,
} from '../types';

interface SchoolBusSchoolDetailPageProps {
  schoolId: number;
  onClose: () => void;
}

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
  schoolId: propSchoolId,
  onClose,
}: SchoolBusSchoolDetailPageProps) {
  const params = useParams();
  const searchParams = useSearchParams();
  const pathname = usePathname();
  const router = useRouter();

  // Ensure we extract the pure numeric ID for display/fetching if a string was passed at runtime
  let rawId = params?.id ? decodeURIComponent(String(params.id)) : String(propSchoolId);
  if ((!rawId || !rawId.includes('&')) && typeof window !== 'undefined') {
    const windowPath = window.location.pathname;
    const match = windowPath.match(/\/school-bus\/schools\/(.+)$/);
    if (match && match[1]) {
      rawId = decodeURIComponent(match[1]);
    }
  }

  const cleanSchoolId = rawId.includes('&')
    ? Number(rawId.split('&')[0])
    : Number(rawId || propSchoolId);

  // Parse tab and view from dynamic path if format is id&tab=...
  let tabFromUrl = searchParams.get('tab');
  let viewParam = searchParams.get('view') || 'network';

  if (rawId.includes('&')) {
    const decoded = decodeURIComponent(rawId);
    const parts = decoded.split('&');
    for (let i = 1; i < parts.length; i++) {
      const [k, v] = parts[i].split('=');
      if (k === 'tab') tabFromUrl = v;
      if (k === 'view') viewParam = v;
    }
  }

  const activeTab = tabFromUrl === 'linked-pickups' ? 'pickups' : 'general';

  const setActiveTab = (newTab: 'general' | 'pickups') => {
    const urlTab = newTab === 'pickups' ? 'linked-pickups' : newTab;
    router.push(`/school-bus/schools/${cleanSchoolId}&tab=${urlTab}&view=${viewParam}`);
  };

  // --- RTK queries & mutations ---
  const { data: schoolDetailData, isLoading: loadingSchool, refetch: refetchSchool } =
    useGetSchoolByIdQuery(cleanSchoolId);
  const school = schoolDetailData?.data;

  const { data: linkedPickupData, refetch: refetchLinkedPickups } = useGetSchoolPickupPointsQuery({
    schoolId: cleanSchoolId,
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
  const [linkPickupPoint, { isLoading: linkingPickup }] = useLinkSchoolPickupPointMutation();
  const [unlinkPickupPoint, { isLoading: unlinkingPickup }] = useUnlinkSchoolPickupPointMutation();

  // --- Dialog states ---
  const [schoolDialogOpen, setSchoolDialogOpen] = React.useState(false);
  const [linkDialogOpen, setLinkDialogOpen] = React.useState(false);
  const [deleteTarget, setDeleteTarget] = React.useState<{
    type: 'link';
    id: number;
    title: string;
    description: string;
  } | null>(null);

  // --- Filters ---
  const [pickupSearch, setPickupSearch] = React.useState('');
  const [pickupUsageFilter, setPickupUsageFilter] = React.useState<'ALL' | 'PICKUP' | 'DROPOFF' | 'PICKUP_DROPOFF'>('ALL');
  const [pickupCoordsFilter, setPickupCoordsFilter] = React.useState<'ALL' | 'CONFIGURED' | 'MISSING'>('ALL');

  // --- Map fit control key ---
  const [fitAllKey, setFitAllKey] = React.useState(0);
  const [fitSelectedKey, setFitSelectedKey] = React.useState(0);
  const handleFitAll = React.useCallback(() => setFitAllKey((k) => k + 1), []);
  const handleFitSelected = React.useCallback(() => setFitSelectedKey((k) => k + 1), []);
  const [selectedPickupPointId, setSelectedPickupPointId] = React.useState<number | null>(null);

  // --- Handlers ---
  const refetchAll = React.useCallback(async () => {
    await Promise.all([refetchSchool(), refetchLinkedPickups()]);
  }, [refetchSchool, refetchLinkedPickups]);

  const handleEditSchool = async (values: any) => {
    try {
      const response = await updateSchool({ id: cleanSchoolId, body: values }).unwrap();
      toast.success(response.message || 'School details updated');
      setSchoolDialogOpen(false);
      refetchSchool();
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to update school');
    }
  };

  const handleLinkPickupPoint = async (values: any) => {
    try {
      const response = await linkPickupPoint({ schoolId: cleanSchoolId, body: values }).unwrap();
      toast.success(response.message || 'Pickup point linked successfully');
      setLinkDialogOpen(false);
      refetchAll();
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to link pickup point');
    }
  };

  const handleConfirmDelete = async () => {
    if (!deleteTarget) return;
    try {
      if (deleteTarget.type === 'link') {
        const response = await unlinkPickupPoint({ schoolId: cleanSchoolId, pickupPointId: deleteTarget.id }).unwrap();
        toast.success(response.message || 'Pickup point unlinked');
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

  const schoolPickupPointCount = school.pickupPointCount ?? linkedPickupPoints?.length ?? 0;
  const hasMissingCoords = school.anyLinkedPointMissingCoordinates ||
    (linkedPickupPoints && linkedPickupPoints.some((lp) => typeof lp.pickupPointLatitude !== 'number' || typeof lp.pickupPointLongitude !== 'number'));

  return (
    <>
      <SchoolBusPageShell
        title={school.name}
        description={school.address || 'Manage school profiles, associated schedules, and pickup points.'}
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
                setLinkDialogOpen(true);
              }}
            >
              <Plus className="mr-1.5 h-4 w-4" />
              Link Pickup
            </Button>
          </div>
        }
      >
        <div className="flex flex-col gap-6">
          {/* Summary metrics bar */}
          <div className="grid gap-3 grid-cols-2">
            <SchoolBusMetricCard
              label="Linked Pickups"
              value={schoolPickupPointCount}
              icon={MapPin}
              tone="pickup"
              variant="compact"
            />
            <SchoolBusMetricCard
              label="Coordinated points"
              value={schoolPickupPointCount && hasMissingCoords ? 'Warning' : 'Configured'}
              icon={AlertTriangle}
              tone={hasMissingCoords ? 'warning' : 'success'}
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
                  selectedSchoolId={cleanSchoolId}
                  selectedPickupPointId={selectedPickupPointId}
                  onSchoolSelect={() => {}}
                  onPickupPointSelect={(id) => setSelectedPickupPointId(id)}
                  fitAllKey={fitAllKey}
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
            legend={<SchoolBusMapLegend showRouteLines={false} />}
            onFitAll={handleFitAll}
            onFitRoute={handleFitSelected}
            canFitAll={hasMapData}
            canFitRoute={!!cleanSchoolId || !!selectedPickupPointId}
            fitRouteLabel="Fit Selected"
          />
        </div>
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
              <SchoolBusSelect
                value={pickupUsageFilter}
                onChange={(val) => setPickupUsageFilter(val as any)}
                options={[
                  { label: 'All usage types', value: 'ALL' },
                  { label: 'Pickup only', value: 'PICKUP' },
                  { label: 'Drop-off only', value: 'DROPOFF' },
                  { label: 'Pickup & Drop-off', value: 'PICKUP_DROPOFF' },
                ]}
                placeholder="Select usage type"
                fullWidth
                className="border-slate-200 hover:border-slate-350 rounded-lg text-xs"
              />
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

        {/* Link Pickup Point Dialog */}
        <LinkPickupPointDialog
          open={linkDialogOpen}
          onOpenChange={setLinkDialogOpen}
          pickupPoints={allPickupPoints}
          isLoading={linkingPickup}
          onSubmit={handleLinkPickupPoint}
        />

        {/* Delete Confirmation Dialog */}
        <SchoolBusDeleteDialog
          open={Boolean(deleteTarget)}
          onOpenChange={(open) => {
            if (!open) setDeleteTarget(null);
          }}
          title={deleteTarget?.title || 'Delete confirmation'}
          description={deleteTarget?.description || 'This operation is permanent.'}
          isLoading={linkingPickup || unlinkingPickup}
          onConfirm={handleConfirmDelete}
        />
      </>
    );
  }
}
