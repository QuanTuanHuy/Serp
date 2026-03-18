/**
 * Edit Facility Page
 * @author QuanTuanHuy
 * @description Part of Serp Project - Edit facility route
 */

'use client';

import { useRouter, useParams } from 'next/navigation';
import { EditFacilityPage } from '@/modules/purchase/pages';

export default function EditFacilityRoute() {
  const router = useRouter();
  const params = useParams();
  const facilityId = params.facilityId as string;

  return (
    <EditFacilityPage
      facilityId={facilityId}
      onSuccess={() => router.push(`/purchase/facilities/${facilityId}`)}
      onCancel={() => router.back()}
    />
  );
}
