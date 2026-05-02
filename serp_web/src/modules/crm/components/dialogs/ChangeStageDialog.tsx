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
import { PIPELINE_STAGES } from '../../utils';
import type { OpportunityStage } from '../../types';

interface ChangeStageDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: () => void;
  isLoading?: boolean;
  selectedStage: OpportunityStage | '';
  onStageChange: (stage: OpportunityStage) => void;
  stageNotes: string;
  onStageNotesChange: (value: string) => void;
  lostReason?: string;
  onLostReasonChange?: (value: string) => void;
}

export const ChangeStageDialog: React.FC<ChangeStageDialogProps> = ({
  open,
  onOpenChange,
  onSubmit,
  isLoading = false,
  selectedStage,
  onStageChange,
  stageNotes,
  onStageNotesChange,
  lostReason = '',
  onLostReasonChange,
}) => {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Change Pipeline Stage</DialogTitle>
          <DialogDescription>
            Update the current stage of this opportunity.
          </DialogDescription>
        </DialogHeader>
        <div className='space-y-4'>
          <div className='space-y-2'>
            <Label>Stage</Label>
            <Select
              value={selectedStage}
              onValueChange={(value) =>
                onStageChange(value as OpportunityStage)
              }
            >
              <SelectTrigger>
                <SelectValue placeholder='Select stage' />
              </SelectTrigger>
              <SelectContent>
                {PIPELINE_STAGES.map((stage) => (
                  <SelectItem key={stage.stage} value={stage.stage}>
                    {stage.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className='space-y-2'>
            <Label htmlFor='stageNotes'>Notes</Label>
            <Textarea
              id='stageNotes'
              value={stageNotes}
              onChange={(e) => onStageNotesChange(e.target.value)}
              placeholder='Optional stage update notes'
            />
          </div>
          {selectedStage === 'CLOSED_LOST' && onLostReasonChange && (
            <div className='space-y-2'>
              <Label htmlFor='stageLossReason'>Loss Reason</Label>
              <Textarea
                id='stageLossReason'
                value={lostReason}
                onChange={(e) => onLostReasonChange(e.target.value)}
                placeholder='Required when closing as lost'
              />
            </div>
          )}
        </div>
        <DialogFooter>
          <Button
            variant='outline'
            onClick={() => onOpenChange(false)}
            disabled={isLoading}
          >
            Cancel
          </Button>
          <Button onClick={onSubmit} disabled={!selectedStage || isLoading}>
            Update Stage
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
