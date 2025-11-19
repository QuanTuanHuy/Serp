/**
 * PTM v2 - Settings Page
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - User preferences and availability settings
 */

'use client';

import { useState } from 'react';
import { Settings, Clock, Sparkles, Shield, Bell } from 'lucide-react';
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
import { AlgorithmPreferences } from '@/modules/ptmv2/components/settings/AlgorithmPreferences';
import { BufferSettings } from '@/modules/ptmv2/components/settings/BufferSettings';

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
        <TabsList className='grid w-full grid-cols-4 max-w-3xl'>
          <TabsTrigger value='availability' className='gap-2'>
            <Clock className='h-4 w-4' />
            Availability
          </TabsTrigger>
          <TabsTrigger value='algorithm' className='gap-2'>
            <Sparkles className='h-4 w-4' />
            Algorithm
          </TabsTrigger>
          <TabsTrigger value='buffers' className='gap-2'>
            <Shield className='h-4 w-4' />
            Buffers
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

        {/* Algorithm Preferences Tab */}
        <TabsContent value='algorithm' className='space-y-6'>
          <AlgorithmPreferences />
        </TabsContent>

        {/* Buffer Settings Tab */}
        <TabsContent value='buffers' className='space-y-6'>
          <BufferSettings />
        </TabsContent>

        {/* Notifications Tab */}
        <TabsContent value='notifications' className='space-y-6'>
          <Card>
            <CardHeader>
              <CardTitle>Notification Preferences</CardTitle>
              <CardDescription>
                Configure how you receive deadline alerts and schedule updates
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
