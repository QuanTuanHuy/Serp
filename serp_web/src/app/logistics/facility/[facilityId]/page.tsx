import { FacilityDetailPage } from '@/modules/logistics/pages/facilities';

interface Props {
  params: Promise<{ facilityId: string }>;
}

export default async function Page({ params }: Props) {
  const { facilityId } = await params;
  return <FacilityDetailPage facilityId={facilityId} />;
}
