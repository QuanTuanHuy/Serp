/**
 * Author: Nguyễn Thế Anh
 * Description: Part of Serp Project - Post office Excel import toolbar
 */

import React from 'react';
import { TmsExcelImportToolbar } from '../../../components/list';
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
  lastImportJob: ImportHistory | null;
  onSelectImportFile: (event: React.ChangeEvent<HTMLInputElement>) => void;
  onDownloadTemplate: () => Promise<void>;
  onValidateImportFile: () => Promise<void>;
  onImportFile: () => Promise<void>;
  className?: string;
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
  lastImportJob,
  onSelectImportFile,
  onDownloadTemplate,
  onValidateImportFile,
  onImportFile,
  className,
}) => {
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
    <TmsExcelImportToolbar
      className={className}
      canImport={isTmsAdmin}
      entityLabel='post offices'
      isBusy={isImportFlowBusy}
      isExportingTemplate={isExportingTemplate}
      isValidating={isValidatingImport}
      isImporting={isImportingPostOffices}
      importFileInputKey={importFileInputKey}
      selectedFileName={selectedImportFile?.name}
      validateImportResult={validateImportResult}
      permissionHint='Import requires TMS_ADMIN.'
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
      details={details}
    />
  );
};
