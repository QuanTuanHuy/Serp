/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Order Excel import toolbar
 */

import React from 'react';
import { TmsExcelImportToolbar } from '../../../../components/list';
import type {
  ImportHistory,
  OrderImportItem,
  ValidateImportFileResponse,
} from '../../../../types';

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
  onDownloadTemplate,
  onSelectImportFile,
  onValidateImportFile,
  onImportFile,
  className,
}) => {
  return (
    <TmsExcelImportToolbar
      className={className}
      canImport={canMutateOrders}
      entityLabel='đơn hàng'
      isBusy={isImportFlowBusy}
      isExportingTemplate={isExportingTemplate}
      isValidating={isValidatingImport}
      isImporting={isImportingOrders}
      importFileInputKey={importFileInputKey}
      selectedFileName={selectedImportFile?.name}
      validateImportResult={validateImportResult}
      permissionHint='Nhập Excel yêu cầu vai trò TMS_ADMIN hoặc TMS_CUSTOMER.'
      onDownloadTemplate={onDownloadTemplate}
      onSelectFile={onSelectImportFile}
      onValidate={onValidateImportFile}
      onConfirmImport={onImportFile}
    />
  );
};
