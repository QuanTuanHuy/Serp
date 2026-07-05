import { useState } from 'react';
import { Edit, Mail, Phone, Building2, Briefcase, Calendar, DollarSign, Loader2 } from 'lucide-react';
import { Avatar, AvatarFallback, Badge, Input } from '@/shared/components/ui';
import type { Lead } from '../../../../types';

interface LeadProfileSidebarProps {
  lead: Lead;
  isUpdating: boolean;
  onUpdateLead: (data: Partial<Lead>) => Promise<void>;
}

export function LeadProfileSidebar({ lead, isUpdating, onUpdateLead }: LeadProfileSidebarProps) {
  const [editingField, setEditingField] = useState<string | null>(null);
  const [editValue, setEditValue] = useState<string>('');

  const handleStartEdit = (field: string, val: string) => {
    setEditingField(field);
    setEditValue(val);
  };

  const handleSave = async (field: keyof Lead) => {
    if (editValue === String(lead[field])) {
      setEditingField(null);
      return;
    }
    try {
      let typedValue: any = editValue;
      if (field === 'estimatedValue') typedValue = Number(editValue) || 0;
      await onUpdateLead({ [field]: typedValue });
    } catch (err) {
      // Parent will toast error
    } finally {
      setEditingField(null);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent, field: keyof Lead) => {
    if (e.key === 'Enter') handleSave(field);
    if (e.key === 'Escape') setEditingField(null);
  };

  const initials = lead.name ? lead.name.slice(0, 2).toUpperCase() : 'LD';

  return (
    <div className="space-y-6">
      <div className="flex flex-col items-center text-center space-y-3">
        <Avatar className="h-20 w-20 ring-4 ring-muted shadow-sm">
          <AvatarFallback className="bg-gradient-to-tr from-blue-500 to-indigo-600 text-white text-xl font-bold">
            {initials}
          </AvatarFallback>
        </Avatar>
        <div>
          <h2 className="text-xl font-bold text-foreground">{lead.name || 'Unnamed Lead'}</h2>
          <div className="mt-1 flex items-center justify-center gap-2">
            <Badge variant="secondary">{lead.leadStatus}</Badge>
            <Badge variant="outline">{lead.leadSource}</Badge>
          </div>
        </div>
      </div>

      <div className="border-t border-muted/50 pt-4 space-y-4">
        <h3 className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Contact Info</h3>
        
        {/* Email field */}
        <div className="group relative py-1 rounded-md px-2 hover:bg-muted/30 transition-colors">
          <span className="text-xs text-muted-foreground block">Email</span>
          {editingField === 'email' ? (
            <Input
              value={editValue}
              onChange={(e) => setEditValue(e.target.value)}
              onBlur={() => handleSave('email')}
              onKeyDown={(e) => handleKeyDown(e, 'email')}
              autoFocus
              className="h-8 py-1 mt-1 text-sm"
            />
          ) : (
            <div onClick={() => handleStartEdit('email', lead.email || '')} className="flex items-center gap-2 cursor-pointer mt-1">
              <Mail className="h-4 w-4 text-muted-foreground" />
              <span className="text-sm font-medium text-foreground">{lead.email || 'Click to add email'}</span>
              <Edit className="h-3 w-3 opacity-0 group-hover:opacity-100 ml-auto text-muted-foreground transition-opacity" />
            </div>
          )}
        </div>

        {/* Phone field */}
        <div className="group relative py-1 rounded-md px-2 hover:bg-muted/30 transition-colors">
          <span className="text-xs text-muted-foreground block">Phone</span>
          {editingField === 'phone' ? (
            <Input
              value={editValue}
              onChange={(e) => setEditValue(e.target.value)}
              onBlur={() => handleSave('phone')}
              onKeyDown={(e) => handleKeyDown(e, 'phone')}
              autoFocus
              className="h-8 py-1 mt-1 text-sm"
            />
          ) : (
            <div onClick={() => handleStartEdit('phone', lead.phone || '')} className="flex items-center gap-2 cursor-pointer mt-1">
              <Phone className="h-4 w-4 text-muted-foreground" />
              <span className="text-sm font-medium text-foreground">{lead.phone || 'Click to add phone'}</span>
              <Edit className="h-3 w-3 opacity-0 group-hover:opacity-100 ml-auto text-muted-foreground transition-opacity" />
            </div>
          )}
        </div>

        {/* Company field */}
        <div className="group relative py-1 rounded-md px-2 hover:bg-muted/30 transition-colors">
          <span className="text-xs text-muted-foreground block">Company</span>
          {editingField === 'company' ? (
            <Input
              value={editValue}
              onChange={(e) => setEditValue(e.target.value)}
              onBlur={() => handleSave('company')}
              onKeyDown={(e) => handleKeyDown(e, 'company')}
              autoFocus
              className="h-8 py-1 mt-1 text-sm"
            />
          ) : (
            <div onClick={() => handleStartEdit('company', lead.company || '')} className="flex items-center gap-2 cursor-pointer mt-1">
              <Building2 className="h-4 w-4 text-muted-foreground" />
              <span className="text-sm font-medium text-foreground">{lead.company || 'Click to add company'}</span>
              <Edit className="h-3 w-3 opacity-0 group-hover:opacity-100 ml-auto text-muted-foreground transition-opacity" />
            </div>
          )}
        </div>

        {/* Job Title field */}
        <div className="group relative py-1 rounded-md px-2 hover:bg-muted/30 transition-colors">
          <span className="text-xs text-muted-foreground block">Job Title</span>
          {editingField === 'jobTitle' ? (
            <Input
              value={editValue}
              onChange={(e) => setEditValue(e.target.value)}
              onBlur={() => handleSave('jobTitle')}
              onKeyDown={(e) => handleKeyDown(e, 'jobTitle')}
              autoFocus
              className="h-8 py-1 mt-1 text-sm"
            />
          ) : (
            <div onClick={() => handleStartEdit('jobTitle', lead.jobTitle || '')} className="flex items-center gap-2 cursor-pointer mt-1">
              <Briefcase className="h-4 w-4 text-muted-foreground" />
              <span className="text-sm font-medium text-foreground">{lead.jobTitle || 'Click to add job title'}</span>
              <Edit className="h-3 w-3 opacity-0 group-hover:opacity-100 ml-auto text-muted-foreground transition-opacity" />
            </div>
          )}
        </div>

        {/* Estimated Value field */}
        <div className="group relative py-1 rounded-md px-2 hover:bg-muted/30 transition-colors">
          <span className="text-xs text-muted-foreground block">Estimated Value</span>
          {editingField === 'estimatedValue' ? (
            <Input
              type="number"
              value={editValue}
              onChange={(e) => setEditValue(e.target.value)}
              onBlur={() => handleSave('estimatedValue')}
              onKeyDown={(e) => handleKeyDown(e, 'estimatedValue')}
              autoFocus
              className="h-8 py-1 mt-1 text-sm"
            />
          ) : (
            <div onClick={() => handleStartEdit('estimatedValue', String(lead.estimatedValue || ''))} className="flex items-center gap-2 cursor-pointer mt-1">
              <DollarSign className="h-4 w-4 text-muted-foreground" />
              <span className="text-sm font-medium text-foreground">
                {lead.estimatedValue
                  ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(lead.estimatedValue)
                  : 'Click to add value'}
              </span>
              <Edit className="h-3 w-3 opacity-0 group-hover:opacity-100 ml-auto text-muted-foreground transition-opacity" />
            </div>
          )}
        </div>

        {/* Follow Up Date field */}
        <div className="group relative py-1 rounded-md px-2 hover:bg-muted/30 transition-colors">
          <span className="text-xs text-muted-foreground block">Follow Up Date</span>
          {editingField === 'followUpDate' ? (
            <Input
              type="date"
              value={editValue}
              onChange={(e) => setEditValue(e.target.value)}
              onBlur={() => handleSave('followUpDate')}
              onKeyDown={(e) => handleKeyDown(e, 'followUpDate')}
              autoFocus
              className="h-8 py-1 mt-1 text-sm"
            />
          ) : (
            <div onClick={() => handleStartEdit('followUpDate', lead.followUpDate ? lead.followUpDate.split('T')[0] : '')} className="flex items-center gap-2 cursor-pointer mt-1">
              <Calendar className="h-4 w-4 text-muted-foreground" />
              <span className="text-sm font-medium text-foreground">
                {lead.followUpDate
                  ? new Date(lead.followUpDate).toLocaleDateString('vi-VN', { year: 'numeric', month: 'long', day: 'numeric' })
                  : 'Click to add follow up date'}
              </span>
              <Edit className="h-3 w-3 opacity-0 group-hover:opacity-100 ml-auto text-muted-foreground transition-opacity" />
            </div>
          )}
        </div>
      </div>
      {isUpdating && (
        <div className="flex items-center justify-center gap-2 text-xs text-muted-foreground">
          <Loader2 className="h-3.5 w-3.5 animate-spin" /> Saving changes...
        </div>
      )}
    </div>
  );
}
