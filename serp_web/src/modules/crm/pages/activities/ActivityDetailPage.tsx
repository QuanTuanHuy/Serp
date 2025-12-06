/**
 * Activity Detail Page Component
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Full activity details with edit capabilities
 */

'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import {
  ArrowLeft,
  Edit,
  Trash2,
  Clock,
  MapPin,
  Users,
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
  ArrowRight,
  User,
  Building2,
  Target,
  Timer,
  Flag,
  RefreshCw,
  MoreHorizontal,
  Copy,
  ExternalLink,
  Bell,
} from 'lucide-react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { Badge } from '@/shared/components/ui/badge';
import {
  Tabs,
  TabsList,
  TabsTrigger,
  TabsContent,
} from '@/shared/components/ui/tabs';
import { Textarea } from '@/shared/components/ui/textarea';
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
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/shared/components/ui/dropdown-menu';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { Avatar, AvatarFallback } from '@/shared/components/ui/avatar';
import { Separator } from '@/shared/components/ui/separator';
import { Activity, ActivityType, ActivityStatus, Priority } from '../../types';
import { MOCK_ACTIVITIES } from '../../mocks';

interface ActivityDetailPageProps {
  activityId: string;
}

// Activity type configuration
const ACTIVITY_TYPE_CONFIG: Record<
  ActivityType,
  { label: string; icon: React.ElementType; color: string; bgColor: string }
> = {
  CALL: {
    label: 'Cuộc gọi',
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
    label: 'Cuộc họp',
    icon: Video,
    color: 'text-purple-600',
    bgColor: 'bg-purple-100',
  },
  TASK: {
    label: 'Công việc',
    icon: ListTodo,
    color: 'text-orange-600',
    bgColor: 'bg-orange-100',
  },
  NOTE: {
    label: 'Ghi chú',
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
    label: 'Đề xuất',
    icon: FileText,
    color: 'text-indigo-600',
    bgColor: 'bg-indigo-100',
  },
  FOLLOW_UP: {
    label: 'Theo dõi',
    icon: RefreshCw,
    color: 'text-cyan-600',
    bgColor: 'bg-cyan-100',
  },
};

// Activity status configuration
const ACTIVITY_STATUS_CONFIG: Record<
  ActivityStatus,
  { label: string; color: string; bgColor: string; icon: React.ElementType }
> = {
  PLANNED: {
    label: 'Planned',
    color: 'text-blue-700',
    bgColor: 'bg-blue-100',
    icon: Calendar,
  },
  IN_PROGRESS: {
    label: 'In Progress',
    color: 'text-yellow-700',
    bgColor: 'bg-yellow-100',
    icon: Clock,
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
    label: 'Quá hạn',
    color: 'text-red-700',
    bgColor: 'bg-red-100',
    icon: AlertCircle,
  },
};

// Priority configuration
const PRIORITY_CONFIG: Record<
  Priority,
  { label: string; color: string; bgColor: string }
> = {
  LOW: { label: 'Thấp', color: 'text-green-700', bgColor: 'bg-green-100' },
  MEDIUM: {
    label: 'Trung bình',
    color: 'text-yellow-700',
    bgColor: 'bg-yellow-100',
  },
  HIGH: { label: 'Cao', color: 'text-orange-700', bgColor: 'bg-orange-100' },
  URGENT: { label: 'Khẩn cấp', color: 'text-red-700', bgColor: 'bg-red-100' },
};

export function ActivityDetailPage({ activityId }: ActivityDetailPageProps) {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState('details');
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [showEditStatusDialog, setShowEditStatusDialog] = useState(false);
  const [showAddNoteDialog, setShowAddNoteDialog] = useState(false);
  const [newStatus, setNewStatus] = useState<ActivityStatus | ''>('');
  const [newNote, setNewNote] = useState('');

  // Find activity from mock data
  const activity = MOCK_ACTIVITIES.find((a) => a.id === activityId);

  if (!activity) {
    return (
      <div className='flex h-[60vh] flex-col items-center justify-center'>
        <AlertCircle className='mb-4 h-16 w-16 text-muted-foreground' />
        <h2 className='mb-2 text-xl font-semibold text-foreground'>
          Không tìm thấy hoạt động
        </h2>
        <p className='mb-4 text-muted-foreground'>
          Hoạt động này không tồn tại hoặc đã bị xóa
        </p>
        <Button asChild>
          <Link href='/crm/activities'>
            <ArrowLeft className='mr-2 h-4 w-4' />
            Quay lại danh sách
          </Link>
        </Button>
      </div>
    );
  }

  const typeConfig = ACTIVITY_TYPE_CONFIG[activity.type];
  const statusConfig = ACTIVITY_STATUS_CONFIG[activity.status];
  const priorityConfig = PRIORITY_CONFIG[activity.priority];
  const TypeIcon = typeConfig.icon;
  const StatusIcon = statusConfig.icon;

  // Format date helper
  const formatDate = (dateString?: string) => {
    if (!dateString) return 'Chưa xác định';
    return new Date(dateString).toLocaleDateString('vi-VN', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  // Format duration helper
  const formatDuration = (minutes?: number) => {
    if (!minutes) return 'Chưa xác định';
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (hours > 0) {
      return `${hours} giờ ${mins > 0 ? `${mins} phút` : ''}`;
    }
    return `${mins} phút`;
  };

  // Get related entity link
  const getRelatedLink = () => {
    switch (activity.relatedTo.type) {
      case 'CUSTOMER':
        return `/crm/customers/${activity.relatedTo.id}`;
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

  const RelatedIcon = getRelatedIcon();

  // Mock related activities (would come from API)
  const relatedActivities = MOCK_ACTIVITIES.filter(
    (a) => a.id !== activity.id && a.relatedTo.id === activity.relatedTo.id
  ).slice(0, 5);

  // Mock activity notes/comments
  const activityNotes = [
    {
      id: '1',
      author: 'Nguyễn Văn An',
      content: 'Đã liên hệ khách hàng, họ rất quan tâm đến sản phẩm.',
      createdAt: '2025-01-15T10:30:00Z',
    },
    {
      id: '2',
      author: 'Trần Thị Bình',
      content: 'Cần chuẩn bị thêm tài liệu demo cho cuộc họp tuần sau.',
      createdAt: '2025-01-14T14:15:00Z',
    },
  ];

  const handleStatusChange = () => {
    if (newStatus) {
      // Would update via API
      console.log('Updating status to:', newStatus);
      setShowEditStatusDialog(false);
      setNewStatus('');
    }
  };

  const handleAddNote = () => {
    if (newNote.trim()) {
      // Would add via API
      console.log('Adding note:', newNote);
      setShowAddNoteDialog(false);
      setNewNote('');
    }
  };

  const handleDelete = () => {
    // Would delete via API
    console.log('Deleting activity:', activityId);
    setShowDeleteDialog(false);
    router.push('/crm/activities');
  };

  return (
    <div className='space-y-6'>
      {/* Header */}
      <div className='flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between'>
        <div className='flex items-center gap-4'>
          <Button variant='ghost' size='icon' asChild>
            <Link href='/crm/activities'>
              <ArrowLeft className='h-5 w-5' />
            </Link>
          </Button>
          <div className='flex items-center gap-3'>
            <div className={`rounded-xl p-3 ${typeConfig.bgColor}`}>
              <TypeIcon className={`h-6 w-6 ${typeConfig.color}`} />
            </div>
            <div>
              <h1 className='text-2xl font-bold text-foreground'>
                {activity.subject}
              </h1>
              <div className='mt-1 flex items-center gap-2'>
                <Badge
                  className={`${statusConfig.bgColor} ${statusConfig.color}`}
                >
                  <StatusIcon className='mr-1 h-3 w-3' />
                  {statusConfig.label}
                </Badge>
                <Badge variant='outline'>{typeConfig.label}</Badge>
                <Badge
                  className={`${priorityConfig.bgColor} ${priorityConfig.color}`}
                >
                  <Flag className='mr-1 h-3 w-3' />
                  {priorityConfig.label}
                </Badge>
              </div>
            </div>
          </div>
        </div>

        <div className='flex items-center gap-2'>
          <Button
            variant='outline'
            onClick={() => setShowEditStatusDialog(true)}
          >
            <RefreshCw className='mr-2 h-4 w-4' />
            Cập nhật trạng thái
          </Button>
          <Button variant='outline' asChild>
            <Link href={`/crm/activities/${activityId}/edit`}>
              <Edit className='mr-2 h-4 w-4' />
              Chỉnh sửa
            </Link>
          </Button>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant='outline' size='icon'>
                <MoreHorizontal className='h-4 w-4' />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align='end'>
              <DropdownMenuItem>
                <Copy className='mr-2 h-4 w-4' />
                Tạo bản sao
              </DropdownMenuItem>
              <DropdownMenuItem>
                <Bell className='mr-2 h-4 w-4' />
                Đặt nhắc nhở
              </DropdownMenuItem>
              <DropdownMenuItem>
                <ExternalLink className='mr-2 h-4 w-4' />
                Xuất PDF
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem
                className='text-red-600'
                onClick={() => setShowDeleteDialog(true)}
              >
                <Trash2 className='mr-2 h-4 w-4' />
                Xóa hoạt động
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>

      {/* Main Content */}
      <div className='grid gap-6 lg:grid-cols-3'>
        {/* Left Column - Main Info */}
        <div className='space-y-6 lg:col-span-2'>
          <Tabs value={activeTab} onValueChange={setActiveTab}>
            <TabsList className='w-full justify-start'>
              <TabsTrigger value='details'>Chi tiết</TabsTrigger>
              <TabsTrigger value='notes'>
                Ghi chú ({activityNotes.length})
              </TabsTrigger>
              <TabsTrigger value='related'>
                Liên quan ({relatedActivities.length})
              </TabsTrigger>
            </TabsList>

            <TabsContent value='details' className='mt-4 space-y-6'>
              {/* Description */}
              <Card className='border-none shadow-sm'>
                <CardHeader className='pb-3'>
                  <CardTitle className='text-lg font-semibold'>Mô tả</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className='whitespace-pre-wrap text-foreground/80'>
                    {activity.description || 'Không có mô tả'}
                  </p>
                </CardContent>
              </Card>

              {/* Schedule Info */}
              <Card className='border-none shadow-sm'>
                <CardHeader className='pb-3'>
                  <CardTitle className='text-lg font-semibold'>
                    Thông tin lịch trình
                  </CardTitle>
                </CardHeader>
                <CardContent className='space-y-4'>
                  <div className='grid gap-4 sm:grid-cols-2'>
                    <div className='flex items-start gap-3'>
                      <div className='rounded-lg bg-blue-100 p-2'>
                        <Calendar className='h-4 w-4 text-blue-600' />
                      </div>
                      <div>
                        <p className='text-sm text-muted-foreground'>
                          Thời gian dự kiến
                        </p>
                        <p className='font-medium text-foreground'>
                          {formatDate(activity.scheduledDate)}
                        </p>
                      </div>
                    </div>
                    {activity.actualDate && (
                      <div className='flex items-start gap-3'>
                        <div className='rounded-lg bg-green-100 p-2'>
                          <CheckCircle className='h-4 w-4 text-green-600' />
                        </div>
                        <div>
                          <p className='text-sm text-muted-foreground'>
                            Thời gian thực tế
                          </p>
                          <p className='font-medium text-foreground'>
                            {formatDate(activity.actualDate)}
                          </p>
                        </div>
                      </div>
                    )}
                    <div className='flex items-start gap-3'>
                      <div className='rounded-lg bg-purple-100 p-2'>
                        <Timer className='h-4 w-4 text-purple-600' />
                      </div>
                      <div>
                        <p className='text-sm text-muted-foreground'>
                          Thời lượng
                        </p>
                        <p className='font-medium text-foreground'>
                          {formatDuration(activity.duration)}
                        </p>
                      </div>
                    </div>
                    {activity.location && (
                      <div className='flex items-start gap-3'>
                        <div className='rounded-lg bg-orange-100 p-2'>
                          <MapPin className='h-4 w-4 text-orange-600' />
                        </div>
                        <div>
                          <p className='text-sm text-muted-foreground'>
                            Địa điểm
                          </p>
                          <p className='font-medium text-foreground'>
                            {activity.location}
                          </p>
                        </div>
                      </div>
                    )}
                  </div>
                </CardContent>
              </Card>

              {/* Participants */}
              {activity.participants && activity.participants.length > 0 && (
                <Card className='border-none shadow-sm'>
                  <CardHeader className='pb-3'>
                    <CardTitle className='flex items-center text-lg font-semibold'>
                      <Users className='mr-2 h-5 w-5 text-muted-foreground' />
                      Người tham gia ({activity.participants.length})
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className='flex flex-wrap gap-2'>
                      {activity.participants.map((participant, index) => (
                        <div
                          key={index}
                          className='flex items-center gap-2 rounded-full bg-muted px-3 py-1.5'
                        >
                          <Avatar className='h-6 w-6'>
                            <AvatarFallback className='text-xs'>
                              {participant
                                .split(' ')
                                .map((n) => n[0])
                                .join('')}
                            </AvatarFallback>
                          </Avatar>
                          <span className='text-sm font-medium text-foreground'>
                            {participant}
                          </span>
                        </div>
                      ))}
                    </div>
                  </CardContent>
                </Card>
              )}

              {/* Outcome */}
              {activity.outcome && (
                <Card className='border-none shadow-sm'>
                  <CardHeader className='pb-3'>
                    <CardTitle className='text-lg font-semibold'>
                      Kết quả
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className='whitespace-pre-wrap text-foreground/80'>
                      {activity.outcome}
                    </p>
                  </CardContent>
                </Card>
              )}

              {/* Follow-up */}
              {activity.followUpRequired && (
                <Card className='border-none bg-amber-50 shadow-sm'>
                  <CardHeader className='pb-3'>
                    <CardTitle className='flex items-center text-lg font-semibold text-amber-800'>
                      <Bell className='mr-2 h-5 w-5' />
                      Cần theo dõi
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className='flex items-center justify-between'>
                      <div>
                        <p className='text-amber-700'>
                          Ngày theo dõi: {formatDate(activity.followUpDate)}
                        </p>
                      </div>
                      <Button
                        size='sm'
                        className='bg-amber-600 hover:bg-amber-700'
                      >
                        <Calendar className='mr-2 h-4 w-4' />
                        Tạo hoạt động theo dõi
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              )}

              {/* Tags */}
              {activity.tags.length > 0 && (
                <Card className='border-none shadow-sm'>
                  <CardHeader className='pb-3'>
                    <CardTitle className='text-lg font-semibold'>
                      Tags
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className='flex flex-wrap gap-2'>
                      {activity.tags.map((tag) => (
                        <Badge key={tag} variant='secondary'>
                          {tag}
                        </Badge>
                      ))}
                    </div>
                  </CardContent>
                </Card>
              )}
            </TabsContent>

            <TabsContent value='notes' className='mt-4 space-y-4'>
              <div className='flex justify-end'>
                <Button onClick={() => setShowAddNoteDialog(true)}>
                  <MessageSquare className='mr-2 h-4 w-4' />
                  Add Note
                </Button>
              </div>

              {activityNotes.length > 0 ? (
                <div className='space-y-4'>
                  {activityNotes.map((note) => (
                    <Card key={note.id} className='border-none shadow-sm'>
                      <CardContent className='pt-4'>
                        <div className='flex items-start gap-3'>
                          <Avatar className='h-8 w-8'>
                            <AvatarFallback>
                              {note.author
                                .split(' ')
                                .map((n) => n[0])
                                .join('')}
                            </AvatarFallback>
                          </Avatar>
                          <div className='flex-1'>
                            <div className='flex items-center justify-between'>
                              <p className='font-medium text-foreground'>
                                {note.author}
                              </p>
                              <p className='text-sm text-muted-foreground'>
                                {new Date(note.createdAt).toLocaleDateString(
                                  'vi-VN',
                                  {
                                    day: '2-digit',
                                    month: '2-digit',
                                    year: 'numeric',
                                    hour: '2-digit',
                                    minute: '2-digit',
                                  }
                                )}
                              </p>
                            </div>
                            <p className='mt-2 text-foreground/80'>
                              {note.content}
                            </p>
                          </div>
                        </div>
                      </CardContent>
                    </Card>
                  ))}
                </div>
              ) : (
                <Card className='border-none shadow-sm'>
                  <CardContent className='flex flex-col items-center justify-center py-12'>
                    <MessageSquare className='mb-4 h-12 w-12 text-muted-foreground/50' />
                    <p className='text-muted-foreground'>Chưa có ghi chú nào</p>
                  </CardContent>
                </Card>
              )}
            </TabsContent>

            <TabsContent value='related' className='mt-4 space-y-4'>
              {relatedActivities.length > 0 ? (
                <div className='space-y-3'>
                  {relatedActivities.map((relatedActivity) => {
                    const relTypeConfig =
                      ACTIVITY_TYPE_CONFIG[relatedActivity.type];
                    const relStatusConfig =
                      ACTIVITY_STATUS_CONFIG[relatedActivity.status];
                    const RelTypeIcon = relTypeConfig.icon;
                    return (
                      <Card
                        key={relatedActivity.id}
                        className='cursor-pointer border-none shadow-sm transition-shadow hover:shadow-md'
                        onClick={() =>
                          router.push(`/crm/activities/${relatedActivity.id}`)
                        }
                      >
                        <CardContent className='flex items-center gap-4 p-4'>
                          <div
                            className={`rounded-lg p-2 ${relTypeConfig.bgColor}`}
                          >
                            <RelTypeIcon
                              className={`h-5 w-5 ${relTypeConfig.color}`}
                            />
                          </div>
                          <div className='flex-1'>
                            <p className='font-medium text-foreground'>
                              {relatedActivity.subject}
                            </p>
                            <p className='text-sm text-muted-foreground'>
                              {formatDate(relatedActivity.scheduledDate)}
                            </p>
                          </div>
                          <Badge
                            className={`${relStatusConfig.bgColor} ${relStatusConfig.color}`}
                          >
                            {relStatusConfig.label}
                          </Badge>
                          <ArrowRight className='h-4 w-4 text-muted-foreground' />
                        </CardContent>
                      </Card>
                    );
                  })}
                </div>
              ) : (
                <Card className='border-none shadow-sm'>
                  <CardContent className='flex flex-col items-center justify-center py-12'>
                    <Calendar className='mb-4 h-12 w-12 text-muted-foreground/50' />
                    <p className='text-muted-foreground'>
                      Không có hoạt động liên quan
                    </p>
                  </CardContent>
                </Card>
              )}
            </TabsContent>
          </Tabs>
        </div>

        {/* Right Column - Sidebar */}
        <div className='space-y-6'>
          {/* Related Entity */}
          <Card className='border-none shadow-sm'>
            <CardHeader className='pb-3'>
              <CardTitle className='text-lg font-semibold'>
                Liên kết với
              </CardTitle>
            </CardHeader>
            <CardContent>
              <Link href={getRelatedLink()} className='block'>
                <div className='flex items-center gap-3 rounded-lg bg-muted p-3 transition-colors hover:bg-muted/80'>
                  <div className='rounded-lg bg-blue-100 dark:bg-blue-900/30 p-2'>
                    <RelatedIcon className='h-5 w-5 text-blue-600 dark:text-blue-400' />
                  </div>
                  <div className='flex-1'>
                    <p className='text-sm text-muted-foreground'>
                      {activity.relatedTo.type === 'CUSTOMER' && 'Khách hàng'}
                      {activity.relatedTo.type === 'LEAD' && 'Lead'}
                      {activity.relatedTo.type === 'OPPORTUNITY' && 'Cơ hội'}
                    </p>
                    <p className='font-medium text-foreground'>
                      {activity.relatedTo.name}
                    </p>
                  </div>
                  <ExternalLink className='h-4 w-4 text-muted-foreground' />
                </div>
              </Link>
            </CardContent>
          </Card>

          {/* Assigned To */}
          <Card className='border-none shadow-sm'>
            <CardHeader className='pb-3'>
              <CardTitle className='text-lg font-semibold'>
                Người phụ trách
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className='flex items-center gap-3'>
                <Avatar className='h-10 w-10'>
                  <AvatarFallback>
                    {activity.assignedToName
                      .split(' ')
                      .map((n) => n[0])
                      .join('')}
                  </AvatarFallback>
                </Avatar>
                <div>
                  <p className='font-medium text-foreground'>
                    {activity.assignedToName}
                  </p>
                  <p className='text-sm text-muted-foreground'>
                    Sales Representative
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Activity Info */}
          <Card className='border-none shadow-sm'>
            <CardHeader className='pb-3'>
              <CardTitle className='text-lg font-semibold'>Thông tin</CardTitle>
            </CardHeader>
            <CardContent className='space-y-4'>
              <div className='flex items-center justify-between'>
                <span className='text-sm text-muted-foreground'>
                  Mã hoạt động
                </span>
                <span className='font-mono text-sm text-foreground'>
                  #{activity.id}
                </span>
              </div>
              <Separator />
              <div className='flex items-center justify-between'>
                <span className='text-sm text-muted-foreground'>Ngày tạo</span>
                <span className='text-sm text-foreground'>
                  {new Date(activity.createdAt).toLocaleDateString('vi-VN')}
                </span>
              </div>
              <Separator />
              <div className='flex items-center justify-between'>
                <span className='text-sm text-muted-foreground'>
                  Cập nhật lần cuối
                </span>
                <span className='text-sm text-foreground'>
                  {new Date(activity.updatedAt).toLocaleDateString('vi-VN')}
                </span>
              </div>
              <Separator />
              <div className='flex items-center justify-between'>
                <span className='text-sm text-muted-foreground'>
                  Trạng thái
                </span>
                <span className={`text-sm font-medium ${statusConfig.color}`}>
                  {statusConfig.label}
                </span>
              </div>
            </CardContent>
          </Card>

          {/* Quick Actions */}
          <Card className='border-none shadow-sm'>
            <CardHeader className='pb-3'>
              <CardTitle className='text-lg font-semibold'>
                Thao tác nhanh
              </CardTitle>
            </CardHeader>
            <CardContent className='space-y-2'>
              {activity.status !== 'COMPLETED' && (
                <Button
                  className='w-full justify-start'
                  variant='outline'
                  onClick={() => {
                    setNewStatus('COMPLETED');
                    setShowEditStatusDialog(true);
                  }}
                >
                  <CheckCircle className='mr-2 h-4 w-4 text-green-600' />
                  Đánh dấu hoàn thành
                </Button>
              )}
              <Button className='w-full justify-start' variant='outline'>
                <Copy className='mr-2 h-4 w-4' />
                Tạo hoạt động tương tự
              </Button>
              <Button className='w-full justify-start' variant='outline'>
                <Calendar className='mr-2 h-4 w-4' />
                Lên lịch lại
              </Button>
            </CardContent>
          </Card>
        </div>
      </div>

      {/* Delete Confirmation Dialog */}
      <Dialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Xác nhận xóa hoạt động</DialogTitle>
            <DialogDescription>
              Bạn có chắc chắn muốn xóa hoạt động &quot;{activity.subject}
              &quot;? Hành động này không thể hoàn tác.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button
              variant='outline'
              onClick={() => setShowDeleteDialog(false)}
            >
              Hủy
            </Button>
            <Button variant='destructive' onClick={handleDelete}>
              Xóa hoạt động
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Edit Status Dialog */}
      <Dialog
        open={showEditStatusDialog}
        onOpenChange={setShowEditStatusDialog}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Cập nhật trạng thái</DialogTitle>
            <DialogDescription>
              Chọn trạng thái mới cho hoạt động này
            </DialogDescription>
          </DialogHeader>
          <div className='py-4'>
            <Select
              value={newStatus}
              onValueChange={(value) => setNewStatus(value as ActivityStatus)}
            >
              <SelectTrigger>
                <SelectValue placeholder='Chọn trạng thái' />
              </SelectTrigger>
              <SelectContent>
                {Object.entries(ACTIVITY_STATUS_CONFIG).map(([key, config]) => (
                  <SelectItem key={key} value={key}>
                    <div className='flex items-center gap-2'>
                      <config.icon className={`h-4 w-4 ${config.color}`} />
                      {config.label}
                    </div>
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <DialogFooter>
            <Button
              variant='outline'
              onClick={() => setShowEditStatusDialog(false)}
            >
              Hủy
            </Button>
            <Button onClick={handleStatusChange} disabled={!newStatus}>
              Cập nhật
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Add Note Dialog */}
      <Dialog open={showAddNoteDialog} onOpenChange={setShowAddNoteDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Thêm ghi chú</DialogTitle>
            <DialogDescription>
              Thêm ghi chú mới cho hoạt động này
            </DialogDescription>
          </DialogHeader>
          <div className='py-4'>
            <Textarea
              placeholder='Nhập nội dung ghi chú...'
              value={newNote}
              onChange={(e) => setNewNote(e.target.value)}
              rows={4}
            />
          </div>
          <DialogFooter>
            <Button
              variant='outline'
              onClick={() => setShowAddNoteDialog(false)}
            >
              Hủy
            </Button>
            <Button onClick={handleAddNote} disabled={!newNote.trim()}>
              Thêm ghi chú
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

export default ActivityDetailPage;
