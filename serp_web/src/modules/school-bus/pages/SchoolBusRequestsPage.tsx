'use client';

import {
  useApproveTransportRequestMutation,
  useGetTransportRequestsQuery,
} from '../api/schoolBusApi';
import { SchoolBusSection } from '../components/SchoolBusSection';

export function SchoolBusRequestsPage() {
  const { data, isLoading } = useGetTransportRequestsQuery();
  const [approveTransportRequest, { isLoading: approving }] =
    useApproveTransportRequestMutation();

  return (
    <SchoolBusSection
      title='Transport Requests'
      description='Pending parent requests and approval queue.'
    >
      {isLoading ? (
        <p className='text-sm text-muted-foreground'>Loading requests...</p>
      ) : (
        <div className='space-y-3'>
          {data?.data.map((request) => (
            <div key={request.id} className='rounded-xl border p-4'>
              <div className='flex items-center justify-between gap-4'>
                <div>
                  <p className='font-medium'>
                    {request.requestType} • {request.school?.name || 'Unknown school'}
                  </p>
                  <p className='text-sm text-muted-foreground'>
                    {request.parentProfile?.fullName || 'Unknown parent'} •{' '}
                    {request.effectiveFrom}
                  </p>
                </div>
                <div className='flex items-center gap-3'>
                  <span className='text-xs font-semibold text-muted-foreground'>
                    {request.status}
                  </span>
                  {request.status === 'PENDING' ? (
                    <button
                      className='rounded-lg border px-3 py-2 text-sm font-medium hover:bg-muted disabled:opacity-50'
                      disabled={approving}
                      onClick={() => approveTransportRequest(request.id)}
                    >
                      Approve
                    </button>
                  ) : null}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </SchoolBusSection>
  );
}
