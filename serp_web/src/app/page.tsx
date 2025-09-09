import {
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  ThemeToggle,
  Input,
} from '@/shared/components';

export default function Home() {
  return (
    <div className='container mx-auto p-8 space-y-8'>
      {/* Header with Theme Toggle */}
      <div className='flex justify-between items-center'>
        <h1 className='text-4xl font-bold'>
          SERP - Enterprise Resource Planning
        </h1>
        <ThemeToggle />
      </div>

      {/* Welcome Card */}
      <Card className='max-w-2xl'>
        <CardHeader>
          <CardTitle>Welcome to SERP Web</CardTitle>
          <CardDescription>
            Modern ERP system with modular architecture, built with Next.js and
            TypeScript
          </CardDescription>
        </CardHeader>
        <CardContent className='space-y-4'>
          <div className='flex gap-2'>
            <Input placeholder='Search...' className='flex-1' />
            <Button>Search</Button>
          </div>
          <div className='flex gap-2'>
            <Button variant='default'>CRM Module</Button>
            <Button variant='outline'>Accounting</Button>
            <Button variant='secondary'>Inventory</Button>
          </div>
        </CardContent>
      </Card>

      {/* Modules Overview */}
      <div className='grid grid-cols-1 md:grid-cols-3 gap-6'>
        <Card>
          <CardHeader>
            <CardTitle>CRM</CardTitle>
            <CardDescription>Customer Relationship Management</CardDescription>
          </CardHeader>
          <CardContent>
            <Button variant='outline' className='w-full'>
              Open CRM
            </Button>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Accounting</CardTitle>
            <CardDescription>Financial Management</CardDescription>
          </CardHeader>
          <CardContent>
            <Button variant='outline' className='w-full'>
              Open Accounting
            </Button>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Inventory</CardTitle>
            <CardDescription>Stock Management</CardDescription>
          </CardHeader>
          <CardContent>
            <Button variant='outline' className='w-full'>
              Open Inventory
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
