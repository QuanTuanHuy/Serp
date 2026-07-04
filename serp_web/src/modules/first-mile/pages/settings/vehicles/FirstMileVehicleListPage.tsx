/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - First-mile vehicle management page
 */

'use client';

import React from 'react';
import { getErrorMessage, useAppDispatch, useAppSelector } from '@/lib/store';
import { ConfirmDialog } from '@/shared/components/ui/confirm-dialog';
import { useNotification } from '@/shared/hooks';
import {
  firstMileApi,
  useCreateVehicleMutation,
  useDeleteVehicleMutation,
  useGetActiveCouriersByPostOfficeQuery,
  useGetPostOfficesQuery,
  useGetVehicleByIdQuery,
  useGetVehiclesQuery,
  useImportVehiclesMutation,
  useLazyExportVehicleTemplateQuery,
  useUpdateVehicleMutation,
  useValidateVehicleImportMutation,
} from '../../../api';
import type {
  ImportHistory,
  PostOfficeStaff,
  ValidateImportFileResponse,
  Vehicle,
  VehicleImportItem,
} from '../../../types';
import {
  VehicleDetailDialog,
  VehicleFormDialog,
  VehicleImportCard,
  VehiclePageHeader,
  VehicleResultsCard,
  VehicleSearchCard,
} from './components';
import {
  buildVehicleRequest,
  DEFAULT_VEHICLE_FORM,
  mapVehicleToFormState,
  PAGE_SIZE,
  parseOptionalPositiveInteger,
  resolveVehicleAccessScope,
  validateVehicleForm,
  type VehicleFormMode,
  type VehicleFormState,
} from './firstMileVehiclePageModels';

interface FirstMileVehicleListPageProps {
  title?: string;
  description?: string;
}

export const FirstMileVehicleListPage: React.FC<
  FirstMileVehicleListPageProps
> = ({ title, description }) => {
  const dispatch = useAppDispatch();
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
  const [courierById, setCourierById] = React.useState<
    Record<number, PostOfficeStaff>
  >({});
  const failedCourierIdRef = React.useRef<Set<number>>(new Set());

  const accessScope = React.useMemo(
    () => resolveVehicleAccessScope(roles),
    [roles]
  );

  const canViewVehicles = accessScope !== 'NO_ACCESS';
  const canManageVehicles =
    accessScope === 'ADMIN_ALL' || accessScope === 'MANAGER_POST_OFFICES';

  const selectedPostOfficeNumericId = React.useMemo(
    () => parseOptionalPositiveInteger(formValues.postOfficeId),
    [formValues.postOfficeId]
  );

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

  const { data: postOfficesData, isFetching: isFetchingPostOffices } =
    useGetPostOfficesQuery(
      {
        page: 0,
        size: 500,
      },
      {
        skip: !canManageVehicles,
      }
    );

  const {
    data: couriersByPostOfficeData,
    isFetching: isFetchingCouriersByPostOffice,
  } = useGetActiveCouriersByPostOfficeQuery(selectedPostOfficeNumericId ?? 0, {
    skip:
      !canManageVehicles ||
      !isFormDialogOpen ||
      selectedPostOfficeNumericId === undefined,
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

  const formatCourierOptionLabel = React.useCallback(
    (courier: PostOfficeStaff & { id: number }) => {
      const courierCode = courier.code?.trim();
      const courierFullName = courier.fullName?.trim();

      if (courierCode && courierFullName) {
        return `${courierCode} - ${courierFullName}`;
      }

      if (courierFullName) {
        return courierFullName;
      }

      if (courierCode) {
        return courierCode;
      }

      return `Courier #${courier.id}`;
    },
    []
  );

  const postOfficeOptions = React.useMemo(() => {
    const options = (postOfficesData?.items ?? []).map((postOffice) => {
      const postOfficeCode = postOffice.code?.trim();
      const postOfficeName = postOffice.name?.trim();

      return {
        value: String(postOffice.id),
        label:
          postOfficeCode && postOfficeName
            ? `${postOfficeCode} - ${postOfficeName}`
            : postOfficeCode ||
              postOfficeName ||
              `Post office #${postOffice.id}`,
      };
    });

    if (
      selectedPostOfficeNumericId &&
      !options.some(
        (option) => option.value === String(selectedPostOfficeNumericId)
      )
    ) {
      options.unshift({
        value: String(selectedPostOfficeNumericId),
        label: `Post office #${selectedPostOfficeNumericId}`,
      });
    }

    return options;
  }, [postOfficesData, selectedPostOfficeNumericId]);

  const courierOptions = React.useMemo(() => {
    const options = (couriersByPostOfficeData ?? [])
      .filter(
        (
          courier
        ): courier is PostOfficeStaff & {
          id: number;
        } => Number.isInteger(courier.id) && courier.id > 0
      )
      .map((courier) => ({
        value: String(courier.id),
        label: formatCourierOptionLabel(courier),
      }))
      .sort((a, b) =>
        a.label.localeCompare(b.label, 'en-US', {
          sensitivity: 'base',
        })
      );

    const selectedCourierStaffId = parseOptionalPositiveInteger(
      formValues.postOfficeStaffId
    );

    if (
      selectedCourierStaffId &&
      !options.some((option) => option.value === String(selectedCourierStaffId))
    ) {
      const selectedCourier = courierById[selectedCourierStaffId];

      options.unshift({
        value: String(selectedCourierStaffId),
        label:
          selectedCourier && Number.isInteger(selectedCourier.id)
            ? formatCourierOptionLabel(
                selectedCourier as PostOfficeStaff & { id: number }
              )
            : `Courier #${selectedCourierStaffId}`,
      });
    }

    return options;
  }, [
    courierById,
    couriersByPostOfficeData,
    formValues.postOfficeStaffId,
    formatCourierOptionLabel,
  ]);

  const courierIdsToResolve = React.useMemo(() => {
    const ids = new Set<number>();

    for (const vehicle of data?.items ?? []) {
      if (
        Number.isInteger(vehicle.postOfficeStaffId) &&
        (vehicle.postOfficeStaffId as number) > 0
      ) {
        ids.add(vehicle.postOfficeStaffId as number);
      }
    }

    if (
      Number.isInteger(vehicleDetail?.postOfficeStaffId) &&
      (vehicleDetail?.postOfficeStaffId as number) > 0
    ) {
      ids.add(vehicleDetail?.postOfficeStaffId as number);
    }

    return Array.from(ids);
  }, [data?.items, vehicleDetail?.postOfficeStaffId]);

  React.useEffect(() => {
    const missingCourierIds = courierIdsToResolve.filter((id) => {
      return !courierById[id] && !failedCourierIdRef.current.has(id);
    });

    if (missingCourierIds.length === 0) {
      return;
    }

    let isCancelled = false;

    const fetchCourierDetails = async () => {
      await Promise.all(
        missingCourierIds.map(async (courierId) => {
          try {
            const courier = await dispatch(
              firstMileApi.endpoints.getPostOfficeStaffById.initiate(
                courierId,
                {
                  subscribe: false,
                }
              )
            ).unwrap();

            if (
              isCancelled ||
              !Number.isInteger(courier.id) ||
              courier.id <= 0
            ) {
              return;
            }

            setCourierById((prev) => {
              if (prev[courier.id]) {
                return prev;
              }

              return {
                ...prev,
                [courier.id]: courier,
              };
            });
          } catch {
            if (isCancelled) {
              return;
            }

            failedCourierIdRef.current.add(courierId);
          }
        })
      );
    };

    void fetchCourierDetails();

    return () => {
      isCancelled = true;
    };
  }, [courierById, courierIdsToResolve, dispatch]);

  const resolveCourierLabel = React.useCallback(
    (postOfficeStaffId?: number): string => {
      const courierId = postOfficeStaffId ?? 0;

      if (!Number.isInteger(courierId) || courierId <= 0) {
        return 'Not assigned';
      }

      const courier = courierById[courierId];

      if (!courier) {
        if (failedCourierIdRef.current.has(courierId)) {
          return `Courier #${courierId}`;
        }

        return 'Loading courier...';
      }

      return formatCourierOptionLabel(
        courier as PostOfficeStaff & {
          id: number;
        }
      );
    },
    [courierById, formatCourierOptionLabel]
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
        'For manager role, either post office or courier staff is required.'
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
        <VehiclePageHeader
          canManageVehicles={canManageVehicles}
          title={title}
          description={description}
          onCreateVehicle={handleOpenCreateDialog}
          importAction={
            <VehicleImportCard
              canManageVehicles={canManageVehicles}
              isImportFlowBusy={isImportFlowBusy}
              isExportingTemplate={isExportingTemplate}
              isValidatingImport={isValidatingImport}
              isImportingVehicles={isImportingVehicles}
              importFileInputKey={importFileInputKey}
              selectedImportFile={selectedImportFile}
              validateImportResult={validateImportResult}
              lastImportJob={lastImportJob}
              onDownloadTemplate={handleDownloadTemplate}
              onSelectImportFile={handleSelectImportFile}
              onValidateFile={handleValidateImportFile}
              onImportFile={handleImportFile}
            />
          }
        />

        <VehicleSearchCard
          canViewVehicles={canViewVehicles}
          keywordInput={keywordInput}
          isFetching={isFetching}
          onKeywordInputChange={setKeywordInput}
          onSubmit={handleSearch}
          onRefresh={() => {
            void refetch();
          }}
        />

        <VehicleResultsCard
          canViewVehicles={canViewVehicles}
          canManageVehicles={canManageVehicles}
          data={data}
          isLoading={isLoading}
          isFetching={isFetching}
          isSaving={isSaving}
          isDeleting={isDeleting}
          onViewDetails={handleOpenDetails}
          onEdit={handleOpenEditDialog}
          onDelete={handleRequestDelete}
          onPreviousPage={() => {
            setPage((prev) => Math.max(prev - 1, 0));
          }}
          onNextPage={() => {
            setPage((prev) => prev + 1);
          }}
          resolveCourierLabel={resolveCourierLabel}
        />
      </div>

      <VehicleDetailDialog
        open={isDetailDialogOpen}
        canManageVehicles={canManageVehicles}
        isFetchingVehicleDetail={isFetchingVehicleDetail}
        vehicleDetail={vehicleDetail}
        onOpenChange={handleDetailDialogOpenChange}
        onEditFromDetails={handleEditFromDetails}
        resolveCourierLabel={resolveCourierLabel}
      />

      <VehicleFormDialog
        open={isFormDialogOpen}
        formMode={formMode}
        formValues={formValues}
        isSaving={isSaving}
        postOfficeOptions={postOfficeOptions}
        courierOptions={courierOptions}
        isLoadingPostOffices={isFetchingPostOffices}
        isLoadingCouriers={isFetchingCouriersByPostOffice}
        onOpenChange={handleFormDialogOpenChange}
        onSubmit={handleSubmitForm}
        onUpdateField={updateFormField}
      />

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
