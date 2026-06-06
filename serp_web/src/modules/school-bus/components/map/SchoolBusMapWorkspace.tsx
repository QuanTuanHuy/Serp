'use client';

import * as React from 'react';
import { cn } from '@/shared/utils';
import { MapExpandContext } from './MapExpandContext';
import { MapMarkerVisibilityProvider } from './MapMarkerVisibilityContext';
import { MapToolbar } from './MapToolbar';

interface SchoolBusMapWorkspaceProps {
  map: React.ReactNode;
  panel?: React.ReactNode;
  legend?: React.ReactNode;
  allowFullscreen?: boolean;
  mapHeightClassName?: string;
  className?: string;
  panelClassName?: string;
  onFitAll?: () => void;
  onFitRoute?: () => void;
  canFitAll?: boolean;
  canFitRoute?: boolean;
  fitRouteLabel?: string;
  compact?: boolean;
  flat?: boolean;
  isExpanded?: boolean;
  onToggleExpand?: () => void;
  // Legacy / backward compatibility props
  defaultPreset?: any;
  defaultShowLegend?: boolean;
}

export function SchoolBusMapWorkspace({
  map,
  panel,
  legend,
  allowFullscreen = true,
  mapHeightClassName,
  className,
  panelClassName,
  onFitAll,
  onFitRoute,
  canFitAll,
  canFitRoute,
  fitRouteLabel = 'Fit Selected',
  compact,
  flat = false,
  isExpanded,
  onToggleExpand,
}: SchoolBusMapWorkspaceProps) {
  const [internalExpanded, setInternalExpanded] = React.useState(false);
  const expanded = isExpanded !== undefined ? isExpanded : internalExpanded;
  const toggleExpand = onToggleExpand !== undefined ? onToggleExpand : () => setInternalExpanded((v) => !v);
  const [expandKey, setExpandKey] = React.useState(0);

  React.useEffect(() => {
    setExpandKey((k) => k + 1);
  }, [expanded]);

  const contextValue = React.useMemo(
    () => ({ isExpanded: expanded, expandKey }),
    [expanded, expandKey]
  );

  return (
    <MapExpandContext.Provider value={contextValue}>
      <MapMarkerVisibilityProvider>
        <div
          className={cn(
            expanded
              ? 'fixed inset-4 z-[900] flex flex-col bg-white shadow-[0_24px_60px_rgba(15,23,42,0.24)] border border-slate-300 rounded-[24px] overflow-hidden'
              : cn(
                  flat
                    ? 'flex flex-col overflow-hidden w-full h-full'
                    : 'rounded-[24px] border border-slate-200 bg-white overflow-hidden shadow-[0_14px_36px_rgba(15,23,42,0.05)] flex flex-col',
                  className
                )
          )}
        >
          {/* Main workspace area */}
          <div className='flex flex-1 overflow-hidden min-h-0 min-w-0'>
            {/* Left/Center: Map Container */}
            <div
              className={cn(
                'relative flex-1 min-h-0 min-w-0 isolate',
                !expanded && (mapHeightClassName ?? 'h-[480px]')
              )}
            >
              {map}

              {/* Floating Absolute MapToolbar Overlay */}
              <div className='absolute right-3 top-3 z-[1000]'>
                <MapToolbar
                  onFitAll={onFitAll}
                  onFitRoute={onFitRoute}
                  canFitAll={canFitAll}
                  canFitRoute={canFitRoute}
                  fitRouteLabel={fitRouteLabel}
                  isExpanded={expanded}
                  onToggleExpand={toggleExpand}
                  showExpand={allowFullscreen}
                  compact={compact ?? Boolean(panel)}
                />
              </div>

              {/* Floating Absolute SchoolBusMapLegend Overlay */}
              {legend && (
                <div className='absolute left-3 bottom-3 z-[1000] max-w-[280px]'>
                  {legend}
                </div>
              )}
            </div>

            {/* Right: Data Panel (Displayed alongside Map if provided) */}
            {panel && (
              <div
                className={cn(
                  'shrink-0 overflow-y-auto border-l border-slate-200 bg-white',
                  panelClassName ?? 'w-[300px] p-4',
                  expanded ? 'h-full' : 'min-h-0'
                )}
              >
                {panel}
              </div>
            )}
          </div>
        </div>
      </MapMarkerVisibilityProvider>
    </MapExpandContext.Provider>
  );
}
