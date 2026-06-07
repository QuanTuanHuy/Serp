'use client';

import * as React from 'react';
import { History } from 'lucide-react';
import {
  Button,
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from '@/shared/components/ui';
import {
  useGetSubscriptionHistoryQuery,
  useGetSubscriptionPausePeriodsQuery,
} from '../../api/schoolBusApi';
import { SchoolBusTimeline } from './SchoolBusTimeline';
import { mapSubscriptionHistoryToTimeline, mapPausePeriodsToTimeline } from './mappers';
import type { TimelineEvent } from './SchoolBusTimeline';

interface SubscriptionHistoryDialogProps {
  subscriptionId: number;
  subscriptionCode: string;
}

export function SubscriptionHistoryDialog({
  subscriptionId,
  subscriptionCode,
}: SubscriptionHistoryDialogProps) {
  const [open, setOpen] = React.useState(false);
  const { data: historyData, isLoading: historyLoading, isError: historyError } =
    useGetSubscriptionHistoryQuery(subscriptionId, { skip: !open });
  const { data: pauseData, isLoading: pauseLoading } =
    useGetSubscriptionPausePeriodsQuery(subscriptionId, { skip: !open });

  const events = React.useMemo<TimelineEvent[]>(() => {
    const historyEvents = mapSubscriptionHistoryToTimeline(historyData?.data ?? []);
    const pauseEvents = mapPausePeriodsToTimeline(pauseData?.data ?? []);
    return [...historyEvents, ...pauseEvents].sort(
      (a, b) => new Date(b.occurredAt).getTime() - new Date(a.occurredAt).getTime()
    );
  }, [historyData, pauseData]);

  return (
    <>
      <Button
        size='sm'
        variant='outline'
        onClick={() => setOpen(true)}
        title='View history'
      >
        <History className='mr-1 h-3.5 w-3.5' />
        History
      </Button>
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent className='max-w-lg'>
          <DialogHeader>
            <DialogTitle>Subscription History — {subscriptionCode}</DialogTitle>
            <DialogDescription>
              Complete history of changes and pause periods for this subscription.
            </DialogDescription>
          </DialogHeader>
          <SchoolBusTimeline
            events={events}
            mode='full'
            groupByDate
            isLoading={historyLoading || pauseLoading}
            isError={historyError}
            maxHeight='480px'
          />
        </DialogContent>
      </Dialog>
    </>
  );
}
