/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Second-mile auto bagging dialog
 */

import type React from 'react';

import { Loader2 } from 'lucide-react';

import {
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Label,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components';

import { TmsCombobox } from '../../../components/TmsCombobox';
import type {
  AutoSecondMileBaggingPlan,
  Hub,
  SecondMileOrder,
} from '../../../types';
import {
  BAG_DESTINATION_TYPE_OPTIONS,
  type AutoBaggingFormValues,
  formatNumber,
} from '../bagPageModels';
import { AutoBaggingOrderMultiSelect } from './AutoBaggingOrderMultiSelect';

interface AutoBaggingDialogProps {
  open: boolean;
  values: AutoBaggingFormValues;
  hubs: Hub[];
  orders: SecondMileOrder[];
  destinationPostOfficeOptions: Array<{ value: string; label: string }>;
  plan?: AutoSecondMileBaggingPlan | null;
  isPlanning: boolean;
  isExecuting: boolean;
  isOrdersLoading: boolean;
  isLoadingDestinationPostOffices: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmitPreview: (event: React.FormEvent) => void;
  onExecute: () => void;
  onUpdateField: <K extends keyof AutoBaggingFormValues>(
    field: K,
    value: AutoBaggingFormValues[K]
  ) => void;
}

export function AutoBaggingDialog({
  open,
  values,
  hubs,
  orders,
  destinationPostOfficeOptions,
  plan,
  isPlanning,
  isExecuting,
  isOrdersLoading,
  isLoadingDestinationPostOffices,
  onOpenChange,
  onSubmitPreview,
  onExecute,
  onUpdateField,
}: AutoBaggingDialogProps) {
  const hubOptions = hubs.map((hub) => ({
    value: String(hub.id),
    label: `${hub.code} - ${hub.name}`,
  }));

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-4xl'>
        <DialogHeader>
          <DialogTitle>Auto plan bags</DialogTitle>
          <DialogDescription>
            Group inbound orders into new open bags.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={onSubmitPreview} className='space-y-4'>
          <div className='grid gap-4 md:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='auto-origin-hub'>Origin hub *</Label>
              <TmsCombobox
                id='auto-origin-hub'
                value={values.originHubId}
                onValueChange={(value) => {
                  onUpdateField('originHubId', value);
                  if (values.destinationHubId === value) {
                    onUpdateField('destinationHubId', '');
                  }
                  onUpdateField('destinationPostOfficeCode', '');
                }}
                options={hubOptions}
                placeholder='Select origin hub'
                emptyText='No hubs found'
                disabled={isPlanning || isExecuting}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='auto-destination-type'>Destination type *</Label>
              <TmsCombobox
                id='auto-destination-type'
                value={values.destinationType}
                onValueChange={(value) => {
                  onUpdateField(
                    'destinationType',
                    value as AutoBaggingFormValues['destinationType']
                  );
                  onUpdateField('destinationHubId', '');
                  onUpdateField('destinationPostOfficeCode', '');
                }}
                options={BAG_DESTINATION_TYPE_OPTIONS}
                placeholder='Select destination type'
                emptyText='No destination types found'
                disabled={isPlanning || isExecuting}
              />
            </div>

            {values.destinationType === 'HUB' ? (
              <div className='space-y-2'>
                <Label htmlFor='auto-destination-hub'>Destination hub *</Label>
                <TmsCombobox
                  id='auto-destination-hub'
                  value={values.destinationHubId}
                  onValueChange={(value) =>
                    onUpdateField('destinationHubId', value)
                  }
                  options={hubOptions.filter(
                    (hub) => hub.value !== values.originHubId
                  )}
                  placeholder='Select destination hub'
                  emptyText='No destination hubs found'
                  disabled={isPlanning || isExecuting}
                />
              </div>
            ) : (
              <div className='space-y-2'>
                <Label htmlFor='auto-destination-post-office'>
                  Destination post office *
                </Label>
                <TmsCombobox
                  id='auto-destination-post-office'
                  value={values.destinationPostOfficeCode}
                  onValueChange={(value) =>
                    onUpdateField('destinationPostOfficeCode', value)
                  }
                  options={destinationPostOfficeOptions}
                  disabled={
                    isPlanning || isExecuting || isLoadingDestinationPostOffices
                  }
                  loading={isLoadingDestinationPostOffices}
                  placeholder={
                    values.originHubId
                      ? 'Select destination post office'
                      : 'Select origin hub first'
                  }
                  emptyText='No mapped post offices found'
                />
              </div>
            )}

            <div className='space-y-2 md:col-span-2'>
              <Label htmlFor='auto-order-codes'>Orders *</Label>
              <AutoBaggingOrderMultiSelect
                id='auto-order-codes'
                orders={orders}
                selectedOrderCodes={values.orderCodes}
                onSelectionChange={(orderCodes) =>
                  onUpdateField('orderCodes', orderCodes)
                }
                disabled={isPlanning || isExecuting}
                loading={isOrdersLoading}
                placeholder='Select orders'
              />
            </div>
          </div>

          {plan && (
            <div className='rounded-md border'>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Bag code</TableHead>
                    <TableHead>Orders</TableHead>
                    <TableHead>Weight</TableHead>
                    <TableHead>Volume</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {plan.items.length === 0 ? (
                    <TableRow>
                      <TableCell
                        colSpan={4}
                        className='h-20 text-center text-muted-foreground'
                      >
                        No bag groups returned.
                      </TableCell>
                    </TableRow>
                  ) : (
                    plan.items.map((item) => (
                      <TableRow key={item.bagCode}>
                        <TableCell className='font-medium'>
                          {item.bagCode}
                        </TableCell>
                        <TableCell>{item.orderCodes.join(', ')}</TableCell>
                        <TableCell>
                          {formatNumber(item.totalWeight)} kg
                        </TableCell>
                        <TableCell>
                          {formatNumber(item.totalVolume)} m3
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </div>
          )}

          <DialogFooter>
            <Button
              type='button'
              variant='outline'
              disabled={isPlanning || isExecuting}
              onClick={() => onOpenChange(false)}
            >
              Cancel
            </Button>
            <Button type='submit' variant='outline' disabled={isPlanning}>
              {isPlanning && <Loader2 className='h-4 w-4 animate-spin' />}
              Preview
            </Button>
            <Button
              type='button'
              disabled={
                !plan || isPlanning || isExecuting || plan.items.length === 0
              }
              onClick={onExecute}
            >
              {isExecuting && <Loader2 className='h-4 w-4 animate-spin' />}
              Execute
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
