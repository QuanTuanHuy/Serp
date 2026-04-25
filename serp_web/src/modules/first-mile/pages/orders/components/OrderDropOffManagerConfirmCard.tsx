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
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import { Loader2 } from 'lucide-react';
import type { PostOffice } from '../../../types';

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
            <Select
              value={postOfficeIdInput || undefined}
              onValueChange={onPostOfficeIdInputChange}
              disabled={isLoadingPostOffices || isSubmitting}
            >
              <SelectTrigger id='dropOffPostOfficeId'>
                <SelectValue placeholder='Select post office' />
              </SelectTrigger>
              <SelectContent>
                {postOffices.map((postOffice) => (
                  <SelectItem key={postOffice.id} value={String(postOffice.id)}>
                    {postOffice.code} - {postOffice.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
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
