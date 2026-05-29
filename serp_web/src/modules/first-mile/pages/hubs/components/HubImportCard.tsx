/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Hub Excel import toolbar
 */

import React from 'react';
import { TmsExcelImportToolbar } from '../../../components/list';
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
  validatedPreviewItems: HubImportItem[];
  lastImportJob: ImportHistory | null;
  previewLimit: number;
  getProvinceLabel: (provinceCode?: string) => string;
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
  validatedPreviewItems,
  lastImportJob,
  previewLimit,
  getProvinceLabel,
  onSelectImportFile,
  onDownloadTemplate,
  onValidateImportFile,
  onImportFile,
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
                    key={`${item.code || 'hub'}-${index}`}
                    className='rounded-md border p-2 text-muted-foreground'
                  >
                    <p className='font-medium text-foreground'>
                      {item.code || '-'} | {item.name || '-'}
                    </p>
                    <p>{getProvinceLabel(item.province_code)}</p>
                  </div>
                ))}
              </div>
            ) : null}
            {validateImportResult.data.length > previewLimit ? (
              <p>Showing first {previewLimit} rows.</p>
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
      onImport={() => {
        void onImportFile();
      }}
      canImportFile={Boolean(validateImportResult?.is_success)}
      details={details}
    />
  );
};
