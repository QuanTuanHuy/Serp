/**
 * Suppliers Page
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Supplier management
 */

'use client';

import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components';

export default function SuppliersPage() {
  return (
    <div className='space-y-6'>
      <div className='flex items-center justify-between'>
        <h1 className='text-3xl font-bold'>Suppliers</h1>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Supplier Management</CardTitle>
        </CardHeader>
        <CardContent>
          <p className='text-muted-foreground'>
            Manage your supplier network and relationships
          </p>
        </CardContent>
      </Card>
    </div>
  );
}
