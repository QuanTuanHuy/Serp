/**
 * Inventory Item Detail Page
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - View inventory item details
 */

import { InventoryDetailPage } from '@/modules/sales';

interface PageProps {
  params: {
    itemId: string;
  };
}

export default function InventoryItemDetailPage({ params }: PageProps) {
  return <InventoryDetailPage itemId={params.itemId} />;
}
