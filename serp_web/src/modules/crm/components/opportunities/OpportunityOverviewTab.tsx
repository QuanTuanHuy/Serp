// Author: QuanTuanHuy, Description: Part of Serp Project

'use client';

import {
  Card,
  CardContent,
  CardHeader,
  Avatar,
  AvatarFallback,
} from '@/shared/components/ui';
import { Building2 } from 'lucide-react';
import { cn } from '@/shared/utils';
import { formatDate, formatDateTime } from '../../utils';
import type { Opportunity } from '../../types';

interface OpportunityOverviewTabProps {
  opportunity: Opportunity;
  stageLabel: string;
  estimatedValue: number;
  weightedValue: number;
  probability: number;
  daysInPipeline: number;
  daysUntilClose: number;
  formatCurrency: (value?: number) => string;
}

export const OpportunityOverviewTab: React.FC<OpportunityOverviewTabProps> = ({
  opportunity,
  stageLabel,
  estimatedValue,
  weightedValue,
  probability,
  daysInPipeline,
  daysUntilClose,
  formatCurrency,
}) => {
  const daysToCloseDisplay =
    daysUntilClose < 0
      ? `${Math.abs(daysUntilClose)} overdue`
      : String(daysUntilClose);
  return (
    <div className='grid grid-cols-1 gap-6 lg:grid-cols-3'>
      <div className='space-y-6 lg:col-span-2'>
        <Card>
          <CardHeader>
            <h3 className='font-semibold'>Description</h3>
          </CardHeader>
          <CardContent>
            <p className='text-muted-foreground'>
              {opportunity.description || 'No description provided.'}
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <h3 className='font-semibold'>Key Details</h3>
          </CardHeader>
          <CardContent>
            <div className='grid grid-cols-1 gap-4 md:grid-cols-2'>
              <div>
                <label className='text-sm font-medium text-muted-foreground'>
                  Account
                </label>
                <p className='flex items-center gap-2 font-medium'>
                  <Building2 className='h-4 w-4 text-muted-foreground' />
                  {opportunity.customerName ||
                    `Account #${opportunity.accountId || ''}`}
                </p>
              </div>
              <div>
                <label className='text-sm font-medium text-muted-foreground'>
                  Assigned To
                </label>
                <p className='flex items-center gap-2 font-medium'>
                  <Avatar className='h-5 w-5'>
                    <AvatarFallback className='text-xs'>
                      {(opportunity.assignedToName || 'Unassigned')
                        .split(' ')
                        .map((part) => part[0])
                        .join('')}
                    </AvatarFallback>
                  </Avatar>
                  {opportunity.assignedToName || 'Unassigned'}
                </p>
              </div>
              <div>
                <label className='text-sm font-medium text-muted-foreground'>
                  Expected Close Date
                </label>
                <p className='font-medium'>
                  {formatDate(opportunity.expectedCloseDate)}
                </p>
              </div>
              <div>
                <label className='text-sm font-medium text-muted-foreground'>
                  Actual Close Date
                </label>
                <p className='font-medium'>
                  {formatDate(opportunity.actualCloseDate)}
                </p>
              </div>
              <div>
                <label className='text-sm font-medium text-muted-foreground'>
                  Lead ID
                </label>
                <p className='font-medium'>
                  {opportunity.leadId || 'No linked lead'}
                </p>
              </div>
              <div>
                <label className='text-sm font-medium text-muted-foreground'>
                  Days in Pipeline
                </label>
                <p className='font-medium'>{daysInPipeline}</p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <h3 className='font-semibold'>Notes</h3>
          </CardHeader>
          <CardContent>
            <p className='text-muted-foreground'>
              {opportunity.notes || 'No notes available.'}
            </p>
          </CardContent>
        </Card>
      </div>

      <div className='space-y-6'>
        <Card>
          <CardHeader>
            <h3 className='font-semibold'>Opportunity Summary</h3>
          </CardHeader>
          <CardContent className='space-y-4'>
            <div className='flex items-center justify-between'>
              <span className='text-sm text-muted-foreground'>Stage</span>
              <span className='font-medium'>{stageLabel}</span>
            </div>
            <div className='flex items-center justify-between'>
              <span className='text-sm text-muted-foreground'>
                Estimated Value
              </span>
              <span className='font-medium'>
                {formatCurrency(estimatedValue)}
              </span>
            </div>
            <div className='flex items-center justify-between'>
              <span className='text-sm text-muted-foreground'>
                Weighted Value
              </span>
              <span className='font-medium'>
                {formatCurrency(weightedValue)}
              </span>
            </div>
            <div className='flex items-center justify-between'>
              <span className='text-sm text-muted-foreground'>
                Actual Value
              </span>
              <span className='font-medium'>
                {formatCurrency(opportunity.actualValue)}
              </span>
            </div>
            <div className='flex items-center justify-between'>
              <span className='text-sm text-muted-foreground'>Probability</span>
              <span className='font-medium'>{probability}%</span>
            </div>
            <div className='flex items-center justify-between'>
              <span className='text-sm text-muted-foreground'>
                Days to Close
              </span>
              <span
                className={cn(
                  'font-medium',
                  daysUntilClose < 0 && 'text-destructive'
                )}
              >
                {daysToCloseDisplay}
              </span>
            </div>
            {opportunity.lostReason && (
              <div>
                <span className='text-sm text-muted-foreground'>
                  Loss Reason
                </span>
                <p className='mt-1 text-sm font-medium'>
                  {opportunity.lostReason}
                </p>
              </div>
            )}
            {opportunity.reopenReason && (
              <div>
                <span className='text-sm text-muted-foreground'>
                  Reopen Reason
                </span>
                <p className='mt-1 text-sm font-medium'>
                  {opportunity.reopenReason}
                </p>
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <h3 className='font-semibold'>System Metadata</h3>
          </CardHeader>
          <CardContent className='space-y-3'>
            <div>
              <span className='text-sm text-muted-foreground'>Created</span>
              <p className='text-sm font-medium'>
                {formatDateTime(opportunity.createdAt)}
              </p>
            </div>
            <div>
              <span className='text-sm text-muted-foreground'>
                Last Updated
              </span>
              <p className='text-sm font-medium'>
                {formatDateTime(opportunity.updatedAt)}
              </p>
            </div>
            <div>
              <span className='text-sm text-muted-foreground'>Created By</span>
              <p className='text-sm font-medium'>
                {String(opportunity.customFields?.createdBy || 'Not available')}
              </p>
            </div>
            <div>
              <span className='text-sm text-muted-foreground'>Updated By</span>
              <p className='text-sm font-medium'>
                {String(opportunity.customFields?.updatedBy || 'Not available')}
              </p>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};
