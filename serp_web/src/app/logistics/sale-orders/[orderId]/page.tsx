import { OrderDetailPage } from '@/modules/logistics/pages/sale-orders';

export default async function SaleOrderDetailRoute({
  params,
}: {
  params: Promise<{ orderId: string }>;
}) {
  const { orderId } = await params;
  return <OrderDetailPage orderId={orderId} />;
}
