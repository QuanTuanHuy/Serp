/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Pickup and delivery delivery page
 */

'use client';

import React from 'react';
import { InboundSortingPage } from './InboundSortingPage';
import { LastMileDispatchersPage } from './LastMileDispatchersPage';

export const DeliveryPage: React.FC = () => {
  return (
    <div className='space-y-6'>
      <InboundSortingPage />
      <LastMileDispatchersPage />
    </div>
  );
};
