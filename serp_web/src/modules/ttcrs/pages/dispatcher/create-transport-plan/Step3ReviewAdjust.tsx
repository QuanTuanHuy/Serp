import { useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import dynamic from 'next/dynamic';
import {
  AlertCircle,
  AlertTriangle,
  ArrowDown,
  ArrowLeft,
  ArrowUp,
  ClipboardCheck,
  Loader2,
  MoveHorizontal,
  Route,
  Truck,
  User,
  XCircle,
} from 'lucide-react';
import { toast } from 'sonner';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useGetDispatcherLocationsQuery,
  useGetDispatcherDriversQuery,
  useSaveTransportPlansMutation,
} from '../../../api/ttcrsApi';
import type { TransportPlanResult } from '../../../types';
import type { RoutePolylineData } from '../../../components/RouteMapPanel';
import { ROUTE_COLORS } from './constants';
import {
  EditableRoute,
  generatePlanOutput,
  initEditableRoutes,
  isDepotStop,
  RouteValidation,
  validateRoute,
} from './routeEditing';

const DynamicRouteMapPanel = dynamic(
  () =>
    import('../../../components/RouteMapPanel').then((module) => ({
      default: module.RouteMapPanel,
    })),
  {
    ssr: false,
    loading: () => (
      <div className='flex h-full w-full items-center justify-center rounded-lg bg-muted/30 animate-pulse'>
        <span className='text-xs text-muted-foreground'>Loading map...</span>
      </div>
    ),
  }
);

interface Step3Props {
  planResult: TransportPlanResult;
  onBack: () => void;
}

export function Step3ReviewAdjust({ planResult, onBack }: Step3Props) {
  const router = useRouter();
  const [routes, setRoutes] = useState<EditableRoute[]>(() =>
    initEditableRoutes(planResult)
  );

  const validations = useMemo<RouteValidation[]>(
    () => routes.map(validateRoute),
    [routes]
  );

  const totalUnscheduled =
    (planResult.unscheduledExEmptyRequests?.length ?? 0) +
    (planResult.unscheduledExLadenRequests?.length ?? 0) +
    (planResult.unscheduledImEmptyRequests?.length ?? 0) +
    (planResult.unscheduledImLadenRequests?.length ?? 0);

  const stats = planResult.statisticInformation;
  const allValid = validations.every((validation) => validation.isValid);

  const { data: locationsData, isLoading: isLoadingLocations } =
    useGetDispatcherLocationsQuery();

  const { data: driversData } = useGetDispatcherDriversQuery();
  const drivers = driversData?.data ?? [];

  const handleAssignDriver = (routeIdx: number, driverIdStr: string) => {
    const driverId = driverIdStr ? Number(driverIdStr) : undefined;
    setRoutes((prev) =>
      prev.map((route, idx) =>
        idx === routeIdx ? { ...route, driverId } : route
      )
    );
  };

  const locationGeoMap = useMemo(() => {
    const map = new Map<string, { lat: number; lng: number }>();
    (locationsData?.data ?? []).forEach((location) => {
      if (location.lat != null && location.lng != null) {
        map.set(location.locationCode, {
          lat: location.lat,
          lng: location.lng,
        });
      }
    });
    return map;
  }, [locationsData]);

  const routePolylines = useMemo<RoutePolylineData[]>(() => {
    return routes.map((route, index) => {
      const stopsWithCoords = route.stops
        .map((stop) => {
          const geo = locationGeoMap.get(stop.locationCode);
          if (!geo) {
            return null;
          }

          return {
            ...stop,
            isDepot: isDepotStop(stop),
            lat: geo.lat,
            lng: geo.lng,
          };
        })
        .filter((stop): stop is NonNullable<typeof stop> => stop !== null);

      return {
        truckCode: route.truckCode || `Truck ${index + 1}`,
        color: ROUTE_COLORS[index % ROUTE_COLORS.length],
        coords: stopsWithCoords.map(
          (stop) => [stop.lat, stop.lng] as [number, number]
        ),
        stops: stopsWithCoords,
      };
    });
  }, [routes, locationGeoMap]);

  const handleMoveStop = (
    fromRouteIdx: number,
    stopIdx: number,
    toRouteIdx: number
  ) => {
    setRoutes((prev) => {
      const next = prev.map((route) => ({ ...route, stops: [...route.stops] }));
      const [stop] = next[fromRouteIdx].stops.splice(stopIdx, 1);

      const targetStops = next[toRouteIdx].stops;
      let insertIdx = targetStops.length;
      for (let i = targetStops.length - 1; i >= 0; i -= 1) {
        if (isDepotStop(targetStops[i])) {
          insertIdx = i;
        } else {
          break;
        }
      }

      targetStops.splice(insertIdx, 0, stop);
      return next;
    });
  };

  const handleMoveWithinRoute = (
    routeIdx: number,
    stopIdx: number,
    direction: 'up' | 'down'
  ) => {
    setRoutes((prev) => {
      const next = prev.map((route) => ({ ...route, stops: [...route.stops] }));
      const stops = next[routeIdx].stops;

      const nonDepotIndices = stops
        .map((stop, index) => ({ stop, index }))
        .filter(({ stop }) => !isDepotStop(stop))
        .map(({ index }) => index);

      const position = nonDepotIndices.indexOf(stopIdx);
      if (position === -1) {
        return prev;
      }

      const targetPos = direction === 'up' ? position - 1 : position + 1;
      if (targetPos < 0 || targetPos >= nonDepotIndices.length) {
        return prev;
      }

      const targetIdx = nonDepotIndices[targetPos];
      [stops[stopIdx], stops[targetIdx]] = [stops[targetIdx], stops[stopIdx]];
      return next;
    });
  };

  const [saveTransportPlans, { isLoading: isSaving }] =
    useSaveTransportPlansMutation();

  const handleFinish = async () => {
    const allErrors = validations.flatMap((v) => v.errors);
    if (allErrors.length > 0) {
      toast.error(`Cannot finish: ${allErrors[0]}`);
      return;
    }

    const payload = {
      plans: routes.map((route) => ({
        truckCode: route.truckCode,
        driverId: route.driverId ?? null,
        stops: route.stops.map((stop, idx) => ({
          sequence: idx + 1,
          locationCode: stop.locationCode,
          action: stop.action,
          plannedArrival: stop.arrivalTime,
          requestId: stop.requestId,
        })),
      })),
    };

    try {
      await saveTransportPlans(payload).unwrap();
      const hasWarnings = validations.some((v) => v.warnings.length > 0);
      toast.success(
        hasWarnings
          ? 'Transport plans saved with warnings.'
          : 'Transport plans saved successfully.'
      );
      router.push('/ttcrs/dispatcher/routes');
    } catch {
      toast.error('Failed to save transport plans. Please try again.');
    }
  };

  return (
    <div className='flex flex-col gap-4'>
      <div className='flex items-center justify-between'>
        <p className='text-sm text-muted-foreground'>
          Review algorithm output then click Finish.
        </p>
        <div className='flex items-center gap-2'>
          <Button variant='outline' size='sm' onClick={onBack}>
            <ArrowLeft className='mr-1 h-4 w-4' />
            Back
          </Button>
          <Button
            size='sm'
            className='bg-orange-600 text-white hover:bg-orange-700 disabled:opacity-50'
            onClick={handleFinish}
            disabled={!allValid || isSaving}
            title={
              !allValid ? 'Fix validation errors before finishing' : undefined
            }
          >
            {isSaving ? (
              <Loader2 className='mr-1 h-4 w-4 animate-spin' />
            ) : (
              <ClipboardCheck className='mr-1 h-4 w-4' />
            )}
            {isSaving ? 'Saving...' : 'Finish'}
          </Button>
        </div>
      </div>

      <div className='grid grid-cols-2 gap-3 sm:grid-cols-3'>
        {[
          { label: 'Total Requests', value: stats?.totalRequests ?? 0 },
          {
            label: 'Rejected',
            value: stats?.totalRejectedRequests ?? 0,
            highlight: (stats?.totalRejectedRequests ?? 0) > 0,
          },
          { label: 'Trucks Used', value: routes.length },

        ].map(({ label, value, highlight }) => (
          <Card key={label} className='gap-1 py-3'>
            <CardContent className='px-4 py-0'>
              <p className='text-xs text-muted-foreground'>{label}</p>
              <p
                className={cn(
                  'text-2xl font-bold',
                  highlight ? 'text-destructive' : 'text-foreground'
                )}
              >
                {value}
              </p>
            </CardContent>
          </Card>
        ))}
      </div>

      {totalUnscheduled > 0 && (
        <div className='flex items-start gap-3 rounded-lg border border-amber-300 bg-amber-50 p-4 dark:bg-amber-950/20'>
          <AlertCircle className='mt-0.5 h-5 w-5 shrink-0 text-amber-600' />
          <div>
            <p className='text-sm font-semibold text-amber-700 dark:text-amber-400'>
              {totalUnscheduled} unscheduled request
              {totalUnscheduled > 1 ? 's' : ''}
            </p>
            <p className='mt-0.5 text-xs text-muted-foreground'>
              These could not be scheduled by the algorithm. Redistribute stops
              between routes to accommodate them, or go back and add more
              resources.
            </p>
          </div>
        </div>
      )}

      <div className='flex items-start gap-4'>
        <div className='min-w-0 flex-1 overflow-x-auto pb-2'>
          {routes.length === 0 ? (
            <div className='flex flex-col items-center justify-center gap-3 py-20'>
              <Route className='h-12 w-12 text-muted-foreground/40' />
              <p className='text-sm text-muted-foreground'>
                No routes were generated.
              </p>
            </div>
          ) : (
            <div className='flex gap-4' style={{ width: 'max-content' }}>
              {routes.map((route, routeIdx) => {
                const validation = validations[routeIdx];
                const nonDepotCount = route.stops.filter(
                  (stop) => !isDepotStop(stop)
                ).length;
                const nonDepotIndices = route.stops
                  .map((stop, index) => ({ stop, index }))
                  .filter(({ stop }) => !isDepotStop(stop))
                  .map(({ index }) => index);
                const routeColor = ROUTE_COLORS[routeIdx % ROUTE_COLORS.length];

                return (
                  <Card
                    key={route.truckCode + routeIdx}
                    className='flex flex-col gap-0 overflow-hidden py-0'
                    style={{ width: '288px', minWidth: '288px' }}
                  >
                    <CardHeader
                      className='shrink-0 border-b px-4 py-3'
                      style={{ borderLeft: `4px solid ${routeColor}` }}
                    >
                      <div className='flex items-center justify-between gap-2'>
                        <div className='flex min-w-0 items-center gap-2'>
                          <Truck
                            className='h-4 w-4 shrink-0'
                            style={{ color: routeColor }}
                          />
                          <span className='truncate font-mono text-sm font-semibold'>
                            {route.truckCode || `Truck ${routeIdx + 1}`}
                          </span>
                        </div>
                        {/* {validation.errors.length > 0 ? (
                          <Badge className='shrink-0 border-red-200 bg-red-100 text-xs text-red-700'>
                            x {validation.errors.length}
                          </Badge>
                        ) : validation.warnings.length > 0 ? (
                          <Badge className='shrink-0 border-amber-200 bg-amber-100 text-xs text-amber-700'>
                            ! {validation.warnings.length}
                          </Badge>
                        ) : (
                          <Badge className='shrink-0 border-green-200 bg-green-100 text-xs text-green-700'>
                            Valid
                          </Badge>
                        )} */}
                      </div>
                      <p className='mt-0.5 text-xs text-muted-foreground'>
                        {nonDepotCount} op. stop{nonDepotCount !== 1 ? 's' : ''}
                      </p>
                      <div className='mt-2 flex items-center gap-1.5'>
                        <User className='h-3.5 w-3.5 shrink-0 text-muted-foreground' />
                        <Select
                          value={route.driverId ? String(route.driverId) : ''}
                          onValueChange={(val) =>
                            handleAssignDriver(routeIdx, val)
                          }
                        >
                          <SelectTrigger className='h-7 flex-1 text-xs'>
                            <SelectValue placeholder='Assign driver...' />
                          </SelectTrigger>
                          <SelectContent>
                            {drivers.map((driver) => (
                              <SelectItem
                                key={driver.userId}
                                value={String(driver.userId)}
                                className='text-xs'
                              >
                                {driver.name}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      </div>
                    </CardHeader>

                    <CardContent className='max-h-96 flex-1 overflow-y-auto p-0'>
                      <div className='divide-y divide-border'>
                        {route.stops.length === 0 ? (
                          <p className='py-6 text-center text-xs italic text-muted-foreground'>
                            No stops
                          </p>
                        ) : (
                          route.stops.map((stop, stopIdx) => {
                            const depot = isDepotStop(stop);
                            const posInNonDepot =
                              nonDepotIndices.indexOf(stopIdx);
                            const canMoveUp = !depot && posInNonDepot > 0;
                            const canMoveDown =
                              !depot &&
                              posInNonDepot < nonDepotIndices.length - 1;

                            return (
                              <div
                                key={stopIdx}
                                className={cn(
                                  'flex items-center gap-1.5 px-3 py-2',
                                  depot
                                    ? 'bg-muted/40 text-muted-foreground italic'
                                    : 'hover:bg-muted/20'
                                )}
                              >
                                <span className='w-4 shrink-0 text-center font-mono text-xs text-muted-foreground/60'>
                                  {stopIdx + 1}
                                </span>

                                <div className='min-w-0 flex-1'>
                                  <p className='truncate font-mono text-xs font-medium'>
                                    {stop.locationCode}
                                  </p>
                                  <p className='truncate text-xs text-muted-foreground'>
                                    {stop.action}
                                  </p>
                                </div>

                                {stop.arrivalTime && (
                                  <div className='shrink-0 text-right font-mono text-xs text-muted-foreground'>
                                    <p>{stop.arrivalTime.slice(0, 10)}</p>
                                    <p>{stop.arrivalTime.slice(11, 16)}</p>
                                  </div>
                                )}

                                {/* {depot ? (
                                  <span className='shrink-0 text-xs text-muted-foreground/40'>
                                    lock
                                  </span>
                                ) : (
                                  <div className='flex shrink-0 items-center gap-0.5'>
                                    <Button
                                      type='button'
                                      variant='ghost'
                                      size='icon'
                                      className='h-5 w-5 text-muted-foreground'
                                      disabled={!canMoveUp}
                                      onClick={() =>
                                        handleMoveWithinRoute(routeIdx, stopIdx, 'up')
                                      }
                                      title='Move up'
                                    >
                                      <ArrowUp className='h-3 w-3' />
                                    </Button>
                                    <Button
                                      type='button'
                                      variant='ghost'
                                      size='icon'
                                      className='h-5 w-5 text-muted-foreground'
                                      disabled={!canMoveDown}
                                      onClick={() =>
                                        handleMoveWithinRoute(routeIdx, stopIdx, 'down')
                                      }
                                      title='Move down'
                                    >
                                      <ArrowDown className='h-3 w-3' />
                                    </Button>
                                    {routes.length > 1 && (
                                      <DropdownMenu>
                                        <DropdownMenuTrigger asChild>
                                          <Button
                                            type='button'
                                            variant='ghost'
                                            size='icon'
                                            className='h-5 w-5 text-muted-foreground'
                                            title='Move to another route'
                                          >
                                            <MoveHorizontal className='h-3 w-3' />
                                          </Button>
                                        </DropdownMenuTrigger>
                                        <DropdownMenuContent align='end'>
                                          <p className='px-2 py-1.5 text-xs font-medium text-muted-foreground'>
                                            Move to route:
                                          </p>
                                          {routes.map((targetRoute, targetRouteIdx) => {
                                            if (targetRouteIdx === routeIdx) {
                                              return null;
                                            }

                                            return (
                                              <DropdownMenuItem
                                                key={targetRouteIdx}
                                                className='font-mono text-xs'
                                                onSelect={() =>
                                                  handleMoveStop(
                                                    routeIdx,
                                                    stopIdx,
                                                    targetRouteIdx
                                                  )
                                                }
                                              >
                                                <span
                                                  className='mr-2 inline-block h-2 w-2 shrink-0 rounded-full'
                                                  style={{
                                                    backgroundColor:
                                                      ROUTE_COLORS[
                                                        targetRouteIdx % ROUTE_COLORS.length
                                                      ],
                                                  }}
                                                />
                                                {targetRoute.truckCode ||
                                                  `Truck ${targetRouteIdx + 1}`}
                                              </DropdownMenuItem>
                                            );
                                          })}
                                        </DropdownMenuContent>
                                      </DropdownMenu>
                                    )}
                                  </div>
                                )} */}
                              </div>
                            );
                          })
                        )}
                      </div>
                    </CardContent>

                    {(validation.errors.length > 0 ||
                      validation.warnings.length > 0) && (
                      <div className='shrink-0 space-y-0.5 border-t bg-muted/10 px-3 py-2'>
                        {validation.errors.map((error, index) => (
                          <p
                            key={index}
                            className='flex items-start gap-1 text-xs text-destructive'
                          >
                            <XCircle className='mt-0.5 h-3 w-3 shrink-0' />
                            {error}
                          </p>
                        ))}
                        {validation.warnings.map((warning, index) => (
                          <p
                            key={index}
                            className='flex items-start gap-1 text-xs text-amber-600 dark:text-amber-400'
                          >
                            <AlertTriangle className='mt-0.5 h-3 w-3 shrink-0' />
                            {warning}
                          </p>
                        ))}
                      </div>
                    )}
                  </Card>
                );
              })}
            </div>
          )}
        </div>

        <div
          className='sticky top-6 shrink-0 overflow-hidden rounded-lg border shadow-sm'
          style={{ width: '440px', height: '580px' }}
        >
          {isLoadingLocations ? (
            <div className='flex h-full items-center justify-center'>
              <Loader2 className='h-6 w-6 animate-spin text-muted-foreground' />
            </div>
          ) : (
            <DynamicRouteMapPanel routes={routePolylines} />
          )}
        </div>
      </div>
    </div>
  );
}
