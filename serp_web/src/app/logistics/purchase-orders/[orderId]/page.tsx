import { OrderDetailPage } from '@/modules/logistics/pages/purchase-orders';

export default async function PurchaseOrderDetailRoute({
  params,
}: {
  params: Promise<{ orderId: string }>;
}) {
  const { orderId } = await params;
  return <OrderDetailPage orderId={orderId} />;
}
