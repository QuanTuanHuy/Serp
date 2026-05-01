/*
Author: QuanTuanHuy
Description: Part of Serp Project - Delivery plan detail route
*/

import { DeliveryPlanDetailPage } from '@/modules/logistics2';

export default async function Logistics2DeliveryPlanDetailRoute({
  params,
}: {
  params: Promise<{ planId: string }>;
}) {
  const { planId } = await params;

  return <DeliveryPlanDetailPage planId={planId} />;
}
