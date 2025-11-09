/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings modules management page
 */

'use client';

import React, { useEffect, useMemo, useState } from 'react';
import { useDebounce } from '@/shared/hooks';
import { useNotification } from '@/shared/hooks/use-notification';
import { useSettingsModules } from '@/modules/settings/hooks/useModules';
import { ModuleUsersDialog } from '@/modules/settings/components';
import { RequestModuleDialog } from '@/modules/settings/components/modules/RequestModuleDialog';
import { useRequestMoreModulesMutation } from '@/modules/settings/services/modules/modulesApi';
import {
  Puzzle,
  Users,
  CheckCircle2,
  XCircle,
  Shield,
  Lock,
  Unlock,
  TrendingUp,
  Activity,
  ChevronRight,
  Search,
  Settings,
  Sparkles,
} from 'lucide-react';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Badge } from '@/shared/components/ui/badge';
import { Switch } from '@/shared/components/ui/switch';
import { Label } from '@/shared/components/ui/label';
import { SettingsStatsCard } from '@/modules/settings';
import { Separator } from '@/shared/components/ui/separator';
import { Progress } from '@/shared/components/ui/progress';
import { MODULE_ICONS } from '@/shared/constants/moduleIcons';

import type { AccessibleModule } from '@/modules/settings/types/module-access.types';

export default function SettingsModulesPage() {
  const notification = useNotification();
  const [searchQuery, setSearchQuery] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [requestDialogOpen, setRequestDialogOpen] = useState(false);
  const [selectedModule, setSelectedModule] = useState<
    AccessibleModule | undefined
  >(undefined);
  const debouncedSearch = useDebounce(searchQuery, 300);

  const {
    modules,
    activeModules,
    totalActiveUsers,
    totalUsersBaseline,
    setSearch,
  } = useSettingsModules();

  const [requestMoreModules, { isLoading: isRequesting }] =
    useRequestMoreModulesMutation();

  useEffect(() => {
    setSearch(debouncedSearch);
  }, [debouncedSearch, setSearch]);

  const handleRequestModule = async () => {
    if (!selectedModule?.moduleId) {
      notification.error('Module ID not found');
      return;
    }

    try {
      const result = await requestMoreModules({
        additionalModuleIds: [selectedModule.moduleId],
      }).unwrap();

      notification.success(
        result.message || 'Module request submitted successfully'
      );
      setRequestDialogOpen(false);
      setSelectedModule(undefined);
    } catch (error: any) {
      notification.error(
        error?.data?.message ||
          'Failed to submit module request. Please try again.'
      );
    }
  };

  const handleEnableModule = (module: AccessibleModule) => {
    setSelectedModule(module);
    setRequestDialogOpen(true);
  };

  const getModuleIcon = (moduleCode: string) => {
    const iconConfig = MODULE_ICONS[moduleCode as keyof typeof MODULE_ICONS];
    if (!iconConfig) return <Puzzle className='h-5 w-5' />;
    const Icon = iconConfig.icon;
    return <Icon className='h-5 w-5' />;
  };

  const getModuleColor = (moduleCode: string) => {
    const iconConfig = MODULE_ICONS[moduleCode as keyof typeof MODULE_ICONS];
    return iconConfig?.color || 'text-gray-600';
  };

  const getModuleBgColor = (moduleCode: string) => {
    const iconConfig = MODULE_ICONS[moduleCode as keyof typeof MODULE_ICONS];
    return iconConfig?.bgColor || 'bg-gray-50 dark:bg-gray-950';
  };

  const calculateUsagePercentage = (active: number, total: number) => {
    return total > 0 ? Math.round((active / total) * 100) : 0;
  };

  return (
    <div className='space-y-6'>
      {/* Page Header */}
      <div className='flex flex-col gap-4 md:flex-row md:items-center md:justify-between'>
        <div>
          <h1 className='text-3xl font-bold tracking-tight'>Module Access</h1>
          <p className='text-muted-foreground mt-2'>
            Manage which modules are available to your organization and control
            user access
          </p>
        </div>
      </div>

      {/* Stats Grid */}
      <div className='grid gap-4 md:grid-cols-2 lg:grid-cols-4'>
        <SettingsStatsCard
          title='Active Modules'
          value={activeModules.length}
          description={`of ${modules.length} available`}
          icon={<Puzzle className='h-4 w-4' />}
        />

        <SettingsStatsCard
          title='Total Access Granted'
          value={totalActiveUsers}
          description='User-module assignments'
          icon={<Users className='h-4 w-4' />}
        />

        <SettingsStatsCard
          title='Most Used'
          value='PTM'
          description='25 users active'
          icon={<TrendingUp className='h-4 w-4' />}
        />

        <SettingsStatsCard
          title='Avg Adoption'
          value={`${useMemo(() => {
            const denom = modules.length * (totalUsersBaseline || 0);
            if (!denom) return 0;
            return Math.round((totalActiveUsers / denom) * 100);
          }, [modules.length, totalUsersBaseline, totalActiveUsers])}%`}
          description='Across all modules'
          icon={<Activity className='h-4 w-4' />}
        />
      </div>

      {/* Search and Filter */}
      <Card>
        <CardHeader>
          <CardTitle>All Modules</CardTitle>
          <CardDescription>
            Enable or disable modules and configure access settings
          </CardDescription>
        </CardHeader>
        <CardContent className='space-y-4'>
          {/* Search Bar */}
          <div className='relative'>
            <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
            <Input
              placeholder='Search modules...'
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className='pl-10'
            />
          </div>

          {/* Module Cards Grid */}
          <div className='grid gap-4 md:grid-cols-2 lg:grid-cols-2'>
            {modules.map((module) => {
              const usagePercent = calculateUsagePercentage(
                module.activeUserCount || 0,
                module.totalUsersCount || 0
              );

              return (
                <Card
                  key={module.moduleCode}
                  className={`transition-all hover:shadow-md ${
                    module.isActive
                      ? 'border-purple-200 dark:border-purple-800'
                      : 'opacity-60'
                  }`}
                >
                  <CardHeader className='pb-3'>
                    <div className='flex items-start justify-between'>
                      <div className='flex items-center gap-3 flex-1'>
                        <div
                          className={`h-12 w-12 rounded-lg flex items-center justify-center ${getModuleBgColor(
                            module.moduleCode
                          )}`}
                        >
                          <span className={getModuleColor(module.moduleCode)}>
                            {getModuleIcon(module.moduleCode)}
                          </span>
                        </div>
                        <div className='flex-1'>
                          <div className='flex items-center gap-2'>
                            <CardTitle className='text-base'>
                              {module.moduleName}
                            </CardTitle>
                            {module.isActive ? (
                              <Badge
                                variant='default'
                                className='bg-green-100 text-green-700 hover:bg-green-100 dark:bg-green-900 dark:text-green-300'
                              >
                                <CheckCircle2 className='h-3 w-3 mr-1' />
                                Active
                              </Badge>
                            ) : (
                              <Badge variant='secondary'>
                                <XCircle className='h-3 w-3 mr-1' />
                                Disabled
                              </Badge>
                            )}
                          </div>
                          <CardDescription className='text-xs mt-1'>
                            {module.moduleDescription}
                          </CardDescription>
                        </div>
                      </div>
                      <Switch
                        checked={module.isActive}
                        onCheckedChange={() =>
                          console.log('Toggle', module.moduleCode)
                        }
                        className='data-[state=checked]:bg-purple-600'
                      />
                    </div>
                  </CardHeader>

                  {module.isActive && (
                    <CardContent className='space-y-4'>
                      {/* Usage Stats */}
                      <div className='space-y-2'>
                        <div className='flex items-center justify-between text-sm'>
                          <div className='flex items-center gap-2 text-muted-foreground'>
                            <Users className='h-4 w-4' />
                            <span>User Access</span>
                          </div>
                          <span className='font-medium'>
                            {module.activeUserCount || 0} /{' '}
                            {module.totalUsersCount || 0}
                          </span>
                        </div>
                        <Progress value={usagePercent} className='h-2' />
                        <p className='text-xs text-muted-foreground'>
                          {usagePercent}% of organization using this module
                        </p>
                      </div>

                      <Separator />

                      {/* Access Settings */}
                      <div className='space-y-3'>
                        <div className='flex items-center justify-between'>
                          <div className='space-y-0.5'>
                            <Label className='text-sm font-medium flex items-center gap-2'>
                              <Unlock className='h-4 w-4 text-muted-foreground' />
                              Auto-grant to new users
                            </Label>
                            <p className='text-xs text-muted-foreground'>
                              Automatically give access when users join
                            </p>
                          </div>
                          <Switch
                            checked={Boolean(module.isAutoGrantToNewUsers)}
                            onCheckedChange={() =>
                              console.log(
                                'Toggle auto-grant',
                                module.moduleCode
                              )
                            }
                            className='data-[state=checked]:bg-purple-600'
                          />
                        </div>

                        {module.requiredRoles &&
                          module.requiredRoles.length > 0 && (
                            <>
                              <Separator />
                              <div className='space-y-2'>
                                <Label className='text-sm font-medium flex items-center gap-2'>
                                  <Shield className='h-4 w-4 text-muted-foreground' />
                                  Required Roles
                                </Label>
                                <div className='flex flex-wrap gap-1'>
                                  {module.requiredRoles.map((role, index) => (
                                    <Badge
                                      key={index}
                                      variant='outline'
                                      className='text-xs'
                                    >
                                      {role.replace('_', ' ')}
                                    </Badge>
                                  ))}
                                </div>
                              </div>
                            </>
                          )}
                      </div>

                      <Separator />

                      {/* Action Buttons */}
                      <div className='flex gap-2'>
                        <Button
                          variant='outline'
                          size='sm'
                          className='flex-1'
                          onClick={() => {
                            setSelectedModule(module);
                            setDialogOpen(true);
                          }}
                        >
                          <Users className='h-4 w-4 mr-2' />
                          Manage Users
                        </Button>
                        <Button
                          variant='outline'
                          size='sm'
                          className='flex-1'
                          onClick={() =>
                            console.log('Configure', module.moduleCode)
                          }
                        >
                          <Settings className='h-4 w-4 mr-2' />
                          Configure
                        </Button>
                      </div>
                    </CardContent>
                  )}

                  {!module.isActive && (
                    <CardContent>
                      <div className='text-center py-6'>
                        <div className='mb-4 inline-flex items-center justify-center w-16 h-16 rounded-full bg-purple-50 dark:bg-purple-950/20'>
                          <Lock className='h-8 w-8 text-purple-600 dark:text-purple-400' />
                        </div>
                        <h4 className='font-semibold text-base mb-2'>
                          Module Not Available
                        </h4>
                        <p className='text-sm text-muted-foreground mb-4 max-w-xs mx-auto'>
                          Request access to add this module to your
                          organization's subscription
                        </p>
                        <Button
                          variant='outline'
                          size='sm'
                          className='border-purple-200 text-purple-600 hover:bg-purple-50 dark:border-purple-800 dark:text-purple-400'
                          onClick={() => handleEnableModule(module)}
                        >
                          <Sparkles className='h-4 w-4 mr-2' />
                          Request Module
                        </Button>
                      </div>
                    </CardContent>
                  )}
                </Card>
              );
            })}
          </div>
        </CardContent>
      </Card>

      {/* Manage Users Dialog */}
      <ModuleUsersDialog
        open={dialogOpen}
        module={selectedModule}
        onOpenChange={(v) => setDialogOpen(v)}
      />

      {/* Request Module Dialog */}
      <RequestModuleDialog
        open={requestDialogOpen}
        module={selectedModule}
        onOpenChange={setRequestDialogOpen}
        onConfirm={handleRequestModule}
        isLoading={isRequesting}
      />

      {/* Quick Actions */}
      <Card className='border-purple-200 dark:border-purple-800 bg-purple-50/50 dark:bg-purple-950/20'>
        <CardHeader>
          <div className='flex items-center gap-3'>
            <div className='h-10 w-10 rounded-lg bg-purple-600 flex items-center justify-center'>
              <Puzzle className='h-5 w-5 text-white' />
            </div>
            <div>
              <CardTitle className='text-base'>Need More Modules?</CardTitle>
              <CardDescription>
                Upgrade your plan to unlock additional modules
              </CardDescription>
            </div>
          </div>
        </CardHeader>
        <CardContent className='flex gap-2'>
          <Button className='bg-purple-600 hover:bg-purple-700'>
            View Available Modules
            <ChevronRight className='h-4 w-4 ml-2' />
          </Button>
          <Button variant='outline'>Contact Sales</Button>
        </CardContent>
      </Card>
    </div>
  );
}
