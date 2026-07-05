'use client';

import * as React from 'react';
import { Search } from 'lucide-react';
import type { LucideIcon } from 'lucide-react';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { schoolBusUi } from '../../theme';
import { SchoolBusPaginationBar } from '../SchoolBusPaginationBar';
import { SchoolBusEmptyState } from '../SchoolBusEmptyState';

export interface SchoolBusTableColumn<T> {
  key: string;
  header: React.ReactNode;
  className?: string;
  headerClassName?: string;
  width?: string;
  sticky?: 'left' | 'right';
  render: (row: T) => React.ReactNode;
}

export interface SchoolBusTableTab {
  key: string;
  label: string;
  count?: number;
}

export interface SchoolBusTablePagination {
  page?: any; // PagedResponse<any>
  onPageChange: (page: number) => void;
}

interface SchoolBusDataTableProps<T> {
  // Simple Mode Header
  title?: string;
  description?: string;

  // Tabs Mode
  tabs?: SchoolBusTableTab[];
  activeTab?: string;
  onTabChange?: (key: string) => void;

  // Toolbar (filters, search, etc.)
  toolbar?: React.ReactNode;

  // Table Configuration
  data?: T[];
  columns?: SchoolBusTableColumn<T>[];
  isLoading?: boolean;

  // Sticky columns optional controls
  stickyFirstColumn?: boolean;
  stickyActionColumn?: boolean;

  // Pagination
  pagination?: SchoolBusTablePagination;

  // Empty State
  emptyIcon?: LucideIcon;
  emptyTitle?: string;
  emptyDescription?: string;

  // Custom Children (replaces default table rendering)
  children?: React.ReactNode;

  // Extra classes for card container
  className?: string;

  // Row Interactions
  onRowDoubleClick?: (row: T) => void;
}

export function SchoolBusDataTable<T>({
  title,
  description,
  tabs,
  activeTab,
  onTabChange,
  toolbar,
  data = [],
  columns = [],
  isLoading = false,
  stickyFirstColumn = false,
  stickyActionColumn = false,
  pagination,
  emptyIcon,
  emptyTitle,
  emptyDescription,
  children,
  className,
  onRowDoubleClick,
}: SchoolBusDataTableProps<T>) {
  // Detect if there are tabs
  const hasTabs = tabs && tabs.length > 0;

  // Render non-tabbed title/description header
  const renderHeader = () => {
    if (hasTabs || (!title && !description)) return null;
    return (
      <div className='px-6 pt-5 pb-4'>
        {title && (
          <h3 className='text-base font-semibold text-foreground'>{title}</h3>
        )}
        {description && (
          <p className='mt-1 text-xs text-muted-foreground'>{description}</p>
        )}
      </div>
    );
  };

  // Render tabbed switcher header
  const renderTabs = () => {
    if (!hasTabs) return null;
    return (
      <div className='border-b border-border bg-muted/20 px-6 pt-4 pb-3'>
        <div className='flex items-center justify-start'>
          <div className='inline-flex h-10 items-center justify-start gap-1 rounded-xl border border-border bg-muted/60 p-1 shadow-none'>
            {tabs.map((tab) => {
              const isActive = activeTab === tab.key;
              return (
                <button
                  key={tab.key}
                  type='button'
                  onClick={() => onTabChange?.(tab.key)}
                  className={cn(
                    'group relative rounded-lg px-3.5 py-1.5 text-xs font-semibold transition-all duration-150 outline-none select-none border-0 shadow-none',
                    isActive
                      ? 'bg-[#C81E3A] text-white hover:bg-[#B31B34] active:bg-[#99182D]'
                      : 'text-muted-foreground hover:bg-muted hover:text-foreground'
                  )}
                >
                  <span className='inline-flex items-center gap-1.5 whitespace-nowrap'>
                    <span>{tab.label}</span>
                    {tab.count !== undefined && (
                      <span
                        className={cn(
                          'rounded-md px-1.5 py-0.5 text-[9px] font-bold transition-colors duration-150',
                          isActive
                            ? 'bg-white/20 text-white'
                            : 'bg-muted text-muted-foreground group-hover:text-foreground'
                        )}
                      >
                        {tab.count}
                      </span>
                    )}
                  </span>
                </button>
              );
            })}
          </div>
        </div>
      </div>
    );
  };

  // Render Toolbar
  const renderToolbar = () => {
    if (!toolbar) return null;
    return (
      <div
        className={cn(
          'flex flex-wrap items-center justify-between gap-4 bg-muted/20 px-6 py-3',
          hasTabs ? 'border-b border-border' : 'border-t border-b border-border'
        )}
      >
        {toolbar}
      </div>
    );
  };

  // Render Body Content
  const renderBody = () => {
    if (isLoading) {
      return (
        <div className='flex flex-col items-center justify-center gap-3 px-6 py-16 text-sm text-muted-foreground'>
          <div className='h-6 w-6 animate-spin rounded-full border-2 border-border border-t-[#C81E3A]' />
          <span>Đang tải dữ liệu...</span>
        </div>
      );
    }

    if (children) {
      return children;
    }

    if (!data || data.length === 0) {
      return (
        <div className='px-6 py-12'>
          <SchoolBusEmptyState
            title={emptyTitle || 'Không tìm thấy dữ liệu'}
            description={
              emptyDescription || 'Hãy thử điều chỉnh bộ lọc hoặc từ khóa tìm kiếm.'
            }
            icon={emptyIcon}
            className='min-h-[220px]'
          />
        </div>
      );
    }

    return (
      <div className='overflow-x-auto min-h-0 min-w-0'>
        <Table className='min-w-full divide-y divide-border'>
          <TableHeader className='border-b border-border bg-muted/40 hover:bg-muted/40'>
            <TableRow className='border-b border-border'>
              {columns.map((col, index) => {
                const isLeftSticky =
                  col.sticky === 'left' || (index === 0 && stickyFirstColumn);
                const isRightSticky =
                  col.sticky === 'right' ||
                  (index === columns.length - 1 && stickyActionColumn);
                return (
                  <TableHead
                    key={col.key}
                    className={cn(
                      'h-10 text-xs font-bold text-muted-foreground uppercase tracking-wider whitespace-nowrap',
                      col.headerClassName,
                      isLeftSticky &&
                        'sticky left-0 z-20 border-r border-border bg-muted shadow-[2px_0_5px_-2px_rgba(0,0,0,0.05)]',
                      isRightSticky &&
                        'sticky right-0 z-20 border-l border-border bg-muted shadow-[-2px_0_5px_-2px_rgba(0,0,0,0.05)]'
                    )}
                    style={{ width: col.width }}
                  >
                    {col.header}
                  </TableHead>
                );
              })}
            </TableRow>
          </TableHeader>
          <TableBody className='divide-y divide-border border-b border-border'>
            {data.map((row, rowIndex) => (
              <TableRow
                key={(row as any).id || rowIndex}
                className={cn(
                  'group border-b border-border transition-colors last:border-b-0 hover:bg-muted/40',
                  onRowDoubleClick && 'cursor-pointer select-none'
                )}
                onDoubleClick={() => onRowDoubleClick?.(row)}
              >
                {columns.map((col, colIndex) => {
                  const isLeftSticky =
                    col.sticky === 'left' ||
                    (colIndex === 0 && stickyFirstColumn);
                  const isRightSticky =
                    col.sticky === 'right' ||
                    (colIndex === columns.length - 1 && stickyActionColumn);
                  return (
                    <TableCell
                      key={col.key}
                      className={cn(
                        'py-3.5 text-sm text-foreground whitespace-nowrap align-middle',
                        col.className,
                        isLeftSticky &&
                          'sticky left-0 z-10 border-r border-border bg-card shadow-[2px_0_5px_-2px_rgba(0,0,0,0.05)] transition-colors group-hover:bg-muted',
                        isRightSticky &&
                          'sticky right-0 z-10 border-l border-border bg-card shadow-[-2px_0_5px_-2px_rgba(0,0,0,0.05)] transition-colors group-hover:bg-muted'
                      )}
                    >
                      {col.render(row)}
                    </TableCell>
                  );
                })}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    );
  };

  // Render Footer Pagination
  const renderPagination = () => {
    if (!pagination || !pagination.page) return null;
    return (
      <SchoolBusPaginationBar
        page={pagination.page}
        onPageChange={pagination.onPageChange}
      />
    );
  };

  return (
    <div
      className={cn(
        schoolBusUi.card,
        'flex flex-col overflow-hidden p-0',
        className
      )}
    >
      {renderHeader()}
      {renderTabs()}
      {renderToolbar()}
      {renderBody()}
      {renderPagination()}
    </div>
  );
}
