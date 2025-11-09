/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Request More Modules Dialog
 */

'use client';

import React from 'react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui/dialog';
import { Button } from '@/shared/components/ui/button';
import { Badge } from '@/shared/components/ui/badge';
import { Card } from '@/shared/components/ui/card';
import {
  Package,
  AlertCircle,
  Loader2,
  CheckCircle2,
  Sparkles,
} from 'lucide-react';
import type { AccessibleModule } from '@/modules/settings/types/module-access.types';
import { MODULE_ICONS } from '@/shared/constants/moduleIcons';

interface RequestModuleDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  module: AccessibleModule | undefined;
  onConfirm: () => void;
  isLoading?: boolean;
  success?: boolean;
}

export const RequestModuleDialog: React.FC<RequestModuleDialogProps> = ({
  open,
  onOpenChange,
  module,
  onConfirm,
  isLoading = false,
}) => {
  if (!module) return null;

  const getModuleIcon = (moduleCode: string) => {
    const iconConfig = MODULE_ICONS[moduleCode as keyof typeof MODULE_ICONS];
    if (!iconConfig) return <Package className='h-6 w-6' />;
    const Icon = iconConfig.icon;
    return <Icon className='h-6 w-6' />;
  };

  const getModuleColor = (moduleCode: string) => {
    const iconConfig = MODULE_ICONS[moduleCode as keyof typeof MODULE_ICONS];
    return iconConfig?.color || 'text-gray-600';
  };

  const getModuleBgColor = (moduleCode: string) => {
    const iconConfig = MODULE_ICONS[moduleCode as keyof typeof MODULE_ICONS];
    return iconConfig?.bgColor || 'bg-gray-50 dark:bg-gray-950';
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-[500px] !max-w-4xl'>
        <DialogHeader>
          <DialogTitle className='flex items-center gap-2'>
            <Sparkles className='h-5 w-5 text-purple-600' />
            Request Module Access
          </DialogTitle>
          <DialogDescription>
            Submit a request to enable this module for your organization
          </DialogDescription>
        </DialogHeader>

        <div className='space-y-4 py-4'>
          {/* Module Info Card */}
          <div className='border rounded-lg p-4 bg-muted/30'>
            <div className='flex items-start gap-3'>
              <div
                className={`h-12 w-12 rounded-lg flex items-center justify-center flex-shrink-0 ${getModuleBgColor(
                  module.moduleCode
                )}`}
              >
                <span className={getModuleColor(module.moduleCode)}>
                  {getModuleIcon(module.moduleCode)}
                </span>
              </div>

              <div className='flex-1 min-w-0'>
                <div className='flex items-center gap-2 mb-1'>
                  <h3 className='font-semibold text-base'>
                    {module.moduleName}
                  </h3>
                  <Badge variant='outline' className='text-xs font-mono'>
                    {module.moduleCode}
                  </Badge>
                </div>
                {module.moduleDescription && (
                  <p className='text-sm text-muted-foreground'>
                    {module.moduleDescription}
                  </p>
                )}
              </div>
            </div>
          </div>

          {/* Info Alert */}
          <Card className='border-blue-200 bg-blue-50/50 dark:bg-blue-950/20 p-3'>
            <div className='flex gap-2'>
              <AlertCircle className='h-4 w-4 text-blue-600 mt-0.5 flex-shrink-0' />
              <p className='text-sm text-blue-900 dark:text-blue-100'>
                Your request will be sent to the system administrator for
                review. You'll be notified once the module is approved and
                activated for your organization.
              </p>
            </div>
          </Card>

          {/* Benefits List */}
          <div className='space-y-2'>
            <p className='text-sm font-medium text-muted-foreground'>
              What happens next:
            </p>
            <ul className='space-y-2 text-sm'>
              <li className='flex items-start gap-2'>
                <CheckCircle2 className='h-4 w-4 text-green-600 mt-0.5 flex-shrink-0' />
                <span>Administrator reviews your request</span>
              </li>
              <li className='flex items-start gap-2'>
                <CheckCircle2 className='h-4 w-4 text-green-600 mt-0.5 flex-shrink-0' />
                <span>Module is added to your subscription plan</span>
              </li>
              <li className='flex items-start gap-2'>
                <CheckCircle2 className='h-4 w-4 text-green-600 mt-0.5 flex-shrink-0' />
                <span>You can assign access to your team members</span>
              </li>
            </ul>
          </div>
        </div>

        <DialogFooter>
          <Button
            variant='outline'
            onClick={() => onOpenChange(false)}
            disabled={isLoading}
          >
            Cancel
          </Button>
          <Button
            onClick={onConfirm}
            disabled={isLoading}
            className='bg-purple-600 hover:bg-purple-700'
          >
            {isLoading && <Loader2 className='mr-2 h-4 w-4 animate-spin' />}
            Submit Request
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
