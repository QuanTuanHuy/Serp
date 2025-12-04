'use client';

/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Export Dropdown Component
 */

import React from 'react';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
  DropdownMenuSeparator,
  DropdownMenuLabel,
} from '@/shared/components/ui/dropdown-menu';
import { Button } from '@/shared/components/ui/button';
import {
  Download,
  FileSpreadsheet,
  FileJson,
  FileText,
  Loader2,
} from 'lucide-react';
import {
  exportData,
  type ExportFormat,
  type ExportColumn,
} from '../../utils/export';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export interface ExportDropdownProps<T = any> {
  data: T[];
  columns: ExportColumn<T>[];
  filename: string;
  disabled?: boolean;
  isLoading?: boolean;
  onExportStart?: (format: ExportFormat) => void;
  onExportComplete?: (format: ExportFormat, count: number) => void;
  onExportError?: (error: Error) => void;
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function ExportDropdown<T = any>({
  data,
  columns,
  filename,
  disabled = false,
  isLoading = false,
  onExportStart,
  onExportComplete,
  onExportError,
}: ExportDropdownProps<T>) {
  const [exporting, setExporting] = React.useState(false);

  const handleExport = async (format: ExportFormat) => {
    if (data.length === 0) {
      return;
    }

    try {
      setExporting(true);
      onExportStart?.(format);

      // Add timestamp to filename
      const timestamp = new Date().toISOString().split('T')[0];
      const exportFilename = `${filename}_${timestamp}`;

      exportData({
        data,
        columns,
        filename: exportFilename,
        format,
        includeHeaders: true,
      });

      onExportComplete?.(format, data.length);
    } catch (error) {
      console.error('Export failed:', error);
      onExportError?.(
        error instanceof Error ? error : new Error('Export failed')
      );
    } finally {
      setExporting(false);
    }
  };

  const isDisabled = disabled || isLoading || exporting || data.length === 0;

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button
          variant='outline'
          size='sm'
          disabled={isDisabled}
          className='gap-2'
        >
          {isLoading || exporting ? (
            <Loader2 className='h-4 w-4 animate-spin' />
          ) : (
            <Download className='h-4 w-4' />
          )}
          Xuất dữ liệu
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align='end' className='w-48'>
        <DropdownMenuLabel className='text-xs text-muted-foreground'>
          {data.length} bản ghi
        </DropdownMenuLabel>
        <DropdownMenuSeparator />
        <DropdownMenuItem
          onClick={() => handleExport('csv')}
          className='gap-2 cursor-pointer'
        >
          <FileText className='h-4 w-4 text-green-600' />
          <span>Xuất CSV</span>
        </DropdownMenuItem>
        <DropdownMenuItem
          onClick={() => handleExport('excel')}
          className='gap-2 cursor-pointer'
        >
          <FileSpreadsheet className='h-4 w-4 text-emerald-600' />
          <span>Xuất Excel</span>
        </DropdownMenuItem>
        <DropdownMenuItem
          onClick={() => handleExport('json')}
          className='gap-2 cursor-pointer'
        >
          <FileJson className='h-4 w-4 text-blue-600' />
          <span>Xuất JSON</span>
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
