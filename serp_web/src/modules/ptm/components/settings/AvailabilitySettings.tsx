/**
 * PTM v2 - Availability Settings Container
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Container component for availability calendar with API integration
 */

'use client';

import { useEffect, useState } from 'react';
import { Loader2, AlertCircle, Sparkles } from 'lucide-react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Alert, AlertDescription } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { toast } from 'sonner';
import { AvailabilityCalendar } from './AvailabilityCalendar';
import type {
  AvailabilitySlot,
  AvailabilityCalendar as AvailabilityType,
} from '../../types';

interface AvailabilitySettingsProps {
  className?: string;
}

// Helper function to convert minutes to time string
const minutesToTime = (minutes: number): string => {
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  return `${hours.toString().padStart(2, '0')}:${mins.toString().padStart(2, '0')}`;
};

// Helper function to convert time string to minutes
const timeToMinutes = (time: string): number => {
  const [hours, minutes] = time.split(':').map(Number);
  return hours * 60 + minutes;
};

export function AvailabilitySettings({ className }: AvailabilitySettingsProps) {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [availabilityData, setAvailabilityData] = useState<AvailabilitySlot[]>(
    []
  );

  // Mock data for development - replace with API call
  useEffect(() => {
    loadAvailability();
  }, []);

  const loadAvailability = async () => {
    setIsLoading(true);
    setError(null);

    try {
      // TODO: Replace with actual API call
      // const data = await useGetAvailabilityCalendarQuery();

      // Mock data for demonstration
      await new Promise((resolve) => setTimeout(resolve, 500));

      const mockData: AvailabilitySlot[] = [
        // Monday
        {
          dayOfWeek: 1,
          startTime: '09:00',
          endTime: '12:00',
          slotType: 'regular',
          isEnabled: true,
        },
        {
          dayOfWeek: 1,
          startTime: '13:00',
          endTime: '15:00',
          slotType: 'focus',
          isEnabled: true,
        },
        {
          dayOfWeek: 1,
          startTime: '15:30',
          endTime: '17:00',
          slotType: 'regular',
          isEnabled: true,
        },

        // Tuesday
        {
          dayOfWeek: 2,
          startTime: '09:00',
          endTime: '11:00',
          slotType: 'focus',
          isEnabled: true,
        },
        {
          dayOfWeek: 2,
          startTime: '11:00',
          endTime: '17:00',
          slotType: 'regular',
          isEnabled: true,
        },

        // Wednesday
        {
          dayOfWeek: 3,
          startTime: '09:00',
          endTime: '12:00',
          slotType: 'regular',
          isEnabled: true,
        },
        {
          dayOfWeek: 3,
          startTime: '13:00',
          endTime: '16:00',
          slotType: 'focus',
          isEnabled: true,
        },

        // Thursday
        {
          dayOfWeek: 4,
          startTime: '09:00',
          endTime: '17:00',
          slotType: 'regular',
          isEnabled: true,
        },

        // Friday
        {
          dayOfWeek: 5,
          startTime: '09:00',
          endTime: '11:00',
          slotType: 'focus',
          isEnabled: true,
        },
        {
          dayOfWeek: 5,
          startTime: '11:00',
          endTime: '15:00',
          slotType: 'regular',
          isEnabled: true,
        },
      ];

      setAvailabilityData(mockData);
    } catch (err) {
      setError('Failed to load availability settings');
      toast.error('Failed to load availability settings');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSave = async (slots: AvailabilitySlot[]) => {
    setIsLoading(true);
    setError(null);

    try {
      // Convert UI slots to API format
      const apiData: AvailabilityType[] = slots.map((slot) => ({
        dayOfWeek: slot.dayOfWeek,
        startMin: timeToMinutes(slot.startTime),
        endMin: timeToMinutes(slot.endTime),
        slotType: slot.slotType,
        activeStatus: 'ACTIVE' as const,
      }));

      // TODO: Replace with actual API call
      // await replaceAvailabilityCalendar({ items: apiData });

      // Mock API call
      await new Promise((resolve) => setTimeout(resolve, 800));

      setAvailabilityData(slots);

      toast.success('Availability saved successfully!', {
        description: 'AI will use these hours for scheduling.',
      });
    } catch (err) {
      setError('Failed to save availability settings');
      toast.error('Failed to save availability settings');
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading && availabilityData.length === 0) {
    return (
      <div
        className={cn(
          'flex items-center justify-center min-h-[400px]',
          className
        )}
      >
        <div className='text-center space-y-3'>
          <Loader2 className='h-8 w-8 animate-spin mx-auto text-muted-foreground' />
          <p className='text-sm text-muted-foreground'>
            Loading availability...
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className={cn('space-y-6', className)}>
      {error && (
        <Alert variant='destructive'>
          <AlertCircle className='h-4 w-4' />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {/* AI Recommendation Card */}
      <Card className='bg-gradient-to-br from-blue-50 to-purple-50 dark:from-blue-950/20 dark:to-purple-950/20 border-blue-200 dark:border-blue-800'>
        <CardHeader>
          <CardTitle className='flex items-center gap-2 text-blue-900 dark:text-blue-100'>
            <Sparkles className='h-5 w-5 text-blue-600' />
            💡 Smart Scheduling Tips
          </CardTitle>
        </CardHeader>
        <CardContent className='space-y-3 text-sm text-blue-700 dark:text-blue-300'>
          <p>
            <strong>Focus Time:</strong> Mark your most productive hours
            (typically 9-11 AM) as focus time. AI will schedule complex tasks
            during these blocks.
          </p>
          <p>
            <strong>Regular Time:</strong> Standard availability for any task
            type. AI balances task complexity and urgency.
          </p>
          <p>
            <strong>Flexible Time:</strong> Low-priority slots. AI uses these
            only when necessary or for quick tasks.
          </p>
        </CardContent>
      </Card>

      {/* Main Calendar Component */}
      <AvailabilityCalendar
        initialSlots={availabilityData}
        onSave={handleSave}
      />
    </div>
  );
}
