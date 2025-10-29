/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings users management page
 */

'use client';

import React, { useState } from 'react';
import {
  Users,
  UserPlus,
  Search,
  Filter,
  Mail,
  Phone,
  MoreHorizontal,
  Edit,
  Trash2,
  Shield,
  CheckCircle2,
  XCircle,
  Clock,
  Download,
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';
import { Badge } from '@/shared/components/ui/badge';
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from '@/shared/components/ui/avatar';
import { SettingsStatsCard, SettingsActionMenu } from '@/modules/settings';

export default function SettingsUsersPage() {
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');

  // Mock data
  const mockUsers = [
    {
      id: '1',
      email: 'john.doe@acme.com',
      firstName: 'John',
      lastName: 'Doe',
      fullName: 'John Doe',
      avatarUrl: '',
      status: 'ACTIVE',
      roles: ['ORG_ADMIN', 'CRM_SALES_PERSON'],
      departmentName: 'Sales',
      position: 'Sales Manager',
      phone: '+1 (555) 123-4567',
      joinedAt: '2024-01-15T10:00:00Z',
      lastActiveAt: '2024-03-20T15:30:00Z',
    },
    {
      id: '2',
      email: 'jane.smith@acme.com',
      firstName: 'Jane',
      lastName: 'Smith',
      fullName: 'Jane Smith',
      avatarUrl: '',
      status: 'ACTIVE',
      roles: ['PTM_ADMIN'],
      departmentName: 'Engineering',
      position: 'Project Lead',
      phone: '+1 (555) 234-5678',
      joinedAt: '2024-02-01T09:00:00Z',
      lastActiveAt: '2024-03-20T16:00:00Z',
    },
    {
      id: '3',
      email: 'mike.wilson@acme.com',
      firstName: 'Mike',
      lastName: 'Wilson',
      fullName: 'Mike Wilson',
      avatarUrl: '',
      status: 'PENDING',
      roles: ['CRM_SALES_PERSON'],
      departmentName: 'Sales',
      position: 'Sales Representative',
      phone: '',
      joinedAt: '2024-03-15T10:00:00Z',
      lastActiveAt: null,
    },
    {
      id: '4',
      email: 'sarah.johnson@acme.com',
      firstName: 'Sarah',
      lastName: 'Johnson',
      fullName: 'Sarah Johnson',
      avatarUrl: '',
      status: 'INACTIVE',
      roles: ['PTM_USER'],
      departmentName: 'Marketing',
      position: 'Marketing Specialist',
      phone: '+1 (555) 345-6789',
      joinedAt: '2024-01-20T11:00:00Z',
      lastActiveAt: '2024-03-10T14:00:00Z',
    },
  ];

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return <CheckCircle2 className='h-4 w-4 text-green-600' />;
      case 'PENDING':
        return <Clock className='h-4 w-4 text-yellow-600' />;
      case 'INACTIVE':
        return <XCircle className='h-4 w-4 text-gray-400' />;
      default:
        return null;
    }
  };

  const getStatusVariant = (
    status: string
  ): 'default' | 'secondary' | 'destructive' | 'outline' => {
    switch (status) {
      case 'ACTIVE':
        return 'default';
      case 'PENDING':
        return 'secondary';
      case 'INACTIVE':
      case 'SUSPENDED':
        return 'destructive';
      default:
        return 'outline';
    }
  };

  return (
    <div className='space-y-6'>
      {/* Page Header */}
      <div className='flex flex-col gap-4 md:flex-row md:items-center md:justify-between'>
        <div>
          <h1 className='text-3xl font-bold tracking-tight'>User Management</h1>
          <p className='text-muted-foreground mt-2'>
            Manage organization members, roles, and permissions
          </p>
        </div>
        <Button className='bg-purple-600 hover:bg-purple-700'>
          <UserPlus className='h-4 w-4 mr-2' />
          Invite User
        </Button>
      </div>

      {/* Stats Grid */}
      <div className='grid gap-4 md:grid-cols-2 lg:grid-cols-4'>
        <SettingsStatsCard
          title='Total Users'
          value={45}
          description='Organization members'
          icon={<Users className='h-4 w-4' />}
          trend={{ value: 12.5, label: 'vs last month' }}
        />

        <SettingsStatsCard
          title='Active Users'
          value={42}
          description='Currently active'
          icon={<CheckCircle2 className='h-4 w-4' />}
        />

        <SettingsStatsCard
          title='Pending Invites'
          value={3}
          description='Awaiting acceptance'
          icon={<Clock className='h-4 w-4' />}
        />

        <SettingsStatsCard
          title='Admin Users'
          value={5}
          description='With admin privileges'
          icon={<Shield className='h-4 w-4' />}
        />
      </div>

      {/* Filters and Search */}
      <Card>
        <CardHeader>
          <div className='flex flex-col gap-4 md:flex-row md:items-center md:justify-between'>
            <div>
              <CardTitle>All Users</CardTitle>
              <CardDescription>
                View and manage organization members
              </CardDescription>
            </div>
            <Button variant='outline' size='sm'>
              <Download className='h-4 w-4 mr-2' />
              Export
            </Button>
          </div>
        </CardHeader>
        <CardContent className='space-y-4'>
          {/* Search and Filter Bar */}
          <div className='flex flex-col gap-4 md:flex-row'>
            <div className='relative flex-1'>
              <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
              <Input
                placeholder='Search by name, email, or department...'
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className='pl-10'
              />
            </div>
            <div className='flex gap-2'>
              <Select value={statusFilter} onValueChange={setStatusFilter}>
                <SelectTrigger className='w-[150px]'>
                  <Filter className='h-4 w-4 mr-2' />
                  <SelectValue placeholder='Status' />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value='all'>All Status</SelectItem>
                  <SelectItem value='ACTIVE'>Active</SelectItem>
                  <SelectItem value='PENDING'>Pending</SelectItem>
                  <SelectItem value='INACTIVE'>Inactive</SelectItem>
                  <SelectItem value='SUSPENDED'>Suspended</SelectItem>
                </SelectContent>
              </Select>

              <Select defaultValue='all'>
                <SelectTrigger className='w-[150px]'>
                  <SelectValue placeholder='Role' />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value='all'>All Roles</SelectItem>
                  <SelectItem value='ORG_ADMIN'>Org Admin</SelectItem>
                  <SelectItem value='CRM_SALES_PERSON'>Sales Person</SelectItem>
                  <SelectItem value='PTM_ADMIN'>PTM Admin</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* Users Table */}
          <div className='rounded-md border'>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>User</TableHead>
                  <TableHead>Department</TableHead>
                  <TableHead>Roles</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Last Active</TableHead>
                  <TableHead className='text-right'>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {mockUsers.map((user) => (
                  <TableRow key={user.id}>
                    <TableCell>
                      <div className='flex items-center gap-3'>
                        <Avatar className='h-10 w-10'>
                          <AvatarImage
                            src={user.avatarUrl}
                            alt={user.fullName}
                          />
                          <AvatarFallback className='bg-purple-100 text-purple-700'>
                            {user.firstName[0]}
                            {user.lastName[0]}
                          </AvatarFallback>
                        </Avatar>
                        <div>
                          <p className='font-medium'>{user.fullName}</p>
                          <div className='flex items-center gap-2 text-xs text-muted-foreground'>
                            <Mail className='h-3 w-3' />
                            {user.email}
                          </div>
                          {user.phone && (
                            <div className='flex items-center gap-2 text-xs text-muted-foreground'>
                              <Phone className='h-3 w-3' />
                              {user.phone}
                            </div>
                          )}
                        </div>
                      </div>
                    </TableCell>
                    <TableCell>
                      <div>
                        <p className='font-medium text-sm'>
                          {user.departmentName}
                        </p>
                        <p className='text-xs text-muted-foreground'>
                          {user.position}
                        </p>
                      </div>
                    </TableCell>
                    <TableCell>
                      <div className='flex flex-wrap gap-1'>
                        {user.roles.map((role, index) => (
                          <Badge
                            key={index}
                            variant='outline'
                            className='text-xs'
                          >
                            {role.replace('_', ' ')}
                          </Badge>
                        ))}
                      </div>
                    </TableCell>
                    <TableCell>
                      <Badge
                        variant={getStatusVariant(user.status)}
                        className='flex items-center gap-1 w-fit'
                      >
                        {getStatusIcon(user.status)}
                        {user.status}
                      </Badge>
                    </TableCell>
                    <TableCell className='text-sm text-muted-foreground'>
                      {user.lastActiveAt
                        ? new Date(user.lastActiveAt).toLocaleDateString(
                            'en-US',
                            {
                              month: 'short',
                              day: 'numeric',
                              year: 'numeric',
                            }
                          )
                        : 'Never'}
                    </TableCell>
                    <TableCell className='text-right'>
                      <SettingsActionMenu
                        items={[
                          {
                            label: 'Edit User',
                            onClick: () => console.log('Edit', user.id),
                            icon: <Edit className='h-4 w-4' />,
                          },
                          {
                            label: 'Manage Roles',
                            onClick: () => console.log('Manage roles', user.id),
                            icon: <Shield className='h-4 w-4' />,
                          },
                          {
                            label: 'Remove User',
                            onClick: () => console.log('Remove', user.id),
                            icon: <Trash2 className='h-4 w-4' />,
                            variant: 'destructive',
                            separator: true,
                          },
                        ]}
                      />
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>

          {/* Pagination */}
          <div className='flex items-center justify-between'>
            <p className='text-sm text-muted-foreground'>
              Showing <span className='font-medium'>1-4</span> of{' '}
              <span className='font-medium'>45</span> users
            </p>
            <div className='flex gap-2'>
              <Button variant='outline' size='sm' disabled>
                Previous
              </Button>
              <Button variant='outline' size='sm'>
                Next
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
