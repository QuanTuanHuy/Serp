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
        <CardTitle>Drop-off Confirmation</CardTitle>
        <CardDescription>
          For post office managers: confirm customer drop-off order at the
          current post office.
        </CardDescription>
      </CardHeader>
      <CardContent className='space-y-3'>
        <div className='grid gap-3 md:grid-cols-2'>
          <div className='space-y-2'>
            <Label htmlFor='dropOffOrderId'>Order ID *</Label>
            <Input
              id='dropOffOrderId'
              inputMode='numeric'
              placeholder='Enter order id'
              value={orderIdInput}
              onChange={(event) => onOrderIdInputChange(event.target.value)}
              disabled={isSubmitting}
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='dropOffPostOfficeId'>Post office *</Label>
            <TmsCombobox
              id='dropOffPostOfficeId'
              value={postOfficeIdInput}
              onValueChange={onPostOfficeIdInputChange}
              options={postOfficeOptions}
              placeholder='Select post office'
              emptyText='No post offices found'
              disabled={isLoadingPostOffices || isSubmitting}
              loading={isLoadingPostOffices}
            />
          </div>
        </div>

        <p className='text-xs text-muted-foreground'>
          The backend validates manager assignment to the selected post office
          before confirming.
        </p>

        <Button type='button' onClick={onSubmit} disabled={isSubmitting}>
          {isSubmitting ? (
            <Loader2 className='mr-2 h-4 w-4 animate-spin' />
          ) : null}
          Confirm drop-off order
        </Button>
      </CardContent>
    </Card>
  );
};
