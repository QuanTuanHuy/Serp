// Purchase Edit Supplier Route Page (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { useRouter, useParams } from 'next/navigation';
import { EditSupplierPage } from '@/modules/purchase/pages';

export default function EditSupplierRoute() {
  const router = useRouter();
  const params = useParams();
  const supplierId = params.supplierId as string;

  return (
    <EditSupplierPage
      supplierId={supplierId}
      onSuccess={() => router.push(`/purchase/suppliers/${supplierId}`)}
      onCancel={() => router.back()}
    />
  );
}
