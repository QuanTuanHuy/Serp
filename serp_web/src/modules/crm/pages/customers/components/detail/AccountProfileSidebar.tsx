'use client';

import { useState } from 'react';
import { Edit, Check, X, Building2, Mail, Phone, Globe, MapPin, Tag } from 'lucide-react';
import { Input, Button, Textarea } from '@/shared/components/ui';
import type { Customer } from '../../../../types';
import { formatCurrency } from '../../../../utils';

interface AccountProfileSidebarProps {
  customer: Customer;
  isUpdating: boolean;
  onUpdateAccount: (data: any) => Promise<void>;
}

export function AccountProfileSidebar({
  customer,
  isUpdating,
  onUpdateAccount,
}: AccountProfileSidebarProps) {
  const [editingField, setEditingField] = useState<string | null>(null);
  const [tempValue, setTempValue] = useState<any>(null);

  const startEditing = (field: string, val: any) => {
    setEditingField(field);
    setTempValue(val);
  };

  const handleSave = async (field: string) => {
    try {
      await onUpdateAccount({ [field]: tempValue });
      setEditingField(null);
    } catch {
      // Handled in parent toast
    }
  };

  const handleCancel = () => {
    setEditingField(null);
    setTempValue(null);
  };

  return (
    <div className="space-y-6 text-sm">
      <div>
        <div className="text-xs text-muted-foreground uppercase tracking-wider font-semibold">Account Profile</div>
        {editingField === 'name' ? (
          <div className="flex items-center gap-1 mt-1">
            <Input value={tempValue || ''} onChange={(e) => setTempValue(e.target.value)} className="h-8 text-sm" />
            <Button size="icon" variant="ghost" className="h-8 w-8 text-emerald-600" onClick={() => handleSave('name')} disabled={isUpdating}>
              <Check className="h-4 w-4" />
            </Button>
            <Button size="icon" variant="ghost" className="h-8 w-8 text-rose-600" onClick={handleCancel}>
              <X className="h-4 w-4" />
            </Button>
          </div>
        ) : (
          <h2 className="text-lg font-bold mt-1 group flex items-center gap-2 cursor-pointer hover:text-primary transition" onClick={() => startEditing('name', customer.name)}>
            {customer.name}
            <Edit className="h-3.5 w-3.5 text-muted-foreground opacity-0 group-hover:opacity-100 transition" />
          </h2>
        )}
      </div>

      <div className="space-y-4 pt-4 border-t border-muted/50">
        {/* Company Name */}
        <div className="space-y-1">
          <label className="text-xs text-muted-foreground font-semibold uppercase flex items-center gap-1">
            <Building2 className="h-3 w-3" /> Company
          </label>
          {editingField === 'companyName' ? (
            <div className="flex items-center gap-1">
              <Input value={tempValue || ''} onChange={(e) => setTempValue(e.target.value)} className="h-8 text-sm" />
              <Button size="icon" variant="ghost" className="h-8 w-8 text-emerald-600" onClick={() => handleSave('companyName')} disabled={isUpdating}>
                <Check className="h-4 w-4" />
              </Button>
              <Button size="icon" variant="ghost" className="h-8 w-8 text-rose-600" onClick={handleCancel}>
                <X className="h-4 w-4" />
              </Button>
            </div>
          ) : (
            <div className="group flex items-center justify-between cursor-pointer py-1 px-1.5 hover:bg-muted/40 rounded transition" onClick={() => startEditing('companyName', customer.companyName || '')}>
              <span className="text-foreground">{customer.companyName || 'Not set'}</span>
              <Edit className="h-3 w-3 text-muted-foreground opacity-0 group-hover:opacity-100 transition" />
            </div>
          )}
        </div>

        {/* Email */}
        <div className="space-y-1">
          <label className="text-xs text-muted-foreground font-semibold uppercase flex items-center gap-1">
            <Mail className="h-3 w-3" /> Email
          </label>
          {editingField === 'email' ? (
            <div className="flex items-center gap-1">
              <Input value={tempValue || ''} onChange={(e) => setTempValue(e.target.value)} className="h-8 text-sm" />
              <Button size="icon" variant="ghost" className="h-8 w-8 text-emerald-600" onClick={() => handleSave('email')} disabled={isUpdating}>
                <Check className="h-4 w-4" />
              </Button>
              <Button size="icon" variant="ghost" className="h-8 w-8 text-rose-600" onClick={handleCancel}>
                <X className="h-4 w-4" />
              </Button>
            </div>
          ) : (
            <div className="group flex items-center justify-between cursor-pointer py-1 px-1.5 hover:bg-muted/40 rounded transition" onClick={() => startEditing('email', customer.email)}>
              <span className="text-foreground">{customer.email}</span>
              <Edit className="h-3 w-3 text-muted-foreground opacity-0 group-hover:opacity-100 transition" />
            </div>
          )}
        </div>

        {/* Phone */}
        <div className="space-y-1">
          <label className="text-xs text-muted-foreground font-semibold uppercase flex items-center gap-1">
            <Phone className="h-3 w-3" /> Phone
          </label>
          {editingField === 'phone' ? (
            <div className="flex items-center gap-1">
              <Input value={tempValue || ''} onChange={(e) => setTempValue(e.target.value)} className="h-8 text-sm" />
              <Button size="icon" variant="ghost" className="h-8 w-8 text-emerald-600" onClick={() => handleSave('phone')} disabled={isUpdating}>
                <Check className="h-4 w-4" />
              </Button>
              <Button size="icon" variant="ghost" className="h-8 w-8 text-rose-600" onClick={handleCancel}>
                <X className="h-4 w-4" />
              </Button>
            </div>
          ) : (
            <div className="group flex items-center justify-between cursor-pointer py-1 px-1.5 hover:bg-muted/40 rounded transition" onClick={() => startEditing('phone', customer.phone || '')}>
              <span className="text-foreground">{customer.phone || 'N/A'}</span>
              <Edit className="h-3 w-3 text-muted-foreground opacity-0 group-hover:opacity-100 transition" />
            </div>
          )}
        </div>

        {/* Industry */}
        <div className="space-y-1">
          <label className="text-xs text-muted-foreground font-semibold uppercase flex items-center gap-1">
            <Tag className="h-3 w-3" /> Industry
          </label>
          {editingField === 'industry' ? (
            <div className="flex items-center gap-1">
              <Input value={tempValue || ''} onChange={(e) => setTempValue(e.target.value)} className="h-8 text-sm" />
              <Button size="icon" variant="ghost" className="h-8 w-8 text-emerald-600" onClick={() => handleSave('industry')} disabled={isUpdating}>
                <Check className="h-4 w-4" />
              </Button>
              <Button size="icon" variant="ghost" className="h-8 w-8 text-rose-600" onClick={handleCancel}>
                <X className="h-4 w-4" />
              </Button>
            </div>
          ) : (
            <div className="group flex items-center justify-between cursor-pointer py-1 px-1.5 hover:bg-muted/40 rounded transition" onClick={() => startEditing('industry', customer.industry || '')}>
              <span className="text-foreground capitalize">{customer.industry || 'N/A'}</span>
              <Edit className="h-3 w-3 text-muted-foreground opacity-0 group-hover:opacity-100 transition" />
            </div>
          )}
        </div>

        {/* Tax Number */}
        <div className="space-y-1">
          <label className="text-xs text-muted-foreground font-semibold uppercase">Tax Number</label>
          {editingField === 'taxNumber' ? (
            <div className="flex items-center gap-1">
              <Input value={tempValue || ''} onChange={(e) => setTempValue(e.target.value)} className="h-8 text-sm" />
              <Button size="icon" variant="ghost" className="h-8 w-8 text-emerald-600" onClick={() => handleSave('taxNumber')} disabled={isUpdating}>
                <Check className="h-4 w-4" />
              </Button>
              <Button size="icon" variant="ghost" className="h-8 w-8 text-rose-600" onClick={handleCancel}>
                <X className="h-4 w-4" />
              </Button>
            </div>
          ) : (
            <div className="group flex items-center justify-between cursor-pointer py-1 px-1.5 hover:bg-muted/40 rounded transition" onClick={() => startEditing('taxNumber', customer.taxNumber || '')}>
              <span className="text-foreground">{customer.taxNumber || 'N/A'}</span>
              <Edit className="h-3 w-3 text-muted-foreground opacity-0 group-hover:opacity-100 transition" />
            </div>
          )}
        </div>

        {/* Address */}
        <div className="space-y-1">
          <label className="text-xs text-muted-foreground font-semibold uppercase flex items-center gap-1">
            <MapPin className="h-3 w-3" /> Address
          </label>
          {editingField === 'address' ? (
            <div className="space-y-1">
              <Textarea value={tempValue || ''} onChange={(e) => setTempValue(e.target.value)} rows={2} className="text-xs animate-in fade-in duration-300" />
              <div className="flex justify-end gap-1">
                <Button size="sm" variant="ghost" className="h-7 text-xs text-rose-600" onClick={handleCancel}>Cancel</Button>
                <Button size="sm" className="h-7 text-xs text-white bg-emerald-600 hover:bg-emerald-700" onClick={() => handleSave('address')} disabled={isUpdating}>Save</Button>
              </div>
            </div>
          ) : (
            <div className="group relative cursor-pointer p-2 hover:bg-muted/40 rounded border border-muted/20 transition min-h-[40px]" onClick={() => startEditing('address', customer.address || '')}>
              <p className="text-xs whitespace-pre-wrap leading-relaxed">{customer.address || 'Add address...'}</p>
              <Edit className="absolute top-2 right-2 h-3 w-3 text-muted-foreground opacity-0 group-hover:opacity-100 transition" />
            </div>
          )}
        </div>

        {/* Notes */}
        <div className="space-y-1 pt-2 border-t border-muted/30">
          <label className="text-xs text-muted-foreground font-semibold uppercase">Account Notes</label>
          {editingField === 'notes' ? (
            <div className="space-y-1">
              <Textarea value={tempValue || ''} onChange={(e) => setTempValue(e.target.value)} rows={3} className="text-xs animate-in fade-in duration-300" />
              <div className="flex justify-end gap-1">
                <Button size="sm" variant="ghost" className="h-7 text-xs text-rose-600" onClick={handleCancel}>Cancel</Button>
                <Button size="sm" className="h-7 text-xs text-white bg-emerald-600 hover:bg-emerald-700" onClick={() => handleSave('notes')} disabled={isUpdating}>Save</Button>
              </div>
            </div>
          ) : (
            <div className="group relative cursor-pointer p-2 hover:bg-muted/40 rounded border border-muted/20 transition min-h-[50px]" onClick={() => startEditing('notes', customer.notes || '')}>
              <p className="text-xs text-muted-foreground whitespace-pre-wrap leading-relaxed">{customer.notes || 'Add account remarks...'}</p>
              <Edit className="absolute top-2 right-2 h-3 w-3 text-muted-foreground opacity-0 group-hover:opacity-100 transition" />
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
