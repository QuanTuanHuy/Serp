/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Order import card
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
  OrderImportItem,
  ValidateImportFileResponse,
} from '../../../types';

interface OrderImportCardProps {
  canMutateOrders: boolean;
  isImportFlowBusy: boolean;
  isExportingTemplate: boolean;
  isValidatingImport: boolean;
  isImportingOrders: boolean;
  importFileInputKey: number;
  selectedImportFile: File | null;
  validateImportResult: ValidateImportFileResponse<OrderImportItem> | null;
  validatedPreviewItems: OrderImportItem[];
  importPreviewLimit: number;
  lastImportJob: ImportHistory | null;
  onDownloadTemplate: () => void;
  onSelectImportFile: (event: React.ChangeEvent<HTMLInputElement>) => void;
  onValidateImportFile: () => void;
  onImportFile: () => void;
  formatProductsPreview: (products?: OrderImportItem['products']) => string;
}

export const OrderImportCard: React.FC<OrderImportCardProps> = ({
  canMutateOrders,
  isImportFlowBusy,
  isExportingTemplate,
  isValidatingImport,
  isImportingOrders,
  importFileInputKey,
  selectedImportFile,
  validateImportResult,
  validatedPreviewItems,
  importPreviewLimit,
  lastImportJob,
  onDownloadTemplate,
  onSelectImportFile,
  onValidateImportFile,
  onImportFile,
  formatProductsPreview,
}) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Excel Import</CardTitle>
        <CardDescription>
          Download template, validate the completed file, then import orders.
        </CardDescription>
      </CardHeader>
      <CardContent className='space-y-4'>
        <div className='flex flex-col gap-2 lg:flex-row lg:items-center'>
          <Button
            type='button'
            variant='outline'
            onClick={onDownloadTemplate}
            disabled={!canMutateOrders || isImportFlowBusy}
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
            disabled={!canMutateOrders || isImportFlowBusy}
            className='lg:max-w-sm'
          />

          <Button
            type='button'
            variant='outline'
            onClick={onValidateImportFile}
            disabled={
              !canMutateOrders || !selectedImportFile || isImportFlowBusy
            }
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
              !canMutateOrders ||
              !selectedImportFile ||
              !validateImportResult?.is_success ||
              isImportFlowBusy
            }
          >
            {isImportingOrders ? (
              <Loader2 className='h-4 w-4 mr-2 animate-spin' />
            ) : (
              <FileUp className='h-4 w-4 mr-2' />
            )}
            Import file
          </Button>
        </div>

        {!canMutateOrders && (
          <p className='text-xs text-muted-foreground'>
            Import actions require TMS_ADMIN or TMS_CUSTOMER permission.
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
                  {validateImportResult.data.length} validated order(s)
                </p>

                <div className='grid gap-2 sm:grid-cols-2'>
                  {validatedPreviewItems.map((item, index) => (
                    <div
                      key={`${item.customer_order_code || 'order'}-${index}`}
                      className='rounded-md border p-2 text-xs space-y-1'
                    >
                      <p className='font-medium'>
                        {item.customer_order_code || '-'} |{' '}
                        {item.order_type || '-'} | {item.fee_payer || '-'}
                      </p>
                      <p className='text-muted-foreground'>
                        Sender:{' '}
                        {item.sender_name
                          ? `${item.sender_name}${item.sender_phone ? ` (${item.sender_phone})` : ''}`
                          : '-'}
                      </p>
                      <p className='text-muted-foreground'>
                        Receiver:{' '}
                        {item.receiver_name
                          ? `${item.receiver_name}${item.receiver_phone ? ` (${item.receiver_phone})` : ''}`
                          : '-'}
                      </p>
                      <p className='text-muted-foreground'>
                        COD: {item.is_cod ? 'Yes' : 'No'} | Products:{' '}
                        {item.products?.length ?? 0}
                      </p>
                      <p className='text-muted-foreground'>
                        Product preview: {formatProductsPreview(item.products)}
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

                {validateImportResult.data.length > importPreviewLimit && (
                  <p className='text-xs text-muted-foreground'>
                    Showing only the first {importPreviewLimit} order(s).
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
