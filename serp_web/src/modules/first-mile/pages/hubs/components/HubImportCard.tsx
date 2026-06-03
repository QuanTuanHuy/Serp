/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Hub Excel import toolbar
 */

import React from 'react';
import {
  TmsExcelImportToolbar,
  TmsImportValidationResultDialog,
} from '../../../components/list';
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
  lastImportJob,
  onSelectImportFile,
  onDownloadTemplate,
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
        canImport={isTmsAdmin}
        entityLabel='hubs'
        isBusy={isImportFlowBusy}
        isExportingTemplate={isExportingTemplate}
        isValidating={isValidatingImport}
        isImporting={isImportingHubs}
        importFileInputKey={importFileInputKey}
        selectedFileName={selectedImportFile?.name}
        permissionHint='Import requires TMS_ADMIN.'
        onDownloadTemplate={() => {
          void onDownloadTemplate();
        }}
        onSelectFile={onSelectImportFile}
        onValidate={() => {
          void onValidateImportFile();
        }}
        details={details}
      />
      <TmsImportValidationResultDialog
        open={isValidationDialogOpen}
        onOpenChange={setIsValidationDialogOpen}
        result={validateImportResult}
        entityLabel='hub'
        isImporting={isImportingHubs}
        onConfirmImport={() => {
          void onImportFile();
        }}
      />
    </>
  );
};
