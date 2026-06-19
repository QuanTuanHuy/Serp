// Author: QuanTuanHuy, Description: Part of Serp Project

'use client';

import {
  ArrowLeft,
  MoreHorizontal,
  Edit,
  Trash2,
  RefreshCw,
  TrendingUp,
} from 'lucide-react';
import {
  Avatar,
  AvatarFallback,
  Badge,
  Button,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/shared/components/ui';
import { Building2, Calendar, CalendarClock, User } from 'lucide-react';
import { cn } from '@/shared/utils';
import { formatDate } from '../../utils';
import type { Opportunity } from '../../types';

interface OpportunityHeaderProps {
  opportunity: Opportunity;
  stageConfig: {
    label: string;
    color: string;
    bgColor: string;
  };
  typeConfig: {
    label: string;
    color: string;
  };
  isClosed: boolean;
  canReopen: boolean;
  onBack: () => void;
  onEdit: () => void;
  onChangeStage: () => void;
  onReopen: () => void;
  onDelete: () => void;
  onRequestMeeting?: () => void;
  canRequestMeeting?: boolean;
}

export const OpportunityHeader: React.FC<OpportunityHeaderProps> = ({
  opportunity,
  stageConfig,
  typeConfig,
  isClosed,
  canReopen,
  onBack,
  onEdit,
  onChangeStage,
  onReopen,
  onDelete,
  onRequestMeeting,
  canRequestMeeting = false,
}) => {
  return (
    <div className='flex items-start justify-between'>
      <div className='flex items-start gap-4'>
        <Button variant='outline' size='icon' onClick={onBack}>
          <ArrowLeft className='h-4 w-4' />
        </Button>
        <div>
          <div className='mb-2 flex items-center gap-3'>
            <h1 className='text-2xl font-bold text-foreground'>
              {opportunity.name || 'Opportunity'}
            </h1>
            <Badge className={cn(stageConfig.bgColor, stageConfig.color)}>
              {stageConfig.label}
            </Badge>
            <Badge className={typeConfig.color}>{typeConfig.label}</Badge>
          </div>
          <div className='flex flex-wrap items-center gap-4 text-sm text-muted-foreground'>
            <span className='flex items-center gap-1'>
              <Building2 className='h-4 w-4' />
              {opportunity.customerName ||
                `Account #${opportunity.accountId || ''}`}
            </span>
            <span className='flex items-center gap-1'>
              <User className='h-4 w-4' />
              {opportunity.assignedToName || 'Unassigned'}
            </span>
            <span className='flex items-center gap-1'>
              <Calendar className='h-4 w-4' />
              Close: {formatDate(opportunity.expectedCloseDate)}
            </span>
          </div>
        </div>
      </div>

      <div className='flex items-center gap-2'>
        {canReopen && (
          <Button variant='outline' onClick={onReopen}>
            <RefreshCw className='mr-2 h-4 w-4' />
            Reopen
          </Button>
        )}
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant='outline' size='icon'>
              <MoreHorizontal className='h-4 w-4' />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align='end'>
            <DropdownMenuItem onClick={onEdit}>
              <Edit className='mr-2 h-4 w-4' />
              Edit Opportunity
            </DropdownMenuItem>
            {!isClosed && (
              <DropdownMenuItem onClick={onChangeStage}>
                <TrendingUp className='mr-2 h-4 w-4' />
                Change Stage
              </DropdownMenuItem>
            )}
            {!isClosed && canRequestMeeting && onRequestMeeting && (
              <DropdownMenuItem onClick={onRequestMeeting}>
                <CalendarClock className='mr-2 h-4 w-4' />
                Request meeting
              </DropdownMenuItem>
            )}
            <DropdownMenuItem className='text-red-600' onClick={onDelete}>
              <Trash2 className='mr-2 h-4 w-4' />
              Delete Opportunity
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </div>
  );
};
