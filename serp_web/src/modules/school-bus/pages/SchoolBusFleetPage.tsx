'use client';

import {
  useGetAttendantsQuery,
  useGetBusesQuery,
  useGetDriversQuery,
} from '../api/schoolBusApi';
import { SchoolBusSection } from '../components/SchoolBusSection';

export function SchoolBusFleetPage() {
  const { data: busesData } = useGetBusesQuery();
  const { data: driversData } = useGetDriversQuery();
  const { data: attendantsData } = useGetAttendantsQuery();

  return (
    <div className='grid gap-6 xl:grid-cols-3'>
      <SchoolBusSection title='Buses'>
        <div className='space-y-3'>
          {busesData?.data.map((bus) => (
            <div key={bus.id} className='rounded-xl border p-4'>
              <p className='font-medium'>{bus.plateNumber}</p>
              <p className='text-sm text-muted-foreground'>
                {bus.busType || 'Standard'} • Capacity {bus.capacity} • {bus.status}
              </p>
            </div>
          ))}
        </div>
      </SchoolBusSection>
      <SchoolBusSection title='Drivers'>
        <div className='space-y-3'>
          {driversData?.data.map((driver) => (
            <div key={driver.id} className='rounded-xl border p-4'>
              <p className='font-medium'>{driver.fullName}</p>
              <p className='text-sm text-muted-foreground'>
                {driver.licenseNumber || 'No license'} • {driver.status}
              </p>
            </div>
          ))}
        </div>
      </SchoolBusSection>
      <SchoolBusSection title='Attendants'>
        <div className='space-y-3'>
          {attendantsData?.data.map((attendant) => (
            <div key={attendant.id} className='rounded-xl border p-4'>
              <p className='font-medium'>{attendant.fullName}</p>
              <p className='text-sm text-muted-foreground'>
                {attendant.phone || 'No phone'} • {attendant.status}
              </p>
            </div>
          ))}
        </div>
      </SchoolBusSection>
    </div>
  );
}
