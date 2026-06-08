/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item resolution transition helpers
 */

'use client';

import { useEffect, useState } from 'react';

import {
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';

import type {
  PMResolutionSettingsApi,
  PMWorkItemTransitionApi,
} from '../../types/api';

const DONE_KEYS = new Set(['done', 'closed', 'resolved']);

export function transitionNeedsResolution(transition: PMWorkItemTransitionApi) {
  const categoryKey = transition.targetStatusCategory?.key?.toLowerCase();
  const categoryName = transition.targetStatusCategory?.name?.toLowerCase();
  const statusName = transition.targetStatus?.name?.toLowerCase();

  return (
    (categoryKey ? DONE_KEYS.has(categoryKey) : false) ||
    categoryName === 'done' ||
    statusName === 'done' ||
    statusName === 'closed' ||
    statusName === 'resolved'
  );
}

export function WorkItemResolutionTransitionDialog({
  open,
  transitionLabel,
  resolutions,
  isLoading,
  isSubmitting,
  onOpenChange,
  onConfirm,
}: {
  open: boolean;
  transitionLabel?: string;
  resolutions: PMResolutionSettingsApi[];
  isLoading?: boolean;
  isSubmitting?: boolean;
  onOpenChange: (open: boolean) => void;
  onConfirm: (resolutionId: number) => Promise<void>;
}) {
  const [selectedId, setSelectedId] = useState('');

  useEffect(() => {
    if (!open) {
      return;
    }
    setSelectedId(resolutions[0] ? String(resolutions[0].id) : '');
  }, [open, resolutions]);

  const handleConfirm = async () => {
    const resolutionId = Number(selectedId);
    if (!Number.isFinite(resolutionId)) {
      return;
    }
    await onConfirm(resolutionId);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Select resolution</DialogTitle>
          <DialogDescription>
            Choose the outcome to apply before moving this work item
            {transitionLabel ? ` to ${transitionLabel}` : ''}.
          </DialogDescription>
        </DialogHeader>

        <div className='space-y-2'>
          <Select
            value={selectedId}
            onValueChange={setSelectedId}
            disabled={isLoading || resolutions.length === 0}
          >
            <SelectTrigger>
              <SelectValue
                placeholder={
                  isLoading ? 'Loading resolutions' : 'Select resolution'
                }
              />
            </SelectTrigger>
            <SelectContent>
              {resolutions.map((resolution) => (
                <SelectItem key={resolution.id} value={String(resolution.id)}>
                  {resolution.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          {resolutions.length === 0 && !isLoading ? (
            <p className='text-sm text-muted-foreground'>
              No resolutions are available.
            </p>
          ) : null}
        </div>

        <DialogFooter>
          <Button
            type='button'
            variant='outline'
            onClick={() => onOpenChange(false)}
            disabled={isSubmitting}
          >
            Cancel
          </Button>
          <Button
            type='button'
            onClick={handleConfirm}
            disabled={!selectedId || isLoading || isSubmitting}
          >
            {isSubmitting ? 'Saving...' : 'Apply'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
