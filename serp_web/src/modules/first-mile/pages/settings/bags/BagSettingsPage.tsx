/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - TMS bag settings page
 */

'use client';

import React from 'react';
import { Loader2, RefreshCw, Save, ShieldAlert } from 'lucide-react';

import { getErrorMessage, useAppSelector } from '@/lib/store';
import {
  Alert,
  AlertDescription,
  AlertTitle,
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Input,
  Label,
} from '@/shared/components/ui';
import { useNotification } from '@/shared/hooks';

import {
  useGetSecondMileBagCapacitySettingsQuery,
  useUpdateSecondMileBagCapacitySettingsMutation,
} from '../../../api/firstMileApi';
import { canManageBags } from '../../bags/bagPageModels';

type BagCapacitySettingsFormValues = {
  maxWeight: string;
  maxVolume: string;
  maxOrders: string;
};

const EMPTY_FORM_VALUES: BagCapacitySettingsFormValues = {
  maxWeight: '',
  maxVolume: '',
  maxOrders: '',
};

export function BagSettingsPage() {
  const notification = useNotification();
  const roles = useAppSelector(
    (state) => state.account.user.profile?.roles ?? []
  );
  const canManage = canManageBags(roles);

  const [formValues, setFormValues] =
    React.useState<BagCapacitySettingsFormValues>(EMPTY_FORM_VALUES);

  const {
    data: bagCapacitySettings,
    isFetching,
    refetch,
  } = useGetSecondMileBagCapacitySettingsQuery(undefined, {
    skip: !canManage,
  });
  const [updateBagCapacitySettings, { isLoading: isSaving }] =
    useUpdateSecondMileBagCapacitySettingsMutation();

  React.useEffect(() => {
    if (!bagCapacitySettings) {
      return;
    }

    setFormValues({
      maxWeight: bagCapacitySettings.maxWeight
        ? String(bagCapacitySettings.maxWeight)
        : '',
      maxVolume: bagCapacitySettings.maxVolume
        ? String(bagCapacitySettings.maxVolume)
        : '',
      maxOrders: bagCapacitySettings.maxOrders
        ? String(bagCapacitySettings.maxOrders)
        : '',
    });
  }, [bagCapacitySettings]);

  const updateField = (
    field: keyof BagCapacitySettingsFormValues,
    value: string
  ) => {
    setFormValues((current) => ({ ...current, [field]: value }));
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();

    const maxWeight = Number(formValues.maxWeight);
    const maxVolume = Number(formValues.maxVolume);
    const maxOrders = Number(formValues.maxOrders);

    if (!Number.isFinite(maxWeight) || maxWeight <= 0) {
      notification.error('Khối lượng tối đa phải lớn hơn 0.');
      return;
    }
    if (!Number.isFinite(maxVolume) || maxVolume <= 0) {
      notification.error('Thể tích tối đa phải lớn hơn 0.');
      return;
    }
    if (!Number.isInteger(maxOrders) || maxOrders <= 0) {
      notification.error('Số đơn tối đa phải là số nguyên dương.');
      return;
    }

    try {
      await updateBagCapacitySettings({
        max_weight: maxWeight,
        max_volume: maxVolume,
        max_orders: maxOrders,
      }).unwrap();
      notification.success('Đã cập nhật mặc định túi.');
      void refetch();
    } catch (error) {
      notification.error('Không thể cập nhật mặc định túi.', {
        description: getErrorMessage(error),
      });
    }
  };

  if (!canManage) {
    return (
      <Alert>
        <ShieldAlert className='h-4 w-4' />
        <AlertTitle>Không có quyền truy cập</AlertTitle>
        <AlertDescription>
          Thiết lập túi yêu cầu quyền quản trị TMS hoặc quản lý hub.
        </AlertDescription>
      </Alert>
    );
  }

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between'>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>Túi hàng</h1>
          <p className='text-muted-foreground'>
            Thiết lập sức chứa mặc định dùng khi tạo túi mới và lập túi tự động.
          </p>
        </div>
        <Button
          type='button'
          variant='outline'
          disabled={isFetching}
          onClick={() => void refetch()}
        >
          <RefreshCw className='h-4 w-4' />
          Làm mới
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Mặc định túi</CardTitle>
          <CardDescription>
            Các giá trị này được điền sẵn khi tạo túi và được dùng làm giới hạn
            cho luồng lập túi tự động.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form className='space-y-5' onSubmit={handleSubmit}>
            <div className='grid gap-4 sm:grid-cols-3'>
              <div className='space-y-2'>
                <Label htmlFor='bag-default-max-weight'>
                  Khối lượng tối đa (kg)
                </Label>
                <Input
                  id='bag-default-max-weight'
                  type='number'
                  min='0'
                  step='0.01'
                  value={formValues.maxWeight}
                  onChange={(event) =>
                    updateField('maxWeight', event.target.value)
                  }
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='bag-default-max-volume'>
                  Thể tích tối đa (m3)
                </Label>
                <Input
                  id='bag-default-max-volume'
                  type='number'
                  min='0'
                  step='0.001'
                  value={formValues.maxVolume}
                  onChange={(event) =>
                    updateField('maxVolume', event.target.value)
                  }
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='bag-default-max-orders'>Số đơn tối đa</Label>
                <Input
                  id='bag-default-max-orders'
                  type='number'
                  min='1'
                  step='1'
                  value={formValues.maxOrders}
                  onChange={(event) =>
                    updateField('maxOrders', event.target.value)
                  }
                />
              </div>
            </div>

            <div className='flex justify-end'>
              <Button type='submit' disabled={isSaving || isFetching}>
                {isSaving ? (
                  <Loader2 className='h-4 w-4 animate-spin' />
                ) : (
                  <Save className='h-4 w-4' />
                )}
                Lưu mặc định
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
