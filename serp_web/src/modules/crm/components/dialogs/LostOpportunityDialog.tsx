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
  Label,
  Textarea,
} from '@/shared/components/ui';

interface LostOpportunityDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: () => void;
  isLoading?: boolean;
  opportunityName?: string;
  lostReason: string;
  onLostReasonChange: (value: string) => void;
}

export const LostOpportunityDialog: React.FC<LostOpportunityDialogProps> = ({
  open,
  onOpenChange,
  onSubmit,
  isLoading = false,
  opportunityName,
  lostReason,
  onLostReasonChange,
}) => {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>
            {opportunityName
              ? 'Move Opportunity to Closed Lost'
              : 'Mark as Lost'}
          </DialogTitle>
          <DialogDescription>
            {opportunityName
              ? `Provide the reason for closing ${opportunityName} as lost.`
              : 'Provide the reason for closing this opportunity as lost.'}
          </DialogDescription>
        </DialogHeader>
        <div className='space-y-2'>
          <Label htmlFor='lostReason'>Loss Reason</Label>
          <Textarea
            id='lostReason'
            value={lostReason}
            onChange={(e) => onLostReasonChange(e.target.value)}
            placeholder='Required loss reason'
          />
        </div>
        <DialogFooter>
          <Button
            variant='outline'
            onClick={() => onOpenChange(false)}
            disabled={isLoading}
          >
            Cancel
          </Button>
          <Button
            variant='destructive'
            onClick={onSubmit}
            disabled={!lostReason.trim() || isLoading}
          >
            {opportunityName ? 'Confirm Lost' : 'Close as Lost'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
