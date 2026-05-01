import type {
  Account,
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
  Lead,
  LeadFilters,
  PaginationParams,
} from '../types';

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

type BackendLeadConversion = {
  leadId: number | string;
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
    address.country,
  ].filter(Boolean);
  return parts.length > 0 ? parts.join(', ') : undefined;
};

export const mapBackendAccountToAccount = (
  account: BackendAccount
): Account => ({
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
  customFields: {
    industry: account.industry || undefined,
    companySize: account.companySize || undefined,
    city: account.address?.city || undefined,
    state: account.address?.state || undefined,
    zipCode: account.address?.zipCode || undefined,
    country: account.address?.country || undefined,
    paymentTerms: account.paymentTerms || undefined,
    creditLimit: account.creditLimit || undefined,
    wonOpportunities: account.wonOpportunities || 0,
  },
  totalValue: Number(account.totalRevenue || 0),
  lastContactDate: undefined,
});

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
): Activity => ({
  id: String(activity.id),
  createdAt: toIsoString(activity.createdAt),
  updatedAt: toIsoString(activity.updatedAt),
  isActive: activity.status !== 'CANCELLED',
  type: (activity.activityType as Activity['type']) || 'TASK',
  status: (activity.status as Activity['status']) || 'PLANNED',
  subject: activity.subject || 'Untitled activity',
  description: activity.description || undefined,
  scheduledDate: toIsoString(activity.activityDate),
  actualDate: undefined,
  duration: activity.durationMinutes || undefined,
  priority: (activity.priority as Activity['priority']) || 'MEDIUM',
  assignedTo: String(activity.assignedTo || ''),
  assignedToName: 'Unassigned',
  relatedTo: {
    type: activity.accountId
      ? 'CUSTOMER'
      : activity.leadId
        ? 'LEAD'
        : 'OPPORTUNITY',
    id: String(
      activity.accountId || activity.leadId || activity.opportunityId || activity.contactId || ''
    ),
    name: activity.subject || 'Related item',
  },
  participants: [],
  location: activity.location || undefined,
  outcome: activity.outcome || undefined,
  followUpRequired: false,
  followUpDate: activity.dueDate ? toIsoString(activity.dueDate) : undefined,
  tags: [],
  customFields: {
    notes: activity.notes || undefined,
    reminderDate: activity.reminderDate ? toIsoString(activity.reminderDate) : undefined,
    attachments: activity.attachments || [],
    contactId: activity.contactId ? String(activity.contactId) : undefined,
  },
});

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
  assignedTo?: number | string | null
) => {
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
    customerName: accountId ? `Account #${accountId}` : undefined,
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
    assignedToName: mapBackendOpportunityAssignedToName(opportunity.assignedTo),
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

  return {
    name: data.name,
    email: data.email || undefined,
    phone: data.phone || undefined,
    website: data.website || undefined,
    notes: data.notes || undefined,
    accountType: data.customerType,
    industry: data.tags?.[0] || undefined,
    companySize: data.companySize || undefined,
    taxId: data.taxNumber || undefined,
    creditLimit: data.creditLimit,
    paymentTerms: data.paymentTerms || undefined,
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

export const mapLeadConversionResponse = (
  response: GeneralResponse<BackendLeadConversion>
) => ({
  success: response.code >= 200 && response.code < 300,
  message: response.message,
  timestamp: new Date().toISOString(),
  data: {
    leadId: String(response.data.leadId),
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
