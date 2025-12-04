/**
 * CustomerCard Component - Enhanced customer display card
 * @author QuanTuanHuy
 * @description Part of Serp Project - CRM customer card with avatar and quick actions
 */

'use client';

import React from 'react';
import { cn } from '@/shared/utils';
import {
  Mail,
  Phone,
  Building2,
  MapPin,
  MoreHorizontal,
  ExternalLink,
  Edit,
  Trash2,
  Star,
  Globe,
} from 'lucide-react';
import type { Customer, CustomerStatus, CustomerType } from '../../types';

export interface CustomerCardProps {
  customer: Customer;
  onClick?: () => void;
  onEdit?: () => void;
  onDelete?: () => void;
  onEmailClick?: () => void;
  onCallClick?: () => void;
  className?: string;
  variant?: 'default' | 'compact';
}

const statusStyles: Record<
  CustomerStatus,
  { bg: string; text: string; dot: string }
> = {
  ACTIVE: {
    bg: 'bg-emerald-100 dark:bg-emerald-900/30',
    text: 'text-emerald-700 dark:text-emerald-400',
    dot: 'bg-emerald-500',
  },
  INACTIVE: {
    bg: 'bg-slate-100 dark:bg-slate-800/50',
    text: 'text-slate-600 dark:text-slate-400',
    dot: 'bg-slate-400',
  },
  POTENTIAL: {
    bg: 'bg-blue-100 dark:bg-blue-900/30',
    text: 'text-blue-700 dark:text-blue-400',
    dot: 'bg-blue-500',
  },
  BLOCKED: {
    bg: 'bg-rose-100 dark:bg-rose-900/30',
    text: 'text-rose-700 dark:text-rose-400',
    dot: 'bg-rose-500',
  },
};

const typeStyles: Record<
  CustomerType,
  { icon: typeof Building2; label: string }
> = {
  INDIVIDUAL: { icon: Star, label: 'Individual' },
  COMPANY: { icon: Building2, label: 'Company' },
};

const formatCurrency = (value: number): string => {
  if (value >= 1000000) return `$${(value / 1000000).toFixed(1)}M`;
  if (value >= 1000) return `$${(value / 1000).toFixed(0)}K`;
  return `$${value.toLocaleString()}`;
};

const getInitials = (name: string): string => {
  return name
    .split(' ')
    .map((n) => n.charAt(0))
    .join('')
    .toUpperCase()
    .slice(0, 2);
};

export const CustomerCard: React.FC<CustomerCardProps> = ({
  customer,
  onClick,
  onEdit,
  onDelete,
  onEmailClick,
  onCallClick,
  className,
  variant = 'default',
}) => {
  const [showMenu, setShowMenu] = React.useState(false);
  const status = statusStyles[customer.status] || statusStyles.ACTIVE;
  const type = typeStyles[customer.customerType] || typeStyles.INDIVIDUAL;
  const TypeIcon = type.icon;

  const handleMenuClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    setShowMenu(!showMenu);
  };

  if (variant === 'compact') {
    return (
      <div
        className={cn(
          'group flex items-center gap-3 p-3 rounded-lg border bg-card',
          'hover:shadow-md hover:border-primary/20 transition-all duration-200 cursor-pointer',
          className
        )}
        onClick={onClick}
      >
        {/* Avatar */}
        <div className='flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-primary/20 to-primary/5 text-primary font-semibold text-sm'>
          {getInitials(customer.name)}
        </div>

        {/* Info */}
        <div className='flex-1 min-w-0'>
          <p className='font-medium text-sm truncate'>{customer.name}</p>
          <p className='text-xs text-muted-foreground truncate'>
            {customer.email}
          </p>
        </div>

        {/* Status */}
        <div className={cn('h-2 w-2 rounded-full', status.dot)} />
      </div>
    );
  }

  return (
    <div
      className={cn(
        'group relative overflow-hidden rounded-xl border bg-card',
        'hover:shadow-lg hover:border-primary/20 transition-all duration-200 cursor-pointer',
        className
      )}
      onClick={onClick}
    >
      {/* Header with gradient accent */}
      <div className='relative h-2 bg-gradient-to-r from-primary/60 via-primary/40 to-primary/20' />

      {/* Content */}
      <div className='p-5'>
        {/* Top Row - Avatar & Actions */}
        <div className='flex items-start justify-between mb-4'>
          {/* Avatar & Name */}
          <div className='flex items-center gap-3'>
            <div className='relative'>
              <div className='flex h-12 w-12 items-center justify-center rounded-full bg-gradient-to-br from-primary/20 to-primary/5 text-primary font-semibold text-lg shadow-sm'>
                {getInitials(customer.name)}
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
                {customer.name}
              </h3>
              {customer.companyName && (
                <p className='text-xs text-muted-foreground truncate'>
                  {customer.companyName}
                </p>
              )}
            </div>
          </div>

          {/* Actions Menu */}
          <div className='relative'>
            <button
              onClick={handleMenuClick}
              className='p-1.5 rounded-md hover:bg-muted opacity-0 group-hover:opacity-100 transition-opacity'
            >
              <MoreHorizontal className='h-4 w-4 text-muted-foreground' />
            </button>

            {showMenu && (
              <div className='absolute right-0 top-full mt-1 w-36 rounded-lg border bg-popover shadow-lg z-10'>
                <div className='p-1'>
                  {onClick && (
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        onClick();
                        setShowMenu(false);
                      }}
                      className='w-full flex items-center gap-2 px-3 py-2 text-sm rounded-md hover:bg-muted'
                    >
                      <ExternalLink className='h-4 w-4' />
                      View Details
                    </button>
                  )}
                  {onEdit && (
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        onEdit();
                        setShowMenu(false);
                      }}
                      className='w-full flex items-center gap-2 px-3 py-2 text-sm rounded-md hover:bg-muted'
                    >
                      <Edit className='h-4 w-4' />
                      Edit
                    </button>
                  )}
                  {onDelete && (
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        onDelete();
                        setShowMenu(false);
                      }}
                      className='w-full flex items-center gap-2 px-3 py-2 text-sm rounded-md hover:bg-muted text-destructive'
                    >
                      <Trash2 className='h-4 w-4' />
                      Delete
                    </button>
                  )}
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Status & Type Badges */}
        <div className='flex items-center gap-2 mb-4'>
          <span
            className={cn(
              'inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium',
              status.bg,
              status.text
            )}
          >
            <span className={cn('h-1.5 w-1.5 rounded-full', status.dot)} />
            {customer.status.charAt(0) + customer.status.slice(1).toLowerCase()}
          </span>
          <span className='inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium bg-muted text-muted-foreground'>
            <TypeIcon className='h-3 w-3' />
            {type.label}
          </span>
        </div>

        {/* Contact Info */}
        <div className='space-y-2 mb-4'>
          <div className='flex items-center gap-2 text-sm'>
            <Mail className='h-4 w-4 text-muted-foreground shrink-0' />
            <span className='truncate text-muted-foreground'>
              {customer.email}
            </span>
          </div>
          {customer.phone && (
            <div className='flex items-center gap-2 text-sm'>
              <Phone className='h-4 w-4 text-muted-foreground shrink-0' />
              <span className='text-muted-foreground'>{customer.phone}</span>
            </div>
          )}
          {customer.address && (
            <div className='flex items-center gap-2 text-sm'>
              <MapPin className='h-4 w-4 text-muted-foreground shrink-0' />
              <span className='truncate text-muted-foreground'>
                {customer.address}
              </span>
            </div>
          )}
          {customer.website && (
            <div className='flex items-center gap-2 text-sm'>
              <Globe className='h-4 w-4 text-muted-foreground shrink-0' />
              <span className='truncate text-muted-foreground'>
                {customer.website}
              </span>
            </div>
          )}
        </div>

        {/* Tags */}
        {customer.tags && customer.tags.length > 0 && (
          <div className='flex flex-wrap gap-1 mb-4'>
            {customer.tags.slice(0, 3).map((tag) => (
              <span
                key={tag}
                className='px-2 py-0.5 rounded-md bg-secondary text-secondary-foreground text-xs'
              >
                {tag}
              </span>
            ))}
            {customer.tags.length > 3 && (
              <span className='px-2 py-0.5 text-xs text-muted-foreground'>
                +{customer.tags.length - 3}
              </span>
            )}
          </div>
        )}

        {/* Footer - Value & Quick Actions */}
        <div className='flex items-center justify-between pt-4 border-t'>
          <div>
            <p className='text-xs text-muted-foreground'>Total Value</p>
            <p className='text-lg font-bold text-foreground'>
              {formatCurrency(customer.totalValue || 0)}
            </p>
          </div>

          {/* Quick Action Buttons */}
          <div className='flex items-center gap-1'>
            {onEmailClick && (
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  onEmailClick();
                }}
                className='p-2 rounded-lg hover:bg-blue-100 dark:hover:bg-blue-900/30 text-blue-600 dark:text-blue-400 transition-colors'
                title='Send Email'
              >
                <Mail className='h-4 w-4' />
              </button>
            )}
            {onCallClick && customer.phone && (
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  onCallClick();
                }}
                className='p-2 rounded-lg hover:bg-emerald-100 dark:hover:bg-emerald-900/30 text-emerald-600 dark:text-emerald-400 transition-colors'
                title='Call'
              >
                <Phone className='h-4 w-4' />
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
