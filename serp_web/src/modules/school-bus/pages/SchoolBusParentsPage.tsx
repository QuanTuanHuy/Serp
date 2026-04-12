'use client';

import * as React from 'react';
import { Contact, Mail, Pencil, Phone, Plus, Trash2 } from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/shared/components/ui';
import {
  useCreateParentMutation,
  useDeleteParentMutation,
  useGetParentsQuery,
  useUpdateParentMutation,
} from '../api/schoolBusApi';
import { SchoolBusDeleteDialog } from '../components/SchoolBusDeleteDialog';
import { ParentFormDialog } from '../components/SchoolBusMasterDataForms';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusPaginationBar } from '../components/SchoolBusPaginationBar';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusSection } from '../components/SchoolBusSection';
import { SchoolBusScrollableTable } from '../components/SchoolBusScrollableTable';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { useSchoolBusPagination } from '../hooks/useSchoolBusPagination';
import type { SchoolBusParent } from '../types';
import { getPageItems } from '../utils';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';

export function SchoolBusParentsPage() {
  const pagination = useSchoolBusPagination({
    page: 0,
    size: 10,
    sortBy: 'fullName',
    sortDirection: 'ASC',
  });
  const { data, isLoading } = useGetParentsQuery(pagination.params);
  const [createParent, { isLoading: creating }] = useCreateParentMutation();
  const [updateParent, { isLoading: updating }] = useUpdateParentMutation();
  const [deleteParent, { isLoading: deleting }] = useDeleteParentMutation();
  const parents = getPageItems(data?.data);

  const [dialogOpen, setDialogOpen] = React.useState(false);
  const [editingParent, setEditingParent] =
    React.useState<SchoolBusParent | null>(null);
  const [deletingParent, setDeletingParent] =
    React.useState<SchoolBusParent | null>(null);

  const handleSave = async (values: any) => {
    try {
      const response = editingParent
        ? await updateParent({ id: editingParent.id, body: values }).unwrap()
        : await createParent(values).unwrap();
      toast.success(response.message || 'Parent profile saved');
      setDialogOpen(false);
      setEditingParent(null);
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to save parent profile');
    }
  };

  const handleDelete = async () => {
    if (!deletingParent) {
      return;
    }

    try {
      const response = await deleteParent(deletingParent.id).unwrap();
      toast.success(response.message || 'Parent profile deleted');
      setDeletingParent(null);
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to delete parent profile');
    }
  };

  return (
    <>
      <SchoolBusPageShell
        title='Parent profiles'
        description='Parent accounts stay owned by account, while this page maintains the operational profile used by requests and student linkage.'
        actions={
          <Button
            className='rounded-full'
            onClick={() => {
              setEditingParent(null);
              setDialogOpen(true);
            }}
          >
            <Plus className='h-4 w-4' />
            Add parent profile
          </Button>
        }
      >
        <div className='grid gap-4 md:grid-cols-3'>
          <SchoolBusMetricCard
            label='Parent profiles'
            value={parents.length}
            hint='Profiles linked to school-bus operations'
            icon={Contact}
            tone='info'
          />
          <SchoolBusMetricCard
            label='Profiles with email'
            value={parents.filter((parent) => parent.email).length}
            hint='Ready for future notification flows'
            icon={Mail}
            tone='success'
          />
          <SchoolBusMetricCard
            label='Profiles with phone'
            value={parents.filter((parent) => parent.phone).length}
            hint='Reachable for operational escalation'
            icon={Phone}
            tone='default'
          />
        </div>

        <SchoolBusSection
          title='Parent directory'
          description='Maintain operational contact data for the School Bus module.'
        >
          {isLoading ? (
            <p className='text-sm text-muted-foreground'>Loading parents...</p>
          ) : parents.length === 0 ? (
            <SchoolBusEmptyState
              title='No parent profiles available'
              description='Create parent profiles so requests and family linkage can be managed in the module.'
              icon={Contact}
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
                  <TableHead>Parent</TableHead>
                  <TableHead>Contact</TableHead>
                  <TableHead>Address</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead className='text-right'>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {parents.map((parent) => (
                  <TableRow key={parent.id}>
                    <TableCell>
                      <div>
                        <p className='font-medium'>{parent.fullName}</p>
                        <p className='text-xs text-muted-foreground'>
                          Account user #{parent.userId}
                        </p>
                      </div>
                    </TableCell>
                    <TableCell>
                      <div>
                        <p>{parent.phone || 'No phone'}</p>
                        <p className='text-xs text-muted-foreground'>
                          {parent.email || 'No email'}
                        </p>
                      </div>
                    </TableCell>
                    <TableCell>{parent.address || 'No address'}</TableCell>
                    <TableCell>
                      <SchoolBusStatusBadge
                        status={parent.isActive ? 'ACTIVE' : 'INACTIVE'}
                      />
                    </TableCell>
                    <TableCell className='text-right'>
                      <div className='flex justify-end gap-2'>
                        <Button
                          size='icon'
                          variant='outline'
                          onClick={() => {
                            setEditingParent(parent);
                            setDialogOpen(true);
                          }}
                        >
                          <Pencil className='h-4 w-4' />
                        </Button>
                        <Button
                          size='icon'
                          variant='outline'
                          onClick={() => setDeletingParent(parent)}
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

      <ParentFormDialog
        open={dialogOpen}
        onOpenChange={(open) => {
          setDialogOpen(open);
          if (!open) {
            setEditingParent(null);
          }
        }}
        initialData={editingParent}
        isLoading={creating || updating}
        onSubmit={handleSave}
      />

      <SchoolBusDeleteDialog
        open={Boolean(deletingParent)}
        onOpenChange={(open) => {
          if (!open) {
            setDeletingParent(null);
          }
        }}
        title='Delete parent profile'
        description='This will soft-delete the operational parent profile from school-bus use.'
        isLoading={deleting}
        onConfirm={handleDelete}
      />
    </>
  );
}
