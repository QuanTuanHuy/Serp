/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Last-mile operations page
 */

'use client';

import React, { useState } from 'react';
import { PackageCheck, Route } from 'lucide-react';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui/tabs';
import { InboundSortingPage } from './InboundSortingPage';
import { DeliveryManifestListPage } from './DeliveryManifestListPage';

export const LastMilePage: React.FC = () => {
  const [activeTab, setActiveTab] = useState('inbound-sorting');

  return (
    <div className='space-y-6'>
      <Tabs
        value={activeTab}
        onValueChange={setActiveTab}
        className='space-y-4'
      >
        <TabsList className='grid w-full grid-cols-2 lg:w-auto'>
          <TabsTrigger value='inbound-sorting' className='gap-2'>
            <PackageCheck className='h-4 w-4' />
            Inbound Sorting
          </TabsTrigger>
          <TabsTrigger value='delivery-manifests' className='gap-2'>
            <Route className='h-4 w-4' />
            Delivery Manifests
          </TabsTrigger>
        </TabsList>

        <TabsContent value='inbound-sorting' className='mt-0'>
          <InboundSortingPage />
        </TabsContent>

        <TabsContent value='delivery-manifests' className='mt-0'>
          <DeliveryManifestListPage />
        </TabsContent>
      </Tabs>
    </div>
  );
};
