/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

import { CustomerDetailPageEnhanced } from '@/modules/crm/pages';

interface Props {
  params: Promise<{
    customerId: string;
  }>;
}

export default async function Page({ params }: Props) {
  const { customerId } = await params;
  return <CustomerDetailPageEnhanced customerId={customerId} />;
}
