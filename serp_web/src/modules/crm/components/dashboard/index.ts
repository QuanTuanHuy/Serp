/**
 * Dashboard Components Barrel Exports
 * @author QuanTuanHuy
 * @description Part of Serp Project - CRM Dashboard components
 */

export {
  StatsCard,
  TotalCustomersStats,
  ActiveLeadsStats,
  RevenueStats,
  ConversionRateStats,
} from './StatsCard';
export type { StatsCardProps } from './StatsCard';

export { PipelineFunnel, DEFAULT_PIPELINE_STAGES } from './PipelineFunnel';
export type { PipelineStage, PipelineFunnelProps } from './PipelineFunnel';

export { SalesTrendChart, DEFAULT_SALES_DATA } from './SalesTrendChart';
export type { SalesDataPoint, SalesTrendChartProps } from './SalesTrendChart';

export { RecentActivity, DEFAULT_ACTIVITIES } from './RecentActivity';
export type { ActivityItem, RecentActivityProps } from './RecentActivity';

export { QuickActions } from './QuickActions';
export type { QuickActionItem, QuickActionsProps } from './QuickActions';
