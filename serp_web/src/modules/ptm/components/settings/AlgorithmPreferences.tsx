/**
 * PTM v2 - Algorithm Preferences Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Algorithm tuning and scheduling preferences
 */

'use client';

import { useState, useEffect } from 'react';
import {
  Sparkles,
  Zap,
  Target,
  Clock,
  Shuffle,
  TrendingUp,
  Settings2,
  Save,
  RotateCcw,
  Info,
} from 'lucide-react';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { Label } from '@/shared/components/ui/label';
import { Slider } from '@/shared/components/ui/slider';
import { Switch } from '@/shared/components/ui/switch';
import { RadioGroup, RadioGroupItem } from '@/shared/components/ui/radio-group';
import { Badge } from '@/shared/components/ui/badge';
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '@/shared/components/ui/tooltip';
import { cn } from '@/shared/utils';
import { toast } from 'sonner';
import type { AlgorithmType } from '../../types';

interface GoalWeights {
  priority: number;
  deadline: number;
  focusTime: number;
  contextSwitch: number;
}

interface AlgorithmSettings {
  defaultAlgorithm: AlgorithmType;
  goalWeights: GoalWeights;
  autoOptimize: boolean;
  autoOptimizeTriggers: {
    onTaskCreate: boolean;
    onTaskUpdate: boolean;
    onDeadlineChange: boolean;
    daily: boolean;
  };
  optimizationSchedule: {
    dailyTime: string; // "09:00" format
    enabled: boolean;
  };
  heuristicSettings: {
    maxIterations: number;
    localSearchDepth: number;
  };
  milpSettings: {
    maxSolveTime: number; // seconds
    optimalityGap: number; // percentage
  };
}

interface AlgorithmPreferencesProps {
  className?: string;
}

const DEFAULT_SETTINGS: AlgorithmSettings = {
  defaultAlgorithm: 'hybrid',
  goalWeights: {
    priority: 40,
    deadline: 30,
    focusTime: 20,
    contextSwitch: 10,
  },
  autoOptimize: true,
  autoOptimizeTriggers: {
    onTaskCreate: true,
    onTaskUpdate: true,
    onDeadlineChange: true,
    daily: true,
  },
  optimizationSchedule: {
    dailyTime: '09:00',
    enabled: true,
  },
  heuristicSettings: {
    maxIterations: 1000,
    localSearchDepth: 3,
  },
  milpSettings: {
    maxSolveTime: 5,
    optimalityGap: 5,
  },
};

const ALGORITHM_INFO: Record<
  AlgorithmType,
  { label: string; speed: string; quality: string; description: string }
> = {
  local_heuristic: {
    label: 'Fast (Heuristic)',
    speed: '~0.5s',
    quality: '80% optimal',
    description:
      'Quick greedy algorithm with local search. Best for immediate scheduling needs.',
  },
  hybrid: {
    label: 'Balanced (Hybrid)',
    speed: '~1.5s',
    quality: '90% optimal',
    description:
      'Heuristic warm-start + MILP refinement. Recommended for most cases.',
  },
  milp_optimized: {
    label: 'Optimal (MILP)',
    speed: '3-5s',
    quality: '95%+ optimal',
    description:
      'Mathematical optimization for best results. May take longer for complex schedules.',
  },
};

export function AlgorithmPreferences({ className }: AlgorithmPreferencesProps) {
  const [settings, setSettings] = useState<AlgorithmSettings>(DEFAULT_SETTINGS);
  const [hasChanges, setHasChanges] = useState(false);

  // Load settings from API (mock)
  useEffect(() => {
    // TODO: Load from API
    const loadSettings = async () => {
      // Mock API call
      await new Promise((resolve) => setTimeout(resolve, 300));
      setSettings(DEFAULT_SETTINGS);
    };
    loadSettings();
  }, []);

  const updateGoalWeight = (goal: keyof GoalWeights, value: number) => {
    setSettings((prev) => ({
      ...prev,
      goalWeights: { ...prev.goalWeights, [goal]: value },
    }));
    setHasChanges(true);
  };

  const normalizeWeights = () => {
    const total = Object.values(settings.goalWeights).reduce(
      (sum, val) => sum + val,
      0
    );
    if (total === 100) return;

    const normalized = { ...settings.goalWeights };
    const scale = 100 / total;
    Object.keys(normalized).forEach((key) => {
      normalized[key as keyof GoalWeights] = Math.round(
        normalized[key as keyof GoalWeights] * scale
      );
    });

    setSettings((prev) => ({ ...prev, goalWeights: normalized }));
  };

  const handleSave = async () => {
    // Normalize weights to 100%
    normalizeWeights();

    // TODO: Save to API
    await new Promise((resolve) => setTimeout(resolve, 500));

    setHasChanges(false);
    toast.success('Algorithm preferences saved!', {
      description: 'Your scheduling will use the new settings.',
    });
  };

  const handleReset = () => {
    setSettings(DEFAULT_SETTINGS);
    setHasChanges(true);
    toast.info('Settings reset to defaults');
  };

  const totalWeight = Object.values(settings.goalWeights).reduce(
    (sum, val) => sum + val,
    0
  );

  return (
    <div className={cn('space-y-6', className)}>
      {/* Header */}
      <div className='flex items-start justify-between'>
        <div>
          <h2 className='text-2xl font-bold flex items-center gap-2'>
            <Settings2 className='h-6 w-6 text-purple-600' />
            Algorithm Preferences
          </h2>
          <p className='text-muted-foreground mt-1'>
            Fine-tune how AI schedules your tasks
          </p>
        </div>
        <div className='flex gap-2'>
          <Button variant='outline' size='sm' onClick={handleReset}>
            <RotateCcw className='h-4 w-4 mr-1' />
            Reset
          </Button>
          {hasChanges && (
            <Button onClick={handleSave} size='sm'>
              <Save className='h-4 w-4 mr-1' />
              Save Changes
            </Button>
          )}
        </div>
      </div>

      {/* Default Algorithm */}
      <Card>
        <CardHeader>
          <CardTitle className='flex items-center gap-2'>
            <Zap className='h-5 w-5 text-amber-600' />
            Default Algorithm
          </CardTitle>
          <CardDescription>
            Choose the algorithm for automatic scheduling
          </CardDescription>
        </CardHeader>
        <CardContent className='space-y-4'>
          <RadioGroup
            value={settings.defaultAlgorithm}
            onValueChange={(value: AlgorithmType) => {
              setSettings((prev) => ({ ...prev, defaultAlgorithm: value }));
              setHasChanges(true);
            }}
          >
            {(Object.keys(ALGORITHM_INFO) as AlgorithmType[]).map((type) => {
              const info = ALGORITHM_INFO[type];
              return (
                <div
                  key={type}
                  className={cn(
                    'flex items-start space-x-3 p-4 rounded-lg border transition-all',
                    settings.defaultAlgorithm === type
                      ? 'border-primary bg-primary/5'
                      : 'border-border hover:border-primary/50'
                  )}
                >
                  <RadioGroupItem value={type} id={type} className='mt-1' />
                  <div className='flex-1'>
                    <Label
                      htmlFor={type}
                      className='font-semibold cursor-pointer flex items-center gap-2'
                    >
                      {info.label}
                      {type === 'hybrid' && (
                        <Badge variant='secondary' className='text-xs'>
                          Recommended
                        </Badge>
                      )}
                    </Label>
                    <p className='text-sm text-muted-foreground mt-1'>
                      {info.description}
                    </p>
                    <div className='flex gap-4 mt-2 text-xs'>
                      <div className='flex items-center gap-1'>
                        <Clock className='h-3 w-3 text-muted-foreground' />
                        <span className='text-muted-foreground'>
                          Speed: {info.speed}
                        </span>
                      </div>
                      <div className='flex items-center gap-1'>
                        <TrendingUp className='h-3 w-3 text-muted-foreground' />
                        <span className='text-muted-foreground'>
                          Quality: {info.quality}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              );
            })}
          </RadioGroup>
        </CardContent>
      </Card>

      {/* Goal Weights */}
      <Card>
        <CardHeader>
          <div className='flex items-center justify-between'>
            <div>
              <CardTitle className='flex items-center gap-2'>
                <Target className='h-5 w-5 text-blue-600' />
                Optimization Goals
              </CardTitle>
              <CardDescription>
                Adjust importance of each factor (must total 100%)
              </CardDescription>
            </div>
            <TooltipProvider>
              <Tooltip>
                <TooltipTrigger asChild>
                  <Badge
                    variant={totalWeight === 100 ? 'default' : 'destructive'}
                  >
                    Total: {totalWeight}%
                  </Badge>
                </TooltipTrigger>
                <TooltipContent>
                  {totalWeight === 100
                    ? 'Weights are balanced'
                    : `Adjust to total 100% (currently ${totalWeight}%)`}
                </TooltipContent>
              </Tooltip>
            </TooltipProvider>
          </div>
        </CardHeader>
        <CardContent className='space-y-6'>
          {/* Priority Weight */}
          <div className='space-y-3'>
            <div className='flex items-center justify-between'>
              <div className='flex items-center gap-2'>
                <Label className='text-sm font-medium'>Priority Weight</Label>
                <TooltipProvider>
                  <Tooltip>
                    <TooltipTrigger>
                      <Info className='h-4 w-4 text-muted-foreground' />
                    </TooltipTrigger>
                    <TooltipContent>
                      <p className='max-w-xs'>
                        How much to favor high-priority tasks over low-priority
                        ones
                      </p>
                    </TooltipContent>
                  </Tooltip>
                </TooltipProvider>
              </div>
              <span className='text-sm font-bold text-red-600'>
                {settings.goalWeights.priority}%
              </span>
            </div>
            <Slider
              value={[settings.goalWeights.priority]}
              min={0}
              max={100}
              step={5}
              onValueChange={([value]) => updateGoalWeight('priority', value)}
              className='[&_[role=slider]]:bg-red-600 [&_[role=slider]]:border-red-600'
            />
            <p className='text-xs text-muted-foreground'>
              Higher = Schedule high-priority tasks in best time slots
            </p>
          </div>

          {/* Deadline Weight */}
          <div className='space-y-3'>
            <div className='flex items-center justify-between'>
              <div className='flex items-center gap-2'>
                <Label className='text-sm font-medium'>Deadline Urgency</Label>
                <TooltipProvider>
                  <Tooltip>
                    <TooltipTrigger>
                      <Info className='h-4 w-4 text-muted-foreground' />
                    </TooltipTrigger>
                    <TooltipContent>
                      <p className='max-w-xs'>
                        How urgently to schedule tasks approaching deadlines
                      </p>
                    </TooltipContent>
                  </Tooltip>
                </TooltipProvider>
              </div>
              <span className='text-sm font-bold text-orange-600'>
                {settings.goalWeights.deadline}%
              </span>
            </div>
            <Slider
              value={[settings.goalWeights.deadline]}
              min={0}
              max={100}
              step={5}
              onValueChange={([value]) => updateGoalWeight('deadline', value)}
              className='[&_[role=slider]]:bg-orange-600 [&_[role=slider]]:border-orange-600'
            />
            <p className='text-xs text-muted-foreground'>
              Higher = Prioritize tasks with approaching deadlines
            </p>
          </div>

          {/* Focus Time Weight */}
          <div className='space-y-3'>
            <div className='flex items-center justify-between'>
              <div className='flex items-center gap-2'>
                <Label className='text-sm font-medium'>Focus Time Match</Label>
                <TooltipProvider>
                  <Tooltip>
                    <TooltipTrigger>
                      <Info className='h-4 w-4 text-muted-foreground' />
                    </TooltipTrigger>
                    <TooltipContent>
                      <p className='max-w-xs'>
                        How much to prefer scheduling deep work during focus
                        blocks
                      </p>
                    </TooltipContent>
                  </Tooltip>
                </TooltipProvider>
              </div>
              <span className='text-sm font-bold text-purple-600'>
                {settings.goalWeights.focusTime}%
              </span>
            </div>
            <Slider
              value={[settings.goalWeights.focusTime]}
              min={0}
              max={100}
              step={5}
              onValueChange={([value]) => updateGoalWeight('focusTime', value)}
              className='[&_[role=slider]]:bg-purple-600 [&_[role=slider]]:border-purple-600'
            />
            <p className='text-xs text-muted-foreground'>
              Higher = Strictly match deep work to focus time blocks
            </p>
          </div>

          {/* Context Switch Penalty */}
          <div className='space-y-3'>
            <div className='flex items-center justify-between'>
              <div className='flex items-center gap-2'>
                <Label className='text-sm font-medium'>
                  Context Switch Penalty
                </Label>
                <TooltipProvider>
                  <Tooltip>
                    <TooltipTrigger>
                      <Info className='h-4 w-4 text-muted-foreground' />
                    </TooltipTrigger>
                    <TooltipContent>
                      <p className='max-w-xs'>
                        Penalty for scheduling different task types back-to-back
                      </p>
                    </TooltipContent>
                  </Tooltip>
                </TooltipProvider>
              </div>
              <span className='text-sm font-bold text-gray-600'>
                {settings.goalWeights.contextSwitch}%
              </span>
            </div>
            <Slider
              value={[settings.goalWeights.contextSwitch]}
              min={0}
              max={100}
              step={5}
              onValueChange={([value]) =>
                updateGoalWeight('contextSwitch', value)
              }
              className='[&_[role=slider]]:bg-gray-600 [&_[role=slider]]:border-gray-600'
            />
            <p className='text-xs text-muted-foreground'>
              Higher = Group similar tasks together to reduce switching
            </p>
          </div>

          {/* Normalize Button */}
          {totalWeight !== 100 && (
            <div className='pt-4 border-t'>
              <Button
                variant='outline'
                size='sm'
                onClick={normalizeWeights}
                className='w-full'
              >
                <Shuffle className='h-4 w-4 mr-2' />
                Auto-balance to 100%
              </Button>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Auto-Optimization */}
      <Card>
        <CardHeader>
          <CardTitle className='flex items-center gap-2'>
            <Sparkles className='h-5 w-5 text-purple-600' />
            Auto-Optimization
          </CardTitle>
          <CardDescription>
            Automatically re-optimize schedule when changes occur
          </CardDescription>
        </CardHeader>
        <CardContent className='space-y-4'>
          {/* Master Toggle */}
          <div className='flex items-center justify-between p-4 rounded-lg border bg-muted/50'>
            <div>
              <Label className='font-semibold'>Enable Auto-Optimization</Label>
              <p className='text-sm text-muted-foreground mt-1'>
                AI will automatically reschedule when tasks change
              </p>
            </div>
            <Switch
              checked={settings.autoOptimize}
              onCheckedChange={(checked) => {
                setSettings((prev) => ({ ...prev, autoOptimize: checked }));
                setHasChanges(true);
              }}
            />
          </div>

          {/* Trigger Settings */}
          {settings.autoOptimize && (
            <div className='space-y-3 pl-4 border-l-2'>
              <Label className='text-sm font-medium'>Trigger on:</Label>

              <div className='space-y-2'>
                <div className='flex items-center justify-between'>
                  <Label className='text-sm font-normal cursor-pointer'>
                    Task created
                  </Label>
                  <Switch
                    checked={settings.autoOptimizeTriggers.onTaskCreate}
                    onCheckedChange={(checked) => {
                      setSettings((prev) => ({
                        ...prev,
                        autoOptimizeTriggers: {
                          ...prev.autoOptimizeTriggers,
                          onTaskCreate: checked,
                        },
                      }));
                      setHasChanges(true);
                    }}
                  />
                </div>

                <div className='flex items-center justify-between'>
                  <Label className='text-sm font-normal cursor-pointer'>
                    Task updated (status, priority, duration)
                  </Label>
                  <Switch
                    checked={settings.autoOptimizeTriggers.onTaskUpdate}
                    onCheckedChange={(checked) => {
                      setSettings((prev) => ({
                        ...prev,
                        autoOptimizeTriggers: {
                          ...prev.autoOptimizeTriggers,
                          onTaskUpdate: checked,
                        },
                      }));
                      setHasChanges(true);
                    }}
                  />
                </div>

                <div className='flex items-center justify-between'>
                  <Label className='text-sm font-normal cursor-pointer'>
                    Deadline changed
                  </Label>
                  <Switch
                    checked={settings.autoOptimizeTriggers.onDeadlineChange}
                    onCheckedChange={(checked) => {
                      setSettings((prev) => ({
                        ...prev,
                        autoOptimizeTriggers: {
                          ...prev.autoOptimizeTriggers,
                          onDeadlineChange: checked,
                        },
                      }));
                      setHasChanges(true);
                    }}
                  />
                </div>

                <div className='flex items-center justify-between'>
                  <Label className='text-sm font-normal cursor-pointer'>
                    Daily (scheduled time)
                  </Label>
                  <Switch
                    checked={settings.autoOptimizeTriggers.daily}
                    onCheckedChange={(checked) => {
                      setSettings((prev) => ({
                        ...prev,
                        autoOptimizeTriggers: {
                          ...prev.autoOptimizeTriggers,
                          daily: checked,
                        },
                      }));
                      setHasChanges(true);
                    }}
                  />
                </div>
              </div>

              {/* Daily Schedule Time */}
              {settings.autoOptimizeTriggers.daily && (
                <div className='pt-3 border-t'>
                  <Label className='text-sm'>Daily optimization time:</Label>
                  <input
                    type='time'
                    value={settings.optimizationSchedule.dailyTime}
                    onChange={(e) => {
                      setSettings((prev) => ({
                        ...prev,
                        optimizationSchedule: {
                          ...prev.optimizationSchedule,
                          dailyTime: e.target.value,
                        },
                      }));
                      setHasChanges(true);
                    }}
                    className='mt-2 flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm'
                  />
                  <p className='text-xs text-muted-foreground mt-1'>
                    Schedule will be optimized automatically at this time each
                    day
                  </p>
                </div>
              )}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Advanced Settings */}
      <Card>
        <CardHeader>
          <CardTitle className='flex items-center gap-2 text-base'>
            <Settings2 className='h-4 w-4 text-muted-foreground' />
            Advanced Algorithm Settings
          </CardTitle>
          <CardDescription className='text-xs'>
            Fine-tune algorithm performance (for advanced users)
          </CardDescription>
        </CardHeader>
        <CardContent className='space-y-6'>
          {/* Heuristic Settings */}
          <div className='space-y-4'>
            <Label className='text-sm font-semibold'>Heuristic Algorithm</Label>

            <div className='space-y-3 pl-4 border-l-2'>
              <div className='space-y-2'>
                <div className='flex items-center justify-between'>
                  <Label className='text-sm'>Max Iterations</Label>
                  <span className='text-sm font-medium'>
                    {settings.heuristicSettings.maxIterations}
                  </span>
                </div>
                <Slider
                  value={[settings.heuristicSettings.maxIterations]}
                  min={100}
                  max={5000}
                  step={100}
                  onValueChange={([value]) => {
                    setSettings((prev) => ({
                      ...prev,
                      heuristicSettings: {
                        ...prev.heuristicSettings,
                        maxIterations: value,
                      },
                    }));
                    setHasChanges(true);
                  }}
                />
                <p className='text-xs text-muted-foreground'>
                  More iterations = better quality but slower
                </p>
              </div>

              <div className='space-y-2'>
                <div className='flex items-center justify-between'>
                  <Label className='text-sm'>Local Search Depth</Label>
                  <span className='text-sm font-medium'>
                    {settings.heuristicSettings.localSearchDepth}
                  </span>
                </div>
                <Slider
                  value={[settings.heuristicSettings.localSearchDepth]}
                  min={1}
                  max={10}
                  step={1}
                  onValueChange={([value]) => {
                    setSettings((prev) => ({
                      ...prev,
                      heuristicSettings: {
                        ...prev.heuristicSettings,
                        localSearchDepth: value,
                      },
                    }));
                    setHasChanges(true);
                  }}
                />
                <p className='text-xs text-muted-foreground'>
                  Deeper search = more thorough local optimization
                </p>
              </div>
            </div>
          </div>

          {/* MILP Settings */}
          <div className='space-y-4'>
            <Label className='text-sm font-semibold'>MILP Optimizer</Label>

            <div className='space-y-3 pl-4 border-l-2'>
              <div className='space-y-2'>
                <div className='flex items-center justify-between'>
                  <Label className='text-sm'>Max Solve Time (seconds)</Label>
                  <span className='text-sm font-medium'>
                    {settings.milpSettings.maxSolveTime}s
                  </span>
                </div>
                <Slider
                  value={[settings.milpSettings.maxSolveTime]}
                  min={1}
                  max={30}
                  step={1}
                  onValueChange={([value]) => {
                    setSettings((prev) => ({
                      ...prev,
                      milpSettings: {
                        ...prev.milpSettings,
                        maxSolveTime: value,
                      },
                    }));
                    setHasChanges(true);
                  }}
                />
                <p className='text-xs text-muted-foreground'>
                  Longer time allows finding better solutions
                </p>
              </div>

              <div className='space-y-2'>
                <div className='flex items-center justify-between'>
                  <Label className='text-sm'>Optimality Gap (%)</Label>
                  <span className='text-sm font-medium'>
                    {settings.milpSettings.optimalityGap}%
                  </span>
                </div>
                <Slider
                  value={[settings.milpSettings.optimalityGap]}
                  min={0}
                  max={20}
                  step={1}
                  onValueChange={([value]) => {
                    setSettings((prev) => ({
                      ...prev,
                      milpSettings: {
                        ...prev.milpSettings,
                        optimalityGap: value,
                      },
                    }));
                    setHasChanges(true);
                  }}
                />
                <p className='text-xs text-muted-foreground'>
                  Lower gap = closer to optimal (but slower)
                </p>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Save Actions */}
      {hasChanges && (
        <div className='flex justify-end gap-2 pt-4 border-t'>
          <Button
            variant='outline'
            onClick={() => {
              setSettings(DEFAULT_SETTINGS);
              setHasChanges(false);
              toast.info('Changes discarded');
            }}
          >
            Discard Changes
          </Button>
          <Button onClick={handleSave}>
            <Save className='h-4 w-4 mr-2' />
            Save Preferences
          </Button>
        </div>
      )}
    </div>
  );
}
