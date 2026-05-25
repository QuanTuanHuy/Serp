'use client';

import * as React from 'react';
import { Expand, Minimize2, Navigation, RouteIcon } from 'lucide-react';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';

interface MapToolbarProps {
  onFitAll: () => void;
  onFitRoute: () => void;
  canFitRoute: boolean;
  /** Disable Fit All when there are no markers on the map yet. */
  canFitAll?: boolean;
  isExpanded: boolean;
  onToggleExpand: () => void;
  className?: string;
  /** Custom label for the second fit button (default: 'Fit Route'). */
  fitRouteLabel?: string;
  /** Set to false to hide the separator and expand/collapse button. */
  showExpand?: boolean;
}

export function MapToolbar({
  onFitAll,
  onFitRoute,
  canFitRoute,
  canFitAll = true,
  isExpanded,
  onToggleExpand,
  className,
  fitRouteLabel = 'Fit Route',
  showExpand = true,
}: MapToolbarProps) {
  return (
    <div
      className={cn(
        'flex items-center gap-1 rounded-xl border border-slate-200 bg-white/90 px-2 py-1.5 shadow-md backdrop-blur-sm',
        className
      )}
    >
      <ToolBtn
        onClick={onFitAll}
        icon={<Navigation className='h-3.5 w-3.5' />}
        label='Fit All'
        title='Zoom to fit all markers'
        disabled={!canFitAll}
      />
      <ToolBtn
        onClick={onFitRoute}
        icon={<RouteIcon className='h-3.5 w-3.5' />}
        label={fitRouteLabel}
        title={
          canFitRoute
            ? `Zoom to fit ${fitRouteLabel.toLowerCase()}`
            : `Select an item first to use ${fitRouteLabel}`
        }
        disabled={!canFitRoute}
      />
      {showExpand && (
        <>
          <div className='mx-1 h-4 w-px bg-slate-200' />
          <ToolBtn
            onClick={onToggleExpand}
            icon={
              isExpanded ? (
                <Minimize2 className='h-3.5 w-3.5' />
              ) : (
                <Expand className='h-3.5 w-3.5' />
              )
            }
            label={isExpanded ? 'Collapse' : 'Expand'}
            title={isExpanded ? 'Collapse map' : 'Expand map'}
            active={isExpanded}
          />
        </>
      )}
    </div>
  );
}

function ToolBtn({
  onClick,
  icon,
  label,
  title,
  disabled,
  active,
}: {
  onClick: () => void;
  icon: React.ReactNode;
  label: string;
  title?: string;
  disabled?: boolean;
  active?: boolean;
}) {
  return (
    <Button
      type='button'
      size='sm'
      variant='ghost'
      onClick={onClick}
      disabled={disabled}
      title={title}
      className={cn(
        'h-7 gap-1 rounded-lg px-2 text-xs font-medium text-slate-700 hover:bg-slate-100 hover:text-slate-900',
        active && 'bg-slate-100 text-slate-900',
        disabled && 'cursor-not-allowed opacity-40'
      )}
    >
      {icon}
      <span className='hidden sm:inline'>{label}</span>
    </Button>
  );
}
