/**
 * Activity Detail Page Component
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Modernized 2-column activity details
 */

'use client';

import React, { useState } from 'react';
import { toast } from 'sonner';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import {
  ArrowLeft,
  Edit,
  Clock,
  Calendar,
  CheckCircle,
  AlertCircle,
  Phone,
  Mail,
  Video,
  FileText,
  ListTodo,
  MessageSquare,
  Presentation,
  Flag,
  RefreshCw,
  MoreHorizontal,
  Bell,
  Trash2,
} from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import { Badge } from '@/shared/components/ui/badge';
import {
  Tabs,
  TabsList,
  TabsTrigger,
  TabsContent,
} from '@/shared/components/ui/tabs';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/shared/components/ui/dialog';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/shared/components/ui/dropdown-menu';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';

import type {
  Activity,
  ActivityDisplayStatus,
  ActivityType,
  ActivityStatus,
  Priority,
} from '../../types';
import {
  useCancelActivityMutation,
  useCompleteActivityMutation,
  useDeleteActivityMutation,
  useRescheduleActivityMutation,
  useGetActivityQuery,
} from '../../api/crmApi';
import { getActivityDisplayStatus } from '../../utils';

// Sub-components
import { ActivityProfile } from './components/detail/ActivityProfile';
import { ActivityNotesTab } from './components/detail/ActivityNotesTab';
import { ActivityMetadataSidebar } from './components/detail/ActivityMetadataSidebar';

interface ActivityDetailPageProps {
  activityId: string;
}

const ACTIVITY_TYPE_CONFIG: Record<
  ActivityType,
  { label: string; icon: React.ElementType; color: string; bgColor: string }
> = {
  CALL: {
    label: 'Call',
    icon: Phone,
    color: 'text-blue-600',
    bgColor: 'bg-blue-100',
  },
  EMAIL: {
    label: 'Email',
    icon: Mail,
    color: 'text-green-600',
    bgColor: 'bg-green-100',
  },
  MEETING: {
    label: 'Meeting',
    icon: Video,
    color: 'text-purple-600',
    bgColor: 'bg-purple-100',
  },
  TASK: {
    label: 'Task',
    icon: ListTodo,
    color: 'text-orange-600',
    bgColor: 'bg-orange-100',
  },
  NOTE: {
    label: 'Note',
    icon: MessageSquare,
    color: 'text-gray-600 dark:text-gray-300',
    bgColor: 'bg-gray-100 dark:bg-gray-800',
  },
  DEMO: {
    label: 'Demo',
    icon: Presentation,
    color: 'text-pink-600',
    bgColor: 'bg-pink-100',
  },
  PROPOSAL: {
    label: 'Proposal',
    icon: FileText,
    color: 'text-indigo-600',
    bgColor: 'bg-indigo-100',
  },
  FOLLOW_UP: {
    label: 'Follow Up',
    icon: RefreshCw,
    color: 'text-cyan-600',
    bgColor: 'bg-cyan-100',
  },
};

const ACTIVITY_STATUS_CONFIG: Record<
  ActivityDisplayStatus,
  { label: string; color: string; bgColor: string; icon: React.ElementType }
> = {
  PLANNED: {
    label: 'Planned',
    color: 'text-blue-700',
    bgColor: 'bg-blue-100',
    icon: Calendar,
  },
  COMPLETED: {
    label: 'Completed',
    color: 'text-green-700',
    bgColor: 'bg-green-100',
    icon: CheckCircle,
  },
  CANCELLED: {
    label: 'Cancelled',
    color: 'text-gray-700 dark:text-gray-300',
    bgColor: 'bg-gray-100 dark:bg-gray-800',
    icon: AlertCircle,
  },
  OVERDUE: {
    label: 'Overdue',
    color: 'text-red-700',
    bgColor: 'bg-red-100',
    icon: AlertCircle,
  },
};



const PRIORITY_CONFIG: Record<
  Priority,
  { label: string; color: string; bgColor: string }
> = {
  LOW: { label: 'Low', color: 'text-green-700', bgColor: 'bg-green-100' },
  MEDIUM: {
    label: 'Medium',
    color: 'text-yellow-700',
    bgColor: 'bg-yellow-100',
  },
  HIGH: { label: 'High', color: 'text-orange-700', bgColor: 'bg-orange-100' },
  URGENT: { label: 'Urgent', color: 'text-red-700', bgColor: 'bg-red-100' },
};

export function ActivityDetailPage({ activityId }: ActivityDetailPageProps) {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState('details');
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [showEditStatusDialog, setShowEditStatusDialog] = useState(false);
  const [showRescheduleDialog, setShowRescheduleDialog] = useState(false);
  const [newStatus, setNewStatus] = useState<ActivityStatus | ''>('');
  const [rescheduleData, setRescheduleData] = useState({
    dueDate: '',
    reminderDate: '',
  });

  const { data, isLoading, isError } = useGetActivityQuery(activityId);
  const [completeActivity] = useCompleteActivityMutation();
  const [cancelActivity] = useCancelActivityMutation();
  const [deleteActivity] = useDeleteActivityMutation();
  const [rescheduleActivity] = useRescheduleActivityMutation();
  
  const activity = data?.data;

  if (isLoading) {
    return (
      <div className="flex h-[60vh] items-center justify-center text-muted-foreground text-xs">
        Loading workspace details...
      </div>
    );
  }

  if (isError || !activity) {
    return (
      <div className="flex h-[60vh] flex-col items-center justify-center">
        <AlertCircle className="mb-4 h-16 w-16 text-muted-foreground" />
        <h2 className="mb-2 text-xl font-bold text-foreground">Activity not found</h2>
        <p className="mb-4 text-xs text-muted-foreground">This activity does not exist or has been deleted</p>
        <Button asChild>
          <Link href="/crm/activities">
            <ArrowLeft className="mr-2 h-4 w-4" /> Back to activities
          </Link>
        </Button>
      </div>
    );
  }

  const typeConfig = ACTIVITY_TYPE_CONFIG[activity.type] || ACTIVITY_TYPE_CONFIG.TASK;
  const statusConfig = ACTIVITY_STATUS_CONFIG[getActivityDisplayStatus(activity)];
  const priorityConfig = PRIORITY_CONFIG[activity.priority];
  const TypeIcon = typeConfig.icon;
  const StatusIcon = statusConfig.icon;

  const formatDate = (dateString?: string) => {
    if (!dateString) return 'TBD';
    return new Date(dateString).toLocaleDateString('vi-VN', {
      weekday: 'short',
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const formatDuration = (minutes?: number) => {
    if (!minutes) return 'TBD';
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (hours > 0) {
      return `${hours}h${mins > 0 ? ` ${mins}m` : ''}`;
    }
    return `${mins}m`;
  };

  const assigneeDisplayName =
    activity.assignedToName?.trim() ||
    (activity.assignedTo?.trim() ? `User #${activity.assignedTo}` : 'Unassigned');

  const assigneeInitials =
    assigneeDisplayName
      .split(/\s+/)
      .filter(Boolean)
      .map((segment) => segment[0])
      .join('')
      .toUpperCase()
      .slice(0, 2) || '?';

  const activityNotes = activity.customFields?.notes
    ? [
        {
          id: activity.id,
          author: assigneeDisplayName || 'System',
          content: String(activity.customFields.notes),
          createdAt: activity.updatedAt,
        },
      ]
    : [];

  const handleStatusChange = async () => {
    try {
      if (newStatus === 'COMPLETED') {
        await completeActivity({ id: activityId }).unwrap();
        toast.success('Activity completed');
      } else if (newStatus === 'CANCELLED') {
        await cancelActivity(activityId).unwrap();
        toast.success('Activity cancelled');
      }
      setShowEditStatusDialog(false);
      setNewStatus('');
    } catch {
      toast.error('Failed to update activity status');
    }
  };

  const handleReschedule = async () => {
    try {
      if (!rescheduleData.dueDate) {
        toast.error('Due date is required');
        return;
      }

      const dueDateEpoch = new Date(rescheduleData.dueDate).getTime();
      const reminderDateEpoch = rescheduleData.reminderDate
        ? new Date(rescheduleData.reminderDate).getTime()
        : undefined;

      await rescheduleActivity({
        id: activityId,
        data: {
          dueDate: dueDateEpoch,
          reminderDate: reminderDateEpoch,
        },
      }).unwrap();
      toast.success('Activity rescheduled');
      setShowRescheduleDialog(false);
      setRescheduleData({ dueDate: '', reminderDate: '' });
    } catch {
      toast.error('Failed to reschedule activity');
    }
  };

  const handleDelete = async () => {
    try {
      await deleteActivity(activityId).unwrap();
      toast.success('Activity deleted');
      setShowDeleteDialog(false);
      router.push('/crm/activities');
    } catch {
      toast.error('Failed to delete activity');
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between pb-4 border-b border-muted/50">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" asChild className="rounded-full">
            <Link href="/crm/activities">
              <ArrowLeft className="h-5 w-5" />
            </Link>
          </Button>
          <div className="flex items-center gap-3">
            <div className={`rounded-xl p-2.5 ${typeConfig.bgColor}`}>
              <TypeIcon className={`h-5.5 w-5.5 ${typeConfig.color}`} />
            </div>
            <div>
              <h1 className="text-xl font-extrabold text-foreground tracking-tight">
                {activity.subject}
              </h1>
              <div className="mt-1 flex items-center gap-2 flex-wrap">
                <Badge className={`${statusConfig.bgColor} ${statusConfig.color} text-[10px] px-2`}>
                  <StatusIcon className="mr-1 h-3.5 w-3.5" />
                  {statusConfig.label}
                </Badge>
                <Badge variant="outline" className="text-[10px] px-2">{typeConfig.label}</Badge>
                <Badge className={`${priorityConfig.bgColor} ${priorityConfig.color} text-[10px] px-2`}>
                  <Flag className="mr-1 h-3 w-3" />
                  {priorityConfig.label}
                </Badge>
              </div>
            </div>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" asChild>
            <Link href={`/crm/activities/${activityId}/edit`}>
              <Edit className="mr-1.5 h-4 w-4" /> Edit Details
            </Link>
          </Button>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="outline" size="icon">
                <MoreHorizontal className="h-4 w-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem className="text-red-600 focus:bg-red-50 focus:text-red-700" onClick={() => setShowDeleteDialog(true)}>
                <Trash2 className="mr-2 h-4 w-4" /> Delete record
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>

      {/* Main 2-Column Grid */}
      <div className="grid gap-6 grid-cols-1 lg:grid-cols-3">
        {/* Left Column: Workspaces */}
        <div className="lg:col-span-2 space-y-6">
          <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
            <TabsList className="grid w-full grid-cols-2">
              <TabsTrigger value="details">Activity Details</TabsTrigger>
              <TabsTrigger value="notes">Notes ({activityNotes.length})</TabsTrigger>
            </TabsList>

            <TabsContent value="details" className="mt-6">
              <ActivityProfile activity={activity} formatDate={formatDate} />
            </TabsContent>

            <TabsContent value="notes" className="mt-6">
              <ActivityNotesTab notes={activityNotes} />
            </TabsContent>
          </Tabs>
        </div>

        {/* Right Column: Metadata Sidebar */}
        <div className="lg:col-span-1">
          <ActivityMetadataSidebar
            activity={activity}
            assigneeDisplayName={assigneeDisplayName}
            assigneeInitials={assigneeInitials}
            formatDate={formatDate}
            formatDuration={formatDuration}
            onOpenReschedule={() => setShowRescheduleDialog(true)}
            onOpenStatusUpdate={() => {
              setNewStatus('COMPLETED');
              setShowEditStatusDialog(true);
            }}
            onDeleteActivity={() => setShowDeleteDialog(true)}
          />
        </div>
      </div>

      {/* Dialogs */}
      <Dialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Confirm delete activity</DialogTitle>
            <DialogDescription>
              Are you sure you want to delete activity &quot;{activity.subject}&quot;? This action cannot be undone.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowDeleteDialog(false)}>Cancel</Button>
            <Button variant="destructive" onClick={handleDelete}>Delete record</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={showEditStatusDialog} onOpenChange={setShowEditStatusDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Update status</DialogTitle>
            <DialogDescription>Select new status for this activity</DialogDescription>
          </DialogHeader>
          <div className="py-4">
            <Select value={newStatus} onValueChange={(value) => setNewStatus(value as ActivityStatus)}>
              <SelectTrigger>
                <SelectValue placeholder="Select status" />
              </SelectTrigger>
              <SelectContent>
                {(['COMPLETED', 'CANCELLED'] as ActivityStatus[]).map((status) => {
                  const config = ACTIVITY_STATUS_CONFIG[status as ActivityDisplayStatus];
                  return (
                    <SelectItem key={status} value={status}>
                      <div className="flex items-center gap-2">
                        <config.icon className={`h-4 w-4 ${config.color}`} />
                        {config.label}
                      </div>
                    </SelectItem>
                  );
                })}
              </SelectContent>
            </Select>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowEditStatusDialog(false)}>Cancel</Button>
            <Button onClick={handleStatusChange} disabled={!newStatus}>Update</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={showRescheduleDialog} onOpenChange={setShowRescheduleDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Reschedule activity</DialogTitle>
            <DialogDescription>Update the due date and reminder for this activity</DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div>
              <label className="text-xs font-semibold mb-1.5 block">Due Date</label>
              <input
                type="datetime-local"
                value={rescheduleData.dueDate}
                onChange={(e) => setRescheduleData({ ...rescheduleData, dueDate: e.target.value })}
                className="w-full px-3 py-2 border border-border rounded-lg bg-background text-sm focus:outline-none focus:ring-2 focus:ring-ring"
              />
            </div>
            <div>
              <label className="text-xs font-semibold mb-1.5 block">Reminder Date (Optional)</label>
              <input
                type="datetime-local"
                value={rescheduleData.reminderDate}
                onChange={(e) => setRescheduleData({ ...rescheduleData, reminderDate: e.target.value })}
                className="w-full px-3 py-2 border border-border rounded-lg bg-background text-sm focus:outline-none focus:ring-2 focus:ring-ring"
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => {
              setShowRescheduleDialog(false);
              setRescheduleData({ dueDate: '', reminderDate: '' });
            }}>Cancel</Button>
            <Button onClick={handleReschedule} disabled={!rescheduleData.dueDate}>Reschedule</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

export default ActivityDetailPage;
