/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - CRM Analytics Route
 */

import Link from 'next/link';
import { BarChart3 } from 'lucide-react';
import { Button, Card, CardContent } from '@/shared/components/ui';

export default function AnalyticsPage() {
  return (
    <div className='flex min-h-[50vh] items-center justify-center'>
      <Card className='max-w-lg'>
        <CardContent className='space-y-4 py-10 text-center'>
          <div className='mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-muted text-muted-foreground'>
            <BarChart3 className='h-6 w-6' />
          </div>
          <div>
            <h1 className='text-xl font-semibold'>Analytics unavailable</h1>
            <p className='mt-2 text-sm text-muted-foreground'>
              CRM analytics needs backend endpoints before this page can show
              live metrics.
            </p>
          </div>
          <Button asChild>
            <Link href='/crm/dashboard'>Back to dashboard</Link>
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
