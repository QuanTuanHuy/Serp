// Author: QuanTuanHuy, Description: Part of Serp Project

import { useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import { getErrorMessage } from '@/lib/store/api';
import {
  useChangeOpportunityStageMutation,
  useDeleteOpportunityMutation,
} from '../api/crmApi';
import type { ChangeOpportunityStageRequest } from '../types';

export const useOpportunityActions = (opportunityId: string) => {
  const router = useRouter();
  const [changeOpportunityStage] = useChangeOpportunityStageMutation();
  const [deleteOpportunity] = useDeleteOpportunityMutation();

  const handleStageChange = useCallback(
    async (
      payload: ChangeOpportunityStageRequest,
      successMessage: string,
      onSuccess?: () => void
    ) => {
      try {
        await changeOpportunityStage({
          id: opportunityId,
          data: payload,
        }).unwrap();
        toast.success(successMessage);
        onSuccess?.();
      } catch (error) {
        toast.error('Failed to update opportunity stage', {
          description: getErrorMessage(error),
        });
      }
    },
    [opportunityId, changeOpportunityStage]
  );

  const handleMarkAsWon = useCallback(
    async (actualValue?: string, notes?: string, onSuccess?: () => void) => {
      try {
        await changeOpportunityStage({
          id: opportunityId,
          data: {
            stage: 'CLOSED_WON',
            actualValue: actualValue ? Number(actualValue) : undefined,
            notes: notes || undefined,
          },
        }).unwrap();
        toast.success('Close opportunity as won successfully');
        onSuccess?.();
      } catch (error) {
        toast.error('Failed to close opportunity as won', {
          description: getErrorMessage(error),
        });
      }
    },
    [opportunityId, changeOpportunityStage]
  );

  const handleMarkAsLost = useCallback(
    async (lossReason: string, onSuccess?: () => void) => {
      if (!lossReason.trim()) return;

      try {
        await changeOpportunityStage({
          id: opportunityId,
          data: {
            stage: 'CLOSED_LOST',
            lossReason: lossReason.trim(),
          },
        }).unwrap();
        toast.success('Close opportunity as lost successfully');
        onSuccess?.();
      } catch (error) {
        toast.error('Failed to close opportunity as lost', {
          description: getErrorMessage(error),
        });
      }
    },
    [opportunityId, changeOpportunityStage]
  );

  const handleReopen = useCallback(
    async (
      reopenStage: string,
      reopenReason: string,
      onSuccess?: () => void
    ) => {
      if (!reopenReason.trim()) return;

      try {
        await changeOpportunityStage({
          id: opportunityId,
          data: {
            stage: reopenStage as any,
            reopenReason: reopenReason.trim(),
          },
        }).unwrap();
        toast.success('Reopen opportunity successfully');
        onSuccess?.();
      } catch (error) {
        toast.error('Failed to reopen opportunity', {
          description: getErrorMessage(error),
        });
      }
    },
    [opportunityId, changeOpportunityStage]
  );

  const handleDelete = useCallback(async () => {
    try {
      await deleteOpportunity(opportunityId).unwrap();
      toast.success('Delete opportunity successfully');
      router.push('/crm/opportunities');
    } catch (error) {
      toast.error('Failed to delete opportunity', {
        description: getErrorMessage(error),
      });
    }
  }, [opportunityId, deleteOpportunity, router]);

  return {
    handleStageChange,
    handleMarkAsWon,
    handleMarkAsLost,
    handleReopen,
    handleDelete,
  };
};
