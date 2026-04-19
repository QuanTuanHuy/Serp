/**
 * Author: GitHub Copilot
 * Description: Part of Serp Project - Dispatcher no-access card
 */

import React from 'react';
import { Card, CardContent } from '@/shared/components/ui';
import { ShieldAlert } from 'lucide-react';
import type { NoAccessCardProps } from './types';

export const DispatchNoAccessCard: React.FC<NoAccessCardProps> = ({
  message,
}) => {
  return (
    <Card>
      <CardContent className='pt-6'>
        <div className='flex items-center gap-2 text-muted-foreground'>
          <ShieldAlert className='h-4 w-4' />
          {message}
        </div>
      </CardContent>
    </Card>
  );
};
