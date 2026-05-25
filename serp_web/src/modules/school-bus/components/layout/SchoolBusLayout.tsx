'use client';

import React from 'react';
import {
  DynamicSidebar,
  RouteGuard,
  SidebarProvider,
  useSidebarContext,
} from '@/shared/components';
import { cn } from '@/shared/utils';
import { SchoolBusAuthGuard } from '../SchoolBusAuthGuard';
import { schoolBusThemeStyle, schoolBusUi } from '../../theme';

interface SchoolBusLayoutProps {
  children: React.ReactNode;
}

const SchoolBusLayoutContent: React.FC<SchoolBusLayoutProps> = ({
  children,
}) => {
  const { isCollapsed } = useSidebarContext();

  return (
    <div
      className={cn('school-bus-shell flex min-h-screen', schoolBusUi.pageGradient)}
      style={schoolBusThemeStyle}
    >
      <style>
        {`
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
        `}
      </style>
      <DynamicSidebar moduleCode='SCHOOL_BUS' />
      <div
        className={cn(
          'flex min-h-screen flex-1 flex-col transition-all duration-300',
          isCollapsed ? 'pl-16' : 'pl-64'
        )}
      >
        <main className='flex-1'>
          <div className='container mx-auto p-6'>
            <RouteGuard moduleCode='SCHOOL_BUS'>{children}</RouteGuard>
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
