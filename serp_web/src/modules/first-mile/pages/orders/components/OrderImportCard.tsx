/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Order Excel import toolbar
 */

import React from 'react';
import { TmsExcelImportToolbar } from '../../../components/list';
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
  className?: string;
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
  className,
}) => {
  const details =
    validateImportResult || lastImportJob ? (
      <div className='space-y-3 text-xs'>
        {validateImportResult ? (
          <div className='space-y-2 rounded-md border p-2'>
            <p className='font-medium'>
              Validation:{' '}
              {validateImportResult.is_success ? 'Success' : 'Failed'} (
              {validateImportResult.data.length} rows)
            </p>
            {validateImportResult.error_message ? (
              <pre className='whitespace-pre-wrap rounded-md bg-muted p-2 text-destructive'>
                {validateImportResult.error_message}
              </pre>
            ) : null}
            {validatedPreviewItems.length > 0 ? (
              <div className='grid gap-2 sm:grid-cols-2'>
                {validatedPreviewItems.map((item, index) => (
                  <div
                    key={`${item.customer_order_code || 'order'}-${index}`}
                    className='rounded-md border p-2 text-muted-foreground'
                  >
                    <p className='font-medium text-foreground'>
                      {item.customer_order_code || '-'}
                    </p>
                    <p>Products: {formatProductsPreview(item.products)}</p>
                  </div>
                ))}
              </div>
            ) : null}
            {validateImportResult.data.length > importPreviewLimit ? (
              <p className='text-muted-foreground'>
                Showing first {importPreviewLimit} of{' '}
                {validateImportResult.data.length} rows.
              </p>
            ) : null}
          </div>
        ) : null}

        {lastImportJob ? (
          <div className='rounded-md border p-2 text-muted-foreground'>
            <p className='font-medium text-foreground'>Latest import job</p>
            <p>
              #{lastImportJob.id} - {lastImportJob.file_name} ({lastImportJob.status})
            </p>
          </div>
        ) : null}
      </div>
    ) : undefined;

  return (
    <TmsExcelImportToolbar
      className={className}
      canImport={canMutateOrders}
      entityLabel='orders'
      isBusy={isImportFlowBusy}
      isExportingTemplate={isExportingTemplate}
      isValidating={isValidatingImport}
      isImporting={isImportingOrders}
      importFileInputKey={importFileInputKey}
      selectedFileName={selectedImportFile?.name}
      permissionHint='Import requires TMS_ADMIN or TMS_CUSTOMER.'
      onDownloadTemplate={onDownloadTemplate}
      onSelectFile={onSelectImportFile}
      onValidate={onValidateImportFile}
      onImport={onImportFile}
      canImportFile={Boolean(validateImportResult?.is_success)}
      details={details}
    />
  );
};
