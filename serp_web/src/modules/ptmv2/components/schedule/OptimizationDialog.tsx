/**
 * PTM v2 - Optimization Dialog Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - AI schedule optimization configuration
 */

'use client';

import { useState } from 'react';
import { Sparkles, TrendingUp, Calendar, Zap, Settings2 } from 'lucide-react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/shared/components/ui/dialog';
import { Button } from '@/shared/components/ui/button';
import { Label } from '@/shared/components/ui/label';
import { Slider } from '@/shared/components/ui/slider';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { Checkbox } from '@/shared/components/ui/checkbox';
import { Badge } from '@/shared/components/ui/badge';
import { Separator } from '@/shared/components/ui/separator';
import { cn } from '@/shared/utils';
import type { AlgorithmType } from '../../types';

interface OptimizationDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onOptimize: (config: OptimizationConfig) => void;
  isOptimizing?: boolean;
}

export interface OptimizationConfig {
  algorithmType: AlgorithmType;
  dateRange: { startDateMs: number; endDateMs: number };
  goals: {
    priority: number;
    deadline: number;
    focusTime: number;
    contextSwitch: number;
  };
  constraints: {
    respectFocusBlocks: boolean;
    noTasksBeforeHour: number;
    maxHoursPerDay: number;
    allowWeekends: boolean;
  };
}

export function OptimizationDialog({
  open,
  onOpenChange,
  onOptimize,
  isOptimizing = false,
}: OptimizationDialogProps) {
  const [algorithmType, setAlgorithmType] =
    useState<AlgorithmType>('local_heuristic');
  const [goals, setGoals] = useState({
    priority: 90,
    deadline: 70,
    focusTime: 80,
    contextSwitch: 60,
  });
  const [constraints, setConstraints] = useState({
    respectFocusBlocks: true,
    noTasksBeforeHour: 8,
    maxHoursPerDay: 8,
    allowWeekends: false,
  });

  const handleOptimize = () => {
    const now = new Date();
    const startOfWeek = new Date(now);
    startOfWeek.setDate(now.getDate() - now.getDay());
    startOfWeek.setHours(0, 0, 0, 0);

    const endOfWeek = new Date(startOfWeek);
    endOfWeek.setDate(startOfWeek.getDate() + 7);

    onOptimize({
      algorithmType,
      dateRange: {
        startDateMs: startOfWeek.getTime(),
        endDateMs: endOfWeek.getTime(),
      },
      goals,
      constraints,
    });
  };

  const getAlgorithmDescription = (type: AlgorithmType) => {
    switch (type) {
      case 'local_heuristic':
        return 'Fast optimization using greedy heuristics. Best for quick re-scheduling.';
      case 'milp_optimized':
        return 'Advanced optimization using mathematical programming. Finds optimal schedule but takes longer.';
      case 'hybrid':
        return 'Balanced approach combining speed and quality. Recommended for most cases.';
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='!max-w-2xl max-h-[90vh] overflow-y-auto'>
        <DialogHeader>
          <DialogTitle className='flex items-center gap-2 text-2xl'>
            <div className='p-2 rounded-lg bg-gradient-to-br from-purple-500/10 to-blue-500/10'>
              <Sparkles className='h-5 w-5 text-purple-600 dark:text-purple-400' />
            </div>
            AI Schedule Optimizer
          </DialogTitle>
          <DialogDescription>
            Configure optimization parameters to find the best schedule for your
            tasks
          </DialogDescription>
        </DialogHeader>

        <div className='space-y-6 py-4'>
          {/* Algorithm Selection */}
          <div className='space-y-3'>
            <div className='flex items-center gap-2'>
              <Settings2 className='h-4 w-4 text-primary' />
              <Label className='text-base font-semibold'>Algorithm Type</Label>
            </div>
            <Select
              value={algorithmType}
              onValueChange={(value) =>
                setAlgorithmType(value as AlgorithmType)
              }
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='local_heuristic'>
                  <div className='space-y-1'>
                    <p className='font-medium'>Local Heuristic</p>
                    <p className='text-xs text-muted-foreground'>
                      Fast & Simple
                    </p>
                  </div>
                </SelectItem>
                <SelectItem value='hybrid'>
                  <div className='space-y-1'>
                    <p className='font-medium'>Hybrid</p>
                    <p className='text-xs text-muted-foreground'>
                      Balanced (Recommended)
                    </p>
                  </div>
                </SelectItem>
                <SelectItem value='milp_optimized'>
                  <div className='space-y-1'>
                    <p className='font-medium'>MILP Optimized</p>
                    <p className='text-xs text-muted-foreground'>
                      Optimal but Slower
                    </p>
                  </div>
                </SelectItem>
              </SelectContent>
            </Select>
            <p className='text-sm text-muted-foreground'>
              {getAlgorithmDescription(algorithmType)}
            </p>
          </div>

          <Separator />

          {/* Optimization Goals */}
          <div className='space-y-4'>
            <div className='flex items-center gap-2'>
              <TrendingUp className='h-4 w-4 text-primary' />
              <Label className='text-base font-semibold'>
                Optimization Goals
              </Label>
              <Badge variant='secondary' className='ml-auto'>
                Adjust weights
              </Badge>
            </div>

            {/* Priority Weight */}
            <div className='space-y-2'>
              <div className='flex items-center justify-between'>
                <Label className='text-sm font-medium'>Priority Score</Label>
                <span className='text-sm font-bold text-primary'>
                  {goals.priority}%
                </span>
              </div>
              <Slider
                value={[goals.priority]}
                onValueChange={(value) =>
                  setGoals({ ...goals, priority: value[0] })
                }
                min={0}
                max={100}
                step={5}
                className='w-full'
              />
              <p className='text-xs text-muted-foreground'>
                Prioritize high-priority tasks first
              </p>
            </div>

            {/* Deadline Weight */}
            <div className='space-y-2'>
              <div className='flex items-center justify-between'>
                <Label className='text-sm font-medium'>Deadline Urgency</Label>
                <span className='text-sm font-bold text-amber-600'>
                  {goals.deadline}%
                </span>
              </div>
              <Slider
                value={[goals.deadline]}
                onValueChange={(value) =>
                  setGoals({ ...goals, deadline: value[0] })
                }
                min={0}
                max={100}
                step={5}
                className='w-full'
              />
              <p className='text-xs text-muted-foreground'>
                Schedule tasks closer to deadlines earlier
              </p>
            </div>

            {/* Focus Time Weight */}
            <div className='space-y-2'>
              <div className='flex items-center justify-between'>
                <Label className='text-sm font-medium'>Focus Time Bonus</Label>
                <span className='text-sm font-bold text-purple-600'>
                  {goals.focusTime}%
                </span>
              </div>
              <Slider
                value={[goals.focusTime]}
                onValueChange={(value) =>
                  setGoals({ ...goals, focusTime: value[0] })
                }
                min={0}
                max={100}
                step={5}
                className='w-full'
              />
              <p className='text-xs text-muted-foreground'>
                Align deep work tasks with focus blocks
              </p>
            </div>

            {/* Context Switch Penalty */}
            <div className='space-y-2'>
              <div className='flex items-center justify-between'>
                <Label className='text-sm font-medium'>
                  Minimize Context Switching
                </Label>
                <span className='text-sm font-bold text-red-600'>
                  {goals.contextSwitch}%
                </span>
              </div>
              <Slider
                value={[goals.contextSwitch]}
                onValueChange={(value) =>
                  setGoals({ ...goals, contextSwitch: value[0] })
                }
                min={0}
                max={100}
                step={5}
                className='w-full'
              />
              <p className='text-xs text-muted-foreground'>
                Group similar tasks together
              </p>
            </div>
          </div>

          <Separator />

          {/* Constraints */}
          <div className='space-y-4'>
            <div className='flex items-center gap-2'>
              <Calendar className='h-4 w-4 text-primary' />
              <Label className='text-base font-semibold'>Constraints</Label>
            </div>

            <div className='space-y-3'>
              {/* Respect Focus Blocks */}
              <div className='flex items-start gap-3 p-3 rounded-lg border bg-muted/50'>
                <Checkbox
                  id='respect-focus'
                  checked={constraints.respectFocusBlocks}
                  onCheckedChange={(checked) =>
                    setConstraints({
                      ...constraints,
                      respectFocusBlocks: checked as boolean,
                    })
                  }
                />
                <div className='space-y-1'>
                  <Label htmlFor='respect-focus' className='cursor-pointer'>
                    Respect Focus Blocks
                  </Label>
                  <p className='text-xs text-muted-foreground'>
                    Only schedule deep work during configured focus time blocks
                  </p>
                </div>
              </div>

              {/* Allow Weekends */}
              <div className='flex items-start gap-3 p-3 rounded-lg border bg-muted/50'>
                <Checkbox
                  id='allow-weekends'
                  checked={constraints.allowWeekends}
                  onCheckedChange={(checked) =>
                    setConstraints({
                      ...constraints,
                      allowWeekends: checked as boolean,
                    })
                  }
                />
                <div className='space-y-1'>
                  <Label htmlFor='allow-weekends' className='cursor-pointer'>
                    Schedule on Weekends
                  </Label>
                  <p className='text-xs text-muted-foreground'>
                    Allow tasks to be scheduled on Saturday and Sunday
                  </p>
                </div>
              </div>

              {/* Working Hours */}
              <div className='space-y-3 p-3 rounded-lg border bg-muted/50'>
                <Label className='text-sm font-medium'>Working Hours</Label>
                <div className='grid grid-cols-2 gap-4'>
                  <div className='space-y-2'>
                    <Label className='text-xs text-muted-foreground'>
                      Start Time
                    </Label>
                    <Select
                      value={constraints.noTasksBeforeHour.toString()}
                      onValueChange={(value) =>
                        setConstraints({
                          ...constraints,
                          noTasksBeforeHour: parseInt(value),
                        })
                      }
                    >
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {Array.from({ length: 12 }, (_, i) => i + 6).map(
                          (hour) => (
                            <SelectItem key={hour} value={hour.toString()}>
                              {hour}:00 {hour < 12 ? 'AM' : 'PM'}
                            </SelectItem>
                          )
                        )}
                      </SelectContent>
                    </Select>
                  </div>

                  <div className='space-y-2'>
                    <Label className='text-xs text-muted-foreground'>
                      Max Hours/Day
                    </Label>
                    <Select
                      value={constraints.maxHoursPerDay.toString()}
                      onValueChange={(value) =>
                        setConstraints({
                          ...constraints,
                          maxHoursPerDay: parseInt(value),
                        })
                      }
                    >
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {Array.from({ length: 9 }, (_, i) => i + 4).map(
                          (hours) => (
                            <SelectItem key={hours} value={hours.toString()}>
                              {hours} hours
                            </SelectItem>
                          )
                        )}
                      </SelectContent>
                    </Select>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <DialogFooter>
          <Button variant='outline' onClick={() => onOpenChange(false)}>
            Cancel
          </Button>
          <Button
            onClick={handleOptimize}
            disabled={isOptimizing}
            className='bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-700 hover:to-blue-700'
          >
            {isOptimizing ? (
              <>
                <Zap className='h-4 w-4 mr-2 animate-pulse' />
                Optimizing...
              </>
            ) : (
              <>
                <Sparkles className='h-4 w-4 mr-2' />
                Run Optimization
              </>
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
