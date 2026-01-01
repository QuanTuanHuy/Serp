/*
Author: QuanTuanHuy
Description: Part of Serp Project - Logistics Inventory Detail Route
*/

import { InventoryDetailPage } from '@/modules/logistics';

export default function InventoryItemDetailPage({
  params,
}: {
  params: { itemId: string };
}) {
  return <InventoryDetailPage itemId={params.itemId} />;
}
