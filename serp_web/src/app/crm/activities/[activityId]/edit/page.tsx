/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

import { EditActivityPage } from '@/modules/crm/pages';

interface Props {
  params: Promise<{
    activityId: string;
  }>;
}

export default async function Page({ params }: Props) {
  const { activityId } = await params;
  return <EditActivityPage activityId={activityId} />;
}
