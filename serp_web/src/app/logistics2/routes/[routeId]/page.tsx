import RouteDetailPage from '@/modules/logistics2/pages/routes/RouteDetailPage';

export default async function Logistics2RouteDetailPage({
  params,
}: {
  params: Promise<{ routeId: string }>;
}) {
  const { routeId } = await params;

  return <RouteDetailPage routeId={routeId} />;
}
