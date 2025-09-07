import Link from 'next/link';

import { Container, PageHeader } from '@/components/layout/main-layout';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';

export default function Home() {
  return (
    <Container className='py-8'>
      <PageHeader
        title='Welcome to SERP'
        description='Modern Enterprise Resource Planning system built with Next.js, TypeScript, and Tailwind CSS'
      />

      <div className='grid gap-6 md:grid-cols-2 lg:grid-cols-3'>
        <Card>
          <CardHeader>
            <CardTitle>Dashboard</CardTitle>
          </CardHeader>
          <CardContent>
            <p className='text-sm text-muted-foreground mb-4'>
              Monitor your business performance with comprehensive analytics and
              real-time insights.
            </p>
            <Button asChild>
              <Link href='/dashboard'>Go to Dashboard</Link>
            </Button>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Customer Relationship</CardTitle>
          </CardHeader>
          <CardContent>
            <p className='text-sm text-muted-foreground mb-4'>
              Manage customer relationships, track leads, and optimize your
              sales pipeline.
            </p>
            <Button asChild variant='outline'>
              <Link href='/crm'>Explore CRM</Link>
            </Button>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Accounting</CardTitle>
          </CardHeader>
          <CardContent>
            <p className='text-sm text-muted-foreground mb-4'>
              Handle invoicing, payments, and financial reporting with ease and
              accuracy.
            </p>
            <Button asChild variant='outline'>
              <Link href='/accounting'>View Accounting</Link>
            </Button>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Inventory Management</CardTitle>
          </CardHeader>
          <CardContent>
            <p className='text-sm text-muted-foreground mb-4'>
              Track products, manage stock levels, and optimize your warehouse
              operations.
            </p>
            <Button asChild variant='outline'>
              <Link href='/inventory'>Manage Inventory</Link>
            </Button>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Project Management</CardTitle>
          </CardHeader>
          <CardContent>
            <p className='text-sm text-muted-foreground mb-4'>
              Plan projects, assign tasks, and track progress across your
              organization.
            </p>
            <Button asChild variant='outline'>
              <Link href='/projects'>View Projects</Link>
            </Button>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Human Resources</CardTitle>
          </CardHeader>
          <CardContent>
            <p className='text-sm text-muted-foreground mb-4'>
              Manage employees, track attendance, and streamline HR processes.
            </p>
            <Button asChild variant='outline'>
              <Link href='/hr'>HR Management</Link>
            </Button>
          </CardContent>
        </Card>
      </div>

      <div className='mt-12 text-center'>
        <h2 className='text-2xl font-semibold mb-4'>Ready to get started?</h2>
        <p className='text-muted-foreground mb-6'>
          This application is built with modern technologies including Next.js
          15, TypeScript, Tailwind CSS, and Redux Toolkit.
        </p>
        <div className='flex gap-4 justify-center'>
          <Button size='lg'>
            <Link href='/auth/login'>Login</Link>
          </Button>
          <Button variant='outline' size='lg'>
            <Link href='/auth/register'>Create Account</Link>
          </Button>
        </div>
      </div>
    </Container>
  );
}
