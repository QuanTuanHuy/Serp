/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Create Delivery Manifest Dialog
 */

'use client';

import React, { useState, useMemo } from 'react';
import { Truck, Package, Route, Plus, X } from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import { Card } from '@/shared/components/ui/card';
import { Input } from '@/shared/components/ui/input';
import { Textarea } from '@/shared/components/ui/textarea';
import { Badge } from '@/shared/components/ui/badge';
import { TmsCombobox } from '../../../../components/TmsCombobox';
import {
  useGetActiveCouriersByPostOfficeQuery,
  useGetFirstMileVehiclesQuery,
} from '../../../../api/firstMileApi';
import {
  useGetInboundOrdersQuery,
  useCreateDeliveryManifestMutation,
} from '../../../../api/lastMileApi';

const NONE_VALUE = '__NONE__';

interface Props {
  postOfficeCode: string;
  postOfficeId?: number;
  onClose: () => void;
  onCreated: () => void;
}

export const DeliveryManifestFormDialog: React.FC<Props> = ({
  postOfficeCode,
  postOfficeId,
  onClose,
  onCreated,
}) => {
  const [courierId, setCourierId] = useState('');
  const [vehicleId, setVehicleId] = useState('');
  const [plannedDate, setPlannedDate] = useState(
    new Date().toISOString().slice(0, 10)
  );
  const [plannedDepartureAt, setPlannedDepartureAt] = useState('');
  const [note, setNote] = useState('');
  const [selectedOrders, setSelectedOrders] = useState<Set<string>>(new Set());

  const { data: couriers, isLoading: isLoadingCouriers } =
    useGetActiveCouriersByPostOfficeQuery(postOfficeId ?? 0, {
      skip: !postOfficeId,
    });

  const { data: vehiclesData, isLoading: isLoadingVehicles } =
    useGetFirstMileVehiclesQuery({ page: 0, size: 500 });

  const { data: readyOrders, isLoading: loadingOrders } =
    useGetInboundOrdersQuery({
      postOfficeCode,
      status: 'READY_FOR_DELIVERY',
    });

  const [createManifest, { isLoading: isCreating }] =
    useCreateDeliveryManifestMutation();

  const courierOptions = useMemo(
    () => [
      { value: NONE_VALUE, label: 'Chưa phân công nhân viên' },
      ...(couriers ?? []).map((courier) => ({
        value: String(courier.id),
        label: `${courier.code?.trim() || `NVGN-${courier.id}`} - ${
          courier.fullName?.trim() || 'Nhân viên chưa có tên'
        }`,
      })),
    ],
    [couriers]
  );

  const vehicleOptions = useMemo(
    () => [
      { value: NONE_VALUE, label: 'Chưa phân công xe' },
      ...(vehiclesData?.items ?? [])
        .filter(
          (vehicle) =>
            ((postOfficeId !== undefined &&
              vehicle.postOfficeId === postOfficeId) ||
              vehicle.postOfficeCode === postOfficeCode) &&
            vehicle.status === 'ACTIVE'
        )
        .map((vehicle) => ({
          value: String(vehicle.id),
          label: `${vehicle.licensePlate}${
            vehicle.postOfficeName ? ` - ${vehicle.postOfficeName}` : ''
          }`,
        })),
    ],
    [postOfficeCode, postOfficeId, vehiclesData]
  );

  const handleToggleOrder = (code: string) => {
    setSelectedOrders((prev) => {
      const next = new Set(prev);
      if (next.has(code)) next.delete(code);
      else next.add(code);
      return next;
    });
  };

  const selectAllOrders = () => {
    if (readyOrders) {
      setSelectedOrders(new Set(readyOrders.map((o) => o.orderCode)));
    }
  };

  const handleCreate = async () => {
    if (selectedOrders.size === 0) return;
    try {
      await createManifest({
        postOfficeCode,
        courierId: courierId ? Number(courierId) : undefined,
        vehicleId: vehicleId || undefined,
        plannedDate,
        plannedDepartureAt: plannedDepartureAt || undefined,
        orderCodes: Array.from(selectedOrders),
        note: note || undefined,
      }).unwrap();
      onCreated();
    } catch {
      // Error handled by RTK Query
    }
  };

  return (
    <div className='fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4'>
      <Card className='w-full max-w-3xl max-h-[90vh] overflow-y-auto p-6'>
        <div className='flex items-center justify-between mb-6'>
          <div className='flex items-center gap-2'>
            <Route className='h-5 w-5 text-primary' />
            <h2 className='text-lg font-semibold'>Tạo bảng kê giao hàng</h2>
          </div>
          <Button variant='ghost' size='sm' onClick={onClose}>
            <X className='h-4 w-4' />
          </Button>
        </div>

        {/* Info Banner */}
        <div className='bg-blue-50 dark:bg-blue-950/30 border border-blue-200 rounded-lg p-3 mb-4 text-sm text-blue-700 dark:text-blue-300'>
          Chọn các đơn đã sẵn sàng giao. Hệ thống sẽ tự động tối ưu tuyến giao
          bằng thuật toán Nearest Neighbor + 2-opt.
        </div>

        {/* Form Fields */}
        <div className='grid grid-cols-2 gap-4 mb-6'>
          <div className='space-y-1'>
            <label className='text-xs font-medium text-muted-foreground'>
              Nhân viên giao nhận
            </label>
            <TmsCombobox
              id='delivery-manifest-courier'
              value={courierId || NONE_VALUE}
              onValueChange={(value) =>
                setCourierId(value === NONE_VALUE ? '' : value)
              }
              options={courierOptions}
              placeholder='Chọn nhân viên giao nhận'
              emptyText={
                postOfficeId
                  ? 'Không tìm thấy nhân viên giao nhận'
                  : 'Chọn bưu cục trước'
              }
              disabled={!postOfficeId || isLoadingCouriers}
              loading={isLoadingCouriers}
            />
          </div>
          <div className='space-y-1'>
            <label className='text-xs font-medium text-muted-foreground'>
              Xe
            </label>
            <TmsCombobox
              id='delivery-manifest-vehicle'
              value={vehicleId || NONE_VALUE}
              onValueChange={(value) =>
                setVehicleId(value === NONE_VALUE ? '' : value)
              }
              options={vehicleOptions}
              placeholder='Chọn xe'
              emptyText='Không tìm thấy xe đang hoạt động'
              disabled={isLoadingVehicles}
              loading={isLoadingVehicles}
            />
          </div>
          <div className='space-y-1'>
            <label className='text-xs font-medium text-muted-foreground'>
              Ngày dự kiến
            </label>
            <Input
              type='date'
              value={plannedDate}
              onChange={(e) => setPlannedDate(e.target.value)}
            />
          </div>
          <div className='space-y-1'>
            <label className='text-xs font-medium text-muted-foreground'>
              Thời gian xuất phát dự kiến
            </label>
            <Input
              type='datetime-local'
              value={plannedDepartureAt}
              onChange={(e) => setPlannedDepartureAt(e.target.value)}
            />
          </div>
          <div className='col-span-2 space-y-1'>
            <label className='text-xs font-medium text-muted-foreground'>
              Ghi chú
            </label>
            <Textarea
              placeholder='Ví dụ: khu vực ưu tiên, hướng dẫn đặc biệt...'
              value={note}
              onChange={(e) => setNote(e.target.value)}
              rows={2}
            />
          </div>
        </div>

        {/* Order Selection */}
        <div className='space-y-3'>
          <div className='flex items-center justify-between'>
            <h3 className='font-medium flex items-center gap-2'>
              <Package className='h-4 w-4' />
              Đơn sẵn sàng giao ({readyOrders?.length ?? 0})
            </h3>
            <div className='flex gap-2'>
              <Badge variant='outline'>Đã chọn {selectedOrders.size}</Badge>
              <Button variant='outline' size='sm' onClick={selectAllOrders}>
                Chọn tất cả
              </Button>
            </div>
          </div>

          {loadingOrders ? (
            <p className='text-sm text-muted-foreground py-4 text-center'>
              Đang tải đơn khả dụng...
            </p>
          ) : !readyOrders?.length ? (
            <div className='text-center py-6 text-muted-foreground border rounded-lg'>
              <Package className='h-8 w-8 mx-auto mb-2 opacity-50' />
              <p className='text-sm'>Không có đơn sẵn sàng giao.</p>
              <p className='text-xs mt-1'>
                Hãy xác nhận đơn nhập bưu cục trước ở trang phân loại đơn nhập.
              </p>
            </div>
          ) : (
            <div className='border rounded-lg max-h-60 overflow-y-auto divide-y'>
              {readyOrders.map((order) => (
                <label
                  key={order.orderCode}
                  className='flex items-center gap-3 p-3 hover:bg-muted/50 cursor-pointer'
                >
                  <input
                    type='checkbox'
                    checked={selectedOrders.has(order.orderCode)}
                    onChange={() => handleToggleOrder(order.orderCode)}
                    className='h-4 w-4 rounded'
                  />
                  <div className='flex-1 min-w-0'>
                    <span className='font-mono text-sm font-medium'>
                      {order.orderCode}
                    </span>
                    <span className='text-xs text-muted-foreground ml-2'>
                      {order.receiverName} • {order.receiverAddressDetail}
                    </span>
                  </div>
                  {order.codAmount > 0 && (
                    <span className='text-xs text-amber-600 font-medium'>
                      COD {order.codAmount.toLocaleString()} VND
                    </span>
                  )}
                </label>
              ))}
            </div>
          )}
        </div>

        {/* Actions */}
        <div className='flex justify-end gap-3 mt-6 pt-4 border-t'>
          <Button variant='outline' onClick={onClose}>
            Hủy
          </Button>
          <Button
            onClick={handleCreate}
            disabled={selectedOrders.size === 0 || isCreating}
          >
            <Truck className='h-4 w-4 mr-2' />
            {isCreating
              ? 'Đang tạo...'
              : `Tạo bảng kê (${selectedOrders.size} đơn)`}
          </Button>
        </div>
      </Card>
    </div>
  );
};
