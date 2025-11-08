/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Plan Modules Management Dialog
 */

'use client';

import React, { useMemo, useState } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from '@/shared/components/ui/dialog';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Badge } from '@/shared/components/ui/badge';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { Label } from '@/shared/components/ui/label';
import {
  Package,
  Plus,
  Trash2,
  Search,
  CheckCircle,
  XCircle,
  Loader2,
  Shield,
} from 'lucide-react';
import type {
  SubscriptionPlan,
  PlanModule,
  Module,
  LicenseType,
  AddModuleToPlanRequest,
} from '../../types';
import { useNotification } from '@/shared/hooks/use-notification';
import { cn } from '@/shared/utils';

interface PlanModulesDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  plan: SubscriptionPlan;
  planModules: PlanModule[];
  allModules: Module[];
  isLoadingModules: boolean;
  onAddModule: (planId: string, data: AddModuleToPlanRequest) => Promise<void>;
  onRemoveModule: (planId: string, moduleId: number) => Promise<void>;
  isAdding?: boolean;
  isRemoving?: boolean;
}

const LICENSE_TYPES: { value: LicenseType; label: string; color: string }[] = [
  { value: 'FREE', label: 'Free', color: 'bg-green-100 text-green-800' },
  {
    value: 'BASIC',
    label: 'Basic',
    color: 'bg-blue-100 text-blue-800',
  },
  {
    value: 'PROFESSIONAL',
    label: 'Professional',
    color: 'bg-purple-100 text-purple-800',
  },
  {
    value: 'ENTERPRISE',
    label: 'Enterprise',
    color: 'bg-orange-100 text-orange-800',
  },
  { value: 'TRIAL', label: 'Trial', color: 'bg-yellow-100 text-yellow-800' },
  { value: 'CUSTOM', label: 'Custom', color: 'bg-pink-100 text-pink-800' },
];

export const PlanModulesDialog: React.FC<PlanModulesDialogProps> = ({
  open,
  onOpenChange,
  plan,
  planModules,
  allModules,
  isLoadingModules,
  onAddModule,
  onRemoveModule,
  isAdding = false,
  isRemoving = false,
}) => {
  const notification = useNotification();
  const [search, setSearch] = useState('');
  const [showAddForm, setShowAddForm] = useState(false);
  const [selectedModuleId, setSelectedModuleId] = useState<number | null>(null);
  const [licenseType, setLicenseType] = useState<LicenseType>('BASIC');
  const [maxUsersPerModule, setMaxUsersPerModule] = useState<string>('');

  const enrichedPlanModules = useMemo(() => {
    if (!planModules || planModules.length === 0) {
      return [];
    }

    return planModules.map((pm) => {
      const moduleDetails = allModules.find((m) => m.id === pm.moduleId);

      return {
        ...pm,
        moduleName:
          moduleDetails?.name || pm.moduleName || `Module #${pm.moduleId}`,
        moduleCode:
          moduleDetails?.code || pm.moduleCode || `MOD-${pm.moduleId}`,
      };
    });
  }, [planModules, allModules]);

  // Get modules that are not yet in the plan
  const availableModules = useMemo(() => {
    const planModuleIds = new Set(planModules.map((pm) => pm.moduleId));
    return allModules.filter((m) => !planModuleIds.has(m.id));
  }, [allModules, planModules]);

  // Filter modules based on search
  const filteredPlanModules = useMemo(() => {
    const term = search.toLowerCase().trim();
    if (!term) return enrichedPlanModules;

    return enrichedPlanModules.filter(
      (pm) =>
        pm.moduleName?.toLowerCase().includes(term) ||
        pm.moduleCode?.toLowerCase().includes(term) ||
        pm.licenseType.toLowerCase().includes(term)
    );
  }, [enrichedPlanModules, search]);

  const handleAddModule = async () => {
    if (!selectedModuleId) {
      notification.error('Please select a module');
      return;
    }

    try {
      const data: AddModuleToPlanRequest = {
        moduleId: selectedModuleId,
        licenseType,
        isIncluded: true,
        maxUsersPerModule: maxUsersPerModule
          ? parseInt(maxUsersPerModule)
          : undefined,
      };

      await onAddModule(String(plan.id), data);
      notification.success('Module added to plan successfully');

      // Reset form
      setSelectedModuleId(null);
      setLicenseType('BASIC');
      setMaxUsersPerModule('');
      setShowAddForm(false);
    } catch (error) {
      // Error handled in parent
    }
  };

  const handleRemoveModule = async (moduleId: number, moduleName: string) => {
    if (
      !confirm(
        `Are you sure you want to remove "${moduleName}" from this plan?`
      )
    ) {
      return;
    }

    try {
      await onRemoveModule(String(plan.id), moduleId);
      notification.success('Module removed from plan successfully');
    } catch (error) {
      // Error handled in parent
    }
  };

  const getLicenseColor = (license: LicenseType) => {
    return (
      LICENSE_TYPES.find((lt) => lt.value === license)?.color ||
      'bg-gray-100 text-gray-800'
    );
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='!max-w-5xl max-h-[90vh] overflow-y-auto'>
        <DialogHeader>
          <DialogTitle className='flex items-center gap-2'>
            <Package className='h-5 w-5' />
            Manage Modules for {plan.planName}
          </DialogTitle>
          <DialogDescription>
            Add or remove modules from this subscription plan
          </DialogDescription>
        </DialogHeader>

        <div className='flex flex-col gap-4'>
          {/* Search and Add Button */}
          <div className='flex items-center gap-2'>
            <div className='relative flex-1'>
              <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
              <Input
                placeholder='Search modules...'
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                className='pl-9'
              />
            </div>
            <Button
              size='sm'
              onClick={() => setShowAddForm(!showAddForm)}
              disabled={availableModules.length === 0}
            >
              <Plus className='h-4 w-4 mr-2' />
              Add Module
            </Button>
          </div>

          {/* Add Module Form */}
          {showAddForm && (
            <Card className='border-primary'>
              <CardHeader className='pb-3'>
                <CardTitle className='text-base'>Add New Module</CardTitle>
                <CardDescription>
                  Select a module and configure its settings
                </CardDescription>
              </CardHeader>
              <CardContent className='space-y-4'>
                <div className='grid gap-4 md:grid-cols-2'>
                  <div className='space-y-2'>
                    <Label htmlFor='module'>
                      Module <span className='text-destructive'>*</span>
                    </Label>
                    <Select
                      value={selectedModuleId?.toString() || ''}
                      onValueChange={(value) =>
                        setSelectedModuleId(parseInt(value))
                      }
                    >
                      <SelectTrigger id='module'>
                        <SelectValue placeholder='Select a module' />
                      </SelectTrigger>
                      <SelectContent>
                        {availableModules.map((module) => (
                          <SelectItem
                            key={module.id}
                            value={module.id.toString()}
                          >
                            <div className='flex items-center gap-2'>
                              <span>{module.name}</span>
                              <span className='text-xs text-muted-foreground'>
                                ({module.code})
                              </span>
                            </div>
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  <div className='space-y-2'>
                    <Label htmlFor='licenseType'>
                      License Type <span className='text-destructive'>*</span>
                    </Label>
                    <Select
                      value={licenseType}
                      onValueChange={(value) =>
                        setLicenseType(value as LicenseType)
                      }
                    >
                      <SelectTrigger id='licenseType'>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {LICENSE_TYPES.map((lt) => (
                          <SelectItem key={lt.value} value={lt.value}>
                            {lt.label}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='maxUsers'>Max Users Per Module</Label>
                  <Input
                    id='maxUsers'
                    type='number'
                    min='1'
                    placeholder='Leave empty to inherit from plan'
                    value={maxUsersPerModule}
                    onChange={(e) => setMaxUsersPerModule(e.target.value)}
                  />
                  <p className='text-xs text-muted-foreground'>
                    Leave empty to use the plan's max users limit
                  </p>
                </div>

                <div className='flex justify-end gap-2'>
                  <Button
                    variant='outline'
                    size='sm'
                    onClick={() => {
                      setShowAddForm(false);
                      setSelectedModuleId(null);
                      setLicenseType('BASIC');
                      setMaxUsersPerModule('');
                    }}
                  >
                    Cancel
                  </Button>
                  <Button
                    size='sm'
                    onClick={handleAddModule}
                    disabled={!selectedModuleId || isAdding}
                  >
                    {isAdding && (
                      <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                    )}
                    Add Module
                  </Button>
                </div>
              </CardContent>
            </Card>
          )}

          {/* Modules List */}
          <Card className='flex flex-col'>
            <CardHeader className='pb-3'>
              <CardTitle className='text-base flex items-center justify-between'>
                <span>
                  Plan Modules ({filteredPlanModules.length}/
                  {enrichedPlanModules.length})
                </span>
              </CardTitle>
            </CardHeader>
            <CardContent className='p-0'>
              <div className='px-6 pb-6'>
                {isLoadingModules ? (
                  <div className='flex items-center justify-center h-48'>
                    <div className='flex flex-col items-center gap-2'>
                      <Loader2 className='h-8 w-8 animate-spin text-muted-foreground' />
                      <p className='text-sm text-muted-foreground'>
                        Loading modules...
                      </p>
                    </div>
                  </div>
                ) : filteredPlanModules.length === 0 ? (
                  <div className='flex flex-col items-center justify-center h-48 text-center'>
                    <Package className='h-12 w-12 text-muted-foreground mb-4' />
                    <p className='text-sm text-muted-foreground'>
                      {search
                        ? 'No modules found matching your search'
                        : 'No modules added to this plan yet'}
                    </p>
                    {!search && availableModules.length > 0 && (
                      <Button
                        size='sm'
                        variant='outline'
                        className='mt-4'
                        onClick={() => setShowAddForm(true)}
                      >
                        <Plus className='h-4 w-4 mr-2' />
                        Add First Module
                      </Button>
                    )}
                  </div>
                ) : (
                  <div className='space-y-3'>
                    {filteredPlanModules.map((planModule) => (
                      <Card
                        key={planModule.id}
                        className={cn(
                          'transition-colors',
                          !planModule.isIncluded && 'opacity-60'
                        )}
                      >
                        <CardContent className='p-4'>
                          <div className='flex items-start justify-between gap-4'>
                            <div className='flex-1 space-y-2'>
                              <div className='flex items-center gap-2'>
                                <h4 className='font-semibold'>
                                  {planModule.moduleName}
                                </h4>
                                <Badge
                                  variant='outline'
                                  className='text-xs font-mono'
                                >
                                  {planModule.moduleCode}
                                </Badge>
                                {planModule.isIncluded ? (
                                  <CheckCircle className='h-4 w-4 text-green-600' />
                                ) : (
                                  <XCircle className='h-4 w-4 text-red-600' />
                                )}
                              </div>

                              <div className='flex items-center gap-3 text-sm'>
                                <div className='flex items-center gap-1'>
                                  <Shield className='h-3 w-3 text-muted-foreground' />
                                  <Badge
                                    className={cn(
                                      'text-xs',
                                      getLicenseColor(planModule.licenseType)
                                    )}
                                  >
                                    {planModule.licenseType}
                                  </Badge>
                                </div>

                                {planModule.maxUsersPerModule && (
                                  <span className='text-muted-foreground'>
                                    Max Users: {planModule.maxUsersPerModule}
                                  </span>
                                )}
                              </div>
                            </div>

                            <Button
                              variant='ghost'
                              size='sm'
                              onClick={() =>
                                handleRemoveModule(
                                  planModule.moduleId,
                                  planModule.moduleName
                                )
                              }
                              disabled={isRemoving}
                            >
                              <Trash2 className='h-4 w-4 text-destructive' />
                            </Button>
                          </div>
                        </CardContent>
                      </Card>
                    ))}
                  </div>
                )}
              </div>
            </CardContent>
          </Card>
        </div>

        <div className='flex justify-end'>
          <Button variant='outline' onClick={() => onOpenChange(false)}>
            Close
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
};
