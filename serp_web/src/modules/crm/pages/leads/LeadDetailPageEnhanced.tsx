/**
 * Lead Detail Page Component - Enhanced Version
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Detailed lead view with conversion flow
 */

'use client';

import { useMemo, useState } from 'react';
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
  FileText,
  ChevronRight,
  RefreshCw,
} from 'lucide-react';
import { getErrorMessage } from '@/lib/store/api';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Button,
  Badge,
  Tabs,
  TabsList,
  TabsTrigger,
  TabsContent,
  Textarea,
  Input,
  Label,
  Progress,
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
  Avatar,
  AvatarFallback,
  Separator,
} from '@/shared/components/ui';
import { toast } from 'sonner';
import { cn } from '@/shared/utils';
import {
  useConvertLeadMutation,
  useDeleteLeadMutation,
  useDisqualifyLeadMutation,
  useGetLeadActivitiesQuery,
  useGetLeadQuery,
  useQualifyLeadMutation,
} from '../../api/crmApi';
import type { LeadSource, LeadStatus } from '../../types';

interface LeadDetailPageProps {
  leadId: string;
}

const LEAD_STATUS_CONFIG: Record<
  Exclude<LeadStatus, 'LOST'>,
  {
    label: string;
    color: string;
    bgColor: string;
    icon: React.ElementType;
    step: number;
  }
> = {
  NEW: {
    label: 'New',
    color: 'text-blue-700',
    bgColor: 'bg-blue-100',
    icon: RefreshCw,
    step: 1,
  },
  CONTACTED: {
    label: 'Contacted',
    color: 'text-purple-700',
    bgColor: 'bg-purple-100',
    icon: Phone,
    step: 2,
  },
  NURTURING: {
    label: 'Nurturing',
    color: 'text-indigo-700',
    bgColor: 'bg-indigo-100',
    icon: Clock,
    step: 2,
  },
  QUALIFIED: {
    label: 'Qualified',
    color: 'text-green-700',
    bgColor: 'bg-green-100',
    icon: CheckCircle,
    step: 3,
  },
  DISQUALIFIED: {
    label: 'Disqualified',
    color: 'text-red-700',
    bgColor: 'bg-red-100',
    icon: AlertCircle,
    step: 0,
  },
  CONVERTED: {
    label: 'Converted',
    color: 'text-emerald-700',
    bgColor: 'bg-emerald-100',
    icon: UserPlus,
    step: 4,
  },
};

const LEAD_SOURCE_CONFIG: Record<LeadSource, { label: string; icon: React.ElementType }> = {
  WEBSITE: { label: 'Website', icon: Globe },
  SOCIAL_MEDIA: { label: 'Social Media', icon: MessageSquare },
  REFERRAL: { label: 'Referral', icon: Users },
  COLD_CALL: { label: 'Cold Call', icon: Phone },
  EMAIL_CAMPAIGN: { label: 'Email Campaign', icon: Mail },
  EMAIL: { label: 'Email', icon: Mail },
  PHONE: { label: 'Phone', icon: Phone },
  TRADE_SHOW: { label: 'Trade Show', icon: Building2 },
  OTHER: { label: 'Other', icon: FileText },
};

export function LeadDetailPage({ leadId }: LeadDetailPageProps) {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState('overview');
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [showConvertDialog, setShowConvertDialog] = useState(false);
  const [showQualifyDialog, setShowQualifyDialog] = useState(false);
  const [showDisqualifyDialog, setShowDisqualifyDialog] = useState(false);
  const [qualifyNotes, setQualifyNotes] = useState('');
  const [disqualifyNotes, setDisqualifyNotes] = useState('');
  const [convertForm, setConvertForm] = useState({
    createAccount: true,
    createOpportunity: true,
    existingAccountId: '',
    accountName: '',
    accountNotes: '',
    opportunityName: '',
    opportunityAmount: '',
    opportunityNotes: '',
  });

  const { data, isLoading } = useGetLeadQuery(leadId);
  const { data: activitiesData, isLoading: isActivitiesLoading } =
    useGetLeadActivitiesQuery({ leadId, page: 1, size: 20 });
  const [deleteLead] = useDeleteLeadMutation();
  const [qualifyLead] = useQualifyLeadMutation();
  const [disqualifyLead] = useDisqualifyLeadMutation();
  const [convertLead] = useConvertLeadMutation();

  const lead = data?.data;
  const activities = activitiesData?.data.data || [];

  const leadStatus = (lead?.leadStatus || 'NEW') as Exclude<LeadStatus, 'LOST'>;
  const leadSource = (lead?.leadSource || 'WEBSITE') as LeadSource;
  const statusConfig = LEAD_STATUS_CONFIG[leadStatus];
  const sourceConfig = LEAD_SOURCE_CONFIG[leadSource];
  const StatusIcon = statusConfig.icon;
  const SourceIcon = sourceConfig.icon;

  const leadScore = useMemo(() => {
    if (typeof lead?.leadScore === 'number') {
      return lead.leadScore;
    }

    let score = 0;
    if (lead?.email) score += 20;
    if (lead?.phone) score += 15;
    if (lead?.company) score += 20;
    if (lead?.estimatedValue && lead.estimatedValue > 0) score += 20;
    if (leadStatus === 'QUALIFIED') score += 25;
    else if (leadStatus === 'CONTACTED' || leadStatus === 'NURTURING') score += 15;
    else if (leadStatus === 'NEW') score += 5;
    return Math.min(score, 100);
  }, [lead?.company, lead?.email, lead?.estimatedValue, lead?.leadScore, lead?.phone, leadStatus]);

  if (!isLoading && !lead) {
    return (
      <div className='flex h-[60vh] flex-col items-center justify-center'>
        <AlertCircle className='mb-4 h-16 w-16 text-muted-foreground' />
        <h2 className='mb-2 text-xl font-semibold text-foreground'>
          Lead not found
        </h2>
        <p className='mb-4 text-muted-foreground'>This lead does not exist or has been deleted.</p>
        <Button asChild>
          <Link href='/crm/leads'>
            <ArrowLeft className='mr-2 h-4 w-4' />
            Back to lead list
          </Link>
        </Button>
      </div>
    );
  }

  const formatDate = (dateString?: string) => {
    if (!dateString) return 'Not available';
    return new Date(dateString).toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  const formatCurrency = (value?: number) => {
    if (!value) return 'Not available';
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      maximumFractionDigits: 0,
    }).format(value);
  };

  const statusProgress = () => {
    if (leadStatus === 'DISQUALIFIED') return 0;
    return (statusConfig.step / 4) * 100;
  };

  const handleDelete = async () => {
    try {
      await deleteLead(leadId).unwrap();
      toast.success('Delete lead successfully');
      router.push('/crm/leads');
    } catch (error) {
      toast.error('Failed to delete lead', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleQualify = async () => {
    try {
      await qualifyLead({ id: leadId, data: { notes: qualifyNotes } }).unwrap();
      toast.success('Qualify lead successfully');
      setShowQualifyDialog(false);
      setQualifyNotes('');
    } catch (error) {
      toast.error('Failed to qualify lead', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleDisqualify = async () => {
    try {
      await disqualifyLead({ id: leadId, data: { notes: disqualifyNotes } }).unwrap();
      toast.success('Disqualify lead successfully');
      setShowDisqualifyDialog(false);
      setDisqualifyNotes('');
    } catch (error) {
      toast.error('Failed to disqualify lead', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleConvert = async () => {
    try {
      const result = await convertLead({
        id: leadId,
        data: {
          createAccount: convertForm.createAccount,
          createOpportunity: convertForm.createOpportunity,
          existingAccountId: convertForm.existingAccountId
            ? Number(convertForm.existingAccountId)
            : undefined,
          accountData: convertForm.createAccount
            ? {
                name: convertForm.accountName || lead?.company || lead?.name,
                notes: convertForm.accountNotes || lead?.notes,
              }
            : undefined,
          opportunityData: convertForm.createOpportunity
            ? {
                name:
                  convertForm.opportunityName || `Opportunity from ${lead?.name}`,
                amount: convertForm.opportunityAmount
                  ? Number(convertForm.opportunityAmount)
                  : lead?.estimatedValue,
                notes: convertForm.opportunityNotes || lead?.notes,
              }
            : undefined,
        },
      }).unwrap();
      toast.success(result.data.message || 'Convert lead successfully');
      setShowConvertDialog(false);
      if (result.data.accountId) {
        router.push(`/crm/accounts/${result.data.accountId}`);
      }
    } catch (error) {
      toast.error('Failed to convert lead', {
        description: getErrorMessage(error),
      });
    }
  };

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between'>
        <div className='flex items-center gap-4'>
          <Button variant='ghost' size='icon' asChild>
            <Link href='/crm/leads'>
              <ArrowLeft className='h-5 w-5' />
            </Link>
          </Button>
          <div>
            <h1 className='text-2xl font-bold text-foreground'>{lead?.name}</h1>
            <div className='mt-1 flex flex-wrap items-center gap-2'>
              <Badge className={`${statusConfig.bgColor} ${statusConfig.color}`}>
                <StatusIcon className='mr-1 h-3 w-3' />
                {statusConfig.label}
              </Badge>
              <Badge variant='outline' className='flex items-center gap-1'>
                <SourceIcon className='h-3 w-3' />
                {sourceConfig.label}
              </Badge>
            </div>
          </div>
        </div>

        <div className='flex flex-wrap items-center gap-2'>
          {leadStatus === 'QUALIFIED' && (
            <Button
              onClick={() => setShowConvertDialog(true)}
              className='bg-green-600 hover:bg-green-700'
            >
              <UserPlus className='mr-2 h-4 w-4' />
              Convert
            </Button>
          )}
          <Button variant='outline' onClick={() => setShowQualifyDialog(true)}>
            <CheckCircle className='mr-2 h-4 w-4' />
            Qualify
          </Button>
          <Button variant='outline' onClick={() => setShowDisqualifyDialog(true)}>
            <AlertCircle className='mr-2 h-4 w-4' />
            Disqualify
          </Button>
          <Button variant='outline' asChild>
            <Link href={`/crm/leads/${leadId}/edit`}>
              <Edit className='mr-2 h-4 w-4' />
              Edit
            </Link>
          </Button>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant='outline' size='icon'>
                <MoreHorizontal className='h-4 w-4' />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align='end'>
              <DropdownMenuItem disabled>
                <Mail className='mr-2 h-4 w-4' />
                Send Email
              </DropdownMenuItem>
              <DropdownMenuItem disabled>
                <Phone className='mr-2 h-4 w-4' />
                Make Call
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem disabled>
                <Copy className='mr-2 h-4 w-4' />
                Duplicate
              </DropdownMenuItem>
              <DropdownMenuItem disabled>
                <ExternalLink className='mr-2 h-4 w-4' />
                Export PDF
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem
                className='text-red-600'
                onClick={() => setShowDeleteDialog(true)}
              >
                <Trash2 className='mr-2 h-4 w-4' />
                Delete Lead
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>

      <Card className='border-none shadow-sm'>
        <CardContent className='py-4'>
          <div className='mb-2 flex items-center justify-between'>
            <span className='text-sm font-medium text-foreground'>
              Conversion Progress
            </span>
            <span className='text-sm text-muted-foreground'>
              {statusConfig.label}
            </span>
          </div>
          <Progress value={statusProgress()} className='h-2' />
          <div className='mt-3 flex justify-between'>
            {['NEW', 'CONTACTED', 'QUALIFIED', 'CONVERTED'].map((status, index) => {
              const config = LEAD_STATUS_CONFIG[status as Exclude<LeadStatus, 'LOST'>];
              const Icon = config.icon;
              const isActive = statusConfig.step >= index + 1;
              const isCurrent = statusConfig.step === index + 1;
              return (
                <div
                  key={status}
                  className={cn(
                    'flex flex-col items-center',
                    isActive ? 'text-blue-600 dark:text-blue-400' : 'text-muted-foreground',
                    isCurrent && 'font-semibold'
                  )}
                >
                  <div className={cn('rounded-full p-2', isActive ? 'bg-blue-100 dark:bg-blue-900/30' : 'bg-muted')}>
                    <Icon className='h-4 w-4' />
                  </div>
                  <span className='mt-1 hidden text-xs sm:block'>{config.label}</span>
                </div>
              );
            })}
          </div>
        </CardContent>
      </Card>

      <div className='grid gap-6 lg:grid-cols-3'>
        <div className='space-y-6 lg:col-span-2'>
          <Tabs value={activeTab} onValueChange={setActiveTab}>
            <TabsList className='w-full justify-start'>
              <TabsTrigger value='overview'>Overview</TabsTrigger>
              <TabsTrigger value='activities'>Activities ({activities.length})</TabsTrigger>
              <TabsTrigger value='notes'>Notes</TabsTrigger>
            </TabsList>

            <TabsContent value='overview' className='mt-4 space-y-6'>
              <Card className='border-none shadow-sm'>
                <CardHeader className='pb-3'>
                  <CardTitle className='flex items-center text-lg font-semibold'>
                    <User className='mr-2 h-5 w-5 text-muted-foreground' />
                    Contact Information
                  </CardTitle>
                </CardHeader>
                <CardContent className='grid gap-4 sm:grid-cols-2'>
                  <div className='flex items-center gap-3'>
                    <div className='rounded-lg bg-blue-100 p-2'>
                      <Mail className='h-4 w-4 text-blue-600' />
                    </div>
                    <div>
                      <p className='text-sm text-muted-foreground'>Email</p>
                      <p className='font-medium text-foreground'>
                        {lead?.email || 'Not available'}
                      </p>
                    </div>
                  </div>
                  <div className='flex items-center gap-3'>
                    <div className='rounded-lg bg-green-100 p-2'>
                      <Phone className='h-4 w-4 text-green-600' />
                    </div>
                    <div>
                      <p className='text-sm text-muted-foreground'>Phone</p>
                      <p className='font-medium text-foreground'>
                        {lead?.phone || 'Not available'}
                      </p>
                    </div>
                  </div>
                  <div className='flex items-center gap-3'>
                    <div className='rounded-lg bg-purple-100 p-2'>
                      <Building2 className='h-4 w-4 text-purple-600' />
                    </div>
                    <div>
                      <p className='text-sm text-muted-foreground'>Company</p>
                      <p className='font-medium text-foreground'>
                        {lead?.company || 'Not available'}
                      </p>
                    </div>
                  </div>
                  <div className='flex items-center gap-3'>
                    <div className='rounded-lg bg-orange-100 p-2'>
                      <Briefcase className='h-4 w-4 text-orange-600' />
                    </div>
                    <div>
                      <p className='text-sm text-muted-foreground'>Job Title</p>
                      <p className='font-medium text-foreground'>
                        {lead?.jobTitle || 'Not available'}
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card className='border-none shadow-sm'>
                <CardHeader className='pb-3'>
                  <CardTitle className='flex items-center text-lg font-semibold'>
                    <Target className='mr-2 h-5 w-5 text-muted-foreground' />
                    Lead Details
                  </CardTitle>
                </CardHeader>
                <CardContent className='grid gap-4 sm:grid-cols-2'>
                  <div className='flex items-center gap-3'>
                    <div className='rounded-lg bg-emerald-100 p-2'>
                      <DollarSign className='h-4 w-4 text-emerald-600' />
                    </div>
                    <div>
                      <p className='text-sm text-muted-foreground'>Estimated Value</p>
                      <p className='font-medium text-foreground'>
                        {formatCurrency(lead?.estimatedValue)}
                      </p>
                    </div>
                  </div>
                  <div className='flex items-center gap-3'>
                    <div className='rounded-lg bg-pink-100 p-2'>
                      <Calendar className='h-4 w-4 text-pink-600' />
                    </div>
                    <div>
                      <p className='text-sm text-muted-foreground'>Follow Up Date</p>
                      <p className='font-medium text-foreground'>
                        {formatDate(lead?.followUpDate)}
                      </p>
                    </div>
                  </div>
                  <div className='flex items-center gap-3'>
                    <div className='rounded-lg bg-cyan-100 p-2'>
                      <SourceIcon className='h-4 w-4 text-cyan-600' />
                    </div>
                    <div>
                      <p className='text-sm text-muted-foreground'>Source</p>
                      <p className='font-medium text-foreground'>{sourceConfig.label}</p>
                    </div>
                  </div>
                  <div className='flex items-center gap-3'>
                    <div className='rounded-lg bg-amber-100 p-2'>
                      <Clock className='h-4 w-4 text-amber-600' />
                    </div>
                    <div>
                      <p className='text-sm text-muted-foreground'>Updated</p>
                      <p className='font-medium text-foreground'>
                        {formatDate(lead?.updatedAt)}
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card className='border-none shadow-sm'>
                <CardHeader className='pb-3'>
                  <CardTitle className='text-lg font-semibold'>Primary Notes</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className='whitespace-pre-wrap text-foreground/80'>
                    {lead?.notes || 'No notes available.'}
                  </p>
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value='activities' className='mt-4 space-y-4'>
              {isActivitiesLoading ? (
                <Card className='border-none shadow-sm'>
                  <CardContent className='py-12 text-center text-muted-foreground'>
                    Loading activities...
                  </CardContent>
                </Card>
              ) : activities.length > 0 ? (
                <div className='space-y-3'>
                  {activities.map((activity) => (
                    <Card
                      key={activity.id}
                      className='cursor-pointer border-none shadow-sm transition-shadow hover:shadow-md'
                      onClick={() => router.push(`/crm/activities/${activity.id}`)}
                    >
                      <CardContent className='flex items-center gap-4 p-4'>
                        <div className='rounded-lg bg-blue-100 p-2'>
                          <Activity className='h-5 w-5 text-blue-600' />
                        </div>
                        <div className='flex-1'>
                          <p className='font-medium text-foreground'>{activity.subject}</p>
                          <p className='text-sm text-muted-foreground'>
                            {formatDate(activity.scheduledDate)} • {activity.type}
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
                    <p className='text-muted-foreground'>No activities available.</p>
                  </CardContent>
                </Card>
              )}
            </TabsContent>

            <TabsContent value='notes' className='mt-4 space-y-4'>
              <Card className='border-none shadow-sm'>
                <CardContent className='flex flex-col items-center justify-center py-12'>
                  <MessageSquare className='mb-4 h-12 w-12 text-muted-foreground/50' />
                  <p className='text-muted-foreground'>
                    Dedicated lead notes API is not integrated yet.
                  </p>
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>
        </div>

        <div className='space-y-6'>
          <Card className='border-none shadow-sm'>
            <CardHeader className='pb-3'>
              <CardTitle className='flex items-center text-lg font-semibold'>
                <TrendingUp className='mr-2 h-5 w-5 text-muted-foreground' />
                Lead Score
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className='flex items-center justify-center'>
                <div className='relative'>
                  <svg className='h-32 w-32 -rotate-90'>
                    <circle cx='64' cy='64' r='56' fill='none' className='stroke-muted' strokeWidth='12' />
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
            </CardContent>
          </Card>

          <Card className='border-none shadow-sm'>
            <CardHeader className='pb-3'>
              <CardTitle className='text-lg font-semibold'>Assigned To</CardTitle>
            </CardHeader>
            <CardContent>
              {lead?.assignedTo ? (
                <div className='flex items-center gap-3'>
                  <Avatar className='h-10 w-10'>
                    <AvatarFallback>U</AvatarFallback>
                  </Avatar>
                  <div>
                    <p className='font-medium text-foreground'>User #{lead.assignedTo}</p>
                    <p className='text-sm text-muted-foreground'>Assigned user</p>
                  </div>
                </div>
              ) : (
                <div className='text-center'>
                  <p className='text-muted-foreground'>Not assigned</p>
                </div>
              )}
            </CardContent>
          </Card>

          <Card className='border-none shadow-sm'>
            <CardHeader className='pb-3'>
              <CardTitle className='text-lg font-semibold'>Information</CardTitle>
            </CardHeader>
            <CardContent className='space-y-4'>
              <div className='flex items-center justify-between'>
                <span className='text-sm text-muted-foreground'>Lead ID</span>
                <span className='font-mono text-sm text-foreground'>#{lead?.id}</span>
              </div>
              <Separator />
              <div className='flex items-center justify-between'>
                <span className='text-sm text-muted-foreground'>Created Date</span>
                <span className='text-sm text-foreground'>{formatDate(lead?.createdAt)}</span>
              </div>
              <Separator />
              <div className='flex items-center justify-between'>
                <span className='text-sm text-muted-foreground'>Updated</span>
                <span className='text-sm text-foreground'>{formatDate(lead?.updatedAt)}</span>
              </div>
            </CardContent>
          </Card>

          <Card className='border-none shadow-sm'>
            <CardHeader className='pb-3'>
              <CardTitle className='text-lg font-semibold'>Quick Actions</CardTitle>
            </CardHeader>
            <CardContent className='space-y-2'>
              <Button className='w-full justify-start' variant='outline' disabled>
                <Mail className='mr-2 h-4 w-4 text-blue-600' />
                Send Email
              </Button>
              <Button className='w-full justify-start' variant='outline' disabled>
                <Phone className='mr-2 h-4 w-4 text-green-600' />
                Make Call
              </Button>
              <Button className='w-full justify-start' variant='outline' disabled>
                <Calendar className='mr-2 h-4 w-4 text-purple-600' />
                Schedule Meeting
              </Button>
              <Button className='w-full justify-start' variant='outline' disabled>
                <Activity className='mr-2 h-4 w-4 text-orange-600' />
                Log Activity
              </Button>
              {leadStatus === 'QUALIFIED' && (
                <Button
                  className='w-full justify-start bg-green-600 text-white hover:bg-green-700'
                  onClick={() => setShowConvertDialog(true)}
                >
                  <UserPlus className='mr-2 h-4 w-4' />
                  Convert to Account
                </Button>
              )}
            </CardContent>
          </Card>
        </div>
      </div>

      <Dialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Confirm Lead Deletion</DialogTitle>
            <DialogDescription>
              Are you sure you want to delete lead &quot;{lead?.name}&quot;? This action cannot be undone.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant='outline' onClick={() => setShowDeleteDialog(false)}>
              Cancel
            </Button>
            <Button variant='destructive' onClick={handleDelete}>
              Delete lead
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={showQualifyDialog} onOpenChange={setShowQualifyDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Qualify lead</DialogTitle>
            <DialogDescription>Add qualification notes for this lead.</DialogDescription>
          </DialogHeader>
          <Textarea rows={4} value={qualifyNotes} onChange={(e) => setQualifyNotes(e.target.value)} />
          <DialogFooter>
            <Button variant='outline' onClick={() => setShowQualifyDialog(false)}>
              Cancel
            </Button>
            <Button onClick={handleQualify} disabled={!qualifyNotes.trim()}>
              Qualify
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={showDisqualifyDialog} onOpenChange={setShowDisqualifyDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Disqualify lead</DialogTitle>
            <DialogDescription>Add disqualification notes for this lead.</DialogDescription>
          </DialogHeader>
          <Textarea rows={4} value={disqualifyNotes} onChange={(e) => setDisqualifyNotes(e.target.value)} />
          <DialogFooter>
            <Button variant='outline' onClick={() => setShowDisqualifyDialog(false)}>
              Cancel
            </Button>
            <Button onClick={handleDisqualify} disabled={!disqualifyNotes.trim()}>
              Disqualify
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={showConvertDialog} onOpenChange={setShowConvertDialog}>
        <DialogContent className='w-[95vw] max-w-2xl'>
          <DialogHeader>
            <DialogTitle>Convert lead</DialogTitle>
            <DialogDescription>Create account/opportunity from this lead.</DialogDescription>
          </DialogHeader>
          <div className='space-y-4'>
            <div className='grid grid-cols-1 gap-4 md:grid-cols-2'>
              <div className='space-y-2'>
                <Label htmlFor='accountName'>Account Name</Label>
                <Input
                  id='accountName'
                  value={convertForm.accountName}
                  onChange={(e) =>
                    setConvertForm((prev) => ({ ...prev, accountName: e.target.value }))
                  }
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='existingAccountId'>Existing Account ID</Label>
                <Input
                  id='existingAccountId'
                  value={convertForm.existingAccountId}
                  onChange={(e) =>
                    setConvertForm((prev) => ({ ...prev, existingAccountId: e.target.value }))
                  }
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='opportunityName'>Opportunity Name</Label>
                <Input
                  id='opportunityName'
                  value={convertForm.opportunityName}
                  onChange={(e) =>
                    setConvertForm((prev) => ({ ...prev, opportunityName: e.target.value }))
                  }
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='opportunityAmount'>Opportunity Amount</Label>
                <Input
                  id='opportunityAmount'
                  type='number'
                  value={convertForm.opportunityAmount}
                  onChange={(e) =>
                    setConvertForm((prev) => ({ ...prev, opportunityAmount: e.target.value }))
                  }
                />
              </div>
            </div>
            <div className='space-y-2'>
              <Label htmlFor='accountNotes'>Account Notes</Label>
              <Textarea
                id='accountNotes'
                rows={3}
                value={convertForm.accountNotes}
                onChange={(e) =>
                  setConvertForm((prev) => ({ ...prev, accountNotes: e.target.value }))
                }
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='opportunityNotes'>Opportunity Notes</Label>
              <Textarea
                id='opportunityNotes'
                rows={3}
                value={convertForm.opportunityNotes}
                onChange={(e) =>
                  setConvertForm((prev) => ({ ...prev, opportunityNotes: e.target.value }))
                }
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant='outline' onClick={() => setShowConvertDialog(false)}>
              Cancel
            </Button>
            <Button onClick={handleConvert}>Convert</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

export default LeadDetailPage;
