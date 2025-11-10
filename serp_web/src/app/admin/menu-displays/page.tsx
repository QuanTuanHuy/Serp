/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Menu Displays management page
 */

'use client';

import React, { useState } from 'react';
import { useMenuDisplays } from '@/modules/admin/hooks/useMenuDisplays';
import { MenuDisplayTree } from '@/modules/admin/components/menu-displays/MenuDisplayTree';
import { MenuDisplayFormDialog } from '@/modules/admin/components/menu-displays/MenuDisplayFormDialog';
import { RoleAssignmentDialog } from '@/modules/admin/components/menu-displays/RoleAssignmentDialog';
import { MenuDisplayDetailsDialog } from '@/modules/admin/components/menu-displays/MenuDisplayDetailsDialog';
import { Card, Button, Input } from '@/shared/components';
import {
  Menu,
  Plus,
  Search,
  ChevronDown,
  ChevronRight,
  Loader2,
  Eye,
  EyeOff,
  BarChart3,
} from 'lucide-react';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { Badge } from '@/shared/components/ui/badge';
import { useGetModulesQuery } from '@/modules/admin/services/adminApi';
import { useDebounce } from '@/shared/hooks/use-debounce';

export default function MenuDisplaysPage() {
  const [searchInput, setSearchInput] = useState('');
  const debouncedSearch = useDebounce(searchInput, 500);

  const {
    menuDisplays,
    menuTree,
    stats,
    isLoading,
    error,
    isDialogOpen,
    isCreating,
    selectedMenuDisplay,
    filters,
    expandedNodes,
    handleSearch,
    handleModuleFilter,
    handleClearFilters,
    openCreateDialog,
    openEditDialog,
    closeDialog,
    toggleNode,
    expandAll,
    collapseAll,
    submitMenuDisplay,
    handleDeleteMenuDisplay,
    isCreatingMenuDisplay,
    isUpdatingMenuDisplay,
    pagination,
    handlePageChange,
    handlePageSizeChange,
    handleAssignRole,
    handleUnassignRole,
    isAssigningRole,
    isUnassigningRole,
    roleDialogOpen,
    selectedMenuForRole,
    openRoleDialog,
    closeRoleDialog,
    detailsDialogOpen,
    selectedMenuForDetails,
    openDetailsDialog,
    closeDetailsDialog,
  } = useMenuDisplays();

  const { data: modules = [] } = useGetModulesQuery();

  React.useEffect(() => {
    handleSearch(debouncedSearch);
  }, [debouncedSearch, handleSearch]);

  return (
    <div className='space-y-4 sm:space-y-6 px-4 sm:px-6 lg:px-8'>
      {/* Page Header */}
      <div className='flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4'>
        <div>
          <h1 className='text-2xl sm:text-3xl font-bold tracking-tight'>
            Menu Displays
          </h1>
          <p className='text-muted-foreground mt-1 sm:mt-2'>
            Manage navigation menus and their hierarchies
          </p>
        </div>
        <div className='flex gap-2'>
          <Button variant='outline' size='sm' onClick={expandAll}>
            <ChevronDown className='h-4 w-4 mr-2' />
            Expand All
          </Button>
          <Button variant='outline' size='sm' onClick={collapseAll}>
            <ChevronRight className='h-4 w-4 mr-2' />
            Collapse All
          </Button>
          <Button size='sm' onClick={openCreateDialog}>
            <Plus className='h-4 w-4 mr-2' />
            Create Menu
          </Button>
        </div>
      </div>

      {/* Stats Grid */}
      <div className='grid gap-4 grid-cols-1 sm:grid-cols-2 lg:grid-cols-4'>
        <Card className='p-4'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm font-medium text-muted-foreground'>
                Total Menus
              </p>
              <p className='text-2xl font-bold mt-1'>{stats.total}</p>
            </div>
            <div className='h-10 w-10 rounded-lg bg-primary/10 flex items-center justify-center'>
              <Menu className='h-5 w-5 text-primary' />
            </div>
          </div>
        </Card>

        <Card className='p-4'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm font-medium text-muted-foreground'>
                Visible
              </p>
              <p className='text-2xl font-bold mt-1'>{stats.visible}</p>
            </div>
            <div className='h-10 w-10 rounded-lg bg-green-500/10 flex items-center justify-center'>
              <Eye className='h-5 w-5 text-green-600' />
            </div>
          </div>
        </Card>

        <Card className='p-4'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm font-medium text-muted-foreground'>
                Hidden
              </p>
              <p className='text-2xl font-bold mt-1'>{stats.hidden}</p>
            </div>
            <div className='h-10 w-10 rounded-lg bg-orange-500/10 flex items-center justify-center'>
              <EyeOff className='h-5 w-5 text-orange-600' />
            </div>
          </div>
        </Card>

        <Card className='p-4'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm font-medium text-muted-foreground'>
                Modules
              </p>
              <p className='text-2xl font-bold mt-1'>
                {Object.keys(stats.byModule).length}
              </p>
            </div>
            <div className='h-10 w-10 rounded-lg bg-blue-500/10 flex items-center justify-center'>
              <BarChart3 className='h-5 w-5 text-blue-600' />
            </div>
          </div>
        </Card>
      </div>

      {/* Filters Card */}
      <Card>
        <div className='p-4'>
          <div className='grid gap-4 md:grid-cols-3'>
            {/* Search */}
            <div className='md:col-span-2'>
              <div className='relative'>
                <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
                <Input
                  placeholder='Search by name, path, description, or module...'
                  value={searchInput}
                  onChange={(e) => setSearchInput(e.target.value)}
                  className='pl-10'
                />
              </div>
            </div>

            {/* Module Filter */}
            <div>
              <Select
                value={filters.moduleId?.toString() || 'all'}
                onValueChange={(value) =>
                  handleModuleFilter(
                    value === 'all' ? undefined : parseInt(value)
                  )
                }
              >
                <SelectTrigger>
                  <SelectValue placeholder='All Modules' />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value='all'>All Modules</SelectItem>
                  {modules.map((module) => (
                    <SelectItem key={module.id} value={module.id.toString()}>
                      {module.code}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* Active Filters */}
          {(filters.search || filters.moduleId) && (
            <div className='flex items-center gap-2 mt-3 pt-3 border-t'>
              <span className='text-sm text-muted-foreground'>
                Active filters:
              </span>
              {filters.search && (
                <Badge variant='secondary'>Search: {filters.search}</Badge>
              )}
              {filters.moduleId && (
                <Badge variant='secondary'>
                  Module: {modules.find((m) => m.id === filters.moduleId)?.code}
                </Badge>
              )}
              <Button
                variant='ghost'
                size='sm'
                onClick={() => {
                  setSearchInput('');
                  handleClearFilters();
                }}
                className='ml-auto'
              >
                Clear all
              </Button>
            </div>
          )}
        </div>
      </Card>

      {/* Menu Tree */}
      <Card>
        <div className='p-6'>
          {isLoading ? (
            <div className='flex items-center justify-center h-64'>
              <div className='flex flex-col items-center gap-3'>
                <Loader2 className='h-8 w-8 animate-spin text-muted-foreground' />
                <p className='text-sm text-muted-foreground'>
                  Loading menu displays...
                </p>
              </div>
            </div>
          ) : error ? (
            <div className='flex items-center justify-center h-64'>
              <div className='flex flex-col items-center gap-3 text-center'>
                <div className='h-12 w-12 rounded-full bg-destructive/10 flex items-center justify-center'>
                  <Menu className='h-6 w-6 text-destructive' />
                </div>
                <div>
                  <p className='font-medium text-destructive'>
                    Failed to load menu displays
                  </p>
                  <p className='text-sm text-muted-foreground mt-1'>
                    Please try refreshing the page
                  </p>
                </div>
              </div>
            </div>
          ) : menuTree.length === 0 ? (
            <div className='flex flex-col items-center justify-center text-center px-4 py-12'>
              <div className='h-16 w-16 rounded-full bg-muted flex items-center justify-center mb-4'>
                <Menu className='h-8 w-8 text-muted-foreground' />
              </div>
              <h3 className='text-lg font-medium'>No menu displays yet</h3>
              <p className='text-sm text-muted-foreground mt-2 max-w-sm'>
                {filters.search || filters.moduleId
                  ? 'No menus match your current filters. Try adjusting your search criteria.'
                  : 'Create your first menu display to get started with navigation management.'}
              </p>
              {!filters.search && !filters.moduleId && (
                <Button size='sm' className='mt-4' onClick={openCreateDialog}>
                  <Plus className='h-4 w-4 mr-2' />
                  Create Menu
                </Button>
              )}
            </div>
          ) : (
            <div>
              <div className='mb-4 pb-3 border-b'>
                <p className='text-sm text-muted-foreground'>
                  Showing {menuTree.length} top-level menu
                  {menuTree.length !== 1 ? 's' : ''} ({pagination.totalItems}{' '}
                  total)
                </p>
              </div>
              <MenuDisplayTree
                nodes={menuTree}
                expandedNodes={expandedNodes}
                onToggle={toggleNode}
                onEdit={openEditDialog}
                onDelete={handleDeleteMenuDisplay}
                onAssignRoles={openRoleDialog}
                onViewDetails={openDetailsDialog}
              />

              {/* Pagination Controls */}
              <div className='flex flex-col sm:flex-row items-center justify-between gap-3 mt-6'>
                <p className='text-sm text-muted-foreground'>
                  Page {pagination.currentPage + 1} of {pagination.totalPages}
                </p>

                <div className='flex items-center gap-2'>
                  <Button
                    variant='outline'
                    size='sm'
                    disabled={pagination.currentPage <= 0}
                    onClick={() => handlePageChange(pagination.currentPage - 1)}
                  >
                    Previous
                  </Button>
                  <Button
                    variant='outline'
                    size='sm'
                    disabled={
                      pagination.totalPages === 0 ||
                      pagination.currentPage >= pagination.totalPages - 1
                    }
                    onClick={() => handlePageChange(pagination.currentPage + 1)}
                  >
                    Next
                  </Button>

                  <div className='flex items-center gap-2 ml-2'>
                    <span className='text-sm text-muted-foreground'>Rows:</span>
                    <Select
                      value={String(pagination.pageSize)}
                      onValueChange={(v) => handlePageSizeChange(parseInt(v))}
                    >
                      <SelectTrigger className='w-[90px]'>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {[10, 20, 50, 100].map((size) => (
                          <SelectItem key={size} value={String(size)}>
                            {size}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      </Card>

      {/* Form Dialog */}
      <MenuDisplayFormDialog
        open={isDialogOpen}
        onOpenChange={(open) => {
          if (!open) closeDialog();
        }}
        menuDisplay={selectedMenuDisplay}
        onSubmit={submitMenuDisplay}
        isLoading={isCreatingMenuDisplay || isUpdatingMenuDisplay}
        allMenuDisplays={menuDisplays}
      />

      {/* Role Assignment Dialog */}
      <RoleAssignmentDialog
        open={roleDialogOpen}
        onOpenChange={closeRoleDialog}
        menuDisplay={selectedMenuForRole}
        onAssign={handleAssignRole}
        onUnassign={handleUnassignRole}
        isLoading={isAssigningRole || isUnassigningRole}
      />

      {/* Details Dialog */}
      <MenuDisplayDetailsDialog
        open={detailsDialogOpen}
        onOpenChange={closeDetailsDialog}
        menuDisplay={selectedMenuForDetails}
      />
    </div>
  );
}
