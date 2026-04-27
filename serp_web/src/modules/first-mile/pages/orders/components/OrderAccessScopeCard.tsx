/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Order access scope card
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
import { Shield } from 'lucide-react';

interface OrderAccessScopeCardProps {
  canViewOrders: boolean;
  badgeLabel: string;
  description: string;
}

export const OrderAccessScopeCard: React.FC<OrderAccessScopeCardProps> = ({
  canViewOrders,
  badgeLabel,
  description,
}) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle className='flex items-center gap-2'>
          <Shield className='h-5 w-5' />
          Access Scope
        </CardTitle>
        <CardDescription>
          The system automatically limits visible orders by your current role.
        </CardDescription>
      </CardHeader>
      <CardContent className='space-y-2'>
        <Badge
          variant={canViewOrders ? 'secondary' : 'destructive'}
          className='w-fit'
        >
          {badgeLabel}
        </Badge>
        <p className='text-sm text-muted-foreground'>{description}</p>
      </CardContent>
    </Card>
  );
};
