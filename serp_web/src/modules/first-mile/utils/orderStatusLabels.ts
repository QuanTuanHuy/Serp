/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - TMS order status labels
 */

import type { FirstMileOrderStatus, SecondMileOrderStatus } from '../types';

export type TmsOrderStatus = FirstMileOrderStatus | SecondMileOrderStatus;

export const ORDER_STATUS_LABELS: Record<TmsOrderStatus, string> = {
  CREATED: 'Mới tạo',
  ASSIGNED_TO_PICKUP: 'Đã phân công lấy hàng',
  PICKING_UP: 'Đang lấy hàng',
  PICKUP_FAILED: 'Lấy hàng thất bại',
  PICKED_UP: 'Đã lấy hàng',
  PENDING_ORIGIN_POST_OFFICE_INBOUND: 'Chờ nhập bưu cục gửi',
  AT_ORIGIN_POST_OFFICE: 'Tại bưu cục gửi',
  OUTBOUND_READY_FROM_PO: 'Sẵn sàng bàn giao sang hub',
  INBOUND_AT_ORIGIN_HUB: 'Hub gửi đã nhận đơn',
  BAGGING_IN_PROGRESS: 'Đang gom vào bao',
  BAGGED: 'Đã gom vào bao',
  BAG_SEALED: 'Bao đã niêm phong',
  BAG_IN_TRANSIT: 'Bao đang vận chuyển',
  INBOUND_AT_DESTINATION_HUB: 'Hub đích đã nhận đơn',
  INBOUND_AT_DESTINATION_POST_OFFICE: 'Bưu cục đích đã nhận đơn',
  READY_FOR_DELIVERY: 'Sẵn sàng giao',
  OUT_FOR_DELIVERY: 'Đang giao',
  DELIVERED: 'Đã giao',
  DELIVERY_FAILED: 'Giao hàng thất bại',
  RETURNED_TO_SENDER: 'Hoàn về người gửi',
  CANCELLED: 'Đã hủy',
  LOST_OR_DAMAGED: 'Thất lạc / hư hỏng',
};

export const formatOrderStatusLabel = (status?: string | null): string => {
  if (!status) {
    return '--';
  }

  return ORDER_STATUS_LABELS[status as TmsOrderStatus] ?? status;
};
