'use client';

import * as React from 'react';
import {
  AlertTriangle,
  CheckCircle2,
  Eye,
  GraduationCap,
  Pencil,
  Plus,
  Search,
  Trash2,
  User,
  Users,
} from 'lucide-react';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { toast } from 'sonner';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useCreateStudentMutation,
  useDeleteStudentMutation,
  useGetStudentByIdQuery,
  useGetStudentsQuery,
  useUpdateStudentMutation,
  useGetSchoolDropdownOptionsQuery,
  useGetParentDropdownOptionsQuery,
} from '../api/schoolBusApi';
import { SchoolBusSelect } from '../components/ui/SchoolBusSelect';
import { SchoolBusDeleteDialog } from '../components/SchoolBusDeleteDialog';
import { StudentFormDialog } from '../components/SchoolBusMasterDataForms';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusReadOnlyDetailDialog } from '../components/SchoolBusReadOnlyDetailDialog';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { SchoolBusDataTable } from '../components/ui/SchoolBusDataTable';
import type { SchoolBusTableColumn } from '../components/ui/SchoolBusDataTable';
import { useSchoolBusPagination } from '../hooks/useSchoolBusPagination';
import type { SchoolBusStudent, SchoolBusStudentUpsertRequest } from '../types';
import { formatDate, getPageItems, SCHOOL_BUS_PAGE_QUERY_OPTIONS } from '../utils';
import { useSchoolBusAccess } from '../security/schoolBusAccess';
import { genderLabel, getLabel, profileStatusLabel } from '../schoolBusLabels';

function UnassignedBadge() {
  return (
    <span className='inline-flex items-center rounded-full bg-slate-100 px-2 py-0.5 text-[10px] font-medium text-slate-500 ring-1 ring-slate-200'>
      Chưa gán
    </span>
  );
}

function formatStudentDate(value?: string | null) {
  return value ? formatDate(value) : '-';
}

// -- Main Page -----------------------------------------------------------------

export function SchoolBusStudentsPage() {
  const access = useSchoolBusAccess();
  const pagination = useSchoolBusPagination({
    page: 0,
    size: 10,
    sortBy: 'fullName',
    sortDirection: 'ASC',
  });
  const [dialogOpen, setDialogOpen] = React.useState(false);
  const [editingStudentId, setEditingStudentId] = React.useState<number | null>(
    null
  );
  const [viewingStudentId, setViewingStudentId] = React.useState<number | null>(
    null
  );
  const [deletingStudent, setDeletingStudent] =
    React.useState<SchoolBusStudent | null>(null);

  const { data, isLoading } = useGetStudentsQuery(
    pagination.params,
    SCHOOL_BUS_PAGE_QUERY_OPTIONS
  );
  const { data: schoolsData } = useGetSchoolDropdownOptionsQuery(undefined, {
    skip: !dialogOpen,
  });
  const { data: parentsData } = useGetParentDropdownOptionsQuery(undefined, {
    skip: !dialogOpen || access.isParentOnly,
  });
  const { data: editingStudentData } = useGetStudentByIdQuery(
    editingStudentId || 0,
    { skip: !dialogOpen || !editingStudentId }
  );
  const {
    data: viewingStudentData,
    isFetching: loadingViewingStudent,
    isError: viewingStudentError,
  } = useGetStudentByIdQuery(viewingStudentId || 0, {
    skip: !viewingStudentId,
  });
  const [createStudent, { isLoading: creating }] = useCreateStudentMutation();
  const [updateStudent, { isLoading: updating }] = useUpdateStudentMutation();
  const [deleteStudent, { isLoading: deleting }] = useDeleteStudentMutation();

  const students = getPageItems(data?.data);
  const schools = schoolsData?.data || [];
  const parents = parentsData?.data || [];

  // --- Stats -------------------------------------------------------
  const uniqueSchools = new Set(
    students.map((s) => s.schoolName).filter(Boolean)
  ).size;
  const linkedParents = new Set(
    students.map((s) => s.parentProfileId).filter(Boolean)
  ).size;
  const activeStudents = students.filter((s) => s.isActive !== false).length;

  // --- Filter state ------------------------------------------------
  const [searchTerm, setSearchTerm] = React.useState('');
  const [filterSchool, setFilterSchool] = React.useState<string>('');
  const [filterStatus, setFilterStatus] = React.useState<string>('');

  // Debounced keyword search
  const [debouncedSearch, setDebouncedSearch] = React.useState('');
  React.useEffect(() => {
    const t = setTimeout(() => setDebouncedSearch(searchTerm), 300);
    return () => clearTimeout(t);
  }, [searchTerm]);
  React.useEffect(() => {
    pagination.setKeyword(debouncedSearch || ''); // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debouncedSearch]);

  // Client-side filters
  const filteredStudents = React.useMemo(() => {
    let result = students;
    if (filterSchool)
      result = result.filter((s) => s.schoolName === filterSchool);
    if (filterStatus === 'active')
      result = result.filter((s) => s.isActive !== false);
    if (filterStatus === 'inactive')
      result = result.filter((s) => s.isActive === false);
    return result;
  }, [students, filterSchool, filterStatus]);

  // Unique school names for filter
  const schoolNames = React.useMemo(
    () =>
      [...new Set(students.map((s) => s.schoolName).filter(Boolean))].sort(),
    [students]
  );

  // --- Dialog state ------------------------------------------------


  const editingStudent = editingStudentData?.data || null;
  const viewingStudent = viewingStudentData?.data || null;

  const handleSave = async (values: SchoolBusStudentUpsertRequest) => {
    try {
      const response = editingStudentId
        ? await updateStudent({ id: editingStudentId, body: values }).unwrap()
        : await createStudent(values).unwrap();
      toast.success(response.message || 'Đã lưu học sinh');
      setDialogOpen(false);
      setEditingStudentId(null);
    } catch {
      toast.error('Không thể lưu học sinh');
    }
  };

  const handleDelete = async () => {
    if (!deletingStudent) return;
    try {
      const response = await deleteStudent(deletingStudent.id).unwrap();
      toast.success(response.message || 'Đã xóa học sinh');
      setDeletingStudent(null);
    } catch {
      toast.error('Không thể xóa học sinh');
    }
  };

  const studentColumns: SchoolBusTableColumn<SchoolBusStudent>[] = [
    {
      key: 'student',
      header: 'Học sinh',
      className: 'pl-6',
      headerClassName: 'pl-6',
      render: (student) => (
        <div className='flex items-center gap-3'>
          <div className='flex h-8 w-8 shrink-0 items-center justify-center rounded-xl bg-violet-50 text-[#7C3AED] ring-1 ring-inset ring-[#7C3AED]/10'>
            <User className='h-4.5 w-4.5' />
          </div>
          <div className='flex flex-col min-w-0'>
            <p className='font-semibold text-slate-900 truncate'>
              {student.fullName}
            </p>
            <p className='text-xs text-muted-foreground flex flex-wrap items-center gap-1.5 mt-0.5'>
              {student.studentCode ? (
                <span className='inline-flex items-center rounded-md bg-violet-50 px-1.5 py-0.5 text-[10px] font-semibold text-violet-700 ring-1 ring-inset ring-violet-700/10'>
                  {student.studentCode}
                </span>
              ) : (
                <span className='text-slate-400'>Chưa có mã</span>
              )}
              <span className='text-slate-300'>-</span>
              <span>
                {[student.grade, student.className]
                  .filter(Boolean)
                  .join(' / ') || 'Chưa có khối/lớp'}
              </span>
            </p>
          </div>
        </div>
      ),
    },
    {
      key: 'school',
      header: 'Trường học',
      render: (student) => (
        <span className='font-medium text-slate-700'>
          {student.schoolName || 'Chưa có'}
        </span>
      ),
    },
    {
      key: 'parent',
      header: 'Phụ huynh / Liên hệ',
      render: (student) => (
        <div>
          <p className='text-sm font-medium text-slate-700'>
            {student.parentProfileName || <UnassignedBadge />}
          </p>
          {student.homeAddress && (
            <p className='text-xs text-muted-foreground truncate max-w-[160px]'>
              {student.homeAddress}
            </p>
          )}
        </div>
      ),
    },
    {
      key: 'pickup',
      header: 'Điểm đón mặc định',
      render: (student) => student.pickupPointName || '-',
    },
    {
      key: 'dropoff',
      header: 'Điểm trả mặc định',
      render: (student) => student.defaultDropoffPointName || '-',
    },
    {
      key: 'status',
      header: 'Trạng thái',
      render: (student) => (
        <SchoolBusStatusBadge
          status={student.isActive ? 'ACTIVE' : 'INACTIVE'}
        />
      ),
    },
    {
      key: 'actions',
      header: 'Thao tác',
      className: 'pr-6 text-right',
      headerClassName: 'pr-6 text-right',
      render: (student) => (
        <div className='flex justify-end gap-2'>
          <Button
            size='icon'
            variant='outline'
            className='h-8 w-8 rounded-lg border-slate-200 text-slate-500 hover:bg-slate-50 hover:text-slate-700'
            onClick={() => {
              setViewingStudentId(student.id);
            }}
          >
            <Eye className='h-3.5 w-3.5' />
          </Button>
          {access.canWriteStudentData && (
            <>
              <Button
                size='icon'
                variant='outline'
                className='h-8 w-8 text-slate-500 hover:text-slate-900 border-slate-200'
                onClick={() => {
                  setEditingStudentId(student.id);
                  setDialogOpen(true);
                }}
              >
                <Pencil className='h-4 w-4' />
              </Button>
              <Button
                size='icon'
                variant='outline'
                className='h-8 w-8 text-slate-500 hover:text-red-600 hover:bg-red-50 hover:border-red-100 border-slate-200'
                onClick={() => setDeletingStudent(student)}
              >
                <Trash2 className='h-4 w-4' />
              </Button>
            </>
          )}
        </div>
      ),
    },
  ];

  const visibleColumns = React.useMemo(() => {
    return studentColumns.filter((col) => {
      if (access.isParentOnly && col.key === 'parent') return false;
      return true;
    });
  }, [access.isParentOnly, studentColumns]);

  const toolbar = (
    <div className='flex flex-wrap items-center gap-3 flex-1 min-w-0'>
      {/* Search */}
      <div className='relative flex-1 min-w-[200px] max-w-xs'>
        <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400' />
        <input
          type='text'
          placeholder='Tìm theo tên hoặc mã học sinh...'
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className='w-full h-9 rounded-lg border border-slate-200 bg-white py-1.5 pl-9 pr-3 text-xs outline-none focus:border-slate-300 focus:ring-1 focus:ring-slate-200'
        />
      </div>
      <SchoolBusSelect
        value={filterSchool}
        onChange={setFilterSchool}
        placeholder='Tất cả trường học'
        options={schoolNames.map((name) => {
          const count = students.filter((s) => s.schoolName === name).length;
          return {
            label: name,
            value: name,
            badge: (
              <span className='inline-flex items-center rounded-md bg-slate-100 px-1.5 py-0.5 text-[9px] font-medium text-slate-600'>
                {count}
              </span>
            ),
          };
        })}
        clearable
        searchable
      />
      <SchoolBusSelect
        value={filterStatus}
        onChange={setFilterStatus}
        placeholder='Tất cả trạng thái'
        options={[
          {
            label: 'Đang hoạt động',
            value: 'active',
            badge: (
              <span className='inline-flex items-center rounded-md bg-slate-100 px-1.5 py-0.5 text-[9px] font-medium text-slate-600'>
                {students.filter((s) => s.isActive !== false).length}
              </span>
            ),
          },
          {
            label: 'Ngừng hoạt động',
            value: 'inactive',
            badge: (
              <span className='inline-flex items-center rounded-md bg-slate-100 px-1.5 py-0.5 text-[9px] font-medium text-slate-600'>
                {students.filter((s) => s.isActive === false).length}
              </span>
            ),
          },
        ]}
        clearable
      />
    </div>
  );

  return (
    <>
      <SchoolBusPageShell
        title='Học sinh'
        description='Quản lý hồ sơ học sinh phục vụ lập tuyến, yêu cầu xe bus và điểm danh.'
        breadcrumb={
          <SchoolBusBreadcrumb
            items={[
              { label: 'Điều phối xe buýt', href: '/school-bus/dispatch' },
              { label: 'Học sinh', current: true },
            ]}
          />
        }
        actions={
          access.canWriteStudentData ? (
            <Button
              className='rounded-full bg-[#C81E3A] hover:bg-[#A6142D] text-white'
              onClick={() => {
                setEditingStudentId(null);
                setDialogOpen(true);
              }}
            >
              <Plus className='h-4 w-4' /> Thêm học sinh
            </Button>
          ) : undefined
        }
      >
        <div className='flex flex-col gap-6'>
          {/* Stats */}
          <div
            className={cn(
              'grid gap-3',
              access.isParentOnly
                ? 'grid-cols-1 md:grid-cols-3'
                : 'grid-cols-2 lg:grid-cols-4'
            )}
          >
            <SchoolBusMetricCard
              label='Học sinh'
              value={students.length}
              icon={User}
              tone='student'
            />
            <SchoolBusMetricCard
              label='Trường học'
              value={uniqueSchools}
              icon={GraduationCap}
              tone='school'
            />
            {!access.isParentOnly && (
              <SchoolBusMetricCard
                label='Phụ huynh đã liên kết'
                value={linkedParents}
                icon={Users}
                tone='default'
              />
            )}
            <SchoolBusMetricCard
              label='Học sinh đang hoạt động'
              value={activeStudents}
              icon={CheckCircle2}
              tone='success'
            />
          </div>

          <SchoolBusDataTable
            title='Danh sách học sinh'
            description='Quản lý hồ sơ học sinh, trường học, phụ huynh và điểm đón/trả mặc định.'
            toolbar={toolbar}
            data={filteredStudents}
            columns={visibleColumns}
            isLoading={isLoading}
            pagination={{ page: data?.data, onPageChange: pagination.setPage }}
            stickyFirstColumn
            stickyActionColumn
            emptyIcon={User}
            emptyTitle={
              students.length === 0
                ? 'Chưa có học sinh'
                : 'Không có học sinh phù hợp với bộ lọc'
            }
            emptyDescription={
              students.length === 0
                ? 'Hãy tạo học sinh và liên kết với phụ huynh, điểm đón/trả mặc định.'
                : 'Hãy thử điều chỉnh từ khóa tìm kiếm hoặc xóa bộ lọc.'
            }
          />
        </div>
      </SchoolBusPageShell>

      <StudentFormDialog
        open={dialogOpen && (!editingStudentId || Boolean(editingStudent))}
        onOpenChange={(open) => {
          setDialogOpen(open);
          if (!open) setEditingStudentId(null);
        }}
        initialData={editingStudent}
        schools={schools}
        parents={parents}
        isLoading={creating || updating}
        onSubmit={handleSave}
        isParent={access.isParentOnly}
      />

      <SchoolBusReadOnlyDetailDialog
        open={Boolean(viewingStudentId)}
        onOpenChange={(open) => {
          if (!open) setViewingStudentId(null);
        }}
        title='Chi tiết học sinh'
        description='Thông tin học sinh được tải từ hệ thống.'
        isLoading={loadingViewingStudent}
        isError={viewingStudentError}
        sections={[
          {
            title: 'Thông tin cơ bản',
            fields: [
              { label: 'Họ tên học sinh', value: viewingStudent?.fullName },
              { label: 'Mã học sinh', value: viewingStudent?.studentCode },
              { label: 'Trường học', value: viewingStudent?.schoolName },
              {
                label: 'Khối / lớp',
                value:
                  [viewingStudent?.grade, viewingStudent?.className]
                    .filter(Boolean)
                    .join(' / ') || null,
              },
              {
                label: 'Ngày sinh',
                value: formatStudentDate(viewingStudent?.dateOfBirth),
              },
              {
                label: 'Giới tính',
                value: getLabel(genderLabel, viewingStudent?.gender),
              },
              {
                label: 'Trạng thái',
                value: getLabel(
                  profileStatusLabel,
                  viewingStudent?.isActive === false ? 'INACTIVE' : 'ACTIVE'
                ),
              },
            ],
          },
          {
            title: 'Phụ huynh và thông tin đón/trả mặc định',
            fields: [
              { label: 'Phụ huynh', value: viewingStudent?.parentProfileName },
              {
                label: 'Điểm đón mặc định',
                value: viewingStudent?.pickupPointName,
              },
              {
                label: 'Điểm trả mặc định',
                value: viewingStudent?.defaultDropoffPointName,
              },
              {
                label: 'Địa chỉ nhà',
                value: viewingStudent?.homeAddress,
                fullWidth: true,
              },
              {
                label: 'Ghi chú đặc biệt',
                value: viewingStudent?.specialNote,
                fullWidth: true,
              },
            ],
          },
        ]}
      />

      <SchoolBusDeleteDialog
        open={Boolean(deletingStudent)}
        onOpenChange={(open) => {
          if (!open) setDeletingStudent(null);
        }}
        title='Xóa học sinh'
        description='Học sinh sẽ được xóa mềm khỏi danh sách đang sử dụng trong vận hành xe bus.'
        isLoading={deleting}
        onConfirm={handleDelete}
      />
    </>
  );
}


