// ActionMenu Component (authors: QuanTuanHuy, Description: Part of Serp Project)

import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { useState, useRef, useEffect } from 'react';

interface ActionMenuItem {
  label: string;
  icon?: React.ReactNode;
  onClick: () => void;
  variant?: 'default' | 'destructive';
  disabled?: boolean;
}

interface ActionMenuProps {
  items: ActionMenuItem[];
  className?: string;
  trigger?: React.ReactNode;
  align?: 'left' | 'right';
}

export const ActionMenu: React.FC<ActionMenuProps> = ({
  items,
  className,
  trigger,
  align = 'right',
}) => {
  const [isOpen, setIsOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  // Close menu when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };

    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isOpen]);

  // Handle item click
  const handleItemClick = (item: ActionMenuItem) => {
    if (!item.disabled) {
      item.onClick();
      setIsOpen(false);
    }
  };

  // Default trigger (three dots menu)
  const defaultTrigger = (
    <Button
      variant='ghost'
      size='sm'
      className='h-8 w-8 p-0 hover:bg-gray-100'
      onClick={(e) => {
        e.stopPropagation();
        setIsOpen(!isOpen);
      }}
    >
      <svg className='h-4 w-4' fill='currentColor' viewBox='0 0 24 24'>
        <path d='M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z' />
      </svg>
    </Button>
  );

  return (
    <div className={cn('relative inline-block', className)} ref={menuRef}>
      {/* Trigger */}
      {trigger ? (
        <div
          onClick={(e) => {
            e.stopPropagation();
            setIsOpen(!isOpen);
          }}
        >
          {trigger}
        </div>
      ) : (
        defaultTrigger
      )}

      {/* Menu */}
      {isOpen && (
        <div
          className={cn(
            'absolute z-50 mt-1 w-48 rounded-md border bg-white shadow-lg',
            align === 'right' ? 'right-0' : 'left-0'
          )}
        >
          <div className='py-1'>
            {items.map((item, index) => (
              <button
                key={index}
                onClick={(e) => {
                  e.stopPropagation();
                  handleItemClick(item);
                }}
                disabled={item.disabled}
                className={cn(
                  'flex w-full items-center px-4 py-2 text-sm text-left transition-colors',
                  item.disabled
                    ? 'text-gray-400 cursor-not-allowed'
                    : item.variant === 'destructive'
                      ? 'text-red-600 hover:bg-red-50 hover:text-red-700'
                      : 'text-gray-700 hover:bg-gray-50 hover:text-gray-900'
                )}
              >
                {item.icon && (
                  <span className='mr-3 flex-shrink-0'>{item.icon}</span>
                )}
                {item.label}
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
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
        icon: (
          <svg
            className='h-4 w-4'
            fill='none'
            stroke='currentColor'
            viewBox='0 0 24 24'
          >
            <path
              strokeLinecap='round'
              strokeLinejoin='round'
              strokeWidth='2'
              d='M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z'
            />
          </svg>
        ),
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
        icon: (
          <svg
            className='h-4 w-4'
            fill='none'
            stroke='currentColor'
            viewBox='0 0 24 24'
          >
            <path
              strokeLinecap='round'
              strokeLinejoin='round'
              strokeWidth='2'
              d='M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16'
            />
          </svg>
        ),
        onClick,
        disabled,
        variant: 'destructive',
      },
    ]}
  />
);

export default ActionMenu;
