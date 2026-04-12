'use client';

import { Activity, BarChart3, CheckCircle2, FileText } from 'lucide-react';
import { useGetSchoolBusReportQuery } from '../api/schoolBusApi';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusSection } from '../components/SchoolBusSection';
import { schoolBusUi } from '../theme';

export function SchoolBusReportsPage() {
  const { data, isLoading } = useGetSchoolBusReportQuery();
  const report = data?.data;

  return (
    <SchoolBusPageShell
      title='Operational reporting'
      description='Sprint V1 keeps reporting intentionally lean: one clear summary surface for approvals, route activity, attendance volume, and audit volume.'
    >
      {isLoading || !report ? (
        <SchoolBusEmptyState
          title='Report summary is loading'
          description='The operations summary endpoint is still loading. Once available, sprint 1 reporting will surface here.'
          icon={BarChart3}
        />
      ) : (
        <div className='space-y-6'>
          <div className='grid gap-4 md:grid-cols-2 xl:grid-cols-4'>
            <SchoolBusMetricCard
              label='Total requests'
              value={report.totalRequests}
              hint='Transport demand records in scope'
              icon={FileText}
              tone='info'
            />
            <SchoolBusMetricCard
              label='Approved requests'
              value={report.approvedRequests}
              hint='Requests released into operations'
              icon={CheckCircle2}
              tone='success'
            />
            <SchoolBusMetricCard
              label='Active routes'
              value={report.activeRoutes}
              hint='Routes still moving through execution'
              icon={Activity}
              tone='warning'
            />
            <SchoolBusMetricCard
              label='Audit events'
              value={report.auditEvents}
              hint='Recorded operational trail in the backend'
              icon={BarChart3}
              tone='default'
            />
          </div>

          <div className='grid gap-6 xl:grid-cols-[0.9fr_1.1fr]'>
            <SchoolBusSection
              title='KPI breakdown'
              description='The current backend report endpoint already gives enough signal for V1 operational review.'
            >
              <div className='grid gap-3 md:grid-cols-2'>
                {[
                  ['Total requests', report.totalRequests],
                  ['Approved requests', report.approvedRequests],
                  ['Rejected requests', report.rejectedRequests],
                  ['Active routes', report.activeRoutes],
                  ['Completed routes', report.completedRoutes],
                  ['Attendance events', report.attendanceEvents],
                  ['Audit events', report.auditEvents],
                ].map(([label, value]) => (
                  <div key={label} className={schoolBusUi.interactiveCard}>
                    <p className='text-sm text-slate-500'>{label}</p>
                    <p className='mt-2 text-2xl font-semibold text-slate-950'>
                      {value}
                    </p>
                  </div>
                ))}
              </div>
            </SchoolBusSection>

            <SchoolBusSection
              title='What this report supports in V1'
              description='The reporting surface is intentionally compact and tied to the implemented backend flows.'
            >
              <div className='space-y-3'>
                <div className={schoolBusUi.interactiveCard}>
                  <p className='font-medium'>Operational review</p>
                  <p className='mt-2 text-sm leading-6 text-muted-foreground'>
                    Dispatchers can quickly review demand, approvals, route volume,
                    and attendance intensity without exporting data.
                  </p>
                </div>
                <div className={schoolBusUi.interactiveCard}>
                  <p className='font-medium'>Management snapshot</p>
                  <p className='mt-2 text-sm leading-6 text-muted-foreground'>
                    School-bus admins get a compact KPI layer suitable for V1
                    operational steering and review.
                  </p>
                </div>
                <div className={schoolBusUi.interactiveCard}>
                  <p className='font-medium'>Known limit</p>
                  <p className='mt-2 text-sm leading-6 text-muted-foreground'>
                    There is no date-range filtering, route drill-down, or export yet.
                    Those belong in sprint 2 reporting.
                  </p>
                </div>
              </div>
            </SchoolBusSection>
          </div>
        </div>
      )}
    </SchoolBusPageShell>
  );
}
