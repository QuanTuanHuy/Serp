/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Dispatcher access scope card
 */

import React from 'react';
import {
  Badge,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';
import { Route } from 'lucide-react';
import type { AccessScopeCardProps } from './types';

export const DispatchAccessScopeCard: React.FC<AccessScopeCardProps> = ({
  canDispatch,
  scopeBadgeLabel,
  scopeDescription,
}) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle className='flex items-center gap-2'>
          <Route className='h-5 w-5' />
          Access Scope
        </CardTitle>
        <CardDescription>
          Dispatch permissions are validated by your role and backend scope.
        </CardDescription>
      </CardHeader>
      <CardContent className='space-y-2'>
        <Badge
          variant={canDispatch ? 'secondary' : 'destructive'}
          className='w-fit'
        >
          {scopeBadgeLabel}
        </Badge>
        <p className='text-sm text-muted-foreground'>{scopeDescription}</p>
      </CardContent>
    </Card>
  );
};
