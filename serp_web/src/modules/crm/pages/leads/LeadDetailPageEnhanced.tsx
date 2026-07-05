/**
 * Lead Detail Page Component - Enhanced UX/UI 3-Column Version
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Detailed lead view with conversion flow
 */

'use client';

import { useMemo, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { ArrowLeft, Trash2, Edit, MoreHorizontal, AlertCircle, RefreshCw } from 'lucide-react';
import { getErrorMessage } from '@/lib/store/api';
import {
  Button,
  Progress,
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
  Textarea,
  Input,
  Label,
} from '@/shared/components/ui';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/shared/components/ui';
import { toast } from 'sonner';

// Custom API imports
import {
  useDeleteLeadMutation,
  useGetLeadActivitiesQuery,
  useGetLeadQuery,
  useUpdateLeadStatusMutation,
  useUpdateLeadMutation,
  useAssignLeadMutation,
  useGetCrmNotesQuery,
  useCreateCrmNoteMutation,
  useCreateActivityMutation,
} from '../../api/crmApi';

import { useGetOrganizationUsersQuery } from '@/modules/settings/services/users/usersApi';
import { useGetMyModulesQuery } from '@/modules/account/services/moduleApi';
import { selectOrganizationId } from '@/modules/account/store';
import { useAppSelector } from '@/shared/hooks';

// Sub-components
import { LeadProfileSidebar } from './components/detail/LeadProfileSidebar';
import { QuickComposer } from './components/detail/QuickComposer';
import { UnifiedTimeline } from './components/detail/UnifiedTimeline';
import { InsightsSidebar } from './components/detail/InsightsSidebar';
import { RequestMeetingDialog } from '../../components/meeting-requests';
import type { LeadStatus, BackendActivityType } from '../../types';

interface LeadDetailPageProps {
  leadId: string;
}

export function LeadDetailPage({ leadId }: LeadDetailPageProps) {
  const router = useRouter();

  // Dialog trigger states
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [showConvertDialog, setShowConvertDialog] = useState(false);
  const [showQualifyDialog, setShowQualifyDialog] = useState(false);
  const [showDisqualifyDialog, setShowDisqualifyDialog] = useState(false);
  const [showMeetingRequestDialog, setShowMeetingRequestDialog] = useState(false);

  // Dialog note fields
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

  // API Hooks (RSC Waterfalls eliminated - executing parallel requests via RTK Query)
  const { data: leadResponse, isLoading: isLoadingLead } = useGetLeadQuery(leadId);
  const { data: activitiesResponse, isLoading: isLoadingActivities } = useGetLeadActivitiesQuery({ leadId, page: 1, size: 50 });
  const { data: notesResponse, isLoading: isLoadingNotes } = useGetCrmNotesQuery({ entityType: 'LEAD', entityId: leadId });

  // Organization users fetching logic for real note/activity creator mapping
  const organizationId = useAppSelector(selectOrganizationId);
  const { data: myModules } = useGetMyModulesQuery(undefined, {
    skip: !organizationId,
  });
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

  const [deleteLead] = useDeleteLeadMutation();
  const [updateLeadStatus] = useUpdateLeadStatusMutation();
  const [updateLead, { isLoading: isUpdatingLead }] = useUpdateLeadMutation();
  const [assignLead] = useAssignLeadMutation();
  const [createNote] = useCreateCrmNoteMutation();
  const [createActivity] = useCreateActivityMutation();

  const lead = leadResponse?.data;
  const activities = activitiesResponse?.data.data || [];
  const notes = notesResponse?.data.data || [];
  const users = orgUsersResponse?.data?.items ?? [];

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

  // Calculate lead status progress
  const statusProgress = useMemo(() => {
    if (!lead) return 0;
    const statusMap: Record<LeadStatus, number> = {
      NEW: 25,
      CONTACTED: 50,
      NURTURING: 50,
      QUALIFIED: 75,
      CONVERTED: 100,
      DISQUALIFIED: 0,
      LOST: 0,
    };
    return statusMap[lead.leadStatus || 'NEW'] || 0;
  }, [lead]);

  // Calculate score using Vercel best practices (derived inline from static details)
  const leadScore = useMemo(() => {
    if (!lead) return 0;
    if (typeof lead.leadScore === 'number') return lead.leadScore;

    let score = 0;
    if (lead.email) score += 20;
    if (lead.phone) score += 15;
    if (lead.company) score += 20;
    if (lead.estimatedValue && lead.estimatedValue > 0) score += 20;
    if (lead.leadStatus === 'QUALIFIED') score += 25;
    else if (lead.leadStatus === 'CONTACTED' || lead.leadStatus === 'NURTURING') score += 15;
    else if (lead.leadStatus === 'NEW') score += 5;

    return Math.min(score, 100);
  }, [lead]);

  // Mutations wrapper logic
  const handleUpdateLeadField = async (data: any) => {
    try {
      await updateLead({ id: leadId, data }).unwrap();
      toast.success('Lead updated successfully');
    } catch (error) {
      toast.error('Failed to update lead', { description: getErrorMessage(error) });
      throw error;
    }
  };

  const handleAssignLead = async (assignedToId: number) => {
    try {
      await assignLead({ id: leadId, data: { assignedTo: assignedToId } }).unwrap();
      toast.success('Lead reassigned successfully');
    } catch (error) {
      toast.error('Failed to assign lead', { description: getErrorMessage(error) });
    }
  };

  const handleAddNote = async (content: string) => {
    console.log("LeadDetailPage: handleAddNote called. LeadId:", leadId, "Number(LeadId):", Number(leadId), "Content:", content);
    try {
      console.log("LeadDetailPage: Calling createNote mutation...");
      const res = await createNote({ entityType: 'LEAD', entityId: Number(leadId), content }).unwrap();
      console.log("LeadDetailPage: createNote mutation completed. Result:", res);
      toast.success('Note added successfully');
    } catch (error) {
      console.error("LeadDetailPage: createNote mutation failed. Error:", error);
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
        leadId: Number(leadId),
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

  const handleDelete = async () => {
    try {
      await deleteLead(leadId).unwrap();
      toast.success('Delete lead successfully');
      router.push('/crm/leads');
    } catch (error) {
      toast.error('Failed to delete lead', { description: getErrorMessage(error) });
    }
  };

  const handleQualify = async () => {
    try {
      const result = await updateLeadStatus({
        id: leadId,
        data: {
          fromStatus: lead?.leadStatus,
          toStatus: 'QUALIFIED',
          notes: qualifyNotes.trim(),
        },
      }).unwrap();
      toast.success(result.data.message || 'Qualify lead successfully');
      setShowQualifyDialog(false);
      setQualifyNotes('');
    } catch (error) {
      toast.error('Failed to qualify lead', { description: getErrorMessage(error) });
    }
  };

  const handleDisqualify = async () => {
    try {
      const result = await updateLeadStatus({
        id: leadId,
        data: {
          fromStatus: lead?.leadStatus,
          toStatus: 'DISQUALIFIED',
          notes: disqualifyNotes.trim(),
        },
      }).unwrap();
      toast.success(result.data.message || 'Disqualify lead successfully');
      setShowDisqualifyDialog(false);
      setDisqualifyNotes('');
    } catch (error) {
      toast.error('Failed to disqualify lead', { description: getErrorMessage(error) });
    }
  };

  const handleConvert = async () => {
    try {
      const result = await updateLeadStatus({
        id: leadId,
        data: {
          fromStatus: lead?.leadStatus,
          toStatus: 'CONVERTED',
          conversion: {
            createAccount: convertForm.createAccount,
            createOpportunity: convertForm.createOpportunity,
            existingAccountId: convertForm.existingAccountId ? Number(convertForm.existingAccountId) : undefined,
            accountData: convertForm.createAccount ? {
              name: convertForm.accountName || lead?.company || lead?.name,
              notes: convertForm.accountNotes || lead?.notes,
            } : undefined,
            opportunityData: convertForm.createOpportunity ? {
              name: convertForm.opportunityName || `Opportunity from ${lead?.name}`,
              amount: convertForm.opportunityAmount ? Number(convertForm.opportunityAmount) : lead?.estimatedValue,
              notes: convertForm.opportunityNotes || lead?.notes,
            } : undefined,
          },
        },
      }).unwrap();
      toast.success(result.data.message || 'Convert lead successfully');
      setShowConvertDialog(false);
      if (result.data.accountId) {
        router.push(`/crm/accounts/${result.data.accountId}`);
      }
    } catch (error) {
      toast.error('Failed to convert lead', { description: getErrorMessage(error) });
    }
  };

  if (isLoadingLead) {
    return (
      <div className="flex h-[60vh] items-center justify-center gap-2">
        <RefreshCw className="h-6 w-6 animate-spin text-primary" /> Loading lead details...
      </div>
    );
  }

  if (!lead) {
    return (
      <div className="flex h-[60vh] flex-col items-center justify-center">
        <AlertCircle className="mb-4 h-16 w-16 text-muted-foreground" />
        <h2 className="mb-2 text-xl font-semibold text-foreground">Lead not found</h2>
        <p className="mb-4 text-muted-foreground">This lead does not exist or has been deleted.</p>
        <Button asChild>
          <Link href="/crm/leads">
            <ArrowLeft className="mr-2 h-4 w-4" /> Back to lead list
          </Link>
        </Button>
      </div>
    );
  }

  const linkedAccountId = lead.convertedAccountId?.trim() || lead.convertedToCustomerId?.trim() || undefined;

  return (
    <div className="space-y-6">
      {/* Header Action Bar */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between pb-4 border-b border-muted/50">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" asChild className="rounded-full">
            <Link href="/crm/leads">
              <ArrowLeft className="h-5 w-5" />
            </Link>
          </Button>
          <div>
            <div className="text-xs text-muted-foreground uppercase tracking-wider font-semibold">CRM Lead Workspace</div>
            <h1 className="text-2xl font-extrabold text-foreground tracking-tight">{lead.name}</h1>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" asChild>
            <Link href={`/crm/leads/${leadId}/edit`}>
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
              <DropdownMenuItem className="text-rose-600 focus:text-rose-600 focus:bg-rose-50" onClick={() => setShowDeleteDialog(true)}>
                <Trash2 className="mr-2 h-4 w-4" /> Delete Lead
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>

      {/* Minimal flat pipeline progress bar */}
      <div className="space-y-1">
        <div className="flex justify-between text-xs text-muted-foreground font-semibold">
          <span>Pipeline Progression</span>
          <span>{lead.leadStatus} ({statusProgress}%)</span>
        </div>
        <Progress value={statusProgress} className="h-1.5 bg-muted/60" />
      </div>

      {/* Main 3-Column Responsive Grid */}
      <div className="grid gap-6 grid-cols-1 lg:grid-cols-4">
        {/* Column 1: Profile Sidebar */}
        <div className="lg:col-span-1 border border-muted/50 bg-card rounded-2xl p-5 shadow-sm self-start">
          <LeadProfileSidebar lead={lead} isUpdating={isUpdatingLead} onUpdateLead={handleUpdateLeadField} />
        </div>

        {/* Column 2 & 3: Interaction Timeline Hub */}
        <div className="lg:col-span-2 space-y-6">
          <QuickComposer leadId={leadId} onAddNote={handleAddNote} onAddActivity={handleAddActivity} />
          <UnifiedTimeline activities={activities} notes={notes} isLoading={isLoadingActivities || isLoadingNotes} getUserName={getUserName} />
        </div>

        {/* Column 4: Insights & Conversion Hub */}
        <div className="lg:col-span-1 space-y-6">
          <InsightsSidebar
            lead={lead}
            leadScore={leadScore}
            onAssignLead={handleAssignLead}
            onOpenConvert={() => setShowConvertDialog(true)}
            onOpenQualify={() => setShowQualifyDialog(true)}
            onOpenDisqualify={() => setShowDisqualifyDialog(true)}
            onOpenMeetingRequest={() => setShowMeetingRequestDialog(true)}
            users={users}
            getUserName={getUserName}
          />
        </div>
      </div>

      {/* Dialog: Delete */}
      <Dialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Confirm Lead Deletion</DialogTitle>
            <DialogDescription>
              Are you sure you want to delete lead &quot;{lead.name}&quot;? This action cannot be undone.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowDeleteDialog(false)}>Cancel</Button>
            <Button variant="destructive" onClick={handleDelete}>Delete lead</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Dialog: Qualify */}
      <Dialog open={showQualifyDialog} onOpenChange={setShowQualifyDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Qualify lead</DialogTitle>
            <DialogDescription>Add qualification notes for this lead.</DialogDescription>
          </DialogHeader>
          <Textarea rows={4} value={qualifyNotes} onChange={(e) => setQualifyNotes(e.target.value)} />
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowQualifyDialog(false)}>Cancel</Button>
            <Button onClick={handleQualify} disabled={!qualifyNotes.trim()}>Qualify</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Dialog: Disqualify */}
      <Dialog open={showDisqualifyDialog} onOpenChange={setShowDisqualifyDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Disqualify lead</DialogTitle>
            <DialogDescription>Add disqualification notes for this lead.</DialogDescription>
          </DialogHeader>
          <Textarea rows={4} value={disqualifyNotes} onChange={(e) => setDisqualifyNotes(e.target.value)} />
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowDisqualifyDialog(false)}>Cancel</Button>
            <Button onClick={handleDisqualify} disabled={!disqualifyNotes.trim()}>Disqualify</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Dialog: Convert */}
      <Dialog open={showConvertDialog} onOpenChange={setShowConvertDialog}>
        <DialogContent className="w-[95vw] max-w-2xl">
          <DialogHeader>
            <DialogTitle>Convert lead</DialogTitle>
            <DialogDescription>Create account/opportunity from this lead.</DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="accountName">Account Name</Label>
                <Input
                  id="accountName"
                  value={convertForm.accountName}
                  onChange={(e) => setConvertForm((prev) => ({ ...prev, accountName: e.target.value }))}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="existingAccountId">Existing Account ID</Label>
                <Input
                  id="existingAccountId"
                  value={convertForm.existingAccountId}
                  onChange={(e) => setConvertForm((prev) => ({ ...prev, existingAccountId: e.target.value }))}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="opportunityName">Opportunity Name</Label>
                <Input
                  id="opportunityName"
                  value={convertForm.opportunityName}
                  onChange={(e) => setConvertForm((prev) => ({ ...prev, opportunityName: e.target.value }))}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="opportunityAmount">Opportunity Amount</Label>
                <Input
                  id="opportunityAmount"
                  type="number"
                  value={convertForm.opportunityAmount}
                  onChange={(e) => setConvertForm((prev) => ({ ...prev, opportunityAmount: e.target.value }))}
                />
              </div>
            </div>
            <div className="space-y-2">
              <Label htmlFor="accountNotes">Account Notes</Label>
              <Textarea
                id="accountNotes"
                rows={3}
                value={convertForm.accountNotes}
                onChange={(e) => setConvertForm((prev) => ({ ...prev, accountNotes: e.target.value }))}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="opportunityNotes">Opportunity Notes</Label>
              <Textarea
                id="opportunityNotes"
                rows={3}
                value={convertForm.opportunityNotes}
                onChange={(e) => setConvertForm((prev) => ({ ...prev, opportunityNotes: e.target.value }))}
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowConvertDialog(false)}>Cancel</Button>
            <Button onClick={handleConvert}>Convert</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Dialog: Local Meeting Request */}
      {linkedAccountId && (
        <RequestMeetingDialog
          open={showMeetingRequestDialog}
          onOpenChange={setShowMeetingRequestDialog}
          accountId={linkedAccountId}
          accountName={lead.company || lead.name}
        />
      )}
    </div>
  );
}

export default LeadDetailPage;
