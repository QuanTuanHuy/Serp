/**
 * PTM v2 - Buffer Settings Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Task buffer and context switch protection
 */

'use client';

import { useState, useEffect } from 'react';
import {
  Clock,
  Shield,
  Zap,
  AlertTriangle,
  Save,
  RotateCcw,
  Info,
  TrendingUp,
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
import { Badge } from '@/shared/components/ui/badge';
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '@/shared/components/ui/tooltip';
import { cn } from '@/shared/utils';
import { toast } from 'sonner';

interface BufferSettings {
  defaultBufferMinutes: number;
  contextSwitchPenalty: number; // percentage (0-50%)
  applyBufferBetweenProjects: boolean;
  applyBufferBetweenTaskTypes: boolean;
  applyBufferAfterMeetings: boolean;
  buffersByTaskType: {
    deepWork: number;
    meetings: number;
    quick: number;
    review: number;
  };
  contextSwitchSettings: {
    projectSwitch: number; // minutes penalty
    taskTypeSwitch: number;
    meetingToDeepWork: number;
  };
  visualization: {
    showBuffersInCalendar: boolean;
    highlightContextSwitches: boolean;
  };
}

interface BufferSettingsProps {
  className?: string;
}

const DEFAULT_SETTINGS: BufferSettings = {
  defaultBufferMinutes: 10,
  contextSwitchPenalty: 20,
  applyBufferBetweenProjects: true,
  applyBufferBetweenTaskTypes: true,
  applyBufferAfterMeetings: true,
  buffersByTaskType: {
    deepWork: 15,
    meetings: 5,
    quick: 0,
    review: 10,
  },
  contextSwitchSettings: {
    projectSwitch: 10,
    taskTypeSwitch: 5,
    meetingToDeepWork: 15,
  },
  visualization: {
    showBuffersInCalendar: true,
    highlightContextSwitches: true,
  },
};

const TASK_TYPE_INFO = [
  {
    key: 'deepWork' as const,
    label: 'Deep Work',
    description: 'Complex focused tasks requiring high concentration',
    color: 'purple',
    icon: '🧠',
  },
  {
    key: 'meetings' as const,
    label: 'Meetings',
    description: 'Collaborative sessions and discussions',
    color: 'blue',
    icon: '👥',
  },
  {
    key: 'quick' as const,
    label: 'Quick Tasks',
    description: 'Simple tasks under 30 minutes',
    color: 'green',
    icon: '⚡',
  },
  {
    key: 'review' as const,
    label: 'Review Tasks',
    description: 'Code review, document review, etc.',
    color: 'amber',
    icon: '📝',
  },
];

export function BufferSettings({ className }: BufferSettingsProps) {
  const [settings, setSettings] = useState<BufferSettings>(DEFAULT_SETTINGS);
  const [hasChanges, setHasChanges] = useState(false);

  // Load settings from API (mock)
  useEffect(() => {
    const loadSettings = async () => {
      await new Promise((resolve) => setTimeout(resolve, 300));
      setSettings(DEFAULT_SETTINGS);
    };
    loadSettings();
  }, []);

  const handleSave = async () => {
    // TODO: Save to API
    await new Promise((resolve) => setTimeout(resolve, 500));

    setHasChanges(false);
    toast.success('Buffer settings saved!', {
      description: 'Task scheduling will respect your buffer preferences.',
    });
  };

  const handleReset = () => {
    setSettings(DEFAULT_SETTINGS);
    setHasChanges(true);
    toast.info('Settings reset to defaults');
  };

  const getBufferImpact = () => {
    const avgBuffer = settings.defaultBufferMinutes;
    const tasksPerDay = 8;
    const totalBufferMinutes = avgBuffer * tasksPerDay;
    const protectedHours = Math.round((totalBufferMinutes / 60) * 10) / 10;

    return {
      totalBufferMinutes,
      protectedHours,
      productivity: settings.contextSwitchPenalty > 15 ? 'High' : 'Medium',
    };
  };

  const impact = getBufferImpact();

  return (
    <div className={cn('space-y-6', className)}>
      {/* Header */}
      <div className='flex items-start justify-between'>
        <div>
          <h2 className='text-2xl font-bold flex items-center gap-2'>
            <Shield className='h-6 w-6 text-blue-600' />
            Buffer & Context Switch Protection
          </h2>
          <p className='text-muted-foreground mt-1'>
            Configure time buffers to prevent cognitive overload
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

      {/* Impact Summary */}
      <Card className='border-blue-200 bg-blue-50/50 dark:bg-blue-950/20'>
        <CardContent className='pt-6'>
          <div className='grid grid-cols-3 gap-4 text-center'>
            <div>
              <div className='text-2xl font-bold text-blue-600'>
                {impact.totalBufferMinutes}min
              </div>
              <div className='text-xs text-muted-foreground mt-1'>
                Daily Buffer Time
              </div>
            </div>
            <div>
              <div className='text-2xl font-bold text-green-600'>
                {impact.protectedHours}h
              </div>
              <div className='text-xs text-muted-foreground mt-1'>
                Protected Hours
              </div>
            </div>
            <div>
              <div className='text-2xl font-bold text-purple-600'>
                {impact.productivity}
              </div>
              <div className='text-xs text-muted-foreground mt-1'>
                Focus Protection
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Default Buffer */}
      <Card>
        <CardHeader>
          <CardTitle className='flex items-center gap-2'>
            <Clock className='h-5 w-5 text-blue-600' />
            Default Task Buffer
          </CardTitle>
          <CardDescription>
            Automatic break time added between scheduled tasks
          </CardDescription>
        </CardHeader>
        <CardContent className='space-y-6'>
          <div className='space-y-3'>
            <div className='flex items-center justify-between'>
              <Label className='text-sm font-medium'>Buffer Duration</Label>
              <div className='flex items-center gap-2'>
                <Badge variant='secondary' className='font-mono'>
                  {settings.defaultBufferMinutes} min
                </Badge>
                <TooltipProvider>
                  <Tooltip>
                    <TooltipTrigger>
                      <Info className='h-4 w-4 text-muted-foreground' />
                    </TooltipTrigger>
                    <TooltipContent>
                      <p className='max-w-xs'>
                        Time added between tasks for mental reset, bathroom
                        breaks, etc.
                      </p>
                    </TooltipContent>
                  </Tooltip>
                </TooltipProvider>
              </div>
            </div>

            <Slider
              value={[settings.defaultBufferMinutes]}
              min={0}
              max={30}
              step={5}
              onValueChange={([value]) => {
                setSettings((prev) => ({
                  ...prev,
                  defaultBufferMinutes: value,
                }));
                setHasChanges(true);
              }}
              className='[&_[role=slider]]:bg-blue-600 [&_[role=slider]]:border-blue-600'
            />

            <div className='flex justify-between text-xs text-muted-foreground'>
              <span>No buffer (0 min)</span>
              <span>Standard (10 min)</span>
              <span>Generous (30 min)</span>
            </div>
          </div>

          {/* Buffer Application Rules */}
          <div className='space-y-3 pt-4 border-t'>
            <Label className='text-sm font-medium'>Apply buffers:</Label>

            <div className='space-y-2'>
              <div className='flex items-center justify-between p-3 rounded-lg border'>
                <div>
                  <Label className='text-sm font-medium cursor-pointer'>
                    Between different projects
                  </Label>
                  <p className='text-xs text-muted-foreground mt-1'>
                    Mental reset when switching project context
                  </p>
                </div>
                <Switch
                  checked={settings.applyBufferBetweenProjects}
                  onCheckedChange={(checked) => {
                    setSettings((prev) => ({
                      ...prev,
                      applyBufferBetweenProjects: checked,
                    }));
                    setHasChanges(true);
                  }}
                />
              </div>

              <div className='flex items-center justify-between p-3 rounded-lg border'>
                <div>
                  <Label className='text-sm font-medium cursor-pointer'>
                    Between different task types
                  </Label>
                  <p className='text-xs text-muted-foreground mt-1'>
                    Transition time when changing task nature
                  </p>
                </div>
                <Switch
                  checked={settings.applyBufferBetweenTaskTypes}
                  onCheckedChange={(checked) => {
                    setSettings((prev) => ({
                      ...prev,
                      applyBufferBetweenTaskTypes: checked,
                    }));
                    setHasChanges(true);
                  }}
                />
              </div>

              <div className='flex items-center justify-between p-3 rounded-lg border'>
                <div>
                  <Label className='text-sm font-medium cursor-pointer'>
                    After meetings
                  </Label>
                  <p className='text-xs text-muted-foreground mt-1'>
                    Recovery time to return to focused work
                  </p>
                </div>
                <Switch
                  checked={settings.applyBufferAfterMeetings}
                  onCheckedChange={(checked) => {
                    setSettings((prev) => ({
                      ...prev,
                      applyBufferAfterMeetings: checked,
                    }));
                    setHasChanges(true);
                  }}
                />
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Task Type Specific Buffers */}
      <Card>
        <CardHeader>
          <CardTitle className='flex items-center gap-2'>
            <Zap className='h-5 w-5 text-amber-600' />
            Task Type Specific Buffers
          </CardTitle>
          <CardDescription>
            Customize buffer duration by task complexity
          </CardDescription>
        </CardHeader>
        <CardContent className='space-y-4'>
          {TASK_TYPE_INFO.map((typeInfo) => (
            <div
              key={typeInfo.key}
              className='space-y-2 p-4 rounded-lg border bg-card'
            >
              <div className='flex items-center justify-between'>
                <div className='flex items-center gap-2'>
                  <span className='text-xl'>{typeInfo.icon}</span>
                  <div>
                    <Label className='text-sm font-medium'>
                      {typeInfo.label}
                    </Label>
                    <p className='text-xs text-muted-foreground'>
                      {typeInfo.description}
                    </p>
                  </div>
                </div>
                <Badge variant='outline' className='font-mono'>
                  {settings.buffersByTaskType[typeInfo.key]} min
                </Badge>
              </div>

              <Slider
                value={[settings.buffersByTaskType[typeInfo.key]]}
                min={0}
                max={30}
                step={5}
                onValueChange={([value]) => {
                  setSettings((prev) => ({
                    ...prev,
                    buffersByTaskType: {
                      ...prev.buffersByTaskType,
                      [typeInfo.key]: value,
                    },
                  }));
                  setHasChanges(true);
                }}
              />
            </div>
          ))}
        </CardContent>
      </Card>

      {/* Context Switch Penalty */}
      <Card>
        <CardHeader>
          <CardTitle className='flex items-center gap-2'>
            <AlertTriangle className='h-5 w-5 text-orange-600' />
            Context Switch Penalty
          </CardTitle>
          <CardDescription>
            Discourage rapid task switching to maintain focus
          </CardDescription>
        </CardHeader>
        <CardContent className='space-y-6'>
          {/* Master Penalty Slider */}
          <div className='space-y-3'>
            <div className='flex items-center justify-between'>
              <div className='flex items-center gap-2'>
                <Label className='text-sm font-medium'>
                  Overall Penalty Weight
                </Label>
                <TooltipProvider>
                  <Tooltip>
                    <TooltipTrigger>
                      <Info className='h-4 w-4 text-muted-foreground' />
                    </TooltipTrigger>
                    <TooltipContent>
                      <p className='max-w-xs'>
                        How much the algorithm tries to avoid context switches.
                        Higher = more task batching.
                      </p>
                    </TooltipContent>
                  </Tooltip>
                </TooltipProvider>
              </div>
              <Badge
                variant={
                  settings.contextSwitchPenalty > 30 ? 'default' : 'secondary'
                }
                className='font-mono'
              >
                {settings.contextSwitchPenalty}%
              </Badge>
            </div>

            <Slider
              value={[settings.contextSwitchPenalty]}
              min={0}
              max={50}
              step={5}
              onValueChange={([value]) => {
                setSettings((prev) => ({
                  ...prev,
                  contextSwitchPenalty: value,
                }));
                setHasChanges(true);
              }}
              className='[&_[role=slider]]:bg-orange-600 [&_[role=slider]]:border-orange-600'
            />

            <div className='flex justify-between text-xs text-muted-foreground'>
              <span>No penalty</span>
              <span>Moderate batching</span>
              <span>Strong batching</span>
            </div>
          </div>

          {/* Specific Switch Penalties */}
          <div className='space-y-4 pt-4 border-t'>
            <Label className='text-sm font-medium'>
              Specific Switch Penalties:
            </Label>

            <div className='space-y-3 pl-4 border-l-2'>
              <div className='space-y-2'>
                <div className='flex items-center justify-between'>
                  <Label className='text-sm'>
                    Project Switch
                    <span className='text-muted-foreground ml-1'>(min)</span>
                  </Label>
                  <span className='text-sm font-medium'>
                    +{settings.contextSwitchSettings.projectSwitch} min
                  </span>
                </div>
                <Slider
                  value={[settings.contextSwitchSettings.projectSwitch]}
                  min={0}
                  max={30}
                  step={5}
                  onValueChange={([value]) => {
                    setSettings((prev) => ({
                      ...prev,
                      contextSwitchSettings: {
                        ...prev.contextSwitchSettings,
                        projectSwitch: value,
                      },
                    }));
                    setHasChanges(true);
                  }}
                />
                <p className='text-xs text-muted-foreground'>
                  Extra time needed when switching between projects
                </p>
              </div>

              <div className='space-y-2'>
                <div className='flex items-center justify-between'>
                  <Label className='text-sm'>
                    Task Type Switch
                    <span className='text-muted-foreground ml-1'>(min)</span>
                  </Label>
                  <span className='text-sm font-medium'>
                    +{settings.contextSwitchSettings.taskTypeSwitch} min
                  </span>
                </div>
                <Slider
                  value={[settings.contextSwitchSettings.taskTypeSwitch]}
                  min={0}
                  max={20}
                  step={5}
                  onValueChange={([value]) => {
                    setSettings((prev) => ({
                      ...prev,
                      contextSwitchSettings: {
                        ...prev.contextSwitchSettings,
                        taskTypeSwitch: value,
                      },
                    }));
                    setHasChanges(true);
                  }}
                />
                <p className='text-xs text-muted-foreground'>
                  Extra time when changing task type (e.g., meeting → coding)
                </p>
              </div>

              <div className='space-y-2'>
                <div className='flex items-center justify-between'>
                  <Label className='text-sm'>
                    Meeting → Deep Work
                    <span className='text-muted-foreground ml-1'>(min)</span>
                  </Label>
                  <span className='text-sm font-medium'>
                    +{settings.contextSwitchSettings.meetingToDeepWork} min
                  </span>
                </div>
                <Slider
                  value={[settings.contextSwitchSettings.meetingToDeepWork]}
                  min={0}
                  max={30}
                  step={5}
                  onValueChange={([value]) => {
                    setSettings((prev) => ({
                      ...prev,
                      contextSwitchSettings: {
                        ...prev.contextSwitchSettings,
                        meetingToDeepWork: value,
                      },
                    }));
                    setHasChanges(true);
                  }}
                />
                <p className='text-xs text-muted-foreground'>
                  Recovery time after meetings before deep focus work
                </p>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Visualization Settings */}
      <Card>
        <CardHeader>
          <CardTitle className='flex items-center gap-2 text-base'>
            <TrendingUp className='h-4 w-4 text-muted-foreground' />
            Calendar Visualization
          </CardTitle>
          <CardDescription className='text-xs'>
            How to display buffers and context switches in calendar
          </CardDescription>
        </CardHeader>
        <CardContent className='space-y-3'>
          <div className='flex items-center justify-between p-3 rounded-lg border'>
            <div>
              <Label className='text-sm font-medium cursor-pointer'>
                Show buffers in calendar
              </Label>
              <p className='text-xs text-muted-foreground mt-1'>
                Display buffer blocks as separate calendar events
              </p>
            </div>
            <Switch
              checked={settings.visualization.showBuffersInCalendar}
              onCheckedChange={(checked) => {
                setSettings((prev) => ({
                  ...prev,
                  visualization: {
                    ...prev.visualization,
                    showBuffersInCalendar: checked,
                  },
                }));
                setHasChanges(true);
              }}
            />
          </div>

          <div className='flex items-center justify-between p-3 rounded-lg border'>
            <div>
              <Label className='text-sm font-medium cursor-pointer'>
                Highlight context switches
              </Label>
              <p className='text-xs text-muted-foreground mt-1'>
                Mark task boundaries where context switches occur
              </p>
            </div>
            <Switch
              checked={settings.visualization.highlightContextSwitches}
              onCheckedChange={(checked) => {
                setSettings((prev) => ({
                  ...prev,
                  visualization: {
                    ...prev.visualization,
                    highlightContextSwitches: checked,
                  },
                }));
                setHasChanges(true);
              }}
            />
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
            Save Buffer Settings
          </Button>
        </div>
      )}
    </div>
  );
}
