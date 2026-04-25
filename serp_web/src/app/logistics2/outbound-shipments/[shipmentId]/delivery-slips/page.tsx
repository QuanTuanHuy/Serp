import { OutboundShipmentDetailPage } from '@/modules/logistics2';

export default async function Logistics2OutboundShipmentDeliverySlipsRoute({
  params,
}: {
  params: Promise<{ shipmentId: string }>;
}) {
  const { shipmentId } = await params;

  return (
    <OutboundShipmentDetailPage
      shipmentId={shipmentId}
      initialTab='delivery-slips'
    />
  );
}
