'use client';

import { useRef, useState } from 'react';
import { Camera, CheckCircle2, Loader2, Upload, X } from 'lucide-react';
import { Button, Card, CardContent } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import type { StopAction, TransportPlanStopDetail } from '../../../../types';
import { useUploadEvidenceMutation } from '../../../../api/ttcrsApi';

const ACTION_LABELS: Record<StopAction, string> = {
  DEPOT_START: 'Depot Start',
  DEPOT_END: 'Depot End',
  PICKUP_CONTAINER: 'Pick Up Container',
  DELIVERY_CONTAINER: 'Deliver Container',
  PICKUP_TRAILER: 'Pick Up Trailer',
  DROP_TRAILER: 'Drop Trailer',
};

interface EvidencePageProps {
  stop: TransportPlanStopDetail;
  onComplete: (evidenceUrl: string | null) => void;
  isLastStop: boolean;
  isCompleting: boolean;
}

export function EvidencePage({
  stop,
  onComplete,
  isLastStop,
  isCompleting,
}: EvidencePageProps) {
  const fileRef = useRef<HTMLInputElement>(null);
  const [preview, setPreview] = useState<string | null>(null);
  const [uploadedUrl, setUploadedUrl] = useState<string | null>(null);

  const [uploadEvidence, { isLoading: isUploading }] = useUploadEvidenceMutation();

  const requiresEvidence = stop.requestId != null;

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setPreview(URL.createObjectURL(file));
    setUploadedUrl(null);

    const formData = new FormData();
    formData.append('file', file);

    try {
      const result = await uploadEvidence(formData).unwrap();
      setUploadedUrl(result.data.url);
    } catch {
      setPreview(null);
    }
  };

  const canComplete = !requiresEvidence || uploadedUrl != null;

  const handleComplete = () => {
    onComplete(uploadedUrl);
  };

  return (
    <div className="flex flex-col items-center gap-6 px-4 py-8 max-w-md mx-auto w-full">
      {/* Stop info */}
      <Card className="w-full">
        <CardContent className="p-4 text-center">
          <p className="text-xs text-muted-foreground uppercase tracking-wide">
            {ACTION_LABELS[stop.action]}
          </p>
          <p className="mt-1 text-2xl font-bold font-mono">{stop.locationCode}</p>
          {stop.requestId != null && (
            <p className="mt-1 text-sm text-muted-foreground">
              Request #{stop.requestId}
            </p>
          )}
        </CardContent>
      </Card>

      {/* Evidence upload */}
      <div className="w-full space-y-3">
        <p className="text-sm font-medium text-center">
          {requiresEvidence ? 'Upload evidence photo (required)' : 'Upload evidence photo (optional)'}
        </p>

        {/* Photo area */}
        <div
          className={cn(
            'relative w-full aspect-video rounded-xl border-2 border-dashed flex flex-col items-center justify-center cursor-pointer overflow-hidden',
            preview ? 'border-primary' : 'border-muted-foreground/30 hover:border-primary/50',
          )}
          onClick={() => fileRef.current?.click()}
        >
          {preview ? (
            <>
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img src={preview} alt="evidence" className="object-cover w-full h-full" />
              {isUploading && (
                <div className="absolute inset-0 bg-black/40 flex items-center justify-center">
                  <Loader2 className="h-8 w-8 animate-spin text-white" />
                </div>
              )}
              {uploadedUrl && (
                <div className="absolute top-2 right-2 bg-green-500 text-white rounded-full p-1">
                  <CheckCircle2 className="h-4 w-4" />
                </div>
              )}
              <button
                className="absolute top-2 left-2 bg-black/60 text-white rounded-full p-1"
                onClick={(e) => { e.stopPropagation(); setPreview(null); setUploadedUrl(null); }}
              >
                <X className="h-4 w-4" />
              </button>
            </>
          ) : (
            <div className="flex flex-col items-center gap-2 text-muted-foreground p-6">
              <Camera className="h-10 w-10" />
              <span className="text-sm">Tap to take photo or choose file</span>
            </div>
          )}
        </div>

        <input
          ref={fileRef}
          type="file"
          accept="image/*"
          capture="environment"
          className="hidden"
          onChange={handleFileChange}
        />
      </div>

      <Button
        className="w-full"
        size="lg"
        onClick={handleComplete}
        disabled={!canComplete || isUploading || isCompleting}
      >
        {isCompleting ? (
          <>
            <Loader2 className="mr-2 h-4 w-4 animate-spin" />
            {isLastStop ? 'Completing route…' : 'Completing stop…'}
          </>
        ) : (
          <>
            <Upload className="mr-2 h-4 w-4" />
            {isLastStop ? 'Complete Route' : 'Complete Stop & Continue'}
          </>
        )}
      </Button>
    </div>
  );
}
