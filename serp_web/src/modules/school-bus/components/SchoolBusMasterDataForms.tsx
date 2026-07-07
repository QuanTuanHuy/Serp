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
import {
  BUS_STATUS_OPTIONS,
  PROFILE_STATUS_OPTIONS,
  STAFF_STATUS_OPTIONS,
} from '../constants';
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
import { toCoordinateString } from '../utils';
import { SchoolBusFormDialog } from './SchoolBusFormDialog';
import { LocationPickerMap } from './map/LocationPickerMap';
import { useGetSchoolPickupPointDropdownOptionsQuery } from '../api/schoolBusApi';
import { genderLabel, getLabel } from '../schoolBusLabels';

const schoolSchema = z.object({
  name: z.string().min(1, 'Vui lòng nhập tên trường.'),
  address: z.string().optional(),
  contactPhone: z.string().optional(),
  contactEmail: z
    .string()
    .optional()
    .refine((v) => !v || /^[\w.+-]+@[\w.-]+\.[a-zA-Z]{2,}$/.test(v), {
      message: 'Email không đúng định dạng.',
    }),
  latitude: z.string().optional(),
  longitude: z.string().optional(),
  isActive: z.boolean().default(true),
});

const pickupPointSchema = z.object({
  name: z.string().min(1, 'Vui lòng nhập tên điểm đón/trả.'),
  address: z.string().min(1, 'Vui lòng nhập địa chỉ điểm đón/trả.'),
  latitude: z.string().optional(),
  longitude: z.string().optional(),
  usageType: z.string().min(1, 'Vui lòng chọn loại sử dụng.'),
  pickupInstruction: z.string().optional(),
  isActive: z.boolean().default(true),
});

const depotSchema = z.object({
  name: z.string().min(1, 'Vui lòng nhập tên bãi xe'),
  address: z.string().optional(),
  latitude: z.string().optional(),
  longitude: z.string().optional(),
  contactPhone: z.string().optional(),
  description: z.string().optional(),
  isActive: z.boolean().default(true),
});

const parentSchema = z.object({
  accountUserId: z.coerce.number().min(1, 'Vui lòng chọn tài khoản liên kết.'),
  fullName: z.string().min(1, 'Vui lòng nhập họ và tên.'),
  phone: z.string().min(1, 'Vui lòng nhập số điện thoại.'),
  email: z
    .string()
    .optional()
    .refine((v) => !v || /^[\w.+-]+@[\w.-]+\.[a-zA-Z]{2,}$/.test(v), {
      message: 'Email không đúng định dạng.',
    }),
  address: z.string().optional(),
  isActive: z.boolean().default(true),
});

const studentSchema = z.object({
  schoolId: z.coerce.number().min(1, 'Vui lòng chọn trường học.'),
  parentProfileId: z.coerce.number().min(1, 'Vui lòng chọn phụ huynh.'),
  pickupPointId: z.string().optional(),
  defaultDropoffPointId: z.string().optional(),
  fullName: z.string().min(1, 'Vui lòng nhập họ tên học sinh.'),
  grade: z.string().optional(),
  className: z.string().optional(),
  dateOfBirth: z.string().optional(),
  gender: z.string().optional(),
  homeAddress: z.string().optional(),
  specialNote: z.string().optional(),
  isActive: z.boolean().default(true),
});

const busSchema = z.object({
  plateNumber: z.string().min(1, 'Vui lòng nhập biển số xe.'),
  busType: z.string().optional(),
  capacity: z.coerce.number().min(1, 'Sức chứa phải lớn hơn hoặc bằng 1.'),
  status: z.string().min(1, 'Vui lòng chọn trạng thái.'),
  homeDepotId: z.string().min(1, 'Vui lòng chọn bãi xe mặc định.'),
  isActive: z.boolean().default(true),
});

const driverSchema = z.object({
  accountUserId: z.coerce.number().min(1, 'Vui lòng chọn tài khoản người dùng.'),
  fullName: z.string().min(1, 'Vui lòng nhập họ tên.'),
  phone: z.string().optional(),
  status: z.string().min(1, 'Vui lòng chọn trạng thái.'),
  isActive: z.boolean().default(true),
});

const attendantSchema = z.object({
  accountUserId: z.coerce.number().min(1, 'Vui lòng chọn tài khoản người dùng.'),
  fullName: z.string().min(1, 'Vui lòng nhập họ tên.'),
  phone: z.string().optional(),
  status: z.string().min(1, 'Vui lòng chọn trạng thái.'),
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
      isActive: initialData?.isActive || true,
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
      isActive: initialData?.isActive || true,
    });
  }, [form, initialData]);

  return (
    <SchoolBusFormDialog
      open={open}
      onOpenChange={onOpenChange}
      title={initialData ? 'Chỉnh sửa trường' : 'Tạo trường'}
      description='Quản lý thông tin định danh và liên hệ của trường.'
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
        <FormSectionHeader title='1. Thông tin trường học' />
        <TextField
          form={form}
          name='name'
          label='Tên trường *'
          className='md:col-span-2'
        />
        {initialData?.code ? (
          <ReadOnlyField
            label='Mã trường'
            value={initialData.code}
            className='md:col-span-1'
          />
        ) : null}

        <FormSectionHeader title='2. Thông tin liên hệ' />
        <TextField
          form={form}
          name='contactPhone'
          label='Số điện thoại liên hệ'
          className='md:col-span-1'
        />
        <TextField
          form={form}
          name='contactEmail'
          label='Email liên hệ'
          className='md:col-span-1'
        />
        <TextareaField form={form} name='address' label='Địa chỉ' />

        <FormSectionHeader title='3. Tọa độ' />
        <TextField form={form} name='latitude' label='Vĩ độ' />
        <TextField form={form} name='longitude' label='Kinh độ' />

        <FormSectionHeader title='4. Bản đồ vị trí trường' />
        <div className='col-span-full'>
          <LocationPickerMap
            kind='school'
            value={{
              latitude: form.watch('latitude')
                ? Number(form.watch('latitude'))
                : null,
              longitude: form.watch('longitude')
                ? Number(form.watch('longitude'))
                : null,
            }}
            onChange={({
              latitude,
              longitude,
            }: {
              latitude: number;
              longitude: number;
            }) => {
              form.setValue('latitude', latitude.toFixed(6), {
                shouldDirty: true,
              });
              form.setValue('longitude', longitude.toFixed(6), {
                shouldDirty: true,
              });
            }}
            onAddressResolved={(address: string) =>
              form.setValue('address', address, { shouldDirty: true })
            }
            title='Vị trí trường học'
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
      usageType: initialData?.usageType || '',
      pickupInstruction: initialData?.pickupInstruction || '',
      isActive: initialData?.isActive || true,
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
      usageType: initialData?.usageType || '',
      pickupInstruction: initialData?.pickupInstruction || '',
      isActive: initialData?.isActive || true,
    });
  }, [form, initialData, schools]);

  return (
    <SchoolBusFormDialog
      open={open}
      onOpenChange={onOpenChange}
      title={initialData ? 'Chỉnh sửa điểm đón/trả' : 'Tạo điểm đón/trả'}
      description='Quản lý các điểm đón/trả phục vụ đăng ký và lập tuyến.'
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
            usageType: values.usageType || null,
            pickupInstruction: values.pickupInstruction || null,
            isActive: values.isActive,
          })
        }
        stickyFooter
      >
        <FormSectionHeader title='1. Thông tin điểm đón/trả' />
        {initialData?.code && (
          <ReadOnlyField
            label='Mã điểm đón/trả'
            value={initialData.code}
            className='md:col-span-1'
          />
        )}
        <TextField
          form={form}
          name='name'
          label='Tên điểm đón/trả *'
          className={initialData?.code ? 'md:col-span-1' : 'md:col-span-2'}
        />

        <FormSectionHeader title='2. Thông tin sử dụng' />
        <SelectField
          form={form}
          name='usageType'
          label='Loại sử dụng *'
          allowEmpty
          emptyLabel='Chưa chọn'
          options={[
            { value: 'PICKUP_ONLY', label: 'Chỉ đón' },
            { value: 'DROPOFF_ONLY', label: 'Chỉ trả' },
            { value: 'PICKUP_DROPOFF', label: 'Đón và trả' },
          ]}
          className='md:col-span-1'
        />
        <TextareaField
          form={form}
          name='pickupInstruction'
          label='Hướng dẫn đón/trả'
        />
        <TextareaField form={form} name='address' label='Địa chỉ *' />

        <FormSectionHeader title='3. Tọa độ' />
        <TextField form={form} name='latitude' label='Vĩ độ' />
        <TextField form={form} name='longitude' label='Kinh độ' />

        <FormSectionHeader title='4. Bản đồ vị trí điểm đón/trả' />
        <div className='col-span-full'>
          <LocationPickerMap
            kind='pickup'
            value={{
              latitude: form.watch('latitude')
                ? Number(form.watch('latitude'))
                : null,
              longitude: form.watch('longitude')
                ? Number(form.watch('longitude'))
                : null,
            }}
            referenceMarkers={schools
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
              }))}
            onChange={({
              latitude,
              longitude,
            }: {
              latitude: number;
              longitude: number;
            }) => {
              form.setValue('latitude', latitude.toFixed(6), {
                shouldDirty: true,
              });
              form.setValue('longitude', longitude.toFixed(6), {
                shouldDirty: true,
              });
            }}
            onAddressResolved={(address: string) =>
              form.setValue('address', address, { shouldDirty: true })
            }
            title='Vị trí điểm đón/trả'
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
      isActive: initialData?.isActive || true,
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
      isActive: initialData?.isActive || true,
    });
  }, [form, initialData]);

  return (
    <SchoolBusFormDialog
      open={open}
      onOpenChange={onOpenChange}
      title={initialData ? 'Chỉnh sửa bãi xe' : 'Tạo bãi xe'}
      description='Đăng ký bãi xe cố định dùng làm điểm bắt đầu hoặc kết thúc tuyến.'
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
        {/* -- Section: Depot information -- */}
        <FormSectionHeader title='Thông tin bãi xe' />
        <TextField form={form} name='name' label='Tên bãi xe *' />
        {initialData?.code ? (
          <ReadOnlyField label='Mã bãi xe' value={initialData.code} />
        ) : null}
        <TextField form={form} name='contactPhone' label='Số điện thoại liên hệ' />
        <TextareaField form={form} name='address' label='Địa chỉ' />
        <TextareaField form={form} name='description' label='Mô tả' />

        {/* -- Section: Coordinates -- */}
        <FormSectionHeader title='Tọa độ' />
        <TextField form={form} name='latitude' label='Vĩ độ' />
        <TextField form={form} name='longitude' label='Kinh độ' />

        {/* -- Section: Map picker -- */}
        <FormSectionHeader title='Chọn vị trí trên bản đồ' />
        <div className='md:col-span-2'>
          <LocationPickerMap
            kind='depot'
            value={{
              latitude: form.watch('latitude')
                ? Number(form.watch('latitude'))
                : null,
              longitude: form.watch('longitude')
                ? Number(form.watch('longitude'))
                : null,
            }}
            onChange={({
              latitude,
              longitude,
            }: {
              latitude: number;
              longitude: number;
            }) => {
              form.setValue('latitude', latitude.toFixed(6), {
                shouldDirty: true,
              });
              form.setValue('longitude', longitude.toFixed(6), {
                shouldDirty: true,
              });
            }}
            onAddressResolved={(address: string) =>
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
      accountUserId: initialData?.accountUserId || initialData?.userId || 0,
      fullName: initialData?.fullName || '',
      phone: initialData?.phone || '',
      email: initialData?.email || '',
      address: initialData?.address || '',
      isActive: initialData?.isActive || true,
    },
  });

  React.useEffect(() => {
    form.reset({
      accountUserId: initialData?.accountUserId || initialData?.userId || 0,
      fullName: initialData?.fullName || '',
      phone: initialData?.phone || '',
      email: initialData?.email || '',
      address: initialData?.address || '',
      isActive: initialData?.isActive || true,
    });
  }, [form, initialData]);

  const isEditMode = Boolean(initialData);
  const selectedUserId = Number(form.watch('accountUserId') || 0);
  const selectedUser = accountUsers.find((user) => user.id === selectedUserId);

  const handleAccountUserChange = (value: string) => {
    const userId = Number(value);
    const user = accountUsers.find((candidate) => candidate.id === userId);

    form.setValue('accountUserId', userId, {
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
      title={initialData ? 'Chỉnh sửa hồ sơ phụ huynh' : 'Tạo hồ sơ phụ huynh'}
      description='Lưu hồ sơ phụ huynh dùng trong vận hành xe bus và liên kết với tài khoản người dùng.'
    >
      <SimpleForm
        form={form}
        isLoading={isLoading}
        onCancel={() => onOpenChange(false)}
        onSubmit={onSubmit}
      >
        {/* -- Section: Linked account -- */}
        <FormSectionHeader title='Tài khoản liên kết' />
        <SelectField
          form={form}
          name='accountUserId'
          label='Tài khoản liên kết *'
          className='md:col-span-2'
          disabled={
            isLoadingAccountUsers || isEditMode || accountUsers.length === 0
          }
          description={
            isEditMode
              ? 'Tài khoản người dùng sẽ không thể thay đổi sau khi hồ sơ được liên kết.'
              : accountUsers.length === 0
                ? 'Chưa có người dùng có quyền truy cập module School Bus. Vui lòng cấp quyền trong Account trước.'
                : selectedUser
                  ? `Đã chọn: ${selectedUser.email}`
                  : 'Liên kết hồ sơ phụ huynh với tài khoản đăng nhập trên nền tảng.'
          }
          placeholder={
            isLoadingAccountUsers
              ? 'Đang tải người dùng Account...'
              : 'Chọn người dùng Account'
          }
          options={accountUsers.map((user) => ({
            value: String(user.id),
            label: getAccountUserOptionLabel(user),
          }))}
          onChange={handleAccountUserChange}
        />

        {/* -- Section: Contact information -- */}
        <FormSectionHeader title='Thông tin liên hệ' />
        <TextField form={form} name='fullName' label='Họ và tên *' />
        <TextField form={form} name='phone' label='Số điện thoại *' />
        <TextField form={form} name='email' label='Email' />

        {/* -- Section: Address -- */}
        <FormSectionHeader title='Địa chỉ' />
        <TextareaField form={form} name='address' label='Địa chỉ' />
      </SimpleForm>
    </SchoolBusFormDialog>
  );
}

function getAccountUserFullName(user: SchoolBusAccountUser) {
  return [user.firstName, user.lastName].filter(Boolean).join(' ').trim();
}

function getAccountUserOptionLabel(user: SchoolBusAccountUser) {
  const fullName =
    getAccountUserFullName(user) || user.email || `Người dùng #${user.id}`;
  return `${fullName} - ${user.email} - #${user.id}`;
}

interface StudentFormDialogProps extends BaseDialogProps {
  initialData?: SchoolBusStudent | null;
  schools: any[];
  parents: any[];
  onSubmit: (values: SchoolBusStudentUpsertRequest) => Promise<void>;
  isParent?: boolean;
}

export function StudentFormDialog({
  open,
  onOpenChange,
  initialData,
  schools,
  parents,
  onSubmit,
  isLoading = false,
  isParent = false,
}: StudentFormDialogProps) {
  const form = useForm<StudentFormValues>({
    resolver: zodResolver(studentSchema) as any,
    defaultValues: {
      schoolId: initialData?.schoolId || schools[0]?.id || 0,
      parentProfileId:
        initialData?.parentProfileId ||
        (isParent ? 999999 : (parents[0]?.id || 0)),
      pickupPointId:
        initialData?.pickupPointId === null ||
        initialData?.pickupPointId === undefined
          ? ''
          : String(initialData.pickupPointId),
      defaultDropoffPointId:
        initialData?.defaultDropoffPointId === null ||
        initialData?.defaultDropoffPointId === undefined
          ? ''
          : String(initialData.defaultDropoffPointId),
      fullName: initialData?.fullName || '',
      grade: initialData?.grade || '',
      className: initialData?.className || '',
      dateOfBirth: initialData?.dateOfBirth || '',
      gender: initialData?.gender || '',
      homeAddress: initialData?.homeAddress || '',
      specialNote: initialData?.specialNote || '',
      isActive: initialData?.isActive || true,
    },
  });

  React.useEffect(() => {
    form.reset({
      schoolId: initialData?.schoolId || schools[0]?.id || 0,
      parentProfileId:
        initialData?.parentProfileId ||
        (isParent ? 999999 : (parents[0]?.id || 0)),
      pickupPointId:
        initialData?.pickupPointId === null ||
        initialData?.pickupPointId === undefined
          ? ''
          : String(initialData.pickupPointId),
      defaultDropoffPointId:
        initialData?.defaultDropoffPointId === null ||
        initialData?.defaultDropoffPointId === undefined
          ? ''
          : String(initialData.defaultDropoffPointId),
      fullName: initialData?.fullName || '',
      grade: initialData?.grade || '',
      className: initialData?.className || '',
      dateOfBirth: initialData?.dateOfBirth || '',
      gender: initialData?.gender || '',
      homeAddress: initialData?.homeAddress || '',
      specialNote: initialData?.specialNote || '',
      isActive: initialData?.isActive || true,
    });
  }, [form, initialData, schools, parents, isParent]);

  // Watch schoolId to filter pickup/dropoff by school
  const selectedSchoolId = form.watch('schoolId');
  const numericSchoolId = selectedSchoolId ? Number(selectedSchoolId) : 0;

  // When school changes, clear pickup/dropoff selections
  const prevSchoolRef = React.useRef(numericSchoolId);
  React.useEffect(() => {
    if (
      prevSchoolRef.current !== numericSchoolId &&
      prevSchoolRef.current !== 0
    ) {
      form.setValue('pickupPointId', '');
      form.setValue('defaultDropoffPointId', '');
    }
    prevSchoolRef.current = numericSchoolId;
  }, [numericSchoolId, form]);

  // Fetch active school pickup point links from backend dynamically when school changes using the dropdown API
  const { data: dropdownPointsData, isFetching } =
    useGetSchoolPickupPointDropdownOptionsQuery(numericSchoolId, {
      skip: !numericSchoolId,
    });
  const schoolDropdownPoints = dropdownPointsData?.data || [];

  // Pickup options: options with usage PICKUP_ONLY or PICKUP_DROPOFF
  const pickupOptions = React.useMemo(() => {
    if (!numericSchoolId) return [];
    return schoolDropdownPoints
      .filter(
        (sp) =>
          sp.metadata?.usageType === 'PICKUP_ONLY' ||
          sp.metadata?.usageType === 'PICKUP_DROPOFF'
      )
      .map((sp) => ({ value: String(sp.id), label: sp.label }));
  }, [schoolDropdownPoints, numericSchoolId]);

  // Dropoff options: options with usage DROPOFF_ONLY or PICKUP_DROPOFF
  const dropoffOptions = React.useMemo(() => {
    if (!numericSchoolId) return [];
    return schoolDropdownPoints
      .filter(
        (sp) =>
          sp.metadata?.usageType === 'DROPOFF_ONLY' ||
          sp.metadata?.usageType === 'PICKUP_DROPOFF'
      )
      .map((sp) => ({ value: String(sp.id), label: sp.label }));
  }, [schoolDropdownPoints, numericSchoolId]);

  const noSchoolSelected = !numericSchoolId;

  return (
    <SchoolBusFormDialog
      open={open}
      onOpenChange={onOpenChange}
      title={initialData ? 'Chỉnh sửa học sinh' : 'Tạo học sinh'}
      description='Quản lý hồ sơ học sinh phục vụ yêu cầu xe bus, lập tuyến và điểm danh.'
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
            pickupPointId: values.pickupPointId
              ? Number(values.pickupPointId)
              : null,
            defaultDropoffPointId: values.defaultDropoffPointId
              ? Number(values.defaultDropoffPointId)
              : null,
            fullName: values.fullName,
            grade: values.grade || undefined,
            className: values.className || undefined,
            dateOfBirth: values.dateOfBirth || null,
            gender: values.gender || null,
            homeAddress: values.homeAddress || undefined,
            specialNote: values.specialNote || undefined,
            isActive: values.isActive,
          })
        }
      >
        {/* -- Section: Basic information -- */}
        <FormSectionHeader title='Thông tin cơ bản' />
        <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
          <SelectField
            form={form}
            name='schoolId'
            label='Trường học *'
            options={schools.map((school) => ({
              value: String(school.id),
              label: school.label,
            }))}
          />
          <TextField form={form} name='fullName' label='Họ tên học sinh *' />
        </div>
        {initialData?.studentCode ? (
          <ReadOnlyField label='Mã học sinh' value={initialData.studentCode} />
        ) : null}
        <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
          <TextField form={form} name='grade' label='Khối' />
          <TextField form={form} name='className' label='Lớp' />
        </div>
        <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
          <TextField
            form={form}
            name='dateOfBirth'
            label='Ngày sinh'
            type='date'
          />
          <SelectField
            form={form}
            name='gender'
            label='Giới tính'
            allowEmpty
            emptyLabel='Chưa chọn'
            options={[
              { value: 'MALE', label: getLabel(genderLabel, 'MALE') },
              { value: 'FEMALE', label: getLabel(genderLabel, 'FEMALE') },
              { value: 'OTHER', label: getLabel(genderLabel, 'OTHER') },
            ]}
          />
        </div>

        {/* -- Section: Parent & contact -- */}
        {!isParent && (
          <>
            <FormSectionHeader title='Phụ huynh và liên hệ' />
            <SelectField
              form={form}
              name='parentProfileId'
              label='Phụ huynh *'
              options={parents.map((parent) => ({
                value: String(parent.id),
                label: parent.label,
              }))}
            />
          </>
        )}

        {/* -- Section: Transport defaults -- */}
        <FormSectionHeader title='Thông tin đón/trả mặc định' />
        <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
          <SelectField
            form={form}
            name='pickupPointId'
            label='Điểm đón mặc định'
            allowEmpty
            emptyLabel={
              isFetching
                ? 'Đang tải điểm đón/trả...'
                : noSchoolSelected
                  ? 'Chọn trường trước'
                  : 'Chưa có điểm đón/trả'
            }
            options={pickupOptions}
            disabled={noSchoolSelected || isFetching}
          />
          <SelectField
            form={form}
            name='defaultDropoffPointId'
            label='Điểm trả mặc định'
            allowEmpty
            emptyLabel={
              isFetching
                ? 'Đang tải điểm trả...'
                : noSchoolSelected
                  ? 'Chọn trường trước'
                  : 'Chưa có điểm trả'
            }
            options={dropoffOptions}
            disabled={noSchoolSelected || isFetching}
          />
        </div>
        <TextareaField form={form} name='homeAddress' label='Địa chỉ nhà' />

        {/* -- Section: Notes -- */}
        <FormSectionHeader title='Ghi chú' />
        <TextareaField form={form} name='specialNote' label='Ghi chú đặc biệt' />
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
        initialData?.capacity ||
        (defaultBusTypeMeta?.value && defaultBusTypeMeta.value > 0
          ? defaultBusTypeMeta.value
          : 1),
      status: initialData?.status || BUS_STATUS_OPTIONS[0].value,
      homeDepotId: initialData?.homeDepotId
        ? String(initialData.homeDepotId)
        : '',
      isActive: initialData?.isActive || true,
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
        initialData?.capacity ||
        (nextBusTypeMeta?.value && nextBusTypeMeta.value > 0
          ? nextBusTypeMeta.value
          : 1),
      status: initialData?.status || BUS_STATUS_OPTIONS[0].value,
      homeDepotId: initialData?.homeDepotId
        ? String(initialData.homeDepotId)
        : '',
      isActive: initialData?.isActive || true,
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
      title={initialData ? 'Chỉnh sửa xe' : 'Tạo xe'}
      description='Quản lý danh mục xe dùng cho phân công tuyến.'
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
        {/* -- Section: Bus information -- */}
        <FormSectionHeader title='Thông tin xe' />
        <TextField form={form} name='plateNumber' label='Biển số xe *' />
        <SelectField
          form={form}
          name='busType'
          label='Loại xe'
          disabled={isLoadingBusTypes || busTypeOptions.length === 0}
          description={
            busTypeOptions.length === 0
              ? 'Chưa có loại xe khả dụng từ backend.'
              : selectedBusType
                ? `${selectedBusType.description} - sức chứa ${selectedBusType.value || 'tùy chỉnh'}`
                : 'Chọn loại xe.'
          }
          placeholder={
            isLoadingBusTypes ? 'Đang tải loại xe...' : 'Chọn loại xe'
          }
          options={busTypeOptions.map((busType) => ({
            value: busType.code,
            label: `${busType.description} (${busType.code})`,
          }))}
          onChange={handleBusTypeChange}
        />
        <TextField
          form={form}
          name='capacity'
          label='Sức chứa *'
          type='number'
          disabled={!isCustomBusType}
          description={
            isCustomBusType
              ? 'Có thể chỉnh sửa sức chứa với loại xe tùy chỉnh.'
              : 'Sức chứa được tự động điền theo loại xe đã chọn.'
          }
        />

        {/* -- Section: Assignment defaults -- */}
        <FormSectionHeader title='Cấu hình phân công mặc định' />
        <SelectField
          form={form}
          name='homeDepotId'
          label='Bãi xe mặc định'
          allowEmpty
          emptyLabel='Chưa có bãi xe mặc định'
          options={depots.map((depot) => ({
            value: String(depot.id),
            label: `${depot.name}${depot.code ? ` (${depot.code})` : ''}`,
          }))}
        />

        {/* -- Section: Operational status -- */}
        <FormSectionHeader title='Trạng thái vận hành' />
        <SelectField
          form={form}
          name='status'
          label='Trạng thái *'
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
      accountUserId: initialData?.accountUserId || initialData?.userId || 0,
      fullName: initialData?.fullName || '',
      phone: initialData?.phone || '',
      status: initialData?.status || STAFF_STATUS_OPTIONS[0].value,
      isActive: initialData?.isActive || true,
    },
  });

  React.useEffect(() => {
    form.reset({
      accountUserId: initialData?.accountUserId || initialData?.userId || 0,
      fullName: initialData?.fullName || '',
      phone: initialData?.phone || '',
      status: initialData?.status || STAFF_STATUS_OPTIONS[0].value,
      isActive: initialData?.isActive || true,
    });
  }, [form, initialData]);

  const isEditMode = Boolean(initialData);
  const selectedUserId = Number(form.watch('accountUserId') || 0);
  const selectedUser = accountUsers.find((user) => user.id === selectedUserId);

  const handleAccountUserChange = (value: string) => {
    const userId = Number(value);
    const user = accountUsers.find((candidate) => candidate.id === userId);

    form.setValue('accountUserId', userId, {
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
      title={initialData ? 'Chỉnh sửa tài xế' : 'Tạo tài xế'}
      description='Quản lý trạng thái sẵn sàng của tài xế.'
    >
      <SimpleForm
        form={form}
        isLoading={isLoading}
        onCancel={() => onOpenChange(false)}
        onSubmit={onSubmit}
      >
        {/* -- Section: Account link -- */}
        <FormSectionHeader title='Liên kết tài khoản' />
        <SelectField
          form={form}
          name='accountUserId'
          label='Tài khoản liên kết *'
          className='md:col-span-2'
          disabled={
            isLoadingAccountUsers || isEditMode || accountUsers.length === 0
          }
          description={
            isEditMode
              ? 'Tài khoản bị khóa chỉnh sửa sau khi hồ sơ tài xế đã được liên kết.'
              : accountUsers.length === 0
                ? 'Chưa có người dùng có quyền truy cập module School Bus. Vui lòng cấp quyền trong Account trước.'
                : selectedUser
                  ? `Đã chọn: ${selectedUser.email}`
                  : 'Chọn tài khoản người dùng hiện có nếu tài xế cần đăng nhập.'
          }
          placeholder={
            isLoadingAccountUsers
              ? 'Đang tải người dùng Account...'
              : 'Chọn người dùng Account'
          }
          options={accountUsers.map((user) => ({
            value: String(user.id),
            label: getAccountUserOptionLabel(user),
          }))}
          onChange={handleAccountUserChange}
        />

        {/* -- Section: Driver information -- */}
        <FormSectionHeader title='Thông tin tài xế' />
        <TextField form={form} name='fullName' label='Họ tên *' />
        <TextField form={form} name='phone' label='Số điện thoại' />

        {/* -- Section: Operational status -- */}
        <FormSectionHeader title='Trạng thái vận hành' />
        <SelectField
          form={form}
          name='status'
          label='Trạng thái *'
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
      accountUserId: initialData?.accountUserId || initialData?.userId || 0,
      fullName: initialData?.fullName || '',
      phone: initialData?.phone || '',
      status: initialData?.status || STAFF_STATUS_OPTIONS[0].value,
      isActive: initialData?.isActive || true,
    },
  });

  React.useEffect(() => {
    form.reset({
      accountUserId: initialData?.accountUserId || initialData?.userId || 0,
      fullName: initialData?.fullName || '',
      phone: initialData?.phone || '',
      status: initialData?.status || STAFF_STATUS_OPTIONS[0].value,
      isActive: initialData?.isActive || true,
    });
  }, [form, initialData]);

  const isEditMode = Boolean(initialData);
  const selectedUserId = Number(form.watch('accountUserId') || 0);
  const selectedUser = accountUsers.find((user) => user.id === selectedUserId);

  const handleAccountUserChange = (value: string) => {
    const userId = Number(value);
    const user = accountUsers.find((candidate) => candidate.id === userId);

    form.setValue('accountUserId', userId, {
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
      title={initialData ? 'Chỉnh sửa phụ xe' : 'Tạo phụ xe'}
      description='Quản lý phụ xe hỗ trợ vận hành tuyến.'
    >
      <SimpleForm
        form={form}
        isLoading={isLoading}
        onCancel={() => onOpenChange(false)}
        onSubmit={onSubmit}
      >
        {/* -- Section: Account link -- */}
        <FormSectionHeader title='Liên kết tài khoản' />
        <SelectField
          form={form}
          name='accountUserId'
          label='Tài khoản liên kết *'
          className='md:col-span-2'
          disabled={
            isLoadingAccountUsers || isEditMode || accountUsers.length === 0
          }
          description={
            isEditMode
              ? 'Tài khoản bị khóa chỉnh sửa sau khi hồ sơ phụ xe đã được liên kết.'
              : accountUsers.length === 0
                ? 'Chưa có người dùng có quyền truy cập module School Bus. Vui lòng cấp quyền trong Account trước.'
                : selectedUser
                  ? `Đã chọn: ${selectedUser.email}`
                  : 'Chọn tài khoản người dùng hiện có nếu phụ xe cần đăng nhập.'
          }
          placeholder={
            isLoadingAccountUsers
              ? 'Đang tải người dùng Account...'
              : 'Chọn người dùng Account'
          }
          options={accountUsers.map((user) => ({
            value: String(user.id),
            label: getAccountUserOptionLabel(user),
          }))}
          onChange={handleAccountUserChange}
        />

        {/* -- Section: Attendant information -- */}
        <FormSectionHeader title='Thông tin phụ xe' />
        <TextField form={form} name='fullName' label='Họ tên *' />
        <TextField form={form} name='phone' label='Số điện thoại' />

        {/* -- Section: Operational status -- */}
        <FormSectionHeader title='Trạng thái vận hành' />
        <SelectField
          form={form}
          name='status'
          label='Trạng thái *'
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
        <form
          onSubmit={form.handleSubmit(onSubmit)}
          className='flex-1 flex flex-col min-h-0 overflow-hidden'
        >
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
              Hủy
            </Button>
            <Button
              type='submit'
              className={schoolBusUi.primaryButton}
              disabled={isLoading}
            >
              {isLoading ? 'Đang lưu...' : 'Lưu'}
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
            Hủy
          </Button>
          <Button
            type='submit'
            className={schoolBusUi.primaryButton}
            disabled={isLoading}
          >
            {isLoading ? 'Đang lưu...' : 'Lưu'}
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
}: FieldProps & {
  type?: string;
  disabled?: boolean;
  description?: string;
  className?: string;
}) {
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
                value={(field.value as string) || ''}
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
      <p className='text-xs font-semibold uppercase tracking-wide text-slate-500'>
        {title}
      </p>
    </div>
  );
}

function ReadOnlyField({
  label,
  value,
  className,
}: {
  label: string;
  value: string;
  className?: string;
}) {
  return (
    <div className={cn('space-y-2', className)}>
      <FormLabel>{label}</FormLabel>
      <div className='rounded-xl border border-slate-200 bg-slate-50/70 px-3 py-2 text-sm font-semibold text-slate-700'>
        {value}
      </div>
    </div>
  );
}

function TextareaField({ form, name, label }: FieldProps) {
  return (
    <FormField
      control={form.control}
      name={name as any}
      render={({ field }) => (
        <FormItem className='md:col-span-2'>
          <FormLabel>{label}</FormLabel>
          <FormControl>
            <Textarea {...field} value={(field.value as string) || ''} />
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
  emptyLabel = 'Không có',
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

  const isSearchable = searchable || options.length > 6;

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
              value={String(field.value || (allowEmpty ? emptyValue : ''))}
              onChange={(value) => {
                const val = value === emptyValue ? '' : value;
                field.onChange(val);
                if (onChange) {
                  onChange(val);
                }
              }}
              placeholder={placeholder || `Chọn ${label.toLowerCase()}`}
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

