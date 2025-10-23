/*
Author: QuanTuanHuy
Description: Part of Serp Project - DataTable types for reusable table component
*/

import { ReactNode } from 'react';

export interface ColumnDef<TData = any> {
  id: string;
  header: string;
  accessor: string | ((row: TData) => any);
  cell?: (data: { row: TData; value: any }) => ReactNode;
  sortable?: boolean;
  defaultVisible?: boolean;
  width?: string;
  align?: 'left' | 'center' | 'right';
}

export interface ColumnVisibility {
  [columnId: string]: boolean;
}

export interface DataTableProps<TData = any> {
  columns: ColumnDef<TData>[];
  data: TData[];
  keyExtractor: (row: TData) => string | number;
  isLoading?: boolean;
  error?: any;
  onRowClick?: (row: TData) => void;
  storageKey?: string; // Key for localStorage to persist column visibility
  className?: string;
  // Pagination props
  pagination?: {
    currentPage: number;
    totalPages: number;
    totalItems: number;
    onPageChange: (page: number) => void;
    isFetching?: boolean;
  };
}

export interface ColumnVisibilityMenuProps {
  columns: ColumnDef[];
  visibility: ColumnVisibility;
  onVisibilityChange: (columnId: string, visible: boolean) => void;
}
