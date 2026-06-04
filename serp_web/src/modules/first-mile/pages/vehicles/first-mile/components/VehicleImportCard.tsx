/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Vehicle import card
 */

import React from 'react';
import { TmsExcelImportToolbar } from '../../../../components/list';
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
  onDownloadTemplate,
  onSelectImportFile,
  onValidateFile,
  onImportFile,
}) => {
  return (
    <TmsExcelImportToolbar
      canImport={canManageVehicles}
      entityLabel='vehicles'
      isBusy={isImportFlowBusy}
      isExportingTemplate={isExportingTemplate}
      isValidating={isValidatingImport}
      isImporting={isImportingVehicles}
      importFileInputKey={importFileInputKey}
      selectedFileName={selectedImportFile?.name}
      validateImportResult={validateImportResult}
      permissionHint='Import actions require TMS_ADMIN or TMS_POSTOFFICER_MANAGER permission.'
      onDownloadTemplate={onDownloadTemplate}
      onSelectFile={onSelectImportFile}
      onValidate={onValidateFile}
      onConfirmImport={onImportFile}
    />
  );
};
