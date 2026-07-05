'use client';

import { Calendar, Mail, Phone, CreditCard, Ban, CheckCircle } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle, Button, Avatar, AvatarFallback, Badge } from '@/shared/components/ui';
import type { Customer, Contact } from '../../../../types';
import { formatCurrency } from '../../../../utils';

interface AccountInsightsSidebarProps {
  customer: Customer;
  primaryContact?: Contact;
  onOpenCreditLimitDialog: () => void;
  onOpenMeetingRequest: () => void;
  onActivate: () => Promise<void>;
  onDeactivate: () => Promise<void>;
  onDeleteAccount: () => void;
  isActivating: boolean;
  isDeactivating: boolean;
}

export function AccountInsightsSidebar({
  customer,
  primaryContact,
  onOpenCreditLimitDialog,
  onOpenMeetingRequest,
  onActivate,
  onDeactivate,
  onDeleteAccount,
  isActivating,
  isDeactivating,
}: AccountInsightsSidebarProps) {
  const creditLimit = Number(customer.creditLimit || 0);
  const totalValue = Number(customer.totalValue || 0);
  
  // Utilization computation
  const utilizationPercent = creditLimit > 0 ? Math.min(100, Math.round((totalValue / creditLimit) * 100)) : 0;
  const isHighUtilization = utilizationPercent >= 90;
  const barColor = isHighUtilization ? 'bg-destructive' : utilizationPercent >= 70 ? 'bg-amber-500' : 'bg-emerald-500';

  return (
    <div className="space-y-6">
      {/* Credit Utilization Card */}
      <Card className="border border-muted/50 shadow-sm rounded-xl overflow-hidden">
        <CardHeader className="pb-2">
          <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground flex items-center justify-between">
            Credit Health Indicator
            <CreditCard className="h-3.5 w-3.5 text-muted-foreground" />
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-1">
            <div className="flex justify-between text-xs font-semibold">
              <span>Utilization Rate</span>
              <span className={isHighUtilization ? 'text-destructive font-bold' : ''}>{utilizationPercent}%</span>
            </div>
            {creditLimit > 0 ? (
              <div className="w-full bg-muted rounded-full h-1.5 overflow-hidden">
                <div className={`h-full ${barColor}`} style={{ width: `${utilizationPercent}%` }} />
              </div>
            ) : (
              <div className="text-[10px] text-muted-foreground italic">No credit limit configured.</div>
            )}
          </div>

          <div className="grid grid-cols-2 gap-2 text-xs pt-2 border-t border-muted/20">
            <div>
              <div className="text-[9px] text-muted-foreground uppercase font-bold">Total Spent</div>
              <div className="font-bold text-foreground">{formatCurrency(totalValue)}</div>
            </div>
            <div>
              <div className="text-[9px] text-muted-foreground uppercase font-bold">Limit</div>
              <div className="font-bold text-foreground">{creditLimit > 0 ? formatCurrency(creditLimit) : 'No limit'}</div>
            </div>
          </div>

          <Button size="sm" variant="outline" className="w-full text-xs h-8" onClick={onOpenCreditLimitDialog}>
            Adjust Credit Limit
          </Button>
        </CardContent>
      </Card>

      {/* Primary Contact Card */}
      <Card className="border border-muted/50 shadow-sm rounded-xl">
        <CardHeader className="pb-2">
          <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Primary Contact</CardTitle>
        </CardHeader>
        <CardContent>
          {primaryContact ? (
            <div className="flex items-start gap-3">
              <Avatar className="h-9 w-9">
                <AvatarFallback className="text-xs bg-primary text-primary-foreground">
                  {`${primaryContact.firstName?.[0] || ''}${primaryContact.lastName?.[0] || ''}`.toUpperCase() || 'C'}
                </AvatarFallback>
              </Avatar>
              <div className="space-y-0.5 overflow-hidden">
                <div className="text-xs font-bold truncate">{primaryContact.firstName} {primaryContact.lastName}</div>
                {primaryContact.jobTitle && <div className="text-[10px] text-muted-foreground truncate">{primaryContact.jobTitle}</div>}
                <div className="flex flex-col text-[10px] text-muted-foreground/80 pt-1.5 space-y-1">
                  <span className="flex items-center gap-1.5"><Mail className="h-3 w-3" /> {primaryContact.email}</span>
                  {primaryContact.phone && <span className="flex items-center gap-1.5"><Phone className="h-3 w-3" /> {primaryContact.phone}</span>}
                </div>
              </div>
            </div>
          ) : (
            <div className="text-xs text-muted-foreground italic text-center py-2">No primary contact selected.</div>
          )}
        </CardContent>
      </Card>

      {/* Preferences Card */}
      {((customer.preferredDays && customer.preferredDays.length > 0) || (customer.preferredTimeSlots && customer.preferredTimeSlots.length > 0)) && (
        <Card className="border border-muted/50 shadow-sm rounded-xl">
          <CardHeader className="pb-2">
            <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Communication Preferences</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {customer.preferredDays && customer.preferredDays.length > 0 && (
              <div className="space-y-1">
                <div className="text-[9px] text-muted-foreground uppercase font-bold">Best Days</div>
                <div className="flex flex-wrap gap-1">
                  {customer.preferredDays.map((day) => (
                    <Badge key={day} variant="secondary" className="text-[9px] px-1.5 py-0.5 lowercase">{day.toLowerCase()}</Badge>
                  ))}
                </div>
              </div>
            )}
            {customer.preferredTimeSlots && customer.preferredTimeSlots.length > 0 && (
              <div className="space-y-1">
                <div className="text-[9px] text-muted-foreground uppercase font-bold">Best Hours</div>
                <div className="flex flex-wrap gap-1">
                  {customer.preferredTimeSlots.map((slot) => (
                    <Badge key={slot} variant="secondary" className="text-[9px] px-1.5 py-0.5 lowercase">{slot.toLowerCase().replace('_', ' ')}</Badge>
                  ))}
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      )}

      {/* Action Hub */}
      <Card className="border border-muted/50 shadow-sm rounded-xl">
        <CardHeader className="pb-2">
          <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Quick Action Hub</CardTitle>
        </CardHeader>
        <CardContent className="space-y-2">
          <Button variant="outline" className="w-full justify-start text-xs h-9 border-muted/50 hover:bg-muted/40 text-foreground" onClick={onOpenMeetingRequest}>
            <Calendar className="mr-2 h-4 w-4 text-muted-foreground" /> Request Meeting
          </Button>
          {customer.status === 'INACTIVE' ? (
            <Button variant="outline" className="w-full justify-start text-xs h-9 text-emerald-600 border-emerald-200 hover:bg-emerald-50" onClick={onActivate} disabled={isActivating}>
              <CheckCircle className="mr-2 h-4 w-4" /> Activate Account
            </Button>
          ) : (
            <Button variant="outline" className="w-full justify-start text-xs h-9 text-amber-600 border-amber-200 hover:bg-amber-50" onClick={onDeactivate} disabled={isDeactivating}>
              <Ban className="mr-2 h-4 w-4" /> Deactivate Account
            </Button>
          )}
          <Button variant="ghost" className="w-full justify-start text-xs h-9 text-rose-600 hover:text-rose-700 bg-rose-50 hover:bg-rose-100/80" onClick={onDeleteAccount}>
            Delete Account Profile
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
