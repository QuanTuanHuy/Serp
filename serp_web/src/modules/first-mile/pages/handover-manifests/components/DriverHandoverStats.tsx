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
      <MetricItem label='Ready' value={readyCount} />
      <MetricItem label='In transit' value={inTransitCount} />
      <MetricItem label='Arrived' value={arrivedCount} />
      <MetricItem label='Completed' value={completedCount} />
    </div>
  );
}
