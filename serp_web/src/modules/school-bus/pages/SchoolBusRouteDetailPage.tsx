'use client';

import Link from 'next/link';
import * as React from 'react';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import {
  ClipboardCheck,
  BusFront,
  MapPinned,
  Pencil,
  PlayCircle,
  Route,
  Sparkles,
  UserCog,
  GraduationCap,
  Calendar,
  Clock3,
  MapPin,
  Users,
  FileText,
  History,
  Download,
  AlertTriangle,
  CheckCircle2,
  Info,
  Settings,
  Activity,
  FileSpreadsheet,
  Copy,
} from 'lucide-react';
import { toast } from 'sonner';
import {
  Button,
  Badge,
  Tabs,
  TabsList,
  TabsTrigger,
  TabsContent,
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  SheetDescription,
  ScrollArea,
  Accordion,
  AccordionItem,
  AccordionTrigger,
  AccordionContent,
  Tooltip,
  TooltipTrigger,
  TooltipContent,
  TooltipProvider,
  Select,
  SelectTrigger,
  SelectValue,
  SelectContent,
  SelectItem,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useAssignRouteMutation,
  useComputeRoutePathMutation,
  useValidateRouteMutation,
  useCreateTripFromRouteMutation,
  useGetAttendantsQuery,
  useGetBusesQuery,
  useGetDriversQuery,
  useGetRoutePathQuery,
  useGetRouteByIdQuery,
  useGetLatestRouteCalculationTraceQuery,
  useGetRouteCalculationTraceHistoryQuery,
  useLazyExportLatestRouteCalculationTraceQuery,
  useLazyExportRouteCalculationTraceQuery,
} from '../api/schoolBusApi';
import { RouteAssignmentDialog } from '../components/SchoolBusWorkflowForms';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusSection } from '../components/SchoolBusSection';
import { SchoolBusScrollableTable } from '../components/SchoolBusScrollableTable';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { RouteMap } from '../components/map/RouteMap';
import { SchoolBusMapLegend } from '../components/map/SchoolBusMapLegend';
import { SchoolBusMapWorkspace } from '../components/map/SchoolBusMapWorkspace';
import { schoolBusUi } from '../theme';
import { formatDate, formatDateTime, getPageItems, SCHOOL_BUS_OPTION_QUERY } from '../utils';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';

interface SchoolBusRouteDetailPageProps {
  routeId: number;
}

export function SchoolBusRouteDetailPage({
  routeId,
}: SchoolBusRouteDetailPageProps) {
  const { data, isLoading, refetch: refetchRoute } = useGetRouteByIdQuery(routeId);
  const { data: busesData } = useGetBusesQuery({
    ...SCHOOL_BUS_OPTION_QUERY,
    sortBy: 'plateNumber',
  });
  const { data: driversData } = useGetDriversQuery({
    ...SCHOOL_BUS_OPTION_QUERY,
    sortBy: 'fullName',
  });
  const { data: attendantsData } = useGetAttendantsQuery({
    ...SCHOOL_BUS_OPTION_QUERY,
    sortBy: 'fullName',
  });
  const [computeRoutePath, { isLoading: computingPath }] =
    useComputeRoutePathMutation();
  const [validateRoute, { isLoading: validatingRoute }] =
    useValidateRouteMutation();
  const [assignRoute, { isLoading: assigning }] = useAssignRouteMutation();
  const [createTripFromRoute, { isLoading: creatingTrip }] =
    useCreateTripFromRouteMutation();
  const [assignmentOpen, setAssignmentOpen] = React.useState(false);
  const [fitKey, setFitKey] = React.useState(0);
  const { data: routePathData, refetch: refetchPath } = useGetRoutePathQuery(routeId);

  // Calculation Trace Queries & Mutations
  const { data: latestTraceData, refetch: refetchLatestTrace } = useGetLatestRouteCalculationTraceQuery(routeId);
  const { data: traceHistoryData, refetch: refetchTraceHistory } = useGetRouteCalculationTraceHistoryQuery(routeId);
  const [triggerExportLatest, { isLoading: exportingLatest }] = useLazyExportLatestRouteCalculationTraceQuery();
  const [triggerExportSpecific, { isLoading: exportingSpecific }] = useLazyExportRouteCalculationTraceQuery();

  const [selectedTraceId, setSelectedTraceId] = React.useState<number | null>(null);
  const [isDetailOpen, setIsDetailOpen] = React.useState(false);
  const [activeTab, setActiveTab] = React.useState('overview');
  const [rawJsonType, setRawJsonType] = React.useState<string>('full');

  const handleDownloadLatestExcel = async () => {
    try {
      const blob = await triggerExportLatest(routeId).unwrap();
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `routing-trace-${routeId}-latest.xlsx`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      toast.success('Excel export downloaded');
    } catch (err: any) {
      toast.error('Failed to export Excel file');
    }
  };

  const handleDownloadSpecificExcel = async (traceId: number) => {
    try {
      const blob = await triggerExportSpecific({ routeId, traceId }).unwrap();
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `routing-trace-${routeId}-${traceId}.xlsx`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      toast.success(`Excel export for trace #${traceId} downloaded`);
    } catch (err: any) {
      toast.error('Failed to export Excel file');
    }
  };

  const selectedTrace = React.useMemo(() => {
    if (!selectedTraceId) return null;
    return (
      traceHistoryData?.data?.find((t) => t.id === selectedTraceId) ||
      (latestTraceData?.data?.id === selectedTraceId ? latestTraceData?.data : null)
    );
  }, [selectedTraceId, traceHistoryData, latestTraceData]);

  const parsedInput = React.useMemo(() => {
    if (!selectedTrace?.inputJson) return null;
    try {
      return JSON.parse(selectedTrace.inputJson);
    } catch (e) {
      return null;
    }
  }, [selectedTrace]);

  const parsedMatrix = React.useMemo(() => {
    if (!selectedTrace?.matrixJson) return null;
    try {
      return JSON.parse(selectedTrace.matrixJson);
    } catch (e) {
      return null;
    }
  }, [selectedTrace]);

  const parsedTimeline = React.useMemo(() => {
    if (!selectedTrace?.timelineJson) return null;
    try {
      return JSON.parse(selectedTrace.timelineJson);
    } catch (e) {
      return null;
    }
  }, [selectedTrace]);

  const parsedIssues = React.useMemo(() => {
    if (!selectedTrace?.issuesJson) return null;
    try {
      return JSON.parse(selectedTrace.issuesJson);
    } catch (e) {
      return null;
    }
  }, [selectedTrace]);

  const parsedConfig = React.useMemo(() => {
    if (!selectedTrace?.configSnapshotJson) return null;
    try {
      return JSON.parse(selectedTrace.configSnapshotJson);
    } catch (e) {
      return null;
    }
  }, [selectedTrace]);

  const latestIssues = React.useMemo(() => {
    if (!latestTraceData?.data?.issuesJson) return [];
    try {
      const parsed = JSON.parse(latestTraceData.data.issuesJson);
      return Array.isArray(parsed) ? parsed : [];
    } catch (e) {
      return [];
    }
  }, [latestTraceData]);

  const detail = data?.data;

  React.useEffect(() => {
    if (typeof window !== 'undefined') {
      const params = new URLSearchParams(window.location.search);
      if (params.get('assign') === 'true') {
        setAssignmentOpen(true);
      }
    }
  }, []);

  const handleComputePath = async () => {
    try {
      const response = await computeRoutePath(routeId).unwrap();
      
      // Refetch details, path, and traces immediately to update UI
      refetchRoute();
      refetchPath();
      refetchLatestTrace();
      refetchTraceHistory();

      if (response.data?.estimated) {
        toast.warning(response.data.warning || 'Routing fallback path applied');
      } else {
        toast.success('Computed real route path');
      }
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to compute route path');
    }
  };

  const handleValidateRoute = async () => {
    try {
      const response = await validateRoute(routeId).unwrap();
      refetchRoute();
      refetchLatestTrace();
      refetchTraceHistory();
      
      const blockingCount = response.data?.blockingIssueCount ?? 0;
      const warningCount = response.data?.warningIssueCount ?? 0;
      
      if (blockingCount > 0) {
        toast.error(`Validation failed: ${blockingCount} blocking issues found.`);
      } else if (warningCount > 0) {
        toast.warning(`Validated with warnings: ${warningCount} warnings found.`);
      } else {
        toast.success('Validation success! Route is feasible for dispatch.');
      }
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to validate route');
    }
  };

  const handleAssign = async (values: any) => {
    try {
      const response = await assignRoute({ id: routeId, body: values }).unwrap();
      toast.success(response.message || 'Route assigned');
      setAssignmentOpen(false);
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to assign route');
    }
  };

  const handleCreateTrip = async () => {
    try {
      const response = await createTripFromRoute(routeId).unwrap();
      toast.success(response.message || 'Trip created from route');
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to create trip');
    }
  };

  if (isLoading || !detail) {
    return (
      <SchoolBusPageShell title='Route detail' description='Loading route detail...'>
        <SchoolBusEmptyState
          title='Loading route detail'
          description='Fetching route, stop, and assignment data.'
        />
      </SchoolBusPageShell>
    );
  }

  const route = detail.route;
  const isAssigned = !!detail.assignment;
  // Middle stops that lack coordinates (terminals use route-level coords instead)
  const missingStopCoordinates = detail.stops.filter(
    (stop) =>
      stop.stopPurpose !== 'START_TERMINAL' &&
      stop.stopPurpose !== 'END_TERMINAL' &&
      (typeof stop.pickupPointLatitude !== 'number' ||
        typeof stop.pickupPointLongitude !== 'number'),
  ).length;

  const friendlyStatusLabel = (status: string) => {
    const s = status.toUpperCase();
    if (s === 'TRIP_CREATED') return 'Trip created';
    if (s === 'PUBLISHED') return 'Published';
    if (s === 'PLANNED') return 'Planned';
    if (s === 'ASSIGNED') return 'Assigned';
    if (s === 'IN_PROGRESS') return 'In progress';
    if (s === 'COMPLETED') return 'Completed';
    if (s === 'CANCELLED') return 'Cancelled';
    return status;
  };

  return (
    <>
      <SchoolBusPageShell
        title={`${route.routeCode} - ${route.routeName}`}
        description='Inspect route state, stop plan, and assignment details before and during execution.'
        actions={
          <>
            {['DRAFT', 'PLANNED', 'ASSIGNED'].includes(route.status) ? (
              <Button variant='outline' className='rounded-full' asChild>
                <Link href={`/school-bus/dispatch/${route.id}/edit`}>
                  <Pencil className='h-4 w-4' />
                  Edit
                </Link>
              </Button>
            ) : null}
            {route.blockingIssueCount && route.blockingIssueCount > 0 ? (
              <TooltipProvider>
                <Tooltip>
                  <TooltipTrigger asChild>
                    <span>
                      <Button
                        variant={isAssigned ? 'outline' : 'default'}
                        className={cn(
                          'rounded-full font-semibold opacity-50 cursor-not-allowed',
                          !isAssigned && 'bg-slate-300 text-slate-500 border-0 hover:bg-slate-300'
                        )}
                        disabled
                      >
                        <UserCog className='h-4 w-4' />
                        {isAssigned ? 'Manage assignment' : 'Assign resources'}
                      </Button>
                    </span>
                  </TooltipTrigger>
                  <TooltipContent>
                    <p className='text-xs'>Cannot assign resources. Route has blocking issues.</p>
                  </TooltipContent>
                </Tooltip>
              </TooltipProvider>
            ) : (
              <Button
                variant={isAssigned ? 'outline' : 'default'}
                className={cn(
                  'rounded-full font-semibold',
                  !isAssigned && 'bg-[#C81E3A] hover:bg-[#B31B34] text-white border-0'
                )}
                onClick={() => setAssignmentOpen(true)}
              >
                <UserCog className='h-4 w-4' />
                {isAssigned ? 'Manage assignment' : 'Assign resources'}
              </Button>
            )}
            <TooltipProvider>
              <Tooltip>
                <TooltipTrigger asChild>
                  <Button
                    variant='outline'
                    className='rounded-full border-[#C81E3A] text-[#C81E3A] hover:bg-rose-50 hover:text-[#B31B34]'
                    onClick={handleValidateRoute}
                    disabled={validatingRoute}
                  >
                    <ClipboardCheck className='h-4 w-4' />
                    {validatingRoute ? 'Validating...' : 'Validate Route'}
                  </Button>
                </TooltipTrigger>
                <TooltipContent>
                  <p className='text-xs max-w-[250px]'>
                    Trigger feasibility checks and generate warning/blocking issues.
                  </p>
                </TooltipContent>
              </Tooltip>
            </TooltipProvider>
            <Button variant='outline' className='rounded-full' asChild>
              <Link href={`/school-bus/dispatch/planning?sessionId=${route.planningSessionId || ''}&routeId=${route.id || ''}`}>
                <Sparkles className='h-4 w-4' />
                Plan workspace
              </Link>
            </Button>
            <TooltipProvider>
              <Tooltip>
                <TooltipTrigger asChild>
                  <Button
                    variant='outline'
                    className='rounded-full'
                    onClick={handleComputePath}
                    disabled={computingPath}
                  >
                    <MapPinned className='h-4 w-4' />
                    {computingPath ? 'Computing...' : 'Recompute path'}
                  </Button>
                </TooltipTrigger>
                <TooltipContent>
                  <p className='text-xs max-w-[250px]'>
                    Recalculate route geometry, timeline and calculation trace after stop changes.
                  </p>
                </TooltipContent>
              </Tooltip>
            </TooltipProvider>
            {route.status === 'ASSIGNED' ? (
              <Button
                className='rounded-full bg-indigo-600 hover:bg-indigo-750 text-white font-semibold'
                onClick={handleCreateTrip}
                disabled={creatingTrip}
              >
                <BusFront className='h-4 w-4 mr-1' />
                {creatingTrip ? 'Creating...' : 'Create trip'}
              </Button>
            ) : null}
          </>
        }
      >
        {/* Validation Issues Strip */}
        {detail.issues && detail.issues.length > 0 && (
          <div className='mb-6 bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-4'>
            <div className='flex items-center gap-2 pb-2.5 border-b border-slate-100'>
              <AlertTriangle className='h-4.5 w-4.5 text-[#C81E3A] shrink-0' />
              <h3 className='font-bold text-slate-900 text-sm'>Validation Issues ({detail.issues.length})</h3>
            </div>
            
            <div className='space-y-4'>
              {/* Blocking Issues */}
              {detail.blockingIssues && detail.blockingIssues.length > 0 && (
                <div className='space-y-2.5'>
                  <h4 className='text-xs font-bold text-rose-800 flex items-center gap-1.5'>
                    <span className='h-1.5 w-1.5 rounded-full bg-rose-600' />
                    Blocking Issues ({detail.blockingIssues.length})
                  </h4>
                  <div className='grid gap-2'>
                    {detail.blockingIssues.map((issue, idx) => (
                      <div key={idx} className='bg-rose-50/40 border border-rose-100 rounded-xl p-3.5 space-y-1.5 text-xs'>
                        <div className='flex justify-between items-start gap-3'>
                          <div className='font-bold text-slate-850 leading-normal'>
                            {issue.message}
                          </div>
                          <span className='shrink-0 px-2 py-0.5 rounded text-[9px] font-extrabold uppercase bg-rose-100 text-rose-800 border border-rose-200/60 tracking-wider'>
                            {issue.issueType}
                          </span>
                        </div>
                        {issue.stopName && (
                          <div className='text-slate-500 font-medium text-[11px] flex items-center gap-1'>
                            <span>📍 Stop:</span>
                            <span className='font-bold text-slate-700'>{issue.stopName}</span>
                          </div>
                        )}
                        {issue.studentName && (
                          <div className='text-slate-500 font-medium text-[11px] flex items-center gap-1'>
                            <span>👤 Student:</span>
                            <span className='font-bold text-slate-700'>{issue.studentName}</span>
                          </div>
                        )}
                        {issue.suggestedFix && (
                          <div className='mt-2 pt-2 border-t border-rose-250/40 text-[11px] text-rose-900/90 leading-normal font-semibold flex items-start gap-1'>
                            <span className='font-extrabold text-rose-955 shrink-0'>Fix:</span>
                            <span>{issue.suggestedFix}</span>
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Warning Issues */}
              {detail.warningIssues && detail.warningIssues.length > 0 && (
                <div className='space-y-2.5'>
                  <h4 className='text-xs font-bold text-amber-800 flex items-center gap-1.5'>
                    <span className='h-1.5 w-1.5 rounded-full bg-amber-600' />
                    Warnings ({detail.warningIssues.length})
                  </h4>
                  <div className='grid gap-2'>
                    {detail.warningIssues.map((issue, idx) => (
                      <div key={idx} className='bg-amber-50/40 border border-amber-100 rounded-xl p-3.5 space-y-1.5 text-xs'>
                        <div className='flex justify-between items-start gap-3'>
                          <div className='font-bold text-slate-850 leading-normal'>
                            {issue.message}
                          </div>
                          <span className='shrink-0 px-2 py-0.5 rounded text-[9px] font-extrabold uppercase bg-amber-100 text-amber-800 border border-amber-200/60 tracking-wider'>
                            {issue.issueType}
                          </span>
                        </div>
                        {issue.stopName && (
                          <div className='text-slate-500 font-medium text-[11px] flex items-center gap-1'>
                            <span>📍 Stop:</span>
                            <span className='font-bold text-slate-700'>{issue.stopName}</span>
                          </div>
                        )}
                        {issue.studentName && (
                          <div className='text-slate-500 font-medium text-[11px] flex items-center gap-1'>
                            <span>👤 Student:</span>
                            <span className='font-bold text-slate-700'>{issue.studentName}</span>
                          </div>
                        )}
                        {issue.suggestedFix && (
                          <div className='mt-2 pt-2 border-t border-amber-250/40 text-[11px] text-amber-900/90 leading-normal font-semibold flex items-start gap-1'>
                            <span className='font-extrabold text-amber-955 shrink-0'>Fix:</span>
                            <span>{issue.suggestedFix}</span>
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>
        )}

        {/* Readiness Strip */}
        <div className='grid grid-cols-2 md:grid-cols-5 gap-4 mb-6'>
          <ReadinessCard
            label="Planning"
            value={route.status === 'DRAFT' ? 'Draft' : 'Complete'}
            status={route.status === 'DRAFT' ? 'warning' : 'success'}
          />
          <ReadinessCard
            label="Validation"
            value={
              (route.blockingIssueCount ?? 0) > 0
                ? `${route.blockingIssueCount} Blocking`
                : (route.issueCount ?? 0) > 0
                ? `${route.issueCount} Warning`
                : 'Valid'
            }
            status={
              (route.blockingIssueCount ?? 0) > 0
                ? 'danger'
                : (route.issueCount ?? 0) > 0
                ? 'warning'
                : 'success'
            }
          />
          <ReadinessCard
            label="Assignment"
            value={isAssigned ? 'Ready' : 'Missing'}
            status={isAssigned ? 'success' : 'warning'}
          />
          <ReadinessCard
            label="Trip Snapshot"
            value={['TRIP_CREATED', 'IN_PROGRESS', 'COMPLETED'].includes(route.status) ? 'Locked' : 'Not created'}
            status={['TRIP_CREATED', 'IN_PROGRESS', 'COMPLETED'].includes(route.status) ? 'success' : 'muted'}
          />
          <ReadinessCard
            label="Attendance"
            value={['TRIP_CREATED', 'IN_PROGRESS', 'COMPLETED'].includes(route.status) ? 'Available' : 'Locked'}
            status={['TRIP_CREATED', 'IN_PROGRESS', 'COMPLETED'].includes(route.status) ? 'success' : 'muted'}
          />
        </div>

        {/* Main 2-column workspace */}
        <div className='grid gap-6 xl:grid-cols-[0.95fr_1.05fr] items-start'>
          
          {/* Left Column */}
          <div className='flex flex-col gap-6'>
            
            {/* Route Summary compact card */}
            <div className='bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-4'>
              <div className='flex items-center justify-between pb-2.5 border-b border-slate-100'>
                <div className='flex items-center gap-2'>
                  <Route className='h-4.5 w-4.5 text-indigo-600 shrink-0' />
                  <h3 className='font-bold text-slate-900 text-sm'>Route Summary</h3>
                </div>
                <span className='rounded-full px-2.5 py-0.5 text-[10px] font-extrabold border shadow-none bg-slate-50 border-slate-200 text-slate-600 uppercase tracking-wider'>
                  {friendlyStatusLabel(route.status)}
                </span>
              </div>
              
              <div className='grid grid-cols-2 gap-y-4 gap-x-6 text-xs'>
                <SummaryItem label="School" value={route.schoolName} icon={GraduationCap} />
                <SummaryItem
                  label="Direction"
                  value={route.routeDirection === 'RETURN' ? 'Return (Chiều về)' : 'Outbound (Chiều đi)'}
                  icon={MapPinned}
                  isBadge
                  badgeColor={route.routeDirection === 'RETURN' ? 'bg-orange-50 text-orange-700 border-orange-100' : 'bg-blue-50 text-blue-700 border-blue-100'}
                />
                <SummaryItem label="Service Date" value={formatDate(route.serviceDate)} icon={Calendar} />
                <SummaryItem label="Schedule" value={route.schoolScheduleName} icon={Clock3} />
                <SummaryItem label="Start Terminal" value={`${route.startLocationName} (${route.startLocationType})`} icon={MapPin} />
                <SummaryItem label="End Terminal" value={`${route.endLocationName} (${route.endLocationType})`} icon={MapPin} />
                <SummaryItem label="Planned Distance" value={route.plannedDistanceKm != null ? `${route.plannedDistanceKm} km` : 'N/A'} icon={Route} />
                <SummaryItem label="Planned Duration" value={route.plannedDurationMin != null ? `${route.plannedDurationMin} mins` : 'N/A'} icon={Clock3} />
                <SummaryItem label="Started At" value={formatDateTime(route.startedAt) || 'N/A'} icon={PlayCircle} />
                <SummaryItem label="Completed At" value={formatDateTime(route.completedAt) || 'N/A'} icon={ClipboardCheck} />
              </div>
            </div>

            {/* Assignment Details card */}
            <div className='bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-4'>
              <div className='flex items-center justify-between pb-2.5 border-b border-slate-100'>
                <div className='flex items-center gap-2'>
                  <UserCog className='h-4.5 w-4.5 text-emerald-600 shrink-0' />
                  <h3 className='font-bold text-slate-900 text-sm'>Assignment Details</h3>
                </div>
                <span className={cn(
                  'rounded-full px-2.5 py-0.5 text-[10px] font-bold border shadow-none uppercase tracking-wider',
                  isAssigned
                    ? 'bg-emerald-50 text-emerald-700 border-emerald-250'
                    : 'bg-amber-50 text-amber-700 border-amber-250'
                )}>
                  {isAssigned ? 'Ready for execution' : `Missing ${[!detail.assignment?.busId, !detail.assignment?.driverId, !detail.assignment?.attendantId].filter(Boolean).length} resource(s)`}
                </span>
              </div>

              <div className='grid gap-3'>
                <AssignmentRow
                  label="Bus Vehicle"
                  value={detail.assignment?.busPlateNumber}
                  type="bus"
                />
                <AssignmentRow
                  label="Driver"
                  value={detail.assignment?.driverName}
                  type="driver"
                />
                <AssignmentRow
                  label="Attendant"
                  value={detail.assignment?.attendantName}
                  type="attendant"
                />
              </div>

              <div className='pt-2'>
                {route.blockingIssueCount && route.blockingIssueCount > 0 ? (
                  <TooltipProvider>
                    <Tooltip>
                      <TooltipTrigger asChild>
                        <span>
                          <Button
                            variant={isAssigned ? 'outline' : 'default'}
                            className={cn(
                              'w-full rounded-full text-xs font-semibold h-9 opacity-50 cursor-not-allowed',
                              !isAssigned && 'bg-slate-300 text-slate-500 border-0 hover:bg-slate-300'
                            )}
                            disabled
                          >
                            <UserCog className='h-3.5 w-3.5 mr-1.5' />
                            {isAssigned ? 'Manage assignment' : 'Assign resources'}
                          </Button>
                        </span>
                      </TooltipTrigger>
                      <TooltipContent>
                        <p className='text-xs'>Cannot assign resources. Route has blocking issues.</p>
                      </TooltipContent>
                    </Tooltip>
                  </TooltipProvider>
                ) : (
                  <Button
                    variant={isAssigned ? 'outline' : 'default'}
                    className={cn(
                      'w-full rounded-full text-xs font-semibold h-9',
                      !isAssigned && 'bg-[#C81E3A] hover:bg-[#B31B34] text-white border-0'
                    )}
                    onClick={() => setAssignmentOpen(true)}
                  >
                    <UserCog className='h-3.5 w-3.5 mr-1.5' />
                    {isAssigned ? 'Manage assignment' : 'Assign resources'}
                  </Button>
                )}
              </div>
            </div>

            {/* Next Recommended Actions (Workflow Stepper) */}
            <div className='bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-4'>
              <div className='flex items-center gap-2 pb-2.5 border-b border-slate-100'>
                <ClipboardCheck className='h-4.5 w-4.5 text-indigo-600 shrink-0' />
                <h3 className='font-bold text-slate-900 text-sm'>Route Workflow</h3>
              </div>

              <div className='relative pl-3.5 space-y-6 before:absolute before:left-[23px] before:top-2 before:bottom-2 before:w-[2px] before:bg-slate-100'>
                {/* Step 1: Plan Route */}
                <WorkflowStep
                  stepNumber={1}
                  title="Plan Route"
                  description="Define school, schedule, direction, and draft stops."
                  status="completed"
                />

                {/* Step 2: Assign Resources */}
                <WorkflowStep
                  stepNumber={2}
                  title="Assign Resources"
                  description="Assign vehicle, driver, and crew to execute this route."
                  status={isAssigned ? 'completed' : 'current'}
                  action={!isAssigned ? (
                    <Button size='sm' variant='outline' className='h-7 text-[10px] rounded-full border-slate-200 mt-2 font-semibold shadow-none' onClick={() => setAssignmentOpen(true)}>
                      Assign resources
                    </Button>
                  ) : null}
                />

                {/* Step 3: Create Trip Snapshot */}
                <WorkflowStep
                  stepNumber={3}
                  title="Create Trip Snapshot"
                  description="Lock route structure into an active trip manifest."
                  status={
                    ['TRIP_CREATED', 'IN_PROGRESS', 'COMPLETED'].includes(route.status)
                      ? 'completed'
                      : isAssigned
                        ? 'available'
                        : 'locked'
                  }
                  action={
                    route.status === 'ASSIGNED' ? (
                      <Button size='sm' className='h-7 text-[10px] rounded-full bg-[#C81E3A] hover:bg-[#B31B34] text-white mt-2 font-semibold shadow-none' onClick={handleCreateTrip} disabled={creatingTrip}>
                        {creatingTrip ? 'Creating...' : 'Create trip'}
                      </Button>
                    ) : null
                  }
                />

                {/* Step 4: Run Attendance */}
                <WorkflowStep
                  stepNumber={4}
                  title="Run Attendance"
                  description="Check-in and check-out students using manifest lists."
                  status={
                    ['IN_PROGRESS', 'COMPLETED'].includes(route.status)
                      ? 'completed'
                      : ['TRIP_CREATED'].includes(route.status)
                        ? 'available'
                        : 'locked'
                  }
                  action={
                    ['TRIP_CREATED', 'IN_PROGRESS'].includes(route.status) ? (
                      <Button size='sm' variant='outline' className='h-7 text-[10px] rounded-full border-slate-200 mt-2 font-semibold shadow-none' asChild>
                        <Link href='/school-bus/attendance'>
                          Open attendance
                        </Link>
                      </Button>
                    ) : null
                  }
                />

                {/* Step 5: Complete Journey */}
                <WorkflowStep
                  stepNumber={5}
                  title="Complete Trip"
                  description="Mark trip execution as finished."
                  status={
                    route.status === 'COMPLETED'
                      ? 'completed'
                      : route.status === 'IN_PROGRESS'
                        ? 'current'
                        : 'locked'
                  }
                />
              </div>
            </div>

            {/* Calculation Trace Snapshot & History */}
            <div className='bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-4'>
              <div className='flex items-center justify-between pb-2.5 border-b border-slate-100'>
                <div className='flex items-center gap-2'>
                  <Activity className='h-4.5 w-4.5 text-[#C81E3A] shrink-0' />
                  <h3 className='font-bold text-slate-900 text-sm'>Routing Calculation Trace</h3>
                </div>
                {latestTraceData?.data ? (
                  <Badge variant='outline' className='bg-slate-50 border-slate-200 text-slate-600 font-semibold px-2 py-0.5 text-[10px]'>
                    Latest: #{latestTraceData.data.id}
                  </Badge>
                ) : (
                  <Badge variant='outline' className='bg-slate-50 border-slate-200 text-slate-500 font-semibold px-2 py-0.5 text-[10px]'>
                    No trace found
                  </Badge>
                )}
              </div>

              {latestTraceData?.data ? (
                <div className='space-y-4'>
                  {/* Latest Trace Info Grid */}
                  <div className='grid grid-cols-2 gap-3 bg-slate-50 p-3.5 rounded-xl border border-slate-100 text-xs'>
                    <div className='space-y-1'>
                      <span className='text-slate-400 font-medium block'>Type / Method</span>
                      <span className='font-semibold text-slate-800 break-words'>{latestTraceData.data.calculationType}</span>
                    </div>
                    <div className='space-y-1'>
                      <span className='text-slate-400 font-medium block'>Provider Source</span>
                      <span className='font-semibold text-slate-800'>{latestTraceData.data.sourceSummary || 'N/A'}</span>
                    </div>
                    <div className='space-y-1'>
                      <span className='text-slate-400 font-medium block'>Status</span>
                      <span className={cn(
                        'inline-flex items-center gap-1 font-bold rounded px-1.5 py-0.5 text-[10px]',
                        latestTraceData.data.calculationStatus === 'SUCCESS' 
                          ? 'bg-emerald-50 text-emerald-700 border border-emerald-100' 
                          : 'bg-rose-50 text-rose-700 border border-rose-100'
                      )}>
                        {latestTraceData.data.calculationStatus}
                      </span>
                    </div>
                    <div className='space-y-1'>
                      <span className='text-slate-400 font-medium block'>Computed At</span>
                      <span className='font-medium text-slate-700'>{formatDateTime(latestTraceData.data.createdAt)}</span>
                    </div>
                    <div className='space-y-1 col-span-2 pt-1 border-t border-slate-200/60 flex justify-between items-center'>
                      <span className='text-slate-400 font-medium'>Issues / Violations</span>
                      <div className='flex gap-1.5'>
                        <span className={cn(
                          'px-1.5 py-0.5 rounded text-[10px] font-bold border',
                          (latestTraceData.data.blockingIssueCount ?? 0) > 0 
                            ? 'bg-rose-50 text-rose-700 border-rose-150' 
                            : 'bg-slate-100 text-slate-600 border-slate-150'
                        )}>
                          {latestTraceData.data.blockingIssueCount ?? 0} Blocking
                        </span>
                        <span className={cn(
                          'px-1.5 py-0.5 rounded text-[10px] font-bold border',
                          (latestTraceData.data.issueCount ?? 0) > 0 
                            ? 'bg-amber-50 text-amber-700 border-amber-150' 
                            : 'bg-slate-100 text-slate-600 border-slate-150'
                        )}>
                          {latestTraceData.data.issueCount ?? 0} Warning
                        </span>
                      </div>
                    </div>
                  </div>

                  {/* Actions for Latest Trace */}
                  <div className='flex gap-2'>
                    <Button 
                      size='sm' 
                      variant='outline'
                      className='flex-1 h-9 rounded-xl font-semibold border-slate-200 text-slate-700 hover:bg-slate-50 hover:text-slate-900 shadow-sm'
                      onClick={() => {
                        setSelectedTraceId(latestTraceData.data.id);
                        setActiveTab('overview');
                        setIsDetailOpen(true);
                      }}
                    >
                      <FileText className='h-3.5 w-3.5 mr-1.5 text-slate-500' />
                      View Trace
                    </Button>
                    <Button 
                      size='sm' 
                      variant='outline'
                      className='h-9 px-3 rounded-xl border-slate-200 text-slate-700 hover:bg-slate-50 hover:text-slate-900 shadow-sm'
                      onClick={handleDownloadLatestExcel}
                      disabled={exportingLatest}
                    >
                      <Download className='h-3.5 w-3.5 text-slate-500' />
                    </Button>
                  </div>
                </div>
              ) : (
                <div className='text-center py-6 bg-slate-50/50 rounded-2xl border border-dashed border-slate-200 p-5 space-y-2'>
                  <div className='inline-flex h-9 w-9 items-center justify-center rounded-full bg-slate-100 text-slate-400 mx-auto'>
                    <Info className='h-4.5 w-4.5' />
                  </div>
                  <span className='text-slate-750 text-xs font-bold block'>
                    No routing calculation trace available yet
                  </span>
                  <span className='text-slate-400 text-[10px] block max-w-[280px] mx-auto leading-normal'>
                    Recompute path to generate the initial geometry, stops, and calculation trace.
                  </span>
                </div>
              )}

              {/* History Section Collapsible */}
              {traceHistoryData?.data && traceHistoryData.data.length > 0 && (
                <div className='pt-2 border-t border-slate-100'>
                  <Accordion type='single' collapsible className='w-full'>
                    <AccordionItem value='history-list' className='border-0'>
                      <AccordionTrigger className='py-2 hover:no-underline text-xs font-bold text-slate-500 hover:text-slate-800 flex justify-between'>
                        <span className='flex items-center gap-1.5'>
                          <History className='h-3.5 w-3.5 text-slate-400' />
                          Calculation History ({traceHistoryData.data.length})
                        </span>
                      </AccordionTrigger>
                      <AccordionContent className='pt-2 pb-0'>
                        <div className='max-h-[220px] overflow-y-auto pr-1 space-y-2 divide-y divide-slate-100/80'>
                          {traceHistoryData.data.map((trace) => (
                            <div key={trace.id} className='pt-2 first:pt-0 flex items-center justify-between text-xs'>
                              <div className='space-y-0.5'>
                                <div className='flex items-center gap-1.5'>
                                  <span className='font-bold text-slate-700'>#{trace.id}</span>
                                  <span className='text-[10px] text-slate-400'>{formatDateTime(trace.createdAt)}</span>
                                </div>
                                <div className='flex items-center gap-2 text-[10px] text-slate-500'>
                                  <span className='font-semibold bg-slate-100 px-1.5 py-0.2 rounded text-slate-600'>{trace.calculationType}</span>
                                  <span className='text-slate-400'>•</span>
                                  <span className={cn(
                                    (trace.blockingIssueCount ?? 0) > 0 ? 'text-rose-600 font-semibold' : 'text-slate-400'
                                  )}>
                                    {trace.blockingIssueCount ?? 0} blk / {trace.issueCount ?? 0} wrn
                                  </span>
                                </div>
                              </div>
                              <div className='flex items-center gap-1.5 shrink-0'>
                                <Button
                                  variant='ghost'
                                  size='sm'
                                  className='h-7 w-7 p-0 rounded-lg text-slate-500 hover:text-[#C81E3A] hover:bg-slate-50'
                                  onClick={() => {
                                    setSelectedTraceId(trace.id);
                                    setActiveTab('overview');
                                    setIsDetailOpen(true);
                                  }}
                                >
                                  <FileText className='h-3.5 w-3.5' />
                                </Button>
                                <Button
                                  variant='ghost'
                                  size='sm'
                                  className='h-7 w-7 p-0 rounded-lg text-slate-500 hover:text-[#C81E3A] hover:bg-slate-50'
                                  onClick={() => handleDownloadSpecificExcel(trace.id)}
                                  disabled={exportingSpecific}
                                >
                                  <Download className='h-3.5 w-3.5' />
                                </Button>
                              </div>
                            </div>
                          ))}
                        </div>
                      </AccordionContent>
                    </AccordionItem>
                  </Accordion>
                </div>
              )}
            </div>

          </div>

          {/* Right Column */}
          <div className='flex flex-col gap-6 lg:sticky lg:top-6 lg:self-start'>
            
            {/* Route Map Card with Workspace Wrapper */}
            <div className='bg-white border border-slate-200 rounded-2xl overflow-hidden shadow-sm flex flex-col'>
              <div className='p-4 border-b border-slate-100 flex items-center justify-between'>
                <h3 className='font-bold text-slate-900 text-sm flex items-center gap-2'>
                  <MapPinned className='h-4.5 w-4.5 text-indigo-600' />
                  Route Map
                </h3>
                {missingStopCoordinates > 0 && (
                  <span className='rounded bg-amber-50 border border-amber-100 text-amber-700 font-bold text-[9px] px-1.5 py-0.5 animate-pulse'>
                    ⚠ {missingStopCoordinates} stop(s) missing coords
                  </span>
                )}
              </div>
              <div className='w-full bg-slate-50'>
                <SchoolBusMapWorkspace
                  flat
                  mapHeightClassName='h-[420px]'
                  map={
                    <RouteMap
                      route={detail.route}
                      stops={detail.stops}
                      assignment={detail.assignment}
                      routePath={routePathData?.data}
                      className='h-full w-full'
                      fitKey={fitKey}
                    />
                  }
                  legend={<SchoolBusMapLegend />}
                  onFitAll={() => setFitKey((k) => k + 1)}
                  canFitAll={true}
                />
              </div>
            </div>

            {/* Stop sequence timeline */}
            <div className='bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-4'>
              <div className='flex items-center justify-between pb-2.5 border-b border-slate-100'>
                <div className='flex items-center gap-2'>
                  <Route className='h-4.5 w-4.5 text-indigo-600 shrink-0' />
                  <h3 className='font-bold text-slate-900 text-sm'>Stop Sequence Plan</h3>
                </div>
                <span className='text-[10px] text-slate-500 font-bold bg-slate-50 border border-slate-150 px-2 py-0.5 rounded-full'>{detail.stops.length} location(s)</span>
              </div>

              {detail.stops.length === 0 ? (
                <SchoolBusEmptyState
                  title='No stops generated yet'
                  description='Generate a route plan to materialize the stop sequence.'
                  icon={Route}
                />
              ) : (
                <div className='relative pl-3 space-y-4 before:absolute before:left-[11px] before:top-2 before:bottom-2 before:w-[2px] before:bg-slate-100'>
                  {detail.stops.map((stop) => {
                    const isStart = stop.stopPurpose === 'START_TERMINAL';
                    const isEnd = stop.stopPurpose === 'END_TERMINAL';
                    const isDropoff = stop.stopPurpose === 'DROPOFF';
                    
                    return (
                      <div key={stop.id} className='relative flex items-start gap-4 text-xs'>
                        {/* Node */}
                        <span className={cn(
                          'absolute -left-[20px] top-1 flex h-[22px] w-[22px] items-center justify-center rounded-full text-[9px] font-extrabold ring-4 ring-white border shadow-sm',
                          isStart ? 'bg-orange-500 border-orange-655 text-white' :
                          isEnd ? 'bg-red-500 border-red-655 text-white' :
                          isDropoff ? 'bg-emerald-500 border-emerald-655 text-white' :
                          'bg-blue-500 border-blue-655 text-white'
                        )}>
                          {isStart ? 'S' : isEnd ? 'E' : stop.stopOrder}
                        </span>

                        <div className='min-w-0 flex-1 p-3 rounded-xl bg-slate-50/50 border border-slate-100 flex items-center justify-between gap-3'>
                          <div className='min-w-0'>
                            <p className='font-bold text-slate-800 truncate'>
                              {stop.displayName ?? stop.pickupPointName ?? `Stop #${stop.id}`}
                            </p>
                            <p className='text-[10px] text-slate-450 mt-1 flex items-center gap-1.5'>
                              <span className={cn(
                                'inline-flex items-center rounded px-1.5 py-0.5 text-[8px] font-extrabold border uppercase tracking-wider',
                                isStart ? 'bg-orange-50 text-orange-700 border-orange-100' :
                                isEnd ? 'bg-red-50 text-red-700 border-red-100' :
                                isDropoff ? 'bg-emerald-50 text-emerald-700 border-emerald-100' :
                                'bg-blue-50 text-blue-700 border-blue-100'
                              )}>
                                {isStart ? 'Depot' : isEnd ? 'School' : isDropoff ? 'Drop-off' : 'Pickup'}
                              </span>
                              <span>•</span>
                              <span>{stop.estimatedStudentCount ?? 0} student(s)</span>
                            </p>
                          </div>
                          
                          <div className='text-right text-[10px] shrink-0 font-bold text-slate-500 bg-white border border-slate-150 px-2.5 py-1.5 rounded-lg shadow-sm leading-tight'>
                            <div>Arr: {stop.plannedArrivalTime || '—'}</div>
                            <div className='mt-0.5 border-t border-slate-100 pt-0.5'>Dep: {stop.plannedDepartureTime || '—'}</div>
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>

          </div>

        </div>
      </SchoolBusPageShell>

      <RouteAssignmentDialog
        open={assignmentOpen}
        onOpenChange={setAssignmentOpen}
        initialData={detail.assignment}
        buses={getPageItems(busesData?.data)}
        drivers={getPageItems(driversData?.data)}
        attendants={getPageItems(attendantsData?.data)}
        onSubmit={handleAssign}
        isLoading={assigning}
      />

      <Sheet open={isDetailOpen} onOpenChange={setIsDetailOpen}>
        <SheetContent className="sm:max-w-3xl w-full md:w-3/4 flex flex-col h-full bg-slate-50 border-l border-slate-200">
          <SheetHeader className="bg-white border-b border-slate-100 p-5 shrink-0">
            <div className="flex items-center justify-between">
              <div className="space-y-1">
                <SheetTitle className="text-lg font-bold text-slate-900 flex items-center gap-2">
                  <Activity className="h-5 w-5 text-[#C81E3A]" />
                  Calculation Trace Detail
                </SheetTitle>
                <SheetDescription className="text-xs text-slate-500">
                  Audit logs, matrix results, and feasibility diagnostics for Trace ID #{selectedTrace?.id}
                </SheetDescription>
              </div>
              {selectedTrace && (
                <Button 
                  size="sm" 
                  variant="outline" 
                  className="rounded-xl border-slate-200 text-xs font-semibold mr-6 hover:bg-slate-50 hover:text-slate-900 shadow-sm"
                  onClick={() => handleDownloadSpecificExcel(selectedTrace.id)}
                  disabled={exportingSpecific}
                >
                  <Download className="h-3.5 w-3.5 mr-1.5 text-slate-550" />
                  Export Excel
                </Button>
              )}
            </div>
          </SheetHeader>

          {selectedTrace ? (
            <Tabs value={activeTab} onValueChange={setActiveTab} className="flex-1 flex flex-col min-h-0 bg-white">
              <div className="px-5 border-b border-slate-100 shrink-0 bg-white">
                <TabsList className="bg-slate-100/80 border border-slate-200/40 flex overflow-x-auto whitespace-nowrap p-1 gap-1 h-11 w-full justify-start rounded-xl">
                  <TabsTrigger value="overview" className="rounded-lg text-xs py-1.5 px-3 font-semibold">Overview</TabsTrigger>
                  <TabsTrigger value="points" className="rounded-lg text-xs py-1.5 px-3 font-semibold">Matrix Points</TabsTrigger>
                  <TabsTrigger value="timeline" className="rounded-lg text-xs py-1.5 px-3 font-semibold">Stop Timeline</TabsTrigger>
                  <TabsTrigger value="issues" className="rounded-lg text-xs py-1.5 px-3 font-semibold flex items-center gap-1.5">
                    Issues
                    {((selectedTrace.issueCount ?? 0) > 0 || (selectedTrace.blockingIssueCount ?? 0) > 0) && (
                      <span className={cn(
                        "px-1.5 py-0.2 rounded-full text-[9px] font-bold",
                        (selectedTrace.blockingIssueCount ?? 0) > 0 ? "bg-rose-100 text-rose-800" : "bg-amber-100 text-amber-800"
                      )}>
                        {(selectedTrace.blockingIssueCount ?? 0) + (selectedTrace.issueCount ?? 0)}
                      </span>
                    )}
                  </TabsTrigger>
                  <TabsTrigger value="config" className="rounded-lg text-xs py-1.5 px-3 font-semibold">Config</TabsTrigger>
                  <TabsTrigger value="raw" className="rounded-lg text-xs py-1.5 px-3 font-semibold">Raw JSON</TabsTrigger>
                </TabsList>
              </div>

              <div className="flex-1 min-h-0 overflow-hidden bg-slate-50/40">
                {/* 1. OVERVIEW TAB */}
                <TabsContent value="overview" className="h-full m-0 p-5 overflow-y-auto">
                  <div className="space-y-5">
                    {/* Basic Grid */}
                    <div className="bg-white border border-slate-200/80 rounded-2xl p-5 shadow-sm space-y-4">
                      <h4 className="font-bold text-slate-800 text-sm border-b border-slate-100 pb-2.5">Metadata Information</h4>
                      <div className="grid grid-cols-2 md:grid-cols-3 gap-y-4 gap-x-6 text-xs">
                        <div className="space-y-1">
                          <span className="text-slate-400 font-medium">Trace ID</span>
                          <p className="font-bold text-slate-900">#{selectedTrace.id}</p>
                        </div>
                        <div className="space-y-1">
                          <span className="text-slate-400 font-medium">Calculation Type</span>
                          <p className="font-bold text-slate-900">{selectedTrace.calculationType}</p>
                        </div>
                        <div className="space-y-1">
                          <span className="text-slate-400 font-medium">Status</span>
                          <div>
                            <Badge className={cn(
                              "font-bold text-[10px] shadow-none uppercase px-2 py-0.5",
                              selectedTrace.calculationStatus === 'SUCCESS' 
                                ? 'bg-emerald-50 text-emerald-700 border border-emerald-200' 
                                : 'bg-rose-50 text-rose-700 border border-rose-200'
                            )}>
                              {selectedTrace.calculationStatus}
                            </Badge>
                          </div>
                        </div>
                        <div className="space-y-1">
                          <span className="text-slate-400 font-medium">Created At</span>
                          <p className="font-bold text-slate-800">{formatDateTime(selectedTrace.createdAt)}</p>
                        </div>
                        <div className="space-y-1">
                          <span className="text-slate-400 font-medium">Created By</span>
                          <p className="font-bold text-slate-800">{selectedTrace.createdBy || 'System'}</p>
                        </div>
                        <div className="space-y-1">
                          <span className="text-slate-400 font-medium">Matrix Source</span>
                          <p className="font-bold text-slate-800">{selectedTrace.sourceSummary || 'N/A'}</p>
                        </div>
                      </div>
                    </div>

                    {/* Stats summary banner */}
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                      <div className="bg-white border border-slate-200/80 rounded-2xl p-4 shadow-sm text-center">
                        <span className="text-[10px] font-bold text-slate-400 uppercase">Total Stops</span>
                        <p className="text-xl font-extrabold text-slate-900 mt-1">
                          {parsedTimeline ? parsedTimeline.length : parsedInput?.stops?.length || 'N/A'}
                        </p>
                      </div>
                      <div className="bg-white border border-slate-200/80 rounded-2xl p-4 shadow-sm text-center">
                        <span className="text-[10px] font-bold text-slate-400 uppercase">Routing Provider</span>
                        <p className="text-sm font-extrabold text-slate-800 mt-2">
                          {parsedConfig?.routingProvider || 'OSRM'}
                        </p>
                      </div>
                      <div className="bg-white border border-slate-200/80 rounded-2xl p-4 shadow-sm text-center">
                        <span className="text-[10px] font-bold text-slate-400 uppercase">Blocking Issues</span>
                        <p className={cn(
                          "text-xl font-extrabold mt-1",
                          (selectedTrace.blockingIssueCount ?? 0) > 0 ? "text-rose-600 animate-pulse" : "text-emerald-600"
                        )}>
                          {selectedTrace.blockingIssueCount ?? 0}
                        </p>
                      </div>
                      <div className="bg-white border border-slate-200/80 rounded-2xl p-4 shadow-sm text-center">
                        <span className="text-[10px] font-bold text-slate-400 uppercase">Warnings</span>
                        <p className={cn(
                          "text-xl font-extrabold mt-1",
                          (selectedTrace.issueCount ?? 0) > 0 ? "text-amber-600" : "text-slate-500"
                        )}>
                          {selectedTrace.issueCount ?? 0}
                        </p>
                      </div>
                    </div>

                    {/* Timeline summary card */}
                    {parsedTimeline && parsedTimeline.length > 0 && (
                      <div className="bg-white border border-slate-200/80 rounded-2xl p-5 shadow-sm space-y-3">
                        <h4 className="font-bold text-slate-800 text-sm">Journey Timeline Snapshot</h4>
                        <div className="relative border-l border-slate-200 pl-4 ml-2.5 space-y-4 py-2">
                          {parsedTimeline.slice(0, 3).map((stop: any, idx: number) => (
                            <div key={idx} className="relative text-xs">
                              <span className="absolute -left-[22px] top-1.5 flex h-2.5 w-2.5 items-center justify-center rounded-full bg-[#C81E3A] ring-4 ring-white" />
                              <div className="flex justify-between font-bold text-slate-700">
                                <span>{stop.displayName}</span>
                                <span className="text-[#C81E3A] font-semibold">Arr: {stop.plannedArrivalTime || 'N/A'}</span>
                              </div>
                              <p className="text-[10px] text-slate-400 mt-0.5">{stop.stopPurpose} {stop.waitMinutes > 0 && `• Wait: ${stop.waitMinutes} mins`}</p>
                            </div>
                          ))}
                          {parsedTimeline.length > 3 && (
                            <div className="text-[11px] text-slate-400 italic pt-1">
                              ...and {parsedTimeline.length - 3} more stop(s). Select Stop Timeline tab to view full details.
                            </div>
                          )}
                        </div>
                      </div>
                    )}
                  </div>
                </TabsContent>

                {/* 2. MATRIX POINTS TAB */}
                <TabsContent value="points" className="h-full m-0 p-5 overflow-y-auto">
                  <div className="bg-white border border-slate-200/80 rounded-2xl overflow-hidden shadow-sm">
                    <div className="p-4 border-b border-slate-100 bg-white">
                      <h4 className="font-bold text-slate-800 text-sm">Matrix Points & Coordinates</h4>
                      <p className="text-[10px] text-slate-400 mt-0.5">List of unique locations resolved for distance matrix computations</p>
                    </div>
                    {parsedMatrix?.points && parsedMatrix.points.length > 0 ? (
                      <Table className="w-full">
                        <TableHeader>
                          <TableRow className="bg-slate-50/70 border-b border-slate-150">
                            <TableHead className="w-[80px] font-bold text-slate-700 text-xs">Index</TableHead>
                            <TableHead className="font-bold text-slate-700 text-xs">Point Name</TableHead>
                            <TableHead className="w-[120px] font-bold text-slate-700 text-xs text-center">Latitude</TableHead>
                            <TableHead className="w-[120px] font-bold text-slate-700 text-xs text-center">Longitude</TableHead>
                          </TableRow>
                        </TableHeader>
                        <TableBody>
                          {parsedMatrix.points.map((point: any, idx: number) => (
                            <TableRow key={idx} className="border-b border-slate-100 hover:bg-slate-50/50">
                              <TableCell className="font-bold text-slate-500 text-xs">{idx + 1}</TableCell>
                              <TableCell className="font-semibold text-slate-800 text-xs truncate max-w-[320px]">
                                {point.name || `Point ${idx + 1}`}
                              </TableCell>
                              <TableCell className="text-center font-medium text-slate-600 text-xs">
                                {point.latitude?.toFixed(6) || point.lat?.toFixed(6) || 'N/A'}
                              </TableCell>
                              <TableCell className="text-center font-medium text-slate-600 text-xs">
                                {point.longitude?.toFixed(6) || point.lng?.toFixed(6) || 'N/A'}
                              </TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    ) : parsedInput?.stops && parsedInput.stops.length > 0 ? (
                      <Table className="w-full">
                        <TableHeader>
                          <TableRow className="bg-slate-50/70 border-b border-slate-150">
                            <TableHead className="w-[80px] font-bold text-slate-700 text-xs font-semibold">Index</TableHead>
                            <TableHead className="font-bold text-slate-700 text-xs font-semibold">Stop Name</TableHead>
                            <TableHead className="w-[120px] font-bold text-slate-700 text-xs text-center font-semibold">Purpose</TableHead>
                            <TableHead className="w-[150px] font-bold text-slate-700 text-xs text-center font-semibold">Coordinates</TableHead>
                          </TableRow>
                        </TableHeader>
                        <TableBody>
                          {parsedInput.stops.map((stop: any, idx: number) => (
                            <TableRow key={idx} className="border-b border-slate-100 hover:bg-slate-50/50">
                              <TableCell className="font-bold text-slate-505 text-xs">{idx + 1}</TableCell>
                              <TableCell className="font-semibold text-slate-800 text-xs truncate max-w-[320px]">
                                {stop.pickupPointName || stop.name || 'N/A'}
                              </TableCell>
                              <TableCell className="text-center text-xs">
                                <Badge variant="outline" className="text-[10px] font-medium uppercase py-0 text-slate-500 bg-slate-50">
                                  {stop.stopPurpose || 'N/A'}
                                </Badge>
                              </TableCell>
                              <TableCell className="text-center font-medium text-slate-600 text-xs">
                                {stop.latitude && stop.longitude ? `${stop.latitude.toFixed(6)}, ${stop.longitude.toFixed(6)}` : 'N/A'}
                              </TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    ) : (
                      <div className="p-8 text-center text-slate-400 text-xs italic">
                        No matrix point or input stop data found in this trace snapshot.
                      </div>
                    )}
                  </div>
                </TabsContent>

                {/* 3. STOP TIMELINE TAB */}
                <TabsContent value="timeline" className="h-full m-0 p-5 overflow-y-auto">
                  <div className="bg-white border border-slate-200/80 rounded-2xl overflow-hidden shadow-sm">
                    <div className="p-4 border-b border-slate-100 bg-white">
                      <h4 className="font-bold text-slate-800 text-sm">Calculated Stop Timeline</h4>
                      <p className="text-[10px] text-slate-400 mt-0.5">Sequential arrival & departure plan satisfying service windows</p>
                    </div>
                    {parsedTimeline && parsedTimeline.length > 0 ? (
                      <Table className="w-full">
                        <TableHeader>
                          <TableRow className="bg-slate-50/70 border-b border-slate-150">
                            <TableHead className="w-[60px] font-bold text-slate-700 text-xs text-center">Order</TableHead>
                            <TableHead className="font-bold text-slate-700 text-xs">Stop / Point</TableHead>
                            <TableHead className="w-[100px] font-bold text-slate-700 text-xs text-center">Arrival</TableHead>
                            <TableHead className="w-[100px] font-bold text-slate-700 text-xs text-center">Departure</TableHead>
                            <TableHead className="w-[90px] font-bold text-slate-700 text-xs text-center">Wait (m)</TableHead>
                            <TableHead className="w-[140px] font-bold text-slate-700 text-xs text-center">Window Target</TableHead>
                            <TableHead className="w-[100px] font-bold text-slate-700 text-xs text-center">Status</TableHead>
                          </TableRow>
                        </TableHeader>
                        <TableBody>
                          {parsedTimeline.map((stop: any, idx: number) => {
                            const isViolated = stop.windowSatisfied === false;
                            const hasWindow = stop.windowStart || stop.windowEnd;
                            return (
                              <TableRow key={idx} className="border-b border-slate-100 hover:bg-slate-50/50">
                                <TableCell className="font-extrabold text-slate-505 text-xs text-center">{stop.stopOrder ?? idx + 1}</TableCell>
                                <TableCell className="min-w-[180px] text-xs">
                                  <span className="font-bold text-slate-800 block">{stop.displayName}</span>
                                  <span className="text-[9px] text-slate-400 uppercase font-semibold">{stop.stopPurpose}</span>
                                </TableCell>
                                <TableCell className="text-center font-semibold text-slate-800 text-xs">{stop.plannedArrivalTime || 'N/A'}</TableCell>
                                <TableCell className="text-center font-semibold text-slate-800 text-xs">{stop.plannedDepartureTime || 'N/A'}</TableCell>
                                <TableCell className="text-center font-semibold text-slate-600 text-xs">{stop.waitMinutes ?? 0}</TableCell>
                                <TableCell className="text-center font-medium text-slate-505 text-[11px]">
                                  {hasWindow ? `${stop.windowStart || '00:00'} - ${stop.windowEnd || '23:59'}` : 'Anytime'}
                                </TableCell>
                                <TableCell className="text-center text-xs">
                                  {hasWindow ? (
                                    <Badge className={cn(
                                      "text-[9px] font-bold shadow-none uppercase px-1.5 py-0.2",
                                      isViolated 
                                        ? "bg-rose-50 text-rose-700 border border-rose-250" 
                                        : "bg-emerald-50 text-emerald-700 border border-emerald-250"
                                    )}>
                                      {isViolated ? "Violated" : "Satisfied"}
                                    </Badge>
                                  ) : (
                                    <span className="text-[10px] text-slate-400 font-medium">N/A</span>
                                  )}
                                </TableCell>
                              </TableRow>
                            );
                          })}
                        </TableBody>
                      </Table>
                    ) : (
                      <div className="p-8 text-center text-slate-400 text-xs italic">
                        No timeline stops found in this trace snapshot.
                      </div>
                    )}
                  </div>
                </TabsContent>

                {/* 4. ISSUES TAB */}
                <TabsContent value="issues" className="h-full m-0 p-5 overflow-y-auto">
                  <div className="space-y-4">
                    {parsedIssues && parsedIssues.length > 0 ? (
                      parsedIssues.map((issue: any, idx: number) => {
                        const isBlocking = issue.isBlocking || issue.blocking || issue.severity?.toUpperCase() === 'BLOCKING';
                        return (
                          <div 
                            key={idx} 
                            className={cn(
                              "border rounded-2xl p-4 flex gap-3 shadow-sm",
                              isBlocking 
                                ? "bg-rose-50/50 border-rose-200 text-rose-900" 
                                : "bg-amber-50/50 border-amber-250 text-amber-900"
                            )}
                          >
                            <AlertTriangle className={cn("h-5 w-5 shrink-0 mt-0.5", isBlocking ? "text-rose-600" : "text-amber-600")} />
                            <div className="space-y-1 text-xs">
                              <div className="flex items-center gap-2">
                                <span className={cn(
                                  "rounded px-1.5 py-0.2 text-[9px] font-bold uppercase",
                                  isBlocking ? "bg-rose-100 text-rose-800" : "bg-amber-100 text-amber-800"
                                )}>
                                  {isBlocking ? "Blocking Issue" : "Warning"}
                                </span>
                                {issue.errorCode && (
                                  <span className="font-semibold text-slate-650 bg-white/80 border px-1 rounded text-[10px]">
                                    {issue.errorCode}
                                  </span>
                                )}
                              </div>
                              <p className="font-bold mt-1 text-slate-900">{issue.description || issue.message || 'Unknown routing issue'}</p>
                              {issue.refId && (
                                <p className="text-[10px] text-slate-400">Reference ID: {issue.refId} ({issue.refType || 'Stop'})</p>
                              )}
                            </div>
                          </div>
                        );
                      })
                    ) : (
                      <div className="bg-emerald-50/50 border border-emerald-200 rounded-2xl p-6 text-center space-y-3">
                        <div className="inline-flex h-10 w-10 items-center justify-center rounded-full bg-emerald-100 text-emerald-600">
                          <CheckCircle2 className="h-6 w-6" />
                        </div>
                        <h5 className="font-bold text-emerald-950 text-sm">No Issues Found</h5>
                        <p className="text-xs text-emerald-800 max-w-sm mx-auto">
                          This route plan calculation trace satisfies all logistics constraints, including student time-in-transit limits and pickup window allocations.
                        </p>
                      </div>
                    )}
                  </div>
                </TabsContent>

                {/* 5. CONFIG TAB */}
                <TabsContent value="config" className="h-full m-0 p-5 overflow-y-auto">
                  <div className="bg-white border border-slate-200/80 rounded-2xl overflow-hidden shadow-sm">
                    <div className="p-4 border-b border-slate-100 bg-white">
                      <h4 className="font-bold text-slate-800 text-sm">Routing Engine Configuration</h4>
                      <p className="text-[10px] text-slate-400 mt-0.5">Parameters applied to the route solver at the time of computation</p>
                    </div>
                    {parsedConfig ? (
                      <Table className="w-full">
                        <TableHeader>
                          <TableRow className="bg-slate-50/70 border-b border-slate-150">
                            <TableHead className="font-bold text-slate-700 text-xs">Parameter Key</TableHead>
                            <TableHead className="font-bold text-slate-700 text-xs">Applied Value</TableHead>
                            <TableHead className="font-bold text-slate-700 text-xs">Description</TableHead>
                          </TableRow>
                        </TableHeader>
                        <TableBody>
                          {Object.entries(parsedConfig).map(([key, val]: [string, any], idx: number) => {
                            let valStr = String(val);
                            if (typeof val === 'boolean') valStr = val ? 'Enabled (True)' : 'Disabled (False)';
                            
                            // Human friendly descriptions
                            let desc = 'Routing algorithm constraint parameter';
                            if (key === 'routingProvider') desc = 'Calculates distance/duration matrices via online maps or spatial math';
                            else if (key === 'maxWalkingDistanceMeters') desc = 'Max distance (meters) a student walks to reach stops';
                            else if (key === 'maxStudentTransitTimeMinutes') desc = 'Max time (minutes) a student can spend riding the bus';
                            else if (key === 'speedMultiplier') desc = 'Adjusts average speeds based on traffic factor simulations';
                            else if (key === 'depotStart') desc = 'Requires routing paths to begin from the designated depot';
                            else if (key === 'depotEnd') desc = 'Requires routing paths to terminate at the designated depot';
                            
                            return (
                              <TableRow key={idx} className="border-b border-slate-100 hover:bg-slate-50/50">
                                <TableCell className="font-mono text-[11px] text-slate-655">{key}</TableCell>
                                <TableCell className="font-bold text-slate-800 text-xs">{valStr}</TableCell>
                                <TableCell className="text-slate-400 text-xs font-medium">{desc}</TableCell>
                              </TableRow>
                            );
                          })}
                        </TableBody>
                      </Table>
                    ) : (
                      <div className="p-8 text-center text-slate-400 text-xs italic">
                        No config snapshot parameters found in this trace.
                      </div>
                    )}
                  </div>
                </TabsContent>

                {/* 6. RAW JSON TAB */}
                <TabsContent value="raw" className="h-full m-0 p-5 overflow-y-auto">
                  <div className="space-y-4">
                    <div className="flex items-center justify-between bg-white p-3.5 border border-slate-200 rounded-2xl gap-3 shadow-sm">
                      <div className="flex items-center gap-3">
                        <span className="text-xs font-bold text-slate-500 flex items-center gap-1.5 shrink-0">
                          <Settings className="h-4 w-4 text-slate-400" />
                          Select JSON field:
                        </span>
                        <Select value={rawJsonType} onValueChange={setRawJsonType}>
                          <SelectTrigger className="w-[220px] h-9 text-xs font-semibold bg-slate-50 border border-slate-200 rounded-xl text-slate-800 focus:outline-none">
                            <SelectValue placeholder="Select JSON field" />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="full">Full Trace Entity</SelectItem>
                            <SelectItem value="matrix">Matrix JSON (N x N Matrix)</SelectItem>
                            <SelectItem value="timeline">Timeline JSON (Stop Timeline)</SelectItem>
                            <SelectItem value="issues">Issues JSON (Route Issues)</SelectItem>
                            <SelectItem value="config">Config Snapshot JSON</SelectItem>
                            <SelectItem value="input">Input JSON (Solver Input)</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>

                      <Button
                        size="sm"
                        variant="outline"
                        className="h-9 rounded-xl px-3.5 border-slate-200 text-slate-700 hover:bg-slate-50 hover:text-slate-900 font-semibold shadow-sm"
                        onClick={() => {
                          let displayObj: any = selectedTrace;
                          if (rawJsonType === 'matrix') displayObj = parsedMatrix;
                          else if (rawJsonType === 'timeline') displayObj = parsedTimeline;
                          else if (rawJsonType === 'issues') displayObj = parsedIssues;
                          else if (rawJsonType === 'config') displayObj = parsedConfig;
                          else if (rawJsonType === 'input') displayObj = parsedInput;

                          navigator.clipboard.writeText(JSON.stringify(displayObj, null, 2));
                          toast.success('JSON copied to clipboard');
                        }}
                      >
                        <Copy className="h-3.5 w-3.5 mr-1.5 text-slate-550" />
                        Copy JSON
                      </Button>
                    </div>

                    <ScrollArea className="h-[480px] w-full rounded-2xl border border-slate-200 bg-slate-50/50 shadow-inner">
                      <pre className="text-[11px] font-mono text-slate-800 p-5 overflow-x-auto leading-relaxed select-all">
                        {(() => {
                          let displayObj: any = selectedTrace;
                          if (rawJsonType === 'matrix') displayObj = parsedMatrix;
                          else if (rawJsonType === 'timeline') displayObj = parsedTimeline;
                          else if (rawJsonType === 'issues') displayObj = parsedIssues;
                          else if (rawJsonType === 'config') displayObj = parsedConfig;
                          else if (rawJsonType === 'input') displayObj = parsedInput;

                          return JSON.stringify(displayObj, null, 2);
                        })()}
                      </pre>
                    </ScrollArea>
                  </div>
                </TabsContent>
              </div>
            </Tabs>
          ) : (
            <div className="flex-1 flex items-center justify-center p-8 bg-slate-50 text-slate-400 text-xs italic">
              No trace selected.
            </div>
          )}
        </SheetContent>
      </Sheet>

      <RouteAssignmentDialog
        open={assignmentOpen}
        onOpenChange={setAssignmentOpen}
        initialData={detail.assignment}
        buses={getPageItems(busesData?.data)}
        drivers={getPageItems(driversData?.data)}
        attendants={getPageItems(attendantsData?.data)}
        onSubmit={handleAssign}
        isLoading={assigning}
      />
    </>
  );
}

function ReadinessCard({
  label,
  value,
  status,
}: {
  label: string;
  value: string;
  status: 'success' | 'warning' | 'muted' | 'danger';
}) {
  return (
    <div className='bg-white border border-slate-205 rounded-2xl p-4 shadow-sm flex flex-col justify-between h-[80px]'>
      <span className='text-[10px] font-bold text-slate-400 uppercase tracking-wider'>{label}</span>
      <span className={cn(
        'text-sm font-extrabold mt-1 inline-flex items-center gap-1.5',
        status === 'success' ? 'text-emerald-700' :
        status === 'warning' ? 'text-amber-700' :
        status === 'danger' ? 'text-rose-700' : 'text-slate-400'
      )}>
        {status === 'success' ? '✓ ' : 
         status === 'warning' ? '⚠ ' : 
         status === 'danger' ? '✗ ' : '🔒 '}
        {value}
      </span>
    </div>
  );
}

function SummaryItem({
  label,
  value,
  icon: Icon,
  isBadge = false,
  badgeColor = '',
}: {
  label: string;
  value: string | null | undefined;
  icon: any;
  isBadge?: boolean;
  badgeColor?: string;
}) {
  const displayVal = value || 'N/A';
  const isNa = displayVal === 'N/A';
  return (
    <div className='flex items-start gap-2.5 min-w-0'>
      <div className='flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-slate-50 text-slate-400 mt-0.5 border border-slate-100'>
        <Icon className='h-3.5 w-3.5' />
      </div>
      <div className='min-w-0 flex-1'>
        <p className='text-[10px] font-semibold text-slate-400 leading-none'>{label}</p>
        {isBadge && !isNa ? (
          <span className={cn('inline-flex items-center rounded px-1.5 py-0.5 text-[10px] font-bold border mt-1', badgeColor)}>
            {displayVal}
          </span>
        ) : (
          <p className={cn('font-bold text-slate-705 truncate mt-1 text-[11px]', isNa && 'text-slate-400 italic font-medium')}>{displayVal}</p>
        )}
      </div>
    </div>
  );
}

function AssignmentRow({
  label,
  value,
  type,
}: {
  label: string;
  value: string | null | undefined;
  type: 'bus' | 'driver' | 'attendant';
}) {
  const Icon = type === 'bus' ? BusFront : type === 'driver' ? UserCog : Users;
  const isMissing = !value;
  return (
    <div className={cn(
      'flex items-center justify-between p-3.5 rounded-xl border transition-all duration-155',
      isMissing
        ? 'bg-amber-50/20 border-amber-100 text-amber-800'
        : 'bg-slate-50/50 border-slate-100 text-slate-800'
    )}>
      <div className='flex items-center gap-3.5 min-w-0'>
        <div className={cn(
          'flex h-8 w-8 shrink-0 items-center justify-center rounded-full border shadow-sm',
          isMissing
            ? 'bg-amber-50 text-amber-500 border-amber-100'
            : 'bg-indigo-50 text-indigo-650 border-indigo-100'
        )}>
          <Icon className='h-4 w-4' />
        </div>
        <div className='min-w-0'>
          <p className='text-[10px] font-semibold text-slate-405 leading-none'>{label}</p>
          <p className={cn('font-bold text-slate-800 mt-1 truncate text-xs', isMissing && 'text-amber-700 italic font-medium')}>
            {value || `Missing ${type}`}
          </p>
        </div>
      </div>
      <span className={cn(
        'rounded-full px-2.5 py-0.5 text-[9px] font-extrabold border shadow-none shrink-0 uppercase tracking-wider',
        isMissing
          ? 'bg-amber-50 text-amber-750 border-amber-200'
          : 'bg-emerald-50 text-emerald-700 border-emerald-200'
      )}>
        {isMissing ? 'Missing' : 'Assigned'}
      </span>
    </div>
  );
}

function WorkflowStep({
  stepNumber,
  title,
  description,
  status,
  action,
}: {
  stepNumber: number;
  title: string;
  description: string;
  status: 'completed' | 'current' | 'available' | 'locked';
  action?: React.ReactNode;
}) {
  return (
    <div className='relative flex items-start gap-4'>
      <span className={cn(
        'z-10 flex h-6 w-6 shrink-0 items-center justify-center rounded-full text-xs font-bold ring-4 ring-white border shadow-sm',
        status === 'completed' ? 'bg-emerald-500 border-emerald-600 text-white' :
        status === 'current' ? 'bg-[#C81E3A] border-[#C81E3A] text-white animate-pulse' :
        status === 'available' ? 'bg-indigo-50 border-indigo-400 text-indigo-755' :
        'bg-slate-105 border-slate-200 text-slate-400'
      )}>
        {status === 'completed' ? '✓' : stepNumber}
      </span>
      
      <div className='min-w-0 flex-1 bg-slate-50/30 border border-slate-100/60 rounded-xl p-3.5 shadow-sm'>
        <p className={cn(
          'text-xs font-bold leading-none',
          status === 'completed' ? 'text-slate-800' :
          status === 'current' ? 'text-[#C81E3A]' :
          status === 'available' ? 'text-indigo-950 font-bold' :
          'text-slate-400'
        )}>
          {title}
        </p>
        <p className='text-[10px] text-slate-500 mt-1.5 leading-relaxed'>{description}</p>
        {action}
      </div>
    </div>
  );
}
