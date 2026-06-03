/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Second-mile vehicle import card
 */

import React from 'react';
import {
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Input,
} from '@/shared/components/ui';
import { Download, FileUp, Loader2 } from 'lucide-react';
import { TmsImportValidationResultDialog } from '../../../../components/list';
import type {
  ImportHistory,
  SecondMileVehicleImportItem,
  ValidateImportFileResponse,
} from '../../../../types';

interface SecondMileVehicleImportCardProps {
  canManage: boolean;
  isBusy: boolean;
  isExporting: boolean;
  isValidating: boolean;
  isImporting: boolean;
  importFileInputKey: number;
  selectedFile: File | null;
  validateResult: ValidateImportFileResponse<SecondMileVehicleImportItem> | null;
  lastImportJob: ImportHistory | null;
  onDownloadTemplate: () => void;
  onSelectFile: (event: React.ChangeEvent<HTMLInputElement>) => void;
  onValidate: () => void;
  onImport: () => void;
}

export const SecondMileVehicleImportCard: React.FC<
  SecondMileVehicleImportCardProps
> = ({
  canManage,
  isBusy,
  isExporting,
  isValidating,
  isImporting,
  importFileInputKey,
  selectedFile,
  validateResult,
  lastImportJob,
  onDownloadTemplate,
  onSelectFile,
  onValidate,
  onImport,
}) => {
  const [isValidationDialogOpen, setIsValidationDialogOpen] =
    React.useState(false);

  React.useEffect(() => {
    if (validateResult) {
      setIsValidationDialogOpen(true);
    }
  }, [validateResult]);

  return (
    <>
      <Card>
        <CardHeader>
          <CardTitle>Excel import</CardTitle>
          <CardDescription>
            Download template, choose the file, then import vehicles (sheet
            VEHICLE).
          </CardDescription>
        </CardHeader>
        <CardContent className='space-y-4'>
          <div className='flex flex-col gap-2 lg:flex-row lg:items-center'>
            <Button
              type='button'
              variant='outline'
              onClick={onDownloadTemplate}
              disabled={!canManage || isBusy}
            >
              {isExporting ? (
                <Loader2 className='mr-2 h-4 w-4 animate-spin' />
              ) : (
                <Download className='mr-2 h-4 w-4' />
              )}
              Download template
            </Button>
            <Input
              key={importFileInputKey}
              type='file'
              accept='.xlsx,.xls'
              onChange={onSelectFile}
              disabled={!canManage || isBusy}
              className='lg:max-w-sm'
            />
            <Button
              type='button'
              onClick={onValidate}
              disabled={!canManage || !selectedFile || isBusy}
            >
              {isValidating || isImporting ? (
                <Loader2 className='mr-2 h-4 w-4 animate-spin' />
              ) : (
                <FileUp className='mr-2 h-4 w-4' />
              )}
              Import file
            </Button>
          </div>

          {!canManage && (
            <p className='text-xs text-muted-foreground'>
              Import requires TMS_ADMIN permission.
            </p>
          )}

          {selectedFile && (
            <p className='text-sm text-muted-foreground'>
              Selected: {selectedFile.name}
            </p>
          )}

          {lastImportJob && (
            <div className='rounded-lg border p-3 text-sm'>
              <p className='font-medium'>
                Latest import job #{lastImportJob.id}
              </p>
              <p className='text-muted-foreground'>
                {lastImportJob.status} — success {lastImportJob.success_records}{' '}
                / failed {lastImportJob.failed_records}
              </p>
            </div>
          )}
        </CardContent>
      </Card>
      <TmsImportValidationResultDialog
        open={isValidationDialogOpen}
        onOpenChange={setIsValidationDialogOpen}
        result={validateResult}
        entityLabel='vehicle'
        isImporting={isImporting}
        onConfirmImport={onImport}
      />
    </>
  );
};
