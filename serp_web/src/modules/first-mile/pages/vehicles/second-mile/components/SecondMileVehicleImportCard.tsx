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
import type {
  ImportHistory,
  SecondMileVehicleImportItem,
  ValidateImportFileResponse,
} from '../../../../types';
import { IMPORT_PREVIEW_LIMIT } from '../vehiclePageModels';

interface SecondMileVehicleImportCardProps {
  canManage: boolean;
  isBusy: boolean;
  isExporting: boolean;
  isValidating: boolean;
  isImporting: boolean;
  importFileInputKey: number;
  selectedFile: File | null;
  validateResult: ValidateImportFileResponse<SecondMileVehicleImportItem> | null;
  previewItems: SecondMileVehicleImportItem[];
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
  previewItems,
  lastImportJob,
  onDownloadTemplate,
  onSelectFile,
  onValidate,
  onImport,
}) => (
  <Card>
    <CardHeader>
      <CardTitle>Excel import</CardTitle>
      <CardDescription>
        Download template, validate the file, then import vehicles (sheet
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
          variant='outline'
          onClick={onValidate}
          disabled={!canManage || !selectedFile || isBusy}
        >
          {isValidating && <Loader2 className='mr-2 h-4 w-4 animate-spin' />}
          Validate file
        </Button>
        <Button
          type='button'
          onClick={onImport}
          disabled={
            !canManage ||
            !selectedFile ||
            !validateResult?.is_success ||
            isBusy
          }
        >
          {isImporting ? (
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

      {validateResult && (
        <div className='space-y-3 rounded-lg border p-3'>
          <p className='text-sm font-medium'>
            Validation: {validateResult.is_success ? 'Success' : 'Failed'} (
            {validateResult.data.length} rows)
          </p>
          {validateResult.error_message && (
            <pre className='whitespace-pre-wrap rounded-md bg-muted p-2 text-xs text-destructive'>
              {validateResult.error_message}
            </pre>
          )}
          {previewItems.length > 0 && (
            <div className='grid gap-2 sm:grid-cols-2'>
              {previewItems.map((item, index) => (
                <div
                  key={`${item.license_plate}-${index}`}
                  className='rounded-md border p-2 text-xs'
                >
                  <p className='font-medium'>{item.license_plate}</p>
                  <p className='text-muted-foreground'>
                    {item.vehicle_type} | bags: {item.max_bags} | hub:{' '}
                    {item.hub_code ?? item.hub_id}
                  </p>
                  <p className='text-muted-foreground'>
                    Driver: {item.driver_code ?? item.assigned_staff_id ?? '-'}
                  </p>
                </div>
              ))}
            </div>
          )}
          {validateResult.data.length > IMPORT_PREVIEW_LIMIT && (
            <p className='text-xs text-muted-foreground'>
              Showing first {IMPORT_PREVIEW_LIMIT} rows only.
            </p>
          )}
        </div>
      )}

      {lastImportJob && (
        <div className='rounded-lg border p-3 text-sm'>
          <p className='font-medium'>Latest import job #{lastImportJob.id}</p>
          <p className='text-muted-foreground'>
            {lastImportJob.status} — success {lastImportJob.success_records} /
            failed {lastImportJob.failed_records}
          </p>
        </div>
      )}
    </CardContent>
  </Card>
);
