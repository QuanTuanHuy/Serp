'use client';

import * as React from 'react';
import { Building2, MapPinned, Pencil, Phone, Plus, Trash2 } from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/shared/components/ui';
import {
  useCreatePickupPointMutation,
  useCreateSchoolMutation,
  useDeletePickupPointMutation,
  useDeleteSchoolMutation,
  useGetPickupPointsQuery,
  useGetSchoolsQuery,
  useUpdatePickupPointMutation,
  useUpdateSchoolMutation,
} from '../api/schoolBusApi';
import { SchoolBusDeleteDialog } from '../components/SchoolBusDeleteDialog';
import {
  PickupPointFormDialog,
  SchoolFormDialog,
} from '../components/SchoolBusMasterDataForms';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusPaginationBar } from '../components/SchoolBusPaginationBar';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusSection } from '../components/SchoolBusSection';
import { SchoolBusScrollableTable } from '../components/SchoolBusScrollableTable';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { OperationsMap } from '../components/map/OperationsMap';
import { useSchoolBusPagination } from '../hooks/useSchoolBusPagination';
import type { SchoolBusPickupPoint, SchoolBusSchool } from '../types';
import { getPageItems, SCHOOL_BUS_OPTION_QUERY } from '../utils';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';

type DeleteTarget =
  | { type: 'school'; entity: SchoolBusSchool }
  | { type: 'pickup'; entity: SchoolBusPickupPoint }
  | null;

export function SchoolBusSchoolsPage() {
  const schoolsPagination = useSchoolBusPagination({
    page: 0,
    size: 10,
    sortBy: 'name',
    sortDirection: 'ASC',
  });
  const pickupPagination = useSchoolBusPagination({
    page: 0,
    size: 10,
    sortBy: 'name',
    sortDirection: 'ASC',
  });
  const { data, isLoading } = useGetSchoolsQuery(schoolsPagination.params);
  const { data: allSchoolsData } = useGetSchoolsQuery({
    ...SCHOOL_BUS_OPTION_QUERY,
    sortBy: 'name',
  });
  const { data: pickupPointsData } = useGetPickupPointsQuery(
    pickupPagination.params
  );
  const [createSchool, { isLoading: creatingSchool }] = useCreateSchoolMutation();
  const [updateSchool, { isLoading: updatingSchool }] = useUpdateSchoolMutation();
  const [deleteSchool, { isLoading: deletingSchool }] = useDeleteSchoolMutation();
  const [createPickupPoint, { isLoading: creatingPickup }] =
    useCreatePickupPointMutation();
  const [updatePickupPoint, { isLoading: updatingPickup }] =
    useUpdatePickupPointMutation();
  const [deletePickupPoint, { isLoading: deletingPickup }] =
    useDeletePickupPointMutation();

  const schools = getPageItems(data?.data);
  const allSchools = getPageItems(allSchoolsData?.data);
  const pickupPoints = getPageItems(pickupPointsData?.data);

  const [schoolDialogOpen, setSchoolDialogOpen] = React.useState(false);
  const [pickupDialogOpen, setPickupDialogOpen] = React.useState(false);
  const [editingSchool, setEditingSchool] = React.useState<SchoolBusSchool | null>(
    null
  );
  const [editingPickup, setEditingPickup] =
    React.useState<SchoolBusPickupPoint | null>(null);
  const [deleteTarget, setDeleteTarget] = React.useState<DeleteTarget>(null);
  const [selectedSchoolId, setSelectedSchoolId] = React.useState<number | null>(
    null
  );
  const [selectedPickupPointId, setSelectedPickupPointId] =
    React.useState<number | null>(null);

  React.useEffect(() => {
    if (!selectedSchoolId && schools.length > 0) {
      setSelectedSchoolId(schools[0].id);
    }
  }, [schools, selectedSchoolId]);

  const handleSaveSchool = async (values: any) => {
    try {
      const response = editingSchool
        ? await updateSchool({ id: editingSchool.id, body: values }).unwrap()
        : await createSchool(values).unwrap();
      toast.success(response.message || 'School saved');
      setSchoolDialogOpen(false);
      setEditingSchool(null);
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to save school');
    }
  };

  const handleSavePickupPoint = async (values: any) => {
    try {
      const response = editingPickup
        ? await updatePickupPoint({ id: editingPickup.id, body: values }).unwrap()
        : await createPickupPoint(values).unwrap();
      toast.success(response.message || 'Pickup point saved');
      setPickupDialogOpen(false);
      setEditingPickup(null);
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to save pickup point');
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) {
      return;
    }

    try {
      if (deleteTarget.type === 'school') {
        const response = await deleteSchool(deleteTarget.entity.id).unwrap();
        toast.success(response.message || 'School deleted');
      } else {
        const response = await deletePickupPoint(deleteTarget.entity.id).unwrap();
        toast.success(response.message || 'Pickup point deleted');
      }
      setDeleteTarget(null);
    } catch (error: any) {
      toast.error(error?.data?.message || 'Delete failed');
    }
  };

  return (
    <>
      <SchoolBusPageShell
        title='Schools and pickup network'
        description='Manage school entities and the pickup-point network that powers request intake and routing.'
        actions={
          <>
            <Button
              variant='outline'
              className='rounded-full'
              onClick={() => {
                setEditingPickup(null);
                setPickupDialogOpen(true);
              }}
            >
              <Plus className='h-4 w-4' />
              Add pickup point
            </Button>
            <Button
              className='rounded-full'
              onClick={() => {
                setEditingSchool(null);
                setSchoolDialogOpen(true);
              }}
            >
              <Plus className='h-4 w-4' />
              Add school
            </Button>
          </>
        }
      >
        <div className='grid gap-4 md:grid-cols-3'>
          <SchoolBusMetricCard
            label='Schools in tenant'
            value={schools.length}
            hint='Configured campuses available for requests and routes'
            icon={Building2}
            tone='info'
          />
          <SchoolBusMetricCard
            label='Pickup points'
            value={pickupPoints.length}
            hint='Operational boarding points'
            icon={MapPinned}
            tone='success'
          />
          <SchoolBusMetricCard
            label='Contact-ready schools'
            value={
              schools.filter((school) => school.contactPhone || school.contactEmail)
                .length
            }
            hint='Schools with at least one contact channel'
            icon={Phone}
            tone='default'
          />
        </div>

        <div className='grid gap-6 xl:grid-cols-[1.2fr_0.8fr]'>
          <SchoolBusSection
            title='School directory'
            description='Create, update, and soft-delete school master data.'
          >
            {isLoading ? (
              <p className='text-sm text-muted-foreground'>Loading schools...</p>
            ) : schools.length === 0 ? (
              <SchoolBusEmptyState
                title='No schools registered yet'
                description='Add your first school to unlock routing and request setup.'
                icon={Building2}
              />
            ) : (
              <SchoolBusScrollableTable
                footer={
                  <SchoolBusPaginationBar
                    page={data?.data}
                    onPageChange={schoolsPagination.setPage}
                  />
                }
              >
                <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>School</TableHead>
                    <TableHead>Code</TableHead>
                    <TableHead>Contact</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead className='text-right'>Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {schools.map((school) => (
                    <TableRow key={school.id}>
                      <TableCell>
                        <button
                          type='button'
                          className='text-left'
                          onClick={() => {
                            setSelectedSchoolId(school.id);
                            setSelectedPickupPointId(null);
                          }}
                        >
                          <p className='font-medium'>{school.name}</p>
                          <p className='text-xs text-muted-foreground'>
                            {school.address || 'No address'}
                          </p>
                        </button>
                      </TableCell>
                      <TableCell>{school.code || 'N/A'}</TableCell>
                      <TableCell>
                        <div className='text-sm'>
                          <p>{school.contactPhone || 'No phone'}</p>
                          <p className='text-xs text-muted-foreground'>
                            {school.contactEmail || 'No email'}
                          </p>
                        </div>
                      </TableCell>
                      <TableCell>
                        <SchoolBusStatusBadge
                          status={school.isActive ? 'ACTIVE' : 'INACTIVE'}
                        />
                      </TableCell>
                      <TableCell className='text-right'>
                        <div className='flex justify-end gap-2'>
                          <Button
                            size='icon'
                            variant='outline'
                            onClick={() => {
                              setEditingSchool(school);
                              setSchoolDialogOpen(true);
                            }}
                          >
                            <Pencil className='h-4 w-4' />
                          </Button>
                          <Button
                            size='icon'
                            variant='outline'
                            onClick={() =>
                              setDeleteTarget({ type: 'school', entity: school })
                            }
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

          <SchoolBusSection
            title='Pickup point footprint'
            description='Boarding points grouped into the operational network.'
          >
            {pickupPoints.length === 0 ? (
              <SchoolBusEmptyState
                title='No pickup points available'
                description='Add pickup points to make request-to-route planning actionable.'
                icon={MapPinned}
                className='min-h-[220px]'
              />
            ) : (
              <SchoolBusScrollableTable
                footer={
                  <SchoolBusPaginationBar
                    page={pickupPointsData?.data}
                    onPageChange={pickupPagination.setPage}
                  />
                }
              >
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Pickup point</TableHead>
                      <TableHead>School</TableHead>
                      <TableHead>Address</TableHead>
                      <TableHead>Window</TableHead>
                      <TableHead className='text-right'>Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {pickupPoints.map((pickupPoint) => (
                      <TableRow key={pickupPoint.id}>
                        <TableCell>
                          <button
                            type='button'
                            className='text-left font-medium'
                            onClick={() => {
                              setSelectedPickupPointId(pickupPoint.id);
                              setSelectedSchoolId(pickupPoint.schoolId);
                            }}
                          >
                            {pickupPoint.name}
                          </button>
                        </TableCell>
                        <TableCell>{pickupPoint.schoolName}</TableCell>
                        <TableCell>{pickupPoint.address}</TableCell>
                        <TableCell>
                          {pickupPoint.pickupWindowStart || 'N/A'} -{' '}
                          {pickupPoint.pickupWindowEnd || 'N/A'}
                        </TableCell>
                        <TableCell className='text-right'>
                          <div className='flex justify-end gap-2'>
                            <Button
                              size='icon'
                              variant='outline'
                              onClick={() => {
                                setEditingPickup(pickupPoint);
                                setPickupDialogOpen(true);
                              }}
                            >
                              <Pencil className='h-4 w-4' />
                            </Button>
                            <Button
                              size='icon'
                              variant='outline'
                              onClick={() =>
                                setDeleteTarget({
                                  type: 'pickup',
                                  entity: pickupPoint,
                                })
                              }
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
        </div>

        <SchoolBusSection
          title='Map workspace'
          description='Use the map to inspect schools and pickup points together. Rows and markers stay synchronized.'
        >
          <div className='space-y-3'>
            <p className='text-sm text-muted-foreground'>
              Selected school: {schools.find((school) => school.id === selectedSchoolId)?.name || 'None'}
              {' · '}
              Selected pickup point:{' '}
              {pickupPoints.find((pickup) => pickup.id === selectedPickupPointId)?.name ||
                'None'}
            </p>
            <OperationsMap
              schools={schools}
              pickupPoints={
                selectedSchoolId
                  ? pickupPoints.filter(
                      (pickupPoint) => pickupPoint.schoolId === selectedSchoolId
                    )
                  : pickupPoints
              }
              selectedSchoolId={selectedSchoolId}
              selectedPickupPointId={selectedPickupPointId}
              onSchoolSelect={(id) => {
                setSelectedSchoolId(id);
                setSelectedPickupPointId(null);
              }}
              onPickupPointSelect={(id) => {
                const pickupPoint = pickupPoints.find((item) => item.id === id);
                setSelectedPickupPointId(id);
                setSelectedSchoolId(pickupPoint?.schoolId ?? null);
              }}
            />
          </div>
        </SchoolBusSection>
      </SchoolBusPageShell>

      <SchoolFormDialog
        open={schoolDialogOpen}
        onOpenChange={(open) => {
          setSchoolDialogOpen(open);
          if (!open) {
            setEditingSchool(null);
          }
        }}
        initialData={editingSchool}
        isLoading={creatingSchool || updatingSchool}
        onSubmit={handleSaveSchool}
      />

      <PickupPointFormDialog
        open={pickupDialogOpen}
        onOpenChange={(open) => {
          setPickupDialogOpen(open);
          if (!open) {
            setEditingPickup(null);
          }
        }}
        initialData={editingPickup}
        schools={allSchools.length > 0 ? allSchools : schools}
        isLoading={creatingPickup || updatingPickup}
        onSubmit={handleSavePickupPoint}
      />

      <SchoolBusDeleteDialog
        open={Boolean(deleteTarget)}
        onOpenChange={(open) => {
          if (!open) {
            setDeleteTarget(null);
          }
        }}
        title={
          deleteTarget?.type === 'school'
            ? 'Delete school'
            : 'Delete pickup point'
        }
        description={
          deleteTarget?.type === 'school'
            ? 'This will soft-delete the school record. Existing historical data remains preserved.'
            : 'This will soft-delete the pickup point from operational use.'
        }
        isLoading={deletingSchool || deletingPickup}
        onConfirm={handleDelete}
      />
    </>
  );
}
