// Author: QuanTuanHuy, Description: Part of Serp Project

import { useState, useCallback } from 'react';
import type { OpportunityStage } from '../types';

export const useOpportunityDialogs = () => {
  const [isStageDialogOpen, setIsStageDialogOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [isLostDialogOpen, setIsLostDialogOpen] = useState(false);
  const [isWonDialogOpen, setIsWonDialogOpen] = useState(false);
  const [isReopenDialogOpen, setIsReopenDialogOpen] = useState(false);

  const [selectedStage, setSelectedStage] = useState<OpportunityStage | ''>('');
  const [stageNotes, setStageNotes] = useState('');
  const [lostReason, setLostReason] = useState('');
  const [wonActualValue, setWonActualValue] = useState('');
  const [wonNotes, setWonNotes] = useState('');
  const [reopenReason, setReopenReason] = useState('');
  const [reopenStage, setReopenStage] =
    useState<OpportunityStage>('PROSPECTING');

  const resetDialogStates = useCallback(() => {
    setSelectedStage('');
    setStageNotes('');
    setLostReason('');
    setWonActualValue('');
    setWonNotes('');
    setReopenReason('');
    setReopenStage('PROSPECTING');
  }, []);

  return {
    isStageDialogOpen,
    setIsStageDialogOpen,
    isDeleteDialogOpen,
    setIsDeleteDialogOpen,
    isLostDialogOpen,
    setIsLostDialogOpen,
    isWonDialogOpen,
    setIsWonDialogOpen,
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
    wonNotes,
    setWonNotes,
    reopenReason,
    setReopenReason,
    reopenStage,
    setReopenStage,
    resetDialogStates,
  };
};
