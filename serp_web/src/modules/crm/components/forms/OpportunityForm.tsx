// OpportunityForm Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { useEffect, useMemo } from 'react';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Sparkles } from 'lucide-react';

import {
  Button,
  Input,
  Label,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Textarea,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { useGetAccountsQuery, useGetLeadsQuery } from '../../api/crmApi';
import { formatCurrency, toLocalDateInputValue } from '../../utils';
import { CRMDatePicker, CRMUserSelect } from '../shared';
import type {
  CreateOpportunityRequest,
  Opportunity,
  OpportunityStage,
  UpdateOpportunityRequest,
} from '../../types';

const opportunitySchema = z.object({
  name: z
    .string()
    .trim()
    .min(1, 'Opportunity name is required.')
    .max(255, 'Name is too long.'),
  accountId: z.string().trim().min(1, 'Account is required.'),
  leadId: z.string().trim().optional(),
  stage: z.enum([
    'PROSPECTING',
    'QUALIFICATION',
    'PROPOSAL',
    'NEGOTIATION',
    'CLOSED_WON',
    'CLOSED_LOST',
  ]),
  estimatedValue: z
    .number()
    .min(0, 'Estimated value must be greater than or equal to 0.'),
  expectedCloseDate: z.string().min(1, 'Expected close date is required.'),
  assignedTo: z.string().trim().optional(),
  description: z
    .string()
    .max(1000, 'Description must not exceed 1000 characters.')
    .optional(),
  notes: z
    .string()
    .max(1000, 'Notes must not exceed 1000 characters.')
    .optional(),
});

type OpportunityFormData = z.infer<typeof opportunitySchema>;

interface OpportunityFormProps {
  opportunity?: Opportunity;
  onSubmit: (
    data: CreateOpportunityRequest | UpdateOpportunityRequest
  ) => Promise<void>;
  onCancel?: () => void;
  isLoading?: boolean;
  className?: string;
}

const defaultValues: OpportunityFormData = {
  name: '',
  accountId: '',
  leadId: '',
  stage: 'PROSPECTING',
  estimatedValue: 0,
  expectedCloseDate: '',
  assignedTo: '',
  description: '',
  notes: '',
};

const formatExpectedCloseDateForInput = (iso: string | undefined): string => {
  if (!iso?.trim()) return '';
  return iso.includes('T') ? iso.split('T')[0]! : iso.slice(0, 10);
};

const opportunityToFormValues = (
  opportunity: Opportunity
): OpportunityFormData => ({
  name: opportunity.name,
  accountId: opportunity.accountId || opportunity.customerId || '',
  leadId: opportunity.leadId || '',
  stage: opportunity.stage,
  estimatedValue: opportunity.estimatedValue ?? opportunity.value ?? 0,
  expectedCloseDate: formatExpectedCloseDateForInput(
    opportunity.expectedCloseDate
  ),
  assignedTo: opportunity.assignedTo || '',
  description: opportunity.description || '',
  notes: opportunity.notes || '',
});

export const OpportunityForm: React.FC<OpportunityFormProps> = ({
  opportunity,
  onSubmit,
  onCancel,
  isLoading = false,
  className,
}) => {
  const isEditing = !!opportunity;

  const { data: accountsResponse, isLoading: isAccountsLoading } =
    useGetAccountsQuery({
      filters: {},
      pagination: { page: 1, limit: 100, sortBy: 'name', sortOrder: 'asc' },
    });
  const { data: leadsResponse, isLoading: isLeadsLoading } = useGetLeadsQuery({
    filters: {},
    pagination: { page: 1, limit: 100, sortBy: 'name', sortOrder: 'asc' },
  });

  const accounts = useMemo(
    () => accountsResponse?.data?.data || [],
    [accountsResponse]
  );
  const leads = useMemo(() => leadsResponse?.data?.data || [], [leadsResponse]);

  const {
    register,
    handleSubmit,
    control,
    watch,
    reset,
    formState: { errors, isSubmitting },
    setValue,
  } = useForm<OpportunityFormData>({
    resolver: zodResolver(opportunitySchema),
    defaultValues,
  });

  useEffect(() => {
    if (opportunity) {
      reset(opportunityToFormValues(opportunity));
    } else {
      reset(defaultValues);
    }
  }, [opportunity?.id, opportunity?.updatedAt, reset]);

  const watchedStage = watch('stage');
  const estimatedValue = watch('estimatedValue') || 0;

  const stageProbability: Record<OpportunityStage, number> = {
    PROSPECTING: 10,
    QUALIFICATION: 25,
    PROPOSAL: 50,
    NEGOTIATION: 75,
    CLOSED_WON: 100,
    CLOSED_LOST: 0,
  };

  const computedProbability = stageProbability[watchedStage];
  const weightedValue = (estimatedValue * computedProbability) / 100;

  const onFormSubmit = handleSubmit(async (data) => {
    await onSubmit({
      name: data.name,
      accountId: data.accountId,
      leadId: data.leadId?.trim() ? data.leadId.trim() : undefined,
      stage: data.stage,
      estimatedValue: data.estimatedValue,
      expectedCloseDate: data.expectedCloseDate,
      assignedTo: data.assignedTo?.trim() ? data.assignedTo.trim() : undefined,
      description: data.description?.trim() || undefined,
      notes: data.notes?.trim() || undefined,
    });
  });

  return (
    <div className={cn("grid grid-cols-1 lg:grid-cols-3 gap-6", className)}>
      <Card className="lg:col-span-2 border border-muted/50 shadow-sm rounded-xl">
        <CardHeader>
          <CardTitle className="text-xl font-extrabold tracking-tight text-foreground">
            {isEditing ? 'Edit Opportunity Profile' : 'Create Opportunity Profile'}
          </CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={onFormSubmit} className="space-y-6">
            <div className="space-y-4">
              <h3 className="text-sm font-semibold uppercase tracking-wider text-muted-foreground">Basic Information</h3>
              
              <div className="space-y-2">
                <Label htmlFor="name">Opportunity Name *</Label>
                <Input
                  id="name"
                  {...register('name')}
                  placeholder="e.g. Enterprise CRM Deal"
                  className={errors.name ? 'border-destructive' : ''}
                  disabled={isLoading}
                />
                {errors.name && <p className="text-xs text-destructive">{errors.name.message}</p>}
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>Account *</Label>
                  <Select
                    value={watch('accountId')}
                    onValueChange={(value) => setValue('accountId', value)}
                    disabled={isLoading || isAccountsLoading}
                  >
                    <SelectTrigger className={errors.accountId ? 'border-destructive' : ''}>
                      <SelectValue placeholder={isAccountsLoading ? 'Loading accounts...' : 'Select account'} />
                    </SelectTrigger>
                    <SelectContent>
                      {accounts.map((acc) => (
                        <SelectItem key={acc.id} value={acc.id}>{acc.name}</SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  {errors.accountId && <p className="text-xs text-destructive">{errors.accountId.message}</p>}
                </div>

                <div className="space-y-2">
                  <Label>Lead Source</Label>
                  <Select
                    value={watch('leadId') || ''}
                    onValueChange={(value) => setValue('leadId', value)}
                    disabled={isLoading || isLeadsLoading}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder={isLeadsLoading ? 'Loading leads...' : 'Select lead (optional)'} />
                    </SelectTrigger>
                    <SelectContent>
                      {leads.map((ld) => (
                        <SelectItem key={ld.id} value={ld.id}>{ld.name || ld.email || `Lead #${ld.id}`}</SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>
            </div>

            <div className="space-y-4 pt-4 border-t border-muted/30">
              <h3 className="text-sm font-semibold uppercase tracking-wider text-muted-foreground">Pipeline Settings</h3>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>Stage *</Label>
                  <Select
                    value={watch('stage')}
                    onValueChange={(value) => setValue('stage', value as OpportunityStage)}
                    disabled={isLoading}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select stage" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="PROSPECTING">Prospecting</SelectItem>
                      <SelectItem value="QUALIFICATION">Qualification</SelectItem>
                      <SelectItem value="PROPOSAL">Proposal</SelectItem>
                      <SelectItem value="NEGOTIATION">Negotiation</SelectItem>
                      <SelectItem value="CLOSED_WON">Closed Won</SelectItem>
                      <SelectItem value="CLOSED_LOST">Closed Lost</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="assignedTo">Assigned Rep</Label>
                  <Controller
                    name="assignedTo"
                    control={control}
                    render={({ field }) => (
                      <CRMUserSelect
                        id="assignedTo"
                        value={field.value}
                        onChange={field.onChange}
                        fallbackUserName={opportunity?.assignedToName}
                        disabled={isLoading}
                      />
                    )}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="estimatedValue">Estimated Value *</Label>
                  <Input
                    id="estimatedValue"
                    type="number"
                    min={0}
                    {...register('estimatedValue', { valueAsNumber: true })}
                    placeholder="0"
                    className={errors.estimatedValue ? 'border-destructive' : ''}
                    disabled={isLoading}
                  />
                  {errors.estimatedValue && <p className="text-xs text-destructive">{errors.estimatedValue.message}</p>}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="expectedCloseDate">Expected Close Date *</Label>
                  <Controller
                    name="expectedCloseDate"
                    control={control}
                    render={({ field }) => (
                      <CRMDatePicker
                        id="expectedCloseDate"
                        value={field.value}
                        onChange={(date) => field.onChange(date ? toLocalDateInputValue(date) : '')}
                        disabled={isLoading}
                        className={errors.expectedCloseDate ? 'border-destructive' : ''}
                      />
                    )}
                  />
                  {errors.expectedCloseDate && <p className="text-xs text-destructive">{errors.expectedCloseDate.message}</p>}
                </div>
              </div>
            </div>

            <div className="space-y-4 pt-4 border-t border-muted/30">
              <h3 className="text-sm font-semibold uppercase tracking-wider text-muted-foreground">Additional Information</h3>
              <div className="space-y-2">
                <Label htmlFor="description">Deal Description</Label>
                <Textarea id="description" {...register('description')} rows={3} placeholder="Describe the opportunity terms..." disabled={isLoading} />
              </div>
              <div className="space-y-2">
                <Label htmlFor="notes">Static Notes</Label>
                <Textarea id="notes" {...register('notes')} rows={2} placeholder="Add miscellaneous remarks..." disabled={isLoading} />
              </div>
            </div>

            <div className="flex justify-end space-x-3 pt-6 border-t border-muted/30">
              {onCancel && (
                <Button type="button" variant="outline" onClick={onCancel} disabled={isLoading || isSubmitting}>
                  Cancel
                </Button>
              )}
              <Button type="submit" disabled={isLoading || isSubmitting}>
                {isSubmitting ? 'Saving...' : isEditing ? 'Update Opportunity' : 'Create Opportunity'}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      {/* Live Calculator Column */}
      <div className="lg:col-span-1 space-y-6">
        <Card className="border border-muted/50 shadow-sm rounded-xl overflow-hidden sticky top-6">
          <CardHeader className="bg-primary/[0.03] border-b border-muted/40">
            <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground flex items-center gap-1.5">
              <Sparkles className="h-4 w-4 text-amber-500 animate-pulse" /> Live Deal Calculator
            </CardTitle>
          </CardHeader>
          <CardContent className="p-5 space-y-6">
            <div className="space-y-1">
              <div className="text-xs text-muted-foreground uppercase font-semibold">Deal Stage</div>
              <div className="text-sm font-bold text-foreground">{watchedStage}</div>
            </div>
            
            <div className="space-y-1">
              <div className="text-xs text-muted-foreground uppercase font-semibold">Win Probability</div>
              <div className="text-3xl font-extrabold text-primary">{computedProbability}%</div>
            </div>

            <div className="space-y-1 pt-4 border-t border-muted/30">
              <div className="text-xs text-muted-foreground uppercase font-semibold">Weighted Value</div>
              <div className="text-xl font-bold text-emerald-600">{formatCurrency(weightedValue)}</div>
              <div className="text-[10px] text-muted-foreground italic mt-1">
                Weighted Value = Est. Value ({formatCurrency(estimatedValue)}) × Prob. ({computedProbability}%)
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default OpportunityForm;
