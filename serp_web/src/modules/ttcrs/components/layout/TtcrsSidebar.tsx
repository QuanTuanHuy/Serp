/**
 * Author: Tran Ngoc Hung
 * Description: Part of Serp Project - TTCRS dispatcher sidebar navigation
 */

'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import {
  ArrowLeft,
  ChevronRight,
  FileText,
  MapPin,
  Package2,
  PanelLeftClose,
  PanelLeftOpen,
  Route,
  Truck,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { Button } from '@/shared/components/ui/button';
import { useAppSelector } from '@/shared/hooks';
import { selectUserProfile } from '@/modules/account/store';
import { useTtcrsSidebar } from '../../contexts/TtcrsSidebarContext';

// ---------------------------------------------------------------------------
// Nav config — add new sections / items here as features are implemented
// ---------------------------------------------------------------------------

interface NavItem {
  name: string;
  href: string;
  icon: React.ComponentType<{ className?: string }>;
  /** When true, only exact pathname match activates the item */
  exact?: boolean;
}

interface NavSection {
  heading: string;
  items: NavItem[];
}

const DISPATCHER_SECTIONS: NavSection[] = [
  {
    heading: 'Transport',
    items: [
      { name: 'Requests',  href: '/ttcrs/dispatcher/requests',  icon: FileText },
      { name: 'Locations', href: '/ttcrs/dispatcher/locations', icon: MapPin },
      { name: 'Resources', href: '/ttcrs/dispatcher/resources', icon: Package2 },
    ],
  },
  {
    heading: 'Planning',
    items: [
      { name: 'Truck Routes', href: '/ttcrs/dispatcher/routes', icon: Truck },
    ],
  },
];

const DRIVER_SECTIONS: NavSection[] = [
  {
    heading: 'My Work',
    items: [
      { name: 'My Routes', href: '/ttcrs/driver/routes', icon: Route },
    ],
  },
];

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

export const TtcrsSidebar: React.FC = () => {
  const pathname = usePathname();
  const { isCollapsed, toggleSidebar } = useTtcrsSidebar();
  const [isLogoHovered, setIsLogoHovered] = React.useState(false);

  const user = useAppSelector(selectUserProfile);
  const isDispatcher = user?.roles?.includes('TTCRS_DISPATCHER') ?? false;
  const isDriver     = user?.roles?.includes('TTCRS_DRIVER') ?? false;

  const navSections: NavSection[] = [
    ...(isDispatcher ? DISPATCHER_SECTIONS : []),
    ...(isDriver     ? DRIVER_SECTIONS     : []),
  ];

  const isActive = (item: NavItem) =>
    item.exact
      ? pathname === item.href
      : pathname === item.href || pathname.startsWith(item.href + '/');

  return (
    <aside
      className={cn(
        'fixed left-0 top-0 z-30 flex h-screen flex-col border-r bg-background transition-all duration-300',
        isCollapsed ? 'w-16' : 'w-64'
      )}
    >
      {/* ── Brand header ──────────────────────────────────────────────── */}
      <div className='flex h-16 items-center justify-between border-b px-3'>
        <Link
          href='/home'
          className='group flex items-center space-x-3 transition-colors'
          onMouseEnter={() => setIsLogoHovered(true)}
          onMouseLeave={() => setIsLogoHovered(false)}
          title='Back to SERP Home'
        >
          <div className='flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-lg bg-orange-600 text-white transition-colors group-hover:bg-orange-700'>
            {isLogoHovered ? (
              <ArrowLeft className='h-5 w-5' />
            ) : (
              <Truck className='h-5 w-5' />
            )}
          </div>
          {!isCollapsed && (
            <span className='text-sm font-semibold transition-colors group-hover:text-orange-600'>
              TTCRS
            </span>
          )}
        </Link>

        {!isCollapsed && (
          <Button
            variant='ghost'
            size='icon'
            className='h-8 w-8'
            onClick={toggleSidebar}
            title='Collapse sidebar'
          >
            <PanelLeftClose className='h-4 w-4' />
          </Button>
        )}
      </div>

      {/* ── Navigation ────────────────────────────────────────────────── */}
      <nav className='flex-1 overflow-y-auto p-2'>
        {/* Expand button when collapsed */}
        {isCollapsed && (
          <Button
            variant='ghost'
            size='icon'
            className='mt-4 w-full'
            onClick={toggleSidebar}
            title='Expand sidebar'
          >
            <PanelLeftOpen className='h-4 w-4' />
          </Button>
        )}

        {/* Sections */}
        {navSections.map((section) => (
          <div key={section.heading} className='mb-4'>
            {/* Section heading — hidden when collapsed */}
            {!isCollapsed && (
              <p className='mb-1 px-3 text-[10px] font-semibold uppercase tracking-widest text-muted-foreground'>
                {section.heading}
              </p>
            )}

            {section.items.map((item) => {
              const Icon = item.icon;
              const active = isActive(item);

              return (
                <Link
                  key={item.href}
                  href={item.href}
                  className={cn(
                    'group flex items-center justify-between rounded-lg px-3 py-2.5 text-sm font-medium transition-all hover:bg-accent hover:text-accent-foreground',
                    active
                      ? 'bg-accent text-accent-foreground shadow-sm'
                      : 'text-muted-foreground',
                    isCollapsed && 'justify-center'
                  )}
                  title={isCollapsed ? item.name : undefined}
                >
                  <div className='flex items-center space-x-3'>
                    <Icon
                      className={cn(
                        'h-5 w-5 flex-shrink-0 transition-colors',
                        active
                          ? 'text-orange-600'
                          : 'text-muted-foreground group-hover:text-accent-foreground'
                      )}
                    />
                    {!isCollapsed && <span>{item.name}</span>}
                  </div>

                  {active && !isCollapsed && (
                    <ChevronRight className='h-4 w-4 text-orange-600' />
                  )}
                </Link>
              );
            })}
          </div>
        ))}
      </nav>

      {/* ── Footer ────────────────────────────────────────────────────── */}
      {!isCollapsed && (
        <div className='border-t p-4'>
          <div className='rounded-lg bg-orange-50 p-3 text-xs text-muted-foreground dark:bg-orange-950'>
            <p className='font-medium text-orange-900 dark:text-orange-100'>
              {isDispatcher && !isDriver ? 'Dispatcher Console' : isDriver && !isDispatcher ? 'Driver Console' : 'TTCRS Console'}
            </p>
            <p className='mt-1'>Truck Trailer Container Routing</p>
          </div>
        </div>
      )}
    </aside>
  );
};
