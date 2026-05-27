'use client';

import React, { useState } from 'react';
import { UserPlus, CheckCircle2, AlertCircle, Loader2 } from 'lucide-react';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { SchoolBusSection } from '../SchoolBusSection';
import { useAddStudentToStopMutation } from '../../api/schoolBusApi';
import type { SchoolBusEligibleStudent, SchoolBusRoute } from '../../types';

interface ManualDemandAssignPanelProps {
  /** The route to assign students to */
  selectedRoute: SchoolBusRoute | null;
  /** All eligible students for the current session */
  eligibleStudents: SchoolBusEligibleStudent[];
  /** Session ID — used for cache invalidation */
  sessionId: number;
}

export function ManualDemandAssignPanel({
  selectedRoute,
  eligibleStudents,
  sessionId,
}: ManualDemandAssignPanelProps) {
  const [addStudentToStop] = useAddStudentToStopMutation();
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
      await addStudentToStop({
        routeId: selectedRoute.id,
        sessionId,
        body: { studentId: student.studentId, subscriptionId: student.subscriptionId },
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
        <p className='text-sm font-medium text-slate-500'>Select a route to assign students</p>
        <p className='mt-1 text-xs text-slate-400'>Click a route card above to select it</p>
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

  if (eligibleStudents.length === 0) {
    return (
      <SchoolBusSection title='Assign Demand'>
        <p className='text-xs text-slate-400'>No eligible students for this session.</p>
      </SchoolBusSection>
    );
  }

  return (
    <SchoolBusSection
      title={`Assign to "${selectedRoute.routeName}"`}
      action={
        <span className='rounded-full bg-rose-100 px-2 py-0.5 text-[10px] font-semibold text-rose-700'>
          {direction}
        </span>
      }
    >
      {error && (
        <div className='mb-3 flex items-center gap-2 rounded-xl border border-rose-200 bg-rose-50/60 px-3 py-2 text-xs font-medium text-rose-700'>
          <AlertCircle className='h-3.5 w-3.5 shrink-0' />
          {error}
        </div>
      )}

      <p className='mb-2 text-[11px] text-slate-400'>
        Click <strong>Add</strong> to assign a student to this route.
        A stop will be auto-created if needed.
      </p>

      <div className='max-h-80 space-y-1.5 overflow-y-auto pr-1'>
        {eligibleStudents.map(student => {
          const point = direction === 'OUTBOUND'
            ? student.pickupPointName
            : student.dropoffPointName;
          const hasPoint = direction === 'OUTBOUND'
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
                  : 'border-slate-100 bg-slate-50/40 hover:border-slate-200 hover:bg-slate-50',
              )}
            >
              <div className='flex-1 min-w-0'>
                <p className='text-sm font-semibold text-slate-800 truncate'>
                  {student.studentName}
                </p>
                <p className='mt-0.5 text-[11px] truncate text-slate-400'>
                  {hasPoint ? (
                    <>📍 {point ?? '—'}</>
                  ) : (
                    <span className='text-amber-600'>⚠️ No {direction === 'OUTBOUND' ? 'pickup' : 'dropoff'} point</span>
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
                    'ml-2 shrink-0 rounded-full border-rose-200 px-3 py-1 text-[11px] font-semibold text-rose-600',
                    'hover:bg-rose-50 hover:border-rose-300 disabled:opacity-40',
                  )}
                >
                  {isThisLoading ? <Loader2 className='h-3 w-3 animate-spin' /> : 'Add'}
                </Button>
              )}
            </div>
          );
        })}
      </div>
    </SchoolBusSection>
  );
}
