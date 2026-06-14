import { InventoryDetailPage } from '@/modules/logistics'; //[cite: 2]

export default async function InventoryItemDetailPage({
  params,
}: {
  params: Promise<{ itemId: string }>;
}) {
  const { itemId } = await params;
  return <InventoryDetailPage itemId={itemId} />;
}
