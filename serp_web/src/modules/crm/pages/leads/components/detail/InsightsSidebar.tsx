import { ChevronDown, TrendingUp, UserPlus, CheckCircle, AlertCircle, Calendar, Sparkles } from 'lucide-react';
import { Button, Card, CardContent, CardHeader, CardTitle, Avatar, AvatarFallback, Popover, PopoverContent, PopoverTrigger } from '@/shared/components/ui';
import { DropdownMenu, DropdownMenuTrigger, DropdownMenuContent, DropdownMenuItem } from '@/shared/components/ui';
import { Accordion, AccordionItem, AccordionTrigger, AccordionContent } from '@/shared/components/ui';
import type { Lead } from '../../../../types';

interface InsightsSidebarProps {
  lead: Lead;
  leadScore: number;
  onAssignLead: (assignedToId: number) => Promise<void>;
  onOpenConvert: () => void;
  onOpenQualify: () => void;
  onOpenDisqualify: () => void;
  onOpenMeetingRequest: () => void;
}

export function InsightsSidebar({
  lead,
  leadScore,
  onAssignLead,
  onOpenConvert,
  onOpenQualify,
  onOpenDisqualify,
  onOpenMeetingRequest,
}: InsightsSidebarProps) {
  const scoreColor = leadScore >= 75 ? 'stroke-emerald-500' : leadScore >= 50 ? 'stroke-amber-500' : 'stroke-rose-500';

  const formatDate = (dateStr?: string) => {
    if (!dateStr) return 'Not available';
    return new Date(dateStr).toLocaleDateString('vi-VN', { year: 'numeric', month: 'long', day: 'numeric' });
  };

  return (
    <div className="space-y-6">
      {/* Lead Score Radial Arc */}
      <Card className="border border-muted/50 shadow-sm rounded-xl overflow-hidden">
        <CardHeader className="pb-2">
          <CardTitle className="text-sm font-semibold flex items-center gap-2">
            <TrendingUp className="h-4 w-4 text-muted-foreground" /> Lead Quality Score
          </CardTitle>
        </CardHeader>
        <CardContent className="flex flex-col items-center justify-center pb-4">
          <div className="relative h-32 w-32 flex items-center justify-center">
            <svg className="h-full w-full -rotate-90">
              <circle cx="64" cy="64" r="50" fill="none" className="stroke-muted/40" strokeWidth="8" />
              <circle
                cx="64"
                cy="64"
                r="50"
                fill="none"
                className={scoreColor}
                strokeWidth="8"
                strokeDasharray={`${(leadScore / 100) * 314} 314`}
                strokeLinecap="round"
              />
            </svg>
            <div className="absolute inset-0 flex flex-col items-center justify-center">
              <span className="text-3xl font-extrabold text-foreground">{leadScore}</span>
              <span className="text-[10px] text-muted-foreground uppercase font-bold tracking-wider">Score</span>
            </div>
          </div>
          
          {/* Popover explaining score */}
          <Popover>
            <PopoverTrigger asChild>
              <Button variant="ghost" size="sm" className="mt-2 text-xs text-muted-foreground hover:text-foreground">
                <Sparkles className="h-3 w-3 mr-1" /> View calculation details
              </Button>
            </PopoverTrigger>
            <PopoverContent className="w-64 p-3 text-xs space-y-2">
              <h4 className="font-bold text-foreground">Score Calculation</h4>
              <div className="space-y-1 text-muted-foreground">
                <div className="flex justify-between"><span>Email provided</span><span className="text-emerald-600 font-semibold">+20</span></div>
                <div className="flex justify-between"><span>Phone provided</span><span className="text-emerald-600 font-semibold">+15</span></div>
                <div className="flex justify-between"><span>Company provided</span><span className="text-emerald-600 font-semibold">+20</span></div>
                <div className="flex justify-between"><span>Status state weight</span><span className="text-emerald-600 font-semibold">+25</span></div>
              </div>
            </PopoverContent>
          </Popover>
        </CardContent>
      </Card>

      {/* Assigned User Selection */}
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
                    {lead.assignedTo ? `User #${lead.assignedTo}` : 'Unassigned'}
                  </span>
                </div>
                <ChevronDown className="h-4 w-4 text-muted-foreground" />
              </div>
            </DropdownMenuTrigger>
            <DropdownMenuContent className="w-56" align="end">
              <DropdownMenuItem onClick={() => onAssignLead(1)}>User #1 (Admin)</DropdownMenuItem>
              <DropdownMenuItem onClick={() => onAssignLead(2)}>User #2 (Sales Rep)</DropdownMenuItem>
              <DropdownMenuItem onClick={() => onAssignLead(3)}>User #3 (Manager)</DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </CardContent>
      </Card>

      {/* Action Hub buttons */}
      <Card className="border border-muted/50 shadow-sm rounded-xl">
        <CardHeader className="pb-2">
          <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Lead Transition Actions</CardTitle>
        </CardHeader>
        <CardContent className="space-y-2">
          {lead.leadStatus === 'QUALIFIED' ? (
            <Button onClick={onOpenConvert} className="w-full justify-start bg-emerald-600 hover:bg-emerald-700 text-white shadow-sm">
              <UserPlus className="mr-2 h-4 w-4" /> Convert to Account
            </Button>
          ) : (
            <Button onClick={onOpenQualify} variant="outline" className="w-full justify-start hover:bg-muted/40">
              <CheckCircle className="mr-2 h-4 w-4 text-emerald-500" /> Qualify Lead
            </Button>
          )}
          <Button onClick={onOpenDisqualify} variant="outline" className="w-full justify-start hover:bg-muted/40">
            <AlertCircle className="mr-2 h-4 w-4 text-rose-500" /> Disqualify Lead
          </Button>
          <Button
            onClick={onOpenMeetingRequest}
            variant="outline"
            disabled={!lead.convertedAccountId}
            className="w-full justify-start hover:bg-muted/40"
            title={lead.convertedAccountId ? undefined : "Convert this lead to an account before scheduling meetings"}
          >
            <Calendar className="mr-2 h-4 w-4 text-blue-500" /> Request local meeting
          </Button>
        </CardContent>
      </Card>

      {/* Collapsible Metadata */}
      <Accordion type="single" collapsible className="w-full">
        <AccordionItem value="meta" className="border-none">
          <AccordionTrigger className="text-xs font-semibold uppercase tracking-wider text-muted-foreground hover:no-underline py-2">
            System Metadata
          </AccordionTrigger>
          <AccordionContent className="space-y-2 text-xs text-muted-foreground pt-1">
            <div className="flex justify-between"><span>Lead ID</span><span className="font-mono">#{lead.id}</span></div>
            <div className="flex justify-between"><span>Created</span><span>{formatDate(lead.createdAt)}</span></div>
            <div className="flex justify-between"><span>Last Update</span><span>{formatDate(lead.updatedAt)}</span></div>
          </AccordionContent>
        </AccordionItem>
      </Accordion>
    </div>
  );
}
