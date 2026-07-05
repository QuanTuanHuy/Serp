/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Second-mile bag dispatchers page
 */

'use client';

import * as React from 'react';
import { Boxes, PackageCheck, RefreshCw } from 'lucide-react';

import { getErrorMessage, useAppSelector } from '@/lib/store';
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
  useCreateBagDistributionManifestMutation,
  useGetHubsQuery,
  useGetPostOfficesQuery,
  useGetSecondMileBagsQuery,
  useGetSecondMileRoutesQuery,
  useGetSecondMileVehiclesQuery,
} from '../../../api/firstMileApi';
import type {
  AutoPlanBagDistributionRequest,
  BagDistributionPlan,
  CreateBagDistributionManifestRequest,
  SecondMileBag,
} from '../../../types';
import { KpiCard, PlanningTab, ReadyBagsTab } from './components';
import {
  buildHubOptions,
  buildPostOfficeOptions,
  buildRouteOptions,
  buildVehicleOptions,
  canManageDistribution,
  formatNumber,
  makeDefaultPlanningState,
  parseNumber,
  toApiDateTime,
  type DestinationFilter,
  type PlanningState,
  type SelectedBagSummary,
  type TabValue,
} from './secondMileDispatchModels';

export function SecondMileDispatchersPage() {
  const roles = useAppSelector(
    (state) => state.account.user.profile?.roles ?? []
  );
  const notification = useNotification();
  const canManage = canManageDistribution(roles);

  const [activeTab, setActiveTab] = React.useState<TabValue>('planning');
  const [planning, setPlanning] = React.useState<PlanningState>(
    makeDefaultPlanningState
  );
  const [selectedBagIds, setSelectedBagIds] = React.useState<number[]>([]);
  const [readySearch, setReadySearch] = React.useState('');
  const [destinationFilter, setDestinationFilter] =
    React.useState<DestinationFilter>('ALL');
  const [planResult, setPlanResult] =
    React.useState<BagDistributionPlan | null>(null);

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

  const [autoPlanDistribution, { isLoading: isPlanning }] =
    useAutoPlanBagDistributionMutation();
  const [createManifest, { isLoading: isCreatingManifest }] =
    useCreateBagDistributionManifestMutation();

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
      notification.error('Các túi hàng được chọn phải cùng điểm đến.');
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
    if (!planning.originHubId) return 'Chọn hub xuất phát.';
    if (planning.destinationType === 'HUB' && !planning.destinationHubId) {
      return 'Chọn hub đích.';
    }
    if (
      planning.destinationType === 'POST_OFFICE' &&
      !planning.destinationPostOfficeCode
    ) {
      return 'Chọn bưu cục đích.';
    }
    if (!planning.plannedDepartureAt || !planning.plannedArrivalAt) {
      return 'Nhập thời gian xuất phát và thời gian đến dự kiến.';
    }
    if (
      new Date(planning.plannedArrivalAt).getTime() <=
      new Date(planning.plannedDepartureAt).getTime()
    ) {
      return 'Thời gian đến dự kiến phải sau thời gian xuất phát.';
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
      notification.error('Bạn cần quyền quản lý hub để lập kế hoạch.');
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
          `Đã tạo ${result.manifestCount} biên bản từ kế hoạch tự động.`
        );
        void refetchReadyBags();
      } else {
        notification.success('Đã sẵn sàng bản xem trước kế hoạch.');
      }
    } catch (err) {
      notification.error(
        execute
          ? 'Không thể chạy kế hoạch tự động.'
          : 'Không thể xem trước kế hoạch.',
        {
          description: getErrorMessage(err),
        }
      );
    }
  };

  const handleCreateManual = async () => {
    if (!canManage) {
      notification.error('Bạn cần quyền quản lý hub để tạo biên bản thủ công.');
      return;
    }
    const error = validatePlan();
    if (error) {
      notification.error(error);
      return;
    }
    if (!planning.routeId) {
      notification.error('Chọn tuyến cho biên bản thủ công.');
      return;
    }
    if (!planning.vehicleId) {
      notification.error('Chọn xe cho biên bản thủ công.');
      return;
    }
    if (selectedBagIds.length === 0) {
      notification.error('Chọn ít nhất một túi hàng đã niêm phong.');
      return;
    }
    if (selectedDestinationSummary?.sameDestination === false) {
      notification.error(
        'Các túi hàng được chọn phải cùng hub xuất phát và điểm đến.'
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
        'Túi hàng được chọn phải khớp chặng đang lập kế hoạch. Hãy dùng điểm đến đã chọn hoặc điều chỉnh kế hoạch.'
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
      await createManifest(request).unwrap();
      setSelectedBagIds([]);
      setActiveTab('ready');
      notification.success('Đã tạo biên bản điều phối thành công.');
    } catch (err) {
      notification.error('Không thể tạo biên bản điều phối.', {
        description: getErrorMessage(err),
      });
    }
  };

  const totalReady = readyBagsData?.totalItems ?? readyBags.length;
  return (
    <div className='space-y-6 p-6'>
      <div className='flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between'>
        <div className='space-y-2'>
          <div>
            <h1 className='text-2xl font-semibold tracking-tight'>
              Điều phối hàng hóa chặng trung chuyển
            </h1>
            <p className='max-w-3xl text-sm text-muted-foreground'>
              Lập kế hoạch gom túi hàng theo tuyến, tạo biên bản điều phối và
              hỗ trợ tài xế check-in trong chặng trung chuyển.
            </p>
          </div>
        </div>
        <div className='flex flex-wrap gap-2'>
          <Button
            variant='outline'
            onClick={() => {
              void refetchReadyBags();
            }}
          >
            <RefreshCw className='h-4 w-4' />
            Làm mới
          </Button>
        </div>
      </div>

      <div className='grid gap-3 md:grid-cols-2'>
        <KpiCard
          icon={Boxes}
          label='Túi sẵn sàng'
          value={formatNumber(totalReady, 0)}
          hint='Túi đã niêm phong chờ điều phối'
        />
        <KpiCard
          icon={PackageCheck}
          label='Túi đã chọn'
          value={formatNumber(selectedBagIds.length, 0)}
          hint='Dùng để tạo biên bản thủ công'
        />
      </div>

      <Tabs
        value={activeTab}
        onValueChange={(value) => setActiveTab(value as TabValue)}
      >
        <TabsList className='grid w-full grid-cols-2 lg:w-auto'>
          <TabsTrigger value='ready'>Túi sẵn sàng</TabsTrigger>
          <TabsTrigger value='planning'>Lập kế hoạch</TabsTrigger>
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

      </Tabs>
    </div>
  );
}
