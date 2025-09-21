// Local Storage Service (authors: QuanTuanHuy, Description: Part of Serp Project)

import type {
  Customer,
  Lead,
  Opportunity,
  Activity,
  PaginatedResponse,
  CustomerFilters,
  LeadFilters,
  OpportunityFilters,
  ActivityFilters,
  PaginationParams,
} from '../types';
import { generateAllMockData } from './mockDataGenerator';

// Storage keys
const STORAGE_KEYS = {
  CUSTOMERS: 'crm_customers',
  LEADS: 'crm_leads',
  OPPORTUNITIES: 'crm_opportunities',
  ACTIVITIES: 'crm_activities',
  INITIALIZED: 'crm_initialized',
} as const;

// Base storage interface
interface StorageService<T> {
  getAll(): T[];
  getById(id: string): T | null;
  create(item: Omit<T, 'id' | 'createdAt' | 'updatedAt'>): T;
  update(id: string, updates: Partial<T>): T | null;
  delete(id: string): boolean;
  search(filters: any, pagination: PaginationParams): PaginatedResponse<T>;
}

// Generic storage implementation
class LocalStorageService<
  T extends { id: string; createdAt: string; updatedAt: string },
> implements StorageService<T>
{
  constructor(private storageKey: string) {}

  private getStorageData(): T[] {
    try {
      const data = localStorage.getItem(this.storageKey);
      return data ? JSON.parse(data) : [];
    } catch (error) {
      console.error(
        `Error reading ${this.storageKey} from localStorage:`,
        error
      );
      return [];
    }
  }

  private setStorageData(data: T[]): void {
    try {
      localStorage.setItem(this.storageKey, JSON.stringify(data));
    } catch (error) {
      console.error(`Error writing ${this.storageKey} to localStorage:`, error);
    }
  }

  getAll(): T[] {
    return this.getStorageData();
  }

  getById(id: string): T | null {
    const items = this.getStorageData();
    return items.find((item) => item.id === id) || null;
  }

  create(itemData: Omit<T, 'id' | 'createdAt' | 'updatedAt'>): T {
    const items = this.getStorageData();
    const now = new Date().toISOString();

    const newItem = {
      ...itemData,
      id: Math.random().toString(36).substr(2, 9),
      createdAt: now,
      updatedAt: now,
    } as T;

    items.push(newItem);
    this.setStorageData(items);

    return newItem;
  }

  update(id: string, updates: Partial<T>): T | null {
    const items = this.getStorageData();
    const index = items.findIndex((item) => item.id === id);

    if (index === -1) return null;

    const updatedItem = {
      ...items[index],
      ...updates,
      updatedAt: new Date().toISOString(),
    };

    items[index] = updatedItem;
    this.setStorageData(items);

    return updatedItem;
  }

  delete(id: string): boolean {
    const items = this.getStorageData();
    const filteredItems = items.filter((item) => item.id !== id);

    if (filteredItems.length === items.length) return false;

    this.setStorageData(filteredItems);
    return true;
  }

  search(filters: any, pagination: PaginationParams): PaginatedResponse<T> {
    let items = this.getStorageData();

    // Apply filters
    items = this.applyFilters(items, filters);

    // Apply sorting
    if (pagination.sortBy) {
      items = this.applySorting(
        items,
        pagination.sortBy,
        pagination.sortOrder || 'desc'
      );
    }

    // Apply pagination
    const total = items.length;
    const startIndex = (pagination.page - 1) * pagination.limit;
    const endIndex = startIndex + pagination.limit;
    const paginatedItems = items.slice(startIndex, endIndex);

    return {
      data: paginatedItems,
      pagination: {
        page: pagination.page,
        limit: pagination.limit,
        total,
        totalPages: Math.ceil(total / pagination.limit),
        hasNext: endIndex < total,
        hasPrevious: pagination.page > 1,
      },
    };
  }

  private applyFilters(items: T[], filters: any): T[] {
    return items.filter((item) => {
      for (const [key, value] of Object.entries(filters)) {
        if (value === undefined || value === null || value === '') continue;

        if (key === 'search') {
          // Generic search across text fields
          const searchValue = (value as string).toLowerCase();
          const searchableFields = [
            'name',
            'email',
            'firstName',
            'lastName',
            'subject',
            'description',
          ];
          const hasMatch = searchableFields.some((field) => {
            const fieldValue = (item as any)[field];
            return (
              fieldValue &&
              fieldValue.toString().toLowerCase().includes(searchValue)
            );
          });
          if (!hasMatch) return false;
        } else if (Array.isArray(value)) {
          // Array filters (status, type, etc.)
          if (value.length > 0 && !value.includes((item as any)[key]))
            return false;
        } else if (key.endsWith('From')) {
          // Date range filters (From)
          const dateField = key.replace('From', '');
          const itemDate = (item as any)[dateField];
          if (itemDate && new Date(itemDate) < new Date(value as string))
            return false;
        } else if (key.endsWith('To')) {
          // Date range filters (To)
          const dateField = key.replace('To', '');
          const itemDate = (item as any)[dateField];
          if (itemDate && new Date(itemDate) > new Date(value as string))
            return false;
        } else if (key.startsWith('min')) {
          // Min value filters
          const valueField = key.replace('min', '').toLowerCase();
          const itemValue = (item as any)[valueField];
          if (itemValue !== undefined && itemValue < (value as number))
            return false;
        } else if (key.startsWith('max')) {
          // Max value filters
          const valueField = key.replace('max', '').toLowerCase();
          const itemValue = (item as any)[valueField];
          if (itemValue !== undefined && itemValue > (value as number))
            return false;
        } else {
          // Exact match filters
          if ((item as any)[key] !== value) return false;
        }
      }
      return true;
    });
  }

  private applySorting(
    items: T[],
    sortBy: string,
    sortOrder: 'asc' | 'desc'
  ): T[] {
    return [...items].sort((a, b) => {
      const aValue = (a as any)[sortBy];
      const bValue = (b as any)[sortBy];

      if (aValue === null || aValue === undefined) return 1;
      if (bValue === null || bValue === undefined) return -1;

      let comparison = 0;

      if (typeof aValue === 'string') {
        comparison = aValue.localeCompare(bValue);
      } else if (typeof aValue === 'number') {
        comparison = aValue - bValue;
      } else if (aValue instanceof Date || typeof aValue === 'string') {
        comparison = new Date(aValue).getTime() - new Date(bValue).getTime();
      }

      return sortOrder === 'asc' ? comparison : -comparison;
    });
  }

  // Bulk operations
  bulkDelete(ids: string[]): { success: number; failed: number } {
    const items = this.getStorageData();
    const initialCount = items.length;
    const filteredItems = items.filter((item) => !ids.includes(item.id));

    this.setStorageData(filteredItems);

    const deletedCount = initialCount - filteredItems.length;
    return {
      success: deletedCount,
      failed: ids.length - deletedCount,
    };
  }

  bulkUpdate(updates: Array<{ id: string; data: Partial<T> }>): {
    success: number;
    failed: number;
  } {
    const items = this.getStorageData();
    let successCount = 0;

    updates.forEach(({ id, data }) => {
      const index = items.findIndex((item) => item.id === id);
      if (index !== -1) {
        items[index] = {
          ...items[index],
          ...data,
          updatedAt: new Date().toISOString(),
        };
        successCount++;
      }
    });

    this.setStorageData(items);

    return {
      success: successCount,
      failed: updates.length - successCount,
    };
  }
}

// Initialize mock data
const initializeMockData = () => {
  const isInitialized = localStorage.getItem(STORAGE_KEYS.INITIALIZED);

  if (!isInitialized) {
    console.log('Initializing CRM mock data...');
    const mockData = generateAllMockData();

    localStorage.setItem(
      STORAGE_KEYS.CUSTOMERS,
      JSON.stringify(mockData.customers)
    );
    localStorage.setItem(STORAGE_KEYS.LEADS, JSON.stringify(mockData.leads));
    localStorage.setItem(
      STORAGE_KEYS.OPPORTUNITIES,
      JSON.stringify(mockData.opportunities)
    );
    localStorage.setItem(
      STORAGE_KEYS.ACTIVITIES,
      JSON.stringify(mockData.activities)
    );
    localStorage.setItem(STORAGE_KEYS.INITIALIZED, 'true');

    console.log('Mock data initialized:', {
      customers: mockData.customers.length,
      leads: mockData.leads.length,
      opportunities: mockData.opportunities.length,
      activities: mockData.activities.length,
    });
  }
};

// Create service instances
export const customerService = new LocalStorageService<Customer>(
  STORAGE_KEYS.CUSTOMERS
);
export const leadService = new LocalStorageService<Lead>(STORAGE_KEYS.LEADS);
export const opportunityService = new LocalStorageService<Opportunity>(
  STORAGE_KEYS.OPPORTUNITIES
);
export const activityService = new LocalStorageService<Activity>(
  STORAGE_KEYS.ACTIVITIES
);

// Initialize on module load
if (typeof window !== 'undefined') {
  initializeMockData();
}

// Export utilities
export const resetMockData = () => {
  Object.values(STORAGE_KEYS).forEach((key) => {
    localStorage.removeItem(key);
  });
  initializeMockData();
};

export const exportData = () => {
  return {
    customers: customerService.getAll(),
    leads: leadService.getAll(),
    opportunities: opportunityService.getAll(),
    activities: activityService.getAll(),
  };
};

export const importData = (data: {
  customers?: Customer[];
  leads?: Lead[];
  opportunities?: Opportunity[];
  activities?: Activity[];
}) => {
  if (data.customers) {
    localStorage.setItem(
      STORAGE_KEYS.CUSTOMERS,
      JSON.stringify(data.customers)
    );
  }
  if (data.leads) {
    localStorage.setItem(STORAGE_KEYS.LEADS, JSON.stringify(data.leads));
  }
  if (data.opportunities) {
    localStorage.setItem(
      STORAGE_KEYS.OPPORTUNITIES,
      JSON.stringify(data.opportunities)
    );
  }
  if (data.activities) {
    localStorage.setItem(
      STORAGE_KEYS.ACTIVITIES,
      JSON.stringify(data.activities)
    );
  }
};
