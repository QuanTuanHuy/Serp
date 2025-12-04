// Analytics Components Exports (authors: QuanTuanHuy, Description: Part of Serp Project)

export {
  MetricsCard,
  TotalCustomersCard,
  ActiveLeadsCard,
  TotalRevenueCard,
  ConversionRateCard,
} from './MetricsCard';
export { PipelineChart } from './PipelineChart';
export { SalesChart } from './SalesChart';

// New Analytics Components
export { KPICard, type KPICardProps } from './KPICard';
export {
  PipelineFunnel as SalesPipelineFunnel,
  type FunnelStage,
  type PipelineFunnelProps as SalesPipelineFunnelProps,
} from './PipelineFunnel';
export {
  ActivityTimeline,
  type ActivityData,
  type ActivityTimelineProps,
} from './ActivityTimeline';
export {
  LeadSourceChart,
  type SourceData,
  type LeadSourceChartProps,
} from './LeadSourceChart';
export {
  RevenueTrendChart,
  type RevenueData,
  type RevenueTrendChartProps,
} from './RevenueTrendChart';
export {
  TopPerformers,
  type PerformerData,
  type TopPerformersProps,
} from './TopPerformers';

// Re-export types for convenience
export type {
  CRMMetrics,
  PipelineMetrics,
  SalesMetrics,
  LeadSourceMetrics,
} from '../../types';
