/**
 * PTM Calendar Page - Daily Calendar
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Daily Calendar and Events
 */

'use client';

import React, { useState } from 'react';
import { Button, Card } from '@/shared/components';
import {
  Calendar as CalendarIcon,
  Plus,
  ChevronLeft,
  ChevronRight,
  Clock,
  MapPin,
  Users,
} from 'lucide-react';

const Calendar: React.FC = () => {
  const [currentDate, setCurrentDate] = useState(new Date());

  const events = [
    {
      id: 1,
      title: 'Team Standup',
      time: '9:00 AM - 9:30 AM',
      location: 'Conference Room A',
      attendees: 5,
      type: 'meeting',
    },
    {
      id: 2,
      title: 'Code Review Session',
      time: '10:00 AM - 11:30 AM',
      location: 'Online',
      attendees: 3,
      type: 'work',
    },
    {
      id: 3,
      title: 'Lunch Break',
      time: '12:00 PM - 1:00 PM',
      location: 'Cafeteria',
      attendees: 1,
      type: 'break',
    },
    {
      id: 4,
      title: 'Client Presentation',
      time: '2:00 PM - 3:00 PM',
      location: 'Meeting Room B',
      attendees: 8,
      type: 'meeting',
    },
    {
      id: 5,
      title: 'Documentation Review',
      time: '4:00 PM - 5:00 PM',
      location: 'Online',
      attendees: 2,
      type: 'work',
    },
  ];

  const getEventColor = (type: string) => {
    switch (type) {
      case 'meeting':
        return 'bg-blue-100 text-blue-700 border-blue-200';
      case 'work':
        return 'bg-green-100 text-green-700 border-green-200';
      case 'break':
        return 'bg-yellow-100 text-yellow-700 border-yellow-200';
      default:
        return 'bg-gray-100 text-gray-700 border-gray-200';
    }
  };

  const formatDate = (date: Date) => {
    return date.toLocaleDateString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  const navigateDate = (direction: 'prev' | 'next') => {
    const newDate = new Date(currentDate);
    newDate.setDate(newDate.getDate() + (direction === 'next' ? 1 : -1));
    setCurrentDate(newDate);
  };

  return (
    <div className='space-y-6'>
      {/* Header */}
      <div className='flex items-center justify-between'>
        <div className='flex items-center gap-2'>
          <CalendarIcon className='h-6 w-6' />
          <h1 className='text-2xl font-bold'>Daily Calendar</h1>
        </div>
        <Button>
          <Plus className='mr-2 h-4 w-4' />
          New Event
        </Button>
      </div>

      {/* Date Navigation */}
      <Card className='p-6'>
        <div className='flex items-center justify-between'>
          <Button
            variant='outline'
            size='sm'
            onClick={() => navigateDate('prev')}
          >
            <ChevronLeft className='h-4 w-4' />
          </Button>

          <div className='text-center'>
            <h2 className='text-lg font-semibold'>{formatDate(currentDate)}</h2>
            <p className='text-sm text-muted-foreground'>
              {events.length} events scheduled
            </p>
          </div>

          <Button
            variant='outline'
            size='sm'
            onClick={() => navigateDate('next')}
          >
            <ChevronRight className='h-4 w-4' />
          </Button>
        </div>
      </Card>

      {/* Today's Schedule */}
      <Card className='p-6'>
        <h3 className='mb-4 text-lg font-semibold'>Today's Schedule</h3>
        <div className='space-y-3'>
          {events.map((event) => (
            <div
              key={event.id}
              className={`rounded-lg border p-4 ${getEventColor(event.type)}`}
            >
              <div className='flex items-start justify-between'>
                <div className='flex-1'>
                  <h4 className='font-medium'>{event.title}</h4>
                  <div className='mt-2 space-y-1'>
                    <div className='flex items-center gap-2 text-sm'>
                      <Clock className='h-4 w-4' />
                      <span>{event.time}</span>
                    </div>
                    <div className='flex items-center gap-2 text-sm'>
                      <MapPin className='h-4 w-4' />
                      <span>{event.location}</span>
                    </div>
                    <div className='flex items-center gap-2 text-sm'>
                      <Users className='h-4 w-4' />
                      <span>{event.attendees} attendees</span>
                    </div>
                  </div>
                </div>
                <div className='flex gap-2'>
                  <Button size='sm' variant='outline'>
                    Edit
                  </Button>
                  <Button size='sm' variant='outline'>
                    Join
                  </Button>
                </div>
              </div>
            </div>
          ))}
        </div>
      </Card>

      {/* Quick Stats */}
      <div className='grid grid-cols-1 gap-4 md:grid-cols-4'>
        <Card className='p-6 text-center'>
          <div className='text-2xl font-bold text-blue-600'>5</div>
          <div className='text-sm text-muted-foreground'>Total Events</div>
        </Card>
        <Card className='p-6 text-center'>
          <div className='text-2xl font-bold text-green-600'>3</div>
          <div className='text-sm text-muted-foreground'>Meetings</div>
        </Card>
        <Card className='p-6 text-center'>
          <div className='text-2xl font-bold text-yellow-600'>7h</div>
          <div className='text-sm text-muted-foreground'>Total Duration</div>
        </Card>
        <Card className='p-6 text-center'>
          <div className='text-2xl font-bold text-purple-600'>2</div>
          <div className='text-sm text-muted-foreground'>Free Slots</div>
        </Card>
      </div>

      {/* Calendar Actions */}
      <Card className='p-6'>
        <h3 className='mb-4 text-lg font-semibold'>Quick Actions</h3>
        <div className='grid grid-cols-1 gap-4 md:grid-cols-4'>
          <Button variant='outline' className='justify-start'>
            <Plus className='mr-2 h-4 w-4' />
            Schedule Meeting
          </Button>
          <Button variant='outline' className='justify-start'>
            <Clock className='mr-2 h-4 w-4' />
            Block Focus Time
          </Button>
          <Button variant='outline' className='justify-start'>
            <CalendarIcon className='mr-2 h-4 w-4' />
            View Week
          </Button>
          <Button variant='outline' className='justify-start'>
            <Users className='mr-2 h-4 w-4' />
            Team Calendar
          </Button>
        </div>
      </Card>
    </div>
  );
};

export default Calendar;
