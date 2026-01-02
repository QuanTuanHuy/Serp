/**
 * Shipments Page
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Shipment tracking
 */

'use client';

import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components';

export default function ShipmentsPage() {
  return (
    <div className='space-y-6'>
      <div className='flex items-center justify-between'>
        <h1 className='text-3xl font-bold'>Shipments</h1>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Shipment Tracking</CardTitle>
        </CardHeader>
        <CardContent>
          <p className='text-muted-foreground'>
            Monitor and track incoming shipments
          </p>
        </CardContent>
      </Card>
    </div>
  );
}
