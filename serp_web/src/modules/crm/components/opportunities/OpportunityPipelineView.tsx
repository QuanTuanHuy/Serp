// Author: QuanTuanHuy, Description: Part of Serp Project

'use client';

import {
  DragDropContext,
  Draggable,
  Droppable,
  type DropResult,
} from '@hello-pangea/dnd';
import { cn } from '@/shared/utils';
import { OpportunityCard } from '../cards';
import { PIPELINE_STAGES_FOR_LIST } from '../../utils';
import type { Opportunity, OpportunityStage } from '../../types';

interface OpportunityPipelineViewProps {
  opportunitiesByStage: Record<OpportunityStage, Opportunity[]>;
  stageValues: Record<OpportunityStage, number>;
  onDragEnd: (result: DropResult) => void;
  onViewOpportunity: (opportunityId: string) => void;
  onDeleteOpportunity: (opportunityId: string) => void;
}

export const OpportunityPipelineView: React.FC<
  OpportunityPipelineViewProps
> = ({
  opportunitiesByStage,
  stageValues,
  onDragEnd,
  onViewOpportunity,
  onDeleteOpportunity,
}) => {
  return (
    <DragDropContext onDragEnd={onDragEnd}>
      <div className='overflow-x-auto pb-4'>
        <div className='mb-4 flex items-center justify-between gap-3 rounded-xl border bg-muted/20 px-4 py-3'>
          <p className='text-sm font-medium'>Pipeline Board</p>
          <p className='text-xs text-muted-foreground'>
            Drag opportunities across stages. Dropping into Won, Lost, or
            reopening from Lost will ask for more details.
          </p>
        </div>
        <div className='grid min-w-[1200px] grid-cols-6 gap-4'>
          {PIPELINE_STAGES_FOR_LIST.map(({ stage, label, color }) => (
            <Droppable key={stage} droppableId={stage}>
              {(provided, snapshot) => (
                <div
                  ref={provided.innerRef}
                  {...provided.droppableProps}
                  className={cn(
                    'min-h-[500px] rounded-xl bg-muted/30 p-4 transition-colors',
                    snapshot.isDraggingOver &&
                      'bg-primary/10 ring-1 ring-primary/20'
                  )}
                >
                  <div className='mb-4'>
                    <div className='mb-2 flex items-center justify-between'>
                      <div className='flex items-center gap-2'>
                        <div className={cn('h-3 w-3 rounded-full', color)} />
                        <h3 className='text-sm font-semibold'>{label}</h3>
                      </div>
                      <span className='rounded-full bg-background px-2 py-1 text-xs text-muted-foreground'>
                        {opportunitiesByStage[stage as OpportunityStage]
                          ?.length || 0}
                      </span>
                    </div>
                    <p className='text-sm font-medium text-muted-foreground'>
                      ${stageValues[stage as OpportunityStage].toLocaleString()}
                    </p>
                  </div>

                  <div className='space-y-3'>
                    {opportunitiesByStage[stage as OpportunityStage]?.map(
                      (opportunity, index) => (
                        <Draggable
                          key={opportunity.id}
                          draggableId={`opportunity-${opportunity.id}`}
                          index={index}
                          isDragDisabled={opportunity.stage === 'CLOSED_WON'}
                        >
                          {(provided, snapshot) => (
                            <div
                              ref={provided.innerRef}
                              {...provided.draggableProps}
                              {...provided.dragHandleProps}
                              className={cn(
                                snapshot.isDragging && 'rotate-[1deg]'
                              )}
                            >
                              <OpportunityCard
                                opportunity={opportunity}
                                variant='pipeline'
                                className={cn(
                                  snapshot.isDragging &&
                                    'shadow-lg ring-2 ring-primary/20'
                                )}
                                onClick={() =>
                                  onViewOpportunity(opportunity.id)
                                }
                                onDelete={() =>
                                  onDeleteOpportunity(opportunity.id)
                                }
                              />
                            </div>
                          )}
                        </Draggable>
                      )
                    )}
                    {provided.placeholder}
                    {opportunitiesByStage[stage as OpportunityStage].length ===
                      0 && (
                      <p className='py-8 text-center text-xs text-muted-foreground'>
                        No deals
                      </p>
                    )}
                  </div>
                </div>
              )}
            </Droppable>
          ))}
        </div>
      </div>
    </DragDropContext>
  );
};
