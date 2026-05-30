'use client';

import * as React from 'react';
import {
  AlertTriangle,
  CheckCircle2,
  Contact,
  Mail,
  Pencil,
  Phone,
  Plus,
  Search,
  Trash2,
} from 'lucide-react';
import { toast } from 'sonner';
import { useAppSelector } from '@/lib/store';
import { selectUserProfile } from '@/modules/account/store';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useCreateParentMutation,
  useDeleteParentMutation,
  useGetParentsQuery,
  useGetStudentsQuery,
  useUpdateParentMutation,
} from '../api/schoolBusApi';
import { useGetSchoolBusModuleUsersQuery } from '../api/schoolBusAccountApi';
import { SchoolBusDeleteDialog } from '../components/SchoolBusDeleteDialog';
import { SchoolBusFilterSelect } from '../components/SchoolBusFilterSelect';
import { ParentFormDialog } from '../components/SchoolBusMasterDataForms';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusPaginationBar } from '../components/SchoolBusPaginationBar';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusScrollableTable } from '../components/SchoolBusScrollableTable';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { SCHOOL_BUS_ACCOUNT_MODULE_ID } from '../constants';
import { useSchoolBusPagination } from '../hooks/useSchoolBusPagination';
import type { SchoolBusAccountUser, SchoolBusParent } from '../types';
import { getPageItems, SCHOOL_BUS_OPTION_QUERY } from '../utils';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';

// ── Contact readiness helpers ─────────────────────────────────────────────────

type ContactReadiness = 'reachable' | 'missing-phone' | 'missing-email' | 'no-linked-student';

function getContactReadiness(parent: SchoolBusParent, linkedStudentCount: number): ContactReadiness {
  if (linkedStudentCount === 0) return 'no-linked-student';
  if (!parent.phone) return 'missing-phone';
  if (!parent.email) return 'missing-email';
  return 'reachable';
}

const contactReadinessConfig: Record<ContactReadiness, { label: string; className: string }> = {
  reachable: { label: 'Reachable', className: 'bg-emerald-50 text-emerald-700 ring-emerald-200' },
  'missing-phone': { label: 'Missing phone', className: 'bg-amber-50 text-amber-700 ring-amber-200' },
  'missing-email': { label: 'Missing email', className: 'bg-amber-50 text-amber-700 ring-amber-200' },
  'no-linked-student': { label: 'No linked student', className: 'bg-rose-50 text-rose-700 ring-rose-200' },
};

function ContactReadinessBadge({ readiness }: { readiness: ContactReadiness }) {
  const cfg = contactReadinessConfig[readiness];
  return (
    <span className={cn('inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-[10px] font-semibold ring-1', cfg.className)}>
      {readiness === 'reachable' ? <CheckCircle2 className='h-3 w-3' /> : <AlertTriangle className='h-3 w-3' />}
      {cfg.label}
    </span>
  );
}

// ── Main Page ─────────────────────────────────────────────────────────────────

export function SchoolBusParentsPage() {
  const currentUser = useAppSelector(selectUserProfile);
  const organizationId = currentUser?.organizationId;
  const pagination = useSchoolBusPagination({ page: 0, size: 10, sortBy: 'fullName', sortDirection: 'ASC' });

  const [dialogOpen, setDialogOpen] = React.useState(false);
  const [editingParent, setEditingParent] = React.useState<SchoolBusParent | null>(null);
  const [deletingParent, setDeletingParent] = React.useState<SchoolBusParent | null>(null);

  const { data, isLoading } = useGetParentsQuery(pagination.params);
  const { data: parentOptionsData } = useGetParentsQuery(
    { page: 0, size: 100, sortBy: 'fullName', sortDirection: 'ASC' },
    { skip: !dialogOpen },
  );
  const { data: studentsData } = useGetStudentsQuery({ ...SCHOOL_BUS_OPTION_QUERY, sortBy: 'fullName' });
  const { data: accountUsersData, isFetching: loadingAccountUsers } = useGetSchoolBusModuleUsersQuery(
    { organizationId: organizationId || 0, moduleId: SCHOOL_BUS_ACCOUNT_MODULE_ID },
    { skip: !dialogOpen || !organizationId },
  );

  const [createParent, { isLoading: creating }] = useCreateParentMutation();
  const [updateParent, { isLoading: updating }] = useUpdateParentMutation();
  const [deleteParent, { isLoading: deleting }] = useDeleteParentMutation();

  const parents = getPageItems(data?.data);
  const parentOptions = getPageItems(parentOptionsData?.data);
  const allStudents = getPageItems(studentsData?.data);
  const accountUsers = accountUsersData?.data || [];

  // ─── Linked students count map ──────────────────────────────────
  const linkedStudentsMap = React.useMemo(() => {
    const map = new Map<number, number>();
    for (const student of allStudents) {
      if (student.parentProfileId) {
        map.set(student.parentProfileId, (map.get(student.parentProfileId) || 0) + 1);
      }
    }
    return map;
  }, [allStudents]);

  // ─── Stats ──────────────────────────────────────────────────────
  const withEmail = parents.filter((p) => p.email).length;
  const withPhone = parents.filter((p) => p.phone).length;
  const noLinkedStudents = parents.filter((p) => !linkedStudentsMap.get(p.id)).length;

  // ─── Filter state ───────────────────────────────────────────────
  const [searchTerm, setSearchTerm] = React.useState('');
  const [filterContact, setFilterContact] = React.useState<string>('');
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
  const filteredParents = React.useMemo(() => {
    let result = parents;
    if (filterContact === 'has-phone') result = result.filter((p) => p.phone);
    if (filterContact === 'has-email') result = result.filter((p) => p.email);
    if (filterContact === 'missing-phone') result = result.filter((p) => !p.phone);
    if (filterContact === 'missing-email') result = result.filter((p) => !p.email);
    if (filterStatus === 'active') result = result.filter((p) => p.isActive !== false);
    if (filterStatus === 'inactive') result = result.filter((p) => p.isActive === false);
    return result;
  }, [parents, filterContact, filterStatus]);

  // ─── Dialog helpers ─────────────────────────────────────────────
  const parentDialogUsers = React.useMemo(
    () => buildAvailableParentAccountUsers({ users: accountUsers, parentProfiles: parentOptions, editingParent }),
    [accountUsers, parentOptions, editingParent],
  );

  const handleSave = async (values: any) => {
    try {
      const response = editingParent
        ? await updateParent({ id: editingParent.id, body: values }).unwrap()
        : await createParent(values).unwrap();
      toast.success(response.message || 'Parent profile saved');
      setDialogOpen(false);
      setEditingParent(null);
    } catch (error: any) { toast.error(error?.data?.message || 'Failed to save parent profile'); }
  };

  const handleDelete = async () => {
    if (!deletingParent) return;
    try {
      const response = await deleteParent(deletingParent.id).unwrap();
      toast.success(response.message || 'Parent profile deleted');
      setDeletingParent(null);
    } catch (error: any) { toast.error(error?.data?.message || 'Failed to delete parent profile'); }
  };

  return (
    <>
      <SchoolBusPageShell
        title='Parent profiles'
        description='Operational parent directory — contact data powering notifications, escalation, and student linkage.'
        breadcrumb={
          <SchoolBusBreadcrumb items={[
            { label: 'School Bus Ops', href: '/school-bus/dispatch' },
            { label: 'Parents', current: true },
          ]} />
        }
        actions={
          <Button className='rounded-full' onClick={() => { setEditingParent(null); setDialogOpen(true); }}>
            <Plus className='h-4 w-4' /> Add parent profile
          </Button>
        }
      >
        {/* Stats */}
        <div className='grid gap-3 grid-cols-2 lg:grid-cols-4'>
          <SchoolBusMetricCard label='Parent profiles' value={parents.length} icon={Contact} tone='info' hint='Profiles linked to school-bus operations' />
          <SchoolBusMetricCard label='With email' value={withEmail} icon={Mail} tone='success' hint='Ready for notification flows' />
          <SchoolBusMetricCard label='With phone' value={withPhone} icon={Phone} tone='default' hint='Reachable for escalation' />
          <SchoolBusMetricCard
            label='No linked student'
            value={noLinkedStudents}
            icon={AlertTriangle}
            tone={noLinkedStudents > 0 ? 'warning' : 'success'}
            hint={noLinkedStudents > 0 ? 'Parents without any linked student' : 'All parents linked'}
          />
        </div>

        {/* Search & Filter bar */}
        <div className='flex flex-wrap items-center gap-3 rounded-xl border border-slate-200 bg-white px-4 py-3 shadow-sm'>
          {/* Search */}
          <div className='relative flex-1 min-w-[200px]'>
            <Search className='absolute left-2.5 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-slate-400' />
            <input
              type='text'
              placeholder='Search by name, phone, or email...'
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className='w-full rounded-lg border border-slate-200 bg-slate-50 py-1.5 pl-8 pr-3 text-xs outline-none focus:border-rose-300 focus:ring-1 focus:ring-rose-200'
            />
          </div>
          <SchoolBusFilterSelect
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
          <p className='text-sm text-muted-foreground'>Loading parents...</p>
        ) : filteredParents.length === 0 ? (
          <SchoolBusEmptyState
            title='No parent profiles found'
            description={parents.length === 0
              ? 'Create parent profiles so requests and family linkage can be managed in the module.'
              : 'No parents match the current filters. Try adjusting your search criteria.'}
            icon={Contact}
          />
        ) : (
          <SchoolBusScrollableTable footer={<SchoolBusPaginationBar page={data?.data} onPageChange={pagination.setPage} />}>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Parent</TableHead>
                  <TableHead>Contact</TableHead>
                  <TableHead>Linked students</TableHead>
                  <TableHead>Contact readiness</TableHead>
                  <TableHead>Address</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead className='text-right'>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredParents.map((parent) => {
                  const studentCount = linkedStudentsMap.get(parent.id) || 0;
                  const readiness = getContactReadiness(parent, studentCount);
                  return (
                    <TableRow key={parent.id}>
                      <TableCell>
                        <div>
                          <p className='font-medium'>{parent.fullName}</p>
                          <p className='text-xs text-muted-foreground'>
                            Linked account #{parent.userId}
                          </p>
                        </div>
                      </TableCell>
                      <TableCell>
                        <div>
                          <p className='text-sm'>{parent.phone || <span className='text-slate-400 italic'>No phone</span>}</p>
                          <p className='text-xs text-muted-foreground'>{parent.email || <span className='italic'>No email</span>}</p>
                        </div>
                      </TableCell>
                      <TableCell>
                        {studentCount > 0 ? (
                          <span className='inline-flex items-center rounded-full bg-blue-50 px-2 py-0.5 text-[10px] font-semibold text-blue-700 ring-1 ring-blue-200'>
                            {studentCount} student{studentCount > 1 ? 's' : ''}
                          </span>
                        ) : (
                          <span className='inline-flex items-center rounded-full bg-slate-100 px-2 py-0.5 text-[10px] font-medium text-slate-500 ring-1 ring-slate-200'>
                            None
                          </span>
                        )}
                      </TableCell>
                      <TableCell>
                        <ContactReadinessBadge readiness={readiness} />
                      </TableCell>
                      <TableCell>
                        <span className='text-sm'>{parent.address || <span className='text-slate-400 italic'>No address</span>}</span>
                      </TableCell>
                      <TableCell>
                        <SchoolBusStatusBadge status={parent.isActive ? 'ACTIVE' : 'INACTIVE'} />
                      </TableCell>
                      <TableCell className='text-right'>
                        <div className='flex justify-end gap-2'>
                          <Button size='icon' variant='outline' onClick={() => { setEditingParent(parent); setDialogOpen(true); }}>
                            <Pencil className='h-4 w-4' />
                          </Button>
                          <Button size='icon' variant='outline' onClick={() => setDeletingParent(parent)}>
                            <Trash2 className='h-4 w-4' />
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          </SchoolBusScrollableTable>
        )}
      </SchoolBusPageShell>

      <ParentFormDialog
        open={dialogOpen}
        onOpenChange={(open) => { setDialogOpen(open); if (!open) setEditingParent(null); }}
        initialData={editingParent}
        accountUsers={parentDialogUsers}
        isLoadingAccountUsers={loadingAccountUsers}
        isLoading={creating || updating}
        onSubmit={handleSave}
      />

      <SchoolBusDeleteDialog
        open={Boolean(deletingParent)}
        onOpenChange={(open) => { if (!open) setDeletingParent(null); }}
        title='Delete parent profile'
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
      .map((parent) => parent.userId),
  );

  const availableUsers = users.filter(
    (user) => !linkedUserIds.has(user.id) || user.id === editingParent?.userId,
  );

  if (editingParent && !availableUsers.some((user) => user.id === editingParent.userId)) {
    return [
      {
        id: editingParent.userId,
        email: editingParent.email || `user-${editingParent.userId}@unknown.local`,
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
