'use client';

import * as React from 'react';
import { Building2, MapPinned, Pencil, Plus, Trash2, Warehouse } from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/shared/components/ui';
import {
  useCreatePickupPointMutation,
  useCreateDepotMutation,
  useCreateSchoolMutation,
  useDeleteDepotMutation,
  useDeletePickupPointMutation,
  useDeleteSchoolMutation,
  useGetDepotsQuery,
  useGetPickupPointsQuery,
  useGetSchoolsQuery,
  useUpdateDepotMutation,
  useUpdatePickupPointMutation,
  useUpdateSchoolMutation,
} from '../api/schoolBusApi';
import { SchoolBusDeleteDialog } from '../components/SchoolBusDeleteDialog';
import {
  DepotFormDialog,
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
import { SchoolBusTableTabs } from '../components/SchoolBusTableTabs';
import { OperationsMap } from '../components/map/OperationsMap';
import { SchoolBusMapLegend } from '../components/map/SchoolBusMapLegend';
import { SchoolBusMapWorkspace } from '../components/map/SchoolBusMapWorkspace';
import { useSchoolBusPagination } from '../hooks/useSchoolBusPagination';
import type { SchoolBusDepot, SchoolBusPickupPoint, SchoolBusSchool } from '../types';
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
  | { type: 'depot'; entity: SchoolBusDepot }
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
  const depotPagination = useSchoolBusPagination({
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
  const { data: depotsData } = useGetDepotsQuery(depotPagination.params);
  const [createSchool, { isLoading: creatingSchool }] = useCreateSchoolMutation();
  const [updateSchool, { isLoading: updatingSchool }] = useUpdateSchoolMutation();
  const [deleteSchool, { isLoading: deletingSchool }] = useDeleteSchoolMutation();
  const [createPickupPoint, { isLoading: creatingPickup }] =
    useCreatePickupPointMutation();
  const [updatePickupPoint, { isLoading: updatingPickup }] =
    useUpdatePickupPointMutation();
  const [deletePickupPoint, { isLoading: deletingPickup }] =
    useDeletePickupPointMutation();
  const [createDepot, { isLoading: creatingDepot }] = useCreateDepotMutation();
  const [updateDepot, { isLoading: updatingDepot }] = useUpdateDepotMutation();
  const [deleteDepot, { isLoading: deletingDepot }] = useDeleteDepotMutation();

  const schools = getPageItems(data?.data);
  const allSchools = getPageItems(allSchoolsData?.data);
  const pickupPoints = getPageItems(pickupPointsData?.data);
  const depots = getPageItems(depotsData?.data);

  const [schoolDialogOpen, setSchoolDialogOpen] = React.useState(false);
  const [pickupDialogOpen, setPickupDialogOpen] = React.useState(false);
  const [depotDialogOpen, setDepotDialogOpen] = React.useState(false);
  const [editingSchool, setEditingSchool] = React.useState<SchoolBusSchool | null>(
    null
  );
  const [editingPickup, setEditingPickup] =
    React.useState<SchoolBusPickupPoint | null>(null);
  const [editingDepot, setEditingDepot] = React.useState<SchoolBusDepot | null>(
    null
  );
  const [deleteTarget, setDeleteTarget] = React.useState<DeleteTarget>(null);
  const [selectedSchoolId, setSelectedSchoolId] = React.useState<number | null>(
    null
  );
  const [selectedPickupPointId, setSelectedPickupPointId] =
    React.useState<number | null>(null);
  const [selectedDepotId, setSelectedDepotId] = React.useState<number | null>(
    null
  );

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

  const handleSaveDepot = async (values: any) => {
    try {
      const response = editingDepot
        ? await updateDepot({ id: editingDepot.id, body: values }).unwrap()
        : await createDepot(values).unwrap();
      toast.success(response.message || 'Depot saved');
      setDepotDialogOpen(false);
      setEditingDepot(null);
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to save depot');
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
      } else if (deleteTarget.type === 'pickup') {
        const response = await deletePickupPoint(deleteTarget.entity.id).unwrap();
        toast.success(response.message || 'Pickup point deleted');
      } else {
        const response = await deleteDepot(deleteTarget.entity.id).unwrap();
        toast.success(response.message || 'Depot deleted');
      }
      setDeleteTarget(null);
    } catch (error: any) {
      toast.error(error?.data?.message || 'Delete failed');
    }
  };

  const schoolsTabContent = (
    <div className='space-y-4'>
      <div>
        <h3 className='text-base font-semibold text-slate-950'>School directory</h3>
        <p className='mt-1 text-sm text-slate-500'>
          Create, update, and soft-delete school master data.
        </p>
      </div>
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
    </div>
  );

  const pickupPointsTabContent = (
    <div className='space-y-4'>
      <div>
        <h3 className='text-base font-semibold text-slate-950'>
          Pickup point footprint
        </h3>
        <p className='mt-1 text-sm text-slate-500'>
          Boarding points grouped into the operational network.
        </p>
      </div>
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
    </div>
  );

  const depotsTabContent = (
    <div className='space-y-4'>
      <div>
        <h3 className='text-base font-semibold text-slate-950'>
          Depot directory
        </h3>
        <p className='mt-1 text-sm text-slate-500'>
          Fixed bus yards available as explicit route start or end points.
        </p>
      </div>
      {depots.length === 0 ? (
        <SchoolBusEmptyState
          title='No depots registered yet'
          description='Create depots to model fixed route origins and destinations explicitly.'
          icon={Warehouse}
          className='min-h-[220px]'
        />
      ) : (
        <SchoolBusScrollableTable
          footer={
            <SchoolBusPaginationBar
              page={depotsData?.data}
              onPageChange={depotPagination.setPage}
            />
          }
        >
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Depot</TableHead>
                <TableHead>Address</TableHead>
                <TableHead>Contact</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className='text-right'>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {depots.map((depot) => (
                <TableRow key={depot.id}>
                  <TableCell>
                    <button
                      type='button'
                      className='text-left font-medium'
                      onClick={() => {
                        setSelectedDepotId(depot.id);
                        setSelectedPickupPointId(null);
                      }}
                    >
                      {depot.name}
                    </button>
                  </TableCell>
                  <TableCell>{depot.address || 'No address'}</TableCell>
                  <TableCell>{depot.contactPhone || 'No phone'}</TableCell>
                  <TableCell>
                    <SchoolBusStatusBadge
                      status={depot.isActive ? 'ACTIVE' : 'INACTIVE'}
                    />
                  </TableCell>
                  <TableCell className='text-right'>
                    <div className='flex justify-end gap-2'>
                      <Button
                        size='icon'
                        variant='outline'
                        onClick={() => {
                          setEditingDepot(depot);
                          setDepotDialogOpen(true);
                        }}
                      >
                        <Pencil className='h-4 w-4' />
                      </Button>
                      <Button
                        size='icon'
                        variant='outline'
                        onClick={() =>
                          setDeleteTarget({ type: 'depot', entity: depot })
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
    </div>
  );

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
              variant='outline'
              className='rounded-full'
              onClick={() => {
                setEditingDepot(null);
                setDepotDialogOpen(true);
              }}
            >
              <Plus className='h-4 w-4' />
              Add depot
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
            label='Depots'
            value={depots.length}
            hint='Fixed route origins or destinations'
            icon={Warehouse}
            tone='default'
          />
        </div>

        <SchoolBusTableTabs
          defaultValue='schools'
          tabs={[
            {
              value: 'schools',
              label: 'Schools',
              count: schools.length,
              content: schoolsTabContent,
            },
            {
              value: 'pickup-points',
              label: 'Pickup points',
              count: pickupPoints.length,
              content: pickupPointsTabContent,
            },
            {
              value: 'depots',
              label: 'Depots',
              count: depots.length,
              content: depotsTabContent,
            },
          ]}
        />

                <SchoolBusSection
          title='Map workspace'
          description='Use the map to inspect schools, pickup points, and depots together. Rows and markers stay synchronized.'
        >
          <SchoolBusMapWorkspace
            defaultPreset='map-focus'
            map={
              <OperationsMap
                schools={schools}
                depots={depots}
                pickupPoints={
                  selectedSchoolId
                    ? pickupPoints.filter(
                        (pickupPoint) => pickupPoint.schoolId === selectedSchoolId
                      )
                    : pickupPoints
                }
                selectedSchoolId={selectedSchoolId}
                selectedPickupPointId={selectedPickupPointId}
                selectedDepotId={selectedDepotId}
                onSchoolSelect={(id) => {
                  setSelectedSchoolId(id);
                  setSelectedPickupPointId(null);
                  setSelectedDepotId(null);
                }}
                onPickupPointSelect={(id) => {
                  const pickupPoint = pickupPoints.find((item) => item.id === id);
                  setSelectedPickupPointId(id);
                  setSelectedSchoolId(pickupPoint?.schoolId ?? null);
                  setSelectedDepotId(null);
                }}
                onDepotSelect={(id) => {
                  setSelectedDepotId(id);
                  setSelectedPickupPointId(null);
                }}
                className='h-full w-full'
              />
            }
            legend={<SchoolBusMapLegend />}
            panel={
              <div className='space-y-4'>
                <p className='text-sm font-semibold text-slate-950'>
                  Selection context
                </p>
                <p className='text-xs text-slate-500'>
                  School:{' '}
                  {schools.find((school) => school.id === selectedSchoolId)?.name ||
                    'None'}
                </p>
                <p className='text-xs text-slate-500'>
                  Pickup point:{' '}
                  {pickupPoints.find((pickup) => pickup.id === selectedPickupPointId)?.name ||
                    'None'}
                </p>
                <p className='text-xs text-slate-500'>
                  Depot:{' '}
                  {depots.find((depot) => depot.id === selectedDepotId)?.name ||
                    'None'}
                </p>
                <p className='text-xs text-slate-500'>
                  Click markers or table rows to keep map selection synchronized.
                </p>
              </div>
            }
          />
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

      <DepotFormDialog
        open={depotDialogOpen}
        onOpenChange={(open) => {
          setDepotDialogOpen(open);
          if (!open) {
            setEditingDepot(null);
          }
        }}
        initialData={editingDepot}
        isLoading={creatingDepot || updatingDepot}
        onSubmit={handleSaveDepot}
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
            : deleteTarget?.type === 'pickup'
              ? 'Delete pickup point'
              : 'Delete depot'
        }
        description={
          deleteTarget?.type === 'school'
            ? 'This will soft-delete the school record. Existing historical data remains preserved.'
            : deleteTarget?.type === 'pickup'
              ? 'This will soft-delete the pickup point from operational use.'
              : 'This will soft-delete the depot from route planning use.'
        }
        isLoading={deletingSchool || deletingPickup || deletingDepot}
        onConfirm={handleDelete}
      />
    </>
  );
}

