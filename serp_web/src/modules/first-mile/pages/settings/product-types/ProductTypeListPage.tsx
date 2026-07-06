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
  Popover,
  PopoverContent,
  PopoverTrigger,
  Switch,
} from '@/shared/components/ui';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';
import { useNotification } from '@/shared/hooks';
import {
  Loader2,
  Pencil,
  Plus,
  RefreshCw,
  Search,
  ShieldAlert,
  Trash2,
  X,
} from 'lucide-react';
import {
  useCreateProductTypeMutation,
  useDeleteProductTypeMutation,
  useGetProductTypesQuery,
  useUpdateProductTypeMutation,
} from '../../../api';
import type { ProductType } from '../../../types';

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
  const [searchColumn, setSearchColumn] = React.useState<
    'code' | 'name' | null
  >(null);
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
  const isKeywordActive = Boolean(keyword);

  const handleSearch = (event: React.FormEvent) => {
    event.preventDefault();
    setPage(0);
    setKeyword(keywordInput.trim() || undefined);
    setSearchColumn(null);
  };

  const handleClearSearch = () => {
    setKeywordInput('');
    setKeyword(undefined);
    setPage(0);
    setSearchColumn(null);
  };

  const openCreateDialog = () => {
    if (!isTmsAdmin) {
      notification.error('Chỉ TMS_ADMIN mới có thể tạo loại hàng.');
      return;
    }

    setEditingItem(null);
    setFormValues(DEFAULT_FORM);
    setIsFormOpen(true);
  };

  const openEditDialog = (item: ProductType) => {
    if (!isTmsAdmin) {
      notification.error('Chỉ TMS_ADMIN mới có thể cập nhật loại hàng.');
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
      return 'Vui lòng nhập mã loại hàng.';
    }

    if (!formValues.name.trim()) {
      return 'Vui lòng nhập tên loại hàng.';
    }

    return null;
  };

  const handleSubmitForm = async (event: React.FormEvent) => {
    event.preventDefault();

    if (!isTmsAdmin) {
      notification.error('Chỉ TMS_ADMIN mới có thể lưu loại hàng.');
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
        notification.success('Đã cập nhật loại hàng.');
      } else {
        await createProductType(body).unwrap();
        notification.success('Đã tạo loại hàng.');
        setPage(0);
      }

      setIsFormOpen(false);
      setEditingItem(null);
      void refetch();
    } catch (error) {
      notification.error('Không thể lưu loại hàng.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) {
      return;
    }

    if (!isTmsAdmin) {
      notification.error('Chỉ TMS_ADMIN mới có thể xóa loại hàng.');
      return;
    }

    try {
      await deleteProductType(deleteTarget.id).unwrap();
      notification.success('Đã xóa loại hàng.');
      setDeleteTarget(null);

      if ((data?.items.length ?? 0) === 1 && page > 0) {
        setPage((prev) => Math.max(prev - 1, 0));
      } else {
        void refetch();
      }
    } catch (error) {
      notification.error('Không thể xóa loại hàng.', {
        description: getErrorMessage(error),
      });
    }
  };

  return (
    <>
      <div className='space-y-6'>
        <div className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between'>
          <div className='flex flex-col gap-2'>
            <h1 className='text-2xl font-bold tracking-tight'>Loại hàng</h1>
            <p className='text-muted-foreground'>
              Cấu hình phân loại hàng hóa dùng cho đơn TMS và dữ liệu nhập.
            </p>
          </div>

          {isTmsAdmin ? (
            <Button onClick={openCreateDialog}>
              <Plus className='mr-2 h-4 w-4' />
              Tạo loại hàng
            </Button>
          ) : (
            <Badge variant='outline' className='gap-1'>
              <ShieldAlert className='h-3.5 w-3.5' />
              Chỉ xem, thao tác ghi yêu cầu TMS_ADMIN
            </Badge>
          )}
        </div>

        <Card>
          <CardHeader>
            <div className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between'>
              <CardTitle>Kết quả ({data?.totalItems ?? 0})</CardTitle>
              <Button
                type='button'
                variant='outline'
                onClick={() => refetch()}
                disabled={isFetching}
              >
                <RefreshCw className='mr-2 h-4 w-4' />
                Làm mới
              </Button>
            </div>
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <div className='flex items-center gap-2 text-muted-foreground'>
                <Loader2 className='h-4 w-4 animate-spin' />
                Đang tải loại hàng...
              </div>
            ) : data && data.items.length > 0 ? (
              <div className='space-y-4'>
                <div className='overflow-hidden rounded-md border'>
                  <div className='overflow-x-auto'>
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead className='min-w-[160px]'>
                            <Popover
                              open={searchColumn === 'code'}
                              onOpenChange={(open) =>
                                setSearchColumn(open ? 'code' : null)
                              }
                            >
                              <div className='flex items-center gap-1'>
                                <span>Mã</span>
                                <PopoverTrigger asChild>
                                  <Button
                                    type='button'
                                    variant={
                                      isKeywordActive ? 'outline' : 'ghost'
                                    }
                                    size='icon'
                                    className='size-7'
                                    disabled={isFetching}
                                    title='Tìm mã loại hàng'
                                    aria-label='Tìm mã loại hàng'
                                  >
                                    <Search className='h-4 w-4' />
                                  </Button>
                                </PopoverTrigger>
                              </div>
                              <PopoverContent
                                align='start'
                                sideOffset={8}
                                className='w-72 p-3'
                              >
                                <form
                                  className='flex items-center gap-2'
                                  onSubmit={handleSearch}
                                >
                                  <Input
                                    className='h-9 bg-background'
                                    value={keywordInput}
                                    onChange={(event) =>
                                      setKeywordInput(event.target.value)
                                    }
                                    placeholder='Tìm mã loại hàng...'
                                    disabled={isFetching}
                                  />
                                  <Button
                                    type='submit'
                                    variant='outline'
                                    size='icon'
                                    className='size-9 shrink-0'
                                    disabled={isFetching}
                                    title='Tìm kiếm'
                                    aria-label='Tìm mã loại hàng'
                                  >
                                    <Search className='h-4 w-4' />
                                  </Button>
                                  {isKeywordActive ? (
                                    <Button
                                      type='button'
                                      variant='ghost'
                                      size='icon'
                                      className='size-9 shrink-0'
                                      disabled={isFetching}
                                      onClick={handleClearSearch}
                                      title='Xóa tìm kiếm'
                                      aria-label='Xóa tìm kiếm mã loại hàng'
                                    >
                                      <X className='h-4 w-4' />
                                    </Button>
                                  ) : null}
                                </form>
                              </PopoverContent>
                            </Popover>
                          </TableHead>
                          <TableHead className='min-w-[240px]'>
                            <Popover
                              open={searchColumn === 'name'}
                              onOpenChange={(open) =>
                                setSearchColumn(open ? 'name' : null)
                              }
                            >
                              <div className='flex items-center gap-1'>
                                <span>Tên</span>
                                <PopoverTrigger asChild>
                                  <Button
                                    type='button'
                                    variant={
                                      isKeywordActive ? 'outline' : 'ghost'
                                    }
                                    size='icon'
                                    className='size-7'
                                    disabled={isFetching}
                                    title='Tìm tên loại hàng'
                                    aria-label='Tìm tên loại hàng'
                                  >
                                    <Search className='h-4 w-4' />
                                  </Button>
                                </PopoverTrigger>
                              </div>
                              <PopoverContent
                                align='start'
                                sideOffset={8}
                                className='w-72 p-3'
                              >
                                <form
                                  className='flex items-center gap-2'
                                  onSubmit={handleSearch}
                                >
                                  <Input
                                    className='h-9 bg-background'
                                    value={keywordInput}
                                    onChange={(event) =>
                                      setKeywordInput(event.target.value)
                                    }
                                    placeholder='Tìm tên loại hàng...'
                                    disabled={isFetching}
                                  />
                                  <Button
                                    type='submit'
                                    variant='outline'
                                    size='icon'
                                    className='size-9 shrink-0'
                                    disabled={isFetching}
                                    title='Tìm kiếm'
                                    aria-label='Tìm tên loại hàng'
                                  >
                                    <Search className='h-4 w-4' />
                                  </Button>
                                  {isKeywordActive ? (
                                    <Button
                                      type='button'
                                      variant='ghost'
                                      size='icon'
                                      className='size-9 shrink-0'
                                      disabled={isFetching}
                                      onClick={handleClearSearch}
                                      title='Xóa tìm kiếm'
                                      aria-label='Xóa tìm kiếm tên loại hàng'
                                    >
                                      <X className='h-4 w-4' />
                                    </Button>
                                  ) : null}
                                </form>
                              </PopoverContent>
                            </Popover>
                          </TableHead>
                          <TableHead className='min-w-[120px]'>
                            Trạng thái
                          </TableHead>
                          {isTmsAdmin ? (
                            <TableHead className='sticky right-0 z-20 border-l bg-card text-right'>
                              Thao tác
                            </TableHead>
                          ) : null}
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {data.items.map((item) => (
                          <TableRow key={item.id} className='group'>
                            <TableCell className='font-medium'>
                              {item.code}
                            </TableCell>
                            <TableCell>{item.name}</TableCell>
                            <TableCell>
                              <Badge
                                variant={item.isActive ? 'default' : 'outline'}
                              >
                                {item.isActive ? 'Đang dùng' : 'Ngừng dùng'}
                              </Badge>
                            </TableCell>
                            {isTmsAdmin ? (
                              <TableCell className='sticky right-0 z-10 border-l bg-background text-right group-hover:bg-muted/50'>
                                <div className='flex items-center justify-end gap-1'>
                                  <Button
                                    type='button'
                                    size='icon'
                                    variant='outline'
                                    onClick={() => openEditDialog(item)}
                                    title='Sửa'
                                    aria-label={`Sửa ${item.code}`}
                                  >
                                    <Pencil className='h-4 w-4' />
                                  </Button>
                                  <Button
                                    type='button'
                                    size='icon'
                                    variant='destructive'
                                    onClick={() => setDeleteTarget(item)}
                                    title='Xóa'
                                    aria-label={`Xóa ${item.code}`}
                                  >
                                    <Trash2 className='h-4 w-4' />
                                  </Button>
                                </div>
                              </TableCell>
                            ) : null}
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </div>
                </div>

                <div className='flex items-center justify-between pt-2'>
                  <Button
                    variant='outline'
                    onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
                    disabled={!data.hasPrevious || isFetching}
                  >
                    Trước
                  </Button>
                  <span className='text-sm text-muted-foreground'>
                    Trang {data.currentPage + 1} /{' '}
                    {Math.max(data.totalPages, 1)}
                  </span>
                  <Button
                    variant='outline'
                    onClick={() => setPage((prev) => prev + 1)}
                    disabled={!data.hasNext || isFetching}
                  >
                    Sau
                  </Button>
                </div>
              </div>
            ) : (
              <p className='text-muted-foreground'>Không tìm thấy loại hàng.</p>
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
              {editingItem ? 'Sửa loại hàng' : 'Tạo loại hàng'}
            </DialogTitle>
            <DialogDescription>
              Mã loại hàng được dùng trong form đơn hàng và file Excel nhập.
            </DialogDescription>
          </DialogHeader>

          <form className='space-y-4' onSubmit={handleSubmitForm}>
            <div className='space-y-2'>
              <Label htmlFor='product-type-code'>Mã</Label>
              <Input
                id='product-type-code'
                value={formValues.code}
                onChange={(event) =>
                  setFormValues((prev) => ({
                    ...prev,
                    code: event.target.value,
                  }))
                }
                placeholder='Ví dụ: FRAGILE'
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='product-type-name'>Tên</Label>
              <Input
                id='product-type-name'
                value={formValues.name}
                onChange={(event) =>
                  setFormValues((prev) => ({
                    ...prev,
                    name: event.target.value,
                  }))
                }
                placeholder='Ví dụ: Hàng dễ vỡ'
                disabled={isSaving}
              />
            </div>

            <div className='flex items-center justify-between rounded-md border p-3'>
              <div className='space-y-0.5'>
                <Label htmlFor='product-type-active'>Đang dùng</Label>
                <p className='text-xs text-muted-foreground'>
                  Loại hàng ngừng dùng sẽ bị ẩn khi tạo đơn.
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
                Hủy
              </Button>
              <Button type='submit' disabled={isSaving}>
                {isSaving ? (
                  <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                ) : null}
                {editingItem ? 'Lưu thay đổi' : 'Tạo'}
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
        title='Xóa loại hàng'
        description={
          deleteTarget
            ? `Thao tác này sẽ xóa vĩnh viễn loại hàng ${deleteTarget.code} - ${deleteTarget.name}.`
            : 'Không thể hoàn tác thao tác này.'
        }
        confirmText='Xóa'
        cancelText='Hủy'
        onConfirm={handleDelete}
        isLoading={isDeleting}
        variant='destructive'
      />
    </>
  );
};
