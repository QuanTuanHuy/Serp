/*
Author: QuanTuanHuy
Description: Part of Serp Project - Order Detail Route Page
*/

import { OrderDetailPage } from '@/modules/sales/pages/orders';

export default function OrderDetailRoute({
  params,
}: {
  params: { orderId: string };
}) {
  return <OrderDetailPage orderId={params.orderId} />;
}
