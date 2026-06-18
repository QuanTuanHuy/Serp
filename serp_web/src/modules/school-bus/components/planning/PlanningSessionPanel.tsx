'use client';

import React from 'react';
import { Rocket, X, Loader2, Activity, History } from 'lucide-react';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { SchoolBusStatusBadge } from '../SchoolBusStatusBadge';
import type { SchoolBusPlanningSession } from '../../types';
import { schoolBusUi } from '../../theme';

interface PlanningSessionPanelProps {
  activeSession: SchoolBusPlanningSession | null;
  sessions: SchoolBusPlanningSession[];
  blockingTotal: number;
  canPublish: boolean;
  publishing: boolean;
  cancelling: boolean;
  onPublish: () => void;
  onCancel: () => void;
  onSelectSession: (s: SchoolBusPlanningSession) => void;
  /** When true the Past Sessions list is hidden (used in expanded map mode). */
  hidePastSessions?: boolean;
}

function StatItem({
  label,
  value,
  warn,
}: {
  label: string;
  value: string | number;
  warn?: boolean;
}) {
  return (
    <div className='rounded-xl border border-slate-100 bg-slate-50/80 px-2 py-1.5 text-center shadow-sm'>
      <p className='text-[10px] font-bold text-slate-400 uppercase tracking-wide'>
        {label}
      </p>
      <p
        className={cn(
          'text-sm font-extrabold mt-0.5',
          warn ? 'text-amber-600' : 'text-slate-800'
        )}
      >
        {value}
      </p>
    </div>
  );
}

export function PlanningSessionPanel({
  activeSession,
  sessions,
  blockingTotal,
  canPublish,
  publishing,
  cancelling,
  onPublish,
  onCancel,
  onSelectSession,
  hidePastSessions = false,
}: PlanningSessionPanelProps) {
  const [isCollapsed, setIsCollapsed] = React.useState(!!activeSession);

  React.useEffect(() => {
    if (activeSession) {
      setIsCollapsed(true);
    }
  }, [activeSession]);

  if (!activeSession && sessions.length === 0) return null;

  return (
    <div className='space-y-4'>
      {activeSession && (
        <div className='rounded-2xl border border-slate-200 bg-white p-4 shadow-sm space-y-4'>
          <div className='flex items-center justify-between border-b border-slate-100 pb-2.5'>
            <h3 className='text-sm font-bold text-slate-900 flex items-center gap-2'>
              <Activity className='h-4 w-4 text-emerald-600' />
              Active Session
            </h3>
            <SchoolBusStatusBadge status={activeSession.status} />
          </div>

          <div className='grid grid-cols-3 gap-2'>
            <StatItem
              label='Eligible'
              value={activeSession.totalEligibleStudents}
            />
            <StatItem
              label='Planned'
              value={activeSession.totalPlannedStudents}
            />
            <StatItem
              label='Unassigned'
              value={activeSession.totalUnassignedStudents}
              warn={activeSession.totalUnassignedStudents > 0}
            />
            <StatItem label='Routes' value={activeSession.totalRoutes} />
            <StatItem label='ID' value={`#${activeSession.id}`} />
            <StatItem label='Stops' value={activeSession.totalStops} />
          </div>

          <div className='flex flex-wrap gap-2 pt-2 border-t border-slate-100'>
            <Button
              onClick={onPublish}
              disabled={!canPublish || publishing}
              className={cn(
                schoolBusUi.primaryButton,
                'flex-1 justify-center rounded-xl font-semibold text-xs py-1.5 h-auto'
              )}
            >
              {publishing ? (
                <Loader2 className='mr-1.5 h-3.5 w-3.5 animate-spin' />
              ) : (
                <Rocket className='mr-1.5 h-3.5 w-3.5' />
              )}
              Publish
            </Button>
            <Button
              onClick={onCancel}
              disabled={cancelling || activeSession.status === 'CANCELLED'}
              variant='outline'
              className='flex-1 justify-center rounded-xl border-red-200 text-red-600 hover:bg-red-50 hover:text-red-700 font-semibold text-xs py-1.5 h-auto'
            >
              {cancelling ? (
                <Loader2 className='mr-1.5 h-3.5 w-3.5 animate-spin' />
              ) : (
                <X className='mr-1.5 h-3.5 w-3.5' />
              )}
              Cancel
            </Button>
          </div>

          {blockingTotal > 0 && (
            <p className='text-[11px] font-semibold text-red-600 flex items-center gap-1 bg-red-50 p-2 rounded-lg border border-red-100'>
              🚫 {blockingTotal} blocking issue(s) must be resolved before
              publishing
            </p>
          )}
        </div>
      )}

      {sessions.length > 0 && !hidePastSessions && (
        <div className='rounded-2xl border border-slate-200 bg-white p-4 shadow-sm space-y-3'>
          <div
            className='border-b border-slate-100 pb-2.5 flex items-center justify-between cursor-pointer select-none'
            onClick={() => setIsCollapsed((c) => !c)}
          >
            <h3 className='text-sm font-bold text-slate-900 flex items-center gap-2'>
              <History className='h-4 w-4 text-slate-500' />
              Past Sessions ({sessions.length})
            </h3>
            <span className='text-[10px] font-bold text-slate-400 uppercase tracking-wider hover:text-slate-650 transition-colors'>
              {isCollapsed ? 'Show' : 'Hide'}
            </span>
          </div>

          {!isCollapsed && (
            <div className='max-h-56 space-y-1.5 overflow-y-auto pr-1'>
              {sessions.map((s) => (
                <button
                  key={s.id}
                  onClick={() => onSelectSession(s)}
                  className={cn(
                    'w-full rounded-xl border px-3 py-2.5 text-left transition-all duration-200',
                    activeSession?.id === s.id
                      ? 'border-indigo-500 bg-indigo-50/40 ring-1 ring-indigo-500'
                      : 'border-slate-150 hover:border-slate-250 hover:bg-slate-50'
                  )}
                >
                  <div className='flex items-center justify-between'>
                    <span className='text-xs font-bold text-slate-800'>
                      #{s.id} · {s.serviceDate}
                    </span>
                    <SchoolBusStatusBadge status={s.status} />
                  </div>
                  <p className='mt-1 text-[10px] font-semibold text-slate-500'>
                    {s.schoolName} · {s.routeDirection}
                  </p>
                </button>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
