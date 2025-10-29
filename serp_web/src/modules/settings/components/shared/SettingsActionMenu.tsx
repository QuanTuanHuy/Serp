/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings action menu component
 */

'use client';

import React from 'react';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
  DropdownMenuSeparator,
} from '@/shared/components/ui';
import { Button } from '@/shared/components/ui/button';
import { MoreHorizontal } from 'lucide-react';
import { cn } from '@/shared/utils';

export interface SettingsActionMenuItem {
  label: string;
  onClick: () => void;
  icon?: React.ReactNode;
  variant?: 'default' | 'destructive';
  disabled?: boolean;
  separator?: boolean;
}

export interface SettingsActionMenuProps {
  items: SettingsActionMenuItem[];
  triggerLabel?: string;
  triggerIcon?: React.ReactNode;
  align?: 'start' | 'center' | 'end';
  className?: string;
}

/**
 * SettingsActionMenu - Dropdown menu for row actions in tables
 *
 * Usage:
 * ```tsx
 * <SettingsActionMenu
 *   items={[
 *     { label: 'Edit', onClick: handleEdit, icon: <Edit className="h-4 w-4" /> },
 *     { label: 'Delete', onClick: handleDelete, variant: 'destructive', separator: true },
 *   ]}
 * />
 * ```
 */
export const SettingsActionMenu: React.FC<SettingsActionMenuProps> = ({
  items,
  triggerLabel,
  triggerIcon = <MoreHorizontal className='h-4 w-4' />,
  align = 'end',
  className,
}) => {
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant='ghost' size='sm' className={className}>
          {triggerIcon}
          {triggerLabel && <span className='ml-2'>{triggerLabel}</span>}
        </Button>
      </DropdownMenuTrigger>

      <DropdownMenuContent align={align} className='w-48'>
        {items.map((item, index) => (
          <React.Fragment key={index}>
            {item.separator && <DropdownMenuSeparator />}
            <DropdownMenuItem
              onClick={item.onClick}
              disabled={item.disabled}
              variant={item.variant}
              className={cn(
                'cursor-pointer',
                item.variant === 'destructive' &&
                  'focus:bg-destructive/10 focus:text-destructive'
              )}
            >
              {item.icon && <span className='mr-2'>{item.icon}</span>}
              {item.label}
            </DropdownMenuItem>
          </React.Fragment>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  );
};
