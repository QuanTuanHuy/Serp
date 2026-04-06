'use client';

import {
  useGetAttendanceQuery,
  useGetTripHistoryQuery,
} from '../api/schoolBusApi';
import { SchoolBusSection } from '../components/SchoolBusSection';

export function SchoolBusAttendancePage() {
  const { data: attendanceData } = useGetAttendanceQuery();
  const { data: historyData } = useGetTripHistoryQuery();

  return (
    <div className='grid gap-6 xl:grid-cols-2'>
      <SchoolBusSection title='Attendance Feed'>
        <div className='space-y-3'>
          {attendanceData?.data.map((attendance) => (
            <div key={attendance.id} className='rounded-xl border p-4'>
              <p className='font-medium'>
                {attendance.student?.fullName || 'Unknown student'}
              </p>
              <p className='text-sm text-muted-foreground'>
                {attendance.attendanceType} • {attendance.recordedAt}
              </p>
            </div>
          ))}
        </div>
      </SchoolBusSection>
      <SchoolBusSection title='Trip History'>
        <div className='space-y-3'>
          {historyData?.data.map((trip) => (
            <div key={trip.id} className='rounded-xl border p-4'>
              <p className='font-medium'>{trip.routeCode}</p>
              <p className='text-sm text-muted-foreground'>
                {trip.serviceDate} • {trip.status}
              </p>
            </div>
          ))}
        </div>
      </SchoolBusSection>
    </div>
  );
}
