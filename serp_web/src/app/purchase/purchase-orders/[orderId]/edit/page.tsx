/*
Author: QuanTuanHuy
Description: Part of Serp Project - Edit Purchase Order Route Page
*/

import { EditOrderPage } from '@/modules/purchase/pages/purchase-orders';

export default function EditPurchaseOrderRoute({
  params,
}: {
  params: { orderId: string };
}) {
  return <EditOrderPage orderId={params.orderId} />;
}
