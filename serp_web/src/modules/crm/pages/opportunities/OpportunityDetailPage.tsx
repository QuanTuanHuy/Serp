/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

'use client';

import { useState, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import {
  Card,
  CardContent,
  CardHeader,
  Button,
  Badge,
  Input,
  Textarea,
  Progress,
  Avatar,
  AvatarFallback,
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/shared/components/ui';
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
  Award,
  AlertCircle,
  CheckCircle2,
  XCircle,
  Phone,
  Mail,
  MessageSquare,
  FileText,
  Plus,
  ChevronRight,
  ExternalLink,
  Trophy,
  Percent,
  Package,
  Users,
  History,
  Sparkles,
  ArrowUpRight,
  ArrowDownRight,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { MOCK_OPPORTUNITIES, MOCK_ACTIVITIES } from '../../mocks';
import type {
  Opportunity,
  OpportunityStage,
  OpportunityProduct,
  Activity,
} from '../../types';

// Pipeline stage configuration
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

// Mock products for the opportunity
const MOCK_PRODUCTS: OpportunityProduct[] = [
  {
    id: '1',
    productId: 'prod-001',
    productName: 'Enterprise CRM License',
    quantity: 50,
    unitPrice: 99,
    discount: 10,
    totalPrice: 4455,
  },
  {
    id: '2',
    productId: 'prod-002',
    productName: 'Implementation Services',
    quantity: 1,
    unitPrice: 15000,
    discount: 0,
    totalPrice: 15000,
  },
  {
    id: '3',
    productId: 'prod-003',
    productName: 'Training Package',
    quantity: 2,
    unitPrice: 2500,
    discount: 5,
    totalPrice: 4750,
  },
  {
    id: '4',
    productId: 'prod-004',
    productName: 'Premium Support (Annual)',
    quantity: 1,
    unitPrice: 5000,
    discount: 0,
    totalPrice: 5000,
  },
];

// Mock timeline/history
interface TimelineEvent {
  id: string;
  type: 'stage_change' | 'note' | 'activity' | 'value_change' | 'created';
  title: string;
  description?: string;
  timestamp: string;
  user: string;
  metadata?: Record<string, any>;
}

const MOCK_TIMELINE: TimelineEvent[] = [
  {
    id: '1',
    type: 'stage_change',
    title: 'Stage changed to Negotiation',
    description: 'Moved from Proposal to Negotiation after contract review',
    timestamp: '2024-01-20T14:30:00Z',
    user: 'John Smith',
    metadata: { from: 'PROPOSAL', to: 'NEGOTIATION' },
  },
  {
    id: '2',
    type: 'activity',
    title: 'Meeting scheduled',
    description: 'Final contract negotiation meeting with CFO',
    timestamp: '2024-01-18T10:00:00Z',
    user: 'John Smith',
  },
  {
    id: '3',
    type: 'value_change',
    title: 'Deal value updated',
    description: 'Value increased from $45,000 to $50,000',
    timestamp: '2024-01-15T16:45:00Z',
    user: 'John Smith',
    metadata: { from: 45000, to: 50000 },
  },
  {
    id: '4',
    type: 'note',
    title: 'Added note',
    description: 'Client expressed interest in additional training modules',
    timestamp: '2024-01-12T09:15:00Z',
    user: 'Sarah Johnson',
  },
  {
    id: '5',
    type: 'stage_change',
    title: 'Stage changed to Proposal',
    description: 'Sent detailed proposal with pricing',
    timestamp: '2024-01-10T11:00:00Z',
    user: 'John Smith',
    metadata: { from: 'QUALIFICATION', to: 'PROPOSAL' },
  },
  {
    id: '6',
    type: 'created',
    title: 'Opportunity created',
    description: 'New opportunity from qualified lead',
    timestamp: '2024-01-05T08:30:00Z',
    user: 'John Smith',
  },
];

// Mock competitors
const MOCK_COMPETITORS = [
  { name: 'Salesforce', strength: 'Brand recognition', weakness: 'High cost' },
  {
    name: 'HubSpot',
    strength: 'Easy to use',
    weakness: 'Limited customization',
  },
  { name: 'Zoho CRM', strength: 'Affordable', weakness: 'Less features' },
];

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
  const [isNoteDialogOpen, setIsNoteDialogOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [isLostDialogOpen, setIsLostDialogOpen] = useState(false);
  const [selectedStage, setSelectedStage] = useState<OpportunityStage | ''>('');
  const [noteText, setNoteText] = useState('');
  const [lostReason, setLostReason] = useState('');

  // Find opportunity from mock data
  const opportunity = useMemo(() => {
    return MOCK_OPPORTUNITIES.find((o) => o.id === opportunityId);
  }, [opportunityId]);

  // Get related activities
  const relatedActivities = useMemo(() => {
    return MOCK_ACTIVITIES.filter(
      (a) =>
        a.relatedTo.type === 'OPPORTUNITY' && a.relatedTo.id === opportunityId
    ).slice(0, 5);
  }, [opportunityId]);

  if (!opportunity) {
    return (
      <div className={cn('p-6', className)}>
        <Card>
          <CardContent className='py-16 text-center'>
            <AlertCircle className='w-12 h-12 mx-auto mb-4 text-muted-foreground' />
            <h2 className='text-xl font-semibold mb-2'>
              Opportunity Not Found
            </h2>
            <p className='text-muted-foreground mb-4'>
              The opportunity you're looking for doesn't exist or has been
              deleted.
            </p>
            <Button onClick={() => router.push('/crm/opportunities')}>
              Back to Opportunities
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  const stageConfig = PIPELINE_STAGES.find(
    (s) => s.stage === opportunity.stage
  );
  const typeConfig = OPPORTUNITY_TYPES[opportunity.type];
  const currentStageIndex = PIPELINE_STAGES.findIndex(
    (s) => s.stage === opportunity.stage
  );
  const isClosed =
    opportunity.stage === 'CLOSED_WON' || opportunity.stage === 'CLOSED_LOST';

  // Calculate weighted value
  const weightedValue = (opportunity.value * opportunity.probability) / 100;

  // Calculate days in pipeline
  const daysInPipeline = Math.floor(
    (new Date().getTime() - new Date(opportunity.createdAt).getTime()) /
      (1000 * 60 * 60 * 24)
  );

  // Calculate days until expected close
  const daysUntilClose = Math.floor(
    (new Date(opportunity.expectedCloseDate).getTime() - new Date().getTime()) /
      (1000 * 60 * 60 * 24)
  );

  const handleStageChange = () => {
    if (selectedStage) {
      console.log('Changing stage to:', selectedStage);
      // API call would go here
      setIsStageDialogOpen(false);
      setSelectedStage('');
    }
  };

  const handleMarkAsWon = () => {
    console.log('Marking as won');
    // API call would go here
  };

  const handleMarkAsLost = () => {
    if (lostReason) {
      console.log('Marking as lost with reason:', lostReason);
      // API call would go here
      setIsLostDialogOpen(false);
      setLostReason('');
    }
  };

  const handleAddNote = () => {
    if (noteText.trim()) {
      console.log('Adding note:', noteText);
      // API call would go here
      setIsNoteDialogOpen(false);
      setNoteText('');
    }
  };

  const handleDelete = () => {
    console.log('Deleting opportunity');
    // API call would go here
    router.push('/crm/opportunities');
  };

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(value);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  const formatDateTime = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className={cn('p-6 space-y-6', className)}>
      {/* Header */}
      <div className='flex items-start justify-between'>
        <div className='flex items-start gap-4'>
          <Button
            variant='outline'
            size='icon'
            onClick={() => router.push('/crm/opportunities')}
          >
            <ArrowLeft className='w-4 h-4' />
          </Button>
          <div>
            <div className='flex items-center gap-3 mb-2'>
              <h1 className='text-2xl font-bold text-foreground'>
                {opportunity.name}
              </h1>
              <Badge className={cn(stageConfig?.bgColor, stageConfig?.color)}>
                {stageConfig?.label}
              </Badge>
              <Badge className={typeConfig.color}>{typeConfig.label}</Badge>
            </div>
            <div className='flex items-center gap-4 text-sm text-muted-foreground'>
              <span className='flex items-center gap-1'>
                <Building2 className='w-4 h-4' />
                {opportunity.customerName}
              </span>
              <span className='flex items-center gap-1'>
                <User className='w-4 h-4' />
                {opportunity.assignedToName}
              </span>
              <span className='flex items-center gap-1'>
                <Calendar className='w-4 h-4' />
                Close: {formatDate(opportunity.expectedCloseDate)}
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
                onClick={handleMarkAsWon}
              >
                <Trophy className='w-4 h-4 mr-2' />
                Mark as Won
              </Button>
              <Button
                variant='outline'
                className='text-red-600 border-red-200 hover:bg-red-50 dark:border-red-800 dark:hover:bg-red-950'
                onClick={() => setIsLostDialogOpen(true)}
              >
                <XCircle className='w-4 h-4 mr-2' />
                Mark as Lost
              </Button>
            </>
          )}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant='outline' size='icon'>
                <MoreHorizontal className='w-4 h-4' />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align='end'>
              <DropdownMenuItem
                onClick={() =>
                  router.push(`/crm/opportunities/${opportunityId}/edit`)
                }
              >
                <Edit className='w-4 h-4 mr-2' />
                Edit Opportunity
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => setIsStageDialogOpen(true)}>
                <TrendingUp className='w-4 h-4 mr-2' />
                Change Stage
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => setIsNoteDialogOpen(true)}>
                <MessageSquare className='w-4 h-4 mr-2' />
                Add Note
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem
                className='text-red-600'
                onClick={() => setIsDeleteDialogOpen(true)}
              >
                <Trash2 className='w-4 h-4 mr-2' />
                Delete
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>

      {/* Pipeline Progress */}
      <Card>
        <CardContent className='py-4'>
          <div className='flex items-center justify-between mb-4'>
            <h3 className='font-semibold'>Pipeline Progress</h3>
            {!isClosed && (
              <Button
                variant='outline'
                size='sm'
                onClick={() => setIsStageDialogOpen(true)}
              >
                Change Stage
              </Button>
            )}
          </div>
          <div className='flex items-center gap-2'>
            {PIPELINE_STAGES.filter((s) => s.stage !== 'CLOSED_LOST').map(
              (stage, index) => {
                const isActive = stage.stage === opportunity.stage;
                const isPast =
                  index < currentStageIndex &&
                  opportunity.stage !== 'CLOSED_LOST';
                const isWon = opportunity.stage === 'CLOSED_WON';

                return (
                  <div key={stage.stage} className='flex-1 relative'>
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
                        'text-xs mt-2 text-center',
                        isActive
                          ? 'font-semibold text-foreground'
                          : 'text-muted-foreground'
                      )}
                    >
                      {stage.label}
                    </div>
                    {isActive && (
                      <div className='absolute -top-1 left-1/2 -translate-x-1/2 w-4 h-4 rounded-full bg-primary border-2 border-background' />
                    )}
                  </div>
                );
              }
            )}
          </div>
          {opportunity.stage === 'CLOSED_LOST' && (
            <div className='mt-4 p-3 bg-red-50 dark:bg-red-950/50 rounded-lg border border-red-200 dark:border-red-800'>
              <div className='flex items-center gap-2 text-red-700 dark:text-red-300'>
                <XCircle className='w-4 h-4' />
                <span className='font-medium'>Deal Lost</span>
              </div>
              {opportunity.lostReason && (
                <p className='text-sm text-red-600 dark:text-red-400 mt-1'>
                  Reason: {opportunity.lostReason}
                </p>
              )}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Stats Cards */}
      <div className='grid grid-cols-1 md:grid-cols-4 gap-4'>
        <Card className='bg-gradient-to-br from-green-50 to-green-100/50 dark:from-green-950 dark:to-green-900/50 border-green-200 dark:border-green-800/50'>
          <CardContent className='py-4'>
            <div className='flex items-center justify-between'>
              <div>
                <p className='text-sm text-muted-foreground'>Deal Value</p>
                <p className='text-2xl font-bold text-green-700 dark:text-green-300'>
                  {formatCurrency(opportunity.value)}
                </p>
              </div>
              <div className='p-3 bg-green-500/20 rounded-full'>
                <DollarSign className='w-6 h-6 text-green-600 dark:text-green-400' />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className='bg-gradient-to-br from-blue-50 to-blue-100/50 dark:from-blue-950 dark:to-blue-900/50 border-blue-200 dark:border-blue-800/50'>
          <CardContent className='py-4'>
            <div className='flex items-center justify-between'>
              <div>
                <p className='text-sm text-muted-foreground'>Weighted Value</p>
                <p className='text-2xl font-bold text-blue-700 dark:text-blue-300'>
                  {formatCurrency(weightedValue)}
                </p>
              </div>
              <div className='p-3 bg-blue-500/20 rounded-full'>
                <Target className='w-6 h-6 text-blue-600 dark:text-blue-400' />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className='bg-gradient-to-br from-purple-50 to-purple-100/50 dark:from-purple-950 dark:to-purple-900/50 border-purple-200 dark:border-purple-800/50'>
          <CardContent className='py-4'>
            <div className='flex items-center justify-between'>
              <div>
                <p className='text-sm text-muted-foreground'>Probability</p>
                <p className='text-2xl font-bold text-purple-700 dark:text-purple-300'>
                  {opportunity.probability}%
                </p>
              </div>
              <div className='p-3 bg-purple-500/20 rounded-full'>
                <Percent className='w-6 h-6 text-purple-600 dark:text-purple-400' />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className='bg-gradient-to-br from-orange-50 to-orange-100/50 dark:from-orange-950 dark:to-orange-900/50 border-orange-200 dark:border-orange-800/50'>
          <CardContent className='py-4'>
            <div className='flex items-center justify-between'>
              <div>
                <p className='text-sm text-muted-foreground'>Days to Close</p>
                <p
                  className={cn(
                    'text-2xl font-bold',
                    daysUntilClose < 0
                      ? 'text-red-700 dark:text-red-300'
                      : daysUntilClose < 7
                        ? 'text-orange-700 dark:text-orange-300'
                        : 'text-orange-700 dark:text-orange-300'
                  )}
                >
                  {daysUntilClose < 0
                    ? `${Math.abs(daysUntilClose)} overdue`
                    : daysUntilClose}
                </p>
              </div>
              <div className='p-3 bg-orange-500/20 rounded-full'>
                <Clock className='w-6 h-6 text-orange-600 dark:text-orange-400' />
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Tabs */}
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList>
          <TabsTrigger value='overview'>Overview</TabsTrigger>
          <TabsTrigger value='products'>
            Products ({MOCK_PRODUCTS.length})
          </TabsTrigger>
          <TabsTrigger value='activities'>Activities</TabsTrigger>
          <TabsTrigger value='timeline'>Timeline</TabsTrigger>
          <TabsTrigger value='competitors'>Competitors</TabsTrigger>
        </TabsList>

        {/* Overview Tab */}
        <TabsContent value='overview' className='mt-6'>
          <div className='grid grid-cols-1 lg:grid-cols-3 gap-6'>
            {/* Main Info */}
            <div className='lg:col-span-2 space-y-6'>
              {/* Description */}
              <Card>
                <CardHeader>
                  <h3 className='font-semibold'>Description</h3>
                </CardHeader>
                <CardContent>
                  <p className='text-muted-foreground'>
                    {opportunity.description || 'No description provided.'}
                  </p>
                </CardContent>
              </Card>

              {/* Key Details */}
              <Card>
                <CardHeader>
                  <h3 className='font-semibold'>Key Details</h3>
                </CardHeader>
                <CardContent>
                  <div className='grid grid-cols-2 gap-4'>
                    <div>
                      <label className='text-sm font-medium text-muted-foreground'>
                        Customer
                      </label>
                      <p className='font-medium flex items-center gap-2'>
                        <Building2 className='w-4 h-4 text-muted-foreground' />
                        {opportunity.customerName}
                        <Button variant='ghost' size='sm' className='h-6 px-2'>
                          <ExternalLink className='w-3 h-3' />
                        </Button>
                      </p>
                    </div>
                    <div>
                      <label className='text-sm font-medium text-muted-foreground'>
                        Assigned To
                      </label>
                      <p className='font-medium flex items-center gap-2'>
                        <Avatar className='w-5 h-5'>
                          <AvatarFallback className='text-xs'>
                            {opportunity.assignedToName
                              .split(' ')
                              .map((n) => n[0])
                              .join('')}
                          </AvatarFallback>
                        </Avatar>
                        {opportunity.assignedToName}
                      </p>
                    </div>
                    <div>
                      <label className='text-sm font-medium text-muted-foreground'>
                        Expected Close Date
                      </label>
                      <p className='font-medium'>
                        {formatDate(opportunity.expectedCloseDate)}
                      </p>
                    </div>
                    <div>
                      <label className='text-sm font-medium text-muted-foreground'>
                        Days in Pipeline
                      </label>
                      <p className='font-medium'>{daysInPipeline} days</p>
                    </div>
                    <div>
                      <label className='text-sm font-medium text-muted-foreground'>
                        Created
                      </label>
                      <p className='font-medium'>
                        {formatDate(opportunity.createdAt)}
                      </p>
                    </div>
                    <div>
                      <label className='text-sm font-medium text-muted-foreground'>
                        Last Updated
                      </label>
                      <p className='font-medium'>
                        {formatDate(opportunity.updatedAt)}
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* Next Steps */}
              <Card>
                <CardHeader className='flex flex-row items-center justify-between'>
                  <h3 className='font-semibold'>Next Steps</h3>
                  <Button variant='outline' size='sm'>
                    <Plus className='w-4 h-4 mr-2' />
                    Add Action
                  </Button>
                </CardHeader>
                <CardContent>
                  {opportunity.nextAction ? (
                    <div className='p-4 bg-muted rounded-lg'>
                      <div className='flex items-start justify-between'>
                        <div>
                          <p className='font-medium'>
                            {opportunity.nextAction}
                          </p>
                          {opportunity.nextActionDate && (
                            <p className='text-sm text-muted-foreground mt-1'>
                              Due: {formatDate(opportunity.nextActionDate)}
                            </p>
                          )}
                        </div>
                        <Button variant='outline' size='sm'>
                          Complete
                        </Button>
                      </div>
                    </div>
                  ) : (
                    <p className='text-muted-foreground'>
                      No next action defined.
                    </p>
                  )}
                </CardContent>
              </Card>
            </div>

            {/* Sidebar */}
            <div className='space-y-6'>
              {/* Quick Actions */}
              <Card>
                <CardHeader>
                  <h3 className='font-semibold'>Quick Actions</h3>
                </CardHeader>
                <CardContent className='space-y-2'>
                  <Button variant='outline' className='w-full justify-start'>
                    <Phone className='w-4 h-4 mr-2' />
                    Log Call
                  </Button>
                  <Button variant='outline' className='w-full justify-start'>
                    <Mail className='w-4 h-4 mr-2' />
                    Send Email
                  </Button>
                  <Button variant='outline' className='w-full justify-start'>
                    <Calendar className='w-4 h-4 mr-2' />
                    Schedule Meeting
                  </Button>
                  <Button variant='outline' className='w-full justify-start'>
                    <FileText className='w-4 h-4 mr-2' />
                    Create Proposal
                  </Button>
                </CardContent>
              </Card>

              {/* Tags */}
              <Card>
                <CardHeader>
                  <h3 className='font-semibold'>Tags</h3>
                </CardHeader>
                <CardContent>
                  <div className='flex flex-wrap gap-2'>
                    {opportunity.tags.length > 0 ? (
                      opportunity.tags.map((tag) => (
                        <Badge key={tag} variant='secondary'>
                          {tag}
                        </Badge>
                      ))
                    ) : (
                      <p className='text-sm text-muted-foreground'>No tags</p>
                    )}
                  </div>
                </CardContent>
              </Card>

              {/* Notes */}
              <Card>
                <CardHeader className='flex flex-row items-center justify-between'>
                  <h3 className='font-semibold'>Notes</h3>
                  <Button
                    variant='ghost'
                    size='sm'
                    onClick={() => setIsNoteDialogOpen(true)}
                  >
                    <Plus className='w-4 h-4' />
                  </Button>
                </CardHeader>
                <CardContent>
                  <p className='text-muted-foreground text-sm'>
                    {opportunity.notes || 'No notes yet.'}
                  </p>
                </CardContent>
              </Card>
            </div>
          </div>
        </TabsContent>

        {/* Products Tab */}
        <TabsContent value='products' className='mt-6'>
          <Card>
            <CardHeader className='flex flex-row items-center justify-between'>
              <h3 className='font-semibold'>Products & Services</h3>
              <Button>
                <Plus className='w-4 h-4 mr-2' />
                Add Product
              </Button>
            </CardHeader>
            <CardContent>
              <div className='overflow-x-auto'>
                <table className='w-full'>
                  <thead>
                    <tr className='border-b'>
                      <th className='text-left py-3 px-4 font-medium text-muted-foreground'>
                        Product
                      </th>
                      <th className='text-right py-3 px-4 font-medium text-muted-foreground'>
                        Qty
                      </th>
                      <th className='text-right py-3 px-4 font-medium text-muted-foreground'>
                        Unit Price
                      </th>
                      <th className='text-right py-3 px-4 font-medium text-muted-foreground'>
                        Discount
                      </th>
                      <th className='text-right py-3 px-4 font-medium text-muted-foreground'>
                        Total
                      </th>
                      <th className='w-10'></th>
                    </tr>
                  </thead>
                  <tbody>
                    {MOCK_PRODUCTS.map((product) => (
                      <tr key={product.id} className='border-b last:border-0'>
                        <td className='py-3 px-4'>
                          <div className='flex items-center gap-3'>
                            <div className='p-2 bg-muted rounded'>
                              <Package className='w-4 h-4 text-muted-foreground' />
                            </div>
                            <span className='font-medium'>
                              {product.productName}
                            </span>
                          </div>
                        </td>
                        <td className='py-3 px-4 text-right'>
                          {product.quantity}
                        </td>
                        <td className='py-3 px-4 text-right'>
                          {formatCurrency(product.unitPrice)}
                        </td>
                        <td className='py-3 px-4 text-right'>
                          {product.discount > 0 ? (
                            <Badge variant='secondary'>
                              {product.discount}%
                            </Badge>
                          ) : (
                            '-'
                          )}
                        </td>
                        <td className='py-3 px-4 text-right font-medium'>
                          {formatCurrency(product.totalPrice)}
                        </td>
                        <td className='py-3 px-4'>
                          <Button
                            variant='ghost'
                            size='icon'
                            className='h-8 w-8'
                          >
                            <MoreHorizontal className='w-4 h-4' />
                          </Button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                  <tfoot>
                    <tr className='bg-muted/50'>
                      <td
                        colSpan={4}
                        className='py-3 px-4 font-semibold text-right'
                      >
                        Total Deal Value
                      </td>
                      <td className='py-3 px-4 text-right font-bold text-lg'>
                        {formatCurrency(
                          MOCK_PRODUCTS.reduce(
                            (sum, p) => sum + p.totalPrice,
                            0
                          )
                        )}
                      </td>
                      <td></td>
                    </tr>
                  </tfoot>
                </table>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Activities Tab */}
        <TabsContent value='activities' className='mt-6'>
          <Card>
            <CardHeader className='flex flex-row items-center justify-between'>
              <h3 className='font-semibold'>Recent Activities</h3>
              <Button>
                <Plus className='w-4 h-4 mr-2' />
                Log Activity
              </Button>
            </CardHeader>
            <CardContent>
              {relatedActivities.length > 0 ? (
                <div className='space-y-4'>
                  {relatedActivities.map((activity) => (
                    <div
                      key={activity.id}
                      className='flex items-start gap-4 p-4 bg-muted rounded-lg hover:bg-muted/80 transition-colors cursor-pointer'
                    >
                      <div
                        className={cn(
                          'p-2 rounded-full',
                          activity.status === 'COMPLETED'
                            ? 'bg-green-100 dark:bg-green-900/50'
                            : 'bg-blue-100 dark:bg-blue-900/50'
                        )}
                      >
                        {activity.type === 'CALL' && (
                          <Phone className='w-4 h-4 text-blue-600 dark:text-blue-400' />
                        )}
                        {activity.type === 'EMAIL' && (
                          <Mail className='w-4 h-4 text-blue-600 dark:text-blue-400' />
                        )}
                        {activity.type === 'MEETING' && (
                          <Users className='w-4 h-4 text-purple-600 dark:text-purple-400' />
                        )}
                        {activity.type === 'TASK' && (
                          <CheckCircle2 className='w-4 h-4 text-green-600 dark:text-green-400' />
                        )}
                      </div>
                      <div className='flex-1'>
                        <div className='flex items-center justify-between'>
                          <h4 className='font-medium'>{activity.subject}</h4>
                          <Badge
                            variant={
                              activity.status === 'COMPLETED'
                                ? 'default'
                                : 'secondary'
                            }
                          >
                            {activity.status}
                          </Badge>
                        </div>
                        <p className='text-sm text-muted-foreground mt-1'>
                          {activity.description || 'No description'}
                        </p>
                        <div className='flex items-center gap-4 mt-2 text-xs text-muted-foreground'>
                          <span>{activity.assignedToName}</span>
                          <span>•</span>
                          <span>
                            {formatDateTime(
                              activity.scheduledDate || activity.createdAt
                            )}
                          </span>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className='text-center py-8'>
                  <Calendar className='w-12 h-12 mx-auto mb-4 text-muted-foreground' />
                  <p className='text-muted-foreground mb-4'>
                    No activities recorded yet.
                  </p>
                  <Button>Log First Activity</Button>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        {/* Timeline Tab */}
        <TabsContent value='timeline' className='mt-6'>
          <Card>
            <CardHeader>
              <h3 className='font-semibold'>Opportunity History</h3>
            </CardHeader>
            <CardContent>
              <div className='relative'>
                <div className='absolute left-4 top-0 bottom-0 w-0.5 bg-muted' />
                <div className='space-y-6'>
                  {MOCK_TIMELINE.map((event, index) => (
                    <div key={event.id} className='relative pl-10'>
                      <div
                        className={cn(
                          'absolute left-2 w-5 h-5 rounded-full border-2 border-background flex items-center justify-center',
                          event.type === 'stage_change'
                            ? 'bg-blue-500'
                            : event.type === 'value_change'
                              ? 'bg-green-500'
                              : event.type === 'created'
                                ? 'bg-purple-500'
                                : 'bg-muted'
                        )}
                      >
                        {event.type === 'stage_change' && (
                          <TrendingUp className='w-3 h-3 text-white' />
                        )}
                        {event.type === 'value_change' && (
                          <DollarSign className='w-3 h-3 text-white' />
                        )}
                        {event.type === 'activity' && (
                          <Calendar className='w-3 h-3 text-muted-foreground' />
                        )}
                        {event.type === 'note' && (
                          <MessageSquare className='w-3 h-3 text-muted-foreground' />
                        )}
                        {event.type === 'created' && (
                          <Sparkles className='w-3 h-3 text-white' />
                        )}
                      </div>
                      <div className='bg-muted rounded-lg p-4'>
                        <div className='flex items-center justify-between mb-1'>
                          <h4 className='font-medium'>{event.title}</h4>
                          <span className='text-xs text-muted-foreground'>
                            {formatDateTime(event.timestamp)}
                          </span>
                        </div>
                        {event.description && (
                          <p className='text-sm text-muted-foreground'>
                            {event.description}
                          </p>
                        )}
                        <p className='text-xs text-muted-foreground mt-2'>
                          by {event.user}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Competitors Tab */}
        <TabsContent value='competitors' className='mt-6'>
          <Card>
            <CardHeader className='flex flex-row items-center justify-between'>
              <h3 className='font-semibold'>Competitive Analysis</h3>
              <Button>
                <Plus className='w-4 h-4 mr-2' />
                Add Competitor
              </Button>
            </CardHeader>
            <CardContent>
              {MOCK_COMPETITORS.length > 0 ? (
                <div className='grid grid-cols-1 md:grid-cols-3 gap-4'>
                  {MOCK_COMPETITORS.map((competitor, index) => (
                    <div key={index} className='p-4 bg-muted rounded-lg'>
                      <h4 className='font-semibold mb-3'>{competitor.name}</h4>
                      <div className='space-y-2'>
                        <div>
                          <div className='flex items-center gap-2 text-sm font-medium text-green-600 dark:text-green-400'>
                            <ArrowUpRight className='w-4 h-4' />
                            Strength
                          </div>
                          <p className='text-sm text-muted-foreground'>
                            {competitor.strength}
                          </p>
                        </div>
                        <div>
                          <div className='flex items-center gap-2 text-sm font-medium text-red-600 dark:text-red-400'>
                            <ArrowDownRight className='w-4 h-4' />
                            Weakness
                          </div>
                          <p className='text-sm text-muted-foreground'>
                            {competitor.weakness}
                          </p>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className='text-center py-8'>
                  <Users className='w-12 h-12 mx-auto mb-4 text-muted-foreground' />
                  <p className='text-muted-foreground mb-4'>
                    No competitors added yet.
                  </p>
                  <Button>Add Competitor</Button>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      {/* Change Stage Dialog */}
      <Dialog open={isStageDialogOpen} onOpenChange={setIsStageDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Change Pipeline Stage</DialogTitle>
            <DialogDescription>
              Move this opportunity to a different stage in the pipeline.
            </DialogDescription>
          </DialogHeader>
          <div className='py-4'>
            <Select
              value={selectedStage}
              onValueChange={(v) => setSelectedStage(v as OpportunityStage)}
            >
              <SelectTrigger>
                <SelectValue placeholder='Select new stage' />
              </SelectTrigger>
              <SelectContent>
                {PIPELINE_STAGES.map((stage) => (
                  <SelectItem
                    key={stage.stage}
                    value={stage.stage}
                    disabled={stage.stage === opportunity.stage}
                  >
                    <div className='flex items-center gap-2'>
                      <div
                        className={cn('w-3 h-3 rounded-full', stage.bgColor)}
                      />
                      {stage.label}
                      {stage.stage === opportunity.stage && ' (Current)'}
                    </div>
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <DialogFooter>
            <Button
              variant='outline'
              onClick={() => setIsStageDialogOpen(false)}
            >
              Cancel
            </Button>
            <Button onClick={handleStageChange} disabled={!selectedStage}>
              Update Stage
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Add Note Dialog */}
      <Dialog open={isNoteDialogOpen} onOpenChange={setIsNoteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Add Note</DialogTitle>
            <DialogDescription>
              Add a note to this opportunity for future reference.
            </DialogDescription>
          </DialogHeader>
          <div className='py-4'>
            <Textarea
              placeholder='Enter your note...'
              value={noteText}
              onChange={(e) => setNoteText(e.target.value)}
              rows={4}
            />
          </div>
          <DialogFooter>
            <Button
              variant='outline'
              onClick={() => setIsNoteDialogOpen(false)}
            >
              Cancel
            </Button>
            <Button onClick={handleAddNote} disabled={!noteText.trim()}>
              Add Note
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Mark as Lost Dialog */}
      <Dialog open={isLostDialogOpen} onOpenChange={setIsLostDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Mark as Lost</DialogTitle>
            <DialogDescription>
              Please provide a reason for losing this deal. This helps improve
              future sales strategies.
            </DialogDescription>
          </DialogHeader>
          <div className='py-4'>
            <Select value={lostReason} onValueChange={setLostReason}>
              <SelectTrigger>
                <SelectValue placeholder='Select reason' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='price'>Price too high</SelectItem>
                <SelectItem value='competitor'>Lost to competitor</SelectItem>
                <SelectItem value='no_budget'>No budget</SelectItem>
                <SelectItem value='no_decision'>No decision made</SelectItem>
                <SelectItem value='timing'>Bad timing</SelectItem>
                <SelectItem value='requirements'>
                  Requirements changed
                </SelectItem>
                <SelectItem value='other'>Other</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <DialogFooter>
            <Button
              variant='outline'
              onClick={() => setIsLostDialogOpen(false)}
            >
              Cancel
            </Button>
            <Button
              variant='destructive'
              onClick={handleMarkAsLost}
              disabled={!lostReason}
            >
              Mark as Lost
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Delete Opportunity</DialogTitle>
            <DialogDescription>
              Are you sure you want to delete this opportunity? This action
              cannot be undone.
            </DialogDescription>
          </DialogHeader>
          <div className='py-4'>
            <div className='p-4 bg-red-50 dark:bg-red-950/50 rounded-lg border border-red-200 dark:border-red-800'>
              <p className='font-medium text-red-700 dark:text-red-300'>
                {opportunity.name}
              </p>
              <p className='text-sm text-red-600 dark:text-red-400'>
                Value: {formatCurrency(opportunity.value)}
              </p>
            </div>
          </div>
          <DialogFooter>
            <Button
              variant='outline'
              onClick={() => setIsDeleteDialogOpen(false)}
            >
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
