/*
Author: QuanTuanHuy
Description: Part of Serp Project - Create delivery slip route from outbound shipment
*/

import { CreateDeliverySlipPage } from '@/modules/logistics2';

export default async function Logistics2CreateDeliverySlipRoute({
  params,
}: {
  params: Promise<{ shipmentId: string }>;
}) {
  const { shipmentId } = await params;

  return <CreateDeliverySlipPage shipmentId={shipmentId} />;
}
