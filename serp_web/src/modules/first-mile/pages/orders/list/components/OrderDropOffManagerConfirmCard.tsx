/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Manager drop-off confirmation card
 */

import React from 'react';
import {
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Input,
  Label,
} from '@/shared/components/ui';
import { Loader2 } from 'lucide-react';
import { TmsCombobox } from '@/modules/first-mile/components';
import type { PostOffice } from '../../../../types';

interface OrderDropOffManagerConfirmCardProps {
  orderIdInput: string;
  postOfficeIdInput: string;
  postOffices: PostOffice[];
  isLoadingPostOffices: boolean;
  isSubmitting: boolean;
  onOrderIdInputChange: (value: string) => void;
  onPostOfficeIdInputChange: (value: string) => void;
  onSubmit: () => void;
}

export const OrderDropOffManagerConfirmCard: React.FC<
  OrderDropOffManagerConfirmCardProps
> = ({
  orderIdInput,
  postOfficeIdInput,
  postOffices,
  isLoadingPostOffices,
  isSubmitting,
  onOrderIdInputChange,
  onPostOfficeIdInputChange,
  onSubmit,
}) => {
  const postOfficeOptions = postOffices.map((postOffice) => ({
    value: String(postOffice.id),
    label: `${postOffice.code} - ${postOffice.name}`,
  }));

  return (
    <Card>
      <CardHeader>
        <CardTitle>Xác nhận gửi tại bưu cục</CardTitle>
        <CardDescription>
          Dành cho quản lý bưu cục: xác nhận đơn gửi tại bưu cục hiện tại.
        </CardDescription>
      </CardHeader>
      <CardContent className='space-y-3'>
        <div className='grid gap-3 md:grid-cols-2'>
          <div className='space-y-2'>
            <Label htmlFor='dropOffOrderId'>Mã đơn hàng *</Label>
            <Input
              id='dropOffOrderId'
              inputMode='numeric'
              placeholder='Nhập mã đơn hàng'
              value={orderIdInput}
              onChange={(event) => onOrderIdInputChange(event.target.value)}
              disabled={isSubmitting}
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='dropOffPostOfficeId'>Bưu cục *</Label>
            <TmsCombobox
              id='dropOffPostOfficeId'
              value={postOfficeIdInput}
              onValueChange={onPostOfficeIdInputChange}
              options={postOfficeOptions}
              placeholder='Chọn bưu cục'
              emptyText='Không tìm thấy bưu cục'
              disabled={isLoadingPostOffices || isSubmitting}
              loading={isLoadingPostOffices}
            />
          </div>
        </div>

        <p className='text-xs text-muted-foreground'>
          Backend sẽ kiểm tra quyền quản lý bưu cục đã chọn trước khi xác nhận.
        </p>

        <Button type='button' onClick={onSubmit} disabled={isSubmitting}>
          {isSubmitting ? (
            <Loader2 className='mr-2 h-4 w-4 animate-spin' />
          ) : null}
          Xác nhận đơn gửi tại bưu cục
        </Button>
      </CardContent>
    </Card>
  );
};
