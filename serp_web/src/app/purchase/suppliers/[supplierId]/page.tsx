// Purchase Supplier Detail Route Page (authors: QuanTuanHuy, Description: Part of Serp Project)

import { SupplierDetailPage } from '@/modules/purchase/pages';

interface Props {
  params: Promise<{
    supplierId: string;
  }>;
}

export default async function SupplierDetailRoute({ params }: Props) {
  const { supplierId } = await params;
  return <SupplierDetailPage supplierId={supplierId} />;
}
