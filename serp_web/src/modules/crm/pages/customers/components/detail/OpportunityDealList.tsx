'use client';

import Link from 'next/link';
import { TrendingUp, Calendar, User, ArrowRight, DollarSign } from 'lucide-react';
import { Card, CardContent, Badge, Button } from '@/shared/components/ui';
import type { Opportunity } from '../../../../types';
import { formatCurrency } from '../../../../utils';

interface OpportunityDealListProps {
  opportunities: Opportunity[];
  isLoading: boolean;
}

export function OpportunityDealList({ opportunities, isLoading }: OpportunityDealListProps) {
  if (isLoading) {
    return <div className="text-center py-8 text-muted-foreground text-xs">Loading client opportunities...</div>;
  }

  return (
    <div className="space-y-4">
      <h3 className="text-sm font-semibold uppercase tracking-wider text-muted-foreground flex items-center gap-1.5">
        <TrendingUp className="h-4 w-4 text-emerald-600" /> Associated Opportunities
      </h3>

      {opportunities.length === 0 ? (
        <Card className="border border-dashed border-muted/60 p-8 text-center rounded-xl">
          <DollarSign className="mx-auto mb-2.5 h-10 w-10 text-muted-foreground/60" />
          <p className="text-xs text-muted-foreground font-medium">No sales opportunities linked to this account yet.</p>
        </Card>
      ) : (
        <div className="grid grid-cols-1 gap-3">
          {opportunities.map((opp) => (
            <Card key={opp.id} className="border border-muted/50 shadow-sm rounded-xl overflow-hidden hover:border-muted-foreground/30 transition-all duration-300">
              <CardContent className="p-4 flex flex-col sm:flex-row justify-between sm:items-center gap-4">
                <div className="space-y-1">
                  <div className="flex items-center gap-2 flex-wrap">
                    <span className="text-sm font-bold text-foreground">{opp.name}</span>
                    <Badge variant="outline" className="text-[10px] capitalize px-1.5 bg-muted/40">
                      {opp.stage.replace('_', ' ').toLowerCase()}
                    </Badge>
                  </div>
                  <div className="flex items-center gap-4 text-[10px] text-muted-foreground/80 flex-wrap">
                    <span className="flex items-center gap-1"><Calendar className="h-3 w-3" /> Closes: {opp.expectedCloseDate ? new Date(opp.expectedCloseDate).toLocaleDateString('vi-VN') : 'N/A'}</span>
                    {opp.assignedToName && <span className="flex items-center gap-1"><User className="h-3 w-3" /> Owner: {opp.assignedToName}</span>}
                  </div>
                </div>

                <div className="flex items-center gap-4 justify-between sm:justify-end">
                  <div className="text-right">
                    <div className="text-xs text-muted-foreground uppercase font-semibold">Value</div>
                    <div className="text-sm font-bold text-emerald-700">{formatCurrency(opp.estimatedValue || 0)}</div>
                  </div>
                  <Button size="sm" variant="ghost" className="h-8 px-2" asChild>
                    <Link href={`/crm/opportunities/${opp.id}`}>
                      View <ArrowRight className="ml-1 h-3.5 w-3.5" />
                    </Link>
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
