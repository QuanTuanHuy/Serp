/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

import React from 'react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Checkbox,
  Input,
} from '@/shared/components/ui';
import { AddOn } from '../types';
import { cn } from '@/shared/utils';

interface AddOnsSectionProps {
  addOns: AddOn[];
  onToggle: (addOnId: string) => void;
  onQuantityChange: (addOnId: string, quantity: number) => void;
}

export const AddOnsSection: React.FC<AddOnsSectionProps> = ({
  addOns,
  onToggle,
  onQuantityChange,
}) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Optional Add-ons</CardTitle>
        <p className='text-sm text-muted-foreground'>
          Enhance your subscription with these additional features
        </p>
      </CardHeader>
      <CardContent className='space-y-4'>
        {addOns.map((addOn) => (
          <div
            key={addOn.id}
            className={cn(
              'flex items-start gap-4 p-4 border rounded-lg transition-colors',
              addOn.selected && 'border-primary bg-primary/5'
            )}
          >
            <Checkbox
              id={addOn.id}
              checked={addOn.selected}
              onCheckedChange={() => onToggle(addOn.id)}
              className='mt-1'
            />
            <div className='flex-1'>
              <label
                htmlFor={addOn.id}
                className='cursor-pointer font-medium block'
              >
                {addOn.name}
              </label>
              <p className='text-sm text-muted-foreground mt-1'>
                {addOn.description}
              </p>
              <p className='text-sm font-semibold text-primary mt-2'>
                +${addOn.price} {addOn.unit}
              </p>
            </div>
            {addOn.selected && addOn.id === 'extra-users' && (
              <div className='flex items-center gap-2'>
                <label htmlFor={`${addOn.id}-quantity`} className='text-sm'>
                  Qty:
                </label>
                <Input
                  id={`${addOn.id}-quantity`}
                  type='number'
                  min='1'
                  max='100'
                  value={addOn.quantity || 1}
                  onChange={(e) =>
                    onQuantityChange(addOn.id, parseInt(e.target.value) || 1)
                  }
                  className='w-20'
                />
              </div>
            )}
            {addOn.selected && addOn.id === 'extra-storage' && (
              <div className='flex items-center gap-2'>
                <label htmlFor={`${addOn.id}-quantity`} className='text-sm'>
                  Qty:
                </label>
                <Input
                  id={`${addOn.id}-quantity`}
                  type='number'
                  min='1'
                  max='50'
                  value={addOn.quantity || 1}
                  onChange={(e) =>
                    onQuantityChange(addOn.id, parseInt(e.target.value) || 1)
                  }
                  className='w-20'
                />
              </div>
            )}
          </div>
        ))}
      </CardContent>
    </Card>
  );
};
