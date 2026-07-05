/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Order results card
 */

import React from 'react';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';
import {
  Building2,
  CheckCircle2,
  Eye,
  Loader2,
  Pencil,
  Trash2,
  XCircle,
} from 'lucide-react';
import type {
  FirstMileOrderDetail,
  FirstMileOrderStatus,
  FirstMilePaginatedData,
} from '../../../../types';

interface OrderResultsCardProps {
  canViewOrders: boolean;
  canMutateOrders: boolean;
  canConfirmDropOffAtPostOffice: boolean;
  data?: FirstMilePaginatedData<FirstMileOrderDetail>;
  isLoading: boolean;
  isFetching: boolean;
  loadingOrderActionId: number | null;
  confirmingOrderId: number | null;
  loadingDropOffSuggestionOrderId: number | null;
  onViewDetail: (orderId: number) => void;
  onEdit: (order: FirstMileOrderDetail) => void;
  onRequestCancel: (order: FirstMileOrderDetail) => void;
  onRequestDelete: (order: FirstMileOrderDetail) => void;
  onConfirm: (order: FirstMileOrderDetail) => void;
  onOpenDropOffSuggestions: (order: FirstMileOrderDetail) => void;
  onOpenManagerDropOffConfirm: (order: FirstMileOrderDetail) => void;
  onPreviousPage: () => void;
  onNextPage: () => void;
  formatStatusLabel: (status: FirstMileOrderStatus) => string;
  formatPickupMethodLabel: (
    pickupMethod?: FirstMileOrderDetail['pickupMethod']
  ) => string;
  getStatusBadgeVariant: (
    status: FirstMileOrderStatus
  ) => 'default' | 'secondary' | 'outline' | 'destructive';
  isDraftOrder: (order: FirstMileOrderDetail) => boolean;
  isConfirmableStatus: (status: FirstMileOrderStatus) => boolean;
  isDropOffOrder: (order: FirstMileOrderDetail) => boolean;
  buildOrderAddressLabel: (
    name?: string,
    phone?: string,
    addressDetail?: string
  ) => string;
  buildPostOfficeAssignmentLabel: (order: FirstMileOrderDetail) => string;
  formatDateTime: (value?: string) => string;
}

export const OrderResultsCard: React.FC<OrderResultsCardProps> = ({
  canViewOrders,
  canMutateOrders,
  canConfirmDropOffAtPostOffice,
  data,
  isLoading,
  isFetching,
  loadingOrderActionId,
  confirmingOrderId,
  loadingDropOffSuggestionOrderId,
  onViewDetail,
  onEdit,
  onRequestCancel,
  onRequestDelete,
  onConfirm,
  onOpenDropOffSuggestions,
  onOpenManagerDropOffConfirm,
  onPreviousPage,
  onNextPage,
  formatStatusLabel,
  formatPickupMethodLabel,
  getStatusBadgeVariant,
  isDraftOrder,
  isConfirmableStatus,
  isDropOffOrder,
  buildOrderAddressLabel,
  buildPostOfficeAssignmentLabel,
  formatDateTime,
}) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Kết quả ({data?.totalItems ?? 0})</CardTitle>
      </CardHeader>
      <CardContent>
        {!canViewOrders ? (
          <p className='text-muted-foreground'>
            Bạn không có quyền truy cập dữ liệu đơn hàng.
          </p>
        ) : isLoading ? (
          <div className='flex items-center gap-2 text-muted-foreground'>
            <Loader2 className='h-4 w-4 animate-spin' />
            Đang tải đơn hàng...
          </div>
        ) : data && data.items.length > 0 ? (
          <div className='space-y-4'>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className='min-w-[180px]'>Đơn hàng</TableHead>
                  <TableHead className='min-w-[200px]'>Trạng thái</TableHead>
                  <TableHead className='min-w-[180px]'>
                    Phương thức lấy hàng
                  </TableHead>
                  <TableHead className='min-w-[260px]'>Người gửi</TableHead>
                  <TableHead className='min-w-[260px]'>Người nhận</TableHead>
                  <TableHead className='min-w-[160px]'>Bưu cục</TableHead>
                  <TableHead className='min-w-[220px]'>
                    Khung giờ lấy hàng
                  </TableHead>
                  <TableHead className='min-w-[180px]'>Cập nhật</TableHead>
                  <TableHead className='sticky right-0 z-20 border-l bg-card text-right'>
                    Thao tác
                  </TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {data.items.map((order) => {
                  const isLoadingDetail = loadingOrderActionId === order.id;
                  const isConfirming = confirmingOrderId === order.id;
                  const isLoadingSuggestions =
                    loadingDropOffSuggestionOrderId === order.id;
                  const canUpdateDraft = canMutateOrders && isDraftOrder(order);
                  const dropOffOrder = isDropOffOrder(order);
                  const showDropOffSuggestions =
                    canMutateOrders &&
                    dropOffOrder &&
                    isConfirmableStatus(order.status) &&
                    !order.isConfirm;
                  const showCourierConfirm =
                    canMutateOrders &&
                    !dropOffOrder &&
                    isConfirmableStatus(order.status) &&
                    !order.isConfirm;
                  const showManagerDropOffConfirm =
                    canConfirmDropOffAtPostOffice &&
                    dropOffOrder &&
                    isConfirmableStatus(order.status) &&
                    !order.isConfirm;

                  return (
                    <TableRow key={order.id} className='group'>
                      <TableCell className='whitespace-normal'>
                        <p className='font-medium'>{order.orderCode}</p>
                        <p className='text-xs text-muted-foreground'>
                          Mã khách hàng: {order.customerOrderCode || '--'}
                        </p>
                      </TableCell>
                      <TableCell className='whitespace-normal'>
                        <div className='flex flex-wrap gap-1.5'>
                          <Badge variant={getStatusBadgeVariant(order.status)}>
                            {formatStatusLabel(order.status)}
                          </Badge>
                          <Badge variant='outline'>
                            {order.isConfirm ? 'Đã xác nhận' : 'Chờ xác nhận'}
                          </Badge>
                        </div>
                      </TableCell>
                      <TableCell className='whitespace-normal text-xs text-muted-foreground'>
                        {formatPickupMethodLabel(order.pickupMethod)}
                      </TableCell>
                      <TableCell className='whitespace-normal text-xs text-muted-foreground'>
                        {buildOrderAddressLabel(
                          order.senderName,
                          order.senderPhone,
                          order.senderAddressDetail
                        )}
                      </TableCell>
                      <TableCell className='whitespace-normal text-xs text-muted-foreground'>
                        {buildOrderAddressLabel(
                          order.receiverName,
                          order.receiverPhone,
                          order.receiverAddressDetail
                        )}
                      </TableCell>
                      <TableCell className='text-xs text-muted-foreground'>
                        {buildPostOfficeAssignmentLabel(order)}
                      </TableCell>
                      <TableCell className='whitespace-normal text-xs text-muted-foreground'>
                        {formatDateTime(order.pickupTimeStart)} -{' '}
                        {formatDateTime(order.pickupTimeEnd)}
                      </TableCell>
                      <TableCell className='whitespace-normal text-xs text-muted-foreground'>
                        <p>{formatDateTime(order.updatedAt)}</p>
                      </TableCell>
                      <TableCell className='sticky right-0 z-10 border-l bg-background text-right group-hover:bg-muted/50'>
                        <div className='flex items-center justify-end gap-1'>
                          <Button
                            size='icon'
                            variant='outline'
                            title='Xem chi tiết'
                            onClick={() => onViewDetail(order.id)}
                            disabled={isLoadingDetail}
                            className='h-8 w-8'
                          >
                            {isLoadingDetail ? (
                              <Loader2 className='h-3.5 w-3.5 animate-spin' />
                            ) : (
                              <Eye className='h-3.5 w-3.5' />
                            )}
                            <span className='sr-only'>Xem chi tiết</span>
                          </Button>

                          {canUpdateDraft ? (
                            <>
                              <Button
                                size='icon'
                                variant='outline'
                                title='Sửa đơn hàng'
                                onClick={() => onEdit(order)}
                                disabled={isLoadingDetail}
                                className='h-8 w-8'
                              >
                                <Pencil className='h-3.5 w-3.5' />
                                <span className='sr-only'>Sửa đơn hàng</span>
                              </Button>
                              <Button
                                size='icon'
                                variant='outline'
                                title='Hủy đơn hàng'
                                onClick={() => onRequestCancel(order)}
                                className='h-8 w-8'
                              >
                                <XCircle className='h-3.5 w-3.5' />
                                <span className='sr-only'>Hủy đơn hàng</span>
                              </Button>
                              <Button
                                size='icon'
                                variant='destructive'
                                title='Xóa đơn hàng'
                                onClick={() => onRequestDelete(order)}
                                className='h-8 w-8'
                              >
                                <Trash2 className='h-3.5 w-3.5' />
                                <span className='sr-only'>Xóa đơn hàng</span>
                              </Button>
                            </>
                          ) : null}

                          {showCourierConfirm ? (
                            <Button
                              size='icon'
                              variant='outline'
                              title='Xác nhận đơn hàng'
                              onClick={() => onConfirm(order)}
                              disabled={isConfirming}
                              className='h-8 w-8'
                            >
                              {isConfirming ? (
                                <Loader2 className='h-3.5 w-3.5 animate-spin' />
                              ) : (
                                <CheckCircle2 className='h-3.5 w-3.5' />
                              )}
                              <span className='sr-only'>Xác nhận đơn hàng</span>
                            </Button>
                          ) : null}

                          {showDropOffSuggestions ? (
                            <Button
                              size='icon'
                              variant='outline'
                              title='Xem bưu cục gợi ý'
                              onClick={() => onOpenDropOffSuggestions(order)}
                              disabled={isLoadingSuggestions}
                              className='h-8 w-8'
                            >
                              {isLoadingSuggestions ? (
                                <Loader2 className='h-3.5 w-3.5 animate-spin' />
                              ) : (
                                <Building2 className='h-3.5 w-3.5' />
                              )}
                              <span className='sr-only'>Xem bưu cục gợi ý</span>
                            </Button>
                          ) : null}

                          {showManagerDropOffConfirm ? (
                            <Button
                              size='icon'
                              variant='outline'
                              title='Xác nhận gửi tại bưu cục'
                              onClick={() => onOpenManagerDropOffConfirm(order)}
                              className='h-8 w-8'
                            >
                              <CheckCircle2 className='h-3.5 w-3.5' />
                              <span className='sr-only'>
                                Xác nhận gửi tại bưu cục
                              </span>
                            </Button>
                          ) : null}
                        </div>
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>

            <div className='flex items-center justify-between pt-2'>
              <Button
                variant='outline'
                onClick={onPreviousPage}
                disabled={!data.hasPrevious || isFetching}
              >
                Trang trước
              </Button>
              <span className='text-sm text-muted-foreground'>
                Trang {data.currentPage + 1} / {Math.max(data.totalPages, 1)}
              </span>
              <Button
                variant='outline'
                onClick={onNextPage}
                disabled={!data.hasNext || isFetching}
              >
                Trang sau
              </Button>
            </div>
          </div>
        ) : (
          <p className='text-muted-foreground'>Không tìm thấy đơn hàng nào.</p>
        )}
      </CardContent>
    </Card>
  );
};
