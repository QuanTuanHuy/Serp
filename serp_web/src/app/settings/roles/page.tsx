/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings roles management page
 */

'use client';

import React, { useState } from 'react';
import {
  Shield,
  Plus,
  Search,
  Users,
  Edit,
  Trash2,
  Copy,
  Lock,
  Unlock,
  CheckCircle2,
  Settings,
  Eye,
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
import { SettingsStatsCard, SettingsActionMenu } from '@/modules/settings';
import { Separator } from '@/shared/components/ui/separator';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';

export default function SettingsRolesPage() {
  const [searchQuery, setSearchQuery] = useState('');
  const [scopeFilter, setScopeFilter] = useState('all');

  // Mock data
  const mockRoles = [
    {
      id: '1',
      name: 'Organization Admin',
      code: 'ORG_ADMIN',
      description: 'Full administrative access to organization settings',
      scope: 'ORGANIZATION',
      type: 'SYSTEM',
      userCount: 5,
      permissions: [
        'manage_users',
        'manage_roles',
        'manage_departments',
        'manage_modules',
        'manage_subscription',
        'manage_security',
        'manage_integrations',
      ],
      isSystem: true,
      createdAt: '2024-01-15T10:00:00Z',
    },
    {
      id: '2',
      name: 'CRM Sales Person',
      code: 'CRM_SALES_PERSON',
      description: 'Access to CRM module for sales activities',
      scope: 'MODULE',
      type: 'SYSTEM',
      userCount: 12,
      permissions: [
        'view_customers',
        'create_customers',
        'edit_customers',
        'view_leads',
        'create_leads',
        'edit_leads',
        'view_opportunities',
        'create_opportunities',
      ],
      isSystem: true,
      createdAt: '2024-01-15T10:00:00Z',
    },
    {
      id: '3',
      name: 'Project Manager',
      code: 'PROJECT_MANAGER',
      description: 'Manage projects, tasks, and team assignments',
      scope: 'MODULE',
      type: 'CUSTOM',
      userCount: 8,
      permissions: [
        'view_projects',
        'create_projects',
        'edit_projects',
        'delete_projects',
        'assign_tasks',
        'view_reports',
      ],
      isSystem: false,
      createdAt: '2024-02-01T10:00:00Z',
    },
    {
      id: '4',
      name: 'Team Member',
      code: 'TEAM_MEMBER',
      description: 'Basic access to assigned projects and tasks',
      scope: 'MODULE',
      type: 'CUSTOM',
      userCount: 20,
      permissions: [
        'view_assigned_tasks',
        'update_task_status',
        'add_comments',
        'upload_files',
      ],
      isSystem: false,
      createdAt: '2024-02-15T10:00:00Z',
    },
    {
      id: '5',
      name: 'Finance Manager',
      code: 'FINANCE_MANAGER',
      description: 'Access to financial modules and reporting',
      scope: 'MODULE',
      type: 'CUSTOM',
      userCount: 3,
      permissions: [
        'view_accounting',
        'create_invoices',
        'approve_expenses',
        'view_financial_reports',
      ],
      isSystem: false,
      createdAt: '2024-03-01T10:00:00Z',
    },
  ];

  const systemRoles = mockRoles.filter((r) => r.isSystem);
  const customRoles = mockRoles.filter((r) => !r.isSystem);
  const totalUsers = mockRoles.reduce((sum, r) => sum + r.userCount, 0);

  const getRoleScopeColor = (scope: string) => {
    switch (scope) {
      case 'ORGANIZATION':
        return 'bg-purple-100 text-purple-700 dark:bg-purple-900 dark:text-purple-300';
      case 'MODULE':
        return 'bg-blue-100 text-blue-700 dark:bg-blue-900 dark:text-blue-300';
      case 'DEPARTMENT':
        return 'bg-green-100 text-green-700 dark:bg-green-900 dark:text-green-300';
      default:
        return 'bg-gray-100 text-gray-700 dark:bg-gray-900 dark:text-gray-300';
    }
  };

  return (
    <div className='space-y-6'>
      {/* Page Header */}
      <div className='flex flex-col gap-4 md:flex-row md:items-center md:justify-between'>
        <div>
          <h1 className='text-3xl font-bold tracking-tight'>
            Roles & Permissions
          </h1>
          <p className='text-muted-foreground mt-2'>
            Configure roles and control what users can access and do
          </p>
        </div>
        <Button className='bg-purple-600 hover:bg-purple-700'>
          <Plus className='h-4 w-4 mr-2' />
          Create Role
        </Button>
      </div>

      {/* Stats Grid */}
      <div className='grid gap-4 md:grid-cols-2 lg:grid-cols-4'>
        <SettingsStatsCard
          title='Total Roles'
          value={mockRoles.length}
          description='System & custom roles'
          icon={<Shield className='h-4 w-4' />}
        />

        <SettingsStatsCard
          title='System Roles'
          value={systemRoles.length}
          description='Built-in roles'
          icon={<Lock className='h-4 w-4' />}
        />

        <SettingsStatsCard
          title='Custom Roles'
          value={customRoles.length}
          description='Organization-specific'
          icon={<Unlock className='h-4 w-4' />}
        />

        <SettingsStatsCard
          title='Assigned Users'
          value={totalUsers}
          description='Users with roles'
          icon={<Users className='h-4 w-4' />}
        />
      </div>

      {/* Filters and Search */}
      <Card>
        <CardHeader>
          <div className='flex flex-col gap-4 md:flex-row md:items-center md:justify-between'>
            <div>
              <CardTitle>All Roles</CardTitle>
              <CardDescription>
                Manage roles and their permissions
              </CardDescription>
            </div>
          </div>
        </CardHeader>
        <CardContent className='space-y-4'>
          {/* Search and Filter Bar */}
          <div className='flex flex-col gap-4 md:flex-row'>
            <div className='relative flex-1'>
              <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
              <Input
                placeholder='Search roles...'
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className='pl-10'
              />
            </div>
            <div className='flex gap-2'>
              <Select value={scopeFilter} onValueChange={setScopeFilter}>
                <SelectTrigger className='w-[150px]'>
                  <SelectValue placeholder='Scope' />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value='all'>All Scopes</SelectItem>
                  <SelectItem value='ORGANIZATION'>Organization</SelectItem>
                  <SelectItem value='MODULE'>Module</SelectItem>
                  <SelectItem value='DEPARTMENT'>Department</SelectItem>
                </SelectContent>
              </Select>

              <Select defaultValue='all'>
                <SelectTrigger className='w-[150px]'>
                  <SelectValue placeholder='Type' />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value='all'>All Types</SelectItem>
                  <SelectItem value='SYSTEM'>System</SelectItem>
                  <SelectItem value='CUSTOM'>Custom</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* Roles List */}
          <div className='space-y-3'>
            {mockRoles.map((role) => (
              <Card
                key={role.id}
                className='hover:shadow-md transition-all border-l-4'
                style={{
                  borderLeftColor: role.isSystem ? '#7c3aed' : '#3b82f6',
                }}
              >
                <CardHeader className='pb-3'>
                  <div className='flex items-start justify-between'>
                    <div className='flex items-start gap-3 flex-1'>
                      <div
                        className={`h-12 w-12 rounded-lg flex items-center justify-center ${
                          role.isSystem
                            ? 'bg-purple-100 dark:bg-purple-900'
                            : 'bg-blue-100 dark:bg-blue-900'
                        }`}
                      >
                        {role.isSystem ? (
                          <Lock className='h-6 w-6 text-purple-600 dark:text-purple-400' />
                        ) : (
                          <Shield className='h-6 w-6 text-blue-600 dark:text-blue-400' />
                        )}
                      </div>
                      <div className='flex-1'>
                        <div className='flex items-center gap-2 flex-wrap'>
                          <CardTitle className='text-base'>
                            {role.name}
                          </CardTitle>
                          <Badge
                            variant='outline'
                            className={getRoleScopeColor(role.scope)}
                          >
                            {role.scope}
                          </Badge>
                          {role.isSystem && (
                            <Badge variant='secondary' className='text-xs'>
                              <Lock className='h-3 w-3 mr-1' />
                              System
                            </Badge>
                          )}
                        </div>
                        <CardDescription className='text-sm mt-1'>
                          {role.description}
                        </CardDescription>
                        <div className='flex items-center gap-4 mt-2 text-xs text-muted-foreground'>
                          <div className='flex items-center gap-1'>
                            <Users className='h-3 w-3' />
                            <span>
                              {role.userCount}{' '}
                              {role.userCount === 1 ? 'user' : 'users'}
                            </span>
                          </div>
                          <div className='flex items-center gap-1'>
                            <Shield className='h-3 w-3' />
                            <span>{role.permissions.length} permissions</span>
                          </div>
                        </div>
                      </div>
                    </div>
                    <SettingsActionMenu
                      items={[
                        {
                          label: 'View Details',
                          onClick: () => console.log('View', role.id),
                          icon: <Eye className='h-4 w-4' />,
                        },
                        {
                          label: 'Edit Role',
                          onClick: () => console.log('Edit', role.id),
                          icon: <Edit className='h-4 w-4' />,
                          disabled: role.isSystem,
                        },
                        {
                          label: 'Duplicate',
                          onClick: () => console.log('Duplicate', role.id),
                          icon: <Copy className='h-4 w-4' />,
                        },
                        {
                          label: 'Manage Users',
                          onClick: () => console.log('Manage users', role.id),
                          icon: <Users className='h-4 w-4' />,
                          separator: true,
                        },
                        {
                          label: 'Delete',
                          onClick: () => console.log('Delete', role.id),
                          icon: <Trash2 className='h-4 w-4' />,
                          variant: 'destructive',
                          disabled: role.isSystem,
                          separator: true,
                        },
                      ]}
                    />
                  </div>
                </CardHeader>
                <CardContent>
                  <div className='space-y-3'>
                    <Separator />
                    <div>
                      <p className='text-sm font-medium mb-2 flex items-center gap-2'>
                        <CheckCircle2 className='h-4 w-4 text-muted-foreground' />
                        Key Permissions
                      </p>
                      <div className='flex flex-wrap gap-1'>
                        {role.permissions
                          .slice(0, 6)
                          .map((permission, index) => (
                            <Badge
                              key={index}
                              variant='outline'
                              className='text-xs font-normal'
                            >
                              {permission.replace(/_/g, ' ')}
                            </Badge>
                          ))}
                        {role.permissions.length > 6 && (
                          <Badge variant='secondary' className='text-xs'>
                            +{role.permissions.length - 6} more
                          </Badge>
                        )}
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Help Card */}
      <Card className='border-purple-200 dark:border-purple-800 bg-purple-50/50 dark:bg-purple-950/20'>
        <CardHeader>
          <div className='flex items-start gap-3'>
            <div className='h-10 w-10 rounded-lg bg-purple-600 flex items-center justify-center flex-shrink-0'>
              <Settings className='h-5 w-5 text-white' />
            </div>
            <div>
              <CardTitle className='text-base'>Role Management Tips</CardTitle>
              <CardDescription className='mt-1'>
                <ul className='list-disc list-inside space-y-1 text-sm'>
                  <li>
                    System roles cannot be edited or deleted to maintain
                    security
                  </li>
                  <li>Create custom roles for organization-specific needs</li>
                  <li>Use role scopes to control where permissions apply</li>
                  <li>Regularly review role assignments for security</li>
                </ul>
              </CardDescription>
            </div>
          </div>
        </CardHeader>
      </Card>
    </div>
  );
}
