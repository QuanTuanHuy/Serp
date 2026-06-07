/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - TMS order product type list page
 */

'use client';

import React from 'react';
import { getErrorMessage, useAppSelector } from '@/lib/store';
import { ConfirmDialog } from '@/shared/components/ui/confirm-dialog';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Input,
  Label,
  Switch,
} from '@/shared/components/ui';
import { useNotification } from '@/shared/hooks';
import {
  Loader2,
  Pencil,
  Plus,
  RefreshCw,
  Search,
  ShieldAlert,
  Trash2,
} from 'lucide-react';
import {
  useCreateProductTypeMutation,
  useDeleteProductTypeMutation,
  useGetProductTypesQuery,
  useUpdateProductTypeMutation,
} from '../../api';
import type { ProductType } from '../../types';

const PAGE_SIZE = 20;

interface ProductTypeFormState {
  code: string;
  name: string;
  isActive: boolean;
}

const DEFAULT_FORM: ProductTypeFormState = {
  code: '',
  name: '',
  isActive: true,
};

export const ProductTypeListPage: React.FC = () => {
  const notification = useNotification();
  const isTmsAdmin = useAppSelector((state) =>
    Boolean(state.account.user.profile?.roles?.includes('TMS_ADMIN'))
  );

  const [page, setPage] = React.useState(0);
  const [keywordInput, setKeywordInput] = React.useState('');
  const [keyword, setKeyword] = React.useState<string | undefined>(undefined);
  const [isFormOpen, setIsFormOpen] = React.useState(false);
  const [editingItem, setEditingItem] = React.useState<ProductType | null>(
    null
  );
  const [deleteTarget, setDeleteTarget] = React.useState<ProductType | null>(
    null
  );
  const [formValues, setFormValues] =
    React.useState<ProductTypeFormState>(DEFAULT_FORM);

  const { data, isLoading, isFetching, refetch } = useGetProductTypesQuery({
    page,
    size: PAGE_SIZE,
    keyword,
  });
  const [createProductType, { isLoading: isCreating }] =
    useCreateProductTypeMutation();
  const [updateProductType, { isLoading: isUpdating }] =
    useUpdateProductTypeMutation();
  const [deleteProductType, { isLoading: isDeleting }] =
    useDeleteProductTypeMutation();

  const isSaving = isCreating || isUpdating;

  const handleSearch = (event: React.FormEvent) => {
    event.preventDefault();
    setPage(0);
    setKeyword(keywordInput.trim() || undefined);
  };

  const openCreateDialog = () => {
    if (!isTmsAdmin) {
      notification.error('Only TMS_ADMIN can create product types.');
      return;
    }

    setEditingItem(null);
    setFormValues(DEFAULT_FORM);
    setIsFormOpen(true);
  };

  const openEditDialog = (item: ProductType) => {
    if (!isTmsAdmin) {
      notification.error('Only TMS_ADMIN can update product types.');
      return;
    }

    setEditingItem(item);
    setFormValues({
      code: item.code,
      name: item.name,
      isActive: item.isActive,
    });
    setIsFormOpen(true);
  };

  const validateForm = () => {
    if (!formValues.code.trim()) {
      return 'Product type code is required.';
    }

    if (!formValues.name.trim()) {
      return 'Product type name is required.';
    }

    return null;
  };

  const handleSubmitForm = async (event: React.FormEvent) => {
    event.preventDefault();

    if (!isTmsAdmin) {
      notification.error('Only TMS_ADMIN can save product types.');
      return;
    }

    const validationError = validateForm();
    if (validationError) {
      notification.error(validationError);
      return;
    }

    const body = {
      code: formValues.code.trim(),
      name: formValues.name.trim(),
      is_active: formValues.isActive,
    };

    try {
      if (editingItem) {
        await updateProductType({
          id: editingItem.id,
          body,
        }).unwrap();
        notification.success('Product type updated successfully.');
      } else {
        await createProductType(body).unwrap();
        notification.success('Product type created successfully.');
        setPage(0);
      }

      setIsFormOpen(false);
      setEditingItem(null);
      void refetch();
    } catch (error) {
      notification.error('Failed to save product type.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) {
      return;
    }

    if (!isTmsAdmin) {
      notification.error('Only TMS_ADMIN can delete product types.');
      return;
    }

    try {
      await deleteProductType(deleteTarget.id).unwrap();
      notification.success('Product type deleted successfully.');
      setDeleteTarget(null);

      if ((data?.items.length ?? 0) === 1 && page > 0) {
        setPage((prev) => Math.max(prev - 1, 0));
      } else {
        void refetch();
      }
    } catch (error) {
      notification.error('Failed to delete product type.', {
        description: getErrorMessage(error),
      });
    }
  };

  return (
    <>
      <div className='space-y-6'>
        <div className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between'>
          <div className='flex flex-col gap-2'>
            <h1 className='text-2xl font-bold tracking-tight'>Product Types</h1>
            <p className='text-muted-foreground'>
              Configure product classification used in TMS order imports.
            </p>
          </div>

          {isTmsAdmin ? (
            <Button onClick={openCreateDialog}>
              <Plus className='mr-2 h-4 w-4' />
              New Product Type
            </Button>
          ) : (
            <Badge variant='outline' className='gap-1'>
              <ShieldAlert className='h-3.5 w-3.5' />
              View only (write actions require TMS_ADMIN)
            </Badge>
          )}
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Search</CardTitle>
            <CardDescription>
              Filter product types by code or name.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form
              onSubmit={handleSearch}
              className='flex flex-col gap-2 sm:flex-row'
            >
              <div className='relative flex-1'>
                <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
                <Input
                  className='pl-10'
                  value={keywordInput}
                  onChange={(event) => setKeywordInput(event.target.value)}
                  placeholder='Search product type...'
                />
              </div>
              <Button type='submit'>Apply</Button>
              <Button
                type='button'
                variant='outline'
                onClick={() => refetch()}
                disabled={isFetching}
              >
                <RefreshCw className='mr-2 h-4 w-4' />
                Refresh
              </Button>
            </form>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Results ({data?.totalItems ?? 0})</CardTitle>
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <div className='flex items-center gap-2 text-muted-foreground'>
                <Loader2 className='h-4 w-4 animate-spin' />
                Loading product types...
              </div>
            ) : data && data.items.length > 0 ? (
              <div className='space-y-3'>
                {data.items.map((item) => (
                  <div
                    key={item.id}
                    className='flex flex-col gap-3 rounded-lg border p-3 sm:flex-row sm:items-start sm:justify-between'
                  >
                    <div>
                      <p className='font-medium'>
                        {item.code} - {item.name}
                      </p>
                      <p className='mt-1 text-xs text-muted-foreground'>
                        Active: {item.isActive ? 'Yes' : 'No'}
                      </p>
                    </div>
                    {isTmsAdmin ? (
                      <div className='flex flex-wrap gap-2'>
                        <Button
                          type='button'
                          size='sm'
                          variant='outline'
                          onClick={() => openEditDialog(item)}
                        >
                          <Pencil className='mr-1.5 h-3.5 w-3.5' />
                          Edit
                        </Button>
                        <Button
                          type='button'
                          size='sm'
                          variant='destructive'
                          onClick={() => setDeleteTarget(item)}
                        >
                          <Trash2 className='mr-1.5 h-3.5 w-3.5' />
                          Delete
                        </Button>
                      </div>
                    ) : null}
                  </div>
                ))}

                <div className='flex items-center justify-between pt-2'>
                  <Button
                    variant='outline'
                    onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
                    disabled={!data.hasPrevious || isFetching}
                  >
                    Previous
                  </Button>
                  <span className='text-sm text-muted-foreground'>
                    Page {data.currentPage + 1} / {Math.max(data.totalPages, 1)}
                  </span>
                  <Button
                    variant='outline'
                    onClick={() => setPage((prev) => prev + 1)}
                    disabled={!data.hasNext || isFetching}
                  >
                    Next
                  </Button>
                </div>
              </div>
            ) : (
              <p className='text-muted-foreground'>No product types found.</p>
            )}
          </CardContent>
        </Card>
      </div>

      <Dialog
        open={isFormOpen}
        onOpenChange={(open) => {
          if (!open && isSaving) {
            return;
          }
          setIsFormOpen(open);
          if (!open) {
            setEditingItem(null);
          }
        }}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {editingItem ? 'Edit Product Type' : 'New Product Type'}
            </DialogTitle>
            <DialogDescription>
              Product type codes are used by order forms and Excel import.
            </DialogDescription>
          </DialogHeader>

          <form className='space-y-4' onSubmit={handleSubmitForm}>
            <div className='space-y-2'>
              <Label htmlFor='product-type-code'>Code</Label>
              <Input
                id='product-type-code'
                value={formValues.code}
                onChange={(event) =>
                  setFormValues((prev) => ({
                    ...prev,
                    code: event.target.value,
                  }))
                }
                placeholder='e.g. FRAGILE'
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='product-type-name'>Name</Label>
              <Input
                id='product-type-name'
                value={formValues.name}
                onChange={(event) =>
                  setFormValues((prev) => ({
                    ...prev,
                    name: event.target.value,
                  }))
                }
                placeholder='e.g. Fragile goods'
                disabled={isSaving}
              />
            </div>

            <div className='flex items-center justify-between rounded-md border p-3'>
              <div className='space-y-0.5'>
                <Label htmlFor='product-type-active'>Active</Label>
                <p className='text-xs text-muted-foreground'>
                  Inactive product types are hidden from order creation.
                </p>
              </div>
              <Switch
                id='product-type-active'
                checked={formValues.isActive}
                onCheckedChange={(checked) =>
                  setFormValues((prev) => ({
                    ...prev,
                    isActive: checked,
                  }))
                }
                disabled={isSaving}
              />
            </div>

            <DialogFooter>
              <Button
                type='button'
                variant='outline'
                onClick={() => setIsFormOpen(false)}
                disabled={isSaving}
              >
                Cancel
              </Button>
              <Button type='submit' disabled={isSaving}>
                {isSaving ? (
                  <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                ) : null}
                {editingItem ? 'Save Changes' : 'Create'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      <ConfirmDialog
        open={Boolean(deleteTarget)}
        onOpenChange={(open) => {
          if (!open && !isDeleting) {
            setDeleteTarget(null);
          }
        }}
        title='Delete product type'
        description={
          deleteTarget
            ? `This will permanently delete product type ${deleteTarget.code} - ${deleteTarget.name}.`
            : 'This action cannot be undone.'
        }
        confirmText='Delete'
        cancelText='Cancel'
        onConfirm={handleDelete}
        isLoading={isDeleting}
        variant='destructive'
      />
    </>
  );
};
