/**
 * Edit Customer Page
 * @author QuanTuanHuy
 * @description Part of Serp Project - Edit customer route
 */

'use client';

import { useRouter, useParams } from 'next/navigation';
import { EditCustomerPage } from '@/modules/sales/pages';

export default function EditCustomerRoute() {
  const router = useRouter();
  const params = useParams();
  const customerId = params.customerId as string;

  return (
    <EditCustomerPage
      customerId={customerId}
      onSuccess={() => router.push(`/sales/customers/${customerId}`)}
      onCancel={() => router.back()}
    />
  );
}
