'use client';

import { useGetSchoolBusSummaryQuery } from '../api/schoolBusApi';

export function SchoolBusDashboardPage() {
  const { data, isLoading } = useGetSchoolBusSummaryQuery();
  const summary = data?.data;

  const cards = summary
    ? [
        ['Schools', summary.schoolCount],
        ['Parents', summary.parentCount],
        ['Students', summary.studentCount],
        ['Buses', summary.busCount],
        ['Pending requests', summary.pendingRequestCount],
        ['Assigned routes', summary.assignedRouteCount],
        ['Routes in progress', summary.inProgressRouteCount],
        ['Completed trips', summary.completedTripCount],
      ]
    : [];

  return (
    <div className='space-y-6'>
      <div>
        <h2 className='text-xl font-semibold'>Dashboard</h2>
        <p className='text-sm text-muted-foreground'>
          Overview of fleet, requests, and active tenant operations.
        </p>
      </div>
      {isLoading ? (
        <p className='text-sm text-muted-foreground'>Loading dashboard...</p>
      ) : (
        <div className='grid gap-4 md:grid-cols-2 xl:grid-cols-4'>
          {cards.map(([label, value]) => (
            <div key={label} className='rounded-2xl border bg-card p-5 shadow-sm'>
              <p className='text-sm text-muted-foreground'>{label}</p>
              <p className='mt-3 text-3xl font-semibold tracking-tight'>{value}</p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
