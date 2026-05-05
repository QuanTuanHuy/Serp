/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Hub Excel import card
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
  HubImportItem,
  ImportHistory,
  ValidateImportFileResponse,
} from '../../../types';

interface HubImportCardProps {
  isTmsAdmin: boolean;
  isImportFlowBusy: boolean;
  isExportingTemplate: boolean;
  isValidatingImport: boolean;
  isImportingHubs: boolean;
  importFileInputKey: number;
  selectedImportFile: File | null;
  validateImportResult: ValidateImportFileResponse<HubImportItem> | null;
  validatedPreviewItems: HubImportItem[];
  lastImportJob: ImportHistory | null;
  previewLimit: number;
  getProvinceLabel: (provinceCode?: string) => string;
  onSelectImportFile: (event: React.ChangeEvent<HTMLInputElement>) => void;
  onDownloadTemplate: () => Promise<void>;
  onValidateImportFile: () => Promise<void>;
  onImportFile: () => Promise<void>;
}

export const HubImportCard: React.FC<HubImportCardProps> = ({
  isTmsAdmin,
  isImportFlowBusy,
  isExportingTemplate,
  isValidatingImport,
  isImportingHubs,
  importFileInputKey,
  selectedImportFile,
  validateImportResult,
  validatedPreviewItems,
  lastImportJob,
  previewLimit,
  getProvinceLabel,
  onSelectImportFile,
  onDownloadTemplate,
  onValidateImportFile,
  onImportFile,
}) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Excel import</CardTitle>
        <CardDescription>
          Download the template, validate the filled file, then import hubs.
        </CardDescription>
      </CardHeader>
      <CardContent className='space-y-4'>
        <div className='flex flex-col gap-2 lg:flex-row lg:items-center'>
          <Button
            type='button'
            variant='outline'
            onClick={onDownloadTemplate}
            disabled={!isTmsAdmin || isImportFlowBusy}
          >
            {isExportingTemplate ? (
              <Loader2 className='h-4 w-4 mr-2 animate-spin' />
            ) : (
              <Download className='h-4 w-4 mr-2' />
            )}
            Download template
          </Button>

          <Input
            key={importFileInputKey}
            type='file'
            accept='.xlsx,.xls'
            onChange={onSelectImportFile}
            disabled={!isTmsAdmin || isImportFlowBusy}
            className='lg:max-w-sm'
          />

          <Button
            type='button'
            variant='outline'
            onClick={onValidateImportFile}
            disabled={!isTmsAdmin || !selectedImportFile || isImportFlowBusy}
          >
            {isValidatingImport && (
              <Loader2 className='h-4 w-4 mr-2 animate-spin' />
            )}
            Validate file
          </Button>

          <Button
            type='button'
            onClick={onImportFile}
            disabled={
              !isTmsAdmin ||
              !selectedImportFile ||
              !validateImportResult?.is_success ||
              isImportFlowBusy
            }
          >
            {isImportingHubs ? (
              <Loader2 className='h-4 w-4 mr-2 animate-spin' />
            ) : (
              <FileUp className='h-4 w-4 mr-2' />
            )}
            Import file
          </Button>
        </div>

        {!isTmsAdmin && (
          <p className='text-xs text-muted-foreground'>
            Import actions require TMS_ADMIN permission.
          </p>
        )}

        {selectedImportFile && (
          <p className='text-sm text-muted-foreground'>
            Selected file: {selectedImportFile.name}
          </p>
        )}

        {validateImportResult && (
          <div className='rounded-lg border p-3 space-y-3'>
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
                  {validateImportResult.data.length} row(s)
                </p>
                <div className='grid gap-2 sm:grid-cols-2'>
                  {validatedPreviewItems.map((item, index) => (
                    <div
                      key={`${item.code || 'hub'}-${index}`}
                      className='rounded-md border p-2 text-xs space-y-1'
                    >
                      <p className='font-medium'>
                        {(item.code || '-').trim()} — {(item.name || '-').trim()}
                      </p>
                      <p className='text-muted-foreground'>
                        Type: {item.hub_type ?? '-'}
                      </p>
                      <p className='text-muted-foreground'>
                        Province: {getProvinceLabel(item.province_code)} (
                        {item.province_code ?? '-'})
                      </p>
                      <p className='text-muted-foreground'>
                        Ward code: {item.ward_code ?? '-'}
                      </p>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}

        {lastImportJob && (
          <div className='rounded-md border p-3 text-xs space-y-1'>
            <p className='font-medium'>Last import job</p>
            <p>Status: {lastImportJob.status}</p>
            <p>
              Records: {lastImportJob.success_records} ok /{' '}
              {lastImportJob.failed_records} failed / {lastImportJob.total_records}{' '}
              total
            </p>
            {lastImportJob.error_message && (
              <p className='text-destructive'>{lastImportJob.error_message}</p>
            )}
          </div>
        )}

        <p className='text-xs text-muted-foreground'>
          Preview is limited to the first {previewLimit} validated rows.
        </p>
      </CardContent>
    </Card>
  );
};
