/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

import React from 'react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from '@/shared/components/ui';
import { FAQ } from '../types';

interface FAQSectionProps {
  faqs: FAQ[];
}

export const FAQSection: React.FC<FAQSectionProps> = ({ faqs }) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Frequently Asked Questions</CardTitle>
      </CardHeader>
      <CardContent>
        <Accordion type='single' collapsible className='w-full'>
          {faqs.map((faq, index) => (
            <AccordionItem key={index} value={`item-${index}`}>
              <AccordionTrigger className='text-left'>
                {faq.question}
              </AccordionTrigger>
              <AccordionContent>
                <p className='text-muted-foreground'>{faq.answer}</p>
              </AccordionContent>
            </AccordionItem>
          ))}
        </Accordion>
      </CardContent>
    </Card>
  );
};
