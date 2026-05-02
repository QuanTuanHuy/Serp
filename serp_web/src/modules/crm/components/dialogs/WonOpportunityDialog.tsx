// Author: QuanTuanHuy, Description: Part of Serp Project

'use client';

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

interface WonOpportunityDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: () => void;
  isLoading?: boolean;
  opportunityName?: string;
  actualValue: string;
  onActualValueChange: (value: string) => void;
  notes: string;
  onNotesChange: (value: string) => void;
}

export const WonOpportunityDialog: React.FC<WonOpportunityDialogProps> = ({
  open,
  onOpenChange,
  onSubmit,
  isLoading = false,
  opportunityName,
  actualValue,
  onActualValueChange,
  notes,
  onNotesChange,
}) => {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>
            {opportunityName ? 'Move Opportunity to Closed Won' : 'Mark as Won'}
          </DialogTitle>
          <DialogDescription>
            {opportunityName
              ? `Confirm the final value and add optional closing notes for ${opportunityName}.`
              : 'Close this opportunity as won and optionally set the actual value.'}
          </DialogDescription>
        </DialogHeader>
        <div className='space-y-4'>
          <div className='space-y-2'>
            <Label htmlFor='wonActualValue'>Actual Value</Label>
            <Input
              id='wonActualValue'
              type='number'
              min={0}
              value={actualValue}
              onChange={(e) => onActualValueChange(e.target.value)}
              placeholder='Optional final deal value'
            />
          </div>
          <div className='space-y-2'>
            <Label htmlFor='wonNotes'>Notes</Label>
            <Textarea
              id='wonNotes'
              value={notes}
              onChange={(e) => onNotesChange(e.target.value)}
              placeholder='Optional closing notes'
            />
          </div>
        </div>
        <DialogFooter>
          <Button
            variant='outline'
            onClick={() => onOpenChange(false)}
            disabled={isLoading}
          >
            Cancel
          </Button>
          <Button onClick={onSubmit} disabled={isLoading}>
            {opportunityName ? 'Confirm Won' : 'Close as Won'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
