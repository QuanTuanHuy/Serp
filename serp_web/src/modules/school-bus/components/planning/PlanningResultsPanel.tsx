'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { ChevronUp, ChevronDown, Trash2, AlertTriangle, Info, Ban, MapPin, Users, Plus, Route, Clock, Calendar, AlertCircle, Loader2, Navigation, Sparkles } from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { formatDate } from '../../utils';
import { SchoolBusConfirmDialog, useSchoolBusConfirm } from '../SchoolBusConfirmDialog';
import { schoolBusUi } from '../../theme';
import { ManualDemandAssignPanel } from './ManualDemandAssignPanel';
import { SchoolBusSelect } from '../ui/SchoolBusSelect';
import {
  useGetRouteByIdQuery,
  useReorderRouteStopsMutation,
  useRemoveRouteStopMutation,
  useRemoveRouteStudentMutation,
  useGetDepotsQuery,
  useGetBusesQuery,
  useGetSchoolByIdQuery,
  useDeleteRouteInSessionMutation,
  useGreedyFillRouteMutation,
} from '../../api/schoolBusApi';
import type {
  SchoolBusPlanningPreview,
  SchoolBusPlanningSession,
  SchoolBusRoute,
  SchoolBusEligibleStudent,
  CreateRouteInSessionRequest,
} from '../../types';

/* ── Create Route Dialog ──────────────────────────────────────────────── */

type LocationType = 'SCHOOL' | 'DEPOT';
type DirectionType = 'OUTBOUND' | 'RETURN';

interface CreateRouteFormState {
  routeName: string;
  routeDirection: DirectionType;
  startLocationType: LocationType;
  startDepotId: number | '';
  endLocationType: LocationType;
  endDepotId: number | '';
  busId: number | '';
  planningNotes: string;
}

function CreateRoutePanel({
  session,
  onSubmit,
  submitting,
}: {
  session: SchoolBusPlanningSession;
  onSubmit: (req: CreateRouteInSessionRequest) => void;
  submitting?: boolean;
}) {
  const [open, setOpen] = useState(false);

  const initDir = (session.routeDirection as DirectionType) ?? 'OUTBOUND';
  const [form, setForm] = useState<CreateRouteFormState>({
    routeName: '',
    routeDirection: initDir,
    startLocationType: initDir === 'OUTBOUND' ? 'DEPOT' : 'SCHOOL',
    startDepotId: '',
    endLocationType: initDir === 'OUTBOUND' ? 'SCHOOL' : 'DEPOT',
    endDepotId: '',
    busId: '',
    planningNotes: '',
  });

  useEffect(() => {
    if (initDir === 'OUTBOUND') {
      setForm(p => ({
        ...p,
        routeDirection: initDir,
        startLocationType: 'DEPOT',
        endLocationType: 'SCHOOL',
        endDepotId: ''
      }));
    } else {
      setForm(p => ({
        ...p,
        routeDirection: initDir,
        startLocationType: 'SCHOOL',
        endLocationType: 'DEPOT',
        startDepotId: '',
      }));
    }
  }, [initDir]);

  // Fetch depots for selectors
  const { data: depotsData } = useGetDepotsQuery({ page: 0, size: 100 });
  const depots = depotsData?.data?.items ?? [];
  const hasDepots = depots.length > 0;
  const selectedDepotId = initDir === 'OUTBOUND' ? form.startDepotId : form.endDepotId;
  const { data: busesData, isFetching: loadingBuses } = useGetBusesQuery(
    selectedDepotId ? { page: 0, size: 100, depotId: Number(selectedDepotId), sortBy: 'plateNumber' } : undefined,
    { skip: !selectedDepotId }
  );
  const buses = busesData?.data?.items ?? [];
  const selectedBus = buses.find(b => Number(b.id) === Number(form.busId));

  const needsStartDepot = form.startLocationType === 'DEPOT';
  const needsEndDepot   = form.endLocationType   === 'DEPOT';

  const canSubmit =
    form.routeName.trim() !== '' &&
    (!needsStartDepot || form.startDepotId !== '') &&
    (!needsEndDepot || form.endDepotId !== '') &&
    form.busId !== '';

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!canSubmit) return;
    const req: CreateRouteInSessionRequest = {
      schoolId: session.schoolId,
      routeDirection: form.routeDirection,
      startLocationType: form.startLocationType,
      endLocationType: form.endLocationType,
      routeName: form.routeName,
      serviceDate: session.serviceDate,
      busId: Number(form.busId),
      planningNotes: form.planningNotes || undefined,
    };
    if (form.startLocationType === 'SCHOOL') req.startSchoolId = session.schoolId;
    else req.startDepotId = Number(form.startDepotId);
    if (form.endLocationType === 'SCHOOL') req.endSchoolId = session.schoolId;
    else req.endDepotId = Number(form.endDepotId);
    onSubmit(req);
    setOpen(false);
    setForm(p => ({ ...p, routeName: '', startDepotId: '', endDepotId: '', busId: '', planningNotes: '' }));
  };

  const inputCls = 'w-full rounded-xl border border-slate-200 bg-white px-3 py-1.5 text-xs text-slate-900 shadow-sm focus:outline-none focus:ring-2 focus:ring-slate-900/10 focus:border-slate-900 transition-all';
  const labelCls = 'block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1';

  return (
    <div className='mb-4'>
      <Button
        size='sm'
        variant='outline'
        onClick={() => setOpen(v => !v)}
        className='w-full rounded-full border-red-250 text-[#C81E3A] hover:bg-[#FDECEF]/50 gap-1.5 text-xs font-semibold'
      >
        <Plus className='h-3.5 w-3.5' />
        {open ? 'Cancel Route Builder' : 'Open Route Builder'}
      </Button>
      {open && (
        <form onSubmit={handleSubmit} className='mt-3 rounded-2xl border border-slate-200 bg-slate-50/50 p-4 space-y-4 shadow-sm'>
          <div className='border-b border-slate-100 pb-1.5'>
            <h4 className='text-xs font-bold text-slate-700 uppercase tracking-wide'>Route Builder</h4>
          </div>

          {/* Group 1: Route identity */}
          <div className='space-y-3 p-3 bg-white rounded-xl border border-slate-100 shadow-sm'>
            <p className='text-[10px] font-bold text-slate-400 uppercase tracking-wider leading-none'>1. Route Identity</p>
            <div>
              <label className={labelCls}>Route Name *</label>
              <input
                className={inputCls}
                value={form.routeName}
                onChange={e => setForm(p => ({ ...p, routeName: e.target.value }))}
                placeholder={initDir === 'OUTBOUND' ? 'e.g. Morning Outbound Route A' : 'e.g. Afternoon Return Route A'}
                required
              />
            </div>
            <div>
              <label className={labelCls}>Direction</label>
              <div className='w-full rounded-xl border border-slate-150 bg-slate-50/50 px-3 py-2 text-xs font-semibold text-slate-700 shadow-none'>
                {initDir === 'OUTBOUND' ? (
                  <span>OUTBOUND - Depot ➔ School <span className='text-[10px] font-medium text-slate-400 ml-1'>(Inherited from session context)</span></span>
                ) : (
                  <span>RETURN - School ➔ Depot <span className='text-[10px] font-medium text-slate-400 ml-1'>(Inherited from session context)</span></span>
                )}
              </div>
            </div>
          </div>

          {/* Group 2: Start & End Terminals */}
          <div className='space-y-3 p-3 bg-white rounded-xl border border-slate-100 shadow-sm'>
            <p className='text-[10px] font-bold text-slate-400 uppercase tracking-wider leading-none'>2. Start & End Terminals</p>
            
            {/* Start location */}
            <div className='space-y-2 pt-1.5'>
              <label className={labelCls}>Start Location ({initDir === 'OUTBOUND' ? 'DEPOT' : 'SCHOOL'})</label>
              {initDir === 'OUTBOUND' ? (
                hasDepots ? (
                  <div className='mt-1'>
                    <SchoolBusSelect
                      fullWidth
                      value={form.startDepotId}
                      onChange={val => setForm(p => ({ ...p, startDepotId: val === '' ? '' : Number(val), busId: '' }))}
                      placeholder='- Select depot -'
                      options={depots.map(d => ({
                        label: d.name,
                        value: d.id
                      }))}
                    />
                  </div>
                ) : (
                  <p className='text-[11px] text-amber-600 font-medium'>⚠️ No depots available. Please create a depot first.</p>
                )
              ) : (
                <p className='text-[11px] text-slate-500 bg-slate-50 p-2 rounded-lg border border-slate-100 font-medium'>
                  Starts at school: <span className='font-semibold text-slate-700'>{session.schoolName}</span> (ID: {session.schoolId})
                </p>
              )}
            </div>

            {/* End location */}
            <div className='space-y-2 pt-3 border-t border-slate-100'>
              <label className={labelCls}>End Location ({initDir === 'OUTBOUND' ? 'SCHOOL' : 'DEPOT'})</label>
              {initDir === 'RETURN' ? (
                hasDepots ? (
                  <div className='mt-1'>
                    <SchoolBusSelect
                      fullWidth
                      value={form.endDepotId}
                      onChange={val => setForm(p => ({ ...p, endDepotId: val === '' ? '' : Number(val), busId: '' }))}
                      placeholder='- Select depot -'
                      options={depots.map(d => ({
                        label: d.name,
                        value: d.id
                      }))}
                    />
                  </div>
                ) : (
                  <p className='text-[11px] text-amber-600 font-medium'>⚠️ No depots available. Please create a depot first.</p>
                )
              ) : (
                <p className='text-[11px] text-slate-500 bg-slate-50 p-2 rounded-lg border border-slate-100 font-medium'>
                  Ends at school: <span className='font-semibold text-slate-700'>{session.schoolName}</span> (ID: {session.schoolId})
                </p>
              )}
            </div>
          </div>

          {/* Group 3: Bus */}
          <div className='space-y-3 p-3 bg-white rounded-xl border border-slate-100 shadow-sm'>
            <p className='text-[10px] font-bold text-slate-400 uppercase tracking-wider leading-none'>3. Bus</p>
            <div>
              <label className={labelCls}>Bus *</label>
              <SchoolBusSelect
                fullWidth
                value={form.busId}
                onChange={val => setForm(p => ({ ...p, busId: val === '' ? '' : Number(val) }))}
                disabled={!selectedDepotId || loadingBuses || buses.length === 0}
                placeholder={
                  !selectedDepotId
                    ? 'Select depot first'
                    : loadingBuses
                      ? 'Loading buses...'
                      : buses.length === 0
                        ? 'No buses in this depot'
                        : 'Select bus'
                }
                options={buses.map(bus => ({
                  label: `${bus.plateNumber} - ${bus.capacity} seats - ${bus.busType || 'Vehicle'} - ${bus.status || 'UNKNOWN'}`,
                  value: bus.id
                }))}
              />
            </div>
            {selectedBus ? (
              <div className='grid grid-cols-2 gap-2 rounded-xl border border-slate-100 bg-slate-50/70 p-2 text-[10px]'>
                <div className='col-span-2'>
                  <span className='block text-slate-400 font-bold uppercase'>Bus</span>
                  <span className='font-bold text-slate-800'>{selectedBus.plateNumber}</span>
                </div>
                <div>
                  <span className='block text-slate-400 font-bold uppercase'>Capacity</span>
                  <span className='font-bold text-slate-800'>{selectedBus.capacity} students</span>
                </div>
                <div>
                  <span className='block text-slate-400 font-bold uppercase'>Assigned</span>
                  <span className='font-bold text-slate-800'>0/{selectedBus.capacity}</span>
                </div>
                <div>
                  <span className='block text-slate-400 font-bold uppercase'>Available</span>
                  <span className='font-bold text-slate-800'>{selectedBus.capacity}</span>
                </div>
                <div>
                  <span className='block text-slate-400 font-bold uppercase'>Status</span>
                  <span className='font-bold text-slate-800'>{selectedBus.status || '-'}</span>
                </div>
              </div>
            ) : (
              <p className='text-[11px] text-slate-450 font-medium'>
                {!selectedDepotId ? 'Choose a depot before selecting a bus.' : 'Choose a bus to determine route capacity.'}
              </p>
            )}
          </div>
          {/* Group 4: Notes & validation warnings */}
          <div className='space-y-3 p-3 bg-white rounded-xl border border-slate-100 shadow-sm'>
            <p className='text-[10px] font-bold text-slate-400 uppercase tracking-wider leading-none'>4. Additional Information</p>
            <div>
              <label className={labelCls}>Notes</label>
              <input
                className={inputCls}
                value={form.planningNotes}
                onChange={e => setForm(p => ({ ...p, planningNotes: e.target.value }))}
                placeholder='Optional notes'
              />
            </div>
            {((needsStartDepot && form.startDepotId === '') || (needsEndDepot && form.endDepotId === '')) && (
              <p className='text-[11px] text-amber-600 font-semibold'>⚠️ Depot is required to create this route.</p>
            )}
          </div>

          <div className='flex justify-end gap-2 pt-2'>
            <Button type='button' size='sm' variant='ghost' onClick={() => setOpen(false)} className='text-slate-500 hover:text-slate-900'>Cancel</Button>
            <Button type='submit' size='sm' disabled={submitting || !canSubmit} className='rounded-full bg-[#C81E3A] hover:bg-[#B31B34] text-white'>
              {submitting ? 'Creating...' : 'Create Route'}
            </Button>
          </div>
        </form>
      )}
    </div>
  );
}

/* ── Session Route Card ──────────────────────────────────────────────────── */

function SessionRouteCard({
  route,
  onSelect,
  isSelected,
  onDelete,
  deleting,
  onGreedyFill,
  greedyFilling,
  onAddStudents,
}: {
  route: SchoolBusRoute;
  onSelect: (id: number) => void;
  isSelected: boolean;
  onDelete?: () => void;
  deleting?: boolean;
  onGreedyFill?: () => void;
  greedyFilling?: boolean;
  onAddStudents?: (id: number) => void;
}) {
  const isAssigned = ['ASSIGNED', 'TRIP_CREATED', 'IN_PROGRESS', 'COMPLETED'].includes(route.status);
  const studentCount = route.plannedStudentCount ?? 0;
  const capacity = route.busCapacity ?? route.assignedBusCapacity ?? null;
  const depotName = route.startDepotName ?? (route.startLocationType === 'DEPOT' ? route.startLocationName : route.endLocationName);

  // Deletable if not published/tripped AND has 0 students
  const isDeletable =
    route.status !== 'PUBLISHED' &&
    route.status !== 'TRIP_CREATED' &&
    studentCount === 0;
  const canGreedyFill =
    ['DRAFT', 'GENERATED', 'REVIEWING'].includes(route.status) &&
    Boolean(route.busId) &&
    capacity != null &&
    studentCount < capacity;
  const handleAddStudents = () => (onAddStudents ?? onSelect)(route.id);

  return (
    <div
      id={`route-card-${route.id}`}
      onClick={() => onSelect(route.id)}
      className={cn(
        'cursor-pointer rounded-2xl border p-4 transition-all duration-200 flex flex-col gap-2.5',
        isSelected
          ? 'border-l-4 border-l-[#C81E3A] border-y-slate-350 border-r-slate-355 bg-slate-50/50 shadow-sm'
          : schoolBusUi.interactiveCard
      )}
    >
      <div className='flex items-start justify-between gap-3'>
        <div className='min-w-0'>
          <p className='text-xs font-bold text-slate-900 truncate'>{route.routeName}</p>
          <p className='text-[10px] font-bold text-slate-500 mt-0.5 flex items-center gap-1.5'>
            <span className='px-1.5 py-0.5 bg-indigo-50 border border-indigo-100 rounded text-indigo-700 text-[9px]'>{route.routeCode}</span>
            <span>{route.routeDirection === 'RETURN' ? 'Return' : 'Outbound'}</span>
          </p>
        </div>
        <div className='flex flex-col items-end gap-1.5 shrink-0'>
          <span className={cn(
            'rounded-full px-2.5 py-0.5 text-[10px] font-semibold border shadow-none text-center',
            route.status === 'PUBLISHED' ? 'bg-emerald-50 text-emerald-700 border-emerald-255' :
            route.status === 'CANCELLED' ? 'bg-slate-50 text-slate-500 border-slate-200' : 'bg-amber-50 text-amber-700 border-amber-250'
          )}>{route.status === 'PUBLISHED' ? 'Published' : route.status === 'CANCELLED' ? 'Cancelled' : 'Draft'}</span>
          <span className={cn(
            'rounded-full px-2 py-0.5 text-[9px] font-bold border shadow-none text-center',
            isAssigned
              ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
              : 'bg-amber-50 text-amber-700 border-amber-200'
          )}>
            {isAssigned ? 'Ready for trip' : 'Needs assignment'}
          </span>
        </div>
      </div>

      <div className='grid grid-cols-2 gap-2 text-[11px] text-slate-650 bg-slate-50/60 p-2.5 rounded-xl border border-slate-100/60'>
        <div className='flex items-center gap-1.5 min-w-0'>
          <span className='text-slate-400 font-medium'>Bus:</span>
          <span className='font-bold text-slate-700 truncate'>{route.busPlateNumber ?? 'Not selected'}</span>
        </div>
        <div className='flex items-center gap-1.5 justify-end'>
          <span className='text-slate-400 font-medium'>Students:</span>
          <span className='font-bold text-slate-700'>
            {capacity != null ? `${studentCount}/${capacity}` : studentCount}
          </span>
        </div>
        <div className='flex items-center gap-1.5 min-w-0'>
          <span className='text-slate-400 font-medium'>Depot:</span>
          <span className='font-bold text-slate-700 truncate'>{depotName ?? 'N/A'}</span>
        </div>
        <div className='flex items-center gap-1.5 justify-end'>
          <span className='text-slate-400 font-medium'>Stops:</span>
          <span className='font-bold text-slate-700'>{route.stopsCount ?? 0}</span>
        </div>
        <div className='col-span-2 flex items-center gap-1.5 min-w-0 border-t border-slate-200/40 pt-1.5 mt-0.5'>
          <span className='text-slate-400 font-medium'>Staff:</span>
          <span className='font-semibold text-slate-700 truncate'>
            {route.driverName && route.attendantName ? (
              `Driver: ${route.driverName} · Attendant: ${route.attendantName}`
            ) : route.driverName ? (
              `Driver: ${route.driverName} (No attendant)`
            ) : route.attendantName ? (
              `Attendant: ${route.attendantName} (No driver)`
            ) : (
              <span className='text-slate-400 italic font-medium'>Not assigned</span>
            )}
          </span>
        </div>
      </div>

      <div className='flex items-center justify-between border-t border-slate-100/50 pt-2 text-[10px] text-slate-450'>
        <span>{route.serviceDate}</span>
        {isSelected && (
          <span className='px-2 py-0.5 bg-red-50 text-[#C81E3A] border border-red-100 text-[9px] font-extrabold rounded-full flex items-center gap-1 shadow-sm'>
            Currently Selected
          </span>
        )}
      </div>

      {isSelected && (
        <div className='flex flex-wrap gap-2 border-t border-slate-100/50 pt-2.5 mt-1' onClick={e => e.stopPropagation()}>
          {canGreedyFill && onGreedyFill && (
            <Button
              size='sm'
              variant='outline'
              disabled={greedyFilling}
              className='rounded-full h-8 text-xs font-semibold border-indigo-200 text-indigo-700 bg-indigo-50/40 hover:bg-indigo-50 gap-1.5'
              onClick={onGreedyFill}
            >
              {greedyFilling
                ? <Loader2 className='h-3.5 w-3.5 animate-spin' />
                : <Sparkles className='h-3.5 w-3.5' />}
              {greedyFilling ? 'Running...' : 'Greedy Fill'}
            </Button>
          )}
          {(() => {
            const hasNoOrUnderCapacityStudents = capacity === null || capacity === 0 || studentCount < capacity;
            const hasStudentsButNoStaff = studentCount > 0 && !route.driverName && !route.attendantName;
            const hasStaff = !!route.driverName || !!route.attendantName;

            if (hasStaff) {
              return (
                <>
                  <Button
                    size='sm'
                    className='rounded-full bg-[#C81E3A] hover:bg-[#B31B34] text-white shadow-sm h-8 text-xs font-semibold px-4'
                    asChild
                  >
                    <Link href={`/school-bus/dispatch/${route.id}`}>
                      View Details
                    </Link>
                  </Button>
                  <Button
                    size='sm'
                    variant='outline'
                    className='rounded-full h-8 text-xs font-semibold border-slate-200 text-slate-700 bg-white hover:bg-slate-50 shadow-sm'
                    onClick={handleAddStudents}
                  >
                    Add Students
                  </Button>
                  <Button
                    size='sm'
                    variant='outline'
                    className='rounded-full h-8 text-xs font-semibold border-slate-200 text-slate-700 bg-white hover:bg-slate-50 shadow-sm'
                    asChild
                  >
                    <Link href={`/school-bus/dispatch/${route.id}?assign=true`}>
                      Edit Staff
                    </Link>
                  </Button>
                </>
              );
            }

            if (hasStudentsButNoStaff) {
              return (
                <>
                  <Button
                    size='sm'
                    className='rounded-full bg-[#C81E3A] hover:bg-[#B31B34] text-white shadow-sm h-8 text-xs font-semibold px-4'
                    asChild
                  >
                    <Link href={`/school-bus/dispatch/${route.id}?assign=true`}>
                      Assign Staff
                    </Link>
                  </Button>
                  <Button
                    size='sm'
                    variant='outline'
                    className='rounded-full h-8 text-xs font-semibold border-slate-200 text-slate-700 bg-white hover:bg-slate-50 shadow-sm'
                    onClick={handleAddStudents}
                  >
                    Add Students
                  </Button>
                  <Button
                    size='sm'
                    variant='outline'
                    className='rounded-full h-8 text-xs font-semibold border-slate-200 text-slate-700 bg-white hover:bg-slate-50 shadow-sm'
                    asChild
                  >
                    <Link href={`/school-bus/dispatch/${route.id}`}>
                      View Details
                    </Link>
                  </Button>
                </>
              );
            }

            // Case: hasNoOrUnderCapacityStudents is true (or fallback default)
            return (
              <>
                <Button
                  size='sm'
                  className='rounded-full bg-[#C81E3A] hover:bg-[#B31B34] text-white shadow-sm h-8 text-xs font-semibold px-4'
                  onClick={handleAddStudents}
                >
                  Add Students
                </Button>
                <Button
                  size='sm'
                  variant='outline'
                  className='rounded-full h-8 text-xs font-semibold border-slate-200 text-slate-700 bg-white hover:bg-slate-50 shadow-sm'
                  asChild
                >
                  <Link href={`/school-bus/dispatch/${route.id}`}>
                    View Details
                  </Link>
                </Button>
              </>
            );
          })()}

          {isDeletable && onDelete && (
            <Button
              size='sm'
              variant='ghost'
              disabled={deleting}
              className='rounded-full h-8 text-xs font-semibold text-red-600 hover:text-red-700 hover:bg-red-50/50 gap-1 ml-auto'
              onClick={onDelete}
            >
              {deleting ? (
                <Loader2 className='h-3 w-3 animate-spin' />
              ) : (
                <Trash2 className='h-3.5 w-3.5' />
              )}
              Delete
            </Button>
          )}
        </div>
      )}
    </div>
  );
}

/* ── Route Card ─────────────────────────────────────────────────────────── */

function RouteCard({ route, onSelect, isSelected }: { route: any; onSelect: (id: number) => void; isSelected: boolean }) {
  const isAssigned = ['ASSIGNED', 'TRIP_CREATED', 'IN_PROGRESS', 'COMPLETED'].includes(route.status);

  return (
    <div id={`route-card-${route.routeId}`} onClick={() => onSelect(route.routeId)}
      className={cn(
        'cursor-pointer rounded-2xl border p-4 transition-all duration-200 flex flex-col gap-2.5',
        isSelected
          ? 'border-l-4 border-l-[#C81E3A] border-y-slate-350 border-r-slate-355 bg-slate-50/50 shadow-sm'
          : schoolBusUi.interactiveCard
      )}>
      <div className='flex items-start justify-between gap-3'>
        <div className='min-w-0'>
          <p className='text-xs font-bold text-slate-900 truncate'>{route.routeName}</p>
          <p className='text-[10px] font-bold text-slate-555 mt-0.5 flex items-center gap-1.5'>
            <span className='px-1.5 py-0.5 bg-indigo-50 border border-indigo-100 rounded text-indigo-700 text-[9px]'>{route.routeCode}</span>
          </p>
        </div>
        <div className='flex items-center gap-2'>
          <div className='flex flex-col items-end gap-1 shrink-0'>
            <span className={cn(
              'rounded-full px-2 py-0.5 text-[9px] font-bold border shadow-none text-center',
              isAssigned
                ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                : 'bg-amber-50 text-amber-700 border-amber-200'
            )}>
              {isAssigned ? 'Ready for trip' : 'Needs assignment'}
            </span>
          </div>
        </div>
      </div>
      
      <div className='grid grid-cols-2 gap-2 text-[11px] text-slate-650 bg-slate-50/60 p-2.5 rounded-xl border border-slate-100/60'>
        <div className='flex items-center gap-1.5 min-w-0'>
          <span className='text-slate-400 font-medium'>Capacity:</span>
          <span className='font-bold text-slate-700'>{route.requiredCapacity ?? '-'}</span>
        </div>
        <div className='flex items-center gap-1.5 justify-end'>
          <span className='text-slate-400 font-medium'>Students:</span>
          <span className='font-bold text-slate-700'>{route.studentCount}</span>
        </div>
        <div className='flex items-center gap-1.5 min-w-0'>
          <span className='text-slate-400 font-medium'>Distance:</span>
          <span className='font-bold text-slate-700'>{route.totalDistanceKm != null ? route.totalDistanceKm.toFixed(1) + ' km' : '-'}</span>
        </div>
        <div className='flex items-center gap-1.5 justify-end'>
          <span className='text-slate-400 font-medium'>Duration:</span>
          <span className='font-bold text-slate-700'>{route.totalDurationMin != null ? route.totalDurationMin + ' min' : '-'}</span>
        </div>
        <div className='flex items-center gap-1.5 min-w-0'>
          <span className='text-slate-400 font-medium'>Stops:</span>
          <span className='font-bold text-slate-700'>{route.stopCount ?? 0}</span>
        </div>
      </div>

      <div className='flex items-center justify-between border-t border-slate-100/50 pt-2 text-[10px] text-slate-450'>
        <span>Draft Preview</span>
        {isSelected && (
          <span className='px-2 py-0.5 bg-red-50 text-[#C81E3A] border border-red-100 text-[9px] font-extrabold rounded-full flex items-center gap-1 shadow-sm'>
            Currently Selected
          </span>
        )}
      </div>
      
      {isSelected && (
        <div className='flex flex-wrap gap-2 border-t border-slate-100/50 pt-2.5 mt-1' onClick={e => e.stopPropagation()}>
          <Button
            size='sm'
            className='rounded-full bg-[#C81E3A] hover:bg-[#B31B34] text-white shadow-sm h-8 text-xs font-semibold px-4'
            onClick={() => onSelect(route.routeId)}
          >
            Add Students
          </Button>
          <Button
            size='sm'
            variant='outline'
            className='rounded-full h-8 text-xs font-semibold border-slate-200 text-slate-700 bg-white hover:bg-slate-50 shadow-sm'
            asChild
          >
            <Link href={`/school-bus/dispatch/${route.routeId}`}>
              View Details
            </Link>
          </Button>
          <Button
            size='sm'
            variant='outline'
            className='rounded-full h-8 text-xs font-semibold border-slate-200 text-slate-700 bg-white hover:bg-slate-50 shadow-sm'
            asChild
          >
            <Link href={`/school-bus/dispatch/${route.routeId}?assign=true`}>
              Assign Staff
            </Link>
          </Button>
        </div>
      )}
    </div>
  );
}

/* ── Route Detail Panel ─────────────────────────────────────────────────── */

function RouteDetailPanel({ routeId, sessionId }: { routeId: number; sessionId: number }) {
  const { data: routeDetail, isLoading } = useGetRouteByIdQuery(routeId);
  const [reorderStops, { isLoading: reorderingStops }] = useReorderRouteStopsMutation();
  const [removeStop, { isLoading: removingStop }] = useRemoveRouteStopMutation();
  const [removeStudent, { isLoading: removingStudent }] = useRemoveRouteStudentMutation();
  const detail = routeDetail?.data;

  // ── Confirm dialogs ──────────────────────────────────────────────────
  const stopConfirm = useSchoolBusConfirm({
    onConfirm: async () => {
      const stopId = stopConfirm.confirmState.payload as number;
      try { await removeStop({ routeId, stopId, sessionId }).unwrap(); } catch (e: unknown) {
        const err = e as { data?: { message?: string } };
        toast.error(err?.data?.message ?? 'Failed to remove stop');
      }
    },
    isLoading: removingStop,
  });

  const studentConfirm = useSchoolBusConfirm({
    onConfirm: async () => {
      const { studentId, subscriptionId } = studentConfirm.confirmState.payload as {
        studentId: number;
        subscriptionId: number;
      };
      try { await removeStudent({ routeId, studentId, subscriptionId, sessionId }).unwrap(); } catch (e: unknown) {
        const err = e as { data?: { message?: string } };
        toast.error(err?.data?.message ?? 'Failed to remove student');
      }
    },
    isLoading: removingStudent,
  });

  if (isLoading || !detail) return <div className='py-8 text-center text-sm text-slate-400'>Loading route detail...</div>;

  const stops = detail.stops ?? [];
  const students = detail.students ?? [];
  const editable = !['COMPLETED', 'IN_PROGRESS', 'TRIP_CREATED', 'CANCELLED'].includes(detail.route.status);

  const swap = async (i: number, j: number) => {
    // Only swap middle stops — find their indices in the full stops array
    const middleStops = stops.filter(s => s.stopPurpose !== 'START_TERMINAL' && s.stopPurpose !== 'END_TERMINAL');
    const ids = middleStops.map(s => s.id);
    [ids[i], ids[j]] = [ids[j], ids[i]];
    try {
      await reorderStops({ id: routeId, orderedStopIds: ids, sessionId }).unwrap();
    } catch (e: unknown) {
      const err = e as { data?: { message?: string } };
      toast.error(err?.data?.message ?? 'Failed to reorder stops');
    }
  };

  const delStop = (stopId: number) => {
    stopConfirm.requestConfirm({
      title: 'Remove stop?',
      description: 'This stop and all students assigned to it will be removed from the route.',
      confirmLabel: 'Remove stop',
      variant: 'danger',
      payload: stopId,
    });
  };

  const delStudent = (studentId: number, subscriptionId: number) => {
    studentConfirm.requestConfirm({
      title: 'Remove student?',
      description: 'The student will be unassigned from this route and returned to the unassigned pool.',
      confirmLabel: 'Remove',
      variant: 'danger',
      payload: { studentId, subscriptionId },
    });
  };

  const middleStops = stops.filter(s => s.stopPurpose !== 'START_TERMINAL' && s.stopPurpose !== 'END_TERMINAL');

  const anyLoading = reorderingStops || removingStop || removingStudent;

  return (
    <>
      <div className='p-5 space-y-4 border-t border-slate-200 bg-slate-50/20'>
        <div className='border-b border-slate-100 pb-2'>
          <h3 className='text-sm font-bold text-slate-955 flex items-center gap-2'>
            <Route className='h-4 w-4 text-emerald-600 shrink-0' />
            {detail.route.routeName} - Detail
          </h3>
        </div>

        <div>
          {/* Terminal route banner */}
          {(() => {
            const startTerminal = stops.find(s => s.stopPurpose === 'START_TERMINAL');
            const endTerminal = stops.find(s => s.stopPurpose === 'END_TERMINAL');
            if (!startTerminal && !endTerminal) return null;
            return (
              <div className='mb-4 rounded-2xl border border-slate-100 bg-slate-50/50 p-3 text-xs text-slate-600'>
                <div className='flex items-center gap-2 font-bold text-slate-700 mb-2'>
                  <Navigation className='h-3.5 w-3.5 text-blue-600' />
                  <span className='text-[10px] font-extrabold uppercase tracking-wider text-slate-400'>Terminal Stops</span>
                </div>
                <div className='flex items-center gap-2 pl-5 text-[11px] font-semibold text-slate-700'>
                  <span className='bg-blue-50 text-blue-700 px-1.5 py-0.5 rounded border border-blue-100'>
                    {startTerminal?.depotName || startTerminal?.schoolName || 'Start Depot/School'}
                  </span>
                  <span className='text-slate-400'>➔</span>
                  <span className='bg-blue-50 text-blue-700 px-1.5 py-0.5 rounded border border-blue-100'>
                    {endTerminal?.depotName || endTerminal?.schoolName || 'End Depot/School'}
                  </span>
                </div>
              </div>
            );
          })()}

          <p className='text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-2'>
            {detail.route.routeDirection === 'OUTBOUND' ? 'Pickup Stops' : 'Drop-off Stops'} ({middleStops.length})
          </p>
          {middleStops.length === 0 ? (
            <p className='text-xs text-slate-400 italic bg-white border border-dashed border-slate-200 rounded-2xl p-4 text-center'>
              {detail.route.routeDirection === 'OUTBOUND'
                ? 'No pickup stops created yet. Assign students to auto-generate stops.'
                : 'No drop-off stops created yet. Assign students to auto-generate stops.'}
            </p>
          ) : (
            <div className='space-y-1.5'>
              {middleStops.map((stop, middleIndex) => {
                const isFirstMiddle = middleIndex === 0;
                const isLastMiddle = middleIndex === middleStops.length - 1;
                return (
                  <div key={stop.id} className='flex items-center gap-2 rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs text-slate-700'>
                    <span className='flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-slate-100 text-[10px] font-bold text-slate-650'>
                      {middleIndex + 1}
                    </span>
                    <span className='flex-1 font-semibold truncate'>{stop.pickupPointName || `Stop #${stop.id}`}</span>
                    <span className='text-[10px] text-slate-450'>{stop.estimatedStudentCount ?? 0} students</span>
                    {editable && (
                      <div className='flex gap-0.5' onClick={e => e.stopPropagation()}>
                        <Button variant='ghost' size='icon' className='h-6 w-6' onClick={() => swap(middleIndex, middleIndex - 1)} disabled={isFirstMiddle || anyLoading}><ChevronUp className='h-3 w-3' /></Button>
                        <Button variant='ghost' size='icon' className='h-6 w-6' onClick={() => swap(middleIndex, middleIndex + 1)} disabled={isLastMiddle || anyLoading}><ChevronDown className='h-3 w-3' /></Button>
                        <Button variant='ghost' size='icon' className='h-6 w-6 text-red-500 hover:text-red-700' onClick={() => delStop(stop.id)} disabled={anyLoading}><Trash2 className='h-3 w-3' /></Button>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </div>

        <div>
          {(() => {
            return (
              <>
                <p className='text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-2 mt-4'>Students ({students.length})</p>
                {students.length === 0 ? (
                  <p className='text-xs text-slate-300'>No students assigned to stops</p>
                ) : (
                  <div className='space-y-1.5'>
                    {students.map(ps => (
                      <div key={`${ps.studentId}-${ps.subscriptionId}`} className='flex flex-col gap-1.5 rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-xs text-slate-700 shadow-sm hover:shadow transition-shadow'>
                        <div className='flex items-center gap-2'>
                          <span className='flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-indigo-50 text-indigo-650'>
                            <Users className='h-3 w-3' />
                          </span>
                          <span className='flex-1 font-bold text-slate-800 truncate'>{ps.studentName || `Student #${ps.studentId}`}</span>
                          {editable && (
                            <Button variant='ghost' size='icon' className='h-6 w-6 text-red-500 hover:text-red-700' onClick={() => delStudent(ps.studentId, ps.subscriptionId ?? 0)} disabled={anyLoading}><Trash2 className='h-3 w-3' /></Button>
                          )}
                        </div>
                        <div className='pl-7 text-[10px] text-slate-450 space-y-1.5 border-t border-slate-100/60 pt-2 mt-1'>
                          {ps.pickupPointName && (
                            <div className='flex items-center gap-1.5'>
                              <span className='px-1 py-0.2 rounded-[4px] font-extrabold text-[8px] uppercase tracking-wider bg-emerald-50 text-emerald-700 border border-emerald-100'>
                                PICKUP
                              </span>
                              <span className='text-slate-600 font-semibold truncate'>{ps.pickupPointName}</span>
                            </div>
                          )}
                          {ps.dropoffPointName && (
                            <div className='flex items-center gap-1.5'>
                              <span className='px-1 py-0.2 rounded-[4px] font-extrabold text-[8px] uppercase tracking-wider bg-blue-50 text-blue-700 border border-blue-100'>
                                DROPOFF
                              </span>
                              <span className='text-slate-600 font-semibold truncate'>{ps.dropoffPointName}</span>
                            </div>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </>
            );
          })()}
        </div>

        {/* Staff Section */}
        <div>
          <p className='text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-2 mt-4'>Staff</p>
          <div className='rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-xs text-slate-700 shadow-sm'>
            {detail.route.driverName || detail.route.attendantName ? (
              <div className='space-y-2'>
                {detail.route.driverName && (
                  <div className='flex items-center justify-between'>
                    <div className='flex items-center gap-2'>
                      <span className='w-16 text-slate-400 font-semibold'>Driver:</span>
                      <span className='font-bold text-slate-800'>{detail.route.driverName}</span>
                    </div>
                  </div>
                )}
                {detail.route.attendantName && (
                  <div className='flex items-center justify-between border-t border-slate-100/65 pt-2 mt-2'>
                    <div className='flex items-center gap-2'>
                      <span className='w-16 text-slate-400 font-semibold'>Attendant:</span>
                      <span className='font-bold text-slate-800'>{detail.route.attendantName}</span>
                    </div>
                  </div>
                )}
              </div>
            ) : (
              <p className='text-xs text-slate-450 italic py-1'>No staff assigned to this route.</p>
            )}
          </div>
        </div>

        <hr className='border-slate-150 mt-4' />
      </div>

      {/* Confirm dialogs */}
      <SchoolBusConfirmDialog {...stopConfirm.dialogProps} />
      <SchoolBusConfirmDialog {...studentConfirm.dialogProps} />
    </>
  );
}

/* ── Main Results Panel ─────────────────────────────────────────────────── */

interface PlanningResultsPanelProps {
  preview: SchoolBusPlanningPreview | null;
  /** Routes loaded from backend and preserved across refreshes. */
  sessionRoutes: SchoolBusRoute[];
  activeSession: SchoolBusPlanningSession | null;
  eligibleStudents: SchoolBusEligibleStudent[];
  loadingEligible?: boolean;
  selectedRouteId: number | null;
  onSelectRoute: (id: number | null) => void;
  onCreateManualRoute: (req: CreateRouteInSessionRequest) => void;
  creatingRoute?: boolean;
  form?: any;
  rightPanelTab: 'demand-preview' | 'route-builder';
  onTabChange: (tab: 'demand-preview' | 'route-builder') => void;
  onPreviewDemandClick?: () => void;
  previewing?: boolean;
  onOpenSession?: (id: number) => void;
}

export function PlanningResultsPanel({
  preview,
  sessionRoutes,
  activeSession,
  eligibleStudents,
  loadingEligible,
  selectedRouteId,
  onSelectRoute,
  onCreateManualRoute,
  creatingRoute,
  form,
  rightPanelTab,
  onTabChange,
  onPreviewDemandClick,
  previewing,
  onOpenSession,
}: PlanningResultsPanelProps) {

  const [previewTab, setPreviewTab] = useState<'demands' | 'points' | 'context'>('demands');
  const [pendingAssignScrollRouteId, setPendingAssignScrollRouteId] = useState<number | null>(null);
  const manualAssignPanelRef = React.useRef<HTMLDivElement>(null);

  const [deleteRoute, { isLoading: deletingRoute }] = useDeleteRouteInSessionMutation();
  const [greedyFillRoute, { isLoading: greedyFilling }] = useGreedyFillRouteMutation();

  const handleAddStudents = (routeId: number) => {
    setPendingAssignScrollRouteId(routeId);
    if (selectedRouteId !== routeId) {
      onSelectRoute(routeId);
    }
  };

  useEffect(() => {
    if (rightPanelTab !== 'route-builder'
        || pendingAssignScrollRouteId === null
        || selectedRouteId !== pendingAssignScrollRouteId) {
      return;
    }

    const animationFrame = window.requestAnimationFrame(() => {
      manualAssignPanelRef.current?.scrollIntoView({
        behavior: 'smooth',
        block: 'start',
      });
      setPendingAssignScrollRouteId(null);
    });

    return () => window.cancelAnimationFrame(animationFrame);
  }, [pendingAssignScrollRouteId, rightPanelTab, selectedRouteId]);

  const deleteConfirm = useSchoolBusConfirm({
    onConfirm: async () => {
      const routeId = deleteConfirm.confirmState.payload as number;
      if (!activeSession) return;
      try {
        await deleteRoute({ sessionId: activeSession.id, routeId }).unwrap();
        toast.success('Route deleted successfully');
        if (selectedRouteId === routeId) {
          onSelectRoute(null);
        }
      } catch (e: unknown) {
        const err = e as { data?: { message?: string } };
        toast.error(err?.data?.message ?? 'Failed to delete route');
      }
    },
    isLoading: deletingRoute,
  });

  const greedyConfirm = useSchoolBusConfirm({
    onConfirm: async () => {
      const routeId = greedyConfirm.confirmState.payload as number;
      if (!activeSession) return;
      try {
        const response = await greedyFillRoute({
          sessionId: activeSession.id,
          routeId,
          body: { preserveExistingAssignments: true },
        }).unwrap();
        toast.success(
          `Added ${response.data.addedStudents} students across ${response.data.addedStops} stops. Remaining capacity: ${response.data.remainingCapacity}.`,
        );
      } catch (e: unknown) {
        const err = e as { data?: { message?: string } };
        toast.error(err?.data?.message ?? 'Greedy Fill failed');
        throw e;
      }
    },
    isLoading: greedyFilling,
  });

  const schoolId = Number(preview?.schoolId ?? form?.schoolId) || 0;
  const { data: schoolData } = useGetSchoolByIdQuery(schoolId, { skip: !schoolId });
  const currentSchool = schoolData?.data ?? null;

  const scheduleId = 0; // schedule removed (Phase 3)
  // scheduleData query removed (Phase 3)

  const isPreviewStale = preview ? (
    Number(preview.schoolId) !== Number(form?.schoolId) ||
    preview.serviceDate !== form?.serviceDate ||
    preview.routeDirection !== form?.routeDirection
  ) : false;

  return (
    <div className='flex h-full flex-col bg-white overflow-hidden'>
      {/* Premium Tab Selector */}
      <div className='shrink-0 flex border-b border-slate-200 bg-slate-50/50 p-1.5 gap-1.5'>
        <button
          onClick={() => onTabChange('demand-preview')}
          className={cn(
            'flex-1 flex flex-col items-center justify-center py-2 px-3 rounded-xl border transition-all text-center',
            rightPanelTab === 'demand-preview'
              ? 'bg-white border-slate-200 shadow-sm text-[#C81E3A] font-bold'
              : 'bg-transparent border-transparent text-slate-500 hover:bg-slate-100/50 hover:text-slate-700'
          )}
        >
          <span className='text-xs font-bold'>Student Demand</span>
          <span className='text-[10px] text-slate-400 mt-0.5 font-medium'>
            {preview && !isPreviewStale
              ? `Total ${preview.summary?.totalSubscriptions ?? preview.totalEligibleStudents} • Eligible ${preview.summary?.eligibleStudents ?? preview.totalEligibleStudents} • Points ${preview.summary?.pointCount ?? preview.totalEligiblePickupPoints}`
              : isPreviewStale
                ? 'Out of date'
                : 'No preview'}
          </span>
        </button>
        <button
          onClick={() => onTabChange('route-builder')}
          className={cn(
            'flex-1 flex flex-col items-center justify-center py-2 px-3 rounded-xl border transition-all text-center',
            rightPanelTab === 'route-builder'
              ? 'bg-white border-slate-200 shadow-sm text-[#C81E3A] font-bold'
              : 'bg-transparent border-transparent text-slate-500 hover:bg-slate-100/50 hover:text-slate-700'
          )}
        >
          <span className='text-xs font-bold'>Route Builder</span>
          <span className='text-[10px] text-slate-400 mt-0.5 font-medium'>
            {activeSession
              ? `Routes (${sessionRoutes.length})`
              : 'No session'}
          </span>
        </button>
      </div>

      <div className='flex-1 overflow-y-auto divide-y divide-slate-150'>
        {/* Tab 1: Demand Preview */}
        {rightPanelTab === 'demand-preview' && (
          <>
            {/* Case A: Stale Preview */}
            {isPreviewStale && (
              <div className='flex flex-col items-center justify-center p-8 text-center bg-white h-full min-h-[350px]'>
                <div className='flex h-12 w-12 items-center justify-center rounded-full bg-amber-50 border border-amber-100 text-amber-500 mb-3 shadow-sm'>
                  <AlertTriangle className='h-6 w-6' />
                </div>
                <p className='text-sm font-bold text-slate-700'>Preview is out of date</p>
                <p className='mt-1 text-xs text-slate-455 max-w-[240px] leading-relaxed'>
                  Planning context has changed. Click Preview to refresh.
                </p>
                {onPreviewDemandClick && (
                  <Button
                    size='sm'
                    onClick={onPreviewDemandClick}
                    disabled={previewing}
                    className='mt-4 rounded-full bg-[#C81E3A] text-white hover:bg-[#A91931] px-5 text-xs font-semibold shadow-sm'
                  >
                    {previewing ? 'Previewing...' : 'Preview Demand'}
                  </Button>
                )}
              </div>
            )}

            {/* Case B: No Preview at all */}
            {!preview && !isPreviewStale && (
              <div className='flex flex-col items-center justify-center p-8 text-center bg-white h-full min-h-[350px]'>
                <div className='flex h-12 w-12 items-center justify-center rounded-full bg-slate-50 border border-slate-150 text-slate-400 mb-3 shadow-sm'>
                  <Users className='h-6 w-6' />
                </div>
                <p className='text-sm font-bold text-slate-700'>
                  {activeSession ? 'Preview demand for this session' : 'No demand preview yet'}
                </p>
                <p className='mt-1 text-xs text-slate-455 max-w-[240px] leading-relaxed'>
                  {activeSession
                    ? 'Preview demand for this session to inspect eligible and blocked students.'
                    : 'Complete planning context and click Preview to inspect eligible students.'}
                </p>
                {onPreviewDemandClick && (
                  <Button
                    size='sm'
                    onClick={onPreviewDemandClick}
                    disabled={previewing}
                    className='mt-4 rounded-full bg-[#C81E3A] text-white hover:bg-[#A91931] px-5 text-xs font-semibold shadow-sm'
                  >
                    {previewing ? 'Previewing...' : 'Preview Demand'}
                  </Button>
                )}
              </div>
            )}

            {/* Case C: Active Preview Data */}
            {preview && !isPreviewStale && (() => {
        const summary = preview?.summary;
        const eligibleDemands = preview?.eligibleDemands ?? [];
        const points = preview?.points ?? [];

        if (preview.existingSessionId) {
          return (
            <div className='flex flex-col items-center justify-center p-8 text-center bg-white h-full min-h-[350px] space-y-4'>
              <div className='flex h-12 w-12 items-center justify-center rounded-full bg-amber-50 border border-amber-100 text-amber-500 shadow-sm'>
                <AlertTriangle className='h-6 w-6' />
              </div>
              <p className='text-sm font-bold text-slate-800 leading-snug'>
                Planning session already exists for this school, date and direction. Please open the existing session to continue.
              </p>
              {onOpenSession && (
                <Button
                  size='sm'
                  onClick={() => onOpenSession(preview.existingSessionId!)}
                  className='rounded-full bg-[#C81E3A] text-white hover:bg-[#A91931] px-5 text-xs font-semibold shadow-sm gap-1.5'
                >
                  Open Existing Session
                </Button>
              )}
            </div>
          );
        }

        if (eligibleDemands.length === 0) {
          return (
            <div className='flex flex-col items-center justify-center p-8 text-center bg-white h-full min-h-[350px] space-y-3'>
              <div className='flex h-12 w-12 items-center justify-center rounded-full bg-slate-50 border border-slate-150 text-slate-400 shadow-sm'>
                <Ban className='h-6 w-6' />
              </div>
              <p className='text-sm font-bold text-slate-700'>
                No eligible students for this date and direction.
              </p>
              <p className='text-xs text-slate-450 max-w-[240px] leading-relaxed'>
                Make sure students have active subscriptions for this school and route direction.
              </p>
            </div>
          );
        }

        // Derived variables for display
        const totalSubs = summary?.totalSubscriptions ?? preview.totalEligibleStudents;
        const eligibleCount = summary?.eligibleStudents ?? preview.totalEligibleStudents;
        const pointsCount = summary?.pointCount ?? preview.totalEligiblePickupPoints;

        const currentDirection = preview?.routeDirection ?? form?.routeDirection;

        return (
          <div className='p-5 space-y-4 bg-white rounded-2xl'>
            <div className='border-b border-slate-100 pb-2.5 flex items-center justify-between'>
              <h3 className='text-sm font-bold text-slate-950 flex items-center gap-2'>
                <Users className='h-4 w-4 text-[#C81E3A] shrink-0' />
                Planning Demand Preview
              </h3>
              <span className='px-2 py-0.5 bg-red-50 border border-red-100 text-[#C81E3A] text-[10px] font-bold rounded-full'>
                {currentDirection === 'RETURN' ? 'Return' : 'Outbound'}
              </span>
            </div>

            {/* KPI Summary Cards */}
            <div className='grid grid-cols-3 gap-2 text-xs'>
              <div className='p-2.5 rounded-2xl bg-slate-50 border border-slate-150 shadow-sm flex flex-col justify-between'>
                <span className='text-[9px] font-extrabold text-slate-450 uppercase tracking-wider leading-none'>Total</span>
                <span className='text-sm font-black text-slate-850 mt-1.5'>{totalSubs}</span>
              </div>
              <div className='p-2.5 rounded-2xl bg-emerald-50/50 border border-emerald-100 shadow-sm flex flex-col justify-between'>
                <span className='text-[9px] font-extrabold text-emerald-650 uppercase tracking-wider leading-none'>Eligible</span>
                <span className='text-sm font-black text-emerald-700 mt-1.5'>{eligibleCount}</span>
              </div>
              <div className='p-2.5 rounded-2xl bg-blue-50/50 border border-blue-100 shadow-sm flex flex-col justify-between'>
                <span className='text-[9px] font-extrabold text-blue-650 uppercase tracking-wider leading-none'>Points</span>
                <span className='text-sm font-black text-blue-700 mt-1.5'>{pointsCount}</span>
              </div>
            </div>

            {/* Tab Navigation */}
            <div className='flex border-b border-slate-150 text-xs font-bold'>
              <button
                onClick={() => setPreviewTab('demands')}
                className={cn(
                  'px-3 py-2 border-b-2 transition-all -mb-px',
                  previewTab === 'demands' ? 'border-[#C81E3A] text-slate-900' : 'border-transparent text-slate-400 hover:text-slate-600'
                )}
              >
                Students ({preview ? eligibleDemands.length : 0})
              </button>
              <button
                onClick={() => setPreviewTab('points')}
                className={cn(
                  'px-3 py-2 border-b-2 transition-all -mb-px',
                  previewTab === 'points' ? 'border-[#C81E3A] text-slate-900' : 'border-transparent text-slate-400 hover:text-slate-600'
                )}
              >
                Points ({preview ? points.length : 0})
              </button>
              <button
                onClick={() => setPreviewTab('context')}
                className={cn(
                  'px-3 py-2 border-b-2 transition-all -mb-px',
                  previewTab === 'context' ? 'border-[#C81E3A] text-slate-900' : 'border-transparent text-slate-400 hover:text-slate-600'
                )}
              >
                Context
              </button>
            </div>

            {/* Tab: Students List */}
            {previewTab === 'demands' && (() => {
              if (!preview) {
                return (
                  <div className='flex flex-col items-center justify-center py-12 px-4 text-center bg-slate-50/40 rounded-2xl border border-dashed border-slate-200 mt-2 shadow-sm w-full'>
                    <Users className='h-8 w-8 text-slate-350 mb-2' />
                    <p className='text-xs font-bold text-slate-600'>No Preview Data</p>
                    <p className='text-[10px] text-slate-455 mt-1 max-w-[200px]'>
                      Fill context on the left and click "Preview Demand" to load students.
                    </p>
                  </div>
                );
              }
              return (
                <div className='space-y-3 pt-1'>
                  {eligibleDemands.length === 0 ? (
                    <div className='text-center py-8 text-slate-400 text-xs'>No students found.</div>
                  ) : (
                    <div className='space-y-2 max-h-[380px] overflow-y-auto pr-1'>
                      {eligibleDemands.map(d => (
                        <div
                          key={d.subscriptionId}
                          className='rounded-xl border border-slate-150 p-3 flex flex-col gap-1.5 shadow-sm bg-white'
                        >
                          <div className='min-w-0'>
                            <p className='text-xs font-bold text-slate-800 truncate'>{d.studentName}</p>
                            <p className='text-[10px] text-slate-400 mt-0.5 flex items-center gap-1.5'>
                              <span className='font-bold'>{d.studentCode}</span>
                              <span>•</span>
                              <span>{d.tripOption === 'ONE_WAY' ? 'One Way' : d.tripOption === 'ROUND_TRIP' ? 'Round Trip' : d.tripOption}</span>
                            </p>
                          </div>

                          {d.pointName && (
                            <div className='flex items-center gap-1.5 text-[10px] text-slate-500 bg-slate-50 px-2 py-1 rounded-lg border border-slate-100 mt-0.5'>
                              <MapPin className='h-3 w-3 text-slate-400 shrink-0' />
                              <span className='font-bold truncate'>{d.pointName}</span>
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              );
            })()}

            {/* Tab: Points List */}
            {previewTab === 'points' && (() => {
              if (!preview) {
                return (
                  <div className='flex flex-col items-center justify-center py-12 px-4 text-center bg-slate-50/40 rounded-2xl border border-dashed border-slate-200 mt-2 shadow-sm w-full'>
                    <MapPin className='h-8 w-8 text-slate-350 mb-2' />
                    <p className='text-xs font-bold text-slate-600'>No Preview Data</p>
                    <p className='text-[10px] text-slate-455 mt-1 max-w-[200px]'>
                      Fill context on the left and click "Preview Demand" to load points.
                    </p>
                  </div>
                );
              }
              return (
                <div className='space-y-3 pt-1'>
                  {points.length === 0 ? (
                    <div className='text-center py-8 text-slate-400 text-xs'>No points mapped.</div>
                  ) : (
                    <div className='grid gap-2 max-h-[380px] overflow-y-auto pr-1 sm:grid-cols-2'>
                      {points.map(pp => (
                        <div
                          key={pp.pointId}
                          className='rounded-xl border border-slate-150 p-3 flex flex-col justify-between gap-1 shadow-sm bg-white transition-all'
                        >
                          <div className='min-w-0'>
                            <p className='text-xs font-bold text-slate-800 flex items-center gap-1.5 truncate'>
                              <MapPin className='h-3.5 w-3.5 shrink-0 text-[#C81E3A]' />
                              {pp.pointName}
                            </p>
                            {pp.pointCode && (
                              <p className='text-[9px] font-bold text-slate-400 mt-0.5'>{pp.pointCode}</p>
                            )}
                          </div>

                          <div className='flex items-center justify-between mt-2 pt-2 border-t border-slate-100/50 text-[10px] text-slate-500'>
                            <span className='font-bold'>{pp.studentCount} student(s)</span>
                            {(!pp.latitude || !pp.longitude) && (
                              <span className='text-[9px] font-bold text-amber-600'>No coordinates</span>
                            )}
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              );
            })()}

            {/* Tab 4: Context */}
            {previewTab === 'context' && (() => {
              // 1. School Info
              const schName = preview?.schoolName ?? currentSchool?.name ?? '-';
              const schCode = preview?.schoolCode ?? currentSchool?.code ?? '-';
              const schAddr = preview?.schoolAddress ?? currentSchool?.address ?? '-';


              // 3. Active Days
              const pActiveDays = preview?.activeDays;
              const rawActiveDays = pActiveDays ?? [];
              const activeDaysSet = new Set(rawActiveDays.map((d: string) => d.toUpperCase()));

              const daysOfWeek = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
              const dayLabels: Record<string, string> = {
                MONDAY: 'Mon', TUESDAY: 'Tue', WEDNESDAY: 'Wed', THURSDAY: 'Thu', FRIDAY: 'Fri', SATURDAY: 'Sat', SUNDAY: 'Sun'
              };

              // Check if service date matches schedule days
              const sDate = preview?.serviceDate ?? form?.serviceDate;
              let dateMatchText = '';
              let dateMatchColor = '';
              if (sDate) {
                const dayNames = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];
                const dateDayName = dayNames[new Date(sDate).getDay()];
                const isMatch = activeDaysSet.has(dateDayName);
                if (isMatch) {
                  dateMatchText = 'Service date matches schedule day';
                  dateMatchColor = 'text-emerald-700 bg-emerald-55/10 border-emerald-100';
                } else {
                  dateMatchText = 'Service date is not included in schedule days';
                  dateMatchColor = 'text-red-700 bg-red-55/10 border-red-100';
                }
              }

              // 4. Planning Context
              const serviceDateFormatted = sDate ? formatDate(sDate) : '-';
              const dirVal = preview?.direction ?? form?.routeDirection;
              const dirLabel = dirVal === 'OUTBOUND' ? 'Home ➔ School' : dirVal === 'RETURN' ? 'School ➔ Home' : dirVal ?? '-';
              const hasContext = !!(form?.schoolId || preview?.schoolId);
              if (!hasContext) {
                return (
                  <div className='flex flex-col items-center justify-center py-10 px-4 text-center bg-slate-50/40 rounded-2xl border border-dashed border-slate-200 mt-2 w-full'>
                    <Calendar className='h-8 w-8 text-slate-355 mb-2' />
                    <p className='text-xs font-bold text-slate-650'>Select Context</p>
                    <p className='text-[10px] text-slate-455 mt-1 max-w-[200px]'>
                      Select planning context and preview demand.
                    </p>
                  </div>
                );
              }

              return (
                <div className='space-y-3 pt-1 text-xs text-slate-700 w-full'>
                  {/* School Section */}
                  <div className='rounded-2xl border border-slate-150 bg-white p-3 space-y-1.5 shadow-sm'>
                    <p className='text-[9px] font-extrabold text-slate-455 uppercase tracking-wider'>School</p>
                    <div>
                      <p className='font-bold text-slate-800 text-sm leading-snug'>{schName}</p>
                      <p className='text-[10px] text-slate-400 font-semibold mt-0.5'>{schCode}</p>
                      {schAddr && schAddr !== '-' && (
                        <p className='text-[10px] text-slate-500 mt-1 truncate'>{schAddr}</p>
                      )}
                    </div>
                  </div>

                  {/* Active Days Section */}
                  <div className='rounded-2xl border border-slate-150 bg-white p-3 space-y-2.5 shadow-sm'>
                    <p className='text-[9px] font-extrabold text-slate-455 uppercase tracking-wider'>Active Days</p>
                    <div className='flex flex-wrap gap-1.5'>
                      {daysOfWeek.map(day => {
                        const isActive = activeDaysSet.has(day);
                        return (
                          <span
                            key={day}
                            className={cn(
                              'px-2 py-0.5 rounded-lg border text-[10px] font-bold transition-all',
                              isActive
                                ? 'bg-red-50 text-[#C81E3A] border-red-100'
                                : 'bg-slate-50 text-slate-400 border-slate-100'
                            )}
                          >
                            {dayLabels[day]}
                          </span>
                        );
                      })}
                    </div>
                    {dateMatchText && (
                      <div className={cn(
                        'flex items-center gap-1.5 rounded-xl border px-2.5 py-1.5 text-[10px] font-bold shadow-sm',
                        dateMatchColor
                      )}>
                        <span>{dateMatchText === 'Service date matches schedule day' ? '✓' : '⚠️'}</span>
                        <span>{dateMatchText}</span>
                      </div>
                    )}
                  </div>

                  {/* Planning Section */}
                  <div className='rounded-2xl border border-slate-150 bg-white p-3 space-y-2 shadow-sm'>
                    <p className='text-[9px] font-extrabold text-slate-455 uppercase tracking-wider'>Planning</p>
                    <div className='grid grid-cols-2 gap-x-3 gap-y-2 pt-1 text-[11px]'>
                      <div className='flex flex-col'>
                        <span className='text-[9px] font-bold text-slate-400 uppercase tracking-wider'>Service Date</span>
                        <span className='font-bold text-slate-800 mt-0.5'>{serviceDateFormatted}</span>
                      </div>
                      <div className='flex flex-col'>
                        <span className='text-[9px] font-bold text-slate-400 uppercase tracking-wider'>Direction</span>
                        <span className='font-bold text-slate-800 mt-0.5'>{dirLabel}</span>
                      </div>
                    </div>
                  </div>
                </div>
              );
            })()}
          </div>
        );
      })()}
          </>
        )}

        {/* Tab 2: Route Builder */}
        {rightPanelTab === 'route-builder' && (
          <>
            {/* Case A: No active session */}
            {!activeSession && (
              <div className='flex flex-col items-center justify-center p-8 text-center bg-white h-full min-h-[350px]'>
                <div className='flex h-12 w-12 items-center justify-center rounded-full bg-slate-50 border border-slate-150 text-slate-400 mb-3 shadow-sm'>
                  <Route className='h-6 w-6' />
                </div>
                <p className='text-sm font-bold text-slate-700'>No planning session yet</p>
                <p className='mt-1 text-xs text-slate-455 max-w-[240px] leading-relaxed'>
                  Create a session before building routes.
                </p>
              </div>
            )}

            {/* Case B: Active Session exists */}
            {activeSession && (
              <>
        <div className='p-5 space-y-4'>
          <div className='border-b border-slate-100 pb-2.5 flex items-center justify-between'>
            <h3 className='text-sm font-bold text-slate-955 flex items-center gap-2'>
              <Route className='h-4 w-4 text-blue-600 shrink-0' />
              Routes ({sessionRoutes.length})
            </h3>
          </div>
          <CreateRoutePanel
            session={activeSession}
            onSubmit={onCreateManualRoute}
            submitting={creatingRoute}
          />
          {creatingRoute ? (
            <div className='flex flex-col items-center justify-center py-10 px-4 text-center bg-slate-50/50 border border-slate-200 rounded-2xl shadow-sm space-y-3 min-h-[160px] animate-pulse'>
              <Loader2 className='h-7 w-7 text-blue-600 animate-spin' />
              <div className='space-y-1'>
                <p className='text-xs font-bold text-slate-700'>Creating route...</p>
                <p className='text-[10px] text-slate-455 font-semibold'>Computing initial path and timeline...</p>
              </div>
            </div>
          ) : sessionRoutes.length === 0 ? (
            <div className='flex flex-col items-center justify-center p-8 border border-dashed border-slate-200 rounded-2xl bg-slate-50/50 text-center'>
              <Route className='h-8 w-8 text-slate-300 stroke-[1.5] mb-2' />
              <p className='text-xs font-bold text-slate-500'>No routes yet</p>
              <p className='text-[10px] text-slate-400 mt-0.5'>Create your first route above to get started.</p>
            </div>
          ) : (
            <div className='space-y-3'>
              {sessionRoutes.map(r => (
                <SessionRouteCard
                  key={r.id}
                  route={r}
                  onSelect={id => onSelectRoute(selectedRouteId === id ? null : id)}
                  isSelected={selectedRouteId === r.id}
                  onAddStudents={handleAddStudents}
                  onDelete={() => {
                    deleteConfirm.requestConfirm({
                      title: 'Delete empty route?',
                      description: 'Are you sure you want to delete this route? This action cannot be undone.',
                      confirmLabel: 'Delete',
                      variant: 'danger',
                      payload: r.id,
                    });
                  }}
                  deleting={deletingRoute && deleteConfirm.confirmState.payload === r.id}
                  onGreedyFill={() => {
                    greedyConfirm.requestConfirm({
                      title: 'Greedy Fill this route?',
                      description: 'Automatically assign eligible students to this route based on distance and remaining bus capacity?',
                      confirmLabel: 'Greedy Fill',
                      variant: 'primary',
                      payload: r.id,
                    });
                  }}
                  greedyFilling={greedyFilling && greedyConfirm.confirmState.payload === r.id}
                />
              ))}
            </div>
          )}
        </div>

      {/* Demand assignment panel - shown when a route is selected */}
        <div
          ref={manualAssignPanelRef}
          id='manual-demand-assign-panel'
          className='scroll-mt-4'
        >
          <ManualDemandAssignPanel
            selectedRoute={sessionRoutes.find(r => r.id === selectedRouteId) ?? null}
            eligibleStudents={eligibleStudents}
            sessionId={activeSession.id}
            loadingEligible={loadingEligible}
          />
        </div>
              </>
            )}
          </>
        )}

        {selectedRouteId && rightPanelTab === 'route-builder' && (
          <RouteDetailPanel routeId={selectedRouteId} sessionId={activeSession?.id ?? 0} />
        )}
      </div>
      <SchoolBusConfirmDialog {...deleteConfirm.dialogProps} />
      <SchoolBusConfirmDialog {...greedyConfirm.dialogProps} />
    </div>
  );
}
