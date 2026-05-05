/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Hub list, CRUD, import, post office links
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
  Search,
  Building2,
  AlertCircle,
  MapPin,
  Plus,
  X,
  Pencil,
  Trash2,
  Eye,
  ImageUp,
  RefreshCw,
  ShieldAlert,
} from 'lucide-react';
import {
  useGetHubsQuery,
  useGetHubPostOfficesQuery,
  useAssignPostOfficeToHubMutation,
  useRemovePostOfficeFromHubMutation,
  useGetPostOfficesQuery,
  useGetProvincesQuery,
  useGetWardsByProvinceCodeQuery,
  useCreateHubMutation,
  useUpdateHubMutation,
  useDeleteHubMutation,
  useUploadHubImageMutation,
  useLazyExportHubTemplateQuery,
  useValidateHubImportMutation,
  useImportHubsMutation,
} from '../../api';
import type {
  Hub,
  HubPostOfficeMapping,
  HubStatus,
  HubImportItem,
  ImportHistory,
  ValidateImportFileResponse,
  Ward,
} from '../../types';
import { HubFormDialog, HubImportCard } from './components';
import {
  buildCreateHubRequest,
  buildUpdateHubRequest,
  DEFAULT_HUB_FORM,
  getHubTypeLabel,
  HUB_STATUS_OPTIONS,
  mapHubToFormState,
  validateHubForm,
  type HubFormMode,
  type HubFormState,
} from './hubForm';

const PAGE_SIZE = 20;
const IMPORT_PREVIEW_LIMIT = 5;

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

export function HubListPage() {
  const notification = useNotification();
  const isTmsAdmin = useAppSelector((state) =>
    Boolean(state.account.user.profile?.roles?.includes('TMS_ADMIN'))
  );

  const [searchKeyword, setSearchKeyword] = React.useState('');
  const [statusFilter, setStatusFilter] = React.useState<'ALL' | HubStatus>(
    'ALL'
  );
  const [currentPage, setCurrentPage] = React.useState(0);

  const { data: hubsData, isFetching, refetch } = useGetHubsQuery({
    page: currentPage,
    size: PAGE_SIZE,
    keyword: searchKeyword || undefined,
    status: statusFilter === 'ALL' ? undefined : statusFilter,
  });

  const hubs = hubsData?.items || [];
  const totalPages = hubsData?.totalPages || 0;
  const hasNext = hubsData?.hasNext || false;
  const hasPrev = hubsData?.hasPrevious || false;

  const [managePostOfficesHub, setManagePostOfficesHub] =
    React.useState<Hub | null>(null);
  const [postOfficeDialogOpen, setPostOfficeDialogOpen] =
    React.useState(false);
  const [postOfficePageNumber, setPostOfficePageNumber] = React.useState(0);

  const { data: hubPostOfficeData, isFetching: isFetchingHubPostOffices, refetch: refetchHubPostOffices } =
    useGetHubPostOfficesQuery(
      {
        hubId: managePostOfficesHub?.id || 0,
        page: postOfficePageNumber,
        size: 20,
      },
      { skip: !managePostOfficesHub }
    );

  const hubPostOffices = hubPostOfficeData?.items || [];
  const hasNextPostOffice = hubPostOfficeData?.hasNext || false;
  const hasPrevPostOffice = hubPostOfficeData?.hasPrevious || false;

  const [assignMutation, { isLoading: isAssigning }] =
    useAssignPostOfficeToHubMutation();
  const [removeMutation, { isLoading: isRemoving }] =
    useRemovePostOfficeFromHubMutation();

  const [assignDialogOpen, setAssignDialogOpen] = React.useState(false);
  const [assignSearchKeyword, setAssignSearchKeyword] = React.useState('');
  const [selectedPostOfficeCode, setSelectedPostOfficeCode] =
    React.useState('');

  const { data: availablePostOfficesData } = useGetPostOfficesQuery(
    {
      page: 0,
      size: 50,
      keyword: assignSearchKeyword || undefined,
    },
    { skip: !assignDialogOpen }
  );

  const availablePostOffices = availablePostOfficesData?.items || [];

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
  const [validateImportResult, setValidateImportResult] = React.useState<
    ValidateImportFileResponse<HubImportItem> | null
  >(null);
  const [lastImportJob, setLastImportJob] =
    React.useState<ImportHistory | null>(null);

  const imageInputRef = React.useRef<HTMLInputElement | null>(null);
  const [imageUploadHubId, setImageUploadHubId] = React.useState<
    number | null
  >(null);

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
  }, [
    selectedFormProvinceCode,
    selectedFormWardCode,
    wardsForFormData,
  ]);

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
  const [importHubs, { isLoading: isImportingHubs }] =
    useImportHubsMutation();

  const updateFormField = React.useCallback(
    <K extends keyof HubFormState>(field: K, value: HubFormState[K]) => {
      setFormValues((prev) => ({ ...prev, [field]: value }));
    },
    []
  );

  const validatedPreviewItems = React.useMemo(
    () => validateImportResult?.data?.slice(0, IMPORT_PREVIEW_LIMIT) ?? [],
    [validateImportResult]
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
    } catch (error) {
      notification.error('Failed to save hub.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleDeleteHub = async () => {
    if (!deleteTarget || !isTmsAdmin) {
      return;
    }
    try {
      await deleteHub(deleteTarget.id).unwrap();
      notification.success('Hub deleted successfully.');
      setDeleteTarget(null);
      refetch();
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
      } else {
        notification.error('Validation completed with errors.', {
          description: result.error_message,
        });
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
    if (!validateImportResult?.is_success) {
      notification.error('Please validate the selected file before importing.');
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
    } catch (error) {
      notification.error('Failed to import hub file.', {
        description: getErrorMessage(error),
      });
    }
  };

  const openPostOfficeDialog = (hub: Hub) => {
    setManagePostOfficesHub(hub);
    setPostOfficePageNumber(0);
    setPostOfficeDialogOpen(true);
  };

  const handleAssignPostOffice = async () => {
    if (!isTmsAdmin) {
      notification.error('Only TMS_ADMIN can assign post offices to hubs.');
      return;
    }
    if (!managePostOfficesHub || !selectedPostOfficeCode) {
      notification.error('Please select a post office.');
      return;
    }
    try {
      await assignMutation({
        hubId: managePostOfficesHub.id,
        request: { post_office_code: selectedPostOfficeCode },
      }).unwrap();
      notification.success('Post office assigned successfully.');
      setAssignDialogOpen(false);
      setSelectedPostOfficeCode('');
      setAssignSearchKeyword('');
      refetchHubPostOffices();
      refetch();
    } catch (error) {
      notification.error('Failed to assign post office.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleRemovePostOffice = async (mapping: HubPostOfficeMapping) => {
    if (!isTmsAdmin) {
      notification.error('Only TMS_ADMIN can remove post offices from hubs.');
      return;
    }
    if (!managePostOfficesHub) {
      return;
    }
    try {
      await removeMutation({
        hubId: managePostOfficesHub.id,
        postOfficeCode: mapping.postOfficeCode,
      }).unwrap();
      notification.success('Post office removed from hub.');
      refetchHubPostOffices();
      refetch();
    } catch (error) {
      notification.error('Failed to remove post office.', {
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

  const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setSearchKeyword(event.target.value);
    setCurrentPage(0);
  };

  const handleStatusFilterChange = (value: string) => {
    setStatusFilter(value as 'ALL' | HubStatus);
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
              Distribution hubs (second-mile). Post office links sync to
              first-mile.
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
              <Button type='button' onClick={openCreateDialog}>
                <Plus className='h-4 w-4 mr-2' />
                New hub
              </Button>
            ) : (
              <Badge variant='outline' className='gap-1'>
                <ShieldAlert className='h-3.5 w-3.5' />
                View only (write actions require TMS_ADMIN)
              </Badge>
            )}
          </div>
        </div>

        <HubImportCard
          isTmsAdmin={isTmsAdmin}
          isImportFlowBusy={isImportFlowBusy}
          isExportingTemplate={isExportingTemplate}
          isValidatingImport={isValidatingImport}
          isImportingHubs={isImportingHubs}
          importFileInputKey={importFileInputKey}
          selectedImportFile={selectedImportFile}
          validateImportResult={validateImportResult}
          validatedPreviewItems={validatedPreviewItems}
          lastImportJob={lastImportJob}
          previewLimit={IMPORT_PREVIEW_LIMIT}
          getProvinceLabel={getProvinceLabel}
          onSelectImportFile={(e) => {
            const file = e.target.files?.[0];
            setSelectedImportFile(file ?? null);
            setValidateImportResult(null);
          }}
          onDownloadTemplate={handleDownloadTemplate}
          onValidateImportFile={handleValidateImportFile}
          onImportFile={handleImportFile}
        />

        <Card>
          <CardHeader>
            <CardTitle className='flex items-center gap-2'>
              <Building2 className='h-5 w-5' />
              Hub list
            </CardTitle>
            <CardDescription>Search and open hub details.</CardDescription>
          </CardHeader>
          <CardContent className='space-y-4'>
            <div className='flex gap-3 flex-wrap'>
              <div className='flex-1 min-w-[200px]'>
                <div className='relative'>
                  <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
                  <Input
                    placeholder='Search by code or name'
                    className='pl-9'
                    value={searchKeyword}
                    onChange={handleSearchChange}
                  />
                </div>
              </div>
              <Select
                value={statusFilter}
                onValueChange={handleStatusFilterChange}
              >
                <SelectTrigger className='w-48'>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value='ALL'>All statuses</SelectItem>
                  {HUB_STATUS_OPTIONS.map((opt) => (
                    <SelectItem key={opt.value} value={opt.value}>
                      {opt.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {isFetching && hubs.length === 0 ? (
              <div className='text-center text-muted-foreground py-8'>
                Loading hubs...
              </div>
            ) : hubs.length === 0 ? (
              <div className='text-center text-muted-foreground py-8'>
                No hubs found
              </div>
            ) : (
              <div className='grid gap-4 md:grid-cols-2 lg:grid-cols-3'>
                {hubs.map((hub) => (
                  <Card
                    key={hub.id}
                    className='hover:shadow-md transition-shadow'
                  >
                    <CardHeader className='pb-3'>
                      <div className='flex items-start justify-between gap-2'>
                        <div className='space-y-1 min-w-0'>
                          <CardTitle className='text-lg truncate'>
                            {hub.name}
                          </CardTitle>
                          <CardDescription className='font-mono text-xs'>
                            {hub.code}
                          </CardDescription>
                        </div>
                        <Badge variant={getHubStatusBadgeVariant(hub.status)}>
                          {hub.status}
                        </Badge>
                      </div>
                    </CardHeader>
                    <CardContent className='space-y-3'>
                      {hub.imageUrl && (
                        // eslint-disable-next-line @next/next/no-img-element
                        <img
                          src={hub.imageUrl}
                          alt=''
                          className='h-28 w-full rounded-md object-cover border'
                        />
                      )}
                      <div className='text-sm'>
                        <span className='font-medium'>Type:</span>{' '}
                        {getHubTypeLabel(hub.hubType)}
                      </div>
                      {hub.addressDetail && (
                        <div className='flex items-start gap-2 text-sm text-muted-foreground'>
                          <MapPin className='h-4 w-4 mt-0.5 shrink-0' />
                          <span className='line-clamp-2'>{hub.addressDetail}</span>
                        </div>
                      )}
                      {hub.dailyCapacity !== undefined && (
                        <div className='text-sm'>
                          <span className='font-medium'>Load:</span>{' '}
                          {hub.currentLoad ?? 0}/{hub.dailyCapacity}
                        </div>
                      )}
                      <div className='flex flex-wrap gap-2'>
                        <Button
                          variant='outline'
                          size='sm'
                          onClick={() => setDetailHub(hub)}
                        >
                          <Eye className='h-4 w-4 mr-1' />
                          Details
                        </Button>
                        <Button
                          variant='outline'
                          size='sm'
                          onClick={() => openPostOfficeDialog(hub)}
                        >
                          Post offices
                        </Button>
                        {isTmsAdmin && (
                          <>
                            <Button
                              variant='outline'
                              size='sm'
                              onClick={() => openEditDialog(hub)}
                            >
                              <Pencil className='h-4 w-4 mr-1' />
                              Edit
                            </Button>
                            <Button
                              variant='outline'
                              size='sm'
                              onClick={() => triggerHubImagePicker(hub.id)}
                              disabled={isUploadingImage}
                            >
                              <ImageUp className='h-4 w-4 mr-1' />
                              Image
                            </Button>
                            <Button
                              variant='destructive'
                              size='sm'
                              onClick={() => setDeleteTarget(hub)}
                            >
                              <Trash2 className='h-4 w-4 mr-1' />
                              Delete
                            </Button>
                          </>
                        )}
                      </div>
                    </CardContent>
                  </Card>
                ))}
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
        formValues={formValues}
        selectedProvinceCode={selectedFormProvinceCode}
        selectedWardCode={selectedFormWardCode}
        provinceSelectOptions={provinceSelectOptions}
        wardSelectOptions={wardSelectOptions}
        isFetchingWardsForForm={isFetchingWardsForForm}
        onOpenChange={setFormDialogOpen}
        onSubmit={handleSubmitHubForm}
        updateFormField={updateFormField}
      />

      <Dialog open={Boolean(detailHub)} onOpenChange={() => setDetailHub(null)}>
        <DialogContent className='max-w-lg max-h-[90vh] overflow-y-auto'>
          <DialogHeader>
            <DialogTitle>{detailHub?.name}</DialogTitle>
            <DialogDescription className='font-mono'>
              {detailHub?.code}
            </DialogDescription>
          </DialogHeader>
          {detailHub && (
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
                <span className='font-medium'>Status:</span> {detailHub.status}
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
                <span className='font-medium'>Capacity:</span>{' '}
                {detailHub.currentLoad ?? 0}/{detailHub.dailyCapacity ?? 0}
              </p>
              {(detailHub.latitude != null || detailHub.longitude != null) && (
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
                      openEditDialog(detailHub);
                    }}
                  >
                    <Pencil className='h-4 w-4 mr-1' />
                    Edit
                  </Button>
                </div>
              )}
            </div>
          )}
        </DialogContent>
      </Dialog>

      <Dialog
        open={postOfficeDialogOpen}
        onOpenChange={(open) => {
          setPostOfficeDialogOpen(open);
          if (!open) {
            setManagePostOfficesHub(null);
          }
        }}
      >
        <DialogContent className='max-w-2xl max-h-[80vh] overflow-y-auto'>
          <DialogHeader>
            <DialogTitle>
              Post offices — {managePostOfficesHub?.name} (
              {managePostOfficesHub?.code})
            </DialogTitle>
            <DialogDescription>
              {isTmsAdmin
                ? 'Assign or remove post offices for this hub.'
                : 'View post offices linked to this hub.'}
            </DialogDescription>
          </DialogHeader>

          <div className='space-y-4'>
            {isTmsAdmin && (
              <Button
                variant='default'
                size='sm'
                className='w-full'
                onClick={() => setAssignDialogOpen(true)}
                disabled={isAssigning}
              >
                <Plus className='h-4 w-4 mr-2' />
                Assign post office
              </Button>
            )}

            {isFetchingHubPostOffices && hubPostOffices.length === 0 ? (
              <div className='text-center text-muted-foreground py-4'>
                Loading post offices...
              </div>
            ) : hubPostOffices.length === 0 ? (
              <div className='text-center text-muted-foreground py-4 flex flex-col items-center gap-2'>
                <AlertCircle className='h-8 w-8' />
                <p>No post offices assigned yet</p>
              </div>
            ) : (
              <div className='space-y-2'>
                {hubPostOffices.map((mapping) => (
                  <div
                    key={mapping.id}
                    className='flex items-center justify-between p-3 border rounded-lg'
                  >
                    <div>
                      <div className='font-medium'>{mapping.postOfficeCode}</div>
                      <div className='text-xs text-muted-foreground'>
                        Assigned:{' '}
                        {mapping.createdAt
                          ? new Date(mapping.createdAt).toLocaleDateString()
                          : '—'}
                      </div>
                    </div>
                    {isTmsAdmin && (
                      <Button
                        variant='destructive'
                        size='sm'
                        onClick={() => handleRemovePostOffice(mapping)}
                        disabled={isRemoving}
                      >
                        <X className='h-4 w-4 mr-1' />
                        Remove
                      </Button>
                    )}
                  </div>
                ))}
              </div>
            )}

            {(hasNextPostOffice || hasPrevPostOffice) && (
              <div className='flex items-center justify-between border-t pt-3'>
                <div className='text-sm text-muted-foreground'>
                  Page {postOfficePageNumber + 1}
                </div>
                <div className='flex gap-2'>
                  <Button
                    variant='outline'
                    size='sm'
                    disabled={!hasPrevPostOffice}
                    onClick={() =>
                      setPostOfficePageNumber((p) => Math.max(0, p - 1))
                    }
                  >
                    Previous
                  </Button>
                  <Button
                    variant='outline'
                    size='sm'
                    disabled={!hasNextPostOffice}
                    onClick={() => setPostOfficePageNumber((p) => p + 1)}
                  >
                    Next
                  </Button>
                </div>
              </div>
            )}
          </div>
        </DialogContent>
      </Dialog>

      <Dialog open={assignDialogOpen} onOpenChange={setAssignDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Assign post office</DialogTitle>
            <DialogDescription>
              Select a post office for {managePostOfficesHub?.name}
            </DialogDescription>
          </DialogHeader>
          <div className='space-y-4'>
            <div className='space-y-2'>
              <Label htmlFor='assign-search'>Search</Label>
              <Input
                id='assign-search'
                placeholder='Code or name'
                value={assignSearchKeyword}
                onChange={(e) => setAssignSearchKeyword(e.target.value)}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='assign-po-select'>Post office</Label>
              <Select
                value={selectedPostOfficeCode}
                onValueChange={setSelectedPostOfficeCode}
              >
                <SelectTrigger id='assign-po-select'>
                  <SelectValue placeholder='Select a post office' />
                </SelectTrigger>
                <SelectContent>
                  {availablePostOffices.length === 0 ? (
                    <SelectItem value='__empty__' disabled>
                      No post offices available
                    </SelectItem>
                  ) : (
                    availablePostOffices.map((po) => (
                      <SelectItem key={po.code} value={po.code}>
                        {po.name} ({po.code})
                      </SelectItem>
                    ))
                  )}
                </SelectContent>
              </Select>
            </div>

            <div className='flex justify-end gap-2 pt-4 border-t'>
              <Button
                variant='outline'
                onClick={() => {
                  setAssignDialogOpen(false);
                  setSelectedPostOfficeCode('');
                  setAssignSearchKeyword('');
                }}
              >
                Cancel
              </Button>
              <Button
                onClick={handleAssignPostOffice}
                disabled={!selectedPostOfficeCode || isAssigning}
              >
                {isAssigning ? 'Assigning...' : 'Assign'}
              </Button>
            </div>
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
