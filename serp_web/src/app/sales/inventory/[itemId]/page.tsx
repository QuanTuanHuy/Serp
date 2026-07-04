import { InventoryDetailPage } from '@/modules/sales';

interface PageProps {
  params: Promise<{
    itemId: string;
  }>;
}

export default async function InventoryItemDetailPage({ params }: PageProps) {
  const resolvedParams = await params;

  return <InventoryDetailPage itemId={resolvedParams.itemId} />;
}
