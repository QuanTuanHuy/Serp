'use client';

import * as React from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import {
  Button,
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
  Input,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Textarea,
} from '@/shared/components/ui';
import { BUS_STATUS_OPTIONS, PROFILE_STATUS_OPTIONS } from '../constants';
import { schoolBusUi } from '../theme';
import type {
  SchoolBusAttendant,
  SchoolBusAccountUser,
  SchoolBusAttendantUpsertRequest,
  SchoolBusBus,
  SchoolBusBusType,
  SchoolBusBusUpsertRequest,
  SchoolBusDepot,
  SchoolBusDepotUpsertRequest,
  SchoolBusDriver,
  SchoolBusDriverUpsertRequest,
  SchoolBusParent,
  SchoolBusParentUpsertRequest,
  SchoolBusPickupPoint,
  SchoolBusPickupPointUpsertRequest,
  SchoolBusSchool,
  SchoolBusSchoolUpsertRequest,
  SchoolBusStudent,
  SchoolBusStudentUpsertRequest,
} from '../types';
import { toCoordinateString } from '../utils';
import { SchoolBusFormDialog } from './SchoolBusFormDialog';
import { LocationPickerMap } from './map/LocationPickerMap';

const schoolSchema = z.object({
  name: z.string().min(1, 'School name is required'),
  address: z.string().optional(),
  contactPhone: z.string().optional(),
  contactEmail: z.string().optional(),
  latitude: z.string().optional(),
  longitude: z.string().optional(),
  isActive: z.boolean().default(true),
});

const pickupPointSchema = z.object({
  schoolId: z.coerce.number().min(1, 'School is required'),
  name: z.string().min(1, 'Pickup point name is required'),
  address: z.string().min(1, 'Address is required'),
  latitude: z.string().optional(),
  longitude: z.string().optional(),
  pickupWindowStart: z.string().optional(),
  pickupWindowEnd: z.string().optional(),
  isActive: z.boolean().default(true),
});

const depotSchema = z.object({
  name: z.string().min(1, 'Depot name is required'),
  address: z.string().optional(),
  latitude: z.string().optional(),
  longitude: z.string().optional(),
  contactPhone: z.string().optional(),
  description: z.string().optional(),
  isActive: z.boolean().default(true),
});

const parentSchema = z.object({
  userId: z.coerce.number().min(1, 'User ID is required'),
  fullName: z.string().min(1, 'Full name is required'),
  phone: z.string().optional(),
  email: z.string().optional(),
  address: z.string().optional(),
  isActive: z.boolean().default(true),
});

const studentSchema = z.object({
  schoolId: z.coerce.number().min(1, 'School is required'),
  parentProfileId: z.coerce.number().min(1, 'Parent is required'),
  pickupPointId: z.string().optional(),
  fullName: z.string().min(1, 'Full name is required'),
  grade: z.string().optional(),
  homeAddress: z.string().optional(),
  isActive: z.boolean().default(true),
});

const busSchema = z.object({
  plateNumber: z.string().min(1, 'Plate number is required'),
  busType: z.string().optional(),
  capacity: z.coerce.number().min(1, 'Capacity must be at least 1'),
  status: z.string().min(1, 'Status is required'),
  isActive: z.boolean().default(true),
});

const driverSchema = z.object({
  userId: z.coerce.number().min(1, 'User ID is required'),
  fullName: z.string().min(1, 'Full name is required'),
  phone: z.string().optional(),
  licenseNumber: z.string().optional(),
  status: z.string().min(1, 'Status is required'),
  isActive: z.boolean().default(true),
});

const attendantSchema = z.object({
  userId: z.coerce.number().min(1, 'User ID is required'),
  fullName: z.string().min(1, 'Full name is required'),
  phone: z.string().optional(),
  status: z.string().min(1, 'Status is required'),
  isActive: z.boolean().default(true),
});

type SchoolFormValues = z.infer<typeof schoolSchema>;
type PickupPointFormValues = z.infer<typeof pickupPointSchema>;
type DepotFormValues = z.infer<typeof depotSchema>;
type ParentFormValues = z.infer<typeof parentSchema>;
type StudentFormValues = z.infer<typeof studentSchema>;
type BusFormValues = z.infer<typeof busSchema>;
type DriverFormValues = z.infer<typeof driverSchema>;
type AttendantFormValues = z.infer<typeof attendantSchema>;

interface BaseDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  isLoading?: boolean;
}

interface SchoolFormDialogProps extends BaseDialogProps {
  initialData?: SchoolBusSchool | null;
  onSubmit: (values: SchoolBusSchoolUpsertRequest) => Promise<void>;
}

export function SchoolFormDialog({
  open,
  onOpenChange,
  initialData,
  onSubmit,
  isLoading = false,
}: SchoolFormDialogProps) {
  const form = useForm<SchoolFormValues>({
    resolver: zodResolver(schoolSchema) as any,
    defaultValues: {
      name: initialData?.name || '',
      address: initialData?.address || '',
      contactPhone: initialData?.contactPhone || '',
      contactEmail: initialData?.contactEmail || '',
      latitude: toCoordinateString(initialData?.latitude),
      longitude: toCoordinateString(initialData?.longitude),
      isActive: initialData?.isActive ?? true,
    },
  });

  React.useEffect(() => {
    form.reset({
      name: initialData?.name || '',
      address: initialData?.address || '',
      contactPhone: initialData?.contactPhone || '',
      contactEmail: initialData?.contactEmail || '',
      latitude: toCoordinateString(initialData?.latitude),
      longitude: toCoordinateString(initialData?.longitude),
      isActive: initialData?.isActive ?? true,
    });
  }, [form, initialData]);

  return (
    <SchoolBusFormDialog
      open={open}
      onOpenChange={onOpenChange}
      title={initialData ? 'Edit school' : 'Create school'}
      description='Manage school identity and contact details for the tenant.'
    >
      <SimpleForm
        form={form}
        isLoading={isLoading}
        onSubmit={async (values) =>
          onSubmit({
            name: values.name,
            address: values.address || undefined,
            contactPhone: values.contactPhone || undefined,
            contactEmail: values.contactEmail || undefined,
            latitude: values.latitude ? Number(values.latitude) : null,
            longitude: values.longitude ? Number(values.longitude) : null,
            isActive: values.isActive,
          })
        }
        onCancel={() => onOpenChange(false)}
      >
        <TextField form={form} name='name' label='School name' />
        {initialData?.code ? (
          <ReadOnlyField label='School code' value={initialData.code} />
        ) : null}
        <TextareaField form={form} name='address' label='Address' />
        <TextField form={form} name='contactPhone' label='Contact phone' />
        <TextField form={form} name='contactEmail' label='Contact email' />
        <TextField form={form} name='latitude' label='Latitude' />
        <TextField form={form} name='longitude' label='Longitude' />
        <div className='md:col-span-2'>
          <LocationPickerMap
            value={{
              latitude: form.watch('latitude')
                ? Number(form.watch('latitude'))
                : null,
              longitude: form.watch('longitude')
                ? Number(form.watch('longitude'))
                : null,
            }}
            onChange={({ latitude, longitude }) => {
              form.setValue('latitude', latitude.toFixed(6), {
                shouldDirty: true,
              });
              form.setValue('longitude', longitude.toFixed(6), {
                shouldDirty: true,
              });
            }}
            onAddressResolved={(address) =>
              form.setValue('address', address, { shouldDirty: true })
            }
            title='School location'
          />
        </div>
      </SimpleForm>
    </SchoolBusFormDialog>
  );
}

interface PickupPointFormDialogProps extends BaseDialogProps {
  initialData?: SchoolBusPickupPoint | null;
  schools: SchoolBusSchool[];
  onSubmit: (values: SchoolBusPickupPointUpsertRequest) => Promise<void>;
}

export function PickupPointFormDialog({
  open,
  onOpenChange,
  initialData,
  schools,
  onSubmit,
  isLoading = false,
}: PickupPointFormDialogProps) {
  const form = useForm<PickupPointFormValues>({
    resolver: zodResolver(pickupPointSchema) as any,
    defaultValues: {
      schoolId: initialData?.schoolId ?? schools[0]?.id ?? 0,
      name: initialData?.name || '',
      address: initialData?.address || '',
      latitude:
        initialData?.latitude === null || initialData?.latitude === undefined
          ? ''
          : String(initialData.latitude),
      longitude:
        initialData?.longitude === null || initialData?.longitude === undefined
          ? ''
          : String(initialData.longitude),
      pickupWindowStart: initialData?.pickupWindowStart || '',
      pickupWindowEnd: initialData?.pickupWindowEnd || '',
      isActive: initialData?.isActive ?? true,
    },
  });

  React.useEffect(() => {
    form.reset({
      schoolId: initialData?.schoolId ?? schools[0]?.id ?? 0,
      name: initialData?.name || '',
      address: initialData?.address || '',
      latitude:
        initialData?.latitude === null || initialData?.latitude === undefined
          ? ''
          : String(initialData.latitude),
      longitude:
        initialData?.longitude === null || initialData?.longitude === undefined
          ? ''
          : String(initialData.longitude),
      pickupWindowStart: initialData?.pickupWindowStart || '',
      pickupWindowEnd: initialData?.pickupWindowEnd || '',
      isActive: initialData?.isActive ?? true,
    });
  }, [form, initialData, schools]);

  return (
    <SchoolBusFormDialog
      open={open}
      onOpenChange={onOpenChange}
      title={initialData ? 'Edit pickup point' : 'Create pickup point'}
      description='Register boarding locations for request intake and route planning.'
    >
      <SimpleForm
        form={form}
        isLoading={isLoading}
        onCancel={() => onOpenChange(false)}
        onSubmit={async (values) =>
          onSubmit({
            schoolId: values.schoolId,
            name: values.name,
            address: values.address,
            latitude: values.latitude ? Number(values.latitude) : null,
            longitude: values.longitude ? Number(values.longitude) : null,
            pickupWindowStart: values.pickupWindowStart || null,
            pickupWindowEnd: values.pickupWindowEnd || null,
            isActive: values.isActive,
          })
        }
      >
        <SelectField
          form={form}
          name='schoolId'
          label='School'
          options={schools.map((school) => ({
            value: String(school.id),
            label: school.name,
          }))}
        />
        <TextField form={form} name='name' label='Pickup point name' />
        <TextareaField form={form} name='address' label='Address' />
        <TextField form={form} name='latitude' label='Latitude' />
        <TextField form={form} name='longitude' label='Longitude' />
        <TextField
          form={form}
          name='pickupWindowStart'
          label='Pickup window start'
          type='time'
        />
        <TextField
          form={form}
          name='pickupWindowEnd'
          label='Pickup window end'
          type='time'
        />
        <div className='md:col-span-2'>
          <LocationPickerMap
            value={{
              latitude: form.watch('latitude')
                ? Number(form.watch('latitude'))
                : null,
              longitude: form.watch('longitude')
                ? Number(form.watch('longitude'))
                : null,
            }}
            referenceMarkers={
              schools
                .filter(
                  (school) =>
                    school.id === Number(form.watch('schoolId')) &&
                    typeof school.latitude === 'number' &&
                    typeof school.longitude === 'number'
                )
                .map((school) => ({
                  id: `school-${school.id}`,
                  name: school.name,
                  address: school.address,
                  latitude: school.latitude,
                  longitude: school.longitude,
                  type: 'school' as const,
                }))
            }
            onChange={({ latitude, longitude }) => {
              form.setValue('latitude', latitude.toFixed(6), {
                shouldDirty: true,
              });
              form.setValue('longitude', longitude.toFixed(6), {
                shouldDirty: true,
              });
            }}
            onAddressResolved={(address) =>
              form.setValue('address', address, { shouldDirty: true })
            }
            title='Pickup point location'
          />
        </div>
      </SimpleForm>
    </SchoolBusFormDialog>
  );
}

interface DepotFormDialogProps extends BaseDialogProps {
  initialData?: SchoolBusDepot | null;
  onSubmit: (values: SchoolBusDepotUpsertRequest) => Promise<void>;
}

export function DepotFormDialog({
  open,
  onOpenChange,
  initialData,
  onSubmit,
  isLoading = false,
}: DepotFormDialogProps) {
  const form = useForm<DepotFormValues>({
    resolver: zodResolver(depotSchema) as any,
    defaultValues: {
      name: initialData?.name || '',
      address: initialData?.address || '',
      latitude: toCoordinateString(initialData?.latitude),
      longitude: toCoordinateString(initialData?.longitude),
      contactPhone: initialData?.contactPhone || '',
      description: initialData?.description || '',
      isActive: initialData?.isActive ?? true,
    },
  });

  React.useEffect(() => {
    form.reset({
      name: initialData?.name || '',
      address: initialData?.address || '',
      latitude: toCoordinateString(initialData?.latitude),
      longitude: toCoordinateString(initialData?.longitude),
      contactPhone: initialData?.contactPhone || '',
      description: initialData?.description || '',
      isActive: initialData?.isActive ?? true,
    });
  }, [form, initialData]);

  return (
    <SchoolBusFormDialog
      open={open}
      onOpenChange={onOpenChange}
      title={initialData ? 'Edit depot' : 'Create depot'}
      description='Register a fixed bus yard used as a route start or end point.'
    >
      <SimpleForm
        form={form}
        isLoading={isLoading}
        onCancel={() => onOpenChange(false)}
        onSubmit={async (values) =>
          onSubmit({
            name: values.name,
            address: values.address || undefined,
            latitude: values.latitude ? Number(values.latitude) : null,
            longitude: values.longitude ? Number(values.longitude) : null,
            contactPhone: values.contactPhone || undefined,
            description: values.description || undefined,
            isActive: values.isActive,
          })
        }
      >
        <TextField form={form} name='name' label='Depot name' />
        <TextField form={form} name='contactPhone' label='Contact phone' />
        <TextareaField form={form} name='address' label='Address' />
        <TextField form={form} name='latitude' label='Latitude' />
        <TextField form={form} name='longitude' label='Longitude' />
        <TextareaField form={form} name='description' label='Description' />
        <div className='md:col-span-2'>
          <LocationPickerMap
            value={{
              latitude: form.watch('latitude')
                ? Number(form.watch('latitude'))
                : null,
              longitude: form.watch('longitude')
                ? Number(form.watch('longitude'))
                : null,
            }}
            onChange={({ latitude, longitude }) => {
              form.setValue('latitude', latitude.toFixed(6), {
                shouldDirty: true,
              });
              form.setValue('longitude', longitude.toFixed(6), {
                shouldDirty: true,
              });
            }}
            onAddressResolved={(address) =>
              form.setValue('address', address, { shouldDirty: true })
            }
            title='Depot location'
          />
        </div>
      </SimpleForm>
    </SchoolBusFormDialog>
  );
}

interface ParentFormDialogProps extends BaseDialogProps {
  initialData?: SchoolBusParent | null;
  accountUsers: SchoolBusAccountUser[];
  isLoadingAccountUsers?: boolean;
  onSubmit: (values: SchoolBusParentUpsertRequest) => Promise<void>;
}

export function ParentFormDialog({
  open,
  onOpenChange,
  initialData,
  accountUsers,
  isLoadingAccountUsers = false,
  onSubmit,
  isLoading = false,
}: ParentFormDialogProps) {
  const form = useForm<ParentFormValues>({
    resolver: zodResolver(parentSchema) as any,
    defaultValues: {
      userId: initialData?.userId ?? 0,
      fullName: initialData?.fullName || '',
      phone: initialData?.phone || '',
      email: initialData?.email || '',
      address: initialData?.address || '',
      isActive: initialData?.isActive ?? true,
    },
  });

  React.useEffect(() => {
    form.reset({
      userId: initialData?.userId ?? 0,
      fullName: initialData?.fullName || '',
      phone: initialData?.phone || '',
      email: initialData?.email || '',
      address: initialData?.address || '',
      isActive: initialData?.isActive ?? true,
    });
  }, [form, initialData]);

  const isEditMode = Boolean(initialData);
  const selectedUserId = Number(form.watch('userId') || 0);
  const selectedUser = accountUsers.find((user) => user.id === selectedUserId);

  const handleAccountUserChange = (value: string) => {
    const userId = Number(value);
    const user = accountUsers.find((candidate) => candidate.id === userId);

    form.setValue('userId', userId, {
      shouldDirty: true,
      shouldValidate: true,
    });

    if (!user || isEditMode) {
      return;
    }

    const fullName = getAccountUserFullName(user);
    if (fullName) {
      form.setValue('fullName', fullName, {
        shouldDirty: true,
        shouldValidate: true,
      });
    }
    if (user.email) {
      form.setValue('email', user.email, { shouldDirty: true });
    }
    if (user.phoneNumber) {
      form.setValue('phone', user.phoneNumber, { shouldDirty: true });
    }
  };

  return (
    <SchoolBusFormDialog
      open={open}
      onOpenChange={onOpenChange}
      title={initialData ? 'Edit parent profile' : 'Create parent profile'}
      description='Store the school-bus operational profile linked to an account user.'
    >
      <SimpleForm
        form={form}
        isLoading={isLoading}
        onCancel={() => onOpenChange(false)}
        onSubmit={onSubmit}
      >
        <SelectField
          form={form}
          name='userId'
          label='Account user'
          className='md:col-span-2'
          disabled={isLoadingAccountUsers || isEditMode || accountUsers.length === 0}
          description={
            isEditMode
              ? 'Account user is locked after the profile is linked.'
              : accountUsers.length === 0
                ? 'No School Bus account users available. Create and grant module access in Account first.'
                : selectedUser
                  ? `Selected #${selectedUser.id} - ${selectedUser.email}`
                  : 'Select an account user already granted School Bus access.'
          }
          placeholder={
            isLoadingAccountUsers ? 'Loading account users...' : 'Select account user'
          }
          options={accountUsers.map((user) => ({
            value: String(user.id),
            label: getAccountUserOptionLabel(user),
          }))}
          onChange={handleAccountUserChange}
        />
        <TextField form={form} name='fullName' label='Full name' />
        <TextField form={form} name='phone' label='Phone' />
        <TextField form={form} name='email' label='Email' />
        <TextareaField form={form} name='address' label='Address' />
      </SimpleForm>
    </SchoolBusFormDialog>
  );
}

function getAccountUserFullName(user: SchoolBusAccountUser) {
  return [user.firstName, user.lastName].filter(Boolean).join(' ').trim();
}

function getAccountUserOptionLabel(user: SchoolBusAccountUser) {
  const fullName = getAccountUserFullName(user) || user.email || `User #${user.id}`;
  return `${fullName} - ${user.email} - #${user.id}`;
}

interface StudentFormDialogProps extends BaseDialogProps {
  initialData?: SchoolBusStudent | null;
  schools: SchoolBusSchool[];
  parents: SchoolBusParent[];
  pickupPoints: SchoolBusPickupPoint[];
  onSubmit: (values: SchoolBusStudentUpsertRequest) => Promise<void>;
}

export function StudentFormDialog({
  open,
  onOpenChange,
  initialData,
  schools,
  parents,
  pickupPoints,
  onSubmit,
  isLoading = false,
}: StudentFormDialogProps) {
  const form = useForm<StudentFormValues>({
    resolver: zodResolver(studentSchema) as any,
    defaultValues: {
      schoolId: initialData?.schoolId ?? schools[0]?.id ?? 0,
      parentProfileId: initialData?.parentProfileId ?? parents[0]?.id ?? 0,
      pickupPointId:
        initialData?.pickupPointId === null || initialData?.pickupPointId === undefined
          ? ''
          : String(initialData.pickupPointId),
      fullName: initialData?.fullName || '',
      grade: initialData?.grade || '',
      homeAddress: initialData?.homeAddress || '',
      isActive: initialData?.isActive ?? true,
    },
  });

  React.useEffect(() => {
    form.reset({
      schoolId: initialData?.schoolId ?? schools[0]?.id ?? 0,
      parentProfileId: initialData?.parentProfileId ?? parents[0]?.id ?? 0,
      pickupPointId:
        initialData?.pickupPointId === null || initialData?.pickupPointId === undefined
          ? ''
          : String(initialData.pickupPointId),
      fullName: initialData?.fullName || '',
      grade: initialData?.grade || '',
      homeAddress: initialData?.homeAddress || '',
      isActive: initialData?.isActive ?? true,
    });
  }, [form, initialData, schools, parents]);

  return (
    <SchoolBusFormDialog
      open={open}
      onOpenChange={onOpenChange}
      title={initialData ? 'Edit student' : 'Create student'}
      description='Manage the roster used by transport requests, routing, and attendance.'
    >
      <SimpleForm
        form={form}
        isLoading={isLoading}
        onCancel={() => onOpenChange(false)}
        onSubmit={async (values) =>
          onSubmit({
            schoolId: values.schoolId,
            parentProfileId: values.parentProfileId,
            pickupPointId: values.pickupPointId ? Number(values.pickupPointId) : null,
            fullName: values.fullName,
            grade: values.grade || undefined,
            homeAddress: values.homeAddress || undefined,
            isActive: values.isActive,
          })
        }
      >
        <SelectField
          form={form}
          name='schoolId'
          label='School'
          options={schools.map((school) => ({
            value: String(school.id),
            label: school.name,
          }))}
        />
        <SelectField
          form={form}
          name='parentProfileId'
          label='Parent'
          options={parents.map((parent) => ({
            value: String(parent.id),
            label: parent.fullName,
          }))}
        />
        <SelectField
          form={form}
          name='pickupPointId'
          label='Pickup point'
          allowEmpty
          emptyLabel='No pickup point'
          options={pickupPoints.map((pickupPoint) => ({
            value: String(pickupPoint.id),
            label: `${pickupPoint.name} (${pickupPoint.schoolName})`,
          }))}
        />
        <TextField form={form} name='fullName' label='Full name' />
        {initialData?.studentCode ? (
          <ReadOnlyField label='Student code' value={initialData.studentCode} />
        ) : null}
        <TextField form={form} name='grade' label='Grade' />
        <TextareaField form={form} name='homeAddress' label='Home address' />
      </SimpleForm>
    </SchoolBusFormDialog>
  );
}

interface BusFormDialogProps extends BaseDialogProps {
  initialData?: SchoolBusBus | null;
  busTypes: SchoolBusBusType[];
  isLoadingBusTypes?: boolean;
  onSubmit: (values: SchoolBusBusUpsertRequest) => Promise<void>;
}

export function BusFormDialog({
  open,
  onOpenChange,
  initialData,
  busTypes,
  isLoadingBusTypes = false,
  onSubmit,
  isLoading = false,
}: BusFormDialogProps) {
  const defaultBusType = initialData?.busType || busTypes[0]?.code || '';
  const defaultBusTypeMeta = busTypes.find(
    (busType) => busType.code === defaultBusType
  );
  const form = useForm<BusFormValues>({
    resolver: zodResolver(busSchema) as any,
    defaultValues: {
      plateNumber: initialData?.plateNumber || '',
      busType: defaultBusType,
      capacity:
        initialData?.capacity ??
        (defaultBusTypeMeta?.value && defaultBusTypeMeta.value > 0
          ? defaultBusTypeMeta.value
          : 1),
      status: initialData?.status || BUS_STATUS_OPTIONS[0].value,
      isActive: initialData?.isActive ?? true,
    },
  });

  React.useEffect(() => {
    const nextBusType = initialData?.busType || busTypes[0]?.code || '';
    const nextBusTypeMeta = busTypes.find(
      (busType) => busType.code === nextBusType
    );
    form.reset({
      plateNumber: initialData?.plateNumber || '',
      busType: nextBusType,
      capacity:
        initialData?.capacity ??
        (nextBusTypeMeta?.value && nextBusTypeMeta.value > 0
          ? nextBusTypeMeta.value
          : 1),
      status: initialData?.status || BUS_STATUS_OPTIONS[0].value,
      isActive: initialData?.isActive ?? true,
    });
  }, [form, initialData, busTypes]);

  const busTypeOptions = React.useMemo(() => {
    if (
      initialData?.busType &&
      !busTypes.some((busType) => busType.code === initialData.busType)
    ) {
      return [
        {
          code: initialData.busType,
          value: initialData.capacity,
          description: `${initialData.busType} (legacy)`,
        },
        ...busTypes,
      ];
    }

    return busTypes;
  }, [busTypes, initialData]);

  const selectedBusTypeCode = form.watch('busType');
  const selectedBusType = busTypeOptions.find(
    (busType) => busType.code === selectedBusTypeCode
  );
  const isCustomBusType =
    selectedBusTypeCode === 'CUSTOM_BUS' || !selectedBusType;

  const handleBusTypeChange = (value: string) => {
    const busType = busTypeOptions.find((option) => option.code === value);

    form.setValue('busType', value, {
      shouldDirty: true,
      shouldValidate: true,
    });

    if (busType?.value && busType.value > 0) {
      form.setValue('capacity', busType.value, {
        shouldDirty: true,
        shouldValidate: true,
      });
      return;
    }

    if (!form.getValues('capacity') || Number(form.getValues('capacity')) < 1) {
      form.setValue('capacity', 1, {
        shouldDirty: true,
        shouldValidate: true,
      });
    }
  };

  return (
    <SchoolBusFormDialog
      open={open}
      onOpenChange={onOpenChange}
      title={initialData ? 'Edit bus' : 'Create bus'}
      description='Maintain the fleet inventory used for assignment.'
    >
      <SimpleForm
        form={form}
        isLoading={isLoading}
        onCancel={() => onOpenChange(false)}
        onSubmit={onSubmit}
      >
        <TextField form={form} name='plateNumber' label='Plate number' />
        <SelectField
          form={form}
          name='busType'
          label='Bus type'
          disabled={isLoadingBusTypes || busTypeOptions.length === 0}
          description={
            busTypeOptions.length === 0
              ? 'No bus types available from backend.'
              : selectedBusType
                ? `${selectedBusType.description} - capacity ${selectedBusType.value || 'custom'}`
                : 'Select a bus type.'
          }
          placeholder={isLoadingBusTypes ? 'Loading bus types...' : 'Select bus type'}
          options={busTypeOptions.map((busType) => ({
            value: busType.code,
            label: `${busType.description} (${busType.code})`,
          }))}
          onChange={handleBusTypeChange}
        />
        <TextField
          form={form}
          name='capacity'
          label='Capacity'
          type='number'
          disabled={!isCustomBusType}
          description={
            isCustomBusType
              ? 'Custom bus capacity can be edited.'
              : 'Capacity is auto-filled from the selected bus type.'
          }
        />
        <SelectField
          form={form}
          name='status'
          label='Status'
          options={BUS_STATUS_OPTIONS.map((option) => ({
            value: option.value,
            label: option.label,
          }))}
        />
      </SimpleForm>
    </SchoolBusFormDialog>
  );
}

interface DriverFormDialogProps extends BaseDialogProps {
  initialData?: SchoolBusDriver | null;
  accountUsers: SchoolBusAccountUser[];
  isLoadingAccountUsers?: boolean;
  onSubmit: (values: SchoolBusDriverUpsertRequest) => Promise<void>;
}

export function DriverFormDialog({
  open,
  onOpenChange,
  initialData,
  accountUsers,
  isLoadingAccountUsers = false,
  onSubmit,
  isLoading = false,
}: DriverFormDialogProps) {
  const form = useForm<DriverFormValues>({
    resolver: zodResolver(driverSchema) as any,
    defaultValues: {
      userId: initialData?.userId ?? 0,
      fullName: initialData?.fullName || '',
      phone: initialData?.phone || '',
      licenseNumber: initialData?.licenseNumber || '',
      status: initialData?.status || PROFILE_STATUS_OPTIONS[0].value,
      isActive: initialData?.isActive ?? true,
    },
  });

  React.useEffect(() => {
    form.reset({
      userId: initialData?.userId ?? 0,
      fullName: initialData?.fullName || '',
      phone: initialData?.phone || '',
      licenseNumber: initialData?.licenseNumber || '',
      status: initialData?.status || PROFILE_STATUS_OPTIONS[0].value,
      isActive: initialData?.isActive ?? true,
    });
  }, [form, initialData]);

  const isEditMode = Boolean(initialData);
  const selectedUserId = Number(form.watch('userId') || 0);
  const selectedUser = accountUsers.find((user) => user.id === selectedUserId);

  const handleAccountUserChange = (value: string) => {
    const userId = Number(value);
    const user = accountUsers.find((candidate) => candidate.id === userId);

    form.setValue('userId', userId, {
      shouldDirty: true,
      shouldValidate: true,
    });

    if (!user || isEditMode) {
      return;
    }

    const fullName = getAccountUserFullName(user);
    if (fullName) {
      form.setValue('fullName', fullName, {
        shouldDirty: true,
        shouldValidate: true,
      });
    }
    if (user.phoneNumber) {
      form.setValue('phone', user.phoneNumber, { shouldDirty: true });
    }
  };

  return (
    <SchoolBusFormDialog
      open={open}
      onOpenChange={onOpenChange}
      title={initialData ? 'Edit driver' : 'Create driver'}
      description='Manage driver availability and license metadata.'
    >
      <SimpleForm
        form={form}
        isLoading={isLoading}
        onCancel={() => onOpenChange(false)}
        onSubmit={onSubmit}
      >
        <SelectField
          form={form}
          name='userId'
          label='Account user'
          className='md:col-span-2'
          disabled={isLoadingAccountUsers || isEditMode || accountUsers.length === 0}
          description={
            isEditMode
              ? 'Account user is locked after the driver profile is linked.'
              : accountUsers.length === 0
                ? 'No School Bus account users available. Create and grant module access in Account first.'
                : selectedUser
                  ? `Selected #${selectedUser.id} - ${selectedUser.email}`
                  : 'Select an account user already granted School Bus access.'
          }
          placeholder={
            isLoadingAccountUsers ? 'Loading account users...' : 'Select account user'
          }
          options={accountUsers.map((user) => ({
            value: String(user.id),
            label: getAccountUserOptionLabel(user),
          }))}
          onChange={handleAccountUserChange}
        />
        <TextField form={form} name='fullName' label='Full name' />
        <TextField form={form} name='phone' label='Phone' />
        <TextField form={form} name='licenseNumber' label='License number' />
        <SelectField
          form={form}
          name='status'
          label='Status'
          options={PROFILE_STATUS_OPTIONS.map((option) => ({
            value: option.value,
            label: option.label,
          }))}
        />
      </SimpleForm>
    </SchoolBusFormDialog>
  );
}

interface AttendantFormDialogProps extends BaseDialogProps {
  initialData?: SchoolBusAttendant | null;
  accountUsers: SchoolBusAccountUser[];
  isLoadingAccountUsers?: boolean;
  onSubmit: (values: SchoolBusAttendantUpsertRequest) => Promise<void>;
}

export function AttendantFormDialog({
  open,
  onOpenChange,
  initialData,
  accountUsers,
  isLoadingAccountUsers = false,
  onSubmit,
  isLoading = false,
}: AttendantFormDialogProps) {
  const form = useForm<AttendantFormValues>({
    resolver: zodResolver(attendantSchema) as any,
    defaultValues: {
      userId: initialData?.userId ?? 0,
      fullName: initialData?.fullName || '',
      phone: initialData?.phone || '',
      status: initialData?.status || PROFILE_STATUS_OPTIONS[0].value,
      isActive: initialData?.isActive ?? true,
    },
  });

  React.useEffect(() => {
    form.reset({
      userId: initialData?.userId ?? 0,
      fullName: initialData?.fullName || '',
      phone: initialData?.phone || '',
      status: initialData?.status || PROFILE_STATUS_OPTIONS[0].value,
      isActive: initialData?.isActive ?? true,
    });
  }, [form, initialData]);

  const isEditMode = Boolean(initialData);
  const selectedUserId = Number(form.watch('userId') || 0);
  const selectedUser = accountUsers.find((user) => user.id === selectedUserId);

  const handleAccountUserChange = (value: string) => {
    const userId = Number(value);
    const user = accountUsers.find((candidate) => candidate.id === userId);

    form.setValue('userId', userId, {
      shouldDirty: true,
      shouldValidate: true,
    });

    if (!user || isEditMode) {
      return;
    }

    const fullName = getAccountUserFullName(user);
    if (fullName) {
      form.setValue('fullName', fullName, {
        shouldDirty: true,
        shouldValidate: true,
      });
    }
    if (user.phoneNumber) {
      form.setValue('phone', user.phoneNumber, { shouldDirty: true });
    }
  };

  return (
    <SchoolBusFormDialog
      open={open}
      onOpenChange={onOpenChange}
      title={initialData ? 'Edit attendant' : 'Create attendant'}
      description='Manage on-board support staff for route execution.'
    >
      <SimpleForm
        form={form}
        isLoading={isLoading}
        onCancel={() => onOpenChange(false)}
        onSubmit={onSubmit}
      >
        <SelectField
          form={form}
          name='userId'
          label='Account user'
          className='md:col-span-2'
          disabled={isLoadingAccountUsers || isEditMode || accountUsers.length === 0}
          description={
            isEditMode
              ? 'Account user is locked after the attendant profile is linked.'
              : accountUsers.length === 0
                ? 'No School Bus account users available. Create and grant module access in Account first.'
                : selectedUser
                  ? `Selected #${selectedUser.id} - ${selectedUser.email}`
                  : 'Select an account user already granted School Bus access.'
          }
          placeholder={
            isLoadingAccountUsers ? 'Loading account users...' : 'Select account user'
          }
          options={accountUsers.map((user) => ({
            value: String(user.id),
            label: getAccountUserOptionLabel(user),
          }))}
          onChange={handleAccountUserChange}
        />
        <TextField form={form} name='fullName' label='Full name' />
        <TextField form={form} name='phone' label='Phone' />
        <SelectField
          form={form}
          name='status'
          label='Status'
          options={PROFILE_STATUS_OPTIONS.map((option) => ({
            value: option.value,
            label: option.label,
          }))}
        />
      </SimpleForm>
    </SchoolBusFormDialog>
  );
}

interface SimpleFormProps {
  form: any;
  onSubmit: (values: any) => Promise<void>;
  onCancel: () => void;
  isLoading?: boolean;
  children: React.ReactNode;
}

function SimpleForm({
  form,
  onSubmit,
  onCancel,
  isLoading = false,
  children,
}: SimpleFormProps) {
  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className='space-y-4'>
        <div className='grid gap-4 md:grid-cols-2'>{children}</div>
        <div className='flex justify-end gap-2 border-t pt-4'>
          <Button
            type='button'
            variant='outline'
            className={schoolBusUi.outlineButton}
            onClick={onCancel}
            disabled={isLoading}
          >
            Cancel
          </Button>
          <Button
            type='submit'
            className={schoolBusUi.primaryButton}
            disabled={isLoading}
          >
            {isLoading ? 'Saving...' : 'Save'}
          </Button>
        </div>
      </form>
    </Form>
  );
}

interface FieldProps {
  form: any;
  name: string;
  label: string;
}

function TextField({
  form,
  name,
  label,
  type = 'text',
  disabled = false,
  description,
}: FieldProps & { type?: string; disabled?: boolean; description?: string }) {
  return (
    <FormField
      control={form.control}
      name={name as any}
      render={({ field }) => (
        <FormItem>
          <FormLabel>{label}</FormLabel>
          <FormControl>
            <Input
              {...field}
              type={type}
              value={(field.value as string) ?? ''}
              disabled={disabled}
            />
          </FormControl>
          {description ? (
            <FormDescription>{description}</FormDescription>
          ) : null}
          <FormMessage />
        </FormItem>
      )}
    />
  );
}

function ReadOnlyField({ label, value }: { label: string; value: string }) {
  return (
    <div className='space-y-2'>
      <FormLabel>{label}</FormLabel>
      <div className='rounded-xl border border-rose-100 bg-rose-50/70 px-3 py-2 text-sm font-semibold text-rose-700'>
        {value}
      </div>
    </div>
  );
}

function TextareaField({
  form,
  name,
  label,
}: FieldProps) {
  return (
    <FormField
      control={form.control}
      name={name as any}
      render={({ field }) => (
        <FormItem className='md:col-span-2'>
          <FormLabel>{label}</FormLabel>
          <FormControl>
            <Textarea {...field} value={(field.value as string) ?? ''} />
          </FormControl>
          <FormMessage />
        </FormItem>
      )}
    />
  );
}

function SelectField({
  form,
  name,
  label,
  options,
  allowEmpty = false,
  emptyLabel = 'None',
  emptyValue = '__none__',
  disabled = false,
  description,
  placeholder,
  onChange,
  className,
}: FieldProps & {
  options: Array<{ value: string; label: string }>;
  allowEmpty?: boolean;
  emptyLabel?: string;
  emptyValue?: string;
  disabled?: boolean;
  description?: string;
  placeholder?: string;
  onChange?: (value: string) => void;
  className?: string;
}) {
  return (
    <FormField
      control={form.control}
      name={name as any}
      render={({ field }) => (
        <FormItem className={className}>
          <FormLabel>{label}</FormLabel>
          <Select
            onValueChange={(value) =>
              onChange
                ? onChange(value === emptyValue ? '' : value)
                : field.onChange(value === emptyValue ? '' : value)
            }
            value={String(field.value ?? (allowEmpty ? emptyValue : ''))}
            disabled={disabled}
          >
            <FormControl>
              <SelectTrigger className='h-11 w-full min-w-0 max-w-full rounded-xl border-slate-200 bg-white px-3 text-left shadow-sm [&_[data-slot=select-value]]:block [&_[data-slot=select-value]]:max-w-[calc(100%-1.75rem)] [&_[data-slot=select-value]]:truncate'>
                <SelectValue
                  placeholder={placeholder || `Select ${label.toLowerCase()}`}
                />
              </SelectTrigger>
            </FormControl>
            <SelectContent className='z-[120] max-h-72 w-[var(--radix-select-trigger-width)] min-w-[var(--radix-select-trigger-width)] max-w-[min(860px,calc(100vw-2rem))] overflow-x-hidden rounded-xl border-slate-200 bg-white'>
              {allowEmpty ? (
                <SelectItem value={emptyValue} className='max-w-full pr-10'>
                  <span className='block max-w-full truncate'>{emptyLabel}</span>
                </SelectItem>
              ) : null}
              {options.map((option) => (
                <SelectItem
                  key={option.value}
                  value={option.value}
                  className='max-w-full pr-10'
                >
                  <span className='block max-w-full truncate'>
                    {option.label}
                  </span>
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          {description ? (
            <FormDescription>{description}</FormDescription>
          ) : null}
          <FormMessage />
        </FormItem>
      )}
    />
  );
}
