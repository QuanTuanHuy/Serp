/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - TMS sidebar menu item component with submenu support
 */

'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { ChevronDown, ChevronRight } from 'lucide-react';
import type { SidebarMenuItem } from '@/shared/hooks';
import { cn, getIconComponent } from '@/shared/utils';

interface TmsSidebarMenuItemProps {
  item: SidebarMenuItem;
  isCollapsed: boolean;
  level?: number;
}

export const TmsSidebarMenuItem: React.FC<TmsSidebarMenuItemProps> = ({
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

  const hasActiveChild = item.children?.some((child) => isActive(child.href));
  const active = isActive(item.href) || hasActiveChild;

  React.useEffect(() => {
    if (hasActiveChild && !isCollapsed) {
      setIsExpanded(true);
    }
  }, [hasActiveChild, isCollapsed]);

  const handleClick = (e: React.MouseEvent) => {
    if (hasChildren && !isCollapsed) {
      e.preventDefault();
      setIsExpanded(!isExpanded);
    }
  };

  return (
    <div>
      <Link
        href={item.href}
        onClick={handleClick}
        className={cn(
          'group flex items-center justify-between rounded-lg px-3 py-2.5 text-sm font-medium transition-all hover:bg-accent hover:text-accent-foreground',
          active
            ? 'bg-accent text-accent-foreground shadow-sm'
            : 'text-muted-foreground',
          isCollapsed && 'justify-center',
          level > 0 && 'pl-8'
        )}
        title={isCollapsed ? item.name : undefined}
      >
        <div className='flex items-center space-x-3'>
          <Icon
            className={cn(
              'h-5 w-5 flex-shrink-0 transition-colors',
              active
                ? 'text-primary'
                : 'text-muted-foreground group-hover:text-accent-foreground'
            )}
          />
          {!isCollapsed && <span>{item.name}</span>}
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

      {hasChildren && isExpanded && !isCollapsed && (
        <div className='mt-1 space-y-1 pl-4'>
          {item.children!.map((child) => (
            <TmsSidebarMenuItem
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
