/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

import { AccountDetailPage } from '@/modules/crm/pages';

interface Props {
  params: Promise<{
    accountId: string;
  }>;
}

export default async function Page({ params }: Props) {
  const { accountId } = await params;
  return <AccountDetailPage customerId={accountId} />;
}
