/**
 * Purchase Order Detail Route
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Purchase order detail page
 */

import { OrderDetailPage } from '@/modules/purchase/pages/purchase-orders';

export default async function PurchaseOrderDetailRoute({
  params,
}: {
  params: Promise<{ orderId: string }>;
}) {
  const { orderId } = await params;
  return <OrderDetailPage orderId={orderId} />;
}
