import { SchoolBusAttendanceDetailPage } from '@/modules/school-bus';

interface PageProps {
  params: Promise<{ tripId: string }>;
}

export default async function Page({ params }: PageProps) {
  const { tripId } = await params;
  return <SchoolBusAttendanceDetailPage tripId={Number(tripId)} />;
}
