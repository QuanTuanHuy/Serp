/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

import { FacilityDetailPage } from '@/modules/purchase/pages';

interface Props {
  params: Promise<{
    facilityId: string;
  }>;
}

export default async function Page({ params }: Props) {
  const { facilityId } = await params;
  return <FacilityDetailPage facilityId={facilityId} />;
}
