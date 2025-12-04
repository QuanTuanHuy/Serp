'use client';

/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - CRM Analytics Dashboard Page
 */

import React, { useState } from 'react';
import {
  KPICard,
  SalesPipelineFunnel,
  ActivityTimeline,
  LeadSourceChart,
  RevenueTrendChart,
  TopPerformers,
  type FunnelStage,
  type ActivityData,
  type SourceData,
  type RevenueData,
  type PerformerData,
} from '../../components/analytics';
import { Button } from '@/shared/components/ui/button';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import {
  Users,
  Target,
  DollarSign,
  TrendingUp,
  Calendar,
  RefreshCw,
  Download,
  Filter,
} from 'lucide-react';

// Mock data for analytics
const MOCK_PIPELINE_STAGES: FunnelStage[] = [
  {
    name: 'Tiềm năng',
    count: 45,
    value: 4500000000,
    color: '#3B82F6',
    conversionRate: 100,
  },
  {
    name: 'Đã liên hệ',
    count: 38,
    value: 3800000000,
    color: '#8B5CF6',
    conversionRate: 84,
  },
  {
    name: 'Trình bày',
    count: 25,
    value: 2500000000,
    color: '#EC4899',
    conversionRate: 66,
  },
  {
    name: 'Đề xuất',
    count: 18,
    value: 1800000000,
    color: '#F97316',
    conversionRate: 72,
  },
  {
    name: 'Thương lượng',
    count: 12,
    value: 1200000000,
    color: '#EAB308',
    conversionRate: 67,
  },
  {
    name: 'Chốt đơn',
    count: 8,
    value: 800000000,
    color: '#22C55E',
    conversionRate: 67,
  },
];

const MOCK_ACTIVITY_DATA: ActivityData[] = [
  { date: '2024-01-08', calls: 12, emails: 25, meetings: 3, tasks: 8 },
  { date: '2024-01-09', calls: 15, emails: 20, meetings: 5, tasks: 12 },
  { date: '2024-01-10', calls: 8, emails: 30, meetings: 2, tasks: 6 },
  { date: '2024-01-11', calls: 20, emails: 18, meetings: 4, tasks: 10 },
  { date: '2024-01-12', calls: 10, emails: 22, meetings: 6, tasks: 15 },
  { date: '2024-01-13', calls: 5, emails: 8, meetings: 1, tasks: 3 },
  { date: '2024-01-14', calls: 3, emails: 5, meetings: 0, tasks: 2 },
];

const MOCK_LEAD_SOURCES: SourceData[] = [
  {
    source: 'Website',
    count: 120,
    value: 12000000000,
    conversionRate: 25,
    color: '#3B82F6',
  },
  {
    source: 'Giới thiệu',
    count: 85,
    value: 8500000000,
    conversionRate: 40,
    color: '#22C55E',
  },
  {
    source: 'Quảng cáo',
    count: 65,
    value: 6500000000,
    conversionRate: 18,
    color: '#F97316',
  },
  {
    source: 'Triển lãm',
    count: 45,
    value: 4500000000,
    conversionRate: 35,
    color: '#8B5CF6',
  },
  {
    source: 'Email marketing',
    count: 35,
    value: 3500000000,
    conversionRate: 15,
    color: '#EC4899',
  },
  {
    source: 'Mạng xã hội',
    count: 30,
    value: 3000000000,
    conversionRate: 12,
    color: '#EAB308',
  },
];

const MOCK_REVENUE_DATA: RevenueData[] = [
  { month: 'Tháng 7', revenue: 2500000000, target: 3000000000, deals: 12 },
  { month: 'Tháng 8', revenue: 3200000000, target: 3000000000, deals: 15 },
  { month: 'Tháng 9', revenue: 2800000000, target: 3200000000, deals: 13 },
  { month: 'Tháng 10', revenue: 3500000000, target: 3200000000, deals: 18 },
  { month: 'Tháng 11', revenue: 4200000000, target: 3500000000, deals: 22 },
  { month: 'Tháng 12', revenue: 3800000000, target: 4000000000, deals: 20 },
];

const MOCK_PERFORMERS: PerformerData[] = [
  {
    id: '1',
    name: 'Nguyễn Văn An',
    deals: 25,
    revenue: 2500000000,
    target: 2000000000,
    trend: 15,
  },
  {
    id: '2',
    name: 'Trần Thị Bình',
    deals: 22,
    revenue: 2200000000,
    target: 2000000000,
    trend: 8,
  },
  {
    id: '3',
    name: 'Lê Hoàng Cường',
    deals: 18,
    revenue: 1800000000,
    target: 2000000000,
    trend: -5,
  },
  {
    id: '4',
    name: 'Phạm Minh Dũng',
    deals: 15,
    revenue: 1500000000,
    target: 1800000000,
    trend: 12,
  },
  {
    id: '5',
    name: 'Hoàng Thu Hà',
    deals: 12,
    revenue: 1200000000,
    target: 1500000000,
    trend: 0,
  },
];

type TimeRange = '7d' | '30d' | '90d' | '12m' | 'ytd';

export default function CRMAnalyticsPage() {
  const [timeRange, setTimeRange] = useState<TimeRange>('30d');
  const [isRefreshing, setIsRefreshing] = useState(false);

  const handleRefresh = () => {
    setIsRefreshing(true);
    // Simulate refresh
    setTimeout(() => {
      setIsRefreshing(false);
    }, 1000);
  };

  const timeRangeLabels: Record<TimeRange, string> = {
    '7d': '7 ngày',
    '30d': '30 ngày',
    '90d': '90 ngày',
    '12m': '12 tháng',
    ytd: 'Năm nay',
  };

  return (
    <div className='space-y-6'>
      {/* Page Header */}
      <div className='flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4'>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>Phân tích CRM</h1>
          <p className='text-muted-foreground'>
            Tổng quan hiệu suất và chỉ số kinh doanh
          </p>
        </div>
        <div className='flex items-center gap-2'>
          <Select
            value={timeRange}
            onValueChange={(v) => setTimeRange(v as TimeRange)}
          >
            <SelectTrigger className='w-32'>
              <Calendar className='h-4 w-4 mr-2' />
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {Object.entries(timeRangeLabels).map(([value, label]) => (
                <SelectItem key={value} value={value}>
                  {label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Button variant='outline' size='sm' className='gap-2'>
            <Filter className='h-4 w-4' />
            Lọc
          </Button>
          <Button
            variant='outline'
            size='sm'
            onClick={handleRefresh}
            disabled={isRefreshing}
            className='gap-2'
          >
            <RefreshCw
              className={`h-4 w-4 ${isRefreshing ? 'animate-spin' : ''}`}
            />
            Làm mới
          </Button>
          <Button variant='outline' size='sm' className='gap-2'>
            <Download className='h-4 w-4' />
            Xuất báo cáo
          </Button>
        </div>
      </div>
      {/* KPI Cards */}
      <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6'>
        <KPICard
          title='Tổng khách hàng'
          value='1,234'
          subtitle='+45 khách mới trong tháng'
          trend={{ value: 12, label: 'vs tháng trước' }}
          icon={Users}
          iconColor='text-blue-600'
        />
        <KPICard
          title='Leads hoạt động'
          value='380'
          subtitle='85 cần theo dõi'
          trend={{ value: 8, label: 'vs tháng trước' }}
          icon={Target}
          iconColor='text-purple-600'
        />
        <KPICard
          title='Doanh thu tháng'
          value='3.8 tỷ'
          subtitle='Mục tiêu: 4 tỷ (95%)'
          trend={{ value: -5, label: 'vs tháng trước' }}
          icon={DollarSign}
          iconColor='text-green-600'
        />
        <KPICard
          title='Tỉ lệ chuyển đổi'
          value='24.5%'
          subtitle='Từ lead → deal'
          trend={{ value: 3.2, label: 'vs tháng trước' }}
          icon={TrendingUp}
          iconColor='text-orange-600'
        />
      </div>

      {/* Main Charts Row */}
      <div className='grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6'>
        <RevenueTrendChart
          data={MOCK_REVENUE_DATA}
          title='Xu hướng doanh thu'
          description='6 tháng gần đây'
        />
        <SalesPipelineFunnel
          stages={MOCK_PIPELINE_STAGES}
          title='Pipeline bán hàng'
          description='Phân bố cơ hội theo giai đoạn'
        />
      </div>

      {/* Secondary Charts Row */}
      <div className='grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6'>
        <LeadSourceChart
          data={MOCK_LEAD_SOURCES}
          title='Nguồn Lead'
          description='Phân tích hiệu quả các kênh'
        />
        <ActivityTimeline
          data={MOCK_ACTIVITY_DATA}
          title='Hoạt động'
          description='7 ngày gần đây'
        />
        <TopPerformers
          data={MOCK_PERFORMERS}
          title='Bảng xếp hạng'
          description='Top nhân viên tháng này'
        />
      </div>

      {/* Additional Insights */}
      <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4'>
        <KPICard
          title='Thời gian chốt TB'
          value='18 ngày'
          subtitle='Nhanh hơn 3 ngày'
          trend={{ value: -15 }}
          iconColor='text-cyan-600'
        />
        <KPICard
          title='Giá trị deal TB'
          value='185 tr'
          subtitle='Tăng từ 165 tr'
          trend={{ value: 12 }}
          iconColor='text-emerald-600'
        />
        <KPICard
          title='Tỉ lệ thắng'
          value='32%'
          subtitle='Cải thiện 5% vs Q3'
          trend={{ value: 5 }}
          iconColor='text-violet-600'
        />
        <KPICard
          title='Hoạt động/ngày'
          value='47'
          subtitle='Email, call, meeting'
          trend={{ value: 8 }}
          iconColor='text-rose-600'
        />
      </div>
    </div>
  );
}
