import { redirect } from 'next/navigation';

interface PageProps {
  params: Promise<{ tripId: string }>;
}

export default async function Page({ params }: PageProps) {
  const { tripId } = await params;
  redirect(`/school-bus/trips/${tripId}`);
}
