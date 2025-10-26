/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Module Form Dialog Component
 */

'use client';

import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui';
import { ModuleForm } from './ModuleForm';
import type { Module } from '../../types';

interface ModuleFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  module?: Module;
  onSubmit: (
    data: Omit<Module, 'id' | 'createdAt' | 'updatedAt'>
  ) => Promise<void>;
  isLoading?: boolean;
}

export const ModuleFormDialog: React.FC<ModuleFormDialogProps> = ({
  open,
  onOpenChange,
  module,
  onSubmit,
  isLoading = false,
}) => {
  const handleSuccess = async (data: any) => {
    await onSubmit(data);
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-h-[90vh] overflow-y-auto sm:max-w-3xl md:max-w-4xl lg:max-w-5xl xl:max-w-6xl'>
        <DialogHeader>
          <DialogTitle>
            {module ? 'Edit Module' : 'Create New Module'}
          </DialogTitle>
        </DialogHeader>

        <ModuleForm
          module={module}
          onSubmit={handleSuccess}
          onCancel={() => onOpenChange(false)}
          isLoading={isLoading}
        />
      </DialogContent>
    </Dialog>
  );
};
