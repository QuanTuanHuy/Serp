import { TtcrsLayout } from '@/modules/ttcrs/components/layout';

export default function DispatcherLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return <TtcrsLayout>{children}</TtcrsLayout>;
}
