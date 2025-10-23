// ActionMenu Component (authors: QuanTuanHuy, Description: Part of Serp Project)

import React from 'react';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
  DropdownMenuSeparator,
} from '@/shared/components/ui';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { MoreHorizontal, Edit, Trash2 } from 'lucide-react';

interface ActionMenuItem {
  label: string;
  icon?: React.ReactNode;
  onClick: () => void;
  variant?: 'default' | 'destructive';
  disabled?: boolean;
  separator?: boolean;
}

interface ActionMenuProps {
  items: ActionMenuItem[];
  className?: string;
  trigger?: React.ReactNode;
  align?: 'start' | 'center' | 'end';
}

export const ActionMenu: React.FC<ActionMenuProps> = ({
  items,
  className,
  trigger,
  align = 'end',
}) => {
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        {trigger || (
          <Button variant='ghost' size='sm' className='h-8 w-8 p-0'>
            <MoreHorizontal className='h-4 w-4' />
          </Button>
        )}
      </DropdownMenuTrigger>

      <DropdownMenuContent align={align} className={cn('w-48', className)}>
        {items.map((item, index) => (
          <React.Fragment key={index}>
            {item.separator && <DropdownMenuSeparator />}
            <DropdownMenuItem
              onClick={item.onClick}
              disabled={item.disabled}
              variant={item.variant}
              className='cursor-pointer'
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

// Quick action components for common actions
export const EditAction: React.FC<{
  onClick: () => void;
  disabled?: boolean;
}> = ({ onClick, disabled = false }) => (
  <ActionMenu
    items={[
      {
        label: 'Edit',
        icon: <Edit className='h-4 w-4' />,
        onClick,
        disabled,
      },
    ]}
  />
);

export const DeleteAction: React.FC<{
  onClick: () => void;
  disabled?: boolean;
}> = ({ onClick, disabled = false }) => (
  <ActionMenu
    items={[
      {
        label: 'Delete',
        icon: <Trash2 className='h-4 w-4' />,
        onClick,
        disabled,
        variant: 'destructive',
      },
    ]}
  />
);

export default ActionMenu;
