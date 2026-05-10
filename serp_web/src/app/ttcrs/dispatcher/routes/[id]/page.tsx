import { TransportRouteDetailPage } from '@/modules/ttcrs/pages';

interface Props {
  params: Promise<{ id: string }>;
}

export default async function RouteDetailPage({ params }: Props) {
  const { id } = await params;
  return <TransportRouteDetailPage planId={Number(id)} />;
}
