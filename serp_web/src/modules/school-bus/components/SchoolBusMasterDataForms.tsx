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
import { cn } from '@/shared/utils';
import { BUS_STATUS_OPTIONS, PROFILE_STATUS_OPTIONS, STAFF_STATUS_OPTIONS } from '../constants';
import { schoolBusUi } from '../theme';
import { SchoolBusSelect } from './ui/SchoolBusSelect';
import { SchoolBusDatePicker } from './ui/SchoolBusDatePicker';
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
  SchoolBusSchoolPickupPoint,
  SchoolBusStudent,
  SchoolBusStudentUpsertRequest,
} from '../types';
import { SHIFT_TYPE_OPTIONS } from '../constants';
import { toCoordinateString } from '../utils';
import { SchoolBusFormDialog } from './SchoolBusFormDialog';
import { LocationPickerMap } from './map/LocationPickerMap';

const schoolSchema = z.object({
  name: z.string().min(1, 'School name is required'),
  address: z.string().optional(),
  contactPhone: z.string().optional(),
  contactEmail: z.string().optional().refine(
    (v) => !v || /^[\w.+-]+@[\w.-]+\.[a-zA-Z]{2,}$/.test(v),
    { message: 'Invalid email format' }
  ),
  latitude: z.string().optional(),
  longitude: z.string().optional(),
  isActive: z.boolean().default(true),
});

const pickupPointSchema = z.object({
  name: z.string().min(1, 'Pickup point name is required'),
  address: z.string().min(1, 'Address is required'),
  latitude: z.string().optional(),
  longitude: z.string().optional(),
  zoneCode: z.string().optional(),
  usageType: z.string().min(1, 'Usage type is required'),
  pickupInstruction: z.string().optional(),
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
  phone: z.string().min(1, 'Phone is required'),
  email: z.string().optional().refine(
    (v) => !v || /^[\w.+-]+@[\w.-]+\.[a-zA-Z]{2,}$/.test(v),
    { message: 'Invalid email format' }
  ),
  address: z.string().optional(),
  isActive: z.boolean().default(true),
});

const studentSchema = z.object({
  schoolId: z.coerce.number().min(1, 'School is required'),
  parentProfileId: z.coerce.number().min(1, 'Parent is required'),
  pickupPointId: z.string().optional(),
  defaultDropoffPointId: z.string().optional(),
  fullName: z.string().min(1, 'Full name is required'),
  grade: z.string().optional(),
  className: z.string().optional(),
  dateOfBirth: z.string().optional(),
  gender: z.string().optional(),
  homeAddress: z.string().optional(),
  emergencyContactName: z.string().optional(),
  emergencyContactPhone: z.string().optional(),
  specialNote: z.string().optional(),
  isActive: z.boolean().default(true),
});

const busSchema = z.object({
  plateNumber: z.string().min(1, 'Plate number is required'),
  busType: z.string().optional(),
  capacity: z.coerce.number().min(1, 'Capacity must be at least 1'),
  status: z.string().min(1, 'Status is required'),
  homeDepotId: z.string().min(1, 'Home depot is required'),
  isActive: z.boolean().default(true),
});

const driverSchema = z.object({
  userId: z.coerce.number().min(1, 'User ID is required'),
  fullName: z.string().min(1, 'Full name is required'),
  phone: z.string().optional(),
  licenseNumber: z.string().min(1, 'License number is required'),
  licenseClass: z.string().min(1, 'License class is required'),
  licenseExpiryDate: z.string().min(1, 'License expiry date is required'),
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
      stickyFooter
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
        stickyFooter
      >
        <FormSectionHeader title='1. School information' />
        <TextField form={form} name='name' label='School name *' className='md:col-span-2' />
        {initialData?.code ? (
          <ReadOnlyField label='School code' value={initialData.code} className='md:col-span-1' />
        ) : null}

        <FormSectionHeader title='2. Contact details' />
        <TextField form={form} name='contactPhone' label='Contact phone' className='md:col-span-1' />
        <TextField form={form} name='contactEmail' label='Contact email' className='md:col-span-1' />
        <TextareaField form={form} name='address' label='Address' />

        <FormSectionHeader title='3. Coordinates' />
        <TextField form={form} name='latitude' label='Latitude' />
        <TextField form={form} name='longitude' label='Longitude' />

        <FormSectionHeader title='4. School location map' />
        <div className='col-span-full'>
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
      zoneCode: initialData?.zoneCode || '',
      usageType: initialData?.usageType || '',
      pickupInstruction: initialData?.pickupInstruction || '',
      isActive: initialData?.isActive ?? true,
    },
  });

  React.useEffect(() => {
    form.reset({
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
      zoneCode: initialData?.zoneCode || '',
      usageType: initialData?.usageType || '',
      pickupInstruction: initialData?.pickupInstruction || '',
      isActive: initialData?.isActive ?? true,
    });
  }, [form, initialData, schools]);

  return (
    <SchoolBusFormDialog
      open={open}
      onOpenChange={onOpenChange}
      title={initialData ? 'Edit pickup point' : 'Create pickup point'}
      description='Register boarding locations for request intake and route planning.'
      stickyFooter
    >
      <SimpleForm
        form={form}
        isLoading={isLoading}
        onCancel={() => onOpenChange(false)}
        onSubmit={async (values) =>
          onSubmit({
            name: values.name,
            address: values.address,
            latitude: values.latitude ? Number(values.latitude) : null,
            longitude: values.longitude ? Number(values.longitude) : null,
            zoneCode: values.zoneCode || null,
            usageType: values.usageType || null,
            pickupInstruction: values.pickupInstruction || null,
            isActive: values.isActive,
          })
        }
        stickyFooter
      >
        <FormSectionHeader title='1. Pickup point information' />
        <TextField form={form} name='name' label='Pickup point name *' className='md:col-span-2' />
        <TextField form={form} name='zoneCode' label='Zone code' className='md:col-span-1' />

        <FormSectionHeader title='2. Usage details' />
        <SelectField
          form={form}
          name='usageType'
          label='Usage type *'
          allowEmpty
          emptyLabel='Not specified'
          options={[
            { value: 'PICKUP_ONLY', label: 'Pickup only' },
            { value: 'DROPOFF_ONLY', label: 'Drop-off only' },
            { value: 'PICKUP_DROPOFF', label: 'Pickup & drop-off' },
          ]}
          className='md:col-span-1'
        />
        <TextareaField form={form} name='pickupInstruction' label='Pickup instruction' />
        <TextareaField form={form} name='address' label='Address *' />

        <FormSectionHeader title='3. Coordinates' />
        <TextField form={form} name='latitude' label='Latitude' />
        <TextField form={form} name='longitude' label='Longitude' />

        <FormSectionHeader title='4. Pickup point location map' />
        <div className='col-span-full'>
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
        {/* ── Section: Depot information ── */}
        <FormSectionHeader title='Depot information' />
        <TextField form={form} name='name' label='Depot name *' />
        {initialData?.code ? (
          <ReadOnlyField label='Depot code' value={initialData.code} />
        ) : null}
        <TextField form={form} name='contactPhone' label='Contact phone' />
        <TextareaField form={form} name='address' label='Address' />
        <TextareaField form={form} name='description' label='Description' />

        {/* ── Section: Coordinates ── */}
        <FormSectionHeader title='Coordinates' />
        <TextField form={form} name='latitude' label='Latitude' />
        <TextField form={form} name='longitude' label='Longitude' />

        {/* ── Section: Map picker ── */}
        <FormSectionHeader title='Map picker' />
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
        {/* ── Section: Linked account ── */}
        <FormSectionHeader title='Linked account' />
        <SelectField
          form={form}
          name='userId'
          label='Linked account user *'
          className='md:col-span-2'
          disabled={isLoadingAccountUsers || isEditMode || accountUsers.length === 0}
          description={
            isEditMode
              ? 'Account user is locked after the profile is linked.'
              : accountUsers.length === 0
                ? 'No users with School Bus module access found. Grant access in Account settings first.'
                : selectedUser
                  ? `Selected #${selectedUser.id} - ${selectedUser.email}`
                  : 'This links the parent profile to a platform login account.'
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

        {/* ── Section: Contact information ── */}
        <FormSectionHeader title='Contact information' />
        <TextField form={form} name='fullName' label='Full name *' />
        <TextField form={form} name='phone' label='Phone *' />
        <TextField form={form} name='email' label='Email' />

        {/* ── Section: Address ── */}
        <FormSectionHeader title='Address' />
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
  /** Active school-pickup links used to filter pickup/dropoff per school */
  schoolPickupPoints?: SchoolBusSchoolPickupPoint[];
  onSubmit: (values: SchoolBusStudentUpsertRequest) => Promise<void>;
}

export function StudentFormDialog({
  open,
  onOpenChange,
  initialData,
  schools,
  parents,
  pickupPoints,
  schoolPickupPoints = [],
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
      defaultDropoffPointId:
        initialData?.defaultDropoffPointId === null || initialData?.defaultDropoffPointId === undefined
          ? ''
          : String(initialData.defaultDropoffPointId),
      fullName: initialData?.fullName || '',
      grade: initialData?.grade || '',
      className: initialData?.className || '',
      dateOfBirth: initialData?.dateOfBirth || '',
      gender: initialData?.gender || '',
      homeAddress: initialData?.homeAddress || '',
      emergencyContactName: initialData?.emergencyContactName || '',
      emergencyContactPhone: initialData?.emergencyContactPhone || '',
      specialNote: initialData?.specialNote || '',
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
      defaultDropoffPointId:
        initialData?.defaultDropoffPointId === null || initialData?.defaultDropoffPointId === undefined
          ? ''
          : String(initialData.defaultDropoffPointId),
      fullName: initialData?.fullName || '',
      grade: initialData?.grade || '',
      className: initialData?.className || '',
      dateOfBirth: initialData?.dateOfBirth || '',
      gender: initialData?.gender || '',
      homeAddress: initialData?.homeAddress || '',
      emergencyContactName: initialData?.emergencyContactName || '',
      emergencyContactPhone: initialData?.emergencyContactPhone || '',
      specialNote: initialData?.specialNote || '',
      isActive: initialData?.isActive ?? true,
    });
  }, [form, initialData, schools, parents]);

  // Watch schoolId to filter pickup/dropoff by school
  const selectedSchoolId = form.watch('schoolId');

  // When school changes, clear pickup/dropoff selections
  const prevSchoolRef = React.useRef(selectedSchoolId);
  React.useEffect(() => {
    if (prevSchoolRef.current !== selectedSchoolId && prevSchoolRef.current !== 0) {
      form.setValue('pickupPointId', '');
      form.setValue('defaultDropoffPointId', '');
    }
    prevSchoolRef.current = selectedSchoolId;
  }, [selectedSchoolId, form]);

  // Filter linked pickup points by the selected school
  const linkedPointIds = React.useMemo(() => {
    if (!selectedSchoolId) return new Set<number>();
    return new Set(
      schoolPickupPoints
        .filter((sp) => sp.schoolId === selectedSchoolId)
        .map((sp) => sp.pickupPointId)
    );
  }, [schoolPickupPoints, selectedSchoolId]);

  // Pickup options: linked points with usage PICKUP_ONLY or PICKUP_DROPOFF
  const pickupOptions = React.useMemo(() => {
    if (!selectedSchoolId) return [];
    return pickupPoints
      .filter(
        (pp) =>
          linkedPointIds.has(pp.id) &&
          (pp.usageType === 'PICKUP_ONLY' || pp.usageType === 'PICKUP_DROPOFF')
      )
      .map((pp) => ({ value: String(pp.id), label: pp.name }));
  }, [pickupPoints, linkedPointIds, selectedSchoolId]);

  // Dropoff options: linked points with usage DROPOFF_ONLY or PICKUP_DROPOFF
  const dropoffOptions = React.useMemo(() => {
    if (!selectedSchoolId) return [];
    return pickupPoints
      .filter(
        (pp) =>
          linkedPointIds.has(pp.id) &&
          (pp.usageType === 'DROPOFF_ONLY' || pp.usageType === 'PICKUP_DROPOFF')
      )
      .map((pp) => ({ value: String(pp.id), label: pp.name }));
  }, [pickupPoints, linkedPointIds, selectedSchoolId]);

  const noSchoolSelected = !selectedSchoolId;

  return (
    <SchoolBusFormDialog
      open={open}
      onOpenChange={onOpenChange}
      title={initialData ? 'Edit student' : 'Create student'}
      description='Manage the roster used by transport requests, routing, and attendance.'
      stickyFooter
    >
      <SimpleForm
        form={form}
        isLoading={isLoading}
        onCancel={() => onOpenChange(false)}
        stickyFooter
        onSubmit={async (values) =>
          onSubmit({
            schoolId: values.schoolId,
            parentProfileId: values.parentProfileId,
            pickupPointId: values.pickupPointId ? Number(values.pickupPointId) : null,
            defaultDropoffPointId: values.defaultDropoffPointId ? Number(values.defaultDropoffPointId) : null,
            fullName: values.fullName,
            grade: values.grade || undefined,
            className: values.className || undefined,
            dateOfBirth: values.dateOfBirth || null,
            gender: values.gender || null,
            homeAddress: values.homeAddress || undefined,
            emergencyContactName: values.emergencyContactName || undefined,
            emergencyContactPhone: values.emergencyContactPhone || undefined,
            specialNote: values.specialNote || undefined,
            isActive: values.isActive,
          })
        }
      >
        {/* ── Section: Basic information ── */}
        <FormSectionHeader title='Basic information' />
        <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
          <SelectField
            form={form}
            name='schoolId'
            label='School *'
            options={schools.map((school) => ({
              value: String(school.id),
              label: school.name,
            }))}
          />
          <TextField form={form} name='fullName' label='Student name *' />
        </div>
        {initialData?.studentCode ? (
          <ReadOnlyField label='Student code' value={initialData.studentCode} />
        ) : null}
        <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
          <TextField form={form} name='grade' label='Grade' />
          <TextField form={form} name='className' label='Class name' />
        </div>
        <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
          <TextField form={form} name='dateOfBirth' label='Date of birth' type='date' />
          <SelectField
            form={form}
            name='gender'
            label='Gender'
            allowEmpty
            emptyLabel='Not specified'
            options={[
              { value: 'MALE', label: 'Male' },
              { value: 'FEMALE', label: 'Female' },
              { value: 'OTHER', label: 'Other' },
            ]}
          />
        </div>

        {/* ── Section: Parent & contact ── */}
        <FormSectionHeader title='Parent & contact' />
        <SelectField
          form={form}
          name='parentProfileId'
          label='Parent *'
          options={parents.map((parent) => ({
            value: String(parent.id),
            label: parent.fullName,
          }))}
        />
        <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
          <TextField form={form} name='emergencyContactName' label='Emergency contact name' />
          <TextField form={form} name='emergencyContactPhone' label='Emergency contact phone' />
        </div>

        {/* ── Section: Transport defaults ── */}
        <FormSectionHeader title='Transport defaults' />
        <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
          <SelectField
            form={form}
            name='pickupPointId'
            label='Default pickup point'
            allowEmpty
            emptyLabel={noSchoolSelected ? 'Select a school first' : 'No pickup point'}
            options={pickupOptions}
            disabled={noSchoolSelected}
          />
          <SelectField
            form={form}
            name='defaultDropoffPointId'
            label='Default drop-off point'
            allowEmpty
            emptyLabel={noSchoolSelected ? 'Select a school first' : 'No drop-off point'}
            options={dropoffOptions}
            disabled={noSchoolSelected}
          />
        </div>
        <TextareaField form={form} name='homeAddress' label='Home address' />

        {/* ── Section: Notes ── */}
        <FormSectionHeader title='Notes' />
        <TextareaField form={form} name='specialNote' label='Special note' />
      </SimpleForm>
    </SchoolBusFormDialog>
  );
}

interface BusFormDialogProps extends BaseDialogProps {
  initialData?: SchoolBusBus | null;
  busTypes: SchoolBusBusType[];
  isLoadingBusTypes?: boolean;
  depots?: SchoolBusDepot[];
  onSubmit: (values: SchoolBusBusUpsertRequest) => Promise<void>;
}

export function BusFormDialog({
  open,
  onOpenChange,
  initialData,
  busTypes,
  isLoadingBusTypes = false,
  depots = [],
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
      homeDepotId: initialData?.homeDepotId ? String(initialData.homeDepotId) : '',
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
      homeDepotId: initialData?.homeDepotId ? String(initialData.homeDepotId) : '',
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
        onSubmit={async (values) =>
          onSubmit({
            ...values,
            homeDepotId: values.homeDepotId ? Number(values.homeDepotId) : null,
          })
        }
      >
        {/* ── Section: Bus information ── */}
        <FormSectionHeader title='Bus information' />
        <TextField form={form} name='plateNumber' label='Plate number *' />
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
          label='Capacity *'
          type='number'
          disabled={!isCustomBusType}
          description={
            isCustomBusType
              ? 'Custom bus capacity can be edited.'
              : 'Capacity is auto-filled from the selected bus type.'
          }
        />

        {/* ── Section: Assignment defaults ── */}
        <FormSectionHeader title='Assignment defaults' />
        <SelectField
          form={form}
          name='homeDepotId'
          label='Home depot'
          allowEmpty
          emptyLabel='No home depot'
          options={depots.map((depot) => ({
            value: String(depot.id),
            label: `${depot.name}${depot.code ? ` (${depot.code})` : ''}`,
          }))}
        />

        {/* ── Section: Operational status ── */}
        <FormSectionHeader title='Operational status' />
        <SelectField
          form={form}
          name='status'
          label='Status *'
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
      licenseClass: initialData?.licenseClass || '',
      licenseExpiryDate: initialData?.licenseExpiryDate || '',
      status: initialData?.status || STAFF_STATUS_OPTIONS[0].value,
      isActive: initialData?.isActive ?? true,
    },
  });

  React.useEffect(() => {
    form.reset({
      userId: initialData?.userId ?? 0,
      fullName: initialData?.fullName || '',
      phone: initialData?.phone || '',
      licenseNumber: initialData?.licenseNumber || '',
      licenseClass: initialData?.licenseClass || '',
      licenseExpiryDate: initialData?.licenseExpiryDate || '',
      status: initialData?.status || STAFF_STATUS_OPTIONS[0].value,
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
        {/* ── Section: Account link ── */}
        <FormSectionHeader title='Account link' />
        <SelectField
          form={form}
          name='userId'
          label='Linked account user *'
          className='md:col-span-2'
          disabled={isLoadingAccountUsers || isEditMode || accountUsers.length === 0}
          description={
            isEditMode
              ? 'Account user is locked after the driver profile is linked.'
              : accountUsers.length === 0
                ? 'No users with School Bus module access found. Grant access in Account settings first.'
                : selectedUser
                  ? `Selected #${selectedUser.id} - ${selectedUser.email}`
                  : 'Choose an existing user account if this driver can log in.'
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

        {/* ── Section: Driver information ── */}
        <FormSectionHeader title='Driver information' />
        <TextField form={form} name='fullName' label='Full name *' />
        <TextField form={form} name='phone' label='Phone' />

        {/* ── Section: License information ── */}
        <FormSectionHeader title='License information' />
        <TextField form={form} name='licenseNumber' label='License number *' />
        <TextField form={form} name='licenseClass' label='License class *' />
        <TextField form={form} name='licenseExpiryDate' label='License expiry date *' type='date' />

        {/* ── Section: Operational status ── */}
        <FormSectionHeader title='Operational status' />
        <SelectField
          form={form}
          name='status'
          label='Status *'
          options={STAFF_STATUS_OPTIONS.map((option) => ({
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
      status: initialData?.status || STAFF_STATUS_OPTIONS[0].value,
      isActive: initialData?.isActive ?? true,
    },
  });

  React.useEffect(() => {
    form.reset({
      userId: initialData?.userId ?? 0,
      fullName: initialData?.fullName || '',
      phone: initialData?.phone || '',
      status: initialData?.status || STAFF_STATUS_OPTIONS[0].value,
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
        {/* ── Section: Account link ── */}
        <FormSectionHeader title='Account link' />
        <SelectField
          form={form}
          name='userId'
          label='Linked account user *'
          className='md:col-span-2'
          disabled={isLoadingAccountUsers || isEditMode || accountUsers.length === 0}
          description={
            isEditMode
              ? 'Account user is locked after the attendant profile is linked.'
              : accountUsers.length === 0
                ? 'No users with School Bus module access found. Grant access in Account settings first.'
                : selectedUser
                  ? `Selected #${selectedUser.id} - ${selectedUser.email}`
                  : 'Choose an existing user account if this attendant can log in.'
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

        {/* ── Section: Attendant information ── */}
        <FormSectionHeader title='Attendant information' />
        <TextField form={form} name='fullName' label='Full name *' />
        <TextField form={form} name='phone' label='Phone' />

        {/* ── Section: Operational status ── */}
        <FormSectionHeader title='Operational status' />
        <SelectField
          form={form}
          name='status'
          label='Status *'
          options={STAFF_STATUS_OPTIONS.map((option) => ({
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
  stickyFooter?: boolean;
}

function SimpleForm({
  form,
  onSubmit,
  onCancel,
  isLoading = false,
  children,
  stickyFooter = false,
}: SimpleFormProps) {
  if (stickyFooter) {
    return (
      <Form {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} className='flex-1 flex flex-col min-h-0 overflow-hidden'>
          {/* Form Body - Scrollable */}
          <div className='flex-1 overflow-y-auto px-6 py-2 sm:px-8 min-h-0'>
            <div className='grid gap-4 md:grid-cols-2 pb-4'>{children}</div>
          </div>
          {/* Form Footer - Sticky */}
          <div className='flex justify-end gap-2 border-t px-6 py-4 sm:px-8 bg-white shrink-0'>
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
  className,
}: FieldProps & { type?: string; disabled?: boolean; description?: string; className?: string }) {
  return (
    <FormField
      control={form.control}
      name={name as any}
      render={({ field }) => (
        <FormItem className={className}>
          <FormLabel>{label}</FormLabel>
          <FormControl>
            {type === 'date' ? (
              <SchoolBusDatePicker
                fullWidth
                value={field.value}
                onChange={field.onChange}
                disabled={disabled}
              />
            ) : (
              <Input
                {...field}
                type={type}
                value={(field.value as string) ?? ''}
                disabled={disabled}
              />
            )}
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

function FormSectionHeader({ title }: { title: string }) {
  return (
    <div className='col-span-full border-b border-slate-100 pb-1 pt-2'>
      <p className='text-xs font-semibold uppercase tracking-wide text-slate-500'>{title}</p>
    </div>
  );
}

function ReadOnlyField({ label, value, className }: { label: string; value: string; className?: string }) {
  return (
    <div className={cn('space-y-2', className)}>
      <FormLabel>{label}</FormLabel>
      <div className='rounded-xl border border-slate-200 bg-slate-50/70 px-3 py-2 text-sm font-semibold text-slate-700'>
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
  searchable,
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
  searchable?: boolean;
}) {
  const selectOptions = React.useMemo(() => {
    const list = options.map((opt) => ({ label: opt.label, value: opt.value }));
    if (allowEmpty) {
      list.unshift({ label: emptyLabel, value: emptyValue });
    }
    return list;
  }, [options, allowEmpty, emptyLabel, emptyValue]);

  const isSearchable = searchable ?? (options.length > 6);

  return (
    <FormField
      control={form.control}
      name={name as any}
      render={({ field }) => (
        <FormItem className={className}>
          <FormLabel>{label}</FormLabel>
          <FormControl>
            <SchoolBusSelect
              fullWidth
              size='md'
              className='h-11 rounded-xl w-full text-slate-900 border-slate-200 shadow-sm'
              disabled={disabled}
              value={String(field.value ?? (allowEmpty ? emptyValue : ''))}
              onChange={(value) => {
                const val = value === emptyValue ? '' : value;
                field.onChange(val);
                if (onChange) {
                  onChange(val);
                }
              }}
              placeholder={placeholder || `Select ${label.toLowerCase()}`}
              options={selectOptions}
              searchable={isSearchable}
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
