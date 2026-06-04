/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - TMS Excel import dialog trigger
 */

'use client';

import React from 'react';
import {
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Input,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  Check,
  Download,
  FileSpreadsheet,
  FileUp,
  Loader2,
} from 'lucide-react';
import type { ValidateImportFileResponse } from '../../types';
import { TmsImportValidationResultPanel } from './TmsImportValidationResultDialog';

type ImportStep = 'upload' | 'validate';

interface TmsExcelImportToolbarProps<T extends object> {
  canImport: boolean;
  entityLabel: string;
  isBusy: boolean;
  isExportingTemplate: boolean;
  isValidating: boolean;
  isImporting: boolean;
  importFileInputKey: number;
  selectedFileName?: string | null;
  validateImportResult?: ValidateImportFileResponse<T> | null;
  permissionHint?: string;
  onDownloadTemplate: () => void;
  onSelectFile: (event: React.ChangeEvent<HTMLInputElement>) => void;
  onValidate: () => void;
  onConfirmImport?: () => void;
  details?: React.ReactNode;
  className?: string;
}

export const TmsExcelImportToolbar = <T extends object>({
  canImport,
  entityLabel,
  isBusy,
  isExportingTemplate,
  isValidating,
  isImporting,
  importFileInputKey,
  selectedFileName,
  validateImportResult,
  permissionHint,
  onDownloadTemplate,
  onSelectFile,
  onValidate,
  onConfirmImport,
  details,
  className,
}: TmsExcelImportToolbarProps<T>) => {
  const [isOpen, setIsOpen] = React.useState(false);
  const [currentStep, setCurrentStep] = React.useState<ImportStep>('upload');

  React.useEffect(() => {
    if (validateImportResult && isOpen) {
      setCurrentStep('validate');
    }
  }, [isOpen, validateImportResult]);

  if (!canImport && !permissionHint) {
    return null;
  }

  const closeDialog = () => {
    if (!isImporting) {
      setIsOpen(false);
    }
  };

  const handleOpenChange = (open: boolean) => {
    if (isImporting) {
      return;
    }

    setIsOpen(open);
    if (open) {
      setCurrentStep(validateImportResult ? 'validate' : 'upload');
    } else {
      setCurrentStep('upload');
    }
  };

  const stepItems: Array<{
    key: ImportStep;
    label: string;
  }> = [
    { key: 'upload', label: 'Upload file' },
    { key: 'validate', label: 'Validate data' },
  ];

  const activeStepIndex = stepItems.findIndex(
    (step) => step.key === currentStep
  );

  return (
    <>
      <Button
        type='button'
        variant='outline'
        onClick={() => handleOpenChange(true)}
        disabled={!canImport}
        title={!canImport ? permissionHint : undefined}
        className={className}
      >
        <FileSpreadsheet className='mr-2 h-4 w-4 text-emerald-600' />
        Import
      </Button>

      <Dialog open={isOpen} onOpenChange={handleOpenChange}>
        <DialogContent className='flex h-[88vh] w-[calc(100vw-2rem)] max-w-[calc(100vw-2rem)] flex-col overflow-hidden p-4 sm:max-w-6xl'>
          <div className='space-y-4'>
            <DialogHeader>
              <DialogTitle>Import {entityLabel} from Excel</DialogTitle>
              <DialogDescription>
                Upload the completed workbook, validate the rows, then confirm
                the import.
              </DialogDescription>
            </DialogHeader>

            <div className='flex items-center gap-3'>
              {stepItems.map((step, index) => {
                const isActive = step.key === currentStep;
                const isComplete = index < activeStepIndex;

                return (
                  <React.Fragment key={step.key}>
                    <div className='flex min-w-0 items-center gap-2'>
                      <div
                        className={cn(
                          'flex h-8 w-8 shrink-0 items-center justify-center rounded-full border text-xs font-medium',
                          isActive &&
                            'border-primary bg-primary text-primary-foreground',
                          isComplete &&
                            'border-emerald-600 bg-emerald-600 text-white',
                          !isActive &&
                            !isComplete &&
                            'border-muted-foreground/30 text-muted-foreground'
                        )}
                      >
                        {isComplete ? <Check className='h-4 w-4' /> : index + 1}
                      </div>
                      <span
                        className={cn(
                          'truncate text-sm font-medium',
                          !isActive && 'text-muted-foreground'
                        )}
                      >
                        {step.label}
                      </span>
                    </div>
                    {index < stepItems.length - 1 ? (
                      <div className='h-px flex-1 bg-border' />
                    ) : null}
                  </React.Fragment>
                );
              })}
            </div>
          </div>

          {currentStep === 'upload' ? (
            <div className='flex min-h-0 flex-1 flex-col gap-4 overflow-auto py-2'>
              <div className='rounded-lg border border-dashed p-6'>
                <div className='flex flex-col items-center gap-3 text-center'>
                  <div className='flex h-12 w-12 items-center justify-center rounded-full bg-emerald-50 text-emerald-700'>
                    <FileSpreadsheet className='h-6 w-6' />
                  </div>
                  <div>
                    <p className='text-sm font-medium'>
                      Choose an Excel workbook
                    </p>
                    <p className='text-xs text-muted-foreground'>
                      Supported formats: .xlsx and .xls.
                    </p>
                  </div>
                  <Input
                    key={importFileInputKey}
                    type='file'
                    accept='.xlsx,.xls'
                    onChange={onSelectFile}
                    disabled={!canImport || isBusy}
                    className='max-w-sm'
                  />
                  {selectedFileName ? (
                    <p className='max-w-full truncate text-sm text-muted-foreground'>
                      Selected: {selectedFileName}
                    </p>
                  ) : null}
                </div>
              </div>

              <div className='flex flex-col gap-3 rounded-md border bg-muted/30 p-3 sm:flex-row sm:items-center sm:justify-between'>
                <p className='text-sm text-muted-foreground'>
                  Use the latest template before filling the Excel file.
                </p>
                <Button
                  type='button'
                  variant='outline'
                  onClick={onDownloadTemplate}
                  disabled={!canImport || isBusy}
                >
                  {isExportingTemplate ? (
                    <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                  ) : (
                    <Download className='mr-2 h-4 w-4' />
                  )}
                  Download template
                </Button>
              </div>

              {!canImport && permissionHint ? (
                <p className='text-xs text-muted-foreground'>
                  {permissionHint}
                </p>
              ) : null}

              {details ? <div className='space-y-2'>{details}</div> : null}
            </div>
          ) : validateImportResult ? (
            <TmsImportValidationResultPanel
              result={validateImportResult}
              entityLabel={entityLabel}
              isImporting={isImporting}
              onBack={() => setCurrentStep('upload')}
              onClose={closeDialog}
              onConfirmImport={onConfirmImport}
            />
          ) : null}

          {currentStep === 'upload' ? (
            <DialogFooter className='gap-2 sm:justify-end'>
              <Button
                type='button'
                variant='outline'
                onClick={closeDialog}
                disabled={isBusy}
              >
                Cancel
              </Button>
              <Button
                type='button'
                onClick={onValidate}
                disabled={!canImport || !selectedFileName || isBusy}
              >
                {isValidating || isImporting ? (
                  <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                ) : (
                  <FileUp className='mr-2 h-4 w-4' />
                )}
                Validate file
              </Button>
            </DialogFooter>
          ) : null}
        </DialogContent>
      </Dialog>
    </>
  );
};
