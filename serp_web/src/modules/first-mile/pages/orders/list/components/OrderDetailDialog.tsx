/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Order detail dialog
 */

import React from 'react';
import {
  Badge,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui';
import { Loader2, MapPin } from 'lucide-react';
import type {
  FirstMileOrderDetail,
  FirstMileOrderStatus,
} from '../../../../types';
import { getOrderProductCategoryLabel } from '../orderPageModels';
import { OrderRoutePreviewMap } from './OrderRoutePreviewMap';

interface OrderDetailDialogProps {
  open: boolean;
  detailOrder: FirstMileOrderDetail | null;
  onOpenChange: (open: boolean) => void;
  formatStatusLabel: (status: FirstMileOrderStatus) => string;
  formatPickupMethodLabel: (
    pickupMethod?: FirstMileOrderDetail['pickupMethod']
  ) => string;
  buildOrderAddressLabel: (
    name?: string,
    phone?: string,
    addressDetail?: string
  ) => string;
  getProvinceLabel: (provinceCode?: string) => string;
  getWardLabel: (provinceCode?: string, wardCode?: string) => string;
  formatDateTime: (value?: string) => string;
}

export const OrderDetailDialog: React.FC<OrderDetailDialogProps> = ({
  open,
  detailOrder,
  onOpenChange,
  formatStatusLabel,
  formatPickupMethodLabel,
  buildOrderAddressLabel,
  getProvinceLabel,
  getWardLabel,
  formatDateTime,
}) => {
  const hasRouteCoordinates = React.useMemo(() => {
    if (!detailOrder) {
      return false;
    }

    return (
      typeof detailOrder.senderLatitude === 'number' &&
      Number.isFinite(detailOrder.senderLatitude) &&
      typeof detailOrder.senderLongitude === 'number' &&
      Number.isFinite(detailOrder.senderLongitude) &&
      typeof detailOrder.receiverLatitude === 'number' &&
      Number.isFinite(detailOrder.receiverLatitude) &&
      typeof detailOrder.receiverLongitude === 'number' &&
      Number.isFinite(detailOrder.receiverLongitude)
    );
  }, [detailOrder]);

  const formatCoordinate = (value?: number): string => {
    if (value === undefined || value === null || !Number.isFinite(value)) {
      return '--';
    }

    return value.toFixed(6);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-h-[90vh] overflow-y-auto sm:max-w-3xl'>
        <DialogHeader>
          <DialogTitle>Chi tiết đơn hàng</DialogTitle>
          <DialogDescription>
            Xem đầy đủ thông tin của một đơn hàng first-mile.
          </DialogDescription>
        </DialogHeader>

        {!detailOrder ? (
          <div className='flex items-center gap-2 text-muted-foreground'>
            <Loader2 className='h-4 w-4 animate-spin' />
            Đang tải chi tiết đơn hàng...
          </div>
        ) : (
          <div className='space-y-4 text-sm'>
            <div className='grid gap-3 md:grid-cols-2'>
              <div>
                <p className='text-muted-foreground'>Mã đơn hàng</p>
                <p className='font-medium'>{detailOrder.orderCode}</p>
              </div>
              <div>
                <p className='text-muted-foreground'>Mã đơn của khách hàng</p>
                <p className='font-medium'>
                  {detailOrder.customerOrderCode || '--'}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Trạng thái</p>
                <p className='font-medium'>
                  {formatStatusLabel(detailOrder.status)}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Xác nhận</p>
                <p className='font-medium'>
                  {detailOrder.isConfirm ? 'Đã xác nhận' : 'Chờ xác nhận'}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Phương thức lấy hàng</p>
                <p className='font-medium'>
                  {formatPickupMethodLabel(detailOrder.pickupMethod)}
                </p>
              </div>
            </div>

            <div className='space-y-2 rounded-md border p-3'>
              <p className='font-semibold'>Người gửi</p>
              <p>
                {buildOrderAddressLabel(
                  detailOrder.senderName,
                  detailOrder.senderPhone,
                  detailOrder.senderAddressDetail
                )}
              </p>
              <p className='text-muted-foreground'>
                {getProvinceLabel(detailOrder.senderProvinceCode)} /{' '}
                {getWardLabel(
                  detailOrder.senderProvinceCode,
                  detailOrder.senderWardCode
                )}
              </p>
              <p className='text-xs text-muted-foreground'>
                Mã: {detailOrder.senderProvinceCode || '--'} /{' '}
                {detailOrder.senderWardCode || '--'}
              </p>
              <p className='text-muted-foreground'>
                Tọa độ: {detailOrder.senderLatitude ?? '--'},
                {detailOrder.senderLongitude ?? '--'}
              </p>
            </div>

            <div className='space-y-2 rounded-md border p-3'>
              <p className='font-semibold'>Người nhận</p>
              <p>
                {buildOrderAddressLabel(
                  detailOrder.receiverName,
                  detailOrder.receiverPhone,
                  detailOrder.receiverAddressDetail
                )}
              </p>
              <p className='text-muted-foreground'>
                {getProvinceLabel(detailOrder.receiverProvinceCode)} /{' '}
                {getWardLabel(
                  detailOrder.receiverProvinceCode,
                  detailOrder.receiverWardCode
                )}
              </p>
              <p className='text-xs text-muted-foreground'>
                Mã: {detailOrder.receiverProvinceCode || '--'} /{' '}
                {detailOrder.receiverWardCode || '--'}
              </p>
              <p className='text-muted-foreground'>
                Tọa độ: {detailOrder.receiverLatitude ?? '--'},
                {detailOrder.receiverLongitude ?? '--'}
              </p>
            </div>

            <div className='space-y-2 rounded-md border p-3'>
              <p className='font-semibold'>Bản đồ từ điểm lấy đến điểm giao</p>
              {hasRouteCoordinates ? (
                <>
                  <OrderRoutePreviewMap
                    senderLatitude={detailOrder.senderLatitude as number}
                    senderLongitude={detailOrder.senderLongitude as number}
                    receiverLatitude={detailOrder.receiverLatitude as number}
                    receiverLongitude={detailOrder.receiverLongitude as number}
                    className='h-72'
                  />
                  <p className='text-xs text-muted-foreground'>
                    Mốc xanh: điểm lấy hàng. Mốc đỏ: điểm giao hàng.
                  </p>
                </>
              ) : (
                <p className='text-muted-foreground'>
                  Không thể hiển thị bản đồ lộ trình vì tọa độ đơn hàng chưa đầy
                  đủ.
                </p>
              )}
            </div>

            <div className='grid gap-3 md:grid-cols-2'>
              <div>
                <p className='text-muted-foreground'>Bắt đầu lấy hàng</p>
                <p className='font-medium'>
                  {formatDateTime(detailOrder.pickupTimeStart)}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Kết thúc lấy hàng</p>
                <p className='font-medium'>
                  {formatDateTime(detailOrder.pickupTimeEnd)}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>
                  Thời gian yêu cầu giao hàng
                </p>
                <p className='font-medium'>
                  {detailOrder.deliveryRequestTime || '--'}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Loại đơn hàng</p>
                <p className='font-medium'>{detailOrder.orderType || '--'}</p>
              </div>
              <div>
                <p className='text-muted-foreground'>Phân loại hàng hóa</p>
                <p className='font-medium'>
                  {getOrderProductCategoryLabel(
                    detailOrder.orderProductCategory
                  )}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Bên trả phí</p>
                <p className='font-medium'>{detailOrder.feePayer || '--'}</p>
              </div>
              <div>
                <p className='text-muted-foreground'>Trạng thái thanh toán</p>
                <p className='font-medium'>
                  {detailOrder.paymentStatus || '--'}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Tổng khối lượng</p>
                <p className='font-medium'>{detailOrder.totalWeight ?? '--'}</p>
              </div>
              <div>
                <p className='text-muted-foreground'>Tổng giá trị</p>
                <p className='font-medium'>{detailOrder.totalValue ?? '--'}</p>
              </div>
              <div>
                <p className='text-muted-foreground'>Tổng thể tích</p>
                <p className='font-medium'>{detailOrder.totalVolume ?? '--'}</p>
              </div>
              <div>
                <p className='text-muted-foreground'>Số tiền COD</p>
                <p className='font-medium'>{detailOrder.codAmount ?? '--'}</p>
              </div>
            </div>

            <div className='space-y-2'>
              <p className='text-muted-foreground'>Sản phẩm</p>
              {detailOrder.products && detailOrder.products.length > 0 ? (
                <div className='space-y-2'>
                  {detailOrder.products.map((product, index) => (
                    <div
                      key={`${detailOrder.id}-${product.id ?? index}`}
                      className='rounded-md border p-2'
                    >
                      <p className='font-medium'>{product.name || '--'}</p>
                      <p className='text-xs text-muted-foreground'>
                        SL: {product.quantity ?? '--'} | Khối lượng:{' '}
                        {product.weight ?? '--'}g | Giá trị:{' '}
                        {product.value ?? '--'}
                      </p>
                    </div>
                  ))}
                </div>
              ) : (
                <p className='text-muted-foreground'>
                  Không có chi tiết sản phẩm.
                </p>
              )}
            </div>

            {detailOrder.note ? (
              <div>
                <p className='text-muted-foreground'>Ghi chú</p>
                <p>{detailOrder.note}</p>
              </div>
            ) : null}

            <div className='text-xs text-muted-foreground'>
              Tạo lúc: {formatDateTime(detailOrder.createdAt)} | Cập nhật:{' '}
              {formatDateTime(detailOrder.updatedAt)} | Cập nhật bởi:{' '}
              {detailOrder.updatedBy || '--'}
            </div>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
};
