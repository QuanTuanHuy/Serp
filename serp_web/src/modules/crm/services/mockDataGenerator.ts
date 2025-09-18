// Mock Data Generator (authors: QuanTuanHuy, Description: Part of Serp Project)

import type {
  Customer,
  Lead,
  Opportunity,
  Activity,
  CustomerType,
  CustomerStatus,
  LeadSource,
  LeadStatus,
  OpportunityStage,
  OpportunityType,
  ActivityType,
  ActivityStatus,
  Priority,
} from '../types';

// Mock user data
const MOCK_USERS = [
  { id: '1', name: 'John Smith', email: 'john.smith@company.com' },
  { id: '2', name: 'Sarah Johnson', email: 'sarah.johnson@company.com' },
  { id: '3', name: 'Mike Wilson', email: 'mike.wilson@company.com' },
  { id: '4', name: 'Lisa Brown', email: 'lisa.brown@company.com' },
  { id: '5', name: 'David Lee', email: 'david.lee@company.com' },
];

// Helper functions
const randomId = () => Math.random().toString(36).substr(2, 9);
const randomDate = (start: Date, end: Date) =>
  new Date(
    start.getTime() + Math.random() * (end.getTime() - start.getTime())
  ).toISOString();
const randomElement = <T>(arr: T[]) =>
  arr[Math.floor(Math.random() * arr.length)];
const randomUser = () => randomElement(MOCK_USERS);
const randomBool = () => Math.random() > 0.5;
const randomNumber = (min: number, max: number) =>
  Math.floor(Math.random() * (max - min + 1)) + min;

// Generate mock customers
export const generateMockCustomers = (count: number = 50): Customer[] => {
  const customers: Customer[] = [];

  const firstNames = [
    'John',
    'Jane',
    'Mike',
    'Sarah',
    'David',
    'Lisa',
    'Tom',
    'Anna',
    'Chris',
    'Emma',
  ];
  const lastNames = [
    'Smith',
    'Johnson',
    'Wilson',
    'Brown',
    'Davis',
    'Miller',
    'Jones',
    'Garcia',
    'Rodriguez',
    'Lee',
  ];
  const companies = [
    'TechCorp Inc',
    'Global Solutions Ltd',
    'InnovateTech',
    'Digital Dynamics',
    'Future Systems',
    'SmartBusiness Co',
    'NextGen Industries',
    'ProActive Solutions',
    'Dynamic Enterprises',
    'TechVision LLC',
  ];
  const domains = [
    'gmail.com',
    'outlook.com',
    'company.com',
    'business.org',
    'enterprise.net',
  ];

  for (let i = 0; i < count; i++) {
    const firstName = randomElement(firstNames);
    const lastName = randomElement(lastNames);
    const isCompany = randomBool();
    const company = isCompany ? randomElement(companies) : undefined;
    const name = isCompany ? company! : `${firstName} ${lastName}`;
    const email = `${firstName.toLowerCase()}.${lastName.toLowerCase()}@${randomElement(domains)}`;

    const customer: Customer = {
      id: randomId(),
      name,
      email,
      phone: randomBool()
        ? `+1-555-${randomNumber(100, 999)}-${randomNumber(1000, 9999)}`
        : undefined,
      address: randomBool()
        ? `${randomNumber(100, 999)} Main St, City, State ${randomNumber(10000, 99999)}`
        : undefined,
      customerType: isCompany ? 'COMPANY' : 'INDIVIDUAL',
      status: randomElement([
        'ACTIVE',
        'INACTIVE',
        'POTENTIAL',
        'BLOCKED',
      ] as CustomerStatus[]),
      companyName: isCompany ? company : undefined,
      taxNumber:
        isCompany && randomBool()
          ? `TAX${randomNumber(100000, 999999)}`
          : undefined,
      website:
        isCompany && randomBool()
          ? `https://www.${company?.toLowerCase().replace(/\s+/g, '')}.com`
          : undefined,
      notes: randomBool()
        ? 'Important customer with high potential value.'
        : undefined,
      assignedSalesRep: randomUser().name,
      tags: randomBool()
        ? [
            randomElement([
              'VIP',
              'High Value',
              'Tech Sector',
              'Enterprise',
              'SMB',
            ]),
          ]
        : [],
      customFields: {},
      totalValue: randomNumber(1000, 500000),
      lastContactDate: randomBool()
        ? randomDate(new Date(2024, 0, 1), new Date())
        : undefined,
      createdAt: randomDate(new Date(2023, 0, 1), new Date()),
      updatedAt: randomDate(new Date(2024, 0, 1), new Date()),
      isActive: true,
    };

    customers.push(customer);
  }

  return customers;
};

// Generate mock leads
export const generateMockLeads = (count: number = 30): Lead[] => {
  const leads: Lead[] = [];

  const firstNames = [
    'Alex',
    'Jamie',
    'Morgan',
    'Taylor',
    'Jordan',
    'Casey',
    'Robin',
    'Sam',
    'Riley',
    'Avery',
  ];
  const lastNames = [
    'Anderson',
    'Thompson',
    'White',
    'Martin',
    'Clark',
    'Hall',
    'Allen',
    'Wright',
    'King',
    'Scott',
  ];
  const companies = [
    'StartupTech',
    'GrowthCorp',
    'ScaleUp Inc',
    'NewVenture Ltd',
    'FreshIdeas Co',
    'ModernBiz',
    'NextLevel Corp',
    'BrightFuture Inc',
    'InnovatePlus',
    'TechStartup LLC',
  ];
  const jobTitles = [
    'CEO',
    'CTO',
    'VP Sales',
    'Marketing Director',
    'Operations Manager',
    'Product Manager',
    'Business Developer',
    'IT Director',
    'Sales Manager',
    'COO',
  ];

  for (let i = 0; i < count; i++) {
    const firstName = randomElement(firstNames);
    const lastName = randomElement(lastNames);
    const email = `${firstName.toLowerCase()}.${lastName.toLowerCase()}@${randomElement(['prospect.com', 'newclient.org', 'potential.net'])}`;

    const lead: Lead = {
      id: randomId(),
      firstName,
      lastName,
      email,
      phone: randomBool()
        ? `+1-555-${randomNumber(100, 999)}-${randomNumber(1000, 9999)}`
        : undefined,
      company: randomBool() ? randomElement(companies) : undefined,
      jobTitle: randomBool() ? randomElement(jobTitles) : undefined,
      source: randomElement([
        'WEBSITE',
        'REFERRAL',
        'EMAIL',
        'PHONE',
        'SOCIAL_MEDIA',
        'TRADE_SHOW',
        'OTHER',
      ] as LeadSource[]),
      status: randomElement([
        'NEW',
        'CONTACTED',
        'QUALIFIED',
        'CONVERTED',
        'LOST',
      ] as LeadStatus[]),
      priority: randomElement([
        'LOW',
        'MEDIUM',
        'HIGH',
        'URGENT',
      ] as Priority[]),
      assignedTo: randomBool() ? randomUser().id : undefined,
      estimatedValue: randomBool() ? randomNumber(5000, 100000) : undefined,
      expectedCloseDate: randomBool()
        ? randomDate(new Date(), new Date(2024, 11, 31))
        : undefined,
      notes: randomBool()
        ? 'Promising lead with strong interest in our solutions.'
        : undefined,
      tags: randomBool()
        ? [
            randomElement([
              'Hot Lead',
              'Enterprise',
              'SMB',
              'High Priority',
              'Follow Up',
            ]),
          ]
        : [],
      customFields: {},
      conversionDate: undefined,
      convertedToCustomerId: undefined,
      lastActivityDate: randomBool()
        ? randomDate(new Date(2024, 0, 1), new Date())
        : undefined,
      createdAt: randomDate(new Date(2023, 6, 1), new Date()),
      updatedAt: randomDate(new Date(2024, 0, 1), new Date()),
      isActive: true,
    };

    leads.push(lead);
  }

  return leads;
};

// Generate mock opportunities
export const generateMockOpportunities = (
  customers: Customer[],
  count: number = 25
): Opportunity[] => {
  const opportunities: Opportunity[] = [];

  const opportunityNames = [
    'Q1 Software License Renewal',
    'Enterprise Platform Implementation',
    'Cloud Migration Project',
    'Digital Transformation Initiative',
    'Security Upgrade Package',
    'Analytics Solution Deployment',
    'Mobile App Development',
    'Infrastructure Modernization',
    'Training & Support Package',
    'Custom Integration Project',
    'Consulting Services Agreement',
    'Hardware Refresh Program',
  ];

  for (let i = 0; i < count; i++) {
    const customer = randomElement(customers);
    const stage = randomElement([
      'PROSPECTING',
      'QUALIFICATION',
      'PROPOSAL',
      'NEGOTIATION',
      'CLOSED_WON',
      'CLOSED_LOST',
    ] as OpportunityStage[]);
    const user = randomUser();

    const opportunity: Opportunity = {
      id: randomId(),
      name: randomElement(opportunityNames),
      customerId: customer.id,
      customerName: customer.name,
      stage,
      type: randomElement([
        'NEW_BUSINESS',
        'EXISTING_BUSINESS',
        'RENEWAL',
      ] as OpportunityType[]),
      value: randomNumber(10000, 500000),
      probability:
        stage === 'CLOSED_WON'
          ? 100
          : stage === 'CLOSED_LOST'
            ? 0
            : randomNumber(10, 90),
      expectedCloseDate: randomDate(new Date(), new Date(2024, 11, 31)),
      actualCloseDate: ['CLOSED_WON', 'CLOSED_LOST'].includes(stage)
        ? randomDate(new Date(2024, 0, 1), new Date())
        : undefined,
      assignedTo: user.id,
      assignedToName: user.name,
      description: randomBool()
        ? 'Strategic opportunity to expand our relationship with this key customer.'
        : undefined,
      tags: randomBool()
        ? [
            randomElement([
              'Strategic',
              'High Value',
              'Competitive',
              'Renewal',
              'Expansion',
            ]),
          ]
        : [],
      products: [
        {
          id: randomId(),
          productId: `PROD${randomNumber(100, 999)}`,
          productName: randomElement([
            'Software License',
            'Professional Services',
            'Support Package',
            'Training Program',
          ]),
          quantity: randomNumber(1, 10),
          unitPrice: randomNumber(1000, 50000),
          discount: randomNumber(0, 20),
          totalPrice: 0, // Will be calculated
        },
      ],
      notes: randomBool()
        ? 'Key decision makers identified. Strong technical fit confirmed.'
        : undefined,
      competitors: randomBool()
        ? [randomElement(['CompetitorA', 'CompetitorB', 'CompetitorC'])]
        : undefined,
      nextAction: randomBool()
        ? 'Schedule technical demo with key stakeholders'
        : undefined,
      nextActionDate: randomBool()
        ? randomDate(new Date(), new Date(2024, 6, 30))
        : undefined,
      lostReason:
        stage === 'CLOSED_LOST'
          ? randomElement([
              'Price',
              'Competitor',
              'Budget',
              'Timing',
              'No Decision',
            ])
          : undefined,
      customFields: {},
      createdAt: randomDate(new Date(2023, 6, 1), new Date()),
      updatedAt: randomDate(new Date(2024, 0, 1), new Date()),
      isActive: true,
    };

    // Calculate product total price
    opportunity.products.forEach((product) => {
      product.totalPrice =
        product.quantity * product.unitPrice * (1 - product.discount / 100);
    });

    opportunities.push(opportunity);
  }

  return opportunities;
};

// Generate mock activities
export const generateMockActivities = (
  customers: Customer[],
  leads: Lead[],
  opportunities: Opportunity[],
  count: number = 100
): Activity[] => {
  const activities: Activity[] = [];

  const subjects = {
    CALL: [
      'Initial discovery call',
      'Follow-up call',
      'Product demo call',
      'Pricing discussion',
      'Contract review call',
    ],
    EMAIL: [
      'Proposal sent',
      'Follow-up email',
      'Meeting confirmation',
      'Documentation shared',
      'Contract terms discussion',
    ],
    MEETING: [
      'Product demonstration',
      'Stakeholder meeting',
      'Contract negotiation',
      'Project kickoff',
      'Requirements gathering',
    ],
    TASK: [
      'Prepare proposal',
      'Research competitor',
      'Update CRM records',
      'Schedule follow-up',
      'Send documentation',
    ],
    NOTE: [
      'Meeting notes',
      'Customer feedback',
      'Internal discussion',
      'Decision update',
      'Next steps documented',
    ],
    DEMO: [
      'Product demonstration',
      'Feature showcase',
      'Technical demo',
      'Solution overview',
      'Platform walkthrough',
    ],
    PROPOSAL: [
      'Technical proposal',
      'Commercial proposal',
      'Custom solution proposal',
      'Service agreement',
      'Partnership proposal',
    ],
    FOLLOW_UP: [
      'Post-meeting follow-up',
      'Action item follow-up',
      'Decision follow-up',
      'Timeline check-in',
      'Status update',
    ],
  };

  const locations = [
    'Conference Room A',
    'Customer Office',
    'Online Meeting',
    'Phone Call',
    'Video Conference',
    'Our Office',
    'Client Site',
    'Restaurant Meeting',
    'Coffee Shop',
    'Trade Show Booth',
  ];

  for (let i = 0; i < count; i++) {
    const activityType = randomElement([
      'CALL',
      'EMAIL',
      'MEETING',
      'TASK',
      'NOTE',
      'DEMO',
      'PROPOSAL',
      'FOLLOW_UP',
    ] as ActivityType[]);
    const user = randomUser();

    // Randomly choose related entity
    const relatedTypes = ['CUSTOMER', 'LEAD', 'OPPORTUNITY'] as const;
    const relatedType = randomElement([...relatedTypes]);
    let relatedEntity: { id: string; name: string };

    switch (relatedType) {
      case 'CUSTOMER':
        const customer = randomElement(customers);
        relatedEntity = { id: customer.id, name: customer.name };
        break;
      case 'LEAD':
        const lead = randomElement(leads);
        relatedEntity = {
          id: lead.id,
          name: `${lead.firstName} ${lead.lastName}`,
        };
        break;
      case 'OPPORTUNITY':
        const opportunity = randomElement(opportunities);
        relatedEntity = { id: opportunity.id, name: opportunity.name };
        break;
    }

    const isCompleted = randomBool();
    const isOverdue = !isCompleted && randomBool();

    const activity: Activity = {
      id: randomId(),
      type: activityType,
      status: isOverdue
        ? 'OVERDUE'
        : isCompleted
          ? 'COMPLETED'
          : randomElement(['PLANNED', 'IN_PROGRESS'] as ActivityStatus[]),
      subject: randomElement(subjects[activityType]),
      description: randomBool()
        ? 'Detailed discussion about requirements and next steps.'
        : undefined,
      scheduledDate: randomDate(new Date(2024, 0, 1), new Date(2024, 6, 30)),
      actualDate: isCompleted
        ? randomDate(new Date(2024, 0, 1), new Date())
        : undefined,
      duration: randomNumber(15, 180),
      priority: randomElement([
        'LOW',
        'MEDIUM',
        'HIGH',
        'URGENT',
      ] as Priority[]),
      assignedTo: user.id,
      assignedToName: user.name,
      relatedTo: {
        type: relatedType as 'CUSTOMER' | 'LEAD' | 'OPPORTUNITY',
        id: relatedEntity!.id,
        name: relatedEntity!.name,
      },
      participants: randomBool()
        ? [randomUser().email, randomUser().email]
        : undefined,
      location: ['MEETING', 'DEMO'].includes(activityType)
        ? randomElement(locations)
        : undefined,
      outcome: isCompleted
        ? randomElement([
            'Successful',
            'Needs follow-up',
            'Postponed',
            'Cancelled',
          ])
        : undefined,
      followUpRequired: isCompleted && randomBool(),
      followUpDate:
        isCompleted && randomBool()
          ? randomDate(new Date(), new Date(2024, 6, 30))
          : undefined,
      tags: randomBool()
        ? [
            randomElement([
              'Important',
              'Urgent',
              'Follow-up Required',
              'Decision Pending',
            ]),
          ]
        : [],
      customFields: {},
      createdAt: randomDate(new Date(2023, 6, 1), new Date()),
      updatedAt: randomDate(new Date(2024, 0, 1), new Date()),
      isActive: true,
    };

    activities.push(activity);
  }

  return activities.sort(
    (a, b) =>
      new Date(b.scheduledDate!).getTime() -
      new Date(a.scheduledDate!).getTime()
  );
};

// Generate all mock data
export const generateAllMockData = () => {
  const customers = generateMockCustomers(50);
  const leads = generateMockLeads(30);
  const opportunities = generateMockOpportunities(customers, 25);
  const activities = generateMockActivities(
    customers,
    leads,
    opportunities,
    100
  );

  return {
    customers,
    leads,
    opportunities,
    activities,
  };
};
