/**
 * Purchase Products Page
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Product catalog management
 */

'use client';

import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components';

export default function PurchaseProductsPage() {
  return (
    <div className='space-y-6'>
      <div className='flex items-center justify-between'>
        <h1 className='text-3xl font-bold'>Products</h1>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Product Catalog</CardTitle>
        </CardHeader>
        <CardContent>
          <p className='text-muted-foreground'>
            Manage your product catalog and pricing
          </p>
        </CardContent>
      </Card>
    </div>
  );
}
