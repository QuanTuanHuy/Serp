'use client';

import { FileText, CheckCircle, Bell, Tag } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';
import { Badge } from '@/shared/components/ui/badge';
import type { Activity } from '../../../../types';

interface ActivityProfileProps {
  activity: Activity;
  formatDate: (dateString?: string) => string;
}

export function ActivityProfile({ activity, formatDate }: ActivityProfileProps) {
  const isCompleted = activity.status === 'COMPLETED';

  return (
    <div className="space-y-6">
      {/* Description */}
      <Card className="border border-muted/50 shadow-sm rounded-xl overflow-hidden">
        <CardHeader className="pb-3">
          <CardTitle className="text-xs font-bold uppercase tracking-wider text-muted-foreground flex items-center gap-2">
            <FileText className="h-4 w-4" /> Description
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-xs text-foreground/80 leading-relaxed whitespace-pre-wrap">
            {activity.description || 'No description provided.'}
          </p>
        </CardContent>
      </Card>

      {/* Outcome (Only if Completed) */}
      {isCompleted && activity.outcome && (
        <Card className="border border-muted/50 shadow-sm rounded-xl overflow-hidden">
          <CardHeader className="pb-3">
            <CardTitle className="text-xs font-bold uppercase tracking-wider text-muted-foreground flex items-center gap-2">
              <CheckCircle className="h-4 w-4 text-emerald-600" /> Outcome / Results
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-xs text-foreground/80 leading-relaxed whitespace-pre-wrap">
              {activity.outcome}
            </p>
          </CardContent>
        </Card>
      )}

      {/* Follow-up Required (Only if applicable) */}
      {activity.followUpRequired && (
        <Card className="border border-amber-200/50 bg-amber-50/50 dark:bg-amber-950/20 shadow-sm rounded-xl overflow-hidden">
          <CardHeader className="pb-3">
            <CardTitle className="text-xs font-bold uppercase tracking-wider text-amber-800 dark:text-amber-400 flex items-center gap-2">
              <Bell className="h-4 w-4 animate-bounce" /> Follow-up Action Required
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-xs text-amber-700 dark:text-amber-300">
              Follow-up scheduled date: <span className="font-semibold">{formatDate(activity.followUpDate)}</span>
            </p>
          </CardContent>
        </Card>
      )}

      {/* Tags */}
      {activity.tags && activity.tags.length > 0 && (
        <Card className="border border-muted/50 shadow-sm rounded-xl overflow-hidden">
          <CardHeader className="pb-3">
            <CardTitle className="text-xs font-bold uppercase tracking-wider text-muted-foreground flex items-center gap-2">
              <Tag className="h-4 w-4" /> Activity Tags
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex flex-wrap gap-1.5">
              {activity.tags.map((tag) => (
                <Badge key={tag} variant="secondary" className="text-[10px] px-2 py-0.5">
                  {tag}
                </Badge>
              ))}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
