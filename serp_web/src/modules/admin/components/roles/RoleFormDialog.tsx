/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Role Form Dialog Component
 */

'use client';

import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui';
import { RoleForm } from './RoleForm';
import type { Role } from '../../types';

interface RoleFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  role?: Role;
  onSubmit: (data: any) => Promise<void>;
  isLoading?: boolean;
}

export const RoleFormDialog: React.FC<RoleFormDialogProps> = ({
  open,
  onOpenChange,
  role,
  onSubmit,
  isLoading = false,
}) => {
  const handleSuccess = async (data: any) => {
    await onSubmit(data);
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-w-4xl max-h-[90vh] overflow-y-auto'>
        <DialogHeader>
          <DialogTitle>{role ? 'Edit Role' : 'Create New Role'}</DialogTitle>
        </DialogHeader>

        <RoleForm
          role={role}
          onSubmit={handleSuccess}
          onCancel={() => onOpenChange(false)}
          isLoading={isLoading}
        />
      </DialogContent>
    </Dialog>
  );
};
