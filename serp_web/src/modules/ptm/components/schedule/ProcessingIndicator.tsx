/*
Author: QuanTuanHuy
Description: Part of Serp Project
Processing Indicator - Shows when schedule plan is being optimized
*/

'use client';

import { useEffect, useState } from 'react';
import { Clock, TrendingUp, CheckCircle2 } from 'lucide-react';
import { Card, CardContent } from '@/shared/components/ui/card';
import { Progress } from '@/shared/components/ui/progress';

interface ProcessingIndicatorProps {
  totalTasks: number;
  onComplete?: () => void;
}

export function ProcessingIndicator({
  totalTasks,
  onComplete,
}: ProcessingIndicatorProps) {
  const [progress, setProgress] = useState(0);
  const [message, setMessage] = useState('Analyzing tasks...');

  useEffect(() => {
    const messages = [
      'Analyzing task priorities...',
      'Checking deadlines and constraints...',
      'Finding optimal time slots...',
      'Applying scheduling algorithm...',
      'Calculating event placements...',
      'Finalizing schedule...',
    ];

    let messageIndex = 0;
    let currentProgress = 0;

    const messageInterval = setInterval(() => {
      messageIndex = (messageIndex + 1) % messages.length;
      setMessage(messages[messageIndex]);
    }, 2000);

    const progressInterval = setInterval(() => {
      currentProgress += Math.random() * 15;
      if (currentProgress >= 100) {
        currentProgress = 100;
        clearInterval(progressInterval);
        clearInterval(messageInterval);
        setMessage('Optimization complete!');
        setTimeout(() => {
          onComplete?.();
        }, 1000);
      }
      setProgress(currentProgress);
    }, 500);

    return () => {
      clearInterval(messageInterval);
      clearInterval(progressInterval);
    };
  }, [onComplete]);

  return (
    <Card className='border-blue-200 bg-blue-50/50'>
      <CardContent className='p-6'>
        <div className='space-y-4'>
          {/* Header */}
          <div className='flex items-center gap-3'>
            <div className='flex h-12 w-12 items-center justify-center rounded-full bg-blue-100'>
              {progress < 100 ? (
                <Clock className='h-6 w-6 animate-spin text-blue-600' />
              ) : (
                <CheckCircle2 className='h-6 w-6 text-green-600' />
              )}
            </div>
            <div className='flex-1'>
              <h3 className='font-semibold'>
                {progress < 100
                  ? 'Optimizing Your Schedule'
                  : 'Optimization Complete'}
              </h3>
              <p className='text-sm text-muted-foreground'>{message}</p>
            </div>
          </div>

          {/* Progress Bar */}
          <div className='space-y-2'>
            <Progress value={progress} className='h-2' />
            <div className='flex items-center justify-between text-xs text-muted-foreground'>
              <span>Processing {totalTasks} tasks</span>
              <span>{Math.round(progress)}%</span>
            </div>
          </div>

          {/* Info */}
          <div className='rounded-lg border bg-card/50 p-3 text-xs text-muted-foreground'>
            <div className='flex items-start gap-2'>
              <TrendingUp className='mt-0.5 h-4 w-4 flex-shrink-0 text-blue-600' />
              <div>
                <strong className='text-foreground'>About Optimization:</strong>
                <br />
                Our scheduling algorithm uses a hybrid approach combining greedy
                insertion and ripple effect strategies to find optimal time
                slots while respecting deadlines, priorities, and your
                availability.
              </div>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
