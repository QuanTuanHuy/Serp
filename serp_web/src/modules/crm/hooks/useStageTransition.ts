// Author: QuanTuanHuy, Description: Part of Serp Project

import { useState, useCallback } from 'react';
import { toast } from 'sonner';
import { getErrorMessage } from '@/lib/store/api';
import { useChangeOpportunityStageMutation } from '../api/crmApi';
import type { OpportunityStage, ChangeOpportunityStageRequest } from '../types';

interface PendingStageTransition {
  opportunityId: string;
  opportunityName: string;
  fromStage: OpportunityStage;
  toStage: OpportunityStage;
}

export const useStageTransition = () => {
  const [pendingStageTransition, setPendingStageTransition] =
    useState<PendingStageTransition | null>(null);
  const [wonActualValue, setWonActualValue] = useState('');
  const [wonNotes, setWonNotes] = useState('');
  const [lostReason, setLostReason] = useState('');
  const [reopenReason, setReopenReason] = useState('');

  const [changeOpportunityStage, { isLoading: isUpdatingOpportunityStage }] =
    useChangeOpportunityStageMutation();

  const resetPendingStageTransition = useCallback(() => {
    setPendingStageTransition(null);
    setWonActualValue('');
    setWonNotes('');
    setLostReason('');
    setReopenReason('');
  }, []);

  const submitStageTransition = useCallback(
    async (payload: ChangeOpportunityStageRequest, successMessage: string) => {
      if (!pendingStageTransition) return;

      await changeOpportunityStage({
        id: pendingStageTransition.opportunityId,
        data: payload,
      }).unwrap();

      toast.success(successMessage);
      resetPendingStageTransition();
    },
    [
      pendingStageTransition,
      changeOpportunityStage,
      resetPendingStageTransition,
    ]
  );

  const handleSubmitWonTransition = useCallback(async () => {
    try {
      await submitStageTransition(
        {
          stage: 'CLOSED_WON',
          actualValue: wonActualValue ? Number(wonActualValue) : undefined,
          notes: wonNotes.trim() || undefined,
        },
        'Opportunity moved to Closed Won'
      );
    } catch (error) {
      toast.error('Failed to close opportunity as won', {
        description: getErrorMessage(error),
      });
    }
  }, [submitStageTransition, wonActualValue, wonNotes]);

  const handleSubmitLostTransition = useCallback(async () => {
    if (!lostReason.trim()) return;

    try {
      await submitStageTransition(
        {
          stage: 'CLOSED_LOST',
          lossReason: lostReason.trim(),
        },
        'Opportunity moved to Closed Lost'
      );
    } catch (error) {
      toast.error('Failed to close opportunity as lost', {
        description: getErrorMessage(error),
      });
    }
  }, [submitStageTransition, lostReason]);

  const handleSubmitReopenTransition = useCallback(async () => {
    if (!pendingStageTransition || !reopenReason.trim()) return;

    try {
      await submitStageTransition(
        {
          stage: pendingStageTransition.toStage,
          reopenReason: reopenReason.trim(),
        },
        `Opportunity reopened to ${pendingStageTransition.toStage.replace('_', ' ')}`
      );
    } catch (error) {
      toast.error('Failed to reopen opportunity', {
        description: getErrorMessage(error),
      });
    }
  }, [submitStageTransition, pendingStageTransition, reopenReason]);

  const isWonDialogOpen = pendingStageTransition?.toStage === 'CLOSED_WON';
  const isLostDialogOpen = pendingStageTransition?.toStage === 'CLOSED_LOST';
  const isReopenDialogOpen =
    pendingStageTransition?.fromStage === 'CLOSED_LOST' &&
    pendingStageTransition?.toStage !== 'CLOSED_WON' &&
    pendingStageTransition?.toStage !== 'CLOSED_LOST';

  return {
    pendingStageTransition,
    setPendingStageTransition,
    wonActualValue,
    setWonActualValue,
    wonNotes,
    setWonNotes,
    lostReason,
    setLostReason,
    reopenReason,
    setReopenReason,
    isUpdatingOpportunityStage,
    resetPendingStageTransition,
    handleSubmitWonTransition,
    handleSubmitLostTransition,
    handleSubmitReopenTransition,
    isWonDialogOpen,
    isLostDialogOpen,
    isReopenDialogOpen,
  };
};
