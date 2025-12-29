/*
Author: QuanTuanHuy
Description: Part of Serp Project - Create Order Route Page
*/

import { CreateOrderPage } from '@/modules/sales/pages/orders';

export default async function CreateOrderRoute({
  params,
}: {
  params: Promise<{ customerId?: string }>;
}) {
  const { customerId } = await params;
  return <CreateOrderPage customerId={customerId} />;
}
