'use client';

import { useGetStudentsQuery } from '../api/schoolBusApi';
import { SchoolBusSection } from '../components/SchoolBusSection';

export function SchoolBusStudentsPage() {
  const { data, isLoading } = useGetStudentsQuery();

  return (
    <SchoolBusSection
      title='Students'
      description='Student roster, family link, and assigned school.'
    >
      {isLoading ? (
        <p className='text-sm text-muted-foreground'>Loading students...</p>
      ) : (
        <div className='space-y-3'>
          {data?.data.map((student) => (
            <div key={student.id} className='rounded-xl border p-4'>
              <p className='font-medium'>{student.fullName}</p>
              <p className='text-sm text-muted-foreground'>
                {student.studentCode || 'No code'} • {student.grade || 'No grade'} •{' '}
                {student.school?.name || 'No school'}
              </p>
            </div>
          ))}
        </div>
      )}
    </SchoolBusSection>
  );
}
