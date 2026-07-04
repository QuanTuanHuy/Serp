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
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui';
import { useNotification } from '@/shared/hooks';
import { ConfirmDialog } from '@/shared/components/ui/confirm-dialog';
import { Plus, ShieldAlert } from 'lucide-react';
import {
  useCreatePostOfficeMutation,
  useDeletePostOfficeMutation,
  useGetOrdersQuery,
  useGetPostOfficesQuery,
  useGetWardsByProvinceCodeQuery,
  useImportPostOfficesMutation,
  useLazyExportPostOfficeTemplateQuery,
  useUpdatePostOfficeMutation,
  useUploadPostOfficeImageMutation,
  useValidatePostOfficeImportMutation,
} from '../../../api';
import {
  CoordinatePickerMap,
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
  ValidateImportFileResponse,
  Ward,
} from '../../../types';
import {
  PostOfficeFormDialog,
  PostOfficeImportCard,
  PostOfficeResultsCard,
} from './components';
import {
  buildPostOfficeListFilters,
  DEFAULT_POST_OFFICE_FILTER_FORM,
  type PostOfficeFilterFormState,
} from './postOfficeFilterModels';
import {
  buildCreatePostOfficeRequest,
  DEFAULT_POST_OFFICE_FORM,
  formatPostOfficeStatusLabel,
  getStatusBadgeVariant,
  mapPostOfficeToFormState,
  type PostOfficeFormMode,
  type PostOfficeFormState,
  validatePostOfficeForm,
} from './postOfficeForm';
import { usePostOfficeLocations } from './usePostOfficeLocations';

const DEFAULT_PAGE_SIZE = 20;
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
  const [pageSize, setPageSize] = React.useState(DEFAULT_PAGE_SIZE);
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

  const { data, isLoading, isFetching, refetch } = useGetPostOfficesQuery({
    page,
    size: pageSize,
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

  const [createPostOffice, { isLoading: isCreating }] =
    useCreatePostOfficeMutation();
  const [updatePostOffice, { isLoading: isUpdating }] =
    useUpdatePostOfficeMutation();
  const [uploadPostOfficeImage, { isLoading: isUploadingImage }] =
    useUploadPostOfficeImageMutation();
  const [deletePostOffice, { isLoading: isDeleting }] =
    useDeletePostOfficeMutation();
  const [triggerExportPostOfficeTemplate, { isFetching: isExportingTemplate }] =
    useLazyExportPostOfficeTemplateQuery();
  const [validatePostOfficeImport, { isLoading: isValidatingImport }] =
    useValidatePostOfficeImportMutation();
  const [importPostOfficeFile, { isLoading: isImportingPostOffices }] =
    useImportPostOfficesMutation();

  const isSaving = isCreating || isUpdating;
  const isImportFlowBusy =
    isExportingTemplate || isValidatingImport || isImportingPostOffices;

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
        error instanceof Error
          ? error.message
          : 'Giá trị tìm kiếm không hợp lệ.'
      );
    }
  };

  const handleClearFilters = () => {
    setFilterFormValues(DEFAULT_POST_OFFICE_FILTER_FORM);
    setAppliedFilters({});
    setPage(0);
  };

  const handlePageSizeChange = (nextPageSize: number) => {
    setPageSize(nextPageSize);
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
      notification.error('Chỉ TMS_ADMIN được tải mẫu bưu cục.');
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

      notification.success('Đã tải mẫu bưu cục.');
    } catch (error) {
      notification.error('Không thể tải mẫu bưu cục.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleValidateImportFile = async () => {
    if (!isTmsAdmin) {
      notification.error('Chỉ TMS_ADMIN được kiểm tra file nhập bưu cục.');
      return;
    }

    const formData = buildImportFormData();
    if (!formData) {
      notification.error('Vui lòng chọn file Excel trước.');
      return;
    }

    try {
      const result = await validatePostOfficeImport(formData).unwrap();
      setValidateImportResult(result);

      if (result.is_success) {
        notification.success('File hợp lệ.', {
          description: `${result.data.length} dòng sẵn sàng nhập.`,
        });
      }
    } catch (error) {
      notification.error('Không thể kiểm tra file nhập bưu cục.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleImportFile = async () => {
    if (!isTmsAdmin) {
      notification.error('Chỉ TMS_ADMIN được nhập bưu cục.');
      return;
    }

    if (!validateImportResult) {
      notification.error('Vui lòng kiểm tra file đã chọn trước khi nhập.');
      return;
    }

    if (!validateImportResult.is_success) {
      return;
    }

    const formData = buildImportFormData();
    if (!formData) {
      notification.error('Vui lòng chọn file Excel trước.');
      return;
    }

    try {
      const importResult = await importPostOfficeFile(formData).unwrap();
      setLastImportJob(importResult);
      resetImportFileSelection();

      notification.success('Đã tạo tác vụ nhập bưu cục.', {
        description: `Tác vụ #${importResult.id} đang ở trạng thái ${importResult.status}.`,
      });

      void refetch();
      if (mapBounds) {
        void refetchMapPostOffices();
      }
    } catch (error) {
      notification.error('Không thể nhập file bưu cục.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleOpenCreateDialog = () => {
    if (!isTmsAdmin) {
      notification.error('Chỉ TMS_ADMIN được tạo bưu cục.');
      return;
    }

    setFormMode('create');
    setEditingId(null);
    setFormValues(DEFAULT_POST_OFFICE_FORM);
    setIsFormDialogOpen(true);
  };

  const handleOpenEditDialog = (postOffice: PostOffice) => {
    if (!isTmsAdmin) {
      notification.error('Chỉ TMS_ADMIN được cập nhật bưu cục.');
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
      notification.error('Chỉ TMS_ADMIN được thay đổi bưu cục.');
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
        notification.success('Đã tạo bưu cục.');

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
          notification.error('Thiếu mã định danh bưu cục để cập nhật.');
          return;
        }

        await updatePostOffice({
          id: editingId,
          body: payload,
        }).unwrap();

        notification.success('Đã cập nhật bưu cục.');
        void refetch();
        if (mapBounds) {
          void refetchMapPostOffices();
        }
      }

      setIsFormDialogOpen(false);
      setEditingId(null);
    } catch (error) {
      notification.error('Không thể lưu bưu cục.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleRequestDelete = (postOffice: PostOffice) => {
    if (!isTmsAdmin) {
      notification.error('Chỉ TMS_ADMIN được xóa bưu cục.');
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

  const handleUploadImage = async (postOffice: PostOffice, file: File) => {
    if (!isTmsAdmin) {
      notification.error('Chỉ TMS_ADMIN được tải ảnh bưu cục.');
      return;
    }

    try {
      await uploadPostOfficeImage({ id: postOffice.id, file }).unwrap();
      notification.success('Đã tải ảnh bưu cục.');
      void refetch();
      if (mapBounds) {
        void refetchMapPostOffices();
      }
    } catch (error) {
      notification.error('Không thể tải ảnh bưu cục.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleDeletePostOffice = async () => {
    if (!deleteTarget) {
      return;
    }

    if (!isTmsAdmin) {
      notification.error('Chỉ TMS_ADMIN được xóa bưu cục.');
      return;
    }

    try {
      await deletePostOffice(deleteTarget.id).unwrap();
      notification.success('Đã xóa bưu cục.');
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
      notification.error('Không thể xóa bưu cục.', {
        description: getErrorMessage(error),
      });
    }
  };

  return (
    <>
      <div className='space-y-6'>
        <div className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between'>
          <div className='flex flex-col gap-2'>
            <h1 className='text-2xl font-bold tracking-tight'>Bưu cục</h1>
            <p className='text-muted-foreground'>
              Quản lý bưu cục và theo dõi chất lượng tọa độ vị trí.
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
                Thêm bưu cục
              </Button>
            </div>
          ) : (
            <Badge variant='outline' className='gap-1'>
              <ShieldAlert className='h-3.5 w-3.5' />
              Chỉ xem (thao tác ghi cần TMS_ADMIN)
            </Badge>
          )}
        </div>

        <TmsEntityLocationMap
          title='Bản đồ bưu cục'
          description='Chỉ tải marker của các bưu cục nằm trong vùng bản đồ đang hiển thị.'
          points={mapPostOfficePoints}
          markerColor='#1d4ed8'
          markerFillColor='#2563eb'
          loading={!mapBounds || isFetchingMapPostOffices}
          totalItems={mapPostOfficeData?.totalItems}
          emptyText='Không có bưu cục đã định vị trong vùng bản đồ này.'
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
          isUploadingImage={isUploadingImage}
          filterFormValues={filterFormValues}
          provinceSelectOptions={provinceSelectOptions}
          filterWardOptions={filterWardOptions}
          selectedFilterProvinceCode={selectedFilterProvinceCode}
          selectedFilterWardCode={selectedFilterWardCode}
          isFetchingWardsForFilter={isFetchingWardsForFilter}
          onViewDetails={handleOpenDetail}
          onEdit={handleOpenEditDialog}
          onUploadImage={handleUploadImage}
          onDelete={handleRequestDelete}
          onFilterFieldChange={updateFilterField}
          onSearchSubmit={handleApplyFilters}
          onClearSearch={handleClearFilters}
          pageSize={pageSize}
          onPageSizeChange={handlePageSizeChange}
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
            <DialogTitle>Chi tiết bưu cục</DialogTitle>
            <DialogDescription>
              Thông tin chi tiết và vị trí bản đồ của bưu cục đã chọn.
            </DialogDescription>
          </DialogHeader>

          {detailTarget ? (
            <Tabs
              value={detailTab}
              onValueChange={(value) => setDetailTab(value as typeof detailTab)}
            >
              <TabsList>
                <TabsTrigger value='details'>Chi tiết</TabsTrigger>
                <TabsTrigger value='orders'>Đơn hàng</TabsTrigger>
              </TabsList>
              <TabsContent value='details' className='mt-4'>
                <div className='space-y-4'>
                  <div className='grid gap-3 md:grid-cols-2 text-sm'>
                    <div>
                      <p className='text-muted-foreground'>Mã bưu cục</p>
                      <p className='font-medium'>{detailTarget.code}</p>
                    </div>
                    <div>
                      <p className='text-muted-foreground'>Tên bưu cục</p>
                      <p className='font-medium'>{detailTarget.name}</p>
                    </div>
                    <div>
                      <p className='text-muted-foreground'>Trạng thái</p>
                      <Badge
                        variant={getStatusBadgeVariant(detailTarget.status)}
                      >
                        {formatPostOfficeStatusLabel(detailTarget.status)}
                      </Badge>
                    </div>
                    <div>
                      <p className='text-muted-foreground'>Số điện thoại</p>
                      <p className='font-medium'>
                        {detailTarget.phoneNumber || '--'}
                      </p>
                    </div>
                    <div className='md:col-span-2'>
                      <p className='text-muted-foreground'>Địa chỉ</p>
                      <p className='font-medium'>
                        {detailTarget.addressDetail}
                      </p>
                    </div>
                    <div>
                      <p className='text-muted-foreground'>Tỉnh/Phường xã</p>
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
                        Bán kính phục vụ (m)
                      </p>
                      <p className='font-medium'>
                        {detailTarget.serviceRadiusM}
                      </p>
                    </div>
                    <div>
                      <p className='text-muted-foreground'>Sức chứa lấy hàng</p>
                      <p className='font-medium'>
                        {detailTarget.dailyCapacity ?? '--'}
                      </p>
                    </div>
                    <div>
                      <p className='text-muted-foreground'>
                        Tải lấy hàng hiện tại
                      </p>
                      <p className='font-medium'>
                        {detailTarget.currentLoad ?? '--'}
                      </p>
                    </div>
                    <div>
                      <p className='text-muted-foreground'>
                        Sức chứa giao hàng
                      </p>
                      <p className='font-medium'>
                        {detailTarget.deliveryCapacity ?? '--'}
                      </p>
                    </div>
                    <div>
                      <p className='text-muted-foreground'>
                        Tải giao hàng hiện tại
                      </p>
                      <p className='font-medium'>
                        {detailTarget.currentDeliveryLoad ?? '--'}
                      </p>
                    </div>
                    <div>
                      <p className='text-muted-foreground'>Độ ưu tiên</p>
                      <p className='font-medium'>
                        {detailTarget.priority ?? '--'}
                      </p>
                    </div>
                    <div>
                      <p className='text-muted-foreground'>Tọa độ</p>
                      <p className='font-medium'>
                        {detailTarget.latitude ?? '--'},{' '}
                        {detailTarget.longitude ?? '--'}
                      </p>
                    </div>
                  </div>

                  <div className='space-y-2'>
                    <p className='text-sm font-medium'>Bản đồ</p>
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
                        Bưu cục này chưa có tọa độ định vị.
                      </p>
                    )}
                  </div>
                </div>
              </TabsContent>
              <TabsContent value='orders' className='mt-4'>
                <TmsEntityOrdersTab
                  data={detailOrdersData}
                  isFetching={isFetchingDetailOrders}
                  page={detailOrdersPage}
                  emptyText='Hiện không có đơn hàng tại bưu cục này.'
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

      <ConfirmDialog
        open={Boolean(deleteTarget)}
        onOpenChange={(open) => {
          if (!open && !isDeleting) {
            setDeleteTarget(null);
          }
        }}
        title='Xóa bưu cục'
        description={
          deleteTarget
            ? `Bưu cục ${deleteTarget.code} - ${deleteTarget.name} sẽ bị xóa vĩnh viễn.`
            : 'Không thể hoàn tác thao tác này.'
        }
        confirmText='Xóa'
        cancelText='Hủy'
        onConfirm={handleDeletePostOffice}
        isLoading={isDeleting}
        variant='destructive'
      />
    </>
  );
};
