/**
 * LeadCard Component - Enhanced lead display card with score indicator
 * @author QuanTuanHuy
 * @description Part of Serp Project - CRM lead card with source badge
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
  Mail,
  Phone,
  Building2,
  MoreHorizontal,
  ExternalLink,
  Edit,
  Trash2,
  UserCheck,
  Globe,
  MessageSquare,
  Calendar,
  Target,
  Users,
  type LucideIcon,
} from 'lucide-react';
import type { Lead, LeadStatus, LeadSource, Priority } from '../../types';

export interface LeadCardProps {
  lead: Lead;
  onClick?: () => void;
  onEdit?: () => void;
  onDelete?: () => void;
  onConvert?: () => void;
  onEmailClick?: () => void;
  onCallClick?: () => void;
  className?: string;
  variant?: 'default' | 'compact' | 'kanban';
}

const statusStyles: Record<
  LeadStatus,
  { bg: string; text: string; dot: string }
> = {
  NEW: {
    bg: 'bg-blue-100 dark:bg-blue-900/30',
    text: 'text-blue-700 dark:text-blue-400',
    dot: 'bg-blue-500',
  },
  CONTACTED: {
    bg: 'bg-amber-100 dark:bg-amber-900/30',
    text: 'text-amber-700 dark:text-amber-400',
    dot: 'bg-amber-500',
  },
  QUALIFIED: {
    bg: 'bg-emerald-100 dark:bg-emerald-900/30',
    text: 'text-emerald-700 dark:text-emerald-400',
    dot: 'bg-emerald-500',
  },
  CONVERTED: {
    bg: 'bg-purple-100 dark:bg-purple-900/30',
    text: 'text-purple-700 dark:text-purple-400',
    dot: 'bg-purple-500',
  },
  LOST: {
    bg: 'bg-slate-100 dark:bg-slate-800/50',
    text: 'text-slate-600 dark:text-slate-400',
    dot: 'bg-slate-400',
  },
};

const sourceConfig: Record<
  LeadSource,
  { icon: LucideIcon; label: string; color: string }
> = {
  WEBSITE: { icon: Globe, label: 'Website', color: 'text-blue-500' },
  REFERRAL: { icon: Users, label: 'Referral', color: 'text-emerald-500' },
  EMAIL: { icon: Mail, label: 'Email', color: 'text-indigo-500' },
  PHONE: { icon: Phone, label: 'Phone', color: 'text-green-500' },
  SOCIAL_MEDIA: {
    icon: MessageSquare,
    label: 'Social',
    color: 'text-pink-500',
  },
  TRADE_SHOW: { icon: Calendar, label: 'Trade Show', color: 'text-orange-500' },
  OTHER: { icon: Target, label: 'Other', color: 'text-slate-500' },
};

const priorityStyles: Record<Priority, { bg: string; text: string }> = {
  LOW: {
    bg: 'bg-slate-100 dark:bg-slate-800/50',
    text: 'text-slate-600 dark:text-slate-400',
  },
  MEDIUM: {
    bg: 'bg-blue-100 dark:bg-blue-900/30',
    text: 'text-blue-600 dark:text-blue-400',
  },
  HIGH: {
    bg: 'bg-amber-100 dark:bg-amber-900/30',
    text: 'text-amber-600 dark:text-amber-400',
  },
  URGENT: {
    bg: 'bg-rose-100 dark:bg-rose-900/30',
    text: 'text-rose-600 dark:text-rose-400',
  },
};

const formatCurrency = (value: number): string => {
  if (value >= 1000000) return `$${(value / 1000000).toFixed(1)}M`;
  if (value >= 1000) return `$${(value / 1000).toFixed(0)}K`;
  return `$${value.toLocaleString()}`;
};

const getInitials = (firstName: string, lastName: string): string => {
  return `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase();
};

// Lead Score Component
const LeadScoreIndicator: React.FC<{ score?: number }> = ({ score = 0 }) => {
  const normalizedScore = Math.min(Math.max(score, 0), 100);
  const getScoreColor = () => {
    if (normalizedScore >= 80) return 'text-emerald-500';
    if (normalizedScore >= 60) return 'text-blue-500';
    if (normalizedScore >= 40) return 'text-amber-500';
    return 'text-slate-400';
  };

  const getScoreBg = () => {
    if (normalizedScore >= 80) return 'stroke-emerald-500';
    if (normalizedScore >= 60) return 'stroke-blue-500';
    if (normalizedScore >= 40) return 'stroke-amber-500';
    return 'stroke-slate-300';
  };

  return (
    <div className='relative flex items-center justify-center h-10 w-10'>
      <svg className='h-10 w-10 -rotate-90' viewBox='0 0 36 36'>
        <circle
          cx='18'
          cy='18'
          r='14'
          fill='none'
          strokeWidth='3'
          className='stroke-muted'
        />
        <circle
          cx='18'
          cy='18'
          r='14'
          fill='none'
          strokeWidth='3'
          strokeDasharray={`${(normalizedScore / 100) * 88} 88`}
          strokeLinecap='round'
          className={getScoreBg()}
        />
      </svg>
      <span className={cn('absolute text-xs font-bold', getScoreColor())}>
        {normalizedScore}
      </span>
    </div>
  );
};

export const LeadCard: React.FC<LeadCardProps> = ({
  lead,
  onClick,
  onEdit,
  onDelete,
  onConvert,
  onEmailClick,
  onCallClick,
  className,
  variant = 'default',
}) => {
  const status = statusStyles[lead.status] || statusStyles.NEW;
  const source = sourceConfig[lead.source] || sourceConfig.OTHER;
  const priority = priorityStyles[lead.priority] || priorityStyles.MEDIUM;
  const SourceIcon = source.icon;
  const fullName = `${lead.firstName} ${lead.lastName}`;

  // Kanban card variant
  if (variant === 'kanban') {
    return (
      <Card
        className={cn(
          'group p-3 shadow-sm',
          'hover:shadow-md hover:border-primary/20 transition-all duration-200 cursor-pointer',
          className
        )}
        onClick={onClick}
      >
        <div className='flex items-start justify-between mb-2'>
          <div className='flex items-center gap-2'>
            <div className='flex h-8 w-8 items-center justify-center rounded-full bg-gradient-to-br from-primary/20 to-primary/5 text-primary font-semibold text-xs'>
              {getInitials(lead.firstName, lead.lastName)}
            </div>
            <div className='min-w-0'>
              <p className='font-medium text-sm truncate'>{fullName}</p>
              {lead.company && (
                <p className='text-xs text-muted-foreground truncate'>
                  {lead.company}
                </p>
              )}
            </div>
          </div>
          <LeadScoreIndicator score={50} />
        </div>

        <div className='flex items-center justify-between'>
          <span className={cn('text-xs font-medium', source.color)}>
            <SourceIcon className='h-3 w-3 inline mr-1' />
            {source.label}
          </span>
          {lead.estimatedValue && (
            <span className='text-xs font-semibold'>
              {formatCurrency(lead.estimatedValue)}
            </span>
          )}
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
        <div className='flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-primary/20 to-primary/5 text-primary font-semibold text-sm'>
          {getInitials(lead.firstName, lead.lastName)}
        </div>

        <div className='flex-1 min-w-0'>
          <p className='font-medium text-sm truncate'>{fullName}</p>
          <p className='text-xs text-muted-foreground truncate'>{lead.email}</p>
        </div>

        <Badge variant='secondary' className={cn(status.bg, status.text)}>
          {lead.status}
        </Badge>
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
      {/* Priority indicator bar */}
      <div
        className={cn(
          'h-1',
          lead.priority === 'URGENT'
            ? 'bg-gradient-to-r from-rose-500 to-rose-400'
            : lead.priority === 'HIGH'
              ? 'bg-gradient-to-r from-amber-500 to-amber-400'
              : lead.priority === 'MEDIUM'
                ? 'bg-gradient-to-r from-blue-500 to-blue-400'
                : 'bg-gradient-to-r from-slate-300 to-slate-200'
        )}
      />

      <CardContent className='p-5'>
        {/* Top Row */}
        <div className='flex items-start justify-between mb-4'>
          <div className='flex items-center gap-3'>
            <div className='relative'>
              <div className='flex h-12 w-12 items-center justify-center rounded-full bg-gradient-to-br from-primary/20 to-primary/5 text-primary font-semibold text-lg shadow-sm'>
                {getInitials(lead.firstName, lead.lastName)}
              </div>
              <div
                className={cn(
                  'absolute -bottom-0.5 -right-0.5 h-3.5 w-3.5 rounded-full border-2 border-card',
                  status.dot
                )}
              />
            </div>
            <div className='min-w-0'>
              <h3 className='font-semibold text-foreground truncate'>
                {fullName}
              </h3>
              {lead.jobTitle && (
                <p className='text-xs text-muted-foreground truncate'>
                  {lead.jobTitle}
                </p>
              )}
            </div>
          </div>

          {/* Score & Menu */}
          <div className='flex items-center gap-2'>
            <LeadScoreIndicator score={50} />
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
                {onConvert && lead.status !== 'CONVERTED' && (
                  <DropdownMenuItem
                    onClick={(e) => {
                      e.stopPropagation();
                      onConvert();
                    }}
                    className='text-emerald-600 focus:text-emerald-600'
                  >
                    <UserCheck className='h-4 w-4 mr-2' />
                    Convert
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
        </div>

        {/* Status & Source Badges */}
        <div className='flex items-center gap-2 mb-4'>
          <Badge
            variant='secondary'
            className={cn('gap-1', status.bg, status.text)}
          >
            <span className={cn('h-1.5 w-1.5 rounded-full', status.dot)} />
            {lead.status.charAt(0) + lead.status.slice(1).toLowerCase()}
          </Badge>
          <Badge variant='outline' className='gap-1'>
            <SourceIcon className={cn('h-3 w-3', source.color)} />
            {source.label}
          </Badge>
          <Badge variant='secondary' className={cn(priority.bg, priority.text)}>
            {lead.priority}
          </Badge>
        </div>

        {/* Contact Info */}
        <div className='space-y-2 mb-4'>
          {lead.company && (
            <div className='flex items-center gap-2 text-sm'>
              <Building2 className='h-4 w-4 text-muted-foreground shrink-0' />
              <span className='truncate text-muted-foreground'>
                {lead.company}
              </span>
            </div>
          )}
          <div className='flex items-center gap-2 text-sm'>
            <Mail className='h-4 w-4 text-muted-foreground shrink-0' />
            <span className='truncate text-muted-foreground'>{lead.email}</span>
          </div>
          {lead.phone && (
            <div className='flex items-center gap-2 text-sm'>
              <Phone className='h-4 w-4 text-muted-foreground shrink-0' />
              <span className='text-muted-foreground'>{lead.phone}</span>
            </div>
          )}
        </div>

        {/* Tags */}
        {lead.tags && lead.tags.length > 0 && (
          <div className='flex flex-wrap gap-1 mb-4'>
            {lead.tags.slice(0, 3).map((tag) => (
              <span
                key={tag}
                className='px-2 py-0.5 rounded-md bg-secondary text-secondary-foreground text-xs'
              >
                {tag}
              </span>
            ))}
            {lead.tags.length > 3 && (
              <span className='px-2 py-0.5 text-xs text-muted-foreground'>
                +{lead.tags.length - 3}
              </span>
            )}
          </div>
        )}

        {/* Footer */}
        <div className='flex items-center justify-between pt-4 border-t'>
          <div>
            <p className='text-xs text-muted-foreground'>Est. Value</p>
            <p className='text-lg font-bold text-foreground'>
              {lead.estimatedValue ? formatCurrency(lead.estimatedValue) : '-'}
            </p>
          </div>

          <div className='flex items-center gap-1'>
            {onEmailClick && (
              <Button
                variant='ghost'
                size='icon'
                className='h-9 w-9 hover:bg-blue-100 dark:hover:bg-blue-900/30 text-blue-600 dark:text-blue-400'
                onClick={(e) => {
                  e.stopPropagation();
                  onEmailClick();
                }}
                title='Send Email'
              >
                <Mail className='h-4 w-4' />
              </Button>
            )}
            {onCallClick && lead.phone && (
              <Button
                variant='ghost'
                size='icon'
                className='h-9 w-9 hover:bg-emerald-100 dark:hover:bg-emerald-900/30 text-emerald-600 dark:text-emerald-400'
                onClick={(e) => {
                  e.stopPropagation();
                  onCallClick();
                }}
                title='Call'
              >
                <Phone className='h-4 w-4' />
              </Button>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  );
};
