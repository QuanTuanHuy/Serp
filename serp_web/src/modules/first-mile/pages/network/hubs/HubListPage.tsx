/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Hub list, CRUD, import, and staff
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
  Label,
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
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
  ShieldAlert,
  UserCog,
} from 'lucide-react';
import type { TmsFilterMode } from '../../../components/list';
import {
  TmsCombobox,
  TmsEntityLocationMap,
  TmsEntityOrdersTab,
  type TmsLocationMapPoint,
  type TmsMapBounds,
} from '../../../components';
import {
  useGetHubsQuery,
  useGetHubPostOfficesQuery,
  useGetOrdersQuery,
  useGetProvincesQuery,
  useGetWardsByProvinceCodeQuery,
  useGeocodeAddressMutation,
  useGetSecondMileHubStaffAssignmentsQuery,
  useGetSecondMileAssignableStaffsQuery,
  useAssignSecondMileStaffToHubMutation,
  useUnassignSecondMileHubStaffAssignmentMutation,
  useCreateHubMutation,
  useUpdateHubMutation,
  useDeleteHubMutation,
  useUploadHubImageMutation,
  useLazyExportHubTemplateQuery,
  useValidateHubImportMutation,
  useImportHubsMutation,
} from '../../../api';
import type {
  FirstMileOrderStatus,
  Hub,
  HubStatus,
  HubImportItem,
  HubListFilters,
  SecondMileHubStaffRole,
  ImportHistory,
  ValidateImportFileResponse,
  Ward,
} from '../../../types';
import { HubFiltersCard, HubFormDialog, HubImportCard } from './components';
import {
  buildHubListFilters,
  countActiveHubAdvancedFilters,
  DEFAULT_HUB_FILTER_FORM,
  type HubFilterFormState,
} from './hubFilterModels';
import {
  buildCreateHubRequest,
  buildUpdateHubRequest,
  DEFAULT_HUB_FORM,
  getHubTypeLabel,
  mapHubToFormState,
  validateHubForm,
  type HubFormMode,
  type HubFormState,
} from './hubForm';

const PAGE_SIZE = 20;
const MAP_PAGE_SIZE = 500;
const HUB_ORDER_PAGE_SIZE = 10;
const HUB_STOCK_ORDER_STATUSES: FirstMileOrderStatus[] = [
  'INBOUND_AT_ORIGIN_HUB',
  'BAGGING_IN_PROGRESS',
  'BAGGED',
  'BAG_SEALED',
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
  'sticky right-0 z-10 w-[240px] min-w-[240px] border-l bg-card';
const HUB_ACTION_HEADER_CLASS = `${HUB_ACTION_COLUMN_CLASS} z-20`;

export function HubListPage() {
  const notification = useNotification();
  const roles = useAppSelector(
    (state) => state.account.user.profile?.roles ?? []
  );
  const isTmsAdmin = roles.includes('TMS_ADMIN');
  const canManageHubStaffAssignments =
    roles.includes('TMS_ADMIN') || roles.includes('TMS_HUB_MANAGER');

  const [filterMode, setFilterMode] = React.useState<TmsFilterMode>('basic');
  const [filterFormValues, setFilterFormValues] =
    React.useState<HubFilterFormState>(DEFAULT_HUB_FILTER_FORM);
  const [appliedFilters, setAppliedFilters] = React.useState<HubListFilters>(
    {}
  );
  const [currentPage, setCurrentPage] = React.useState(0);
  const [mapBounds, setMapBounds] = React.useState<TmsMapBounds | null>(null);

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

  const advancedFieldCount = React.useMemo(
    () => countActiveHubAdvancedFilters(filterFormValues),
    [filterFormValues]
  );

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

  const [manageStaffHub, setManageStaffHub] = React.useState<Hub | null>(null);
  const [staffDialogOpen, setStaffDialogOpen] = React.useState(false);
  const [staffRoleFilter, setStaffRoleFilter] = React.useState<
    'ALL' | SecondMileHubStaffRole
  >('ALL');
  const [staffRoleToAssign, setStaffRoleToAssign] =
    React.useState<SecondMileHubStaffRole>('EMPLOYEE');
  const [staffSearchKeyword, setStaffSearchKeyword] = React.useState('');
  const [selectedStaffIdToAssign, setSelectedStaffIdToAssign] =
    React.useState('');

  const [formDialogOpen, setFormDialogOpen] = React.useState(false);
  const [formMode, setFormMode] = React.useState<HubFormMode>('create');
  const [editingHubId, setEditingHubId] = React.useState<number | null>(null);
  const [formValues, setFormValues] =
    React.useState<HubFormState>(DEFAULT_HUB_FORM);

  const [detailHub, setDetailHub] = React.useState<Hub | null>(null);
  const [detailTab, setDetailTab] = React.useState<'details' | 'orders'>(
    'details'
  );
  const [detailOrdersPage, setDetailOrdersPage] = React.useState(0);
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

  const {
    data: detailHubPostOfficeData,
    isFetching: isFetchingDetailHubPostOffices,
  } = useGetHubPostOfficesQuery(
    {
      hubId: detailHub?.id || 0,
      page: 0,
      size: 500,
    },
    { skip: !detailHub }
  );
  const detailHubPostOfficeCodes = React.useMemo(
    () =>
      (detailHubPostOfficeData?.items ?? [])
        .map((mapping) => mapping.postOfficeCode)
        .filter((code): code is string => Boolean(code)),
    [detailHubPostOfficeData?.items]
  );
  const { data: detailOrdersData, isFetching: isFetchingDetailOrders } =
    useGetOrdersQuery(
      {
        page: detailOrdersPage,
        size: HUB_ORDER_PAGE_SIZE,
        originPostOfficeCodes: detailHubPostOfficeCodes,
        statuses: HUB_STOCK_ORDER_STATUSES,
      },
      { skip: !detailHub || detailHubPostOfficeCodes.length === 0 }
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
  const [assignSecondMileStaffToHub, { isLoading: isAssigningHubStaff }] =
    useAssignSecondMileStaffToHubMutation();
  const [
    unassignSecondMileHubStaffAssignment,
    { isLoading: isUnassigningHubStaff },
  ] = useUnassignSecondMileHubStaffAssignmentMutation();

  const {
    data: hubStaffAssignments,
    isFetching: isFetchingHubStaffAssignments,
    refetch: refetchHubStaffAssignments,
  } = useGetSecondMileHubStaffAssignmentsQuery(
    {
      hubId: manageStaffHub?.id ?? 0,
      ...(staffRoleFilter !== 'ALL' ? { role: staffRoleFilter } : {}),
    },
    { skip: !manageStaffHub }
  );

  const {
    data: assignableHubStaffs,
    isFetching: isFetchingAssignableHubStaffs,
  } = useGetSecondMileAssignableStaffsQuery(
    {
      role: staffRoleToAssign,
      ...(staffSearchKeyword.trim()
        ? { keyword: staffSearchKeyword.trim() }
        : {}),
    },
    { skip: !manageStaffHub }
  );
  const staffRoleFilterOptions = [
    { value: 'ALL', label: 'All roles' },
    { value: 'MANAGER', label: 'Manager' },
    { value: 'EMPLOYEE', label: 'Employee' },
    { value: 'DRIVER', label: 'Driver' },
  ];
  const staffRoleAssignOptions = [
    { value: 'MANAGER', label: 'Manager' },
    { value: 'EMPLOYEE', label: 'Employee' },
    { value: 'DRIVER', label: 'Driver' },
  ];
  const assignableStaffOptions = (assignableHubStaffs ?? []).map((staff) => ({
    value: String(staff.id),
    label:
      (staff.fullName || staff.code || `#${staff.id}`) +
      (staff.code ? ` (${staff.code})` : ''),
  }));

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
      notification.error('Only TMS_ADMIN can create hubs.');
      return;
    }
    setFormMode('create');
    setEditingHubId(null);
    setFormValues(DEFAULT_HUB_FORM);
    setFormDialogOpen(true);
  };

  const openEditDialog = (hub: Hub) => {
    if (!isTmsAdmin) {
      notification.error('Only TMS_ADMIN can update hubs.');
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
      notification.error('Only TMS_ADMIN can save hubs.');
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
        notification.success('Hub created successfully.');
      } else if (editingHubId != null) {
        await updateHub({
          id: editingHubId,
          body: buildUpdateHubRequest(formValues),
        }).unwrap();
        notification.success('Hub updated successfully.');
      }
      setFormDialogOpen(false);
      refetch();
      if (mapBounds) {
        void refetchMapHubs();
      }
    } catch (error) {
      notification.error('Failed to save hub.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleGeocodeHubAddress = React.useCallback(async () => {
    if (!isTmsAdmin) {
      notification.error('Only TMS_ADMIN can geocode hub location.');
      return;
    }

    const addressDetail = formValues.address_detail.trim();
    if (!addressDetail) {
      notification.error('Address detail is required before geocoding.');
      return;
    }

    const provinceCode = selectedFormProvinceCode;
    if (!provinceCode) {
      notification.error('Select a province before geocoding.');
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
      notification.success('Coordinates updated from address geocoding.');
    } catch (error) {
      notification.error('Failed to geocode hub address.', {
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
      notification.success('Hub deleted successfully.');
      setDeleteTarget(null);
      refetch();
      if (mapBounds) {
        void refetchMapHubs();
      }
    } catch (error) {
      notification.error('Failed to delete hub.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleDownloadTemplate = async () => {
    if (!isTmsAdmin) {
      notification.error('Only TMS_ADMIN can download hub templates.');
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
      notification.success('Hub template downloaded successfully.');
    } catch (error) {
      notification.error('Failed to download hub template.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleValidateImportFile = async () => {
    if (!isTmsAdmin) {
      notification.error('Only TMS_ADMIN can validate hub imports.');
      return;
    }
    if (!selectedImportFile) {
      notification.error('Please select an Excel file first.');
      return;
    }
    try {
      const formData = new FormData();
      formData.append('file', selectedImportFile);
      const result = await validateHubImport(formData).unwrap();
      setValidateImportResult(result);
      if (result.is_success) {
        notification.success('File validated successfully.');
      }
    } catch (error) {
      notification.error('Failed to validate hub import file.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleImportFile = async () => {
    if (!isTmsAdmin) {
      notification.error('Only TMS_ADMIN can import hubs.');
      return;
    }
    if (!selectedImportFile) {
      notification.error('Please select an Excel file first.');
      return;
    }
    if (!validateImportResult) {
      notification.error('Please validate the selected file before importing.');
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
      notification.success('Hub import job created.', {
        description: `Import ID: ${job.id}`,
      });
      resetImportFileSelection();
      refetch();
      if (mapBounds) {
        void refetchMapHubs();
      }
    } catch (error) {
      notification.error('Failed to import hub file.', {
        description: getErrorMessage(error),
      });
    }
  };

  const openHubStaffDialog = (hub: Hub) => {
    setManageStaffHub(hub);
    setStaffRoleFilter('ALL');
    setStaffRoleToAssign('EMPLOYEE');
    setStaffSearchKeyword('');
    setSelectedStaffIdToAssign('');
    setStaffDialogOpen(true);
  };

  const openHubDetail = React.useCallback((hub: Hub) => {
    setDetailHub(hub);
    setDetailTab('details');
    setDetailOrdersPage(0);
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

  const handleAssignHubStaff = async () => {
    if (!canManageHubStaffAssignments) {
      notification.error('Only TMS_ADMIN or TMS_HUB_MANAGER can assign staff.');
      return;
    }
    if (!manageStaffHub?.id) {
      return;
    }
    const staffId = Number(selectedStaffIdToAssign);
    if (!Number.isInteger(staffId) || staffId <= 0) {
      notification.error('Select a staff from dropdown.');
      return;
    }

    try {
      await assignSecondMileStaffToHub({
        staffId,
        hubId: manageStaffHub.id,
      }).unwrap();
      notification.success('Hub staff assigned successfully.');
      setSelectedStaffIdToAssign('');
      void refetchHubStaffAssignments();
    } catch (error) {
      notification.error('Failed to assign hub staff.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleUnassignHubStaff = async (assignmentId?: number) => {
    if (!canManageHubStaffAssignments) {
      notification.error(
        'Only TMS_ADMIN or TMS_HUB_MANAGER can unassign staff.'
      );
      return;
    }
    if (!assignmentId) {
      return;
    }
    try {
      await unassignSecondMileHubStaffAssignment(assignmentId).unwrap();
      notification.success('Hub staff unassigned successfully.');
      void refetchHubStaffAssignments();
    } catch (error) {
      notification.error('Failed to unassign hub staff.', {
        description: getErrorMessage(error),
      });
    }
  };

  const triggerHubImagePicker = (hubId: number) => {
    if (!isTmsAdmin) {
      notification.error('Only TMS_ADMIN can upload hub images.');
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
      notification.success('Hub image updated successfully.');
      refetch();
    } catch (error) {
      notification.error('Failed to upload hub image.', {
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
        error instanceof Error ? error.message : 'Invalid filter values.'
      );
    }
  };

  const handleClearFilters = () => {
    setFilterFormValues(DEFAULT_HUB_FILTER_FORM);
    setAppliedFilters({});
    setFilterMode('basic');
    setCurrentPage(0);
  };

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
            <h1 className='text-2xl font-bold tracking-tight'>Hubs</h1>
            <p className='text-muted-foreground'>
              Manage second-mile hubs, staff assignments, location quality, and
              hub stock visibility.
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
              Refresh
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
                  New hub
                </Button>
              </>
            ) : (
              <Badge variant='outline' className='gap-1'>
                <ShieldAlert className='h-3.5 w-3.5' />
                View only (write actions require TMS_ADMIN)
              </Badge>
            )}
          </div>
        </div>

        <HubFiltersCard
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
          title='Hub map'
          description='Markers are loaded only for hubs inside the visible map area.'
          points={mapHubPoints}
          markerColor='#b45309'
          markerFillColor='#f59e0b'
          loading={!mapBounds || isFetchingMapHubs}
          totalItems={mapHubsData?.totalItems}
          emptyText='No geocoded hubs in this map area.'
          onBoundsChange={handleMapBoundsChange}
          onPointClick={handleMapHubClick}
        />

        <Card>
          <CardHeader>
            <CardTitle className='flex items-center gap-2'>
              <Building2 className='h-5 w-5' />
              Hub table
            </CardTitle>
          </CardHeader>
          <CardContent className='space-y-4'>
            {isFetching && hubs.length === 0 ? (
              <div className='text-center text-muted-foreground py-8'>
                Loading hubs...
              </div>
            ) : hubs.length === 0 ? (
              <div className='text-center text-muted-foreground py-8'>
                No hubs found
              </div>
            ) : (
              <div className='rounded-md border'>
                <Table className='min-w-[1240px]'>
                  <TableHeader>
                    <TableRow>
                      <TableHead className='w-[150px]'>Code</TableHead>
                      <TableHead className='w-[220px]'>Name</TableHead>
                      <TableHead className='w-[120px]'>Type</TableHead>
                      <TableHead className='w-[130px]'>Status</TableHead>
                      <TableHead className='w-[220px]'>
                        Province / Ward
                      </TableHead>
                      <TableHead className='w-[280px]'>Address</TableHead>
                      <TableHead className='w-[130px]'>Hub load</TableHead>
                      <TableHead className='w-[180px]'>Coordinates</TableHead>
                      <TableHead className={HUB_ACTION_HEADER_CLASS}>
                        <span className='flex justify-end'>Actions</span>
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
                          <TableCell>{getHubTypeLabel(hub.hubType)}</TableCell>
                          <TableCell>
                            <Badge
                              variant={getHubStatusBadgeVariant(hub.status)}
                            >
                              {hub.status}
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
                                    aria-label='View hub details'
                                    onClick={() => openHubDetail(hub)}
                                  >
                                    <Eye className='h-4 w-4' />
                                  </Button>
                                </TooltipTrigger>
                                <TooltipContent>View details</TooltipContent>
                              </Tooltip>

                              <Tooltip>
                                <TooltipTrigger asChild>
                                  <Button
                                    type='button'
                                    variant='outline'
                                    size='icon'
                                    aria-label='Manage hub staff'
                                    onClick={() => openHubStaffDialog(hub)}
                                  >
                                    <UserCog className='h-4 w-4' />
                                  </Button>
                                </TooltipTrigger>
                                <TooltipContent>Staff</TooltipContent>
                              </Tooltip>

                              {isTmsAdmin ? (
                                <>
                                  <Tooltip>
                                    <TooltipTrigger asChild>
                                      <Button
                                        type='button'
                                        variant='outline'
                                        size='icon'
                                        aria-label='Edit hub'
                                        onClick={() => openEditDialog(hub)}
                                      >
                                        <Pencil className='h-4 w-4' />
                                      </Button>
                                    </TooltipTrigger>
                                    <TooltipContent>Edit</TooltipContent>
                                  </Tooltip>
                                  <Tooltip>
                                    <TooltipTrigger asChild>
                                      <Button
                                        type='button'
                                        variant='outline'
                                        size='icon'
                                        aria-label='Upload hub image'
                                        onClick={() =>
                                          triggerHubImagePicker(hub.id)
                                        }
                                        disabled={isUploadingImage}
                                      >
                                        <ImageUp className='h-4 w-4' />
                                      </Button>
                                    </TooltipTrigger>
                                    <TooltipContent>Image</TooltipContent>
                                  </Tooltip>
                                  <Tooltip>
                                    <TooltipTrigger asChild>
                                      <Button
                                        type='button'
                                        variant='destructive'
                                        size='icon'
                                        aria-label='Delete hub'
                                        onClick={() => setDeleteTarget(hub)}
                                      >
                                        <Trash2 className='h-4 w-4' />
                                      </Button>
                                    </TooltipTrigger>
                                    <TooltipContent>Delete</TooltipContent>
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
                  Page {currentPage + 1} of {totalPages}
                </div>
                <div className='flex gap-2'>
                  <Button
                    variant='outline'
                    size='sm'
                    disabled={!hasPrev || isFetching}
                    onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
                  >
                    Previous
                  </Button>
                  <Button
                    variant='outline'
                    size='sm'
                    disabled={!hasNext || isFetching}
                    onClick={() => setCurrentPage((p) => p + 1)}
                  >
                    Next
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
            setDetailTab('details');
            setDetailOrdersPage(0);
          }
        }}
      >
        <DialogContent className='max-w-5xl max-h-[90vh] overflow-y-auto'>
          <DialogHeader>
            <DialogTitle>{detailHub?.name}</DialogTitle>
            <DialogDescription className='font-mono'>
              {detailHub?.code}
            </DialogDescription>
          </DialogHeader>
          {detailHub && (
            <Tabs
              value={detailTab}
              onValueChange={(value) => setDetailTab(value as typeof detailTab)}
            >
              <TabsList>
                <TabsTrigger value='details'>Details</TabsTrigger>
                <TabsTrigger value='orders'>Orders</TabsTrigger>
              </TabsList>
              <TabsContent value='details' className='mt-4'>
                <div className='space-y-3 text-sm'>
                  {detailHub.imageUrl && (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img
                      src={detailHub.imageUrl}
                      alt=''
                      className='w-full rounded-md border max-h-48 object-cover'
                    />
                  )}
                  <p>
                    <span className='font-medium'>Type:</span>{' '}
                    {getHubTypeLabel(detailHub.hubType)}
                  </p>
                  <p>
                    <span className='font-medium'>Status:</span>{' '}
                    {detailHub.status}
                  </p>
                  <p>
                    <span className='font-medium'>Province / Ward:</span>{' '}
                    {getProvinceLabel(detailHub.provinceCode)} /{' '}
                    {detailHub.wardCode}
                  </p>
                  <p>
                    <span className='font-medium'>Address:</span>{' '}
                    {detailHub.addressDetail}
                  </p>
                  {detailHub.phoneNumber && (
                    <p>
                      <span className='font-medium'>Phone:</span>{' '}
                      {detailHub.phoneNumber}
                    </p>
                  )}
                  <p>
                    <span className='font-medium'>Hub load:</span>{' '}
                    {detailHub.currentLoad ?? 0}/{detailHub.dailyCapacity ?? 0}
                  </p>
                  {(detailHub.latitude != null ||
                    detailHub.longitude != null) && (
                    <p>
                      <span className='font-medium'>Coordinates:</span>{' '}
                      {detailHub.latitude}, {detailHub.longitude}
                    </p>
                  )}
                  {isTmsAdmin && (
                    <div className='flex flex-wrap gap-2 pt-2'>
                      <Button
                        size='sm'
                        variant='outline'
                        onClick={() => triggerHubImagePicker(detailHub.id)}
                        disabled={isUploadingImage}
                      >
                        <ImageUp className='h-4 w-4 mr-1' />
                        Change image
                      </Button>
                      <Button
                        size='sm'
                        variant='outline'
                        onClick={() => {
                          setDetailHub(null);
                          setDetailTab('details');
                          setDetailOrdersPage(0);
                          openEditDialog(detailHub);
                        }}
                      >
                        <Pencil className='h-4 w-4 mr-1' />
                        Edit
                      </Button>
                    </div>
                  )}
                </div>
              </TabsContent>
              <TabsContent value='orders' className='mt-4'>
                <TmsEntityOrdersTab
                  data={detailOrdersData}
                  isFetching={
                    isFetchingDetailHubPostOffices || isFetchingDetailOrders
                  }
                  page={detailOrdersPage}
                  emptyText='No orders are currently at this hub.'
                  onPreviousPage={() =>
                    setDetailOrdersPage((prev) => Math.max(prev - 1, 0))
                  }
                  onNextPage={() => setDetailOrdersPage((prev) => prev + 1)}
                />
              </TabsContent>
            </Tabs>
          )}
        </DialogContent>
      </Dialog>

      <Dialog
        open={staffDialogOpen}
        onOpenChange={(open) => {
          setStaffDialogOpen(open);
          if (!open) {
            setManageStaffHub(null);
          }
        }}
      >
        <DialogContent className='max-w-2xl max-h-[80vh] overflow-y-auto'>
          <DialogHeader>
            <DialogTitle>
              Hub staff assignments — {manageStaffHub?.name} (
              {manageStaffHub?.code})
            </DialogTitle>
            <DialogDescription>
              Assign or unassign manager/employee/driver for this hub.
            </DialogDescription>
          </DialogHeader>

          <div className='space-y-4'>
            <div className='grid gap-3 sm:grid-cols-2'>
              <div className='space-y-2'>
                <Label htmlFor='hub-staff-role-filter'>Role filter</Label>
                <TmsCombobox
                  id='hub-staff-role-filter'
                  value={staffRoleFilter}
                  onValueChange={(value) =>
                    setStaffRoleFilter(value as 'ALL' | SecondMileHubStaffRole)
                  }
                  options={staffRoleFilterOptions}
                  placeholder='All roles'
                  emptyText='No roles found'
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='hub-staff-role-assign'>Assign role</Label>
                <TmsCombobox
                  id='hub-staff-role-assign'
                  value={staffRoleToAssign}
                  onValueChange={(value) =>
                    setStaffRoleToAssign(value as SecondMileHubStaffRole)
                  }
                  options={staffRoleAssignOptions}
                  placeholder='Select role'
                  emptyText='No roles found'
                />
              </div>
            </div>

            <div className='space-y-2'>
              <Label htmlFor='hub-staff-search'>Search staff (code/name)</Label>
              <Input
                id='hub-staff-search'
                placeholder='e.g. USR_123_DRIVER or Nguyen Van A'
                value={staffSearchKeyword}
                onChange={(event) => setStaffSearchKeyword(event.target.value)}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='hub-staff-select'>Staff</Label>
              <div className='flex gap-2'>
                <TmsCombobox
                  id='hub-staff-select'
                  value={selectedStaffIdToAssign}
                  onValueChange={setSelectedStaffIdToAssign}
                  options={assignableStaffOptions}
                  placeholder={
                    isFetchingAssignableHubStaffs
                      ? 'Loading staffs...'
                      : 'Select staff'
                  }
                  emptyText='No staffs found'
                  loading={isFetchingAssignableHubStaffs}
                />

                <Button
                  onClick={() => void handleAssignHubStaff()}
                  disabled={
                    !canManageHubStaffAssignments ||
                    isAssigningHubStaff ||
                    !selectedStaffIdToAssign
                  }
                >
                  {isAssigningHubStaff ? 'Assigning...' : 'Assign'}
                </Button>
              </div>
            </div>

            {isFetchingHubStaffAssignments ? (
              <p className='text-sm text-muted-foreground'>
                Loading assignments...
              </p>
            ) : (hubStaffAssignments ?? []).length === 0 ? (
              <p className='text-sm text-muted-foreground'>
                No active staff assignments for this hub.
              </p>
            ) : (
              <div className='space-y-2'>
                {(hubStaffAssignments ?? []).map((assignment) => (
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
                      disabled={
                        !canManageHubStaffAssignments || isUnassigningHubStaff
                      }
                      onClick={() => void handleUnassignHubStaff(assignment.id)}
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
        title='Delete hub'
        description={
          deleteTarget
            ? `This will permanently delete hub ${deleteTarget.code} — ${deleteTarget.name}.`
            : 'This action cannot be undone.'
        }
        confirmText='Delete'
        cancelText='Cancel'
        onConfirm={handleDeleteHub}
        isLoading={isDeleting}
        variant='destructive'
      />
    </>
  );
}
