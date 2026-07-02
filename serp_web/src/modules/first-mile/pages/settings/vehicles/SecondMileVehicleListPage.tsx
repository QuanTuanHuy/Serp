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
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Input,
} from '@/shared/components/ui';
import { ConfirmDialog } from '@/shared/components/ui/confirm-dialog';
import { useNotification } from '@/shared/hooks';
import { Plus, RefreshCw, Search, ShieldAlert } from 'lucide-react';
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
  PAGE_SIZE,
  parseOptionalPositiveInteger,
  validateVehicleForm,
  type VehicleFormMode,
  type VehicleFormState,
} from './secondMileVehiclePageModels';

interface SecondMileVehicleListPageProps {
  showScopeNavigation?: boolean;
}

export function SecondMileVehicleListPage({
  showScopeNavigation = true,
}: SecondMileVehicleListPageProps) {
  const notification = useNotification();
  const isTmsAdmin = useAppSelector((state) =>
    Boolean(state.account.user.profile?.roles?.includes('TMS_ADMIN'))
  );

  const [page, setPage] = React.useState(0);
  const [keywordInput, setKeywordInput] = React.useState('');
  const [keyword, setKeyword] = React.useState<string | undefined>();
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
      { page, size: PAGE_SIZE, keyword },
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
      return formatDriverLabel(assignment) ?? `Driver #${assignment.staffId}`;
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
        label: `Driver #${selectedDriverStaffId}`,
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

  if (!isTmsAdmin) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className='flex items-center gap-2'>
            <ShieldAlert className='h-5 w-5' />
            Access denied
          </CardTitle>
          <CardDescription>
            Second-mile vehicles require TMS_ADMIN role.
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
            showScopeNavigation ? 'sm:justify-between' : 'sm:justify-end'
          }`}
        >
          {showScopeNavigation ? (
            <div>
              <div className='mb-2 flex flex-wrap gap-2 text-sm'>
                <Link
                  href='/first-mile/settings/vehicles?scope=first-mile'
                  className='text-muted-foreground hover:text-foreground'
                >
                  First-mile vehicles
                </Link>
                <span className='text-muted-foreground'>/</span>
                <span className='font-medium'>Second-mile</span>
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
                  notification.success('Template downloaded.');
                } catch (error) {
                  notification.error('Download failed.', {
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
                  notification.error('Select a file first.');
                  return;
                }
                const formData = new FormData();
                formData.append('file', importFile);
                try {
                  const result = await validateImport(formData).unwrap();
                  setValidateResult(result);
                  if (result.is_success) {
                    notification.success(
                      `Validated ${result.data.length} row(s).`
                    );
                  }
                } catch (error) {
                  notification.error('Validate failed.', {
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
                  notification.success(`Import job #${job.id} started.`);
                  void refetch();
                } catch (error) {
                  notification.error('Import failed.', {
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
              New vehicle
            </Button>
          </div>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Search</CardTitle>
          </CardHeader>
          <CardContent>
            <form
              className='flex flex-col gap-2 md:flex-row'
              onSubmit={(event) => {
                event.preventDefault();
                setPage(0);
                setKeyword(keywordInput.trim() || undefined);
              }}
            >
              <div className='relative flex-1'>
                <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
                <Input
                  className='pl-10'
                  value={keywordInput}
                  onChange={(e) => setKeywordInput(e.target.value)}
                  placeholder='License plate...'
                />
              </div>
              <Button type='submit'>Search</Button>
              <Button
                type='button'
                variant='outline'
                disabled={isFetching}
                onClick={() => void refetch()}
              >
                <RefreshCw className='mr-2 h-4 w-4' />
                Refresh
              </Button>
            </form>
          </CardContent>
        </Card>

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
          onDelete={setDeleteTarget}
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
              notification.success('Vehicle created.');
              if (page !== 0) setPage(0);
              else void refetch();
            } else if (editingId !== null) {
              await updateVehicle({ id: editingId, body }).unwrap();
              notification.success('Vehicle updated.');
              void refetch();
            }
            setIsFormOpen(false);
            setEditingId(null);
          } catch (error) {
            notification.error('Save failed.', {
              description: getErrorMessage(error),
            });
          }
        }}
      />

      <SecondMileVehicleDetailDialog
        open={detailOpen}
        canManage={isTmsAdmin}
        isFetching={isFetchingDetail}
        isUploadingImage={isUploadingImage}
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
        onUploadImage={async (file) => {
          if (!selectedId) return;
          try {
            await uploadImage({ id: selectedId, file }).unwrap();
            setImageRefreshKey(Date.now());
            void refetch();
            void refetchVehicleDetail();
            notification.success('Image uploaded.');
          } catch (error) {
            notification.error('Upload failed.', {
              description: getErrorMessage(error),
            });
          }
        }}
      />

      <ConfirmDialog
        open={Boolean(deleteTarget)}
        onOpenChange={(open) => {
          if (!open && !isDeleting) setDeleteTarget(null);
        }}
        title='Delete vehicle'
        description={
          deleteTarget
            ? `Delete vehicle ${deleteTarget.licensePlate}?`
            : undefined
        }
        confirmText='Delete'
        variant='destructive'
        isLoading={isDeleting}
        onConfirm={async () => {
          if (!deleteTarget) return;
          try {
            await deleteVehicle(deleteTarget.id).unwrap();
            notification.success('Vehicle deleted.');
            setDeleteTarget(null);
            void refetch();
          } catch (error) {
            notification.error('Delete failed.', {
              description: getErrorMessage(error),
            });
          }
        }}
      />
    </>
  );
}
