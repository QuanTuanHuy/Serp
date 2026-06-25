/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Last-mile operations page
 */

'use client';

import React from 'react';
import { InboundSortingPage } from './InboundSortingPage';

export const LastMilePage: React.FC = () => {
  return (
    <div className='space-y-6'>
      <InboundSortingPage />
    </div>
  );
};
