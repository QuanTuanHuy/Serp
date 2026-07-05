/*
Author: QuanTuanHuy
Description: Part of Serp Project - Redesigned Account detail page backed by CRM APIs
*/

'use client';

import { useState, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import { getErrorMessage } from '@/lib/store/api';
import {
  Card,
  CardContent,
  CardHeader,
  Button,
  Badge,
  Input,
  Label,
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
} from '@/shared/components/ui';
import {
  ArrowLeft,
  Edit,
  Star,
  AlertCircle,
  Building2,
  CheckCircle2,
  DollarSign,
  CreditCard,
} from 'lucide-react';
import { toast } from 'sonner';
import { cn } from '@/shared/utils';

// Shared and sub-components
import { ContactList } from '../../components/contacts';
import type { ContactFormData } from '../../components/contacts';
import { RequestMeetingDialog } from '../../components/meeting-requests';

// Sub-components for 3-column layout
import { AccountProfileSidebar } from './components/detail/AccountProfileSidebar';
import { QuickComposer } from './components/detail/QuickComposer';
import { UnifiedTimeline } from './components/detail/UnifiedTimeline';
import { OpportunityDealList } from './components/detail/OpportunityDealList';
import { AccountInsightsSidebar } from './components/detail/AccountInsightsSidebar';

// APIs & Store
import {
  useActivateAccountMutation,
  useCreateAccountContactMutation,
  useDeactivateAccountMutation,
  useDeleteAccountMutation,
  useDeleteContactMutation,
  useGetAccountActivitiesQuery,
  useGetAccountContactsQuery,
  useGetAccountQuery,
  useSetPrimaryContactMutation,
  useUpdateAccountCreditLimitMutation,
  useUpdateContactMutation,
  useUpdateAccountMutation,
  useGetCrmNotesQuery,
  useCreateCrmNoteMutation,
  useCreateActivityMutation,
  useGetOpportunitiesQuery,
} from '../../api/crmApi';
import { useGetOrganizationUsersQuery } from '@/modules/settings/services/users/usersApi';
import { useGetMyModulesQuery } from '@/modules/account/services/moduleApi';
import { selectOrganizationId } from '@/modules/account/store';
import { useAppSelector } from '@/shared/hooks';

import { ACCOUNT_TIERS } from '../../types/constants';
import type { Contact, BackendActivityType } from '../../types';
import { formatCurrency } from '../../utils';

interface CustomerDetailPageEnhancedProps {
  customerId: string;
  className?: string;
}

const STATUS_CONFIG = {
  ACTIVE: {
    label: 'Active',
    color: 'text-green-700 dark:text-green-300',
    bgColor: 'bg-green-100 dark:bg-green-900/50',
  },
  INACTIVE: {
    label: 'Inactive',
    color: 'text-gray-700 dark:text-gray-300',
    bgColor: 'bg-gray-100 dark:bg-gray-800',
  },
};

export const CustomerDetailPageEnhanced: React.FC<CustomerDetailPageEnhancedProps> = ({
  customerId,
  className,
}) => {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState('overview');
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [isCreditLimitDialogOpen, setIsCreditLimitDialogOpen] = useState(false);
  const [creditLimitInput, setCreditLimitInput] = useState('');
  const [meetingRequestOpen, setMeetingRequestOpen] = useState(false);

  // Parallel data fetching to eliminate waterfalls
  const { data: accountResponse, isLoading: isAccountLoading } = useGetAccountQuery(customerId);
  const { data: contactsResponse } = useGetAccountContactsQuery(customerId);
  const { data: activitiesResponse, isLoading: isLoadingActivities } = useGetAccountActivitiesQuery({
    accountId: customerId,
    page: 1,
    size: 50,
  });
  const { data: notesResponse, isLoading: isLoadingNotes } = useGetCrmNotesQuery({
    entityType: 'ACCOUNT',
    entityId: customerId,
  });
  const { data: opportunitiesResponse, isLoading: isLoadingOpps } = useGetOpportunitiesQuery({
    filters: { customerId: [customerId] },
    pagination: { page: 1, limit: 50 },
  });

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

  const [updateAccount, { isLoading: isUpdatingAccount }] = useUpdateAccountMutation();
  const [createContact] = useCreateAccountContactMutation();
  const [updateContact] = useUpdateContactMutation();
  const [deleteContact] = useDeleteContactMutation();
  const [setPrimaryContact] = useSetPrimaryContactMutation();
  const [deleteAccount] = useDeleteAccountMutation();
  const [activateAccount, { isLoading: isActivating }] = useActivateAccountMutation();
  const [deactivateAccount, { isLoading: isDeactivating }] = useDeactivateAccountMutation();
  const [updateCreditLimit, { isLoading: isUpdatingCreditLimit }] = useUpdateAccountCreditLimitMutation();
  const [createCrmNote] = useCreateCrmNoteMutation();
  const [createActivity] = useCreateActivityMutation();

  const customer = accountResponse?.data;
  const accountContacts = contactsResponse?.data ?? [];
  const relatedActivities = activitiesResponse?.data?.data ?? [];
  const notes = notesResponse?.data?.data || [];
  const opportunities = opportunitiesResponse?.data?.data || [];
  const primaryContact = accountContacts.find((contact) => contact.isPrimary);
  const users = orgUsersResponse?.data?.items || [];

  const getUserName = useMemo(() => {
    return (userId?: string | number) => {
      if (!userId) return 'System';
      const user = users.find((u) => String(u.id) === String(userId));
      if (!user) return `User #${userId}`;
      const name = [user.firstName, user.lastName].filter(Boolean).join(' ');
      return name || user.email;
    };
  }, [users]);

  if (isAccountLoading) {
    return <div className="text-center py-16 text-muted-foreground text-sm">Loading workspace...</div>;
  }

  if (!customer) {
    return (
      <div className={cn('p-6', className)}>
        <Card>
          <CardContent className="py-16 text-center">
            <AlertCircle className="mx-auto mb-4 h-12 w-12 text-muted-foreground" />
            <h2 className="mb-2 text-xl font-semibold">Account Not Found</h2>
            <p className="mb-4 text-muted-foreground">The account you&apos;re looking for doesn&apos;t exist or has been deleted.</p>
            <Button onClick={() => router.push('/crm/accounts')}>Back to Accounts</Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  const statusConfig = STATUS_CONFIG[customer.status] ?? STATUS_CONFIG.ACTIVE;
  const tierLabel = customer.tier
    ? (ACCOUNT_TIERS.find((tier) => tier.value === customer.tier)?.label ?? customer.tier)
    : null;

  const handleUpdateAccountField = async (data: any) => {
    try {
      await updateAccount({ id: customerId, data }).unwrap();
      toast.success('Account updated successfully');
    } catch (error) {
      toast.error('Failed to update account', { description: getErrorMessage(error) });
      throw error;
    }
  };

  const handleDelete = () => {
    deleteAccount(customerId)
      .unwrap()
      .then(() => {
        toast.success('Delete account successfully');
        router.push('/crm/accounts');
      })
      .catch((error) => {
        toast.error('Failed to delete account', { description: getErrorMessage(error) });
      });
  };

  const openCreditLimitDialog = () => {
    setCreditLimitInput(customer.creditLimit != null ? String(customer.creditLimit) : '');
    setIsCreditLimitDialogOpen(true);
  };

  const handleSaveCreditLimit = async () => {
    const creditLimit = Number(creditLimitInput);
    if (!Number.isFinite(creditLimit) || creditLimit < 0) {
      toast.error('Enter a valid credit limit');
      return;
    }

    try {
      await updateCreditLimit({ id: customerId, creditLimit }).unwrap();
      toast.success('Credit limit updated');
      setIsCreditLimitDialogOpen(false);
    } catch (error) {
      toast.error('Failed to update credit limit', { description: getErrorMessage(error) });
    }
  };

  const handleActivate = async () => {
    try {
      await activateAccount(customerId).unwrap();
      toast.success('Account activated');
    } catch (error) {
      toast.error('Failed to activate account', { description: getErrorMessage(error) });
    }
  };

  const handleDeactivate = async () => {
    try {
      await deactivateAccount(customerId).unwrap();
      toast.success('Account deactivated');
    } catch (error) {
      toast.error('Failed to deactivate account', { description: getErrorMessage(error) });
    }
  };

  const handleAddNote = async (content: string) => {
    try {
      await createCrmNote({ entityType: 'ACCOUNT', entityId: Number(customerId), content }).unwrap();
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
        accountId: Number(customerId),
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

  const handleAddContact = async (data: ContactFormData) => {
    try {
      await createContact({
        accountId: customerId,
        data: {
          name: data.name.trim(),
          email: data.email,
          phone: data.phone,
          jobPosition: data.jobPosition,
          isPrimary: data.isPrimary,
          street: data.street,
          city: data.city,
          state: data.state,
          zipCode: data.zipCode,
          country: data.country,
          contactType: data.contactType,
          activeStatus: data.activeStatus,
          linkedInUrl: data.linkedInUrl,
          twitterHandle: data.twitterHandle,
          notes: data.notes,
        },
      }).unwrap();
      toast.success('Create contact successfully');
    } catch (error) {
      toast.error('Failed to create contact', { description: getErrorMessage(error) });
    }
  };

  const handleEditContact = async (contact: Contact, data: ContactFormData) => {
    try {
      await updateContact({
        id: contact.id,
        data: {
          name: data.name.trim(),
          email: data.email,
          phone: data.phone,
          jobPosition: data.jobPosition,
          street: data.street,
          city: data.city,
          state: data.state,
          zipCode: data.zipCode,
          country: data.country,
          contactType: data.contactType,
          activeStatus: data.activeStatus,
          linkedInUrl: data.linkedInUrl,
          twitterHandle: data.twitterHandle,
          notes: data.notes,
        },
      }).unwrap();
      toast.success('Update contact successfully');
    } catch (error) {
      toast.error('Failed to update contact', { description: getErrorMessage(error) });
    }
  };

  const handleDeleteContact = async (contact: Contact) => {
    try {
      await deleteContact(contact.id).unwrap();
      toast.success('Delete contact successfully');
    } catch (error) {
      toast.error('Failed to delete contact', { description: getErrorMessage(error) });
    }
  };

  const handleSetPrimaryContact = async (contact: Contact) => {
    try {
      await setPrimaryContact(contact.id).unwrap();
      toast.success('Primary contact updated successfully');
    } catch (error) {
      toast.error('Failed to update primary contact', { description: getErrorMessage(error) });
    }
  };

  return (
    <div className={cn('space-y-6 p-6', className)}>
      {/* Header bar */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between pb-4 border-b border-muted/50">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" onClick={() => router.push('/crm/accounts')} className="rounded-full">
            <ArrowLeft className="h-5 w-5" />
          </Button>
          <div>
            <div className="text-xs text-muted-foreground uppercase tracking-wider font-semibold">CRM Account Workspace</div>
            <div className="flex items-center gap-3 flex-wrap">
              <h1 className="text-2xl font-extrabold text-foreground tracking-tight">{customer.name}</h1>
              <Badge className={cn(statusConfig.bgColor, statusConfig.color)}>{statusConfig.label}</Badge>
              {tierLabel && <Badge variant="outline">{tierLabel}</Badge>}
              {customer.tags.includes('VIP') && (
                <Badge className="bg-yellow-100 text-yellow-700 dark:bg-yellow-900/50 dark:text-yellow-300">
                  <Star className="mr-1 h-3 w-3 fill-current animate-pulse" /> VIP
                </Badge>
              )}
            </div>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" onClick={() => router.push(`/crm/accounts/${customerId}/edit`)}>
            <Edit className="mr-1.5 h-4 w-4" /> Edit Profile
          </Button>
        </div>
      </div>

      {/* Metric Cards strip */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <AccountMetricCard label="Total Value" value={formatCurrency(customer.totalValue || 0)} icon={DollarSign} tone="green" />
        <AccountMetricCard label="Opportunities" value={customer.totalOpportunities ?? 0} icon={Building2} tone="blue" />
        <AccountMetricCard label="Won Opportunities" value={customer.wonOpportunities ?? 0} icon={CheckCircle2} tone="purple" />
        <AccountMetricCard label="Credit Limit" value={customer.creditLimit != null ? formatCurrency(Number(customer.creditLimit)) : 'N/A'} icon={CreditCard} tone="orange" />
      </div>

      {/* Main 3-Column Layout */}
      <div className="grid gap-6 grid-cols-1 lg:grid-cols-4">
        {/* Column 1: Profile Sidebar */}
        <div className="lg:col-span-1 border border-muted/50 bg-card rounded-2xl p-5 shadow-sm self-start">
          <AccountProfileSidebar customer={customer} isUpdating={isUpdatingAccount} onUpdateAccount={handleUpdateAccountField} />
        </div>

        {/* Column 2 & 3: Interaction Hub */}
        <div className="lg:col-span-2 space-y-6">
          <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
            <TabsList className="grid w-full grid-cols-3">
              <TabsTrigger value="overview">Timeline</TabsTrigger>
              <TabsTrigger value="contacts">Contacts ({accountContacts.length})</TabsTrigger>
              <TabsTrigger value="opportunities">Opportunities ({opportunities.length})</TabsTrigger>
            </TabsList>

            <TabsContent value="overview" className="space-y-6 mt-6">
              <QuickComposer onAddNote={handleAddNote} onAddActivity={handleAddActivity} />
              <UnifiedTimeline activities={relatedActivities} notes={notes} isLoading={isLoadingActivities || isLoadingNotes} getUserName={getUserName} />
            </TabsContent>

            <TabsContent value="contacts" className="mt-6">
              <ContactList
                contacts={accountContacts}
                title="Account Contacts"
                emptyMessage="No contacts found for this account"
                onAddContact={handleAddContact}
                onEditContact={handleEditContact}
                onDeleteContact={handleDeleteContact}
                onSetPrimary={handleSetPrimaryContact}
              />
            </TabsContent>

            <TabsContent value="opportunities" className="mt-6">
              <OpportunityDealList opportunities={opportunities} isLoading={isLoadingOpps} />
            </TabsContent>
          </Tabs>
        </div>

        {/* Column 4: Insights & Actions */}
        <div className="lg:col-span-1 space-y-6">
          <AccountInsightsSidebar
            customer={customer}
            primaryContact={primaryContact}
            onOpenCreditLimitDialog={openCreditLimitDialog}
            onOpenMeetingRequest={() => setMeetingRequestOpen(true)}
            onActivate={handleActivate}
            onDeactivate={handleDeactivate}
            onDeleteAccount={() => setIsDeleteDialogOpen(true)}
            isActivating={isActivating}
            isDeactivating={isDeactivating}
          />
        </div>
      </div>

      {/* Dialogs */}
      <Dialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Delete Account</DialogTitle>
            <DialogDescription>Are you sure you want to delete this account? This action cannot be undone.</DialogDescription>
          </DialogHeader>
          <div className="py-4">
            <div className="rounded-lg border border-red-200 bg-red-50 p-4 dark:border-red-800 dark:bg-red-950/50">
              <div className="flex items-center gap-3">
                <Avatar>
                  <AvatarFallback>{customer.name.slice(0, 2).toUpperCase()}</AvatarFallback>
                </Avatar>
                <div>
                  <p className="font-medium text-red-700 dark:text-red-300">{customer.name}</p>
                  <p className="text-sm text-red-600 dark:text-red-400">{customer.email}</p>
                </div>
              </div>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsDeleteDialogOpen(false)}>Cancel</Button>
            <Button variant="destructive" onClick={handleDelete}>Delete Account</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={isCreditLimitDialogOpen} onOpenChange={setIsCreditLimitDialogOpen}>
        <DialogContent className="sm:max-w-[400px]">
          <DialogHeader>
            <DialogTitle>Edit credit limit</DialogTitle>
            <DialogDescription>Set the credit limit for {customer.name}. Use 0 if none.</DialogDescription>
          </DialogHeader>
          <div className="py-2">
            <Label htmlFor="credit-limit-input">Credit limit (VND)</Label>
            <Input
              id="credit-limit-input"
              type="number"
              min={0}
              step={1}
              className="mt-1.5"
              value={creditLimitInput}
              onChange={(event) => setCreditLimitInput(event.target.value)}
            />
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsCreditLimitDialogOpen(false)}>Cancel</Button>
            <Button onClick={handleSaveCreditLimit} disabled={isUpdatingCreditLimit}>
              {isUpdatingCreditLimit ? 'Saving...' : 'Save'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <RequestMeetingDialog open={meetingRequestOpen} onOpenChange={setMeetingRequestOpen} accountId={customerId} accountName={customer.name} />
    </div>
  );
};

interface AccountMetricCardProps {
  label: string;
  value: string | number;
  icon: any;
  tone: 'green' | 'blue' | 'purple' | 'orange';
}

const toneStyles: Record<AccountMetricCardProps['tone'], { card: string; icon: string }> = {
  green: {
    card: 'from-green-50 to-green-100/50 border-green-200 dark:from-green-950 dark:to-green-900/50 dark:border-green-800/50',
    icon: 'bg-green-500/20 text-green-600 dark:text-green-400',
  },
  blue: {
    card: 'from-blue-50 to-blue-100/50 border-blue-200 dark:from-blue-950 dark:to-blue-900/50 dark:border-blue-800/50',
    icon: 'bg-blue-500/20 text-blue-600 dark:text-blue-400',
  },
  purple: {
    card: 'from-purple-50 to-purple-100/50 border-purple-200 dark:from-purple-950 dark:to-purple-900/50 dark:border-purple-800/50',
    icon: 'bg-purple-500/20 text-purple-600 dark:text-purple-400',
  },
  orange: {
    card: 'from-orange-50 to-orange-100/50 border-orange-200 dark:from-orange-950 dark:to-orange-900/50 dark:border-orange-800/50',
    icon: 'bg-orange-500/20 text-orange-600 dark:text-orange-400',
  },
};

const AccountMetricCard: React.FC<AccountMetricCardProps> = ({ label, value, icon: Icon, tone }) => {
  const styles = toneStyles[tone];
  return (
    <Card className={cn('bg-gradient-to-br border border-muted/40 shadow-sm rounded-xl', styles.card)}>
      <CardContent className="py-4">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm text-muted-foreground font-medium">{label}</p>
            <p className="text-2xl font-extrabold">{value}</p>
          </div>
          <div className={cn('rounded-full p-3', styles.icon)}>
            <Icon className="h-6 w-6" />
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

export default CustomerDetailPageEnhanced;
