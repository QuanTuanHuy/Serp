/*
Author: QuanTuanHuy
Description: Part of Serp Project - Account detail page backed by CRM APIs
*/

'use client';

import { useState } from 'react';
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
  Building2,
  Clock,
  Phone,
  Mail,
  Globe,
  MapPin,
  Users,
  Star,
  AlertCircle,
  CheckCircle2,
  CreditCard,
  Tag,
  type LucideIcon,
} from 'lucide-react';
import { toast } from 'sonner';
import { cn } from '@/shared/utils';
import { ContactList } from '../../components/contacts';
import type { ContactFormData } from '../../components/contacts';
import { RequestMeetingDialog } from '../../components/meeting-requests';
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
} from '../../api/crmApi';
import {
  ACCOUNT_TIERS,
  PREFERRED_DAYS,
  PREFERRED_TIME_SLOTS,
} from '../../types/constants';
import type { Activity, Contact } from '../../types';

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

interface CustomerDetailPageEnhancedProps {
  customerId: string;
  className?: string;
}

export const CustomerDetailPageEnhanced: React.FC<
  CustomerDetailPageEnhancedProps
> = ({ customerId, className }) => {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState('overview');
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [isCreditLimitDialogOpen, setIsCreditLimitDialogOpen] = useState(false);
  const [creditLimitInput, setCreditLimitInput] = useState('');
  const [meetingRequestOpen, setMeetingRequestOpen] = useState(false);

  const { data: accountResponse, isLoading: isAccountLoading } =
    useGetAccountQuery(customerId);
  const { data: contactsResponse } = useGetAccountContactsQuery(customerId);
  const { data: activitiesResponse } = useGetAccountActivitiesQuery({
    accountId: customerId,
    page: 1,
    size: 20,
  });
  const [createContact] = useCreateAccountContactMutation();
  const [updateContact] = useUpdateContactMutation();
  const [deleteContact] = useDeleteContactMutation();
  const [setPrimaryContact] = useSetPrimaryContactMutation();
  const [deleteAccount] = useDeleteAccountMutation();
  const [activateAccount, { isLoading: isActivating }] =
    useActivateAccountMutation();
  const [deactivateAccount, { isLoading: isDeactivating }] =
    useDeactivateAccountMutation();
  const [updateCreditLimit, { isLoading: isUpdatingCreditLimit }] =
    useUpdateAccountCreditLimitMutation();

  const customer = accountResponse?.data;
  const accountContacts = contactsResponse?.data ?? [];
  const relatedActivities = activitiesResponse?.data?.data ?? [];
  const primaryContact = accountContacts.find((contact) => contact.isPrimary);

  if (!isAccountLoading && !customer) {
    return (
      <div className={cn('p-6', className)}>
        <Card>
          <CardContent className='py-16 text-center'>
            <AlertCircle className='mx-auto mb-4 h-12 w-12 text-muted-foreground' />
            <h2 className='mb-2 text-xl font-semibold'>Account Not Found</h2>
            <p className='mb-4 text-muted-foreground'>
              The account you're looking for doesn't exist or has been deleted.
            </p>
            <Button onClick={() => router.push('/crm/accounts')}>
              Back to Accounts
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (!customer) {
    return null;
  }

  const statusConfig = STATUS_CONFIG[customer.status] ?? STATUS_CONFIG.ACTIVE;
  const tierLabel = customer.tier
    ? (ACCOUNT_TIERS.find((tier) => tier.value === customer.tier)?.label ??
      customer.tier)
    : null;

  const handleDelete = () => {
    deleteAccount(customerId)
      .unwrap()
      .then(() => {
        toast.success('Delete account successfully');
        router.push('/crm/accounts');
      })
      .catch((error) => {
        toast.error('Failed to delete account', {
          description: getErrorMessage(error),
        });
      });
  };

  const openCreditLimitDialog = () => {
    setCreditLimitInput(
      customer.creditLimit != null ? String(customer.creditLimit) : ''
    );
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
      toast.error('Failed to update credit limit', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleActivate = async () => {
    try {
      await activateAccount(customerId).unwrap();
      toast.success('Account activated');
    } catch (error) {
      toast.error('Failed to activate account', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleDeactivate = async () => {
    try {
      await deactivateAccount(customerId).unwrap();
      toast.success('Account deactivated');
    } catch (error) {
      toast.error('Failed to deactivate account', {
        description: getErrorMessage(error),
      });
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
      toast.error('Failed to create contact', {
        description: getErrorMessage(error),
      });
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
      toast.error('Failed to update contact', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleDeleteContact = async (contact: Contact) => {
    try {
      await deleteContact(contact.id).unwrap();
      toast.success('Delete contact successfully');
    } catch (error) {
      toast.error('Failed to delete contact', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleSetPrimaryContact = async (contact: Contact) => {
    try {
      await setPrimaryContact(contact.id).unwrap();
      toast.success('Primary contact updated successfully');
    } catch (error) {
      toast.error('Failed to update primary contact', {
        description: getErrorMessage(error),
      });
    }
  };

  const formatCurrency = (value: number) =>
    new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      minimumFractionDigits: 0,
    }).format(value);

  const formatDate = (dateString?: string) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  const formatDateTime = (dateString?: string) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getActivityIcon = (activity: Activity) => {
    if (activity.type === 'CALL') return Phone;
    if (activity.type === 'EMAIL') return Mail;
    if (activity.type === 'MEETING') return Users;
    return CheckCircle2;
  };

  return (
    <div className={cn('space-y-6 p-6', className)}>
      <div className='flex items-start justify-between gap-4'>
        <div className='flex items-start gap-4'>
          <Button
            variant='outline'
            size='icon'
            onClick={() => router.push('/crm/accounts')}
          >
            <ArrowLeft className='h-4 w-4' />
          </Button>
          <div className='flex items-start gap-4'>
            <Avatar className='h-16 w-16'>
              <AvatarFallback className='bg-primary text-xl text-primary-foreground'>
                {customer.name
                  .split(' ')
                  .map((part) => part[0])
                  .join('')
                  .slice(0, 2)}
              </AvatarFallback>
            </Avatar>
            <div>
              <div className='mb-2 flex flex-wrap items-center gap-3'>
                <h1 className='text-2xl font-bold text-foreground'>
                  {customer.name}
                </h1>
                <Badge className={cn(statusConfig.bgColor, statusConfig.color)}>
                  {statusConfig.label}
                </Badge>
                {tierLabel && <Badge variant='outline'>{tierLabel}</Badge>}
                {customer.tags.includes('VIP') && (
                  <Badge className='bg-yellow-100 text-yellow-700 dark:bg-yellow-900/50 dark:text-yellow-300'>
                    <Star className='mr-1 h-3 w-3 fill-current' />
                    VIP
                  </Badge>
                )}
              </div>
              <div className='flex flex-wrap items-center gap-4 text-sm text-muted-foreground'>
                {customer.companyName && (
                  <span className='flex items-center gap-1'>
                    <Building2 className='h-4 w-4' />
                    {customer.companyName}
                  </span>
                )}
                <span className='flex items-center gap-1'>
                  <Mail className='h-4 w-4' />
                  {customer.email}
                </span>
                {customer.phone && (
                  <span className='flex items-center gap-1'>
                    <Phone className='h-4 w-4' />
                    {customer.phone}
                  </span>
                )}
              </div>
            </div>
          </div>
        </div>

        <div className='flex items-center gap-2'>
          <Button
            variant='outline'
            onClick={() => router.push(`/crm/accounts/${customerId}/edit`)}
          >
            <Edit className='mr-2 h-4 w-4' />
            Edit
          </Button>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant='outline' size='icon'>
                <MoreHorizontal className='h-4 w-4' />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align='end'>
              {customer.status === 'INACTIVE' ? (
                <DropdownMenuItem
                  onClick={handleActivate}
                  disabled={isActivating}
                >
                  <CheckCircle2 className='mr-2 h-4 w-4' />
                  Activate
                </DropdownMenuItem>
              ) : (
                <DropdownMenuItem
                  onClick={handleDeactivate}
                  disabled={isDeactivating}
                >
                  <AlertCircle className='mr-2 h-4 w-4' />
                  Deactivate
                </DropdownMenuItem>
              )}
              <DropdownMenuItem onClick={openCreditLimitDialog}>
                <CreditCard className='mr-2 h-4 w-4' />
                Edit credit limit
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem
                className='text-red-600'
                onClick={() => setIsDeleteDialogOpen(true)}
              >
                <Trash2 className='mr-2 h-4 w-4' />
                Delete Account
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>

      <div className='grid grid-cols-1 gap-4 md:grid-cols-4'>
        <AccountMetricCard
          label='Total Value'
          value={formatCurrency(customer.totalValue || 0)}
          icon={DollarSign}
          tone='green'
        />
        <AccountMetricCard
          label='Opportunities'
          value={customer.totalOpportunities ?? 0}
          icon={Building2}
          tone='blue'
        />
        <AccountMetricCard
          label='Won Opportunities'
          value={customer.wonOpportunities ?? 0}
          icon={CheckCircle2}
          tone='purple'
        />
        <AccountMetricCard
          label='Credit Limit'
          value={
            customer.creditLimit != null
              ? formatCurrency(Number(customer.creditLimit))
              : 'N/A'
          }
          icon={CreditCard}
          tone='orange'
        />
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList>
          <TabsTrigger value='overview'>Overview</TabsTrigger>
          <TabsTrigger value='contacts'>
            Contacts ({accountContacts.length})
          </TabsTrigger>
          <TabsTrigger value='activities'>Activities</TabsTrigger>
        </TabsList>

        <TabsContent value='overview' className='mt-6'>
          <div className='grid grid-cols-1 gap-6 lg:grid-cols-3'>
            <div className='space-y-6 lg:col-span-2'>
              <Card>
                <CardHeader>
                  <h3 className='font-semibold'>Account Information</h3>
                </CardHeader>
                <CardContent>
                  <div className='grid grid-cols-1 gap-4 sm:grid-cols-2'>
                    <InfoField
                      label='Account Type'
                      value={customer.customerType.toLowerCase()}
                    />
                    <div>
                      <LabelText>Status</LabelText>
                      <Badge
                        className={cn(statusConfig.bgColor, statusConfig.color)}
                      >
                        {statusConfig.label}
                      </Badge>
                    </div>
                    <InfoField
                      icon={Mail}
                      label='Email'
                      value={customer.email}
                    />
                    <InfoField
                      icon={Phone}
                      label='Phone'
                      value={customer.phone || 'N/A'}
                    />
                    {customer.companyName && (
                      <InfoField
                        icon={Building2}
                        label='Company'
                        value={customer.companyName}
                      />
                    )}
                    {customer.website && (
                      <InfoField
                        icon={Globe}
                        label='Website'
                        value={customer.website}
                      />
                    )}
                    {customer.address && (
                      <div className='sm:col-span-2'>
                        <InfoField
                          icon={MapPin}
                          label='Address'
                          value={customer.address}
                        />
                      </div>
                    )}
                    <InfoField label='Tax Number' value={customer.taxNumber} />
                    <InfoField
                      label='Industry'
                      value={customer.industry || customer.tags[0]}
                    />
                    <InfoField
                      label='Company Size'
                      value={customer.companySize}
                    />
                    <InfoField label='Tier' value={tierLabel ?? undefined} />
                    <InfoField label='Language' value={customer.language} />
                    <InfoField
                      icon={Clock}
                      label='Timezone'
                      value={customer.timezone}
                    />
                    <InfoField
                      label='Account Since'
                      value={formatDate(customer.createdAt)}
                    />
                    <InfoField
                      label='Last Contact'
                      value={formatDate(customer.lastContactDate)}
                    />
                  </div>
                </CardContent>
              </Card>

              {customer.notes && (
                <Card>
                  <CardHeader>
                    <h3 className='font-semibold'>Account Notes</h3>
                  </CardHeader>
                  <CardContent>
                    <p className='text-sm text-muted-foreground'>
                      {customer.notes}
                    </p>
                  </CardContent>
                </Card>
              )}
            </div>

            <div className='space-y-6'>
              <Card>
                <CardHeader>
                  <h3 className='font-semibold'>Quick Actions</h3>
                </CardHeader>
                <CardContent className='space-y-2'>
                  <Button
                    variant='outline'
                    className='w-full justify-start'
                    onClick={() => setMeetingRequestOpen(true)}
                  >
                    <Calendar className='mr-2 h-4 w-4' />
                    Request meeting
                  </Button>
                  <Button
                    variant='outline'
                    className='w-full justify-start'
                    onClick={() => router.push('/crm/activities')}
                  >
                    <CheckCircle2 className='mr-2 h-4 w-4' />
                    Open activities
                  </Button>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <h3 className='font-semibold'>Contact Preferences</h3>
                </CardHeader>
                <CardContent className='space-y-3'>
                  <PreferenceBadges
                    label='Preferred times'
                    values={customer.preferredTimeSlots}
                    resolveLabel={(slot) =>
                      PREFERRED_TIME_SLOTS.find((item) => item.value === slot)
                        ?.label ?? slot
                    }
                  />
                  <PreferenceBadges
                    label='Preferred days'
                    values={customer.preferredDays}
                    resolveLabel={(day) =>
                      PREFERRED_DAYS.find((item) => item.value === day)
                        ?.label ?? day
                    }
                  />
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <h3 className='font-semibold'>Tags</h3>
                </CardHeader>
                <CardContent>
                  <div className='flex flex-wrap gap-2'>
                    {customer.tags.length > 0 ? (
                      customer.tags.map((tag) => (
                        <Badge key={tag} variant='secondary' className='gap-1'>
                          <Tag className='h-3 w-3' />
                          {tag}
                        </Badge>
                      ))
                    ) : (
                      <p className='text-sm text-muted-foreground'>No tags</p>
                    )}
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <h3 className='font-semibold'>Primary Contact</h3>
                </CardHeader>
                <CardContent>
                  {primaryContact ? (
                    <div className='flex items-start gap-3'>
                      <Avatar>
                        <AvatarFallback>
                          {`${primaryContact.firstName[0] ?? ''}${
                            primaryContact.lastName[0] ?? ''
                          }`}
                        </AvatarFallback>
                      </Avatar>
                      <div>
                        <p className='font-medium'>
                          {primaryContact.firstName} {primaryContact.lastName}
                        </p>
                        <p className='text-sm text-muted-foreground'>
                          {primaryContact.jobTitle || primaryContact.email}
                        </p>
                      </div>
                    </div>
                  ) : (
                    <p className='text-sm text-muted-foreground'>
                      No primary contact
                    </p>
                  )}
                </CardContent>
              </Card>
            </div>
          </div>
        </TabsContent>

        <TabsContent value='contacts' className='mt-6'>
          <ContactList
            contacts={accountContacts}
            title='Account Contacts'
            emptyMessage='No contacts found for this account'
            onAddContact={handleAddContact}
            onEditContact={handleEditContact}
            onDeleteContact={handleDeleteContact}
            onSetPrimary={handleSetPrimaryContact}
          />
        </TabsContent>

        <TabsContent value='activities' className='mt-6'>
          <Card>
            <CardHeader className='flex flex-row items-center justify-between'>
              <h3 className='font-semibold'>Recent Activities</h3>
              <Button onClick={() => router.push('/crm/activities')}>
                <CheckCircle2 className='mr-2 h-4 w-4' />
                Open Activities
              </Button>
            </CardHeader>
            <CardContent>
              {relatedActivities.length > 0 ? (
                <div className='space-y-4'>
                  {relatedActivities.map((activity) => {
                    const ActivityIcon = getActivityIcon(activity);

                    return (
                      <div
                        key={activity.id}
                        className='flex items-start gap-4 rounded-lg bg-muted p-4'
                      >
                        <div
                          className={cn(
                            'rounded-full p-2',
                            activity.status === 'COMPLETED'
                              ? 'bg-green-100 dark:bg-green-900/50'
                              : 'bg-blue-100 dark:bg-blue-900/50'
                          )}
                        >
                          <ActivityIcon className='h-4 w-4 text-blue-600 dark:text-blue-400' />
                        </div>
                        <div className='flex-1'>
                          <div className='flex items-center justify-between gap-3'>
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
                          <p className='mt-1 text-sm text-muted-foreground'>
                            {activity.description || 'No description'}
                          </p>
                          <div className='mt-2 flex items-center gap-4 text-xs text-muted-foreground'>
                            <span>{activity.assignedToName}</span>
                            <span>
                              {formatDateTime(activity.scheduledDate)}
                            </span>
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              ) : (
                <div className='py-8 text-center'>
                  <Calendar className='mx-auto mb-4 h-12 w-12 text-muted-foreground' />
                  <p className='mb-4 text-muted-foreground'>
                    No activities recorded yet.
                  </p>
                  <Button onClick={() => router.push('/crm/activities')}>
                    Open Activities
                  </Button>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      <Dialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Delete Account</DialogTitle>
            <DialogDescription>
              Are you sure you want to delete this account? This action cannot
              be undone.
            </DialogDescription>
          </DialogHeader>
          <div className='py-4'>
            <div className='rounded-lg border border-red-200 bg-red-50 p-4 dark:border-red-800 dark:bg-red-950/50'>
              <div className='flex items-center gap-3'>
                <Avatar>
                  <AvatarFallback>
                    {customer.name
                      .split(' ')
                      .map((part) => part[0])
                      .join('')
                      .slice(0, 2)}
                  </AvatarFallback>
                </Avatar>
                <div>
                  <p className='font-medium text-red-700 dark:text-red-300'>
                    {customer.name}
                  </p>
                  <p className='text-sm text-red-600 dark:text-red-400'>
                    {customer.email}
                  </p>
                </div>
              </div>
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
              Delete Account
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog
        open={isCreditLimitDialogOpen}
        onOpenChange={setIsCreditLimitDialogOpen}
      >
        <DialogContent className='sm:max-w-[400px]'>
          <DialogHeader>
            <DialogTitle>Edit credit limit</DialogTitle>
            <DialogDescription>
              Set the credit limit for {customer.name}. Use 0 if none.
            </DialogDescription>
          </DialogHeader>
          <div className='py-2'>
            <Label htmlFor='credit-limit-input'>Credit limit (VND)</Label>
            <Input
              id='credit-limit-input'
              type='number'
              min={0}
              step={1}
              className='mt-1.5'
              value={creditLimitInput}
              onChange={(event) => setCreditLimitInput(event.target.value)}
            />
          </div>
          <DialogFooter>
            <Button
              variant='outline'
              onClick={() => setIsCreditLimitDialogOpen(false)}
            >
              Cancel
            </Button>
            <Button
              onClick={handleSaveCreditLimit}
              disabled={isUpdatingCreditLimit}
            >
              {isUpdatingCreditLimit ? 'Saving...' : 'Save'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <RequestMeetingDialog
        open={meetingRequestOpen}
        onOpenChange={setMeetingRequestOpen}
        accountId={customerId}
        accountName={customer.name}
      />
    </div>
  );
};

interface AccountMetricCardProps {
  label: string;
  value: string | number;
  icon: LucideIcon;
  tone: 'green' | 'blue' | 'purple' | 'orange';
}

const toneStyles: Record<
  AccountMetricCardProps['tone'],
  { card: string; icon: string }
> = {
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

const AccountMetricCard: React.FC<AccountMetricCardProps> = ({
  label,
  value,
  icon: Icon,
  tone,
}) => {
  const styles = toneStyles[tone];

  return (
    <Card className={cn('bg-gradient-to-br', styles.card)}>
      <CardContent className='py-4'>
        <div className='flex items-center justify-between'>
          <div>
            <p className='text-sm text-muted-foreground'>{label}</p>
            <p className='text-2xl font-bold'>{value}</p>
          </div>
          <div className={cn('rounded-full p-3', styles.icon)}>
            <Icon className='h-6 w-6' />
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

interface InfoFieldProps {
  label: string;
  value?: string | number | null;
  icon?: LucideIcon;
}

const LabelText: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <p className='mb-1 text-sm font-medium text-muted-foreground'>{children}</p>
);

const InfoField: React.FC<InfoFieldProps> = ({ label, value, icon: Icon }) => (
  <div>
    <LabelText>{label}</LabelText>
    <p className='flex items-center gap-2 font-medium capitalize'>
      {Icon && <Icon className='h-4 w-4 text-muted-foreground' />}
      {value || 'N/A'}
    </p>
  </div>
);

interface PreferenceBadgesProps<T extends string> {
  label: string;
  values?: T[];
  resolveLabel: (value: T) => string;
}

function PreferenceBadges<T extends string>({
  label,
  values,
  resolveLabel,
}: PreferenceBadgesProps<T>) {
  return (
    <div>
      <p className='mb-1.5 text-xs font-medium text-muted-foreground'>
        {label}
      </p>
      <div className='flex flex-wrap gap-1.5'>
        {values && values.length > 0 ? (
          values.map((value) => (
            <Badge key={value} variant='secondary'>
              {resolveLabel(value)}
            </Badge>
          ))
        ) : (
          <p className='text-sm text-muted-foreground'>N/A</p>
        )}
      </div>
    </div>
  );
}

export default CustomerDetailPageEnhanced;
