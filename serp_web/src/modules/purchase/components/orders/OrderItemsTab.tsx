/*
Author: QuanTuanHuy
Description: Part of Serp Project - Order Items Tab Component with Inline Editing
*/

'use client';

import React, { useState } from 'react';
import {
  Button,
  Input,
  Select,
  SelectTrigger,
  SelectValue,
  SelectContent,
  SelectItem,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';
import {
  Table,
  TableHeader,
  TableBody,
  TableRow,
  TableHead,
  TableCell,
} from '@/shared/components/ui/table';
import { Pencil, Trash2, Check, X, Plus } from 'lucide-react';
import { useForm } from 'react-hook-form';
import {
  useUpdateOrderItemMutation,
  useDeleteOrderItemMutation,
  useAddProductToOrderMutation,
  useGetProductsQuery,
} from '../../services';
import { useNotification } from '@/shared/hooks/use-notification';
import { usePermissions } from '@/modules/account';
import type {
  OrderDetail,
  OrderItem,
  UpdateOrderItemRequest,
  AddOrderItemRequest,
} from '../../types';

interface OrderItemsTabProps {
  order: OrderDetail;
  onRefresh: () => void;
}

export const OrderItemsTab: React.FC<OrderItemsTabProps> = ({
  order,
  onRefresh,
}) => {
  const { success, error: showError } = useNotification();
  const { hasAnyRole } = usePermissions();
  const canEditOrder = hasAnyRole(['PURCHASE_STAFF', 'PURCHASE_ADMIN']);
  const [editingItemId, setEditingItemId] = useState<string | null>(null);
  const [isAddingNew, setIsAddingNew] = useState(false);

  const { data: productsResponse } = useGetProductsQuery({
    page: 0,
    pageSize: 1000,
  });

  const [updateOrderItem, { isLoading: isUpdating }] =
    useUpdateOrderItemMutation();
  const [deleteOrderItem, { isLoading: isDeleting }] =
    useDeleteOrderItemMutation();
  const [addProductToOrder, { isLoading: isAdding }] =
    useAddProductToOrderMutation();

  const products = productsResponse?.data?.items || [];
  const productMap = new Map(products.map((p: any) => [p.id, p]));

  // Form for editing existing item
  const {
    register: registerEdit,
    handleSubmit: handleSubmitEdit,
    setValue: setEditValue,
    watch: watchEdit,
    reset: resetEdit,
  } = useForm<UpdateOrderItemRequest>();

  // Form for adding new item
  const {
    register: registerAdd,
    handleSubmit: handleSubmitAdd,
    setValue: setAddValue,
    watch: watchAdd,
    reset: resetAdd,
  } = useForm<AddOrderItemRequest>();

  const handleEdit = (item: OrderItem) => {
    setEditingItemId(item.id);
    setEditValue('orderItemSeqId', item.orderItemSeqId);
    setEditValue('quantity', item.quantity);
    setEditValue('tax', item.tax || 0);
    setEditValue('discount', item.discount || 0);
  };

  const handleCancelEdit = () => {
    setEditingItemId(null);
    resetEdit();
  };

  const handleSaveEdit = async (
    data: UpdateOrderItemRequest,
    itemId: string
  ) => {
    try {
      const result = await updateOrderItem({
        orderId: order.id,
        orderItemId: itemId,
        data,
      }).unwrap();

      if (result.code === 200 && result.status.toLowerCase() === 'success') {
        success('Cập nhật sản phẩm thành công');
        setEditingItemId(null);
        resetEdit();
        onRefresh();
      } else {
        showError('Cập nhật thất bại', { description: result.message });
      }
    } catch (err: any) {
      showError('Lỗi khi cập nhật', {
        description: err?.data?.message || 'Đã xảy ra lỗi không mong muốn',
      });
    }
  };

  const handleDelete = async (itemId: string) => {
    if (!confirm('Bạn có chắc chắn muốn xóa sản phẩm này?')) return;

    try {
      const result = await deleteOrderItem({
        orderId: order.id,
        orderItemId: itemId,
      }).unwrap();

      if (result.code === 200 && result.status.toLowerCase() === 'success') {
        success('Xóa sản phẩm thành công');
        onRefresh();
      } else {
        showError('Xóa thất bại', { description: result.message });
      }
    } catch (err: any) {
      showError('Lỗi khi xóa', {
        description: err?.data?.message || 'Đã xảy ra lỗi không mong muốn',
      });
    }
  };

  const handleAddNew = () => {
    setIsAddingNew(true);
    const nextSeqId =
      Math.max(0, ...(order.orderItems?.map((i) => i.orderItemSeqId) || [])) +
      1;
    setAddValue('orderItemSeqId', nextSeqId);
    setAddValue('quantity', 1);
    setAddValue('tax', 0);
    setAddValue('discount', 0);
  };

  const handleCancelAdd = () => {
    setIsAddingNew(false);
    resetAdd();
  };

  const handleSaveAdd = async (data: AddOrderItemRequest) => {
    try {
      const result = await addProductToOrder({
        orderId: order.id,
        data,
      }).unwrap();

      if (result.code === 200 && result.status.toLowerCase() === 'success') {
        success('Thêm sản phẩm thành công');
        setIsAddingNew(false);
        resetAdd();
        onRefresh();
      } else {
        showError('Thêm thất bại', { description: result.message });
      }
    } catch (err: any) {
      showError('Lỗi khi thêm', {
        description: err?.data?.message || 'Đã xảy ra lỗi không mong muốn',
      });
    }
  };

  const calculateAmount = (
    price: number,
    quantity: number,
    tax: number,
    discount: number
  ) => {
    const subtotal = price * quantity;
    const taxAmount = subtotal * (tax / 100);
    const discountAmount = subtotal * (discount / 100);
    return subtotal + taxAmount - discountAmount;
  };

  const totalAmount =
    order.orderItems?.reduce((sum, item) => sum + item.amount, 0) || 0;

  const getAvailableProducts = () => {
    const usedProductIds = new Set(
      order.orderItems?.map((item) => item.productId) || []
    );
    return products.filter((p: any) => !usedProductIds.has(p.id));
  };

  return (
    <div className='space-y-4'>
      <Card>
        <CardHeader>
          <div className='flex items-center justify-between'>
            <CardTitle>Danh sách sản phẩm</CardTitle>
            {canEditOrder && !isAddingNew && (
              <Button
                onClick={handleAddNew}
                disabled={
                  editingItemId !== null || getAvailableProducts().length === 0
                }
                variant='outline'
              >
                <Plus className='mr-2 h-4 w-4' />
                Thêm sản phẩm
              </Button>
            )}
          </div>
        </CardHeader>
        <CardContent className='p-6'>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Sản phẩm</TableHead>
                <TableHead className='text-right'>Số lượng</TableHead>
                <TableHead>Đơn vị</TableHead>
                <TableHead className='text-right'>Đơn giá</TableHead>
                <TableHead className='text-right'>Thuế (%)</TableHead>
                <TableHead className='text-right'>Giảm giá</TableHead>
                <TableHead className='text-right'>Thành tiền</TableHead>
                <TableHead className='text-center'>Hành động</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {order.orderItems?.map((item) => {
                const product = productMap.get(item.productId);
                const isEditing = editingItemId === item.id;

                return (
                  <TableRow key={item.id}>
                    <TableCell>{product?.name || 'N/A'}</TableCell>
                    <TableCell className='text-right'>
                      {isEditing ? (
                        <Input
                          type='number'
                          {...registerEdit('quantity', { valueAsNumber: true })}
                          className='w-20 text-right'
                          min='1'
                        />
                      ) : (
                        item.quantity
                      )}
                    </TableCell>
                    <TableCell>{item.unit || 'N/A'}</TableCell>
                    <TableCell className='text-right'>
                      {item.price?.toLocaleString('vi-VN')} ₫
                    </TableCell>
                    <TableCell className='text-right'>
                      {isEditing ? (
                        <Input
                          type='number'
                          {...registerEdit('tax', { valueAsNumber: true })}
                          className='w-20 text-right'
                          min='0'
                          max='100'
                          step='0.01'
                        />
                      ) : (
                        `${item.tax || 0}%`
                      )}
                    </TableCell>
                    <TableCell className='text-right'>
                      {isEditing ? (
                        <Input
                          type='number'
                          {...registerEdit('discount', { valueAsNumber: true })}
                          className='w-20 text-right'
                          min='0'
                          max='100'
                          step='0.01'
                        />
                      ) : (
                        `${item.discount || 0}₫`
                      )}
                    </TableCell>
                    <TableCell className='text-right font-medium'>
                      {item.amount?.toLocaleString('vi-VN')} ₫
                    </TableCell>
                    <TableCell>
                      {canEditOrder && (
                        <div className='flex items-center justify-center gap-2'>
                          {isEditing ? (
                            <>
                              <Button
                                size='sm'
                                variant='ghost'
                                onClick={handleSubmitEdit((data) =>
                                  handleSaveEdit(data, item.id)
                                )}
                                disabled={isUpdating}
                              >
                                <Check className='h-4 w-4 text-green-600' />
                              </Button>
                              <Button
                                size='sm'
                                variant='ghost'
                                onClick={handleCancelEdit}
                                disabled={isUpdating}
                              >
                                <X className='h-4 w-4 text-red-600' />
                              </Button>
                            </>
                          ) : (
                            <>
                              <Button
                                size='sm'
                                variant='ghost'
                                onClick={() => handleEdit(item)}
                                disabled={editingItemId !== null || isAddingNew}
                              >
                                <Pencil className='h-4 w-4' />
                              </Button>
                              <Button
                                size='sm'
                                variant='ghost'
                                onClick={() => handleDelete(item.id)}
                                disabled={
                                  isDeleting ||
                                  editingItemId !== null ||
                                  isAddingNew
                                }
                              >
                                <Trash2 className='h-4 w-4 text-red-600' />
                              </Button>
                            </>
                          )}
                        </div>
                      )}
                    </TableCell>
                  </TableRow>
                );
              })}

              {/* Add New Row */}
              {isAddingNew && (
                <TableRow className='bg-muted/50'>
                  <TableCell>
                    <Select
                      value={watchAdd('productId')}
                      onValueChange={(value) => setAddValue('productId', value)}
                    >
                      <SelectTrigger>
                        <SelectValue placeholder='Chọn sản phẩm' />
                      </SelectTrigger>
                      <SelectContent>
                        {getAvailableProducts().map((product: any) => (
                          <SelectItem key={product.id} value={product.id}>
                            {product.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </TableCell>
                  <TableCell className='text-right'>
                    <Input
                      type='number'
                      {...registerAdd('quantity', { valueAsNumber: true })}
                      className='w-20 text-right'
                      min='1'
                    />
                  </TableCell>
                  <TableCell>-</TableCell>
                  <TableCell className='text-right'>-</TableCell>
                  <TableCell className='text-right'>
                    <Input
                      type='number'
                      {...registerAdd('tax', { valueAsNumber: true })}
                      className='w-20 text-right'
                      min='0'
                      max='100'
                      step='0.01'
                    />
                  </TableCell>
                  <TableCell className='text-right'>
                    <Input
                      type='number'
                      {...registerAdd('discount', { valueAsNumber: true })}
                      className='w-20 text-right'
                      min='0'
                      max='100'
                      step='0.01'
                    />
                  </TableCell>
                  <TableCell className='text-right'>-</TableCell>
                  <TableCell>
                    <div className='flex items-center justify-center gap-2'>
                      <Button
                        size='sm'
                        variant='ghost'
                        onClick={handleSubmitAdd(handleSaveAdd)}
                        disabled={isAdding}
                      >
                        <Check className='h-4 w-4 text-green-600' />
                      </Button>
                      <Button
                        size='sm'
                        variant='ghost'
                        onClick={handleCancelAdd}
                        disabled={isAdding}
                      >
                        <X className='h-4 w-4 text-red-600' />
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>

          {/* Total Row */}
          <div className='flex justify-end items-center mt-4 pt-4 border-t'>
            <div className='text-lg font-semibold'>
              Tổng tiền:{' '}
              <span className='text-primary'>
                {totalAmount.toLocaleString('vi-VN')} ₫
              </span>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};
