/*
Author: QuanTuanHuy
Description: Part of Serp Project - Edit Order Route Page
*/

import { EditOrderPage } from '@/modules/sales/pages/orders';

export default function EditOrderRoute({
  params,
}: {
  params: { orderId: string };
}) {
  return <EditOrderPage orderId={params.orderId} />;
}
