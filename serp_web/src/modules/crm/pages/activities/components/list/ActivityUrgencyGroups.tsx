'use client';

import { useState, useMemo } from 'react';
import { ChevronDown, ChevronRight, Clock } from 'lucide-react';
import { Card, CardContent } from '@/shared/components/ui/card';
import { Badge } from '@/shared/components/ui/badge';
import type { Activity } from '../../../../types';

interface ActivityUrgencyGroupsProps {
  activities: Activity[];
  selectedActivityIds: Set<string>;
  onSelectActivity: (id: string) => void;
  onViewActivity: (id: string) => void;
  getActivityIcon: (type: any) => any;
  getActivityColor: (type: any) => string;
  formatDate: (dateString?: string) => string;
}

export function ActivityUrgencyGroups({
  activities,
  selectedActivityIds,
  onSelectActivity,
  onViewActivity,
  getActivityIcon,
  getActivityColor,
  formatDate,
}: ActivityUrgencyGroupsProps) {
  const [collapsedGroups, setCollapsedGroups] = useState<Record<string, boolean>>({
    overdue: false,
    today: false,
    upcoming: false,
    history: true,
  });

  const toggleGroup = (groupKey: string) => {
    setCollapsedGroups((prev) => ({ ...prev, [groupKey]: !prev[groupKey] }));
  };

  const groups = useMemo(() => {
    const overdue: Activity[] = [];
    const today: Activity[] = [];
    const upcoming: Activity[] = [];
    const history: Activity[] = [];

    const now = new Date();
    const todayStr = now.toDateString();

    activities.forEach((act) => {
      if (act.status === 'COMPLETED' || act.status === 'CANCELLED') {
        history.push(act);
      } else {
        const actDate = act.scheduledDate ? new Date(act.scheduledDate) : null;
        if (!actDate) {
          upcoming.push(act);
        } else if (actDate.getTime() < now.getTime() && actDate.toDateString() !== todayStr) {
          overdue.push(act);
        } else if (actDate.toDateString() === todayStr) {
          today.push(act);
        } else {
          upcoming.push(act);
        }
      }
    });

    return { overdue, today, upcoming, history };
  }, [activities]);

  const renderSection = (title: string, groupKey: string, list: Activity[]) => {
    const isCollapsed = collapsedGroups[groupKey];
    if (list.length === 0) return null;

    return (
      <div className="space-y-2.5">
        <button
          onClick={() => toggleGroup(groupKey)}
          className="flex items-center gap-2 text-xs font-extrabold uppercase tracking-wider text-muted-foreground hover:text-foreground transition select-none"
        >
          {isCollapsed ? <ChevronRight className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
          {title} ({list.length})
        </button>

        {!isCollapsed && (
          <div className="space-y-2 pl-4 border-l border-muted/50">
            {list.map((act) => {
              const Icon = getActivityIcon(act.type);
              const badgeColor = getActivityColor(act.type);
              const isSelected = selectedActivityIds.has(act.id);

              return (
                <div
                  key={act.id}
                  className={`flex items-center justify-between p-3.5 bg-card border border-muted/50 hover:border-muted-foreground/30 rounded-xl transition duration-300 ${
                    isSelected ? 'ring-2 ring-primary' : 'shadow-sm'
                  }`}
                >
                  <div className="flex items-center gap-3 overflow-hidden">
                    <input
                      type="checkbox"
                      checked={isSelected}
                      onChange={() => onSelectActivity(act.id)}
                      className="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary shrink-0 cursor-pointer"
                    />
                    <div className={`p-2 rounded-lg shrink-0 ${badgeColor}`}>
                      <Icon className="h-4 w-4" />
                    </div>
                    <div className="overflow-hidden">
                      <div
                        className="text-xs font-bold text-foreground hover:text-primary cursor-pointer truncate transition"
                        onClick={() => onViewActivity(act.id)}
                      >
                        {act.subject}
                      </div>
                      <div className="text-[10px] text-muted-foreground/80 flex items-center gap-2 mt-0.5">
                        <Clock className="h-3 w-3" /> {formatDate(act.scheduledDate)}
                        {act.assignedToName && <span>• Rep: {act.assignedToName}</span>}
                      </div>
                    </div>
                  </div>

                  <Badge variant={act.priority === 'URGENT' || act.priority === 'HIGH' ? 'destructive' : 'secondary'} className="text-[9px] px-1.5 h-5">
                    {act.priority}
                  </Badge>
                </div>
              );
            })}
          </div>
        )}
      </div>
    );
  };

  return (
    <div className="space-y-6">
      {renderSection('Overdue Tasks', 'overdue', groups.overdue)}
      {renderSection("Today's Agenda", 'today', groups.today)}
      {renderSection('Upcoming Tasks', 'upcoming', groups.upcoming)}
      {renderSection('Recently Closed / History', 'history', groups.history)}
    </div>
  );
}
