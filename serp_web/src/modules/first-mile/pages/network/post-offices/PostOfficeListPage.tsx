/**
 * Author: Nguyễn Thế Anh
 * Description: Part of Serp Project - First-mile post office list page
 */

'use client';

import React from 'react';
import { getErrorMessage, useAppSelector } from '@/lib/store';
import {
  Badge,
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  Input,
  Label,
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui';
import { useNotification } from '@/shared/hooks';
import { ConfirmDialog } from '@/shared/components/ui/confirm-dialog';
import { Plus, ShieldAlert, UserCog } from 'lucide-react';
import type { TmsFilterMode } from '../../../components/list';
import {
  useCreatePostOfficeMutation,
  useDeletePostOfficeMutation,
  useGetOrdersQuery,
  useGetPostOfficesQuery,
  useGetWardsByProvinceCodeQuery,
  useGetPostOfficeStaffAssignmentsByPostOfficeQuery,
  useGetAssignablePostOfficeStaffsQuery,
  useAssignCourierToPostOfficeMutation,
  useAssignManagerToPostOfficeMutation,
  useUnassignPostOfficeStaffAssignmentMutation,
  useImportPostOfficesMutation,
  useLazyExportPostOfficeTemplateQuery,
  useUpdatePostOfficeMutation,
  useValidatePostOfficeImportMutation,
} from '../../../api';
import {
  CoordinatePickerMap,
  TmsCombobox,
  TmsEntityLocationMap,
  TmsEntityOrdersTab,
  type TmsLocationMapPoint,
  type TmsMapBounds,
} from '../../../components';
import type {
  FirstMileOrderStatus,
  ImportHistory,
  PostOffice,
  PostOfficeImportItem,
  PostOfficeListFilters,
  PostOfficeStaffRole,
  ValidateImportFileResponse,
  Ward,
} from '../../../types';
import {
  PostOfficeFiltersCard,
  PostOfficeFormDialog,
  PostOfficeImportCard,
  PostOfficeResultsCard,
} from './components';
import {
  buildPostOfficeListFilters,
  countActivePostOfficeAdvancedFilters,
  DEFAULT_POST_OFFICE_FILTER_FORM,
  type PostOfficeFilterFormState,
} from './postOfficeFilterModels';
import {
  buildCreatePostOfficeRequest,
  DEFAULT_POST_OFFICE_FORM,
  getStatusBadgeVariant,
  mapPostOfficeToFormState,
  type PostOfficeFormMode,
  type PostOfficeFormState,
  validatePostOfficeForm,
} from './postOfficeForm';
import { usePostOfficeLocations } from './usePostOfficeLocations';

const PAGE_SIZE = 20;
const MAP_PAGE_SIZE = 500;
const IMPORT_PREVIEW_LIMIT = 5;
const POST_OFFICE_ORDER_PAGE_SIZE = 10;
const POST_OFFICE_STOCK_ORDER_STATUSES: FirstMileOrderStatus[] = [
  'AT_ORIGIN_POST_OFFICE',
];

function areMapBoundsEqual(
  current: TmsMapBounds | null,
  next: TmsMapBounds
): boolean {
  return (
    current?.minLatitude === next.minLatitude &&
    current.maxLatitude === next.maxLatitude &&
    current.minLongitude === next.minLongitude &&
    current.maxLongitude === next.maxLongitude
  );
}

export const PostOfficeListPage: React.FC = () => {
  const notification = useNotification();
  const isTmsAdmin = useAppSelector((state) =>
    Boolean(state.account.user.profile?.roles?.includes('TMS_ADMIN'))
  );

  const [page, setPage] = React.useState(0);
  const [filterMode, setFilterMode] = React.useState<TmsFilterMode>('basic');
  const [filterFormValues, setFilterFormValues] =
    React.useState<PostOfficeFilterFormState>(DEFAULT_POST_OFFICE_FILTER_FORM);
  const [appliedFilters, setAppliedFilters] =
    React.useState<PostOfficeListFilters>({});
  const [mapBounds, setMapBounds] = React.useState<TmsMapBounds | null>(null);
  const [formMode, setFormMode] = React.useState<PostOfficeFormMode>('create');
  const [isFormDialogOpen, setIsFormDialogOpen] = React.useState(false);
  const [editingId, setEditingId] = React.useState<number | null>(null);
  const [formValues, setFormValues] = React.useState<PostOfficeFormState>(
    DEFAULT_POST_OFFICE_FORM
  );
  const [detailTarget, setDetailTarget] = React.useState<PostOffice | null>(
    null
  );
  const [detailTab, setDetailTab] = React.useState<'details' | 'orders'>(
    'details'
  );
  const [detailOrdersPage, setDetailOrdersPage] = React.useState(0);
  const [deleteTarget, setDeleteTarget] = React.useState<PostOffice | null>(
    null
  );
  const [selectedImportFile, setSelectedImportFile] =
    React.useState<File | null>(null);
  const [importFileInputKey, setImportFileInputKey] = React.useState(0);
  const [validateImportResult, setValidateImportResult] =
    React.useState<ValidateImportFileResponse<PostOfficeImportItem> | null>(
      null
    );
  const [lastImportJob, setLastImportJob] =
    React.useState<ImportHistory | null>(null);
  const [manageStaffPostOffice, setManageStaffPostOffice] =
    React.useState<PostOffice | null>(null);
  const [staffDialogOpen, setStaffDialogOpen] = React.useState(false);
  const [staffRoleFilter, setStaffRoleFilter] = React.useState<
    'ALL' | PostOfficeStaffRole
  >('ALL');
  const [staffRoleToAssign, setStaffRoleToAssign] =
    React.useState<PostOfficeStaffRole>('COURIER');
  const [staffSearchKeyword, setStaffSearchKeyword] = React.useState('');
  const [selectedStaffIdToAssign, setSelectedStaffIdToAssign] =
    React.useState('');

  const { data, isLoading, isFetching, refetch } = useGetPostOfficesQuery({
    page,
    size: PAGE_SIZE,
    ...appliedFilters,
  });

  const {
    data: mapPostOfficeData,
    isFetching: isFetchingMapPostOffices,
    refetch: refetchMapPostOffices,
  } = useGetPostOfficesQuery(
    {
      page: 0,
      size: MAP_PAGE_SIZE,
      ...appliedFilters,
      hasLocation: true,
      ...(mapBounds ?? {}),
    },
    { skip: !mapBounds }
  );
  const { data: detailOrdersData, isFetching: isFetchingDetailOrders } =
    useGetOrdersQuery(
      {
        page: detailOrdersPage,
        size: POST_OFFICE_ORDER_PAGE_SIZE,
        originPostOfficeCode: detailTarget?.code,
        statuses: POST_OFFICE_STOCK_ORDER_STATUSES,
      },
      { skip: !detailTarget }
    );

  const mapPostOfficePoints = React.useMemo(
    () =>
      (mapPostOfficeData?.items ?? []).flatMap((postOffice) => {
        if (
          postOffice.latitude === undefined ||
          postOffice.latitude === null ||
          postOffice.longitude === undefined ||
          postOffice.longitude === null
        ) {
          return [];
        }

        return [
          {
            id: postOffice.id,
            code: postOffice.code,
            name: postOffice.name,
            latitude: postOffice.latitude,
            longitude: postOffice.longitude,
            address: postOffice.addressDetail,
            status: postOffice.status,
          },
        ];
      }),
    [mapPostOfficeData]
  );

  const handleMapBoundsChange = React.useCallback((bounds: TmsMapBounds) => {
    setMapBounds((current) =>
      areMapBoundsEqual(current, bounds) ? current : bounds
    );
  }, []);

  const selectedFilterProvinceCode = React.useMemo(
    () => filterFormValues.provinceCode.trim(),
    [filterFormValues.provinceCode]
  );

  const selectedFilterWardCode = React.useMemo(
    () => filterFormValues.wardCode.trim(),
    [filterFormValues.wardCode]
  );

  const { data: wardsForFilterData, isFetching: isFetchingWardsForFilter } =
    useGetWardsByProvinceCodeQuery(
      {
        provinceCode: selectedFilterProvinceCode,
        page: 0,
        size: 1000,
      },
      {
        skip: !selectedFilterProvinceCode,
      }
    );

  const advancedFieldCount = React.useMemo(
    () => countActivePostOfficeAdvancedFilters(filterFormValues),
    [filterFormValues]
  );

  const filterWardOptions = React.useMemo(() => {
    const options = [...(wardsForFilterData?.items ?? [])];

    if (
      selectedFilterWardCode &&
      !options.some((ward) => ward.wardCode === selectedFilterWardCode)
    ) {
      options.unshift({
        wardCode: selectedFilterWardCode,
        name: selectedFilterWardCode,
        provinceCode: selectedFilterProvinceCode,
      } as Ward);
    }

    return options;
  }, [selectedFilterProvinceCode, selectedFilterWardCode, wardsForFilterData]);

  const [createPostOffice, { isLoading: isCreating }] =
    useCreatePostOfficeMutation();
  const [updatePostOffice, { isLoading: isUpdating }] =
    useUpdatePostOfficeMutation();
  const [deletePostOffice, { isLoading: isDeleting }] =
    useDeletePostOfficeMutation();
  const [triggerExportPostOfficeTemplate, { isFetching: isExportingTemplate }] =
    useLazyExportPostOfficeTemplateQuery();
  const [validatePostOfficeImport, { isLoading: isValidatingImport }] =
    useValidatePostOfficeImportMutation();
  const [importPostOfficeFile, { isLoading: isImportingPostOffices }] =
    useImportPostOfficesMutation();
  const [assignCourierToPostOffice, { isLoading: isAssigningCourier }] =
    useAssignCourierToPostOfficeMutation();
  const [assignManagerToPostOffice, { isLoading: isAssigningManager }] =
    useAssignManagerToPostOfficeMutation();
  const [unassignPostOfficeStaffAssignment, { isLoading: isUnassigningStaff }] =
    useUnassignPostOfficeStaffAssignmentMutation();

  const {
    data: staffAssignments,
    isFetching: isFetchingStaffAssignments,
    refetch: refetchStaffAssignments,
  } = useGetPostOfficeStaffAssignmentsByPostOfficeQuery(
    {
      postOfficeId: manageStaffPostOffice?.id ?? 0,
      ...(staffRoleFilter !== 'ALL' ? { role: staffRoleFilter } : {}),
    },
    { skip: !manageStaffPostOffice }
  );

  const {
    data: assignablePostOfficeStaffs,
    isFetching: isFetchingAssignableStaffs,
  } = useGetAssignablePostOfficeStaffsQuery(
    {
      role: staffRoleToAssign,
      ...(staffSearchKeyword.trim()
        ? { keyword: staffSearchKeyword.trim() }
        : {}),
    },
    { skip: !manageStaffPostOffice }
  );
  const staffRoleFilterOptions = [
    { value: 'ALL', label: 'All roles' },
    { value: 'MANAGER', label: 'Manager' },
    { value: 'COURIER', label: 'Courier' },
  ];
  const staffRoleAssignOptions = [
    { value: 'MANAGER', label: 'Manager' },
    { value: 'COURIER', label: 'Courier' },
  ];
  const assignableStaffOptions = (assignablePostOfficeStaffs ?? []).map(
    (staff) => ({
      value: String(staff.id),
      label:
        (staff.fullName || staff.code || `#${staff.id}`) +
        (staff.code ? ` (${staff.code})` : ''),
    })
  );

  const isSaving = isCreating || isUpdating;
  const isImportFlowBusy =
    isExportingTemplate || isValidatingImport || isImportingPostOffices;

  const updateFormField = React.useCallback(
    <K extends keyof PostOfficeFormState>(
      field: K,
      value: PostOfficeFormState[K]
    ) => {
      setFormValues((prev) => ({
        ...prev,
        [field]: value,
      }));
    },
    []
  );

  const updateFilterField = React.useCallback(
    <K extends keyof PostOfficeFilterFormState>(
      field: K,
      value: PostOfficeFilterFormState[K]
    ) => {
      setFilterFormValues((prev) => ({
        ...prev,
        [field]: value,
      }));
    },
    []
  );

  const handleApplyFilters = (event: React.FormEvent) => {
    event.preventDefault();

    try {
      const nextFilters = buildPostOfficeListFilters(filterFormValues);
      setPage(0);
      setAppliedFilters(nextFilters);
    } catch (error) {
      notification.error(
        error instanceof Error ? error.message : 'Invalid filter values.'
      );
    }
  };

  const handleClearFilters = () => {
    setFilterFormValues(DEFAULT_POST_OFFICE_FILTER_FORM);
    setAppliedFilters({});
    setFilterMode('basic');
    setPage(0);
  };

  const validatedPreviewItems = React.useMemo(
    () => validateImportResult?.data?.slice(0, IMPORT_PREVIEW_LIMIT) ?? [],
    [validateImportResult]
  );

  const {
    selectedProvinceCode,
    selectedWardCode,
    provinceSelectOptions,
    wardSelectOptions,
    isFetchingWardsForForm,
    getProvinceLabel,
    getWardLabel,
  } = usePostOfficeLocations({
    postOffices: data?.items,
    previewItems: validatedPreviewItems,
    formProvinceCode: formValues.province_code,
    formWardCode: formValues.ward_code,
  });

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
    if (!isTmsAdmin) {
      notification.error('Only TMS_ADMIN can download post office templates.');
      return;
    }

    try {
      const blob = await triggerExportPostOfficeTemplate(undefined).unwrap();
      const downloadUrl = URL.createObjectURL(blob);
      const link = document.createElement('a');

      link.href = downloadUrl;
      link.download = 'post_office_template.xlsx';
      document.body.appendChild(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(downloadUrl);

      notification.success('Post office template downloaded successfully.');
    } catch (error) {
      notification.error('Failed to download post office template.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleValidateImportFile = async () => {
    if (!isTmsAdmin) {
      notification.error('Only TMS_ADMIN can validate post office imports.');
      return;
    }

    const formData = buildImportFormData();
    if (!formData) {
      notification.error('Please select an Excel file first.');
      return;
    }

    try {
      const result = await validatePostOfficeImport(formData).unwrap();
      setValidateImportResult(result);

      if (result.is_success) {
        notification.success('File validated successfully.', {
          description: `${result.data.length} row(s) are ready to import.`,
        });
      }
    } catch (error) {
      notification.error('Failed to validate post office import file.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleImportFile = async () => {
    if (!isTmsAdmin) {
      notification.error('Only TMS_ADMIN can import post offices.');
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
      const importResult = await importPostOfficeFile(formData).unwrap();
      setLastImportJob(importResult);
      resetImportFileSelection();

      notification.success('Post office import job created.', {
        description: `Job #${importResult.id} is ${importResult.status}.`,
      });

      void refetch();
      if (mapBounds) {
        void refetchMapPostOffices();
      }
    } catch (error) {
      notification.error('Failed to import post office file.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleOpenCreateDialog = () => {
    if (!isTmsAdmin) {
      notification.error('Only TMS_ADMIN can create post offices.');
      return;
    }

    setFormMode('create');
    setEditingId(null);
    setFormValues(DEFAULT_POST_OFFICE_FORM);
    setIsFormDialogOpen(true);
  };

  const handleOpenEditDialog = (postOffice: PostOffice) => {
    if (!isTmsAdmin) {
      notification.error('Only TMS_ADMIN can update post offices.');
      return;
    }

    setFormMode('edit');
    setEditingId(postOffice.id);
    setFormValues(mapPostOfficeToFormState(postOffice));
    setIsFormDialogOpen(true);
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
    setEditingId(null);
  };

  const handleSubmitForm = async (event: React.FormEvent) => {
    event.preventDefault();

    if (!isTmsAdmin) {
      notification.error('Only TMS_ADMIN can modify post offices.');
      return;
    }

    const validationError = validatePostOfficeForm(formValues);
    if (validationError) {
      notification.error(validationError);
      return;
    }

    const payload = buildCreatePostOfficeRequest(formValues);

    try {
      if (formMode === 'create') {
        await createPostOffice(payload).unwrap();
        notification.success('Post office created successfully.');

        if (page !== 0) {
          setPage(0);
        } else {
          void refetch();
        }

        if (mapBounds) {
          void refetchMapPostOffices();
        }
      } else {
        if (editingId === null) {
          notification.error('Missing post office id for update.');
          return;
        }

        await updatePostOffice({
          id: editingId,
          body: payload,
        }).unwrap();

        notification.success('Post office updated successfully.');
        void refetch();
        if (mapBounds) {
          void refetchMapPostOffices();
        }
      }

      setIsFormDialogOpen(false);
      setEditingId(null);
    } catch (error) {
      notification.error('Failed to save post office.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleRequestDelete = (postOffice: PostOffice) => {
    if (!isTmsAdmin) {
      notification.error('Only TMS_ADMIN can delete post offices.');
      return;
    }

    setDeleteTarget(postOffice);
  };

  const handleOpenDetail = (postOffice: PostOffice) => {
    setDetailTarget(postOffice);
    setDetailTab('details');
    setDetailOrdersPage(0);
  };

  const handleMapPostOfficeClick = React.useCallback(
    (point: TmsLocationMapPoint) => {
      const postOffice =
        (mapPostOfficeData?.items ?? []).find((item) => item.id === point.id) ??
        (data?.items ?? []).find((item) => item.id === point.id);

      if (postOffice) {
        setDetailTarget(postOffice);
        setDetailTab('details');
        setDetailOrdersPage(0);
      }
    },
    [data?.items, mapPostOfficeData?.items]
  );

  const handleOpenManageStaff = (postOffice: PostOffice) => {
    if (!isTmsAdmin) {
      notification.error(
        'Only TMS_ADMIN can manage post office staff assignments.'
      );
      return;
    }
    setManageStaffPostOffice(postOffice);
    setStaffRoleFilter('ALL');
    setStaffRoleToAssign('COURIER');
    setStaffSearchKeyword('');
    setSelectedStaffIdToAssign('');
    setStaffDialogOpen(true);
  };

  const handleAssignStaffToPostOffice = async () => {
    if (!isTmsAdmin) {
      notification.error('Only TMS_ADMIN can assign post office staff.');
      return;
    }
    if (!manageStaffPostOffice?.id) {
      return;
    }
    const staffId = Number(selectedStaffIdToAssign);
    if (!Number.isInteger(staffId) || staffId <= 0) {
      notification.error('Select a staff from dropdown.');
      return;
    }

    try {
      if (staffRoleToAssign === 'MANAGER') {
        await assignManagerToPostOffice({
          staffId,
          postOfficeId: manageStaffPostOffice.id,
        }).unwrap();
      } else {
        await assignCourierToPostOffice({
          staffId,
          postOfficeId: manageStaffPostOffice.id,
        }).unwrap();
      }
      notification.success('Staff assignment updated successfully.');
      setSelectedStaffIdToAssign('');
      void refetchStaffAssignments();
    } catch (error) {
      notification.error('Failed to assign staff to post office.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleUnassignStaffFromPostOffice = async (assignmentId?: number) => {
    if (!isTmsAdmin) {
      notification.error('Only TMS_ADMIN can unassign post office staff.');
      return;
    }
    if (!assignmentId) {
      return;
    }

    try {
      await unassignPostOfficeStaffAssignment(assignmentId).unwrap();
      notification.success('Staff unassigned successfully.');
      void refetchStaffAssignments();
    } catch (error) {
      notification.error('Failed to unassign staff.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleDeletePostOffice = async () => {
    if (!deleteTarget) {
      return;
    }

    if (!isTmsAdmin) {
      notification.error('Only TMS_ADMIN can delete post offices.');
      return;
    }

    try {
      await deletePostOffice(deleteTarget.id).unwrap();
      notification.success('Post office deleted successfully.');
      setDeleteTarget(null);

      if ((data?.items.length ?? 0) === 1 && page > 0) {
        setPage((prev) => Math.max(prev - 1, 0));
      } else {
        void refetch();
      }
      if (mapBounds) {
        void refetchMapPostOffices();
      }
    } catch (error) {
      notification.error('Failed to delete post office.', {
        description: getErrorMessage(error),
      });
    }
  };

  return (
    <>
      <div className='space-y-6'>
        <div className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between'>
          <div className='flex flex-col gap-2'>
            <h1 className='text-2xl font-bold tracking-tight'>Post Offices</h1>
            <p className='text-muted-foreground'>
              Manage post offices and monitor geocoded location quality.
            </p>
          </div>

          {isTmsAdmin ? (
            <div className='flex flex-wrap items-center gap-2'>
              <PostOfficeImportCard
                isTmsAdmin={isTmsAdmin}
                isImportFlowBusy={isImportFlowBusy}
                isExportingTemplate={isExportingTemplate}
                isValidatingImport={isValidatingImport}
                isImportingPostOffices={isImportingPostOffices}
                importFileInputKey={importFileInputKey}
                selectedImportFile={selectedImportFile}
                validateImportResult={validateImportResult}
                lastImportJob={lastImportJob}
                onSelectImportFile={handleSelectImportFile}
                onDownloadTemplate={handleDownloadTemplate}
                onValidateImportFile={handleValidateImportFile}
                onImportFile={handleImportFile}
              />
              <Button onClick={handleOpenCreateDialog}>
                <Plus className='h-4 w-4 mr-2' />
                New Post Office
              </Button>
            </div>
          ) : (
            <Badge variant='outline' className='gap-1'>
              <ShieldAlert className='h-3.5 w-3.5' />
              View only (write actions require TMS_ADMIN)
            </Badge>
          )}
        </div>

        <PostOfficeFiltersCard
          filterMode={filterMode}
          filterFormValues={filterFormValues}
          advancedFieldCount={advancedFieldCount}
          isFetching={isFetching}
          provinceSelectOptions={provinceSelectOptions}
          filterWardOptions={filterWardOptions}
          selectedFilterProvinceCode={selectedFilterProvinceCode}
          selectedFilterWardCode={selectedFilterWardCode}
          isFetchingWardsForFilter={isFetchingWardsForFilter}
          onFilterModeChange={setFilterMode}
          onFilterFieldChange={updateFilterField}
          onApplyFilters={handleApplyFilters}
          onClearFilters={handleClearFilters}
          onRefresh={() => {
            void refetch();
          }}
        />

        <TmsEntityLocationMap
          title='Post office map'
          description='Markers are loaded only for post offices inside the visible map area.'
          points={mapPostOfficePoints}
          markerColor='#1d4ed8'
          markerFillColor='#2563eb'
          loading={!mapBounds || isFetchingMapPostOffices}
          totalItems={mapPostOfficeData?.totalItems}
          emptyText='No geocoded post offices in this map area.'
          onBoundsChange={handleMapBoundsChange}
          onPointClick={handleMapPostOfficeClick}
        />

        <PostOfficeResultsCard
          data={data}
          isLoading={isLoading}
          isFetching={isFetching}
          isTmsAdmin={isTmsAdmin}
          isSaving={isSaving}
          isDeleting={isDeleting}
          onViewDetails={handleOpenDetail}
          onEdit={handleOpenEditDialog}
          onDelete={handleRequestDelete}
          onPreviousPage={() => setPage((prev) => Math.max(prev - 1, 0))}
          onNextPage={() => setPage((prev) => prev + 1)}
          getProvinceLabel={getProvinceLabel}
          getWardLabel={getWardLabel}
          getStatusBadgeVariant={getStatusBadgeVariant}
        />
      </div>

      <PostOfficeFormDialog
        open={isFormDialogOpen}
        formMode={formMode}
        isSaving={isSaving}
        formValues={formValues}
        selectedProvinceCode={selectedProvinceCode}
        selectedWardCode={selectedWardCode}
        provinceSelectOptions={provinceSelectOptions}
        wardSelectOptions={wardSelectOptions}
        isFetchingWardsForForm={isFetchingWardsForForm}
        onOpenChange={handleFormDialogOpenChange}
        onSubmit={handleSubmitForm}
        updateFormField={updateFormField}
      />

      <Dialog
        open={Boolean(detailTarget)}
        onOpenChange={(open) => {
          if (!open) {
            setDetailTarget(null);
            setDetailTab('details');
            setDetailOrdersPage(0);
          }
        }}
      >
        <DialogContent className='sm:max-w-5xl max-h-[90vh] overflow-y-auto'>
          <DialogHeader>
            <DialogTitle>Post Office Details</DialogTitle>
            <DialogDescription>
              Detailed information and map location of selected post office.
            </DialogDescription>
          </DialogHeader>

          {detailTarget ? (
            <Tabs
              value={detailTab}
              onValueChange={(value) => setDetailTab(value as typeof detailTab)}
            >
              <TabsList>
                <TabsTrigger value='details'>Details</TabsTrigger>
                <TabsTrigger value='orders'>Orders</TabsTrigger>
              </TabsList>
              <TabsContent value='details' className='mt-4'>
                <div className='space-y-4'>
                  <div className='grid gap-3 md:grid-cols-2 text-sm'>
                    <div>
                      <p className='text-muted-foreground'>Code</p>
                      <p className='font-medium'>{detailTarget.code}</p>
                    </div>
                    <div>
                      <p className='text-muted-foreground'>Name</p>
                      <p className='font-medium'>{detailTarget.name}</p>
                    </div>
                    <div>
                      <p className='text-muted-foreground'>Status</p>
                      <Badge
                        variant={getStatusBadgeVariant(detailTarget.status)}
                      >
                        {detailTarget.status}
                      </Badge>
                    </div>
                    <div>
                      <p className='text-muted-foreground'>Phone</p>
                      <p className='font-medium'>
                        {detailTarget.phoneNumber || '--'}
                      </p>
                    </div>
                    <div className='md:col-span-2'>
                      <p className='text-muted-foreground'>Address</p>
                      <p className='font-medium'>
                        {detailTarget.addressDetail}
                      </p>
                    </div>
                    <div>
                      <p className='text-muted-foreground'>Province / Ward</p>
                      <p className='font-medium'>
                        {getProvinceLabel(detailTarget.provinceCode)} /{' '}
                        {getWardLabel(
                          detailTarget.provinceCode,
                          detailTarget.wardCode
                        )}
                      </p>
                    </div>
                    <div>
                      <p className='text-muted-foreground'>
                        Service radius (m)
                      </p>
                      <p className='font-medium'>
                        {detailTarget.serviceRadiusM}
                      </p>
                    </div>
                    <div>
                      <p className='text-muted-foreground'>
                        Pickup order capacity
                      </p>
                      <p className='font-medium'>
                        {detailTarget.dailyCapacity ?? '--'}
                      </p>
                    </div>
                    <div>
                      <p className='text-muted-foreground'>
                        Current pickup load
                      </p>
                      <p className='font-medium'>
                        {detailTarget.currentLoad ?? '--'}
                      </p>
                    </div>
                    <div>
                      <p className='text-muted-foreground'>Delivery capacity</p>
                      <p className='font-medium'>
                        {detailTarget.deliveryCapacity ?? '--'}
                      </p>
                    </div>
                    <div>
                      <p className='text-muted-foreground'>
                        Current delivery load
                      </p>
                      <p className='font-medium'>
                        {detailTarget.currentDeliveryLoad ?? '--'}
                      </p>
                    </div>
                    <div>
                      <p className='text-muted-foreground'>Priority</p>
                      <p className='font-medium'>
                        {detailTarget.priority ?? '--'}
                      </p>
                    </div>
                    <div>
                      <p className='text-muted-foreground'>Coordinates</p>
                      <p className='font-medium'>
                        {detailTarget.latitude ?? '--'},{' '}
                        {detailTarget.longitude ?? '--'}
                      </p>
                    </div>
                  </div>

                  <div className='space-y-2'>
                    <p className='text-sm font-medium'>Map</p>
                    {detailTarget.latitude !== undefined &&
                    detailTarget.latitude !== null &&
                    detailTarget.longitude !== undefined &&
                    detailTarget.longitude !== null ? (
                      <CoordinatePickerMap
                        latitude={detailTarget.latitude}
                        longitude={detailTarget.longitude}
                        disabled
                        className='h-72'
                        onChange={() => {
                          // Read-only map in detail mode.
                        }}
                      />
                    ) : (
                      <p className='text-sm text-muted-foreground'>
                        This post office does not have geocoded coordinates yet.
                      </p>
                    )}
                  </div>

                  {isTmsAdmin ? (
                    <div className='flex justify-end border-t pt-3'>
                      <Button
                        type='button'
                        variant='outline'
                        onClick={() => {
                          if (detailTarget) {
                            handleOpenManageStaff(detailTarget);
                          }
                        }}
                      >
                        <UserCog className='h-4 w-4 mr-1' />
                        Manage staff assignments
                      </Button>
                    </div>
                  ) : null}
                </div>
              </TabsContent>
              <TabsContent value='orders' className='mt-4'>
                <TmsEntityOrdersTab
                  data={detailOrdersData}
                  isFetching={isFetchingDetailOrders}
                  page={detailOrdersPage}
                  emptyText='No orders are currently at this post office.'
                  onPreviousPage={() =>
                    setDetailOrdersPage((prev) => Math.max(prev - 1, 0))
                  }
                  onNextPage={() => setDetailOrdersPage((prev) => prev + 1)}
                />
              </TabsContent>
            </Tabs>
          ) : null}
        </DialogContent>
      </Dialog>

      <Dialog
        open={staffDialogOpen}
        onOpenChange={(open) => {
          setStaffDialogOpen(open);
          if (!open) {
            setManageStaffPostOffice(null);
          }
        }}
      >
        <DialogContent className='max-w-2xl max-h-[80vh] overflow-y-auto'>
          <DialogHeader>
            <DialogTitle>
              Post office staff assignments — {manageStaffPostOffice?.name} (
              {manageStaffPostOffice?.code})
            </DialogTitle>
            <DialogDescription>
              Assign or unassign manager/courier for this post office.
            </DialogDescription>
          </DialogHeader>

          <div className='space-y-4'>
            <div className='grid gap-3 sm:grid-cols-2'>
              <div className='space-y-2'>
                <Label htmlFor='po-staff-role-filter'>Role filter</Label>
                <TmsCombobox
                  id='po-staff-role-filter'
                  value={staffRoleFilter}
                  onValueChange={(value) =>
                    setStaffRoleFilter(value as 'ALL' | PostOfficeStaffRole)
                  }
                  options={staffRoleFilterOptions}
                  placeholder='All roles'
                  emptyText='No roles found'
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='po-staff-role-assign'>Assign role</Label>
                <TmsCombobox
                  id='po-staff-role-assign'
                  value={staffRoleToAssign}
                  onValueChange={(value) =>
                    setStaffRoleToAssign(value as PostOfficeStaffRole)
                  }
                  options={staffRoleAssignOptions}
                  placeholder='Select role'
                  emptyText='No roles found'
                />
              </div>
            </div>

            <div className='space-y-2'>
              <Label htmlFor='po-staff-search'>Search staff (code/name)</Label>
              <Input
                id='po-staff-search'
                placeholder='e.g. USR_123_COURIER or Nguyen Van A'
                value={staffSearchKeyword}
                onChange={(event) => setStaffSearchKeyword(event.target.value)}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='po-staff-select'>Staff</Label>
              <div className='flex gap-2'>
                <TmsCombobox
                  id='po-staff-select'
                  value={selectedStaffIdToAssign}
                  onValueChange={setSelectedStaffIdToAssign}
                  options={assignableStaffOptions}
                  placeholder={
                    isFetchingAssignableStaffs
                      ? 'Loading staffs...'
                      : 'Select staff'
                  }
                  emptyText='No staffs found'
                  loading={isFetchingAssignableStaffs}
                />
                <Button
                  onClick={() => void handleAssignStaffToPostOffice()}
                  disabled={
                    isAssigningCourier ||
                    isAssigningManager ||
                    !selectedStaffIdToAssign
                  }
                >
                  {isAssigningCourier || isAssigningManager
                    ? 'Assigning...'
                    : 'Assign'}
                </Button>
              </div>
            </div>

            {isFetchingStaffAssignments ? (
              <p className='text-sm text-muted-foreground'>
                Loading assignments...
              </p>
            ) : (staffAssignments ?? []).length === 0 ? (
              <p className='text-sm text-muted-foreground'>
                No active staff assignments for this post office.
              </p>
            ) : (
              <div className='space-y-2'>
                {(staffAssignments ?? []).map((assignment) => (
                  <div
                    key={assignment.id}
                    className='flex items-center justify-between gap-2 rounded-md border p-3'
                  >
                    <div className='text-sm'>
                      <p className='font-medium'>
                        {assignment.staffFullName ||
                          assignment.staffCode ||
                          `#${assignment.staffId}`}
                      </p>
                      <p className='text-muted-foreground'>
                        Role: {assignment.staffRole || '--'} · From:{' '}
                        {assignment.assignedFrom || '--'}
                      </p>
                    </div>
                    <Button
                      size='sm'
                      variant='destructive'
                      disabled={isUnassigningStaff}
                      onClick={() =>
                        void handleUnassignStaffFromPostOffice(assignment.id)
                      }
                    >
                      Unassign
                    </Button>
                  </div>
                ))}
              </div>
            )}
          </div>
        </DialogContent>
      </Dialog>

      <ConfirmDialog
        open={Boolean(deleteTarget)}
        onOpenChange={(open) => {
          if (!open && !isDeleting) {
            setDeleteTarget(null);
          }
        }}
        title='Delete post office'
        description={
          deleteTarget
            ? `This will permanently delete post office ${deleteTarget.code} - ${deleteTarget.name}.`
            : 'This action cannot be undone.'
        }
        confirmText='Delete'
        cancelText='Cancel'
        onConfirm={handleDeletePostOffice}
        isLoading={isDeleting}
        variant='destructive'
      />
    </>
  );
};
