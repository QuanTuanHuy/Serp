import { OutboundShipmentDetailPage } from '@/modules/logistics/pages/outbound-shipments';

export default async function OutboundShipmentDetailRoute({
  params,
}: {
  params: Promise<{ shipmentId: string }>;
}) {
  const { shipmentId } = await params;
  return <OutboundShipmentDetailPage shipmentId={shipmentId} />;
}
