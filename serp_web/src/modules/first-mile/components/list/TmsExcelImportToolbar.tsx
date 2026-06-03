/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Compact TMS Excel import toolbar
 */

'use client';

import React from 'react';
import { Button, Input } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  ChevronDown,
  ChevronUp,
  Download,
  FileUp,
  Loader2,
} from 'lucide-react';

interface TmsExcelImportToolbarProps {
  canImport: boolean;
  entityLabel: string;
  isBusy: boolean;
  isExportingTemplate: boolean;
  isValidating: boolean;
  isImporting: boolean;
  importFileInputKey: number;
  selectedFileName?: string | null;
  permissionHint?: string;
  onDownloadTemplate: () => void;
  onSelectFile: (event: React.ChangeEvent<HTMLInputElement>) => void;
  onValidate: () => void;
  details?: React.ReactNode;
  className?: string;
}

export const TmsExcelImportToolbar: React.FC<TmsExcelImportToolbarProps> = ({
  canImport,
  entityLabel,
  isBusy,
  isExportingTemplate,
  isValidating,
  isImporting,
  importFileInputKey,
  selectedFileName,
  permissionHint,
  onDownloadTemplate,
  onSelectFile,
  onValidate,
  details,
  className,
}) => {
  const [showDetails, setShowDetails] = React.useState(false);
  const hasDetails = Boolean(details);

  if (!canImport && !permissionHint) {
    return null;
  }

  return (
    <div
      className={cn(
        'rounded-lg border bg-card px-3 py-2 text-card-foreground shadow-sm',
        className
      )}
    >
      <div className='flex flex-col gap-2 lg:flex-row lg:items-center lg:justify-between'>
        <div className='min-w-0'>
          <p className='text-xs font-medium'>Excel import</p>
          <p className='text-[11px] text-muted-foreground'>
            Template and Excel file for {entityLabel}.
          </p>
        </div>

        <div className='flex flex-wrap items-center gap-2'>
          <Button
            type='button'
            size='sm'
            variant='outline'
            onClick={onDownloadTemplate}
            disabled={!canImport || isBusy}
          >
            {isExportingTemplate ? (
              <Loader2 className='mr-1.5 h-3.5 w-3.5 animate-spin' />
            ) : (
              <Download className='mr-1.5 h-3.5 w-3.5' />
            )}
            Template
          </Button>

          <Input
            key={importFileInputKey}
            type='file'
            accept='.xlsx,.xls'
            onChange={onSelectFile}
            disabled={!canImport || isBusy}
            className='h-8 max-w-[200px] text-xs file:mr-2 file:text-xs'
          />

          <Button
            type='button'
            size='sm'
            onClick={onValidate}
            disabled={!canImport || !selectedFileName || isBusy}
          >
            {isValidating || isImporting ? (
              <Loader2 className='mr-1.5 h-3.5 w-3.5 animate-spin' />
            ) : (
              <FileUp className='mr-1.5 h-3.5 w-3.5' />
            )}
            Import
          </Button>

          {hasDetails ? (
            <Button
              type='button'
              size='sm'
              variant='ghost'
              className='h-8 px-2'
              onClick={() => setShowDetails((prev) => !prev)}
            >
              {showDetails ? (
                <ChevronUp className='h-4 w-4' />
              ) : (
                <ChevronDown className='h-4 w-4' />
              )}
            </Button>
          ) : null}
        </div>
      </div>

      {selectedFileName ? (
        <p className='mt-1 truncate text-[11px] text-muted-foreground'>
          Selected: {selectedFileName}
        </p>
      ) : null}

      {!canImport && permissionHint ? (
        <p className='mt-1 text-[11px] text-muted-foreground'>
          {permissionHint}
        </p>
      ) : null}

      {hasDetails && showDetails ? (
        <div className='mt-2 space-y-2 border-t pt-2'>{details}</div>
      ) : null}
    </div>
  );
};
