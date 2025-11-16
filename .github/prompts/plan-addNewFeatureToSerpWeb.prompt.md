# Plan: Thêm chức năng mới cho serp_web

Để thêm một chức năng mới vào `serp_web`, bạn cần thực hiện các bước theo kiến trúc modular đã định nghĩa, sử dụng RTK Query, Redux Toolkit, và Shadcn UI.

## Steps

### 1. Define types
Tạo file types trong `src/modules/{module}/types/{feature}.types.ts` với interfaces cho entity, request DTOs, response types, và filters. Export qua `index.ts` của thư mục types.

**Ví dụ:** `src/modules/ptm/types/project.types.ts`
```typescript
export interface Project {
  id: number;
  name: string;
  description?: string;
  status: 'ACTIVE' | 'COMPLETED' | 'ARCHIVED';
  ownerId: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateProjectRequest {
  name: string;
  description?: string;
  ownerId: number;
}

export interface UpdateProjectRequest {
  name?: string;
  description?: string;
  status?: string;
}

export interface ProjectFilters extends SearchParams {
  search?: string;
  status?: string;
  ownerId?: number;
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortDir?: 'ASC' | 'DESC';
}

export type ProjectResponse = ApiResponse<Project>;
export type ProjectsResponse = PaginatedResponse<Project>;
```

**Update:** `src/modules/{module}/types/index.ts`
```typescript
export type * from './project.types';
```

### 2. Create RTK Query API service
Tạo file `src/modules/{module}/services/{feature}/{feature}Api.ts` với `api.injectEndpoints()`, định nghĩa `query`/`mutation` endpoints, thêm `providesTags`/`invalidatesTags` để cache invalidation. Export hooks và thêm tag type vào `src/lib/store/api/baseApi.ts`. Barrel export qua `services/{module}Api.ts`.

**Ví dụ:** `src/modules/ptm/services/projects/projectsApi.ts`
```typescript
import { api } from '@/lib/store/api';
import { createDataTransform, createPaginatedTransform } from '@/lib/store/api/utils';
import type { 
  Project, 
  CreateProjectRequest, 
  UpdateProjectRequest,
  ProjectFilters,
  ProjectResponse,
  ProjectsResponse 
} from '../../types';

export const projectsApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getProjects: builder.query<ProjectsResponse, ProjectFilters>({
      query: (params) => ({
        url: '/projects',
        method: 'GET',
        params,
      }),
      transformResponse: createPaginatedTransform<Project>(),
      providesTags: (result) =>
        result?.data.items
          ? [
              ...result.data.items.map(({ id }) => ({ type: 'ptm/Project' as const, id })),
              { type: 'ptm/Project', id: 'LIST' }
            ]
          : [{ type: 'ptm/Project', id: 'LIST' }],
    }),
    
    getProjectById: builder.query<ProjectResponse, number>({
      query: (id) => ({ url: `/projects/${id}`, method: 'GET' }),
      transformResponse: createDataTransform<Project>(),
      providesTags: (result, error, id) => [{ type: 'ptm/Project', id }],
    }),
    
    createProject: builder.mutation<ProjectResponse, CreateProjectRequest>({
      query: (body) => ({ url: '/projects', method: 'POST', body }),
      transformResponse: createDataTransform<Project>(),
      invalidatesTags: [{ type: 'ptm/Project', id: 'LIST' }],
    }),
    
    updateProject: builder.mutation<ProjectResponse, { projectId: number; data: UpdateProjectRequest }>({
      query: ({ projectId, data }) => ({ 
        url: `/projects/${projectId}`, 
        method: 'PATCH', 
        body: data 
      }),
      transformResponse: createDataTransform<Project>(),
      invalidatesTags: (result, error, { projectId }) => [
        { type: 'ptm/Project', id: projectId },
        { type: 'ptm/Project', id: 'LIST' }
      ],
    }),
    
    deleteProject: builder.mutation<void, number>({
      query: (projectId) => ({ url: `/projects/${projectId}`, method: 'DELETE' }),
      invalidatesTags: (result, error, projectId) => [
        { type: 'ptm/Project', id: projectId },
        { type: 'ptm/Project', id: 'LIST' }
      ],
    }),
  }),
});

export const { 
  useGetProjectsQuery, 
  useGetProjectByIdQuery,
  useCreateProjectMutation,
  useUpdateProjectMutation,
  useDeleteProjectMutation,
} = projectsApi;
```

**Update:** `src/lib/store/api/baseApi.ts` (thêm tag type)
```typescript
tagTypes: [
  // ... existing tags
  'ptm/Project', // ✅ Add new tag
],
```

**Update:** `src/modules/ptm/services/ptmApi.ts` (barrel export)
```typescript
export { 
  projectsApi, 
  useGetProjectsQuery, 
  useGetProjectByIdQuery,
  useCreateProjectMutation,
  useUpdateProjectMutation,
  useDeleteProjectMutation,
} from './projects/projectsApi';
```

### 3. Create Redux slice for UI state
Tạo `src/modules/{module}/store/{feature}/{feature}Slice.ts` với state cho filters, dialog open/close, selected items. Thêm reducers và selectors. Update `store/index.ts` để combine reducer và export actions. Update `src/lib/store/index.ts` để include trong root reducer.

**Ví dụ:** `src/modules/ptm/store/projects/projectsSlice.ts`
```typescript
import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { RootState } from '@/lib/store';
import type { ProjectFilters } from '../../types';

export interface ProjectsUiState {
  filters: ProjectFilters;
  selectedProjectId: number | null;
  dialogOpen: boolean;
  dialogMode: 'create' | 'edit' | 'view';
  viewMode: 'list' | 'grid';
}

const initialState: ProjectsUiState = {
  filters: {
    page: 0,
    pageSize: 10,
    sortBy: 'createdAt',
    sortDir: 'DESC',
  },
  selectedProjectId: null,
  dialogOpen: false,
  dialogMode: 'create',
  viewMode: 'list',
};

const projectsSlice = createSlice({
  name: 'ptm/projectsUi',
  initialState,
  reducers: {
    setSearch(state, action: PayloadAction<string | undefined>) {
      state.filters.search = action.payload;
      state.filters.page = 0; // Reset page on search
    },
    setStatus(state, action: PayloadAction<string | undefined>) {
      state.filters.status = action.payload;
      state.filters.page = 0;
    },
    setPage(state, action: PayloadAction<number>) {
      state.filters.page = action.payload;
    },
    setPageSize(state, action: PayloadAction<number>) {
      state.filters.pageSize = action.payload;
      state.filters.page = 0;
    },
    setSorting(state, action: PayloadAction<{ sortBy: string; sortDir: 'ASC' | 'DESC' }>) {
      state.filters.sortBy = action.payload.sortBy;
      state.filters.sortDir = action.payload.sortDir;
    },
    setDialogOpen(state, action: PayloadAction<boolean>) {
      state.dialogOpen = action.payload;
      if (!action.payload) {
        state.selectedProjectId = null;
        state.dialogMode = 'create';
      }
    },
    openCreateDialog(state) {
      state.dialogOpen = true;
      state.dialogMode = 'create';
      state.selectedProjectId = null;
    },
    openEditDialog(state, action: PayloadAction<number>) {
      state.dialogOpen = true;
      state.dialogMode = 'edit';
      state.selectedProjectId = action.payload;
    },
    openViewDialog(state, action: PayloadAction<number>) {
      state.dialogOpen = true;
      state.dialogMode = 'view';
      state.selectedProjectId = action.payload;
    },
    setSelectedProject(state, action: PayloadAction<number | null>) {
      state.selectedProjectId = action.payload;
    },
    setViewMode(state, action: PayloadAction<'list' | 'grid'>) {
      state.viewMode = action.payload;
    },
    resetFilters(state) {
      state.filters = initialState.filters;
    },
  },
});

export const {
  setSearch,
  setStatus,
  setPage,
  setPageSize,
  setSorting,
  setDialogOpen,
  openCreateDialog,
  openEditDialog,
  openViewDialog,
  setSelectedProject,
  setViewMode,
  resetFilters,
} = projectsSlice.actions;

// Selectors
export const selectProjectsFilters = (state: RootState) => state.ptm.projects.filters;
export const selectProjectsDialogOpen = (state: RootState) => state.ptm.projects.dialogOpen;
export const selectProjectsDialogMode = (state: RootState) => state.ptm.projects.dialogMode;
export const selectSelectedProjectId = (state: RootState) => state.ptm.projects.selectedProjectId;
export const selectProjectsViewMode = (state: RootState) => state.ptm.projects.viewMode;
export const selectProjectsUiState = (state: RootState) => state.ptm.projects;

export default projectsSlice.reducer;
```

**Update:** `src/modules/ptm/store/index.ts`
```typescript
import { combineReducers } from '@reduxjs/toolkit';
import projectsReducer from './projects/projectsSlice';
// ... other reducers

export const ptmReducer = combineReducers({
  tasks: tasksReducer,
  projects: projectsReducer, // ✅ Add new slice
});

// Re-export actions and selectors
export { 
  setSearch as setProjectsSearch,
  setStatus as setProjectsStatus,
  setPage as setProjectsPage,
  openCreateDialog as openCreateProjectDialog,
  openEditDialog as openEditProjectDialog,
  selectProjectsFilters,
  selectProjectsDialogOpen,
  selectSelectedProjectId,
} from './projects/projectsSlice';
```

**Update:** `src/lib/store/index.ts`
```typescript
import { ptmReducer } from '@/modules/ptm/store';

const rootReducer = combineReducers({
  [api.reducerPath]: api.reducer,
  account: accountReducer,
  admin: adminReducer,
  ptm: ptmReducer, // ✅ Add PTM reducer if not exists
});
```

### 4. Create custom hook
Tạo `src/modules/{module}/hooks/use{Feature}.ts` kết hợp RTK Query hooks, Redux state, notifications, và business logic. Return object với data, loading states, filters, và handler functions. Export qua `hooks/index.ts`.

**Ví dụ:** `src/modules/ptm/hooks/useProjects.ts`
```typescript
import { useCallback, useMemo } from 'react';
import { useAppDispatch, useAppSelector } from '@/shared/hooks';
import { useNotification } from '@/shared/hooks/useNotification';
import { 
  useGetProjectsQuery, 
  useCreateProjectMutation,
  useUpdateProjectMutation,
  useDeleteProjectMutation,
} from '../services/ptmApi';
import {
  selectProjectsFilters,
  selectProjectsDialogOpen,
  selectProjectsDialogMode,
  selectSelectedProjectId,
  setProjectsSearch,
  setProjectsStatus,
  setProjectsPage,
  setProjectsPageSize,
  setSorting as setProjectsSorting,
  openCreateProjectDialog,
  openEditProjectDialog,
  setDialogOpen as setProjectsDialogOpen,
  resetFilters,
} from '../store';
import type { CreateProjectRequest, UpdateProjectRequest } from '../types';
import { getErrorMessage, isSuccessResponse } from '@/lib/store/api/utils';

export function useProjects() {
  const dispatch = useAppDispatch();
  const { success, error: showError } = useNotification();
  
  // Redux state
  const filters = useAppSelector(selectProjectsFilters);
  const dialogOpen = useAppSelector(selectProjectsDialogOpen);
  const dialogMode = useAppSelector(selectProjectsDialogMode);
  const selectedProjectId = useAppSelector(selectSelectedProjectId);
  
  // RTK Query hooks
  const { 
    data: response, 
    isLoading, 
    isFetching, 
    error, 
    refetch 
  } = useGetProjectsQuery(filters);
  
  const [createProject, { isLoading: isCreating }] = useCreateProjectMutation();
  const [updateProject, { isLoading: isUpdating }] = useUpdateProjectMutation();
  const [deleteProject, { isLoading: isDeleting }] = useDeleteProjectMutation();
  
  // Extract data from response
  const projects = useMemo(() => {
    return response && isSuccessResponse(response) 
      ? response.data.items 
      : [];
  }, [response]);
  
  const pagination = useMemo(() => {
    if (!response || !isSuccessResponse(response)) {
      return {
        currentPage: 0,
        totalPages: 0,
        totalItems: 0,
        pageSize: filters.pageSize || 10,
      };
    }
    return {
      currentPage: response.data.currentPage,
      totalPages: response.data.totalPages,
      totalItems: response.data.totalItems,
      pageSize: response.data.pageSize,
    };
  }, [response, filters.pageSize]);
  
  // Filter handlers
  const handleSearch = useCallback(
    (search: string) => dispatch(setProjectsSearch(search || undefined)),
    [dispatch]
  );
  
  const handleStatusChange = useCallback(
    (status: string | undefined) => dispatch(setProjectsStatus(status)),
    [dispatch]
  );
  
  const handlePageChange = useCallback(
    (page: number) => dispatch(setProjectsPage(page)),
    [dispatch]
  );
  
  const handlePageSizeChange = useCallback(
    (pageSize: number) => dispatch(setProjectsPageSize(pageSize)),
    [dispatch]
  );
  
  const handleSortingChange = useCallback(
    (sortBy: string, sortDir: 'ASC' | 'DESC') => 
      dispatch(setProjectsSorting({ sortBy, sortDir })),
    [dispatch]
  );
  
  const handleResetFilters = useCallback(() => {
    dispatch(resetFilters());
  }, [dispatch]);
  
  // Dialog handlers
  const handleOpenCreateDialog = useCallback(() => {
    dispatch(openCreateProjectDialog());
  }, [dispatch]);
  
  const handleOpenEditDialog = useCallback((projectId: number) => {
    dispatch(openEditProjectDialog(projectId));
  }, [dispatch]);
  
  const handleCloseDialog = useCallback(() => {
    dispatch(setProjectsDialogOpen(false));
  }, [dispatch]);
  
  // CRUD handlers
  const handleCreateProject = useCallback(
    async (data: CreateProjectRequest) => {
      try {
        const result = await createProject(data).unwrap();
        if (isSuccessResponse(result)) {
          success('Project created successfully');
          handleCloseDialog();
          await refetch();
          return result.data;
        }
      } catch (err: any) {
        showError(getErrorMessage(err));
        throw err;
      }
    },
    [createProject, refetch, success, showError, handleCloseDialog]
  );
  
  const handleUpdateProject = useCallback(
    async (projectId: number, data: UpdateProjectRequest) => {
      try {
        const result = await updateProject({ projectId, data }).unwrap();
        if (isSuccessResponse(result)) {
          success('Project updated successfully');
          handleCloseDialog();
          await refetch();
          return result.data;
        }
      } catch (err: any) {
        showError(getErrorMessage(err));
        throw err;
      }
    },
    [updateProject, refetch, success, showError, handleCloseDialog]
  );
  
  const handleDeleteProject = useCallback(
    async (projectId: number) => {
      try {
        await deleteProject(projectId).unwrap();
        success('Project deleted successfully');
        await refetch();
      } catch (err: any) {
        showError(getErrorMessage(err));
        throw err;
      }
    },
    [deleteProject, refetch, success, showError]
  );
  
  return {
    // State
    filters,
    projects,
    pagination,
    isLoading,
    isFetching,
    error,
    dialogOpen,
    dialogMode,
    selectedProjectId,
    
    // Filter controls
    handleSearch,
    handleStatusChange,
    handlePageChange,
    handlePageSizeChange,
    handleSortingChange,
    handleResetFilters,
    
    // Dialog controls
    handleOpenCreateDialog,
    handleOpenEditDialog,
    handleCloseDialog,
    
    // CRUD operations
    handleCreateProject,
    handleUpdateProject,
    handleDeleteProject,
    isCreating,
    isUpdating,
    isDeleting,
    
    // Utilities
    refetch,
  };
}

export type UseProjectsReturn = ReturnType<typeof useProjects>;
```

**Update:** `src/modules/ptm/hooks/index.ts`
```typescript
export { useProjects } from './useProjects';
export type { UseProjectsReturn } from './useProjects';
```

### 5. Build React components
Tạo form component (`{Feature}Form.tsx`) với React Hook Form + Zod validation, dialog wrapper (`{Feature}FormDialog.tsx`), và các shared components cần thiết trong `components/{feature}/`. Export qua `components/index.ts`.

**Ví dụ:** `src/modules/ptm/components/projects/ProjectForm.tsx`
```typescript
import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { 
  Card, 
  CardContent, 
  CardHeader, 
  CardTitle,
  Label, 
  Input, 
  Textarea,
  Select,
  SelectTrigger,
  SelectValue,
  SelectContent,
  SelectItem,
  Button 
} from '@/shared/components/ui';
import { Loader2 } from 'lucide-react';
import type { Project } from '../../types';

const projectFormSchema = z.object({
  name: z.string().min(3, 'Name must be at least 3 characters'),
  description: z.string().optional(),
  status: z.enum(['ACTIVE', 'COMPLETED', 'ARCHIVED']).optional(),
});

type ProjectFormData = z.infer<typeof projectFormSchema>;

interface ProjectFormProps {
  project?: Project;
  onSubmit: (data: ProjectFormData) => Promise<void>;
  onCancel?: () => void;
  isLoading?: boolean;
}

export const ProjectForm: React.FC<ProjectFormProps> = ({ 
  project, 
  onSubmit, 
  onCancel,
  isLoading = false 
}) => {
  const { 
    register, 
    handleSubmit, 
    watch,
    formState: { errors } 
  } = useForm<ProjectFormData>({
    resolver: zodResolver(projectFormSchema),
    defaultValues: project ? {
      name: project.name,
      description: project.description || '',
      status: project.status,
    } : {
      name: '',
      description: '',
      status: 'ACTIVE',
    },
  });
  
  const handleFormSubmit = handleSubmit(async (data) => {
    await onSubmit(data);
  });
  
  return (
    <form onSubmit={handleFormSubmit} className='space-y-6'>
      <Card>
        <CardHeader>
          <CardTitle>{project ? 'Edit Project' : 'Create New Project'}</CardTitle>
        </CardHeader>
        <CardContent className='space-y-4'>
          <div className='space-y-2'>
            <Label htmlFor='name'>Project Name *</Label>
            <Input 
              id='name'
              {...register('name')} 
              placeholder='Enter project name'
              disabled={isLoading}
            />
            {errors.name && (
              <p className='text-sm text-destructive'>{errors.name.message}</p>
            )}
          </div>
          
          <div className='space-y-2'>
            <Label htmlFor='description'>Description</Label>
            <Textarea 
              id='description'
              {...register('description')} 
              placeholder='Enter project description'
              rows={4}
              disabled={isLoading}
            />
          </div>
          
          {project && (
            <div className='space-y-2'>
              <Label htmlFor='status'>Status</Label>
              <Select 
                defaultValue={project.status}
                {...register('status')}
                disabled={isLoading}
              >
                <SelectTrigger>
                  <SelectValue placeholder='Select status' />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value='ACTIVE'>Active</SelectItem>
                  <SelectItem value='COMPLETED'>Completed</SelectItem>
                  <SelectItem value='ARCHIVED'>Archived</SelectItem>
                </SelectContent>
              </Select>
            </div>
          )}
        </CardContent>
      </Card>
      
      <div className='flex justify-end gap-3'>
        {onCancel && (
          <Button 
            type='button' 
            variant='outline' 
            onClick={onCancel}
            disabled={isLoading}
          >
            Cancel
          </Button>
        )}
        <Button type='submit' disabled={isLoading}>
          {isLoading && <Loader2 className='mr-2 h-4 w-4 animate-spin' />}
          {project ? 'Update Project' : 'Create Project'}
        </Button>
      </div>
    </form>
  );
};
```

**Ví dụ:** `src/modules/ptm/components/projects/ProjectFormDialog.tsx`
```typescript
import React from 'react';
import { 
  Dialog, 
  DialogContent, 
  DialogHeader, 
  DialogTitle 
} from '@/shared/components/ui';
import { ProjectForm } from './ProjectForm';
import type { Project, CreateProjectRequest, UpdateProjectRequest } from '../../types';

interface ProjectFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  project?: Project;
  onSubmit: (data: CreateProjectRequest | UpdateProjectRequest) => Promise<void>;
  isLoading?: boolean;
}

export const ProjectFormDialog: React.FC<ProjectFormDialogProps> = ({ 
  open, 
  onOpenChange, 
  project,
  onSubmit,
  isLoading = false 
}) => {
  const handleCancel = () => {
    onOpenChange(false);
  };
  
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-w-2xl max-h-[90vh] overflow-y-auto'>
        <DialogHeader>
          <DialogTitle>
            {project ? 'Edit Project' : 'Create New Project'}
          </DialogTitle>
        </DialogHeader>
        <ProjectForm 
          project={project}
          onSubmit={onSubmit}
          onCancel={handleCancel}
          isLoading={isLoading}
        />
      </DialogContent>
    </Dialog>
  );
};
```

**Update:** `src/modules/ptm/components/index.ts`
```typescript
export { ProjectForm } from './projects/ProjectForm';
export { ProjectFormDialog } from './projects/ProjectFormDialog';
```

**Update:** `src/modules/ptm/index.ts` (module barrel export)
```typescript
// Components
export { ProjectForm, ProjectFormDialog } from './components';

// Hooks
export { useProjects } from './hooks';
export type { UseProjectsReturn } from './hooks';

// Services
export { 
  useGetProjectsQuery, 
  useGetProjectByIdQuery,
  useCreateProjectMutation,
  useUpdateProjectMutation,
  useDeleteProjectMutation,
} from './services/ptmApi';

// Types
export type { 
  Project, 
  CreateProjectRequest, 
  UpdateProjectRequest,
  ProjectFilters,
  ProjectResponse,
  ProjectsResponse,
} from './types';

// Store
export {
  setProjectsSearch,
  setProjectsStatus,
  setProjectsPage,
  openCreateProjectDialog,
  openEditProjectDialog,
  selectProjectsFilters,
  selectProjectsDialogOpen,
  selectSelectedProjectId,
} from './store';
```

### 6. Create Next.js page
Tạo `src/app/{module}/{feature}/page.tsx` với 'use client' directive, import custom hook và components, render DataTable với columns, search, filters, và dialogs. Add layout wrapper nếu cần (`layout.tsx`).

**Ví dụ:** `src/app/ptm/projects/page.tsx`
```typescript
/*
Author: QuanTuanHuy
Description: Part of Serp Project - PTM Projects Page
*/

'use client';

import React, { useMemo, useState } from 'react';
import { useProjects, ProjectFormDialog, type Project } from '@/modules/ptm';
import { 
  Button, 
  Card, 
  CardContent,
  CardHeader,
  CardTitle,
  Input,
  Select,
  SelectTrigger,
  SelectValue,
  SelectContent,
  SelectItem,
  Badge,
} from '@/shared/components/ui';
import { DataTable } from '@/shared/components';
import type { ColumnDef } from '@tanstack/react-table';
import { Plus, Search, Filter, MoreVertical, Pencil, Trash2 } from 'lucide-react';
import { formatDate } from '@/shared/utils';

export default function ProjectsPage() {
  const { 
    projects, 
    pagination, 
    filters, 
    isLoading,
    isFetching,
    dialogOpen,
    dialogMode,
    selectedProjectId,
    handleSearch,
    handleStatusChange,
    handlePageChange,
    handlePageSizeChange,
    handleOpenCreateDialog,
    handleOpenEditDialog,
    handleCloseDialog,
    handleCreateProject,
    handleUpdateProject,
    handleDeleteProject,
    isCreating,
    isUpdating,
    isDeleting,
  } = useProjects();
  
  const [searchInput, setSearchInput] = useState(filters.search || '');
  
  // Get selected project for edit mode
  const selectedProject = useMemo(() => {
    if (!selectedProjectId) return undefined;
    return projects.find(p => p.id === selectedProjectId);
  }, [selectedProjectId, projects]);
  
  // Define table columns
  const columns = useMemo<ColumnDef<Project>[]>(() => [
    {
      id: 'name',
      header: 'Project Name',
      accessorKey: 'name',
      cell: ({ row }) => (
        <div className='font-medium'>{row.original.name}</div>
      ),
    },
    {
      id: 'description',
      header: 'Description',
      accessorKey: 'description',
      cell: ({ row }) => (
        <div className='text-muted-foreground truncate max-w-xs'>
          {row.original.description || '-'}
        </div>
      ),
    },
    {
      id: 'status',
      header: 'Status',
      accessorKey: 'status',
      cell: ({ row }) => {
        const status = row.original.status;
        const variant = 
          status === 'ACTIVE' ? 'default' :
          status === 'COMPLETED' ? 'success' :
          'secondary';
        return <Badge variant={variant}>{status}</Badge>;
      },
    },
    {
      id: 'createdAt',
      header: 'Created At',
      accessorKey: 'createdAt',
      cell: ({ row }) => formatDate(row.original.createdAt),
    },
    {
      id: 'actions',
      header: '',
      cell: ({ row }) => (
        <div className='flex gap-2'>
          <Button
            variant='ghost'
            size='sm'
            onClick={() => handleOpenEditDialog(row.original.id)}
          >
            <Pencil className='h-4 w-4' />
          </Button>
          <Button
            variant='ghost'
            size='sm'
            onClick={() => handleDeleteProject(row.original.id)}
            disabled={isDeleting}
          >
            <Trash2 className='h-4 w-4' />
          </Button>
        </div>
      ),
    },
  ], [handleOpenEditDialog, handleDeleteProject, isDeleting]);
  
  const handleSearchInputChange = (value: string) => {
    setSearchInput(value);
    handleSearch(value);
  };
  
  const handleFormSubmit = async (data: any) => {
    if (dialogMode === 'edit' && selectedProjectId) {
      await handleUpdateProject(selectedProjectId, data);
    } else {
      await handleCreateProject(data);
    }
  };
  
  return (
    <div className='space-y-6 p-6'>
      {/* Header */}
      <div className='flex items-center justify-between'>
        <div>
          <h1 className='text-3xl font-bold tracking-tight'>Projects</h1>
          <p className='text-muted-foreground'>
            Manage your projects and track progress
          </p>
        </div>
        <Button onClick={handleOpenCreateDialog}>
          <Plus className='mr-2 h-4 w-4' />
          Create Project
        </Button>
      </div>
      
      {/* Filters */}
      <Card>
        <CardHeader>
          <CardTitle className='flex items-center gap-2'>
            <Filter className='h-5 w-5' />
            Filters
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className='flex gap-4'>
            <div className='flex-1'>
              <div className='relative'>
                <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
                <Input
                  placeholder='Search projects...'
                  value={searchInput}
                  onChange={(e) => handleSearchInputChange(e.target.value)}
                  className='pl-9'
                />
              </div>
            </div>
            <Select
              value={filters.status || 'all'}
              onValueChange={(value) => 
                handleStatusChange(value === 'all' ? undefined : value)
              }
            >
              <SelectTrigger className='w-[180px]'>
                <SelectValue placeholder='Filter by status' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='all'>All Status</SelectItem>
                <SelectItem value='ACTIVE'>Active</SelectItem>
                <SelectItem value='COMPLETED'>Completed</SelectItem>
                <SelectItem value='ARCHIVED'>Archived</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>
      
      {/* Data Table */}
      <Card>
        <CardContent className='p-0'>
          <DataTable 
            columns={columns} 
            data={projects}
            pagination={pagination}
            onPageChange={handlePageChange}
            onPageSizeChange={handlePageSizeChange}
            isLoading={isLoading || isFetching}
          />
        </CardContent>
      </Card>
      
      {/* Form Dialog */}
      <ProjectFormDialog
        open={dialogOpen}
        onOpenChange={handleCloseDialog}
        project={selectedProject}
        onSubmit={handleFormSubmit}
        isLoading={isCreating || isUpdating}
      />
    </div>
  );
}
```

## Further Considerations

### 1. Organization scoping
Nếu feature cần scope theo organization (như Settings module), thêm logic lấy `organizationId` từ `useAuth()` và sử dụng `skip` option trong RTK Query. Nếu là system-wide (như Admin module), bỏ qua scoping.

**Ví dụ với organization scoping:**
```typescript
export function useProjects() {
  const { user } = useAuth();
  const organizationId = user?.organizationId;
  
  const { data, isLoading } = useGetProjectsQuery(
    {
      organizationId: organizationId!,
      ...filters
    },
    { skip: !organizationId } // Don't query until organizationId available
  );
  
  // ... rest of hook
}
```

### 2. Server vs client pagination
Settings module dùng server-side pagination (backend API hỗ trợ page/pageSize params), Admin module một số endpoint dùng client-side filtering trong `useMemo`. Kiểm tra API Gateway để xác định pattern phù hợp.

**Server-side pagination** (recommended):
- Backend API trả về `PaginatedResponse<T>` với `currentPage`, `totalPages`, `totalItems`
- Frontend gửi `page`, `pageSize` trong query params
- Hiệu quả hơn cho datasets lớn

**Client-side pagination** (chỉ khi cần):
- Backend trả về toàn bộ data
- Frontend filter/sort/paginate trong `useMemo`
- Phù hợp cho danh sách nhỏ (< 100 items)

### 3. Additional infrastructure needed
Nếu là module hoàn toàn mới, cần tạo:

1. **Layout components:**
   - `src/modules/{module}/components/layout/{Module}Layout.tsx`
   - use dynamic sidebar in `src/shared/components/DynamicSidebar`
   - `src/modules/{module}/components/layout/{Module}Header.tsx`

2. **Auth guard:**
   - `src/modules/{module}/components/{Module}AuthGuard.tsx`

3. **App layout wrapper:**
   - `src/app/{module}/layout.tsx`

**Ví dụ:** `src/app/ptm/layout.tsx`
```typescript
import { PtmLayout } from '@/modules/ptm';

export default function Layout({ children }: { children: React.ReactNode }) {
  return <PtmLayout>{children}</PtmLayout>;
}
```

### 4. API Gateway endpoint integration
Đảm bảo backend service đã expose endpoints và API Gateway đã route đúng:

**Kiểm tra:**
1. Backend service có endpoints (`/api/v1/projects`) ✅
2. API Gateway route requests đến backend service ✅
3. JWT authentication middleware enabled ✅
4. CORS configured properly ✅

**Tham khảo:** Đọc file `api_gateway/src/ui/router/router.go` để xem cách routing được thiết lập.

### 5. Testing considerations
Sau khi implement, nên test:

1. **API integration:** Test với Postman/curl xem API có hoạt động
2. **UI flow:** Test toàn bộ CRUD operations từ UI
3. **Error handling:** Test các trường hợp lỗi (network, validation, permissions)
4. **Loading states:** Test UX khi đang loading/fetching
5. **Cache invalidation:** Test xem data có refresh đúng sau mutations

### 6. Performance optimization
Đối với features có data lớn, cân nhắc:

1. **Debounce search:** Sử dụng `useDebounce` hook để tránh query liên tục
2. **Memoization:** Sử dụng `useMemo` cho derived data
3. **Virtual scrolling:** Cho danh sách rất dài (sử dụng `react-virtual`)
4. **Lazy loading:** Load components khi cần với `React.lazy()`
5. **Image optimization:** Sử dụng Next.js Image component

### 7. Accessibility (a11y)
Đảm bảo components accessible:

1. **Semantic HTML:** Sử dụng đúng tags (`<button>`, `<input>`, etc.)
2. **ARIA labels:** Thêm `aria-label` cho icons
3. **Keyboard navigation:** Test với Tab, Enter, Escape
4. **Focus management:** Quản lý focus trong dialogs
5. **Screen reader:** Test với screen reader tools

### 8. Error boundary
Wrap feature trong error boundary để catch runtime errors:

```typescript
// src/app/ptm/projects/error.tsx
'use client';

export default function Error({
  error,
  reset,
}: {
  error: Error;
  reset: () => void;
}) {
  return (
    <div className='flex flex-col items-center justify-center p-6'>
      <h2>Something went wrong!</h2>
      <button onClick={reset}>Try again</button>
    </div>
  );
}
```

## Checklist

- [ ] Define types in `types/{feature}.types.ts`
- [ ] Update `types/index.ts` barrel export
- [ ] Create RTK Query API service in `services/{feature}/{feature}Api.ts`
- [ ] Add tag type to `src/lib/store/api/baseApi.ts`
- [ ] Update `services/{module}Api.ts` barrel export
- [ ] Create Redux slice in `store/{feature}/{feature}Slice.ts`
- [ ] Update `store/index.ts` to combine reducer
- [ ] Update `src/lib/store/index.ts` to include module reducer
- [ ] Create custom hook in `hooks/use{Feature}.ts`
- [ ] Update `hooks/index.ts` barrel export
- [ ] Create form component `components/{feature}/{Feature}Form.tsx`
- [ ] Create dialog component `components/{feature}/{Feature}FormDialog.tsx`
- [ ] Update `components/index.ts` barrel export
- [ ] Update module `index.ts` barrel export
- [ ] Create Next.js page `src/app/{module}/{feature}/page.tsx`
- [ ] Add layout wrapper if new module `src/app/{module}/layout.tsx`
- [ ] Test API integration (Postman/curl)
- [ ] Test full CRUD flow in UI
- [ ] Test error handling
- [ ] Test loading states
- [ ] Test cache invalidation
- [ ] Verify accessibility (keyboard navigation)
- [ ] Check responsive design (mobile/tablet)
- [ ] Add error boundary if needed
- [ ] Document any API changes needed
