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
import type {
  ImportHistory,
  ValidateImportFileResponse,
  VehicleImportItem,
} from '../../../types';
import { IMPORT_PREVIEW_LIMIT } from '../vehiclePageModels';

interface VehicleImportCardProps {
  canManageVehicles: boolean;
  isImportFlowBusy: boolean;
  isExportingTemplate: boolean;
  isValidatingImport: boolean;
  isImportingVehicles: boolean;
  importFileInputKey: number;
  selectedImportFile: File | null;
  validateImportResult: ValidateImportFileResponse<VehicleImportItem> | null;
  validatedPreviewItems: VehicleImportItem[];
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
  validatedPreviewItems,
  lastImportJob,
  onDownloadTemplate,
  onSelectImportFile,
  onValidateFile,
  onImportFile,
}) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Excel Import</CardTitle>
        <CardDescription>
          Download template, validate the completed file, then import vehicles.
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
            variant='outline'
            onClick={onValidateFile}
            disabled={
              !canManageVehicles || !selectedImportFile || isImportFlowBusy
            }
          >
            {isValidatingImport && (
              <Loader2 className='mr-2 h-4 w-4 animate-spin' />
            )}
            Validate file
          </Button>

          <Button
            type='button'
            onClick={onImportFile}
            disabled={
              !canManageVehicles ||
              !selectedImportFile ||
              !validateImportResult?.is_success ||
              isImportFlowBusy
            }
          >
            {isImportingVehicles ? (
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

        {validateImportResult && (
          <div className='space-y-3 rounded-lg border p-3'>
            <div className='flex flex-wrap items-center gap-x-4 gap-y-1'>
              <p className='text-sm font-medium'>
                Validation:{' '}
                {validateImportResult.is_success ? 'Success' : 'Failed'}
              </p>
              <p className='text-xs text-muted-foreground'>
                File ID: {validateImportResult.file_id}
              </p>
              <p className='text-xs text-muted-foreground'>
                Parsed rows: {validateImportResult.data.length}
              </p>
            </div>

            {validateImportResult.error_message && (
              <pre className='whitespace-pre-wrap rounded-md bg-muted p-2 text-xs text-destructive'>
                {validateImportResult.error_message}
              </pre>
            )}

            {validatedPreviewItems.length > 0 && (
              <div className='space-y-2'>
                <p className='text-xs text-muted-foreground'>
                  Preview {validatedPreviewItems.length}/
                  {validateImportResult.data.length} validated row(s)
                </p>

                <div className='grid gap-2 sm:grid-cols-2'>
                  {validatedPreviewItems.map((item, index) => (
                    <div
                      key={`${item.license_plate || 'vehicle'}-${index}`}
                      className='space-y-1 rounded-md border p-2 text-xs'
                    >
                      <p className='font-medium'>
                        {(item.license_plate || '-').trim()} |{' '}
                        {item.vehicle_type || '-'} | {item.status || '-'}
                      </p>
                      <p className='text-muted-foreground'>
                        Post office:{' '}
                        {item.post_office_code
                          ? `${item.post_office_code}${item.post_office_name ? ` - ${item.post_office_name}` : ''}`
                          : '-'}
                      </p>
                      <p className='text-muted-foreground'>
                        Courier:{' '}
                        {item.post_office_staff_code
                          ? `${item.post_office_staff_code}${item.post_office_staff_name ? ` - ${item.post_office_staff_name}` : ''}`
                          : '-'}
                      </p>
                      <p className='text-muted-foreground'>
                        Source rows:{' '}
                        {item.source_rows?.length
                          ? item.source_rows.join(', ')
                          : '-'}
                      </p>
                    </div>
                  ))}
                </div>

                {validateImportResult.data.length > IMPORT_PREVIEW_LIMIT && (
                  <p className='text-xs text-muted-foreground'>
                    Showing only the first {IMPORT_PREVIEW_LIMIT} row(s).
                  </p>
                )}
              </div>
            )}
          </div>
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
  );
};
