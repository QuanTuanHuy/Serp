/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - TMS vehicle settings page
 */

'use client';

import React from 'react';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui';
import { FirstMileVehicleListPage } from './FirstMileVehicleListPage';
import { SecondMileVehicleListPage } from './SecondMileVehicleListPage';

export type VehicleSettingsScope = 'first-mile' | 'second-mile';

interface VehicleSettingsPageProps {
  initialScope?: VehicleSettingsScope;
}

export const VehicleSettingsPage: React.FC<VehicleSettingsPageProps> = ({
  initialScope = 'first-mile',
}) => {
  const [scope, setScope] = React.useState<VehicleSettingsScope>(initialScope);

  const handleScopeChange = (value: string) => {
    if (value === 'first-mile' || value === 'second-mile') {
      setScope(value);
    }
  };

  return (
    <div className='space-y-6'>
      <div>
        <h1 className='text-2xl font-bold tracking-tight'>Vehicles</h1>
        <p className='text-muted-foreground'>
          Manage first-mile and second-mile vehicles from TMS settings.
        </p>
      </div>

      <Tabs value={scope} onValueChange={handleScopeChange}>
        <TabsList className='grid w-full grid-cols-2 sm:w-auto'>
          <TabsTrigger value='first-mile'>First-mile</TabsTrigger>
          <TabsTrigger value='second-mile'>Second-mile</TabsTrigger>
        </TabsList>

        <TabsContent value='first-mile' className='mt-6'>
          <FirstMileVehicleListPage />
        </TabsContent>

        <TabsContent value='second-mile' className='mt-6'>
          <SecondMileVehicleListPage showScopeNavigation={false} />
        </TabsContent>
      </Tabs>
    </div>
  );
};
