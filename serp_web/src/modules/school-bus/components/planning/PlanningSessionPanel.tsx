'use client';

import React from 'react';
import { Zap, Rocket, X, Loader2 } from 'lucide-react';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { SchoolBusSection } from '../SchoolBusSection';
import { SchoolBusStatusBadge } from '../SchoolBusStatusBadge';
import { schoolBusUi } from '../../theme';
import type { SchoolBusPlanningSession, PlanningMethod } from '../../types';

const methodLabel: Record<PlanningMethod, string> = { MANUAL: 'Manual', GREEDY: 'Greedy' };

interface PlanningSessionPanelProps {
  activeSession: SchoolBusPlanningSession | null;
  sessions: SchoolBusPlanningSession[];
  planningMethod: PlanningMethod;
  blockingTotal: number;
  canPublish: boolean;
  generating: boolean;
  publishing: boolean;
  cancelling: boolean;
  hasRoutes: boolean;
  onGenerate: () => void;
  onPublish: () => void;
  onCancel: () => void;
  onSelectSession: (s: SchoolBusPlanningSession) => void;
  /** When true the Past Sessions list is hidden (used in expanded map mode). */
  hidePastSessions?: boolean;
}

function StatItem({ label, value, warn }: { label: string; value: string | number; warn?: boolean }) {
  return (
    <div className='rounded-xl border border-slate-100 bg-slate-50/80 px-3 py-2'>
      <p className='text-[11px] text-slate-400'>{label}</p>
      <p className={cn('text-base font-bold', warn ? 'text-amber-600' : 'text-slate-900')}>{value}</p>
    </div>
  );
}

export function PlanningSessionPanel({
  activeSession, sessions, planningMethod, blockingTotal, canPublish,
  generating, publishing, cancelling, hasRoutes,
  onGenerate, onPublish, onCancel, onSelectSession,
  hidePastSessions = false,
}: PlanningSessionPanelProps) {
  if (!activeSession && sessions.length === 0) return null;

  return (
    <div className='space-y-4'>
      {activeSession && (
        <SchoolBusSection title='Active Session' action={<SchoolBusStatusBadge status={activeSession.status} />}>
          <div className='grid grid-cols-3 gap-2'>
            <StatItem label='Eligible' value={activeSession.totalEligibleStudents} />
            <StatItem label='Planned' value={activeSession.totalPlannedStudents} />
            <StatItem label='Unassigned' value={activeSession.totalUnassignedStudents} warn={activeSession.totalUnassignedStudents > 0} />
            <StatItem label='Routes' value={activeSession.totalRoutes} />
            <StatItem label='Session ID' value={`#${activeSession.id}`} />
            <StatItem label='Method' value={methodLabel[activeSession.planningMethod]} />
          </div>

          <div className='mt-4 flex flex-wrap gap-2'>
            {planningMethod === 'GREEDY' && (
              <Button onClick={onGenerate} disabled={generating || activeSession.status === 'PUBLISHED' || activeSession.status === 'CANCELLED'}
                variant='outline' className={schoolBusUi.outlineButton}>
                {generating ? <Loader2 className='mr-1.5 h-3.5 w-3.5 animate-spin' /> : <Zap className='mr-1.5 h-3.5 w-3.5' />}
                Generate Routes
              </Button>
            )}
            <Button onClick={onPublish} disabled={!canPublish || publishing} className={cn('rounded-full bg-emerald-600 text-white shadow-sm hover:bg-emerald-700')}>
              {publishing ? <Loader2 className='mr-1.5 h-3.5 w-3.5 animate-spin' /> : <Rocket className='mr-1.5 h-3.5 w-3.5' />}
              Publish
            </Button>
            <Button onClick={onCancel} disabled={cancelling || activeSession.status === 'CANCELLED'} variant='outline'
              className='rounded-full border-rose-200 text-rose-600 hover:bg-rose-50 hover:text-rose-700'>
              {cancelling ? <Loader2 className='mr-1.5 h-3.5 w-3.5 animate-spin' /> : <X className='mr-1.5 h-3.5 w-3.5' />}
              Cancel
            </Button>
          </div>

          {blockingTotal > 0 && (
            <p className='mt-3 text-xs font-medium text-rose-600'>
              🚫 {blockingTotal} blocking issue(s) must be resolved before publishing
            </p>
          )}
        </SchoolBusSection>
      )}

      {sessions.length > 0 && !hidePastSessions && (
        <SchoolBusSection title={`Past Sessions (${sessions.length})`}>
          <div className='max-h-56 space-y-1.5 overflow-y-auto pr-1'>
            {sessions.map(s => (
              <button key={s.id} onClick={() => onSelectSession(s)}
                className={cn(
                  'w-full rounded-xl border px-3 py-2.5 text-left transition',
                  activeSession?.id === s.id
                    ? 'border-rose-200 bg-rose-50/60'
                    : 'border-transparent hover:border-slate-200 hover:bg-slate-50',
                )}>
                <div className='flex items-center justify-between'>
                  <span className='text-sm font-semibold text-slate-800'>#{s.id} · {s.serviceDate}</span>
                  <SchoolBusStatusBadge status={s.status} />
                </div>
                <p className='mt-0.5 text-xs text-slate-400'>
                  {s.schoolName} · {s.routeDirection} · {methodLabel[s.planningMethod]}
                </p>
              </button>
            ))}
          </div>
        </SchoolBusSection>
      )}
    </div>
  );
}
