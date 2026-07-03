/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM optimization run risks dashboard
 */

'use client';

import { useMemo, useState } from 'react';
import { ShieldAlert, AlertTriangle, Info, MapPin, Search } from 'lucide-react';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Input,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import type {
  PMOptimizationRunApi,
  PMOptimizationRunItemApi,
} from '../../types/api';

const CODE_LABELS: Record<string, string> = {
  DEPENDENCY_CYCLE: 'Vòng lặp phụ thuộc',
  DEPENDENCY_VIOLATION: 'Vi phạm thứ tự phụ thuộc',
  EXTERNAL_DEPENDENCY: 'Phụ thuộc ngoài phạm vi',
  MISSING_ESTIMATE: 'Thiếu thời gian ước lượng',
  DEFAULT_DURATION_USED: 'Sử dụng thời lượng mặc định',
  LOW_CONFIDENCE_DURATION: 'Ước lượng thời gian độ tin cậy thấp',
  NO_ELIGIBLE_ASSIGNEE: 'Không tìm thấy người thực hiện phù hợp',
  OVER_CAPACITY: 'Quá tải tài nguyên/tải trọng',
  LATE_RISK: 'Rủi ro trễ hạn',
  STALE_ITEM: 'Dữ liệu gốc đã bị thay đổi',
  PERMISSION_DENIED: 'Không có quyền cập nhật công việc',
  INVALID_OVERRIDE: 'Ghi đè không hợp lệ',
  REQUIRED_SKILL_MISSING: 'Thiếu kỹ năng bắt buộc',
  PARTIAL_SKILL_MATCH: 'Chỉ đáp ứng một phần kỹ năng',
};

const CATEGORIES = {
  ALL: 'Tất cả',
  DEPENDENCY: 'Phụ thuộc',
  CAPACITY: 'Tải trọng',
  SKILLS: 'Kỹ năng',
  SYSTEM: 'Hệ thống',
};

export interface UnifiedRisk {
  id: string;
  workItemId?: number | null;
  workItemKey?: string | null;
  workItemSummary?: string | null;
  severity: 'ERROR' | 'WARN' | 'INFO';
  code: string;
  message: string;
  details?: string | null;
  source: 'RUN_WARNING' | 'ITEM_VIOLATION';
  rawItem?: PMOptimizationRunItemApi;
}

interface PMOptimizationRunRisksProps {
  run: PMOptimizationRunApi;
  onLocateItem: (workItemId: number) => void;
}

export function PMOptimizationRunRisks({
  run,
  onLocateItem,
}: PMOptimizationRunRisksProps) {
  const [selectedSeverity, setSelectedSeverity] = useState<
    'ALL' | 'ERROR' | 'WARN' | 'INFO'
  >('ALL');
  const [selectedCategory, setSelectedCategory] =
    useState<keyof typeof CATEGORIES>('ALL');
  const [searchQuery, setSearchQuery] = useState('');

  const items = run.items || [];
  const warnings = run.warnings || [];

  // Parse detailsJson into readable text
  const parseDetails = (detailsJson?: string | null): string | null => {
    if (!detailsJson) return null;
    try {
      const data = JSON.parse(detailsJson);
      if (typeof data === 'object' && data !== null) {
        if (Array.isArray(data)) {
          if (data.length === 0) return null;
          return data
            .map((val) =>
              typeof val === 'object' ? JSON.stringify(val) : String(val)
            )
            .join(', ');
        }

        const predId =
          data.predecessorId ||
          data.dependencyWorkItemId ||
          data.predecessorWorkItemId;
        if (predId) {
          const matchedItem = items.find(
            (it) => it.workItemId === Number(predId)
          );
          if (matchedItem?.workItem) {
            return `Tác vụ liên quan: ${matchedItem.workItem.key} (${matchedItem.workItem.summary})`;
          }
          return `Tác vụ ID: ${predId}`;
        }

        if (
          data.requiredCapacity !== undefined ||
          data.availableCapacity !== undefined
        ) {
          const req =
            data.requiredCapacity !== undefined
              ? `${data.requiredCapacity}h`
              : '?';
          const avail =
            data.availableCapacity !== undefined
              ? `${data.availableCapacity}h`
              : '?';
          return `Yêu cầu: ${req} | Khả dụng: ${avail}`;
        }

        return Object.entries(data)
          .map(
            ([key, val]) =>
              `${key}: ${typeof val === 'object' ? JSON.stringify(val) : val}`
          )
          .join(' | ');
      }
      return String(data);
    } catch {
      return detailsJson;
    }
  };

  // Convert violation string to UnifiedRisk
  const parseViolationString = (
    violationStr: string,
    item: PMOptimizationRunItemApi,
    index: number
  ): UnifiedRisk => {
    const colonIndex = violationStr.indexOf(':');
    let code = 'VIOLATION';
    let message = violationStr;
    if (colonIndex !== -1) {
      const parsedCode = violationStr.substring(0, colonIndex).trim();
      if (/^[A-Z_]+$/.test(parsedCode)) {
        code = parsedCode;
        message = violationStr.substring(colonIndex + 1).trim();
      }
    }

    let severity: 'ERROR' | 'WARN' | 'INFO' = 'WARN';
    if (
      [
        'DEPENDENCY_CYCLE',
        'DEPENDENCY_VIOLATION',
        'NO_ELIGIBLE_ASSIGNEE',
        'OVER_CAPACITY',
        'STALE_ITEM',
        'PERMISSION_DENIED',
        'INVALID_OVERRIDE',
      ].includes(code)
    ) {
      severity = 'ERROR';
    } else if (
      [
        'EXTERNAL_DEPENDENCY',
        'PARTIAL_CALENDAR_COVERAGE',
        'PARTIAL_WORKLOAD_COVERAGE',
      ].includes(code)
    ) {
      severity = 'INFO';
    }

    return {
      id: `violation-${item.id}-${index}`,
      workItemId: item.workItemId,
      workItemKey: item.workItem?.key,
      workItemSummary: item.workItem?.summary,
      severity,
      code,
      message,
      source: 'ITEM_VIOLATION',
      rawItem: item,
    };
  };

  // Aggregate run.warnings and item.violations
  const allRisks = useMemo(() => {
    const list: UnifiedRisk[] = [];

    // Add run-level warnings
    warnings.forEach((w) => {
      let severity: 'ERROR' | 'WARN' | 'INFO' = 'WARN';
      const code = w.code || '';
      if (
        w.severity === 'ERROR' ||
        [
          'DEPENDENCY_CYCLE',
          'DEPENDENCY_VIOLATION',
          'NO_ELIGIBLE_ASSIGNEE',
          'OVER_CAPACITY',
          'STALE_ITEM',
          'PERMISSION_DENIED',
          'INVALID_OVERRIDE',
        ].includes(code)
      ) {
        severity = 'ERROR';
      } else if (
        w.severity === 'INFO' ||
        [
          'EXTERNAL_DEPENDENCY',
          'PARTIAL_CALENDAR_COVERAGE',
          'PARTIAL_WORKLOAD_COVERAGE',
        ].includes(code)
      ) {
        severity = 'INFO';
      }

      const matchedItem = w.workItemId
        ? items.find((it) => it.workItemId === w.workItemId)
        : null;

      list.push({
        id: `warning-${w.id}`,
        workItemId: w.workItemId,
        workItemKey: matchedItem?.workItem?.key,
        workItemSummary: matchedItem?.workItem?.summary,
        severity,
        code,
        message: w.message || '',
        details: parseDetails(w.detailsJson),
        source: 'RUN_WARNING',
      });
    });

    // Add item-level violations
    items.forEach((item) => {
      const violations = item.violations || [];
      violations.forEach((violationStr, index) => {
        list.push(parseViolationString(violationStr, item, index));
      });
    });

    return list;
  }, [warnings, items]);

  const getCategory = (code: string): string => {
    const c = code.toUpperCase();
    if (c.includes('DEPENDENCY')) return 'DEPENDENCY';
    if (
      c.includes('CAPACITY') ||
      c.includes('WORKLOAD') ||
      c.includes('LATE_RISK') ||
      c.includes('CALENDAR')
    )
      return 'CAPACITY';
    if (c.includes('SKILL')) return 'SKILLS';
    return 'SYSTEM';
  };

  // Filtered risks
  const filteredRisks = useMemo(() => {
    return allRisks.filter((risk) => {
      // 1. Severity filter
      if (selectedSeverity !== 'ALL' && risk.severity !== selectedSeverity) {
        return false;
      }
      // 2. Category filter
      if (
        selectedCategory !== 'ALL' &&
        getCategory(risk.code) !== selectedCategory
      ) {
        return false;
      }
      // 3. Search query filter
      if (searchQuery.trim()) {
        const query = searchQuery.toLowerCase();
        const keyMatch = risk.workItemKey?.toLowerCase().includes(query);
        const summaryMatch = risk.workItemSummary
          ?.toLowerCase()
          .includes(query);
        const msgMatch = risk.message.toLowerCase().includes(query);
        const codeMatch = risk.code.toLowerCase().includes(query);
        if (!keyMatch && !summaryMatch && !msgMatch && !codeMatch) {
          return false;
        }
      }
      return true;
    });
  }, [allRisks, selectedSeverity, selectedCategory, searchQuery]);

  // Counts for cards
  const counts = useMemo(() => {
    return allRisks.reduce(
      (acc, r) => {
        acc.total++;
        if (r.severity === 'ERROR') acc.errors++;
        else if (r.severity === 'WARN') acc.warnings++;
        else if (r.severity === 'INFO') acc.infos++;
        return acc;
      },
      { total: 0, errors: 0, warnings: 0, infos: 0 }
    );
  }, [allRisks]);

  // Grouped by workItemId
  const groupedRisks = useMemo(() => {
    const groups: Record<
      string,
      {
        workItemId?: number | null;
        workItemKey?: string | null;
        workItemSummary?: string | null;
        risks: UnifiedRisk[];
      }
    > = {};

    filteredRisks.forEach((risk) => {
      const groupKey = risk.workItemId ? String(risk.workItemId) : 'global';
      if (!groups[groupKey]) {
        groups[groupKey] = {
          workItemId: risk.workItemId,
          workItemKey: risk.workItemKey,
          workItemSummary: risk.workItemSummary,
          risks: [],
        };
      }
      groups[groupKey].risks.push(risk);
    });

    return Object.values(groups).sort((a, b) => {
      if (!a.workItemId) return -1; // Global first
      if (!b.workItemId) return 1;
      return a.workItemId - b.workItemId;
    });
  }, [filteredRisks]);

  const severityStyles: Record<
    string,
    { bg: string; border: string; text: string; icon: any }
  > = {
    ERROR: {
      bg: 'bg-red-50 dark:bg-red-950/20',
      border: 'border-red-200 dark:border-red-900',
      text: 'text-red-700 dark:text-red-400',
      icon: ShieldAlert,
    },
    WARN: {
      bg: 'bg-amber-50 dark:bg-amber-950/20',
      border: 'border-amber-200 dark:border-amber-900',
      text: 'text-amber-700 dark:text-amber-400',
      icon: AlertTriangle,
    },
    INFO: {
      bg: 'bg-blue-50 dark:bg-blue-950/20',
      border: 'border-blue-200 dark:border-blue-900',
      text: 'text-blue-700 dark:text-blue-400',
      icon: Info,
    },
  };

  return (
    <div className='space-y-4'>
      {/* 1. Summary Cards */}
      <div className='grid gap-3 grid-cols-2 md:grid-cols-4'>
        <Card
          className={cn(
            'cursor-pointer border-l-4 transition-all hover:bg-muted/50',
            selectedSeverity === 'ALL'
              ? 'border-primary shadow-sm bg-primary/5'
              : 'border-l-muted'
          )}
          onClick={() => setSelectedSeverity('ALL')}
        >
          <CardContent className='p-4'>
            <p className='text-xs font-semibold uppercase tracking-wider text-muted-foreground'>
              Total Issues
            </p>
            <h3 className='mt-1 text-2xl font-bold'>{counts.total}</h3>
          </CardContent>
        </Card>

        <Card
          className={cn(
            'cursor-pointer border-l-4 border-l-red-500 transition-all hover:bg-muted/50',
            selectedSeverity === 'ERROR' ? 'shadow-sm bg-red-500/5' : ''
          )}
          onClick={() => setSelectedSeverity('ERROR')}
        >
          <CardContent className='p-4'>
            <p className='text-xs font-semibold uppercase tracking-wider text-red-500'>
              Errors
            </p>
            <h3 className='mt-1 text-2xl font-bold text-red-500'>
              {counts.errors}
            </h3>
          </CardContent>
        </Card>

        <Card
          className={cn(
            'cursor-pointer border-l-4 border-l-amber-500 transition-all hover:bg-muted/50',
            selectedSeverity === 'WARN' ? 'shadow-sm bg-amber-500/5' : ''
          )}
          onClick={() => setSelectedSeverity('WARN')}
        >
          <CardContent className='p-4'>
            <p className='text-xs font-semibold uppercase tracking-wider text-amber-500'>
              Warnings
            </p>
            <h3 className='mt-1 text-2xl font-bold text-amber-500'>
              {counts.warnings}
            </h3>
          </CardContent>
        </Card>

        <Card
          className={cn(
            'cursor-pointer border-l-4 border-l-blue-500 transition-all hover:bg-muted/50',
            selectedSeverity === 'INFO' ? 'shadow-sm bg-blue-500/5' : ''
          )}
          onClick={() => setSelectedSeverity('INFO')}
        >
          <CardContent className='p-4'>
            <p className='text-xs font-semibold uppercase tracking-wider text-blue-500'>
              Info
            </p>
            <h3 className='mt-1 text-2xl font-bold text-blue-500'>
              {counts.infos}
            </h3>
          </CardContent>
        </Card>
      </div>

      {/* 2. Filter Toolbar */}
      <Card className='shadow-sm'>
        <CardContent className='flex flex-col gap-3 p-4 md:flex-row md:items-center md:justify-between'>
          <div className='relative flex-1 max-w-sm'>
            <Search className='absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground' />
            <Input
              type='text'
              placeholder='Tìm kiếm mã công việc, từ khóa...'
              className='pl-8'
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
          <div className='flex flex-wrap gap-1.5'>
            {Object.entries(CATEGORIES).map(([key, label]) => (
              <Button
                key={key}
                type='button'
                variant={selectedCategory === key ? 'default' : 'outline'}
                size='sm'
                onClick={() =>
                  setSelectedCategory(key as keyof typeof CATEGORIES)
                }
              >
                {label}
              </Button>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* 3. Grouped Risks List */}
      <div className='space-y-4'>
        {groupedRisks.length ? (
          groupedRisks.map((group) => {
            const isGlobal = !group.workItemId;
            return (
              <Card
                key={
                  isGlobal ? 'global-risks' : `item-risks-${group.workItemId}`
                }
                className='shadow-sm'
              >
                <CardHeader className='border-b bg-muted/10 px-4 py-3 flex flex-row items-center justify-between space-y-0'>
                  <div className='space-y-0.5'>
                    {isGlobal ? (
                      <CardTitle className='text-sm font-semibold'>
                        Rủi ro chung dự án (Global Risks)
                      </CardTitle>
                    ) : (
                      <div className='flex flex-wrap items-center gap-2'>
                        <Badge
                          variant='outline'
                          className='h-5 px-1.5 text-xs font-bold text-primary bg-primary/5'
                        >
                          {group.workItemKey}
                        </Badge>
                        <span className='text-sm font-medium'>
                          {group.workItemSummary}
                        </span>
                      </div>
                    )}
                  </div>
                  {!isGlobal && group.workItemId ? (
                    <Button
                      type='button'
                      size='sm'
                      variant='outline'
                      className='h-8 px-2.5 text-xs'
                      onClick={() => onLocateItem(group.workItemId!)}
                    >
                      <MapPin className='mr-1.5 h-3.5 w-3.5' />
                      Định vị
                    </Button>
                  ) : null}
                </CardHeader>
                <CardContent className='p-3 space-y-2'>
                  {group.risks.map((risk) => {
                    const style =
                      severityStyles[risk.severity] || severityStyles.INFO;
                    const Icon = style.icon;
                    return (
                      <div
                        key={risk.id}
                        className={cn(
                          'flex items-start gap-3 rounded-md border px-3 py-2 text-sm transition-all',
                          style.bg,
                          style.border
                        )}
                      >
                        <Icon
                          className={cn('mt-0.5 h-4 w-4 shrink-0', style.text)}
                        />
                        <div className='space-y-1'>
                          <div className='flex items-center gap-2'>
                            <span className={cn('font-semibold', style.text)}>
                              {CODE_LABELS[risk.code] || risk.code}
                            </span>
                            <span className='text-xs text-muted-foreground'>
                              ({risk.source})
                            </span>
                          </div>
                          <p className='text-muted-foreground'>
                            {risk.message}
                          </p>
                          {risk.details ? (
                            <div className='mt-1 text-xs font-medium text-muted-foreground bg-muted/40 px-2 py-1 rounded inline-block'>
                              Chi tiết: {risk.details}
                            </div>
                          ) : null}
                        </div>
                      </div>
                    );
                  })}
                </CardContent>
              </Card>
            );
          })
        ) : (
          <div className='rounded-md border border-dashed p-10 text-center text-sm text-muted-foreground'>
            Không tìm thấy rủi ro nào khớp với bộ lọc.
          </div>
        )}
      </div>
    </div>
  );
}
