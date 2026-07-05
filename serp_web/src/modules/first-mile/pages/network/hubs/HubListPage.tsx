/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Hub list, CRUD, import, and details
 */

'use client';

import React from 'react';
import { getErrorMessage, useAppSelector } from '@/lib/store';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  Input,
  Popover,
  PopoverContent,
  PopoverTrigger,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/shared/components/ui';
import { ConfirmDialog } from '@/shared/components/ui/confirm-dialog';
import { useNotification } from '@/shared/hooks';
import {
  Building2,
  Plus,
  Pencil,
  Trash2,
  Eye,
  ImageUp,
  RefreshCw,
  Search,
  ShieldAlert,
  X,
} from 'lucide-react';
import {
  CoordinatePickerMap,
  TmsCombobox,
  TmsEntityLocationMap,
  type TmsLocationMapPoint,
  type TmsMapBounds,
} from '../../../components';
import {
  useGetHubsQuery,
  useGetProvincesQuery,
  useGetWardsByProvinceCodeQuery,
  useGeocodeAddressMutation,
  useCreateHubMutation,
  useUpdateHubMutation,
  useDeleteHubMutation,
  useUploadHubImageMutation,
  useLazyExportHubTemplateQuery,
  useValidateHubImportMutation,
  useImportHubsMutation,
} from '../../../api';
import type {
  Hub,
  HubStatus,
  HubImportItem,
  HubListFilters,
  ImportHistory,
  ValidateImportFileResponse,
  Ward,
} from '../../../types';
import { HubFormDialog, HubImportCard } from './components';
import {
  buildHubListFilters,
  DEFAULT_HUB_FILTER_FORM,
  type HubFilterFormState,
} from './hubFilterModels';
import {
  buildCreateHubRequest,
  buildUpdateHubRequest,
  DEFAULT_HUB_FORM,
  getHubStatusLabel,
  HUB_STATUS_OPTIONS,
  mapHubToFormState,
  validateHubForm,
  type HubFormMode,
  type HubFormState,
} from './hubForm';

const PAGE_SIZE = 20;
const MAP_PAGE_SIZE = 500;

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

function getHubStatusBadgeVariant(
  status: HubStatus
): 'default' | 'secondary' | 'destructive' | 'outline' {
  switch (status) {
    case 'ACTIVE':
      return 'default';
    case 'INACTIVE':
      return 'secondary';
    case 'MAINTENANCE':
      return 'outline';
    default:
      return 'secondary';
  }
}

const HUB_ACTION_COLUMN_CLASS =
  'sticky right-0 z-10 w-[200px] min-w-[200px] border-l bg-card';
const HUB_ACTION_HEADER_CLASS = `${HUB_ACTION_COLUMN_CLASS} z-20`;

export function HubListPage() {
  const notification = useNotification();
  const roles = useAppSelector(
    (state) => state.account.user.profile?.roles ?? []
  );
  const isTmsAdmin = roles.includes('TMS_ADMIN');

  const [filterFormValues, setFilterFormValues] =
    React.useState<HubFilterFormState>(DEFAULT_HUB_FILTER_FORM);
  const [appliedFilters, setAppliedFilters] = React.useState<HubListFilters>(
    {}
  );
  const [currentPage, setCurrentPage] = React.useState(0);
  const [mapBounds, setMapBounds] = React.useState<TmsMapBounds | null>(null);
  const [openTableFilter, setOpenTableFilter] = React.useState<
    'code' | 'name' | 'status' | 'location' | 'address' | 'hubLoad' | null
  >(null);
  const codeFilterInputRef = React.useRef<HTMLInputElement>(null);
  const nameFilterInputRef = React.useRef<HTMLInputElement>(null);
  const addressFilterInputRef = React.useRef<HTMLInputElement>(null);

  React.useEffect(() => {
    if (
      openTableFilter !== 'code' &&
      openTableFilter !== 'name' &&
      openTableFilter !== 'address'
    ) {
      return;
    }

    window.setTimeout(() => {
      if (openTableFilter === 'code') {
        codeFilterInputRef.current?.focus();
        codeFilterInputRef.current?.select();
      } else if (openTableFilter === 'name') {
        nameFilterInputRef.current?.focus();
        nameFilterInputRef.current?.select();
      } else {
        addressFilterInputRef.current?.focus();
        addressFilterInputRef.current?.select();
      }
    }, 0);
  }, [openTableFilter]);

  const {
    data: hubsData,
    isFetching,
    refetch,
  } = useGetHubsQuery({
    page: currentPage,
    size: PAGE_SIZE,
    ...appliedFilters,
  });

  const {
    data: mapHubsData,
    isFetching: isFetchingMapHubs,
    refetch: refetchMapHubs,
  } = useGetHubsQuery(
    {
      page: 0,
      size: MAP_PAGE_SIZE,
      ...appliedFilters,
      hasLocation: true,
      ...(mapBounds ?? {}),
    },
    { skip: !mapBounds }
  );

  const mapHubPoints = React.useMemo(
    () =>
      (mapHubsData?.items ?? []).flatMap((hub) => {
        if (
          hub.latitude === undefined ||
          hub.latitude === null ||
          hub.longitude === undefined ||
          hub.longitude === null
        ) {
          return [];
        }

        return [
          {
            id: hub.id,
            code: hub.code,
            name: hub.name,
            latitude: hub.latitude,
            longitude: hub.longitude,
            address: hub.addressDetail,
            status: hub.status,
          },
        ];
      }),
    [mapHubsData]
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
      { skip: !selectedFilterProvinceCode }
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

  const updateFilterField = React.useCallback(
    <K extends keyof HubFilterFormState>(
      field: K,
      value: HubFilterFormState[K]
    ) => {
      setFilterFormValues((prev) => ({
        ...prev,
        [field]: value,
      }));
    },
    []
  );

  const hubs = hubsData?.items || [];
  const totalPages = hubsData?.totalPages || 0;
  const hasNext = hubsData?.hasNext || false;
  const hasPrev = hubsData?.hasPrevious || false;

  const [formDialogOpen, setFormDialogOpen] = React.useState(false);
  const [formMode, setFormMode] = React.useState<HubFormMode>('create');
  const [editingHubId, setEditingHubId] = React.useState<number | null>(null);
  const [formValues, setFormValues] =
    React.useState<HubFormState>(DEFAULT_HUB_FORM);

  const [detailHub, setDetailHub] = React.useState<Hub | null>(null);
  const [deleteTarget, setDeleteTarget] = React.useState<Hub | null>(null);

  const [selectedImportFile, setSelectedImportFile] =
    React.useState<File | null>(null);
  const [importFileInputKey, setImportFileInputKey] = React.useState(0);
  const [validateImportResult, setValidateImportResult] =
    React.useState<ValidateImportFileResponse<HubImportItem> | null>(null);
  const [lastImportJob, setLastImportJob] =
    React.useState<ImportHistory | null>(null);

  const imageInputRef = React.useRef<HTMLInputElement | null>(null);
  const [imageUploadHubId, setImageUploadHubId] = React.useState<number | null>(
    null
  );

  const { data: provincesData } = useGetProvincesQuery({
    page: 0,
    size: 500,
  });
  const provinceSelectOptions = React.useMemo(
    () => provincesData?.items ?? [],
    [provincesData]
  );

  const selectedFormProvinceCode = React.useMemo(
    () => formValues.province_code.trim(),
    [formValues.province_code]
  );
  const selectedFormWardCode = React.useMemo(
    () => formValues.ward_code.trim(),
    [formValues.ward_code]
  );

  const { data: wardsForFormData, isFetching: isFetchingWardsForForm } =
    useGetWardsByProvinceCodeQuery(
      { provinceCode: selectedFormProvinceCode, page: 0, size: 1000 },
      { skip: !selectedFormProvinceCode }
    );

  const wardSelectOptions = React.useMemo(() => {
    const options = [...(wardsForFormData?.items ?? [])];
    if (
      selectedFormWardCode &&
      !options.some((w) => w.wardCode === selectedFormWardCode)
    ) {
      options.unshift({
        wardCode: selectedFormWardCode,
        name: selectedFormWardCode,
        provinceCode: selectedFormProvinceCode,
      } as Ward);
    }
    return options;
  }, [selectedFormProvinceCode, selectedFormWardCode, wardsForFormData]);

  const getProvinceLabel = React.useCallback(
    (provinceCode?: string) => {
      if (!provinceCode) {
        return '-';
      }
      const found = provinceSelectOptions.find(
        (p) => p.provinceCode === provinceCode
      );
      return found?.name ?? provinceCode;
    },
    [provinceSelectOptions]
  );

  const [createHub, { isLoading: isCreating }] = useCreateHubMutation();
  const [updateHub, { isLoading: isUpdating }] = useUpdateHubMutation();
  const [deleteHub, { isLoading: isDeleting }] = useDeleteHubMutation();
  const [uploadHubImage, { isLoading: isUploadingImage }] =
    useUploadHubImageMutation();
  const [triggerExportTemplate, { isFetching: isExportingTemplate }] =
    useLazyExportHubTemplateQuery();
  const [validateHubImport, { isLoading: isValidatingImport }] =
    useValidateHubImportMutation();
  const [importHubs, { isLoading: isImportingHubs }] = useImportHubsMutation();
  const [geocodeAddress, { isLoading: isGeocodingAddress }] =
    useGeocodeAddressMutation();

  const updateFormField = React.useCallback(
    <K extends keyof HubFormState>(field: K, value: HubFormState[K]) => {
      setFormValues((prev) => ({ ...prev, [field]: value }));
    },
    []
  );

  const resetImportFileSelection = React.useCallback(() => {
    setSelectedImportFile(null);
    setValidateImportResult(null);
    setImportFileInputKey((k) => k + 1);
  }, []);

  const isImportFlowBusy =
    isExportingTemplate || isValidatingImport || isImportingHubs;

  const openCreateDialog = () => {
    if (!isTmsAdmin) {
      notification.error('Chỉ TMS_ADMIN có thể tạo hub.');
      return;
    }
    setFormMode('create');
    setEditingHubId(null);
    setFormValues(DEFAULT_HUB_FORM);
    setFormDialogOpen(true);
  };

  const openEditDialog = (hub: Hub) => {
    if (!isTmsAdmin) {
      notification.error('Chỉ TMS_ADMIN có thể cập nhật hub.');
      return;
    }
    setFormMode('edit');
    setEditingHubId(hub.id);
    setFormValues(mapHubToFormState(hub));
    setFormDialogOpen(true);
  };

  const handleSubmitHubForm = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!isTmsAdmin) {
      notification.error('Chỉ TMS_ADMIN có thể lưu hub.');
      return;
    }
    const err = validateHubForm(formValues);
    if (err) {
      notification.error(err);
      return;
    }
    try {
      if (formMode === 'create') {
        await createHub(buildCreateHubRequest(formValues)).unwrap();
        notification.success('Tạo hub thành công.');
      } else if (editingHubId != null) {
        await updateHub({
          id: editingHubId,
          body: buildUpdateHubRequest(formValues),
        }).unwrap();
        notification.success('Cập nhật hub thành công.');
      }
      setFormDialogOpen(false);
      refetch();
      if (mapBounds) {
        void refetchMapHubs();
      }
    } catch (error) {
      notification.error('Không thể lưu hub.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleGeocodeHubAddress = React.useCallback(async () => {
    if (!isTmsAdmin) {
      notification.error('Chỉ TMS_ADMIN có thể định vị hub.');
      return;
    }

    const addressDetail = formValues.address_detail.trim();
    if (!addressDetail) {
      notification.error('Cần nhập địa chỉ chi tiết trước khi định vị.');
      return;
    }

    const provinceCode = selectedFormProvinceCode;
    if (!provinceCode) {
      notification.error('Vui lòng chọn tỉnh/thành phố trước khi định vị.');
      return;
    }

    const wardCode = selectedFormWardCode;
    const provinceName =
      provinceSelectOptions.find(
        (province) => province.provinceCode === provinceCode
      )?.name ?? provinceCode;
    const wardName =
      wardSelectOptions.find((ward) => ward.wardCode === wardCode)?.name ??
      wardCode;

    const query = [addressDetail, wardName, provinceName, 'Vietnam']
      .filter((part) => Boolean(part))
      .join(', ');

    try {
      const geocoded = await geocodeAddress({ address: query }).unwrap();
      updateFormField('latitude', String(geocoded.latitude));
      updateFormField('longitude', String(geocoded.longitude));
      notification.success('Đã cập nhật tọa độ từ địa chỉ.');
    } catch (error) {
      notification.error('Không thể định vị địa chỉ hub.', {
        description: getErrorMessage(error),
      });
    }
  }, [
    formValues.address_detail,
    geocodeAddress,
    isTmsAdmin,
    notification,
    provinceSelectOptions,
    selectedFormProvinceCode,
    selectedFormWardCode,
    updateFormField,
    wardSelectOptions,
  ]);

  const handleDeleteHub = async () => {
    if (!deleteTarget || !isTmsAdmin) {
      return;
    }
    try {
      await deleteHub(deleteTarget.id).unwrap();
      notification.success('Xóa hub thành công.');
      setDeleteTarget(null);
      refetch();
      if (mapBounds) {
        void refetchMapHubs();
      }
    } catch (error) {
      notification.error('Không thể xóa hub.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleDownloadTemplate = async () => {
    if (!isTmsAdmin) {
      notification.error('Chỉ TMS_ADMIN có thể tải mẫu hub.');
      return;
    }
    try {
      const blob = await triggerExportTemplate(undefined).unwrap();
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'hub_template.xlsx';
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
      notification.success('Tải mẫu hub thành công.');
    } catch (error) {
      notification.error('Không thể tải mẫu hub.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleValidateImportFile = async () => {
    if (!isTmsAdmin) {
      notification.error('Chỉ TMS_ADMIN có thể kiểm tra dữ liệu nhập hub.');
      return;
    }
    if (!selectedImportFile) {
      notification.error('Vui lòng chọn tệp Excel trước.');
      return;
    }
    try {
      const formData = new FormData();
      formData.append('file', selectedImportFile);
      const result = await validateHubImport(formData).unwrap();
      setValidateImportResult(result);
      if (result.is_success) {
        notification.success('Kiểm tra tệp thành công.');
      }
    } catch (error) {
      notification.error('Không thể kiểm tra tệp nhập hub.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleImportFile = async () => {
    if (!isTmsAdmin) {
      notification.error('Chỉ TMS_ADMIN có thể nhập hub.');
      return;
    }
    if (!selectedImportFile) {
      notification.error('Vui lòng chọn tệp Excel trước.');
      return;
    }
    if (!validateImportResult) {
      notification.error('Vui lòng kiểm tra tệp đã chọn trước khi nhập.');
      return;
    }
    if (!validateImportResult.is_success) {
      return;
    }
    try {
      const formData = new FormData();
      formData.append('file', selectedImportFile);
      const job = await importHubs(formData).unwrap();
      setLastImportJob(job);
      notification.success('Đã tạo tác vụ nhập hub.', {
        description: `Mã nhập: ${job.id}`,
      });
      resetImportFileSelection();
      refetch();
      if (mapBounds) {
        void refetchMapHubs();
      }
    } catch (error) {
      notification.error('Không thể nhập tệp hub.', {
        description: getErrorMessage(error),
      });
    }
  };

  const openHubDetail = React.useCallback((hub: Hub) => {
    setDetailHub(hub);
  }, []);

  const handleMapHubClick = React.useCallback(
    (point: TmsLocationMapPoint) => {
      const hub =
        (mapHubsData?.items ?? []).find((item) => item.id === point.id) ??
        (hubsData?.items ?? []).find((item) => item.id === point.id);

      if (hub) {
        openHubDetail(hub);
      }
    },
    [hubsData?.items, mapHubsData?.items, openHubDetail]
  );

  const triggerHubImagePicker = (hubId: number) => {
    if (!isTmsAdmin) {
      notification.error('Chỉ TMS_ADMIN có thể tải ảnh hub.');
      return;
    }
    setImageUploadHubId(hubId);
    imageInputRef.current?.click();
  };

  const handleHubImageSelected = async (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    const file = event.target.files?.[0];
    event.target.value = '';
    if (!file || imageUploadHubId == null) {
      setImageUploadHubId(null);
      return;
    }
    try {
      await uploadHubImage({ id: imageUploadHubId, file }).unwrap();
      notification.success('Cập nhật ảnh hub thành công.');
      refetch();
    } catch (error) {
      notification.error('Không thể tải ảnh hub.', {
        description: getErrorMessage(error),
      });
    } finally {
      setImageUploadHubId(null);
    }
  };

  const handleApplyFilters = (event: React.FormEvent) => {
    event.preventDefault();

    try {
      const nextFilters = buildHubListFilters(filterFormValues);
      setCurrentPage(0);
      setAppliedFilters(nextFilters);
    } catch (error) {
      notification.error(
        error instanceof Error ? error.message : 'Giá trị lọc không hợp lệ.'
      );
    }
  };

  const handleTableFilterSubmit = (event: React.FormEvent) => {
    handleApplyFilters(event);
    setOpenTableFilter(null);
  };

  const statusOptions = [
    { value: 'ALL', label: 'Tất cả trạng thái' },
    ...HUB_STATUS_OPTIONS,
  ];
  const provinceOptions = [
    { value: 'ALL', label: 'Tất cả tỉnh/thành phố' },
    ...provinceSelectOptions.flatMap((province) =>
      province.provinceCode
        ? [
            {
              value: province.provinceCode,
              label: `${province.name} (${province.provinceCode})`,
            },
          ]
        : []
    ),
  ];
  const wardOptions = [
    { value: 'ALL', label: 'Tất cả phường/xã' },
    ...filterWardOptions.flatMap((ward) =>
      ward.wardCode
        ? [
            {
              value: ward.wardCode,
              label: `${ward.name} (${ward.wardCode})`,
            },
          ]
        : []
    ),
  ];
  const isCodeFilterActive = Boolean(filterFormValues.code.trim());
  const isNameFilterActive = Boolean(filterFormValues.name.trim());
  const isStatusFilterActive = filterFormValues.status !== 'ALL';
  const isLocationFilterActive =
    Boolean(selectedFilterProvinceCode) || Boolean(selectedFilterWardCode);
  const isAddressFilterActive = Boolean(filterFormValues.keyword.trim());
  const isHubLoadFilterActive =
    Boolean(filterFormValues.minCurrentLoad.trim()) ||
    Boolean(filterFormValues.maxCurrentLoad.trim());

  return (
    <>
      <input
        ref={imageInputRef}
        type='file'
        accept='image/*'
        className='hidden'
        onChange={handleHubImageSelected}
      />

      <div className='space-y-6'>
        <div className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between'>
          <div className='flex flex-col gap-2'>
            <h1 className='text-2xl font-bold tracking-tight'>Hub</h1>
            <p className='text-muted-foreground'>
              Quản lý hub trung chuyển, chất lượng vị trí và tồn đơn tại hub.
            </p>
          </div>
          <div className='flex flex-wrap items-center gap-2'>
            <Button
              type='button'
              variant='outline'
              size='sm'
              onClick={() => refetch()}
              disabled={isFetching}
            >
              <RefreshCw
                className={`h-4 w-4 mr-2 ${isFetching ? 'animate-spin' : ''}`}
              />
              Làm mới
            </Button>
            {isTmsAdmin ? (
              <>
                <HubImportCard
                  isTmsAdmin={isTmsAdmin}
                  isImportFlowBusy={isImportFlowBusy}
                  isExportingTemplate={isExportingTemplate}
                  isValidatingImport={isValidatingImport}
                  isImportingHubs={isImportingHubs}
                  importFileInputKey={importFileInputKey}
                  selectedImportFile={selectedImportFile}
                  validateImportResult={validateImportResult}
                  lastImportJob={lastImportJob}
                  onSelectImportFile={(e) => {
                    const file = e.target.files?.[0];
                    setSelectedImportFile(file ?? null);
                    setValidateImportResult(null);
                  }}
                  onDownloadTemplate={handleDownloadTemplate}
                  onValidateImportFile={handleValidateImportFile}
                  onImportFile={handleImportFile}
                />
                <Button type='button' onClick={openCreateDialog}>
                  <Plus className='h-4 w-4 mr-2' />
                  Tạo hub
                </Button>
              </>
            ) : (
              <Badge variant='outline' className='gap-1'>
                <ShieldAlert className='h-3.5 w-3.5' />
                Chỉ xem (cần quyền TMS_ADMIN để thay đổi dữ liệu)
              </Badge>
            )}
          </div>
        </div>

        <TmsEntityLocationMap
          title='Bản đồ hub'
          description='Chỉ tải marker của hub trong khu vực bản đồ đang hiển thị.'
          points={mapHubPoints}
          markerColor='#b45309'
          markerFillColor='#f59e0b'
          loading={!mapBounds || isFetchingMapHubs}
          totalItems={mapHubsData?.totalItems}
          emptyText='Không có hub đã định vị trong khu vực bản đồ này.'
          onBoundsChange={handleMapBoundsChange}
          onPointClick={handleMapHubClick}
        />

        <Card>
          <CardHeader>
            <CardTitle className='flex items-center gap-2'>
              <Building2 className='h-5 w-5' />
              Danh sách hub
            </CardTitle>
          </CardHeader>
          <CardContent className='space-y-4'>
            {isFetching && hubs.length === 0 ? (
              <div className='text-center text-muted-foreground py-8'>
                Đang tải hub...
              </div>
            ) : hubs.length === 0 ? (
              <div className='text-center text-muted-foreground py-8'>
                Không tìm thấy hub
              </div>
            ) : (
              <div className='rounded-md border'>
                <Table className='min-w-[1240px]'>
                  <TableHeader>
                    <TableRow>
                      <TableHead className='w-[150px]'>
                        <Popover
                          open={openTableFilter === 'code'}
                          onOpenChange={(open) =>
                            setOpenTableFilter(open ? 'code' : null)
                          }
                        >
                          <div className='flex items-center gap-1'>
                            <span>Mã hub</span>
                            <PopoverTrigger asChild>
                              <Button
                                type='button'
                                variant={
                                  isCodeFilterActive ? 'outline' : 'ghost'
                                }
                                size='icon'
                                className='size-7'
                                title='Tìm mã hub'
                                aria-label='Tìm mã hub'
                              >
                                <Search className='h-4 w-4' />
                              </Button>
                            </PopoverTrigger>
                          </div>
                          <PopoverContent
                            align='start'
                            sideOffset={8}
                            className='w-72 p-3'
                          >
                            <form
                              className='flex items-center gap-2'
                              onSubmit={handleTableFilterSubmit}
                            >
                              <Input
                                ref={codeFilterInputRef}
                                className='h-9 bg-background'
                                value={filterFormValues.code}
                                onChange={(event) =>
                                  updateFilterField('code', event.target.value)
                                }
                                placeholder='Tìm mã hub...'
                                disabled={isFetching}
                              />
                              <Button
                                type='submit'
                                variant='outline'
                                size='icon'
                                className='size-9 shrink-0'
                                disabled={isFetching}
                                title='Tìm kiếm'
                                aria-label='Tìm mã hub'
                              >
                                <Search className='h-4 w-4' />
                              </Button>
                              {isCodeFilterActive ? (
                                <Button
                                  type='button'
                                  variant='ghost'
                                  size='icon'
                                  className='size-9 shrink-0'
                                  disabled={isFetching}
                                  onClick={() => updateFilterField('code', '')}
                                  title='Xóa tìm kiếm'
                                  aria-label='Xóa tìm kiếm mã hub'
                                >
                                  <X className='h-4 w-4' />
                                </Button>
                              ) : null}
                            </form>
                          </PopoverContent>
                        </Popover>
                      </TableHead>
                      <TableHead className='w-[220px]'>
                        <Popover
                          open={openTableFilter === 'name'}
                          onOpenChange={(open) =>
                            setOpenTableFilter(open ? 'name' : null)
                          }
                        >
                          <div className='flex items-center gap-1'>
                            <span>Tên hub</span>
                            <PopoverTrigger asChild>
                              <Button
                                type='button'
                                variant={
                                  isNameFilterActive ? 'outline' : 'ghost'
                                }
                                size='icon'
                                className='size-7'
                                title='Tìm tên hub'
                                aria-label='Tìm tên hub'
                              >
                                <Search className='h-4 w-4' />
                              </Button>
                            </PopoverTrigger>
                          </div>
                          <PopoverContent
                            align='start'
                            sideOffset={8}
                            className='w-72 p-3'
                          >
                            <form
                              className='flex items-center gap-2'
                              onSubmit={handleTableFilterSubmit}
                            >
                              <Input
                                ref={nameFilterInputRef}
                                className='h-9 bg-background'
                                value={filterFormValues.name}
                                onChange={(event) =>
                                  updateFilterField('name', event.target.value)
                                }
                                placeholder='Tìm tên hub...'
                                disabled={isFetching}
                              />
                              <Button
                                type='submit'
                                variant='outline'
                                size='icon'
                                className='size-9 shrink-0'
                                disabled={isFetching}
                                title='Tìm kiếm'
                                aria-label='Tìm tên hub'
                              >
                                <Search className='h-4 w-4' />
                              </Button>
                              {isNameFilterActive ? (
                                <Button
                                  type='button'
                                  variant='ghost'
                                  size='icon'
                                  className='size-9 shrink-0'
                                  disabled={isFetching}
                                  onClick={() => updateFilterField('name', '')}
                                  title='Xóa tìm kiếm'
                                  aria-label='Xóa tìm kiếm tên hub'
                                >
                                  <X className='h-4 w-4' />
                                </Button>
                              ) : null}
                            </form>
                          </PopoverContent>
                        </Popover>
                      </TableHead>
                      <TableHead className='w-[130px]'>
                        <Popover
                          open={openTableFilter === 'status'}
                          onOpenChange={(open) =>
                            setOpenTableFilter(open ? 'status' : null)
                          }
                        >
                          <div className='flex items-center gap-1'>
                            <span>Trạng thái</span>
                            <PopoverTrigger asChild>
                              <Button
                                type='button'
                                variant={
                                  isStatusFilterActive ? 'outline' : 'ghost'
                                }
                                size='icon'
                                className='size-7'
                                title='Lọc trạng thái'
                                aria-label='Lọc trạng thái'
                              >
                                <Search className='h-4 w-4' />
                              </Button>
                            </PopoverTrigger>
                          </div>
                          <PopoverContent
                            align='start'
                            sideOffset={8}
                            className='w-60 p-3'
                          >
                            <form
                              className='space-y-3'
                              onSubmit={handleTableFilterSubmit}
                            >
                              <TmsCombobox
                                id='hub-table-filter-status'
                                value={filterFormValues.status}
                                onValueChange={(value) =>
                                  updateFilterField(
                                    'status',
                                    value as HubFilterFormState['status']
                                  )
                                }
                                options={statusOptions}
                                placeholder='Tất cả trạng thái'
                                emptyText='Không tìm thấy trạng thái'
                                disabled={isFetching}
                              />
                              <div className='flex justify-end gap-2'>
                                {isStatusFilterActive ? (
                                  <Button
                                    type='button'
                                    variant='ghost'
                                    size='sm'
                                    disabled={isFetching}
                                    onClick={() =>
                                      updateFilterField('status', 'ALL')
                                    }
                                  >
                                    Xóa
                                  </Button>
                                ) : null}
                                <Button
                                  type='submit'
                                  variant='outline'
                                  size='sm'
                                  disabled={isFetching}
                                >
                                  Tìm kiếm
                                </Button>
                              </div>
                            </form>
                          </PopoverContent>
                        </Popover>
                      </TableHead>
                      <TableHead className='w-[220px]'>
                        <Popover
                          open={openTableFilter === 'location'}
                          onOpenChange={(open) =>
                            setOpenTableFilter(open ? 'location' : null)
                          }
                        >
                          <div className='flex items-center gap-1'>
                            <span>Tỉnh/Phường xã</span>
                            <PopoverTrigger asChild>
                              <Button
                                type='button'
                                variant={
                                  isLocationFilterActive ? 'outline' : 'ghost'
                                }
                                size='icon'
                                className='size-7'
                                title='Lọc tỉnh/phường xã'
                                aria-label='Lọc tỉnh/phường xã'
                              >
                                <Search className='h-4 w-4' />
                              </Button>
                            </PopoverTrigger>
                          </div>
                          <PopoverContent
                            align='start'
                            sideOffset={8}
                            className='w-80 p-3'
                          >
                            <form
                              className='space-y-3'
                              onSubmit={handleTableFilterSubmit}
                            >
                              <TmsCombobox
                                id='hub-table-filter-province'
                                value={selectedFilterProvinceCode || 'ALL'}
                                onValueChange={(value) => {
                                  const nextProvinceCode =
                                    value === 'ALL' ? '' : value;
                                  updateFilterField(
                                    'provinceCode',
                                    nextProvinceCode
                                  );
                                  updateFilterField('wardCode', '');
                                }}
                                options={provinceOptions}
                                placeholder='Tất cả tỉnh/thành phố'
                                emptyText='Không tìm thấy tỉnh/thành phố'
                                disabled={isFetching}
                              />
                              <TmsCombobox
                                id='hub-table-filter-ward'
                                value={selectedFilterWardCode || 'ALL'}
                                onValueChange={(value) =>
                                  updateFilterField(
                                    'wardCode',
                                    value === 'ALL' ? '' : value
                                  )
                                }
                                options={wardOptions}
                                placeholder={
                                  selectedFilterProvinceCode
                                    ? 'Tất cả phường/xã'
                                    : 'Chọn tỉnh trước'
                                }
                                emptyText={
                                  isFetchingWardsForFilter
                                    ? 'Đang tải phường/xã...'
                                    : 'Không có phường/xã'
                                }
                                disabled={
                                  !selectedFilterProvinceCode || isFetching
                                }
                                loading={isFetchingWardsForFilter}
                              />
                              <div className='flex justify-end gap-2'>
                                {isLocationFilterActive ? (
                                  <Button
                                    type='button'
                                    variant='ghost'
                                    size='sm'
                                    disabled={isFetching}
                                    onClick={() => {
                                      updateFilterField('provinceCode', '');
                                      updateFilterField('wardCode', '');
                                    }}
                                  >
                                    Xóa
                                  </Button>
                                ) : null}
                                <Button
                                  type='submit'
                                  variant='outline'
                                  size='sm'
                                  disabled={isFetching}
                                >
                                  Tìm kiếm
                                </Button>
                              </div>
                            </form>
                          </PopoverContent>
                        </Popover>
                      </TableHead>
                      <TableHead className='w-[280px]'>
                        <Popover
                          open={openTableFilter === 'address'}
                          onOpenChange={(open) =>
                            setOpenTableFilter(open ? 'address' : null)
                          }
                        >
                          <div className='flex items-center gap-1'>
                            <span>Địa chỉ</span>
                            <PopoverTrigger asChild>
                              <Button
                                type='button'
                                variant={
                                  isAddressFilterActive ? 'outline' : 'ghost'
                                }
                                size='icon'
                                className='size-7'
                                title='Tìm địa chỉ'
                                aria-label='Tìm địa chỉ'
                              >
                                <Search className='h-4 w-4' />
                              </Button>
                            </PopoverTrigger>
                          </div>
                          <PopoverContent
                            align='start'
                            sideOffset={8}
                            className='w-72 p-3'
                          >
                            <form
                              className='flex items-center gap-2'
                              onSubmit={handleTableFilterSubmit}
                            >
                              <Input
                                ref={addressFilterInputRef}
                                className='h-9 bg-background'
                                value={filterFormValues.keyword}
                                onChange={(event) =>
                                  updateFilterField(
                                    'keyword',
                                    event.target.value
                                  )
                                }
                                placeholder='Tìm địa chỉ...'
                                disabled={isFetching}
                              />
                              <Button
                                type='submit'
                                variant='outline'
                                size='icon'
                                className='size-9 shrink-0'
                                disabled={isFetching}
                                title='Tìm kiếm'
                                aria-label='Tìm địa chỉ'
                              >
                                <Search className='h-4 w-4' />
                              </Button>
                              {isAddressFilterActive ? (
                                <Button
                                  type='button'
                                  variant='ghost'
                                  size='icon'
                                  className='size-9 shrink-0'
                                  disabled={isFetching}
                                  onClick={() =>
                                    updateFilterField('keyword', '')
                                  }
                                  title='Xóa tìm kiếm'
                                  aria-label='Xóa tìm kiếm địa chỉ'
                                >
                                  <X className='h-4 w-4' />
                                </Button>
                              ) : null}
                            </form>
                          </PopoverContent>
                        </Popover>
                      </TableHead>
                      <TableHead className='w-[130px]'>
                        <Popover
                          open={openTableFilter === 'hubLoad'}
                          onOpenChange={(open) =>
                            setOpenTableFilter(open ? 'hubLoad' : null)
                          }
                        >
                          <div className='flex items-center gap-1'>
                            <span>Tải hub</span>
                            <PopoverTrigger asChild>
                              <Button
                                type='button'
                                variant={
                                  isHubLoadFilterActive ? 'outline' : 'ghost'
                                }
                                size='icon'
                                className='size-7'
                                title='Lọc tải hub'
                                aria-label='Lọc tải hub'
                              >
                                <Search className='h-4 w-4' />
                              </Button>
                            </PopoverTrigger>
                          </div>
                          <PopoverContent
                            align='start'
                            sideOffset={8}
                            className='w-72 p-3'
                          >
                            <form
                              className='space-y-3'
                              onSubmit={handleTableFilterSubmit}
                            >
                              <div className='grid grid-cols-2 gap-2'>
                                <Input
                                  type='number'
                                  min={0}
                                  step={1}
                                  className='h-9 bg-background'
                                  value={filterFormValues.minCurrentLoad}
                                  onChange={(event) =>
                                    updateFilterField(
                                      'minCurrentLoad',
                                      event.target.value
                                    )
                                  }
                                  placeholder='Từ'
                                  disabled={isFetching}
                                />
                                <Input
                                  type='number'
                                  min={0}
                                  step={1}
                                  className='h-9 bg-background'
                                  value={filterFormValues.maxCurrentLoad}
                                  onChange={(event) =>
                                    updateFilterField(
                                      'maxCurrentLoad',
                                      event.target.value
                                    )
                                  }
                                  placeholder='Đến'
                                  disabled={isFetching}
                                />
                              </div>
                              <div className='flex justify-end gap-2'>
                                {isHubLoadFilterActive ? (
                                  <Button
                                    type='button'
                                    variant='ghost'
                                    size='sm'
                                    disabled={isFetching}
                                    onClick={() => {
                                      updateFilterField('minCurrentLoad', '');
                                      updateFilterField('maxCurrentLoad', '');
                                    }}
                                  >
                                    Xóa
                                  </Button>
                                ) : null}
                                <Button
                                  type='submit'
                                  variant='outline'
                                  size='sm'
                                  disabled={isFetching}
                                >
                                  Tìm kiếm
                                </Button>
                              </div>
                            </form>
                          </PopoverContent>
                        </Popover>
                      </TableHead>
                      <TableHead className='w-[180px]'>Tọa độ</TableHead>
                      <TableHead className={HUB_ACTION_HEADER_CLASS}>
                        <span className='flex justify-end'>Thao tác</span>
                      </TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {hubs.map((hub) => {
                      const hasCoordinates =
                        hub.latitude !== undefined &&
                        hub.latitude !== null &&
                        hub.longitude !== undefined &&
                        hub.longitude !== null;

                      return (
                        <TableRow key={hub.id}>
                          <TableCell className='font-mono text-xs'>
                            {hub.code}
                          </TableCell>
                          <TableCell className='font-medium'>
                            {hub.name}
                            {hub.phoneNumber ? (
                              <p className='text-xs font-normal text-muted-foreground'>
                                {hub.phoneNumber}
                              </p>
                            ) : null}
                          </TableCell>
                          <TableCell>
                            <Badge
                              variant={getHubStatusBadgeVariant(hub.status)}
                            >
                              {getHubStatusLabel(hub.status)}
                            </Badge>
                          </TableCell>
                          <TableCell>
                            <div className='space-y-1'>
                              <p>{getProvinceLabel(hub.provinceCode)}</p>
                              <p className='text-xs text-muted-foreground'>
                                {hub.wardCode || '--'}
                              </p>
                            </div>
                          </TableCell>
                          <TableCell className='max-w-[280px] whitespace-normal'>
                            <span className='line-clamp-2'>
                              {hub.addressDetail || '--'}
                            </span>
                          </TableCell>
                          <TableCell>
                            {hub.currentLoad ?? 0} / {hub.dailyCapacity ?? '--'}
                          </TableCell>
                          <TableCell className='font-mono text-xs'>
                            {hasCoordinates
                              ? `${hub.latitude}, ${hub.longitude}`
                              : '--'}
                          </TableCell>
                          <TableCell className={HUB_ACTION_COLUMN_CLASS}>
                            <div className='flex justify-end gap-2'>
                              <Tooltip>
                                <TooltipTrigger asChild>
                                  <Button
                                    type='button'
                                    variant='outline'
                                    size='icon'
                                    aria-label={`Xem chi tiết ${hub.code}`}
                                    onClick={() => openHubDetail(hub)}
                                  >
                                    <Eye className='h-4 w-4' />
                                  </Button>
                                </TooltipTrigger>
                                <TooltipContent>Chi tiết</TooltipContent>
                              </Tooltip>

                              {isTmsAdmin ? (
                                <>
                                  <Tooltip>
                                    <TooltipTrigger asChild>
                                      <Button
                                        type='button'
                                        variant='outline'
                                        size='icon'
                                        aria-label={`Sửa ${hub.code}`}
                                        onClick={() => openEditDialog(hub)}
                                      >
                                        <Pencil className='h-4 w-4' />
                                      </Button>
                                    </TooltipTrigger>
                                    <TooltipContent>Sửa</TooltipContent>
                                  </Tooltip>
                                  <Tooltip>
                                    <TooltipTrigger asChild>
                                      <Button
                                        type='button'
                                        variant='outline'
                                        size='icon'
                                        aria-label={`Tải ảnh cho ${hub.code}`}
                                        onClick={() =>
                                          triggerHubImagePicker(hub.id)
                                        }
                                        disabled={isUploadingImage}
                                      >
                                        <ImageUp className='h-4 w-4' />
                                      </Button>
                                    </TooltipTrigger>
                                    <TooltipContent>Ảnh</TooltipContent>
                                  </Tooltip>
                                  <Tooltip>
                                    <TooltipTrigger asChild>
                                      <Button
                                        type='button'
                                        variant='destructive'
                                        size='icon'
                                        aria-label={`Xóa ${hub.code}`}
                                        onClick={() => setDeleteTarget(hub)}
                                      >
                                        <Trash2 className='h-4 w-4' />
                                      </Button>
                                    </TooltipTrigger>
                                    <TooltipContent>Xóa</TooltipContent>
                                  </Tooltip>
                                </>
                              ) : null}
                            </div>
                          </TableCell>
                        </TableRow>
                      );
                    })}
                  </TableBody>
                </Table>
              </div>
            )}

            {totalPages > 1 && (
              <div className='flex items-center justify-between border-t pt-4'>
                <div className='text-sm text-muted-foreground'>
                  Trang {currentPage + 1} / {totalPages}
                </div>
                <div className='flex gap-2'>
                  <Button
                    variant='outline'
                    size='sm'
                    disabled={!hasPrev || isFetching}
                    onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
                  >
                    Trước
                  </Button>
                  <Button
                    variant='outline'
                    size='sm'
                    disabled={!hasNext || isFetching}
                    onClick={() => setCurrentPage((p) => p + 1)}
                  >
                    Sau
                  </Button>
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      <HubFormDialog
        open={formDialogOpen}
        formMode={formMode}
        isSaving={isCreating || isUpdating}
        isGeocodingAddress={isGeocodingAddress}
        formValues={formValues}
        selectedProvinceCode={selectedFormProvinceCode}
        selectedWardCode={selectedFormWardCode}
        provinceSelectOptions={provinceSelectOptions}
        wardSelectOptions={wardSelectOptions}
        isFetchingWardsForForm={isFetchingWardsForForm}
        onOpenChange={setFormDialogOpen}
        onSubmit={handleSubmitHubForm}
        onGeocodeAddress={handleGeocodeHubAddress}
        updateFormField={updateFormField}
      />

      <Dialog
        open={Boolean(detailHub)}
        onOpenChange={(open) => {
          if (!open) {
            setDetailHub(null);
          }
        }}
      >
        <DialogContent className='max-w-5xl max-h-[90vh] overflow-y-auto'>
          <DialogHeader>
            <DialogTitle>Chi tiết hub</DialogTitle>
            <DialogDescription>
              Thông tin chi tiết và vị trí bản đồ của hub đã chọn.
            </DialogDescription>
          </DialogHeader>
          {detailHub && (
            <div className='mt-4 space-y-4'>
              {detailHub.imageUrl && (
                // eslint-disable-next-line @next/next/no-img-element
                <img
                  src={detailHub.imageUrl}
                  alt=''
                  className='w-full rounded-md border max-h-48 object-cover'
                />
              )}
              <div className='grid gap-3 text-sm md:grid-cols-2'>
                <div>
                  <p className='text-muted-foreground'>Mã hub</p>
                  <p className='font-medium'>{detailHub.code}</p>
                </div>
                <div>
                  <p className='text-muted-foreground'>Tên hub</p>
                  <p className='font-medium'>{detailHub.name}</p>
                </div>
                <p>
                  <span className='font-medium'>Trạng thái:</span>{' '}
                  {getHubStatusLabel(detailHub.status)}
                </p>
                <p>
                  <span className='font-medium'>Tỉnh/Phường xã:</span>{' '}
                  {getProvinceLabel(detailHub.provinceCode)} /{' '}
                  {detailHub.wardCode}
                </p>
                <p>
                  <span className='font-medium'>Địa chỉ:</span>{' '}
                  {detailHub.addressDetail}
                </p>
                {detailHub.phoneNumber && (
                  <p>
                    <span className='font-medium'>Số điện thoại:</span>{' '}
                    {detailHub.phoneNumber}
                  </p>
                )}
                <p>
                  <span className='font-medium'>Tải hub:</span>{' '}
                  {detailHub.currentLoad ?? 0}/{detailHub.dailyCapacity ?? 0}
                </p>
                {(detailHub.latitude != null ||
                  detailHub.longitude != null) && (
                  <p>
                    <span className='font-medium'>Tọa độ:</span>{' '}
                    {detailHub.latitude}, {detailHub.longitude}
                  </p>
                )}
              </div>
              <div className='space-y-2'>
                <p className='text-sm font-medium'>Bản đồ</p>
                {detailHub.latitude !== undefined &&
                detailHub.latitude !== null &&
                detailHub.longitude !== undefined &&
                detailHub.longitude !== null ? (
                  <CoordinatePickerMap
                    latitude={detailHub.latitude}
                    longitude={detailHub.longitude}
                    disabled
                    className='h-72'
                    onChange={() => {
                      // Bản đồ chỉ xem trong modal chi tiết.
                    }}
                  />
                ) : (
                  <p className='text-sm text-muted-foreground'>
                    Hub này chưa có tọa độ định vị.
                  </p>
                )}
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>

      <ConfirmDialog
        open={Boolean(deleteTarget)}
        onOpenChange={(open) => {
          if (!open && !isDeleting) {
            setDeleteTarget(null);
          }
        }}
        title='Xóa hub'
        description={
          deleteTarget
            ? `Thao tác này sẽ xóa vĩnh viễn hub ${deleteTarget.code} - ${deleteTarget.name}.`
            : 'Thao tác này không thể hoàn tác.'
        }
        confirmText='Xóa'
        cancelText='Hủy'
        onConfirm={handleDeleteHub}
        isLoading={isDeleting}
        variant='destructive'
      />
    </>
  );
}
