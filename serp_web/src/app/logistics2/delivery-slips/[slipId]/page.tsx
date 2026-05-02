/*
Author: QuanTuanHuy
Description: Part of Serp Project - Delivery slip detail route
*/

import { DeliverySlipDetailPage } from '@/modules/logistics2';

export default async function Logistics2DeliverySlipDetailRoute({
  params,
}: {
  params: Promise<{ slipId: string }>;
}) {
  const { slipId } = await params;

  return <DeliverySlipDetailPage slipId={slipId} />;
}
