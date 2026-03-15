/**
 * Edit Facility Page
 * @author QuanTuanHuy
 * @description Part of Serp Project - Edit facility route
 */

'use client';

import { useRouter, useParams } from 'next/navigation';
import { EditFacilityPage } from '@/modules/sales/pages';

export default function EditFacilityRoute() {
  const router = useRouter();
  const params = useParams();
  const facilityId = params.facilityId as string;

  return (
    <EditFacilityPage
      facilityId={facilityId}
      onSuccess={() => router.push(`/sales/facility/${facilityId}`)}
      onCancel={() => router.back()}
    />
  );
}
