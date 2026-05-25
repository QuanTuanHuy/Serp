import { TtcrsLayout } from '@/modules/ttcrs/components/layout';

export default function DriverLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return <TtcrsLayout>{children}</TtcrsLayout>;
}
