'use client';

import Link from 'next/link';
import { useEffect, useMemo } from 'react';
import { format } from 'date-fns';
import { vi } from 'date-fns/locale';
import {
  ArrowRight,
  CalendarDays,
  CheckCircle2,
  ClipboardCheck,
  Compass,
  Radar,
  Route as RouteIcon,
  Truck,
} from 'lucide-react';
import { useAppDispatch } from '@/lib/store';
import { Badge, Button, Card, CardContent } from '@/shared/components/ui';
import { setActiveModule } from '../../store';

const QUICK_ACTIONS = [
  {
    title: 'Theo dõi lộ trình theo thời gian thực',
    description:
      'Mở tab theo dõi để xem ngay điểm giao tiếp theo và thao tác hoàn thành.',
    href: '/logistics2/next-route',
    icon: Radar,
    accentClass:
      'from-cyan-500/15 via-sky-500/10 to-cyan-500/15 border-cyan-200/80',
  },
  {
    title: 'Xem danh sách lộ trình của tôi',
    description:
      'Kiểm tra tất cả tuyến được phân công trong ngày, ưu tiên tuyến đang chạy.',
    href: '/logistics2/my-routes',
    icon: RouteIcon,
    accentClass:
      'from-emerald-500/15 via-green-500/10 to-emerald-500/15 border-emerald-200/80',
  },
  {
    title: 'Đăng ký xe làm việc',
    description:
      'Nếu chưa có phân công xe cho hôm nay, đăng ký nhanh để nhận lộ trình.',
    href: '/logistics2/vehicle-registration',
    icon: Truck,
    accentClass:
      'from-amber-500/15 via-orange-500/10 to-amber-500/15 border-amber-200/80',
  },
] as const;

const START_STEPS = [
  'Đăng ký xe làm việc cho ngày hiện tại.',
  'Vào Lộ trình của tôi để kiểm tra tuyến được giao.',
  'Khi bắt đầu giao hàng, dùng tab Theo dõi lộ trình để xử lý từng điểm dừng.',
];

export const WelcomePage: React.FC = () => {
  const dispatch = useAppDispatch();

  const todayLabel = useMemo(
    () => format(new Date(), 'EEEE, dd/MM/yyyy', { locale: vi }),
    []
  );

  useEffect(() => {
    dispatch(setActiveModule('routes'));
  }, [dispatch]);

  return (
    <div className='space-y-6'>
      <section className='relative overflow-hidden rounded-3xl border border-sky-200/70 bg-gradient-to-br from-sky-100 via-cyan-50 to-emerald-100 p-6 shadow-lg shadow-sky-100/70'>
        <div className='pointer-events-none absolute -top-20 -right-12 h-56 w-56 rounded-full bg-cyan-300/35 blur-3xl' />
        <div className='pointer-events-none absolute -bottom-24 -left-16 h-56 w-56 rounded-full bg-emerald-300/30 blur-3xl' />

        <div className='relative z-10 space-y-5'>
          <div className='flex flex-wrap items-start justify-between gap-4'>
            <div>
              <p className='text-xs font-semibold uppercase tracking-[0.2em] text-sky-700'>
                Welcome logistics operator
              </p>
              <h1 className='mt-1 text-3xl font-bold tracking-tight text-slate-900'>
                Trung tâm điều phối lộ trình giao hàng
              </h1>
              <p className='mt-3 max-w-3xl text-sm text-slate-700'>
                Quản lý hành trình trong ngày theo luồng đơn giản: kiểm tra phân
                công, bám sát điểm giao tiếp theo và xử lý trạng thái giao ngay
                trên một màn hình.
              </p>
            </div>

            <Badge
              variant='outline'
              className='border-sky-300 bg-white/70 text-slate-700'
            >
              <CalendarDays className='mr-1 h-3.5 w-3.5' />
              {todayLabel}
            </Badge>
          </div>

          <div className='flex flex-wrap gap-3'>
            <Button asChild size='lg' className='bg-sky-600 hover:bg-sky-700'>
              <Link href='/logistics2/next-route'>
                Theo dõi điểm giao tiếp theo
                <ArrowRight className='h-4 w-4' />
              </Link>
            </Button>

            <Button
              asChild
              size='lg'
              variant='outline'
              className='border-sky-300 bg-white/90 text-slate-700 hover:bg-white'
            >
              <Link href='/logistics2/my-routes'>
                Mở lộ trình của tôi
                <Compass className='h-4 w-4' />
              </Link>
            </Button>
          </div>
        </div>
      </section>

      <div className='grid gap-4 lg:grid-cols-3'>
        {QUICK_ACTIONS.map((action) => {
          const Icon = action.icon;

          return (
            <Card
              key={action.title}
              className='overflow-hidden border-slate-200/80 bg-white/95 shadow-sm'
            >
              <CardContent className='p-0'>
                <div
                  className={`h-1.5 w-full bg-gradient-to-r ${action.accentClass}`}
                />
                <div className='space-y-4 p-5'>
                  <div className='flex items-start gap-3'>
                    <div className='rounded-xl bg-slate-100 p-2 text-slate-700'>
                      <Icon className='h-4 w-4' />
                    </div>

                    <div className='space-y-1'>
                      <h2 className='text-base font-semibold text-slate-900'>
                        {action.title}
                      </h2>
                      <p className='text-sm text-slate-600'>
                        {action.description}
                      </p>
                    </div>
                  </div>

                  <Button
                    asChild
                    variant='ghost'
                    className='px-0 text-slate-800'
                  >
                    <Link href={action.href}>
                      Truy cập nhanh
                      <ArrowRight className='h-4 w-4' />
                    </Link>
                  </Button>
                </div>
              </CardContent>
            </Card>
          );
        })}
      </div>

      <div className='grid gap-4 xl:grid-cols-[1.2fr_1fr]'>
        <Card className='border-slate-200/80 bg-white/95 shadow-sm'>
          <CardContent className='space-y-4 p-5'>
            <div className='flex items-center gap-2 text-slate-800'>
              <ClipboardCheck className='h-4 w-4' />
              <h2 className='text-base font-semibold'>Quy trình bắt đầu ca</h2>
            </div>

            <div className='space-y-3'>
              {START_STEPS.map((step, index) => (
                <div key={step} className='flex gap-3'>
                  <span className='mt-0.5 flex h-6 w-6 items-center justify-center rounded-full bg-slate-100 text-xs font-semibold text-slate-700'>
                    {index + 1}
                  </span>
                  <p className='text-sm text-slate-700'>{step}</p>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card className='border-emerald-200/80 bg-emerald-50/70 shadow-sm'>
          <CardContent className='space-y-4 p-5'>
            <div className='flex items-center gap-2 text-emerald-900'>
              <CheckCircle2 className='h-4 w-4' />
              <h2 className='text-base font-semibold'>Mục tiêu vận hành</h2>
            </div>

            <ul className='space-y-2 text-sm text-emerald-900'>
              <li>
                - Giảm thời gian tìm điểm giao kế tiếp xuống còn vài giây.
              </li>
              <li>- Cập nhật trạng thái điểm dừng tức thì sau mỗi lần giao.</li>
              <li>
                - Theo dõi đầy đủ chi tiết route trực tiếp từ màn hình điều
                phối.
              </li>
            </ul>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};
