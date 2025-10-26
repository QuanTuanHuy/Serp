/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Plan Form Dialog Component
 */

'use client';

import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui';
import { PlanForm } from './PlanForm';
import type { SubscriptionPlan } from '../../types';

interface PlanFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  plan?: SubscriptionPlan;
  onSubmit: (
    data: Omit<
      SubscriptionPlan,
      'id' | 'createdAt' | 'updatedAt' | 'createdBy' | 'updatedBy'
    >
  ) => Promise<void>;
  isLoading?: boolean;
}

export const PlanFormDialog: React.FC<PlanFormDialogProps> = ({
  open,
  onOpenChange,
  plan,
  onSubmit,
  isLoading = false,
}) => {
  const handleSuccess = async (data: any) => {
    await onSubmit(data);
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-h-[90vh] overflow-y-auto sm:max-w-3xl md:max-w-4xl lg:max-w-5xl xl:max-w-6xl'>
        <DialogHeader>
          <DialogTitle>
            {plan ? 'Edit Subscription Plan' : 'Create New Subscription Plan'}
          </DialogTitle>
        </DialogHeader>

        <PlanForm
          plan={plan}
          onSubmit={handleSuccess}
          onCancel={() => onOpenChange(false)}
          isLoading={isLoading}
        />
      </DialogContent>
    </Dialog>
  );
};
