/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - TMS dynamic sidebar component
 */

'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import {
  AlertCircle,
  ArrowLeft,
  Loader2,
  PanelLeftClose,
  PanelLeftOpen,
} from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import { useSidebarContext } from '@/shared/components/DynamicSidebar';
import { getModuleIcon } from '@/shared/constants/moduleIcons';
import { useModuleSidebar } from '@/shared/hooks';
import type { SidebarMenuItem } from '@/shared/hooks';
import { cn } from '@/shared/utils';

import { TmsSidebarMenuItem } from './TmsSidebarMenuItem';

interface TmsDynamicSidebarProps {
  className?: string;
}

const TMS_MODULE_CODE = 'TMS';
const TMS_SETTINGS_MENU_ID = -1001;

const isVehiclesMenuItem = (item: SidebarMenuItem) =>
  item.href === '/first-mile/vehicles' ||
  item.href.startsWith('/first-mile/vehicles/');

const isProductTypesMenuItem = (item: SidebarMenuItem) =>
  item.href === '/first-mile/product-types' ||
  item.href.startsWith('/first-mile/product-types/');

const isSettingsMenuItem = (item: SidebarMenuItem) =>
  item.href === '/first-mile/settings' ||
  item.href.startsWith('/first-mile/settings/');

const toSettingsProductTypeMenuItem = (
  item: SidebarMenuItem
): SidebarMenuItem => ({
  ...item,
  href: item.href.replace(
    '/first-mile/product-types',
    '/first-mile/settings/product-types'
  ),
  level: 1,
  children: (item.children ?? []).map(toSettingsProductTypeMenuItem),
});

const buildTmsSidebarMenuItems = (
  menuItems: SidebarMenuItem[]
): SidebarMenuItem[] => {
  let settingsMenuExists = false;
  let vehicleMenuOrder: number | undefined;
  const settingsChildren: SidebarMenuItem[] = [];

  const removeVehicleMenus = (items: SidebarMenuItem[]): SidebarMenuItem[] => {
    return items.flatMap((item) => {
      const children = removeVehicleMenus(item.children ?? []);

      if (isVehiclesMenuItem(item)) {
        vehicleMenuOrder =
          vehicleMenuOrder === undefined
            ? item.order
            : Math.min(vehicleMenuOrder, item.order);

        return children.map((child) => ({
          ...child,
          level: item.level,
        }));
      }

      if (isProductTypesMenuItem(item)) {
        settingsChildren.push(toSettingsProductTypeMenuItem(item));
        return [];
      }

      if (isSettingsMenuItem(item)) {
        settingsMenuExists = true;
      }

      return [
        {
          ...item,
          children,
        },
      ];
    });
  };

  const normalizedItems = removeVehicleMenus(menuItems);

  const appendSettingsChildren = (items: SidebarMenuItem[]) =>
    items.map((item) => {
      if (!isSettingsMenuItem(item)) {
        return item;
      }

      const existingChildren = item.children ?? [];
      const existingHrefs = new Set(existingChildren.map((child) => child.href));
      const nextChildren = [
        ...existingChildren,
        ...settingsChildren.filter((child) => !existingHrefs.has(child.href)),
      ];

      return {
        ...item,
        children: nextChildren.sort((a, b) => a.order - b.order),
      };
    });

  if (!settingsMenuExists) {
    const lastOrder = normalizedItems.reduce(
      (maxOrder, item) => Math.max(maxOrder, item.order),
      0
    );

    normalizedItems.push({
      id: TMS_SETTINGS_MENU_ID,
      name: 'Settings',
      href: '/first-mile/settings',
      icon: 'Settings',
      order: vehicleMenuOrder ?? lastOrder + 1,
      children: settingsChildren.sort((a, b) => a.order - b.order),
      level: 0,
    });
  }

  return appendSettingsChildren(normalizedItems).sort(
    (a, b) => a.order - b.order
  );
};

export const TmsDynamicSidebar: React.FC<TmsDynamicSidebarProps> = ({
  className,
}) => {
  const { isCollapsed, toggleSidebar } = useSidebarContext();
  const [isModuleHovered, setIsModuleHovered] = useState(false);

  const { menuItems, currentModule, isLoading, error, refetch } =
    useModuleSidebar(TMS_MODULE_CODE);

  const tmsMenuItems = React.useMemo(
    () => buildTmsSidebarMenuItems(menuItems),
    [menuItems]
  );
  const hasTmsMenus = tmsMenuItems.length > 0;

  const moduleIcon = getModuleIcon(TMS_MODULE_CODE);
  const ModuleIcon = moduleIcon?.icon;

  return (
    <aside
      className={cn(
        'fixed left-0 top-0 z-30 h-screen border-r bg-background transition-all duration-300',
        isCollapsed ? 'w-16' : 'w-64',
        className
      )}
    >
      <div className='flex h-16 items-center justify-between border-b px-3'>
        <Link
          href='/home'
          className='group flex items-center space-x-3 transition-colors'
          onMouseEnter={() => setIsModuleHovered(true)}
          onMouseLeave={() => setIsModuleHovered(false)}
        >
          <div
            className={cn(
              'flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-lg text-primary-foreground transition-all group-hover:opacity-90',
              'bg-primary',
              isModuleHovered && 'bg-primary'
            )}
          >
            {isModuleHovered ? (
              <ArrowLeft className='h-5 w-5' />
            ) : ModuleIcon ? (
              <ModuleIcon className='h-5 w-5' />
            ) : (
              <span className='text-xs font-bold'>{TMS_MODULE_CODE}</span>
            )}
          </div>

          {!isCollapsed && (
            <span className='text-sm font-semibold transition-colors group-hover:text-primary'>
              {currentModule?.moduleName || TMS_MODULE_CODE}
            </span>
          )}
        </Link>

        {!isCollapsed && (
          <Button
            variant='ghost'
            size='icon'
            className='h-8 w-8'
            onClick={toggleSidebar}
          >
            <PanelLeftClose className='h-4 w-4' />
          </Button>
        )}
      </div>

      <nav className='max-h-[calc(100vh-8rem)] flex-1 space-y-1 overflow-y-auto p-2'>
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

        {isLoading && (
          <div className='flex flex-col items-center justify-center py-8 text-muted-foreground'>
            <Loader2 className='mb-2 h-6 w-6 animate-spin' />
            {!isCollapsed && <p className='text-xs'>Loading menus...</p>}
          </div>
        )}

        {error && !isLoading && (
          <div className='p-4'>
            <div className='flex flex-col items-center space-y-3 text-center'>
              <div className='flex h-12 w-12 items-center justify-center rounded-full bg-destructive/10'>
                <AlertCircle className='h-6 w-6 text-destructive' />
              </div>
              {!isCollapsed && (
                <>
                  <div className='space-y-1'>
                    <p className='text-sm font-medium text-destructive'>
                      Failed to load menus
                    </p>
                    <p className='text-xs text-muted-foreground'>
                      Please try again
                    </p>
                  </div>
                  <Button
                    variant='outline'
                    size='sm'
                    onClick={() => refetch()}
                    className='w-full'
                  >
                    Retry
                  </Button>
                </>
              )}
            </div>
          </div>
        )}

        {!isLoading && !error && !hasTmsMenus && (
          <div className='p-4'>
            {!isCollapsed ? (
              <div className='space-y-2 text-center'>
                <p className='text-sm font-medium text-muted-foreground'>
                  No menus available
                </p>
                <p className='text-xs text-muted-foreground'>
                  Contact your administrator
                </p>
              </div>
            ) : (
              <div className='text-center'>
                <AlertCircle className='mx-auto h-6 w-6 text-muted-foreground' />
              </div>
            )}
          </div>
        )}

        {!isLoading &&
          !error &&
          hasTmsMenus &&
          tmsMenuItems.map((item) => (
            <TmsSidebarMenuItem
              key={item.id}
              item={item}
              isCollapsed={isCollapsed}
            />
          ))}
      </nav>

      {!isCollapsed && currentModule && (
        <div className='border-t p-4'>
          <div className='rounded-lg bg-muted p-3 text-xs text-muted-foreground'>
            <p className='font-medium'>{currentModule.moduleName}</p>
            <p className='mt-1'>
              {currentModule.moduleDescription || 'Module Description'}
            </p>
          </div>
        </div>
      )}
    </aside>
  );
};
