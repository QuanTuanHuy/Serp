/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Driver handover status metrics
 */

import { MetricItem } from './MetricItem';

interface DriverHandoverStatsProps {
  arrivedCount: number;
  completedCount: number;
  inTransitCount: number;
  readyCount: number;
}

export function DriverHandoverStats({
  arrivedCount,
  completedCount,
  inTransitCount,
  readyCount,
}: DriverHandoverStatsProps) {
  return (
    <div className='grid gap-4 md:grid-cols-4'>
      <MetricItem label='Sẵn sàng' value={readyCount} />
      <MetricItem label='Đang vận chuyển' value={inTransitCount} />
      <MetricItem label='Đã đến' value={arrivedCount} />
      <MetricItem label='Hoàn tất' value={completedCount} />
    </div>
  );
}
