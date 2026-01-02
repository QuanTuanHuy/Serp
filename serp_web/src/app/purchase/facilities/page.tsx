/**
 * Facilities Page
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Facility and warehouse management
 */

'use client';

import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components';

export default function FacilitiesPage() {
  return (
    <div className='space-y-6'>
      <div className='flex items-center justify-between'>
        <h1 className='text-3xl font-bold'>Facilities</h1>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Facility Management</CardTitle>
        </CardHeader>
        <CardContent>
          <p className='text-muted-foreground'>
            Manage warehouses and storage facilities
          </p>
        </CardContent>
      </Card>
    </div>
  );
}
