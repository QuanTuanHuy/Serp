'use client';

import { useGetParentsQuery } from '../api/schoolBusApi';
import { SchoolBusSection } from '../components/SchoolBusSection';

export function SchoolBusParentsPage() {
  const { data, isLoading } = useGetParentsQuery();

  return (
    <SchoolBusSection
      title='Parents'
      description='Parent profiles linked to account users.'
    >
      {isLoading ? (
        <p className='text-sm text-muted-foreground'>Loading parents...</p>
      ) : (
        <div className='space-y-3'>
          {data?.data.map((parent) => (
            <div key={parent.id} className='rounded-xl border p-4'>
              <p className='font-medium'>{parent.fullName}</p>
              <p className='text-sm text-muted-foreground'>
                {parent.email || 'No email'} • {parent.phone || 'No phone'}
              </p>
            </div>
          ))}
        </div>
      )}
    </SchoolBusSection>
  );
}
