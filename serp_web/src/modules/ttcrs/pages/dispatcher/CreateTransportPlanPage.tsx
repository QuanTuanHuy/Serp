'use client';

import { useState } from 'react';
import { toast } from 'sonner';
import { AccessDenied } from '@/modules/account/components';
import { selectUserProfile } from '@/modules/account/store';
import { useAppSelector } from '@/shared/hooks';
import { useUpdateDispatcherRequestsStatusMutation } from '../../api/ttcrsApi';
import type { TransportPlanResult } from '../../types';
import {
  Step1SelectRequests,
  Step2PlanSetup,
  Step3ReviewAdjust,
  StepIndicator,
} from './create-transport-plan';

/**
 * Author: Tran Ngoc Hung
 * Description: Transport plan creation wizard orchestrator.
 */
export function CreateTransportPlanPage() {
  const user = useAppSelector(selectUserProfile);
  const isDispatcher = user?.roles?.includes('TTCRS_DISPATCHER') ?? false;

  const [step, setStep] = useState<1 | 2 | 3>(1);
  const [selectedRequestIds, setSelectedRequestIds] = useState<Set<number>>(new Set());
  const [planResult, setPlanResult] = useState<TransportPlanResult | null>(null);

  const [updateStatus, { isLoading: isUpdating }] =
    useUpdateDispatcherRequestsStatusMutation();

  if (!isDispatcher && user !== null) {
    return (
      <AccessDenied
        reason='authorization'
        title='Access Denied'
        description="You don't have the TTCRS_DISPATCHER role required to access this page."
        variant='minimal'
        size='md'
        showBackButton
        showHomeButton
      />
    );
  }

  const toggleRequest = (id: number) => {
    setSelectedRequestIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
  };

  const toggleAllRequests = (ids: number[]) => {
    setSelectedRequestIds((prev) => {
      const allSelected = ids.every((id) => prev.has(id));
      const next = new Set(prev);

      if (allSelected) {
        ids.forEach((id) => next.delete(id));
      } else {
        ids.forEach((id) => next.add(id));
      }

      return next;
    });
  };

  const handleNext = async () => {
    if (selectedRequestIds.size === 0) {
      return;
    }

    try {
      await updateStatus({
        requestIds: Array.from(selectedRequestIds),
        status: 'PLANNED',
      }).unwrap();
      setStep(2);
    } catch {
      toast.error('Failed to lock requests. Please try again.');
    }
  };

  const handleBackFromStep2 = async () => {
    try {
      await updateStatus({
        requestIds: Array.from(selectedRequestIds),
        status: 'PENDING',
      }).unwrap();
      setStep(1);
    } catch {
      toast.error('Failed to release request lock. Please try again.');
    }
  };

  const handlePlanCreated = (result: TransportPlanResult) => {
    setPlanResult(result);
    setStep(3);
  };

  return (
    <div className='flex min-h-screen flex-col overflow-x-hidden bg-background px-12 py-6'>
      <div className='mb-6 flex flex-col gap-1'>
        <h1 className='text-2xl font-bold tracking-tight text-foreground'>
          Create Transport Plan
        </h1>
        <p className='text-sm text-muted-foreground'>
          Select pending requests and assign resources to create truck routes for a transport plan.
        </p>
      </div>

      <StepIndicator step={step} />

      {step === 1 ? (
        <Step1SelectRequests
          selectedIds={selectedRequestIds}
          onToggle={toggleRequest}
          onToggleAll={toggleAllRequests}
          onNext={handleNext}
          isNextLoading={isUpdating}
        />
      ) : step === 2 ? (
        <Step2PlanSetup
          selectedRequestIds={selectedRequestIds}
          onBack={handleBackFromStep2}
          isBackLoading={isUpdating}
          onPlanCreated={handlePlanCreated}
        />
      ) : planResult !== null ? (
        <Step3ReviewAdjust planResult={planResult} onBack={() => setStep(2)} />
      ) : null}
    </div>
  );
}
