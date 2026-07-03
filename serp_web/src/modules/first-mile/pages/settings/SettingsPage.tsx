/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - TMS settings overview page
 */

import Link from 'next/link';
import { ArrowRight, Package, ReceiptText, Truck } from 'lucide-react';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';

export function TmsSettingsPage() {
  return (
    <div className='space-y-6'>
      <div>
        <h1 className='text-2xl font-bold tracking-tight'>Settings</h1>
        <p className='text-muted-foreground'>
          Manage shared TMS configuration and operational master data.
        </p>
      </div>

      <div className='grid gap-4 md:grid-cols-2 xl:grid-cols-3'>
        <Link href='/first-mile/settings/billing'>
          <Card className='h-full transition-colors hover:border-primary'>
            <CardHeader className='space-y-3'>
              <div className='flex h-10 w-10 items-center justify-center rounded-md bg-primary/10 text-primary'>
                <ReceiptText className='h-5 w-5' />
              </div>
              <div>
                <CardTitle>Billing</CardTitle>
                <CardDescription>
                  Manage shipping fee calculation, tariffs, surcharges, and
                  value-added service pricing.
                </CardDescription>
              </div>
            </CardHeader>
            <CardContent className='flex items-center gap-2 text-sm font-medium text-primary'>
              Open billing
              <ArrowRight className='h-4 w-4' />
            </CardContent>
          </Card>
        </Link>

        <Link href='/first-mile/settings/vehicles'>
          <Card className='h-full transition-colors hover:border-primary'>
            <CardHeader className='space-y-3'>
              <div className='flex h-10 w-10 items-center justify-center rounded-md bg-primary/10 text-primary'>
                <Truck className='h-5 w-5' />
              </div>
              <div>
                <CardTitle>Vehicles</CardTitle>
                <CardDescription>
                  Manage first-mile and second-mile vehicle records in one
                  place.
                </CardDescription>
              </div>
            </CardHeader>
            <CardContent className='flex items-center gap-2 text-sm font-medium text-primary'>
              Open vehicles
              <ArrowRight className='h-4 w-4' />
            </CardContent>
          </Card>
        </Link>

        <Link href='/first-mile/settings/product-types'>
          <Card className='h-full transition-colors hover:border-primary'>
            <CardHeader className='space-y-3'>
              <div className='flex h-10 w-10 items-center justify-center rounded-md bg-primary/10 text-primary'>
                <Package className='h-5 w-5' />
              </div>
              <div>
                <CardTitle>Product Types</CardTitle>
                <CardDescription>
                  Configure product classifications used by TMS orders and
                  imports.
                </CardDescription>
              </div>
            </CardHeader>
            <CardContent className='flex items-center gap-2 text-sm font-medium text-primary'>
              Open product types
              <ArrowRight className='h-4 w-4' />
            </CardContent>
          </Card>
        </Link>
      </div>
    </div>
  );
}
