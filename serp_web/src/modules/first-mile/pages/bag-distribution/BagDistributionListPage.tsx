/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Bag distribution workflow page
 */

'use client';

import * as React from 'react';
import {
  Boxes,
  ClipboardList,
  PackageCheck,
  RefreshCw,
  Truck,
} from 'lucide-react';

import { getErrorMessage, useAppSelector } from '@/lib/store';
import { Badge } from '@/shared/components/ui/badge';
import { Button } from '@/shared/components/ui/button';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui/tabs';
import { useNotification } from '@/shared/hooks';

import {
  useAutoPlanBagDistributionMutation,
  useCancelBagDistributionManifestMutation,
  useConfirmBagDistributionManifestInboundMutation,
  useConfirmBagDistributionManifestOutboundMutation,
  useCreateBagDistributionManifestMutation,
  useDriverCheckinBagDistributionEndMutation,
  useDriverCheckinBagDistributionStartMutation,
  useGetBagDistributionManifestByIdQuery,
  useGetBagDistributionManifestsQuery,
  useGetHubsQuery,
  useGetPostOfficesQuery,
  useGetSecondMileBagsQuery,
  useGetSecondMileRoutesQuery,
  useGetSecondMileVehiclesQuery,
} from '../../api/firstMileApi';
import type {
  AutoPlanBagDistributionRequest,
  BagDistributionManifest,
  BagDistributionPlan,
  CreateBagDistributionManifestRequest,
  SecondMileBag,
} from '../../types';
import {
  CheckinDialog,
  DriverTab,
  KpiCard,
  ManifestDetailDialog,
  ManifestTab,
  PlanningTab,
  ReadyBagsTab,
} from './components';
import {
  buildHubOptions,
  buildPostOfficeOptions,
  buildRouteOptions,
  buildVehicleOptions,
  canDriverCheckin,
  canManageDistribution,
  canOperateDistribution,
  formatNumber,
  makeDefaultPlanningState,
  parseNumber,
  toApiDateTime,
  type CheckinMode,
  type CheckinState,
  type DestinationFilter,
  type PlanningState,
  type SelectedBagSummary,
  type StatusFilter,
  type TabValue,
} from './bagDistributionModels';

export function BagDistributionListPage() {
  const roles = useAppSelector(
    (state) => state.account.user.profile?.roles ?? []
  );
  const notification = useNotification();
  const canManage = canManageDistribution(roles);
  const canOperate = canOperateDistribution(roles);
  const canCheckin = canDriverCheckin(roles);

  const [activeTab, setActiveTab] = React.useState<TabValue>('planning');
  const [planning, setPlanning] = React.useState<PlanningState>(
    makeDefaultPlanningState
  );
  const [selectedBagIds, setSelectedBagIds] = React.useState<number[]>([]);
  const [readySearch, setReadySearch] = React.useState('');
  const [destinationFilter, setDestinationFilter] =
    React.useState<DestinationFilter>('ALL');
  const [manifestStatus, setManifestStatus] =
    React.useState<StatusFilter>('ALL');
  const [selectedManifestId, setSelectedManifestId] = React.useState<
    number | null
  >(null);
  const [detailManifestId, setDetailManifestId] = React.useState<number | null>(
    null
  );
  const [planResult, setPlanResult] =
    React.useState<BagDistributionPlan | null>(null);
  const [checkinTarget, setCheckinTarget] = React.useState<{
    manifest: BagDistributionManifest;
    mode: CheckinMode;
  } | null>(null);
  const [checkinState, setCheckinState] = React.useState<CheckinState>({
    latitude: '',
    longitude: '',
    locationLabel: '',
  });

  const { data: hubsData, isFetching: isFetchingHubs } = useGetHubsQuery({
    page: 0,
    size: 200,
    status: 'ACTIVE',
  });
  const { data: postOfficesData, isFetching: isFetchingPostOffices } =
    useGetPostOfficesQuery({
      page: 0,
      size: 200,
      status: 'ACTIVE',
    });

  const routeFilters = React.useMemo(
    () => ({
      page: 0,
      size: 200,
      originType: 'HUB' as const,
      originHubId: planning.originHubId,
      destinationType: planning.destinationType,
      destinationHubId:
        planning.destinationType === 'HUB'
          ? planning.destinationHubId
          : undefined,
      destinationPostOfficeCode:
        planning.destinationType === 'POST_OFFICE'
          ? planning.destinationPostOfficeCode
          : undefined,
      status: 'ACTIVE' as const,
    }),
    [
      planning.destinationHubId,
      planning.destinationPostOfficeCode,
      planning.destinationType,
      planning.originHubId,
    ]
  );

  const { data: routesData, isFetching: isFetchingRoutes } =
    useGetSecondMileRoutesQuery(routeFilters, {
      skip:
        !planning.originHubId ||
        (planning.destinationType === 'HUB' && !planning.destinationHubId) ||
        (planning.destinationType === 'POST_OFFICE' &&
          !planning.destinationPostOfficeCode),
    });

  const { data: vehiclesData, isFetching: isFetchingVehicles } =
    useGetSecondMileVehiclesQuery({
      page: 0,
      size: 200,
      hubId: planning.originHubId,
      status: 'ACTIVE',
    });

  const readyBagFilters = React.useMemo(
    () => ({
      page: 0,
      size: 50,
      keyword: readySearch || undefined,
      originHubId: planning.originHubId,
      destinationType:
        destinationFilter === 'ALL' ? undefined : destinationFilter,
      destinationHubId:
        destinationFilter === 'HUB' ? planning.destinationHubId : undefined,
      destinationPostOfficeCode:
        destinationFilter === 'POST_OFFICE'
          ? planning.destinationPostOfficeCode
          : undefined,
      status: 'SEALED' as const,
    }),
    [
      destinationFilter,
      planning.destinationHubId,
      planning.destinationPostOfficeCode,
      planning.originHubId,
      readySearch,
    ]
  );

  const {
    data: readyBagsData,
    isFetching: isFetchingReadyBags,
    refetch: refetchReadyBags,
  } = useGetSecondMileBagsQuery(readyBagFilters);

  const {
    data: manifestsData,
    isFetching: isFetchingManifests,
    refetch: refetchManifests,
  } = useGetBagDistributionManifestsQuery({
    page: 0,
    size: 50,
    originHubId: planning.originHubId,
    destinationType:
      destinationFilter === 'ALL' ? undefined : destinationFilter,
    status: manifestStatus === 'ALL' ? undefined : manifestStatus,
  });

  const {
    data: driverManifestsData,
    isFetching: isFetchingDriverManifests,
    refetch: refetchDriverManifests,
  } = useGetBagDistributionManifestsQuery({
    page: 0,
    size: 50,
  });

  const { data: selectedManifestDetail, isFetching: isFetchingDetail } =
    useGetBagDistributionManifestByIdQuery(detailManifestId ?? 0, {
      skip: !detailManifestId,
    });

  const [autoPlanDistribution, { isLoading: isPlanning }] =
    useAutoPlanBagDistributionMutation();
  const [createManifest, { isLoading: isCreatingManifest }] =
    useCreateBagDistributionManifestMutation();
  const [confirmOutbound, { isLoading: isConfirmingOutbound }] =
    useConfirmBagDistributionManifestOutboundMutation();
  const [confirmInbound, { isLoading: isConfirmingInbound }] =
    useConfirmBagDistributionManifestInboundMutation();
  const [cancelManifest, { isLoading: isCancelling }] =
    useCancelBagDistributionManifestMutation();
  const [driverCheckinStart, { isLoading: isCheckingInStart }] =
    useDriverCheckinBagDistributionStartMutation();
  const [driverCheckinEnd, { isLoading: isCheckingInEnd }] =
    useDriverCheckinBagDistributionEndMutation();

  const hubs = React.useMemo(() => hubsData?.items ?? [], [hubsData?.items]);
  const postOffices = React.useMemo(
    () => postOfficesData?.items ?? [],
    [postOfficesData?.items]
  );
  const routes = React.useMemo(
    () => routesData?.items ?? [],
    [routesData?.items]
  );
  const vehicles = React.useMemo(
    () => vehiclesData?.items ?? [],
    [vehiclesData?.items]
  );
  const readyBags = React.useMemo(
    () => readyBagsData?.items ?? [],
    [readyBagsData?.items]
  );
  const manifests = React.useMemo(
    () => manifestsData?.items ?? [],
    [manifestsData?.items]
  );
  const driverManifests = (driverManifestsData?.items ?? []).filter(
    (manifest) =>
      manifest.status === 'CREATED' || manifest.status === 'OUTBOUND_CONFIRMED'
  );

  const selectedBags = readyBags.filter((bag) =>
    selectedBagIds.includes(bag.id)
  );

  const selectedDestinationSummary =
    React.useMemo<SelectedBagSummary | null>(() => {
      if (selectedBags.length === 0) return null;
      const first = selectedBags[0];
      const sameDestination = selectedBags.every(
        (bag) =>
          bag.originHubId === first.originHubId &&
          bag.destinationType === first.destinationType &&
          bag.destinationHubId === first.destinationHubId &&
          bag.destinationPostOfficeCode === first.destinationPostOfficeCode
      );

      return {
        sameDestination,
        originHubId: first.originHubId,
        destinationType: first.destinationType,
        destinationHubId: first.destinationHubId,
        destinationPostOfficeCode: first.destinationPostOfficeCode,
        totalWeight: selectedBags.reduce(
          (sum, bag) => sum + (bag.currentWeight ?? 0),
          0
        ),
        totalVolume: selectedBags.reduce(
          (sum, bag) => sum + (bag.currentVolume ?? 0),
          0
        ),
        totalOrders: selectedBags.reduce(
          (sum, bag) => sum + (bag.currentOrders ?? 0),
          0
        ),
      };
    }, [selectedBags]);

  const hubOptions = React.useMemo(() => buildHubOptions(hubs), [hubs]);
  const postOfficeOptions = React.useMemo(
    () => buildPostOfficeOptions(postOffices),
    [postOffices]
  );
  const routeOptions = React.useMemo(() => buildRouteOptions(routes), [routes]);
  const vehicleOptions = React.useMemo(
    () => buildVehicleOptions(vehicles),
    [vehicles]
  );

  React.useEffect(() => {
    const readyBagIds = new Set(readyBags.map((bag) => bag.id));

    setSelectedBagIds((current) => {
      const next = current.filter((bagId) => readyBagIds.has(bagId));
      return next.length === current.length ? current : next;
    });
  }, [readyBags]);

  const updatePlanning = <K extends keyof PlanningState>(
    key: K,
    value: PlanningState[K]
  ) => {
    setPlanning((current) => ({
      ...current,
      [key]: value,
      ...(key === 'originHubId'
        ? {
            routeId: undefined,
            vehicleId: undefined,
          }
        : {}),
      ...(key === 'destinationType'
        ? {
            destinationHubId: undefined,
            destinationPostOfficeCode: undefined,
            routeId: undefined,
          }
        : {}),
      ...(key === 'destinationHubId' || key === 'destinationPostOfficeCode'
        ? { routeId: undefined }
        : {}),
    }));
    setPlanResult(null);
  };

  const toggleBag = (bag: SecondMileBag, checked: boolean) => {
    setSelectedBagIds((current) =>
      checked
        ? Array.from(new Set([...current, bag.id]))
        : current.filter((id) => id !== bag.id)
    );
  };

  const handleUseBagDestination = () => {
    if (!selectedDestinationSummary) return;
    if (!selectedDestinationSummary.sameDestination) {
      notification.error('Selected bags must share the same destination.');
      return;
    }

    setPlanning((current) => ({
      ...current,
      originHubId: selectedDestinationSummary.originHubId,
      destinationType:
        selectedDestinationSummary.destinationType ?? current.destinationType,
      destinationHubId: selectedDestinationSummary.destinationHubId,
      destinationPostOfficeCode:
        selectedDestinationSummary.destinationPostOfficeCode,
      routeId: undefined,
      vehicleId: undefined,
    }));
    setDestinationFilter(selectedDestinationSummary.destinationType ?? 'ALL');
    setActiveTab('planning');
  };

  const validatePlan = (): string | null => {
    if (!planning.originHubId) return 'Select an origin hub.';
    if (planning.destinationType === 'HUB' && !planning.destinationHubId) {
      return 'Select a destination hub.';
    }
    if (
      planning.destinationType === 'POST_OFFICE' &&
      !planning.destinationPostOfficeCode
    ) {
      return 'Select a destination post office.';
    }
    if (!planning.plannedDepartureAt || !planning.plannedArrivalAt) {
      return 'Planned departure and arrival times are required.';
    }
    if (
      new Date(planning.plannedArrivalAt).getTime() <=
      new Date(planning.plannedDepartureAt).getTime()
    ) {
      return 'Planned arrival must be after departure.';
    }
    return null;
  };

  const buildAutoPlanRequest = (
    execute: boolean
  ): AutoPlanBagDistributionRequest => ({
    origin_hub_id: planning.originHubId ?? 0,
    destination_type: planning.destinationType,
    destination_hub_id:
      planning.destinationType === 'HUB'
        ? planning.destinationHubId
        : undefined,
    destination_post_office_code:
      planning.destinationType === 'POST_OFFICE'
        ? planning.destinationPostOfficeCode
        : undefined,
    planned_departure_at: toApiDateTime(planning.plannedDepartureAt),
    planned_arrival_at: toApiDateTime(planning.plannedArrivalAt),
    sealed_sla_hours: parseNumber(planning.sealedSlaHours),
    execute,
    note: planning.note || undefined,
  });

  const handleAutoPlan = async (execute: boolean) => {
    if (!canManage) {
      notification.error('Bag distribution planning requires manager access.');
      return;
    }
    const error = validatePlan();
    if (error) {
      notification.error(error);
      return;
    }

    try {
      const result = await autoPlanDistribution(
        buildAutoPlanRequest(execute)
      ).unwrap();
      setPlanResult(result);
      if (execute) {
        setSelectedBagIds([]);
        notification.success(
          `${result.manifestCount} manifest${
            result.manifestCount === 1 ? '' : 's'
          } created from auto plan.`
        );
        void refetchReadyBags();
        void refetchManifests();
      } else {
        notification.success('Distribution plan preview is ready.');
      }
    } catch (err) {
      notification.error(
        execute ? 'Failed to execute auto plan.' : 'Failed to preview plan.',
        {
          description: getErrorMessage(err),
        }
      );
    }
  };

  const handleCreateManual = async () => {
    if (!canManage) {
      notification.error('Manual manifest creation requires manager access.');
      return;
    }
    const error = validatePlan();
    if (error) {
      notification.error(error);
      return;
    }
    if (!planning.routeId) {
      notification.error('Select a route for manual manifest creation.');
      return;
    }
    if (!planning.vehicleId) {
      notification.error('Select a vehicle for manual manifest creation.');
      return;
    }
    if (selectedBagIds.length === 0) {
      notification.error('Select at least one sealed bag.');
      return;
    }
    if (selectedDestinationSummary?.sameDestination === false) {
      notification.error(
        'Selected bags must share the same origin and destination.'
      );
      return;
    }
    if (
      selectedDestinationSummary &&
      (selectedDestinationSummary.originHubId !== planning.originHubId ||
        selectedDestinationSummary.destinationType !==
          planning.destinationType ||
        selectedDestinationSummary.destinationHubId !==
          planning.destinationHubId ||
        selectedDestinationSummary.destinationPostOfficeCode !==
          planning.destinationPostOfficeCode)
    ) {
      notification.error(
        'Selected bags must match the current plan lane. Use selected destination or adjust the plan.'
      );
      return;
    }

    const request: CreateBagDistributionManifestRequest = {
      origin_hub_id: planning.originHubId ?? 0,
      destination_type: planning.destinationType,
      destination_hub_id:
        planning.destinationType === 'HUB'
          ? planning.destinationHubId
          : undefined,
      destination_post_office_code:
        planning.destinationType === 'POST_OFFICE'
          ? planning.destinationPostOfficeCode
          : undefined,
      route_id: planning.routeId,
      vehicle_id: planning.vehicleId,
      planned_departure_at: toApiDateTime(planning.plannedDepartureAt),
      planned_arrival_at: toApiDateTime(planning.plannedArrivalAt),
      bag_ids: selectedBagIds,
      note: planning.note || undefined,
    };

    try {
      const manifest = await createManifest(request).unwrap();
      setSelectedBagIds([]);
      setSelectedManifestId(manifest.id);
      setActiveTab('manifests');
      notification.success('Bag distribution manifest created successfully.');
    } catch (err) {
      notification.error('Failed to create manifest.', {
        description: getErrorMessage(err),
      });
    }
  };

  const handleConfirmOutbound = async (manifest: BagDistributionManifest) => {
    if (!canOperate) {
      notification.error(
        'Outbound confirmation requires hub operation access.'
      );
      return;
    }
    try {
      await confirmOutbound(manifest.id).unwrap();
      notification.success('Outbound confirmed. Bags are now in transit.');
    } catch (err) {
      notification.error('Failed to confirm outbound.', {
        description: getErrorMessage(err),
      });
    }
  };

  const handleConfirmInbound = async (
    manifest: BagDistributionManifest,
    bagIds?: number[]
  ) => {
    if (!canOperate) {
      notification.error('Inbound confirmation requires hub operation access.');
      return;
    }
    try {
      await confirmInbound({
        manifestId: manifest.id,
        body: bagIds?.length ? { bag_ids: bagIds } : undefined,
      }).unwrap();
      notification.success('Inbound confirmed. Bags have arrived.');
    } catch (err) {
      notification.error('Failed to confirm inbound.', {
        description: getErrorMessage(err),
      });
    }
  };

  const handleCancel = async (manifest: BagDistributionManifest) => {
    if (!canManage) {
      notification.error('Cancelling a manifest requires manager access.');
      return;
    }
    try {
      await cancelManifest(manifest.id).unwrap();
      notification.success('Manifest cancelled successfully.');
    } catch (err) {
      notification.error('Failed to cancel manifest.', {
        description: getErrorMessage(err),
      });
    }
  };

  const openCheckin = (
    manifest: BagDistributionManifest,
    mode: CheckinMode
  ) => {
    if (!canCheckin) {
      notification.error('Driver check-in requires assigned driver access.');
      return;
    }
    setCheckinTarget({ manifest, mode });
    setCheckinState({ latitude: '', longitude: '', locationLabel: '' });
  };

  const handleSubmitCheckin = async () => {
    if (!checkinTarget) return;
    const latitude = Number(checkinState.latitude);
    const longitude = Number(checkinState.longitude);
    if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) {
      notification.error('Latitude and longitude are required.');
      return;
    }
    if (!checkinState.photo) {
      notification.error('Upload a check-in photo.');
      return;
    }

    const formData = new FormData();
    formData.append('latitude', String(latitude));
    formData.append('longitude', String(longitude));
    if (checkinState.locationLabel.trim()) {
      formData.append('location_label', checkinState.locationLabel.trim());
    }
    formData.append('photo', checkinState.photo);

    try {
      if (checkinTarget.mode === 'start') {
        await driverCheckinStart({
          manifestId: checkinTarget.manifest.id,
          formData,
        }).unwrap();
        notification.success('Start check-in submitted successfully.');
      } else {
        await driverCheckinEnd({
          manifestId: checkinTarget.manifest.id,
          formData,
        }).unwrap();
        notification.success('End check-in submitted successfully.');
      }
      setCheckinTarget(null);
      void refetchDriverManifests();
    } catch (err) {
      notification.error('Failed to submit check-in.', {
        description: getErrorMessage(err),
      });
    }
  };

  const totalReady = readyBagsData?.totalItems ?? readyBags.length;
  const createdCount = manifests.filter(
    (manifest) => manifest.status === 'CREATED'
  ).length;
  const inTransitCount = manifests.filter(
    (manifest) => manifest.status === 'OUTBOUND_CONFIRMED'
  ).length;

  return (
    <div className='space-y-6 p-6'>
      <div className='flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between'>
        <div className='space-y-2'>
          <div className='flex flex-wrap items-center gap-2'>
            <Badge variant='outline'>Second-mile Operations</Badge>
            <Badge variant='secondary'>Bag-level dispatch</Badge>
          </div>
          <div>
            <h1 className='text-2xl font-semibold tracking-tight'>
              Bag Distribution
            </h1>
            <p className='max-w-3xl text-sm text-muted-foreground'>
              Plan sealed bags onto routes, create manifests, confirm outbound,
              and receive bags at the destination.
            </p>
          </div>
        </div>
        <div className='flex flex-wrap gap-2'>
          <Button
            variant='outline'
            onClick={() => {
              void refetchReadyBags();
              void refetchManifests();
              void refetchDriverManifests();
            }}
          >
            <RefreshCw className='h-4 w-4' />
            Refresh
          </Button>
        </div>
      </div>

      <div className='grid gap-3 md:grid-cols-4'>
        <KpiCard
          icon={Boxes}
          label='Ready bags'
          value={formatNumber(totalReady, 0)}
          hint='Sealed bags available for distribution'
        />
        <KpiCard
          icon={ClipboardList}
          label='Created manifests'
          value={formatNumber(createdCount, 0)}
          hint='Waiting for outbound confirmation'
        />
        <KpiCard
          icon={Truck}
          label='In transit'
          value={formatNumber(inTransitCount, 0)}
          hint='Outbound confirmed manifests'
        />
        <KpiCard
          icon={PackageCheck}
          label='Selected bags'
          value={formatNumber(selectedBagIds.length, 0)}
          hint='Used for manual manifest creation'
        />
      </div>

      <Tabs
        value={activeTab}
        onValueChange={(value) => setActiveTab(value as TabValue)}
      >
        <TabsList className='grid w-full grid-cols-2 lg:w-auto lg:grid-cols-4'>
          <TabsTrigger value='ready'>Ready bags</TabsTrigger>
          <TabsTrigger value='planning'>Planning</TabsTrigger>
          <TabsTrigger value='manifests'>Manifests</TabsTrigger>
          <TabsTrigger value='driver'>Driver check-in</TabsTrigger>
        </TabsList>

        <TabsContent value='ready' className='mt-4'>
          <ReadyBagsTab
            bags={readyBags}
            selectedBagIds={selectedBagIds}
            isFetching={isFetchingReadyBags}
            readySearch={readySearch}
            destinationFilter={destinationFilter}
            onReadySearchChange={setReadySearch}
            onDestinationFilterChange={setDestinationFilter}
            onToggleBag={toggleBag}
            onSelectAll={(checked) =>
              setSelectedBagIds(checked ? readyBags.map((bag) => bag.id) : [])
            }
            onUseBagDestination={handleUseBagDestination}
            selectedDestinationSummary={selectedDestinationSummary}
          />
        </TabsContent>

        <TabsContent value='planning' className='mt-4'>
          <PlanningTab
            planning={planning}
            canManage={canManage}
            hubs={hubs}
            postOffices={postOffices}
            routes={routes}
            vehicles={vehicles}
            hubOptions={hubOptions}
            postOfficeOptions={postOfficeOptions}
            routeOptions={routeOptions}
            vehicleOptions={vehicleOptions}
            isFetchingHubs={isFetchingHubs}
            isFetchingPostOffices={isFetchingPostOffices}
            isFetchingRoutes={isFetchingRoutes}
            isFetchingVehicles={isFetchingVehicles}
            selectedBags={selectedBags}
            selectedDestinationSummary={selectedDestinationSummary}
            planResult={planResult}
            isPlanning={isPlanning}
            isCreatingManifest={isCreatingManifest}
            onUpdatePlanning={updatePlanning}
            onAutoPlan={handleAutoPlan}
            onCreateManual={handleCreateManual}
          />
        </TabsContent>

        <TabsContent value='manifests' className='mt-4'>
          <ManifestTab
            manifests={manifests}
            selectedManifestId={selectedManifestId}
            manifestStatus={manifestStatus}
            isFetching={isFetchingManifests}
            canManage={canManage}
            canOperate={canOperate}
            isConfirmingOutbound={isConfirmingOutbound}
            isConfirmingInbound={isConfirmingInbound}
            isCancelling={isCancelling}
            onStatusChange={setManifestStatus}
            onSelectManifest={setSelectedManifestId}
            onViewManifest={setDetailManifestId}
            onConfirmOutbound={handleConfirmOutbound}
            onConfirmInbound={(manifest) => handleConfirmInbound(manifest)}
            onCancel={handleCancel}
          />
        </TabsContent>

        <TabsContent value='driver' className='mt-4'>
          <DriverTab
            manifests={driverManifests}
            isFetching={isFetchingDriverManifests}
            canCheckin={canCheckin}
            onOpenCheckin={openCheckin}
            onViewManifest={setDetailManifestId}
          />
        </TabsContent>
      </Tabs>

      <ManifestDetailDialog
        open={Boolean(detailManifestId)}
        manifest={selectedManifestDetail}
        isFetching={isFetchingDetail}
        canOperate={canOperate}
        canManage={canManage}
        onOpenChange={(open) => {
          if (!open) setDetailManifestId(null);
        }}
        onConfirmOutbound={handleConfirmOutbound}
        onConfirmInbound={handleConfirmInbound}
        onCancel={handleCancel}
      />

      <CheckinDialog
        target={checkinTarget}
        state={checkinState}
        isLoading={isCheckingInStart || isCheckingInEnd}
        onStateChange={setCheckinState}
        onOpenChange={(open) => {
          if (!open) setCheckinTarget(null);
        }}
        onSubmit={handleSubmitCheckin}
      />
    </div>
  );
}
