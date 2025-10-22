/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - System-wide users management page
 */

'use client';

import React, { useState } from 'react';
import {
  useGetUsersQuery,
  AdminStatusBadge,
  AdminActionMenu,
} from '@/modules/admin';
import type { UserFilters } from '@/modules/admin';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Input } from '@/shared/components/ui/input';
import { Button } from '@/shared/components/ui/button';
import {
  Users,
  Search,
  Eye,
  Edit,
  Ban,
  CheckCircle,
  ChevronLeft,
  ChevronRight,
  Mail,
  Building2,
} from 'lucide-react';
import { UserProfile } from '@/modules/admin/types';

export default function UsersPage() {
  const [filters, setFilters] = useState<UserFilters>({
    page: 0,
    pageSize: 10,
    sortBy: 'id',
    sortDir: 'DESC',
  });

  const {
    data: response,
    isLoading,
    isFetching,
    error,
  } = useGetUsersQuery(filters);

  const users = response?.data.items || [];
  const totalPages = response?.data.totalPages || 0;
  const currentPage = response?.data.currentPage || 0;

  const handleSearch = (search: string) => {
    setFilters({ ...filters, search, page: 0 });
  };

  const handleFilterChange = (key: keyof UserFilters, value: any) => {
    setFilters({ ...filters, [key]: value, page: 0 });
  };

  const handlePageChange = (newPage: number) => {
    setFilters({ ...filters, page: newPage });
  };

  const formatDate = (isoDate?: string) => {
    if (!isoDate) return 'Never';
    return new Date(isoDate).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  return (
    <div className='space-y-6'>
      {/* Page Header */}
      <div className='flex items-center justify-between'>
        <div>
          <h1 className='text-3xl font-bold tracking-tight'>Users</h1>
          <p className='text-muted-foreground mt-2'>
            System-wide user management and monitoring
          </p>
        </div>

        <div className='flex items-center gap-2'>
          <Button variant='outline' size='sm'>
            Export
          </Button>
        </div>
      </div>

      {/* Filters Card */}
      <Card>
        <CardHeader>
          <CardTitle className='text-base font-medium'>Filters</CardTitle>
        </CardHeader>
        <CardContent>
          <div className='grid gap-4 md:grid-cols-3'>
            {/* Search */}
            <div className='md:col-span-2'>
              <div className='relative'>
                <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
                <Input
                  placeholder='Search by name, email...'
                  value={filters.search || ''}
                  onChange={(e) => handleSearch(e.target.value)}
                  className='pl-10'
                />
              </div>
            </div>

            {/* Status Filter */}
            <div>
              <select
                value={filters.status || ''}
                onChange={(e) =>
                  handleFilterChange('status', e.target.value || undefined)
                }
                className='w-full h-10 rounded-md border border-input bg-background px-3 py-2 text-sm'
              >
                <option value=''>All Statuses</option>
                <option value='ACTIVE'>Active</option>
                <option value='INACTIVE'>Inactive</option>
                <option value='PENDING'>Pending</option>
                <option value='SUSPENDED'>Suspended</option>
              </select>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Table Card */}
      <Card>
        <CardContent className='p-0'>
          {/* Loading State */}
          {isLoading && (
            <div className='flex items-center justify-center h-64'>
              <div className='text-muted-foreground'>Loading users...</div>
            </div>
          )}

          {/* Error State */}
          {error && (
            <div className='flex items-center justify-center h-64'>
              <div className='text-destructive'>Failed to load users</div>
            </div>
          )}

          {/* Table */}
          {!isLoading && !error && response && (
            <>
              <div className='overflow-x-auto'>
                <table className='w-full'>
                  <thead className='border-b bg-muted/50'>
                    <tr>
                      <th className='px-4 py-3 text-left text-sm font-medium'>
                        User
                      </th>
                      <th className='px-4 py-3 text-left text-sm font-medium'>
                        Organization
                      </th>
                      <th className='px-4 py-3 text-left text-sm font-medium'>
                        Type
                      </th>
                      <th className='px-4 py-3 text-left text-sm font-medium'>
                        Status
                      </th>
                      <th className='px-4 py-3 text-left text-sm font-medium'>
                        Last Login
                      </th>
                      <th className='px-4 py-3 text-left text-sm font-medium'>
                        Created
                      </th>
                      <th className='px-4 py-3 text-right text-sm font-medium'>
                        Actions
                      </th>
                    </tr>
                  </thead>
                  <tbody className='divide-y'>
                    {users.map((user: UserProfile) => (
                      <tr
                        key={user.id}
                        className='hover:bg-muted/50 transition-colors'
                      >
                        <td className='px-4 py-3'>
                          <div className='flex items-center gap-3'>
                            <div className='h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center'>
                              <Users className='h-5 w-5 text-primary' />
                            </div>
                            <div>
                              <p className='font-medium'>
                                {user.firstName} {user.lastName}
                              </p>
                              <div className='flex items-center gap-1 text-xs text-muted-foreground'>
                                <Mail className='h-3 w-3' />
                                {user.email}
                              </div>
                            </div>
                          </div>
                        </td>
                        <td className='px-4 py-3'>
                          <div className='flex items-center gap-2'>
                            <Building2 className='h-4 w-4 text-muted-foreground' />
                            <span className='text-sm'>
                              {user.organizationName || 'N/A'}
                            </span>
                          </div>
                        </td>
                        <td className='px-4 py-3'>
                          <span className='text-sm'>{user.userType}</span>
                        </td>
                        <td className='px-4 py-3'>
                          <AdminStatusBadge status={user.status} />
                        </td>
                        <td className='px-4 py-3'>
                          <span className='text-sm text-muted-foreground'>
                            {formatDate(user.lastLoginAt)}
                          </span>
                        </td>
                        <td className='px-4 py-3'>
                          <span className='text-sm text-muted-foreground'>
                            {formatDate(user.createdAt)}
                          </span>
                        </td>
                        <td className='px-4 py-3 text-right'>
                          <AdminActionMenu
                            items={[
                              {
                                label: 'View Profile',
                                onClick: () => console.log('View', user.id),
                                icon: <Eye className='h-4 w-4' />,
                              },
                              {
                                label: 'Edit',
                                onClick: () => console.log('Edit', user.id),
                                icon: <Edit className='h-4 w-4' />,
                              },
                              {
                                label:
                                  user.status === 'ACTIVE'
                                    ? 'Suspend'
                                    : 'Activate',
                                onClick: () =>
                                  console.log('Toggle status', user.id),
                                icon:
                                  user.status === 'ACTIVE' ? (
                                    <Ban className='h-4 w-4' />
                                  ) : (
                                    <CheckCircle className='h-4 w-4' />
                                  ),
                                separator: true,
                                variant:
                                  user.status === 'ACTIVE'
                                    ? 'destructive'
                                    : 'default',
                              },
                            ]}
                          />
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Pagination */}
              <div className='flex items-center justify-between border-t px-4 py-4'>
                <div className='text-sm text-muted-foreground'>
                  Showing {users.length} of {response.data.totalItems} users
                </div>

                <div className='flex items-center gap-2'>
                  <Button
                    variant='outline'
                    size='sm'
                    onClick={() => handlePageChange(filters.page! - 1)}
                    disabled={filters.page === 0 || isFetching}
                  >
                    <ChevronLeft className='h-4 w-4' />
                    Previous
                  </Button>

                  <div className='text-sm'>
                    Page {currentPage + 1} of {totalPages}
                  </div>

                  <Button
                    variant='outline'
                    size='sm'
                    onClick={() => handlePageChange(filters.page! + 1)}
                    disabled={currentPage >= totalPages - 1 || isFetching}
                  >
                    Next
                    <ChevronRight className='h-4 w-4' />
                  </Button>
                </div>
              </div>
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
