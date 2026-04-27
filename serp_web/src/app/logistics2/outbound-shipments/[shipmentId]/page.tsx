import { OutboundShipmentDetailPage } from '@/modules/logistics2';

export default async function Logistics2OutboundShipmentDetailRoute({
  params,
}: {
  params: Promise<{ shipmentId: string }>;
}) {
  const { shipmentId } = await params;

  return <OutboundShipmentDetailPage shipmentId={shipmentId} />;
}
