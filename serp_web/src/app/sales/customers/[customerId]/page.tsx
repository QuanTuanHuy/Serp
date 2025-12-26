/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

import { CustomerDetailPage } from '@/modules/sales/pages';

interface Props {
  params: Promise<{
    customerId: string;
  }>;
}

export default async function Page({ params }: Props) {
  const { customerId } = await params;
  return <CustomerDetailPage customerId={customerId} />;
}
