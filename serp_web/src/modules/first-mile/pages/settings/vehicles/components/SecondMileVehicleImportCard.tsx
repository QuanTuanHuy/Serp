/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Second-mile vehicle import card
 */

import React from 'react';
import {
  TmsExcelImportToolbar,
  type TmsImportPreviewColumn,
} from '../../../../components/list';
import type {
  ImportHistory,
  SecondMileVehicleImportItem,
  SecondMileVehicleStatus,
  SecondMileVehicleType,
  ValidateImportFileResponse,
} from '../../../../types';
import {
  formatOptionalNumber,
  formatStatusLabel,
  formatVehicleType,
} from '../secondMileVehiclePageModels';

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

const joinPreviewParts = (
  parts: Array<number | string | undefined>
): string => {
  return parts
    .map((part) => (part === undefined ? '' : String(part).trim()))
    .filter(Boolean)
    .join(' - ');
};

const readImportValue = (
  item: SecondMileVehicleImportItem,
  keys: string[]
): number | string | undefined => {
  const record = item as Record<string, number | string | undefined>;

  for (const key of keys) {
    const value = record[key];
    if (value !== undefined && value !== null && String(value).trim()) {
      return value;
    }
  }

  return undefined;
};

const readImportNumber = (
  item: SecondMileVehicleImportItem,
  keys: string[]
): number | undefined => {
  const value = readImportValue(item, keys);
  if (value === undefined) {
    return undefined;
  }

  const numericValue = Number(value);
  return Number.isFinite(numericValue) ? numericValue : undefined;
};

const isSecondMileVehicleType = (
  value: number | string | undefined
): value is SecondMileVehicleType => value === 'TRUCK' || value === 'VAN';

const isSecondMileVehicleStatus = (
  value: number | string | undefined
): value is SecondMileVehicleStatus =>
  value === 'ACTIVE' || value === 'INACTIVE' || value === 'MAINTENANCE';

const formatImportHub = (item: SecondMileVehicleImportItem): string => {
  const hubLabel = readImportValue(item, ['hub']);
  if (hubLabel) {
    return String(hubLabel);
  }

  const codeName = joinPreviewParts([
    readImportValue(item, ['hub_code', 'hubCode']),
    readImportValue(item, ['hub_name', 'hubName']),
  ]);
  if (codeName) {
    return codeName;
  }

  const hubId = readImportValue(item, ['hub_id', 'hubId']);
  return hubId ? `Hub #${hubId}` : '-';
};

const formatImportDriver = (item: SecondMileVehicleImportItem): string => {
  const driverLabel = readImportValue(item, ['driver']);
  if (driverLabel) {
    return String(driverLabel);
  }

  const codeName = joinPreviewParts([
    readImportValue(item, ['driver_code', 'driverCode', 'staff_code']),
    readImportValue(item, ['driver_name', 'driverName', 'staff_name']),
  ]);
  if (codeName) {
    return codeName;
  }

  const driverId = readImportValue(item, [
    'assigned_staff_id',
    'assignedStaffId',
    'driver_id',
    'driverId',
  ]);
  return driverId ? `Tài xế #${driverId}` : '-';
};

const SECOND_MILE_VEHICLE_IMPORT_COLUMNS: Array<
  TmsImportPreviewColumn<SecondMileVehicleImportItem>
> = [
  {
    key: 'license_plate',
    label: 'Biển số xe',
    render: (item) =>
      readImportValue(item, ['license_plate', 'licensePlate']) || '-',
  },
  {
    key: 'vehicle_type',
    label: 'Loại xe',
    render: (item) => {
      const vehicleType = readImportValue(item, [
        'vehicle_type',
        'vehicleType',
      ]);
      return isSecondMileVehicleType(vehicleType)
        ? formatVehicleType(vehicleType)
        : '-';
    },
  },
  {
    key: 'hub',
    label: 'Hub',
    render: formatImportHub,
  },
  {
    key: 'driver',
    label: 'Tài xế',
    render: formatImportDriver,
  },
  {
    key: 'max_bags',
    label: 'Số bao',
    render: (item) =>
      formatOptionalNumber(readImportNumber(item, ['max_bags', 'maxBags'])),
  },
  {
    key: 'max_weight',
    label: 'Tải trọng (kg)',
    render: (item) =>
      formatOptionalNumber(
        readImportNumber(item, ['max_weight', 'maxWeight'])
      ),
  },
  {
    key: 'max_volume',
    label: 'Thể tích (m3)',
    render: (item) =>
      formatOptionalNumber(
        readImportNumber(item, ['max_volume', 'maxVolume'])
      ),
  },
  {
    key: 'status',
    label: 'Trạng thái',
    render: (item) => {
      const status = readImportValue(item, ['status']);
      return isSecondMileVehicleStatus(status) ? formatStatusLabel(status) : '-';
    },
  },
];

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
  onDownloadTemplate,
  onSelectFile,
  onValidate,
  onImport,
}) => {
  return (
    <TmsExcelImportToolbar
      canImport={canManage}
      entityLabel='phương tiện'
      isBusy={isBusy}
      isExportingTemplate={isExporting}
      isValidating={isValidating}
      isImporting={isImporting}
      importFileInputKey={importFileInputKey}
      selectedFileName={selectedFile?.name}
      validateImportResult={validateResult}
      permissionHint='Cần quyền TMS_ADMIN để nhập Excel.'
      onDownloadTemplate={onDownloadTemplate}
      onSelectFile={onSelectFile}
      onValidate={onValidate}
      onConfirmImport={onImport}
      previewColumns={SECOND_MILE_VEHICLE_IMPORT_COLUMNS}
    />
  );
};
