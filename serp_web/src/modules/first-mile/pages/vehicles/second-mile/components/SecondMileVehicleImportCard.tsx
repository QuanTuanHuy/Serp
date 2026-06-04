/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Second-mile vehicle import card
 */

import React from 'react';
import { TmsExcelImportToolbar } from '../../../../components/list';
import type {
  ImportHistory,
  SecondMileVehicleImportItem,
  ValidateImportFileResponse,
} from '../../../../types';

interface SecondMileVehicleImportCardProps {
  canManage: boolean;
  isBusy: boolean;
  isExporting: boolean;
  isValidating: boolean;
  isImporting: boolean;
  importFileInputKey: number;
  selectedFile: File | null;
  validateResult: ValidateImportFileResponse<SecondMileVehicleImportItem> | null;
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
  lastImportJob,
  onDownloadTemplate,
  onSelectFile,
  onValidate,
  onImport,
}) => {
  const details = lastImportJob ? (
    <div className='rounded-md border p-2 text-xs'>
      <p className='font-medium text-foreground'>
        Latest import job #{lastImportJob.id}
      </p>
      <p className='text-muted-foreground'>
        {lastImportJob.status} - success {lastImportJob.success_records} /
        failed {lastImportJob.failed_records}
      </p>
    </div>
  ) : undefined;

  return (
    <TmsExcelImportToolbar
      canImport={canManage}
      entityLabel='vehicles'
      isBusy={isBusy}
      isExportingTemplate={isExporting}
      isValidating={isValidating}
      isImporting={isImporting}
      importFileInputKey={importFileInputKey}
      selectedFileName={selectedFile?.name}
      validateImportResult={validateResult}
      permissionHint='Import requires TMS_ADMIN permission.'
      onDownloadTemplate={onDownloadTemplate}
      onSelectFile={onSelectFile}
      onValidate={onValidate}
      onConfirmImport={onImport}
      details={details}
    />
  );
};
