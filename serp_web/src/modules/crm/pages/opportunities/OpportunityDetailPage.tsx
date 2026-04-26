/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

'use client';

import { useMemo, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import {
  ArrowLeft,
  MoreHorizontal,
  Edit,
  Trash2,
  Calendar,
  DollarSign,
  TrendingUp,
  User,
  Building2,
  Clock,
  Target,
  AlertCircle,
  CheckCircle2,
  XCircle,
  MessageSquare,
  FileText,
  Trophy,
  Percent,
  Package,
  History,
  ExternalLink,
  RefreshCw,
} from 'lucide-react';
import { getErrorMessage } from '@/lib/store/api';
import {
  Avatar,
  AvatarFallback,
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
  Input,
  Label,
  Progress,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
  Textarea,
} from '@/shared/components/ui';
import { toast } from 'sonner';
import { cn } from '@/shared/utils';
import {
  useChangeOpportunityStageMutation,
  useCloseOpportunityLostMutation,
  useCloseOpportunityWonMutation,
  useDeleteOpportunityMutation,
  useGetOpportunityActivitiesQuery,
  useGetOpportunityQuery,
  useReopenOpportunityMutation,
} from '../../api/crmApi';
import type { OpportunityStage } from '../../types';

const PIPELINE_STAGES: {
  stage: OpportunityStage;
  label: string;
  color: string;
  bgColor: string;
  probability: number;
}[] = [
  {
    stage: 'PROSPECTING',
    label: 'Prospecting',
    color: 'text-blue-700 dark:text-blue-300',
    bgColor: 'bg-blue-100 dark:bg-blue-900/50',
    probability: 10,
  },
  {
    stage: 'QUALIFICATION',
    label: 'Qualification',
    color: 'text-cyan-700 dark:text-cyan-300',
    bgColor: 'bg-cyan-100 dark:bg-cyan-900/50',
    probability: 25,
  },
  {
    stage: 'PROPOSAL',
    label: 'Proposal',
    color: 'text-yellow-700 dark:text-yellow-300',
    bgColor: 'bg-yellow-100 dark:bg-yellow-900/50',
    probability: 50,
  },
  {
    stage: 'NEGOTIATION',
    label: 'Negotiation',
    color: 'text-orange-700 dark:text-orange-300',
    bgColor: 'bg-orange-100 dark:bg-orange-900/50',
    probability: 75,
  },
  {
    stage: 'CLOSED_WON',
    label: 'Closed Won',
    color: 'text-green-700 dark:text-green-300',
    bgColor: 'bg-green-100 dark:bg-green-900/50',
    probability: 100,
  },
  {
    stage: 'CLOSED_LOST',
    label: 'Closed Lost',
    color: 'text-red-700 dark:text-red-300',
    bgColor: 'bg-red-100 dark:bg-red-900/50',
    probability: 0,
  },
];

const OPPORTUNITY_TYPES = {
  NEW_BUSINESS: {
    label: 'New Business',
    color:
      'bg-purple-100 dark:bg-purple-900/50 text-purple-700 dark:text-purple-300',
  },
  EXISTING_BUSINESS: {
    label: 'Existing Business',
    color: 'bg-blue-100 dark:bg-blue-900/50 text-blue-700 dark:text-blue-300',
  },
  RENEWAL: {
    label: 'Renewal',
    color:
      'bg-green-100 dark:bg-green-900/50 text-green-700 dark:text-green-300',
  },
};

interface OpportunityDetailPageProps {
  opportunityId: string;
  className?: string;
}

export const OpportunityDetailPage: React.FC<OpportunityDetailPageProps> = ({
  opportunityId,
  className,
}) => {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState('overview');
  const [isStageDialogOpen, setIsStageDialogOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [isLostDialogOpen, setIsLostDialogOpen] = useState(false);
  const [isWonDialogOpen, setIsWonDialogOpen] = useState(false);
  const [isReopenDialogOpen, setIsReopenDialogOpen] = useState(false);
  const [selectedStage, setSelectedStage] = useState<OpportunityStage | ''>('');
  const [stageNotes, setStageNotes] = useState('');
  const [lostReason, setLostReason] = useState('');
  const [wonActualValue, setWonActualValue] = useState('');
  const [wonNotes, setWonNotes] = useState('');
  const [reopenReason, setReopenReason] = useState('');
  const [reopenStage, setReopenStage] = useState<OpportunityStage>('PROSPECTING');

  const { data, isLoading } = useGetOpportunityQuery(opportunityId);
  const { data: activitiesData, isLoading: isActivitiesLoading } =
    useGetOpportunityActivitiesQuery({ opportunityId, page: 1, size: 20 });
  const [changeOpportunityStage] = useChangeOpportunityStageMutation();
  const [closeOpportunityWon] = useCloseOpportunityWonMutation();
  const [closeOpportunityLost] = useCloseOpportunityLostMutation();
  const [reopenOpportunity] = useReopenOpportunityMutation();
  const [deleteOpportunity] = useDeleteOpportunityMutation();

  const opportunity = data?.data;
  const activities = activitiesData?.data?.data || [];

  const stageConfig = PIPELINE_STAGES.find(
    (stage) => stage.stage === opportunity?.stage
  );
  const typeConfig =
    OPPORTUNITY_TYPES[opportunity?.type || 'NEW_BUSINESS'];
  const currentStageIndex = PIPELINE_STAGES.findIndex(
    (stage) => stage.stage === opportunity?.stage
  );
  const isClosed =
    opportunity?.stage === 'CLOSED_WON' || opportunity?.stage === 'CLOSED_LOST';
  const probability = opportunity?.probability ?? stageConfig?.probability ?? 0;
  const estimatedValue = opportunity?.estimatedValue ?? opportunity?.value ?? 0;
  const weightedValue = (estimatedValue * probability) / 100;

  const daysInPipeline = useMemo(() => {
    if (!opportunity?.createdAt) return 0;
    return Math.floor(
      (new Date().getTime() - new Date(opportunity.createdAt).getTime()) /
        (1000 * 60 * 60 * 24)
    );
  }, [opportunity?.createdAt]);

  const daysUntilClose = useMemo(() => {
    if (!opportunity?.expectedCloseDate) return 0;
    return Math.floor(
      (new Date(opportunity.expectedCloseDate).getTime() - new Date().getTime()) /
        (1000 * 60 * 60 * 24)
    );
  }, [opportunity?.expectedCloseDate]);

  const formatCurrency = (value?: number) => {
    if (value === undefined) return 'Not available';
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(value);
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return 'Not available';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  const formatDateTime = (dateString?: string) => {
    if (!dateString) return 'Not available';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getActivityBadgeClass = (status?: string) => {
    switch (status) {
      case 'COMPLETED':
        return 'bg-green-100 text-green-700';
      case 'IN_PROGRESS':
        return 'bg-yellow-100 text-yellow-700';
      case 'CANCELLED':
        return 'bg-red-100 text-red-700';
      default:
        return 'bg-blue-100 text-blue-700';
    }
  };

  const handleStageChange = async () => {
    if (!selectedStage) return;

    try {
      await changeOpportunityStage({
        id: opportunityId,
        data: {
          stage: selectedStage,
          notes: stageNotes || undefined,
          lossReason:
            selectedStage === 'CLOSED_LOST' ? lostReason || undefined : undefined,
        },
      }).unwrap();
      toast.success('Update opportunity stage successfully');
      setIsStageDialogOpen(false);
      setSelectedStage('');
      setStageNotes('');
      setLostReason('');
    } catch (error) {
      toast.error('Failed to update opportunity stage', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleMarkAsWon = async () => {
    try {
      await closeOpportunityWon({
        id: opportunityId,
        data: {
          actualValue: wonActualValue ? Number(wonActualValue) : undefined,
          notes: wonNotes || undefined,
        },
      }).unwrap();
      toast.success('Close opportunity as won successfully');
      setIsWonDialogOpen(false);
      setWonActualValue('');
      setWonNotes('');
    } catch (error) {
      toast.error('Failed to close opportunity as won', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleMarkAsLost = async () => {
    if (!lostReason.trim()) return;

    try {
      await closeOpportunityLost({
        id: opportunityId,
        data: { lossReason: lostReason.trim() },
      }).unwrap();
      toast.success('Close opportunity as lost successfully');
      setIsLostDialogOpen(false);
      setLostReason('');
    } catch (error) {
      toast.error('Failed to close opportunity as lost', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleReopen = async () => {
    if (!reopenReason.trim()) return;

    try {
      await reopenOpportunity({
        id: opportunityId,
        data: {
          stage: reopenStage,
          reopenReason: reopenReason.trim(),
        },
      }).unwrap();
      toast.success('Reopen opportunity successfully');
      setIsReopenDialogOpen(false);
      setReopenReason('');
      setReopenStage('PROSPECTING');
    } catch (error) {
      toast.error('Failed to reopen opportunity', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleDelete = async () => {
    try {
      await deleteOpportunity(opportunityId).unwrap();
      toast.success('Delete opportunity successfully');
      router.push('/crm/opportunities');
    } catch (error) {
      toast.error('Failed to delete opportunity', {
        description: getErrorMessage(error),
      });
    }
  };

  if (!isLoading && !opportunity) {
    return (
      <div className={cn('p-6', className)}>
        <Card>
          <CardContent className='py-16 text-center'>
            <AlertCircle className='mx-auto mb-4 h-12 w-12 text-muted-foreground' />
            <h2 className='mb-2 text-xl font-semibold'>Opportunity Not Found</h2>
            <p className='mb-4 text-muted-foreground'>
              The opportunity you're looking for doesn't exist or has been deleted.
            </p>
            <Button asChild>
              <Link href='/crm/opportunities'>Back to Opportunities</Link>
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className={cn('space-y-6 p-6', className)}>
      <div className='flex items-start justify-between'>
        <div className='flex items-start gap-4'>
          <Button variant='outline' size='icon' onClick={() => router.push('/crm/opportunities')}>
            <ArrowLeft className='h-4 w-4' />
          </Button>
          <div>
            <div className='mb-2 flex items-center gap-3'>
              <h1 className='text-2xl font-bold text-foreground'>
                {opportunity?.name || 'Opportunity'}
              </h1>
              {stageConfig && (
                <Badge className={cn(stageConfig.bgColor, stageConfig.color)}>
                  {stageConfig.label}
                </Badge>
              )}
              <Badge className={typeConfig.color}>{typeConfig.label}</Badge>
            </div>
            <div className='flex flex-wrap items-center gap-4 text-sm text-muted-foreground'>
              <span className='flex items-center gap-1'>
                <Building2 className='h-4 w-4' />
                {opportunity?.customerName || `Account #${opportunity?.accountId || ''}`}
              </span>
              <span className='flex items-center gap-1'>
                <User className='h-4 w-4' />
                {opportunity?.assignedToName || 'Unassigned'}
              </span>
              <span className='flex items-center gap-1'>
                <Calendar className='h-4 w-4' />
                Close: {formatDate(opportunity?.expectedCloseDate)}
              </span>
            </div>
          </div>
        </div>

        <div className='flex items-center gap-2'>
          {!isClosed && (
            <>
              <Button
                variant='outline'
                className='text-green-600 border-green-200 hover:bg-green-50 dark:border-green-800 dark:hover:bg-green-950'
                onClick={() => setIsWonDialogOpen(true)}
              >
                <Trophy className='mr-2 h-4 w-4' />
                Mark as Won
              </Button>
              <Button
                variant='outline'
                className='text-red-600 border-red-200 hover:bg-red-50 dark:border-red-800 dark:hover:bg-red-950'
                onClick={() => setIsLostDialogOpen(true)}
              >
                <XCircle className='mr-2 h-4 w-4' />
                Mark as Lost
              </Button>
            </>
          )}
          {isClosed && (
            <Button variant='outline' onClick={() => setIsReopenDialogOpen(true)}>
              <RefreshCw className='mr-2 h-4 w-4' />
              Reopen
            </Button>
          )}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant='outline' size='icon'>
                <MoreHorizontal className='h-4 w-4' />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align='end'>
              <DropdownMenuItem
                onClick={() => router.push(`/crm/opportunities/${opportunityId}/edit`)}
              >
                <Edit className='mr-2 h-4 w-4' />
                Edit Opportunity
              </DropdownMenuItem>
              {!isClosed && (
                <DropdownMenuItem onClick={() => setIsStageDialogOpen(true)}>
                  <TrendingUp className='mr-2 h-4 w-4' />
                  Change Stage
                </DropdownMenuItem>
              )}
              <DropdownMenuItem disabled>
                <MessageSquare className='mr-2 h-4 w-4' />
                Notes are read-only
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem disabled>
                <ExternalLink className='mr-2 h-4 w-4' />
                Export PDF
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem
                className='text-red-600'
                onClick={() => setIsDeleteDialogOpen(true)}
              >
                <Trash2 className='mr-2 h-4 w-4' />
                Delete Opportunity
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>

      <Card>
        <CardContent className='py-4'>
          <div className='mb-4 flex items-center justify-between'>
            <h3 className='font-semibold'>Pipeline Progress</h3>
            {!isClosed && (
              <Button variant='outline' size='sm' onClick={() => setIsStageDialogOpen(true)}>
                Change Stage
              </Button>
            )}
          </div>
          <div className='mb-4 flex items-center gap-2'>
            {PIPELINE_STAGES.filter((stage) => stage.stage !== 'CLOSED_LOST').map(
              (stage, index) => {
                const isActive = stage.stage === opportunity?.stage;
                const isPast = index < currentStageIndex && opportunity?.stage !== 'CLOSED_LOST';
                const isWon = opportunity?.stage === 'CLOSED_WON';

                return (
                  <div key={stage.stage} className='relative flex-1'>
                    <div
                      className={cn(
                        'h-2 rounded-full transition-colors',
                        isActive || isPast || isWon
                          ? stage.stage === 'CLOSED_WON' && isWon
                            ? 'bg-green-500'
                            : 'bg-primary'
                          : 'bg-muted'
                      )}
                    />
                    <div
                      className={cn(
                        'mt-2 text-center text-xs',
                        isActive ? 'font-semibold text-foreground' : 'text-muted-foreground'
                      )}
                    >
                      {stage.label}
                    </div>
                  </div>
                );
              }
            )}
          </div>
          <Progress value={probability} className='h-2' />
        </CardContent>
      </Card>

      <div className='grid grid-cols-1 gap-4 md:grid-cols-4'>
        <Card className='border-green-200 bg-gradient-to-br from-green-50 to-green-100/50 dark:border-green-800/50 dark:from-green-950 dark:to-green-900/50'>
          <CardContent className='py-4'>
            <div className='flex items-center justify-between'>
              <div>
                <p className='text-sm text-muted-foreground'>Deal Value</p>
                <p className='text-2xl font-bold text-green-700 dark:text-green-300'>
                  {formatCurrency(estimatedValue)}
                </p>
              </div>
              <div className='rounded-full bg-green-500/20 p-3'>
                <DollarSign className='h-6 w-6 text-green-600 dark:text-green-400' />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className='border-blue-200 bg-gradient-to-br from-blue-50 to-blue-100/50 dark:border-blue-800/50 dark:from-blue-950 dark:to-blue-900/50'>
          <CardContent className='py-4'>
            <div className='flex items-center justify-between'>
              <div>
                <p className='text-sm text-muted-foreground'>Weighted Value</p>
                <p className='text-2xl font-bold text-blue-700 dark:text-blue-300'>
                  {formatCurrency(weightedValue)}
                </p>
              </div>
              <div className='rounded-full bg-blue-500/20 p-3'>
                <Target className='h-6 w-6 text-blue-600 dark:text-blue-400' />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className='border-purple-200 bg-gradient-to-br from-purple-50 to-purple-100/50 dark:border-purple-800/50 dark:from-purple-950 dark:to-purple-900/50'>
          <CardContent className='py-4'>
            <div className='flex items-center justify-between'>
              <div>
                <p className='text-sm text-muted-foreground'>Probability</p>
                <p className='text-2xl font-bold text-purple-700 dark:text-purple-300'>
                  {probability}%
                </p>
              </div>
              <div className='rounded-full bg-purple-500/20 p-3'>
                <Percent className='h-6 w-6 text-purple-600 dark:text-purple-400' />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className='border-orange-200 bg-gradient-to-br from-orange-50 to-orange-100/50 dark:border-orange-800/50 dark:from-orange-950 dark:to-orange-900/50'>
          <CardContent className='py-4'>
            <div className='flex items-center justify-between'>
              <div>
                <p className='text-sm text-muted-foreground'>Days to Close</p>
                <p
                  className={cn(
                    'text-2xl font-bold',
                    daysUntilClose < 0
                      ? 'text-red-700 dark:text-red-300'
                      : 'text-orange-700 dark:text-orange-300'
                  )}
                >
                  {daysUntilClose < 0 ? `${Math.abs(daysUntilClose)} overdue` : daysUntilClose}
                </p>
              </div>
              <div className='rounded-full bg-orange-500/20 p-3'>
                <Clock className='h-6 w-6 text-orange-600 dark:text-orange-400' />
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList>
          <TabsTrigger value='overview'>Overview</TabsTrigger>
          <TabsTrigger value='activities'>Activities</TabsTrigger>
          <TabsTrigger value='products'>Products</TabsTrigger>
          <TabsTrigger value='timeline'>Timeline</TabsTrigger>
        </TabsList>

        <TabsContent value='overview' className='mt-6'>
          <div className='grid grid-cols-1 gap-6 lg:grid-cols-3'>
            <div className='space-y-6 lg:col-span-2'>
              <Card>
                <CardHeader>
                  <h3 className='font-semibold'>Description</h3>
                </CardHeader>
                <CardContent>
                  <p className='text-muted-foreground'>
                    {opportunity?.description || 'No description provided.'}
                  </p>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <h3 className='font-semibold'>Key Details</h3>
                </CardHeader>
                <CardContent>
                  <div className='grid grid-cols-1 gap-4 md:grid-cols-2'>
                    <div>
                      <label className='text-sm font-medium text-muted-foreground'>Account</label>
                      <p className='flex items-center gap-2 font-medium'>
                        <Building2 className='h-4 w-4 text-muted-foreground' />
                        {opportunity?.customerName || `Account #${opportunity?.accountId || ''}`}
                      </p>
                    </div>
                    <div>
                      <label className='text-sm font-medium text-muted-foreground'>Assigned To</label>
                      <p className='flex items-center gap-2 font-medium'>
                        <Avatar className='h-5 w-5 w-5'>
                          <AvatarFallback className='text-xs'>
                            {(opportunity?.assignedToName || 'Unassigned')
                              .split(' ')
                              .map((part) => part[0])
                              .join('')}
                          </AvatarFallback>
                        </Avatar>
                        {opportunity?.assignedToName || 'Unassigned'}
                      </p>
                    </div>
                    <div>
                      <label className='text-sm font-medium text-muted-foreground'>Expected Close Date</label>
                      <p className='font-medium'>{formatDate(opportunity?.expectedCloseDate)}</p>
                    </div>
                    <div>
                      <label className='text-sm font-medium text-muted-foreground'>Actual Close Date</label>
                      <p className='font-medium'>{formatDate(opportunity?.actualCloseDate)}</p>
                    </div>
                    <div>
                      <label className='text-sm font-medium text-muted-foreground'>Lead ID</label>
                      <p className='font-medium'>{opportunity?.leadId || 'Not linked'}</p>
                    </div>
                    <div>
                      <label className='text-sm font-medium text-muted-foreground'>Days in Pipeline</label>
                      <p className='font-medium'>{daysInPipeline}</p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <h3 className='font-semibold'>Notes</h3>
                </CardHeader>
                <CardContent>
                  <p className='text-muted-foreground'>
                    {opportunity?.notes || 'No notes available.'}
                  </p>
                </CardContent>
              </Card>
            </div>

            <div className='space-y-6'>
              <Card>
                <CardHeader>
                  <h3 className='font-semibold'>Opportunity Summary</h3>
                </CardHeader>
                <CardContent className='space-y-4'>
                  <div className='flex items-center justify-between'>
                    <span className='text-sm text-muted-foreground'>Stage</span>
                    <span className='font-medium'>{stageConfig?.label || 'Unknown'}</span>
                  </div>
                  <div className='flex items-center justify-between'>
                    <span className='text-sm text-muted-foreground'>Estimated Value</span>
                    <span className='font-medium'>{formatCurrency(estimatedValue)}</span>
                  </div>
                  <div className='flex items-center justify-between'>
                    <span className='text-sm text-muted-foreground'>Actual Value</span>
                    <span className='font-medium'>
                      {formatCurrency(opportunity?.actualValue)}
                    </span>
                  </div>
                  <div className='flex items-center justify-between'>
                    <span className='text-sm text-muted-foreground'>Probability</span>
                    <span className='font-medium'>{probability}%</span>
                  </div>
                  {opportunity?.lostReason && (
                    <div>
                      <span className='text-sm text-muted-foreground'>Loss Reason</span>
                      <p className='mt-1 text-sm font-medium'>{opportunity.lostReason}</p>
                    </div>
                  )}
                  {opportunity?.reopenReason && (
                    <div>
                      <span className='text-sm text-muted-foreground'>Reopen Reason</span>
                      <p className='mt-1 text-sm font-medium'>{opportunity.reopenReason}</p>
                    </div>
                  )}
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <h3 className='font-semibold'>System Metadata</h3>
                </CardHeader>
                <CardContent className='space-y-3'>
                  <div>
                    <span className='text-sm text-muted-foreground'>Created</span>
                    <p className='text-sm font-medium'>{formatDateTime(opportunity?.createdAt)}</p>
                  </div>
                  <div>
                    <span className='text-sm text-muted-foreground'>Last Updated</span>
                    <p className='text-sm font-medium'>{formatDateTime(opportunity?.updatedAt)}</p>
                  </div>
                  <div>
                    <span className='text-sm text-muted-foreground'>Created By</span>
                    <p className='text-sm font-medium'>
                      {String(opportunity?.customFields?.createdBy || 'Not available')}
                    </p>
                  </div>
                  <div>
                    <span className='text-sm text-muted-foreground'>Updated By</span>
                    <p className='text-sm font-medium'>
                      {String(opportunity?.customFields?.updatedBy || 'Not available')}
                    </p>
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>
        </TabsContent>

        <TabsContent value='activities' className='mt-6'>
          <Card>
            <CardHeader>
              <h3 className='font-semibold'>Recent Activities</h3>
            </CardHeader>
            <CardContent>
              {isActivitiesLoading ? (
                <p className='text-sm text-muted-foreground'>Loading activities...</p>
              ) : activities.length > 0 ? (
                <div className='space-y-4'>
                  {activities.map((activity) => (
                    <div key={activity.id} className='rounded-lg border p-4'>
                      <div className='mb-2 flex items-start justify-between gap-3'>
                        <div>
                          <p className='font-medium'>{activity.subject}</p>
                          <p className='text-sm text-muted-foreground'>
                            {activity.description || 'No description provided.'}
                          </p>
                        </div>
                        <Badge className={getActivityBadgeClass(activity.status)}>
                          {activity.status}
                        </Badge>
                      </div>
                      <div className='flex flex-wrap gap-4 text-sm text-muted-foreground'>
                        <span className='flex items-center gap-1'>
                          <Clock className='h-4 w-4' />
                          {formatDateTime(activity.scheduledDate || activity.createdAt)}
                        </span>
                        <span className='flex items-center gap-1'>
                          <User className='h-4 w-4' />
                          {activity.assignedToName || 'Unassigned'}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p className='text-sm text-muted-foreground'>
                  No activities available for this opportunity.
                </p>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value='products' className='mt-6'>
          <Card>
            <CardHeader>
              <h3 className='font-semibold'>Products</h3>
            </CardHeader>
            <CardContent className='text-sm text-muted-foreground'>
              <div className='flex items-start gap-3 rounded-lg border border-dashed p-4'>
                <Package className='mt-0.5 h-4 w-4' />
                <div>
                  <p className='font-medium text-foreground'>Products are not integrated yet</p>
                  <p>
                    This section is currently read-only and waiting for backend product line support.
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value='timeline' className='mt-6'>
          <Card>
            <CardHeader>
              <h3 className='font-semibold'>Timeline</h3>
            </CardHeader>
            <CardContent className='text-sm text-muted-foreground'>
              <div className='flex items-start gap-3 rounded-lg border border-dashed p-4'>
                <History className='mt-0.5 h-4 w-4' />
                <div>
                  <p className='font-medium text-foreground'>Timeline is not integrated yet</p>
                  <p>
                    Opportunity change history is currently shown only through the available activity feed.
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      <Dialog open={isStageDialogOpen} onOpenChange={setIsStageDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Change Pipeline Stage</DialogTitle>
            <DialogDescription>
              Update the current stage of this opportunity.
            </DialogDescription>
          </DialogHeader>
          <div className='space-y-4'>
            <div className='space-y-2'>
              <Label>Stage</Label>
              <Select
                value={selectedStage}
                onValueChange={(value) => setSelectedStage(value as OpportunityStage)}
              >
                <SelectTrigger>
                  <SelectValue placeholder='Select stage' />
                </SelectTrigger>
                <SelectContent>
                  {PIPELINE_STAGES.map((stage) => (
                    <SelectItem key={stage.stage} value={stage.stage}>
                      {stage.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className='space-y-2'>
              <Label htmlFor='stageNotes'>Notes</Label>
              <Textarea
                id='stageNotes'
                value={stageNotes}
                onChange={(e) => setStageNotes(e.target.value)}
                placeholder='Optional stage update notes'
              />
            </div>
            {selectedStage === 'CLOSED_LOST' && (
              <div className='space-y-2'>
                <Label htmlFor='stageLossReason'>Loss Reason</Label>
                <Textarea
                  id='stageLossReason'
                  value={lostReason}
                  onChange={(e) => setLostReason(e.target.value)}
                  placeholder='Required when closing as lost'
                />
              </div>
            )}
          </div>
          <DialogFooter>
            <Button variant='outline' onClick={() => setIsStageDialogOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleStageChange} disabled={!selectedStage}>
              Update Stage
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={isWonDialogOpen} onOpenChange={setIsWonDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Mark as Won</DialogTitle>
            <DialogDescription>
              Close this opportunity as won and optionally set the actual value.
            </DialogDescription>
          </DialogHeader>
          <div className='space-y-4'>
            <div className='space-y-2'>
              <Label htmlFor='wonActualValue'>Actual Value</Label>
              <Input
                id='wonActualValue'
                type='number'
                min={0}
                value={wonActualValue}
                onChange={(e) => setWonActualValue(e.target.value)}
                placeholder='Optional final deal value'
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='wonNotes'>Notes</Label>
              <Textarea
                id='wonNotes'
                value={wonNotes}
                onChange={(e) => setWonNotes(e.target.value)}
                placeholder='Optional closing notes'
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant='outline' onClick={() => setIsWonDialogOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleMarkAsWon}>Close as Won</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={isLostDialogOpen} onOpenChange={setIsLostDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Mark as Lost</DialogTitle>
            <DialogDescription>
              Provide the reason for closing this opportunity as lost.
            </DialogDescription>
          </DialogHeader>
          <div className='space-y-2'>
            <Label htmlFor='lostReason'>Loss Reason</Label>
            <Textarea
              id='lostReason'
              value={lostReason}
              onChange={(e) => setLostReason(e.target.value)}
              placeholder='Required loss reason'
            />
          </div>
          <DialogFooter>
            <Button variant='outline' onClick={() => setIsLostDialogOpen(false)}>
              Cancel
            </Button>
            <Button
              variant='destructive'
              onClick={handleMarkAsLost}
              disabled={!lostReason.trim()}
            >
              Close as Lost
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={isReopenDialogOpen} onOpenChange={setIsReopenDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Reopen Opportunity</DialogTitle>
            <DialogDescription>
              Select the stage to move this opportunity back into and explain why.
            </DialogDescription>
          </DialogHeader>
          <div className='space-y-4'>
            <div className='space-y-2'>
              <Label>Stage</Label>
              <Select
                value={reopenStage}
                onValueChange={(value) => setReopenStage(value as OpportunityStage)}
              >
                <SelectTrigger>
                  <SelectValue placeholder='Select stage' />
                </SelectTrigger>
                <SelectContent>
                  {PIPELINE_STAGES.filter(
                    (stage) =>
                      stage.stage !== 'CLOSED_WON' && stage.stage !== 'CLOSED_LOST'
                  ).map((stage) => (
                    <SelectItem key={stage.stage} value={stage.stage}>
                      {stage.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className='space-y-2'>
              <Label htmlFor='reopenReason'>Reopen Reason</Label>
              <Textarea
                id='reopenReason'
                value={reopenReason}
                onChange={(e) => setReopenReason(e.target.value)}
                placeholder='Required reopen reason'
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant='outline' onClick={() => setIsReopenDialogOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleReopen} disabled={!reopenReason.trim()}>
              Reopen Opportunity
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Delete Opportunity</DialogTitle>
            <DialogDescription>
              This action cannot be undone. This opportunity will be permanently deleted.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant='outline' onClick={() => setIsDeleteDialogOpen(false)}>
              Cancel
            </Button>
            <Button variant='destructive' onClick={handleDelete}>
              Delete Opportunity
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default OpportunityDetailPage;
