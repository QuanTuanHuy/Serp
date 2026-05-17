/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item board empty state
 */

import { Columns3 } from 'lucide-react';
import { Card, CardContent } from '@/shared/components/ui';

interface PMWorkItemBoardEmptyProps {
  title: string;
  description: string;
}

export function PMWorkItemBoardEmpty({
  title,
  description,
}: PMWorkItemBoardEmptyProps) {
  return (
    <Card className='rounded-2xl border-dashed border-border/70 bg-muted/10 shadow-sm'>
      <CardContent className='flex min-h-[18rem] flex-col items-center justify-center px-6 py-10 text-center sm:min-h-[22rem]'>
        <div className='mb-4 rounded-2xl border border-border/60 bg-background/80 p-4 text-muted-foreground'>
          <Columns3 className='h-8 w-8' />
        </div>
        <h2 className='text-lg font-semibold text-foreground'>{title}</h2>
        <p className='mt-2 max-w-md text-sm leading-6 text-muted-foreground'>
          {description}
        </p>
      </CardContent>
    </Card>
  );
}
