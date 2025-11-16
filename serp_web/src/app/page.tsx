'use client';

import { Header, AppsShowcase } from '@/shared/components';

export default function LandingPage() {
  return (
    <div className='min-h-screen bg-background w-full'>
      <Header />

      <main className='w-full'>
        {/* Hero Section */}
        <section className='py-20 bg-gradient-to-br from-primary/10 via-background to-secondary/10 w-full'>
          <div className='container mx-auto px-4 text-center'>
            <h1 className='text-4xl md:text-6xl font-bold mb-6'>
              Smart Enterprise Resource Planning
            </h1>
            <p className='text-xl text-muted-foreground mb-8 max-w-3xl mx-auto'>
              Streamline your business operations with our comprehensive ERP
              solution. Manage everything from CRM to accounting in one
              integrated platform.
            </p>
            <div className='flex flex-col sm:flex-row gap-4 justify-center'>
              <button className='bg-primary text-primary-foreground px-8 py-3 rounded-lg font-medium hover:bg-primary/90 transition-colors'>
                Get Started
              </button>
              <button className='border border-input bg-background px-8 py-3 rounded-lg font-medium hover:bg-accent transition-colors'>
                Learn More
              </button>
            </div>
          </div>
        </section>

        {/* Apps Showcase Section */}
        <AppsShowcase />

        {/* Features Section */}
        <section className='py-20 bg-muted/30 w-full'>
          <div className='container mx-auto px-4'>
            <div className='text-center mb-16'>
              <h2 className='text-3xl font-bold mb-4'>Why Choose SERP?</h2>
              <p className='text-muted-foreground text-lg max-w-2xl mx-auto'>
                Built with modern technology and designed for scalability,
                security, and ease of use.
              </p>
            </div>

            <div className='grid grid-cols-1 md:grid-cols-3 gap-8'>
              <div className='text-center p-6'>
                <div className='w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center mx-auto mb-4'>
                  <span className='text-2xl'>🚀</span>
                </div>
                <h3 className='text-xl font-semibold mb-3'>
                  Modern Architecture
                </h3>
                <p className='text-muted-foreground'>
                  Built with microservices architecture for maximum scalability
                  and reliability.
                </p>
              </div>

              <div className='text-center p-6'>
                <div className='w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center mx-auto mb-4'>
                  <span className='text-2xl'>🔒</span>
                </div>
                <h3 className='text-xl font-semibold mb-3'>
                  Enterprise Security
                </h3>
                <p className='text-muted-foreground'>
                  Advanced security features with role-based access control and
                  data encryption.
                </p>
              </div>

              <div className='text-center p-6'>
                <div className='w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center mx-auto mb-4'>
                  <span className='text-2xl'>⚡</span>
                </div>
                <h3 className='text-xl font-semibold mb-3'>
                  Real-time Analytics
                </h3>
                <p className='text-muted-foreground'>
                  Get insights into your business with real-time reporting and
                  analytics.
                </p>
              </div>
            </div>
          </div>
        </section>
      </main>

      {/* Footer */}
      <footer className='border-t py-12 w-full'>
        <div className='container mx-auto px-4'>
          <div className='grid grid-cols-1 md:grid-cols-4 gap-8'>
            <div>
              <div className='flex items-center space-x-2 mb-4'>
                <div className='h-8 w-8 bg-primary rounded-md flex items-center justify-center'>
                  <span className='text-primary-foreground font-bold text-sm'>
                    S
                  </span>
                </div>
                <span className='font-bold text-xl'>SERP</span>
              </div>
              <p className='text-muted-foreground text-sm'>
                Modern ERP solution for businesses of all sizes.
              </p>
            </div>

            <div>
              <h4 className='font-semibold mb-4'>Product</h4>
              <ul className='space-y-2 text-sm text-muted-foreground'>
                <li>
                  <a href='/apps' className='hover:text-foreground'>
                    Apps
                  </a>
                </li>
                <li>
                  <a href='/pricing' className='hover:text-foreground'>
                    Pricing
                  </a>
                </li>
                <li>
                  <a href='/features' className='hover:text-foreground'>
                    Features
                  </a>
                </li>
              </ul>
            </div>

            <div>
              <h4 className='font-semibold mb-4'>Support</h4>
              <ul className='space-y-2 text-sm text-muted-foreground'>
                <li>
                  <a href='/help' className='hover:text-foreground'>
                    Help Center
                  </a>
                </li>
                <li>
                  <a href='/community' className='hover:text-foreground'>
                    Community
                  </a>
                </li>
                <li>
                  <a href='/contact' className='hover:text-foreground'>
                    Contact
                  </a>
                </li>
              </ul>
            </div>

            <div>
              <h4 className='font-semibold mb-4'>Company</h4>
              <ul className='space-y-2 text-sm text-muted-foreground'>
                <li>
                  <a href='/about' className='hover:text-foreground'>
                    About
                  </a>
                </li>
                <li>
                  <a href='/careers' className='hover:text-foreground'>
                    Careers
                  </a>
                </li>
                <li>
                  <a href='/blog' className='hover:text-foreground'>
                    Blog
                  </a>
                </li>
              </ul>
            </div>
          </div>

          <div className='border-t mt-8 pt-8 text-center text-sm text-muted-foreground'>
            <p>&copy; 2025 SERP. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </div>
  );
}
