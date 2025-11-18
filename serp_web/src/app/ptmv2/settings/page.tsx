/**
 * PTM v2 - Settings Page
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - User preferences and availability settings
 */

'use client';

import { useState } from 'react';
import { Settings, Clock, Flame, User, Bell } from 'lucide-react';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui/tabs';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription,
} from '@/shared/components/ui/card';
import { AvailabilitySettings } from '@/modules/ptmv2/components/settings';

export default function PTMSettingsPage() {
  const [activeTab, setActiveTab] = useState('availability');

  return (
    <div className='container mx-auto py-8 px-4 max-w-7xl'>
      {/* Page Header */}
      <div className='mb-8'>
        <h1 className='text-3xl font-bold flex items-center gap-3'>
          <Settings className='h-8 w-8 text-blue-600' />
          Settings
        </h1>
        <p className='text-muted-foreground mt-2'>
          Manage your productivity preferences and availability
        </p>
      </div>

      {/* Settings Tabs */}
      <Tabs
        value={activeTab}
        onValueChange={setActiveTab}
        className='space-y-6'
      >
        <TabsList className='grid w-full grid-cols-4 max-w-2xl'>
          <TabsTrigger value='availability' className='gap-2'>
            <Clock className='h-4 w-4' />
            Availability
          </TabsTrigger>
          <TabsTrigger value='focus' className='gap-2'>
            <Flame className='h-4 w-4' />
            Focus Time
          </TabsTrigger>
          <TabsTrigger value='profile' className='gap-2'>
            <User className='h-4 w-4' />
            Profile
          </TabsTrigger>
          <TabsTrigger value='notifications' className='gap-2'>
            <Bell className='h-4 w-4' />
            Notifications
          </TabsTrigger>
        </TabsList>

        {/* Availability Tab */}
        <TabsContent value='availability' className='space-y-6'>
          <AvailabilitySettings />
        </TabsContent>

        {/* Focus Time Tab (Legacy - kept for reference) */}
        <TabsContent value='focus' className='space-y-6'>
          <Card>
            <CardHeader>
              <CardTitle>Focus Time Protection</CardTitle>
              <CardDescription>
                Advanced focus time settings (deprecated - use Availability
                Calendar with Focus tagging)
              </CardDescription>
            </CardHeader>
            <CardContent>
              <p className='text-sm text-muted-foreground'>
                Focus time is now integrated into the Availability Calendar.
                Switch to the Availability tab to mark your focus periods.
              </p>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Profile Tab */}
        <TabsContent value='profile' className='space-y-6'>
          <Card>
            <CardHeader>
              <CardTitle>Profile Settings</CardTitle>
              <CardDescription>Manage your account information</CardDescription>
            </CardHeader>
            <CardContent>
              <p className='text-sm text-muted-foreground'>
                Profile settings coming soon...
              </p>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Notifications Tab */}
        <TabsContent value='notifications' className='space-y-6'>
          <Card>
            <CardHeader>
              <CardTitle>Notification Preferences</CardTitle>
              <CardDescription>
                Configure how you receive notifications
              </CardDescription>
            </CardHeader>
            <CardContent>
              <p className='text-sm text-muted-foreground'>
                Notification settings coming soon...
              </p>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}
