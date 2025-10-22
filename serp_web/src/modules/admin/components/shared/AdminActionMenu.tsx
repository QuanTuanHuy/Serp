/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin action menu component
 */

'use client';

import React, { useState } from 'react';
import { Button } from '@/shared/components/ui/button';
import { MoreHorizontal } from 'lucide-react';
import { cn } from '@/shared/utils';

export interface AdminActionMenuItem {
  label: string;
  onClick: () => void;
  icon?: React.ReactNode;
  variant?: 'default' | 'destructive';
  disabled?: boolean;
  separator?: boolean;
}

export interface AdminActionMenuProps {
  items: AdminActionMenuItem[];
  triggerLabel?: string;
  triggerIcon?: React.ReactNode;
  align?: 'start' | 'center' | 'end';
  className?: string;
}

/**
 * AdminActionMenu - Dropdown menu for row actions in tables
 * Simple implementation without external dropdown-menu component
 *
 * Usage:
 * ```tsx
 * <AdminActionMenu
 *   items={[
 *     { label: 'Edit', onClick: handleEdit, icon: <Edit className="h-4 w-4" /> },
 *     { label: 'Delete', onClick: handleDelete, variant: 'destructive', separator: true },
 *   ]}
 * />
 * ```
 */
export const AdminActionMenu: React.FC<AdminActionMenuProps> = ({
  items,
  triggerLabel,
  triggerIcon = <MoreHorizontal className='h-4 w-4' />,
  align = 'end',
  className,
}) => {
  const [isOpen, setIsOpen] = useState(false);

  return (
    <div className='relative'>
      <Button
        variant='ghost'
        size='sm'
        className={className}
        onClick={() => setIsOpen(!isOpen)}
      >
        {triggerIcon}
        {triggerLabel && <span className='ml-2'>{triggerLabel}</span>}
      </Button>

      {isOpen && (
        <>
          {/* Backdrop */}
          <div
            className='fixed inset-0 z-40'
            onClick={() => setIsOpen(false)}
          />

          {/* Menu */}
          <div
            className={cn(
              'absolute z-50 mt-1 w-48 rounded-md border bg-popover p-1 shadow-md',
              align === 'end' && 'right-0',
              align === 'start' && 'left-0',
              align === 'center' && 'left-1/2 -translate-x-1/2'
            )}
          >
            {items.map((item, index) => (
              <React.Fragment key={index}>
                {item.separator && <div className='my-1 h-px bg-border' />}
                <button
                  onClick={() => {
                    item.onClick();
                    setIsOpen(false);
                  }}
                  disabled={item.disabled}
                  className={cn(
                    'flex w-full items-center rounded-sm px-2 py-2 text-sm transition-colors hover:bg-accent disabled:opacity-50 disabled:pointer-events-none',
                    item.variant === 'destructive' &&
                      'text-destructive hover:text-destructive'
                  )}
                >
                  {item.icon && <span className='mr-2'>{item.icon}</span>}
                  {item.label}
                </button>
              </React.Fragment>
            ))}
          </div>
        </>
      )}
    </div>
  );
};
