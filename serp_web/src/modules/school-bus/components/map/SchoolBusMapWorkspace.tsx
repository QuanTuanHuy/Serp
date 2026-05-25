'use client';

import * as React from 'react';
import {
  Columns2,
  Expand,
  Focus,
  List,
  Minimize2,
  PanelRight,
} from 'lucide-react';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { MapExpandContext } from './MapExpandContext';
import { MapMarkerVisibilityProvider } from './MapMarkerVisibilityContext';

type WorkspacePreset = 'split' | 'map-focus' | 'data-focus';

interface SchoolBusMapWorkspaceProps {
  map: React.ReactNode;
  panel?: React.ReactNode;
  legend?: React.ReactNode;
  defaultPreset?: WorkspacePreset;
  defaultShowLegend?: boolean;
  allowFullscreen?: boolean;
  mapHeightClassName?: string;
  className?: string;
}

export function SchoolBusMapWorkspace({
  map,
  panel,
  legend,
  defaultPreset = 'split',
  defaultShowLegend = true,
  allowFullscreen = true,
  mapHeightClassName,
  className,
}: SchoolBusMapWorkspaceProps) {
  const [preset, setPreset] = React.useState<WorkspacePreset>(defaultPreset);
  const [showLegend, setShowLegend] = React.useState(defaultShowLegend);
  const [expanded, setExpanded] = React.useState(false);
  // Bumps every time expanded toggles — map clients consume this for invalidateSize()
  const [expandKey, setExpandKey] = React.useState(0);

  React.useEffect(() => {
    setExpandKey((k) => k + 1);
  }, [expanded]);

  const contextValue = React.useMemo(
    () => ({ isExpanded: expanded, expandKey }),
    [expanded, expandKey]
  );

  const gridClass =
    preset === 'map-focus'
      ? 'xl:grid-cols-[82%_18%]'
      : preset === 'data-focus'
        ? 'xl:grid-cols-[58%_42%]'
        : 'xl:grid-cols-[74%_26%]';

  // Fixed-height class used in normal (non-expanded) mode.
  // NOTE: using ?? instead of || to avoid operator-precedence issue with ternary.
  const normalMapHeight =
    mapHeightClassName ??
    (preset === 'map-focus' ? 'h-[620px]' : preset === 'data-focus' ? 'h-[480px]' : 'h-[560px]');

  return (
    <MapExpandContext.Provider value={contextValue}>
      <MapMarkerVisibilityProvider>
        <div
          className={cn(
            'rounded-[24px] border border-slate-200 bg-slate-50/50 p-3',
            expanded
              ? 'fixed inset-4 z-[90] flex flex-col overflow-hidden bg-white/95 shadow-[0_24px_60px_rgba(15,23,42,0.24)]'
              : className
          )}
        >
          {/* ── Toolbar ─────────────────────────────────────────── */}
          <div className='mb-3 flex shrink-0 flex-wrap items-center justify-between gap-2'>
            <div className='flex items-center gap-2'>
              <ToolbarButton
                active={preset === 'split'}
                onClick={() => setPreset('split')}
                icon={Columns2}
                label='Layout split'
              />
              <ToolbarButton
                active={preset === 'map-focus'}
                onClick={() => setPreset('map-focus')}
                icon={Focus}
                label='Map focus'
              />
              <ToolbarButton
                active={preset === 'data-focus'}
                onClick={() => setPreset('data-focus')}
                icon={PanelRight}
                label='Data focus'
              />
            </div>
            <div className='flex items-center gap-2'>
              <ToolbarButton
                active={showLegend}
                onClick={() => setShowLegend((v) => !v)}
                icon={List}
                label='Legend'
              />
              {allowFullscreen ? (
                <ToolbarButton
                  active={expanded}
                  onClick={() => setExpanded((v) => !v)}
                  icon={expanded ? Minimize2 : Expand}
                  label={expanded ? 'Exit full panel' : 'Fullscreen panel'}
                />
              ) : null}
            </div>
          </div>

          {/* ── Content: expanded vs normal ──────────────────────── */}
          {expanded ? (
            /*
             * Expanded layout — map fills all remaining height.
             * Outer div is flex-col (toolbar above, content below).
             * Inner: flex-row — map (flex-1) + optional right panel (fixed width).
             */
            <div className={cn('flex flex-1 gap-3 overflow-hidden', panel ? '' : '')}>
              <div className='flex flex-1 flex-col gap-3 overflow-hidden'>
                <div className='min-h-0 flex-1'>{map}</div>
                {showLegend && legend ? <div className='shrink-0'>{legend}</div> : null}
              </div>
              {panel ? (
                <div className='w-[280px] shrink-0 overflow-y-auto rounded-2xl border border-slate-200 bg-white p-3 shadow-[0_8px_24px_rgba(15,23,42,0.06)]'>
                  {panel}
                </div>
              ) : null}
            </div>
          ) : (
            /*
             * Normal layout — grid with preset columns, fixed-height map.
             */
            <div className={cn('grid gap-3', panel ? gridClass : 'grid-cols-1')}>
              <div className='space-y-3'>
                <div className={normalMapHeight}>{map}</div>
                {showLegend && legend ? <div>{legend}</div> : null}
              </div>
              {panel ? (
                <div className='rounded-2xl border border-slate-200 bg-white p-3 shadow-[0_8px_24px_rgba(15,23,42,0.06)]'>
                  {panel}
                </div>
              ) : null}
            </div>
          )}
        </div>
      </MapMarkerVisibilityProvider>
    </MapExpandContext.Provider>
  );
}

function ToolbarButton({
  active,
  onClick,
  icon: Icon,
  label,
}: {
  active: boolean;
  onClick: () => void;
  icon: React.ComponentType<{ className?: string }>;
  label: string;
}) {
  return (
    <Button
      type='button'
      size='sm'
      variant='outline'
      onClick={onClick}
      className={cn(
        'rounded-full border-slate-300 bg-white text-slate-700',
        active && 'border-rose-300 bg-rose-50 text-rose-700'
      )}
      title={label}
    >
      <Icon className='h-4 w-4' />
      <span className='hidden sm:inline'>{label}</span>
    </Button>
  );
}
