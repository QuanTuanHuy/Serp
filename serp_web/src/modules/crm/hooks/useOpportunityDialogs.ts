// Author: QuanTuanHuy, Description: Part of Serp Project

import { useState, useCallback } from 'react';
import type { OpportunityStage } from '../types';

export const useOpportunityDialogs = () => {
  const [isStageDialogOpen, setIsStageDialogOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [isReopenDialogOpen, setIsReopenDialogOpen] = useState(false);

  const [selectedStage, setSelectedStage] = useState<OpportunityStage | ''>('');
  const [stageNotes, setStageNotes] = useState('');
  const [lostReason, setLostReason] = useState('');
  const [wonActualValue, setWonActualValue] = useState('');
  const [reopenReason, setReopenReason] = useState('');
  const [reopenStage, setReopenStage] =
    useState<OpportunityStage>('PROSPECTING');

  const resetDialogStates = useCallback(() => {
    setSelectedStage('');
    setStageNotes('');
    setLostReason('');
    setWonActualValue('');
    setReopenReason('');
    setReopenStage('PROSPECTING');
  }, []);

  return {
    isStageDialogOpen,
    setIsStageDialogOpen,
    isDeleteDialogOpen,
    setIsDeleteDialogOpen,
    isReopenDialogOpen,
    setIsReopenDialogOpen,
    selectedStage,
    setSelectedStage,
    stageNotes,
    setStageNotes,
    lostReason,
    setLostReason,
    wonActualValue,
    setWonActualValue,
    reopenReason,
    setReopenReason,
    reopenStage,
    setReopenStage,
    resetDialogStates,
  };
};
