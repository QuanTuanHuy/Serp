/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Bag distribution planning tab
 */

import { useState } from 'react';
import { ClipboardList, Loader2, Play, Sparkles } from 'lucide-react';

import {
  TmsCombobox,
  type TmsComboboxOption,
} from '@/modules/first-mile/components';
import { Button } from '@/shared/components/ui/button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui/tabs';
import { Textarea } from '@/shared/components/ui/textarea';

import type {
  BagDistributionPlan,
  Hub,
  PostOffice,
  SecondMileBag,
  SecondMileBagDestinationType,
  SecondMileRoute,
  SecondMileVehicle,
} from '../../../../types';
import type {
  PlanningState,
  SelectedBagSummary,
} from '../secondMileDispatchModels';
import {
  destinationOptions,
  formatNumber,
  parseNumber,
} from '../secondMileDispatchModels';
import { PlanResultCard } from './PlanResultCard';
import { SummaryItem } from './SummaryItem';

type PlanningMode = 'auto' | 'manual';

interface PlanningTabProps {
  planning: PlanningState;
  canManage: boolean;
  hubs: Hub[];
  postOffices: PostOffice[];
  routes: SecondMileRoute[];
  vehicles: SecondMileVehicle[];
  hubOptions: TmsComboboxOption[];
  postOfficeOptions: TmsComboboxOption[];
  routeOptions: TmsComboboxOption[];
  vehicleOptions: TmsComboboxOption[];
  isFetchingHubs: boolean;
  isFetchingPostOffices: boolean;
  isFetchingRoutes: boolean;
  isFetchingVehicles: boolean;
  selectedBags: SecondMileBag[];
  selectedBagCount: number;
  selectedDestinationSummary: SelectedBagSummary | null;
  planResult: BagDistributionPlan | null;
  isPlanning: boolean;
  isCreatingManifest: boolean;
  onUpdatePlanning: <K extends keyof PlanningState>(
    key: K,
    value: PlanningState[K]
  ) => void;
  onAutoPlan: (execute: boolean) => void;
  onCreateManual: () => void;
}

export function PlanningTab({
  planning,
  canManage,
  hubs,
  postOffices,
  routes,
  vehicles,
  hubOptions,
  postOfficeOptions,
  routeOptions,
  vehicleOptions,
  isFetchingHubs,
  isFetchingPostOffices,
  isFetchingRoutes,
  isFetchingVehicles,
  selectedBags,
  selectedBagCount,
  selectedDestinationSummary,
  planResult,
  isPlanning,
  isCreatingManifest,
  onUpdatePlanning,
  onAutoPlan,
  onCreateManual,
}: PlanningTabProps) {
  const originHub = hubs.find((hub) => hub.id === planning.originHubId);
  const destinationHub = hubs.find(
    (hub) => hub.id === planning.destinationHubId
  );
  const destinationPostOffice = postOffices.find(
    (postOffice) => postOffice.code === planning.destinationPostOfficeCode
  );
  const route = routes.find((item) => item.id === planning.routeId);
  const vehicle = vehicles.find((item) => item.id === planning.vehicleId);
  const [planningMode, setPlanningMode] = useState<PlanningMode>('auto');

  return (
    <div className='grid gap-4 xl:grid-cols-[430px_minmax(0,1fr)]'>
      <Card className='min-w-0'>
        <CardHeader>
          <CardTitle>Thiết lập kế hoạch</CardTitle>
          <CardDescription>
            Chọn chặng điều phối, sau đó dùng tự động để hệ thống gợi ý hoặc thủ
            công để tạo biên bản từ các túi đã chọn.
          </CardDescription>
        </CardHeader>
        <CardContent className='space-y-4'>
          {!canManage && (
            <div className='rounded-md border bg-muted/40 p-3 text-sm text-muted-foreground'>
              Bạn có thể xem biên bản, nhưng cần quyền TMS_ADMIN hoặc
              TMS_HUB_MANAGER để tạo kế hoạch.
            </div>
          )}

          <div className='space-y-2'>
            <Label>Hub xuất phát *</Label>
            <TmsCombobox
              value={planning.originHubId ? String(planning.originHubId) : ''}
              onValueChange={(value) =>
                onUpdatePlanning('originHubId', parseNumber(value))
              }
              options={hubOptions}
              placeholder={isFetchingHubs ? 'Đang tải hub...' : 'Chọn hub'}
              emptyText='Không tìm thấy hub đang hoạt động'
              loading={isFetchingHubs}
              clearable
            />
          </div>

          <div className='space-y-2'>
            <Label>Loại điểm đến *</Label>
            <Select
              value={planning.destinationType}
              onValueChange={(value) =>
                onUpdatePlanning(
                  'destinationType',
                  value as SecondMileBagDestinationType
                )
              }
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {destinationOptions.map((option) => (
                  <SelectItem key={option.value} value={option.value}>
                    {option.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          {planning.destinationType === 'HUB' ? (
            <div className='space-y-2'>
              <Label>Hub đích *</Label>
              <TmsCombobox
                value={
                  planning.destinationHubId
                    ? String(planning.destinationHubId)
                    : ''
                }
                onValueChange={(value) =>
                  onUpdatePlanning('destinationHubId', parseNumber(value))
                }
                options={hubOptions}
                placeholder='Chọn hub đích'
                emptyText='Không tìm thấy hub đang hoạt động'
                loading={isFetchingHubs}
                clearable
              />
            </div>
          ) : (
            <div className='space-y-2'>
              <Label>Bưu cục đích *</Label>
              <TmsCombobox
                value={planning.destinationPostOfficeCode ?? ''}
                onValueChange={(value) =>
                  onUpdatePlanning(
                    'destinationPostOfficeCode',
                    value || undefined
                  )
                }
                options={postOfficeOptions}
                placeholder={
                  isFetchingPostOffices ? 'Đang tải bưu cục...' : 'Chọn bưu cục'
                }
                emptyText='Không tìm thấy bưu cục đang hoạt động'
                loading={isFetchingPostOffices}
                clearable
              />
            </div>
          )}

          <div className='grid gap-3 md:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='distribution-departure'>
                Thời gian xuất phát *
              </Label>
              <Input
                id='distribution-departure'
                type='datetime-local'
                value={planning.plannedDepartureAt}
                onChange={(event) =>
                  onUpdatePlanning('plannedDepartureAt', event.target.value)
                }
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='distribution-arrival'>Thời gian đến *</Label>
              <Input
                id='distribution-arrival'
                type='datetime-local'
                value={planning.plannedArrivalAt}
                onChange={(event) =>
                  onUpdatePlanning('plannedArrivalAt', event.target.value)
                }
              />
            </div>
          </div>

          <Tabs
            value={planningMode}
            onValueChange={(value) => setPlanningMode(value as PlanningMode)}
          >
            <TabsList className='grid w-full grid-cols-2'>
              <TabsTrigger value='auto'>Điều phối tự động</TabsTrigger>
              <TabsTrigger value='manual'>Điều phối thủ công</TabsTrigger>
            </TabsList>

            <TabsContent value='auto' className='mt-4'>
              <div className='rounded-md border bg-muted/40 p-3 text-sm text-muted-foreground'>
                Chế độ tự động dùng các túi đã chọn, thời gian dự kiến để chọn
                tuyến, xe phù hợp và tránh trùng lịch tài xế.
              </div>
            </TabsContent>

            <TabsContent value='manual' className='mt-4 space-y-4'>
              <div className='grid gap-3 md:grid-cols-2'>
                <div className='space-y-2'>
                  <Label>Tuyến *</Label>
                  <TmsCombobox
                    value={planning.routeId ? String(planning.routeId) : ''}
                    onValueChange={(value) =>
                      onUpdatePlanning('routeId', parseNumber(value))
                    }
                    options={routeOptions}
                    placeholder={
                      isFetchingRoutes ? 'Đang tải tuyến...' : 'Chọn tuyến'
                    }
                    emptyText='Không có tuyến đang hoạt động cho chặng này'
                    loading={isFetchingRoutes}
                    clearable
                  />
                </div>
                <div className='space-y-2'>
                  <Label>Xe *</Label>
                  <TmsCombobox
                    value={planning.vehicleId ? String(planning.vehicleId) : ''}
                    onValueChange={(value) =>
                      onUpdatePlanning('vehicleId', parseNumber(value))
                    }
                    options={vehicleOptions}
                    placeholder={
                      isFetchingVehicles ? 'Đang tải xe...' : 'Chọn xe'
                    }
                    emptyText='Không có xe đang hoạt động tại hub xuất phát'
                    loading={isFetchingVehicles}
                    clearable
                  />
                </div>
              </div>
            </TabsContent>
          </Tabs>

          <div className='space-y-2'>
            <Label htmlFor='distribution-note'>Ghi chú</Label>
            <Textarea
              id='distribution-note'
              value={planning.note}
              onChange={(event) => onUpdatePlanning('note', event.target.value)}
              placeholder='Chỉ dẫn thêm cho tài xế hoặc nhân sự điểm đến'
              rows={3}
            />
          </div>

          <div className='flex flex-col gap-2 sm:flex-row'>
            {planningMode === 'auto' ? (
              <>
                <Button
                  disabled={!canManage || isPlanning || selectedBagCount === 0}
                  onClick={() => onAutoPlan(false)}
                >
                  {isPlanning ? (
                    <Loader2 className='h-4 w-4 animate-spin' />
                  ) : (
                    <Sparkles className='h-4 w-4' />
                  )}
                  Xem trước kế hoạch
                </Button>
                <Button
                  variant='secondary'
                  disabled={!canManage || isPlanning || selectedBagCount === 0}
                  onClick={() => onAutoPlan(true)}
                >
                  <Play className='h-4 w-4' />
                  Chạy kế hoạch tự động
                </Button>
              </>
            ) : (
              <Button
                variant='outline'
                disabled={!canManage || isCreatingManifest}
                onClick={onCreateManual}
              >
                {isCreatingManifest ? (
                  <Loader2 className='h-4 w-4 animate-spin' />
                ) : (
                  <ClipboardList className='h-4 w-4' />
                )}
                Tạo biên bản thủ công
              </Button>
            )}
          </div>
        </CardContent>
      </Card>

      <div className='min-w-0 space-y-4'>
        <Card className='min-w-0'>
          <CardHeader>
            <CardTitle>Tóm tắt kế hoạch</CardTitle>
            <CardDescription>
              Kiểm tra chặng, phương tiện và số túi đã chọn trước khi tạo biên
              bản.
            </CardDescription>
          </CardHeader>
          <CardContent className='grid gap-3 md:grid-cols-2'>
            <SummaryItem
              label='Điểm xuất phát'
              value={originHub?.code ?? '-'}
            />
            <SummaryItem
              label='Điểm đến'
              value={
                planning.destinationType === 'HUB'
                  ? (destinationHub?.code ?? '-')
                  : (destinationPostOffice?.code ?? '-')
              }
            />
            <SummaryItem label='Tuyến' value={route?.routeCode ?? '-'} />
            <SummaryItem label='Xe' value={vehicle?.licensePlate ?? '-'} />
            <SummaryItem
              label='Túi đã chọn'
              value={formatNumber(selectedBags.length, 0)}
            />
            <SummaryItem
              label='Đơn hàng đã chọn'
              value={formatNumber(selectedDestinationSummary?.totalOrders, 0)}
            />
            <SummaryItem
              label='Tổng khối lượng'
              value={`${formatNumber(selectedDestinationSummary?.totalWeight)} kg`}
            />
            <SummaryItem
              label='Tổng thể tích'
              value={`${formatNumber(selectedDestinationSummary?.totalVolume)} m3`}
            />
          </CardContent>
        </Card>

        <PlanResultCard plan={planResult} />
      </div>
    </div>
  );
}
