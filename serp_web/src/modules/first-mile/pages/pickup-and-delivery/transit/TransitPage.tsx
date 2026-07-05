/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Transit pickup and delivery page
 */

'use client';

import * as React from 'react';
import { PackageCheck, Route } from 'lucide-react';

import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui/tabs';

import { DriverHandoverPage } from '../../handover-manifests/DriverHandoverPage';
import { HubTransferDriverCheckinPanel } from './HubTransferDriverCheckinPanel';

export function TransitPage() {
  const [activeTab, setActiveTab] = React.useState('handover');

  return (
    <div className='space-y-6 p-6'>
      <div>
        <h1 className='text-2xl font-semibold tracking-tight'>Trung chuyển</h1>
        <p className='max-w-3xl text-sm text-muted-foreground'>
          Tập trung các thao tác check-in tài xế cho các chặng trung chuyển,
          tách khỏi gom hàng chặng đầu và giao hàng chặng cuối.
        </p>
      </div>

      <Tabs
        value={activeTab}
        onValueChange={setActiveTab}
        className='space-y-4'
      >
        <TabsList className='grid w-full grid-cols-2 lg:w-auto'>
          <TabsTrigger value='handover'>
            <Route className='h-4 w-4' />
            Bưu cục ↔ Hub
          </TabsTrigger>
          <TabsTrigger value='hub-transfer'>
            <PackageCheck className='h-4 w-4' />
            Hub ↔ Hub/Bưu cục
          </TabsTrigger>
        </TabsList>

        <TabsContent value='handover' className='mt-0'>
          <DriverHandoverPage />
        </TabsContent>

        <TabsContent value='hub-transfer' className='mt-0'>
          <HubTransferDriverCheckinPanel />
        </TabsContent>
      </Tabs>
    </div>
  );
}
