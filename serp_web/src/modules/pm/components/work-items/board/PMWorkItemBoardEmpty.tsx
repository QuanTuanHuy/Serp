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
    <Card className='border-dashed'>
      <CardContent className='flex min-h-[22rem] flex-col items-center justify-center px-6 text-center'>
        <div className='mb-4 rounded-2xl bg-muted p-4 text-muted-foreground'>
          <Columns3 className='h-8 w-8' />
        </div>
        <h2 className='text-lg font-semibold text-foreground'>{title}</h2>
        <p className='mt-2 max-w-md text-sm text-muted-foreground'>
          {description}
        </p>
      </CardContent>
    </Card>
  );
}
