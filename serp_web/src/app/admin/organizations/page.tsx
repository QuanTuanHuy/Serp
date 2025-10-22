/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Organizations management page
 */

'use client';

import React, { useState } from 'react';
import {
  useGetOrganizationsQuery,
  AdminStatusBadge,
  AdminActionMenu,
} from '@/modules/admin';
import type { OrganizationFilters } from '@/modules/admin';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Input } from '@/shared/components/ui/input';
import { Button } from '@/shared/components/ui/button';
import {
  Building2,
  Search,
  Eye,
  Edit,
  Ban,
  CheckCircle,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react';
import { Organization } from '@/modules/admin/types';

export default function OrganizationsPage() {
  const [filters, setFilters] = useState<OrganizationFilters>({
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
  } = useGetOrganizationsQuery(filters);

  const organizations = response?.data.items || [];
  const totalPages = response?.data.totalPages || 0;
  const currentPage = response?.data.currentPage || 0;

  const handleSearch = (search: string) => {
    setFilters({ ...filters, search, page: 0 });
  };

  const handleFilterChange = (key: keyof OrganizationFilters, value: any) => {
    setFilters({ ...filters, [key]: value, page: 0 });
  };

  const handlePageChange = (newPage: number) => {
    setFilters({ ...filters, page: newPage });
  };

  const formatDate = (isoDate?: string) => {
    if (!isoDate) return 'N/A';
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
          <h1 className='text-3xl font-bold tracking-tight'>Organizations</h1>
          <p className='text-muted-foreground mt-2'>
            Manage and monitor all organizations in the system
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
          <div className='grid gap-4 md:grid-cols-4'>
            {/* Search */}
            <div className='md:col-span-2'>
              <div className='relative'>
                <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
                <Input
                  placeholder='Search by name, code, email...'
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
                <option value='TRIAL'>Trial</option>
                <option value='SUSPENDED'>Suspended</option>
                <option value='INACTIVE'>Inactive</option>
              </select>
            </div>

            {/* Type Filter */}
            <div>
              <select
                value={filters.type || ''}
                onChange={(e) =>
                  handleFilterChange('type', e.target.value || undefined)
                }
                className='w-full h-10 rounded-md border border-input bg-background px-3 py-2 text-sm'
              >
                <option value=''>All Types</option>
                <option value='ENTERPRISE'>Enterprise</option>
                <option value='SMB'>SMB</option>
                <option value='STARTUP'>Startup</option>
                <option value='INDIVIDUAL'>Individual</option>
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
              <div className='text-muted-foreground'>
                Loading organizations...
              </div>
            </div>
          )}

          {/* Error State */}
          {error && (
            <div className='flex items-center justify-center h-64'>
              <div className='text-destructive'>
                Failed to load organizations
              </div>
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
                        Organization
                      </th>
                      <th className='px-4 py-3 text-left text-sm font-medium'>
                        Type
                      </th>
                      <th className='px-4 py-3 text-left text-sm font-medium'>
                        Status
                      </th>
                      <th className='px-4 py-3 text-left text-sm font-medium'>
                        Employees
                      </th>
                      <th className='px-4 py-3 text-left text-sm font-medium'>
                        Created
                      </th>
                      <th className='px-4 py-3 text-left text-sm font-medium'>
                        Subscription
                      </th>
                      <th className='px-4 py-3 text-right text-sm font-medium'>
                        Actions
                      </th>
                    </tr>
                  </thead>
                  <tbody className='divide-y'>
                    {organizations.map((org: Organization) => (
                      <tr
                        key={org.id}
                        className='hover:bg-muted/50 transition-colors'
                      >
                        <td className='px-4 py-3'>
                          <div className='flex items-center gap-3'>
                            <div className='h-10 w-10 rounded-lg bg-primary/10 flex items-center justify-center'>
                              <Building2 className='h-5 w-5 text-primary' />
                            </div>
                            <div>
                              <p className='font-medium'>{org.name}</p>
                              <p className='text-xs text-muted-foreground'>
                                {org.code}
                              </p>
                            </div>
                          </div>
                        </td>
                        <td className='px-4 py-3'>
                          <span className='text-sm'>
                            {org.organizationType}
                          </span>
                        </td>
                        <td className='px-4 py-3'>
                          <AdminStatusBadge status={org.status} />
                        </td>
                        <td className='px-4 py-3'>
                          <span className='text-sm'>
                            {org.employeeCount || 'N/A'}
                          </span>
                        </td>
                        <td className='px-4 py-3'>
                          <span className='text-sm text-muted-foreground'>
                            {formatDate(org.createdAt)}
                          </span>
                        </td>
                        <td className='px-4 py-3'>
                          <span className='text-sm text-muted-foreground'>
                            {org.subscriptionExpiresAt
                              ? `Expires ${formatDate(org.subscriptionExpiresAt)}`
                              : 'No subscription'}
                          </span>
                        </td>
                        <td className='px-4 py-3 text-right'>
                          <AdminActionMenu
                            items={[
                              {
                                label: 'View Details',
                                onClick: () => console.log('View', org.id),
                                icon: <Eye className='h-4 w-4' />,
                              },
                              {
                                label: 'Edit',
                                onClick: () => console.log('Edit', org.id),
                                icon: <Edit className='h-4 w-4' />,
                              },
                              {
                                label:
                                  org.status === 'ACTIVE'
                                    ? 'Suspend'
                                    : 'Activate',
                                onClick: () =>
                                  console.log('Toggle status', org.id),
                                icon:
                                  org.status === 'ACTIVE' ? (
                                    <Ban className='h-4 w-4' />
                                  ) : (
                                    <CheckCircle className='h-4 w-4' />
                                  ),
                                separator: true,
                                variant:
                                  org.status === 'ACTIVE'
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
                  Showing {organizations.length} of {response.data.totalItems}{' '}
                  organizations
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
