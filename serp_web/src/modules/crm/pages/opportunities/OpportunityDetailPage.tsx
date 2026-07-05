/*
Author: QuanTuanHuy
Description: Part of Serp Project - Redesigned Opportunity Detail page
*/

'use client';

import { useState, useMemo } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { AlertCircle, ArrowLeft, Edit, MoreHorizontal, Trash2, CheckCircle, RefreshCw } from 'lucide-react';
import {
  Button,
  Card,
  CardContent,
  CardHeader,
  Progress,
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
  Input,
  Label,
  Textarea,
} from '@/shared/components/ui';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { toast } from 'sonner';
import { getErrorMessage } from '@/lib/store/api';

// Dialog components
import {
  ChangeStageDialog,
  ReopenOpportunityDialog,
  DeleteOpportunityDialog,
} from '../../components/dialogs';
import { RequestMeetingDialog } from '../../components/meeting-requests';
import { formatCurrency } from '../../utils';

// Hooks and Mutations
import {
  useGetOpportunityQuery,
  useGetOpportunityActivitiesQuery,
  useUpdateOpportunityMutation,
  useCreateCrmNoteMutation,
  useCreateActivityMutation,
  useGetCrmNotesQuery,
  useGetAccountsQuery,
  useGetLeadsQuery,
} from '../../api/crmApi';
import { useGetOrganizationUsersQuery } from '@/modules/settings/services/users/usersApi';
import { useGetMyModulesQuery } from '@/modules/account/services/moduleApi';
import { selectOrganizationId } from '@/modules/account/store';
import { useAppSelector } from '@/shared/hooks';
import {
  useOpportunityDetail,
  useOpportunityDialogs,
  useOpportunityActions,
} from '../../hooks';

// Redesigned sub-components
import { OpportunityProfileSidebar } from './components/detail/OpportunityProfileSidebar';
import { QuickComposer } from './components/detail/QuickComposer';
import { UnifiedTimeline } from './components/detail/UnifiedTimeline';
import { OpportunityInsightsSidebar } from './components/detail/OpportunityInsightsSidebar';
import type { OpportunityStage, BackendActivityType } from '../../types';

interface OpportunityDetailPageProps {
  opportunityId: string;
  className?: string;
}

function parseOptionalActualValue(raw: string): number | undefined {
  const trimmed = raw.trim();
  if (!trimmed) return undefined;
  const n = Number(trimmed);
  return Number.isFinite(n) ? n : undefined;
}

export const OpportunityDetailPage: React.FC<OpportunityDetailPageProps> = ({
  opportunityId,
  className,
}) => {
  const router = useRouter();
  const [meetingRequestOpen, setMeetingRequestOpen] = useState(false);

  // Parallel data fetching to eliminate waterfalls
  const { data: oppResponse, isLoading: isLoadingOpp } = useGetOpportunityQuery(opportunityId);
  const { data: activitiesResponse, isLoading: isLoadingActivities } = useGetOpportunityActivitiesQuery({ opportunityId, page: 1, size: 50 });
  const { data: notesResponse, isLoading: isLoadingNotes } = useGetCrmNotesQuery({ entityType: 'OPPORTUNITY', entityId: opportunityId });

  // Accounts and Leads queries for left column editing dropdowns
  const { data: accountsResponse } = useGetAccountsQuery({ filters: {}, pagination: { page: 1, limit: 100 } });
  const { data: leadsResponse } = useGetLeadsQuery({ filters: {}, pagination: { page: 1, limit: 100 } });

  // Organization users query for real name resolution & assignment
  const organizationId = useAppSelector(selectOrganizationId);
  const { data: myModules } = useGetMyModulesQuery(undefined, { skip: !organizationId });
  const crmModuleId = myModules?.find((m) => m.moduleCode === 'CRM')?.moduleId;

  const { data: orgUsersResponse } = useGetOrganizationUsersQuery(
    {
      organizationId: organizationId as number,
      page: 0,
      pageSize: 100,
      moduleId: crmModuleId,
    },
    { skip: !organizationId }
  );

  const [updateOpportunity, { isLoading: isUpdating }] = useUpdateOpportunityMutation();
  const [createCrmNote] = useCreateCrmNoteMutation();
  const [createActivity] = useCreateActivityMutation();

  const opportunity = oppResponse?.data;
  const activities = activitiesResponse?.data?.data || [];
  const notes = notesResponse?.data?.data || [];
  const accounts = accountsResponse?.data?.data || [];
  const leads = leadsResponse?.data?.data || [];
  const users = orgUsersResponse?.data?.items || [];

  // Helper hook details
  const {
    stageConfig,
    isClosed,
    canReopen,
    probability,
    estimatedValue,
    weightedValue,
    daysInPipeline,
    daysUntilClose,
  } = useOpportunityDetail(opportunityId);

  const {
    isStageDialogOpen,
    setIsStageDialogOpen,
    isDeleteDialogOpen,
    setIsDeleteDialogOpen,
    isReopenDialogOpen,
    setIsReopenDialogOpen,
    selectedStage,
    setSelectedStage,
    stageNotes,
    setStageNotes,
    lostReason,
    setLostReason,
    wonActualValue,
    setWonActualValue,
    reopenReason,
    setReopenReason,
    reopenStage,
    setReopenStage,
    resetDialogStates,
  } = useOpportunityDialogs();

  const { handleStageChange, handleReopen, handleDelete } = useOpportunityActions(opportunityId);

  // Helper to map userId to display name
  const getUserName = useMemo(() => {
    return (userId?: string | number) => {
      if (!userId) return 'System';
      const user = users.find((u) => String(u.id) === String(userId));
      if (!user) return `User #${userId}`;
      const name = [user.firstName, user.lastName].filter(Boolean).join(' ');
      return name || user.email;
    };
  }, [users]);

  // Handlers
  const handleUpdateOpportunityField = async (data: any) => {
    try {
      await updateOpportunity({ id: opportunityId, data }).unwrap();
      toast.success('Opportunity updated successfully');
    } catch (error) {
      toast.error('Failed to update opportunity', { description: getErrorMessage(error) });
      throw error;
    }
  };

  const handleAssignOpportunity = async (assignedToId: number) => {
    try {
      await updateOpportunity({ id: opportunityId, data: { assignedTo: String(assignedToId) } }).unwrap();
      toast.success('Opportunity reassigned successfully');
    } catch (error) {
      toast.error('Failed to reassign opportunity', { description: getErrorMessage(error) });
    }
  };

  const handleAddNote = async (content: string) => {
    try {
      await createCrmNote({ entityType: 'OPPORTUNITY', entityId: Number(opportunityId), content }).unwrap();
      toast.success('Note added successfully');
    } catch (error) {
      toast.error('Failed to create note', { description: getErrorMessage(error) });
      throw error;
    }
  };

  const handleAddActivity = async (data: any) => {
    try {
      const typeMap: Record<string, BackendActivityType> = {
        CALL: 'CALL',
        EMAIL: 'EMAIL',
        MEETING: 'MEETING',
        OTHER: 'TASK',
      };
      await createActivity({
        opportunityId: Number(opportunityId),
        subject: data.subject,
        activityType: typeMap[data.type] || 'TASK',
        notes: data.notes,
        description: data.notes,
        status: 'COMPLETED',
        activityDate: Date.now(),
      }).unwrap();
      toast.success('Activity logged successfully');
    } catch (error) {
      toast.error('Failed to log activity', { description: getErrorMessage(error) });
      throw error;
    }
  };

  const handleStageClick = (stage: OpportunityStage) => {
    setSelectedStage(stage);
    setIsStageDialogOpen(true);
  };

  if (isLoadingOpp) {
    return <div className="text-center py-16 text-muted-foreground text-sm">Loading workspace...</div>;
  }

  if (!isLoadingOpp && !opportunity) {
    return (
      <div className={cn('p-6', className)}>
        <Card>
          <CardContent className="py-16 text-center">
            <AlertCircle className="mx-auto mb-4 h-12 w-12 text-muted-foreground" />
            <h2 className="mb-2 text-xl font-semibold">Opportunity Not Found</h2>
            <p className="mb-4 text-muted-foreground">The opportunity you&apos;re looking for doesn&apos;t exist or has been deleted.</p>
            <Button asChild>
              <Link href="/crm/opportunities">Back to Opportunities</Link>
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (!opportunity || !stageConfig) return null;

  return (
    <div className={cn('space-y-6 p-6', className)}>
      {/* Header Action Bar */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between pb-4 border-b border-muted/50">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" asChild className="rounded-full">
            <Link href="/crm/opportunities">
              <ArrowLeft className="h-5 w-5" />
            </Link>
          </Button>
          <div>
            <div className="text-xs text-muted-foreground uppercase tracking-wider font-semibold">CRM Opportunity Workspace</div>
            <h1 className="text-2xl font-extrabold text-foreground tracking-tight">{opportunity.name}</h1>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" asChild>
            <Link href={`/crm/opportunities/${opportunityId}/edit`}>
              <Edit className="mr-1.5 h-4 w-4" /> Edit
            </Link>
          </Button>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="outline" size="icon" className="h-9 w-9">
                <MoreHorizontal className="h-4 w-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-40">
              <DropdownMenuItem className="text-rose-600 focus:text-rose-600 focus:bg-rose-50" onClick={() => setIsDeleteDialogOpen(true)}>
                <Trash2 className="mr-2 h-4 w-4" /> Delete Opportunity
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>

      {/* Flat pipeline status progress bar */}
      <div className="space-y-1">
        <div className="flex justify-between text-xs text-muted-foreground font-semibold">
          <span>Deal Progression</span>
          <span>{opportunity.stage} ({probability}%)</span>
        </div>
        <Progress value={probability} className="h-1.5 bg-muted/60" />
      </div>

      {/* Main 3-Column Responsive Grid */}
      <div className="grid gap-6 grid-cols-1 lg:grid-cols-4">
        {/* Column 1: Profile Sidebar */}
        <div className="lg:col-span-1 border border-muted/50 bg-card rounded-2xl p-5 shadow-sm self-start">
          <OpportunityProfileSidebar
            opportunity={opportunity}
            accounts={accounts}
            leads={leads}
            isUpdating={isUpdating}
            onUpdateOpportunity={handleUpdateOpportunityField}
          />
        </div>

        {/* Column 2 & 3: Interaction Hub */}
        <div className="lg:col-span-2 space-y-6">
          <QuickComposer opportunityId={opportunityId} onAddNote={handleAddNote} onAddActivity={handleAddActivity} />
          <UnifiedTimeline activities={activities} notes={notes} isLoading={isLoadingActivities || isLoadingNotes} getUserName={getUserName} />
        </div>

        {/* Column 4: Insights & Conversion Hub */}
        <div className="lg:col-span-1 space-y-6">
          <OpportunityInsightsSidebar
            opportunity={opportunity}
            probability={probability}
            weightedValue={weightedValue}
            daysInPipeline={daysInPipeline}
            daysUntilClose={daysUntilClose}
            users={users}
            getUserName={getUserName}
            onAssignOpportunity={handleAssignOpportunity}
            onChangeStage={handleStageClick}
            onOpenMeetingRequest={() => setMeetingRequestOpen(true)}
            onReopen={() => setIsReopenDialogOpen(true)}
            onDelete={() => setIsDeleteDialogOpen(true)}
          />
        </div>
      </div>

      {/* Dialogs */}
      {opportunity.accountId?.trim() && (
        <RequestMeetingDialog
          open={meetingRequestOpen}
          onOpenChange={setMeetingRequestOpen}
          accountId={opportunity.accountId.trim()}
          accountName={opportunity.customerName}
          opportunityId={opportunityId}
        />
      )}

      <ChangeStageDialog
        open={isStageDialogOpen}
        onOpenChange={setIsStageDialogOpen}
        onSubmit={() => {
          const actualValue =
            selectedStage === 'CLOSED_WON'
              ? parseOptionalActualValue(wonActualValue)
              : undefined;
          handleStageChange(
            {
              stage: selectedStage as any,
              notes: stageNotes || undefined,
              actualValue,
              lossReason:
                selectedStage === 'CLOSED_LOST'
                  ? lostReason || undefined
                  : undefined,
            },
            'Update opportunity stage successfully',
            () => {
              setIsStageDialogOpen(false);
              resetDialogStates();
            }
          );
        }}
        selectedStage={selectedStage}
        onStageChange={setSelectedStage}
        stageNotes={stageNotes}
        onStageNotesChange={setStageNotes}
        lostReason={lostReason}
        onLostReasonChange={setLostReason}
        wonActualValue={wonActualValue}
        onWonActualValueChange={setWonActualValue}
      />

      <ReopenOpportunityDialog
        open={isReopenDialogOpen}
        onOpenChange={setIsReopenDialogOpen}
        onSubmit={() => {
          handleReopen(reopenStage, reopenReason, () => {
            setIsReopenDialogOpen(false);
            resetDialogStates();
          });
        }}
        reopenStage={reopenStage}
        onReopenStageChange={setReopenStage}
        reopenReason={reopenReason}
        onReopenReasonChange={setReopenReason}
      />

      <DeleteOpportunityDialog
        open={isDeleteDialogOpen}
        onOpenChange={setIsDeleteDialogOpen}
        onConfirm={handleDelete}
      />
    </div>
  );
};

export default OpportunityDetailPage;
