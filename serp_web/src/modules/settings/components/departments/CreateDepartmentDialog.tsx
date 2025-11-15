/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Create Department Dialog Component
 */

'use client';

import React, { useState } from 'react';
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
import { Checkbox } from '@/shared/components/ui/checkbox';
import { ScrollArea } from '@/shared/components/ui/scroll-area';
import type { CreateDepartmentRequest } from '../../types';
import type { AccessibleModule } from '../../types/module-access.types';

interface CreateDepartmentDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (data: CreateDepartmentRequest) => Promise<any>;
  isLoading: boolean;
  departments: Array<{ id: number; name: string }>;
  managers: Array<{ id: number; name: string; email?: string }>;
  modules: AccessibleModule[];
}

export const CreateDepartmentDialog: React.FC<CreateDepartmentDialogProps> = ({
  open,
  onOpenChange,
  onSubmit,
  isLoading,
  departments,
  managers,
  modules,
}) => {
  const [formData, setFormData] = useState<CreateDepartmentRequest>({
    name: '',
    description: '',
    parentDepartmentId: undefined,
    managerId: undefined,
    defaultModuleIds: [],
  });

  const [errors, setErrors] = useState<Record<string, string>>({});

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.name?.trim()) {
      newErrors.name = 'Department name is required';
    } else if (formData.name.length < 2) {
      newErrors.name = 'Department name must be at least 2 characters';
    } else if (formData.name.length > 100) {
      newErrors.name = 'Department name must not exceed 100 characters';
    }

    if (formData.description && formData.description.length > 500) {
      newErrors.description = 'Description must not exceed 500 characters';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validate()) return;

    try {
      await onSubmit(formData);
      handleClose();
    } catch (error) {
      // Error handled in parent
    }
  };

  const handleClose = () => {
    setFormData({
      name: '',
      description: '',
      parentDepartmentId: undefined,
      managerId: undefined,
      defaultModuleIds: [],
    });
    setErrors({});
    onOpenChange(false);
  };

  const toggleModule = (moduleId: number) => {
    setFormData((prev) => ({
      ...prev,
      defaultModuleIds: prev.defaultModuleIds?.includes(moduleId)
        ? prev.defaultModuleIds.filter((id) => id !== moduleId)
        : [...(prev.defaultModuleIds || []), moduleId],
    }));
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-[600px] max-h-[90vh]'>
        <DialogHeader>
          <DialogTitle>Create New Department</DialogTitle>
          <DialogDescription>
            Add a new department to your organization structure
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className='space-y-5'>
          <ScrollArea className='max-h-[calc(90vh-200px)] pr-4'>
            <div className='space-y-5'>
              {/* Name */}
              <div className='space-y-2'>
                <Label htmlFor='name'>
                  Department Name <span className='text-red-500'>*</span>
                </Label>
                <Input
                  id='name'
                  placeholder='e.g., Engineering, Sales, Marketing'
                  value={formData.name}
                  onChange={(e) => {
                    setFormData({ ...formData, name: e.target.value });
                    if (errors.name) {
                      setErrors((prev) => ({ ...prev, name: '' }));
                    }
                  }}
                  className={errors.name ? 'border-red-500' : ''}
                />
                {errors.name && (
                  <p className='text-sm text-red-500'>{errors.name}</p>
                )}
              </div>

              {/* Description */}
              <div className='space-y-2'>
                <Label htmlFor='description'>Description</Label>
                <Textarea
                  id='description'
                  placeholder='Brief description of the department'
                  value={formData.description}
                  onChange={(e) => {
                    setFormData({ ...formData, description: e.target.value });
                    if (errors.description) {
                      setErrors((prev) => ({ ...prev, description: '' }));
                    }
                  }}
                  rows={3}
                  className={errors.description ? 'border-red-500' : ''}
                />
                {errors.description && (
                  <p className='text-sm text-red-500'>{errors.description}</p>
                )}
                <p className='text-xs text-muted-foreground'>
                  {formData.description?.length || 0}/500 characters
                </p>
              </div>

              {/* Parent Department */}
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
                    {departments.map((dept) => (
                      <SelectItem key={dept.id} value={dept.id.toString()}>
                        {dept.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              {/* Manager */}
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
                      <SelectItem
                        key={manager.id}
                        value={manager.id.toString()}
                      >
                        <div>
                          <p>{manager.name}</p>
                          {manager.email && (
                            <p className='text-xs text-muted-foreground'>
                              {manager.email}
                            </p>
                          )}
                        </div>
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              {/* Default Modules */}
              {modules.length > 0 && (
                <div className='space-y-3'>
                  <Label>Default Modules Access</Label>
                  <p className='text-sm text-muted-foreground'>
                    Select modules that members of this department will have
                    access to by default
                  </p>
                  <div className='border rounded-lg p-3 space-y-2 max-h-[200px] overflow-y-auto'>
                    {modules.map((module) => (
                      <div
                        key={module.moduleId}
                        className='flex items-center space-x-3'
                      >
                        <Checkbox
                          id={`module-${module.moduleId}`}
                          checked={formData.defaultModuleIds?.includes(
                            module.moduleId!
                          )}
                          onCheckedChange={() => toggleModule(module.moduleId!)}
                        />
                        <Label
                          htmlFor={`module-${module.moduleId}`}
                          className='flex-1 cursor-pointer'
                        >
                          <div>
                            <p className='text-sm font-medium'>
                              {module.moduleName}
                            </p>
                            {module.moduleDescription && (
                              <p className='text-xs text-muted-foreground'>
                                {module.moduleDescription}
                              </p>
                            )}
                          </div>
                        </Label>
                      </div>
                    ))}
                  </div>
                  <p className='text-xs text-muted-foreground'>
                    {formData.defaultModuleIds?.length || 0} module(s) selected
                  </p>
                </div>
              )}
            </div>
          </ScrollArea>

          <DialogFooter>
            <Button
              type='button'
              variant='outline'
              onClick={handleClose}
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
