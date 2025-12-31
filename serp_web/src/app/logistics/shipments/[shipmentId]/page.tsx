import { ShipmentDetailPage } from '@/modules/logistics/pages/shipments';

export default async function ShipmentDetailRoute({
  params,
}: {
  params: Promise<{ shipmentId: string }>;
}) {
  const { shipmentId } = await params;
  return <ShipmentDetailPage shipmentId={shipmentId} />;
}
