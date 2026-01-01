/*
Author: QuanTuanHuy
Description: Part of Serp Project - Order Detail Route Page
*/

import { OrderDetailPage } from '@/modules/sales/pages/orders';

export default async function OrderDetailRoute({
  params,
}: {
  params: Promise<{ orderId: string }>;
}) {
  const { orderId } = await params;
  return <OrderDetailPage orderId={orderId} />;
}
