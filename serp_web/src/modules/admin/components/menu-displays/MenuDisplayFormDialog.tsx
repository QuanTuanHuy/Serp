/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Menu Display Form Dialog
 */

'use client';

import React, { useEffect } from 'react';
import { useForm, Controller } from 'react-hook-form';
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
import { Loader2 } from 'lucide-react';
import { IconPicker } from './IconPicker';
import { useGetModulesQuery } from '../../services/adminApi';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { Switch } from '@/shared/components/ui/switch';
import { Textarea } from '@/shared/components/ui/textarea';
import type {
  MenuDisplayDetail,
  CreateMenuDisplayRequest,
  UpdateMenuDisplayRequest,
} from '../../types';

interface MenuDisplayFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  menuDisplay?: MenuDisplayDetail | null;
  onSubmit: (
    data: CreateMenuDisplayRequest | UpdateMenuDisplayRequest
  ) => Promise<void>;
  isLoading?: boolean;
  allMenuDisplays?: MenuDisplayDetail[];
}

type FormData = {
  name: string;
  path: string;
  icon?: string;
  order: number;
  parentId?: number;
  moduleId: number;
  menuType?: string;
  isVisible?: boolean;
  description?: string;
};

export const MenuDisplayFormDialog: React.FC<MenuDisplayFormDialogProps> = ({
  open,
  onOpenChange,
  menuDisplay,
  onSubmit,
  isLoading = false,
  allMenuDisplays = [],
}) => {
  const isEditing = !!menuDisplay;

  const {
    register,
    handleSubmit,
    control,
    formState: { errors },
    reset,
    watch,
    setValue,
  } = useForm<FormData>({
    defaultValues: {
      name: '',
      path: '',
      icon: undefined,
      order: 0,
      parentId: undefined,
      moduleId: 0,
      menuType: 'SIDEBAR',
      isVisible: true,
      description: '',
    },
  });

  const { data: modules = [], isLoading: isLoadingModules } =
    useGetModulesQuery();

  const selectedModuleId = watch('moduleId');

  // Filter parent menu options based on selected module
  const availableParentMenus = allMenuDisplays.filter((menu) => {
    // Don't show current menu as parent option when editing
    if (isEditing && menu.id === menuDisplay?.id) {
      return false;
    }
    // Only show menus from the same module
    return menu.moduleId === selectedModuleId;
  });

  // Reset form when dialog opens/closes or menuDisplay changes
  useEffect(() => {
    if (open) {
      if (menuDisplay) {
        reset({
          name: menuDisplay.name,
          path: menuDisplay.path,
          icon: menuDisplay.icon || undefined,
          order: menuDisplay.order,
          parentId: menuDisplay.parentId || undefined,
          moduleId: menuDisplay.moduleId,
          menuType: menuDisplay.menuType || 'SIDEBAR',
          isVisible: menuDisplay.isVisible ?? true,
          description: menuDisplay.description || '',
        });
      } else {
        reset({
          name: '',
          path: '',
          icon: undefined,
          order: 0,
          parentId: undefined,
          moduleId: modules[0]?.id || 0,
          menuType: 'SIDEBAR',
          isVisible: true,
          description: '',
        });
      }
    }
  }, [open, menuDisplay, reset, modules]);

  const handleFormSubmit = async (data: FormData) => {
    try {
      if (isEditing) {
        // For update, only send updatable fields
        const updateData: UpdateMenuDisplayRequest = {
          name: data.name,
          path: data.path,
          icon: data.icon,
          order: data.order,
          isVisible: data.isVisible,
          description: data.description,
        };
        await onSubmit(updateData);
      } else {
        // For create, send all required fields
        const createData: CreateMenuDisplayRequest = {
          name: data.name,
          path: data.path,
          icon: data.icon,
          order: data.order,
          parentId: data.parentId,
          moduleId: data.moduleId,
          menuType: data.menuType as any,
          isVisible: data.isVisible,
          description: data.description,
        };
        await onSubmit(createData);
      }
    } catch (error) {
      // Error handling is done in the parent component
      console.error('Form submission error:', error);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='!max-w-5xl max-h-[90vh] overflow-y-auto'>
        <DialogHeader>
          <DialogTitle>
            {isEditing ? 'Edit Menu Display' : 'Create Menu Display'}
          </DialogTitle>
          <DialogDescription>
            {isEditing
              ? 'Update the menu display information below.'
              : 'Fill in the details to create a new menu display.'}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(handleFormSubmit)} className='space-y-4'>
          <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
            {/* Name */}
            <div className='space-y-2'>
              <Label htmlFor='name'>
                Name <span className='text-destructive'>*</span>
              </Label>
              <Input
                id='name'
                {...register('name', { required: 'Name is required' })}
                placeholder='e.g., Dashboard, Users, Settings'
              />
              {errors.name && (
                <p className='text-sm text-destructive'>
                  {errors.name.message}
                </p>
              )}
            </div>

            {/* Path */}
            <div className='space-y-2'>
              <Label htmlFor='path'>
                Path <span className='text-destructive'>*</span>
              </Label>
              <Input
                id='path'
                {...register('path', { required: 'Path is required' })}
                placeholder='e.g., /dashboard, /users'
              />
              {errors.path && (
                <p className='text-sm text-destructive'>
                  {errors.path.message}
                </p>
              )}
            </div>
          </div>

          <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
            {/* Icon */}
            <div className='space-y-2'>
              <Label htmlFor='icon'>Icon</Label>
              <Controller
                name='icon'
                control={control}
                render={({ field }) => (
                  <IconPicker value={field.value} onChange={field.onChange} />
                )}
              />
            </div>

            {/* Order */}
            <div className='space-y-2'>
              <Label htmlFor='order'>
                Display Order <span className='text-destructive'>*</span>
              </Label>
              <Input
                id='order'
                type='number'
                {...register('order', {
                  required: 'Order is required',
                  min: { value: 0, message: 'Order must be >= 0' },
                  valueAsNumber: true,
                })}
                placeholder='0'
              />
              {errors.order && (
                <p className='text-sm text-destructive'>
                  {errors.order.message}
                </p>
              )}
            </div>
          </div>

          {/* Menu Type */}
          <div className='space-y-2'>
            <Label htmlFor='menuType'>
              Menu Type <span className='text-destructive'>*</span>
            </Label>
            <Controller
              name='menuType'
              control={control}
              rules={{ required: 'Menu type is required' }}
              render={({ field }) => (
                <Select value={field.value} onValueChange={field.onChange}>
                  <SelectTrigger>
                    <SelectValue placeholder='Select menu type' />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value='SIDEBAR'>Sidebar</SelectItem>
                    <SelectItem value='TOPBAR'>Topbar</SelectItem>
                    <SelectItem value='DROPDOWN'>Dropdown</SelectItem>
                    <SelectItem value='ACTION'>Action</SelectItem>
                  </SelectContent>
                </Select>
              )}
            />
            {errors.menuType && (
              <p className='text-sm text-destructive'>
                {errors.menuType.message}
              </p>
            )}
          </div>

          {/* Is Visible */}
          <div className='flex items-center justify-between space-y-2'>
            <div className='space-y-0.5'>
              <Label htmlFor='isVisible'>Visible</Label>
              <p className='text-xs text-muted-foreground'>
                Make this menu item visible to users
              </p>
            </div>
            <Controller
              name='isVisible'
              control={control}
              render={({ field }) => (
                <Switch
                  id='isVisible'
                  checked={field.value}
                  onCheckedChange={field.onChange}
                />
              )}
            />
          </div>

          {/* Description */}
          <div className='space-y-2'>
            <Label htmlFor='description'>Description (Optional)</Label>
            <Textarea
              id='description'
              {...register('description')}
              placeholder='Enter a description for this menu item...'
              rows={3}
            />
            <p className='text-xs text-muted-foreground'>
              Provide additional context about this menu item
            </p>
          </div>

          {/* Module - Only shown when creating */}
          {!isEditing && (
            <div className='space-y-2'>
              <Label htmlFor='moduleId'>
                Module <span className='text-destructive'>*</span>
              </Label>
              <Controller
                name='moduleId'
                control={control}
                rules={{ required: 'Module is required' }}
                render={({ field }) => (
                  <Select
                    value={field.value?.toString()}
                    onValueChange={(value) => {
                      field.onChange(parseInt(value));
                      // Reset parent when module changes
                      setValue('parentId', undefined);
                    }}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder='Select a module' />
                    </SelectTrigger>
                    <SelectContent>
                      {isLoadingModules ? (
                        <div className='p-2 text-sm text-muted-foreground text-center'>
                          Loading modules...
                        </div>
                      ) : modules.length === 0 ? (
                        <div className='p-2 text-sm text-muted-foreground text-center'>
                          No modules available
                        </div>
                      ) : (
                        modules.map((module) => (
                          <SelectItem
                            key={module.id}
                            value={module.id.toString()}
                          >
                            {module.code}
                          </SelectItem>
                        ))
                      )}
                    </SelectContent>
                  </Select>
                )}
              />
              {errors.moduleId && (
                <p className='text-sm text-destructive'>
                  {errors.moduleId.message}
                </p>
              )}
            </div>
          )}

          {/* Parent Menu - Only shown when creating */}
          {!isEditing && (
            <div className='space-y-2'>
              <Label htmlFor='parentId'>Parent Menu (Optional)</Label>
              <Controller
                name='parentId'
                control={control}
                render={({ field }) => (
                  <Select
                    value={field.value?.toString() || 'none'}
                    onValueChange={(value) => {
                      field.onChange(
                        value === 'none' ? undefined : parseInt(value)
                      );
                    }}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder='No parent (root level)' />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value='none'>
                        No parent (root level)
                      </SelectItem>
                      {availableParentMenus.map((menu) => (
                        <SelectItem key={menu.id} value={menu.id!.toString()}>
                          {menu.name} ({menu.path})
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                )}
              />
              <p className='text-xs text-muted-foreground'>
                Select a parent menu to create a submenu
              </p>
            </div>
          )}

          <DialogFooter>
            <Button
              type='button'
              variant='outline'
              onClick={() => onOpenChange(false)}
              disabled={isLoading}
            >
              Cancel
            </Button>
            <Button type='submit' disabled={isLoading}>
              {isLoading && <Loader2 className='mr-2 h-4 w-4 animate-spin' />}
              {isEditing ? 'Update' : 'Create'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
