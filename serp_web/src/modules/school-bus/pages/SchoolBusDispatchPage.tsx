'use client';

import {
  useGenerateGreedyPlanMutation,
  useGetRoutesQuery,
} from '../api/schoolBusApi';
import { SchoolBusSection } from '../components/SchoolBusSection';

export function SchoolBusDispatchPage() {
  const { data, isLoading } = useGetRoutesQuery();
  const [generateGreedyPlan, { isLoading: planning }] =
    useGenerateGreedyPlanMutation();

  return (
    <SchoolBusSection
      title='Dispatch Board'
      description='Routes, service dates, and planning actions.'
    >
      {isLoading ? (
        <p className='text-sm text-muted-foreground'>Loading routes...</p>
      ) : (
        <div className='space-y-3'>
          {data?.data.map((route) => (
            <div key={route.id} className='rounded-xl border p-4'>
              <div className='flex items-center justify-between gap-4'>
                <div>
                  <p className='font-medium'>
                    {route.routeCode} • {route.routeName}
                  </p>
                  <p className='text-sm text-muted-foreground'>
                    {route.serviceDate} • {route.shiftType} • {route.status}
                  </p>
                </div>
                <button
                  className='rounded-lg border px-3 py-2 text-sm font-medium hover:bg-muted disabled:opacity-50'
                  disabled={planning}
                  onClick={() => generateGreedyPlan(route.id)}
                >
                  Generate plan
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </SchoolBusSection>
  );
}
