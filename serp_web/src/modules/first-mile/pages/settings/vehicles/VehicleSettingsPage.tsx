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

  const renderScopeTabs = () => (
    <TabsList>
      <TabsTrigger value='first-mile'>Chặng đầu</TabsTrigger>
      <TabsTrigger value='second-mile'>Chặng giữa</TabsTrigger>
    </TabsList>
  );

  return (
    <div className='space-y-6'>
      <div>
        <h1 className='text-2xl font-bold tracking-tight'>Phương tiện</h1>
        <p className='text-muted-foreground'>
          Quản lý phương tiện chặng đầu và chặng giữa trong thiết lập TMS.
        </p>
      </div>

      <Tabs
        value={scope}
        onValueChange={handleScopeChange}
        className='space-y-4'
      >
        <TabsContent value='first-mile'>
          <FirstMileVehicleListPage scopeNavigation={renderScopeTabs()} />
        </TabsContent>

        <TabsContent value='second-mile'>
          <SecondMileVehicleListPage
            showScopeNavigation={false}
            scopeNavigation={renderScopeTabs()}
          />
        </TabsContent>
      </Tabs>
    </div>
  );
};
