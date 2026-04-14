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

  const gridClass =
    preset === 'map-focus'
      ? 'xl:grid-cols-[82%_18%]'
      : preset === 'data-focus'
        ? 'xl:grid-cols-[58%_42%]'
        : 'xl:grid-cols-[74%_26%]';

  const mapHeightClass =
    mapHeightClassName ||
    preset === 'map-focus' ? 'h-[620px]' : preset === 'data-focus' ? 'h-[480px]' : 'h-[560px]';

  return (
    <div
      className={cn(
        'rounded-[24px] border border-slate-200 bg-slate-50/50 p-3',
        expanded &&
          'fixed inset-4 z-[90] overflow-auto bg-white/95 shadow-[0_24px_60px_rgba(15,23,42,0.24)]',
        className
      )}
    >
      <div className='mb-3 flex flex-wrap items-center justify-between gap-2'>
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
            onClick={() => setShowLegend((value) => !value)}
            icon={List}
            label='Legend'
          />
          {allowFullscreen ? (
            <ToolbarButton
              active={expanded}
              onClick={() => setExpanded((value) => !value)}
              icon={expanded ? Minimize2 : Expand}
              label={expanded ? 'Exit full panel' : 'Fullscreen panel'}
            />
          ) : null}
        </div>
      </div>

      <div className={cn('grid gap-3', panel ? gridClass : 'grid-cols-1')}>
        <div className='space-y-3'>
          <div className={mapHeightClass}>{map}</div>
          {showLegend && legend ? <div>{legend}</div> : null}
        </div>
        {panel ? (
          <div className='rounded-2xl border border-slate-200 bg-white p-3 shadow-[0_8px_24px_rgba(15,23,42,0.06)]'>
            {panel}
          </div>
        ) : null}
      </div>
    </div>
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
