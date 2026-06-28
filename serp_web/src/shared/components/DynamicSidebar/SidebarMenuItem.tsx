/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Sidebar Menu Item component with submenu support
 */

'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { ChevronRight, ChevronDown } from 'lucide-react';
import { cn, getIconComponent } from '@/shared/utils';
import type { SidebarMenuItem } from '@/shared/hooks';
import { Tooltip, TooltipTrigger, TooltipContent } from '@/shared/components/ui/tooltip';
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuSub,
  DropdownMenuSubTrigger,
  DropdownMenuSubContent,
} from '@/shared/components/ui/dropdown-menu';

interface SidebarMenuItemProps {
  item: SidebarMenuItem;
  isCollapsed: boolean;
  level?: number;
}

// Recursive renderer for dropdown menu items in collapsed state
const renderDropdownItems = (subItems: SidebarMenuItem[]) => {
  return subItems.map((child) => {
    const childHasChildren = child.children && child.children.length > 0;
    if (childHasChildren) {
      return (
        <DropdownMenuSub key={child.id}>
          <DropdownMenuSubTrigger className="cursor-pointer">
            <span>{child.name}</span>
          </DropdownMenuSubTrigger>
          <DropdownMenuSubContent>
            {renderDropdownItems(child.children!)}
          </DropdownMenuSubContent>
        </DropdownMenuSub>
      );
    }
    return (
      <DropdownMenuItem key={child.id} asChild>
        <Link href={child.href} className="w-full cursor-pointer">
          {child.name}
        </Link>
      </DropdownMenuItem>
    );
  });
};

export const SidebarMenuItemComponent: React.FC<SidebarMenuItemProps> = ({
  item,
  isCollapsed,
  level = 0,
}) => {
  const pathname = usePathname();
  const [isExpanded, setIsExpanded] = useState(false);

  const hasChildren = item.children && item.children.length > 0;
  const Icon = getIconComponent(item.icon);

  const isActive = (href: string) => {
    return pathname === href || pathname.startsWith(href);
  };

  const active = isActive(item.href);

  // If item has children, toggle expansion instead of navigation
  const handleClick = (e: React.MouseEvent) => {
    if (hasChildren && !isCollapsed) {
      e.preventDefault();
      setIsExpanded(!isExpanded);
    }
  };

  const itemLink = (
    <Link
      href={item.href}
      onClick={handleClick}
      className={cn(
        'group flex items-center justify-between rounded-lg px-3 py-2.5 text-sm font-medium transition-all hover:bg-accent hover:text-accent-foreground',
        active
          ? 'bg-accent text-accent-foreground shadow-sm'
          : 'text-muted-foreground',
        isCollapsed && 'justify-center',
        level > 0 && 'pl-8' // Indent submenu items
      )}
      title={undefined} // Handled by Radix Tooltip
    >
      <div className='flex items-center space-x-3'>
        <Icon
          className={cn(
            'h-5 w-5 transition-colors flex-shrink-0',
            active
              ? 'text-primary'
              : 'text-muted-foreground group-hover:text-accent-foreground'
          )}
        />
        <span
          className={cn(
            'transition-all duration-300 ease-in-out overflow-hidden whitespace-nowrap',
            isCollapsed ? 'max-w-0 opacity-0 pointer-events-none' : 'max-w-[200px] opacity-100'
          )}
        >
          {item.name}
        </span>
      </div>

      {!isCollapsed && (
        <>
          {hasChildren ? (
            isExpanded ? (
              <ChevronDown className='h-4 w-4' />
            ) : (
              <ChevronRight className='h-4 w-4' />
            )
          ) : active ? (
            <ChevronRight className='h-4 w-4 text-primary' />
          ) : null}
        </>
      )}
    </Link>
  );

  // Collapsed Render Logic
  if (isCollapsed) {
    if (!hasChildren) {
      return (
        <Tooltip>
          <TooltipTrigger asChild>
            {itemLink}
          </TooltipTrigger>
          <TooltipContent side="right" sideOffset={12}>
            {item.name}
          </TooltipContent>
        </Tooltip>
      );
    } else {
      return (
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            {itemLink}
          </DropdownMenuTrigger>
          <DropdownMenuContent side="right" align="start" sideOffset={12} className="w-56">
            <DropdownMenuLabel>{item.name}</DropdownMenuLabel>
            <DropdownMenuSeparator />
            {renderDropdownItems(item.children!)}
          </DropdownMenuContent>
        </DropdownMenu>
      );
    }
  }

  // Expanded Render Logic
  return (
    <div>
      {itemLink}

      {/* Submenu Items with Smooth Transition */}
      {hasChildren && (
        <div
          className={cn(
            'mt-1 space-y-1 pl-4 overflow-hidden transition-all duration-300 ease-in-out',
            isExpanded && !isCollapsed ? 'max-h-96 opacity-100' : 'max-h-0 opacity-0'
          )}
        >
          {item.children!.map((child) => (
            <SidebarMenuItemComponent
              key={child.id}
              item={child}
              isCollapsed={isCollapsed}
              level={level + 1}
            />
          ))}
        </div>
      )}
    </div>
  );
};
