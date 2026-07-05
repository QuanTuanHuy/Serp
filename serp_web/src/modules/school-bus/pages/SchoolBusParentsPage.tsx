'use client';

import * as React from 'react';
import {
  AlertTriangle,
  CheckCircle2,
  Eye,
  Mail,
  Pencil,
  Phone,
  Plus,
  Search,
  Trash2,
  Users,
} from 'lucide-react';
import { toast } from 'sonner';
import { useAppSelector } from '@/lib/store';
import { selectUserProfile } from '@/modules/account/store';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useCreateParentMutation,
  useDeleteParentMutation,
  useGetParentByIdQuery,
  useGetParentsQuery,
  useUpdateParentMutation,
} from '../api/schoolBusApi';
import { useGetSchoolBusModuleUsersQuery } from '../api/schoolBusAccountApi';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { SchoolBusDeleteDialog } from '../components/SchoolBusDeleteDialog';
import { ParentFormDialog } from '../components/SchoolBusMasterDataForms';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusReadOnlyDetailDialog } from '../components/SchoolBusReadOnlyDetailDialog';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { SchoolBusSelect } from '../components/ui/SchoolBusSelect';
import { SchoolBusDataTable } from '../components/ui/SchoolBusDataTable';
import type { SchoolBusTableColumn } from '../components/ui/SchoolBusDataTable';
import { SCHOOL_BUS_ACCOUNT_MODULE_ID } from '../constants';
import { useSchoolBusPagination } from '../hooks/useSchoolBusPagination';
import type {
  SchoolBusAccountUser,
  SchoolBusParent,
  SchoolBusParentUpsertRequest,
} from '../types';
import { getPageItems, SCHOOL_BUS_PAGE_QUERY_OPTIONS } from '../utils';

type ContactCompleteness =
  | 'reachable'
  | 'missing-phone'
  | 'missing-email'
  | 'no-linked-student';

function getContactCompleteness(
  parent: SchoolBusParent,
  linkedStudentCount?: number | null
): ContactCompleteness {
  if (linkedStudentCount === 0) return 'no-linked-student';
  if (!parent.phone) return 'missing-phone';
  if (!parent.email) return 'missing-email';
  return 'reachable';
}

const contactCompletenessConfig: Record<
  ContactCompleteness,
  { label: string; className: string }
> = {
  reachable: {
    label: 'Reachable',
    className: 'bg-emerald-50 text-emerald-700 ring-emerald-200',
  },
  'missing-phone': {
    label: 'Missing phone',
    className: 'bg-amber-50 text-amber-700 ring-amber-200',
  },
  'missing-email': {
    label: 'Missing email',
    className: 'bg-amber-50 text-amber-700 ring-amber-200',
  },
  'no-linked-student': {
    label: 'Chưa liên kết học sinh',
    className: 'bg-red-50 text-red-700 ring-red-200',
  },
};

function ContactCompletenessBadge({
  completeness,
}: {
  completeness: ContactCompleteness;
}) {
  const cfg = contactCompletenessConfig[completeness];
  return (
    <span
      className={cn(
        'inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-[10px] font-semibold ring-1',
        cfg.className
      )}
    >
      {completeness === 'reachable' ? (
        <CheckCircle2 className='h-3 w-3' />
      ) : (
        <AlertTriangle className='h-3 w-3' />
      )}
      {cfg.label}
    </span>
  );
}

export function SchoolBusParentsPage() {
  const currentUser = useAppSelector(selectUserProfile);
  const organizationId = currentUser?.organizationId;
  const pagination = useSchoolBusPagination({
    page: 0,
    size: 10,
    sortBy: 'fullName',
    sortDirection: 'ASC',
  });

  const [dialogOpen, setDialogOpen] = React.useState(false);
  const [editingParentId, setEditingParentId] = React.useState<number | null>(
    null
  );
  const [viewingParentId, setViewingParentId] = React.useState<number | null>(
    null
  );
  const [deletingParent, setDeletingParent] =
    React.useState<SchoolBusParent | null>(null);
  const [searchTerm, setSearchTerm] = React.useState('');
  const [filterContact, setFilterContact] = React.useState<string>('');
  const [filterStatus, setFilterStatus] = React.useState<string>('');
  const [debouncedSearch, setDebouncedSearch] = React.useState('');

  const { data, isLoading } = useGetParentsQuery(
    pagination.params,
    SCHOOL_BUS_PAGE_QUERY_OPTIONS
  );
  const { data: editingParentData } = useGetParentByIdQuery(
    editingParentId || 0,
    { skip: !dialogOpen || !editingParentId }
  );
  const {
    data: viewingParentData,
    isFetching: loadingViewingParent,
    isError: viewingParentError,
  } = useGetParentByIdQuery(viewingParentId || 0, {
    skip: !viewingParentId,
  });
  const { data: parentOptionsData } = useGetParentsQuery(
    { page: 0, size: 100, sortBy: 'fullName', sortDirection: 'ASC' },
    { skip: !dialogOpen }
  );
  const { data: accountUsersData, isFetching: loadingAccountUsers } =
    useGetSchoolBusModuleUsersQuery(
      {
        organizationId: organizationId || 0,
        moduleId: SCHOOL_BUS_ACCOUNT_MODULE_ID,
      },
      { skip: !dialogOpen || !organizationId }
    );

  const [createParent, { isLoading: creating }] = useCreateParentMutation();
  const [updateParent, { isLoading: updating }] = useUpdateParentMutation();
  const [deleteParent, { isLoading: deleting }] = useDeleteParentMutation();

  const parents = getPageItems(data?.data);
  const editingParent = editingParentData?.data || null;
  const viewingParent = viewingParentData?.data || null;
  const parentOptions = getPageItems(parentOptionsData?.data);
  const accountUsers = accountUsersData?.data || [];

  React.useEffect(() => {
    const timer = setTimeout(() => setDebouncedSearch(searchTerm), 300);
    return () => clearTimeout(timer);
  }, [searchTerm]);

  React.useEffect(() => {
    pagination.setKeyword(debouncedSearch || '');
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debouncedSearch]);

  const withEmail = parents.filter((p) => p.email).length;
  const withPhone = parents.filter((p) => p.phone).length;
  const activeParents = parents.filter((p) => p.isActive !== false).length;

  const filteredParents = React.useMemo(() => {
    let result = parents;
    if (filterContact === 'has-phone') result = result.filter((p) => p.phone);
    if (filterContact === 'has-email') result = result.filter((p) => p.email);
    if (filterContact === 'missing-phone')
      result = result.filter((p) => !p.phone);
    if (filterContact === 'missing-email')
      result = result.filter((p) => !p.email);
    if (filterStatus === 'active')
      result = result.filter((p) => p.isActive !== false);
    if (filterStatus === 'inactive')
      result = result.filter((p) => p.isActive === false);
    return result;
  }, [parents, filterContact, filterStatus]);

  const parentDialogUsers = React.useMemo(
    () =>
      buildAvailableParentAccountUsers({
        users: accountUsers,
        parentProfiles: parentOptions,
        editingParent,
      }),
    [accountUsers, parentOptions, editingParent]
  );

  const handleSave = async (values: SchoolBusParentUpsertRequest) => {
    try {
      const response = editingParentId
        ? await updateParent({ id: editingParentId, body: values }).unwrap()
        : await createParent(values).unwrap();
      toast.success(response.message || 'Parent profile saved');
      setDialogOpen(false);
      setEditingParentId(null);
    } catch {
      toast.error('Không thể lưu hồ sơ phụ huynh');
    }
  };

  const handleDelete = async () => {
    if (!deletingParent) return;
    try {
      const response = await deleteParent(deletingParent.id).unwrap();
      toast.success(response.message || 'Parent profile deleted');
      setDeletingParent(null);
    } catch {
      toast.error('Không thể xóa hồ sơ phụ huynh');
    }
  };

  const parentColumns: SchoolBusTableColumn<SchoolBusParent>[] = [
    {
      key: 'parent',
      header: 'Phụ huynh',
      className: 'pl-6',
      headerClassName: 'pl-6',
      render: (parent) => (
        <div className='flex items-center gap-3'>
          <div className='flex h-8 w-8 shrink-0 items-center justify-center rounded-xl bg-slate-50 text-slate-600 ring-1 ring-slate-200/50'>
            <Users className='h-4.5 w-4.5' />
          </div>
          <div>
            <p className='font-semibold text-slate-900'>{parent.fullName}</p>
            <p className='text-xs text-muted-foreground mt-0.5'>
              {parent.user?.email || 'Local profile'}
            </p>
          </div>
        </div>
      ),
    },
    {
      key: 'contact',
      header: 'Contact',
      render: (parent) => (
        <div>
          <p className='text-sm'>
            {parent.phone || (
              <span className='text-slate-400 italic'>Chưa có số điện thoại</span>
            )}
          </p>
          <p className='text-xs text-muted-foreground'>
            {parent.email || <span className='italic'>Chưa có email</span>}
          </p>
        </div>
      ),
    },
    {
      key: 'students',
      header: 'Linked students',
      render: (parent) => {
        const studentCount = parent.studentCount;
        if (typeof studentCount === 'number' && studentCount > 0) {
          return (
            <span className='inline-flex items-center rounded-full bg-blue-50 px-2 py-0.5 text-[10px] font-semibold text-blue-700 ring-1 ring-blue-200'>
              {studentCount} student{studentCount > 1 ? 's' : ''}
            </span>
          );
        }
        if (typeof studentCount === 'number') {
          return (
            <span className='inline-flex items-center rounded-full bg-slate-100 px-2 py-0.5 text-[10px] font-medium text-slate-500 ring-1 ring-slate-200'>
              None
            </span>
          );
        }
        return (
          <span className='text-xs text-muted-foreground'>Not available</span>
        );
      },
    },
    {
      key: 'completeness',
      header: 'Profile status',
      render: (parent) => (
        <ContactCompletenessBadge
          completeness={getContactCompleteness(parent, parent.studentCount)}
        />
      ),
    },
    {
      key: 'address',
      header: 'Address',
      render: (parent) => (
        <span className='text-sm'>
          {parent.address || (
            <span className='text-slate-400 italic'>Chưa có địa chỉ</span>
          )}
        </span>
      ),
    },
    {
      key: 'status',
      header: 'Trạng thái',
      render: (parent) => (
        <SchoolBusStatusBadge
          status={parent.isActive ? 'ACTIVE' : 'INACTIVE'}
        />
      ),
    },
    {
      key: 'actions',
      header: 'Thao tác',
      className: 'pr-6 text-right',
      headerClassName: 'pr-6 text-right',
      render: (parent) => (
        <div className='flex justify-end gap-2'>
          <Button
            size='icon'
            variant='outline'
            className='h-8 w-8 rounded-lg border-slate-200 text-slate-500 hover:bg-slate-50 hover:text-slate-700'
            onClick={() => setViewingParentId(parent.id)}
          >
            <Eye className='h-3.5 w-3.5' />
          </Button>
          <Button
            size='icon'
            variant='outline'
            className='h-8 w-8 text-slate-500 hover:text-slate-900 border-slate-200'
            onClick={() => {
              setEditingParentId(parent.id);
              setDialogOpen(true);
            }}
          >
            <Pencil className='h-4 w-4' />
          </Button>
          <Button
            size='icon'
            variant='outline'
            className='h-8 w-8 text-slate-500 hover:text-red-600 hover:bg-red-50 hover:border-red-100 border-slate-200'
            onClick={() => setDeletingParent(parent)}
          >
            <Trash2 className='h-4 w-4' />
          </Button>
        </div>
      ),
    },
  ];

  const toolbar = (
    <div className='flex flex-wrap items-center gap-3 flex-1 min-w-0'>
      <div className='relative flex-1 min-w-[200px] max-w-xs'>
        <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400' />
        <input
          type='text'
          placeholder='Search by name, phone, or email...'
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className='w-full h-9 rounded-lg border border-slate-200 bg-white py-1.5 pl-9 pr-3 text-xs outline-none focus:border-slate-300 focus:ring-1 focus:ring-slate-200'
        />
      </div>
      <SchoolBusSelect
        value={filterContact}
        onChange={setFilterContact}
        placeholder='All contacts'
        icon={Phone}
        options={[
          { label: 'Has phone', value: 'has-phone' },
          { label: 'Has email', value: 'has-email' },
          { label: 'Missing phone', value: 'missing-phone' },
          { label: 'Missing email', value: 'missing-email' },
        ]}
        clearable
      />
      <SchoolBusSelect
        value={filterStatus}
        onChange={setFilterStatus}
        placeholder='All statuses'
        options={[
          { label: 'Đang hoạt động', value: 'active' },
          { label: 'Ngừng hoạt động', value: 'inactive' },
        ]}
        clearable
      />
    </div>
  );

  return (
    <>
      <SchoolBusPageShell
        title='Parent profiles'
        description='Operational parent directory - contact data powering notifications, escalation, and student linkage.'
        breadcrumb={
          <SchoolBusBreadcrumb
            items={[
              { label: 'School Bus Ops', href: '/school-bus/dispatch' },
              { label: 'Phụ huynh', current: true },
            ]}
          />
        }
        actions={
          <Button
            className='rounded-full bg-[#C81E3A] hover:bg-[#A6142D] text-white'
            onClick={() => {
              setEditingParentId(null);
              setDialogOpen(true);
            }}
          >
            <Plus className='h-4 w-4' /> Add parent profile
          </Button>
        }
      >
        <div className='flex flex-col gap-6'>
          <div className='grid gap-3 grid-cols-2 lg:grid-cols-4'>
            <SchoolBusMetricCard
              label='Parent profiles'
              value={parents.length}
              icon={Users}
              tone='info'
              hint='Profiles linked to school-bus operations'
            />
            <SchoolBusMetricCard
              label='With email'
              value={withEmail}
              icon={Mail}
              tone='success'
              hint='Ready for notification flows'
            />
            <SchoolBusMetricCard
              label='With phone'
              value={withPhone}
              icon={Phone}
              tone='default'
              hint='Reachable for escalation'
            />
            <SchoolBusMetricCard
              label='Active parents'
              value={activeParents}
              icon={CheckCircle2}
              tone='success'
              hint='Profiles currently active for transport operations'
            />
          </div>

          <SchoolBusDataTable
            title='Parent profiles'
            description='Manage parent profiles, contact data, and student linkages.'
            toolbar={toolbar}
            data={filteredParents}
            columns={parentColumns}
            isLoading={isLoading}
            pagination={{ page: data?.data, onPageChange: pagination.setPage }}
            stickyFirstColumn
            stickyActionColumn
            emptyIcon={Users}
            emptyTitle={
              parents.length === 0
                ? 'Không tìm thấy hồ sơ phụ huynh'
                : 'Không có phụ huynh phù hợp với bộ lọc'
            }
            emptyDescription={
              parents.length === 0
                ? 'Hãy tạo hồ sơ phụ huynh để quản lý yêu cầu và liên kết gia đình trong module.'
                : 'Không có phụ huynh phù hợp với bộ lọc. Hãy thử điều chỉnh tiêu chí tìm kiếm.'
            }
          />
        </div>
      </SchoolBusPageShell>

      <ParentFormDialog
        open={dialogOpen && (!editingParentId || Boolean(editingParent))}
        onOpenChange={(open) => {
          setDialogOpen(open);
          if (!open) setEditingParentId(null);
        }}
        initialData={editingParent}
        accountUsers={parentDialogUsers}
        isLoadingAccountUsers={loadingAccountUsers}
        isLoading={creating || updating}
        onSubmit={handleSave}
      />

      <SchoolBusReadOnlyDetailDialog
        open={Boolean(viewingParentId)}
        onOpenChange={(open) => {
          if (!open) setViewingParentId(null);
        }}
        title='Parent profile detail'
        description='Read-only parent information loaded from the detail API.'
        isLoading={loadingViewingParent}
        isError={viewingParentError}
        sections={[
          {
            title: 'Linked account',
            fields: [
              {
                label: 'Account user ID',
                value:
                  viewingParent?.accountUserId || viewingParent?.userId || null,
              },
              { label: 'Account email', value: viewingParent?.user?.email },
            ],
          },
          {
            title: 'Contact information',
            fields: [
              { label: 'Full name', value: viewingParent?.fullName },
              { label: 'Phone', value: viewingParent?.phone },
              { label: 'Email', value: viewingParent?.email },
              {
                label: 'Trạng thái',
                value: viewingParent?.isActive === false ? 'Ngừng hoạt động' : 'Đang hoạt động',
              },
              {
                label: 'Address',
                value: viewingParent?.address,
                fullWidth: true,
              },
            ],
          },
        ]}
      />

      <SchoolBusDeleteDialog
        open={Boolean(deletingParent)}
        onOpenChange={(open) => {
          if (!open) setDeletingParent(null);
        }}
        title='Xóa hồ sơ phụ huynh'
        description='This will soft-delete the operational parent profile from school-bus use.'
        isLoading={deleting}
        onConfirm={handleDelete}
      />
    </>
  );
}

function buildAvailableParentAccountUsers({
  users,
  parentProfiles,
  editingParent,
}: {
  users: SchoolBusAccountUser[];
  parentProfiles: SchoolBusParent[];
  editingParent: SchoolBusParent | null;
}) {
  const linkedUserIds = new Set(
    parentProfiles
      .filter((parent) => parent.id !== editingParent?.id)
      .filter((parent) => parent.isActive !== false)
      .map((parent) => parent.userId)
  );

  const availableUsers = users.filter(
    (user) => !linkedUserIds.has(user.id) || user.id === editingParent?.userId
  );

  if (
    editingParent &&
    !availableUsers.some((user) => user.id === editingParent.userId)
  ) {
    return [
      {
        id: editingParent.userId,
        email:
          editingParent.email || `user-${editingParent.userId}@unknown.local`,
        firstName: editingParent.fullName,
        lastName: '',
        phoneNumber: editingParent.phone || null,
        status: editingParent.isActive === false ? 'INACTIVE' : 'ACTIVE',
        userType: 'EXTERNAL',
      },
      ...availableUsers,
    ];
  }

  return availableUsers;
}
