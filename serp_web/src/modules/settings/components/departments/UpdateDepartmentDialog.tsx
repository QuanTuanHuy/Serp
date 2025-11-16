/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Update Department Dialog Component
 */

'use client';

import React, { useState, useEffect } from 'react';
import { Loader2 } from 'lucide-react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui/dialog';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import { Textarea } from '@/shared/components/ui/textarea';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { Switch } from '@/shared/components/ui/switch';
import { useNotification } from '@/shared/hooks';
import { isSuccessResponse } from '@/lib/store/api/utils';
import { useUpdateDepartmentMutation } from '@/modules/settings';
import type { UpdateDepartmentRequest, Department } from '@/modules/settings';

interface UpdateDepartmentDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  organizationId: number;
  department: Department | null;
  departments?: Department[];
  managers?: Array<{ id: number; name: string }>;
}

export const UpdateDepartmentDialog: React.FC<UpdateDepartmentDialogProps> = ({
  open,
  onOpenChange,
  organizationId,
  department,
  departments = [],
  managers = [],
}) => {
  const notification = useNotification();
  const [updateDepartment, { isLoading }] = useUpdateDepartmentMutation();

  const [formData, setFormData] = useState<UpdateDepartmentRequest>({
    name: '',
    description: '',
    parentDepartmentId: undefined,
    managerId: undefined,
    isActive: true,
  });

  useEffect(() => {
    if (department) {
      setFormData({
        name: department.name,
        description: department.description || '',
        parentDepartmentId: department.parentDepartmentId,
        managerId: department.managerId,
        isActive: department.isActive,
      });
    }
  }, [department]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!department) return;

    if (!formData.name?.trim()) {
      notification.error('Validation Error', {
        description: 'Department name is required',
      });
      return;
    }

    try {
      const result = await updateDepartment({
        organizationId,
        departmentId: department.id,
        body: formData,
      }).unwrap();

      if (isSuccessResponse(result)) {
        notification.success('Success', {
          description: 'Department updated successfully',
        });
        onOpenChange(false);
      } else {
        notification.error('Error', {
          description: result.message || 'Failed to update department',
        });
      }
    } catch (error: any) {
      notification.error('Error', {
        description: error?.data?.message || 'Failed to update department',
      });
    }
  };

  const availableDepartments = departments.filter(
    (d) => d.id !== department?.id
  );

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-[500px] !max-w-3xl'>
        <DialogHeader>
          <DialogTitle>Update Department</DialogTitle>
          <DialogDescription>
            Update the department information
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className='space-y-4'>
          <div className='space-y-2'>
            <Label htmlFor='name'>
              Department Name <span className='text-red-500'>*</span>
            </Label>
            <Input
              id='name'
              placeholder='e.g., Engineering, Sales'
              value={formData.name}
              onChange={(e) =>
                setFormData({ ...formData, name: e.target.value })
              }
              required
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='description'>Description</Label>
            <Textarea
              id='description'
              placeholder='Brief description of the department'
              value={formData.description}
              onChange={(e) =>
                setFormData({ ...formData, description: e.target.value })
              }
              rows={3}
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='parentDepartment'>Parent Department</Label>
            <Select
              value={formData.parentDepartmentId?.toString() || 'none'}
              onValueChange={(value) =>
                setFormData({
                  ...formData,
                  parentDepartmentId:
                    value === 'none' ? undefined : Number(value),
                })
              }
            >
              <SelectTrigger>
                <SelectValue placeholder='Select parent department (optional)' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='none'>None (Top Level)</SelectItem>
                {availableDepartments.map((dept) => (
                  <SelectItem key={dept.id} value={dept.id.toString()}>
                    {dept.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className='space-y-2'>
            <Label htmlFor='manager'>Manager</Label>
            <Select
              value={formData.managerId?.toString() || 'none'}
              onValueChange={(value) =>
                setFormData({
                  ...formData,
                  managerId: value === 'none' ? undefined : Number(value),
                })
              }
            >
              <SelectTrigger>
                <SelectValue placeholder='Assign a manager (optional)' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='none'>No Manager</SelectItem>
                {managers.map((manager) => (
                  <SelectItem key={manager.id} value={manager.id.toString()}>
                    {manager.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className='flex items-center justify-between'>
            <div className='space-y-0.5'>
              <Label htmlFor='isActive'>Active Status</Label>
              <p className='text-sm text-muted-foreground'>
                Inactive departments are hidden from most views
              </p>
            </div>
            <Switch
              id='isActive'
              checked={formData.isActive}
              onCheckedChange={(checked) =>
                setFormData({ ...formData, isActive: checked })
              }
            />
          </div>

          <DialogFooter>
            <Button
              type='button'
              variant='outline'
              onClick={() => onOpenChange(false)}
              disabled={isLoading}
            >
              Cancel
            </Button>
            <Button
              type='submit'
              className='bg-purple-600 hover:bg-purple-700'
              disabled={isLoading}
            >
              {isLoading && <Loader2 className='mr-2 h-4 w-4 animate-spin' />}
              Submit
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
