/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - First-mile vehicle management page
 */

'use client';

import React from 'react';
import { getErrorMessage, useAppSelector } from '@/lib/store';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Input,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import { ConfirmDialog } from '@/shared/components/ui/confirm-dialog';
import { useNotification } from '@/shared/hooks';
import {
  Download,
  Eye,
  FileUp,
  Loader2,
  Pencil,
  Plus,
  RefreshCw,
  Search,
  Shield,
  ShieldAlert,
  Trash2,
} from 'lucide-react';
import {
  useCreateVehicleMutation,
  useDeleteVehicleMutation,
  useGetVehicleByIdQuery,
  useGetVehiclesQuery,
  useImportVehiclesMutation,
  useLazyExportVehicleTemplateQuery,
  useUpdateVehicleMutation,
  useValidateVehicleImportMutation,
} from '../../api';
import type {
  CreateVehicleRequest,
  ImportHistory,
  ValidateImportFileResponse,
  Vehicle,
  VehicleImportItem,
  VehicleStatus,
  VehicleType,
} from '../../types';

const PAGE_SIZE = 20;
const IMPORT_PREVIEW_LIMIT = 5;

type VehicleAccessScope =
  | 'ADMIN_ALL'
  | 'MANAGER_POST_OFFICES'
  | 'COURIER_READ_ONLY'
  | 'NO_ACCESS';

type BadgeVariant = 'default' | 'secondary' | 'destructive' | 'outline';
type VehicleFormMode = 'create' | 'edit';

interface VehicleFormState {
  licensePlate: string;
  maxWeight: string;
  maxVolume: string;
  postOfficeId: string;
  postOfficeStaffId: string;
  status: VehicleStatus;
  vehicleType: VehicleType;
}

const VEHICLE_STATUS_OPTIONS: Array<{ value: VehicleStatus; label: string }> = [
  { value: 'ACTIVE', label: 'Active' },
  { value: 'IN_USE', label: 'In use' },
  { value: 'FULL', label: 'Full' },
  { value: 'MAINTENANCE', label: 'Maintenance' },
  { value: 'INACTIVE', label: 'Inactive' },
];

const VEHICLE_TYPE_OPTIONS: Array<{ value: VehicleType; label: string }> = [
  { value: 'BIKE', label: 'Bike' },
  { value: 'TRUCK', label: 'Truck' },
];

const DEFAULT_VEHICLE_FORM: VehicleFormState = {
  licensePlate: '',
  maxWeight: '',
  maxVolume: '',
  postOfficeId: '',
  postOfficeStaffId: '',
  status: 'ACTIVE',
  vehicleType: 'BIKE',
};

const resolveVehicleAccessScope = (roles: string[]): VehicleAccessScope => {
  if (roles.includes('TMS_ADMIN')) {
    return 'ADMIN_ALL';
  }

  if (roles.includes('TMS_POSTOFFICER_MANAGER')) {
    return 'MANAGER_POST_OFFICES';
  }

  if (roles.includes('TMS_POSTOFFICER')) {
    return 'COURIER_READ_ONLY';
  }

  return 'NO_ACCESS';
};

const getScopeBadgeLabel = (scope: VehicleAccessScope): string => {
  switch (scope) {
    case 'ADMIN_ALL':
      return 'TMS admin';
    case 'MANAGER_POST_OFFICES':
      return 'Post office manager';
    case 'COURIER_READ_ONLY':
      return 'Courier';
    default:
      return 'No access';
  }
};

const getScopeDescription = (scope: VehicleAccessScope): string => {
  switch (scope) {
    case 'ADMIN_ALL':
      return 'You can view all vehicles in the system.';
    case 'MANAGER_POST_OFFICES':
      return 'You can view vehicles belonging to the post offices you manage.';
    case 'COURIER_READ_ONLY':
      return 'You can view vehicles but cannot create, update, delete, or import.';
    default:
      return 'Your current account does not have permission to access the vehicle list.';
  }
};

const getStatusBadgeVariant = (status: VehicleStatus): BadgeVariant => {
  switch (status) {
    case 'ACTIVE':
      return 'default';
    case 'IN_USE':
    case 'FULL':
      return 'secondary';
    case 'MAINTENANCE':
      return 'outline';
    case 'INACTIVE':
      return 'destructive';
    default:
      return 'outline';
  }
};

const formatStatusLabel = (status: VehicleStatus): string =>
  status.replaceAll('_', ' ');

const formatVehicleType = (vehicleType: VehicleType): string =>
  vehicleType === 'TRUCK' ? 'Truck' : 'Bike';

const formatDateTime = (value?: string): string => {
  if (!value) {
    return '--';
  }

  const parsedDate = new Date(value);
  if (Number.isNaN(parsedDate.getTime())) {
    return value;
  }

  return parsedDate.toLocaleString('en-US');
};

const formatOptionalNumber = (value?: number): string => {
  if (value === undefined || value === null) {
    return '--';
  }

  return String(value);
};

const buildPostOfficeLabel = (vehicle: Vehicle): string => {
  if (!vehicle.postOfficeCode) {
    return 'Not assigned';
  }

  if (!vehicle.postOfficeName) {
    return vehicle.postOfficeCode;
  }

  return `${vehicle.postOfficeCode} - ${vehicle.postOfficeName}`;
};

const parseOptionalNumber = (value: string): number | undefined => {
  const trimmedValue = value.trim();
  if (!trimmedValue) {
    return undefined;
  }

  const parsedValue = Number(trimmedValue);
  if (!Number.isFinite(parsedValue)) {
    return undefined;
  }

  return parsedValue;
};

const parseOptionalPositiveInteger = (value: string): number | undefined => {
  const trimmedValue = value.trim();
  if (!trimmedValue) {
    return undefined;
  }

  const parsedValue = Number(trimmedValue);
  if (!Number.isInteger(parsedValue) || parsedValue <= 0) {
    return undefined;
  }

  return parsedValue;
};

const mapVehicleToFormState = (vehicle: Vehicle): VehicleFormState => {
  return {
    licensePlate: vehicle.licensePlate || '',
    maxWeight:
      vehicle.maxWeight === undefined || vehicle.maxWeight === null
        ? ''
        : String(vehicle.maxWeight),
    maxVolume:
      vehicle.maxVolume === undefined || vehicle.maxVolume === null
        ? ''
        : String(vehicle.maxVolume),
    postOfficeId:
      vehicle.postOfficeId === undefined || vehicle.postOfficeId === null
        ? ''
        : String(vehicle.postOfficeId),
    postOfficeStaffId:
      vehicle.postOfficeStaffId === undefined ||
      vehicle.postOfficeStaffId === null
        ? ''
        : String(vehicle.postOfficeStaffId),
    status: vehicle.status || 'ACTIVE',
    vehicleType: vehicle.vehicleType || 'BIKE',
  };
};

const validateVehicleForm = (values: VehicleFormState): string | null => {
  if (!values.licensePlate.trim()) {
    return 'License plate is required.';
  }

  if (values.maxWeight.trim()) {
    const parsedWeight = parseOptionalNumber(values.maxWeight);
    if (parsedWeight === undefined || parsedWeight <= 0) {
      return 'Max weight must be a number greater than 0.';
    }
  }

  if (values.maxVolume.trim()) {
    const parsedVolume = parseOptionalNumber(values.maxVolume);
    if (parsedVolume === undefined || parsedVolume <= 0) {
      return 'Max volume must be a number greater than 0.';
    }
  }

  if (
    values.postOfficeId.trim() &&
    parseOptionalPositiveInteger(values.postOfficeId) === undefined
  ) {
    return 'Post office ID must be a positive integer.';
  }

  if (
    values.postOfficeStaffId.trim() &&
    parseOptionalPositiveInteger(values.postOfficeStaffId) === undefined
  ) {
    return 'Courier staff ID must be a positive integer.';
  }

  return null;
};

const buildVehicleRequest = (
  values: VehicleFormState
): CreateVehicleRequest => {
  const maxWeight = parseOptionalNumber(values.maxWeight);
  const maxVolume = parseOptionalNumber(values.maxVolume);
  const postOfficeId = parseOptionalPositiveInteger(values.postOfficeId);
  const postOfficeStaffId = parseOptionalPositiveInteger(
    values.postOfficeStaffId
  );

  return {
    license_plate: values.licensePlate.trim(),
    status: values.status,
    vehicle_type: values.vehicleType,
    ...(maxWeight !== undefined ? { max_weight: maxWeight } : {}),
    ...(maxVolume !== undefined ? { max_volume: maxVolume } : {}),
    ...(postOfficeId !== undefined ? { post_office_id: postOfficeId } : {}),
    ...(postOfficeStaffId !== undefined
      ? { post_office_staff_id: postOfficeStaffId }
      : {}),
  };
};

export const VehicleListPage: React.FC = () => {
  const notification = useNotification();
  const profile = useAppSelector((state) => state.account.user.profile);
  const roles = profile?.roles ?? [];

  const [page, setPage] = React.useState(0);
  const [keywordInput, setKeywordInput] = React.useState('');
  const [keyword, setKeyword] = React.useState<string | undefined>(undefined);
  const [isFormDialogOpen, setIsFormDialogOpen] = React.useState(false);
  const [formMode, setFormMode] = React.useState<VehicleFormMode>('create');
  const [editingVehicleId, setEditingVehicleId] = React.useState<number | null>(
    null
  );
  const [formValues, setFormValues] =
    React.useState<VehicleFormState>(DEFAULT_VEHICLE_FORM);
  const [deleteTarget, setDeleteTarget] = React.useState<Vehicle | null>(null);
  const [isDetailDialogOpen, setIsDetailDialogOpen] = React.useState(false);
  const [selectedVehicleId, setSelectedVehicleId] = React.useState<
    number | null
  >(null);
  const [selectedImportFile, setSelectedImportFile] =
    React.useState<File | null>(null);
  const [importFileInputKey, setImportFileInputKey] = React.useState(0);
  const [validateImportResult, setValidateImportResult] =
    React.useState<ValidateImportFileResponse<VehicleImportItem> | null>(null);
  const [lastImportJob, setLastImportJob] =
    React.useState<ImportHistory | null>(null);

  const accessScope = React.useMemo(
    () => resolveVehicleAccessScope(roles),
    [roles]
  );

  const canViewVehicles = accessScope !== 'NO_ACCESS';
  const canManageVehicles =
    accessScope === 'ADMIN_ALL' || accessScope === 'MANAGER_POST_OFFICES';

  const { data, isLoading, isFetching, refetch } = useGetVehiclesQuery(
    {
      page,
      size: PAGE_SIZE,
      keyword,
    },
    {
      skip: !canViewVehicles,
    }
  );

  const { data: vehicleDetail, isFetching: isFetchingVehicleDetail } =
    useGetVehicleByIdQuery(selectedVehicleId ?? 0, {
      skip:
        !canViewVehicles || !isDetailDialogOpen || selectedVehicleId === null,
    });

  const [createVehicle, { isLoading: isCreating }] = useCreateVehicleMutation();
  const [updateVehicle, { isLoading: isUpdating }] = useUpdateVehicleMutation();
  const [deleteVehicle, { isLoading: isDeleting }] = useDeleteVehicleMutation();
  const [triggerExportVehicleTemplate, { isFetching: isExportingTemplate }] =
    useLazyExportVehicleTemplateQuery();
  const [validateVehicleImport, { isLoading: isValidatingImport }] =
    useValidateVehicleImportMutation();
  const [importVehicleFile, { isLoading: isImportingVehicles }] =
    useImportVehiclesMutation();

  const isSaving = isCreating || isUpdating;
  const isImportFlowBusy =
    isExportingTemplate || isValidatingImport || isImportingVehicles;

  const validatedPreviewItems = React.useMemo(
    () => validateImportResult?.data?.slice(0, IMPORT_PREVIEW_LIMIT) ?? [],
    [validateImportResult]
  );

  const updateFormField = React.useCallback(
    <K extends keyof VehicleFormState>(
      field: K,
      value: VehicleFormState[K]
    ) => {
      setFormValues((prev) => ({
        ...prev,
        [field]: value,
      }));
    },
    []
  );

  const handleSearch = (event: React.FormEvent) => {
    event.preventDefault();
    setPage(0);
    setKeyword(keywordInput.trim() || undefined);
  };

  const handleOpenDetails = (vehicleId: number) => {
    if (!canViewVehicles) {
      notification.error('You do not have permission to access vehicle data.');
      return;
    }

    setSelectedVehicleId(vehicleId);
    setIsDetailDialogOpen(true);
  };

  const handleDetailDialogOpenChange = (open: boolean) => {
    setIsDetailDialogOpen(open);
    if (!open) {
      setSelectedVehicleId(null);
    }
  };

  const handleOpenCreateDialog = () => {
    if (!canManageVehicles) {
      notification.error(
        'Only TMS_ADMIN or TMS_POSTOFFICER_MANAGER can create vehicles.'
      );
      return;
    }

    setFormMode('create');
    setEditingVehicleId(null);
    setFormValues(DEFAULT_VEHICLE_FORM);
    setIsFormDialogOpen(true);
  };

  const handleOpenEditDialog = (vehicle: Vehicle) => {
    if (!canManageVehicles) {
      notification.error(
        'Only TMS_ADMIN or TMS_POSTOFFICER_MANAGER can update vehicles.'
      );
      return;
    }

    setFormMode('edit');
    setEditingVehicleId(vehicle.id);
    setFormValues(mapVehicleToFormState(vehicle));
    setIsFormDialogOpen(true);
  };

  const handleEditFromDetails = () => {
    if (!vehicleDetail) {
      return;
    }

    handleOpenEditDialog(vehicleDetail);
    setIsDetailDialogOpen(false);
    setSelectedVehicleId(null);
  };

  const handleFormDialogOpenChange = (open: boolean) => {
    if (open) {
      setIsFormDialogOpen(true);
      return;
    }

    if (isSaving) {
      return;
    }

    setIsFormDialogOpen(false);
    setEditingVehicleId(null);
  };

  const handleSubmitForm = async (event: React.FormEvent) => {
    event.preventDefault();

    if (!canManageVehicles) {
      notification.error(
        'Only TMS_ADMIN or TMS_POSTOFFICER_MANAGER can modify vehicles.'
      );
      return;
    }

    const validationError = validateVehicleForm(formValues);
    if (validationError) {
      notification.error(validationError);
      return;
    }

    if (
      accessScope === 'MANAGER_POST_OFFICES' &&
      !formValues.postOfficeId.trim() &&
      !formValues.postOfficeStaffId.trim()
    ) {
      notification.error(
        'For manager role, either post office ID or courier staff ID is required.'
      );
      return;
    }

    const payload = buildVehicleRequest(formValues);

    try {
      if (formMode === 'create') {
        await createVehicle(payload).unwrap();
        notification.success('Vehicle created successfully.');

        if (page !== 0) {
          setPage(0);
        } else {
          void refetch();
        }
      } else {
        if (editingVehicleId === null) {
          notification.error('Missing vehicle id for update.');
          return;
        }

        await updateVehicle({
          id: editingVehicleId,
          body: payload,
        }).unwrap();

        notification.success('Vehicle updated successfully.');
        void refetch();
      }

      setIsFormDialogOpen(false);
      setEditingVehicleId(null);
    } catch (error) {
      notification.error('Failed to save vehicle.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleRequestDelete = (vehicle: Vehicle) => {
    if (!canManageVehicles) {
      notification.error(
        'Only TMS_ADMIN or TMS_POSTOFFICER_MANAGER can delete vehicles.'
      );
      return;
    }

    setDeleteTarget(vehicle);
  };

  const handleDeleteVehicle = async () => {
    if (!deleteTarget) {
      return;
    }

    if (!canManageVehicles) {
      notification.error(
        'Only TMS_ADMIN or TMS_POSTOFFICER_MANAGER can delete vehicles.'
      );
      return;
    }

    try {
      await deleteVehicle(deleteTarget.id).unwrap();
      notification.success('Vehicle deleted successfully.');
      setDeleteTarget(null);

      if ((data?.items.length ?? 0) === 1 && page > 0) {
        setPage((prev) => Math.max(prev - 1, 0));
      } else {
        void refetch();
      }
    } catch (error) {
      notification.error('Failed to delete vehicle.', {
        description: getErrorMessage(error),
      });
    }
  };

  const resetImportFileSelection = React.useCallback(() => {
    setSelectedImportFile(null);
    setValidateImportResult(null);
    setImportFileInputKey((prev) => prev + 1);
  }, []);

  const handleSelectImportFile = (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    const file = event.target.files?.[0] ?? null;
    setSelectedImportFile(file);
    setValidateImportResult(null);
    setLastImportJob(null);
  };

  const buildImportFormData = () => {
    if (!selectedImportFile) {
      return null;
    }

    const formData = new FormData();
    formData.append('file', selectedImportFile);
    return formData;
  };

  const handleDownloadTemplate = async () => {
    if (!canManageVehicles) {
      notification.error(
        'Only TMS_ADMIN or TMS_POSTOFFICER_MANAGER can download vehicle templates.'
      );
      return;
    }

    try {
      const blob = await triggerExportVehicleTemplate(undefined).unwrap();
      const downloadUrl = URL.createObjectURL(blob);
      const link = document.createElement('a');

      link.href = downloadUrl;
      link.download = 'vehicle_template.xlsx';
      document.body.appendChild(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(downloadUrl);

      notification.success('Vehicle template downloaded successfully.');
    } catch (error) {
      notification.error('Failed to download vehicle template.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleValidateImportFile = async () => {
    if (!canManageVehicles) {
      notification.error(
        'Only TMS_ADMIN or TMS_POSTOFFICER_MANAGER can validate vehicle imports.'
      );
      return;
    }

    const formData = buildImportFormData();
    if (!formData) {
      notification.error('Please select an Excel file first.');
      return;
    }

    try {
      const result = await validateVehicleImport(formData).unwrap();
      setValidateImportResult(result);

      if (result.is_success) {
        notification.success('File validated successfully.', {
          description: `${result.data.length} row(s) are ready to import.`,
        });
      } else {
        notification.error('Validation completed with errors.', {
          description:
            result.error_message ||
            'Please fix the Excel data before importing.',
        });
      }
    } catch (error) {
      notification.error('Failed to validate vehicle import file.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleImportFile = async () => {
    if (!canManageVehicles) {
      notification.error(
        'Only TMS_ADMIN or TMS_POSTOFFICER_MANAGER can import vehicles.'
      );
      return;
    }

    if (!validateImportResult) {
      notification.error('Please validate the selected file before importing.');
      return;
    }

    if (!validateImportResult.is_success) {
      notification.error(
        'Validation has errors. Please fix them before import.'
      );
      return;
    }

    const formData = buildImportFormData();
    if (!formData) {
      notification.error('Please select an Excel file first.');
      return;
    }

    try {
      const importResult = await importVehicleFile(formData).unwrap();
      setLastImportJob(importResult);
      resetImportFileSelection();

      notification.success('Vehicle import job created.', {
        description: `Job #${importResult.id} is ${importResult.status}.`,
      });

      void refetch();
    } catch (error) {
      notification.error('Failed to import vehicle file.', {
        description: getErrorMessage(error),
      });
    }
  };

  return (
    <>
      <div className='space-y-6'>
        <div className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between'>
          <div className='flex flex-col gap-2'>
            <h1 className='text-2xl font-bold tracking-tight'>Vehicles</h1>
            <p className='text-muted-foreground'>
              Track, inspect, and manage first-mile vehicles based on backend
              permissions.
            </p>
          </div>

          {canManageVehicles ? (
            <Button onClick={handleOpenCreateDialog}>
              <Plus className='h-4 w-4 mr-2' />
              New Vehicle
            </Button>
          ) : (
            <Badge variant='outline' className='gap-1'>
              <ShieldAlert className='h-3.5 w-3.5' />
              View only (write actions require admin or manager role)
            </Badge>
          )}
        </div>

        <Card>
          <CardHeader>
            <CardTitle className='flex items-center gap-2'>
              <Shield className='h-5 w-5' />
              Access Scope
            </CardTitle>
            <CardDescription>
              Vehicle visibility and actions are aligned with backend role
              authorization.
            </CardDescription>
          </CardHeader>
          <CardContent className='space-y-2'>
            <Badge
              variant={canViewVehicles ? 'secondary' : 'destructive'}
              className='w-fit'
            >
              {getScopeBadgeLabel(accessScope)}
            </Badge>
            <p className='text-sm text-muted-foreground'>
              {getScopeDescription(accessScope)}
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Search</CardTitle>
            <CardDescription>
              Search vehicles by license plate inside your permitted data scope.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form
              onSubmit={handleSearch}
              className='flex flex-col gap-2 md:flex-row'
            >
              <div className='relative flex-1'>
                <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
                <Input
                  className='pl-10'
                  value={keywordInput}
                  onChange={(event) => setKeywordInput(event.target.value)}
                  placeholder='Search vehicle by license plate...'
                  disabled={!canViewVehicles}
                />
              </div>
              <Button type='submit' disabled={!canViewVehicles}>
                Apply
              </Button>
              <Button
                type='button'
                variant='outline'
                onClick={() => refetch()}
                disabled={!canViewVehicles || isFetching}
              >
                <RefreshCw className='mr-2 h-4 w-4' />
                Refresh
              </Button>
            </form>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Excel Import</CardTitle>
            <CardDescription>
              Download template, validate the completed file, then import
              vehicles.
            </CardDescription>
          </CardHeader>
          <CardContent className='space-y-4'>
            <div className='flex flex-col gap-2 lg:flex-row lg:items-center'>
              <Button
                type='button'
                variant='outline'
                onClick={handleDownloadTemplate}
                disabled={!canManageVehicles || isImportFlowBusy}
              >
                {isExportingTemplate ? (
                  <Loader2 className='h-4 w-4 mr-2 animate-spin' />
                ) : (
                  <Download className='h-4 w-4 mr-2' />
                )}
                Download template
              </Button>

              <Input
                key={importFileInputKey}
                type='file'
                accept='.xlsx,.xls'
                onChange={handleSelectImportFile}
                disabled={!canManageVehicles || isImportFlowBusy}
                className='lg:max-w-sm'
              />

              <Button
                type='button'
                variant='outline'
                onClick={handleValidateImportFile}
                disabled={
                  !canManageVehicles || !selectedImportFile || isImportFlowBusy
                }
              >
                {isValidatingImport && (
                  <Loader2 className='h-4 w-4 mr-2 animate-spin' />
                )}
                Validate file
              </Button>

              <Button
                type='button'
                onClick={handleImportFile}
                disabled={
                  !canManageVehicles ||
                  !selectedImportFile ||
                  !validateImportResult?.is_success ||
                  isImportFlowBusy
                }
              >
                {isImportingVehicles ? (
                  <Loader2 className='h-4 w-4 mr-2 animate-spin' />
                ) : (
                  <FileUp className='h-4 w-4 mr-2' />
                )}
                Import file
              </Button>
            </div>

            {!canManageVehicles && (
              <p className='text-xs text-muted-foreground'>
                Import actions require TMS_ADMIN or TMS_POSTOFFICER_MANAGER
                permission.
              </p>
            )}

            {selectedImportFile && (
              <p className='text-sm text-muted-foreground'>
                Selected file: {selectedImportFile.name}
              </p>
            )}

            {validateImportResult && (
              <div className='rounded-lg border p-3 space-y-3'>
                <div className='flex flex-wrap items-center gap-x-4 gap-y-1'>
                  <p className='text-sm font-medium'>
                    Validation:{' '}
                    {validateImportResult.is_success ? 'Success' : 'Failed'}
                  </p>
                  <p className='text-xs text-muted-foreground'>
                    File ID: {validateImportResult.file_id}
                  </p>
                  <p className='text-xs text-muted-foreground'>
                    Parsed rows: {validateImportResult.data.length}
                  </p>
                </div>

                {validateImportResult.error_message && (
                  <pre className='whitespace-pre-wrap rounded-md bg-muted p-2 text-xs text-destructive'>
                    {validateImportResult.error_message}
                  </pre>
                )}

                {validatedPreviewItems.length > 0 && (
                  <div className='space-y-2'>
                    <p className='text-xs text-muted-foreground'>
                      Preview {validatedPreviewItems.length}/
                      {validateImportResult.data.length} validated row(s)
                    </p>

                    <div className='grid gap-2 sm:grid-cols-2'>
                      {validatedPreviewItems.map((item, index) => (
                        <div
                          key={`${item.license_plate || 'vehicle'}-${index}`}
                          className='rounded-md border p-2 text-xs space-y-1'
                        >
                          <p className='font-medium'>
                            {(item.license_plate || '-').trim()} |{' '}
                            {item.vehicle_type || '-'} | {item.status || '-'}
                          </p>
                          <p className='text-muted-foreground'>
                            Post office:{' '}
                            {item.post_office_code
                              ? `${item.post_office_code}${item.post_office_name ? ` - ${item.post_office_name}` : ''}`
                              : '-'}
                          </p>
                          <p className='text-muted-foreground'>
                            Courier:{' '}
                            {item.post_office_staff_code
                              ? `${item.post_office_staff_code}${item.post_office_staff_name ? ` - ${item.post_office_staff_name}` : ''}`
                              : '-'}
                          </p>
                          <p className='text-muted-foreground'>
                            Source rows:{' '}
                            {item.source_rows?.length
                              ? item.source_rows.join(', ')
                              : '-'}
                          </p>
                        </div>
                      ))}
                    </div>

                    {validateImportResult.data.length >
                      IMPORT_PREVIEW_LIMIT && (
                      <p className='text-xs text-muted-foreground'>
                        Showing only the first {IMPORT_PREVIEW_LIMIT} row(s).
                      </p>
                    )}
                  </div>
                )}
              </div>
            )}

            {lastImportJob && (
              <div className='rounded-lg border p-3 space-y-1'>
                <p className='text-sm font-medium'>Latest import job</p>
                <p className='text-xs text-muted-foreground'>
                  #{lastImportJob.id} - {lastImportJob.file_name}
                </p>
                <p className='text-xs text-muted-foreground'>
                  Status: {lastImportJob.status} | Success/Failed:{' '}
                  {lastImportJob.success_records}/{lastImportJob.failed_records}
                </p>
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Results ({data?.totalItems ?? 0})</CardTitle>
          </CardHeader>
          <CardContent>
            {!canViewVehicles ? (
              <p className='text-muted-foreground'>
                You do not have permission to access vehicle data.
              </p>
            ) : isLoading ? (
              <div className='flex items-center gap-2 text-muted-foreground'>
                <Loader2 className='h-4 w-4 animate-spin' />
                Loading vehicles...
              </div>
            ) : data && data.items.length > 0 ? (
              <div className='space-y-3'>
                {data.items.map((vehicle) => (
                  <div
                    key={vehicle.id}
                    className='rounded-lg border p-3 flex flex-col gap-2 md:flex-row md:items-start md:justify-between'
                  >
                    <div className='space-y-1'>
                      <p className='font-medium'>{vehicle.licensePlate}</p>
                      <p className='text-xs text-muted-foreground'>
                        Type: {formatVehicleType(vehicle.vehicleType)}
                      </p>
                      <p className='text-xs text-muted-foreground'>
                        Post office: {buildPostOfficeLabel(vehicle)}
                      </p>
                      <p className='text-xs text-muted-foreground'>
                        Courier staff ID:{' '}
                        {vehicle.postOfficeStaffId ?? 'Not assigned'}
                      </p>
                      <p className='text-xs text-muted-foreground'>
                        Capacity: {formatOptionalNumber(vehicle.maxWeight)} kg |{' '}
                        {formatOptionalNumber(vehicle.maxVolume)} m3
                      </p>
                    </div>

                    <div className='flex items-center gap-2 flex-wrap md:justify-end'>
                      <Badge variant='outline'>
                        {formatVehicleType(vehicle.vehicleType)}
                      </Badge>
                      <Badge variant={getStatusBadgeVariant(vehicle.status)}>
                        {formatStatusLabel(vehicle.status)}
                      </Badge>
                      <Button
                        type='button'
                        variant='outline'
                        size='sm'
                        onClick={() => handleOpenDetails(vehicle.id)}
                      >
                        <Eye className='h-4 w-4 mr-1' />
                        Details
                      </Button>
                      {canManageVehicles && (
                        <>
                          <Button
                            type='button'
                            variant='outline'
                            size='sm'
                            onClick={() => handleOpenEditDialog(vehicle)}
                            disabled={isSaving || isDeleting}
                          >
                            <Pencil className='h-4 w-4 mr-1' />
                            Edit
                          </Button>
                          <Button
                            type='button'
                            variant='destructive'
                            size='sm'
                            onClick={() => handleRequestDelete(vehicle)}
                            disabled={isSaving || isDeleting}
                          >
                            <Trash2 className='h-4 w-4 mr-1' />
                            Delete
                          </Button>
                        </>
                      )}
                    </div>
                  </div>
                ))}

                <div className='flex items-center justify-between pt-2'>
                  <Button
                    variant='outline'
                    onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
                    disabled={!data.hasPrevious || isFetching}
                  >
                    Previous
                  </Button>
                  <span className='text-sm text-muted-foreground'>
                    Page {data.currentPage + 1} / {Math.max(data.totalPages, 1)}
                  </span>
                  <Button
                    variant='outline'
                    onClick={() => setPage((prev) => prev + 1)}
                    disabled={!data.hasNext || isFetching}
                  >
                    Next
                  </Button>
                </div>
              </div>
            ) : (
              <p className='text-muted-foreground'>No vehicles found.</p>
            )}
          </CardContent>
        </Card>
      </div>

      <Dialog
        open={isDetailDialogOpen}
        onOpenChange={handleDetailDialogOpenChange}
      >
        <DialogContent className='sm:max-w-2xl'>
          <DialogHeader>
            <DialogTitle>Vehicle details</DialogTitle>
            <DialogDescription>
              Review complete vehicle information and audit fields.
            </DialogDescription>
          </DialogHeader>

          {isFetchingVehicleDetail ? (
            <div className='flex items-center gap-2 text-muted-foreground'>
              <Loader2 className='h-4 w-4 animate-spin' />
              Loading vehicle details...
            </div>
          ) : vehicleDetail ? (
            <div className='grid gap-3 sm:grid-cols-2 text-sm'>
              <div>
                <p className='text-muted-foreground'>Vehicle ID</p>
                <p className='font-medium'>{vehicleDetail.id}</p>
              </div>
              <div>
                <p className='text-muted-foreground'>License plate</p>
                <p className='font-medium'>{vehicleDetail.licensePlate}</p>
              </div>
              <div>
                <p className='text-muted-foreground'>Status</p>
                <p className='font-medium'>
                  {formatStatusLabel(vehicleDetail.status)}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Vehicle type</p>
                <p className='font-medium'>
                  {formatVehicleType(vehicleDetail.vehicleType)}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Max weight (kg)</p>
                <p className='font-medium'>
                  {formatOptionalNumber(vehicleDetail.maxWeight)}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Max volume (m3)</p>
                <p className='font-medium'>
                  {formatOptionalNumber(vehicleDetail.maxVolume)}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Post office</p>
                <p className='font-medium'>
                  {buildPostOfficeLabel(vehicleDetail)}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Courier staff ID</p>
                <p className='font-medium'>
                  {vehicleDetail.postOfficeStaffId ?? 'Not assigned'}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Created by</p>
                <p className='font-medium'>{vehicleDetail.createdBy || '--'}</p>
              </div>
              <div>
                <p className='text-muted-foreground'>Updated by</p>
                <p className='font-medium'>{vehicleDetail.updatedBy || '--'}</p>
              </div>
              <div>
                <p className='text-muted-foreground'>Created at</p>
                <p className='font-medium'>
                  {formatDateTime(vehicleDetail.createdAt)}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Updated at</p>
                <p className='font-medium'>
                  {formatDateTime(vehicleDetail.updatedAt)}
                </p>
              </div>
            </div>
          ) : (
            <p className='text-sm text-muted-foreground'>
              Vehicle details are unavailable.
            </p>
          )}

          <DialogFooter>
            <Button
              type='button'
              variant='outline'
              onClick={() => handleDetailDialogOpenChange(false)}
            >
              Close
            </Button>
            {canManageVehicles && vehicleDetail && (
              <Button type='button' onClick={handleEditFromDetails}>
                <Pencil className='h-4 w-4 mr-2' />
                Edit vehicle
              </Button>
            )}
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={isFormDialogOpen} onOpenChange={handleFormDialogOpenChange}>
        <DialogContent className='sm:max-w-2xl'>
          <DialogHeader>
            <DialogTitle>
              {formMode === 'create' ? 'Create vehicle' : 'Update vehicle'}
            </DialogTitle>
            <DialogDescription>
              Fill in required fields to{' '}
              {formMode === 'create' ? 'create a new' : 'update the'} vehicle.
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleSubmitForm} className='space-y-4'>
            <div className='grid gap-4 sm:grid-cols-2'>
              <div className='space-y-2'>
                <Label htmlFor='vehicle-license-plate'>License plate *</Label>
                <Input
                  id='vehicle-license-plate'
                  value={formValues.licensePlate}
                  onChange={(event) =>
                    updateFormField('licensePlate', event.target.value)
                  }
                  disabled={isSaving}
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='vehicle-status'>Status *</Label>
                <Select
                  value={formValues.status}
                  onValueChange={(value) =>
                    updateFormField('status', value as VehicleStatus)
                  }
                  disabled={isSaving}
                >
                  <SelectTrigger id='vehicle-status' className='w-full'>
                    <SelectValue placeholder='Select status' />
                  </SelectTrigger>
                  <SelectContent>
                    {VEHICLE_STATUS_OPTIONS.map((option) => (
                      <SelectItem key={option.value} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='vehicle-type'>Vehicle type *</Label>
                <Select
                  value={formValues.vehicleType}
                  onValueChange={(value) =>
                    updateFormField('vehicleType', value as VehicleType)
                  }
                  disabled={isSaving}
                >
                  <SelectTrigger id='vehicle-type' className='w-full'>
                    <SelectValue placeholder='Select type' />
                  </SelectTrigger>
                  <SelectContent>
                    {VEHICLE_TYPE_OPTIONS.map((option) => (
                      <SelectItem key={option.value} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='vehicle-max-weight'>Max weight (kg)</Label>
                <Input
                  id='vehicle-max-weight'
                  type='number'
                  min={0}
                  step='any'
                  value={formValues.maxWeight}
                  onChange={(event) =>
                    updateFormField('maxWeight', event.target.value)
                  }
                  disabled={isSaving}
                  placeholder='e.g. 150'
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='vehicle-max-volume'>Max volume (m3)</Label>
                <Input
                  id='vehicle-max-volume'
                  type='number'
                  min={0}
                  step='any'
                  value={formValues.maxVolume}
                  onChange={(event) =>
                    updateFormField('maxVolume', event.target.value)
                  }
                  disabled={isSaving}
                  placeholder='e.g. 3.2'
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='vehicle-post-office-id'>Post office ID</Label>
                <Input
                  id='vehicle-post-office-id'
                  type='number'
                  min={1}
                  step={1}
                  value={formValues.postOfficeId}
                  onChange={(event) =>
                    updateFormField('postOfficeId', event.target.value)
                  }
                  disabled={isSaving}
                  placeholder='e.g. 12'
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='vehicle-post-office-staff-id'>
                  Courier staff ID
                </Label>
                <Input
                  id='vehicle-post-office-staff-id'
                  type='number'
                  min={1}
                  step={1}
                  value={formValues.postOfficeStaffId}
                  onChange={(event) =>
                    updateFormField('postOfficeStaffId', event.target.value)
                  }
                  disabled={isSaving}
                  placeholder='e.g. 108'
                />
              </div>
            </div>

            <p className='text-xs text-muted-foreground'>
              For manager role, backend only accepts post office and courier IDs
              within your managed scope.
            </p>

            <DialogFooter>
              <Button
                type='button'
                variant='outline'
                onClick={() => handleFormDialogOpenChange(false)}
                disabled={isSaving}
              >
                Cancel
              </Button>
              <Button type='submit' disabled={isSaving}>
                {isSaving && <Loader2 className='h-4 w-4 mr-2 animate-spin' />}
                {formMode === 'create' ? 'Create' : 'Save changes'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      <ConfirmDialog
        open={Boolean(deleteTarget)}
        onOpenChange={(open) => {
          if (!open && !isDeleting) {
            setDeleteTarget(null);
          }
        }}
        title='Delete vehicle'
        description={
          deleteTarget
            ? `This will permanently delete vehicle ${deleteTarget.licensePlate}.`
            : 'This action cannot be undone.'
        }
        confirmText='Delete'
        cancelText='Cancel'
        onConfirm={handleDeleteVehicle}
        isLoading={isDeleting}
        variant='destructive'
      />
    </>
  );
};
