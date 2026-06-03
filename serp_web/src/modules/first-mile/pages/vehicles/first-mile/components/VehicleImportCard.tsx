/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Vehicle import card
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
  ValidateImportFileResponse,
  VehicleImportItem,
} from '../../../../types';

interface VehicleImportCardProps {
  canManageVehicles: boolean;
  isImportFlowBusy: boolean;
  isExportingTemplate: boolean;
  isValidatingImport: boolean;
  isImportingVehicles: boolean;
  importFileInputKey: number;
  selectedImportFile: File | null;
  validateImportResult: ValidateImportFileResponse<VehicleImportItem> | null;
  lastImportJob: ImportHistory | null;
  onDownloadTemplate: () => void;
  onSelectImportFile: (event: React.ChangeEvent<HTMLInputElement>) => void;
  onValidateFile: () => void;
  onImportFile: () => void;
}

export const VehicleImportCard: React.FC<VehicleImportCardProps> = ({
  canManageVehicles,
  isImportFlowBusy,
  isExportingTemplate,
  isValidatingImport,
  isImportingVehicles,
  importFileInputKey,
  selectedImportFile,
  validateImportResult,
  lastImportJob,
  onDownloadTemplate,
  onSelectImportFile,
  onValidateFile,
  onImportFile,
}) => {
  const [isValidationDialogOpen, setIsValidationDialogOpen] =
    React.useState(false);

  React.useEffect(() => {
    if (validateImportResult) {
      setIsValidationDialogOpen(true);
    }
  }, [validateImportResult]);

  return (
    <>
      <Card>
        <CardHeader>
          <CardTitle>Excel Import</CardTitle>
          <CardDescription>
            Download template, choose the completed file, then import vehicles.
          </CardDescription>
        </CardHeader>
        <CardContent className='space-y-4'>
          <div className='flex flex-col gap-2 lg:flex-row lg:items-center'>
            <Button
              type='button'
              variant='outline'
              onClick={onDownloadTemplate}
              disabled={!canManageVehicles || isImportFlowBusy}
            >
              {isExportingTemplate ? (
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
              onChange={onSelectImportFile}
              disabled={!canManageVehicles || isImportFlowBusy}
              className='lg:max-w-sm'
            />

            <Button
              type='button'
              onClick={onValidateFile}
              disabled={
                !canManageVehicles || !selectedImportFile || isImportFlowBusy
              }
            >
              {isValidatingImport || isImportingVehicles ? (
                <Loader2 className='mr-2 h-4 w-4 animate-spin' />
              ) : (
                <FileUp className='mr-2 h-4 w-4' />
              )}
              Import file
            </Button>
          </div>

          {!canManageVehicles && (
            <p className='text-xs text-muted-foreground'>
              Import actions require TMS_ADMIN or TMS_POSTOFFICER_MANAGER
              permission.
            </p>
          )}

          {selectedImportFile && (
            <p className='text-sm text-muted-foreground'>
              Selected file: {selectedImportFile.name}
            </p>
          )}

          {lastImportJob && (
            <div className='space-y-1 rounded-lg border p-3'>
              <p className='text-sm font-medium'>Latest import job</p>
              <p className='text-xs text-muted-foreground'>
                #{lastImportJob.id} - {lastImportJob.file_name}
              </p>
              <p className='text-xs text-muted-foreground'>
                Status: {lastImportJob.status} | Success/Failed:{' '}
                {lastImportJob.success_records}/{lastImportJob.failed_records}
              </p>
            </div>
          )}
        </CardContent>
      </Card>
      <TmsImportValidationResultDialog
        open={isValidationDialogOpen}
        onOpenChange={setIsValidationDialogOpen}
        result={validateImportResult}
        entityLabel='vehicle'
        isImporting={isImportingVehicles}
        onConfirmImport={onImportFile}
      />
    </>
  );
};
