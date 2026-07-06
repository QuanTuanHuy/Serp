'use client';

import * as React from 'react';
import {
  AlertTriangle,
  Bus,
  ChevronDown,
  Eye,
  MapPin,
  Pencil,
  Plus,
  Search,
  ShieldCheck,
  Trash2,
  UserCog,
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
  useGetAttendantByIdQuery,
  useGetBusByIdQuery,
  useGetBusTypesQuery,
  useGetBusesQuery,
  useGetDepotByIdQuery,
  useGetDepotsQuery,
  useGetDriverByIdQuery,
  useGetDriversQuery,
  useUpdateAttendantMutation,
  useUpdateBusMutation,
  useUpdateDepotMutation,
  useUpdateDriverMutation,
} from '../api/schoolBusApi';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { SchoolBusDeleteDialog } from '../components/SchoolBusDeleteDialog';
import { SchoolBusSelect } from '../components/ui/SchoolBusSelect';
import {
  AttendantFormDialog,
  BusFormDialog,
  DepotFormDialog,
  DriverFormDialog,
} from '../components/SchoolBusMasterDataForms';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import {
  SchoolBusReadOnlyDetailDialog,
  type SchoolBusDetailSection,
} from '../components/SchoolBusReadOnlyDetailDialog';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { SchoolBusDataTable } from '../components/ui/SchoolBusDataTable';
import type { SchoolBusTableColumn } from '../components/ui/SchoolBusDataTable';
import { SCHOOL_BUS_ACCOUNT_MODULE_ID } from '../constants';
import {
  busStatusLabel,
  profileStatusLabel,
  staffStatusLabel,
} from '../schoolBusLabels';
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
  SchoolBusAttendantUpsertRequest,
  SchoolBusBus,
  SchoolBusBusUpsertRequest,
  SchoolBusDepot,
  SchoolBusDepotUpsertRequest,
  SchoolBusDriver,
  SchoolBusDriverUpsertRequest,
} from '../types';
import {
  getPageItems,
  SCHOOL_BUS_OPTION_QUERY,
  SCHOOL_BUS_PAGE_QUERY_OPTIONS,
} from '../utils';

// -- Helpers -------------------------------------------------------------------

function UnassignedBadge({ label = 'Chưa gán' }: { label?: string }) {
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

type FleetDeleteTarget =
  | { type: 'bus'; entity: SchoolBusBus }
  | { type: 'driver'; entity: SchoolBusDriver }
  | { type: 'attendant'; entity: SchoolBusAttendant }
  | { type: 'depot'; entity: SchoolBusDepot }
  | null;

type FleetViewTarget =
  | { type: 'bus'; id: number }
  | { type: 'driver'; id: number }
  | { type: 'attendant'; id: number }
  | { type: 'depot'; id: number }
  | null;

// -- Main Page -----------------------------------------------------------------

export function SchoolBusFleetPage() {
  const currentUser = useAppSelector(selectUserProfile);
  const organizationId = currentUser?.organizationId;

  // --- Active Tab -------------------------------------------------
  const [activeTab, setActiveTab] = React.useState('buses');

  // --- Pagination -------------------------------------------------
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

  // --- Queries ----------------------------------------------------
  const { data: busesData, isLoading: loadingBuses } = useGetBusesQuery(
    busPagination.params,
    { ...SCHOOL_BUS_PAGE_QUERY_OPTIONS, skip: activeTab !== 'buses' }
  );
  const { data: driversData, isLoading: loadingDrivers } = useGetDriversQuery(
    driverPagination.params,
    { ...SCHOOL_BUS_PAGE_QUERY_OPTIONS, skip: activeTab !== 'drivers' }
  );
  const { data: attendantsData, isLoading: loadingAttendants } =
    useGetAttendantsQuery(
      attendantPagination.params,
      { ...SCHOOL_BUS_PAGE_QUERY_OPTIONS, skip: activeTab !== 'attendants' }
    );
  const { data: depotsPageData, isLoading: loadingDepots } = useGetDepotsQuery(
    depotPagination.params,
    { ...SCHOOL_BUS_PAGE_QUERY_OPTIONS, skip: activeTab !== 'depots' }
  );

  // --- Dialog state -----------------------------------------------
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
    { page: 0, size: 100, sortBy: 'fullName', sortDirection: 'ASC' },
    { skip: !driverDialogOpen }
  );
  const { data: attendantOptionsData } = useGetAttendantsQuery(
    { page: 0, size: 100, sortBy: 'fullName', sortDirection: 'ASC' },
    { skip: !attendantDialogOpen }
  );
  const { data: busTypesData, isFetching: loadingBusTypes } =
    useGetBusTypesQuery(undefined, { skip: !busDialogOpen });
  const { data: depotsData } = useGetDepotsQuery(
    { ...SCHOOL_BUS_OPTION_QUERY, sortBy: 'name' },
    { skip: !busDialogOpen && !depotDialogOpen && activeTab !== 'buses' }
  );

  // --- Mutations --------------------------------------------------
  const [createBus, { isLoading: creatingBus }] = useCreateBusMutation();
  const [updateBus, { isLoading: updatingBus }] = useUpdateBusMutation();
  const [deleteBus, { isLoading: deletingBus }] = useDeleteBusMutation();
  const [createDriver, { isLoading: creatingDriver }] =
    useCreateDriverMutation();
  const [updateDriver, { isLoading: updatingDriver }] =
    useUpdateDriverMutation();
  const [deleteDriver, { isLoading: deletingDriver }] =
    useDeleteDriverMutation();
  const [createAttendant, { isLoading: creatingAttendant }] =
    useCreateAttendantMutation();
  const [updateAttendant, { isLoading: updatingAttendant }] =
    useUpdateAttendantMutation();
  const [deleteAttendant, { isLoading: deletingAttendant }] =
    useDeleteAttendantMutation();
  const [createDepot, { isLoading: creatingDepot }] = useCreateDepotMutation();
  const [updateDepot, { isLoading: updatingDepot }] = useUpdateDepotMutation();
  const [deleteDepot, { isLoading: deletingDepot }] = useDeleteDepotMutation();

  // --- Derived data -----------------------------------------------
  const buses = getPageItems(busesData?.data);
  const drivers = getPageItems(driversData?.data);
  const attendants = getPageItems(attendantsData?.data);
  const depotsPage = getPageItems(depotsPageData?.data);
  const driverOptions = getPageItems(driverOptionsData?.data);
  const attendantOptions = getPageItems(attendantOptionsData?.data);
  const accountUsers = accountUsersData?.data || [];
  const busTypes = busTypesData?.data || [];
  const depots = getPageItems(depotsData?.data);

  // --- Stats ------------------------------------------------------
  const availableBuses = buses.filter(
    (b) => b.status === 'AVAILABLE' || b.status === 'ACTIVE'
  ).length;
  const availableDrivers = drivers.filter(
    (d) => d.isActive !== false && d.status !== 'ON_LEAVE'
  ).length;
  const availableAttendants = attendants.filter(
    (a) => a.isActive !== false && a.status !== 'ON_LEAVE'
  ).length;

  // --- Filter state (per tab) -------------------------------------
  const [busSearch, setBusSearch] = React.useState('');
  const [busStatusFilter, setBusStatusFilter] = React.useState('');
  const [busDepotFilter, setBusDepotFilter] = React.useState('');

  const [driverSearch, setDriverSearch] = React.useState('');
  const [driverStatusFilter, setDriverStatusFilter] = React.useState('');

  const [attendantSearch, setAttendantSearch] = React.useState('');
  const [attendantStatusFilter, setAttendantStatusFilter] = React.useState('');

  const [depotSearch, setDepotSearch] = React.useState('');
  const [depotCoordsFilter, setDepotCoordsFilter] = React.useState('');

  // Client-side filters
  const filteredBuses = React.useMemo(() => {
    let result = buses;
    if (busSearch) {
      const q = busSearch.toLowerCase();
      result = result.filter(
        (b) =>
          b.plateNumber.toLowerCase().includes(q) ||
          b.homeDepotName?.toLowerCase().includes(q)
      );
    }
    if (busStatusFilter)
      result = result.filter((b) => b.status === busStatusFilter);
    if (busDepotFilter)
      result = result.filter((b) => b.homeDepotId === Number(busDepotFilter));
    return result;
  }, [buses, busSearch, busStatusFilter, busDepotFilter]);

  const filteredDrivers = React.useMemo(() => {
    let result = drivers;
    if (driverSearch) {
      const q = driverSearch.toLowerCase();
      result = result.filter(
        (d) =>
          d.fullName.toLowerCase().includes(q) ||
          d.phone?.toLowerCase().includes(q)
      );
    }
    if (driverStatusFilter === 'active')
      result = result.filter((d) => d.isActive !== false);
    if (driverStatusFilter === 'inactive')
      result = result.filter((d) => d.isActive === false);
    return result;
  }, [drivers, driverSearch, driverStatusFilter]);

  const filteredAttendants = React.useMemo(() => {
    let result = attendants;
    if (attendantSearch) {
      const q = attendantSearch.toLowerCase();
      result = result.filter((a) => a.fullName.toLowerCase().includes(q));
    }
    if (attendantStatusFilter === 'active')
      result = result.filter((a) => a.isActive !== false);
    if (attendantStatusFilter === 'inactive')
      result = result.filter((a) => a.isActive === false);
    return result;
  }, [attendants, attendantSearch, attendantStatusFilter]);

  const filteredDepots = React.useMemo(() => {
    let result = depotsPage;
    if (depotSearch) {
      const q = depotSearch.toLowerCase();
      result = result.filter(
        (d) =>
          d.name.toLowerCase().includes(q) ||
          d.address?.toLowerCase().includes(q)
      );
    }
    if (depotCoordsFilter === 'set') {
      result = result.filter((d) => d.latitude && d.longitude);
    }
    if (depotCoordsFilter === 'missing') {
      result = result.filter((d) => !d.latitude || !d.longitude);
    }
    return result;
  }, [depotsPage, depotSearch, depotCoordsFilter]);

  // --- Editing state ----------------------------------------------
  const [editingBusId, setEditingBusId] = React.useState<number | null>(null);
  const [editingDriverId, setEditingDriverId] = React.useState<number | null>(
    null
  );
  const [editingAttendantId, setEditingAttendantId] = React.useState<
    number | null
  >(null);
  const [editingDepotId, setEditingDepotId] = React.useState<number | null>(
    null
  );
  const [viewTarget, setViewTarget] = React.useState<FleetViewTarget>(null);
  const [deleteTarget, setDeleteTarget] =
    React.useState<FleetDeleteTarget>(null);

  const selectedBusId =
    editingBusId || (viewTarget?.type === 'bus' ? viewTarget.id : null);
  const selectedDriverId =
    editingDriverId || (viewTarget?.type === 'driver' ? viewTarget.id : null);
  const selectedAttendantId =
    editingAttendantId ||
    (viewTarget?.type === 'attendant' ? viewTarget.id : null);
  const selectedDepotId =
    editingDepotId || (viewTarget?.type === 'depot' ? viewTarget.id : null);

  const {
    data: selectedBusData,
    isFetching: loadingSelectedBus,
    isError: selectedBusError,
  } = useGetBusByIdQuery(selectedBusId || 0, { skip: !selectedBusId });
  const {
    data: selectedDriverData,
    isFetching: loadingSelectedDriver,
    isError: selectedDriverError,
  } = useGetDriverByIdQuery(selectedDriverId || 0, {
    skip: !selectedDriverId,
  });
  const {
    data: selectedAttendantData,
    isFetching: loadingSelectedAttendant,
    isError: selectedAttendantError,
  } = useGetAttendantByIdQuery(selectedAttendantId || 0, {
    skip: !selectedAttendantId,
  });
  const {
    data: selectedDepotData,
    isFetching: loadingSelectedDepot,
    isError: selectedDepotError,
  } = useGetDepotByIdQuery(selectedDepotId || 0, { skip: !selectedDepotId });

  const editingBus = editingBusId ? (selectedBusData?.data || null) : null;
  const editingDriver = editingDriverId
    ? (selectedDriverData?.data || null)
    : null;
  const editingAttendant = editingAttendantId
    ? (selectedAttendantData?.data || null)
    : null;
  const editingDepot = editingDepotId ? (selectedDepotData?.data || null) : null;
  const viewingBus =
    viewTarget?.type === 'bus' ? (selectedBusData?.data || null) : null;
  const viewingDriver =
    viewTarget?.type === 'driver' ? (selectedDriverData?.data || null) : null;
  const viewingAttendant =
    viewTarget?.type === 'attendant'
      ? (selectedAttendantData?.data || null)
      : null;
  const viewingDepot =
    viewTarget?.type === 'depot' ? (selectedDepotData?.data || null) : null;
  const fleetViewTitle = getFleetViewTitle(viewTarget);
  const fleetViewLoading =
    viewTarget?.type === 'bus'
      ? loadingSelectedBus
      : viewTarget?.type === 'driver'
        ? loadingSelectedDriver
        : viewTarget?.type === 'attendant'
          ? loadingSelectedAttendant
          : viewTarget?.type === 'depot'
            ? loadingSelectedDepot
            : false;
  const fleetViewError =
    viewTarget?.type === 'bus'
      ? selectedBusError
      : viewTarget?.type === 'driver'
        ? selectedDriverError
        : viewTarget?.type === 'attendant'
          ? selectedAttendantError
          : viewTarget?.type === 'depot'
            ? selectedDepotError
            : false;
  const fleetViewSections = getFleetViewSections({
    bus: viewingBus,
    driver: viewingDriver,
    attendant: viewingAttendant,
    depot: viewingDepot,
    type: viewTarget?.type || null,
  });

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

  // --- Handlers ---------------------------------------------------
  const handleSaveBus = async (values: SchoolBusBusUpsertRequest) => {
    try {
      const response = editingBusId
        ? await updateBus({ id: editingBusId, body: values }).unwrap()
        : await createBus(values).unwrap();
      toast.success(response.message || 'Bus saved');
      setBusDialogOpen(false);
      setEditingBusId(null);
    } catch {
      toast.error('Không thể lưu xe');
    }
  };

  const handleSaveDriver = async (values: SchoolBusDriverUpsertRequest) => {
    try {
      const response = editingDriverId
        ? await updateDriver({ id: editingDriverId, body: values }).unwrap()
        : await createDriver(values).unwrap();
      toast.success(response.message || 'Driver saved');
      setDriverDialogOpen(false);
      setEditingDriverId(null);
    } catch {
      toast.error('Không thể lưu tài xế');
    }
  };

  const handleSaveAttendant = async (
    values: SchoolBusAttendantUpsertRequest
  ) => {
    try {
      const response = editingAttendantId
        ? await updateAttendant({
            id: editingAttendantId,
            body: values,
          }).unwrap()
        : await createAttendant(values).unwrap();
      toast.success(response.message || 'Attendant saved');
      setAttendantDialogOpen(false);
      setEditingAttendantId(null);
    } catch {
      toast.error('Không thể lưu phụ xe');
    }
  };

  const handleSaveDepot = async (values: SchoolBusDepotUpsertRequest) => {
    try {
      const response = editingDepotId
        ? await updateDepot({ id: editingDepotId, body: values }).unwrap()
        : await createDepot(values).unwrap();
      toast.success(response.message || 'Depot saved');
      setDepotDialogOpen(false);
      setEditingDepotId(null);
    } catch {
      toast.error('Không thể lưu bãi xe');
    }
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
    } catch {
      toast.error('Không thể xóa');
    }
  };

  // --- Table definitions ------------------------------------------

  // Helper to format friendly bus type
  const formatBusType = (type?: string | null) => {
    if (!type) return 'Standard';
    const friendlyMap: Record<string, string> = {
      BUS_16_SEATS: '16-seat bus',
      BUS_29_SEATS: '29-seat bus',
      BUS_35_SEATS: '35-seat bus',
      BUS_45_SEATS: '45-seat bus',
    };
    return friendlyMap[type] || type.replace(/_/g, ' ').toLowerCase();
  };

  // --- Table definitions ------------------------------------------

  const busColumns: SchoolBusTableColumn<SchoolBusBus>[] = [
    {
      key: 'bus',
      header: 'Xe',
      className: 'pl-6',
      headerClassName: 'pl-6',
      render: (bus) => (
        <div className='flex items-center gap-3'>
          <div className='flex h-8 w-8 shrink-0 items-center justify-center rounded-xl bg-indigo-50 text-[#4338CA] border border-indigo-100/50 shadow-sm'>
            <Bus className='h-4.5 w-4.5' />
          </div>
          <span className='font-bold text-slate-900'>{bus.plateNumber}</span>
        </div>
      ),
    },
    {
      key: 'type',
      header: 'Type',
      render: (bus) => (
        <span className='text-xs font-semibold text-slate-600 bg-slate-50 px-2 py-1 rounded-md border border-slate-100'>
          {formatBusType(bus.busType)}
        </span>
      ),
    },
    {
      key: 'capacity',
      header: 'Capacity',
      render: (bus) => (
        <span className='text-xs text-slate-700 font-medium'>
          {bus.capacity} seats
        </span>
      ),
    },
    {
      key: 'depot',
      header: 'Home depot',
      render: (bus) =>
        bus.homeDepotName ? (
          <span className='text-sm text-slate-700 font-semibold'>
            {bus.homeDepotName}
          </span>
        ) : (
          <UnassignedBadge label='Chưa có bãi xe' />
        ),
    },
    {
      key: 'assignment',
      header: 'Assignment',
      render: () => <UnassignedBadge />,
    },
    {
      key: 'status',
      header: 'Trạng thái',
      render: (bus) => (
        <SchoolBusStatusBadge status={bus.status} labelMap={busStatusLabel} />
      ),
    },
    {
      key: 'actions',
      header: 'Thao tác',
      className: 'pr-6 text-right',
      headerClassName: 'pr-6 text-right',
      render: (bus) => (
        <div className='flex justify-end gap-2'>
          <Button
            size='icon'
            variant='outline'
            className='h-8 w-8 rounded-lg border-slate-200 text-slate-500 hover:bg-slate-50 hover:text-slate-700'
            onClick={() => {
              setViewTarget({ type: 'bus', id: bus.id });
            }}
          >
            <Eye className='h-3.5 w-3.5' />
          </Button>
          <Button
            size='icon'
            variant='outline'
            className='h-8 w-8 text-slate-500 hover:text-slate-900 border-slate-200'
            onClick={() => {
              setEditingBusId(bus.id);
              setBusDialogOpen(true);
            }}
          >
            <Pencil className='h-4 w-4' />
          </Button>
          <Button
            size='icon'
            variant='outline'
            className='h-8 w-8 text-slate-500 hover:text-red-600 hover:bg-red-50 hover:border-red-100 border-slate-200'
            onClick={() => setDeleteTarget({ type: 'bus', entity: bus })}
          >
            <Trash2 className='h-4 w-4' />
          </Button>
        </div>
      ),
    },
  ];

  const busToolbar = (
    <div className='flex flex-wrap items-center gap-3 flex-1 min-w-0'>
      <div className='relative flex-1 min-w-[180px] max-w-xs'>
        <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400' />
        <input
          type='text'
          placeholder='Search plate or depot...'
          value={busSearch}
          onChange={(e) => setBusSearch(e.target.value)}
          className='w-full h-9 rounded-lg border border-slate-200 bg-white py-1.5 pl-9 pr-3 text-xs outline-none focus:border-slate-300 focus:ring-1 focus:ring-slate-200'
        />
      </div>
      <SchoolBusSelect
        value={busStatusFilter}
        onChange={setBusStatusFilter}
        placeholder='All statuses'
        options={[
          { label: 'Available', value: 'AVAILABLE' },
          { label: 'In use', value: 'IN_USE' },
          { label: 'Maintenance', value: 'MAINTENANCE' },
          { label: 'Retired', value: 'RETIRED' },
        ]}
        clearable
      />
      <SchoolBusSelect
        value={busDepotFilter}
        onChange={setBusDepotFilter}
        placeholder='All depots'
        icon={Warehouse}
        options={depots.map((d) => ({ label: d.name, value: String(d.id) }))}
        clearable
        searchable
      />
    </div>
  );

  const driverColumns: SchoolBusTableColumn<SchoolBusDriver>[] = [
    {
      key: 'driver',
      header: 'Tài xế',
      className: 'pl-6',
      headerClassName: 'pl-6',
      render: (driver) => (
        <div className='flex items-center gap-3'>
          <div className='flex h-8 w-8 shrink-0 items-center justify-center rounded-xl bg-indigo-50 text-indigo-700 border border-indigo-100/50 shadow-sm'>
            <UserCog className='h-4.5 w-4.5' />
          </div>
          <span className='font-bold text-slate-900'>{driver.fullName}</span>
        </div>
      ),
    },
    {
      key: 'phone',
      header: 'Phone',
      render: (driver) =>
        driver.phone ? (
          <span className='text-sm text-slate-700 font-medium'>
            {driver.phone}
          </span>
        ) : (
          <span className='text-slate-400 italic text-xs'>Chưa có số điện thoại</span>
        ),
    },
    {
      key: 'linkedUser',
      header: 'Linked account',
      render: (driver) => (
        <span className='text-xs font-semibold text-slate-500 bg-slate-50 px-2.5 py-1 rounded-md border border-slate-100'>
          {driver.user?.email || 'Local profile'}
        </span>
      ),
    },
    {
      key: 'status',
      header: 'Trạng thái',
      render: (driver) => (
        <SchoolBusStatusBadge
          status={driver.isActive === false ? 'INACTIVE' : driver.status}
          labelMap={staffStatusLabel}
        />
      ),
    },
    {
      key: 'actions',
      header: 'Thao tác',
      className: 'pr-6 text-right',
      headerClassName: 'pr-6 text-right',
      render: (driver) => (
        <div className='flex justify-end gap-2'>
          <Button
            size='icon'
            variant='outline'
            className='h-8 w-8 rounded-lg border-slate-200 text-slate-500 hover:bg-slate-50 hover:text-slate-700'
            onClick={() => {
              setViewTarget({ type: 'driver', id: driver.id });
            }}
          >
            <Eye className='h-3.5 w-3.5' />
          </Button>
          <Button
            size='icon'
            variant='outline'
            className='h-8 w-8 text-slate-500 hover:text-slate-900 border-slate-200'
            onClick={() => {
              setEditingDriverId(driver.id);
              setDriverDialogOpen(true);
            }}
          >
            <Pencil className='h-4 w-4' />
          </Button>
          <Button
            size='icon'
            variant='outline'
            className='h-8 w-8 text-slate-500 hover:text-red-600 hover:bg-red-50 hover:border-red-100 border-slate-200'
            onClick={() => setDeleteTarget({ type: 'driver', entity: driver })}
          >
            <Trash2 className='h-4 w-4' />
          </Button>
        </div>
      ),
    },
  ];

  const driverToolbar = (
    <div className='flex flex-wrap items-center gap-3 flex-1 min-w-0'>
      <div className='relative flex-1 min-w-[180px] max-w-xs'>
        <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400' />
        <input
          type='text'
          placeholder='Search driver...'
          value={driverSearch}
          onChange={(e) => setDriverSearch(e.target.value)}
          className='w-full h-9 rounded-lg border border-slate-200 bg-white py-1.5 pl-9 pr-3 text-xs outline-none focus:border-slate-300 focus:ring-1 focus:ring-slate-200'
        />
      </div>
      <SchoolBusSelect
        value={driverStatusFilter}
        onChange={setDriverStatusFilter}
        placeholder='All statuses'
        options={[
          { label: 'Đang hoạt động', value: 'active' },
          { label: 'Ngừng hoạt động', value: 'inactive' },
        ]}
        clearable
      />
    </div>
  );

  const attendantColumns: SchoolBusTableColumn<SchoolBusAttendant>[] = [
    {
      key: 'attendant',
      header: 'Phụ xe',
      className: 'pl-6',
      headerClassName: 'pl-6',
      render: (attendant) => (
        <div className='flex items-center gap-3'>
          <div className='flex h-8 w-8 shrink-0 items-center justify-center rounded-xl bg-emerald-50 text-emerald-700 border border-emerald-100/50 shadow-sm'>
            <ShieldCheck className='h-4.5 w-4.5' />
          </div>
          <span className='font-bold text-slate-900'>{attendant.fullName}</span>
        </div>
      ),
    },
    {
      key: 'phone',
      header: 'Phone',
      render: (attendant) =>
        attendant.phone ? (
          <span className='text-sm text-slate-700 font-medium'>
            {attendant.phone}
          </span>
        ) : (
          <span className='text-slate-400 italic text-xs'>Chưa có số điện thoại</span>
        ),
    },
    {
      key: 'assignment',
      header: 'Assignment',
      render: () => <UnassignedBadge />,
    },
    {
      key: 'status',
      header: 'Trạng thái',
      render: (attendant) => (
        <SchoolBusStatusBadge
          status={attendant.isActive === false ? 'INACTIVE' : attendant.status}
          labelMap={staffStatusLabel}
        />
      ),
    },
    {
      key: 'actions',
      header: 'Thao tác',
      className: 'pr-6 text-right',
      headerClassName: 'pr-6 text-right',
      render: (attendant) => (
        <div className='flex justify-end gap-2'>
          <Button
            size='icon'
            variant='outline'
            className='h-8 w-8 rounded-lg border-slate-200 text-slate-500 hover:bg-slate-50 hover:text-slate-700'
            onClick={() => {
              setViewTarget({ type: 'attendant', id: attendant.id });
            }}
          >
            <Eye className='h-3.5 w-3.5' />
          </Button>
          <Button
            size='icon'
            variant='outline'
            className='h-8 w-8 text-slate-500 hover:text-slate-900 border-slate-200'
            onClick={() => {
              setEditingAttendantId(attendant.id);
              setAttendantDialogOpen(true);
            }}
          >
            <Pencil className='h-4 w-4' />
          </Button>
          <Button
            size='icon'
            variant='outline'
            className='h-8 w-8 text-slate-500 hover:text-red-600 hover:bg-red-50 hover:border-red-100 border-slate-200'
            onClick={() =>
              setDeleteTarget({ type: 'attendant', entity: attendant })
            }
          >
            <Trash2 className='h-4 w-4' />
          </Button>
        </div>
      ),
    },
  ];

  const attendantToolbar = (
    <div className='flex flex-wrap items-center gap-3 flex-1 min-w-0'>
      <div className='relative flex-1 min-w-[180px] max-w-xs'>
        <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400' />
        <input
          type='text'
          placeholder='Search attendant...'
          value={attendantSearch}
          onChange={(e) => setAttendantSearch(e.target.value)}
          className='w-full h-9 rounded-lg border border-slate-200 bg-white py-1.5 pl-9 pr-3 text-xs outline-none focus:border-slate-300 focus:ring-1 focus:ring-slate-200'
        />
      </div>
      <SchoolBusSelect
        value={attendantStatusFilter}
        onChange={setAttendantStatusFilter}
        placeholder='All statuses'
        options={[
          { label: 'Đang hoạt động', value: 'active' },
          { label: 'Ngừng hoạt động', value: 'inactive' },
        ]}
        clearable
      />
    </div>
  );

  const depotColumns: SchoolBusTableColumn<SchoolBusDepot>[] = [
    {
      key: 'depot',
      header: 'Bãi xe',
      className: 'pl-6',
      headerClassName: 'pl-6',
      render: (depot) => (
        <div className='flex items-center gap-3'>
          <div className='flex h-8 w-8 shrink-0 items-center justify-center rounded-xl bg-amber-50 text-amber-700 border border-amber-100/50 shadow-sm'>
            <Warehouse className='h-4.5 w-4.5' />
          </div>
          <div>
            <p className='font-bold text-slate-900'>{depot.name}</p>
            {depot.code && (
              <p className='text-[10px] font-mono font-bold text-slate-500 mt-0.5 bg-slate-100 px-1.5 py-0.2 rounded border border-slate-200 w-max'>
                {depot.code}
              </p>
            )}
          </div>
        </div>
      ),
    },
    {
      key: 'address',
      header: 'Address',
      render: (depot) =>
        depot.address ? (
          <span className='text-sm text-slate-600 font-semibold'>
            {depot.address}
          </span>
        ) : (
          <span className='text-slate-400 italic text-xs'>Chưa có địa chỉ</span>
        ),
    },
    {
      key: 'phone',
      header: 'Contact phone',
      render: (depot) =>
        depot.contactPhone ? (
          <span className='text-sm text-slate-700 font-medium'>
            {depot.contactPhone}
          </span>
        ) : (
          <span className='text-slate-400 italic text-xs'>Chưa có số điện thoại</span>
        ),
    },
    {
      key: 'coordinates',
      header: 'Coordinates',
      render: (depot) =>
        depot.latitude && depot.longitude ? (
          <span className='inline-flex items-center gap-1 rounded-md bg-emerald-50 px-2 py-0.5 text-xs font-semibold text-emerald-800 ring-1 ring-inset ring-emerald-600/15'>
            <MapPin className='h-3.5 w-3.5' /> Set
          </span>
        ) : (
          <WarningBadge label='Missing coordinates' />
        ),
    },
    {
      key: 'status',
      header: 'Trạng thái',
      render: (depot) => (
        <SchoolBusStatusBadge
          status={depot.isActive ? 'ACTIVE' : 'INACTIVE'}
          labelMap={profileStatusLabel}
        />
      ),
    },
    {
      key: 'actions',
      header: 'Thao tác',
      className: 'pr-6 text-right',
      headerClassName: 'pr-6 text-right',
      render: (depot) => (
        <div className='flex justify-end gap-2'>
          <Button
            size='icon'
            variant='outline'
            className='h-8 w-8 rounded-lg border-slate-200 text-slate-500 hover:bg-slate-50 hover:text-slate-700'
            onClick={() => {
              setViewTarget({ type: 'depot', id: depot.id });
            }}
          >
            <Eye className='h-3.5 w-3.5' />
          </Button>
          <Button
            size='icon'
            variant='outline'
            className='h-8 w-8 text-slate-500 hover:text-slate-900 border-slate-200'
            onClick={() => {
              setEditingDepotId(depot.id);
              setDepotDialogOpen(true);
            }}
          >
            <Pencil className='h-4 w-4' />
          </Button>
          <Button
            size='icon'
            variant='outline'
            className='h-8 w-8 text-slate-500 hover:text-red-600 hover:bg-red-50 hover:border-red-100 border-slate-200'
            onClick={() => setDeleteTarget({ type: 'depot', entity: depot })}
          >
            <Trash2 className='h-4 w-4' />
          </Button>
        </div>
      ),
    },
  ];

  const depotToolbar = (
    <div className='flex flex-wrap items-center gap-3 flex-1 min-w-0'>
      <div className='relative flex-1 min-w-[180px] max-w-xs'>
        <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400' />
        <input
          type='text'
          placeholder='Search depot...'
          value={depotSearch}
          onChange={(e) => setDepotSearch(e.target.value)}
          className='w-full h-9 rounded-lg border border-slate-200 bg-white py-1.5 pl-9 pr-3 text-xs outline-none focus:border-slate-300 focus:ring-1 focus:ring-slate-200'
        />
      </div>
      <SchoolBusSelect
        value={depotCoordsFilter}
        onChange={setDepotCoordsFilter}
        placeholder='All coordinates'
        options={[
          { label: 'Has coordinates', value: 'set' },
          { label: 'Missing coordinates', value: 'missing' },
        ]}
        clearable
      />
    </div>
  );

  // --- Render -----------------------------------------------------

  return (
    <>
      <SchoolBusPageShell
        title='Fleet & crew status'
        description='Operational fleet overview - vehicle availability, crew licensing, and depot configuration.'
        breadcrumb={
          <SchoolBusBreadcrumb
            items={[
              { label: 'School Bus Ops', href: '/school-bus/dispatch' },
              { label: 'Fleet & Crew', current: true },
            ]}
          />
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
                <DropdownMenuItem
                  onClick={() => {
                    setEditingDriverId(null);
                    setDriverDialogOpen(true);
                  }}
                >
                  <UserCog className='h-4 w-4' /> Add driver
                </DropdownMenuItem>
                <DropdownMenuItem
                  onClick={() => {
                    setEditingAttendantId(null);
                    setAttendantDialogOpen(true);
                  }}
                >
                  <ShieldCheck className='h-4 w-4' /> Add attendant
                </DropdownMenuItem>
                <DropdownMenuItem
                  onClick={() => {
                    setEditingDepotId(null);
                    setDepotDialogOpen(true);
                  }}
                >
                  <Warehouse className='h-4 w-4' /> Add depot
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
            <Button
              className='rounded-full'
              onClick={() => {
                setEditingBusId(null);
                setBusDialogOpen(true);
              }}
            >
              <Plus className='h-4 w-4' /> Add bus
            </Button>
          </div>
        }
      >
        <div className='flex flex-col gap-6'>
          {/* Fleet status stats */}
          <div className='grid gap-3 grid-cols-2 lg:grid-cols-4'>
            <SchoolBusMetricCard
              label='Available buses'
              value={availableBuses}
              hint={`${buses.length} total registered`}
              icon={Bus}
              tone='info'
            />
            <SchoolBusMetricCard
              label='Available drivers'
              value={availableDrivers}
              hint={`${drivers.length} total - ${drivers.length - availableDrivers} unavailable`}
              icon={UserCog}
              tone='linked'
            />
            <SchoolBusMetricCard
              label='Available attendants'
              value={availableAttendants}
              hint={`${attendants.length} total - ${attendants.length - availableAttendants} unavailable`}
              icon={ShieldCheck}
              tone='success'
            />
            <SchoolBusMetricCard
              label='Depots'
              value={depotsPage.length}
              hint={`${depotsPage.filter((d) => d.latitude && d.longitude).length} with coordinates`}
              icon={Warehouse}
              tone='warning'
            />
          </div>

          {/* Unified Table view */}
          <SchoolBusDataTable<any>
            tabs={[
              { key: 'buses', label: 'Buses', count: buses.length },
              { key: 'drivers', label: 'Drivers', count: drivers.length },
              {
                key: 'attendants',
                label: 'Attendants',
                count: attendants.length,
              },
              { key: 'depots', label: 'Depots', count: depotsPage.length },
            ]}
            activeTab={activeTab}
            onTabChange={setActiveTab}
            toolbar={
              activeTab === 'buses'
                ? busToolbar
                : activeTab === 'drivers'
                  ? driverToolbar
                  : activeTab === 'attendants'
                    ? attendantToolbar
                    : depotToolbar
            }
            data={
              activeTab === 'buses'
                ? filteredBuses
                : activeTab === 'drivers'
                  ? filteredDrivers
                  : activeTab === 'attendants'
                    ? filteredAttendants
                    : filteredDepots
            }
            columns={
              activeTab === 'buses'
                ? busColumns
                : activeTab === 'drivers'
                  ? driverColumns
                  : activeTab === 'attendants'
                    ? attendantColumns
                    : depotColumns
            }
            isLoading={
              activeTab === 'buses'
                ? loadingBuses
                : activeTab === 'drivers'
                  ? loadingDrivers
                  : activeTab === 'attendants'
                    ? loadingAttendants
                    : loadingDepots
            }
            pagination={
              activeTab === 'buses' && busesData
                ? { page: busesData?.data, onPageChange: busPagination.setPage }
                : activeTab === 'drivers' && driversData
                  ? {
                      page: driversData?.data,
                      onPageChange: driverPagination.setPage,
                    }
                  : activeTab === 'attendants' && attendantsData
                    ? {
                        page: attendantsData?.data,
                        onPageChange: attendantPagination.setPage,
                      }
                    : activeTab === 'depots' && depotsPageData
                      ? {
                          page: depotsPageData?.data,
                          onPageChange: depotPagination.setPage,
                        }
                      : undefined
            }
            stickyFirstColumn
            stickyActionColumn
            emptyIcon={
              activeTab === 'buses'
                ? Bus
                : activeTab === 'drivers'
                  ? UserCog
                  : activeTab === 'attendants'
                    ? ShieldCheck
                    : Warehouse
            }
            emptyTitle={
              activeTab === 'buses'
                ? buses.length === 0
                  ? 'Không tìm thấy xe'
                  : 'Không có xe phù hợp với bộ lọc'
                : activeTab === 'drivers'
                  ? drivers.length === 0
                    ? 'Không tìm thấy tài xế'
                    : 'Không có tài xế phù hợp với bộ lọc'
                  : activeTab === 'attendants'
                    ? attendants.length === 0
                      ? 'Không tìm thấy phụ xe'
                      : 'Không có phụ xe phù hợp với bộ lọc'
                    : depotsPage.length === 0
                      ? 'Chưa có bãi xes found'
                      : 'Chưa có bãi xes match current search'
            }
            emptyDescription={
              activeTab === 'buses'
                ? buses.length === 0
                  ? 'Add the first bus to make assignment possible.'
                  : 'Hãy thử điều chỉnh từ khóa tìm kiếm hoặc xóa bộ lọc.'
                : activeTab === 'drivers'
                  ? drivers.length === 0
                    ? 'Hãy tạo hồ sơ tài xế để hỗ trợ phân công tuyến.'
                    : 'Hãy thử điều chỉnh từ khóa tìm kiếm hoặc xóa bộ lọc.'
                  : activeTab === 'attendants'
                    ? attendants.length === 0
                      ? 'Hãy tạo hồ sơ phụ xe để hỗ trợ điểm danh.'
                      : 'Hãy thử điều chỉnh từ khóa tìm kiếm hoặc xóa bộ lọc.'
                    : depotsPage.length === 0
                      ? 'Hãy tạo bãi xe để mô hình hóa điểm đầu và điểm cuối tuyến.'
                      : 'Hãy thử điều chỉnh từ khóa tìm kiếm hoặc xóa bộ lọc.'
            }
          />
        </div>
      </SchoolBusPageShell>

      {/* --- Dialogs ----------------------------------------------- */}

      <BusFormDialog
        open={busDialogOpen && (!editingBusId || Boolean(editingBus))}
        onOpenChange={(open) => {
          setBusDialogOpen(open);
          if (!open) setEditingBusId(null);
        }}
        initialData={editingBus}
        busTypes={busTypes}
        isLoadingBusTypes={loadingBusTypes}
        depots={depots}
        isLoading={creatingBus || updatingBus}
        onSubmit={handleSaveBus}
      />

      <DriverFormDialog
        open={driverDialogOpen && (!editingDriverId || Boolean(editingDriver))}
        onOpenChange={(open) => {
          setDriverDialogOpen(open);
          if (!open) setEditingDriverId(null);
        }}
        initialData={editingDriver}
        accountUsers={driverDialogUsers}
        isLoadingAccountUsers={loadingAccountUsers}
        isLoading={creatingDriver || updatingDriver}
        onSubmit={handleSaveDriver}
      />

      <AttendantFormDialog
        open={
          attendantDialogOpen &&
          (!editingAttendantId || Boolean(editingAttendant))
        }
        onOpenChange={(open) => {
          setAttendantDialogOpen(open);
          if (!open) setEditingAttendantId(null);
        }}
        initialData={editingAttendant}
        accountUsers={attendantDialogUsers}
        isLoadingAccountUsers={loadingAccountUsers}
        isLoading={creatingAttendant || updatingAttendant}
        onSubmit={handleSaveAttendant}
      />

      <DepotFormDialog
        open={depotDialogOpen && (!editingDepotId || Boolean(editingDepot))}
        onOpenChange={(open) => {
          setDepotDialogOpen(open);
          if (!open) setEditingDepotId(null);
        }}
        initialData={editingDepot}
        isLoading={creatingDepot || updatingDepot}
        onSubmit={handleSaveDepot}
      />

      <SchoolBusReadOnlyDetailDialog
        open={Boolean(viewTarget)}
        onOpenChange={(open) => {
          if (!open) setViewTarget(null);
        }}
        title={fleetViewTitle}
        description='Read-only fleet and crew information loaded from the detail API.'
        isLoading={fleetViewLoading}
        isError={fleetViewError}
        sections={fleetViewSections}
      />

      <SchoolBusDeleteDialog
        open={Boolean(deleteTarget)}
        onOpenChange={(open) => {
          if (!open) setDeleteTarget(null);
        }}
        title={
          deleteTarget?.type === 'bus'
            ? 'Xóa xe'
            : deleteTarget?.type === 'driver'
              ? 'Xóa tài xế'
              : deleteTarget?.type === 'depot'
                ? 'Xóa bãi xe'
                : 'Xóa phụ xe'
        }
        description='This will soft-delete the selected record from active fleet operations.'
        isLoading={
          deletingBus || deletingDriver || deletingAttendant || deletingDepot
        }
        onConfirm={handleDelete}
      />
    </>
  );
}

function getFleetViewTitle(viewTarget: FleetViewTarget) {
  if (viewTarget?.type === 'bus') return 'Bus detail';
  if (viewTarget?.type === 'driver') return 'Driver detail';
  if (viewTarget?.type === 'attendant') return 'Attendant detail';
  if (viewTarget?.type === 'depot') return 'Depot detail';
  return 'Fleet detail';
}

function getFleetViewSections({
  type,
  bus,
  driver,
  attendant,
  depot,
}: {
  type: Exclude<FleetViewTarget, null>['type'] | null;
  bus: SchoolBusBus | null;
  driver: SchoolBusDriver | null;
  attendant: SchoolBusAttendant | null;
  depot: SchoolBusDepot | null;
}): SchoolBusDetailSection[] {
  if (type === 'bus') {
    return [
      {
        title: 'Bus information',
        fields: [
          { label: 'Plate number', value: bus?.plateNumber },
          { label: 'Bus type', value: bus?.busType },
          { label: 'Capacity', value: bus?.capacity },
          { label: 'Trạng thái', value: bus?.status },
          { label: 'Đang hoạt động', value: bus?.isActive === false ? 'No' : 'Yes' },
        ],
      },
      {
        title: 'Assignment defaults',
        fields: [
          { label: 'Home depot', value: bus?.homeDepotName },
          { label: 'Home depot ID', value: bus?.homeDepotId },
        ],
      },
    ];
  }

  if (type === 'driver') {
    return [
      {
        title: 'Account link',
        fields: [
          { label: 'Account user ID', value: driver?.accountUserId || driver?.userId },
          { label: 'Account email', value: driver?.user?.email },
        ],
      },
      {
        title: 'Driver information',
        fields: [
          { label: 'Full name', value: driver?.fullName },
          { label: 'Phone', value: driver?.phone },
          { label: 'Trạng thái', value: driver?.status },
          { label: 'Đang hoạt động', value: driver?.isActive === false ? 'No' : 'Yes' },
        ],
      },
    ];
  }

  if (type === 'attendant') {
    return [
      {
        title: 'Account link',
        fields: [
          {
            label: 'Account user ID',
            value: attendant?.accountUserId || attendant?.userId,
          },
          { label: 'Account email', value: attendant?.user?.email },
        ],
      },
      {
        title: 'Attendant information',
        fields: [
          { label: 'Full name', value: attendant?.fullName },
          { label: 'Phone', value: attendant?.phone },
          { label: 'Trạng thái', value: attendant?.status },
          {
            label: 'Đang hoạt động',
            value: attendant?.isActive === false ? 'No' : 'Yes',
          },
        ],
      },
    ];
  }

  if (type === 'depot') {
    return [
      {
        title: 'Depot information',
        fields: [
          { label: 'Depot code', value: depot?.code },
          { label: 'Depot name', value: depot?.name },
          { label: 'Contact phone', value: depot?.contactPhone },
          { label: 'Đang hoạt động', value: depot?.isActive === false ? 'No' : 'Yes' },
          { label: 'Address', value: depot?.address, fullWidth: true },
          { label: 'Description', value: depot?.description, fullWidth: true },
        ],
      },
      {
        title: 'Coordinates',
        fields: [
          { label: 'Latitude', value: depot?.latitude },
          { label: 'Longitude', value: depot?.longitude },
        ],
      },
    ];
  }

  return [];
}

// -- Helper: filter account users for crew assignment --------------------------

function buildAvailableCrewAccountUsers<
  TProfile extends { id: number; userId: number; isActive?: boolean | null },
>({
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
