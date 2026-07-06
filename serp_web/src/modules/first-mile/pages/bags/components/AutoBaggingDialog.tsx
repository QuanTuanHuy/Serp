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
          <DialogTitle>Tự động lập túi</DialogTitle>
          <DialogDescription>
            Gom các đơn đã về hub gốc vào những túi đang mở mới.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={onSubmitPreview} className='space-y-4'>
          <div className='grid gap-4 md:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='auto-origin-hub'>Hub gốc *</Label>
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
                placeholder='Chọn hub gốc'
                emptyText='Không tìm thấy hub'
                disabled={isPlanning || isExecuting}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='auto-destination-type'>Loại điểm đến *</Label>
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
                placeholder='Chọn loại điểm đến'
                emptyText='Không tìm thấy loại điểm đến'
                disabled={isPlanning || isExecuting}
              />
            </div>

            {values.destinationType === 'HUB' ? (
              <div className='space-y-2'>
                <Label htmlFor='auto-destination-hub'>Hub đích *</Label>
                <TmsCombobox
                  id='auto-destination-hub'
                  value={values.destinationHubId}
                  onValueChange={(value) =>
                    onUpdateField('destinationHubId', value)
                  }
                  options={hubOptions.filter(
                    (hub) => hub.value !== values.originHubId
                  )}
                  placeholder='Chọn hub đích'
                  emptyText='Không tìm thấy hub đích'
                  disabled={isPlanning || isExecuting}
                />
              </div>
            ) : (
              <div className='space-y-2'>
                <Label htmlFor='auto-destination-post-office'>
                  Bưu cục đích *
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
                      ? 'Chọn bưu cục đích'
                      : 'Chọn hub gốc trước'
                  }
                  emptyText='Không tìm thấy bưu cục được liên kết'
                />
              </div>
            )}

            <div className='space-y-2 md:col-span-2'>
              <Label htmlFor='auto-order-codes'>Đơn hàng *</Label>
              <AutoBaggingOrderMultiSelect
                id='auto-order-codes'
                orders={orders}
                selectedOrderCodes={values.orderCodes}
                onSelectionChange={(orderCodes) =>
                  onUpdateField('orderCodes', orderCodes)
                }
                disabled={isPlanning || isExecuting}
                loading={isOrdersLoading}
                placeholder='Chọn đơn hàng'
              />
            </div>
          </div>

          {plan && (
            <div className='rounded-md border'>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Mã túi</TableHead>
                    <TableHead>Đơn hàng</TableHead>
                    <TableHead>Khối lượng</TableHead>
                    <TableHead>Thể tích</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {plan.items.length === 0 ? (
                    <TableRow>
                      <TableCell
                        colSpan={4}
                        className='h-20 text-center text-muted-foreground'
                      >
                        Chưa có nhóm túi nào được trả về.
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
              Hủy
            </Button>
            <Button type='submit' variant='outline' disabled={isPlanning}>
              {isPlanning && <Loader2 className='h-4 w-4 animate-spin' />}
              Xem trước
            </Button>
            <Button
              type='button'
              disabled={
                !plan || isPlanning || isExecuting || plan.items.length === 0
              }
              onClick={onExecute}
            >
              {isExecuting && <Loader2 className='h-4 w-4 animate-spin' />}
              Thực hiện
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
