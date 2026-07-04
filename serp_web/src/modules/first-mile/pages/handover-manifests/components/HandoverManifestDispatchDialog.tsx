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
          <DialogTitle>Dispatch manifest to hub</DialogTitle>
          <DialogDescription>
            Dispatch is allowed only after all orders have been scanned out.
          </DialogDescription>
        </DialogHeader>

        <div className='space-y-4'>
          <div className='grid gap-3 sm:grid-cols-2'>
            <DetailItem
              label='Manifest'
              value={dispatchManifest?.manifestCode || '--'}
            />
            <DetailItem
              label='Scan progress'
              value={`${getScannedOutOrders(dispatchManifest)}/${getTotalOrders(
                dispatchManifest
              )}`}
            />
          </div>

          <div className='grid gap-4 sm:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='dispatch-route'>Route *</Label>
              <TmsCombobox
                id='dispatch-route'
                value={dispatchRouteId}
                onValueChange={onRouteChange}
                options={dispatchRouteOptions}
                placeholder={
                  isLoadingDispatchRoutes ? 'Loading routes...' : 'Select route'
                }
                emptyText='No matching hub to post office routes'
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='dispatch-vehicle'>Vehicle *</Label>
              <TmsCombobox
                id='dispatch-vehicle'
                value={dispatchVehicleId}
                onValueChange={onVehicleChange}
                options={dispatchVehicleOptions}
                placeholder={
                  isLoadingDispatchVehicles
                    ? 'Loading vehicles...'
                    : 'Select vehicle'
                }
                emptyText='No active vehicles found'
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='dispatch-departure'>Planned departure *</Label>
              <Input
                id='dispatch-departure'
                type='datetime-local'
                value={dispatchDepartureAt}
                onChange={(event) => onDepartureAtChange(event.target.value)}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='dispatch-arrival'>Planned arrival *</Label>
              <Input
                id='dispatch-arrival'
                type='datetime-local'
                value={dispatchArrivalAt}
                onChange={(event) => onArrivalAtChange(event.target.value)}
              />
            </div>
          </div>

          <div className='space-y-2'>
            <Label htmlFor='dispatch-seal'>Seal code</Label>
            <Input
              id='dispatch-seal'
              value={dispatchSealCode}
              onChange={(event) => onSealCodeChange(event.target.value)}
              placeholder='Optional seal code'
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='dispatch-note'>Dispatch note</Label>
            <Textarea
              id='dispatch-note'
              value={dispatchNote}
              onChange={(event) => onNoteChange(event.target.value)}
              placeholder='Optional dispatch note'
            />
          </div>
        </div>

        <DialogFooter>
          <Button variant='outline' onClick={() => onOpenChange(false)}>
            Cancel
          </Button>
          <Button disabled={!canSubmit} onClick={onSubmit}>
            {isDispatching ? 'Dispatching...' : 'Dispatch to hub'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
