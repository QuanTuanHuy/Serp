// Author: QuanTuanHuy, Description: Part of Serp Project

'use client';

import Link from 'next/link';
import {
  ArrowLeft,
  MoreHorizontal,
  Edit,
  Trash2,
  Trophy,
  XCircle,
  RefreshCw,
  TrendingUp,
  MessageSquare,
  ExternalLink,
} from 'lucide-react';
import {
  Avatar,
  AvatarFallback,
  Badge,
  Button,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/shared/components/ui';
import { Building2, Calendar, User } from 'lucide-react';
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
  onMarkAsWon: () => void;
  onMarkAsLost: () => void;
  onReopen: () => void;
  onDelete: () => void;
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
  onMarkAsWon,
  onMarkAsLost,
  onReopen,
  onDelete,
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
        {!isClosed && (
          <>
            <Button
              variant='outline'
              className='text-green-600 border-green-200 hover:bg-green-50 dark:border-green-800 dark:hover:bg-green-950'
              onClick={onMarkAsWon}
            >
              <Trophy className='mr-2 h-4 w-4' />
              Mark as Won
            </Button>
            <Button
              variant='outline'
              className='text-red-600 border-red-200 hover:bg-red-50 dark:border-red-800 dark:hover:bg-red-950'
              onClick={onMarkAsLost}
            >
              <XCircle className='mr-2 h-4 w-4' />
              Mark as Lost
            </Button>
          </>
        )}
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
            <DropdownMenuItem disabled>
              <MessageSquare className='mr-2 h-4 w-4' />
              Notes are currently read-only
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem disabled>
              <ExternalLink className='mr-2 h-4 w-4' />
              Export PDF
            </DropdownMenuItem>
            <DropdownMenuSeparator />
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
