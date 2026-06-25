import { RequestDetailPage } from '@/modules/ttcrs/pages/dispatcher/RequestDetailPage';

interface Props {
  params: Promise<{ id: string }>;
}

export default async function DispatcherRequestDetailPage({ params }: Props) {
  const { id } = await params;
  return <RequestDetailPage requestId={Number(id)} />;
}
