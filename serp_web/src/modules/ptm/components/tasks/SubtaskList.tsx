/**
 * PTM v2 - Subtask List Component (Enhanced with Drag & Drop)
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Hierarchical subtask management with unlimited nesting and drag-and-drop
 */

'use client';

import { useState } from 'react';
import {
  Plus,
  ChevronRight,
  MoreVertical,
  ExternalLink,
  GripVertical,
  Play,
  Pause,
  Check,
  Edit,
  Trash2,
} from 'lucide-react';
import {
  DragDropContext,
  Droppable,
  Draggable,
  DropResult,
} from '@hello-pangea/dnd';
import { Button } from '@/shared/components/ui/button';
import { Card } from '@/shared/components/ui/card';
import { Checkbox } from '@/shared/components/ui/checkbox';
import { Badge } from '@/shared/components/ui/badge';
import { Progress } from '@/shared/components/ui/progress';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/shared/components/ui/dropdown-menu';
import { cn } from '@/shared/utils';
import { toast } from 'sonner';

import {
  useGetSubtasksQuery,
  useCreateTaskMutation,
  useUpdateTaskMutation,
} from '../../api';
import { PriorityBadge, StatusBadge } from '../shared';
import { useTaskActions } from '../../hooks';
import type { Task } from '../../types';

interface SubtaskListProps {
  parentTaskId: number;
  allowNesting?: boolean; // Allow creating sub-subtasks
  showFullDetails?: boolean; // Show priority, dates, etc
  enableDragDrop?: boolean; // Enable drag and drop reordering
  onTaskClick?: (taskId: number) => void; // Open task detail
  onEditOpen?: (taskId: number) => void; // Open edit dialog
  onDeleteOpen?: (taskId: number, title: string) => void; // Open delete dialog
  className?: string;
}

export function SubtaskList({
  parentTaskId,
  allowNesting = true,
  showFullDetails = true,
  enableDragDrop = false,
  onTaskClick,
  onEditOpen,
  onDeleteOpen,
  className,
}: SubtaskListProps) {
  const [isCreating, setIsCreating] = useState(false);
  const [expandedSubtasks, setExpandedSubtasks] = useState<Set<number>>(
    new Set()
  );

  const { data: subtasks = [], isLoading } = useGetSubtasksQuery(parentTaskId);
  const [createTask] = useCreateTaskMutation();
  const [updateTask] = useUpdateTaskMutation();

  // Use shared task actions hook
  const taskActions = useTaskActions({
    onEditOpen,
    onDeleteOpen,
  });

  const completedCount = subtasks.filter((t) => t.status === 'DONE').length;
  const totalCount = subtasks.length;
  const progressPercentage =
    totalCount > 0 ? (completedCount / totalCount) * 100 : 0;

  const handleCreateSubtask = async (title: string) => {
    if (!title.trim()) return;

    try {
      await createTask({
        title: title.trim(),
        parentTaskId,
        priority: 'MEDIUM',
        estimatedDurationMin: 60,
        isDeepWork: false,
        tags: [],
      }).unwrap();
      setIsCreating(false);
      toast.success('Subtask created');
    } catch (error) {
      toast.error('Failed to create subtask');
    }
  };

  const toggleExpand = (subtaskId: number) => {
    const newExpanded = new Set(expandedSubtasks);
    if (newExpanded.has(subtaskId)) {
      newExpanded.delete(subtaskId);
    } else {
      newExpanded.add(subtaskId);
    }
    setExpandedSubtasks(newExpanded);
  };

  const handleDragEnd = async (result: DropResult) => {
    if (!result.destination || !enableDragDrop) return;

    const { source, destination } = result;

    // Same position, no change
    if (source.index === destination.index) return;

    // Reorder logic: update the order of subtasks
    // In a real implementation, you'd call an API to update sort_order
    const reordered = Array.from(subtasks);
    const [removed] = reordered.splice(source.index, 1);
    reordered.splice(destination.index, 0, removed);

    // Update sort order on backend
    try {
      // Update each task's sort order
      await Promise.all(
        reordered.map((task, index) =>
          updateTask({
            id: task.id,
            // sortOrder field would need to be added to backend
            // For now, this is a placeholder
          }).unwrap()
        )
      );
      toast.success('Order updated');
    } catch (error) {
      toast.error('Failed to reorder');
    }
  };

  if (isLoading) {
    return (
      <div className='text-sm text-muted-foreground'>Loading subtasks...</div>
    );
  }

  return (
    <div className={cn('space-y-3', className)}>
      {/* Header with Progress */}
      <div className='space-y-2'>
        <div className='flex items-center justify-between'>
          <h3 className='text-sm font-semibold text-muted-foreground'>
            Subtasks ({completedCount}/{totalCount})
          </h3>
          <Button
            variant='ghost'
            size='sm'
            onClick={() => setIsCreating(true)}
            disabled={isCreating}
          >
            <Plus className='h-4 w-4 mr-1' />
            Add Subtask
          </Button>
        </div>

        {/* Progress Bar */}
        {totalCount > 0 && (
          <div className='space-y-1'>
            <Progress value={progressPercentage} className='h-2' />
            <p className='text-xs text-muted-foreground text-right'>
              {Math.round(progressPercentage)}% complete
            </p>
          </div>
        )}
      </div>

      {/* Quick Add Input */}
      {isCreating && (
        <Card className='p-3'>
          <input
            autoFocus
            type='text'
            placeholder='Subtask title...'
            className='w-full bg-transparent border-none outline-none text-sm'
            onKeyDown={(e) => {
              if (e.key === 'Enter') {
                handleCreateSubtask(e.currentTarget.value);
              } else if (e.key === 'Escape') {
                setIsCreating(false);
              }
            }}
            onBlur={(e) => {
              if (e.target.value.trim()) {
                handleCreateSubtask(e.target.value);
              } else {
                setIsCreating(false);
              }
            }}
          />
        </Card>
      )}

      {/* Subtask List */}
      {subtasks.length === 0 && !isCreating ? (
        <Card className='p-6 text-center text-muted-foreground'>
          <p className='text-sm'>No subtasks yet</p>
          <p className='text-xs mt-1'>
            Click &quot;Add Subtask&quot; to create one
          </p>
        </Card>
      ) : enableDragDrop ? (
        <DragDropContext onDragEnd={handleDragEnd}>
          <Droppable droppableId={`subtasks-${parentTaskId}`}>
            {(provided) => (
              <div
                {...provided.droppableProps}
                ref={provided.innerRef}
                className='space-y-1'
              >
                {subtasks.map((subtask, index) => (
                  <Draggable
                    key={subtask.id}
                    draggableId={`subtask-${subtask.id}`}
                    index={index}
                  >
                    {(provided, snapshot) => (
                      <div ref={provided.innerRef} {...provided.draggableProps}>
                        <SubtaskItem
                          subtask={subtask}
                          showFullDetails={showFullDetails}
                          allowNesting={allowNesting}
                          enableDragDrop={enableDragDrop}
                          isExpanded={expandedSubtasks.has(subtask.id)}
                          isDragging={snapshot.isDragging}
                          dragHandleProps={provided.dragHandleProps}
                          taskActions={taskActions}
                          onToggleExpand={() => toggleExpand(subtask.id)}
                          onTaskClick={onTaskClick}
                          onEditOpen={onEditOpen}
                          onDeleteOpen={onDeleteOpen}
                        />
                      </div>
                    )}
                  </Draggable>
                ))}
                {provided.placeholder}
              </div>
            )}
          </Droppable>
        </DragDropContext>
      ) : (
        <div className='space-y-1'>
          {subtasks.map((subtask) => (
            <SubtaskItem
              key={subtask.id}
              subtask={subtask}
              showFullDetails={showFullDetails}
              allowNesting={allowNesting}
              enableDragDrop={false}
              isExpanded={expandedSubtasks.has(subtask.id)}
              isDragging={false}
              taskActions={taskActions}
              onToggleExpand={() => toggleExpand(subtask.id)}
              onTaskClick={onTaskClick}
              onEditOpen={onEditOpen}
              onDeleteOpen={onDeleteOpen}
            />
          ))}
        </div>
      )}
    </div>
  );
}

// Recursive subtask item component
interface SubtaskItemProps {
  subtask: Task;
  showFullDetails: boolean;
  allowNesting: boolean;
  enableDragDrop: boolean;
  isExpanded: boolean;
  isDragging: boolean;
  dragHandleProps?: any;
  taskActions: ReturnType<typeof useTaskActions>;
  onToggleExpand: () => void;
  onTaskClick?: (taskId: number) => void;
  onEditOpen?: (taskId: number) => void;
  onDeleteOpen?: (taskId: number, title: string) => void;
}

function SubtaskItem({
  subtask,
  showFullDetails,
  allowNesting,
  enableDragDrop,
  isExpanded,
  isDragging,
  dragHandleProps,
  taskActions,
  onToggleExpand,
  onTaskClick,
  onEditOpen,
  onDeleteOpen,
}: SubtaskItemProps) {
  const { data: nestedSubtasks = [] } = useGetSubtasksQuery(subtask.id, {
    skip: !allowNesting,
  });
  const hasNested = nestedSubtasks.length > 0;

  return (
    <div className='space-y-1'>
      <Card
        className={cn(
          'p-2 hover:bg-accent/50 transition-colors group',
          subtask.status === 'DONE' && 'opacity-60',
          isDragging && 'shadow-lg rotate-2'
        )}
      >
        <div className='flex items-start gap-2'>
          {/* Drag handle */}
          {enableDragDrop ? (
            <button
              {...dragHandleProps}
              className='shrink-0 w-4 h-4 flex items-center justify-center opacity-0 group-hover:opacity-100 cursor-grab active:cursor-grabbing'
              aria-label='Drag to reorder'
            >
              <GripVertical className='h-3 w-3' />
            </button>
          ) : (
            <div className='shrink-0 w-4' />
          )}

          {/* Expand button for nested subtasks */}
          {allowNesting && (
            <button
              onClick={onToggleExpand}
              className={cn(
                'shrink-0 w-4 h-4 flex items-center justify-center',
                !hasNested && 'invisible'
              )}
            >
              <ChevronRight
                className={cn(
                  'h-3 w-3 transition-transform',
                  isExpanded && 'rotate-90'
                )}
              />
            </button>
          )}

          {/* Checkbox */}
          <Checkbox
            checked={subtask.status === 'DONE'}
            onCheckedChange={() =>
              taskActions.handleToggleComplete(subtask.id, subtask.status)
            }
            className='mt-0.5'
          />

          {/* Content */}
          <div className='flex-1 min-w-0'>
            <div className='flex items-start gap-2'>
              <button
                onClick={() => onTaskClick?.(subtask.id)}
                className={cn(
                  'text-sm flex-1 text-left hover:text-primary transition-colors',
                  subtask.status === 'DONE' &&
                    'line-through text-muted-foreground'
                )}
              >
                {subtask.title}
              </button>

              {/* Actions */}
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button
                    variant='ghost'
                    size='icon'
                    className='h-6 w-6 opacity-0 group-hover:opacity-100'
                  >
                    <MoreVertical className='h-3 w-3' />
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align='end'>
                  <DropdownMenuItem
                    onClick={() => taskActions.handleOpenDetail(subtask.id)}
                  >
                    <ExternalLink className='h-3 w-3 mr-2' />
                    Open Detail
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />

                  {/* Status Actions */}
                  {subtask.status === 'TODO' && (
                    <DropdownMenuItem
                      onClick={() => taskActions.handleStart(subtask.id)}
                    >
                      <Play className='h-3 w-3 mr-2' />
                      Start
                    </DropdownMenuItem>
                  )}
                  {subtask.status === 'IN_PROGRESS' && (
                    <DropdownMenuItem
                      onClick={() => taskActions.handlePause(subtask.id)}
                    >
                      <Pause className='h-3 w-3 mr-2' />
                      Pause
                    </DropdownMenuItem>
                  )}
                  {subtask.status !== 'DONE' && (
                    <DropdownMenuItem
                      onClick={() =>
                        taskActions.handleToggleComplete(
                          subtask.id,
                          subtask.status
                        )
                      }
                    >
                      <Check className='h-3 w-3 mr-2' />
                      Mark Complete
                    </DropdownMenuItem>
                  )}

                  <DropdownMenuSeparator />

                  {/* Edit & Promote */}
                  <DropdownMenuItem
                    onClick={() => taskActions.handleEdit(subtask.id)}
                  >
                    <Edit className='h-3 w-3 mr-2' />
                    Edit
                  </DropdownMenuItem>
                  <DropdownMenuItem
                    onClick={() => taskActions.handlePromote(subtask.id)}
                  >
                    <ExternalLink className='h-3 w-3 mr-2' />
                    Convert to Task
                  </DropdownMenuItem>

                  <DropdownMenuSeparator />

                  {/* Delete */}
                  <DropdownMenuItem
                    onClick={() =>
                      taskActions.handleDelete(subtask.id, subtask.title)
                    }
                    className='text-red-600'
                  >
                    <Trash2 className='h-3 w-3 mr-2' />
                    Delete
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </div>

            {/* Metadata */}
            {showFullDetails && (
              <div className='flex items-center gap-2 mt-1 flex-wrap'>
                <PriorityBadge
                  priority={subtask.priority}
                  showLabel={false}
                  className='h-5'
                />
                <StatusBadge status={subtask.status} className='h-5 text-xs' />

                {subtask.deadlineMs && (
                  <span className='text-xs text-muted-foreground'>
                    Due: {new Date(subtask.deadlineMs).toLocaleDateString()}
                  </span>
                )}

                {subtask.estimatedDurationMin && (
                  <Badge variant='outline' className='text-xs h-5'>
                    {subtask.estimatedDurationMin >= 60
                      ? `${Math.floor(subtask.estimatedDurationMin / 60)}h ${subtask.estimatedDurationMin % 60}m`
                      : `${subtask.estimatedDurationMin}m`}
                  </Badge>
                )}

                {hasNested && (
                  <Badge variant='secondary' className='text-xs h-5'>
                    {nestedSubtasks.length} subtask
                    {nestedSubtasks.length !== 1 ? 's' : ''}
                  </Badge>
                )}
              </div>
            )}
          </div>
        </div>
      </Card>

      {/* Nested subtasks */}
      {allowNesting && isExpanded && hasNested && (
        <div className='ml-6 border-l-2 border-border pl-2'>
          <SubtaskList
            parentTaskId={subtask.id}
            allowNesting={allowNesting}
            showFullDetails={showFullDetails}
            enableDragDrop={enableDragDrop}
            onTaskClick={onTaskClick}
            onEditOpen={onEditOpen}
            onDeleteOpen={onDeleteOpen}
          />
        </div>
      )}
    </div>
  );
}
