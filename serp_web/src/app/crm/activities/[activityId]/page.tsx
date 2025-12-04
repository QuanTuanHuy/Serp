/**
 * CRM Activity Detail Page Route
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

import { ActivityDetailPage } from '@/modules/crm/pages/activities';

interface ActivityDetailRouteProps {
  params: Promise<{
    activityId: string;
  }>;
}

export default async function ActivityDetailRoute({
  params,
}: ActivityDetailRouteProps) {
  const { activityId } = await params;
  return <ActivityDetailPage activityId={activityId} />;
}
