'use client';

import React, { useRef } from 'react';
import {
  RouteGuard,
  SidebarProvider,
  useSidebarContext,
} from '@/shared/components';
import { cn } from '@/shared/utils';
import { SchoolBusAuthGuard } from '../SchoolBusAuthGuard';
import { schoolBusBrand, schoolBusThemeStyle, schoolBusUi } from '../../theme';
import { SchoolBusTopbar } from './SchoolBusTopbar';
import { SchoolBusSidebar } from './SchoolBusSidebar';

interface SchoolBusLayoutProps {
  children: React.ReactNode;
}

const SchoolBusLayoutContent: React.FC<SchoolBusLayoutProps> = ({
  children,
}) => {
  const containerRef = useRef<HTMLDivElement | null>(null);
  const { isCollapsed } = useSidebarContext();

  return (
    <div
      className={cn(
        'school-bus-shell flex min-h-screen bg-background',
        schoolBusUi.pageGradient
      )}
      style={schoolBusThemeStyle}
    >
      <style>
        {`
          .school-bus-shell {
            --primary: ${schoolBusBrand.primary};
            --primary-foreground: ${schoolBusBrand.white};
            --ring: ${schoolBusBrand.primary};
            --sidebar-primary: ${schoolBusBrand.primary};
            --sidebar-primary-foreground: ${schoolBusBrand.white};
          }

          .school-bus-shell .leaflet-container {
            z-index: 0;
            isolation: isolate;
          }

          .school-bus-shell .leaflet-pane,
          .school-bus-shell .leaflet-map-pane,
          .school-bus-shell .leaflet-tile-pane,
          .school-bus-shell .leaflet-overlay-pane,
          .school-bus-shell .leaflet-shadow-pane,
          .school-bus-shell .leaflet-marker-pane,
          .school-bus-shell .leaflet-tooltip-pane,
          .school-bus-shell .leaflet-popup-pane {
            z-index: 1;
          }

          .school-bus-shell .leaflet-control-container {
            position: relative;
            z-index: 2;
          }

          /* Compatibility layer for legacy School Bus neutral utilities. */
          .dark .school-bus-shell .bg-white,
          .dark .school-bus-shell [class~="bg-white/90"],
          .dark .school-bus-shell [class~="bg-white/80"],
          .dark .school-bus-shell [class~="bg-white/70"],
          .dark .school-bus-shell [class~="bg-white/50"] {
            background-color: var(--card) !important;
          }

          .dark .school-bus-shell .bg-slate-50,
          .dark .school-bus-shell [class~="bg-slate-50/90"],
          .dark .school-bus-shell [class~="bg-slate-50/80"],
          .dark .school-bus-shell [class~="bg-slate-50/60"],
          .dark .school-bus-shell [class~="bg-slate-50/50"],
          .dark .school-bus-shell [class~="bg-slate-50/40"],
          .dark .school-bus-shell [class~="bg-slate-50/30"],
          .dark .school-bus-shell [class~="bg-slate-50/20"],
          .dark .school-bus-shell [class~="bg-slate-50/15"],
          .dark .school-bus-shell [class~="bg-slate-50/10"],
          .dark .school-bus-shell [class~="bg-slate-50/5"],
          .dark .school-bus-shell [class~="bg-slate-50/70"],
          .dark .school-bus-shell [class~="bg-[#f8fafc]"],
          .dark .school-bus-shell [class~="bg-[#fafafa]"],
          .dark .school-bus-shell [class~="bg-[#fcfcfc]"] {
            background-color: var(--muted) !important;
          }

          .dark .school-bus-shell .border-slate-100,
          .dark .school-bus-shell [class~="border-slate-100/40"],
          .dark .school-bus-shell [class~="border-slate-100/50"],
          .dark .school-bus-shell [class~="border-slate-100/60"],
          .dark .school-bus-shell [class~="border-slate-100/65"],
          .dark .school-bus-shell [class~="border-slate-100/70"],
          .dark .school-bus-shell .border-slate-200,
          .dark .school-bus-shell [class~="border-slate-200/20"],
          .dark .school-bus-shell [class~="border-slate-200/50"],
          .dark .school-bus-shell [class~="border-slate-200/80"],
          .dark .school-bus-shell [class~="border-slate-200/60"],
          .dark .school-bus-shell [class~="border-slate-200/40"],
          .dark .school-bus-shell [class~="border-slate-200/70"],
          .dark .school-bus-shell .border-slate-300 {
            border-color: var(--border) !important;
          }

          .dark .school-bus-shell [class~="bg-[#FDECEF]"] {
            background-color: color-mix(in oklch, var(--primary) 18%, transparent) !important;
          }

          .dark .school-bus-shell [class~="border-[#F6CDD5]"] {
            border-color: color-mix(in oklch, var(--primary) 45%, transparent) !important;
          }

          .dark .school-bus-shell [class~="text-[#C81E3A]"] {
            color: color-mix(in oklch, var(--primary) 55%, white) !important;
          }

          .dark .school-bus-shell .text-slate-950,
          .dark .school-bus-shell .text-slate-900,
          .dark .school-bus-shell .text-slate-800 {
            color: var(--foreground) !important;
          }

          .dark .school-bus-shell .text-slate-700,
          .dark .school-bus-shell .text-slate-600,
          .dark .school-bus-shell .text-slate-500,
          .dark .school-bus-shell .text-slate-400 {
            color: var(--muted-foreground) !important;
          }
        `}
      </style>
      <SchoolBusSidebar />
      <div
        ref={containerRef}
        className={cn(
          'flex flex-1 flex-col transition-all duration-300 h-screen overflow-y-auto',
          isCollapsed ? 'pl-16' : 'pl-64'
        )}
      >
        <SchoolBusTopbar scrollContainerRef={containerRef} />
        <main className='flex-1'>
          <div className='container mx-auto p-6'>
            <RouteGuard moduleCode='SCHOOLBUS'>{children}</RouteGuard>
          </div>
        </main>
      </div>
    </div>
  );
};

export const SchoolBusLayout: React.FC<SchoolBusLayoutProps> = ({
  children,
}) => {
  return (
    <SchoolBusAuthGuard>
      <SidebarProvider>
        <SchoolBusLayoutContent>{children}</SchoolBusLayoutContent>
      </SidebarProvider>
    </SchoolBusAuthGuard>
  );
};
