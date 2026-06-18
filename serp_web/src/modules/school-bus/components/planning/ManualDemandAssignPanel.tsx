'use client';

import React, { useState } from 'react';
import { UserPlus, CheckCircle2, AlertCircle, Loader2 } from 'lucide-react';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { SchoolBusSection } from '../SchoolBusSection';
import { useAssignStudentToRouteMutation } from '../../api/schoolBusApi';
import type { SchoolBusEligibleStudent, SchoolBusRoute } from '../../types';

interface ManualDemandAssignPanelProps {
  /** The route to assign students to */
  selectedRoute: SchoolBusRoute | null;
  /** All eligible students for the current session */
  eligibleStudents: SchoolBusEligibleStudent[];
  /** Session ID — used for cache invalidation */
  sessionId: number;
  /** Loading state for eligible students query */
  loadingEligible?: boolean;
}

export function ManualDemandAssignPanel({
  selectedRoute,
  eligibleStudents,
  sessionId,
  loadingEligible,
}: ManualDemandAssignPanelProps) {
  const [assignStudentToRoute] = useAssignStudentToRouteMutation();
  const [lastAssigned, setLastAssigned] = useState<number | null>(null);
  const [loadingStudentId, setLoadingStudentId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  // Filter to unassigned students (those with a relevant point that hasn't been assigned yet)
  const direction = selectedRoute?.routeDirection;

  const handleAssign = async (student: SchoolBusEligibleStudent) => {
    if (!selectedRoute) return;
    setError(null);
    setLoadingStudentId(student.studentId);
    try {
      await assignStudentToRoute({
        routeId: selectedRoute.id,
        sessionId,
        body: {
          studentId: student.studentId,
          subscriptionId: student.subscriptionId,
        },
      }).unwrap();
      setLastAssigned(student.studentId);
      setTimeout(() => setLastAssigned(null), 2000);
    } catch (e: unknown) {
      const err = e as { data?: { message?: string } };
      setError(err?.data?.message ?? 'Failed to assign student');
    } finally {
      setLoadingStudentId(null);
    }
  };

  if (!selectedRoute) {
    return (
      <div className='rounded-2xl border border-slate-100 bg-slate-50/40 px-4 py-6 text-center'>
        <UserPlus className='mx-auto mb-2 h-8 w-8 text-slate-300' />
        <p className='text-sm font-medium text-slate-500'>
          Select a route to assign students
        </p>
        <p className='mt-1 text-xs text-slate-400'>
          Click a route card above to select it
        </p>
      </div>
    );
  }

  const editableStatuses = ['DRAFT', 'GENERATED', 'REVIEWING'];
  if (!editableStatuses.includes(selectedRoute.status)) {
    return (
      <div className='rounded-2xl border border-amber-100 bg-amber-50/40 px-4 py-4 text-center'>
        <p className='text-sm font-medium text-amber-700'>
          Route is {selectedRoute.status} — no further edits allowed
        </p>
      </div>
    );
  }

  const routeCapacity =
    selectedRoute.busCapacity ?? selectedRoute.assignedBusCapacity ?? null;
  const routeStudentCount = selectedRoute.plannedStudentCount ?? 0;

  if (!selectedRoute.busId || routeCapacity == null) {
    return (
      <SchoolBusSection title='Add Students'>
        <div className='rounded-2xl border border-amber-100 bg-amber-50/10 px-4 py-6 text-center'>
          <AlertCircle className='mx-auto mb-2 h-8 w-8 text-amber-500' />
          <p className='text-sm font-bold text-amber-800'>
            Choose a bus before adding students.
          </p>
          <p className='mt-1.5 text-xs text-slate-500 leading-normal px-2'>
            Bus capacity is required before students can be assigned to this
            route.
          </p>
        </div>
      </SchoolBusSection>
    );
  }

  if (routeStudentCount >= routeCapacity) {
    return (
      <SchoolBusSection title='Add Students'>
        <div className='rounded-2xl border border-red-100 bg-red-50/10 px-4 py-6 text-center'>
          <AlertCircle className='mx-auto mb-2 h-8 w-8 text-red-500' />
          <p className='text-sm font-bold text-red-800'>
            Route has reached bus capacity.
          </p>
          <p className='mt-1.5 text-xs text-slate-500 leading-normal px-2'>
            Students: {routeStudentCount}/{routeCapacity}. Choose a larger bus
            or create another route.
          </p>
        </div>
      </SchoolBusSection>
    );
  }

  // Case C: Loading state
  if (loadingEligible) {
    return (
      <SchoolBusSection title='Add Students'>
        <div className='flex items-center gap-2 py-8 justify-center text-slate-500'>
          <Loader2 className='h-4 w-4 animate-spin text-blue-600' />
          <p className='text-xs font-semibold'>Loading assignable demand...</p>
        </div>
      </SchoolBusSection>
    );
  }

  const eligibleCount = eligibleStudents.length;
  // Unassigned = has no assignedRouteId
  const unassignedStudents = eligibleStudents.filter((s) => !s.assignedRouteId);
  const unassignedCount = unassignedStudents.length;

  // Assignable = unassigned and has valid stop for current route direction
  const assignableStudents = unassignedStudents.filter((student) => {
    const hasPoint =
      direction === 'OUTBOUND'
        ? Boolean(student.pickupPointId)
        : Boolean(student.dropoffPointId);
    return hasPoint;
  });
  const assignableCount = assignableStudents.length;

  if (eligibleCount === 0) {
    return (
      <SchoolBusSection title='Add Students'>
        <div className='rounded-2xl border border-slate-100 bg-slate-50/40 px-4 py-6 text-center'>
          <p className='text-xs text-slate-400'>
            No eligible students for this session.
          </p>
        </div>
      </SchoolBusSection>
    );
  }

  // Case A: No unassigned students left
  if (unassignedCount === 0) {
    return (
      <SchoolBusSection title='Add Students'>
        <div className='rounded-2xl border border-emerald-100 bg-emerald-50/10 px-4 py-6 text-center'>
          <CheckCircle2 className='mx-auto mb-2 h-8 w-8 text-emerald-500' />
          <p className='text-sm font-semibold text-emerald-800'>
            All eligible students have been assigned.
          </p>
          <p className='mt-1 text-xs text-slate-400'>
            Every student is assigned to a route in this session.
          </p>
        </div>
      </SchoolBusSection>
    );
  }

  // Case B: Unassigned exist but none can be assigned to this route
  if (assignableCount === 0) {
    return (
      <SchoolBusSection title='Add Students'>
        <div className='rounded-2xl border border-amber-100 bg-amber-50/10 px-4 py-6 text-center'>
          <AlertCircle className='mx-auto mb-2 h-8 w-8 text-amber-500' />
          <p className='text-sm font-bold text-amber-800'>
            No assignable students for this route.
          </p>
          <p className='mt-1.5 text-xs text-slate-500 leading-normal px-2'>
            There are unassigned students, but none can be added to this route
            because of direction, capacity, or pickup/drop-off constraints.
          </p>
        </div>
      </SchoolBusSection>
    );
  }

  return (
    <SchoolBusSection
      title={`Add Students to "${selectedRoute.routeName}"`}
      action={
        <span className='rounded-full bg-slate-100 px-2 py-0.5 text-[10px] font-semibold text-slate-700'>
          {direction}
        </span>
      }
    >
      {error && (
        <div className='mb-3 flex items-center gap-2 rounded-xl border border-red-200 bg-red-50/60 px-3 py-2 text-xs font-medium text-red-700'>
          <AlertCircle className='h-3.5 w-3.5 shrink-0' />
          {error}
        </div>
      )}

      {/* Demand state counters */}
      <div className='grid grid-cols-3 gap-2 mb-3 bg-slate-50/60 p-2 rounded-xl border border-slate-100 text-center'>
        <div className='flex flex-col items-center'>
          <span className='text-[10px] font-bold text-slate-400 uppercase tracking-wider'>
            Eligible
          </span>
          <span className='text-sm font-extrabold text-slate-750 mt-0.5'>
            {eligibleCount}
          </span>
        </div>
        <div className='flex flex-col items-center border-x border-slate-200/50'>
          <span className='text-[10px] font-bold text-slate-400 uppercase tracking-wider'>
            Unassigned
          </span>
          <span className='text-sm font-extrabold text-slate-750 mt-0.5'>
            {unassignedCount}
          </span>
        </div>
        <div className='flex flex-col items-center'>
          <span className='text-[10px] font-bold text-[#C81E3A] uppercase tracking-wider'>
            Assignable
          </span>
          <span className='text-sm font-extrabold text-[#C81E3A] mt-0.5'>
            {assignableCount}
          </span>
        </div>
      </div>

      <p className='mb-3 text-[11px] text-slate-455 leading-relaxed'>
        Add eligible students to the selected route. Stops will be created
        automatically from pickup/drop-off points.
      </p>

      <div className='max-h-80 space-y-1.5 overflow-y-auto pr-1'>
        {assignableStudents.map((student) => {
          const point =
            direction === 'OUTBOUND'
              ? student.pickupPointName
              : student.dropoffPointName;
          const hasPoint =
            direction === 'OUTBOUND'
              ? Boolean(student.pickupPointId)
              : Boolean(student.dropoffPointId);
          const isJustAssigned = lastAssigned === student.studentId;
          const isThisLoading = loadingStudentId === student.studentId;

          return (
            <div
              key={student.studentId}
              className={cn(
                'flex items-center justify-between rounded-xl border px-3 py-2 transition',
                isJustAssigned
                  ? 'border-emerald-200 bg-emerald-50/60'
                  : 'border-slate-100 bg-slate-50/40 hover:border-slate-200 hover:bg-slate-50'
              )}
            >
              <div className='flex-1 min-w-0'>
                <div className='flex items-center gap-1.5'>
                  <p className='text-xs font-bold text-slate-800 truncate'>
                    {student.studentName}
                  </p>
                  <span className='px-1.5 py-0.5 bg-slate-100 border border-slate-200 rounded text-slate-600 text-[8px] font-semibold scale-95 shrink-0'>
                    {student.tripOption === 'ROUND_TRIP'
                      ? 'Round-trip'
                      : student.tripOption}
                  </span>
                </div>
                <p className='mt-0.5 text-[10px] truncate text-slate-450 flex items-center gap-1'>
                  {hasPoint ? (
                    <span>📍 {point ?? '—'}</span>
                  ) : (
                    <span className='text-amber-600'>
                      ⚠️ No {direction === 'OUTBOUND' ? 'pickup' : 'dropoff'}{' '}
                      point
                    </span>
                  )}
                </p>
              </div>

              {isJustAssigned ? (
                <CheckCircle2 className='ml-2 h-4 w-4 shrink-0 text-emerald-500' />
              ) : (
                <Button
                  size='sm'
                  variant='outline'
                  disabled={!hasPoint || loadingStudentId !== null}
                  onClick={() => handleAssign(student)}
                  className={cn(
                    'ml-2 shrink-0 rounded-lg border-slate-200 px-3 py-1 text-[11px] font-semibold text-slate-850',
                    'hover:bg-slate-50 hover:border-slate-300 disabled:opacity-40 shadow-none'
                  )}
                >
                  {isThisLoading ? (
                    <Loader2 className='h-3 w-3 animate-spin text-slate-500' />
                  ) : (
                    'Add'
                  )}
                </Button>
              )}
            </div>
          );
        })}
      </div>
    </SchoolBusSection>
  );
}
