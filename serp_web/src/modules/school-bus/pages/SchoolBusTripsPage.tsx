'use client';

import { BusFront, CheckCircle2, MapPin, PlayCircle } from 'lucide-react';
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
  useArriveTripStopMutation,
  useCompleteTripMutation,
  useDepartTripStopMutation,
  useGetTripsQuery,
  useStartTripMutation,
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

export function SchoolBusTripsPage() {
  const pagination = useSchoolBusPagination({
    page: 0,
    size: 10,
    sortBy: 'serviceDate',
    sortDirection: 'DESC',
  });
  const { data, isLoading } = useGetTripsQuery(pagination.params);
  const [startTrip] = useStartTripMutation();
  const [arriveTripStop] = useArriveTripStopMutation();
  const [departTripStop] = useDepartTripStopMutation();
  const [completeTrip] = useCompleteTripMutation();
  const trips = getPageItems(data?.data);

  const call = async (label: string, action: () => Promise<any>) => {
    try {
      const response = await action();
      toast.success(response.message || `${label} completed`);
    } catch (error: any) {
      toast.error(error?.data?.message || `${label} failed`);
    }
  };

  return (
    <SchoolBusPageShell
      title='Trip execution'
      description='Trips are operational snapshots created from planned routes. Use this page to start trips and process stop arrival/departure order.'
    >
      <div className='grid gap-4 md:grid-cols-3'>
        <SchoolBusMetricCard
          label='Trips'
          value={data?.data?.totalElements ?? 0}
          hint='Route execution records'
          icon={BusFront}
          tone='info'
        />
        <SchoolBusMetricCard
          label='In progress'
          value={trips.filter((trip) => trip.status === 'IN_PROGRESS').length}
          hint='Trips currently operating'
          icon={PlayCircle}
          tone='success'
        />
        <SchoolBusMetricCard
          label='Completed'
          value={trips.filter((trip) => trip.status === 'COMPLETED').length}
          hint='Closed trip executions'
          icon={CheckCircle2}
          tone='default'
        />
      </div>

      <SchoolBusSection
        title='Trip control board'
        description='For each trip, only the next pending stop can be processed by the backend.'
      >
        {isLoading || trips.length === 0 ? (
          <SchoolBusEmptyState
            title={isLoading ? 'Loading trips' : 'No trips yet'}
            description='Create a trip from a dispatched route before execution can start.'
            icon={BusFront}
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
                  <TableHead>Trip</TableHead>
                  <TableHead>Route</TableHead>
                  <TableHead>Service</TableHead>
                  <TableHead>Assignment</TableHead>
                  <TableHead>Stops</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead className='text-right'>Operate</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {trips.map((trip) => {
                  const nextStop = trip.stops.find((stop) =>
                    ['PENDING', 'ARRIVED', 'BOARDING'].includes(stop.status)
                  );
                  return (
                    <TableRow key={trip.id}>
                      <TableCell className='font-semibold text-rose-700'>
                        {trip.tripCode}
                      </TableCell>
                      <TableCell>
                        <div>
                          <p>{trip.routeCode}</p>
                          <p className='text-xs text-slate-500'>{trip.routeName}</p>
                        </div>
                      </TableCell>
                      <TableCell>
                        {formatDate(trip.serviceDate)} - {trip.routeDirection}
                      </TableCell>
                      <TableCell>
                        {trip.busPlateNumber || 'No bus'} /{' '}
                        {trip.driverName || 'No driver'}
                      </TableCell>
                      <TableCell>{trip.stops.length}</TableCell>
                      <TableCell>
                        <SchoolBusStatusBadge status={trip.status} />
                      </TableCell>
                      <TableCell>
                        <div className='flex justify-end gap-2'>
                          <Button
                            size='sm'
                            variant='outline'
                            onClick={() =>
                              call('Start trip', () => startTrip(trip.id).unwrap())
                            }
                          >
                            Start
                          </Button>
                          <Button
                            size='sm'
                            variant='outline'
                            disabled={!nextStop}
                            onClick={() =>
                              nextStop &&
                              call('Arrive stop', () =>
                                arriveTripStop({
                                  tripId: trip.id,
                                  routeStopId: nextStop.routeStopId,
                                }).unwrap()
                              )
                            }
                          >
                            <MapPin className='mr-1 h-4 w-4' />
                            Arrive
                          </Button>
                          <Button
                            size='sm'
                            variant='outline'
                            disabled={!nextStop}
                            onClick={() =>
                              nextStop &&
                              call('Depart stop', () =>
                                departTripStop({
                                  tripId: trip.id,
                                  routeStopId: nextStop.routeStopId,
                                }).unwrap()
                              )
                            }
                          >
                            Depart
                          </Button>
                          <Button
                            size='sm'
                            onClick={() =>
                              call('Complete trip', () =>
                                completeTrip(trip.id).unwrap()
                              )
                            }
                          >
                            Complete
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          </SchoolBusScrollableTable>
        )}
      </SchoolBusSection>
    </SchoolBusPageShell>
  );
}
