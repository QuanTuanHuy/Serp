'use client';

import {
  Bus,
  Flag,
  FlagTriangleRight,
  GraduationCap,
  MapPin,
  MapPinCheck,
  User,
  Warehouse,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { schoolBusBrand } from '../../theme';
import { markerColors, type MarkerKind } from './mapIcons';
import { useMapMarkerVisibility } from './MapMarkerVisibilityContext';

interface SchoolBusMapLegendProps {
  className?: string;
}

const legendItems: {
  kind: MarkerKind;
  label: string;
  icon: React.ComponentType<{ className?: string }>;
}[] = [
  { kind: 'school', label: 'School', icon: GraduationCap },
  { kind: 'depot', label: 'Depot', icon: Warehouse },
  { kind: 'pickup', label: 'Pickup stop', icon: MapPin },
  { kind: 'dropoff', label: 'Drop-off stop', icon: MapPinCheck },
  { kind: 'start', label: 'Start point', icon: Flag },
  { kind: 'end', label: 'End point', icon: FlagTriangleRight },
  { kind: 'bus', label: 'Bus context', icon: Bus },
  { kind: 'student', label: 'Student', icon: User },
];

export function SchoolBusMapLegend({ className }: SchoolBusMapLegendProps) {
  const { isVisible, toggleKind } = useMapMarkerVisibility();

  return (
    <div
      className={cn(
        'rounded-2xl border border-slate-200 bg-white/95 p-3 shadow-[0_12px_28px_rgba(15,23,42,0.08)]',
        className
      )}
    >
      <p className='text-xs font-semibold uppercase tracking-[0.14em] text-slate-500'>
        Map legend
        <span className='ml-1.5 text-[10px] font-normal normal-case tracking-normal text-slate-400'>
          (click to toggle)
        </span>
      </p>
      <div className='mt-3 grid gap-1 text-xs text-slate-700'>
        {legendItems.map((item) => (
          <LegendRow
            key={item.kind}
            {...item}
            active={isVisible(item.kind)}
            onToggle={() => toggleKind(item.kind)}
          />
        ))}
      </div>
      <div className='mt-3 space-y-1 text-xs'>
        <p className='flex items-center gap-2 text-rose-700'>
          <span
            className='block h-[2px] w-8 rounded-full'
            style={{ backgroundColor: schoolBusBrand.rose }}
          />
          Outbound route
        </p>
        <p className='flex items-center gap-2 text-emerald-700'>
          <span
            className='block h-[2px] w-8 rounded-full border-t-2 border-dashed'
            style={{ borderColor: schoolBusBrand.emerald }}
          />
          Return route
        </p>
      </div>
    </div>
  );
}

function LegendRow({
  kind,
  label,
  icon: Icon,
  active,
  onToggle,
}: {
  kind: MarkerKind;
  label: string;
  icon: React.ComponentType<{ className?: string }>;
  active: boolean;
  onToggle: () => void;
}) {
  const color = markerColors[kind];
  return (
    <button
      type='button'
      onClick={onToggle}
      className={cn(
        'flex w-full items-center gap-2 rounded-lg px-1.5 py-1 text-left transition-all duration-200',
        'hover:bg-slate-50',
        active
          ? 'text-slate-700'
          : 'text-slate-300 line-through decoration-slate-300'
      )}
      title={active ? `Hide ${label} markers` : `Show ${label} markers`}
    >
      <span
        className={cn(
          'inline-flex h-6 w-6 shrink-0 items-center justify-center rounded-full border-2 transition-all duration-200'
        )}
        style={{
          backgroundColor: active ? color.bg : '#f8fafc',
          borderColor: active ? color.border : '#e2e8f0',
          color: active ? color.stroke : '#cbd5e1',
        }}
      >
        <Icon className='h-3.5 w-3.5' />
      </span>
      <span className='flex-1'>{label}</span>
      <span
        className={cn(
          'h-2 w-2 rounded-full transition-all duration-200',
          active ? 'bg-emerald-400' : 'bg-slate-200'
        )}
      />
    </button>
  );
}
