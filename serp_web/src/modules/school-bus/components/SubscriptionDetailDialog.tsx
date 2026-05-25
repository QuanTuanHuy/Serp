'use client';

import { X, CalendarDays, User, School, MapPin, Clock, Hash, ExternalLink } from 'lucide-react';
import { useGetSubscriptionByIdQuery } from '../api/schoolBusApi';
import { SchoolBusStatusBadge } from './SchoolBusStatusBadge';
import { formatDate } from '../utils';
import { cn } from '@/shared/utils';

/* ── Day chip helpers ─────────────────────────────────────────────────────── */
const DAYS: { key: keyof DayFields; label: string }[] = [
  { key: 'monday',    label: 'Mon' },
  { key: 'tuesday',  label: 'Tue' },
  { key: 'wednesday',label: 'Wed' },
  { key: 'thursday', label: 'Thu' },
  { key: 'friday',   label: 'Fri' },
  { key: 'saturday', label: 'Sat' },
  { key: 'sunday',   label: 'Sun' },
];

interface DayFields {
  monday: boolean;
  tuesday: boolean;
  wednesday: boolean;
  thursday: boolean;
  friday: boolean;
  saturday: boolean;
  sunday: boolean;
}

/* ── Field row ────────────────────────────────────────────────────────────── */
function Field({ label, value, mono }: { label: string; value: React.ReactNode; mono?: boolean }) {
  return (
    <div className='flex flex-col gap-0.5'>
      <span className='text-[11px] font-medium uppercase tracking-wide text-slate-400'>{label}</span>
      <span className={cn('text-sm text-slate-800', mono && 'font-mono')}>{value || '—'}</span>
    </div>
  );
}

/* ── Section ──────────────────────────────────────────────────────────────── */
function Section({ title, icon: Icon, children }: { title: string; icon: React.ElementType; children: React.ReactNode }) {
  return (
    <div className='rounded-2xl border border-slate-100 bg-slate-50/60 p-4'>
      <div className='mb-3 flex items-center gap-2'>
        <Icon className='h-4 w-4 text-rose-400' />
        <p className='text-xs font-semibold uppercase tracking-wide text-slate-500'>{title}</p>
      </div>
      <div className='grid grid-cols-2 gap-x-6 gap-y-3'>{children}</div>
    </div>
  );
}

/* ── Main dialog ──────────────────────────────────────────────────────────── */
interface Props {
  subscriptionId: number;
  onClose: () => void;
}

export function SubscriptionDetailDialog({ subscriptionId, onClose }: Props) {
  const { data, isLoading } = useGetSubscriptionByIdQuery(subscriptionId);
  const sub = data?.data;

  return (
    /* Backdrop */
    <div
      className='fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm'
      onClick={onClose}
    >
      {/* Panel */}
      <div
        className='relative w-full max-w-xl mx-4 rounded-3xl border border-slate-200 bg-white shadow-2xl shadow-slate-900/20 overflow-hidden'
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className='flex items-center justify-between border-b border-slate-100 px-6 py-4'>
          <div className='flex items-center gap-3'>
            <div className='flex h-9 w-9 items-center justify-center rounded-xl bg-rose-50'>
              <CalendarDays className='h-4 w-4 text-rose-500' />
            </div>
            <div>
              <h2 className='text-base font-bold text-slate-900'>Subscription Detail</h2>
              {sub && (
                <p className='text-xs font-mono text-slate-400'>{sub.subscriptionCode}</p>
              )}
            </div>
          </div>
          <div className='flex items-center gap-2'>
            {sub && <SchoolBusStatusBadge status={sub.status} />}
            <button
              onClick={onClose}
              className='flex h-8 w-8 items-center justify-center rounded-xl text-slate-400 transition hover:bg-slate-100 hover:text-slate-700'
              aria-label='Close'
            >
              <X className='h-4 w-4' />
            </button>
          </div>
        </div>

        {/* Body */}
        <div className='max-h-[70vh] overflow-y-auto px-6 py-5 space-y-4'>
          {isLoading && (
            <div className='flex items-center justify-center py-12'>
              <div className='h-8 w-8 animate-spin rounded-full border-2 border-rose-200 border-t-rose-500' />
            </div>
          )}

          {!isLoading && !sub && (
            <p className='py-8 text-center text-sm text-slate-400'>Could not load subscription detail.</p>
          )}

          {!isLoading && sub && (
            <>
              {/* Identity */}
              <Section title='Identity' icon={Hash}>
                <Field label='Subscription Code' value={sub.subscriptionCode} mono />
                <Field label='Status' value={<SchoolBusStatusBadge status={sub.status} />} />
                <Field label='Source Request ID' value={sub.sourceRequestId ?? '—'} />
                <div /> {/* spacer */}
              </Section>

              {/* Student & School */}
              <Section title='Student & School' icon={User}>
                <Field label='Student' value={sub.studentName} />
                <Field label='School' value={sub.schoolName} />
                <Field label='Schedule' value={sub.schoolScheduleName ?? '—'} />
                <Field label='Trip Option' value={sub.tripOption} />
              </Section>

              {/* Pickup & Dropoff */}
              <Section title='Pickup & Dropoff' icon={MapPin}>
                <Field
                  label='Pickup Point'
                  value={sub.pickupPointName ?? '—'}
                />
                <Field
                  label='Dropoff Point'
                  value={sub.dropoffPointName ?? '—'}
                />
              </Section>

              {/* Schedule */}
              <Section title='Schedule' icon={CalendarDays}>
                {/* Active days */}
                <div className='col-span-2'>
                  <span className='text-[11px] font-medium uppercase tracking-wide text-slate-400'>Active Days</span>
                  <div className='mt-1.5 flex flex-wrap gap-1.5'>
                    {DAYS.map(({ key, label }) => (
                      <span
                        key={key}
                        className={cn(
                          'rounded-lg px-2.5 py-0.5 text-xs font-semibold',
                          sub[key as keyof typeof sub]
                            ? 'bg-rose-100 text-rose-700'
                            : 'bg-slate-100 text-slate-400 line-through'
                        )}
                      >
                        {label}
                      </span>
                    ))}
                  </div>
                </div>
              </Section>

              {/* Effective Period */}
              <Section title='Effective Period' icon={Clock}>
                <Field label='From' value={formatDate(sub.effectiveFrom)} />
                <Field label='To' value={sub.effectiveTo ? formatDate(sub.effectiveTo) : 'Indefinite'} />
              </Section>
            </>
          )}
        </div>

        {/* Footer */}
        <div className='flex items-center justify-end border-t border-slate-100 px-6 py-3'>
          <button
            onClick={onClose}
            className='rounded-xl bg-slate-100 px-4 py-1.5 text-sm font-medium text-slate-600 transition hover:bg-slate-200'
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
