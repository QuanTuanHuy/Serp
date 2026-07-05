'use client';

import { ChevronDown, TrendingUp, Sparkles, Calendar, AlertCircle, RefreshCw } from 'lucide-react';
import { Button, Card, CardContent, CardHeader, CardTitle, Avatar, AvatarFallback } from '@/shared/components/ui';
import { DropdownMenu, DropdownMenuTrigger, DropdownMenuContent, DropdownMenuItem } from '@/shared/components/ui';
import type { Opportunity, OpportunityStage } from '../../../../types';
import { formatCurrency } from '../../../../utils';

interface OpportunityInsightsSidebarProps {
  opportunity: Opportunity;
  probability: number;
  weightedValue: number;
  daysInPipeline: number;
  daysUntilClose: number;
  users: any[];
  getUserName?: (userId?: string | number) => string;
  onAssignOpportunity: (userId: number) => Promise<void>;
  onChangeStage: (stage: OpportunityStage) => void;
  onOpenMeetingRequest: () => void;
  onReopen: () => void;
  onDelete: () => void;
}

export function OpportunityInsightsSidebar({
  opportunity,
  probability,
  weightedValue,
  daysInPipeline,
  daysUntilClose,
  users,
  getUserName,
  onAssignOpportunity,
  onChangeStage,
  onOpenMeetingRequest,
  onReopen,
  onDelete,
}: OpportunityInsightsSidebarProps) {
  const isClosed = opportunity.stage === 'CLOSED_WON' || opportunity.stage === 'CLOSED_LOST';
  const gaugeColor = probability >= 75 ? 'stroke-emerald-500' : probability >= 50 ? 'stroke-amber-500' : probability >= 25 ? 'stroke-blue-500' : 'stroke-rose-500';

  const stagesList: { code: OpportunityStage; label: string; prob: number }[] = [
    { code: 'PROSPECTING', label: 'Prospecting', prob: 10 },
    { code: 'QUALIFICATION', label: 'Qualification', prob: 25 },
    { code: 'PROPOSAL', label: 'Proposal', prob: 50 },
    { code: 'NEGOTIATION', label: 'Negotiation', prob: 75 },
  ];

  return (
    <div className="space-y-6">
      {/* Probability Gauge */}
      <Card className="border border-muted/50 shadow-sm rounded-xl overflow-hidden">
        <CardHeader className="pb-2">
          <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground flex items-center justify-between">
            Forecast Score
            <span className="text-[10px] text-muted-foreground lowercase flex items-center"><TrendingUp className="h-3 w-3 mr-1" /> win probability</span>
          </CardTitle>
        </CardHeader>
        <CardContent className="flex flex-col items-center py-4">
          <div className="relative h-28 w-28">
            <svg className="h-full w-full -rotate-90" viewBox="0 0 36 36">
              <path className="stroke-muted/30" strokeWidth="3" fill="none" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
              <path
                className={`transition-all duration-1000 ease-out ${gaugeColor}`}
                strokeWidth="3"
                strokeDasharray={`${probability}, 100`}
                strokeLinecap="round"
                fill="none"
                d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
              />
            </svg>
            <div className="absolute inset-0 flex flex-col items-center justify-center">
              <span className="text-2xl font-extrabold text-foreground">{probability}%</span>
              <span className="text-[8px] text-muted-foreground uppercase font-bold tracking-wider">Chance</span>
            </div>
          </div>
          
          <div className="text-center mt-3">
            <div className="text-xs text-muted-foreground">Weighted Forecast</div>
            <div className="text-lg font-bold text-foreground">{formatCurrency(weightedValue)}</div>
          </div>
        </CardContent>
      </Card>

      {/* Vertical Pipeline Progress Stepper */}
      <Card className="border border-muted/50 shadow-sm rounded-xl">
        <CardHeader className="pb-2">
          <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Interactive Stage Stepper</CardTitle>
        </CardHeader>
        <CardContent className="pt-2">
          <div className="relative pl-5 space-y-4 before:absolute before:left-[7px] before:top-2 before:bottom-2 before:w-[2px] before:bg-muted/80">
            {stagesList.map((stage) => {
              const isActive = opportunity.stage === stage.code;
              const isCompleted = stagesList.findIndex((s) => s.code === opportunity.stage) > stagesList.findIndex((s) => s.code === stage.code);
              
              return (
                <div key={stage.code} className="relative group cursor-pointer" onClick={() => !isClosed && onChangeStage(stage.code)}>
                  <div className={`absolute -left-[22px] top-1.5 h-3.5 w-3.5 rounded-full border transition-all duration-300 ${
                    isActive ? 'bg-primary border-primary scale-125 ring-2 ring-primary/20 glow' : isCompleted ? 'bg-emerald-600 border-emerald-600' : 'bg-background border-muted'
                  }`} />
                  <div className="flex justify-between items-center pl-2">
                    <span className={`text-xs font-medium transition ${isActive ? 'text-primary font-bold' : 'text-muted-foreground group-hover:text-foreground'}`}>
                      {stage.label}
                    </span>
                    <span className="text-[10px] text-muted-foreground/70">{stage.prob}%</span>
                  </div>
                </div>
              );
            })}

            {/* Closed stage handling */}
            <div className="relative group pl-2 pt-2 border-t border-muted/30">
              <div className="flex justify-between items-center">
                <span className="text-xs font-semibold text-muted-foreground">Closed Status:</span>
                {isClosed ? (
                  <span className={`text-xs font-bold ${opportunity.stage === 'CLOSED_WON' ? 'text-emerald-600' : 'text-rose-600'}`}>
                    {opportunity.stage === 'CLOSED_WON' ? 'Closed Won (100%)' : 'Closed Lost (0%)'}
                  </span>
                ) : (
                  <div className="flex gap-1.5">
                    <Button size="sm" variant="outline" className="text-[10px] text-emerald-600 h-6 px-2 hover:bg-emerald-50 border-emerald-300" onClick={() => onChangeStage('CLOSED_WON')}>Won</Button>
                    <Button size="sm" variant="outline" className="text-[10px] text-rose-600 h-6 px-2 hover:bg-rose-50 border-rose-300" onClick={() => onChangeStage('CLOSED_LOST')}>Lost</Button>
                  </div>
                )}
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Assigned Rep selection */}
      <Card className="border border-muted/50 shadow-sm rounded-xl">
        <CardHeader className="pb-2">
          <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Assigned Rep</CardTitle>
        </CardHeader>
        <CardContent>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <div className="flex items-center justify-between p-2 hover:bg-muted/40 rounded-lg cursor-pointer border border-muted/30">
                <div className="flex items-center gap-2">
                  <Avatar className="h-7 w-7">
                    <AvatarFallback className="text-[10px] bg-muted">R</AvatarFallback>
                  </Avatar>
                  <span className="text-sm font-medium text-foreground">
                    {opportunity.assignedTo ? (getUserName ? getUserName(opportunity.assignedTo) : `User #${opportunity.assignedTo}`) : 'Unassigned'}
                  </span>
                </div>
                <ChevronDown className="h-4 w-4 text-muted-foreground" />
              </div>
            </DropdownMenuTrigger>
            <DropdownMenuContent className="max-h-60 overflow-y-auto w-56" align="end">
              {users.map((user) => (
                <DropdownMenuItem key={user.id} onClick={() => onAssignOpportunity(Number(user.id))}>
                  {[user.firstName, user.lastName].filter(Boolean).join(' ') || user.email}
                </DropdownMenuItem>
              ))}
            </DropdownMenuContent>
          </DropdownMenu>
        </CardContent>
      </Card>

      {/* Deal Transition Actions */}
      <Card className="border border-muted/50 shadow-sm rounded-xl">
        <CardHeader className="pb-2">
          <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Actions Hub</CardTitle>
        </CardHeader>
        <CardContent className="space-y-2">
          <Button className="w-full justify-start border-muted/50 hover:bg-muted/40 text-foreground" variant="outline" onClick={onOpenMeetingRequest}>
            <Calendar className="mr-2 h-4 w-4 text-muted-foreground" /> Request Meeting
          </Button>
          {isClosed && (
            <Button className="w-full justify-start text-emerald-600 hover:text-emerald-700 bg-emerald-50 hover:bg-emerald-100/80" variant="ghost" onClick={onReopen}>
              <RefreshCw className="mr-2 h-4 w-4" /> Reopen Deal
            </Button>
          )}
          <Button className="w-full justify-start text-rose-600 hover:text-rose-700 bg-rose-50 hover:bg-rose-100/80" variant="ghost" onClick={onDelete}>
            <AlertCircle className="mr-2 h-4 w-4" /> Delete Opportunity
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
