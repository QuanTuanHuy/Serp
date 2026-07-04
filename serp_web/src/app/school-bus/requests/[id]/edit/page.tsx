import { SchoolBusRequestFormPage } from '@/modules/school-bus';

export default async function Page({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;

  return <SchoolBusRequestFormPage requestId={Number(id)} />;
}
