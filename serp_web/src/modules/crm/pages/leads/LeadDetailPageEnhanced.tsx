/**
 * Lead Detail Page Component - Enhanced Version
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Detailed lead view with conversion flow
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
  Mail,
  Phone,
  Building2,
  Briefcase,
  Calendar,
  CheckCircle,
  AlertCircle,
  User,
  TrendingUp,
  Target,
  Globe,
  Users,
  MessageSquare,
  Activity,
  MoreHorizontal,
  Copy,
  ExternalLink,
  UserPlus,
  DollarSign,
  Flag,
  Tag,
  FileText,
  ArrowRight,
  Sparkles,
  ChevronRight,
  RefreshCw,
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
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import { Progress } from '@/shared/components/ui/progress';
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
import {
  Lead,
  LeadStatus,
  LeadSource,
  Priority,
  Activity as ActivityType,
} from '../../types';
import { MOCK_LEADS, MOCK_ACTIVITIES } from '../../mocks';

interface LeadDetailPageProps {
  leadId: string;
}

// Lead status configuration
const LEAD_STATUS_CONFIG: Record<
  LeadStatus,
  {
    label: string;
    color: string;
    bgColor: string;
    icon: React.ElementType;
    step: number;
  }
> = {
  NEW: {
    label: 'Mới',
    color: 'text-blue-700',
    bgColor: 'bg-blue-100',
    icon: Sparkles,
    step: 1,
  },
  CONTACTED: {
    label: 'Đã liên hệ',
    color: 'text-purple-700',
    bgColor: 'bg-purple-100',
    icon: Phone,
    step: 2,
  },
  QUALIFIED: {
    label: 'Đủ điều kiện',
    color: 'text-green-700',
    bgColor: 'bg-green-100',
    icon: CheckCircle,
    step: 3,
  },
  CONVERTED: {
    label: 'Đã chuyển đổi',
    color: 'text-emerald-700',
    bgColor: 'bg-emerald-100',
    icon: UserPlus,
    step: 4,
  },
  LOST: {
    label: 'Đã mất',
    color: 'text-gray-700 dark:text-gray-300',
    bgColor: 'bg-gray-100 dark:bg-gray-800',
    icon: AlertCircle,
    step: 0,
  },
};

// Lead source configuration
const LEAD_SOURCE_CONFIG: Record<
  LeadSource,
  { label: string; icon: React.ElementType }
> = {
  WEBSITE: { label: 'Website', icon: Globe },
  REFERRAL: { label: 'Giới thiệu', icon: Users },
  EMAIL: { label: 'Email', icon: Mail },
  PHONE: { label: 'Điện thoại', icon: Phone },
  SOCIAL_MEDIA: { label: 'Mạng xã hội', icon: MessageSquare },
  TRADE_SHOW: { label: 'Triển lãm', icon: Building2 },
  OTHER: { label: 'Khác', icon: FileText },
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

export function LeadDetailPage({ leadId }: LeadDetailPageProps) {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState('overview');
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [showConvertDialog, setShowConvertDialog] = useState(false);
  const [showStatusDialog, setShowStatusDialog] = useState(false);
  const [showAddNoteDialog, setShowAddNoteDialog] = useState(false);
  const [showActivityDialog, setShowActivityDialog] = useState(false);
  const [newStatus, setNewStatus] = useState<LeadStatus | ''>('');
  const [newNote, setNewNote] = useState('');
  const [activityForm, setActivityForm] = useState({
    type: 'CALL',
    subject: '',
    description: '',
    scheduledDate: '',
  });
  const [convertForm, setConvertForm] = useState({
    customerType: 'COMPANY',
    createOpportunity: true,
    opportunityName: '',
    opportunityValue: '',
  });

  // Find lead from mock data
  const lead = MOCK_LEADS.find((l) => l.id === leadId);

  if (!lead) {
    return (
      <div className='flex h-[60vh] flex-col items-center justify-center'>
        <AlertCircle className='mb-4 h-16 w-16 text-muted-foreground' />
        <h2 className='mb-2 text-xl font-semibold text-foreground'>
          Không tìm thấy lead
        </h2>
        <p className='mb-4 text-muted-foreground'>
          Lead này không tồn tại hoặc đã bị xóa
        </p>
        <Button asChild>
          <Link href='/crm/leads'>
            <ArrowLeft className='mr-2 h-4 w-4' />
            Quay lại danh sách
          </Link>
        </Button>
      </div>
    );
  }

  const statusConfig = LEAD_STATUS_CONFIG[lead.status];
  const sourceConfig = LEAD_SOURCE_CONFIG[lead.source];
  const priorityConfig = PRIORITY_CONFIG[lead.priority];
  const StatusIcon = statusConfig.icon;
  const SourceIcon = sourceConfig.icon;

  // Get related activities
  const leadActivities = MOCK_ACTIVITIES.filter(
    (a) => a.relatedTo.type === 'LEAD' && a.relatedTo.id === lead.id
  );

  // Mock notes
  const leadNotes = [
    {
      id: '1',
      author: 'Nguyễn Văn An',
      content:
        'Lead rất quan tâm đến giải pháp ERP. Đã demo qua video call, họ muốn xem thêm tính năng quản lý kho.',
      createdAt: '2025-01-15T10:30:00Z',
    },
    {
      id: '2',
      author: 'Trần Thị Bình',
      content: 'Đã gửi proposal và bảng giá. Chờ phản hồi từ phía khách hàng.',
      createdAt: '2025-01-13T14:15:00Z',
    },
  ];

  // Format date helper
  const formatDate = (dateString?: string) => {
    if (!dateString) return 'Chưa xác định';
    return new Date(dateString).toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  // Format currency helper
  const formatCurrency = (value?: number) => {
    if (!value) return 'Chưa xác định';
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      maximumFractionDigits: 0,
    }).format(value);
  };

  // Calculate lead score (mock)
  const calculateLeadScore = () => {
    let score = 0;
    if (lead.email) score += 20;
    if (lead.phone) score += 15;
    if (lead.company) score += 20;
    if (lead.estimatedValue && lead.estimatedValue > 0) score += 20;
    if (lead.status === 'QUALIFIED') score += 25;
    else if (lead.status === 'CONTACTED') score += 15;
    else if (lead.status === 'NEW') score += 5;
    return Math.min(score, 100);
  };

  const leadScore = calculateLeadScore();

  const handleStatusChange = () => {
    if (newStatus) {
      console.log('Updating status to:', newStatus);
      setShowStatusDialog(false);
      setNewStatus('');
    }
  };

  const handleAddNote = () => {
    if (newNote.trim()) {
      console.log('Adding note:', newNote);
      setShowAddNoteDialog(false);
      setNewNote('');
    }
  };

  const handleConvert = () => {
    console.log('Converting lead:', convertForm);
    setShowConvertDialog(false);
    router.push('/crm/customers');
  };

  const handleDelete = () => {
    console.log('Deleting lead:', leadId);
    setShowDeleteDialog(false);
    router.push('/crm/leads');
  };

  // Progress bar for lead status
  const statusProgress = () => {
    if (lead.status === 'LOST') return 0;
    return (statusConfig.step / 4) * 100;
  };

  return (
    <div className='space-y-6'>
      {/* Header */}
      <div className='flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between'>
        <div className='flex items-center gap-4'>
          <Button variant='ghost' size='icon' asChild>
            <Link href='/crm/leads'>
              <ArrowLeft className='h-5 w-5' />
            </Link>
          </Button>
          <div>
            <h1 className='text-2xl font-bold text-foreground'>
              {lead.firstName} {lead.lastName}
            </h1>
            <div className='mt-1 flex items-center gap-2 flex-wrap'>
              <Badge
                className={`${statusConfig.bgColor} ${statusConfig.color}`}
              >
                <StatusIcon className='mr-1 h-3 w-3' />
                {statusConfig.label}
              </Badge>
              <Badge variant='outline' className='flex items-center gap-1'>
                <SourceIcon className='h-3 w-3' />
                {sourceConfig.label}
              </Badge>
              <Badge
                className={`${priorityConfig.bgColor} ${priorityConfig.color}`}
              >
                <Flag className='mr-1 h-3 w-3' />
                {priorityConfig.label}
              </Badge>
            </div>
          </div>
        </div>

        <div className='flex items-center gap-2 flex-wrap'>
          {lead.status === 'QUALIFIED' && (
            <Button
              onClick={() => setShowConvertDialog(true)}
              className='bg-green-600 hover:bg-green-700'
            >
              <UserPlus className='mr-2 h-4 w-4' />
              Chuyển đổi
            </Button>
          )}
          <Button variant='outline' onClick={() => setShowStatusDialog(true)}>
            <RefreshCw className='mr-2 h-4 w-4' />
            Cập nhật trạng thái
          </Button>
          <Button variant='outline' asChild>
            <Link href={`/crm/leads/${leadId}/edit`}>
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
                <Mail className='mr-2 h-4 w-4' />
                Gửi email
              </DropdownMenuItem>
              <DropdownMenuItem>
                <Phone className='mr-2 h-4 w-4' />
                Gọi điện
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => setShowActivityDialog(true)}>
                <Activity className='mr-2 h-4 w-4' />
                Thêm hoạt động
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem>
                <Copy className='mr-2 h-4 w-4' />
                Tạo bản sao
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
                Xóa lead
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>

      {/* Lead Progress */}
      <Card className='border-none shadow-sm'>
        <CardContent className='py-4'>
          <div className='flex items-center justify-between mb-2'>
            <span className='text-sm font-medium text-foreground'>
              Tiến trình chuyển đổi
            </span>
            <span className='text-sm text-muted-foreground'>
              {statusConfig.label}
            </span>
          </div>
          <Progress value={statusProgress()} className='h-2' />
          <div className='mt-3 flex justify-between'>
            {['NEW', 'CONTACTED', 'QUALIFIED', 'CONVERTED'].map(
              (status, index) => {
                const config = LEAD_STATUS_CONFIG[status as LeadStatus];
                const Icon = config.icon;
                const isActive = statusConfig.step >= index + 1;
                const isCurrent = statusConfig.step === index + 1;
                return (
                  <div
                    key={status}
                    className={`flex flex-col items-center ${
                      isActive
                        ? 'text-blue-600 dark:text-blue-400'
                        : 'text-muted-foreground'
                    } ${isCurrent ? 'font-semibold' : ''}`}
                  >
                    <div
                      className={`rounded-full p-2 ${
                        isActive
                          ? 'bg-blue-100 dark:bg-blue-900/30'
                          : 'bg-muted'
                      }`}
                    >
                      <Icon className='h-4 w-4' />
                    </div>
                    <span className='mt-1 text-xs hidden sm:block'>
                      {config.label}
                    </span>
                  </div>
                );
              }
            )}
          </div>
        </CardContent>
      </Card>

      {/* Main Content */}
      <div className='grid gap-6 lg:grid-cols-3'>
        {/* Left Column - Main Info */}
        <div className='space-y-6 lg:col-span-2'>
          <Tabs value={activeTab} onValueChange={setActiveTab}>
            <TabsList className='w-full justify-start'>
              <TabsTrigger value='overview'>Tổng quan</TabsTrigger>
              <TabsTrigger value='activities'>
                Hoạt động ({leadActivities.length})
              </TabsTrigger>
              <TabsTrigger value='notes'>
                Ghi chú ({leadNotes.length})
              </TabsTrigger>
            </TabsList>

            <TabsContent value='overview' className='mt-4 space-y-6'>
              {/* Contact Information */}
              <Card className='border-none shadow-sm'>
                <CardHeader className='pb-3'>
                  <CardTitle className='flex items-center text-lg font-semibold'>
                    <User className='mr-2 h-5 w-5 text-muted-foreground' />
                    Thông tin liên hệ
                  </CardTitle>
                </CardHeader>
                <CardContent className='grid gap-4 sm:grid-cols-2'>
                  <div className='flex items-center gap-3'>
                    <div className='rounded-lg bg-blue-100 dark:bg-blue-900/30 p-2'>
                      <Mail className='h-4 w-4 text-blue-600 dark:text-blue-400' />
                    </div>
                    <div>
                      <p className='text-sm text-muted-foreground'>Email</p>
                      <a
                        href={`mailto:${lead.email}`}
                        className='font-medium text-blue-600 hover:underline'
                      >
                        {lead.email}
                      </a>
                    </div>
                  </div>
                  <div className='flex items-center gap-3'>
                    <div className='rounded-lg bg-green-100 dark:bg-green-900/30 p-2'>
                      <Phone className='h-4 w-4 text-green-600 dark:text-green-400' />
                    </div>
                    <div>
                      <p className='text-sm text-muted-foreground'>
                        Điện thoại
                      </p>
                      {lead.phone ? (
                        <a
                          href={`tel:${lead.phone}`}
                          className='font-medium text-foreground hover:text-blue-600 dark:hover:text-blue-400'
                        >
                          {lead.phone}
                        </a>
                      ) : (
                        <p className='text-muted-foreground'>Chưa có</p>
                      )}
                    </div>
                  </div>
                  <div className='flex items-center gap-3'>
                    <div className='rounded-lg bg-purple-100 dark:bg-purple-900/30 p-2'>
                      <Building2 className='h-4 w-4 text-purple-600 dark:text-purple-400' />
                    </div>
                    <div>
                      <p className='text-sm text-muted-foreground'>Công ty</p>
                      <p className='font-medium text-foreground'>
                        {lead.company || 'Chưa có'}
                      </p>
                    </div>
                  </div>
                  <div className='flex items-center gap-3'>
                    <div className='rounded-lg bg-orange-100 dark:bg-orange-900/30 p-2'>
                      <Briefcase className='h-4 w-4 text-orange-600 dark:text-orange-400' />
                    </div>
                    <div>
                      <p className='text-sm text-muted-foreground'>Chức vụ</p>
                      <p className='font-medium text-foreground'>
                        {lead.jobTitle || 'Chưa có'}
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* Lead Details */}
              <Card className='border-none shadow-sm'>
                <CardHeader className='pb-3'>
                  <CardTitle className='flex items-center text-lg font-semibold'>
                    <Target className='mr-2 h-5 w-5 text-muted-foreground' />
                    Chi tiết Lead
                  </CardTitle>
                </CardHeader>
                <CardContent className='grid gap-4 sm:grid-cols-2'>
                  <div className='flex items-center gap-3'>
                    <div className='rounded-lg bg-emerald-100 dark:bg-emerald-900/30 p-2'>
                      <DollarSign className='h-4 w-4 text-emerald-600 dark:text-emerald-400' />
                    </div>
                    <div>
                      <p className='text-sm text-muted-foreground'>
                        Giá trị ước tính
                      </p>
                      <p className='font-medium text-foreground'>
                        {formatCurrency(lead.estimatedValue)}
                      </p>
                    </div>
                  </div>
                  <div className='flex items-center gap-3'>
                    <div className='rounded-lg bg-pink-100 dark:bg-pink-900/30 p-2'>
                      <Calendar className='h-4 w-4 text-pink-600 dark:text-pink-400' />
                    </div>
                    <div>
                      <p className='text-sm text-muted-foreground'>
                        Ngày chốt dự kiến
                      </p>
                      <p className='font-medium text-foreground'>
                        {formatDate(lead.expectedCloseDate)}
                      </p>
                    </div>
                  </div>
                  <div className='flex items-center gap-3'>
                    <div className='rounded-lg bg-cyan-100 dark:bg-cyan-900/30 p-2'>
                      <SourceIcon className='h-4 w-4 text-cyan-600 dark:text-cyan-400' />
                    </div>
                    <div>
                      <p className='text-sm text-muted-foreground'>Nguồn</p>
                      <p className='font-medium text-foreground'>
                        {sourceConfig.label}
                      </p>
                    </div>
                  </div>
                  <div className='flex items-center gap-3'>
                    <div className='rounded-lg bg-amber-100 dark:bg-amber-900/30 p-2'>
                      <Clock className='h-4 w-4 text-amber-600 dark:text-amber-400' />
                    </div>
                    <div>
                      <p className='text-sm text-muted-foreground'>
                        Hoạt động cuối
                      </p>
                      <p className='font-medium text-foreground'>
                        {formatDate(lead.lastActivityDate)}
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* Notes Preview */}
              {lead.notes && (
                <Card className='border-none shadow-sm'>
                  <CardHeader className='pb-3'>
                    <CardTitle className='text-lg font-semibold'>
                      Ghi chú
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className='whitespace-pre-wrap text-foreground/80'>
                      {lead.notes}
                    </p>
                  </CardContent>
                </Card>
              )}

              {/* Tags */}
              {lead.tags.length > 0 && (
                <Card className='border-none shadow-sm'>
                  <CardHeader className='pb-3'>
                    <CardTitle className='flex items-center text-lg font-semibold'>
                      <Tag className='mr-2 h-5 w-5 text-muted-foreground' />
                      Tags
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className='flex flex-wrap gap-2'>
                      {lead.tags.map((tag) => (
                        <Badge key={tag} variant='secondary'>
                          {tag}
                        </Badge>
                      ))}
                    </div>
                  </CardContent>
                </Card>
              )}
            </TabsContent>

            <TabsContent value='activities' className='mt-4 space-y-4'>
              <div className='flex justify-end'>
                <Button asChild>
                  <Link
                    href={`/crm/activities/new?relatedType=LEAD&relatedId=${lead.id}`}
                  >
                    <Activity className='mr-2 h-4 w-4' />
                    Thêm hoạt động
                  </Link>
                </Button>
              </div>

              {leadActivities.length > 0 ? (
                <div className='space-y-3'>
                  {leadActivities.map((activity) => (
                    <Card
                      key={activity.id}
                      className='cursor-pointer border-none shadow-sm transition-shadow hover:shadow-md'
                      onClick={() =>
                        router.push(`/crm/activities/${activity.id}`)
                      }
                    >
                      <CardContent className='flex items-center gap-4 p-4'>
                        <div className='rounded-lg bg-blue-100 dark:bg-blue-900/30 p-2'>
                          <Activity className='h-5 w-5 text-blue-600 dark:text-blue-400' />
                        </div>
                        <div className='flex-1'>
                          <p className='font-medium text-foreground'>
                            {activity.subject}
                          </p>
                          <p className='text-sm text-muted-foreground'>
                            {formatDate(activity.scheduledDate)} •{' '}
                            {activity.type}
                          </p>
                        </div>
                        <Badge variant='outline'>{activity.status}</Badge>
                        <ChevronRight className='h-4 w-4 text-muted-foreground' />
                      </CardContent>
                    </Card>
                  ))}
                </div>
              ) : (
                <Card className='border-none shadow-sm'>
                  <CardContent className='flex flex-col items-center justify-center py-12'>
                    <Activity className='mb-4 h-12 w-12 text-muted-foreground/50' />
                    <p className='mb-4 text-muted-foreground'>
                      Chưa có hoạt động nào
                    </p>
                    <Button asChild>
                      <Link
                        href={`/crm/activities/new?relatedType=LEAD&relatedId=${lead.id}`}
                      >
                        Tạo hoạt động đầu tiên
                      </Link>
                    </Button>
                  </CardContent>
                </Card>
              )}
            </TabsContent>

            <TabsContent value='notes' className='mt-4 space-y-4'>
              <div className='flex justify-end'>
                <Button onClick={() => setShowAddNoteDialog(true)}>
                  <MessageSquare className='mr-2 h-4 w-4' />
                  Thêm ghi chú
                </Button>
              </div>

              {leadNotes.length > 0 ? (
                <div className='space-y-4'>
                  {leadNotes.map((note) => (
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
          </Tabs>
        </div>

        {/* Right Column - Sidebar */}
        <div className='space-y-6'>
          {/* Lead Score */}
          <Card className='border-none shadow-sm'>
            <CardHeader className='pb-3'>
              <CardTitle className='flex items-center text-lg font-semibold'>
                <TrendingUp className='mr-2 h-5 w-5 text-muted-foreground' />
                Điểm Lead
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className='flex items-center justify-center'>
                <div className='relative'>
                  <svg className='h-32 w-32 -rotate-90'>
                    <circle
                      cx='64'
                      cy='64'
                      r='56'
                      fill='none'
                      className='stroke-muted'
                      strokeWidth='12'
                    />
                    <circle
                      cx='64'
                      cy='64'
                      r='56'
                      fill='none'
                      stroke={
                        leadScore >= 75
                          ? '#22c55e'
                          : leadScore >= 50
                            ? '#f59e0b'
                            : '#ef4444'
                      }
                      strokeWidth='12'
                      strokeDasharray={`${(leadScore / 100) * 352} 352`}
                      strokeLinecap='round'
                    />
                  </svg>
                  <div className='absolute inset-0 flex flex-col items-center justify-center'>
                    <span className='text-3xl font-bold text-foreground'>
                      {leadScore}
                    </span>
                    <span className='text-sm text-muted-foreground'>/100</span>
                  </div>
                </div>
              </div>
              <p className='mt-4 text-center text-sm text-muted-foreground'>
                {leadScore >= 75
                  ? 'Lead có tiềm năng cao'
                  : leadScore >= 50
                    ? 'Lead có tiềm năng trung bình'
                    : 'Cần thu thập thêm thông tin'}
              </p>
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
              {lead.assignedTo ? (
                <div className='flex items-center gap-3'>
                  <Avatar className='h-10 w-10'>
                    <AvatarFallback>NV</AvatarFallback>
                  </Avatar>
                  <div>
                    <p className='font-medium text-foreground'>Nguyễn Văn An</p>
                    <p className='text-sm text-muted-foreground'>
                      Sales Representative
                    </p>
                  </div>
                </div>
              ) : (
                <div className='text-center'>
                  <p className='text-muted-foreground'>Chưa phân công</p>
                  <Button variant='outline' size='sm' className='mt-2'>
                    Phân công
                  </Button>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Lead Info */}
          <Card className='border-none shadow-sm'>
            <CardHeader className='pb-3'>
              <CardTitle className='text-lg font-semibold'>Thông tin</CardTitle>
            </CardHeader>
            <CardContent className='space-y-4'>
              <div className='flex items-center justify-between'>
                <span className='text-sm text-muted-foreground'>Mã lead</span>
                <span className='font-mono text-sm text-foreground'>
                  #{lead.id}
                </span>
              </div>
              <Separator />
              <div className='flex items-center justify-between'>
                <span className='text-sm text-muted-foreground'>Ngày tạo</span>
                <span className='text-sm text-foreground'>
                  {new Date(lead.createdAt).toLocaleDateString('vi-VN')}
                </span>
              </div>
              <Separator />
              <div className='flex items-center justify-between'>
                <span className='text-sm text-muted-foreground'>Cập nhật</span>
                <span className='text-sm text-foreground'>
                  {new Date(lead.updatedAt).toLocaleDateString('vi-VN')}
                </span>
              </div>
              {lead.conversionDate && (
                <>
                  <Separator />
                  <div className='flex items-center justify-between'>
                    <span className='text-sm text-muted-foreground'>
                      Ngày chuyển đổi
                    </span>
                    <span className='text-sm text-green-600 font-medium'>
                      {new Date(lead.conversionDate).toLocaleDateString(
                        'vi-VN'
                      )}
                    </span>
                  </div>
                </>
              )}
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
              <Button className='w-full justify-start' variant='outline'>
                <Mail className='mr-2 h-4 w-4 text-blue-600' />
                Gửi email
              </Button>
              <Button className='w-full justify-start' variant='outline'>
                <Phone className='mr-2 h-4 w-4 text-green-600' />
                Gọi điện
              </Button>
              <Button className='w-full justify-start' variant='outline'>
                <Calendar className='mr-2 h-4 w-4 text-purple-600' />
                Lên lịch họp
              </Button>
              <Button className='w-full justify-start' variant='outline'>
                <Activity className='mr-2 h-4 w-4 text-orange-600' />
                Ghi nhận hoạt động
              </Button>
              {lead.status === 'QUALIFIED' && (
                <Button
                  className='w-full justify-start bg-green-600 text-white hover:bg-green-700'
                  onClick={() => setShowConvertDialog(true)}
                >
                  <UserPlus className='mr-2 h-4 w-4' />
                  Chuyển đổi thành khách hàng
                </Button>
              )}
            </CardContent>
          </Card>
        </div>
      </div>

      {/* Delete Confirmation Dialog */}
      <Dialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Xác nhận xóa lead</DialogTitle>
            <DialogDescription>
              Bạn có chắc chắn muốn xóa lead &quot;{lead.firstName}{' '}
              {lead.lastName}&quot;? Hành động này không thể hoàn tác.
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
              Xóa lead
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Status Update Dialog */}
      <Dialog open={showStatusDialog} onOpenChange={setShowStatusDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Cập nhật trạng thái</DialogTitle>
            <DialogDescription>
              Chọn trạng thái mới cho lead này
            </DialogDescription>
          </DialogHeader>
          <div className='py-4'>
            <Select
              value={newStatus}
              onValueChange={(value) => setNewStatus(value as LeadStatus)}
            >
              <SelectTrigger>
                <SelectValue placeholder='Chọn trạng thái' />
              </SelectTrigger>
              <SelectContent>
                {Object.entries(LEAD_STATUS_CONFIG).map(([key, config]) => (
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
              onClick={() => setShowStatusDialog(false)}
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
            <DialogDescription>Thêm ghi chú mới cho lead này</DialogDescription>
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

      {/* Add Activity Dialog */}
      <Dialog open={showActivityDialog} onOpenChange={setShowActivityDialog}>
        <DialogContent className='sm:max-w-md'>
          <DialogHeader>
            <DialogTitle>Thêm hoạt động mới</DialogTitle>
            <DialogDescription>
              Ghi nhận hoạt động cho lead {lead.firstName} {lead.lastName}
            </DialogDescription>
          </DialogHeader>
          <div className='space-y-4 py-4'>
            <div className='space-y-2'>
              <Label>Loại hoạt động</Label>
              <Select
                value={activityForm.type}
                onValueChange={(value) =>
                  setActivityForm({ ...activityForm, type: value })
                }
              >
                <SelectTrigger>
                  <SelectValue placeholder='Chọn loại' />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value='CALL'>
                    <div className='flex items-center gap-2'>
                      <Phone className='h-4 w-4' />
                      Cuộc gọi
                    </div>
                  </SelectItem>
                  <SelectItem value='EMAIL'>
                    <div className='flex items-center gap-2'>
                      <Mail className='h-4 w-4' />
                      Email
                    </div>
                  </SelectItem>
                  <SelectItem value='MEETING'>
                    <div className='flex items-center gap-2'>
                      <Calendar className='h-4 w-4' />
                      Cuộc họn
                    </div>
                  </SelectItem>
                  <SelectItem value='NOTE'>
                    <div className='flex items-center gap-2'>
                      <FileText className='h-4 w-4' />
                      Ghi chú
                    </div>
                  </SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className='space-y-2'>
              <Label>Tiêu đề</Label>
              <Input
                placeholder='Nhập tiêu đề hoạt động...'
                value={activityForm.subject}
                onChange={(e) =>
                  setActivityForm({ ...activityForm, subject: e.target.value })
                }
              />
            </div>
            <div className='space-y-2'>
              <Label>Mô tả</Label>
              <Textarea
                placeholder='Mô tả chi tiết...'
                value={activityForm.description}
                onChange={(e) =>
                  setActivityForm({
                    ...activityForm,
                    description: e.target.value,
                  })
                }
                rows={3}
              />
            </div>
            <div className='space-y-2'>
              <Label>Ngày thực hiện</Label>
              <Input
                type='datetime-local'
                value={activityForm.scheduledDate}
                onChange={(e) =>
                  setActivityForm({
                    ...activityForm,
                    scheduledDate: e.target.value,
                  })
                }
              />
            </div>
          </div>
          <DialogFooter>
            <Button
              variant='outline'
              onClick={() => setShowActivityDialog(false)}
            >
              Hủy
            </Button>
            <Button
              onClick={() => {
                console.log('Adding activity:', activityForm);
                setShowActivityDialog(false);
                setActivityForm({
                  type: 'CALL',
                  subject: '',
                  description: '',
                  scheduledDate: '',
                });
              }}
              disabled={!activityForm.subject.trim()}
            >
              Thêm hoạt động
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Convert to Customer Dialog */}
      <Dialog open={showConvertDialog} onOpenChange={setShowConvertDialog}>
        <DialogContent className='sm:max-w-lg'>
          <DialogHeader>
            <DialogTitle>Chuyển đổi Lead thành Khách hàng</DialogTitle>
            <DialogDescription>
              Tạo khách hàng mới từ thông tin lead này
            </DialogDescription>
          </DialogHeader>
          <div className='space-y-4 py-4'>
            <div className='rounded-lg bg-muted p-4'>
              <div className='flex items-center gap-3 mb-3'>
                <Avatar>
                  <AvatarFallback>
                    {lead.firstName[0]}
                    {lead.lastName[0]}
                  </AvatarFallback>
                </Avatar>
                <div>
                  <p className='font-medium'>
                    {lead.firstName} {lead.lastName}
                  </p>
                  <p className='text-sm text-muted-foreground'>
                    {lead.company || lead.email}
                  </p>
                </div>
              </div>
              <div className='flex gap-2'>
                <Badge variant='outline'>{sourceConfig.label}</Badge>
                <Badge
                  className={`${priorityConfig.bgColor} ${priorityConfig.color}`}
                >
                  {priorityConfig.label}
                </Badge>
              </div>
            </div>

            <div className='space-y-3'>
              <div>
                <Label>Loại khách hàng</Label>
                <Select
                  value={convertForm.customerType}
                  onValueChange={(value) =>
                    setConvertForm({ ...convertForm, customerType: value })
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value='INDIVIDUAL'>Cá nhân</SelectItem>
                    <SelectItem value='COMPANY'>Doanh nghiệp</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className='flex items-center gap-2'>
                <input
                  type='checkbox'
                  id='createOpportunity'
                  checked={convertForm.createOpportunity}
                  onChange={(e) =>
                    setConvertForm({
                      ...convertForm,
                      createOpportunity: e.target.checked,
                    })
                  }
                  className='rounded border-gray-300'
                />
                <Label htmlFor='createOpportunity'>Tạo cơ hội kinh doanh</Label>
              </div>

              {convertForm.createOpportunity && (
                <div className='space-y-3 rounded-lg border p-4'>
                  <div>
                    <Label>Tên cơ hội</Label>
                    <Input
                      value={convertForm.opportunityName}
                      onChange={(e) =>
                        setConvertForm({
                          ...convertForm,
                          opportunityName: e.target.value,
                        })
                      }
                      placeholder={`Cơ hội từ ${lead.firstName} ${lead.lastName}`}
                    />
                  </div>
                  <div>
                    <Label>Giá trị ước tính</Label>
                    <Input
                      type='number'
                      value={convertForm.opportunityValue}
                      onChange={(e) =>
                        setConvertForm({
                          ...convertForm,
                          opportunityValue: e.target.value,
                        })
                      }
                      placeholder={lead.estimatedValue?.toString() || '0'}
                    />
                  </div>
                </div>
              )}
            </div>
          </div>
          <DialogFooter>
            <Button
              variant='outline'
              onClick={() => setShowConvertDialog(false)}
            >
              Hủy
            </Button>
            <Button
              onClick={handleConvert}
              className='bg-green-600 hover:bg-green-700'
            >
              <UserPlus className='mr-2 h-4 w-4' />
              Chuyển đổi
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

export default LeadDetailPage;
