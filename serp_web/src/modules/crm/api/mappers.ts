import type {
  Account,
  AccountTier,
  Activity,
  APIResponse,
  Contact,
  CreateAccountRequest,
  CreateLeadRequest,
  CreateOpportunityRequest,
  CreateActivityRequest,
  UpdateActivityRequest,
  Opportunity,
  OpportunityFilters,
  UpdateAccountRequest,
  UpdateLeadRequest,
  UpdateOpportunityRequest,
  CustomerFilters,
  CrmDayOfWeek,
  Lead,
  LeadFilters,
  LeadStatusTransitionResult,
  PaginationParams,
  ActivityFilters,
  ActivityStats,
  BulkActivityRequest,
  BulkActivityResult,
  PreferredTimeSlot,
  MeetingRequest,
  MeetingRequestStatus,
  MeetingRequestType,
} from '../types';

export type BulkActivityRequestPayload = {
  activityIds: number[];
  action: BulkActivityRequest['action'];
  assigneeId?: number;
};

type GeneralResponse<T> = {
  status: string;
  code: number;
  message?: string;
  data: T;
};

type PageResponse<T> = {
  items: T[];
  pagination?: {
    page: number;
    size: number;
    totalItems: number;
    totalPages: number;
    hasNext: boolean;
    hasPrevious: boolean;
  };
};

type BackendAddress = {
  street?: string | null;
  city?: string | null;
  state?: string | null;
  zipCode?: string | null;
  postalCode?: string | null;
  country?: string | null;
};

type BackendAccount = {
  id: number | string;
  name?: string | null;
  industry?: string | null;
  companySize?: string | null;
  website?: string | null;
  phone?: string | null;
  email?: string | null;
  address?: BackendAddress | null;
  taxId?: string | null;
  creditLimit?: number | string | null;
  paymentTerms?: string | null;
  activeStatus?: string | null;
  accountType?: string | null;
  tier?: string | null;
  preferredTimeSlots?: string[] | null;
  preferredDays?: string[] | null;
  language?: string | null;
  timezone?: string | null;
  totalRevenue?: number | string | null;
  totalOpportunities?: number | null;
  wonOpportunities?: number | null;
  notes?: string | null;
  createdAt?: number | string | null;
  updatedAt?: number | string | null;
};

type BackendContact = {
  id: number | string;
  name?: string | null;
  email?: string | null;
  phone?: string | null;
  jobPosition?: string | null;
  accountId?: number | string | null;
  isPrimary?: boolean | null;
  activeStatus?: string | null;
  linkedInUrl?: string | null;
  notes?: string | null;
  createdAt?: number | string | null;
  updatedAt?: number | string | null;
};

type BackendActivity = {
  id: number | string;
  subject?: string | null;
  description?: string | null;
  activityType?: string | null;
  status?: string | null;
  accountId?: number | string | null;
  leadId?: number | string | null;
  opportunityId?: number | string | null;
  contactId?: number | string | null;
  assignedTo?: number | string | null;
  assignedToName?: string | null;
  relatedLeadName?: string | null;
  relatedCustomerName?: string | null;
  relatedOpportunityName?: string | null;
  relatedContactName?: string | null;
  activityDate?: number | string | null;
  dueDate?: number | string | null;
  reminderDate?: number | string | null;
  durationMinutes?: number | null;
  priority?: string | null;
  location?: string | null;
  outcome?: string | null;
  notes?: string | null;
  attachments?: string[] | null;
  createdAt?: number | string | null;
  updatedAt?: number | string | null;
};

type BackendLeadAddress = {
  street?: string | null;
  city?: string | null;
  state?: string | null;
  postalCode?: string | null;
  country?: string | null;
};

type BackendLead = {
  id: number | string;
  company?: string | null;
  industry?: string | null;
  companySize?: string | null;
  website?: string | null;
  name?: string | null;
  email?: string | null;
  phone?: string | null;
  jobTitle?: string | null;
  address?: BackendLeadAddress | null;
  leadSource?: string | null;
  leadStatus?: string | null;
  assignedTo?: number | string | null;
  estimatedValue?: number | string | null;
  leadScore?: number | null;
  followUpDate?: string | null;
  notes?: string | null;
  convertedOpportunityId?: number | string | null;
  convertedAccountId?: number | string | null;
  tenantId?: number | string | null;
  createdAt?: number | string | null;
  updatedAt?: number | string | null;
  createdBy?: number | string | null;
  updatedBy?: number | string | null;
};

type BackendLeadStatusTransition = {
  lead: BackendLead;
  fromStatus?: string | null;
  toStatus?: string | null;
  accountId?: number | string | null;
  opportunityId?: number | string | null;
  contactId?: number | string | null;
  message?: string | null;
};

type BackendOpportunity = {
  id: number | string;
  name?: string | null;
  description?: string | null;
  leadId?: number | string | null;
  accountId?: number | string | null;
  stage?: string | null;
  estimatedValue?: number | string | null;
  actualValue?: number | string | null;
  probability?: number | null;
  expectedCloseDate?: string | null;
  actualCloseDate?: string | null;
  assignedTo?: number | string | null;
  accountName?: string | null;
  assignedToName?: string | null;
  leadName?: string | null;
  lastActivityAt?: number | string | null;
  nextActivityAt?: number | string | null;
  openActivityCount?: number | null;
  overdueActivityCount?: number | null;
  notes?: string | null;
  lossReason?: string | null;
  reopenReason?: string | null;
  tenantId?: number | string | null;
  createdAt?: number | string | null;
  updatedAt?: number | string | null;
  createdBy?: number | string | null;
  updatedBy?: number | string | null;
};

type BackendOpportunityPipelineStage = {
  stage?: string | null;
  count?: number | null;
  totalValue?: number | string | null;
  weightedValue?: number | string | null;
  opportunities?: BackendOpportunity[] | null;
};

type BackendOpportunityPipelineSummary = {
  totalOpportunities?: number | null;
  totalPipelineValue?: number | string | null;
  weightedPipelineValue?: number | string | null;
  averageDealSize?: number | string | null;
};

type BackendOpportunityPipeline = {
  stages?: BackendOpportunityPipelineStage[] | null;
  summary?: BackendOpportunityPipelineSummary | null;
};

const toIsoString = (value?: number | string | null): string => {
  if (!value) return new Date(0).toISOString();
  const timestamp = typeof value === 'string' ? Number(value) : value;
  if (Number.isFinite(timestamp))
    return new Date(Number(timestamp)).toISOString();
  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? new Date(0).toISOString()
    : date.toISOString();
};

const joinAddress = (address?: BackendAddress | null): string | undefined => {
  if (!address) return undefined;
  const parts = [
    address.street,
    address.city,
    address.state,
    address.postalCode ?? address.zipCode,
    address.country,
  ].filter(Boolean);
  return parts.length > 0 ? parts.join(', ') : undefined;
};

const ACCOUNT_TIER_SET = new Set<string>([
  'STANDARD',
  'SILVER',
  'GOLD',
  'PLATINUM',
]);

const PREFERRED_SLOT_SET = new Set<string>(['MORNING', 'AFTERNOON']);

const CRM_DAY_SET = new Set<string>([
  'MONDAY',
  'TUESDAY',
  'WEDNESDAY',
  'THURSDAY',
  'FRIDAY',
  'SATURDAY',
  'SUNDAY',
]);

export const mapBackendAccountToAccount = (
  account: BackendAccount
): Account => {
  const zipOrPostal =
    account.address?.postalCode ?? account.address?.zipCode ?? undefined;

  const tierRaw = account.tier;
  const tier: AccountTier | undefined =
    tierRaw && ACCOUNT_TIER_SET.has(tierRaw)
      ? (tierRaw as AccountTier)
      : undefined;

  const preferredTimeSlots: PreferredTimeSlot[] | undefined = account
    .preferredTimeSlots?.length
    ? (account.preferredTimeSlots.filter((s) =>
        PREFERRED_SLOT_SET.has(s)
      ) as PreferredTimeSlot[])
    : undefined;

  const preferredDays: CrmDayOfWeek[] | undefined = account.preferredDays
    ?.length
    ? (account.preferredDays.filter((d) =>
        CRM_DAY_SET.has(d)
      ) as CrmDayOfWeek[])
    : undefined;

  return {
    id: String(account.id),
    createdAt: toIsoString(account.createdAt),
    updatedAt: toIsoString(account.updatedAt),
    isActive: account.activeStatus !== 'INACTIVE',
    name: account.name || '',
    email: account.email || '',
    phone: account.phone || undefined,
    address: joinAddress(account.address),
    customerType: account.accountType === 'CUSTOMER' ? 'CUSTOMER' : 'PROSPECT',
    status: account.activeStatus === 'INACTIVE' ? 'INACTIVE' : 'ACTIVE',
    companyName: account.name || undefined,
    taxNumber: account.taxId || undefined,
    website: account.website || undefined,
    notes: account.notes || undefined,
    assignedSalesRep: undefined,
    tags: account.industry ? [account.industry] : [],
    industry: account.industry || undefined,
    companySize: account.companySize || undefined,
    tier,
    preferredTimeSlots,
    preferredDays,
    language: account.language || undefined,
    timezone: account.timezone || undefined,
    creditLimit:
      account.creditLimit === null || account.creditLimit === undefined
        ? undefined
        : Number(account.creditLimit),
    paymentTerms: account.paymentTerms || undefined,
    totalOpportunities: account.totalOpportunities ?? undefined,
    wonOpportunities: account.wonOpportunities ?? undefined,
    customFields: {
      industry: account.industry || undefined,
      companySize: account.companySize || undefined,
      city: account.address?.city || undefined,
      state: account.address?.state || undefined,
      zipCode: zipOrPostal,
      country: account.address?.country || undefined,
      paymentTerms: account.paymentTerms || undefined,
      creditLimit: account.creditLimit || undefined,
      wonOpportunities: account.wonOpportunities || 0,
    },
    totalValue: Number(account.totalRevenue || 0),
    lastContactDate: undefined,
  };
};

export const mapBackendContactToContact = (
  contact: BackendContact
): Contact => {
  const name = (contact.name || '').trim();
  const parts = name.split(/\s+/).filter(Boolean);
  const lastName = parts.length > 1 ? parts[0] : '';
  const firstName =
    parts.length > 1 ? parts.slice(1).join(' ') : parts[0] || '';

  return {
    id: String(contact.id),
    firstName,
    lastName,
    email: contact.email || '',
    phone: contact.phone || undefined,
    jobTitle: contact.jobPosition || undefined,
    department: undefined,
    isPrimary: !!contact.isPrimary,
    isFavorite: false,
    notes: contact.notes || undefined,
    linkedInUrl: contact.linkedInUrl || undefined,
    createdAt: toIsoString(contact.createdAt),
    updatedAt: toIsoString(contact.updatedAt),
  };
};

export const mapBackendActivityToActivity = (
  activity: BackendActivity
): Activity => {
  const assigneeIdRaw = activity.assignedTo;
  const assigneeId =
    assigneeIdRaw !== null &&
    assigneeIdRaw !== undefined &&
    String(assigneeIdRaw).trim() !== ''
      ? String(assigneeIdRaw)
      : '';
  const assigneeNameRaw = activity.assignedToName?.trim();
  const assignedToName =
    assigneeNameRaw || (assigneeId ? `User #${assigneeId}` : 'Unassigned');

  return {
    id: String(activity.id),
    createdAt: toIsoString(activity.createdAt),
    updatedAt: toIsoString(activity.updatedAt),
    isActive: activity.status !== 'CANCELLED',
    type: (activity.activityType as Activity['type']) || 'TASK',
    status: (activity.status as Activity['status']) || 'PLANNED',
    subject: activity.subject || 'Untitled activity',
    description: activity.description || undefined,
    scheduledDate: toIsoString(activity.activityDate ?? activity.dueDate),
    actualDate: undefined,
    duration: activity.durationMinutes || undefined,
    priority: (activity.priority as Activity['priority']) || 'MEDIUM',
    assignedTo: assigneeId,
    assignedToName,
    relatedTo: {
      type: activity.accountId
        ? 'CUSTOMER'
        : activity.leadId
          ? 'LEAD'
          : 'OPPORTUNITY',
      id: String(
        activity.accountId ||
          activity.leadId ||
          activity.opportunityId ||
          activity.contactId ||
          ''
      ),
      name:
        activity.relatedCustomerName ||
        activity.relatedLeadName ||
        activity.relatedOpportunityName ||
        activity.relatedContactName ||
        activity.subject ||
        'Related item',
    },
    participants: [],
    location: activity.location || undefined,
    outcome: activity.outcome || undefined,
    followUpRequired: false,
    followUpDate: activity.dueDate ? toIsoString(activity.dueDate) : undefined,
    tags: [],
    customFields: {
      notes: activity.notes || undefined,
      reminderDate: activity.reminderDate
        ? toIsoString(activity.reminderDate)
        : undefined,
      attachments: activity.attachments || [],
      contactId: activity.contactId ? String(activity.contactId) : undefined,
    },
  };
};

const splitLeadName = (name?: string | null) => {
  const normalized = (name || '').trim();
  const parts = normalized.split(/\s+/).filter(Boolean);

  if (parts.length === 0) {
    return { firstName: '', lastName: '' };
  }

  if (parts.length === 1) {
    return { firstName: parts[0], lastName: '' };
  }

  return {
    firstName: parts.slice(0, -1).join(' '),
    lastName: parts[parts.length - 1],
  };
};

export const mapBackendLeadToLead = (lead: BackendLead): Lead => {
  const name = (lead.name || '').trim();
  const { firstName, lastName } = splitLeadName(name);
  const leadSource = (lead.leadSource as Lead['leadSource']) || 'WEBSITE';
  const leadStatus = (lead.leadStatus as Lead['leadStatus']) || 'NEW';

  return {
    id: String(lead.id),
    createdAt: toIsoString(lead.createdAt),
    updatedAt: toIsoString(lead.updatedAt),
    isActive: leadStatus !== 'DISQUALIFIED' && leadStatus !== 'CONVERTED',
    name,
    email: lead.email || '',
    phone: lead.phone || undefined,
    company: lead.company || undefined,
    industry: lead.industry || undefined,
    companySize: lead.companySize || undefined,
    website: lead.website || undefined,
    jobTitle: lead.jobTitle || undefined,
    address: lead.address
      ? {
          street: lead.address.street || undefined,
          city: lead.address.city || undefined,
          state: lead.address.state || undefined,
          postalCode: lead.address.postalCode || undefined,
          country: lead.address.country || undefined,
        }
      : undefined,
    leadSource,
    leadStatus,
    assignedTo:
      lead.assignedTo === null || lead.assignedTo === undefined
        ? undefined
        : String(lead.assignedTo),
    estimatedValue:
      lead.estimatedValue === null || lead.estimatedValue === undefined
        ? undefined
        : Number(lead.estimatedValue),
    leadScore: lead.leadScore || undefined,
    followUpDate: lead.followUpDate || undefined,
    notes: lead.notes || undefined,
    convertedOpportunityId:
      lead.convertedOpportunityId === null ||
      lead.convertedOpportunityId === undefined
        ? undefined
        : String(lead.convertedOpportunityId),
    convertedAccountId:
      lead.convertedAccountId === null || lead.convertedAccountId === undefined
        ? undefined
        : String(lead.convertedAccountId),
    tenantId:
      lead.tenantId === null || lead.tenantId === undefined
        ? undefined
        : String(lead.tenantId),
    createdBy:
      lead.createdBy === null || lead.createdBy === undefined
        ? undefined
        : String(lead.createdBy),
    updatedBy:
      lead.updatedBy === null || lead.updatedBy === undefined
        ? undefined
        : String(lead.updatedBy),
    firstName,
    lastName,
    source: leadSource,
    status: leadStatus,
    priority: 'MEDIUM',
    expectedCloseDate: lead.followUpDate || undefined,
    tags: [],
    customFields: {
      industry: lead.industry || undefined,
      companySize: lead.companySize || undefined,
      street: lead.address?.street || undefined,
      city: lead.address?.city || undefined,
      state: lead.address?.state || undefined,
      postalCode: lead.address?.postalCode || undefined,
      country: lead.address?.country || undefined,
    },
    conversionDate:
      leadStatus === 'CONVERTED' ? toIsoString(lead.updatedAt) : undefined,
    convertedToCustomerId:
      lead.convertedAccountId === null || lead.convertedAccountId === undefined
        ? undefined
        : String(lead.convertedAccountId),
    lastActivityDate: undefined,
  };
};

const mapBackendOpportunityAssignedToName = (
  assignedTo?: number | string | null,
  assignedToName?: string | null
) => {
  const trimmedName = assignedToName?.trim();
  if (trimmedName) {
    return trimmedName;
  }

  if (assignedTo === null || assignedTo === undefined || assignedTo === '') {
    return 'Unassigned';
  }

  return `User #${assignedTo}`;
};

export const mapBackendOpportunityToOpportunity = (
  opportunity: BackendOpportunity
): Opportunity => {
  const accountId =
    opportunity.accountId === null || opportunity.accountId === undefined
      ? ''
      : String(opportunity.accountId);
  const assignedTo =
    opportunity.assignedTo === null || opportunity.assignedTo === undefined
      ? undefined
      : String(opportunity.assignedTo);
  const accountName = opportunity.accountName?.trim() || undefined;
  const estimatedValue = Number(opportunity.estimatedValue || 0);
  const probability = opportunity.probability ?? 0;

  return {
    id: String(opportunity.id),
    createdAt: toIsoString(opportunity.createdAt),
    updatedAt: toIsoString(opportunity.updatedAt),
    isActive:
      opportunity.stage !== 'CLOSED_WON' && opportunity.stage !== 'CLOSED_LOST',
    name: opportunity.name || '',
    accountId,
    leadId:
      opportunity.leadId === null || opportunity.leadId === undefined
        ? undefined
        : String(opportunity.leadId),
    customerId: accountId,
    customerName:
      accountName || (accountId ? `Account #${accountId}` : undefined),
    accountName,
    leadName: opportunity.leadName?.trim() || undefined,
    stage: (opportunity.stage as Opportunity['stage']) || 'PROSPECTING',
    type: 'NEW_BUSINESS',
    estimatedValue,
    value: estimatedValue,
    actualValue:
      opportunity.actualValue === null || opportunity.actualValue === undefined
        ? undefined
        : Number(opportunity.actualValue),
    probability,
    expectedCloseDate:
      opportunity.expectedCloseDate || toIsoString(opportunity.createdAt),
    actualCloseDate: opportunity.actualCloseDate || undefined,
    assignedTo,
    assignedToName: mapBackendOpportunityAssignedToName(
      opportunity.assignedTo,
      opportunity.assignedToName
    ),
    lastActivityAt:
      opportunity.lastActivityAt === null ||
      opportunity.lastActivityAt === undefined
        ? undefined
        : toIsoString(opportunity.lastActivityAt),
    nextActivityAt:
      opportunity.nextActivityAt === null ||
      opportunity.nextActivityAt === undefined
        ? undefined
        : toIsoString(opportunity.nextActivityAt),
    openActivityCount: opportunity.openActivityCount ?? 0,
    overdueActivityCount: opportunity.overdueActivityCount ?? 0,
    description: opportunity.description || undefined,
    tags: [],
    products: [],
    notes: opportunity.notes || undefined,
    competitors: [],
    nextAction: undefined,
    nextActionDate: undefined,
    lostReason: opportunity.lossReason || undefined,
    reopenReason: opportunity.reopenReason || undefined,
    customFields: {
      tenantId:
        opportunity.tenantId === null || opportunity.tenantId === undefined
          ? undefined
          : String(opportunity.tenantId),
      createdBy:
        opportunity.createdBy === null || opportunity.createdBy === undefined
          ? undefined
          : String(opportunity.createdBy),
      updatedBy:
        opportunity.updatedBy === null || opportunity.updatedBy === undefined
          ? undefined
          : String(opportunity.updatedBy),
    },
  };
};

export const mapAccountFormToBackendPayload = (
  data: CreateAccountRequest | UpdateAccountRequest
) => {
  const address =
    data.address
      ?.split(',')
      .map((part) => part.trim())
      .filter(Boolean) || [];

  const industry = data.industry?.trim() || data.tags?.[0] || undefined;

  return {
    name: data.name,
    email: data.email || undefined,
    phone: data.phone || undefined,
    website: data.website || undefined,
    notes: data.notes || undefined,
    accountType: data.customerType,
    industry,
    companySize: data.companySize || undefined,
    taxId: data.taxNumber || undefined,
    creditLimit: data.creditLimit,
    paymentTerms: data.paymentTerms || undefined,
    tier: data.tier,
    preferredTimeSlots: data.preferredTimeSlots,
    preferredDays: data.preferredDays,
    language: data.language?.trim() || undefined,
    timezone: data.timezone?.trim() || undefined,
    street: data.address || address[0],
    city: data.city || address[1],
    state: data.state || address[2],
    zipCode: data.zipCode || undefined,
    country: data.country || address[3],
  };
};

export const mapLeadFormToBackendPayload = (
  data: CreateLeadRequest | UpdateLeadRequest
) => ({
  company: data.company || undefined,
  industry: data.industry || undefined,
  companySize: data.companySize || undefined,
  website: data.website || undefined,
  name: data.name || undefined,
  email: data.email || undefined,
  phone: data.phone || undefined,
  jobTitle: data.jobTitle || undefined,
  street: data.street || undefined,
  city: data.city || undefined,
  state: data.state || undefined,
  postalCode: data.postalCode || undefined,
  country: data.country || undefined,
  leadSource: data.leadSource || undefined,
  assignedTo:
    data.assignedTo && data.assignedTo.trim() !== ''
      ? Number(data.assignedTo)
      : undefined,
  estimatedValue: data.estimatedValue,
  leadScore: data.leadScore,
  followUpDate: data.followUpDate || undefined,
  notes: data.notes || undefined,
});

export const mapOpportunityFormToBackendPayload = (
  data: CreateOpportunityRequest | UpdateOpportunityRequest
) => ({
  name: data.name || undefined,
  description: data.description || undefined,
  accountId:
    'accountId' in data && data.accountId && data.accountId.trim() !== ''
      ? Number(data.accountId)
      : undefined,
  leadId:
    'leadId' in data && data.leadId && data.leadId.trim() !== ''
      ? Number(data.leadId)
      : undefined,
  stage: data.stage || undefined,
  estimatedValue:
    data.estimatedValue !== undefined ? data.estimatedValue : data.value,
  expectedCloseDate: data.expectedCloseDate || undefined,
  assignedTo:
    data.assignedTo && data.assignedTo.trim() !== ''
      ? Number(data.assignedTo)
      : undefined,
  notes: data.notes || undefined,
});

export const mapCustomerFiltersToAccountSearch = (
  filters: CustomerFilters = {},
  pagination: PaginationParams
) => ({
  keyword: filters.search || undefined,
  statuses: filters.status?.map((status) =>
    status === 'ACTIVE' ? 'ACTIVE' : 'INACTIVE'
  ),
  accountType: filters.type?.[0],
  page: pagination.page,
  size: pagination.limit,
  sortBy: pagination.sortBy,
  sortDirection: pagination.sortOrder?.toUpperCase(),
});

export const mapAccountListResponse = (
  response: GeneralResponse<PageResponse<BackendAccount>>
): APIResponse<{
  data: Account[];
  pagination: {
    page: number;
    limit: number;
    total: number;
    totalPages: number;
    hasNext: boolean;
    hasPrevious: boolean;
  };
}> => ({
  success: response.code >= 200 && response.code < 300,
  message: response.message,
  timestamp: new Date().toISOString(),
  data: {
    data: (response.data?.items || []).map(mapBackendAccountToAccount),
    pagination: {
      page: response.data?.pagination?.page || 1,
      limit: response.data?.pagination?.size || 20,
      total: Number(response.data?.pagination?.totalItems || 0),
      totalPages: response.data?.pagination?.totalPages || 1,
      hasNext: !!response.data?.pagination?.hasNext,
      hasPrevious: !!response.data?.pagination?.hasPrevious,
    },
  },
});

export const mapLeadFiltersToSearchRequest = (
  filters: LeadFilters = {},
  pagination: PaginationParams
) => ({
  keyword: filters.search || undefined,
  statuses: filters.status,
  sources: filters.source,
  industries: filters.industries,
  assignedTo:
    filters.assignedTo && filters.assignedTo[0]
      ? Number(filters.assignedTo[0])
      : undefined,
  unassignedOnly: filters.unassignedOnly,
  estimatedValueMin: filters.minValue,
  estimatedValueMax: filters.maxValue,
  leadScoreMin: filters.minScore,
  leadScoreMax: filters.maxScore,
  followUpDateFrom: filters.followUpDateFrom || undefined,
  followUpDateTo: filters.followUpDateTo || undefined,
  createdFrom: filters.createdDateFrom || undefined,
  createdTo: filters.createdDateTo || undefined,
  qualifiedOnly: filters.qualifiedOnly,
  convertedOnly: filters.convertedOnly,
  country: filters.country || undefined,
  city: filters.city || undefined,
  hasEmail: filters.hasEmail,
  hasPhone: filters.hasPhone,
  page: pagination.page,
  size: pagination.limit,
  sortBy: pagination.sortBy,
  sortDirection: pagination.sortOrder?.toUpperCase(),
});

export const mapOpportunityFiltersToSearchRequest = (
  filters: OpportunityFilters = {},
  pagination: PaginationParams
) => ({
  keyword: filters.search || undefined,
  stages: filters.stage,
  accountId:
    filters.customerId && filters.customerId[0]
      ? Number(filters.customerId[0])
      : undefined,
  leadId:
    filters.leadId && filters.leadId[0] ? Number(filters.leadId[0]) : undefined,
  assignedTo:
    filters.assignedTo && filters.assignedTo[0]
      ? Number(filters.assignedTo[0])
      : undefined,
  unassignedOnly: filters.unassignedOnly,
  estimatedValueMin: filters.minValue,
  estimatedValueMax: filters.maxValue,
  probabilityMin: filters.probability?.min,
  probabilityMax: filters.probability?.max,
  expectedCloseDateFrom: filters.expectedCloseDateFrom || undefined,
  expectedCloseDateTo: filters.expectedCloseDateTo || undefined,
  actualCloseDateFrom: filters.actualCloseDateFrom || undefined,
  actualCloseDateTo: filters.actualCloseDateTo || undefined,
  createdFrom: filters.createdDateFrom || undefined,
  createdTo: filters.createdDateTo || undefined,
  hasNotes: filters.hasNotes,
  page: pagination.page,
  size: pagination.limit,
  sortBy: pagination.sortBy,
  sortDirection: pagination.sortOrder?.toUpperCase(),
});

export const mapLeadListResponse = (
  response: GeneralResponse<PageResponse<BackendLead>>
): APIResponse<{
  data: Lead[];
  pagination: {
    page: number;
    limit: number;
    total: number;
    totalPages: number;
    hasNext: boolean;
    hasPrevious: boolean;
  };
}> => ({
  success: response.code >= 200 && response.code < 300,
  message: response.message,
  timestamp: new Date().toISOString(),
  data: {
    data: (response.data?.items || []).map(mapBackendLeadToLead),
    pagination: {
      page: response.data?.pagination?.page || 1,
      limit: response.data?.pagination?.size || 20,
      total: Number(response.data?.pagination?.totalItems || 0),
      totalPages: response.data?.pagination?.totalPages || 1,
      hasNext: !!response.data?.pagination?.hasNext,
      hasPrevious: !!response.data?.pagination?.hasPrevious,
    },
  },
});

export const mapSingleLeadResponse = (
  response: GeneralResponse<BackendLead>
) => ({
  success: response.code >= 200 && response.code < 300,
  message: response.message,
  timestamp: new Date().toISOString(),
  data: mapBackendLeadToLead(response.data),
});

export const mapOpportunityListResponse = (
  response: GeneralResponse<PageResponse<BackendOpportunity>>
): APIResponse<{
  data: Opportunity[];
  pagination: {
    page: number;
    limit: number;
    total: number;
    totalPages: number;
    hasNext: boolean;
    hasPrevious: boolean;
  };
}> => ({
  success: response.code >= 200 && response.code < 300,
  message: response.message,
  timestamp: new Date().toISOString(),
  data: {
    data: (response.data?.items || []).map(mapBackendOpportunityToOpportunity),
    pagination: {
      page: response.data?.pagination?.page || 1,
      limit: response.data?.pagination?.size || 20,
      total: Number(response.data?.pagination?.totalItems || 0),
      totalPages: response.data?.pagination?.totalPages || 1,
      hasNext: !!response.data?.pagination?.hasNext,
      hasPrevious: !!response.data?.pagination?.hasPrevious,
    },
  },
});

export const mapSingleOpportunityResponse = (
  response: GeneralResponse<BackendOpportunity>
) => ({
  success: response.code >= 200 && response.code < 300,
  message: response.message,
  timestamp: new Date().toISOString(),
  data: mapBackendOpportunityToOpportunity(response.data),
});

export const mapOpportunityPipelineResponse = (
  response: GeneralResponse<BackendOpportunityPipeline>
) => ({
  success: response.code >= 200 && response.code < 300,
  message: response.message,
  timestamp: new Date().toISOString(),
  data: {
    stages: (response.data?.stages || []).map((stage) => ({
      stage: (stage.stage as Opportunity['stage']) || 'PROSPECTING',
      count: stage.count || 0,
      totalValue: Number(stage.totalValue || 0),
      weightedValue: Number(stage.weightedValue || 0),
      opportunities: (stage.opportunities || []).map(
        mapBackendOpportunityToOpportunity
      ),
    })),
    summary: {
      totalOpportunities: response.data?.summary?.totalOpportunities || 0,
      totalPipelineValue: Number(
        response.data?.summary?.totalPipelineValue || 0
      ),
      weightedPipelineValue: Number(
        response.data?.summary?.weightedPipelineValue || 0
      ),
      averageDealSize: Number(response.data?.summary?.averageDealSize || 0),
    },
  },
});

export const mapLeadStatusTransitionResponse = (
  response: GeneralResponse<BackendLeadStatusTransition>
): APIResponse<LeadStatusTransitionResult> => ({
  success: response.code >= 200 && response.code < 300,
  message: response.message,
  timestamp: new Date().toISOString(),
  data: {
    lead: mapBackendLeadToLead(response.data.lead),
    fromStatus:
      (response.data.fromStatus as LeadStatusTransitionResult['fromStatus']) ||
      'NEW',
    toStatus:
      (response.data.toStatus as LeadStatusTransitionResult['toStatus']) ||
      'NEW',
    accountId:
      response.data.accountId === null || response.data.accountId === undefined
        ? undefined
        : String(response.data.accountId),
    opportunityId:
      response.data.opportunityId === null ||
      response.data.opportunityId === undefined
        ? undefined
        : String(response.data.opportunityId),
    contactId:
      response.data.contactId === null || response.data.contactId === undefined
        ? undefined
        : String(response.data.contactId),
    message: response.data.message || response.message || undefined,
  },
});

export const mapSingleAccountResponse = (
  response: GeneralResponse<BackendAccount>
) => ({
  success: response.code >= 200 && response.code < 300,
  message: response.message,
  timestamp: new Date().toISOString(),
  data: mapBackendAccountToAccount(response.data),
});

export const mapContactListResponse = (
  response: GeneralResponse<BackendContact[]>
) => ({
  success: response.code >= 200 && response.code < 300,
  message: response.message,
  timestamp: new Date().toISOString(),
  data: (response.data || []).map(mapBackendContactToContact),
});

export const mapSingleActivityResponse = (
  response: GeneralResponse<BackendActivity>
) => ({
  success: response.code >= 200 && response.code < 300,
  message: response.message,
  timestamp: new Date().toISOString(),
  data: mapBackendActivityToActivity(response.data),
});

export const mapActivityListResponse = (
  response: GeneralResponse<PageResponse<BackendActivity>>
): APIResponse<{
  data: Activity[];
  pagination: {
    page: number;
    limit: number;
    total: number;
    totalPages: number;
    hasNext: boolean;
    hasPrevious: boolean;
  };
}> => ({
  success: response.code >= 200 && response.code < 300,
  message: response.message,
  timestamp: new Date().toISOString(),
  data: {
    data: (response.data?.items || []).map(mapBackendActivityToActivity),
    pagination: {
      page: response.data?.pagination?.page || 1,
      limit: response.data?.pagination?.size || 20,
      total: Number(response.data?.pagination?.totalItems || 0),
      totalPages: response.data?.pagination?.totalPages || 1,
      hasNext: !!response.data?.pagination?.hasNext,
      hasPrevious: !!response.data?.pagination?.hasPrevious,
    },
  },
});

export const mapActivityArrayResponse = (
  response: GeneralResponse<BackendActivity[]>
) => ({
  success: response.code >= 200 && response.code < 300,
  message: response.message,
  timestamp: new Date().toISOString(),
  data: (response.data || []).map(mapBackendActivityToActivity),
});

export const mapActivityFormToBackendPayload = (
  data: CreateActivityRequest | UpdateActivityRequest
): CreateActivityRequest | UpdateActivityRequest => data;

const toEpochMillis = (value?: string) => {
  if (!value) {
    return undefined;
  }
  const parsed = new Date(value).getTime();
  return Number.isNaN(parsed) ? undefined : parsed;
};

const toLocalDateTimeString = (value?: string, isEnd = false) => {
  if (!value) {
    return undefined;
  }
  if (value.includes('T')) {
    return value;
  }
  if (/^\d{4}-\d{2}-\d{2}$/.test(value)) {
    return isEnd ? `${value}T23:59:59` : `${value}T00:00:00`;
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return undefined;
  }
  const pad = (n: number) => String(n).padStart(2, '0');
  const year = date.getFullYear();
  const month = pad(date.getMonth() + 1);
  const day = pad(date.getDate());
  return `${year}-${month}-${day}T${isEnd ? '23:59:59' : '00:00:00'}`;
};

export const mapActivityFiltersToBackendRequest = (
  filters: ActivityFilters,
  pagination: PaginationParams
) => ({
  keyword: filters.keyword?.trim() || undefined,
  types: filters.types && filters.types.length > 0 ? filters.types : undefined,
  statuses:
    filters.statuses && filters.statuses.length > 0
      ? filters.statuses
      : undefined,
  priorities:
    filters.priorities && filters.priorities.length > 0
      ? filters.priorities
      : undefined,
  assignedTo: filters.assignedTo ? Number(filters.assignedTo) : undefined,
  leadId: filters.leadId ? Number(filters.leadId) : undefined,
  accountId: filters.accountId ? Number(filters.accountId) : undefined,
  opportunityId: filters.opportunityId
    ? Number(filters.opportunityId)
    : undefined,
  contactId: filters.contactId ? Number(filters.contactId) : undefined,
  activityDateFrom: toLocalDateTimeString(filters.activityDateFrom),
  activityDateTo: toLocalDateTimeString(filters.activityDateTo, true),
  dueDateFrom: toLocalDateTimeString(filters.dueDateFrom),
  dueDateTo: toLocalDateTimeString(filters.dueDateTo, true),
  page: pagination.page,
  size: pagination.limit,
  sortBy: pagination.sortBy || undefined,
  sortDirection: pagination.sortOrder?.toUpperCase() || undefined,
});

export const mapBackendStatsToActivityStats = (
  response: GeneralResponse<ActivityStats>
): APIResponse<ActivityStats> => ({
  success: response.code >= 200 && response.code < 300,
  message: response.message,
  timestamp: new Date().toISOString(),
  data: {
    total: Number(response.data?.total || 0),
    overdue: Number(response.data?.overdue || 0),
    upcoming: Number(response.data?.upcoming || 0),
    byStatus: Object.fromEntries(
      Object.entries(response.data?.byStatus || {}).map(([key, value]) => [
        key,
        Number(value || 0),
      ])
    ),
    byType: Object.fromEntries(
      Object.entries(response.data?.byType || {}).map(([key, value]) => [
        key,
        Number(value || 0),
      ])
    ),
    byPriority: Object.fromEntries(
      Object.entries(response.data?.byPriority || {}).map(([key, value]) => [
        key,
        Number(value || 0),
      ])
    ),
  },
});

export const mapBulkActivityRequestToBackend = (
  request: BulkActivityRequest
): BulkActivityRequestPayload => ({
  activityIds: request.activityIds.map(Number),
  action: request.action,
  assigneeId: request.assigneeId ? Number(request.assigneeId) : undefined,
});

export const mapBulkActivityResponse = (
  response: GeneralResponse<BulkActivityResult>
): APIResponse<BulkActivityResult> => ({
  success: response.code >= 200 && response.code < 300,
  message: response.message,
  timestamp: new Date().toISOString(),
  data: {
    successCount: response.data?.successCount || 0,
    failedCount: response.data?.failedCount || 0,
    message: response.data?.message || response.message || undefined,
  },
});

type BackendMeetingRequest = {
  id?: number | string | null;
  teamId?: number | string | null;
  preferredUserId?: number | string | null;
  assignedTeamMemberId?: number | string | null;
  assignedUserId?: number | string | null;
  scheduledActivityId?: number | string | null;
  scheduledStartTime?: number | string | null;
  accountId?: number | string | null;
  accountName?: string | null;
  opportunityId?: number | string | null;
  opportunityName?: string | null;
  contactId?: number | string | null;
  contactName?: string | null;
  subject?: string | null;
  description?: string | null;
  location?: string | null;
  meetingType?: string | null;
  preferredTimeSlot?: string | null;
  earliestStart?: number | string | null;
  latestStart?: number | string | null;
  requestedDeadline?: number | string | null;
  durationMinutes?: number | string | null;
  status?: string | null;
  schedulingAttempts?: number | string | null;
  priorityScore?: number | string | null;
  failureReason?: string | null;
  tenantId?: number | string | null;
  createdAt?: number | string | null;
  updatedAt?: number | string | null;
  createdBy?: number | string | null;
  updatedBy?: number | string | null;
};

const mapBackendMeetingRequest = (
  row: BackendMeetingRequest
): MeetingRequest => ({
  id: String(row.id ?? ''),
  teamId: String(row.teamId ?? ''),
  preferredUserId:
    row.preferredUserId != null ? String(row.preferredUserId) : undefined,
  assignedTeamMemberId:
    row.assignedTeamMemberId != null
      ? String(row.assignedTeamMemberId)
      : undefined,
  assignedUserId:
    row.assignedUserId != null ? String(row.assignedUserId) : undefined,
  scheduledActivityId:
    row.scheduledActivityId != null
      ? String(row.scheduledActivityId)
      : undefined,
  scheduledStartTime:
    row.scheduledStartTime != null ? Number(row.scheduledStartTime) : undefined,
  accountId: String(row.accountId ?? ''),
  accountName: row.accountName?.trim() || undefined,
  opportunityId:
    row.opportunityId != null ? String(row.opportunityId) : undefined,
  opportunityName: row.opportunityName?.trim() || undefined,
  contactId: row.contactId != null ? String(row.contactId) : undefined,
  contactName: row.contactName?.trim() || undefined,
  subject: row.subject ?? undefined,
  description: row.description ?? undefined,
  location: row.location ?? undefined,
  meetingType: (row.meetingType as MeetingRequestType) || 'DISCOVERY',
  preferredTimeSlot:
    row.preferredTimeSlot as MeetingRequest['preferredTimeSlot'],
  earliestStart: Number(row.earliestStart ?? 0),
  latestStart: Number(row.latestStart ?? 0),
  requestedDeadline: Number(row.requestedDeadline ?? 0),
  durationMinutes:
    row.durationMinutes != null ? Number(row.durationMinutes) : undefined,
  status: (row.status as MeetingRequestStatus) || 'PENDING',
  schedulingAttempts:
    row.schedulingAttempts != null ? Number(row.schedulingAttempts) : undefined,
  priorityScore:
    row.priorityScore != null ? Number(row.priorityScore) : undefined,
  failureReason: row.failureReason ?? undefined,
  tenantId: row.tenantId != null ? String(row.tenantId) : undefined,
  createdAt: row.createdAt != null ? Number(row.createdAt) : undefined,
  updatedAt: row.updatedAt != null ? Number(row.updatedAt) : undefined,
  createdBy: row.createdBy != null ? String(row.createdBy) : undefined,
  updatedBy: row.updatedBy != null ? String(row.updatedBy) : undefined,
});

export const mapMeetingRequestListResponse = (
  response: GeneralResponse<PageResponse<BackendMeetingRequest>>
): APIResponse<{
  data: MeetingRequest[];
  pagination: {
    page: number;
    limit: number;
    total: number;
    totalPages: number;
    hasNext: boolean;
    hasPrevious: boolean;
  };
}> => ({
  success: response.code >= 200 && response.code < 300,
  message: response.message,
  timestamp: new Date().toISOString(),
  data: {
    data: (response.data?.items || []).map(mapBackendMeetingRequest),
    pagination: {
      page: response.data?.pagination?.page || 1,
      limit: response.data?.pagination?.size || 20,
      total: Number(response.data?.pagination?.totalItems || 0),
      totalPages: response.data?.pagination?.totalPages || 1,
      hasNext: !!response.data?.pagination?.hasNext,
      hasPrevious: !!response.data?.pagination?.hasPrevious,
    },
  },
});

export const mapSingleMeetingRequestResponse = (
  response: GeneralResponse<BackendMeetingRequest>
): APIResponse<MeetingRequest> => ({
  success: response.code >= 200 && response.code < 300,
  message: response.message,
  timestamp: new Date().toISOString(),
  data: mapBackendMeetingRequest(response.data),
});
