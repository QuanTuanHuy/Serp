// MetricsCard Component (authors: QuanTuanHuy, Description: Part of Serp Project)

import { Card, CardContent, CardHeader } from '@/shared/components/ui';
import { cn, formatCurrency } from '@/shared/utils';

interface MetricsCardProps {
  title: string;
  value: string | number;
  description?: string;
  icon?: React.ReactNode;
  trend?: {
    value: number;
    isPositive: boolean;
    period: string;
  };
  isLoading?: boolean;
  className?: string;
  variant?: 'default' | 'success' | 'warning' | 'danger';
}

const variantStyles = {
  default: 'border-border',
  success: 'border-green-200 bg-green-50',
  warning: 'border-yellow-200 bg-yellow-50',
  danger: 'border-red-200 bg-red-50',
};

const formatValue = (value: string | number): string => {
  if (typeof value === 'number') {
    // Format large numbers
    if (value >= 1000000) {
      return `${(value / 1000000).toFixed(1)}M`;
    } else if (value >= 1000) {
      return `${(value / 1000).toFixed(1)}K`;
    } else {
      return value.toString();
    }
  }
  return value;
};

export const MetricsCard: React.FC<MetricsCardProps> = ({
  title,
  value,
  description,
  icon,
  trend,
  isLoading = false,
  className,
  variant = 'default',
}) => {
  if (isLoading) {
    return (
      <Card className={cn('animate-pulse', variantStyles[variant], className)}>
        <CardHeader className='pb-2'>
          <div className='flex items-center justify-between'>
            <div className='h-4 bg-muted rounded w-24'></div>
            <div className='h-6 w-6 bg-muted rounded'></div>
          </div>
        </CardHeader>
        <CardContent>
          <div className='h-8 bg-muted rounded w-32 mb-2'></div>
          <div className='h-3 bg-muted rounded w-40'></div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card
      className={cn(
        'transition-all hover:shadow-md',
        variantStyles[variant],
        className
      )}
    >
      <CardHeader className='pb-2'>
        <div className='flex items-center justify-between'>
          <h3 className='text-sm font-medium text-muted-foreground'>{title}</h3>
          {icon && (
            <div className='flex items-center justify-center h-8 w-8 rounded-full bg-muted'>
              {icon}
            </div>
          )}
        </div>
      </CardHeader>
      <CardContent>
        <div className='flex items-baseline justify-between'>
          <p className='text-2xl font-bold'>{formatValue(value)}</p>
          {trend && (
            <div
              className={cn(
                'flex items-center text-sm font-medium',
                trend.isPositive ? 'text-green-600' : 'text-red-600'
              )}
            >
              <span className='mr-1'>{trend.isPositive ? '↗' : '↘'}</span>
              {Math.abs(trend.value)}%
            </div>
          )}
        </div>

        {description && (
          <p className='text-xs text-muted-foreground mt-1'>{description}</p>
        )}

        {trend && (
          <p className='text-xs text-muted-foreground mt-1'>
            vs {trend.period}
          </p>
        )}
      </CardContent>
    </Card>
  );
};

// Predefined metric cards for common CRM metrics
export const TotalCustomersCard: React.FC<{
  value: number;
  trend?: MetricsCardProps['trend'];
  isLoading?: boolean;
}> = ({ value, trend, isLoading }) => (
  <MetricsCard
    title='Total Customers'
    value={value}
    description='Active customers in system'
    icon={
      <svg
        className='h-4 w-4 text-blue-600'
        fill='none'
        stroke='currentColor'
        viewBox='0 0 24 24'
      >
        <path
          strokeLinecap='round'
          strokeLinejoin='round'
          strokeWidth='2'
          d='M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z'
        />
      </svg>
    }
    trend={trend}
    isLoading={isLoading}
    variant='default'
  />
);

export const ActiveLeadsCard: React.FC<{
  value: number;
  trend?: MetricsCardProps['trend'];
  isLoading?: boolean;
}> = ({ value, trend, isLoading }) => (
  <MetricsCard
    title='Active Leads'
    value={value}
    description='Leads in pipeline'
    icon={
      <svg
        className='h-4 w-4 text-green-600'
        fill='none'
        stroke='currentColor'
        viewBox='0 0 24 24'
      >
        <path
          strokeLinecap='round'
          strokeLinejoin='round'
          strokeWidth='2'
          d='M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z'
        />
      </svg>
    }
    trend={trend}
    isLoading={isLoading}
    variant='success'
  />
);

export const TotalRevenueCard: React.FC<{
  value: number;
  trend?: MetricsCardProps['trend'];
  isLoading?: boolean;
}> = ({ value, trend, isLoading }) => (
  <MetricsCard
    title='Total Revenue'
    value={formatCurrency(value)}
    description='Total pipeline value'
    icon={
      <svg
        className='h-4 w-4 text-yellow-600'
        fill='none'
        stroke='currentColor'
        viewBox='0 0 24 24'
      >
        <path
          strokeLinecap='round'
          strokeLinejoin='round'
          strokeWidth='2'
          d='M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1'
        />
      </svg>
    }
    trend={trend}
    isLoading={isLoading}
    variant='warning'
  />
);

export const ConversionRateCard: React.FC<{
  value: number;
  trend?: MetricsCardProps['trend'];
  isLoading?: boolean;
}> = ({ value, trend, isLoading }) => (
  <MetricsCard
    title='Conversion Rate'
    value={`${value.toFixed(1)}%`}
    description='Lead to customer conversion'
    icon={
      <svg
        className='h-4 w-4 text-purple-600'
        fill='none'
        stroke='currentColor'
        viewBox='0 0 24 24'
      >
        <path
          strokeLinecap='round'
          strokeLinejoin='round'
          strokeWidth='2'
          d='M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z'
        />
      </svg>
    }
    trend={trend}
    isLoading={isLoading}
    variant='default'
  />
);

export default MetricsCard;
