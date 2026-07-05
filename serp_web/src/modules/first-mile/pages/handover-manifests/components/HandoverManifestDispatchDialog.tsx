/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Dispatch handover manifest dialog
 */

import {
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Input,
  Label,
  Textarea,
} from '@/shared/components/ui';
import { TmsCombobox, type TmsComboboxOption } from '../../../components';
import type { HandoverManifest } from '../../../types';
import {
  getScannedOutOrders,
  getTotalOrders,
  isReadyForDispatch,
} from '../handoverManifestModels';
import { DetailItem } from './DetailItem';

interface HandoverManifestDispatchDialogProps {
  dispatchArrivalAt: string;
  dispatchDepartureAt: string;
  dispatchManifest: HandoverManifest | null;
  dispatchNote: string;
  dispatchRouteId: string;
  dispatchRouteOptions: TmsComboboxOption[];
  dispatchSealCode: string;
  dispatchVehicleId: string;
  dispatchVehicleOptions: TmsComboboxOption[];
  isDispatching: boolean;
  isLoadingDispatchRoutes: boolean;
  isLoadingDispatchVehicles: boolean;
  onArrivalAtChange: (value: string) => void;
  onDepartureAtChange: (value: string) => void;
  onNoteChange: (value: string) => void;
  onOpenChange: (open: boolean) => void;
  onRouteChange: (value: string) => void;
  onSealCodeChange: (value: string) => void;
  onSubmit: () => void;
  onVehicleChange: (value: string) => void;
  open: boolean;
}

export function HandoverManifestDispatchDialog({
  dispatchArrivalAt,
  dispatchDepartureAt,
  dispatchManifest,
  dispatchNote,
  dispatchRouteId,
  dispatchRouteOptions,
  dispatchSealCode,
  dispatchVehicleId,
  dispatchVehicleOptions,
  isDispatching,
  isLoadingDispatchRoutes,
  isLoadingDispatchVehicles,
  onArrivalAtChange,
  onDepartureAtChange,
  onNoteChange,
  onOpenChange,
  onRouteChange,
  onSealCodeChange,
  onSubmit,
  onVehicleChange,
  open,
}: HandoverManifestDispatchDialogProps) {
  const canSubmit =
    Boolean(dispatchManifest) &&
    Boolean(dispatchManifest && isReadyForDispatch(dispatchManifest)) &&
    Boolean(dispatchRouteId) &&
    Boolean(dispatchVehicleId) &&
    Boolean(dispatchDepartureAt) &&
    Boolean(dispatchArrivalAt) &&
    !isDispatching;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-2xl'>
        <DialogHeader>
          <DialogTitle>Xuất phiếu bàn giao đi hub</DialogTitle>
          <DialogDescription>
            Chỉ có thể xuất đi sau khi tất cả đơn hàng đã được quét xuất.
          </DialogDescription>
        </DialogHeader>

        <div className='space-y-4'>
          <div className='grid gap-3 sm:grid-cols-2'>
            <DetailItem
              label='Phiếu'
              value={dispatchManifest?.manifestCode || '--'}
            />
            <DetailItem
              label='Tiến độ quét'
              value={`${getScannedOutOrders(dispatchManifest)}/${getTotalOrders(
                dispatchManifest
              )}`}
            />
          </div>

          <div className='grid gap-4 sm:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='dispatch-route'>Tuyến *</Label>
              <TmsCombobox
                id='dispatch-route'
                value={dispatchRouteId}
                onValueChange={onRouteChange}
                options={dispatchRouteOptions}
                placeholder={
                  isLoadingDispatchRoutes ? 'Đang tải tuyến...' : 'Chọn tuyến'
                }
                emptyText='Không tìm thấy tuyến phù hợp từ bưu cục đến hub'
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='dispatch-vehicle'>Phương tiện *</Label>
              <TmsCombobox
                id='dispatch-vehicle'
                value={dispatchVehicleId}
                onValueChange={onVehicleChange}
                options={dispatchVehicleOptions}
                placeholder={
                  isLoadingDispatchVehicles
                    ? 'Đang tải phương tiện...'
                    : 'Chọn phương tiện'
                }
                emptyText='Không tìm thấy phương tiện đang hoạt động'
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='dispatch-departure'>Dự kiến đi *</Label>
              <Input
                id='dispatch-departure'
                type='datetime-local'
                value={dispatchDepartureAt}
                onChange={(event) => onDepartureAtChange(event.target.value)}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='dispatch-arrival'>Dự kiến đến *</Label>
              <Input
                id='dispatch-arrival'
                type='datetime-local'
                value={dispatchArrivalAt}
                onChange={(event) => onArrivalAtChange(event.target.value)}
              />
            </div>
          </div>

          <div className='space-y-2'>
            <Label htmlFor='dispatch-seal'>Mã seal</Label>
            <Input
              id='dispatch-seal'
              value={dispatchSealCode}
              onChange={(event) => onSealCodeChange(event.target.value)}
              placeholder='Mã seal nếu có'
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='dispatch-note'>Ghi chú xuất hàng</Label>
            <Textarea
              id='dispatch-note'
              value={dispatchNote}
              onChange={(event) => onNoteChange(event.target.value)}
              placeholder='Ghi chú xuất hàng nếu có'
            />
          </div>
        </div>

        <DialogFooter>
          <Button variant='outline' onClick={() => onOpenChange(false)}>
            Hủy
          </Button>
          <Button disabled={!canSubmit} onClick={onSubmit}>
            {isDispatching ? 'Đang xuất...' : 'Xuất đi hub'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
