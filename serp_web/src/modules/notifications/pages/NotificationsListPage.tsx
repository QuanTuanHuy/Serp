/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Full notifications list with filters and actions
 */

'use client';

import { useCallback, useMemo, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import {
  ArrowLeft,
  Bell,
  CheckCheck,
  Loader2,
  Settings,
  Trash2,
  Archive,
} from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/shared/components/ui/button';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/shared/components/ui/alert-dialog';
import { getErrorMessage } from '@/lib/store/api/utils';
import { useAppDispatch } from '@/shared/hooks';
import {
  useGetNotificationsQuery,
  useMarkNotificationAsReadMutation,
  useMarkAllNotificationsAsReadMutation,
  useDeleteNotificationMutation,
  useArchiveNotificationMutation,
} from '../services/notificationApi';
import {
  markAsRead,
  markAllAsRead,
  revertMarkAsRead,
} from '../store/notificationSlice';
import { NotificationItem } from '../components/NotificationItem';
import { navigateNotificationAction } from '../utils/notificationActionNavigation';
import type { NotificationResponse } from '../types/notification.types';

const PAGE_SIZE = 20;

export function NotificationsListPage() {
  const router = useRouter();
  const dispatch = useAppDispatch();
  const [page, setPage] = useState(0);
  const [readFilter, setReadFilter] = useState<'all' | 'unread' | 'read'>(
    'all'
  );
  const [deleteId, setDeleteId] = useState<number | null>(null);

  const queryParams = useMemo(
    () => ({
      page,
      pageSize: PAGE_SIZE,
      ...(readFilter === 'unread' ? { isRead: false as const } : {}),
      ...(readFilter === 'read' ? { isRead: true as const } : {}),
    }),
    [page, readFilter]
  );

  const { data, isFetching, refetch } = useGetNotificationsQuery(queryParams);
  const [markAsReadApi] = useMarkNotificationAsReadMutation();
  const [markAllAsReadApi] = useMarkAllNotificationsAsReadMutation();
  const [deleteApi] = useDeleteNotificationMutation();
  const [archiveApi] = useArchiveNotificationMutation();

  const notifications = data?.notifications ?? [];
  const totalCount = data?.totalCount ?? 0;
  const totalPages = Math.max(1, Math.ceil(totalCount / PAGE_SIZE));

  const handleMarkAsRead = useCallback(
    async (id: number) => {
      dispatch(markAsRead(id));
      try {
        await markAsReadApi(id).unwrap();
      } catch (err) {
        dispatch(revertMarkAsRead(id));
        toast.error(getErrorMessage(err));
      }
    },
    [dispatch, markAsReadApi]
  );

  const handleMarkAllAsRead = useCallback(async () => {
    try {
      await markAllAsReadApi().unwrap();
      dispatch(markAllAsRead());
      toast.success('Đã đánh dấu tất cả là đã đọc');
      void refetch();
    } catch (err) {
      toast.error(getErrorMessage(err));
    }
  }, [markAllAsReadApi, dispatch, refetch]);

  const handleClick = useCallback(
    (notification: NotificationResponse) => {
      navigateNotificationAction(router, notification.actionUrl);
    },
    [router]
  );

  const confirmDelete = useCallback(async () => {
    if (deleteId === null) return;
    try {
      await deleteApi(deleteId).unwrap();
      toast.success('Đã xóa thông báo');
      setDeleteId(null);
      void refetch();
    } catch (err) {
      toast.error(getErrorMessage(err));
    }
  }, [deleteApi, deleteId, refetch]);

  const handleArchive = useCallback(
    async (id: number) => {
      try {
        await archiveApi(id).unwrap();
        toast.success('Đã lưu trữ thông báo');
        void refetch();
      } catch (err) {
        toast.error(getErrorMessage(err));
      }
    },
    [archiveApi, refetch]
  );

  return (
    <div className='container mx-auto max-w-3xl py-8 px-4'>
      <div className='flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between mb-6'>
        <div className='flex items-center gap-3'>
          <Button variant='ghost' size='icon' asChild>
            <Link href='/home' aria-label='Về trang chủ'>
              <ArrowLeft className='h-5 w-5' />
            </Link>
          </Button>
          <div>
            <h1 className='text-2xl font-semibold tracking-tight'>Thông báo</h1>
            <p className='text-sm text-muted-foreground'>
              {totalCount} thông báo
            </p>
          </div>
        </div>
        <div className='flex flex-wrap items-center gap-2'>
          <Select
            value={readFilter}
            onValueChange={(v) => {
              setReadFilter(v as 'all' | 'unread' | 'read');
              setPage(0);
            }}
          >
            <SelectTrigger className='w-[160px]'>
              <SelectValue placeholder='Lọc' />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value='all'>Tất cả</SelectItem>
              <SelectItem value='unread'>Chưa đọc</SelectItem>
              <SelectItem value='read'>Đã đọc</SelectItem>
            </SelectContent>
          </Select>
          <Button variant='outline' size='sm' asChild>
            <Link href='/notifications/settings'>
              <Settings className='h-4 w-4 mr-1' />
              Cài đặt
            </Link>
          </Button>
          <Button
            variant='outline'
            size='sm'
            onClick={() => void refetch()}
            disabled={isFetching}
          >
            Làm mới
          </Button>
          <Button variant='default' size='sm' onClick={handleMarkAllAsRead}>
            <CheckCheck className='h-4 w-4 mr-1' />
            Đọc tất cả
          </Button>
        </div>
      </div>

      <div className='rounded-lg border bg-card'>
        {isFetching && notifications.length === 0 ? (
          <div className='flex justify-center py-16'>
            <Loader2 className='h-8 w-8 animate-spin text-muted-foreground' />
          </div>
        ) : notifications.length === 0 ? (
          <div className='flex flex-col items-center justify-center py-16 text-muted-foreground'>
            <Bell className='h-12 w-12 mb-3 opacity-40' />
            <p>Không có thông báo</p>
          </div>
        ) : (
          <ul className='divide-y'>
            {notifications.map((n) => (
              <li key={n.id} className='group relative'>
                <NotificationItem
                  notification={n}
                  onClick={handleClick}
                  onMarkAsRead={handleMarkAsRead}
                />
                <div className='absolute right-2 top-2 flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity'>
                  <Button
                    type='button'
                    variant='ghost'
                    size='icon'
                    className='h-8 w-8'
                    title='Lưu trữ'
                    onClick={(e) => {
                      e.stopPropagation();
                      void handleArchive(n.id);
                    }}
                  >
                    <Archive className='h-4 w-4' />
                  </Button>
                  <Button
                    type='button'
                    variant='ghost'
                    size='icon'
                    className='h-8 w-8 text-destructive'
                    title='Xóa'
                    onClick={(e) => {
                      e.stopPropagation();
                      setDeleteId(n.id);
                    }}
                  >
                    <Trash2 className='h-4 w-4' />
                  </Button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>

      {totalPages > 1 && (
        <div className='flex justify-center gap-2 mt-6'>
          <Button
            variant='outline'
            size='sm'
            disabled={page <= 0 || isFetching}
            onClick={() => setPage((p) => Math.max(0, p - 1))}
          >
            Trước
          </Button>
          <span className='flex items-center px-3 text-sm text-muted-foreground'>
            Trang {page + 1} / {totalPages}
          </span>
          <Button
            variant='outline'
            size='sm'
            disabled={page >= totalPages - 1 || isFetching}
            onClick={() => setPage((p) => p + 1)}
          >
            Sau
          </Button>
        </div>
      )}

      <AlertDialog
        open={deleteId !== null}
        onOpenChange={(open) => !open && setDeleteId(null)}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Xóa thông báo?</AlertDialogTitle>
            <AlertDialogDescription>
              Hành động này không thể hoàn tác.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Hủy</AlertDialogCancel>
            <AlertDialogAction
              className='bg-destructive text-destructive-foreground'
              onClick={() => void confirmDelete()}
            >
              Xóa
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
