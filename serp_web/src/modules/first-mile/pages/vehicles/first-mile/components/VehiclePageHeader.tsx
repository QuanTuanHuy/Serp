/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Vehicle page header
 */

import React from 'react';
import { Badge, Button } from '@/shared/components/ui';
import { Plus, ShieldAlert } from 'lucide-react';

interface VehiclePageHeaderProps {
  canManageVehicles: boolean;
  importAction?: React.ReactNode;
  onCreateVehicle: () => void;
}

export const VehiclePageHeader: React.FC<VehiclePageHeaderProps> = ({
  canManageVehicles,
  importAction,
  onCreateVehicle,
}) => {
  return (
    <div className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between'>
      <div className='flex flex-col gap-2'>
        <h1 className='text-2xl font-bold tracking-tight'>Vehicles</h1>
        <p className='text-muted-foreground'>
          Track, inspect, and manage first-mile vehicles based on backend
          permissions.
        </p>
      </div>

      {canManageVehicles ? (
        <div className='flex flex-wrap items-center gap-2'>
          {importAction}
          <Button onClick={onCreateVehicle}>
            <Plus className='mr-2 h-4 w-4' />
            New Vehicle
          </Button>
        </div>
      ) : (
        <Badge variant='outline' className='gap-1'>
          <ShieldAlert className='h-3.5 w-3.5' />
          View only (write actions require admin or manager role)
        </Badge>
      )}
    </div>
  );
};
