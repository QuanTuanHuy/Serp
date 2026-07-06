/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Inbound Sorting Page (Last-mile)
 */

'use client';

import React, { useState, useCallback, useMemo } from 'react';
import {
  PackageSearch,
  CheckCircle2,
  ScanBarcode,
  Info,
  Package,
  Phone,
  MapPin,
  Banknote,
} from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import { Card } from '@/shared/components/ui/card';
import { Input } from '@/shared/components/ui/input';
import { Badge } from '@/shared/components/ui/badge';
import { TmsCombobox } from '../../../components/TmsCombobox';
import { useGetPostOfficesQuery } from '../../../api/firstMileApi';
import {
  useGetInboundOrdersQuery,
  useConfirmInboundOrdersMutation,
} from '../../../api/lastMileApi';
import type { InboundOrderResponse } from '../../../types';

export const InboundSortingPage: React.FC = () => {
  const [postOfficeCode, setPostOfficeCode] = useState('');
  const [searchCode, setSearchCode] = useState('');
  const [selectedOrders, setSelectedOrders] = useState<Set<string>>(new Set());
  const [scanInput, setScanInput] = useState('');

  const { data: postOfficesData, isLoading: isLoadingPostOffices } =
    useGetPostOfficesQuery({ page: 0, size: 200, status: 'ACTIVE' });

  const postOfficeOptions = useMemo(
    () =>
      (postOfficesData?.items ?? []).map((postOffice) => ({
        value: postOffice.code,
        label: `${postOffice.code} - ${postOffice.name}`,
      })),
    [postOfficesData]
  );

  const {
    data: inboundOrders,
    isLoading,
    refetch,
  } = useGetInboundOrdersQuery(
    { postOfficeCode, status: 'INBOUND_AT_DESTINATION_POST_OFFICE' },
    { skip: !postOfficeCode }
  );

  const [confirmInbound, { isLoading: isConfirming }] =
    useConfirmInboundOrdersMutation();

  const handlePostOfficeChange = useCallback((value: string) => {
    setPostOfficeCode(value);
    setSelectedOrders(new Set());
    setSearchCode('');
  }, []);

  const handleSearch = useCallback(() => {
    if (postOfficeCode) {
      refetch();
    }
  }, [postOfficeCode, refetch]);

  const handleScanOrder = useCallback(() => {
    const code = scanInput.trim().toUpperCase();
    if (code) {
      setSelectedOrders((prev) => new Set([...prev, code]));
      setScanInput('');
    }
  }, [scanInput]);

  const handleScanKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      handleScanOrder();
    }
  };

  const toggleOrder = (orderCode: string) => {
    setSelectedOrders((prev) => {
      const next = new Set(prev);
      if (next.has(orderCode)) {
        next.delete(orderCode);
      } else {
        next.add(orderCode);
      }
      return next;
    });
  };

  const selectAll = () => {
    if (inboundOrders) {
      setSelectedOrders(new Set(inboundOrders.map((o) => o.orderCode)));
    }
  };

  const handleConfirmInbound = async () => {
    if (selectedOrders.size === 0) return;
    try {
      await confirmInbound({
        postOfficeCode,
        orderCodes: Array.from(selectedOrders),
      }).unwrap();
      setSelectedOrders(new Set());
      refetch();
    } catch {
      // Error handled by RTK Query
    }
  };

  const filteredOrders = inboundOrders?.filter(
    (o) =>
      !searchCode ||
      o.orderCode.toLowerCase().includes(searchCode.toLowerCase())
  );

  return (
    <div className='space-y-6'>
      {/* Header */}
      <div className='flex items-center justify-between'>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>
            Phân loại đơn nhập bưu cục
          </h1>
          <p className='text-muted-foreground mt-1'>
            Quét và xác nhận các đơn đã đến bưu cục đích để chuẩn bị giao chặng
            cuối.
          </p>
        </div>
      </div>

      {/* Guide Banner */}
      <Card className='border-blue-200 bg-blue-50 dark:bg-blue-950/30 p-4'>
        <div className='flex gap-3'>
          <Info className='h-5 w-5 text-blue-600 flex-shrink-0 mt-0.5' />
          <div className='text-sm text-blue-800 dark:text-blue-200'>
            <p className='font-medium mb-1'>Cách phân loại đơn nhập:</p>
            <ol className='list-decimal list-inside space-y-1 text-blue-700 dark:text-blue-300'>
              <li>Chọn bưu cục và tải danh sách đơn đang chờ</li>
              <li>Quét mã vạch đơn hoặc chọn đơn trong danh sách bên dưới</li>
              <li>
                Bấm &quot;Xác nhận sẵn sàng giao&quot; để chuyển đơn sang trạng
                thái có thể phân công giao
              </li>
            </ol>
          </div>
        </div>
      </Card>

      {/* Search & Scan Section */}
      <Card className='p-4'>
        <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
          {/* Post Office */}
          <div className='space-y-2'>
            <label className='text-sm font-medium'>Bưu cục</label>
            <div className='flex gap-2'>
              <TmsCombobox
                id='inbound-post-office'
                value={postOfficeCode}
                onValueChange={handlePostOfficeChange}
                options={postOfficeOptions}
                placeholder='Chọn bưu cục'
                emptyText='Không tìm thấy bưu cục'
                disabled={isLoadingPostOffices}
                loading={isLoadingPostOffices}
                className='flex-1'
              />
              <Button onClick={handleSearch} disabled={!postOfficeCode}>
                <PackageSearch className='h-4 w-4 mr-2' />
                Tải
              </Button>
            </div>
          </div>

          {/* Scanner Input */}
          <div className='space-y-2'>
            <label className='text-sm font-medium'>Quét mã vạch đơn</label>
            <div className='flex gap-2'>
              <Input
                placeholder='Quét hoặc nhập mã đơn...'
                value={scanInput}
                onChange={(e) => setScanInput(e.target.value)}
                onKeyDown={handleScanKeyDown}
                autoFocus
              />
              <Button variant='outline' onClick={handleScanOrder}>
                <ScanBarcode className='h-4 w-4' />
              </Button>
            </div>
          </div>
        </div>
      </Card>

      {/* Orders List */}
      {postOfficeCode && (
        <Card className='p-4'>
          <div className='flex items-center justify-between mb-4'>
            <div className='flex items-center gap-2'>
              <h2 className='text-lg font-semibold'>Đơn đang chờ</h2>
              {filteredOrders && (
                <Badge variant='secondary'>{filteredOrders.length} đơn</Badge>
              )}
            </div>
            <div className='flex gap-2'>
              <Input
                placeholder='Lọc theo mã...'
                className='w-48'
                value={searchCode}
                onChange={(e) => setSearchCode(e.target.value)}
              />
              <Button variant='outline' size='sm' onClick={selectAll}>
                Chọn tất cả
              </Button>
            </div>
          </div>

          {isLoading ? (
            <div className='text-center py-8 text-muted-foreground'>
              Đang tải đơn...
            </div>
          ) : !filteredOrders?.length ? (
            <div className='text-center py-8 text-muted-foreground'>
              <PackageSearch className='h-12 w-12 mx-auto mb-3 opacity-50' />
              <p>Không tìm thấy đơn nhập bưu cục đang chờ.</p>
              <p className='text-sm mt-1'>
                Đơn sẽ hiển thị tại đây khi túi hàng đến bưu cục này.
              </p>
            </div>
          ) : (
            <div className='space-y-2 max-h-[500px] overflow-y-auto'>
              {filteredOrders.map((order) => (
                <InboundOrderCard
                  key={order.orderCode}
                  order={order}
                  selected={selectedOrders.has(order.orderCode)}
                  onToggle={() => toggleOrder(order.orderCode)}
                />
              ))}
            </div>
          )}
        </Card>
      )}

      {/* Confirm Action */}
      {selectedOrders.size > 0 && (
        <div className='sticky bottom-4 z-10'>
          <Card className='p-4 border-green-200 bg-green-50 dark:bg-green-950/30 shadow-lg'>
            <div className='flex items-center justify-between'>
              <div className='flex items-center gap-3'>
                <CheckCircle2 className='h-5 w-5 text-green-600' />
                <span className='font-medium'>
                  Đã chọn {selectedOrders.size} đơn
                </span>
              </div>
              <Button
                onClick={handleConfirmInbound}
                disabled={isConfirming}
                className='bg-green-600 hover:bg-green-700'
              >
                {isConfirming ? 'Đang xử lý...' : 'Xác nhận sẵn sàng giao'}
              </Button>
            </div>
          </Card>
        </div>
      )}
    </div>
  );
};

// ─── Sub-component ────────────────────────────────────────────────────────

interface InboundOrderCardProps {
  order: InboundOrderResponse;
  selected: boolean;
  onToggle: () => void;
}

const InboundOrderCard: React.FC<InboundOrderCardProps> = ({
  order,
  selected,
  onToggle,
}) => {
  return (
    <div
      onClick={onToggle}
      className={`flex items-center justify-between p-3 rounded-lg border cursor-pointer transition-colors ${
        selected
          ? 'border-green-300 bg-green-50 dark:bg-green-950/20'
          : 'border-border hover:bg-muted/50'
      }`}
    >
      <div className='flex items-center gap-3 min-w-0'>
        <input
          type='checkbox'
          checked={selected}
          onChange={onToggle}
          className='h-4 w-4 rounded border-gray-300'
        />
        <div className='min-w-0'>
          <div className='flex items-center gap-2'>
            <Package className='h-4 w-4 text-muted-foreground' />
            <span className='font-mono font-medium text-sm'>
              {order.orderCode}
            </span>
          </div>
          <div className='flex items-center gap-4 mt-1 text-xs text-muted-foreground'>
            <span className='flex items-center gap-1'>
              <MapPin className='h-3 w-3' />
              {order.receiverAddressDetail || 'Chưa có địa chỉ'}
            </span>
            <span className='flex items-center gap-1'>
              <Phone className='h-3 w-3' />
              {order.receiverPhone || '—'}
            </span>
          </div>
        </div>
      </div>

      <div className='flex items-center gap-3 flex-shrink-0'>
        {order.codAmount > 0 && (
          <Badge variant='outline' className='text-xs'>
            <Banknote className='h-3 w-3 mr-1' />
            COD: {order.codAmount.toLocaleString()}đ
          </Badge>
        )}
        <span className='text-xs text-muted-foreground'>
          {order.receiverName}
        </span>
      </div>
    </div>
  );
};
