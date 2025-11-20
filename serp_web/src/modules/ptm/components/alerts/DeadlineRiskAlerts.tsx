/**
 * PTM v2 - Deadline Risk Alerts Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - AI-powered deadline risk monitoring
 */

'use client';

import { useState } from 'react';
import {
  AlertTriangle,
  Clock,
  TrendingUp,
  Calendar,
  CheckCircle2,
  XCircle,
  Info,
} from 'lucide-react';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { Badge } from '@/shared/components/ui/badge';
import { Progress } from '@/shared/components/ui/progress';
import { cn } from '@/shared/utils';
import { formatDistanceToNow, differenceInDays } from 'date-fns';

export type RiskLevel = 'critical' | 'high' | 'medium' | 'low';

export interface DeadlineRisk {
  id: string;
  taskId: string;
  taskTitle: string;
  projectTitle?: string;
  deadline: number;
  riskLevel: RiskLevel;
  riskScore: number;
  completionRate: number;
  estimatedHours: number;
  remainingHours: number;
  daysRemaining: number;
  reasons: string[];
  suggestions: string[];
}

interface DeadlineRiskAlertsProps {
  className?: string;
}

const MOCK_RISKS: DeadlineRisk[] = [
  {
    id: '1',
    taskId: 'task1',
    taskTitle: 'Complete Phase 3 Features',
    projectTitle: 'PTM v2',
    deadline: Date.now() + 1000 * 60 * 60 * 24 * 2, // 2 days
    riskLevel: 'critical',
    riskScore: 95,
    completionRate: 20,
    estimatedHours: 16,
    remainingHours: 12.8,
    daysRemaining: 2,
    reasons: [
      'Only 20% complete with 2 days remaining',
      'Estimated 12.8 hours needed but only 16 hours available',
      'Multiple dependencies not yet started',
    ],
    suggestions: [
      'Break into smaller tasks and prioritize critical features',
      'Reschedule deadline to +3 days',
      'Request team assistance for parallel work',
    ],
  },
  {
    id: '2',
    taskId: 'task2',
    taskTitle: 'Write documentation',
    projectTitle: 'PTM v2',
    deadline: Date.now() + 1000 * 60 * 60 * 24 * 3, // 3 days
    riskLevel: 'high',
    riskScore: 78,
    completionRate: 40,
    estimatedHours: 8,
    remainingHours: 4.8,
    daysRemaining: 3,
    reasons: [
      'Documentation depends on feature completion',
      'Historical data shows docs take longer than estimated',
    ],
    suggestions: [
      'Start writing overview sections now',
      'Allocate 2-hour focus block tomorrow',
    ],
  },
  {
    id: '3',
    taskId: 'task3',
    taskTitle: 'Code review pending PRs',
    deadline: Date.now() + 1000 * 60 * 60 * 24 * 5, // 5 days
    riskLevel: 'medium',
    riskScore: 45,
    completionRate: 60,
    estimatedHours: 4,
    remainingHours: 1.6,
    daysRemaining: 5,
    reasons: ['3 PRs waiting for review', 'Context switching overhead'],
    suggestions: [
      'Schedule 1-hour review session',
      'Use morning time for better focus',
    ],
  },
  {
    id: '4',
    taskId: 'task4',
    taskTitle: 'Refactor authentication flow',
    projectTitle: 'Account Service',
    deadline: Date.now() + 1000 * 60 * 60 * 24 * 7, // 7 days
    riskLevel: 'low',
    riskScore: 25,
    completionRate: 85,
    estimatedHours: 6,
    remainingHours: 0.9,
    daysRemaining: 7,
    reasons: ['On track, just need final testing'],
    suggestions: ['Allocate 1 hour for integration tests'],
  },
];

const getRiskBadge = (level: RiskLevel) => {
  switch (level) {
    case 'critical':
      return (
        <Badge className='bg-red-100 text-red-700 dark:bg-red-950/20 dark:text-red-400'>
          <XCircle className='h-3 w-3 mr-1' />
          Critical
        </Badge>
      );
    case 'high':
      return (
        <Badge className='bg-orange-100 text-orange-700 dark:bg-orange-950/20 dark:text-orange-400'>
          <AlertTriangle className='h-3 w-3 mr-1' />
          High Risk
        </Badge>
      );
    case 'medium':
      return (
        <Badge className='bg-amber-100 text-amber-700 dark:bg-amber-950/20 dark:text-amber-400'>
          <TrendingUp className='h-3 w-3 mr-1' />
          Medium Risk
        </Badge>
      );
    case 'low':
      return (
        <Badge className='bg-green-100 text-green-700 dark:bg-green-950/20 dark:text-green-400'>
          <CheckCircle2 className='h-3 w-3 mr-1' />
          Low Risk
        </Badge>
      );
  }
};

const getRiskColor = (level: RiskLevel) => {
  switch (level) {
    case 'critical':
      return 'border-red-500 bg-red-50 dark:bg-red-950/10';
    case 'high':
      return 'border-orange-400 bg-orange-50 dark:bg-orange-950/10';
    case 'medium':
      return 'border-amber-400 bg-amber-50 dark:bg-amber-950/10';
    case 'low':
      return 'border-green-400 bg-green-50 dark:bg-green-950/10';
  }
};

export function DeadlineRiskAlerts({ className }: DeadlineRiskAlertsProps) {
  const [expandedId, setExpandedId] = useState<string | null>(null);

  // Sort by risk score descending
  const sortedRisks = [...MOCK_RISKS].sort((a, b) => b.riskScore - a.riskScore);

  const criticalCount = sortedRisks.filter(
    (r) => r.riskLevel === 'critical'
  ).length;
  const highCount = sortedRisks.filter((r) => r.riskLevel === 'high').length;
  const mediumCount = sortedRisks.filter(
    (r) => r.riskLevel === 'medium'
  ).length;

  return (
    <div className={cn('space-y-4', className)}>
      {/* Header */}
      <div className='flex items-center justify-between'>
        <div>
          <h2 className='text-2xl font-bold flex items-center gap-2'>
            <AlertTriangle className='h-6 w-6 text-red-600' />
            Deadline Risk Alerts
          </h2>
          <p className='text-muted-foreground mt-1'>
            AI-powered risk detection for upcoming deadlines
          </p>
        </div>

        <div className='flex items-center gap-2'>
          {criticalCount > 0 && (
            <Badge variant='destructive'>{criticalCount} Critical</Badge>
          )}
          {highCount > 0 && (
            <Badge className='bg-orange-600 hover:bg-orange-700'>
              {highCount} High
            </Badge>
          )}
          {mediumCount > 0 && (
            <Badge className='bg-amber-600 hover:bg-amber-700'>
              {mediumCount} Medium
            </Badge>
          )}
        </div>
      </div>

      {/* Risk Summary */}
      <Card>
        <CardHeader>
          <CardTitle className='text-base'>Risk Overview</CardTitle>
          <CardDescription>
            {sortedRisks.length} tasks monitored, {criticalCount + highCount}{' '}
            requiring immediate attention
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className='grid grid-cols-4 gap-4 text-center'>
            <div>
              <p className='text-2xl font-bold text-red-600'>{criticalCount}</p>
              <p className='text-xs text-muted-foreground'>Critical</p>
            </div>
            <div>
              <p className='text-2xl font-bold text-orange-600'>{highCount}</p>
              <p className='text-xs text-muted-foreground'>High Risk</p>
            </div>
            <div>
              <p className='text-2xl font-bold text-amber-600'>{mediumCount}</p>
              <p className='text-xs text-muted-foreground'>Medium</p>
            </div>
            <div>
              <p className='text-2xl font-bold text-green-600'>
                {sortedRisks.filter((r) => r.riskLevel === 'low').length}
              </p>
              <p className='text-xs text-muted-foreground'>On Track</p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Risk List */}
      <div className='space-y-3'>
        {sortedRisks.map((risk) => (
          <Card
            key={risk.id}
            className={cn('border-l-4', getRiskColor(risk.riskLevel))}
          >
            <CardContent className='p-4'>
              <div className='space-y-3'>
                {/* Header */}
                <div className='flex items-start justify-between gap-2'>
                  <div className='flex-1 min-w-0'>
                    <div className='flex items-center gap-2 mb-1'>
                      {getRiskBadge(risk.riskLevel)}
                      <span className='text-xs text-muted-foreground'>
                        Risk Score: {risk.riskScore}%
                      </span>
                    </div>
                    <h4 className='font-semibold text-sm'>{risk.taskTitle}</h4>
                    {risk.projectTitle && (
                      <p className='text-xs text-muted-foreground'>
                        Project: {risk.projectTitle}
                      </p>
                    )}
                  </div>

                  <Button
                    variant='ghost'
                    size='sm'
                    onClick={() =>
                      setExpandedId(expandedId === risk.id ? null : risk.id)
                    }
                  >
                    {expandedId === risk.id ? 'Hide' : 'Details'}
                  </Button>
                </div>

                {/* Stats */}
                <div className='grid grid-cols-3 gap-3 text-xs'>
                  <div className='flex items-center gap-1.5'>
                    <Calendar className='h-3 w-3 text-muted-foreground' />
                    <span className='font-medium'>
                      {risk.daysRemaining}d remaining
                    </span>
                  </div>
                  <div className='flex items-center gap-1.5'>
                    <Clock className='h-3 w-3 text-muted-foreground' />
                    <span className='font-medium'>
                      {risk.remainingHours.toFixed(1)}h needed
                    </span>
                  </div>
                  <div className='flex items-center gap-1.5'>
                    <TrendingUp className='h-3 w-3 text-muted-foreground' />
                    <span className='font-medium'>
                      {risk.completionRate}% done
                    </span>
                  </div>
                </div>

                {/* Progress */}
                <div>
                  <div className='flex items-center justify-between text-xs mb-1.5'>
                    <span className='text-muted-foreground'>Completion</span>
                    <span className='font-medium'>{risk.completionRate}%</span>
                  </div>
                  <Progress value={risk.completionRate} className='h-2' />
                </div>

                {/* Expanded Details */}
                {expandedId === risk.id && (
                  <div className='pt-3 border-t space-y-3'>
                    <div>
                      <h5 className='text-xs font-semibold mb-2 flex items-center gap-1.5'>
                        <Info className='h-3 w-3' />
                        Risk Factors
                      </h5>
                      <ul className='space-y-1'>
                        {risk.reasons.map((reason, index) => (
                          <li
                            key={index}
                            className='text-xs text-muted-foreground flex items-start gap-2'
                          >
                            <span className='text-red-600 mt-1'>•</span>
                            <span>{reason}</span>
                          </li>
                        ))}
                      </ul>
                    </div>

                    <div>
                      <h5 className='text-xs font-semibold mb-2 flex items-center gap-1.5'>
                        <CheckCircle2 className='h-3 w-3' />
                        AI Suggestions
                      </h5>
                      <ul className='space-y-1'>
                        {risk.suggestions.map((suggestion, index) => (
                          <li
                            key={index}
                            className='text-xs text-muted-foreground flex items-start gap-2'
                          >
                            <span className='text-green-600 mt-1'>→</span>
                            <span>{suggestion}</span>
                          </li>
                        ))}
                      </ul>
                    </div>

                    <div className='flex gap-2 pt-2'>
                      <Button size='sm' variant='outline' className='text-xs'>
                        Reschedule
                      </Button>
                      <Button size='sm' variant='outline' className='text-xs'>
                        Break Down Task
                      </Button>
                      <Button size='sm' variant='outline' className='text-xs'>
                        Mark as Reviewed
                      </Button>
                    </div>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* No risks */}
      {sortedRisks.length === 0 && (
        <Card>
          <CardContent className='p-12 text-center'>
            <CheckCircle2 className='h-12 w-12 text-green-600 mx-auto mb-3' />
            <h3 className='font-semibold mb-1'>All Clear!</h3>
            <p className='text-sm text-muted-foreground'>
              No deadline risks detected. Keep up the great work!
            </p>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
