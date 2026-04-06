'use client';

import { useGetSchoolBusReportQuery } from '../api/schoolBusApi';
import { SchoolBusSection } from '../components/SchoolBusSection';

export function SchoolBusReportsPage() {
  const { data, isLoading } = useGetSchoolBusReportQuery();
  const report = data?.data;

  return (
    <SchoolBusSection
      title='Operational Report'
      description='Snapshot of approvals, route execution, and audit activity.'
    >
      {isLoading || !report ? (
        <p className='text-sm text-muted-foreground'>Loading report...</p>
      ) : (
        <div className='grid gap-4 md:grid-cols-2 xl:grid-cols-4'>
          {[
            ['Total requests', report.totalRequests],
            ['Approved', report.approvedRequests],
            ['Rejected', report.rejectedRequests],
            ['Active routes', report.activeRoutes],
            ['Completed routes', report.completedRoutes],
            ['Attendance events', report.attendanceEvents],
            ['Audit events', report.auditEvents],
          ].map(([label, value]) => (
            <div key={label} className='rounded-xl border p-4'>
              <p className='text-sm text-muted-foreground'>{label}</p>
              <p className='mt-2 text-2xl font-semibold'>{value}</p>
            </div>
          ))}
        </div>
      )}
    </SchoolBusSection>
  );
}
