'use client';

import { Loader2 } from 'lucide-react';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/shared/components/ui';
import { RouteMapPanel } from '../../../components/RouteMapPanel';
import type { RoutePolylineData } from '../../../components/RouteMapPanel';

interface TransportRoutesMapModalProps {
  open: boolean;
  onClose: () => void;
  routes: RoutePolylineData[];
  loading: boolean;
  error?: string | null;
}

export function TransportRoutesMapModal({
  open,
  onClose,
  routes,
  loading,
  error,
}: TransportRoutesMapModalProps) {
  return (
    <Dialog open={open} onOpenChange={(nextOpen) => { if (!nextOpen) onClose(); }}>
      <DialogContent className='max-w-6xl gap-0 p-0'>
        <DialogHeader className='border-b px-6 pb-3 pt-4'>
          <DialogTitle>Selected Truck Routes</DialogTitle>
          <p className='text-sm text-muted-foreground'>
            View one or more executed transport plans on the map.
          </p>
        </DialogHeader>

        <div className='relative h-[640px]'>
          {loading ? (
            <div className='absolute inset-0 z-10 flex items-center justify-center bg-muted/80'>
              <div className='flex items-center gap-2 rounded-lg border bg-background px-4 py-3 shadow-sm'>
                <Loader2 className='h-5 w-5 animate-spin text-muted-foreground' />
                <span className='text-sm text-muted-foreground'>Loading selected routes…</span>
              </div>
            </div>
          ) : error ? (
            <div className='absolute inset-0 z-10 flex items-center justify-center bg-muted/80 px-6 text-center'>
              <div className='max-w-sm rounded-lg border bg-background px-4 py-3 shadow-sm'>
                <p className='text-sm font-medium text-foreground'>Failed to load selected routes.</p>
                <p className='mt-1 text-sm text-muted-foreground'>{error}</p>
              </div>
            </div>
          ) : null}

          {!loading && !error && <RouteMapPanel routes={routes} />}
        </div>
      </DialogContent>
    </Dialog>
  );
}
