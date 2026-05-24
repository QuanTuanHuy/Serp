import { useMemo, useState } from 'react';
import {
  ArrowLeft,
  CheckSquare,
  Container,
  Loader2,
  Package,
  Truck,
} from 'lucide-react';
import { toast } from 'sonner';
import { Button, TableCell } from '@/shared/components/ui';
import {
  useCreateDispatcherTransportPlanMutation,
  useGetDispatcherContainersQuery,
  useGetDispatcherLocationsQuery,
  useGetDispatcherTrailersQuery,
  useGetDispatcherTrucksQuery,
} from '../../../api/ttcrsApi';
import type {
  ContainerItem,
  LocationItem,
  TrailerItem,
  TransportPlanResult,
  TruckItem,
} from '../../../types';
import { VehicleStatusBadge } from './common';
import { sortByStatus } from './helpers';
import { ReturnDepotCell } from './ReturnDepotCell';
import { DepotColumnConfig, ResourceTable } from './ResourceTable';

interface Step2Props {
  selectedRequestIds: Set<number>;
  onBack: () => void;
  isBackLoading: boolean;
  onPlanCreated: (result: TransportPlanResult) => void;
}

export function Step2PlanSetup({
  selectedRequestIds,
  onBack,
  isBackLoading,
  onPlanCreated,
}: Step2Props) {
  const [truckReturnDepots, setTruckReturnDepots] = useState<
    Record<number, string[]>
  >({});
  const [trailerReturnDepots, setTrailerReturnDepots] = useState<
    Record<number, string[]>
  >({});
  const [containerReturnDepots, setContainerReturnDepots] = useState<
    Record<number, string[]>
  >({});

  const [selectedTruckIds, setSelectedTruckIds] = useState<Set<number>>(
    new Set()
  );
  const [selectedTrailerIds, setSelectedTrailerIds] = useState<Set<number>>(
    new Set()
  );
  const [selectedContainerIds, setSelectedContainerIds] = useState<Set<number>>(
    new Set()
  );

  const { data: locationsData, isLoading: isLocationsLoading } =
    useGetDispatcherLocationsQuery();
  const { data: trucksData, isLoading: isTrucksLoading } =
    useGetDispatcherTrucksQuery();
  const { data: trailersData, isLoading: isTrailersLoading } =
    useGetDispatcherTrailersQuery();
  const { data: containersData, isLoading: isContainersLoading } =
    useGetDispatcherContainersQuery();

  const [createTransportPlan, { isLoading: isCreating }] =
    useCreateDispatcherTransportPlanMutation();

  const locations: LocationItem[] = locationsData?.data ?? [];

  const trucks = useMemo(
    () => sortByStatus(trucksData?.data ?? []),
    [trucksData]
  );
  const trailers = useMemo(
    () => sortByStatus(trailersData?.data ?? []),
    [trailersData]
  );
  const containers = useMemo(
    () => sortByStatus(containersData?.data ?? []),
    [containersData]
  );

  const truckDepotLocations = useMemo(
    () => locations.filter((location) => location.type === 'DEPOT_TRUCK'),
    [locations]
  );
  const trailerDepotLocations = useMemo(
    () => locations.filter((location) => location.type === 'DEPOT_TRAILER'),
    [locations]
  );
  const containerDepotLocations = useMemo(
    () => locations.filter((location) => location.type === 'DEPOT_CONTAINER'),
    [locations]
  );

  const handleTruckToggle = (truck: TruckItem) => {
    const isAdding = !selectedTruckIds.has(truck.id);

    setSelectedTruckIds((prev) => {
      const next = new Set(prev);
      if (next.has(truck.id)) {
        next.delete(truck.id);
      } else {
        next.add(truck.id);
      }
      return next;
    });

    if (isAdding) {
      setTruckReturnDepots((prev) => ({
        ...prev,
        [truck.id]:
          prev[truck.id] ??
          (truck.currentLocationCode ? [truck.currentLocationCode] : []),
      }));
    }
  };

  const handleTrailerToggle = (trailer: TrailerItem) => {
    const isAdding = !selectedTrailerIds.has(trailer.id);

    setSelectedTrailerIds((prev) => {
      const next = new Set(prev);
      if (next.has(trailer.id)) {
        next.delete(trailer.id);
      } else {
        next.add(trailer.id);
      }
      return next;
    });

    if (isAdding) {
      setTrailerReturnDepots((prev) => ({
        ...prev,
        [trailer.id]:
          prev[trailer.id] ??
          (trailer.currentLocationCode ? [trailer.currentLocationCode] : []),
      }));
    }
  };

  const handleContainerToggle = (container: ContainerItem) => {
    const isAdding = !selectedContainerIds.has(container.id);

    setSelectedContainerIds((prev) => {
      const next = new Set(prev);
      if (next.has(container.id)) {
        next.delete(container.id);
      } else {
        next.add(container.id);
      }
      return next;
    });

    if (isAdding) {
      setContainerReturnDepots((prev) => ({
        ...prev,
        [container.id]:
          prev[container.id] ??
          (container.currentLocationCode
            ? [container.currentLocationCode]
            : []),
      }));
    }
  };

  const addDepot = (
    setFn: React.Dispatch<React.SetStateAction<Record<number, string[]>>>,
    id: number,
    code: string
  ) => {
    setFn((prev) => ({ ...prev, [id]: [...(prev[id] ?? []), code] }));
  };

  const removeDepot = (
    setFn: React.Dispatch<React.SetStateAction<Record<number, string[]>>>,
    id: number,
    code: string
  ) => {
    setFn((prev) => ({
      ...prev,
      [id]: (prev[id] ?? []).filter((depotCode) => depotCode !== code),
    }));
  };

  const truckDepotColumn: DepotColumnConfig<TruckItem> = {
    header: 'Return Depots',
    renderCell: (truck, isSelected) => (
      <ReturnDepotCell
        resourceId={truck.id}
        isSelected={isSelected}
        depotCodes={truckReturnDepots[truck.id] ?? []}
        availableDepots={truckDepotLocations}
        isDepotsLoading={isLocationsLoading}
        onAdd={(id, code) => addDepot(setTruckReturnDepots, id, code)}
        onRemove={(id, code) => removeDepot(setTruckReturnDepots, id, code)}
      />
    ),
  };

  const trailerDepotColumn: DepotColumnConfig<TrailerItem> = {
    header: 'Return Depots',
    renderCell: (trailer, isSelected) => (
      <ReturnDepotCell
        resourceId={trailer.id}
        isSelected={isSelected}
        depotCodes={trailerReturnDepots[trailer.id] ?? []}
        availableDepots={trailerDepotLocations}
        isDepotsLoading={isLocationsLoading}
        onAdd={(id, code) => addDepot(setTrailerReturnDepots, id, code)}
        onRemove={(id, code) => removeDepot(setTrailerReturnDepots, id, code)}
      />
    ),
  };

  const containerDepotColumn: DepotColumnConfig<ContainerItem> = {
    header: 'Return Depots',
    renderCell: (container, isSelected) => (
      <ReturnDepotCell
        resourceId={container.id}
        isSelected={isSelected}
        depotCodes={containerReturnDepots[container.id] ?? []}
        availableDepots={containerDepotLocations}
        isDepotsLoading={isLocationsLoading}
        onAdd={(id, code) => addDepot(setContainerReturnDepots, id, code)}
        onRemove={(id, code) => removeDepot(setContainerReturnDepots, id, code)}
      />
    ),
  };

  const isFormValid = selectedTruckIds.size > 0;

  const handleCreatePlan = async () => {
    try {
      const response = await createTransportPlan({
        requestIds: Array.from(selectedRequestIds),
        truckReturnDepots: Array.from(selectedTruckIds).map((id) => ({
          resourceId: id,
          returnDepotCodes: truckReturnDepots[id] ?? [],
        })),
        trailerReturnDepots: Array.from(selectedTrailerIds).map((id) => ({
          resourceId: id,
          returnDepotCodes: trailerReturnDepots[id] ?? [],
        })),
        containerReturnDepots: Array.from(selectedContainerIds).map((id) => ({
          resourceId: id,
          returnDepotCodes: containerReturnDepots[id] ?? [],
        })),
        truckIds: Array.from(selectedTruckIds),
        trailerIds: Array.from(selectedTrailerIds),
        containerIds: Array.from(selectedContainerIds),
      }).unwrap();

      onPlanCreated(response.data);
      toast.success('Transport plan computed - review and adjust routes.');
    } catch {
      toast.error('Failed to create transport plan. Please try again.');
    }
  };

  return (
    <div className='flex flex-col gap-6'>
      <div className='flex items-center justify-between'>
        <p className='text-sm text-muted-foreground'>
          {selectedRequestIds.size} request
          {selectedRequestIds.size > 1 ? 's' : ''} selected - select resources
          and configure return depots per row.
        </p>
        <div className='flex items-center gap-2'>
          <Button
            variant='outline'
            size='sm'
            onClick={onBack}
            disabled={isBackLoading || isCreating}
          >
            {isBackLoading ? (
              <Loader2 className='h-4 w-4 animate-spin' />
            ) : (
              <>
                <ArrowLeft className='mr-1 h-4 w-4' />
                Back
              </>
            )}
          </Button>
          <Button
            size='sm'
            className='bg-orange-600 text-white hover:bg-orange-700'
            onClick={handleCreatePlan}
            disabled={!isFormValid || isCreating || isBackLoading}
          >
            {isCreating ? (
              <Loader2 className='mr-1 h-4 w-4 animate-spin' />
            ) : (
              <CheckSquare className='mr-1 h-4 w-4' />
            )}
            Create Plan
          </Button>
        </div>
      </div>

      <div className='flex flex-col gap-4'>
        <ResourceTable<TruckItem>
          title='Trucks'
          icon={<Truck className='h-4 w-4 text-orange-600' />}
          items={trucks}
          isLoading={isTrucksLoading}
          selectedIds={selectedTruckIds}
          onToggle={handleTruckToggle}
          headers={['Code', 'Status', 'Location']}
          depotColumn={truckDepotColumn}
          renderRow={(truck) => (
            <>
              <TableCell className='px-3 py-2 font-mono text-sm font-medium'>
                {truck.code}
              </TableCell>
              <TableCell className='px-3 py-2'>
                <VehicleStatusBadge status={truck.status} />
              </TableCell>
              <TableCell className='px-3 py-2 text-sm text-muted-foreground'>
                {truck.currentLocationCode ?? '-'}
              </TableCell>
            </>
          )}
        />

        <ResourceTable<TrailerItem>
          title='Trailers'
          icon={<Package className='h-4 w-4 text-orange-600' />}
          items={trailers}
          isLoading={isTrailersLoading}
          selectedIds={selectedTrailerIds}
          onToggle={handleTrailerToggle}
          headers={['Code', 'Status', 'Location']}
          depotColumn={trailerDepotColumn}
          renderRow={(trailer) => (
            <>
              <TableCell className='px-3 py-2 font-mono text-sm font-medium'>
                {trailer.code}
              </TableCell>
              <TableCell className='px-3 py-2'>
                <VehicleStatusBadge status={trailer.status} />
              </TableCell>
              <TableCell className='px-3 py-2 text-sm text-muted-foreground'>
                {trailer.currentLocationCode ?? '-'}
              </TableCell>
            </>
          )}
        />

        <ResourceTable<ContainerItem>
          title='Containers'
          icon={<Container className='h-4 w-4 text-orange-600' />}
          items={containers}
          isLoading={isContainersLoading}
          selectedIds={selectedContainerIds}
          onToggle={handleContainerToggle}
          headers={['Code', 'Size', 'Status', 'Location']}
          depotColumn={containerDepotColumn}
          renderRow={(container) => (
            <>
              <TableCell className='px-3 py-2 font-mono text-sm font-medium'>
                {container.code}
              </TableCell>
              <TableCell className='px-3 py-2 text-sm text-muted-foreground'>
                {container.size ?? '-'}
              </TableCell>
              <TableCell className='px-3 py-2'>
                <VehicleStatusBadge status={container.status} />
              </TableCell>
              <TableCell className='px-3 py-2 text-sm text-muted-foreground'>
                {container.currentLocationCode ?? '-'}
              </TableCell>
            </>
          )}
        />
      </div>
    </div>
  );
}
