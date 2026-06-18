'use client';

import * as React from 'react';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui';
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
  flatContent?: boolean;
}

export function SchoolBusTableTabs({
  defaultValue,
  tabs,
  className,
  flatContent = false,
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
    <div className={cn('w-full flex flex-col gap-4', className)}>
      <Tabs value={activeTab} onValueChange={setActiveTab} className='w-full'>
        {/* Tabs switcher wrapper */}
        <div className='flex items-center justify-start'>
          <TabsList className='inline-flex h-10 items-center justify-start gap-1 rounded-xl border border-border bg-muted/60 p-1 shadow-none'>
            {tabs.map((tab) => (
              <TabsTrigger
                key={tab.value}
                value={tab.value}
                className={cn(
                  'group relative rounded-lg px-3.5 py-1.5 text-xs font-semibold transition-all duration-150 outline-none',
                  'text-muted-foreground hover:bg-accent hover:text-accent-foreground',
                  'data-[state=active]:bg-[#C81E3A] data-[state=active]:text-white data-[state=active]:hover:bg-[#C81E3A] data-[state=active]:hover:text-white',
                  'shadow-none border-0'
                )}
              >
                <span className='inline-flex items-center gap-1.5 whitespace-nowrap'>
                  <span>{tab.label}</span>
                  <span
                    className={cn(
                      'rounded-md px-1.5 py-0.5 text-[9px] font-bold transition-colors duration-150',
                      'bg-background/70 text-muted-foreground group-hover:bg-background group-hover:text-foreground',
                      'group-data-[state=active]:bg-white/20 group-data-[state=active]:text-white'
                    )}
                  >
                    {tab.count}
                  </span>
                </span>
              </TabsTrigger>
            ))}
          </TabsList>
        </div>

        {/* Tab content wrapper */}
        {flatContent ? (
          <div className='mt-2'>
            {tabs.map((tab) => (
              <TabsContent
                key={tab.value}
                value={tab.value}
                className='mt-0 p-0'
              >
                <div
                  key={`${tab.value}-${contentMotionKey}`}
                  className='school-bus-tab-panel-enter'
                >
                  {tab.content}
                </div>
              </TabsContent>
            ))}
          </div>
        ) : (
          <div className={cn(schoolBusUi.card, 'mt-2 overflow-hidden p-0')}>
            {tabs.map((tab) => (
              <TabsContent
                key={tab.value}
                value={tab.value}
                className='mt-0 p-0'
              >
                <div
                  key={`${tab.value}-${contentMotionKey}`}
                  className='school-bus-tab-panel-enter'
                >
                  {tab.content}
                </div>
              </TabsContent>
            ))}
          </div>
        )}
      </Tabs>
      <style jsx>{`
        @keyframes schoolBusTabPanelEnter {
          0% {
            opacity: 0;
            transform: translateY(4px);
          }
          100% {
            opacity: 1;
            transform: translateY(0);
          }
        }

        .school-bus-tab-panel-enter {
          animation: schoolBusTabPanelEnter 180ms ease-out;
        }
      `}</style>
    </div>
  );
}
