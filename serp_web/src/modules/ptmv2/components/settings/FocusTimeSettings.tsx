/**
 * PTM v2 - Focus Time Settings Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Manage deep work blocks and protection rules
 */

'use client';

import { useState } from 'react';
import { Plus, Trash2, Lock, Sparkles } from 'lucide-react';
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
import { cn } from '@/shared/utils';
import { toast } from 'sonner';

interface FocusBlock {
  id: string;
  dayOfWeek: number; // 0-6 (Sunday-Saturday)
  startHour: number;
  startMinute: number;
  endHour: number;
  endMinute: number;
}

interface FocusTimeSettingsProps {
  className?: string;
}

const DAYS_OF_WEEK = [
  'Sunday',
  'Monday',
  'Tuesday',
  'Wednesday',
  'Thursday',
  'Friday',
  'Saturday',
];

export function FocusTimeSettings({ className }: FocusTimeSettingsProps) {
  const [focusBlocks, setFocusBlocks] = useState<FocusBlock[]>([
    {
      id: '1',
      dayOfWeek: 1,
      startHour: 9,
      startMinute: 0,
      endHour: 12,
      endMinute: 0,
    },
    {
      id: '2',
      dayOfWeek: 2,
      startHour: 9,
      startMinute: 0,
      endHour: 11,
      endMinute: 0,
    },
    {
      id: '3',
      dayOfWeek: 3,
      startHour: 9,
      startMinute: 0,
      endHour: 12,
      endMinute: 0,
    },
    {
      id: '4',
      dayOfWeek: 5,
      startHour: 9,
      startMinute: 0,
      endHour: 11,
      endMinute: 0,
    },
  ]);

  const [blockMeetings, setBlockMeetings] = useState(true);
  const [muteNotifications, setMuteNotifications] = useState(true);
  const [onlyDeepWork, setOnlyDeepWork] = useState(true);
  const [allowOverrides, setAllowOverrides] = useState(false);
  const [flexibility, setFlexibility] = useState([30]);

  const addFocusBlock = (dayOfWeek: number) => {
    const newBlock: FocusBlock = {
      id: Date.now().toString(),
      dayOfWeek,
      startHour: 9,
      startMinute: 0,
      endHour: 11,
      endMinute: 0,
    };
    setFocusBlocks([...focusBlocks, newBlock]);
    toast.success('Focus block added');
  };

  const removeFocusBlock = (id: string) => {
    setFocusBlocks(focusBlocks.filter((block) => block.id !== id));
    toast.success('Focus block removed');
  };

  const formatTime = (hour: number, minute: number) => {
    const period = hour >= 12 ? 'PM' : 'AM';
    const displayHour = hour > 12 ? hour - 12 : hour === 0 ? 12 : hour;
    return `${displayHour}:${minute.toString().padStart(2, '0')} ${period}`;
  };

  const getDuration = (block: FocusBlock) => {
    const totalMinutes =
      block.endHour * 60 +
      block.endMinute -
      (block.startHour * 60 + block.startMinute);
    const hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;
    return minutes > 0 ? `${hours}h ${minutes}m` : `${hours} hours`;
  };

  const groupedBlocks = DAYS_OF_WEEK.map((day, index) => ({
    day,
    dayIndex: index,
    blocks: focusBlocks.filter((block) => block.dayOfWeek === index),
  }));

  const handleSave = () => {
    toast.success('Focus time settings saved!');
  };

  return (
    <div className={cn('space-y-6', className)}>
      {/* Header */}
      <div>
        <h2 className='text-2xl font-bold flex items-center gap-2'>
          <Lock className='h-6 w-6 text-purple-600' />
          Focus Time Settings
        </h2>
        <p className='text-muted-foreground mt-1'>
          Deep work blocks protect your most productive hours
        </p>
      </div>

      {/* Weekly Schedule */}
      <Card>
        <CardHeader>
          <CardTitle>Weekly Schedule</CardTitle>
          <CardDescription>
            Configure your focus time blocks for each day
          </CardDescription>
        </CardHeader>
        <CardContent className='space-y-4'>
          {groupedBlocks.map(({ day, dayIndex, blocks }) => (
            <div key={dayIndex} className='space-y-2'>
              <div className='flex items-center justify-between'>
                <Label className='text-base font-medium'>{day}</Label>
                <Button
                  variant='outline'
                  size='sm'
                  onClick={() => addFocusBlock(dayIndex)}
                >
                  <Plus className='h-4 w-4 mr-1' />
                  Add Block
                </Button>
              </div>

              {blocks.length === 0 ? (
                <p className='text-sm text-muted-foreground pl-4'>
                  No focus blocks
                </p>
              ) : (
                <div className='space-y-2 pl-4'>
                  {blocks.map((block) => (
                    <div
                      key={block.id}
                      className='flex items-center justify-between p-3 bg-purple-50 dark:bg-purple-950/20 rounded-lg border border-purple-200 dark:border-purple-800'
                    >
                      <div className='flex items-center gap-3'>
                        <Lock className='h-4 w-4 text-purple-600' />
                        <div>
                          <p className='font-medium text-sm'>
                            {formatTime(block.startHour, block.startMinute)} -{' '}
                            {formatTime(block.endHour, block.endMinute)}
                          </p>
                          <p className='text-xs text-muted-foreground'>
                            {getDuration(block)} • Deep Work
                          </p>
                        </div>
                      </div>
                      <Button
                        variant='ghost'
                        size='icon'
                        onClick={() => removeFocusBlock(block.id)}
                        className='h-8 w-8'
                      >
                        <Trash2 className='h-4 w-4 text-red-600' />
                      </Button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          ))}
        </CardContent>
      </Card>

      {/* Protection Rules */}
      <Card>
        <CardHeader>
          <CardTitle>Protection Rules</CardTitle>
          <CardDescription>
            Configure how focus time blocks are protected
          </CardDescription>
        </CardHeader>
        <CardContent className='space-y-6'>
          <div className='flex items-center justify-between'>
            <div className='space-y-0.5'>
              <Label>Block meetings during focus time</Label>
              <p className='text-sm text-muted-foreground'>
                Prevent scheduling meetings in focus blocks
              </p>
            </div>
            <Switch
              checked={blockMeetings}
              onCheckedChange={setBlockMeetings}
            />
          </div>

          <div className='flex items-center justify-between'>
            <div className='space-y-0.5'>
              <Label>Mute notifications</Label>
              <p className='text-sm text-muted-foreground'>
                Silence all notifications during focus time
              </p>
            </div>
            <Switch
              checked={muteNotifications}
              onCheckedChange={setMuteNotifications}
            />
          </div>

          <div className='flex items-center justify-between'>
            <div className='space-y-0.5'>
              <Label>Only schedule deep work tasks</Label>
              <p className='text-sm text-muted-foreground'>
                Restrict focus blocks to tasks marked as deep work
              </p>
            </div>
            <Switch checked={onlyDeepWork} onCheckedChange={setOnlyDeepWork} />
          </div>

          <div className='flex items-center justify-between'>
            <div className='space-y-0.5'>
              <Label>Allow manual overrides</Label>
              <p className='text-sm text-muted-foreground'>
                Permit manual scheduling changes to focus blocks
              </p>
            </div>
            <Switch
              checked={allowOverrides}
              onCheckedChange={setAllowOverrides}
            />
          </div>

          <div className='space-y-3 pt-4 border-t'>
            <Label>Flexibility Level</Label>
            <Slider
              value={flexibility}
              onValueChange={setFlexibility}
              max={100}
              step={10}
              className='cursor-pointer'
            />
            <div className='flex justify-between text-xs text-muted-foreground'>
              <span>Strict</span>
              <span className='font-medium text-foreground'>
                {flexibility[0]}%
              </span>
              <span>Flexible</span>
            </div>
            <p className='text-sm text-muted-foreground'>
              Higher flexibility allows the AI to adjust focus blocks when
              necessary
            </p>
          </div>
        </CardContent>
      </Card>

      {/* AI Recommendation */}
      <Card className='bg-gradient-to-br from-purple-50 to-blue-50 dark:from-purple-950/20 dark:to-blue-950/20 border-purple-200 dark:border-purple-800'>
        <CardContent className='pt-6'>
          <div className='flex items-start gap-3'>
            <Sparkles className='h-5 w-5 text-purple-600 flex-shrink-0 mt-0.5' />
            <div className='space-y-2'>
              <p className='font-medium text-purple-900 dark:text-purple-100'>
                💡 AI Recommendation
              </p>
              <p className='text-sm text-purple-700 dark:text-purple-300'>
                Based on your completion history, you're most productive 9-11 AM
                on weekdays. Consider protecting this time for deep work tasks.
              </p>
              <p className='text-sm text-purple-700 dark:text-purple-300'>
                Your current focus blocks align well with your productivity
                patterns. Keep up the good work!
              </p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Actions */}
      <div className='flex justify-end gap-2'>
        <Button variant='outline'>Reset to Default</Button>
        <Button onClick={handleSave}>Save Settings</Button>
      </div>
    </div>
  );
}
