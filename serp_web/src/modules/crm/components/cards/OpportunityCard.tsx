/**
 * OpportunityCard Component - Enhanced opportunity display card
 * @author QuanTuanHuy
 * @description Part of Serp Project - CRM opportunity card with probability bar
 */

'use client';

import React from 'react';
import { cn } from '@/shared/utils';
import {
  Card,
  CardContent,
  Button,
  Badge,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/shared/components/ui';
import {
  Building2,
  Calendar,
  User,
  MoreHorizontal,
  ExternalLink,
  Edit,
  Trash2,
  TrendingUp,
  Target,
  CheckCircle2,
  XCircle,
} from 'lucide-react';
import type {
  Opportunity,
  OpportunityStage,
  OpportunityType,
} from '../../types';

export interface OpportunityCardProps {
  opportunity: Opportunity;
  onClick?: () => void;
  onEdit?: () => void;
  onDelete?: () => void;
  onStageChange?: (newStage: OpportunityStage) => void;
  className?: string;
  variant?: 'default' | 'compact' | 'pipeline';
}

const stageStyles: Record<
  OpportunityStage,
  { bg: string; text: string; dot: string; icon: typeof Target }
> = {
  PROSPECTING: {
    bg: 'bg-blue-100 dark:bg-blue-900/30',
    text: 'text-blue-700 dark:text-blue-400',
    dot: 'bg-blue-500',
    icon: Target,
  },
  QUALIFICATION: {
    bg: 'bg-indigo-100 dark:bg-indigo-900/30',
    text: 'text-indigo-700 dark:text-indigo-400',
    dot: 'bg-indigo-500',
    icon: TrendingUp,
  },
  PROPOSAL: {
    bg: 'bg-amber-100 dark:bg-amber-900/30',
    text: 'text-amber-700 dark:text-amber-400',
    dot: 'bg-amber-500',
    icon: Building2,
  },
  NEGOTIATION: {
    bg: 'bg-orange-100 dark:bg-orange-900/30',
    text: 'text-orange-700 dark:text-orange-400',
    dot: 'bg-orange-500',
    icon: TrendingUp,
  },
  CLOSED_WON: {
    bg: 'bg-emerald-100 dark:bg-emerald-900/30',
    text: 'text-emerald-700 dark:text-emerald-400',
    dot: 'bg-emerald-500',
    icon: CheckCircle2,
  },
  CLOSED_LOST: {
    bg: 'bg-rose-100 dark:bg-rose-900/30',
    text: 'text-rose-700 dark:text-rose-400',
    dot: 'bg-rose-500',
    icon: XCircle,
  },
};

const typeStyles: Record<OpportunityType, { label: string; color: string }> = {
  NEW_BUSINESS: { label: 'New Business', color: 'text-blue-600' },
  EXISTING_BUSINESS: { label: 'Existing', color: 'text-emerald-600' },
  RENEWAL: { label: 'Renewal', color: 'text-purple-600' },
};

const formatCurrency = (value: number): string => {
  if (value >= 1000000) return `$${(value / 1000000).toFixed(1)}M`;
  if (value >= 1000) return `$${(value / 1000).toFixed(0)}K`;
  return `$${value.toLocaleString()}`;
};

const formatDate = (dateString: string): string => {
  const date = new Date(dateString);
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
};

const getDaysUntil = (
  dateString: string
): { days: number; isOverdue: boolean } => {
  const date = new Date(dateString);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  date.setHours(0, 0, 0, 0);
  const diffTime = date.getTime() - today.getTime();
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  return { days: Math.abs(diffDays), isOverdue: diffDays < 0 };
};

// Probability Bar Component
const ProbabilityBar: React.FC<{ probability: number }> = ({ probability }) => {
  const normalizedProbability = Math.min(Math.max(probability, 0), 100);

  const getColor = () => {
    if (normalizedProbability >= 80) return 'bg-emerald-500';
    if (normalizedProbability >= 60) return 'bg-blue-500';
    if (normalizedProbability >= 40) return 'bg-amber-500';
    if (normalizedProbability >= 20) return 'bg-orange-500';
    return 'bg-rose-500';
  };

  return (
    <div className='space-y-1'>
      <div className='flex items-center justify-between text-xs'>
        <span className='text-muted-foreground'>Probability</span>
        <span className='font-medium'>{normalizedProbability}%</span>
      </div>
      <div className='h-1.5 w-full bg-muted rounded-full overflow-hidden'>
        <div
          className={cn(
            'h-full rounded-full transition-all duration-300',
            getColor()
          )}
          style={{ width: `${normalizedProbability}%` }}
        />
      </div>
    </div>
  );
};

// Stage Progress Component
const StageProgress: React.FC<{ currentStage: OpportunityStage }> = ({
  currentStage,
}) => {
  const stages: OpportunityStage[] = [
    'PROSPECTING',
    'QUALIFICATION',
    'PROPOSAL',
    'NEGOTIATION',
    'CLOSED_WON',
  ];

  const currentIndex = stages.indexOf(currentStage);
  const isClosed =
    currentStage === 'CLOSED_WON' || currentStage === 'CLOSED_LOST';

  return (
    <div className='flex items-center gap-1'>
      {stages.slice(0, -1).map((stage, index) => {
        const isCompleted =
          index < currentIndex || currentStage === 'CLOSED_WON';
        const isCurrent = index === currentIndex && !isClosed;

        return (
          <div
            key={stage}
            className={cn(
              'h-1.5 flex-1 rounded-full transition-colors',
              isCompleted
                ? 'bg-emerald-500'
                : isCurrent
                  ? 'bg-blue-500'
                  : 'bg-muted'
            )}
          />
        );
      })}
    </div>
  );
};

export const OpportunityCard: React.FC<OpportunityCardProps> = ({
  opportunity,
  onClick,
  onEdit,
  onDelete,
  onStageChange,
  className,
  variant = 'default',
}) => {
  const stage = stageStyles[opportunity.stage] || stageStyles.PROSPECTING;
  const type = typeStyles[opportunity.type] || typeStyles.NEW_BUSINESS;
  const StageIcon = stage.icon;
  const closeDateInfo = getDaysUntil(opportunity.expectedCloseDate);

  // Pipeline card variant (for Kanban/Pipeline view)
  if (variant === 'pipeline') {
    return (
      <Card
        className={cn(
          'group p-3 shadow-sm',
          'hover:shadow-md hover:border-primary/20 transition-all duration-200 cursor-pointer',
          className
        )}
        onClick={onClick}
      >
        {/* Header */}
        <div className='flex items-start justify-between mb-2'>
          <h4 className='font-medium text-sm truncate flex-1'>
            {opportunity.name}
          </h4>
          <span className='text-sm font-bold ml-2'>
            {formatCurrency(opportunity.value)}
          </span>
        </div>

        {/* Customer */}
        <p className='text-xs text-muted-foreground truncate mb-2'>
          {opportunity.customerName}
        </p>

        {/* Probability */}
        <ProbabilityBar probability={opportunity.probability} />

        {/* Footer */}
        <div className='flex items-center justify-between mt-3 pt-2 border-t'>
          <div className='flex items-center gap-1 text-xs text-muted-foreground'>
            <Calendar className='h-3 w-3' />
            {formatDate(opportunity.expectedCloseDate)}
          </div>
          <div className='flex items-center gap-1 text-xs'>
            <User className='h-3 w-3 text-muted-foreground' />
            <span className='truncate max-w-16'>
              {opportunity.assignedToName}
            </span>
          </div>
        </div>
      </Card>
    );
  }

  // Compact card variant
  if (variant === 'compact') {
    return (
      <Card
        className={cn(
          'group flex flex-row items-center gap-3 p-3',
          'hover:shadow-md hover:border-primary/20 transition-all duration-200 cursor-pointer',
          className
        )}
        onClick={onClick}
      >
        {/* Icon */}
        <div
          className={cn(
            'flex h-10 w-10 shrink-0 items-center justify-center rounded-lg',
            stage.bg
          )}
        >
          <StageIcon className={cn('h-5 w-5', stage.text)} />
        </div>

        {/* Info */}
        <div className='flex-1 min-w-0'>
          <p className='font-medium text-sm truncate'>{opportunity.name}</p>
          <p className='text-xs text-muted-foreground truncate'>
            {opportunity.customerName}
          </p>
        </div>

        {/* Value */}
        <div className='text-right shrink-0'>
          <p className='font-bold text-sm'>
            {formatCurrency(opportunity.value)}
          </p>
          <p className='text-xs text-muted-foreground'>
            {opportunity.probability}%
          </p>
        </div>
      </Card>
    );
  }

  // Default full card
  return (
    <Card
      className={cn(
        'group relative overflow-hidden',
        'hover:shadow-lg hover:border-primary/20 transition-all duration-200 cursor-pointer',
        className
      )}
      onClick={onClick}
    >
      {/* Stage indicator gradient */}
      <div className={cn('h-1.5', stage.bg)} />

      <CardContent className='p-5'>
        {/* Top Row */}
        <div className='flex items-start justify-between mb-3'>
          <div className='flex items-center gap-3 min-w-0'>
            <div
              className={cn(
                'flex h-11 w-11 shrink-0 items-center justify-center rounded-xl shadow-sm',
                stage.bg
              )}
            >
              <StageIcon className={cn('h-5 w-5', stage.text)} />
            </div>
            <div className='min-w-0'>
              <h3 className='font-semibold text-foreground truncate'>
                {opportunity.name}
              </h3>
              <p className='text-sm text-muted-foreground truncate'>
                {opportunity.customerName}
              </p>
            </div>
          </div>

          {/* Menu */}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button
                variant='ghost'
                size='icon'
                className='h-8 w-8 opacity-0 group-hover:opacity-100 transition-opacity'
                onClick={(e) => e.stopPropagation()}
              >
                <MoreHorizontal className='h-4 w-4 text-muted-foreground' />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align='end'>
              {onClick && (
                <DropdownMenuItem
                  onClick={(e) => {
                    e.stopPropagation();
                    onClick();
                  }}
                >
                  <ExternalLink className='h-4 w-4 mr-2' />
                  View Details
                </DropdownMenuItem>
              )}
              {onEdit && (
                <DropdownMenuItem
                  onClick={(e) => {
                    e.stopPropagation();
                    onEdit();
                  }}
                >
                  <Edit className='h-4 w-4 mr-2' />
                  Edit
                </DropdownMenuItem>
              )}
              {onDelete && (
                <DropdownMenuItem
                  onClick={(e) => {
                    e.stopPropagation();
                    onDelete();
                  }}
                  className='text-destructive focus:text-destructive'
                >
                  <Trash2 className='h-4 w-4 mr-2' />
                  Delete
                </DropdownMenuItem>
              )}
            </DropdownMenuContent>
          </DropdownMenu>
        </div>

        {/* Stage & Type Badges */}
        <div className='flex items-center gap-2 mb-4'>
          <Badge variant='secondary' className={cn(stage.bg, stage.text)}>
            {opportunity.stage.replace('_', ' ')}
          </Badge>
          <Badge variant='outline' className={type.color}>
            {type.label}
          </Badge>
        </div>

        {/* Stage Progress */}
        <div className='mb-4'>
          <StageProgress currentStage={opportunity.stage} />
        </div>

        {/* Probability */}
        <div className='mb-4'>
          <ProbabilityBar probability={opportunity.probability} />
        </div>

        {/* Value */}
        <div className='bg-muted/30 rounded-lg p-3 mb-4'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-xs text-muted-foreground'>Deal Value</p>
              <p className='text-xl font-bold'>
                {formatCurrency(opportunity.value)}
              </p>
            </div>
            <div className='text-right'>
              <p className='text-xs text-muted-foreground'>Weighted</p>
              <p className='text-lg font-semibold text-muted-foreground'>
                {formatCurrency(
                  (opportunity.value * opportunity.probability) / 100
                )}
              </p>
            </div>
          </div>
        </div>

        {/* Footer Info */}
        <div className='grid grid-cols-2 gap-3 pt-4 border-t'>
          <div className='flex items-center gap-2 text-sm'>
            <Calendar className='h-4 w-4 text-muted-foreground' />
            <div>
              <p className='text-xs text-muted-foreground'>Close Date</p>
              <p
                className={cn(
                  'font-medium',
                  closeDateInfo.isOverdue && 'text-rose-500'
                )}
              >
                {formatDate(opportunity.expectedCloseDate)}
                {closeDateInfo.isOverdue && (
                  <span className='ml-1 text-xs'>
                    ({closeDateInfo.days}d overdue)
                  </span>
                )}
              </p>
            </div>
          </div>
          <div className='flex items-center gap-2 text-sm'>
            <User className='h-4 w-4 text-muted-foreground' />
            <div>
              <p className='text-xs text-muted-foreground'>Owner</p>
              <p className='font-medium truncate'>
                {opportunity.assignedToName}
              </p>
            </div>
          </div>
        </div>

        {/* Tags */}
        {opportunity.tags && opportunity.tags.length > 0 && (
          <div className='flex flex-wrap gap-1 mt-4'>
            {opportunity.tags.slice(0, 3).map((tag) => (
              <span
                key={tag}
                className='px-2 py-0.5 rounded-md bg-secondary text-secondary-foreground text-xs'
              >
                {tag}
              </span>
            ))}
            {opportunity.tags.length > 3 && (
              <span className='px-2 py-0.5 text-xs text-muted-foreground'>
                +{opportunity.tags.length - 3}
              </span>
            )}
          </div>
        )}
      </CardContent>
    </Card>
  );
};
