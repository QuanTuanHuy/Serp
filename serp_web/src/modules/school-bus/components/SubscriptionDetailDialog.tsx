'use client';

import {
  X,
  CalendarDays,
  User,
  School,
  MapPin,
  Clock,
  Hash,
  ExternalLink,
} from 'lucide-react';
import { useGetSchoolBusSubscriptionByIdQuery } from '../api/schoolBusApi';
import { SchoolBusStatusBadge } from './SchoolBusStatusBadge';
import { formatDate } from '../utils';
import { cn } from '@/shared/utils';

/* -- Day chip helpers ------------------------------------------------------- */
const DAYS: { key: keyof DayFields; label: string }[] = [
  { key: 'monday', label: 'Mon' },
  { key: 'tuesday', label: 'Tue' },
  { key: 'wednesday', label: 'Wed' },
  { key: 'thursday', label: 'Thu' },
  { key: 'friday', label: 'Fri' },
  { key: 'saturday', label: 'Sat' },
  { key: 'sunday', label: 'Sun' },
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

/* -- Field row -------------------------------------------------------------- */
function Field({
  label,
  value,
  mono,
}: {
  label: string;
  value: React.ReactNode;
  mono?: boolean;
}) {
  return (
    <div className='flex flex-col gap-0.5'>
      <span className='text-[11px] font-medium uppercase tracking-wide text-slate-400'>
        {label}
      </span>
      <span className={cn('text-sm text-foreground', mono && 'font-mono')}>
        {value || '-'}
      </span>
    </div>
  );
}

/* -- Section ---------------------------------------------------------------- */
function Section({
  title,
  icon: Icon,
  children,
}: {
  title: string;
  icon: React.ElementType;
  children: React.ReactNode;
}) {
  return (
    <div className='rounded-2xl border border-border bg-muted/40 p-4'>
      <div className='mb-3 flex items-center gap-2'>
        <Icon className='h-4 w-4 text-muted-foreground' />
        <p className='text-xs font-semibold uppercase tracking-wide text-muted-foreground'>
          {title}
        </p>
      </div>
      <div className='grid grid-cols-2 gap-x-6 gap-y-3'>{children}</div>
    </div>
  );
}

/* -- Main dialog ------------------------------------------------------------ */
interface Props {
  subscriptionId: number;
  onClose: () => void;
}

export function SubscriptionDetailDialog({ subscriptionId, onClose }: Props) {
  const { data, isLoading } =
    useGetSchoolBusSubscriptionByIdQuery(subscriptionId);
  const sub = data?.data;

  return (
    /* Backdrop */
    <div
      className='fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm'
      onClick={onClose}
    >
      {/* Panel */}
      <div
        className='relative mx-4 w-full max-w-xl overflow-hidden rounded-3xl border border-border bg-card text-card-foreground shadow-2xl shadow-black/20'
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className='flex items-center justify-between border-b border-border px-6 py-4'>
          <div className='flex items-center gap-3'>
            <div className='flex h-9 w-9 items-center justify-center rounded-xl bg-muted'>
              <CalendarDays className='h-4 w-4 text-muted-foreground' />
            </div>
            <div>
              <h2 className='text-base font-bold text-foreground'>
                Subscription Detail
              </h2>
              {sub && (
                <p className='text-xs font-mono text-slate-400'>
                  {sub.subscriptionCode}
                </p>
              )}
            </div>
          </div>
          <div className='flex items-center gap-2'>
            {sub && <SchoolBusStatusBadge status={sub.status} />}
            <button
              onClick={onClose}
              className='flex h-8 w-8 items-center justify-center rounded-xl text-muted-foreground transition hover:bg-muted hover:text-foreground'
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
              <div className='h-8 w-8 animate-spin rounded-full border-2 border-border border-t-foreground' />
            </div>
          )}

          {!isLoading && !sub && (
            <p className='py-8 text-center text-sm text-slate-400'>
              Could not load subscription detail.
            </p>
          )}

          {!isLoading && sub && (
            <>
              {/* Identity */}
              <Section title='Identity' icon={Hash}>
                <Field
                  label='Subscription Code'
                  value={sub.subscriptionCode}
                  mono
                />
                <Field
                  label='Status'
                  value={<SchoolBusStatusBadge status={sub.status} />}
                />
                <Field
                  label='Source Request ID'
                  value={sub.sourceRequestId || '-'}
                />
                <div /> {/* spacer */}
              </Section>

              {/* Student & School */}
              <Section title='Student & School' icon={User}>
                <Field label='Student' value={sub.studentName} />
                <Field label='School' value={sub.schoolName} />
                <Field label='Trip Option' value={sub.tripOption} />
                <div />
              </Section>

              {/* Pickup & Dropoff */}
              <Section title='Pickup & Dropoff' icon={MapPin}>
                <Field
                  label='Pickup Point'
                  value={sub.pickupPointName || '-'}
                />
                <Field
                  label='Dropoff Point'
                  value={sub.dropoffPointName || '-'}
                />
              </Section>

              {/* Active days */}
              <Section title='Active Days' icon={CalendarDays}>
                {/* Active days */}
                <div className='col-span-2'>
                  <span className='text-[11px] font-medium uppercase tracking-wide text-slate-400'>
                    Active Days
                  </span>
                  <div className='mt-1.5 flex flex-wrap gap-1.5'>
                    {DAYS.map(({ key, label }) => (
                      <span
                        key={key}
                        className={cn(
                          'rounded-lg px-2.5 py-0.5 text-xs font-semibold',
                          sub[key as keyof typeof sub]
                            ? 'bg-[#C81E3A] text-white font-medium'
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
                <Field
                  label='To'
                  value={
                    sub.effectiveTo ? formatDate(sub.effectiveTo) : 'Indefinite'
                  }
                />
              </Section>
            </>
          )}
        </div>

        {/* Footer */}
        <div className='flex items-center justify-end border-t border-border px-6 py-3'>
          <button
            onClick={onClose}
            className='rounded-xl bg-muted px-4 py-1.5 text-sm font-medium text-muted-foreground transition hover:text-foreground'
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
}

