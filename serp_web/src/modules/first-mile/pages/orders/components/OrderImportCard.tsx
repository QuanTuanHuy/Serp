/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Order Excel import toolbar
 */

import React from 'react';
import {
  TmsExcelImportToolbar,
  TmsImportValidationResultDialog,
} from '../../../components/list';
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
  lastImportJob: ImportHistory | null;
  onDownloadTemplate: () => void;
  onSelectImportFile: (event: React.ChangeEvent<HTMLInputElement>) => void;
  onValidateImportFile: () => void;
  onImportFile: () => void;
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
  lastImportJob,
  onDownloadTemplate,
  onSelectImportFile,
  onValidateImportFile,
  onImportFile,
  className,
}) => {
  const [isValidationDialogOpen, setIsValidationDialogOpen] =
    React.useState(false);

  React.useEffect(() => {
    if (validateImportResult) {
      setIsValidationDialogOpen(true);
    }
  }, [validateImportResult]);

  const details = lastImportJob ? (
    <div className='space-y-3 text-xs'>
      <div className='rounded-md border p-2 text-muted-foreground'>
        <p className='font-medium text-foreground'>Latest import job</p>
        <p>
          #{lastImportJob.id} - {lastImportJob.file_name} (
          {lastImportJob.status})
        </p>
      </div>
    </div>
  ) : undefined;

  return (
    <>
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
        details={details}
      />
      <TmsImportValidationResultDialog
        open={isValidationDialogOpen}
        onOpenChange={setIsValidationDialogOpen}
        result={validateImportResult}
        entityLabel='order'
        isImporting={isImportingOrders}
        onConfirmImport={onImportFile}
      />
    </>
  );
};
