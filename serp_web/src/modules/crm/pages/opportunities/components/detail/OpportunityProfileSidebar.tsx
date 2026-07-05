'use client';

import { useState } from 'react';
import { Edit, Check, X, Calendar, DollarSign, Building } from 'lucide-react';
import { Button, Input, Textarea, Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/shared/components/ui';
import { CRMDatePicker } from '../../../../components/shared';
import { formatCurrency, toLocalDateInputValue } from '../../../../utils';
import type { Opportunity } from '../../../../types';

interface OpportunityProfileSidebarProps {
  opportunity: Opportunity;
  accounts: any[];
  leads: any[];
  isUpdating: boolean;
  onUpdateOpportunity: (data: any) => Promise<void>;
}

export function OpportunityProfileSidebar({
  opportunity,
  accounts,
  leads,
  isUpdating,
  onUpdateOpportunity,
}: OpportunityProfileSidebarProps) {
  const [editingField, setEditingField] = useState<string | null>(null);
  const [tempValue, setTempValue] = useState<any>(null);

  const startEditing = (field: string, initialVal: any) => {
    setEditingField(field);
    setTempValue(initialVal);
  };

  const handleSave = async (field: string) => {
    try {
      let val = tempValue;
      if (field === 'estimatedValue') val = Number(tempValue) || 0;
      await onUpdateOpportunity({ [field]: val });
      setEditingField(null);
    } catch (err) {
      // Handled in parent toast
    }
  };

  const handleCancel = () => {
    setEditingField(null);
    setTempValue(null);
  };

  const getAccountName = (id?: string) => {
    const acc = accounts.find((a) => String(a.id) === String(id));
    return acc ? acc.name : opportunity.customerName || 'No account';
  };

  const getLeadName = (id?: string) => {
    const ld = leads.find((l) => String(l.id) === String(id));
    return ld ? (ld.name || ld.email) : 'No lead';
  };

  return (
    <div className="space-y-6">
      <div>
        <div className="text-xs text-muted-foreground uppercase tracking-wider font-semibold">Deal Info</div>
        {editingField === 'name' ? (
          <div className="flex items-center gap-1 mt-1">
            <Input value={tempValue} onChange={(e) => setTempValue(e.target.value)} className="h-8 text-sm" />
            <Button size="icon" variant="ghost" className="h-8 w-8 text-emerald-600" onClick={() => handleSave('name')} disabled={isUpdating}>
              <Check className="h-4 w-4" />
            </Button>
            <Button size="icon" variant="ghost" className="h-8 w-8 text-rose-600" onClick={handleCancel}>
              <X className="h-4 w-4" />
            </Button>
          </div>
        ) : (
          <h2 className="text-lg font-bold mt-1 group flex items-center gap-2 cursor-pointer hover:text-primary transition" onClick={() => startEditing('name', opportunity.name)}>
            {opportunity.name}
            <Edit className="h-3.5 w-3.5 text-muted-foreground opacity-0 group-hover:opacity-100 transition" />
          </h2>
        )}
      </div>

      <div className="space-y-4 pt-4 border-t border-muted/50 text-sm">
        {/* Account */}
        <div className="space-y-1">
          <label className="text-xs text-muted-foreground font-semibold uppercase flex items-center gap-1">
            <Building className="h-3 w-3" /> Account
          </label>
          {editingField === 'accountId' ? (
            <div className="flex items-center gap-1">
              <Select value={tempValue} onValueChange={setTempValue}>
                <SelectTrigger className="h-8 text-xs">
                  <SelectValue placeholder="Select account" />
                </SelectTrigger>
                <SelectContent>
                  {accounts.map((acc) => (
                    <SelectItem key={acc.id} value={acc.id}>{acc.name}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <Button size="icon" variant="ghost" className="h-8 w-8 text-emerald-600" onClick={() => handleSave('accountId')} disabled={isUpdating}>
                <Check className="h-4 w-4" />
              </Button>
              <Button size="icon" variant="ghost" className="h-8 w-8 text-rose-600" onClick={handleCancel}>
                <X className="h-4 w-4" />
              </Button>
            </div>
          ) : (
            <div className="group flex items-center justify-between cursor-pointer py-1 px-1.5 hover:bg-muted/40 rounded transition" onClick={() => startEditing('accountId', opportunity.accountId || '')}>
              <span className="font-medium text-foreground">{getAccountName(opportunity.accountId)}</span>
              <Edit className="h-3 w-3 text-muted-foreground opacity-0 group-hover:opacity-100 transition" />
            </div>
          )}
        </div>

        {/* Value */}
        <div className="space-y-1">
          <label className="text-xs text-muted-foreground font-semibold uppercase flex items-center gap-1">
            <DollarSign className="h-3 w-3" /> Estimated Value
          </label>
          {editingField === 'estimatedValue' ? (
            <div className="flex items-center gap-1">
              <Input type="number" value={tempValue} onChange={(e) => setTempValue(e.target.value)} className="h-8 text-sm" />
              <Button size="icon" variant="ghost" className="h-8 w-8 text-emerald-600" onClick={() => handleSave('estimatedValue')} disabled={isUpdating}>
                <Check className="h-4 w-4" />
              </Button>
              <Button size="icon" variant="ghost" className="h-8 w-8 text-rose-600" onClick={handleCancel}>
                <X className="h-4 w-4" />
              </Button>
            </div>
          ) : (
            <div className="group flex items-center justify-between cursor-pointer py-1 px-1.5 hover:bg-muted/40 rounded transition" onClick={() => startEditing('estimatedValue', opportunity.estimatedValue || 0)}>
              <span className="font-semibold text-emerald-700">{formatCurrency(opportunity.estimatedValue || 0)}</span>
              <Edit className="h-3 w-3 text-muted-foreground opacity-0 group-hover:opacity-100 transition" />
            </div>
          )}
        </div>

        {/* Expected Close Date */}
        <div className="space-y-1">
          <label className="text-xs text-muted-foreground font-semibold uppercase flex items-center gap-1">
            <Calendar className="h-3 w-3" /> Expected Close Date
          </label>
          {editingField === 'expectedCloseDate' ? (
            <div className="flex items-center gap-1">
              <CRMDatePicker value={tempValue ? new Date(tempValue) : undefined} onChange={(date) => setTempValue(date ? toLocalDateInputValue(date) : '')} />
              <Button size="icon" variant="ghost" className="h-8 w-8 text-emerald-600" onClick={() => handleSave('expectedCloseDate')} disabled={isUpdating}>
                <Check className="h-4 w-4" />
              </Button>
              <Button size="icon" variant="ghost" className="h-8 w-8 text-rose-600" onClick={handleCancel}>
                <X className="h-4 w-4" />
              </Button>
            </div>
          ) : (
            <div className="group flex items-center justify-between cursor-pointer py-1 px-1.5 hover:bg-muted/40 rounded transition" onClick={() => startEditing('expectedCloseDate', opportunity.expectedCloseDate)}>
              <span className="font-medium text-foreground">{opportunity.expectedCloseDate ? new Date(opportunity.expectedCloseDate).toLocaleDateString() : 'Not set'}</span>
              <Edit className="h-3 w-3 text-muted-foreground opacity-0 group-hover:opacity-100 transition" />
            </div>
          )}
        </div>

        {/* Description */}
        <div className="space-y-1 pt-2 border-t border-muted/30">
          <label className="text-xs text-muted-foreground font-semibold uppercase">Description</label>
          {editingField === 'description' ? (
            <div className="space-y-1">
              <Textarea value={tempValue} onChange={(e) => setTempValue(e.target.value)} rows={3} className="text-xs" />
              <div className="flex justify-end gap-1">
                <Button size="sm" variant="ghost" className="h-7 text-xs text-rose-600" onClick={handleCancel}>Cancel</Button>
                <Button size="sm" className="h-7 text-xs text-white bg-emerald-600 hover:bg-emerald-700" onClick={() => handleSave('description')} disabled={isUpdating}>Save</Button>
              </div>
            </div>
          ) : (
            <div className="group relative cursor-pointer p-2 hover:bg-muted/40 rounded border border-muted/20 transition min-h-[50px]" onClick={() => startEditing('description', opportunity.description || '')}>
              <p className="text-xs text-muted-foreground whitespace-pre-wrap leading-relaxed">{opportunity.description || 'Add opportunity description...'}</p>
              <Edit className="absolute top-2 right-2 h-3 w-3 text-muted-foreground opacity-0 group-hover:opacity-100 transition" />
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
