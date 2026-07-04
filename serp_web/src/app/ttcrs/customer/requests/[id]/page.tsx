import { CustomerRequestDetailPage } from '@/modules/ttcrs/pages/customer/CustomerRequestDetailPage';

interface Props {
  params: Promise<{ id: string }>;
}

export default async function CustomerRequestDetailRoutePage({ params }: Props) {
  const { id } = await params;
  return <CustomerRequestDetailPage requestId={Number(id)} />;
}
