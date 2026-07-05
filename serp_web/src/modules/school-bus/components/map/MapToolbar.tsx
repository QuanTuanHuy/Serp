'use client';

import * as React from 'react';
import {
  Compass,
  Expand,
  Minimize2,
  Navigation,
  RouteIcon,
} from 'lucide-react';
import {
  Button,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';

interface MapToolbarProps {
  onFitAll?: () => void;
  onFitRoute?: () => void;
  canFitRoute?: boolean;
  /** Disable Fit All when there are no markers on the map yet. */
  canFitAll?: boolean;
  isExpanded: boolean;
  onToggleExpand: () => void;
  className?: string;
  /** Custom label for the second fit button (default: 'Fit Route'). */
  fitRouteLabel?: string;
  /** Set to false to hide the separator and expand/collapse button. */
  showExpand?: boolean;
  /** Force compact dropdown layout. */
  compact?: boolean;
}

export function MapToolbar({
  onFitAll,
  onFitRoute,
  canFitRoute = false,
  canFitAll = true,
  isExpanded,
  onToggleExpand,
  className,
  fitRouteLabel = 'Fit Route',
  showExpand = true,
  compact = false,
}: MapToolbarProps) {
  const hasFitButtons = Boolean(onFitAll || onFitRoute);

  // Dropdown menu content containing the actions
  const dropdownContent = (
    <DropdownMenuContent
      align='end'
      className='z-[1050] w-48 rounded-xl border border-border bg-popover p-1.5 text-popover-foreground shadow-lg'
    >
      {onFitAll && (
        <DropdownMenuItem
          onClick={onFitAll}
          disabled={!canFitAll}
          className='flex cursor-pointer items-center gap-2 rounded-lg px-2.5 py-2 text-xs font-medium text-muted-foreground hover:bg-muted focus:bg-muted focus:text-foreground disabled:pointer-events-none disabled:opacity-40'
        >
          <Navigation className='h-3.5 w-3.5 text-slate-500' />
          <span>Fit All Markers</span>
        </DropdownMenuItem>
      )}
      {onFitRoute && (
        <DropdownMenuItem
          onClick={onFitRoute}
          disabled={!canFitRoute}
          className='flex cursor-pointer items-center gap-2 rounded-lg px-2.5 py-2 text-xs font-medium text-muted-foreground hover:bg-muted focus:bg-muted focus:text-foreground disabled:pointer-events-none disabled:opacity-40'
        >
          <RouteIcon className='h-3.5 w-3.5 text-slate-500' />
          <span>{fitRouteLabel}</span>
        </DropdownMenuItem>
      )}
      {showExpand && (
        <DropdownMenuItem
          onClick={onToggleExpand}
          className='flex cursor-pointer items-center gap-2 rounded-lg px-2.5 py-2 text-xs font-medium text-muted-foreground hover:bg-muted focus:bg-muted focus:text-foreground'
        >
          {isExpanded ? (
            <>
              <Minimize2 className='h-3.5 w-3.5 text-slate-500' />
              <span>Collapse Map</span>
            </>
          ) : (
            <>
              <Expand className='h-3.5 w-3.5 text-slate-500' />
              <span>Expand Map</span>
            </>
          )}
        </DropdownMenuItem>
      )}
    </DropdownMenuContent>
  );

  return (
    <>
      {/* 1. Compact Dropdown Version: Rendered always if compact is true, or on mobile/small viewports if compact is false */}
      <div className={cn(compact ? 'flex' : 'flex md:hidden', className)}>
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button
              type='button'
              size='sm'
              variant='ghost'
              className='h-8 gap-1.5 rounded-xl border border-border bg-popover/95 px-2.5 text-xs font-semibold text-popover-foreground shadow-md hover:bg-muted focus-visible:ring-0 focus-visible:ring-offset-0 focus:outline-hidden'
            >
              <Compass className='h-4 w-4 text-slate-600 animate-pulse' />
              <span>Map Actions</span>
            </Button>
          </DropdownMenuTrigger>
          {dropdownContent}
        </DropdownMenu>
      </div>

      {/* 2. Full Inline Version: Rendered on medium/larger screens only if compact is false */}
      {!compact && (
        <div
          className={cn(
            'hidden md:flex items-center gap-1 rounded-xl border border-border bg-popover/90 px-2 py-1.5 text-popover-foreground shadow-md backdrop-blur-sm',
            className
          )}
        >
          {onFitAll && (
            <ToolBtn
              onClick={onFitAll}
              icon={<Navigation className='h-3.5 w-3.5' />}
              label='Hiển thị tất cả'
              title='Zoom to fit all markers'
              disabled={!canFitAll}
            />
          )}
          {onFitRoute && (
            <ToolBtn
              onClick={onFitRoute}
              icon={<RouteIcon className='h-3.5 w-3.5' />}
              label={fitRouteLabel}
              title={
                canFitRoute
                  ? `Zoom to fit ${fitRouteLabel.toLowerCase()}`
                  : `Chọn một mục trước để dùng ${fitRouteLabel}`
              }
              disabled={!canFitRoute}
            />
          )}
          {showExpand && (
            <>
              {hasFitButtons && <div className='mx-1 h-4 w-px bg-border' />}
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
      )}
    </>
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
        'h-7 gap-1 rounded-lg px-2 text-xs font-medium text-muted-foreground hover:bg-muted hover:text-foreground',
        active && 'bg-muted text-foreground',
        disabled && 'cursor-not-allowed opacity-40'
      )}
    >
      {icon}
      <span className='hidden sm:inline'>{label}</span>
    </Button>
  );
}
