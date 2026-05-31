'use client';

import * as React from 'react';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { schoolBusUi } from '../theme';

export interface SchoolBusTableTabItem {
  value: string;
  label: string;
  count: number;
  content: React.ReactNode;
}

interface SchoolBusTableTabsProps {
  defaultValue: string;
  tabs: SchoolBusTableTabItem[];
  className?: string;
}

export function SchoolBusTableTabs({
  defaultValue,
  tabs,
  className,
}: SchoolBusTableTabsProps) {
  const [activeTab, setActiveTab] = React.useState(defaultValue);
  const [contentMotionKey, setContentMotionKey] = React.useState(0);

  React.useEffect(() => {
    if (!tabs.some((tab) => tab.value === activeTab)) {
      setActiveTab(tabs[0]?.value || defaultValue);
    }
  }, [activeTab, defaultValue, tabs]);

  React.useEffect(() => {
    setContentMotionKey((value) => value + 1);
  }, [activeTab]);

  if (tabs.length === 0) {
    return null;
  }

  return (
    <div className={cn(schoolBusUi.section, 'overflow-hidden p-0', className)}>
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <div className='border-b border-slate-200 bg-[linear-gradient(180deg,#ffffff_0%,#f8fafc_100%)] px-4 pt-4 sm:px-6'>
          <div className='overflow-x-auto pb-3'>
            <TabsList className='h-auto min-w-max justify-start gap-1 rounded-2xl border border-slate-200 bg-white/95 p-1.5 shadow-sm'>
              {tabs.map((tab) => (
                <TabsTrigger
                  key={tab.value}
                  value={tab.value}
                  className='group relative rounded-xl px-3 py-2 transition-all duration-200 ease-out data-[state=active]:-translate-y-0.5 data-[state=active]:bg-slate-900 data-[state=active]:text-white data-[state=active]:shadow-sm data-[state=inactive]:text-slate-600 data-[state=inactive]:hover:bg-slate-100 data-[state=inactive]:hover:text-slate-900'
                >
                  <span className='inline-flex items-center gap-2 whitespace-nowrap'>
                    <span className='text-sm font-semibold'>{tab.label}</span>
                    <span className='rounded-full border border-slate-200 bg-white/80 px-2 py-0.5 text-[11px] font-semibold text-slate-700 transition-colors duration-200 group-data-[state=active]:border-slate-800 group-data-[state=active]:bg-white/20 group-data-[state=active]:text-white'>
                      {tab.count}
                    </span>
                  </span>
                </TabsTrigger>
              ))}
            </TabsList>
          </div>
        </div>

        {tabs.map((tab) => (
          <TabsContent key={tab.value} value={tab.value} className='mt-0 px-4 py-5 sm:px-6'>
            <div
              key={`${tab.value}-${contentMotionKey}`}
              className='school-bus-tab-panel-enter'
            >
              {tab.content}
            </div>
          </TabsContent>
        ))}
      </Tabs>
      <style jsx>{`
        @keyframes schoolBusTabPanelEnter {
          0% {
            opacity: 0;
            transform: translateY(8px) scale(0.995);
            filter: saturate(0.96);
          }
          100% {
            opacity: 1;
            transform: translateY(0) scale(1);
            filter: saturate(1);
          }
        }

        .school-bus-tab-panel-enter {
          animation: schoolBusTabPanelEnter 180ms ease-out;
        }
      `}</style>
    </div>
  );
}
