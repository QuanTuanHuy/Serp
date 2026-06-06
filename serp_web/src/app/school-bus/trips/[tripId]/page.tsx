import { SchoolBusTripOperationDetailPage } from '@/modules/school-bus';

interface PageProps {
  params: Promise<{ tripId: string }>;
}

export default async function Page({ params }: PageProps) {
  const { tripId } = await params;
  return <SchoolBusTripOperationDetailPage tripId={Number(tripId)} />;
}
