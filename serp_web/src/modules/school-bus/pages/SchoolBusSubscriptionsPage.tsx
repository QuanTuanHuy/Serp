'use client';

import { CalendarDays, PauseCircle, PlayCircle, StopCircle } from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/shared/components/ui';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';
import {
  useActivateSubscriptionMutation,
  useGetSubscriptionsQuery,
  usePauseSubscriptionMutation,
  useStopSubscriptionMutation,
} from '../api/schoolBusApi';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusPaginationBar } from '../components/SchoolBusPaginationBar';
import { SchoolBusScrollableTable } from '../components/SchoolBusScrollableTable';
import { SchoolBusSection } from '../components/SchoolBusSection';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { useSchoolBusPagination } from '../hooks/useSchoolBusPagination';
import { formatDate, getPageItems } from '../utils';

export function SchoolBusSubscriptionsPage() {
  const pagination = useSchoolBusPagination({
    page: 0,
    size: 10,
    sortBy: 'createdAt',
    sortDirection: 'DESC',
  });
  const { data, isLoading } = useGetSubscriptionsQuery(pagination.params);
  const [activateSubscription] = useActivateSubscriptionMutation();
  const [pauseSubscription] = usePauseSubscriptionMutation();
  const [stopSubscription] = useStopSubscriptionMutation();
  const subscriptions = getPageItems(data?.data);

  const mutate = async (action: 'activate' | 'pause' | 'stop', id: number) => {
    try {
      const response =
        action === 'activate'
          ? await activateSubscription(id).unwrap()
          : action === 'pause'
            ? await pauseSubscription(id).unwrap()
            : await stopSubscription(id).unwrap();
      toast.success(response.message || 'Subscription updated');
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to update subscription');
    }
  };

  return (
    <SchoolBusPageShell
      title='Subscriptions'
      description='Long-term student transport demand. Approved requests are converted into active subscriptions and route planning reads from this source.'
    >
      <div className='grid gap-4 md:grid-cols-3'>
        <SchoolBusMetricCard
          label='Subscriptions'
          value={data?.data?.totalElements ?? 0}
          hint='Active and historical service contracts'
          icon={CalendarDays}
          tone='info'
        />
        <SchoolBusMetricCard
          label='Active'
          value={subscriptions.filter((item) => item.status === 'ACTIVE').length}
          hint='Visible in route planning'
          icon={PlayCircle}
          tone='success'
        />
        <SchoolBusMetricCard
          label='Paused or stopped'
          value={subscriptions.filter((item) => item.status !== 'ACTIVE').length}
          hint='Excluded from route generation'
          icon={PauseCircle}
          tone='warning'
        />
      </div>

      <SchoolBusSection
        title='Subscription directory'
        description='Use status actions to control whether a student is eligible for daily routing.'
      >
        {isLoading || subscriptions.length === 0 ? (
          <SchoolBusEmptyState
            title={isLoading ? 'Loading subscriptions' : 'No subscriptions yet'}
            description='Approve a transport request to create subscriptions automatically, or create subscriptions through the API.'
            icon={CalendarDays}
          />
        ) : (
          <SchoolBusScrollableTable
            footer={
              <SchoolBusPaginationBar
                page={data?.data}
                onPageChange={pagination.setPage}
              />
            }
          >
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Code</TableHead>
                  <TableHead>Student</TableHead>
                  <TableHead>School</TableHead>
                  <TableHead>Trip option</TableHead>
                  <TableHead>Pickup / dropoff</TableHead>
                  <TableHead>Effective</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead className='text-right'>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {subscriptions.map((subscription) => (
                  <TableRow key={subscription.id}>
                    <TableCell className='font-semibold text-rose-700'>
                      {subscription.subscriptionCode}
                    </TableCell>
                    <TableCell>{subscription.studentName}</TableCell>
                    <TableCell>{subscription.schoolName}</TableCell>
                    <TableCell>{subscription.tripOption}</TableCell>
                    <TableCell>
                      {subscription.pickupPointName || '-'} /{' '}
                      {subscription.dropoffPointName || '-'}
                    </TableCell>
                    <TableCell>
                      {formatDate(subscription.effectiveFrom)} -{' '}
                      {formatDate(subscription.effectiveTo)}
                    </TableCell>
                    <TableCell>
                      <SchoolBusStatusBadge status={subscription.status} />
                    </TableCell>
                    <TableCell>
                      <div className='flex justify-end gap-2'>
                        <Button
                          size='sm'
                          variant='outline'
                          onClick={() => mutate('activate', subscription.id)}
                        >
                          <PlayCircle className='mr-1 h-4 w-4' />
                          Activate
                        </Button>
                        <Button
                          size='sm'
                          variant='outline'
                          onClick={() => mutate('pause', subscription.id)}
                        >
                          <PauseCircle className='mr-1 h-4 w-4' />
                          Pause
                        </Button>
                        <Button
                          size='sm'
                          variant='outline'
                          onClick={() => mutate('stop', subscription.id)}
                        >
                          <StopCircle className='mr-1 h-4 w-4' />
                          Stop
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </SchoolBusScrollableTable>
        )}
      </SchoolBusSection>
    </SchoolBusPageShell>
  );
}
