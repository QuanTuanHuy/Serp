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
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Textarea,
} from '@/shared/components/ui';
import { ACTIVE_PIPELINE_STAGES } from '../../utils';
import type { OpportunityStage } from '../../types';

interface ReopenOpportunityDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: () => void;
  isLoading?: boolean;
  opportunityName?: string;
  reopenStage: OpportunityStage;
  onReopenStageChange: (stage: OpportunityStage) => void;
  reopenReason: string;
  onReopenReasonChange: (value: string) => void;
}

export const ReopenOpportunityDialog: React.FC<
  ReopenOpportunityDialogProps
> = ({
  open,
  onOpenChange,
  onSubmit,
  isLoading = false,
  opportunityName,
  reopenStage,
  onReopenStageChange,
  reopenReason,
  onReopenReasonChange,
}) => {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Reopen Opportunity</DialogTitle>
          <DialogDescription>
            {opportunityName
              ? `Move ${opportunityName} back into the pipeline and explain why.`
              : 'Select the stage to move this opportunity back into and explain why.'}
          </DialogDescription>
        </DialogHeader>
        <div className='space-y-4'>
          <div className='space-y-2'>
            <Label>Target Stage</Label>
            <Select
              value={reopenStage}
              onValueChange={(value) =>
                onReopenStageChange(value as OpportunityStage)
              }
            >
              <SelectTrigger>
                <SelectValue placeholder='Select stage' />
              </SelectTrigger>
              <SelectContent>
                {ACTIVE_PIPELINE_STAGES.map((stage) => (
                  <SelectItem key={stage.stage} value={stage.stage}>
                    {stage.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className='space-y-2'>
            <Label htmlFor='reopenReason'>Reopen Reason</Label>
            <Textarea
              id='reopenReason'
              value={reopenReason}
              onChange={(e) => onReopenReasonChange(e.target.value)}
              placeholder='Required reopen reason'
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
          <Button
            onClick={onSubmit}
            disabled={!reopenReason.trim() || isLoading}
          >
            Reopen Opportunity
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
