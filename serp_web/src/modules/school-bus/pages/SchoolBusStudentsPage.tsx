'use client';

import * as React from 'react';
import {
  AlertTriangle,
  CheckCircle2,
  GraduationCap,
  Pencil,
  Plus,
  School,
  Search,
  Trash2,
  Users,
} from 'lucide-react';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { toast } from 'sonner';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useCreateStudentMutation,
  useDeleteStudentMutation,
  useGetAllActiveSchoolPickupLinksQuery,
  useGetParentsQuery,
  useGetPickupPointsQuery,
  useGetSchoolsQuery,
  useGetStudentsQuery,
  useUpdateStudentMutation,
} from '../api/schoolBusApi';
import { SchoolBusFilterSelect } from '../components/SchoolBusFilterSelect';
import { SchoolBusDeleteDialog } from '../components/SchoolBusDeleteDialog';
import { StudentFormDialog } from '../components/SchoolBusMasterDataForms';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusPaginationBar } from '../components/SchoolBusPaginationBar';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusScrollableTable } from '../components/SchoolBusScrollableTable';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { useSchoolBusPagination } from '../hooks/useSchoolBusPagination';
import type { SchoolBusStudent } from '../types';
import { getPageItems, SCHOOL_BUS_OPTION_QUERY } from '../utils';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';

// ── Readiness helpers ─────────────────────────────────────────────────────────

type Readiness = 'ready' | 'missing-pickup' | 'missing-dropoff' | 'missing-parent';

function getStudentReadiness(student: SchoolBusStudent): Readiness {
  if (!student.parentProfileId) return 'missing-parent';
  if (!student.pickupPointId) return 'missing-pickup';
  if (!student.defaultDropoffPointId) return 'missing-dropoff';
  return 'ready';
}

const readinessConfig: Record<Readiness, { label: string; className: string }> = {
  ready: { label: 'Ready', className: 'bg-emerald-50 text-emerald-700 ring-emerald-200' },
  'missing-pickup': { label: 'Missing pickup', className: 'bg-amber-50 text-amber-700 ring-amber-200' },
  'missing-dropoff': { label: 'Missing drop-off', className: 'bg-amber-50 text-amber-700 ring-amber-200' },
  'missing-parent': { label: 'Missing parent', className: 'bg-red-50 text-red-700 ring-red-200' },
};

function ReadinessBadge({ readiness }: { readiness: Readiness }) {
  const cfg = readinessConfig[readiness];
  return (
    <span className={cn('inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-[10px] font-semibold ring-1', cfg.className)}>
      {readiness === 'ready' ? <CheckCircle2 className='h-3 w-3' /> : <AlertTriangle className='h-3 w-3' />}
      {cfg.label}
    </span>
  );
}

function UnassignedBadge() {
  return (
    <span className='inline-flex items-center rounded-full bg-slate-100 px-2 py-0.5 text-[10px] font-medium text-slate-500 ring-1 ring-slate-200'>
      Unassigned
    </span>
  );
}

// ── Main Page ─────────────────────────────────────────────────────────────────

export function SchoolBusStudentsPage() {
  const pagination = useSchoolBusPagination({ page: 0, size: 10, sortBy: 'fullName', sortDirection: 'ASC' });
  const { data, isLoading } = useGetStudentsQuery(pagination.params);
  const { data: schoolsData } = useGetSchoolsQuery({ ...SCHOOL_BUS_OPTION_QUERY, sortBy: 'name' });
  const { data: parentsData } = useGetParentsQuery({ ...SCHOOL_BUS_OPTION_QUERY, sortBy: 'fullName' });
  const { data: pickupPointsData } = useGetPickupPointsQuery({ ...SCHOOL_BUS_OPTION_QUERY, sortBy: 'name' });
  const [createStudent, { isLoading: creating }] = useCreateStudentMutation();
  const [updateStudent, { isLoading: updating }] = useUpdateStudentMutation();
  const [deleteStudent, { isLoading: deleting }] = useDeleteStudentMutation();

  const students = getPageItems(data?.data);
  const schools = getPageItems(schoolsData?.data);
  const parents = getPageItems(parentsData?.data);
  const pickupPoints = getPageItems(pickupPointsData?.data);

  const { data: allLinksData } = useGetAllActiveSchoolPickupLinksQuery();
  const schoolPickupPoints = allLinksData?.data ?? [];

  // ─── Stats ───────────────────────────────────────────────────────
  const uniqueSchools = new Set(students.map((s) => s.schoolName).filter(Boolean)).size;
  const linkedParents = new Set(students.map((s) => s.parentProfileId).filter(Boolean)).size;
  const missingTransport = students.filter((s) => !s.pickupPointId || !s.defaultDropoffPointId).length;

  // ─── Filter state ────────────────────────────────────────────────
  const [searchTerm, setSearchTerm] = React.useState('');
  const [filterSchool, setFilterSchool] = React.useState<string>('');
  const [filterReadiness, setFilterReadiness] = React.useState<string>('');
  const [filterStatus, setFilterStatus] = React.useState<string>('');

  // Debounced keyword search
  const [debouncedSearch, setDebouncedSearch] = React.useState('');
  React.useEffect(() => {
    const t = setTimeout(() => setDebouncedSearch(searchTerm), 300);
    return () => clearTimeout(t);
  }, [searchTerm]);
  React.useEffect(() => { pagination.setKeyword(debouncedSearch || ''); // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debouncedSearch]);

  // Client-side filters
  const filteredStudents = React.useMemo(() => {
    let result = students;
    if (filterSchool) result = result.filter((s) => s.schoolName === filterSchool);
    if (filterStatus === 'active') result = result.filter((s) => s.isActive !== false);
    if (filterStatus === 'inactive') result = result.filter((s) => s.isActive === false);
    if (filterReadiness === 'ready') result = result.filter((s) => getStudentReadiness(s) === 'ready');
    if (filterReadiness === 'missing') result = result.filter((s) => getStudentReadiness(s) !== 'ready');
    return result;
  }, [students, filterSchool, filterStatus, filterReadiness]);

  // Unique school names for filter
  const schoolNames = React.useMemo(() => [...new Set(students.map((s) => s.schoolName).filter(Boolean))].sort(), [students]);

  // ─── Dialog state ────────────────────────────────────────────────
  const [dialogOpen, setDialogOpen] = React.useState(false);
  const [editingStudent, setEditingStudent] = React.useState<SchoolBusStudent | null>(null);
  const [deletingStudent, setDeletingStudent] = React.useState<SchoolBusStudent | null>(null);

  const handleSave = async (values: any) => {
    try {
      const response = editingStudent
        ? await updateStudent({ id: editingStudent.id, body: values }).unwrap()
        : await createStudent(values).unwrap();
      toast.success(response.message || 'Student saved');
      setDialogOpen(false);
      setEditingStudent(null);
    } catch (error: any) { toast.error(error?.data?.message || 'Failed to save student'); }
  };

  const handleDelete = async () => {
    if (!deletingStudent) return;
    try {
      const response = await deleteStudent(deletingStudent.id).unwrap();
      toast.success(response.message || 'Student deleted');
      setDeletingStudent(null);
    } catch (error: any) { toast.error(error?.data?.message || 'Failed to delete student'); }
  };

  return (
    <>
      <SchoolBusPageShell
        title='Student roster'
        description='Operational student directory powering route planning, requests, and attendance.'
        breadcrumb={
          <SchoolBusBreadcrumb items={[
            { label: 'School Bus Ops', href: '/school-bus/dispatch' },
            { label: 'Students', current: true },
          ]} />
        }
        actions={
          <Button className='rounded-full' onClick={() => { setEditingStudent(null); setDialogOpen(true); }}>
            <Plus className='h-4 w-4' /> Add student
          </Button>
        }
      >
        {/* Stats */}
        <div className='grid gap-3 grid-cols-2 lg:grid-cols-4'>
          <SchoolBusMetricCard label='Students' value={students.length} icon={GraduationCap} tone='info' />
          <SchoolBusMetricCard label='Schools' value={uniqueSchools} icon={School} tone='success' />
          <SchoolBusMetricCard label='Linked parents' value={linkedParents} icon={Users} tone='default' />
          <SchoolBusMetricCard
            label='Missing transport'
            value={missingTransport}
            icon={AlertTriangle}
            tone={missingTransport > 0 ? 'warning' : 'success'}
            hint={missingTransport > 0 ? 'Students without pickup or drop-off' : 'All transport assigned'}
          />
        </div>

        {/* Search & Filter bar */}
        <div className='flex flex-wrap items-center gap-3 rounded-xl border border-slate-200 bg-white px-4 py-3 shadow-sm'>
          {/* Search */}
          <div className='relative flex-1 min-w-[200px]'>
            <Search className='absolute left-2.5 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-slate-400' />
            <input
              type='text'
              placeholder='Search by name or code...'
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className='w-full rounded-lg border border-slate-200 bg-slate-50 py-1.5 pl-8 pr-3 text-xs outline-none focus:border-slate-300 focus:ring-1 focus:ring-slate-200'
            />
          </div>
          <SchoolBusFilterSelect
            value={filterSchool}
            onChange={setFilterSchool}
            placeholder='All schools'
            icon={School}
            options={schoolNames.map((name) => ({ label: name, value: name }))}
          />
          <SchoolBusFilterSelect
            value={filterReadiness}
            onChange={setFilterReadiness}
            placeholder='All readiness'
            icon={AlertTriangle}
            options={[
              { label: 'Ready', value: 'ready' },
              { label: 'Missing transport', value: 'missing' },
            ]}
          />
          <SchoolBusFilterSelect
            value={filterStatus}
            onChange={setFilterStatus}
            placeholder='All statuses'
            options={[
              { label: 'Active', value: 'active' },
              { label: 'Inactive', value: 'inactive' },
            ]}
          />
        </div>

        {/* Table */}
        {isLoading ? (
          <p className='text-sm text-muted-foreground'>Loading students...</p>
        ) : filteredStudents.length === 0 ? (
          <SchoolBusEmptyState
            title='No students found'
            description={students.length === 0
              ? 'Add student master data so transport requests and attendance can become operational.'
              : 'No students match the current filters. Try adjusting your search criteria.'}
            icon={GraduationCap}
          />
        ) : (
          <SchoolBusScrollableTable footer={<SchoolBusPaginationBar page={data?.data} onPageChange={pagination.setPage} />}>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Student</TableHead>
                  <TableHead>School</TableHead>
                  <TableHead>Parent / Contact</TableHead>
                  <TableHead>Default pickup</TableHead>
                  <TableHead>Default drop-off</TableHead>
                  <TableHead>Readiness</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead className='text-right'>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredStudents.map((student) => (
                  <TableRow key={student.id}>
                    <TableCell>
                      <div>
                        <p className='font-medium'>{student.fullName}</p>
                        <p className='text-xs text-muted-foreground'>
                          {student.studentCode || 'No code'} — {[student.grade, student.className].filter(Boolean).join(' / ') || 'No grade'}
                        </p>
                      </div>
                    </TableCell>
                    <TableCell>
                      <span className='text-sm'>{student.schoolName || '—'}</span>
                    </TableCell>
                    <TableCell>
                      <div>
                        <p className='text-sm'>{student.parentProfileName || <UnassignedBadge />}</p>
                        {student.homeAddress && <p className='text-xs text-muted-foreground truncate max-w-[140px]'>{student.homeAddress}</p>}
                      </div>
                    </TableCell>
                    <TableCell>
                      {student.pickupPointName ? (
                        <span className='text-sm'>{student.pickupPointName}</span>
                      ) : (
                        <UnassignedBadge />
                      )}
                    </TableCell>
                    <TableCell>
                      {student.defaultDropoffPointName ? (
                        <span className='text-sm'>{student.defaultDropoffPointName}</span>
                      ) : (
                        <UnassignedBadge />
                      )}
                    </TableCell>
                    <TableCell>
                      <ReadinessBadge readiness={getStudentReadiness(student)} />
                    </TableCell>
                    <TableCell>
                      <SchoolBusStatusBadge status={student.isActive ? 'ACTIVE' : 'INACTIVE'} />
                    </TableCell>
                    <TableCell className='text-right'>
                      <div className='flex justify-end gap-2'>
                        <Button size='icon' variant='outline' onClick={() => { setEditingStudent(student); setDialogOpen(true); }}>
                          <Pencil className='h-4 w-4' />
                        </Button>
                        <Button size='icon' variant='outline' onClick={() => setDeletingStudent(student)}>
                          <Trash2 className='h-4 w-4' />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </SchoolBusScrollableTable>
        )}
      </SchoolBusPageShell>

      <StudentFormDialog
        open={dialogOpen}
        onOpenChange={(open) => { setDialogOpen(open); if (!open) setEditingStudent(null); }}
        initialData={editingStudent}
        schools={schools}
        parents={parents}
        pickupPoints={pickupPoints}
        schoolPickupPoints={schoolPickupPoints}
        isLoading={creating || updating}
        onSubmit={handleSave}
      />

      <SchoolBusDeleteDialog
        open={Boolean(deletingStudent)}
        onOpenChange={(open) => { if (!open) setDeletingStudent(null); }}
        title='Delete student'
        description='This will soft-delete the student record from active school-bus operations.'
        isLoading={deleting}
        onConfirm={handleDelete}
      />
    </>
  );
}
