'use client';

import * as React from 'react';
import {
  BusFront,
  Pencil,
  Plus,
  ShieldCheck,
  Trash2,
  UserRound,
  Warehouse,
} from 'lucide-react';
import { toast } from 'sonner';
import { useAppSelector } from '@/lib/store';
import { selectUserProfile } from '@/modules/account/store';
import { Button } from '@/shared/components/ui';
import { useGetSchoolBusModuleUsersQuery } from '../api/schoolBusAccountApi';
import {
  useCreateAttendantMutation,
  useCreateBusMutation,
  useCreateDepotMutation,
  useCreateDriverMutation,
  useDeleteAttendantMutation,
  useDeleteBusMutation,
  useDeleteDepotMutation,
  useDeleteDriverMutation,
  useGetAttendantsQuery,
  useGetBusTypesQuery,
  useGetBusesQuery,
  useGetDepotsQuery,
  useGetDriversQuery,
  useUpdateAttendantMutation,
  useUpdateBusMutation,
  useUpdateDepotMutation,
  useUpdateDriverMutation,
} from '../api/schoolBusApi';
import { SchoolBusDeleteDialog } from '../components/SchoolBusDeleteDialog';
import {
  AttendantFormDialog,
  BusFormDialog,
  DepotFormDialog,
  DriverFormDialog,
} from '../components/SchoolBusMasterDataForms';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusPaginationBar } from '../components/SchoolBusPaginationBar';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusScrollableTable } from '../components/SchoolBusScrollableTable';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { SchoolBusTableTabs } from '../components/SchoolBusTableTabs';
import { SCHOOL_BUS_ACCOUNT_MODULE_ID } from '../constants';
import { useSchoolBusPagination } from '../hooks/useSchoolBusPagination';
import type {
  SchoolBusAccountUser,
  SchoolBusAttendant,
  SchoolBusBus,
  SchoolBusDepot,
  SchoolBusDriver,
} from '../types';
import { getPageItems, SCHOOL_BUS_OPTION_QUERY } from '../utils';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';

type FleetDeleteTarget =
  | { type: 'bus'; entity: SchoolBusBus }
  | { type: 'driver'; entity: SchoolBusDriver }
  | { type: 'attendant'; entity: SchoolBusAttendant }
  | { type: 'depot'; entity: SchoolBusDepot }
  | null;

export function SchoolBusFleetPage() {
  const currentUser = useAppSelector(selectUserProfile);
  const organizationId = currentUser?.organizationId;
  const busPagination = useSchoolBusPagination({
    page: 0,
    size: 10,
    sortBy: 'plateNumber',
    sortDirection: 'ASC',
  });
  const driverPagination = useSchoolBusPagination({
    page: 0,
    size: 10,
    sortBy: 'fullName',
    sortDirection: 'ASC',
  });
  const attendantPagination = useSchoolBusPagination({
    page: 0,
    size: 10,
    sortBy: 'fullName',
    sortDirection: 'ASC',
  });
  const depotPagination = useSchoolBusPagination({
    page: 0,
    size: 10,
    sortBy: 'name',
    sortDirection: 'ASC',
  });
  const { data: busesData } = useGetBusesQuery(busPagination.params);
  const { data: driversData } = useGetDriversQuery(driverPagination.params);
  const { data: attendantsData } = useGetAttendantsQuery(
    attendantPagination.params
  );
  const { data: depotsPageData } = useGetDepotsQuery(depotPagination.params);
  const [busDialogOpen, setBusDialogOpen] = React.useState(false);
  const [driverDialogOpen, setDriverDialogOpen] = React.useState(false);
  const [attendantDialogOpen, setAttendantDialogOpen] = React.useState(false);
  const [depotDialogOpen, setDepotDialogOpen] = React.useState(false);
  const accountUserDialogOpen = driverDialogOpen || attendantDialogOpen;
  const { data: accountUsersData, isFetching: loadingAccountUsers } =
    useGetSchoolBusModuleUsersQuery(
      {
        organizationId: organizationId || 0,
        moduleId: SCHOOL_BUS_ACCOUNT_MODULE_ID,
      },
      { skip: !accountUserDialogOpen || !organizationId }
    );
  const { data: driverOptionsData } = useGetDriversQuery(
    {
      page: 0,
      size: 100,
      sortBy: 'fullName',
      sortDirection: 'ASC',
    },
    { skip: !driverDialogOpen }
  );
  const { data: attendantOptionsData } = useGetAttendantsQuery(
    {
      page: 0,
      size: 100,
      sortBy: 'fullName',
      sortDirection: 'ASC',
    },
    { skip: !attendantDialogOpen }
  );
  const { data: busTypesData, isFetching: loadingBusTypes } =
    useGetBusTypesQuery(undefined, { skip: !busDialogOpen });
  const { data: depotsData } = useGetDepotsQuery(
    { ...SCHOOL_BUS_OPTION_QUERY, sortBy: 'name' },
    { skip: !busDialogOpen && !depotDialogOpen }
  );
  const [createBus, { isLoading: creatingBus }] = useCreateBusMutation();
  const [updateBus, { isLoading: updatingBus }] = useUpdateBusMutation();
  const [deleteBus, { isLoading: deletingBus }] = useDeleteBusMutation();
  const [createDriver, { isLoading: creatingDriver }] = useCreateDriverMutation();
  const [updateDriver, { isLoading: updatingDriver }] = useUpdateDriverMutation();
  const [deleteDriver, { isLoading: deletingDriver }] = useDeleteDriverMutation();
  const [createAttendant, { isLoading: creatingAttendant }] =
    useCreateAttendantMutation();
  const [updateAttendant, { isLoading: updatingAttendant }] =
    useUpdateAttendantMutation();
  const [deleteAttendant, { isLoading: deletingAttendant }] =
    useDeleteAttendantMutation();
  const [createDepot, { isLoading: creatingDepot }] = useCreateDepotMutation();
  const [updateDepot, { isLoading: updatingDepot }] = useUpdateDepotMutation();
  const [deleteDepot, { isLoading: deletingDepot }] = useDeleteDepotMutation();

  const buses = getPageItems(busesData?.data);
  const drivers = getPageItems(driversData?.data);
  const attendants = getPageItems(attendantsData?.data);
  const depotsPage = getPageItems(depotsPageData?.data);
  const driverOptions = getPageItems(driverOptionsData?.data);
  const attendantOptions = getPageItems(attendantOptionsData?.data);
  const accountUsers = accountUsersData?.data || [];
  const busTypes = busTypesData?.data || [];
  const depots = getPageItems(depotsData?.data);
  const activeDrivers = drivers.filter((driver) => driver.isActive !== false).length;
  const activeAttendants = attendants.filter(
    (attendant) => attendant.isActive !== false
  ).length;

  const [editingBus, setEditingBus] = React.useState<SchoolBusBus | null>(null);
  const [editingDriver, setEditingDriver] =
    React.useState<SchoolBusDriver | null>(null);
  const [editingAttendant, setEditingAttendant] =
    React.useState<SchoolBusAttendant | null>(null);
  const [editingDepot, setEditingDepot] = React.useState<SchoolBusDepot | null>(null);
  const [deleteTarget, setDeleteTarget] = React.useState<FleetDeleteTarget>(null);

  const driverDialogUsers = React.useMemo(
    () =>
      buildAvailableCrewAccountUsers({
        users: accountUsers,
        crewProfiles: driverOptions,
        editingProfile: editingDriver,
      }),
    [accountUsers, driverOptions, editingDriver]
  );

  const attendantDialogUsers = React.useMemo(
    () =>
      buildAvailableCrewAccountUsers({
        users: accountUsers,
        crewProfiles: attendantOptions,
        editingProfile: editingAttendant,
      }),
    [accountUsers, attendantOptions, editingAttendant]
  );

  const handleSaveBus = async (values: any) => {
    try {
      const response = editingBus
        ? await updateBus({ id: editingBus.id, body: values }).unwrap()
        : await createBus(values).unwrap();
      toast.success(response.message || 'Bus saved');
      setBusDialogOpen(false);
      setEditingBus(null);
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to save bus');
    }
  };

  const handleSaveDriver = async (values: any) => {
    try {
      const response = editingDriver
        ? await updateDriver({ id: editingDriver.id, body: values }).unwrap()
        : await createDriver(values).unwrap();
      toast.success(response.message || 'Driver saved');
      setDriverDialogOpen(false);
      setEditingDriver(null);
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to save driver');
    }
  };

  const handleSaveAttendant = async (values: any) => {
    try {
      const response = editingAttendant
        ? await updateAttendant({ id: editingAttendant.id, body: values }).unwrap()
        : await createAttendant(values).unwrap();
      toast.success(response.message || 'Attendant saved');
      setAttendantDialogOpen(false);
      setEditingAttendant(null);
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to save attendant');
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
      if (deleteTarget.type === 'bus') {
        const response = await deleteBus(deleteTarget.entity.id).unwrap();
        toast.success(response.message || 'Bus deleted');
      } else if (deleteTarget.type === 'driver') {
        const response = await deleteDriver(deleteTarget.entity.id).unwrap();
        toast.success(response.message || 'Driver deleted');
      } else if (deleteTarget.type === 'attendant') {
        const response = await deleteAttendant(deleteTarget.entity.id).unwrap();
        toast.success(response.message || 'Attendant deleted');
      } else {
        const response = await deleteDepot(deleteTarget.entity.id).unwrap();
        toast.success(response.message || 'Depot deleted');
      }
      setDeleteTarget(null);
    } catch (error: any) {
      toast.error(error?.data?.message || 'Delete failed');
    }
  };

  const busesTabContent = (
    <div className='space-y-4'>
      <div>
        <h3 className='text-base font-semibold text-slate-950'>Buses</h3>
        <p className='mt-1 text-sm text-slate-500'>
          Fleet assets available for route assignment.
        </p>
      </div>
      {buses.length === 0 ? (
        <SchoolBusEmptyState
          title='No buses registered'
          description='Add the first bus to make assignment possible.'
          icon={BusFront}
          className='min-h-[220px]'
        />
      ) : (
        <SchoolBusScrollableTable
          footer={
            <SchoolBusPaginationBar
              page={busesData?.data}
              onPageChange={busPagination.setPage}
            />
          }
        >
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Plate</TableHead>
                <TableHead>Type</TableHead>
                <TableHead>Capacity</TableHead>
                <TableHead>Home depot</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className='text-right'>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {buses.map((bus) => (
                <TableRow key={bus.id}>
                  <TableCell className='font-medium'>{bus.plateNumber}</TableCell>
                  <TableCell>{bus.busType || 'Standard'}</TableCell>
                  <TableCell>{bus.capacity}</TableCell>
                  <TableCell>
                    <span className='text-sm text-slate-600'>{bus.homeDepotName || 'No depot'}</span>
                  </TableCell>
                  <TableCell>
                    <SchoolBusStatusBadge status={bus.status} />
                  </TableCell>
                  <TableCell className='text-right'>
                    <div className='flex justify-end gap-2'>
                      <Button
                        size='icon'
                        variant='outline'
                        onClick={() => {
                          setEditingBus(bus);
                          setBusDialogOpen(true);
                        }}
                      >
                        <Pencil className='h-4 w-4' />
                      </Button>
                      <Button
                        size='icon'
                        variant='outline'
                        onClick={() => setDeleteTarget({ type: 'bus', entity: bus })}
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

  const driversTabContent = (
    <div className='space-y-4'>
      <div>
        <h3 className='text-base font-semibold text-slate-950'>Drivers</h3>
        <p className='mt-1 text-sm text-slate-500'>
          Driver availability and licensing visibility for dispatch.
        </p>
      </div>
      {drivers.length === 0 ? (
        <SchoolBusEmptyState
          title='No drivers onboarded'
          description='Create driver profiles to support route assignment.'
          icon={UserRound}
          className='min-h-[220px]'
        />
      ) : (
        <SchoolBusScrollableTable
          footer={
            <SchoolBusPaginationBar
              page={driversData?.data}
              onPageChange={driverPagination.setPage}
            />
          }
        >
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Driver</TableHead>
                <TableHead>Phone</TableHead>
                <TableHead>License</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className='text-right'>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {drivers.map((driver) => (
                <TableRow key={driver.id}>
                  <TableCell className='font-medium'>{driver.fullName}</TableCell>
                  <TableCell>{driver.phone || 'No phone'}</TableCell>
                  <TableCell>{driver.licenseNumber || 'No license'}</TableCell>
                  <TableCell>
                    <SchoolBusStatusBadge
                      status={driver.isActive === false ? 'INACTIVE' : driver.status}
                    />
                  </TableCell>
                  <TableCell className='text-right'>
                    <div className='flex justify-end gap-2'>
                      <Button
                        size='icon'
                        variant='outline'
                        onClick={() => {
                          setEditingDriver(driver);
                          setDriverDialogOpen(true);
                        }}
                      >
                        <Pencil className='h-4 w-4' />
                      </Button>
                      <Button
                        size='icon'
                        variant='outline'
                        onClick={() =>
                          setDeleteTarget({
                            type: 'driver',
                            entity: driver,
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

  const attendantsTabContent = (
    <div className='space-y-4'>
      <div>
        <h3 className='text-base font-semibold text-slate-950'>Attendants</h3>
        <p className='mt-1 text-sm text-slate-500'>
          Support crew available for attendance and supervision.
        </p>
      </div>
      {attendants.length === 0 ? (
        <SchoolBusEmptyState
          title='No attendants onboarded'
          description='Create attendant profiles to support attendance operations.'
          icon={ShieldCheck}
          className='min-h-[220px]'
        />
      ) : (
        <SchoolBusScrollableTable
          footer={
            <SchoolBusPaginationBar
              page={attendantsData?.data}
              onPageChange={attendantPagination.setPage}
            />
          }
        >
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Attendant</TableHead>
                <TableHead>Phone</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className='text-right'>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {attendants.map((attendant) => (
                <TableRow key={attendant.id}>
                  <TableCell className='font-medium'>{attendant.fullName}</TableCell>
                  <TableCell>{attendant.phone || 'No phone'}</TableCell>
                  <TableCell>
                    <SchoolBusStatusBadge
                      status={
                        attendant.isActive === false ? 'INACTIVE' : attendant.status
                      }
                    />
                  </TableCell>
                  <TableCell className='text-right'>
                    <div className='flex justify-end gap-2'>
                      <Button
                        size='icon'
                        variant='outline'
                        onClick={() => {
                          setEditingAttendant(attendant);
                          setAttendantDialogOpen(true);
                        }}
                      >
                        <Pencil className='h-4 w-4' />
                      </Button>
                      <Button
                        size='icon'
                        variant='outline'
                        onClick={() =>
                          setDeleteTarget({
                            type: 'attendant',
                            entity: attendant,
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
      {depotsPage.length === 0 ? (
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
              page={depotsPageData?.data}
              onPageChange={depotPagination.setPage}
            />
          }
        >
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Depot</TableHead>
                <TableHead>Code</TableHead>
                <TableHead>Address</TableHead>
                <TableHead>Contact</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className='text-right'>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {depotsPage.map((depot) => (
                <TableRow key={depot.id}>
                  <TableCell className='font-medium'>{depot.name}</TableCell>
                  <TableCell>
                    <span className='text-xs font-mono text-rose-700'>{depot.code || 'N/A'}</span>
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
        title='Fleet and crew readiness'
        description='V1 now supports direct maintenance of buses, drivers, and attendants from the School Bus console.'
        actions={
          <>
            <Button
              variant='outline'
              className='rounded-full'
              onClick={() => {
                setEditingDriver(null);
                setDriverDialogOpen(true);
              }}
            >
              <Plus className='h-4 w-4' />
              Add driver
            </Button>
            <Button
              variant='outline'
              className='rounded-full'
              onClick={() => {
                setEditingAttendant(null);
                setAttendantDialogOpen(true);
              }}
            >
              <Plus className='h-4 w-4' />
              Add attendant
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
                setEditingBus(null);
                setBusDialogOpen(true);
              }}
            >
              <Plus className='h-4 w-4' />
              Add bus
            </Button>
          </>
        }
      >
        <div className='grid gap-4 md:grid-cols-4'>
          <SchoolBusMetricCard
            label='Buses registered'
            value={buses.length}
            hint='Vehicles available for assignment'
            icon={BusFront}
            tone='info'
          />
          <SchoolBusMetricCard
            label='Active drivers'
            value={activeDrivers}
            hint='Drivers currently usable for route execution'
            icon={UserRound}
            tone='success'
          />
          <SchoolBusMetricCard
            label='Active attendants'
            value={activeAttendants}
            hint='On-board staff available for student service'
            icon={ShieldCheck}
            tone='default'
          />
          <SchoolBusMetricCard
            label='Depots'
            value={depotsPage.length}
            hint='Fixed route origins or destinations'
            icon={Warehouse}
            tone='default'
          />
        </div>

        <SchoolBusTableTabs
          defaultValue='buses'
          tabs={[
            {
              value: 'buses',
              label: 'Buses',
              count: buses.length,
              content: busesTabContent,
            },
            {
              value: 'drivers',
              label: 'Drivers',
              count: drivers.length,
              content: driversTabContent,
            },
            {
              value: 'attendants',
              label: 'Attendants',
              count: attendants.length,
              content: attendantsTabContent,
            },
            {
              value: 'depots',
              label: 'Depots',
              count: depotsPage.length,
              content: depotsTabContent,
            },
          ]}
        />
      </SchoolBusPageShell>

      <BusFormDialog
        open={busDialogOpen}
        onOpenChange={(open) => {
          setBusDialogOpen(open);
          if (!open) {
            setEditingBus(null);
          }
        }}
        initialData={editingBus}
        busTypes={busTypes}
        isLoadingBusTypes={loadingBusTypes}
        depots={depots}
        isLoading={creatingBus || updatingBus}
        onSubmit={handleSaveBus}
      />

      <DriverFormDialog
        open={driverDialogOpen}
        onOpenChange={(open) => {
          setDriverDialogOpen(open);
          if (!open) {
            setEditingDriver(null);
          }
        }}
        initialData={editingDriver}
        accountUsers={driverDialogUsers}
        isLoadingAccountUsers={loadingAccountUsers}
        isLoading={creatingDriver || updatingDriver}
        onSubmit={handleSaveDriver}
      />

      <AttendantFormDialog
        open={attendantDialogOpen}
        onOpenChange={(open) => {
          setAttendantDialogOpen(open);
          if (!open) {
            setEditingAttendant(null);
          }
        }}
        initialData={editingAttendant}
        accountUsers={attendantDialogUsers}
        isLoadingAccountUsers={loadingAccountUsers}
        isLoading={creatingAttendant || updatingAttendant}
        onSubmit={handleSaveAttendant}
      />

      <SchoolBusDeleteDialog
        open={Boolean(deleteTarget)}
        onOpenChange={(open) => {
          if (!open) {
            setDeleteTarget(null);
          }
        }}
        title={
          deleteTarget?.type === 'bus'
            ? 'Delete bus'
            : deleteTarget?.type === 'driver'
              ? 'Delete driver'
              : deleteTarget?.type === 'depot'
                ? 'Delete depot'
                : 'Delete attendant'
        }
        description='This will soft-delete the selected record from active fleet operations.'
        isLoading={deletingBus || deletingDriver || deletingAttendant || deletingDepot}
        onConfirm={handleDelete}
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
    </>
  );
}

function buildAvailableCrewAccountUsers<TProfile extends { id: number; userId: number; isActive?: boolean | null }>({
  users,
  crewProfiles,
  editingProfile,
}: {
  users: SchoolBusAccountUser[];
  crewProfiles: TProfile[];
  editingProfile: TProfile | null;
}) {
  const linkedUserIds = new Set(
    crewProfiles
      .filter((profile) => profile.id !== editingProfile?.id)
      .filter((profile) => profile.isActive !== false)
      .map((profile) => profile.userId)
  );

  const availableUsers = users.filter(
    (user) => !linkedUserIds.has(user.id) || user.id === editingProfile?.userId
  );

  if (
    editingProfile &&
    !availableUsers.some((user) => user.id === editingProfile.userId)
  ) {
    return [
      {
        id: editingProfile.userId,
        email: `user-${editingProfile.userId}@unknown.local`,
        firstName: `Account user #${editingProfile.userId}`,
        lastName: '',
        phoneNumber: null,
        status: editingProfile.isActive === false ? 'INACTIVE' : 'ACTIVE',
        userType: 'EXTERNAL',
      },
      ...availableUsers,
    ];
  }

  return availableUsers;
}
