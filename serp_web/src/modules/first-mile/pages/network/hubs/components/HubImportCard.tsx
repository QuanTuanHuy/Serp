/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Hub Excel import toolbar
 */

import React from 'react';
import { TmsExcelImportToolbar } from '../../../../components/list';
import type {
  HubImportItem,
  ImportHistory,
  ValidateImportFileResponse,
} from '../../../../types';

interface HubImportCardProps {
  isTmsAdmin: boolean;
  isImportFlowBusy: boolean;
  isExportingTemplate: boolean;
  isValidatingImport: boolean;
  isImportingHubs: boolean;
  importFileInputKey: number;
  selectedImportFile: File | null;
  validateImportResult: ValidateImportFileResponse<HubImportItem> | null;
  lastImportJob: ImportHistory | null;
  onSelectImportFile: (event: React.ChangeEvent<HTMLInputElement>) => void;
  onDownloadTemplate: () => Promise<void>;
  onValidateImportFile: () => Promise<void>;
  onImportFile: () => Promise<void>;
  className?: string;
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
  onSelectImportFile,
  onDownloadTemplate,
  onValidateImportFile,
  onImportFile,
  className,
}) => {
  return (
    <TmsExcelImportToolbar
      className={className}
      canImport={isTmsAdmin}
      entityLabel='hub'
      isBusy={isImportFlowBusy}
      isExportingTemplate={isExportingTemplate}
      isValidating={isValidatingImport}
      isImporting={isImportingHubs}
      importFileInputKey={importFileInputKey}
      selectedFileName={selectedImportFile?.name}
      validateImportResult={validateImportResult}
      permissionHint='Nhập dữ liệu cần quyền TMS_ADMIN.'
      onDownloadTemplate={() => {
        void onDownloadTemplate();
      }}
      onSelectFile={onSelectImportFile}
      onValidate={() => {
        void onValidateImportFile();
      }}
      onConfirmImport={() => {
        void onImportFile();
      }}
    />
  );
};
