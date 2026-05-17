/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Vehicles scope selection
 */

import Link from 'next/link';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';

export default function VehiclesScopePage() {
  return (
    <div className='mx-auto max-w-2xl space-y-6 py-8'>
      <div>
        <h1 className='text-2xl font-bold tracking-tight'>Vehicles</h1>
        <p className='text-muted-foreground'>
          Choose the logistics scope to manage vehicles.
        </p>
      </div>
      <div className='grid gap-4 sm:grid-cols-2'>
        <Link href='/first-mile/vehicles/first-mile'>
          <Card className='transition-colors hover:border-primary'>
            <CardHeader>
              <CardTitle>First-mile</CardTitle>
              <CardDescription>
                Post office and courier scoped vehicles (first-mile service).
              </CardDescription>
            </CardHeader>
            <CardContent className='text-sm text-primary'>Open →</CardContent>
          </Card>
        </Link>
        <Link href='/first-mile/vehicles/second-mile'>
          <Card className='transition-colors hover:border-primary'>
            <CardHeader>
              <CardTitle>Second-mile</CardTitle>
              <CardDescription>
                Hub and driver scoped vehicles (second-mile service).
              </CardDescription>
            </CardHeader>
            <CardContent className='text-sm text-primary'>Open →</CardContent>
          </Card>
        </Link>
      </div>
    </div>
  );
}
