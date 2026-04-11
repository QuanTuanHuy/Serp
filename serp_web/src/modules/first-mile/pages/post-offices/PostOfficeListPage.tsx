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
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Input,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import { useNotification } from '@/shared/hooks';
import { ConfirmDialog } from '@/shared/components/ui/confirm-dialog';
import { Plus, RefreshCw, Search, ShieldAlert } from 'lucide-react';
import {
  useCreatePostOfficeMutation,
  useDeletePostOfficeMutation,
  useGetPostOfficesQuery,
  useGetWardsByProvinceCodeQuery,
  useImportPostOfficesMutation,
  useLazyExportPostOfficeTemplateQuery,
  useUpdatePostOfficeMutation,
  useValidatePostOfficeImportMutation,
} from '../../api';
import type {
  ImportHistory,
  PostOffice,
  PostOfficeImportItem,
  PostOfficeListFilters,
  PostOfficeStatus,
  ValidateImportFileResponse,
  Ward,
} from '../../types';
import {
  PostOfficeFormDialog,
  PostOfficeImportCard,
  PostOfficeResultsCard,
} from './components';
import {
  buildCreatePostOfficeRequest,
  DEFAULT_POST_OFFICE_FORM,
  getStatusBadgeVariant,
  mapPostOfficeToFormState,
  POST_OFFICE_STATUS_OPTIONS,
  type PostOfficeFormMode,
  type PostOfficeFormState,
  type PostOfficeViewMode,
  validatePostOfficeForm,
} from './postOfficeForm';
import { usePostOfficeLocations } from './usePostOfficeLocations';

const PAGE_SIZE = 20;
const IMPORT_PREVIEW_LIMIT = 5;

type HasLocationFilter = 'ALL' | 'YES' | 'NO';

interface PostOfficeFilterFormState {
  keyword: string;
  code: string;
  name: string;
  provinceCode: string;
  wardCode: string;
  status: 'ALL' | PostOfficeStatus;
  hasLocation: HasLocationFilter;
  minServiceRadiusM: string;
  maxServiceRadiusM: string;
  minDailyCapacity: string;
  maxDailyCapacity: string;
  minCurrentLoad: string;
  maxCurrentLoad: string;
  minPriority: string;
  maxPriority: string;
}

const DEFAULT_POST_OFFICE_FILTER_FORM: PostOfficeFilterFormState = {
  keyword: '',
  code: '',
  name: '',
  provinceCode: '',
  wardCode: '',
  status: 'ALL',
  hasLocation: 'ALL',
  minServiceRadiusM: '',
  maxServiceRadiusM: '',
  minDailyCapacity: '',
  maxDailyCapacity: '',
  minCurrentLoad: '',
  maxCurrentLoad: '',
  minPriority: '',
  maxPriority: '',
};

const parseOptionalNonNegativeInteger = (
  rawValue: string,
  fieldLabel: string
): number | undefined => {
  const trimmedValue = rawValue.trim();
  if (!trimmedValue) {
    return undefined;
  }

  const parsedValue = Number(trimmedValue);
  if (!Number.isInteger(parsedValue) || parsedValue < 0) {
    throw new Error(`${fieldLabel} must be a non-negative integer.`);
  }

  return parsedValue;
};

const validateRange = (
  minValue: number | undefined,
  maxValue: number | undefined,
  fieldLabel: string
) => {
  if (minValue !== undefined && maxValue !== undefined && minValue > maxValue) {
    throw new Error(
      `${fieldLabel}: min value must be less than or equal to max value.`
    );
  }
};

const normalizeText = (value: string): string | undefined => {
  const trimmedValue = value.trim();
  return trimmedValue ? trimmedValue : undefined;
};

export const PostOfficeListPage: React.FC = () => {
  const notification = useNotification();
  const isTmsAdmin = useAppSelector((state) =>
    Boolean(state.account.user.profile?.roles?.includes('TMS_ADMIN'))
  );

  const [page, setPage] = React.useState(0);
  const [filterFormValues, setFilterFormValues] =
    React.useState<PostOfficeFilterFormState>(DEFAULT_POST_OFFICE_FILTER_FORM);
  const [appliedFilters, setAppliedFilters] =
    React.useState<PostOfficeListFilters>({});
  const [formMode, setFormMode] = React.useState<PostOfficeFormMode>('create');
  const [viewMode, setViewMode] = React.useState<PostOfficeViewMode>('list');
  const [isFormDialogOpen, setIsFormDialogOpen] = React.useState(false);
  const [editingId, setEditingId] = React.useState<number | null>(null);
  const [formValues, setFormValues] = React.useState<PostOfficeFormState>(
    DEFAULT_POST_OFFICE_FORM
  );
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
    size: PAGE_SIZE,
    ...appliedFilters,
  });

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

  const buildPostOfficeFilters =
    React.useCallback((): PostOfficeListFilters => {
      const minServiceRadiusM = parseOptionalNonNegativeInteger(
        filterFormValues.minServiceRadiusM,
        'Min service radius'
      );
      const maxServiceRadiusM = parseOptionalNonNegativeInteger(
        filterFormValues.maxServiceRadiusM,
        'Max service radius'
      );
      const minDailyCapacity = parseOptionalNonNegativeInteger(
        filterFormValues.minDailyCapacity,
        'Min daily capacity'
      );
      const maxDailyCapacity = parseOptionalNonNegativeInteger(
        filterFormValues.maxDailyCapacity,
        'Max daily capacity'
      );
      const minCurrentLoad = parseOptionalNonNegativeInteger(
        filterFormValues.minCurrentLoad,
        'Min current load'
      );
      const maxCurrentLoad = parseOptionalNonNegativeInteger(
        filterFormValues.maxCurrentLoad,
        'Max current load'
      );
      const minPriority = parseOptionalNonNegativeInteger(
        filterFormValues.minPriority,
        'Min priority'
      );
      const maxPriority = parseOptionalNonNegativeInteger(
        filterFormValues.maxPriority,
        'Max priority'
      );

      validateRange(minServiceRadiusM, maxServiceRadiusM, 'Service radius');
      validateRange(minDailyCapacity, maxDailyCapacity, 'Daily capacity');
      validateRange(minCurrentLoad, maxCurrentLoad, 'Current load');
      validateRange(minPriority, maxPriority, 'Priority');

      return {
        keyword: normalizeText(filterFormValues.keyword),
        code: normalizeText(filterFormValues.code),
        name: normalizeText(filterFormValues.name),
        provinceCode: normalizeText(filterFormValues.provinceCode),
        wardCode: normalizeText(filterFormValues.wardCode),
        status:
          filterFormValues.status === 'ALL'
            ? undefined
            : filterFormValues.status,
        hasLocation:
          filterFormValues.hasLocation === 'ALL'
            ? undefined
            : filterFormValues.hasLocation === 'YES',
        minServiceRadiusM,
        maxServiceRadiusM,
        minDailyCapacity,
        maxDailyCapacity,
        minCurrentLoad,
        maxCurrentLoad,
        minPriority,
        maxPriority,
      };
    }, [filterFormValues]);

  const handleApplyFilters = (event: React.FormEvent) => {
    event.preventDefault();

    try {
      const nextFilters = buildPostOfficeFilters();
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
      } else {
        notification.error('Validation completed with errors.', {
          description:
            result.error_message ||
            'Please fix the Excel data before importing.',
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
      const importResult = await importPostOfficeFile(formData).unwrap();
      setLastImportJob(importResult);
      resetImportFileSelection();

      notification.success('Post office import job created.', {
        description: `Job #${importResult.id} is ${importResult.status}.`,
      });

      void refetch();
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
            <Button onClick={handleOpenCreateDialog}>
              <Plus className='h-4 w-4 mr-2' />
              New Post Office
            </Button>
          ) : (
            <Badge variant='outline' className='gap-1'>
              <ShieldAlert className='h-3.5 w-3.5' />
              View only (write actions require TMS_ADMIN)
            </Badge>
          )}
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Filters</CardTitle>
            <CardDescription>
              Combine one or more criteria to filter post offices.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleApplyFilters} className='space-y-4'>
              <div className='grid gap-3 sm:grid-cols-2 xl:grid-cols-4'>
                <div className='space-y-2 sm:col-span-2'>
                  <Label htmlFor='post-office-filter-keyword'>Keyword</Label>
                  <div className='relative'>
                    <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
                    <Input
                      id='post-office-filter-keyword'
                      className='pl-10'
                      value={filterFormValues.keyword}
                      onChange={(event) =>
                        updateFilterField('keyword', event.target.value)
                      }
                      placeholder='Code, name, address...'
                    />
                  </div>
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='post-office-filter-code'>Code</Label>
                  <Input
                    id='post-office-filter-code'
                    value={filterFormValues.code}
                    onChange={(event) =>
                      updateFilterField('code', event.target.value)
                    }
                    placeholder='PO-HCM-01'
                  />
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='post-office-filter-name'>Name</Label>
                  <Input
                    id='post-office-filter-name'
                    value={filterFormValues.name}
                    onChange={(event) =>
                      updateFilterField('name', event.target.value)
                    }
                    placeholder='Post office name'
                  />
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='post-office-filter-status'>Status</Label>
                  <Select
                    value={filterFormValues.status}
                    onValueChange={(value) =>
                      updateFilterField(
                        'status',
                        value as PostOfficeFilterFormState['status']
                      )
                    }
                  >
                    <SelectTrigger
                      id='post-office-filter-status'
                      className='w-full'
                    >
                      <SelectValue placeholder='All statuses' />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value='ALL'>All statuses</SelectItem>
                      {POST_OFFICE_STATUS_OPTIONS.map((option) => (
                        <SelectItem key={option.value} value={option.value}>
                          {option.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='post-office-filter-province'>Province</Label>
                  <Select
                    value={selectedFilterProvinceCode || 'ALL'}
                    onValueChange={(value) => {
                      const nextProvinceCode = value === 'ALL' ? '' : value;
                      updateFilterField('provinceCode', nextProvinceCode);
                      updateFilterField('wardCode', '');
                    }}
                  >
                    <SelectTrigger
                      id='post-office-filter-province'
                      className='w-full'
                    >
                      <SelectValue placeholder='All provinces' />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value='ALL'>All provinces</SelectItem>
                      {provinceSelectOptions.map((province) => {
                        if (!province.provinceCode) {
                          return null;
                        }

                        return (
                          <SelectItem
                            key={province.provinceCode}
                            value={province.provinceCode}
                          >
                            {province.name} ({province.provinceCode})
                          </SelectItem>
                        );
                      })}
                    </SelectContent>
                  </Select>
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='post-office-filter-ward'>Ward</Label>
                  <Select
                    value={selectedFilterWardCode || 'ALL'}
                    onValueChange={(value) =>
                      updateFilterField(
                        'wardCode',
                        value === 'ALL' ? '' : value
                      )
                    }
                    disabled={!selectedFilterProvinceCode}
                  >
                    <SelectTrigger
                      id='post-office-filter-ward'
                      className='w-full'
                    >
                      <SelectValue
                        placeholder={
                          selectedFilterProvinceCode
                            ? 'All wards'
                            : 'Select province first'
                        }
                      />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value='ALL'>All wards</SelectItem>
                      {selectedFilterProvinceCode &&
                      isFetchingWardsForFilter ? (
                        <SelectItem value='__loading__' disabled>
                          Loading wards...
                        </SelectItem>
                      ) : filterWardOptions.length > 0 ? (
                        filterWardOptions.map((ward) => {
                          if (!ward.wardCode) {
                            return null;
                          }

                          return (
                            <SelectItem
                              key={ward.wardCode}
                              value={ward.wardCode}
                            >
                              {ward.name} ({ward.wardCode})
                            </SelectItem>
                          );
                        })
                      ) : (
                        <SelectItem value='__empty__' disabled>
                          No wards available
                        </SelectItem>
                      )}
                    </SelectContent>
                  </Select>
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='post-office-filter-has-location'>
                    Has Location
                  </Label>
                  <Select
                    value={filterFormValues.hasLocation}
                    onValueChange={(value) =>
                      updateFilterField(
                        'hasLocation',
                        value as PostOfficeFilterFormState['hasLocation']
                      )
                    }
                  >
                    <SelectTrigger
                      id='post-office-filter-has-location'
                      className='w-full'
                    >
                      <SelectValue placeholder='All records' />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value='ALL'>All records</SelectItem>
                      <SelectItem value='YES'>Has location</SelectItem>
                      <SelectItem value='NO'>No location</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>

              <div className='grid gap-3 sm:grid-cols-2 xl:grid-cols-4'>
                <div className='space-y-2'>
                  <Label htmlFor='post-office-filter-min-service-radius'>
                    Min service radius (m)
                  </Label>
                  <Input
                    id='post-office-filter-min-service-radius'
                    type='number'
                    min={0}
                    step={1}
                    value={filterFormValues.minServiceRadiusM}
                    onChange={(event) =>
                      updateFilterField('minServiceRadiusM', event.target.value)
                    }
                  />
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='post-office-filter-max-service-radius'>
                    Max service radius (m)
                  </Label>
                  <Input
                    id='post-office-filter-max-service-radius'
                    type='number'
                    min={0}
                    step={1}
                    value={filterFormValues.maxServiceRadiusM}
                    onChange={(event) =>
                      updateFilterField('maxServiceRadiusM', event.target.value)
                    }
                  />
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='post-office-filter-min-daily-capacity'>
                    Min daily capacity
                  </Label>
                  <Input
                    id='post-office-filter-min-daily-capacity'
                    type='number'
                    min={0}
                    step={1}
                    value={filterFormValues.minDailyCapacity}
                    onChange={(event) =>
                      updateFilterField('minDailyCapacity', event.target.value)
                    }
                  />
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='post-office-filter-max-daily-capacity'>
                    Max daily capacity
                  </Label>
                  <Input
                    id='post-office-filter-max-daily-capacity'
                    type='number'
                    min={0}
                    step={1}
                    value={filterFormValues.maxDailyCapacity}
                    onChange={(event) =>
                      updateFilterField('maxDailyCapacity', event.target.value)
                    }
                  />
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='post-office-filter-min-current-load'>
                    Min current load
                  </Label>
                  <Input
                    id='post-office-filter-min-current-load'
                    type='number'
                    min={0}
                    step={1}
                    value={filterFormValues.minCurrentLoad}
                    onChange={(event) =>
                      updateFilterField('minCurrentLoad', event.target.value)
                    }
                  />
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='post-office-filter-max-current-load'>
                    Max current load
                  </Label>
                  <Input
                    id='post-office-filter-max-current-load'
                    type='number'
                    min={0}
                    step={1}
                    value={filterFormValues.maxCurrentLoad}
                    onChange={(event) =>
                      updateFilterField('maxCurrentLoad', event.target.value)
                    }
                  />
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='post-office-filter-min-priority'>
                    Min priority
                  </Label>
                  <Input
                    id='post-office-filter-min-priority'
                    type='number'
                    min={0}
                    step={1}
                    value={filterFormValues.minPriority}
                    onChange={(event) =>
                      updateFilterField('minPriority', event.target.value)
                    }
                  />
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='post-office-filter-max-priority'>
                    Max priority
                  </Label>
                  <Input
                    id='post-office-filter-max-priority'
                    type='number'
                    min={0}
                    step={1}
                    value={filterFormValues.maxPriority}
                    onChange={(event) =>
                      updateFilterField('maxPriority', event.target.value)
                    }
                  />
                </div>
              </div>

              <div className='flex flex-wrap gap-2'>
                <Button type='submit'>Apply filters</Button>
                <Button
                  type='button'
                  variant='outline'
                  onClick={handleClearFilters}
                >
                  Clear
                </Button>
                <Button
                  type='button'
                  variant='outline'
                  onClick={() => refetch()}
                  disabled={isFetching}
                >
                  <RefreshCw className='h-4 w-4 mr-2' />
                  Refresh
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>

        <PostOfficeImportCard
          isTmsAdmin={isTmsAdmin}
          isImportFlowBusy={isImportFlowBusy}
          isExportingTemplate={isExportingTemplate}
          isValidatingImport={isValidatingImport}
          isImportingPostOffices={isImportingPostOffices}
          importFileInputKey={importFileInputKey}
          selectedImportFile={selectedImportFile}
          validateImportResult={validateImportResult}
          validatedPreviewItems={validatedPreviewItems}
          lastImportJob={lastImportJob}
          previewLimit={IMPORT_PREVIEW_LIMIT}
          onSelectImportFile={handleSelectImportFile}
          onDownloadTemplate={handleDownloadTemplate}
          onValidateImportFile={handleValidateImportFile}
          onImportFile={handleImportFile}
          getProvinceLabel={getProvinceLabel}
          getWardLabel={getWardLabel}
        />

        <PostOfficeResultsCard
          data={data}
          isLoading={isLoading}
          isFetching={isFetching}
          viewMode={viewMode}
          isTmsAdmin={isTmsAdmin}
          isSaving={isSaving}
          isDeleting={isDeleting}
          onViewModeChange={setViewMode}
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
