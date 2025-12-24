'use client';

/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Top Performers Leaderboard
 */

import React from 'react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription,
} from '@/shared/components/ui/card';
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from '@/shared/components/ui/avatar';
import { Badge } from '@/shared/components/ui/badge';
import { Progress } from '@/shared/components/ui/progress';
import { Trophy, Medal, Award, TrendingUp, TrendingDown } from 'lucide-react';
import { cn } from '@/shared';

export interface PerformerData {
  id: string;
  name: string;
  avatar?: string;
  deals: number;
  revenue: number;
  target: number;
  trend: number;
}

export interface TopPerformersProps {
  data: PerformerData[];
  title?: string;
  description?: string;
  className?: string;
}

function formatCurrency(value: number): string {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    notation: 'compact',
    maximumFractionDigits: 1,
  }).format(value);
}

function getRankIcon(rank: number) {
  switch (rank) {
    case 1:
      return <Trophy className='h-4 w-4 text-yellow-500' />;
    case 2:
      return <Medal className='h-4 w-4 text-gray-400' />;
    case 3:
      return <Award className='h-4 w-4 text-amber-600' />;
    default:
      return (
        <span className='text-sm font-medium text-muted-foreground'>
          #{rank}
        </span>
      );
  }
}

function getRankBadgeClass(rank: number): string {
  switch (rank) {
    case 1:
      return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400';
    case 2:
      return 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300';
    case 3:
      return 'bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-400';
    default:
      return 'bg-muted text-muted-foreground';
  }
}

export function TopPerformers({
  data,
  title = 'Bảng xếp hạng',
  description = 'Top nhân viên bán hàng',
  className,
}: TopPerformersProps) {
  const sortedData = [...data].sort((a, b) => b.revenue - a.revenue);

  return (
    <Card className={cn('overflow-hidden', className)}>
      <CardHeader>
        <CardTitle>{title}</CardTitle>
        <CardDescription>{description}</CardDescription>
      </CardHeader>
      <CardContent>
        <div className='space-y-4'>
          {sortedData.map((performer, index) => {
            const rank = index + 1;
            const achievementPercent = Math.min(
              (performer.revenue / performer.target) * 100,
              100
            );
            const initials = performer.name
              .split(' ')
              .map((n) => n[0])
              .join('')
              .slice(0, 2);

            return (
              <div
                key={performer.id}
                className={cn(
                  'flex items-center gap-4 p-3 rounded-lg transition-colors',
                  rank <= 3 ? 'bg-muted/50' : 'hover:bg-muted/30'
                )}
              >
                {/* Rank */}
                <div className='w-8 flex items-center justify-center'>
                  {getRankIcon(rank)}
                </div>

                {/* Avatar */}
                <Avatar className='h-10 w-10 border-2 border-background'>
                  <AvatarImage src={performer.avatar} alt={performer.name} />
                  <AvatarFallback className={getRankBadgeClass(rank)}>
                    {initials}
                  </AvatarFallback>
                </Avatar>

                {/* Info */}
                <div className='flex-1 min-w-0'>
                  <div className='flex items-center gap-2'>
                    <span className='font-medium text-foreground truncate'>
                      {performer.name}
                    </span>
                    {rank <= 3 && (
                      <Badge
                        variant='secondary'
                        className={cn('text-xs', getRankBadgeClass(rank))}
                      >
                        Top {rank}
                      </Badge>
                    )}
                  </div>
                  <div className='flex items-center gap-3 mt-1'>
                    <span className='text-xs text-muted-foreground'>
                      {performer.deals} deals
                    </span>
                    <div className='flex-1 max-w-24'>
                      <Progress value={achievementPercent} className='h-1.5' />
                    </div>
                    <span className='text-xs text-muted-foreground'>
                      {achievementPercent.toFixed(0)}%
                    </span>
                  </div>
                </div>

                {/* Revenue & Trend */}
                <div className='text-right'>
                  <div className='font-semibold text-foreground'>
                    {formatCurrency(performer.revenue)}
                  </div>
                  <div
                    className={cn(
                      'flex items-center gap-1 text-xs justify-end',
                      performer.trend > 0
                        ? 'text-green-600 dark:text-green-400'
                        : performer.trend < 0
                          ? 'text-red-600 dark:text-red-400'
                          : 'text-muted-foreground'
                    )}
                  >
                    {performer.trend > 0 ? (
                      <TrendingUp className='h-3 w-3' />
                    ) : performer.trend < 0 ? (
                      <TrendingDown className='h-3 w-3' />
                    ) : null}
                    <span>
                      {performer.trend > 0 ? '+' : ''}
                      {performer.trend}%
                    </span>
                  </div>
                </div>
              </div>
            );
          })}
        </div>

        {/* Summary */}
        <div className='mt-4 pt-4 border-t border-border flex items-center justify-between text-sm'>
          <span className='text-muted-foreground'>
            Tổng: {sortedData.length} nhân viên
          </span>
          <span className='text-foreground font-medium'>
            {formatCurrency(sortedData.reduce((sum, p) => sum + p.revenue, 0))}
          </span>
        </div>
      </CardContent>
    </Card>
  );
}
