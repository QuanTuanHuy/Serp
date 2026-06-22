'use client';

import { ExternalLink } from 'lucide-react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui';
import type { TransportPlanStopDetail } from '../../../types';

interface Props {
  stop: TransportPlanStopDetail | null;
  onClose: () => void;
}

export function EvidenceViewDialog({ stop, onClose }: Props) {
  return (
    <Dialog open={!!stop} onOpenChange={(open) => { if (!open) onClose(); }}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle>Evidence — {stop?.locationCode}</DialogTitle>
        </DialogHeader>
        <div className="space-y-3">
          {stop?.evidenceUrl ? (
            <>
              <img
                src={stop.evidenceUrl}
                alt={`Evidence for ${stop.locationCode}`}
                className="w-full rounded-lg object-contain max-h-96"
              />
              <a
                href={stop.evidenceUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="flex items-center gap-1 text-xs text-blue-600 hover:underline"
              >
                <ExternalLink className="h-3 w-3" />
                Open original
              </a>
            </>
          ) : (
            <p className="py-8 text-center text-sm text-muted-foreground">
              No evidence available
            </p>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
}
