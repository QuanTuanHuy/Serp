'use client';

import { useRouter, useParams } from 'next/navigation';
import { EditFacilityPage } from '@/modules/logistics/pages/facilities';

export default function EditFacilityRoute() {
  const router = useRouter();
  const params = useParams();
  const facilityId = params.facilityId as string;
  return (
    <EditFacilityPage
      facilityId={facilityId}
      onSuccess={() => router.push(`/logistics/facility/${facilityId}`)}
      onCancel={() => router.back()}
    />
  );
}
