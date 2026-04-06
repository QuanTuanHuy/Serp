'use client';

import { useGetSchoolsQuery } from '../api/schoolBusApi';
import { SchoolBusSection } from '../components/SchoolBusSection';

export function SchoolBusSchoolsPage() {
  const { data, isLoading } = useGetSchoolsQuery();

  return (
    <SchoolBusSection
      title='Schools'
      description='Tenant-owned campuses and school contact points.'
    >
      {isLoading ? (
        <p className='text-sm text-muted-foreground'>Loading schools...</p>
      ) : (
        <div className='space-y-3'>
          {data?.data.map((school) => (
            <div key={school.id} className='rounded-xl border p-4'>
              <div className='flex items-center justify-between gap-4'>
                <div>
                  <p className='font-medium'>{school.name}</p>
                  <p className='text-sm text-muted-foreground'>
                    {school.code || 'No code'} • {school.contactPhone || 'No phone'}
                  </p>
                </div>
                <span className='text-xs text-muted-foreground'>
                  {school.active ? 'Active' : 'Inactive'}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}
    </SchoolBusSection>
  );
}
