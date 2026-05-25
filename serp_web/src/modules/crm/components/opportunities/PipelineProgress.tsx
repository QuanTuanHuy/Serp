// Author: QuanTuanHuy, Description: Part of Serp Project

'use client';

import { Progress } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { PIPELINE_STAGES } from '../../utils';
import type { OpportunityStage } from '../../types';

interface PipelineProgressProps {
  currentStage: OpportunityStage;
  probability: number;
}

export const PipelineProgress: React.FC<PipelineProgressProps> = ({
  currentStage,
  probability,
}) => {
  const currentStageIndex = PIPELINE_STAGES.findIndex(
    (stage) => stage.stage === currentStage
  );

  return (
    <div>
      <div className='mb-4 flex items-center gap-2'>
        {PIPELINE_STAGES.filter((stage) => stage.stage !== 'CLOSED_LOST').map(
          (stage, index) => {
            const isActive = stage.stage === currentStage;
            const isPast =
              index < currentStageIndex && currentStage !== 'CLOSED_LOST';
            const isWon = currentStage === 'CLOSED_WON';

            return (
              <div key={stage.stage} className='relative flex-1'>
                <div
                  className={cn(
                    'h-2 rounded-full transition-colors',
                    isActive || isPast || isWon
                      ? stage.stage === 'CLOSED_WON' && isWon
                        ? 'bg-green-500'
                        : 'bg-primary'
                      : 'bg-muted'
                  )}
                />
                <div
                  className={cn(
                    'mt-2 text-center text-xs',
                    isActive
                      ? 'font-semibold text-foreground'
                      : 'text-muted-foreground'
                  )}
                >
                  {stage.label}
                </div>
              </div>
            );
          }
        )}
      </div>
      <Progress value={probability} className='h-2' />
    </div>
  );
};
