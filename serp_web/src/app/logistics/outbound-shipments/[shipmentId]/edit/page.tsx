import { EditOutboundShipmentPage } from '@/modules/logistics/pages/outbound-shipments';

export default async function EditOutboundShipmentRoute({
  params,
}: {
  params: Promise<{ shipmentId: string }>;
}) {
  const { shipmentId } = await params;
  return <EditOutboundShipmentPage shipmentId={shipmentId} />;
}
