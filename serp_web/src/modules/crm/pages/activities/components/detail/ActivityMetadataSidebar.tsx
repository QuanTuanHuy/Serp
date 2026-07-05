'use client';

import Link from 'next/link';
import { Calendar, User, Clock, MapPin, Building2, Target, ExternalLink, CheckCircle } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { Avatar, AvatarFallback } from '@/shared/components/ui/avatar';
import { Separator } from '@/shared/components/ui/separator';
import type { Activity } from '../../../../types';

interface ActivityMetadataSidebarProps {
  activity: Activity;
  assigneeDisplayName: string;
  assigneeInitials: string;
  formatDate: (dateString?: string) => string;
  formatDuration: (minutes?: number) => string;
  onOpenReschedule: () => void;
  onOpenStatusUpdate: () => void;
  onDeleteActivity: () => void;
}

export function ActivityMetadataSidebar({
  activity,
  assigneeDisplayName,
  assigneeInitials,
  formatDate,
  formatDuration,
  onOpenReschedule,
  onOpenStatusUpdate,
  onDeleteActivity,
}: ActivityMetadataSidebarProps) {
  const getRelatedLink = () => {
    switch (activity.relatedTo.type) {
      case 'CUSTOMER':
        return `/crm/accounts/${activity.relatedTo.id}`;
      case 'LEAD':
        return `/crm/leads/${activity.relatedTo.id}`;
      case 'OPPORTUNITY':
        return `/crm/opportunities/${activity.relatedTo.id}`;
      default:
        return '#';
    }
  };

  const getRelatedIcon = () => {
    switch (activity.relatedTo.type) {
      case 'CUSTOMER':
        return Building2;
      case 'LEAD':
        return User;
      case 'OPPORTUNITY':
        return Target;
      default:
        return User;
    }
  };

  const getRelatedLabel = () => {
    switch (activity.relatedTo.type) {
      case 'CUSTOMER':
        return 'Account';
      case 'LEAD':
        return 'Lead';
      case 'OPPORTUNITY':
        return 'Opportunity';
      default:
        return 'Entity';
    }
  };

  const RelatedIcon = getRelatedIcon();
  const relatedName = activity.relatedTo.name || 'Linked Object';

  return (
    <div className="space-y-6">
      {/* Linked Entity Card */}
      <Card className="border border-muted/50 shadow-sm rounded-xl">
        <CardHeader className="pb-2">
          <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Linked Workspace</CardTitle>
        </CardHeader>
        <CardContent>
          <Link href={getRelatedLink()} className="block">
            <div className="flex items-center justify-between p-2.5 bg-muted/40 hover:bg-muted/70 border border-muted/30 rounded-xl transition-all group">
              <div className="flex items-center gap-2.5 overflow-hidden">
                <div className="rounded-lg bg-blue-100 dark:bg-blue-900/30 p-2 text-blue-600 dark:text-blue-400">
                  <RelatedIcon className="h-4.5 w-4.5" />
                </div>
                <div className="overflow-hidden">
                  <div className="text-[10px] text-muted-foreground uppercase font-bold">{getRelatedLabel()}</div>
                  <div className="text-xs font-bold text-foreground truncate group-hover:text-primary transition">{relatedName}</div>
                </div>
              </div>
              <ExternalLink className="h-3.5 w-3.5 text-muted-foreground group-hover:text-primary transition shrink-0" />
            </div>
          </Link>
        </CardContent>
      </Card>

      {/* Assignee Card */}
      <Card className="border border-muted/50 shadow-sm rounded-xl">
        <CardHeader className="pb-2">
          <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Assignee</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-center gap-3">
            <Avatar className="h-9 w-9">
              <AvatarFallback className="text-xs bg-primary text-primary-foreground font-semibold">{assigneeInitials}</AvatarFallback>
            </Avatar>
            <div>
              <p className="text-xs font-bold text-foreground">{assigneeDisplayName}</p>
              <p className="text-[10px] text-muted-foreground">CRM Representative</p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Schedule Info Card */}
      <Card className="border border-muted/50 shadow-sm rounded-xl">
        <CardHeader className="pb-2">
          <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Schedule & Place</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3.5 text-xs">
          <div className="flex items-start gap-2.5">
            <Calendar className="h-4 w-4 text-muted-foreground shrink-0 mt-0.5" />
            <div>
              <span className="text-muted-foreground">Scheduled At</span>
              <p className="font-bold text-foreground mt-0.5">{formatDate(activity.scheduledDate)}</p>
            </div>
          </div>

          {activity.actualDate && (
            <div className="flex items-start gap-2.5">
              <CheckCircle className="h-4 w-4 text-emerald-600 shrink-0 mt-0.5" />
              <div>
                <span className="text-muted-foreground">Actual At</span>
                <p className="font-bold text-foreground mt-0.5">{formatDate(activity.actualDate)}</p>
              </div>
            </div>
          )}

          <div className="flex items-start gap-2.5">
            <Clock className="h-4 w-4 text-muted-foreground shrink-0 mt-0.5" />
            <div>
              <span className="text-muted-foreground">Duration</span>
              <p className="font-bold text-foreground mt-0.5">{formatDuration(activity.duration)}</p>
            </div>
          </div>

          {activity.location && (
            <div className="flex items-start gap-2.5">
              <MapPin className="h-4 w-4 text-muted-foreground shrink-0 mt-0.5" />
              <div>
                <span className="text-muted-foreground">Location</span>
                <p className="font-bold text-foreground mt-0.5">{activity.location}</p>
              </div>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Quick Action List */}
      <Card className="border border-muted/50 shadow-sm rounded-xl">
        <CardHeader className="pb-2">
          <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Actions Hub</CardTitle>
        </CardHeader>
        <CardContent className="space-y-2">
          {activity.status !== 'COMPLETED' && (
            <Button className="w-full justify-start text-xs h-9" variant="outline" onClick={onOpenStatusUpdate}>
              <CheckCircle className="mr-2 h-4 w-4 text-emerald-600" /> Mark Completed
            </Button>
          )}
          <Button className="w-full justify-start text-xs h-9 border-muted/50 hover:bg-muted/40 text-foreground" variant="outline" onClick={onOpenReschedule}>
            <Calendar className="mr-2 h-4 w-4 text-muted-foreground" /> Reschedule Date
          </Button>
          <Button variant="ghost" className="w-full justify-start text-xs h-9 text-rose-600 hover:text-rose-700 bg-rose-50 hover:bg-rose-100/80" onClick={onDeleteActivity}>
            Delete Activity Record
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
