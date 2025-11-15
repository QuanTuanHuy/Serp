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
  Filter,
  Eye,
  ChevronLeft,
  ChevronRight,
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
import { Avatar, AvatarFallback } from '@/shared/components/ui/avatar';
import { Badge } from '@/shared/components/ui/badge';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import {
  SettingsStatsCard,
  SettingsActionMenu,
  SettingsStatusBadge,
  CreateDepartmentDialog,
  UpdateDepartmentDialog,
  DeleteDepartmentDialog,
  DepartmentDetailDialog,
  useSettingsDepartments,
} from '@/modules/settings';
import { Separator } from '@/shared/components/ui/separator';
import type { Department } from '@/modules/settings';

export default function SettingsDepartmentsPage() {
  const {
    organizationId,
    isLoading,
    departments,
    activeDepartments,
    totalPages,
    totalItems,
    currentPage,
    statistics,
    activeModules,
    managers,
    search,
    filters,
    setSearch,
    setPage,
    setActiveFilter,
    setParentFilter,
    clearFilters,
    create,
    update,
    remove,
    createStatus,
    updateStatus,
    deleteStatus,
    useDepartmentMembers,
  } = useSettingsDepartments();

  // Dialog states
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [updateDialogOpen, setUpdateDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [detailDialogOpen, setDetailDialogOpen] = useState(false);
  const [selectedDepartment, setSelectedDepartment] =
    useState<Department | null>(null);

  const [showFilters, setShowFilters] = useState(false);

  const handleEdit = (dept: Department) => {
    setSelectedDepartment(dept);
    setUpdateDialogOpen(true);
  };

  const handleDelete = (dept: Department) => {
    setSelectedDepartment(dept);
    setDeleteDialogOpen(true);
  };

  const handleViewDetails = (dept: Department) => {
    setSelectedDepartment(dept);
    setDetailDialogOpen(true);
  };

  const handleAssignManager = (dept: Department) => {
    setSelectedDepartment(dept);
    setUpdateDialogOpen(true);
  };

  if (!organizationId) {
    return (
      <div className='flex items-center justify-center h-96'>
        <p className='text-muted-foreground'>
          Please log in to view departments
        </p>
      </div>
    );
  }

  const hasFilters =
    filters.isActive !== undefined || filters.parentDepartmentId;

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
        <Button
          className='bg-purple-600 hover:bg-purple-700 cursor-pointer'
          onClick={() => setCreateDialogOpen(true)}
        >
          <Plus className='h-4 w-4' />
          New
        </Button>
      </div>

      {/* Stats Grid */}
      <div className='grid gap-4 md:grid-cols-2 lg:grid-cols-4'>
        <SettingsStatsCard
          title='Total Departments'
          value={statistics?.totalDepartments || 0}
          description='Active departments'
          icon={<Layers className='h-4 w-4' />}
        />

        <SettingsStatsCard
          title='Total Members'
          value={statistics?.totalMembers || 0}
          description='Across all departments'
          icon={<Users className='h-4 w-4' />}
        />

        <SettingsStatsCard
          title='Avg Team Size'
          value={statistics?.averageTeamSize || 0}
          description='Members per department'
          icon={<Users className='h-4 w-4' />}
        />

        <SettingsStatsCard
          title='With Managers'
          value={statistics?.departmentsWithManagers || 0}
          description='Departments assigned'
          icon={<UserPlus className='h-4 w-4' />}
        />
      </div>

      {/* Department List */}
      <Card>
        <CardHeader>
          <div className='flex flex-col gap-4'>
            <div className='flex items-center justify-between'>
              <div>
                <CardTitle>All Departments</CardTitle>
                <CardDescription>
                  {totalItems} department{totalItems !== 1 ? 's' : ''} found
                </CardDescription>
              </div>
              <Button
                variant='outline'
                size='sm'
                onClick={() => setShowFilters(!showFilters)}
              >
                <Filter className='h-4 w-4 mr-2' />
                Filters
                {hasFilters && (
                  <Badge variant='secondary' className='ml-2'>
                    Active
                  </Badge>
                )}
              </Button>
            </div>

            {/* Search & Filters */}
            <div className='space-y-3'>
              <div className='relative'>
                <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
                <Input
                  placeholder='Search departments...'
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  className='pl-10'
                />
              </div>

              {showFilters && (
                <div className='flex flex-wrap gap-3 p-4 bg-muted/50 rounded-lg'>
                  <Select
                    value={
                      filters.isActive === undefined
                        ? 'all'
                        : filters.isActive
                          ? 'active'
                          : 'inactive'
                    }
                    onValueChange={(value) =>
                      setActiveFilter(
                        value === 'all'
                          ? undefined
                          : value === 'active'
                            ? true
                            : false
                      )
                    }
                  >
                    <SelectTrigger className='w-[180px]'>
                      <SelectValue placeholder='Status' />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value='all'>All Status</SelectItem>
                      <SelectItem value='active'>Active Only</SelectItem>
                      <SelectItem value='inactive'>Inactive Only</SelectItem>
                    </SelectContent>
                  </Select>

                  <Select
                    value={filters.parentDepartmentId?.toString() || 'all'}
                    onValueChange={(value) =>
                      setParentFilter(
                        value === 'all' ? undefined : Number(value)
                      )
                    }
                  >
                    <SelectTrigger className='w-[220px]'>
                      <SelectValue placeholder='Parent Department' />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value='all'>All Departments</SelectItem>
                      <SelectItem value='0'>Top Level Only</SelectItem>
                      {activeDepartments.map((dept) => (
                        <SelectItem key={dept.id} value={dept.id.toString()}>
                          Under {dept.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>

                  {hasFilters && (
                    <Button variant='ghost' size='sm' onClick={clearFilters}>
                      Clear Filters
                    </Button>
                  )}
                </div>
              )}
            </div>
          </div>
        </CardHeader>
        <CardContent className='space-y-4'>
          {/* Loading State */}
          {isLoading && (
            <div className='text-center py-12'>
              <p className='text-muted-foreground'>Loading departments...</p>
            </div>
          )}

          {/* Department Cards */}
          {!isLoading && departments.length > 0 && (
            <>
              <div className='grid gap-4 md:grid-cols-2 lg:grid-cols-3'>
                {departments.map((dept) => (
                  <Card
                    key={dept.id}
                    className='hover:shadow-md transition-shadow'
                  >
                    <CardHeader className='pb-3'>
                      <div className='flex items-start justify-between'>
                        <div
                          className='flex items-center gap-3 flex-1 cursor-pointer'
                          onClick={() => handleViewDetails(dept)}
                        >
                          <div className='h-10 w-10 rounded-lg bg-purple-100 dark:bg-purple-900 flex items-center justify-center flex-shrink-0'>
                            <Layers className='h-5 w-5 text-purple-600 dark:text-purple-400' />
                          </div>
                          <div className='min-w-0 flex-1'>
                            <CardTitle className='text-base truncate'>
                              {dept.name}
                            </CardTitle>
                            {dept.parentDepartmentName && (
                              <p className='text-xs text-muted-foreground truncate'>
                                {dept.parentDepartmentName} → {dept.name}
                              </p>
                            )}
                          </div>
                        </div>
                        <SettingsActionMenu
                          items={[
                            {
                              label: 'View Details',
                              onClick: () => handleViewDetails(dept),
                              icon: <Eye className='h-4 w-4' />,
                            },
                            {
                              label: 'Edit Department',
                              onClick: () => handleEdit(dept),
                              icon: <Edit className='h-4 w-4' />,
                            },
                            {
                              label: dept.managerId
                                ? 'Change Manager'
                                : 'Assign Manager',
                              onClick: () => handleAssignManager(dept),
                              icon: <UserPlus className='h-4 w-4' />,
                            },
                            {
                              label: 'Delete',
                              onClick: () => handleDelete(dept),
                              icon: <Trash2 className='h-4 w-4' />,
                              variant: 'destructive',
                              separator: true,
                            },
                          ]}
                          triggerIcon={<MoreVertical className='h-4 w-4' />}
                        />
                      </div>
                    </CardHeader>
                    <CardContent
                      className='space-y-3'
                      onClick={() => handleViewDetails(dept)}
                    >
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
                            {dept.memberCount || 0}{' '}
                            {dept.memberCount === 1 ? 'member' : 'members'}
                          </span>
                        </div>
                        <SettingsStatusBadge
                          status={dept.isActive ? 'ACTIVE' : 'INACTIVE'}
                        />
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
                            <div className='min-w-0 flex-1'>
                              <p className='text-xs text-muted-foreground'>
                                Manager
                              </p>
                              <p className='text-sm font-medium truncate'>
                                {dept.managerName}
                              </p>
                            </div>
                          </div>
                        </>
                      )}
                    </CardContent>
                  </Card>
                ))}
              </div>

              {/* Pagination */}
              {totalPages > 1 && (
                <div className='flex items-center justify-between pt-4'>
                  <p className='text-sm text-muted-foreground'>
                    Page {currentPage + 1} of {totalPages}
                  </p>
                  <div className='flex gap-2'>
                    <Button
                      variant='outline'
                      size='sm'
                      onClick={() => setPage(currentPage - 1)}
                      disabled={currentPage === 0}
                    >
                      <ChevronLeft className='h-4 w-4 mr-1' />
                      Previous
                    </Button>
                    <Button
                      variant='outline'
                      size='sm'
                      onClick={() => setPage(currentPage + 1)}
                      disabled={currentPage >= totalPages - 1}
                    >
                      Next
                      <ChevronRight className='h-4 w-4 ml-1' />
                    </Button>
                  </div>
                </div>
              )}
            </>
          )}

          {/* Empty State */}
          {!isLoading && departments.length === 0 && (
            <div className='text-center py-12'>
              <Building2 className='h-12 w-12 mx-auto text-muted-foreground mb-4' />
              <h3 className='text-lg font-semibold mb-2'>
                No departments found
              </h3>
              <p className='text-muted-foreground mb-4'>
                {search || hasFilters
                  ? 'Try adjusting your search or filters'
                  : 'Get started by creating your first department'}
              </p>
              {!search && !hasFilters && (
                <Button
                  className='bg-purple-600 hover:bg-purple-700'
                  onClick={() => setCreateDialogOpen(true)}
                >
                  <Plus className='h-4 w-4 mr-2' />
                  Create Department
                </Button>
              )}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Dialogs */}
      <CreateDepartmentDialog
        open={createDialogOpen}
        onOpenChange={setCreateDialogOpen}
        onSubmit={create}
        isLoading={createStatus.isLoading}
        departments={activeDepartments}
        managers={managers}
        modules={activeModules}
      />

      <UpdateDepartmentDialog
        open={updateDialogOpen}
        onOpenChange={setUpdateDialogOpen}
        organizationId={organizationId}
        department={selectedDepartment}
        departments={activeDepartments}
        managers={managers}
      />

      <DeleteDepartmentDialog
        open={deleteDialogOpen}
        onOpenChange={setDeleteDialogOpen}
        department={selectedDepartment}
        onConfirm={remove}
        isLoading={deleteStatus.isLoading}
      />

      <DepartmentDetailDialog
        open={detailDialogOpen}
        onOpenChange={setDetailDialogOpen}
        department={selectedDepartment}
        useDepartmentMembers={useDepartmentMembers}
      />
    </div>
  );
}
