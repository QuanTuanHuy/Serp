/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Vehicle access scope card
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
import {
  getScopeBadgeLabel,
  getScopeDescription,
  type VehicleAccessScope,
} from '../vehiclePageModels';

interface VehicleAccessScopeCardProps {
  accessScope: VehicleAccessScope;
  canViewVehicles: boolean;
}

export const VehicleAccessScopeCard: React.FC<VehicleAccessScopeCardProps> = ({
  accessScope,
  canViewVehicles,
}) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle className='flex items-center gap-2'>
          <Shield className='h-5 w-5' />
          Access Scope
        </CardTitle>
        <CardDescription>
          Vehicle visibility and actions are aligned with backend role
          authorization.
        </CardDescription>
      </CardHeader>
      <CardContent className='space-y-2'>
        <Badge
          variant={canViewVehicles ? 'secondary' : 'destructive'}
          className='w-fit'
        >
          {getScopeBadgeLabel(accessScope)}
        </Badge>
        <p className='text-sm text-muted-foreground'>
          {getScopeDescription(accessScope)}
        </p>
      </CardContent>
    </Card>
  );
};
