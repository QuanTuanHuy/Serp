'use client';

import * as React from 'react';
import {
  Bus,
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

export interface LegendItem {
  kind: MarkerKind;
  label: string;
  icon: React.ComponentType<{ className?: string }>;
}

interface SchoolBusMapLegendProps {
  className?: string;
  /** Override the default legend items (e.g. show only a subset). */
  items?: LegendItem[];
  /** Whether to show the route-line legend section. Default true. */
  showRouteLines?: boolean;
  /** Defaults to true so the legend starts collapsed/hidden by default to avoid taking space. */
  defaultCollapsed?: boolean;
}

const DEFAULT_LEGEND_ITEMS: LegendItem[] = [
  { kind: 'school', label: 'Trường học', icon: GraduationCap },
  { kind: 'depot', label: 'Bãi xe', icon: Warehouse },
  { kind: 'pickup', label: 'Điểm đón', icon: MapPin },
  { kind: 'dropoff', label: 'Điểm trả', icon: MapPinCheck },
  { kind: 'bus', label: 'Xe buýt', icon: Bus },
  { kind: 'student', label: 'Học sinh', icon: User },
];

export function SchoolBusMapLegend({
  className,
  items,
  showRouteLines = true,
  defaultCollapsed = true,
}: SchoolBusMapLegendProps) {
  const { isVisible, toggleKind } = useMapMarkerVisibility();
  const [isCollapsed, setIsCollapsed] = React.useState(defaultCollapsed);

  return (
    <div
      className={cn(
        'rounded-2xl border border-border bg-popover/95 p-3 text-popover-foreground shadow-[0_12px_28px_rgba(15,23,42,0.08)] transition-all duration-200 select-none',
        isCollapsed ? 'w-[180px] hover:bg-muted cursor-pointer' : 'w-[200px]',
        className
      )}
      onClick={isCollapsed ? () => setIsCollapsed(false) : undefined}
    >
      <div
        className={cn(
          'flex items-center justify-between text-xs font-semibold uppercase tracking-[0.12em] text-muted-foreground cursor-pointer',
          !isCollapsed && 'border-b border-border pb-1.5'
        )}
        onClick={(e) => {
          if (isCollapsed) return; // parent onClick handles expand
          e.stopPropagation();
          setIsCollapsed(true);
        }}
        title={isCollapsed ? 'Hiện chú giải bản đồ' : 'Ẩn chú giải bản đồ'}
      >
        {isCollapsed ? (
          <span className='normal-case tracking-normal text-muted-foreground hover:text-foreground'>
            Chú giải bản đồ <span className='text-slate-400'>· hiện</span>
          </span>
        ) : (
          <>
            <span>Chú giải bản đồ</span>
            <span className='ml-1 text-[9px] font-normal normal-case tracking-normal text-slate-400'>
              (ẩn)
            </span>
          </>
        )}
      </div>

      {!isCollapsed && (
        <>
          <div className='mt-2 grid gap-1 text-xs text-foreground'>
            {(items || DEFAULT_LEGEND_ITEMS).map((item) => (
              <LegendRow
                key={item.kind}
                {...item}
                active={isVisible(item.kind)}
                onToggle={() => toggleKind(item.kind)}
              />
            ))}
          </div>
          {showRouteLines && (
            <div className='mt-2.5 space-y-1 border-t border-border pt-2 text-xs'>
              <p className='flex items-center gap-2 text-sky-700'>
                <span
                  className='block h-[2px] w-8 rounded-full'
                  style={{ backgroundColor: schoolBusBrand.sky }}
                />
                Tuyến đến trường
              </p>
              <p className='flex items-center gap-2 text-emerald-700'>
                <span
                  className='block h-[2px] w-8 rounded-full border-t-2 border-dashed'
                  style={{ borderColor: schoolBusBrand.emerald }}
                />
                Tuyến về nhà
              </p>
            </div>
          )}
        </>
      )}
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
        'hover:bg-muted',
        active
          ? 'text-foreground'
          : 'text-slate-300 line-through decoration-slate-300'
      )}
      title={active ? `Ẩn ${label}` : `Hiện ${label}`}
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
