import { DriverRouteDetailPage } from '@/modules/ttcrs/pages';

interface Props {
  params: Promise<{ id: string }>;
}

export default async function Page({ params }: Props) {
  const { id } = await params;
  return <DriverRouteDetailPage id={Number(id)} />;
}
