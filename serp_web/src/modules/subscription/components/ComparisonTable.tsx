/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

import React, { useState } from 'react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Button,
} from '@/shared/components/ui';
import { UISubscriptionPlan } from '../types';
import { Check, X, ChevronDown, ChevronUp } from 'lucide-react';
import { cn } from '@/shared/utils';

interface ComparisonTableProps {
  plans: UISubscriptionPlan[];
}

export const ComparisonTable: React.FC<ComparisonTableProps> = ({ plans }) => {
  const [isExpanded, setIsExpanded] = useState(false);

  // Get all unique categories
  const categories = plans[0]?.featuresDetailed || [];

  return (
    <div className='w-full'>
      <div className='text-center mb-6'>
        <Button
          variant='outline'
          onClick={() => setIsExpanded(!isExpanded)}
          className='gap-2'
        >
          {isExpanded ? (
            <>
              <ChevronUp className='h-4 w-4' />
              Hide Detailed Comparison
            </>
          ) : (
            <>
              <ChevronDown className='h-4 w-4' />
              Compare Plans in Detail
            </>
          )}
        </Button>
      </div>

      {isExpanded && (
        <Card className='overflow-hidden'>
          <CardHeader className='bg-muted/50'>
            <CardTitle>Feature Comparison</CardTitle>
          </CardHeader>
          <CardContent className='p-0'>
            <div className='overflow-x-auto'>
              <table className='w-full'>
                <thead className='bg-muted/30 border-b'>
                  <tr>
                    <th className='text-left p-4 font-semibold min-w-[200px]'>
                      Feature
                    </th>
                    {plans.map((plan) => (
                      <th
                        key={plan.id}
                        className='text-center p-4 font-semibold min-w-[150px]'
                      >
                        <div className='flex flex-col items-center gap-1'>
                          <span>{plan.name}</span>
                          <span className='text-sm font-normal text-muted-foreground'>
                            ${plan.monthlyPrice}/mo
                          </span>
                        </div>
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {categories.map((category, catIndex) => (
                    <React.Fragment key={catIndex}>
                      {/* Category Header */}
                      <tr className='bg-muted/20'>
                        <td
                          colSpan={plans.length + 1}
                          className='p-3 font-semibold text-sm'
                        >
                          {category.category}
                        </td>
                      </tr>
                      {/* Features in Category */}
                      {category.features.map((feature, featureIndex) => (
                        <tr
                          key={`${catIndex}-${featureIndex}`}
                          className='border-b hover:bg-muted/10 transition-colors'
                        >
                          <td className='p-4 text-sm'>
                            <div className='flex items-center gap-2'>
                              <span>{feature.name}</span>
                              {feature.tooltip && (
                                <button
                                  type='button'
                                  className='text-muted-foreground cursor-help inline-flex items-center justify-center w-4 h-4 rounded-full border border-current text-xs'
                                  title={feature.tooltip}
                                  aria-label={`More information about ${feature.name}`}
                                >
                                  <span aria-hidden='true'>i</span>
                                </button>
                              )}
                            </div>
                          </td>
                          {plans.map((plan) => {
                            const planFeature =
                              plan.featuresDetailed[catIndex]?.features[
                                featureIndex
                              ];
                            return (
                              <td key={plan.id} className='p-4 text-center'>
                                {planFeature?.included ? (
                                  <Check className='h-5 w-5 text-primary mx-auto' />
                                ) : (
                                  <X className='h-5 w-5 text-muted-foreground mx-auto' />
                                )}
                              </td>
                            );
                          })}
                        </tr>
                      ))}
                    </React.Fragment>
                  ))}
                </tbody>
              </table>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
};
