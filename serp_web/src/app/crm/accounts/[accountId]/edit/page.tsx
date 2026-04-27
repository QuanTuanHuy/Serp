/**
 * Edit Account Page
 * @author QuanTuanHuy
 * @description Part of Serp Project - Edit account route
 */

'use client';

import { useRouter, useParams } from 'next/navigation';
import { EditAccountPage } from '@/modules/crm/pages';

export default function EditCustomerRoute() {
  const router = useRouter();
  const params = useParams();
  const accountId = params.accountId as string;

  return (
    <EditAccountPage
      customerId={accountId}
      onSuccess={() => router.push(`/crm/accounts/${accountId}`)}
      onCancel={() => router.back()}
    />
  );
}
