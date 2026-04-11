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
import { useNotification } from '@/shared/hooks';
import { ConfirmDialog } from '@/shared/components/ui/confirm-dialog';
import {
  Loader2,
  Pencil,
  Plus,
  RefreshCw,
  Search,
  ShieldAlert,
  Trash2,
} from 'lucide-react';
import {
  useCreatePostOfficeMutation,
  useDeletePostOfficeMutation,
  useGetPostOfficesQuery,
  useUpdatePostOfficeMutation,
} from '../../api';
import type {
  CreatePostOfficeRequest,
  PostOffice,
  PostOfficeStatus,
} from '../../types';

const PAGE_SIZE = 20;

type PostOfficeFormMode = 'create' | 'edit';

interface PostOfficeFormState {
  code: string;
  name: string;
  province_code: string;
  ward_code: string;
  address_detail: string;
  phone_number: string;
  operational_start_date: string;
  operational_end_date: string;
  working_start_time: string;
  working_end_time: string;
  service_radius_m: string;
  daily_capacity: string;
  current_load: string;
  priority: string;
  latitude: string;
  longitude: string;
  status: PostOfficeStatus;
}

const POST_OFFICE_STATUS_OPTIONS: Array<{
  value: PostOfficeStatus;
  label: string;
}> = [
  { value: 'ACTIVE', label: 'Active' },
  { value: 'INACTIVE', label: 'Inactive' },
  { value: 'MAINTENANCE', label: 'Maintenance' },
  { value: 'SUSPENDED', label: 'Suspended' },
];

const DEFAULT_POST_OFFICE_FORM: PostOfficeFormState = {
  code: '',
  name: '',
  province_code: '',
  ward_code: '',
  address_detail: '',
  phone_number: '',
  operational_start_date: '',
  operational_end_date: '',
  working_start_time: '',
  working_end_time: '',
  service_radius_m: '1',
  daily_capacity: '0',
  current_load: '0',
  priority: '0',
  latitude: '',
  longitude: '',
  status: 'ACTIVE',
};

const toDateInputValue = (value?: string): string => {
  if (!value) {
    return '';
  }

  return value.slice(0, 10);
};

const toTimeInputValue = (value?: string): string => {
  if (!value) {
    return '';
  }

  return value.slice(0, 5);
};

const mapPostOfficeToFormState = (
  postOffice: PostOffice
): PostOfficeFormState => {
  return {
    code: postOffice.code || '',
    name: postOffice.name || '',
    province_code: postOffice.provinceCode || '',
    ward_code: postOffice.wardCode || '',
    address_detail: postOffice.addressDetail || '',
    phone_number: postOffice.phoneNumber || '',
    operational_start_date: toDateInputValue(postOffice.operationalStartDate),
    operational_end_date: toDateInputValue(postOffice.operationalEndDate),
    working_start_time: toTimeInputValue(postOffice.workingStartTime),
    working_end_time: toTimeInputValue(postOffice.workingEndTime),
    service_radius_m: String(postOffice.serviceRadiusM ?? 1),
    daily_capacity: String(postOffice.dailyCapacity ?? 0),
    current_load: String(postOffice.currentLoad ?? 0),
    priority: String(postOffice.priority ?? 0),
    latitude:
      postOffice.latitude === undefined || postOffice.latitude === null
        ? ''
        : String(postOffice.latitude),
    longitude:
      postOffice.longitude === undefined || postOffice.longitude === null
        ? ''
        : String(postOffice.longitude),
    status: postOffice.status || 'ACTIVE',
  };
};

const validatePostOfficeForm = (values: PostOfficeFormState): string | null => {
  if (!values.code.trim()) {
    return 'Code is required.';
  }

  if (!values.name.trim()) {
    return 'Name is required.';
  }

  if (!values.province_code.trim()) {
    return 'Province code is required.';
  }

  if (!values.ward_code.trim()) {
    return 'Ward code is required.';
  }

  if (!values.address_detail.trim()) {
    return 'Address detail is required.';
  }

  const serviceRadius = Number(values.service_radius_m);
  if (!Number.isInteger(serviceRadius) || serviceRadius < 1) {
    return 'Service radius must be an integer greater than or equal to 1.';
  }

  const dailyCapacity = Number(values.daily_capacity);
  if (!Number.isInteger(dailyCapacity) || dailyCapacity < 0) {
    return 'Daily capacity must be an integer greater than or equal to 0.';
  }

  const currentLoad = Number(values.current_load);
  if (!Number.isInteger(currentLoad) || currentLoad < 0) {
    return 'Current load must be an integer greater than or equal to 0.';
  }

  const priority = Number(values.priority);
  if (!Number.isInteger(priority) || priority < 0) {
    return 'Priority must be an integer greater than or equal to 0.';
  }

  if (values.latitude.trim()) {
    const latitude = Number(values.latitude);
    if (!Number.isFinite(latitude) || latitude < -90 || latitude > 90) {
      return 'Latitude must be between -90 and 90.';
    }
  }

  if (values.longitude.trim()) {
    const longitude = Number(values.longitude);
    if (!Number.isFinite(longitude) || longitude < -180 || longitude > 180) {
      return 'Longitude must be between -180 and 180.';
    }
  }

  return null;
};

const buildCreatePostOfficeRequest = (
  values: PostOfficeFormState
): CreatePostOfficeRequest => {
  return {
    code: values.code.trim(),
    name: values.name.trim(),
    province_code: values.province_code.trim(),
    ward_code: values.ward_code.trim(),
    address_detail: values.address_detail.trim(),
    service_radius_m: Number(values.service_radius_m),
    daily_capacity: Number(values.daily_capacity),
    current_load: Number(values.current_load),
    priority: Number(values.priority),
    status: values.status,
    ...(values.phone_number.trim()
      ? { phone_number: values.phone_number.trim() }
      : {}),
    ...(values.operational_start_date
      ? { operational_start_date: values.operational_start_date }
      : {}),
    ...(values.operational_end_date
      ? { operational_end_date: values.operational_end_date }
      : {}),
    ...(values.working_start_time
      ? { working_start_time: values.working_start_time }
      : {}),
    ...(values.working_end_time
      ? { working_end_time: values.working_end_time }
      : {}),
    ...(values.latitude.trim() ? { latitude: Number(values.latitude) } : {}),
    ...(values.longitude.trim() ? { longitude: Number(values.longitude) } : {}),
  };
};

const getStatusBadgeVariant = (
  status: PostOfficeStatus
): 'default' | 'secondary' | 'outline' | 'destructive' => {
  if (status === 'ACTIVE') {
    return 'default';
  }

  if (status === 'SUSPENDED') {
    return 'destructive';
  }

  if (status === 'MAINTENANCE') {
    return 'outline';
  }

  return 'secondary';
};

export const PostOfficeListPage: React.FC = () => {
  const notification = useNotification();
  const isTmsAdmin = useAppSelector((state) =>
    state.account.user.profile?.roles?.includes('TMS_ADMIN')
  );

  const [page, setPage] = React.useState(0);
  const [keywordInput, setKeywordInput] = React.useState('');
  const [keyword, setKeyword] = React.useState<string | undefined>(undefined);
  const [formMode, setFormMode] = React.useState<PostOfficeFormMode>('create');
  const [isFormDialogOpen, setIsFormDialogOpen] = React.useState(false);
  const [editingId, setEditingId] = React.useState<number | null>(null);
  const [formValues, setFormValues] = React.useState<PostOfficeFormState>(
    DEFAULT_POST_OFFICE_FORM
  );
  const [deleteTarget, setDeleteTarget] = React.useState<PostOffice | null>(
    null
  );

  const { data, isLoading, isFetching, refetch } = useGetPostOfficesQuery({
    page,
    size: PAGE_SIZE,
    keyword,
  });

  const [createPostOffice, { isLoading: isCreating }] =
    useCreatePostOfficeMutation();
  const [updatePostOffice, { isLoading: isUpdating }] =
    useUpdatePostOfficeMutation();
  const [deletePostOffice, { isLoading: isDeleting }] =
    useDeletePostOfficeMutation();

  const isSaving = isCreating || isUpdating;

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

  const handleSearch = (event: React.FormEvent) => {
    event.preventDefault();
    setPage(0);
    setKeyword(keywordInput.trim() || undefined);
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

  const closeFormDialog = () => {
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
            <CardTitle>Search</CardTitle>
            <CardDescription>
              Use keyword to filter by code, name, or location.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSearch} className='flex gap-2'>
              <div className='relative flex-1'>
                <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
                <Input
                  className='pl-10'
                  value={keywordInput}
                  onChange={(event) => setKeywordInput(event.target.value)}
                  placeholder='Search post office...'
                />
              </div>
              <Button type='submit'>Apply</Button>
              <Button
                type='button'
                variant='outline'
                onClick={() => refetch()}
                disabled={isFetching}
              >
                <RefreshCw className='h-4 w-4 mr-2' />
                Refresh
              </Button>
            </form>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Results ({data?.totalItems ?? 0})</CardTitle>
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <div className='flex items-center gap-2 text-muted-foreground'>
                <Loader2 className='h-4 w-4 animate-spin' />
                Loading post offices...
              </div>
            ) : data && data.items.length > 0 ? (
              <div className='space-y-3'>
                {data.items.map((item) => (
                  <div
                    key={item.id}
                    className='rounded-lg border p-3 flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between'
                  >
                    <div className='space-y-1'>
                      <div className='flex items-center gap-2'>
                        <p className='font-medium'>
                          {item.code} - {item.name}
                        </p>
                        <Badge variant={getStatusBadgeVariant(item.status)}>
                          {item.status}
                        </Badge>
                      </div>
                      <p className='text-sm text-muted-foreground'>
                        {item.addressDetail}
                      </p>
                      <p className='text-xs text-muted-foreground'>
                        Province/Ward: {item.provinceCode} / {item.wardCode}
                      </p>
                      {item.latitude !== undefined &&
                        item.latitude !== null &&
                        item.longitude !== undefined &&
                        item.longitude !== null && (
                          <p className='text-xs text-muted-foreground'>
                            GPS: {item.latitude}, {item.longitude}
                          </p>
                        )}
                    </div>

                    {isTmsAdmin && (
                      <div className='flex items-center gap-2 self-end sm:self-start'>
                        <Button
                          type='button'
                          variant='outline'
                          size='sm'
                          onClick={() => handleOpenEditDialog(item)}
                          disabled={isSaving || isDeleting}
                        >
                          <Pencil className='h-4 w-4 mr-1' />
                          Edit
                        </Button>
                        <Button
                          type='button'
                          variant='destructive'
                          size='sm'
                          onClick={() => handleRequestDelete(item)}
                          disabled={isSaving || isDeleting}
                        >
                          <Trash2 className='h-4 w-4 mr-1' />
                          Delete
                        </Button>
                      </div>
                    )}
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
              <p className='text-muted-foreground'>No post offices found.</p>
            )}
          </CardContent>
        </Card>
      </div>

      <Dialog open={isFormDialogOpen} onOpenChange={closeFormDialog}>
        <DialogContent className='sm:max-w-3xl max-h-[85vh] overflow-y-auto'>
          <DialogHeader>
            <DialogTitle>
              {formMode === 'create'
                ? 'Create Post Office'
                : 'Update Post Office'}
            </DialogTitle>
            <DialogDescription>
              Fill in required fields to{' '}
              {formMode === 'create' ? 'create a new' : 'update the'} post
              office.
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleSubmitForm} className='space-y-4'>
            <div className='grid gap-4 sm:grid-cols-2'>
              <div className='space-y-2'>
                <Label htmlFor='post-office-code'>Code *</Label>
                <Input
                  id='post-office-code'
                  value={formValues.code}
                  onChange={(event) =>
                    updateFormField('code', event.target.value)
                  }
                  disabled={isSaving}
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='post-office-name'>Name *</Label>
                <Input
                  id='post-office-name'
                  value={formValues.name}
                  onChange={(event) =>
                    updateFormField('name', event.target.value)
                  }
                  disabled={isSaving}
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='post-office-province'>Province code *</Label>
                <Input
                  id='post-office-province'
                  value={formValues.province_code}
                  onChange={(event) =>
                    updateFormField('province_code', event.target.value)
                  }
                  disabled={isSaving}
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='post-office-ward'>Ward code *</Label>
                <Input
                  id='post-office-ward'
                  value={formValues.ward_code}
                  onChange={(event) =>
                    updateFormField('ward_code', event.target.value)
                  }
                  disabled={isSaving}
                />
              </div>

              <div className='space-y-2 sm:col-span-2'>
                <Label htmlFor='post-office-address'>Address detail *</Label>
                <Input
                  id='post-office-address'
                  value={formValues.address_detail}
                  onChange={(event) =>
                    updateFormField('address_detail', event.target.value)
                  }
                  disabled={isSaving}
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='post-office-phone'>Phone number</Label>
                <Input
                  id='post-office-phone'
                  value={formValues.phone_number}
                  onChange={(event) =>
                    updateFormField('phone_number', event.target.value)
                  }
                  disabled={isSaving}
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='post-office-status'>Status *</Label>
                <Select
                  value={formValues.status}
                  onValueChange={(value) =>
                    updateFormField('status', value as PostOfficeStatus)
                  }
                  disabled={isSaving}
                >
                  <SelectTrigger id='post-office-status' className='w-full'>
                    <SelectValue placeholder='Select status' />
                  </SelectTrigger>
                  <SelectContent>
                    {POST_OFFICE_STATUS_OPTIONS.map((option) => (
                      <SelectItem key={option.value} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='post-office-service-radius'>
                  Service radius (m) *
                </Label>
                <Input
                  id='post-office-service-radius'
                  type='number'
                  min={1}
                  value={formValues.service_radius_m}
                  onChange={(event) =>
                    updateFormField('service_radius_m', event.target.value)
                  }
                  disabled={isSaving}
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='post-office-daily-capacity'>
                  Daily capacity *
                </Label>
                <Input
                  id='post-office-daily-capacity'
                  type='number'
                  min={0}
                  value={formValues.daily_capacity}
                  onChange={(event) =>
                    updateFormField('daily_capacity', event.target.value)
                  }
                  disabled={isSaving}
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='post-office-current-load'>Current load *</Label>
                <Input
                  id='post-office-current-load'
                  type='number'
                  min={0}
                  value={formValues.current_load}
                  onChange={(event) =>
                    updateFormField('current_load', event.target.value)
                  }
                  disabled={isSaving}
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='post-office-priority'>Priority *</Label>
                <Input
                  id='post-office-priority'
                  type='number'
                  min={0}
                  value={formValues.priority}
                  onChange={(event) =>
                    updateFormField('priority', event.target.value)
                  }
                  disabled={isSaving}
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='post-office-latitude'>Latitude</Label>
                <Input
                  id='post-office-latitude'
                  type='number'
                  step='any'
                  min={-90}
                  max={90}
                  value={formValues.latitude}
                  onChange={(event) =>
                    updateFormField('latitude', event.target.value)
                  }
                  disabled={isSaving}
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='post-office-longitude'>Longitude</Label>
                <Input
                  id='post-office-longitude'
                  type='number'
                  step='any'
                  min={-180}
                  max={180}
                  value={formValues.longitude}
                  onChange={(event) =>
                    updateFormField('longitude', event.target.value)
                  }
                  disabled={isSaving}
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='post-office-start-date'>
                  Operational start date
                </Label>
                <Input
                  id='post-office-start-date'
                  type='date'
                  value={formValues.operational_start_date}
                  onChange={(event) =>
                    updateFormField(
                      'operational_start_date',
                      event.target.value
                    )
                  }
                  disabled={isSaving}
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='post-office-end-date'>
                  Operational end date
                </Label>
                <Input
                  id='post-office-end-date'
                  type='date'
                  value={formValues.operational_end_date}
                  onChange={(event) =>
                    updateFormField('operational_end_date', event.target.value)
                  }
                  disabled={isSaving}
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='post-office-start-time'>
                  Working start time
                </Label>
                <Input
                  id='post-office-start-time'
                  type='time'
                  value={formValues.working_start_time}
                  onChange={(event) =>
                    updateFormField('working_start_time', event.target.value)
                  }
                  disabled={isSaving}
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='post-office-end-time'>Working end time</Label>
                <Input
                  id='post-office-end-time'
                  type='time'
                  value={formValues.working_end_time}
                  onChange={(event) =>
                    updateFormField('working_end_time', event.target.value)
                  }
                  disabled={isSaving}
                />
              </div>
            </div>

            <DialogFooter>
              <Button
                type='button'
                variant='outline'
                onClick={closeFormDialog}
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
