/**
 * Author: Nguyễn Thế Anh
 * Description: Part of Serp Project - Post office import card
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
  PostOfficeImportItem,
  ValidateImportFileResponse,
} from '../../../types';

interface PostOfficeImportCardProps {
  isTmsAdmin: boolean;
  isImportFlowBusy: boolean;
  isExportingTemplate: boolean;
  isValidatingImport: boolean;
  isImportingPostOffices: boolean;
  importFileInputKey: number;
  selectedImportFile: File | null;
  validateImportResult: ValidateImportFileResponse<PostOfficeImportItem> | null;
  validatedPreviewItems: PostOfficeImportItem[];
  lastImportJob: ImportHistory | null;
  previewLimit: number;
  onSelectImportFile: (event: React.ChangeEvent<HTMLInputElement>) => void;
  onDownloadTemplate: () => Promise<void>;
  onValidateImportFile: () => Promise<void>;
  onImportFile: () => Promise<void>;
  getProvinceLabel: (provinceCode?: string) => string;
  getWardLabel: (provinceCode?: string, wardCode?: string) => string;
}

export const PostOfficeImportCard: React.FC<PostOfficeImportCardProps> = ({
  isTmsAdmin,
  isImportFlowBusy,
  isExportingTemplate,
  isValidatingImport,
  isImportingPostOffices,
  importFileInputKey,
  selectedImportFile,
  validateImportResult,
  validatedPreviewItems,
  lastImportJob,
  previewLimit,
  onSelectImportFile,
  onDownloadTemplate,
  onValidateImportFile,
  onImportFile,
  getProvinceLabel,
  getWardLabel,
}) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Excel Import</CardTitle>
        <CardDescription>
          Download the template, validate the filled file, then import post
          offices.
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
            {isImportingPostOffices ? (
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
                  {validateImportResult.data.length} validated row(s)
                </p>

                <div className='grid gap-2 sm:grid-cols-2'>
                  {validatedPreviewItems.map((item, index) => (
                    <div
                      key={`${item.code || 'post-office'}-${index}`}
                      className='rounded-md border p-2 text-xs space-y-1'
                    >
                      <p className='font-medium'>
                        {(item.code || '-').trim()} -{' '}
                        {(item.name || '-').trim()}
                      </p>
                      <p className='text-muted-foreground'>
                        Province/Ward: {getProvinceLabel(item.province_code)} /{' '}
                        {getWardLabel(item.province_code, item.ward_code)}
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

                {validateImportResult.data.length > previewLimit && (
                  <p className='text-xs text-muted-foreground'>
                    Showing only the first {previewLimit} row(s).
                  </p>
                )}
              </div>
            )}
          </div>
        )}

        {lastImportJob && (
          <div className='rounded-lg border p-3 space-y-1'>
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
