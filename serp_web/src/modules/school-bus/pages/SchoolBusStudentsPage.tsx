'use client';

import * as React from 'react';
import { GraduationCap, Pencil, Plus, School, Trash2, Users } from 'lucide-react';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { toast } from 'sonner';
import { Button } from '@/shared/components/ui';
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
import { SchoolBusDeleteDialog } from '../components/SchoolBusDeleteDialog';
import { StudentFormDialog } from '../components/SchoolBusMasterDataForms';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusPaginationBar } from '../components/SchoolBusPaginationBar';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusSection } from '../components/SchoolBusSection';
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

export function SchoolBusStudentsPage() {
  const pagination = useSchoolBusPagination({
    page: 0,
    size: 10,
    sortBy: 'fullName',
    sortDirection: 'ASC',
  });
  const { data, isLoading } = useGetStudentsQuery(pagination.params);
  const { data: schoolsData } = useGetSchoolsQuery({
    ...SCHOOL_BUS_OPTION_QUERY,
    sortBy: 'name',
  });
  const { data: parentsData } = useGetParentsQuery({
    ...SCHOOL_BUS_OPTION_QUERY,
    sortBy: 'fullName',
  });
  const { data: pickupPointsData } = useGetPickupPointsQuery({
    ...SCHOOL_BUS_OPTION_QUERY,
    sortBy: 'name',
  });
  const [createStudent, { isLoading: creating }] = useCreateStudentMutation();
  const [updateStudent, { isLoading: updating }] = useUpdateStudentMutation();
  const [deleteStudent, { isLoading: deleting }] = useDeleteStudentMutation();

  const students = getPageItems(data?.data);
  const schools = getPageItems(schoolsData?.data);
  const parents = getPageItems(parentsData?.data);
  const pickupPoints = getPageItems(pickupPointsData?.data);

  // Load all active school-pickup links for student form filtering
  const { data: allLinksData } = useGetAllActiveSchoolPickupLinksQuery();
  const schoolPickupPoints = allLinksData?.data ?? [];

  const uniqueSchools = new Set(
    students.map((student) => student.schoolName).filter(Boolean)
  ).size;
  const linkedParents = new Set(
    students.map((student) => student.parentProfileId).filter(Boolean)
  ).size;

  const [dialogOpen, setDialogOpen] = React.useState(false);
  const [editingStudent, setEditingStudent] =
    React.useState<SchoolBusStudent | null>(null);
  const [deletingStudent, setDeletingStudent] =
    React.useState<SchoolBusStudent | null>(null);

  const handleSave = async (values: any) => {
    try {
      const response = editingStudent
        ? await updateStudent({ id: editingStudent.id, body: values }).unwrap()
        : await createStudent(values).unwrap();
      toast.success(response.message || 'Student saved');
      setDialogOpen(false);
      setEditingStudent(null);
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to save student');
    }
  };

  const handleDelete = async () => {
    if (!deletingStudent) {
      return;
    }

    try {
      const response = await deleteStudent(deletingStudent.id).unwrap();
      toast.success(response.message || 'Student deleted');
      setDeletingStudent(null);
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to delete student');
    }
  };

  return (
    <>
      <SchoolBusPageShell
        title='Student roster'
        description='Students sit at the center of demand, route planning, and attendance. V1 now supports direct roster maintenance.'
        breadcrumb={
          <SchoolBusBreadcrumb
            items={[
              { label: 'School Bus Ops', href: '/school-bus/dispatch' },
              { label: 'Students', current: true },
            ]}
          />
        }
        actions={
          <Button
            className='rounded-full'
            onClick={() => {
              setEditingStudent(null);
              setDialogOpen(true);
            }}
          >
            <Plus className='h-4 w-4' />
            Add student
          </Button>
        }
      >
        <div className='grid gap-4 md:grid-cols-3'>
          <SchoolBusMetricCard
            label='Students managed'
            value={students.length}
            hint='Student records available in this tenant'
            icon={GraduationCap}
            tone='info'
          />
          <SchoolBusMetricCard
            label='Schools represented'
            value={uniqueSchools}
            hint='Distinct schools linked to student records'
            icon={School}
            tone='success'
          />
          <SchoolBusMetricCard
            label='Linked parents'
            value={linkedParents}
            hint='Parent profiles associated with listed students'
            icon={Users}
            tone='default'
          />
        </div>

        <SchoolBusSection
          title='Student directory'
          description='Create, edit, and soft-delete students without leaving the module.'
        >
          {isLoading ? (
            <p className='text-sm text-muted-foreground'>Loading students...</p>
          ) : students.length === 0 ? (
            <SchoolBusEmptyState
              title='No students are available yet'
              description='Add student master data so transport requests and attendance can become operational.'
              icon={GraduationCap}
            />
          ) : (
            <SchoolBusScrollableTable
              footer={
                <SchoolBusPaginationBar
                  page={data?.data}
                  onPageChange={pagination.setPage}
                />
              }
            >
              <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Student</TableHead>
                  <TableHead>School</TableHead>
                  <TableHead>Parent</TableHead>
                  <TableHead>Default pickup</TableHead>
                  <TableHead>Default drop-off</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead className='text-right'>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {students.map((student) => (
                  <TableRow key={student.id}>
                    <TableCell>
                      <div>
                        <p className='font-medium'>{student.fullName}</p>
                        <p className='text-xs text-muted-foreground'>
                          {student.studentCode || 'No code'} —{' '}
                          {[student.grade, student.className].filter(Boolean).join(' / ') || 'No grade'}
                        </p>
                      </div>
                    </TableCell>
                    <TableCell>{student.schoolName}</TableCell>
                    <TableCell>
                      <div>
                        <p>{student.parentProfileName}</p>
                        <p className='text-xs text-muted-foreground'>
                          {student.homeAddress || 'No address'}
                        </p>
                      </div>
                    </TableCell>
                    <TableCell>{student.pickupPointName || 'Unassigned'}</TableCell>
                    <TableCell>{student.defaultDropoffPointName || 'Unassigned'}</TableCell>
                    <TableCell>
                      <SchoolBusStatusBadge
                        status={student.isActive ? 'ACTIVE' : 'INACTIVE'}
                      />
                    </TableCell>
                    <TableCell className='text-right'>
                      <div className='flex justify-end gap-2'>
                        <Button
                          size='icon'
                          variant='outline'
                          onClick={() => {
                            setEditingStudent(student);
                            setDialogOpen(true);
                          }}
                        >
                          <Pencil className='h-4 w-4' />
                        </Button>
                        <Button
                          size='icon'
                          variant='outline'
                          onClick={() => setDeletingStudent(student)}
                        >
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
        </SchoolBusSection>
      </SchoolBusPageShell>

      <StudentFormDialog
        open={dialogOpen}
        onOpenChange={(open) => {
          setDialogOpen(open);
          if (!open) {
            setEditingStudent(null);
          }
        }}
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
        onOpenChange={(open) => {
          if (!open) {
            setDeletingStudent(null);
          }
        }}
        title='Delete student'
        description='This will soft-delete the student record from active school-bus operations.'
        isLoading={deleting}
        onConfirm={handleDelete}
      />
    </>
  );
}
