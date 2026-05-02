/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Open notification deep links consistently
 */

export function navigateNotificationAction(
  router: { push: (href: string) => void },
  actionUrl?: string | null
): void {
  if (!actionUrl) {
    return;
  }
  if (actionUrl.startsWith('/')) {
    router.push(actionUrl);
  } else {
    window.open(actionUrl, '_blank', 'noopener,noreferrer');
  }
}
