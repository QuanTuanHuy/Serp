/**
 * Shipment Detail Route Page - Purchase Module
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project
 */

import { ShipmentDetailPage } from '@/modules/purchase';

interface PageProps {
  params: Promise<{ shipmentId: string }>;
}

export default async function Page({ params }: PageProps) {
  const { shipmentId } = await params;
  return <ShipmentDetailPage shipmentId={shipmentId} />;
}
