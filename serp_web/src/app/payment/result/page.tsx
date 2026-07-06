'use client';

import React from 'react';
import { useSearchParams } from 'next/navigation';
import { Button } from '@/shared/components/ui';

const PAYMENT_RESULT_MESSAGE_TYPE = 'SERP_PAYMENT_RESULT';

const resolvePaymentStatusLabel = (query: Record<string, string>): string => {
  const rawStatus =
    query.status ??
    query.return_code ??
    query.isSuccess ??
    query.is_success ??
    '';
  const normalizedStatus = rawStatus.trim().toLowerCase();

  if (
    normalizedStatus === '1' ||
    normalizedStatus === 'success' ||
    normalizedStatus === 'succeeded' ||
    normalizedStatus === 'true'
  ) {
    return 'Thanh toán thành công';
  }

  if (
    normalizedStatus === '0' ||
    normalizedStatus === '-1' ||
    normalizedStatus === 'failed' ||
    normalizedStatus === 'false'
  ) {
    return 'Thanh toán chưa hoàn tất';
  }

  return 'Đã nhận kết quả thanh toán';
};

export default function PaymentResultPage() {
  const searchParams = useSearchParams();
  const hasSentMessageRef = React.useRef(false);

  const query = React.useMemo(() => {
    const nextQuery: Record<string, string> = {};
    for (const [key, value] of searchParams.entries()) {
      nextQuery[key] = value;
    }
    return nextQuery;
  }, [searchParams]);

  const orderId = React.useMemo(() => {
    const value = Number.parseInt(query.orderId ?? '', 10);
    if (Number.isInteger(value) && value > 0) {
      return value;
    }
    return undefined;
  }, [query.orderId]);

  const manifestId = React.useMemo(() => {
    const value = Number.parseInt(query.manifestId ?? '', 10);
    if (Number.isInteger(value) && value > 0) {
      return value;
    }
    return undefined;
  }, [query.manifestId]);

  const orderCode = React.useMemo(() => {
    const value = (query.orderCode ?? '').trim();
    return value || undefined;
  }, [query.orderCode]);

  const appTransId = React.useMemo(() => {
    const value = (
      query.appTransId ??
      query.apptransid ??
      query.app_trans_id ??
      ''
    ).trim();
    return value || undefined;
  }, [query.appTransId, query.apptransid, query.app_trans_id]);

  React.useEffect(() => {
    if (hasSentMessageRef.current) {
      return;
    }

    if (!window.opener || window.opener.closed) {
      return;
    }

    hasSentMessageRef.current = true;
    window.opener.postMessage(
      {
        type: PAYMENT_RESULT_MESSAGE_TYPE,
        payload: {
          orderId,
          manifestId,
          orderCode,
          appTransId,
          query,
        },
      },
      window.location.origin
    );
  }, [orderId, manifestId, orderCode, appTransId, query]);

  return (
    <main className='flex min-h-screen items-center justify-center bg-background px-4'>
      <section className='w-full max-w-md rounded-lg border bg-card p-6 shadow-sm'>
        <h1 className='text-lg font-semibold'>Kết quả thanh toán</h1>
        <p className='mt-2 text-sm text-muted-foreground'>
          {resolvePaymentStatusLabel(query)}. Bạn có thể đóng cửa sổ này và tiếp
          tục trong hộp thoại xác nhận thanh toán.
        </p>

        {appTransId ? (
          <p className='mt-3 text-xs text-muted-foreground'>
            Mã giao dịch: <span className='font-medium'>{appTransId}</span>
          </p>
        ) : null}

        <div className='mt-5 flex gap-2'>
          <Button type='button' onClick={() => window.close()}>
            Đóng cửa sổ
          </Button>
          <Button
            type='button'
            variant='outline'
            onClick={() =>
              window.location.assign(
                query.source === 'first-mile'
                  ? '/first-mile/pickup-and-delivery/delivery'
                  : '/first-mile/orders/list'
              )
            }
          >
            Đi tới TMS
          </Button>
        </div>
      </section>
    </main>
  );
}
