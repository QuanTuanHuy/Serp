import { format, isToday } from 'date-fns';
import { vi } from 'date-fns/locale';
import { CalendarClock, Plus } from 'lucide-react';
import { Card, CardContent } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import type { VehicleRegistrationDay } from './vehicleRegistration.types';
import { VehicleShipperTag } from './VehicleShipperTag';

interface VehicleRegistrationCalendarProps {
  days: VehicleRegistrationDay[];
  isLoading: boolean;
  onSelectDay: (day: VehicleRegistrationDay) => void;
}

const WEEKDAY_LABELS = [
  'Thứ 2',
  'Thứ 3',
  'Thứ 4',
  'Thứ 5',
  'Thứ 6',
  'Thứ 7',
  'Chủ nhật',
];

export const VehicleRegistrationCalendar: React.FC<
  VehicleRegistrationCalendarProps
> = ({ days, isLoading, onSelectDay }) => {
  return (
    <Card className='overflow-hidden border-slate-200/80 shadow-lg shadow-slate-200/50'>
      <CardContent className='p-0'>
        <div className='relative overflow-x-auto'>
          <div className='min-w-full md:min-w-[980px]'>
            <div className='grid grid-cols-7 border-b bg-slate-100/70'>
              {WEEKDAY_LABELS.map((weekday) => (
                <div
                  key={weekday}
                  className='px-1 py-2 text-center text-[10px] font-semibold uppercase tracking-wide text-slate-600 sm:px-3 sm:text-xs'
                >
                  {weekday}
                </div>
              ))}
            </div>

            <div className='grid grid-cols-7'>
              {days.map((day) => (
                <div
                  key={day.dateKey}
                  role={day.canRegister ? 'button' : undefined}
                  tabIndex={day.canRegister ? 0 : -1}
                  onClick={() => {
                    if (!day.canRegister) return;
                    onSelectDay(day);
                  }}
                  onKeyDown={(event) => {
                    if (!day.canRegister) return;

                    if (event.key === 'Enter' || event.key === ' ') {
                      event.preventDefault();
                      onSelectDay(day);
                    }
                  }}
                  className={cn(
                    'group/day relative min-h-28 border-b border-r p-1.5 text-left transition-colors sm:min-h-44 sm:p-2',
                    !day.isCurrentMonth && 'bg-slate-50/60 opacity-60',
                    day.canRegister &&
                      'cursor-pointer bg-white hover:bg-emerald-50/50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-emerald-500/50',
                    !day.canRegister &&
                      !day.isCurrentMonth &&
                      'cursor-not-allowed'
                  )}
                >
                  <div className='mb-2 flex items-center justify-between gap-2'>
                    <span
                      className={cn(
                        'inline-flex h-6 min-w-6 items-center justify-center rounded-full px-1.5 text-xs font-semibold sm:h-7 sm:min-w-7 sm:px-2 sm:text-sm',
                        isToday(day.date)
                          ? 'bg-sky-600 text-white'
                          : 'text-slate-700'
                      )}
                    >
                      {format(day.date, 'd', { locale: vi })}
                    </span>

                    {day.canRegister && (
                      <span className='inline-flex h-6 w-6 items-center justify-center rounded-full border border-dashed border-emerald-300 bg-white text-emerald-600 opacity-0 transition group-hover/day:opacity-100 group-focus-visible/day:opacity-100 sm:h-7 sm:w-7'>
                        <Plus className='h-4 w-4' />
                      </span>
                    )}
                  </div>

                  {day.registrations.length > 0 ? (
                    <div className='max-h-24 space-y-1 overflow-y-auto pr-1 sm:max-h-28'>
                      {day.registrations.map((registration) => (
                        <VehicleShipperTag
                          key={registration.id}
                          registration={registration}
                          isPastDay={day.isPast}
                        />
                      ))}
                    </div>
                  ) : (
                    <div
                      className={cn(
                        'rounded-lg border border-dashed p-2 text-center text-[11px] leading-relaxed',
                        day.canRegister
                          ? 'border-emerald-300 bg-emerald-50/40 text-emerald-700'
                          : 'border-slate-200 bg-slate-100/40 text-slate-500'
                      )}
                    >
                      {day.canRegister ? 'Đăng ký xe' : 'Không có xe đăng ký'}
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>

          {isLoading && (
            <div className='absolute inset-0 z-10 flex items-center justify-center bg-white/70 backdrop-blur-sm'>
              <div className='inline-flex items-center gap-2 rounded-full border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-slate-700 shadow-sm'>
                <CalendarClock className='h-4 w-4 animate-pulse text-sky-600' />
                Đang tải lịch đăng ký xe...
              </div>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
};
