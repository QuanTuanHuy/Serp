import type {
  Account,
  Activity,
  APIResponse,
  Contact,
  CreateAccountRequest,
  UpdateAccountRequest,
  CustomerFilters,
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
  assignedTo?: number | string | null;
  activityDate?: number | string | null;
  dueDate?: number | string | null;
  durationMinutes?: number | null;
  priority?: string | null;
  location?: string | null;
  outcome?: string | null;
  notes?: string | null;
  createdAt?: number | string | null;
  updatedAt?: number | string | null;
};

const toIsoString = (value?: number | string | null): string => {
  if (!value) return new Date(0).toISOString();
  const timestamp = typeof value === 'string' ? Number(value) : value;
  if (Number.isFinite(timestamp)) return new Date(Number(timestamp)).toISOString();
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? new Date(0).toISOString() : date.toISOString();
};

const joinAddress = (address?: BackendAddress | null): string | undefined => {
  if (!address) return undefined;
  const parts = [address.street, address.city, address.state, address.country].filter(Boolean);
  return parts.length > 0 ? parts.join(', ') : undefined;
};

export const mapBackendAccountToAccount = (account: BackendAccount): Account => ({
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

export const mapBackendContactToContact = (contact: BackendContact): Contact => {
  const name = (contact.name || '').trim();
  const parts = name.split(/\s+/).filter(Boolean);
  const lastName = parts.length > 1 ? parts[0] : '';
  const firstName = parts.length > 1 ? parts.slice(1).join(' ') : parts[0] || '';

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

export const mapBackendActivityToActivity = (activity: BackendActivity): Activity => ({
  id: String(activity.id),
  createdAt: toIsoString(activity.createdAt),
  updatedAt: toIsoString(activity.updatedAt),
  isActive: activity.status !== 'CANCELLED',
  type: (activity.activityType as Activity['type']) || 'NOTE',
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
    type: activity.accountId ? 'CUSTOMER' : activity.leadId ? 'LEAD' : 'OPPORTUNITY',
    id: String(activity.accountId || activity.leadId || activity.opportunityId || ''),
    name: activity.subject || 'Related item',
  },
  participants: [],
  location: activity.location || undefined,
  outcome: activity.outcome || undefined,
  followUpRequired: false,
  followUpDate: activity.dueDate ? toIsoString(activity.dueDate) : undefined,
  tags: [],
  customFields: { notes: activity.notes || undefined },
});

export const mapAccountFormToBackendPayload = (
  data: CreateAccountRequest | UpdateAccountRequest
) => {
  const address = data.address?.split(',').map((part) => part.trim()).filter(Boolean) || [];

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

export const mapCustomerFiltersToAccountSearch = (
  filters: CustomerFilters = {},
  pagination: PaginationParams
) => ({
  keyword: filters.search || undefined,
  statuses: filters.status?.map((status) => (status === 'ACTIVE' ? 'ACTIVE' : 'INACTIVE')),
  accountType: filters.type?.[0],
  page: pagination.page,
  size: pagination.limit,
  sortBy: pagination.sortBy,
  sortDirection: pagination.sortOrder?.toUpperCase(),
});

export const mapAccountListResponse = (
  response: GeneralResponse<PageResponse<BackendAccount>>
) : APIResponse<{ data: Account[]; pagination: { page: number; limit: number; total: number; totalPages: number; hasNext: boolean; hasPrevious: boolean; } }> => ({
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

export const mapSingleAccountResponse = (response: GeneralResponse<BackendAccount>) => ({
  success: response.code >= 200 && response.code < 300,
  message: response.message,
  timestamp: new Date().toISOString(),
  data: mapBackendAccountToAccount(response.data),
});

export const mapContactListResponse = (response: GeneralResponse<BackendContact[]>) => ({
  success: response.code >= 200 && response.code < 300,
  message: response.message,
  timestamp: new Date().toISOString(),
  data: (response.data || []).map(mapBackendContactToContact),
});

export const mapActivityListResponse = (
  response: GeneralResponse<PageResponse<BackendActivity>>
) : APIResponse<{ data: Activity[]; pagination: { page: number; limit: number; total: number; totalPages: number; hasNext: boolean; hasPrevious: boolean; } }> => ({
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
