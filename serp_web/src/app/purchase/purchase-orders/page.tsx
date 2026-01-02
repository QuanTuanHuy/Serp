/**
 * Purchase Orders Page
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Purchase order management
 */

'use client';

import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components';

export default function PurchaseOrdersPage() {
  return (
    <div className='space-y-6'>
      <div className='flex items-center justify-between'>
        <h1 className='text-3xl font-bold'>Purchase Orders</h1>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Order Management</CardTitle>
        </CardHeader>
        <CardContent>
          <p className='text-muted-foreground'>
            Create and track purchase orders
          </p>
        </CardContent>
      </Card>
    </div>
  );
}
