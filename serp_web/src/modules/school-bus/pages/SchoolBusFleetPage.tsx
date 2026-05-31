'use client';

import * as React from 'react';
import {
  AlertTriangle,
  BusFront,
  ChevronDown,
  MapPin,
  Pencil,
  Plus,
  Search,
  ShieldCheck,
  Trash2,
  UserRound,
  Warehouse,
} from 'lucide-react';
import { toast } from 'sonner';
import { useAppSelector } from '@/lib/store';
import { selectUserProfile } from '@/modules/account/store';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
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
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { SchoolBusDeleteDialog } from '../components/SchoolBusDeleteDialog';
import { SchoolBusFilterSelect } from '../components/SchoolBusFilterSelect';
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
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/shared/components/ui/dropdown-menu';
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

// ── Helpers ───────────────────────────────────────────────────────────────────

function UnassignedBadge({ label = 'Unassigned' }: { label?: string }) {
  return (
    <span className='inline-flex items-center rounded-full bg-slate-100 px-2 py-0.5 text-[10px] font-medium text-slate-500 ring-1 ring-slate-200'>
      {label}
    </span>
  );
}

function WarningBadge({ label }: { label: string }) {
  return (
    <span className='inline-flex items-center gap-1 rounded-full bg-amber-50 px-2 py-0.5 text-[10px] font-semibold text-amber-700 ring-1 ring-amber-200'>
      <AlertTriangle className='h-3 w-3' />
      {label}
    </span>
  );
}

function isLicenseExpiringSoon(dateStr?: string | null): boolean {
  if (!dateStr) return false;
  const expiry = new Date(dateStr);
  const now = new Date();
  const daysLeft = (expiry.getTime() - now.getTime()) / (1000 * 60 * 60 * 24);
  return daysLeft <= 30 && daysLeft >= 0;
}

function isLicenseExpired(dateStr?: string | null): boolean {
  if (!dateStr) return false;
  return new Date(dateStr) < new Date();
}

type FleetDeleteTarget =
  | { type: 'bus'; entity: SchoolBusBus }
  | { type: 'driver'; entity: SchoolBusDriver }
  | { type: 'attendant'; entity: SchoolBusAttendant }
  | { type: 'depot'; entity: SchoolBusDepot }
  | null;

// ── Main Page ─────────────────────────────────────────────────────────────────

export function SchoolBusFleetPage() {
  const currentUser = useAppSelector(selectUserProfile);
  const organizationId = currentUser?.organizationId;

  // ─── Pagination ─────────────────────────────────────────────────
  const busPagination = useSchoolBusPagination({ page: 0, size: 10, sortBy: 'plateNumber', sortDirection: 'ASC' });
  const driverPagination = useSchoolBusPagination({ page: 0, size: 10, sortBy: 'fullName', sortDirection: 'ASC' });
  const attendantPagination = useSchoolBusPagination({ page: 0, size: 10, sortBy: 'fullName', sortDirection: 'ASC' });
  const depotPagination = useSchoolBusPagination({ page: 0, size: 10, sortBy: 'name', sortDirection: 'ASC' });

  // ─── Queries ────────────────────────────────────────────────────
  const { data: busesData } = useGetBusesQuery(busPagination.params);
  const { data: driversData } = useGetDriversQuery(driverPagination.params);
  const { data: attendantsData } = useGetAttendantsQuery(attendantPagination.params);
  const { data: depotsPageData } = useGetDepotsQuery(depotPagination.params);

  // ─── Dialog state ───────────────────────────────────────────────
  const [busDialogOpen, setBusDialogOpen] = React.useState(false);
  const [driverDialogOpen, setDriverDialogOpen] = React.useState(false);
  const [attendantDialogOpen, setAttendantDialogOpen] = React.useState(false);
  const [depotDialogOpen, setDepotDialogOpen] = React.useState(false);
  const accountUserDialogOpen = driverDialogOpen || attendantDialogOpen;

  const { data: accountUsersData, isFetching: loadingAccountUsers } = useGetSchoolBusModuleUsersQuery(
    { organizationId: organizationId || 0, moduleId: SCHOOL_BUS_ACCOUNT_MODULE_ID },
    { skip: !accountUserDialogOpen || !organizationId },
  );
  const { data: driverOptionsData } = useGetDriversQuery(
    { page: 0, size: 100, sortBy: 'fullName', sortDirection: 'ASC' },
    { skip: !driverDialogOpen },
  );
  const { data: attendantOptionsData } = useGetAttendantsQuery(
    { page: 0, size: 100, sortBy: 'fullName', sortDirection: 'ASC' },
    { skip: !attendantDialogOpen },
  );
  const { data: busTypesData, isFetching: loadingBusTypes } = useGetBusTypesQuery(undefined, { skip: !busDialogOpen });
  const { data: depotsData } = useGetDepotsQuery(
    { ...SCHOOL_BUS_OPTION_QUERY, sortBy: 'name' },
    { skip: !busDialogOpen && !depotDialogOpen },
  );

  // ─── Mutations ──────────────────────────────────────────────────
  const [createBus, { isLoading: creatingBus }] = useCreateBusMutation();
  const [updateBus, { isLoading: updatingBus }] = useUpdateBusMutation();
  const [deleteBus, { isLoading: deletingBus }] = useDeleteBusMutation();
  const [createDriver, { isLoading: creatingDriver }] = useCreateDriverMutation();
  const [updateDriver, { isLoading: updatingDriver }] = useUpdateDriverMutation();
  const [deleteDriver, { isLoading: deletingDriver }] = useDeleteDriverMutation();
  const [createAttendant, { isLoading: creatingAttendant }] = useCreateAttendantMutation();
  const [updateAttendant, { isLoading: updatingAttendant }] = useUpdateAttendantMutation();
  const [deleteAttendant, { isLoading: deletingAttendant }] = useDeleteAttendantMutation();
  const [createDepot, { isLoading: creatingDepot }] = useCreateDepotMutation();
  const [updateDepot, { isLoading: updatingDepot }] = useUpdateDepotMutation();
  const [deleteDepot, { isLoading: deletingDepot }] = useDeleteDepotMutation();

  // ─── Derived data ───────────────────────────────────────────────
  const buses = getPageItems(busesData?.data);
  const drivers = getPageItems(driversData?.data);
  const attendants = getPageItems(attendantsData?.data);
  const depotsPage = getPageItems(depotsPageData?.data);
  const driverOptions = getPageItems(driverOptionsData?.data);
  const attendantOptions = getPageItems(attendantOptionsData?.data);
  const accountUsers = accountUsersData?.data || [];
  const busTypes = busTypesData?.data || [];
  const depots = getPageItems(depotsData?.data);

  // ─── Stats ──────────────────────────────────────────────────────
  const availableBuses = buses.filter((b) => b.status === 'AVAILABLE' || b.status === 'ACTIVE').length;
  const availableDrivers = drivers.filter((d) => d.isActive !== false && d.status !== 'ON_LEAVE').length;
  const availableAttendants = attendants.filter((a) => a.isActive !== false && a.status !== 'ON_LEAVE').length;

  // ─── Filter state (per tab) ─────────────────────────────────────
  const [busSearch, setBusSearch] = React.useState('');
  const [busStatusFilter, setBusStatusFilter] = React.useState('');
  const [driverSearch, setDriverSearch] = React.useState('');
  const [driverStatusFilter, setDriverStatusFilter] = React.useState('');
  const [attendantSearch, setAttendantSearch] = React.useState('');
  const [attendantStatusFilter, setAttendantStatusFilter] = React.useState('');
  const [depotSearch, setDepotSearch] = React.useState('');

  // Client-side filters
  const filteredBuses = React.useMemo(() => {
    let result = buses;
    if (busSearch) {
      const q = busSearch.toLowerCase();
      result = result.filter((b) => b.plateNumber.toLowerCase().includes(q) || b.homeDepotName?.toLowerCase().includes(q));
    }
    if (busStatusFilter) result = result.filter((b) => b.status === busStatusFilter);
    return result;
  }, [buses, busSearch, busStatusFilter]);

  const filteredDrivers = React.useMemo(() => {
    let result = drivers;
    if (driverSearch) {
      const q = driverSearch.toLowerCase();
      result = result.filter((d) => d.fullName.toLowerCase().includes(q) || d.licenseNumber?.toLowerCase().includes(q));
    }
    if (driverStatusFilter === 'active') result = result.filter((d) => d.isActive !== false);
    if (driverStatusFilter === 'inactive') result = result.filter((d) => d.isActive === false);
    if (driverStatusFilter === 'expiring') result = result.filter((d) => isLicenseExpiringSoon(d.licenseExpiryDate));
    return result;
  }, [drivers, driverSearch, driverStatusFilter]);

  const filteredAttendants = React.useMemo(() => {
    let result = attendants;
    if (attendantSearch) {
      const q = attendantSearch.toLowerCase();
      result = result.filter((a) => a.fullName.toLowerCase().includes(q));
    }
    if (attendantStatusFilter === 'active') result = result.filter((a) => a.isActive !== false);
    if (attendantStatusFilter === 'inactive') result = result.filter((a) => a.isActive === false);
    return result;
  }, [attendants, attendantSearch, attendantStatusFilter]);

  const filteredDepots = React.useMemo(() => {
    if (!depotSearch) return depotsPage;
    const q = depotSearch.toLowerCase();
    return depotsPage.filter((d) => d.name.toLowerCase().includes(q) || d.address?.toLowerCase().includes(q));
  }, [depotsPage, depotSearch]);

  // ─── Editing state ──────────────────────────────────────────────
  const [editingBus, setEditingBus] = React.useState<SchoolBusBus | null>(null);
  const [editingDriver, setEditingDriver] = React.useState<SchoolBusDriver | null>(null);
  const [editingAttendant, setEditingAttendant] = React.useState<SchoolBusAttendant | null>(null);
  const [editingDepot, setEditingDepot] = React.useState<SchoolBusDepot | null>(null);
  const [deleteTarget, setDeleteTarget] = React.useState<FleetDeleteTarget>(null);

  const driverDialogUsers = React.useMemo(
    () => buildAvailableCrewAccountUsers({ users: accountUsers, crewProfiles: driverOptions, editingProfile: editingDriver }),
    [accountUsers, driverOptions, editingDriver],
  );
  const attendantDialogUsers = React.useMemo(
    () => buildAvailableCrewAccountUsers({ users: accountUsers, crewProfiles: attendantOptions, editingProfile: editingAttendant }),
    [accountUsers, attendantOptions, editingAttendant],
  );

  // ─── Handlers ───────────────────────────────────────────────────
  const handleSaveBus = async (values: any) => {
    try {
      const response = editingBus
        ? await updateBus({ id: editingBus.id, body: values }).unwrap()
        : await createBus(values).unwrap();
      toast.success(response.message || 'Bus saved');
      setBusDialogOpen(false); setEditingBus(null);
    } catch (error: any) { toast.error(error?.data?.message || 'Failed to save bus'); }
  };

  const handleSaveDriver = async (values: any) => {
    try {
      const response = editingDriver
        ? await updateDriver({ id: editingDriver.id, body: values }).unwrap()
        : await createDriver(values).unwrap();
      toast.success(response.message || 'Driver saved');
      setDriverDialogOpen(false); setEditingDriver(null);
    } catch (error: any) { toast.error(error?.data?.message || 'Failed to save driver'); }
  };

  const handleSaveAttendant = async (values: any) => {
    try {
      const response = editingAttendant
        ? await updateAttendant({ id: editingAttendant.id, body: values }).unwrap()
        : await createAttendant(values).unwrap();
      toast.success(response.message || 'Attendant saved');
      setAttendantDialogOpen(false); setEditingAttendant(null);
    } catch (error: any) { toast.error(error?.data?.message || 'Failed to save attendant'); }
  };

  const handleSaveDepot = async (values: any) => {
    try {
      const response = editingDepot
        ? await updateDepot({ id: editingDepot.id, body: values }).unwrap()
        : await createDepot(values).unwrap();
      toast.success(response.message || 'Depot saved');
      setDepotDialogOpen(false); setEditingDepot(null);
    } catch (error: any) { toast.error(error?.data?.message || 'Failed to save depot'); }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
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
    } catch (error: any) { toast.error(error?.data?.message || 'Delete failed'); }
  };

  // ─── Tab contents ───────────────────────────────────────────────

  const busesTabContent = (
    <div className='space-y-4'>
      {/* Filter bar */}
      <div className='flex flex-wrap items-center gap-3'>
        <div className='relative flex-1 min-w-[180px]'>
          <Search className='absolute left-2.5 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-slate-400' />
          <input
            type='text'
            placeholder='Search plate or depot...'
            value={busSearch}
            onChange={(e) => setBusSearch(e.target.value)}
            className='w-full rounded-lg border border-slate-200 bg-slate-50 py-1.5 pl-8 pr-3 text-xs outline-none focus:border-slate-300 focus:ring-1 focus:ring-slate-200'
          />
        </div>
        <SchoolBusFilterSelect
          value={busStatusFilter}
          onChange={setBusStatusFilter}
          placeholder='All statuses'
          options={[
            { label: 'Available', value: 'AVAILABLE' },
            { label: 'In use', value: 'IN_USE' },
            { label: 'Maintenance', value: 'MAINTENANCE' },
            { label: 'Retired', value: 'RETIRED' },
          ]}
        />
      </div>
      {/* Table */}
      {filteredBuses.length === 0 ? (
        <SchoolBusEmptyState
          title='No buses found'
          description={buses.length === 0 ? 'Add the first bus to make assignment possible.' : 'No buses match current filters.'}
          icon={BusFront}
          className='min-h-[220px]'
        />
      ) : (
        <SchoolBusScrollableTable footer={<SchoolBusPaginationBar page={busesData?.data} onPageChange={busPagination.setPage} />}>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Plate</TableHead>
                <TableHead>Type</TableHead>
                <TableHead>Capacity</TableHead>
                <TableHead>Home depot</TableHead>
                <TableHead>Assignment</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className='text-right'>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredBuses.map((bus) => (
                <TableRow key={bus.id}>
                  <TableCell className='font-medium'>{bus.plateNumber}</TableCell>
                  <TableCell>{bus.busType || 'Standard'}</TableCell>
                  <TableCell>{bus.capacity} seats</TableCell>
                  <TableCell>
                    {bus.homeDepotName ? (
                      <span className='text-sm'>{bus.homeDepotName}</span>
                    ) : (
                      <UnassignedBadge label='No depot' />
                    )}
                  </TableCell>
                  <TableCell>
                    <UnassignedBadge />
                  </TableCell>
                  <TableCell>
                    <SchoolBusStatusBadge status={bus.status} />
                  </TableCell>
                  <TableCell className='text-right'>
                    <div className='flex justify-end gap-2'>
                      <Button size='icon' variant='outline' onClick={() => { setEditingBus(bus); setBusDialogOpen(true); }}>
                        <Pencil className='h-4 w-4' />
                      </Button>
                      <Button size='icon' variant='outline' onClick={() => setDeleteTarget({ type: 'bus', entity: bus })}>
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
      {/* Filter bar */}
      <div className='flex flex-wrap items-center gap-3'>
        <div className='relative flex-1 min-w-[180px]'>
          <Search className='absolute left-2.5 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-slate-400' />
          <input
            type='text'
            placeholder='Search name or license...'
            value={driverSearch}
            onChange={(e) => setDriverSearch(e.target.value)}
            className='w-full rounded-lg border border-slate-200 bg-slate-50 py-1.5 pl-8 pr-3 text-xs outline-none focus:border-slate-300 focus:ring-1 focus:ring-slate-200'
          />
        </div>
        <SchoolBusFilterSelect
          value={driverStatusFilter}
          onChange={setDriverStatusFilter}
          placeholder='All statuses'
          options={[
            { label: 'Active', value: 'active' },
            { label: 'Inactive', value: 'inactive' },
            { label: 'License expiring', value: 'expiring' },
          ]}
        />
      </div>
      {/* Table */}
      {filteredDrivers.length === 0 ? (
        <SchoolBusEmptyState
          title='No drivers found'
          description={drivers.length === 0 ? 'Create driver profiles to support route assignment.' : 'No drivers match current filters.'}
          icon={UserRound}
          className='min-h-[220px]'
        />
      ) : (
        <SchoolBusScrollableTable footer={<SchoolBusPaginationBar page={driversData?.data} onPageChange={driverPagination.setPage} />}>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Driver</TableHead>
                <TableHead>Phone</TableHead>
                <TableHead>License number</TableHead>
                <TableHead>License class</TableHead>
                <TableHead>License expiry</TableHead>
                <TableHead>Assignment</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className='text-right'>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredDrivers.map((driver) => (
                <TableRow key={driver.id}>
                  <TableCell className='font-medium'>{driver.fullName}</TableCell>
                  <TableCell>{driver.phone || <span className='text-slate-400 italic'>No phone</span>}</TableCell>
                  <TableCell><span className='font-mono text-xs'>{driver.licenseNumber || '—'}</span></TableCell>
                  <TableCell>{driver.licenseClass || '—'}</TableCell>
                  <TableCell>
                    {driver.licenseExpiryDate ? (
                      <span className={cn(
                        'text-xs',
                        isLicenseExpired(driver.licenseExpiryDate) && 'font-semibold text-red-600',
                        isLicenseExpiringSoon(driver.licenseExpiryDate) && !isLicenseExpired(driver.licenseExpiryDate) && 'font-semibold text-amber-600',
                      )}>
                        {driver.licenseExpiryDate}
                        {isLicenseExpired(driver.licenseExpiryDate) && ' (expired)'}
                        {isLicenseExpiringSoon(driver.licenseExpiryDate) && !isLicenseExpired(driver.licenseExpiryDate) && ' (expiring)'}
                      </span>
                    ) : (
                      <WarningBadge label='No expiry date' />
                    )}
                  </TableCell>
                  <TableCell><UnassignedBadge /></TableCell>
                  <TableCell>
                    <SchoolBusStatusBadge status={driver.isActive === false ? 'INACTIVE' : driver.status} />
                  </TableCell>
                  <TableCell className='text-right'>
                    <div className='flex justify-end gap-2'>
                      <Button size='icon' variant='outline' onClick={() => { setEditingDriver(driver); setDriverDialogOpen(true); }}>
                        <Pencil className='h-4 w-4' />
                      </Button>
                      <Button size='icon' variant='outline' onClick={() => setDeleteTarget({ type: 'driver', entity: driver })}>
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
      {/* Filter bar */}
      <div className='flex flex-wrap items-center gap-3'>
        <div className='relative flex-1 min-w-[180px]'>
          <Search className='absolute left-2.5 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-slate-400' />
          <input
            type='text'
            placeholder='Search attendant name...'
            value={attendantSearch}
            onChange={(e) => setAttendantSearch(e.target.value)}
            className='w-full rounded-lg border border-slate-200 bg-slate-50 py-1.5 pl-8 pr-3 text-xs outline-none focus:border-slate-300 focus:ring-1 focus:ring-slate-200'
          />
        </div>
        <SchoolBusFilterSelect
          value={attendantStatusFilter}
          onChange={setAttendantStatusFilter}
          placeholder='All statuses'
          options={[
            { label: 'Active', value: 'active' },
            { label: 'Inactive', value: 'inactive' },
          ]}
        />
      </div>
      {/* Table */}
      {filteredAttendants.length === 0 ? (
        <SchoolBusEmptyState
          title='No attendants found'
          description={attendants.length === 0 ? 'Create attendant profiles to support attendance operations.' : 'No attendants match current filters.'}
          icon={ShieldCheck}
          className='min-h-[220px]'
        />
      ) : (
        <SchoolBusScrollableTable footer={<SchoolBusPaginationBar page={attendantsData?.data} onPageChange={attendantPagination.setPage} />}>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Attendant</TableHead>
                <TableHead>Phone</TableHead>
                <TableHead>Assignment</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className='text-right'>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredAttendants.map((attendant) => (
                <TableRow key={attendant.id}>
                  <TableCell className='font-medium'>{attendant.fullName}</TableCell>
                  <TableCell>{attendant.phone || <span className='text-slate-400 italic'>No phone</span>}</TableCell>
                  <TableCell><UnassignedBadge /></TableCell>
                  <TableCell>
                    <SchoolBusStatusBadge status={attendant.isActive === false ? 'INACTIVE' : attendant.status} />
                  </TableCell>
                  <TableCell className='text-right'>
                    <div className='flex justify-end gap-2'>
                      <Button size='icon' variant='outline' onClick={() => { setEditingAttendant(attendant); setAttendantDialogOpen(true); }}>
                        <Pencil className='h-4 w-4' />
                      </Button>
                      <Button size='icon' variant='outline' onClick={() => setDeleteTarget({ type: 'attendant', entity: attendant })}>
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
      {/* Filter bar */}
      <div className='flex flex-wrap items-center gap-3'>
        <div className='relative flex-1 min-w-[180px]'>
          <Search className='absolute left-2.5 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-slate-400' />
          <input
            type='text'
            placeholder='Search depot name or address...'
            value={depotSearch}
            onChange={(e) => setDepotSearch(e.target.value)}
            className='w-full rounded-lg border border-slate-200 bg-slate-50 py-1.5 pl-8 pr-3 text-xs outline-none focus:border-slate-300 focus:ring-1 focus:ring-slate-200'
          />
        </div>
      </div>
      {/* Table */}
      {filteredDepots.length === 0 ? (
        <SchoolBusEmptyState
          title='No depots found'
          description={depotsPage.length === 0 ? 'Create depots to model fixed route origins and destinations.' : 'No depots match current search.'}
          icon={Warehouse}
          className='min-h-[220px]'
        />
      ) : (
        <SchoolBusScrollableTable footer={<SchoolBusPaginationBar page={depotsPageData?.data} onPageChange={depotPagination.setPage} />}>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Depot</TableHead>
                <TableHead>Address</TableHead>
                <TableHead>Contact phone</TableHead>
                <TableHead>Coordinates</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className='text-right'>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredDepots.map((depot) => (
                <TableRow key={depot.id}>
                  <TableCell>
                    <div>
                      <p className='font-medium'>{depot.name}</p>
                      {depot.code && <p className='text-xs font-mono text-slate-700'>{depot.code}</p>}
                    </div>
                  </TableCell>
                  <TableCell>
                    <span className='text-sm'>{depot.address || <span className='text-slate-400 italic'>No address</span>}</span>
                  </TableCell>
                  <TableCell>{depot.contactPhone || <span className='text-slate-400 italic'>No phone</span>}</TableCell>
                  <TableCell>
                    {depot.latitude && depot.longitude ? (
                      <span className='inline-flex items-center gap-1 text-xs text-emerald-700'>
                        <MapPin className='h-3 w-3' /> Set
                      </span>
                    ) : (
                      <WarningBadge label='Missing coordinates' />
                    )}
                  </TableCell>
                  <TableCell>
                    <SchoolBusStatusBadge status={depot.isActive ? 'ACTIVE' : 'INACTIVE'} />
                  </TableCell>
                  <TableCell className='text-right'>
                    <div className='flex justify-end gap-2'>
                      <Button size='icon' variant='outline' onClick={() => { setEditingDepot(depot); setDepotDialogOpen(true); }}>
                        <Pencil className='h-4 w-4' />
                      </Button>
                      <Button size='icon' variant='outline' onClick={() => setDeleteTarget({ type: 'depot', entity: depot })}>
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

  // ─── Render ─────────────────────────────────────────────────────

  return (
    <>
      <SchoolBusPageShell
        title='Fleet & crew readiness'
        description='Operational fleet overview — vehicle availability, crew licensing, and depot readiness for route execution.'
        breadcrumb={
          <SchoolBusBreadcrumb items={[
            { label: 'School Bus Ops', href: '/school-bus/dispatch' },
            { label: 'Fleet & Crew', current: true },
          ]} />
        }
        actions={
          <div className='flex items-center gap-2'>
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant='outline' className='rounded-full'>
                  <Plus className='h-4 w-4' />
                  Add resource
                  <ChevronDown className='ml-1 h-3 w-3' />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align='end'>
                <DropdownMenuItem onClick={() => { setEditingDriver(null); setDriverDialogOpen(true); }}>
                  <UserRound className='h-4 w-4' /> Add driver
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => { setEditingAttendant(null); setAttendantDialogOpen(true); }}>
                  <ShieldCheck className='h-4 w-4' /> Add attendant
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => { setEditingDepot(null); setDepotDialogOpen(true); }}>
                  <Warehouse className='h-4 w-4' /> Add depot
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
            <Button className='rounded-full' onClick={() => { setEditingBus(null); setBusDialogOpen(true); }}>
              <Plus className='h-4 w-4' /> Add bus
            </Button>
          </div>
        }
      >
        {/* Readiness stats */}
        <div className='grid gap-3 grid-cols-2 lg:grid-cols-4'>
          <SchoolBusMetricCard
            label='Available buses'
            value={availableBuses}
            hint={`${buses.length} total registered`}
            icon={BusFront}
            tone='info'
          />
          <SchoolBusMetricCard
            label='Available drivers'
            value={availableDrivers}
            hint={`${drivers.length} total • ${drivers.length - availableDrivers} unavailable`}
            icon={UserRound}
            tone='success'
          />
          <SchoolBusMetricCard
            label='Available attendants'
            value={availableAttendants}
            hint={`${attendants.length} total • ${attendants.length - availableAttendants} unavailable`}
            icon={ShieldCheck}
            tone='default'
          />
          <SchoolBusMetricCard
            label='Depots'
            value={depotsPage.length}
            hint={`${depotsPage.filter((d) => d.latitude && d.longitude).length} with coordinates`}
            icon={Warehouse}
            tone='default'
          />
        </div>

        {/* Tabbed content */}
        <SchoolBusTableTabs
          defaultValue='buses'
          tabs={[
            { value: 'buses', label: 'Buses', count: buses.length, content: busesTabContent },
            { value: 'drivers', label: 'Drivers', count: drivers.length, content: driversTabContent },
            { value: 'attendants', label: 'Attendants', count: attendants.length, content: attendantsTabContent },
            { value: 'depots', label: 'Depots', count: depotsPage.length, content: depotsTabContent },
          ]}
        />
      </SchoolBusPageShell>

      {/* ─── Dialogs ─────────────────────────────────────────────── */}

      <BusFormDialog
        open={busDialogOpen}
        onOpenChange={(open) => { setBusDialogOpen(open); if (!open) setEditingBus(null); }}
        initialData={editingBus}
        busTypes={busTypes}
        isLoadingBusTypes={loadingBusTypes}
        depots={depots}
        isLoading={creatingBus || updatingBus}
        onSubmit={handleSaveBus}
      />

      <DriverFormDialog
        open={driverDialogOpen}
        onOpenChange={(open) => { setDriverDialogOpen(open); if (!open) setEditingDriver(null); }}
        initialData={editingDriver}
        accountUsers={driverDialogUsers}
        isLoadingAccountUsers={loadingAccountUsers}
        isLoading={creatingDriver || updatingDriver}
        onSubmit={handleSaveDriver}
      />

      <AttendantFormDialog
        open={attendantDialogOpen}
        onOpenChange={(open) => { setAttendantDialogOpen(open); if (!open) setEditingAttendant(null); }}
        initialData={editingAttendant}
        accountUsers={attendantDialogUsers}
        isLoadingAccountUsers={loadingAccountUsers}
        isLoading={creatingAttendant || updatingAttendant}
        onSubmit={handleSaveAttendant}
      />

      <DepotFormDialog
        open={depotDialogOpen}
        onOpenChange={(open) => { setDepotDialogOpen(open); if (!open) setEditingDepot(null); }}
        initialData={editingDepot}
        isLoading={creatingDepot || updatingDepot}
        onSubmit={handleSaveDepot}
      />

      <SchoolBusDeleteDialog
        open={Boolean(deleteTarget)}
        onOpenChange={(open) => { if (!open) setDeleteTarget(null); }}
        title={
          deleteTarget?.type === 'bus' ? 'Delete bus'
            : deleteTarget?.type === 'driver' ? 'Delete driver'
              : deleteTarget?.type === 'depot' ? 'Delete depot'
                : 'Delete attendant'
        }
        description='This will soft-delete the selected record from active fleet operations.'
        isLoading={deletingBus || deletingDriver || deletingAttendant || deletingDepot}
        onConfirm={handleDelete}
      />
    </>
  );
}

// ── Helper: filter account users for crew assignment ──────────────────────────

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
      .map((profile) => profile.userId),
  );

  const availableUsers = users.filter(
    (user) => !linkedUserIds.has(user.id) || user.id === editingProfile?.userId,
  );

  if (editingProfile && !availableUsers.some((user) => user.id === editingProfile.userId)) {
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
