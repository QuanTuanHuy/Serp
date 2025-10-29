/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings departments management page
 */

'use client';

import React, { useState } from 'react';
import {
  Layers,
  Plus,
  Search,
  Users,
  Edit,
  Trash2,
  MoreVertical,
  UserPlus,
  Building2,
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
import { Avatar, AvatarFallback } from '@/shared/components/ui/avatar';
import {
  SettingsStatsCard,
  SettingsActionMenu,
  SettingsStatusBadge,
} from '@/modules/settings';
import { Separator } from '@/shared/components/ui/separator';

export default function SettingsDepartmentsPage() {
  const [searchQuery, setSearchQuery] = useState('');

  // Mock data
  const mockDepartments = [
    {
      id: '1',
      name: 'Sales',
      description: 'Sales and business development team',
      parentDepartmentName: null,
      managerName: 'John Doe',
      memberCount: 12,
      status: 'ACTIVE',
    },
    {
      id: '2',
      name: 'Engineering',
      description: 'Product development and engineering',
      parentDepartmentName: null,
      managerName: 'Jane Smith',
      memberCount: 25,
      status: 'ACTIVE',
    },
    {
      id: '3',
      name: 'Frontend Team',
      description: 'UI/UX and frontend development',
      parentDepartmentName: 'Engineering',
      managerName: 'Mike Wilson',
      memberCount: 8,
      status: 'ACTIVE',
    },
    {
      id: '4',
      name: 'Marketing',
      description: 'Marketing and brand management',
      parentDepartmentName: null,
      managerName: 'Sarah Johnson',
      memberCount: 7,
      status: 'ACTIVE',
    },
    {
      id: '5',
      name: 'Customer Support',
      description: 'Customer service and support',
      parentDepartmentName: null,
      managerName: null,
      memberCount: 5,
      status: 'INACTIVE',
    },
  ];

  return (
    <div className='space-y-6'>
      {/* Page Header */}
      <div className='flex flex-col gap-4 md:flex-row md:items-center md:justify-between'>
        <div>
          <h1 className='text-3xl font-bold tracking-tight'>Departments</h1>
          <p className='text-muted-foreground mt-2'>
            Organize your team into departments and manage hierarchy
          </p>
        </div>
        <Button className='bg-purple-600 hover:bg-purple-700'>
          <Plus className='h-4 w-4 mr-2' />
          Create Department
        </Button>
      </div>

      {/* Stats Grid */}
      <div className='grid gap-4 md:grid-cols-2 lg:grid-cols-4'>
        <SettingsStatsCard
          title='Total Departments'
          value={8}
          description='Active departments'
          icon={<Layers className='h-4 w-4' />}
        />

        <SettingsStatsCard
          title='Total Members'
          value={57}
          description='Across all departments'
          icon={<Users className='h-4 w-4' />}
        />

        <SettingsStatsCard
          title='Avg Team Size'
          value={7}
          description='Members per department'
          icon={<Users className='h-4 w-4' />}
        />

        <SettingsStatsCard
          title='With Managers'
          value={6}
          description='Departments assigned'
          icon={<UserPlus className='h-4 w-4' />}
        />
      </div>

      {/* Department List */}
      <Card>
        <CardHeader>
          <div className='flex flex-col gap-4 md:flex-row md:items-center md:justify-between'>
            <div>
              <CardTitle>All Departments</CardTitle>
              <CardDescription>
                View and manage organization departments
              </CardDescription>
            </div>
          </div>
        </CardHeader>
        <CardContent className='space-y-4'>
          {/* Search Bar */}
          <div className='relative'>
            <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
            <Input
              placeholder='Search departments...'
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className='pl-10'
            />
          </div>

          {/* Department Cards */}
          <div className='grid gap-4 md:grid-cols-2 lg:grid-cols-3'>
            {mockDepartments.map((dept) => (
              <Card key={dept.id} className='hover:shadow-md transition-shadow'>
                <CardHeader className='pb-3'>
                  <div className='flex items-start justify-between'>
                    <div className='flex items-center gap-3'>
                      <div className='h-10 w-10 rounded-lg bg-purple-100 dark:bg-purple-900 flex items-center justify-center'>
                        <Layers className='h-5 w-5 text-purple-600 dark:text-purple-400' />
                      </div>
                      <div>
                        <CardTitle className='text-base'>{dept.name}</CardTitle>
                        {dept.parentDepartmentName && (
                          <p className='text-xs text-muted-foreground'>
                            {dept.parentDepartmentName} → {dept.name}
                          </p>
                        )}
                      </div>
                    </div>
                    <SettingsActionMenu
                      items={[
                        {
                          label: 'Edit Department',
                          onClick: () => console.log('Edit', dept.id),
                          icon: <Edit className='h-4 w-4' />,
                        },
                        {
                          label: 'Add Members',
                          onClick: () => console.log('Add members', dept.id),
                          icon: <UserPlus className='h-4 w-4' />,
                        },
                        {
                          label: 'Delete',
                          onClick: () => console.log('Delete', dept.id),
                          icon: <Trash2 className='h-4 w-4' />,
                          variant: 'destructive',
                          separator: true,
                        },
                      ]}
                      triggerIcon={<MoreVertical className='h-4 w-4' />}
                    />
                  </div>
                </CardHeader>
                <CardContent className='space-y-3'>
                  {dept.description && (
                    <p className='text-sm text-muted-foreground line-clamp-2'>
                      {dept.description}
                    </p>
                  )}

                  <Separator />

                  <div className='flex items-center justify-between text-sm'>
                    <div className='flex items-center gap-2 text-muted-foreground'>
                      <Users className='h-4 w-4' />
                      <span>
                        {dept.memberCount}{' '}
                        {dept.memberCount === 1 ? 'member' : 'members'}
                      </span>
                    </div>
                    <SettingsStatusBadge status={dept.status} />
                  </div>

                  {dept.managerName && (
                    <>
                      <Separator />
                      <div className='flex items-center gap-2'>
                        <Avatar className='h-6 w-6'>
                          <AvatarFallback className='text-xs bg-purple-100 text-purple-700'>
                            {dept.managerName
                              .split(' ')
                              .map((n) => n[0])
                              .join('')}
                          </AvatarFallback>
                        </Avatar>
                        <div>
                          <p className='text-xs text-muted-foreground'>
                            Manager
                          </p>
                          <p className='text-sm font-medium'>
                            {dept.managerName}
                          </p>
                        </div>
                      </div>
                    </>
                  )}

                  {!dept.managerName && (
                    <>
                      <Separator />
                      <Button variant='outline' size='sm' className='w-full'>
                        <UserPlus className='h-4 w-4 mr-2' />
                        Assign Manager
                      </Button>
                    </>
                  )}
                </CardContent>
              </Card>
            ))}
          </div>

          {/* Empty State - when no results */}
          {mockDepartments.length === 0 && (
            <div className='text-center py-12'>
              <Building2 className='h-12 w-12 mx-auto text-muted-foreground mb-4' />
              <h3 className='text-lg font-semibold mb-2'>
                No departments found
              </h3>
              <p className='text-muted-foreground mb-4'>
                Get started by creating your first department
              </p>
              <Button className='bg-purple-600 hover:bg-purple-700'>
                <Plus className='h-4 w-4 mr-2' />
                Create Department
              </Button>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
