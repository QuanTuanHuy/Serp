/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - User notification preferences
 */

'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { ArrowLeft, Loader2 } from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/shared/components/ui/button';
import { Label } from '@/shared/components/ui/label';
import { Switch } from '@/shared/components/ui/switch';
import { Separator } from '@/shared/components/ui/separator';
import { getErrorMessage } from '@/lib/store/api/utils';
import {
  useGetPreferencesQuery,
  useUpdatePreferencesMutation,
} from '../services/preferenceApi';
import { timeStringToMinutes } from '../utils/quietHours';

export function NotificationSettingsPage() {
  const { data, isLoading } = useGetPreferencesQuery();
  const [updatePrefs, { isLoading: isSaving }] = useUpdatePreferencesMutation();

  const [enableInApp, setEnableInApp] = useState(true);
  const [enableEmail, setEnableEmail] = useState(false);
  const [enablePush, setEnablePush] = useState(true);
  const [quietEnabled, setQuietEnabled] = useState(false);
  const [quietStart, setQuietStart] = useState('22:00');
  const [quietEnd, setQuietEnd] = useState('07:00');

  useEffect(() => {
    if (!data) return;
    setEnableInApp(data.enableInApp);
    setEnableEmail(data.enableEmail);
    setEnablePush(data.enablePush);
    setQuietEnabled(data.quietHours.enabled);
    if (data.quietHours.start) {
      setQuietStart(data.quietHours.start);
    }
    if (data.quietHours.end) {
      setQuietEnd(data.quietHours.end);
    }
  }, [data]);

  const handleSave = async () => {
    const startMin = timeStringToMinutes(quietStart);
    const endMin = timeStringToMinutes(quietEnd);
    if (quietEnabled && (startMin === null || endMin === null)) {
      toast.error('Giờ im lặng không hợp lệ (dùng định dạng HH:MM)');
      return;
    }
    try {
      await updatePrefs({
        enableInApp,
        enableEmail,
        enablePush,
        quietHoursEnabled: quietEnabled,
        ...(quietEnabled && startMin !== null && endMin !== null
          ? { quietHoursStart: startMin, quietHoursEnd: endMin }
          : {}),
      }).unwrap();
      toast.success('Đã lưu cài đặt thông báo');
    } catch (err) {
      toast.error(getErrorMessage(err));
    }
  };

  if (isLoading && !data) {
    return (
      <div className='flex justify-center py-24'>
        <Loader2 className='h-8 w-8 animate-spin text-muted-foreground' />
      </div>
    );
  }

  return (
    <div className='container mx-auto max-w-lg py-8 px-4'>
      <div className='flex items-center gap-3 mb-8'>
        <Button variant='ghost' size='icon' asChild>
          <Link href='/notifications' aria-label='Danh sách thông báo'>
            <ArrowLeft className='h-5 w-5' />
          </Link>
        </Button>
        <div>
          <h1 className='text-2xl font-semibold tracking-tight'>
            Cài đặt thông báo
          </h1>
          <p className='text-sm text-muted-foreground'>
            Kênh và khung giờ im lặng
          </p>
        </div>
      </div>

      <div className='space-y-6 rounded-lg border bg-card p-6'>
        <div className='flex items-center justify-between gap-4'>
          <div className='space-y-0.5'>
            <Label htmlFor='in-app'>Trong ứng dụng</Label>
            <p className='text-xs text-muted-foreground'>
              Toast và danh sách thông báo
            </p>
          </div>
          <Switch
            id='in-app'
            checked={enableInApp}
            onCheckedChange={setEnableInApp}
          />
        </div>
        <Separator />
        <div className='flex items-center justify-between gap-4'>
          <div className='space-y-0.5'>
            <Label htmlFor='email'>Email</Label>
            <p className='text-xs text-muted-foreground'>
              Gửi qua email (nếu được cấu hình phía máy chủ)
            </p>
          </div>
          <Switch
            id='email'
            checked={enableEmail}
            onCheckedChange={setEnableEmail}
          />
        </div>
        <Separator />
        <div className='flex items-center justify-between gap-4'>
          <div className='space-y-0.5'>
            <Label htmlFor='push'>Đẩy trình duyệt</Label>
            <p className='text-xs text-muted-foreground'>
              Thông báo hệ thống khi tab không active
            </p>
          </div>
          <Switch
            id='push'
            checked={enablePush}
            onCheckedChange={setEnablePush}
          />
        </div>
        <Separator />
        <div className='space-y-4'>
          <div className='flex items-center justify-between gap-4'>
            <div className='space-y-0.5'>
              <Label htmlFor='quiet'>Giờ im lặng</Label>
              <p className='text-xs text-muted-foreground'>
                Giảm âm thanh và thông báo đẩy trong khung giờ này
              </p>
            </div>
            <Switch
              id='quiet'
              checked={quietEnabled}
              onCheckedChange={setQuietEnabled}
            />
          </div>
          {quietEnabled && (
            <div className='grid grid-cols-2 gap-4 pt-2'>
              <div className='space-y-2'>
                <Label htmlFor='qstart'>Bắt đầu</Label>
                <input
                  id='qstart'
                  type='time'
                  className='flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm'
                  value={quietStart}
                  onChange={(e) => setQuietStart(e.target.value)}
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='qend'>Kết thúc</Label>
                <input
                  id='qend'
                  type='time'
                  className='flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm'
                  value={quietEnd}
                  onChange={(e) => setQuietEnd(e.target.value)}
                />
              </div>
            </div>
          )}
        </div>
        <Button
          className='w-full'
          onClick={() => void handleSave()}
          disabled={isSaving}
        >
          {isSaving && <Loader2 className='h-4 w-4 mr-2 animate-spin' />}
          Lưu
        </Button>
      </div>
    </div>
  );
}
