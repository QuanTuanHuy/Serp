/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Second-mile vehicle management page
 */

'use client';

import React from 'react';
import Link from 'next/link';
import { getErrorMessage, useAppSelector } from '@/lib/store';
import {
  Button,
  Card,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';
import { ConfirmDialog } from '@/shared/components/ui/confirm-dialog';
import { useNotification } from '@/shared/hooks';
import { Plus, ShieldAlert } from 'lucide-react';
import {
  useCreateSecondMileVehicleMutation,
  useDeleteSecondMileVehicleMutation,
  useGetSecondMileAssignableStaffsQuery,
  useGetHubsQuery,
  useGetSecondMileHubStaffAssignmentsQuery,
  useGetSecondMileVehicleByIdQuery,
  useGetSecondMileVehiclesQuery,
  useImportSecondMileVehiclesMutation,
  useLazyExportSecondMileVehicleTemplateQuery,
  useUpdateSecondMileVehicleMutation,
  useUploadSecondMileVehicleImageMutation,
  useValidateSecondMileVehicleImportMutation,
} from '../../../api';
import type {
  Hub,
  ImportHistory,
  SecondMileHubStaff,
  SecondMileHubStaffAssignment,
  SecondMileVehicle,
  SecondMileVehicleImportItem,
  SecondMileVehicleStatus,
  SecondMileVehicleType,
  ValidateImportFileResponse,
} from '../../../types';
import {
  SecondMileVehicleDetailDialog,
  SecondMileVehicleFormDialog,
  SecondMileVehicleImportCard,
  SecondMileVehicleResultsCard,
} from './components';
import {
  buildVehicleRequest,
  DEFAULT_VEHICLE_FORM,
  mapVehicleToFormState,
  parseOptionalPositiveInteger,
  validateVehicleForm,
  type VehicleFormMode,
  type VehicleFormState,
} from './secondMileVehiclePageModels';

interface SecondMileVehicleListPageProps {
  showScopeNavigation?: boolean;
  scopeNavigation?: React.ReactNode;
}

export function SecondMileVehicleListPage({
  showScopeNavigation = true,
  scopeNavigation,
}: SecondMileVehicleListPageProps) {
  const notification = useNotification();
  const isTmsAdmin = useAppSelector((state) =>
    Boolean(state.account.user.profile?.roles?.includes('TMS_ADMIN'))
  );

  const [page, setPage] = React.useState(0);
  const [pageSize, setPageSize] = React.useState(20);
  const [keywordInput, setKeywordInput] = React.useState('');
  const [keyword, setKeyword] = React.useState<string | undefined>();
  const [vehicleTypeFilter, setVehicleTypeFilter] = React.useState<
    SecondMileVehicleType | undefined
  >();
  const [statusFilter, setStatusFilter] = React.useState<
    SecondMileVehicleStatus | undefined
  >();
  const [hubKeywordInput, setHubKeywordInput] = React.useState('');
  const [hubKeyword, setHubKeyword] = React.useState<string | undefined>();
  const [driverKeywordInput, setDriverKeywordInput] = React.useState('');
  const [driverKeyword, setDriverKeyword] = React.useState<
    string | undefined
  >();
  const [isFormOpen, setIsFormOpen] = React.useState(false);
  const [formMode, setFormMode] = React.useState<VehicleFormMode>('create');
  const [editingId, setEditingId] = React.useState<number | null>(null);
  const [formValues, setFormValues] =
    React.useState<VehicleFormState>(DEFAULT_VEHICLE_FORM);
  const [deleteTarget, setDeleteTarget] =
    React.useState<SecondMileVehicle | null>(null);
  const [detailOpen, setDetailOpen] = React.useState(false);
  const [selectedId, setSelectedId] = React.useState<number | null>(null);
  const [importFile, setImportFile] = React.useState<File | null>(null);
  const [importFileKey, setImportFileKey] = React.useState(0);
  const [validateResult, setValidateResult] =
    React.useState<ValidateImportFileResponse<SecondMileVehicleImportItem> | null>(
      null
    );
  const [lastImportJob, setLastImportJob] =
    React.useState<ImportHistory | null>(null);
  const [imageRefreshKey, setImageRefreshKey] = React.useState(0);

  const { data, isLoading, isFetching, refetch } =
    useGetSecondMileVehiclesQuery(
      {
        page,
        size: pageSize,
        keyword,
        vehicleType: vehicleTypeFilter,
        status: statusFilter,
        hubKeyword,
        driverKeyword,
      },
      { skip: !isTmsAdmin }
    );

  const { data: hubsData, isFetching: isFetchingHubs } = useGetHubsQuery(
    { page: 0, size: 500 },
    { skip: !isTmsAdmin }
  );

  const {
    data: vehicleDetail,
    isFetching: isFetchingDetail,
    refetch: refetchVehicleDetail,
  } = useGetSecondMileVehicleByIdQuery(selectedId ?? 0, {
    skip: !isTmsAdmin || !detailOpen || selectedId === null,
  });

  const { data: driversData } = useGetSecondMileAssignableStaffsQuery(
    { role: 'DRIVER' },
    { skip: !isTmsAdmin }
  );

  const hubById = React.useMemo(() => {
    const map: Record<number, Hub> = {};
    for (const hub of hubsData?.items ?? []) {
      map[hub.id] = hub;
    }
    return map;
  }, [hubsData?.items]);

  const hubOptions = React.useMemo(
    () =>
      (hubsData?.items ?? []).map((hub) => ({
        value: String(hub.id),
        label:
          hub.code && hub.name
            ? `${hub.code} - ${hub.name}`
            : hub.code || hub.name || `Hub #${hub.id}`,
      })),
    [hubsData?.items]
  );

  const formatDriverLabel = React.useCallback(
    (driver?: SecondMileHubStaff | SecondMileHubStaffAssignment) => {
      let code: string | undefined;
      let name: string | undefined;

      if (driver && 'code' in driver) {
        code = driver.code?.trim();
        name = driver.fullName?.trim();
      } else {
        const assignment = driver as SecondMileHubStaffAssignment | undefined;
        code = assignment?.staffCode?.trim();
        name = assignment?.staffFullName?.trim();
      }

      if (code && name) {
        return `${code} - ${name}`;
      }
      if (name) {
        return name;
      }
      if (code) {
        return code;
      }

      return undefined;
    },
    []
  );

  const selectedHubNumericId = React.useMemo(
    () => parseOptionalPositiveInteger(formValues.hubId),
    [formValues.hubId]
  );

  const { data: hubDriverAssignments, isFetching: isFetchingHubDrivers } =
    useGetSecondMileHubStaffAssignmentsQuery(
      { hubId: selectedHubNumericId ?? 0, role: 'DRIVER' },
      {
        skip: !isTmsAdmin || !isFormOpen || selectedHubNumericId === undefined,
      }
    );

  const driverLabelByStaffId = React.useMemo(() => {
    const map: Record<number, string> = {};

    for (const driver of driversData ?? []) {
      const label = formatDriverLabel(driver);
      if (label) {
        map[driver.id] = label;
      }
    }

    for (const assignment of hubDriverAssignments ?? []) {
      if (!assignment.staffId) {
        continue;
      }
      const label = formatDriverLabel(assignment);
      if (label) {
        map[assignment.staffId] = label;
      }
    }

    return map;
  }, [driversData, formatDriverLabel, hubDriverAssignments]);

  const formatDriverOptionLabel = React.useCallback(
    (assignment: SecondMileHubStaffAssignment & { staffId: number }) => {
      return formatDriverLabel(assignment) ?? `Tài xế #${assignment.staffId}`;
    },
    [formatDriverLabel]
  );

  const driverOptions = React.useMemo(() => {
    const options = (hubDriverAssignments ?? [])
      .filter(
        (
          assignment
        ): assignment is SecondMileHubStaffAssignment & { staffId: number } =>
          Number.isInteger(assignment.staffId) &&
          (assignment.staffId as number) > 0
      )
      .map((assignment) => ({
        value: String(assignment.staffId),
        label: formatDriverOptionLabel(assignment),
      }))
      .sort((a, b) =>
        a.label.localeCompare(b.label, 'en-US', {
          sensitivity: 'base',
        })
      );

    const selectedDriverStaffId = parseOptionalPositiveInteger(
      formValues.assignedStaffId
    );

    if (
      selectedDriverStaffId &&
      !options.some((option) => option.value === String(selectedDriverStaffId))
    ) {
      options.unshift({
        value: String(selectedDriverStaffId),
        label: `Tài xế #${selectedDriverStaffId}`,
      });
    }

    return options;
  }, [
    formValues.assignedStaffId,
    formatDriverOptionLabel,
    hubDriverAssignments,
  ]);

  const [createVehicle, { isLoading: isCreating }] =
    useCreateSecondMileVehicleMutation();
  const [updateVehicle, { isLoading: isUpdating }] =
    useUpdateSecondMileVehicleMutation();
  const [deleteVehicle, { isLoading: isDeleting }] =
    useDeleteSecondMileVehicleMutation();
  const [uploadImage, { isLoading: isUploadingImage }] =
    useUploadSecondMileVehicleImageMutation();
  const [exportTemplate, { isFetching: isExporting }] =
    useLazyExportSecondMileVehicleTemplateQuery();
  const [validateImport, { isLoading: isValidating }] =
    useValidateSecondMileVehicleImportMutation();
  const [importVehicles, { isLoading: isImporting }] =
    useImportSecondMileVehiclesMutation();

  const isSaving = isCreating || isUpdating;
  const isImportBusy = isExporting || isValidating || isImporting;

  const updateField = <K extends keyof VehicleFormState>(
    field: K,
    value: VehicleFormState[K]
  ) => {
    setFormValues((prev) => ({ ...prev, [field]: value }));
  };

  const handleSearch = (event: React.FormEvent) => {
    event.preventDefault();
    setPage(0);
    setKeyword(keywordInput.trim() || undefined);
  };

  const handleClearSearch = () => {
    setKeywordInput('');
    setKeyword(undefined);
    setPage(0);
  };

  const handleVehicleTypeFilterChange = (
    value?: SecondMileVehicleType
  ) => {
    setVehicleTypeFilter(value);
    setPage(0);
  };

  const handleStatusFilterChange = (value?: SecondMileVehicleStatus) => {
    setStatusFilter(value);
    setPage(0);
  };

  const handleHubSearch = (event: React.FormEvent) => {
    event.preventDefault();
    setPage(0);
    setHubKeyword(hubKeywordInput.trim() || undefined);
  };

  const handleClearHubSearch = () => {
    setHubKeywordInput('');
    setHubKeyword(undefined);
    setPage(0);
  };

  const handleDriverSearch = (event: React.FormEvent) => {
    event.preventDefault();
    setPage(0);
    setDriverKeyword(driverKeywordInput.trim() || undefined);
  };

  const handleClearDriverSearch = () => {
    setDriverKeywordInput('');
    setDriverKeyword(undefined);
    setPage(0);
  };

  const handleUploadVehicleImage = async (
    vehicle: SecondMileVehicle,
    file: File
  ) => {
    try {
      await uploadImage({ id: vehicle.id, file }).unwrap();
      setImageRefreshKey(Date.now());
      void refetch();
      if (selectedId === vehicle.id) {
        void refetchVehicleDetail();
      }
      notification.success('Đã tải ảnh lên.');
    } catch (error) {
      notification.error('Không thể tải ảnh lên.', {
        description: getErrorMessage(error),
      });
    }
  };

  if (!isTmsAdmin) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className='flex items-center gap-2'>
            <ShieldAlert className='h-5 w-5' />
            Không có quyền truy cập
          </CardTitle>
          <CardDescription>
            Phương tiện chặng giữa yêu cầu quyền TMS_ADMIN.
          </CardDescription>
        </CardHeader>
      </Card>
    );
  }

  return (
    <>
      <div className='space-y-6'>
        <div
          className={`flex flex-col gap-3 sm:flex-row sm:items-center ${
            showScopeNavigation || scopeNavigation
              ? 'sm:justify-between'
              : 'sm:justify-end'
          }`}
        >
          {scopeNavigation ? (
            <div>{scopeNavigation}</div>
          ) : showScopeNavigation ? (
            <div>
              <div className='mb-2 flex flex-wrap gap-2 text-sm'>
                <Link
                  href='/first-mile/settings/vehicles?scope=first-mile'
                  className='text-muted-foreground hover:text-foreground'
                >
                  Phương tiện chặng đầu
                </Link>
                <span className='text-muted-foreground'>/</span>
                <span className='font-medium'>Chặng giữa</span>
              </div>
            </div>
          ) : null}
          <div className='flex flex-wrap items-center gap-2'>
            <SecondMileVehicleImportCard
              canManage={isTmsAdmin}
              isBusy={isImportBusy}
              isExporting={isExporting}
              isValidating={isValidating}
              isImporting={isImporting}
              importFileInputKey={importFileKey}
              selectedFile={importFile}
              validateResult={validateResult}
              lastImportJob={lastImportJob}
              onDownloadTemplate={async () => {
                try {
                  const blob = await exportTemplate().unwrap();
                  const url = URL.createObjectURL(blob);
                  const link = document.createElement('a');
                  link.href = url;
                  link.download = 'vehicle_template.xlsx';
                  document.body.appendChild(link);
                  link.click();
                  link.remove();
                  URL.revokeObjectURL(url);
                  notification.success('Đã tải mẫu nhập liệu.');
                } catch (error) {
                  notification.error('Không thể tải mẫu nhập liệu.', {
                    description: getErrorMessage(error),
                  });
                }
              }}
              onSelectFile={(event) => {
                setImportFile(event.target.files?.[0] ?? null);
                setValidateResult(null);
                setLastImportJob(null);
              }}
              onValidate={async () => {
                if (!importFile) {
                  notification.error('Vui lòng chọn tệp Excel trước.');
                  return;
                }
                const formData = new FormData();
                formData.append('file', importFile);
                try {
                  const result = await validateImport(formData).unwrap();
                  setValidateResult(result);
                  if (result.is_success) {
                    notification.success(
                      `Đã kiểm tra ${result.data.length} dòng.`
                    );
                  }
                } catch (error) {
                  notification.error('Không thể kiểm tra tệp nhập.', {
                    description: getErrorMessage(error),
                  });
                }
              }}
              onImport={async () => {
                if (!importFile || !validateResult?.is_success) {
                  return;
                }
                const formData = new FormData();
                formData.append('file', importFile);
                try {
                  const job = await importVehicles(formData).unwrap();
                  setLastImportJob(job);
                  setImportFile(null);
                  setValidateResult(null);
                  setImportFileKey((k) => k + 1);
                  notification.success(`Đã tạo lệnh nhập #${job.id}.`);
                  void refetch();
                } catch (error) {
                  notification.error('Không thể nhập phương tiện.', {
                    description: getErrorMessage(error),
                  });
                }
              }}
            />
            <Button
              onClick={() => {
                setFormMode('create');
                setEditingId(null);
                setFormValues(DEFAULT_VEHICLE_FORM);
                setIsFormOpen(true);
              }}
            >
              <Plus className='mr-2 h-4 w-4' />
              Thêm phương tiện
            </Button>
          </div>
        </div>

        <SecondMileVehicleResultsCard
          canManage={isTmsAdmin}
          data={data}
          driverLabelByStaffId={driverLabelByStaffId}
          imageRefreshKey={imageRefreshKey}
          hubById={hubById}
          isLoading={isLoading}
          isFetching={isFetching}
          isSaving={isSaving}
          isDeleting={isDeleting}
          isUploadingImage={isUploadingImage}
          pageSize={pageSize}
          keywordInput={keywordInput}
          vehicleTypeFilter={vehicleTypeFilter}
          statusFilter={statusFilter}
          hubKeywordInput={hubKeywordInput}
          driverKeywordInput={driverKeywordInput}
          onView={(id) => {
            setSelectedId(id);
            setDetailOpen(true);
          }}
          onEdit={(vehicle) => {
            setFormMode('edit');
            setEditingId(vehicle.id);
            setFormValues(mapVehicleToFormState(vehicle));
            setIsFormOpen(true);
          }}
          onUploadImage={handleUploadVehicleImage}
          onDelete={setDeleteTarget}
          onKeywordInputChange={setKeywordInput}
          onSearchSubmit={handleSearch}
          onClearSearch={handleClearSearch}
          onVehicleTypeFilterChange={handleVehicleTypeFilterChange}
          onStatusFilterChange={handleStatusFilterChange}
          onHubKeywordInputChange={setHubKeywordInput}
          onHubSearchSubmit={handleHubSearch}
          onClearHubSearch={handleClearHubSearch}
          onDriverKeywordInputChange={setDriverKeywordInput}
          onDriverSearchSubmit={handleDriverSearch}
          onClearDriverSearch={handleClearDriverSearch}
          onPageSizeChange={(nextPageSize) => {
            setPageSize(nextPageSize);
            setPage(0);
          }}
          onPreviousPage={() => setPage((p) => Math.max(p - 1, 0))}
          onNextPage={() => setPage((p) => p + 1)}
        />
      </div>

      <SecondMileVehicleFormDialog
        open={isFormOpen}
        formMode={formMode}
        formValues={formValues}
        isSaving={isSaving}
        hubOptions={hubOptions}
        isLoadingHubs={isFetchingHubs}
        driverOptions={driverOptions}
        isLoadingDrivers={isFetchingHubDrivers}
        onOpenChange={(open) => {
          if (!open && !isSaving) {
            setIsFormOpen(false);
            setEditingId(null);
          }
        }}
        onUpdateField={updateField}
        onSubmit={async (event) => {
          event.preventDefault();
          const err = validateVehicleForm(formValues);
          if (err) {
            notification.error(err);
            return;
          }
          const body = buildVehicleRequest(formValues);
          try {
            if (formMode === 'create') {
              await createVehicle(body).unwrap();
              notification.success('Đã tạo phương tiện.');
              if (page !== 0) setPage(0);
              else void refetch();
            } else if (editingId !== null) {
              await updateVehicle({ id: editingId, body }).unwrap();
              notification.success('Đã cập nhật phương tiện.');
              void refetch();
            }
            setIsFormOpen(false);
            setEditingId(null);
          } catch (error) {
            notification.error('Không thể lưu phương tiện.', {
              description: getErrorMessage(error),
            });
          }
        }}
      />

      <SecondMileVehicleDetailDialog
        open={detailOpen}
        canManage={isTmsAdmin}
        isFetching={isFetchingDetail}
        vehicle={vehicleDetail}
        driverLabelByStaffId={driverLabelByStaffId}
        hubById={hubById}
        imageRefreshKey={imageRefreshKey}
        onOpenChange={(open) => {
          setDetailOpen(open);
          if (!open) setSelectedId(null);
        }}
        onEdit={() => {
          if (!vehicleDetail) return;
          setFormMode('edit');
          setEditingId(vehicleDetail.id);
          setFormValues(mapVehicleToFormState(vehicleDetail));
          setDetailOpen(false);
          setSelectedId(null);
          setIsFormOpen(true);
        }}
      />

      <ConfirmDialog
        open={Boolean(deleteTarget)}
        onOpenChange={(open) => {
          if (!open && !isDeleting) setDeleteTarget(null);
        }}
        title='Xóa phương tiện'
        description={
          deleteTarget
            ? `Xóa phương tiện ${deleteTarget.licensePlate}?`
            : undefined
        }
        confirmText='Xóa'
        variant='destructive'
        isLoading={isDeleting}
        onConfirm={async () => {
          if (!deleteTarget) return;
          try {
            await deleteVehicle(deleteTarget.id).unwrap();
            notification.success('Đã xóa phương tiện.');
            setDeleteTarget(null);
            void refetch();
          } catch (error) {
            notification.error('Không thể xóa phương tiện.', {
              description: getErrorMessage(error),
            });
          }
        }}
      />
    </>
  );
}
