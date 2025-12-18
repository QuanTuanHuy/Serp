/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Notification Sound and Vibration Utilities
 */

// Sound notification
let audioContext: AudioContext | null = null;

function getAudioContext(): AudioContext {
  if (!audioContext) {
    audioContext = new (window.AudioContext ||
      (window as any).webkitAudioContext)();
  }
  return audioContext;
}

export function playNotificationSound(
  type: 'default' | 'urgent' = 'default'
): void {
  try {
    const ctx = getAudioContext();
    const oscillator = ctx.createOscillator();
    const gainNode = ctx.createGain();

    oscillator.connect(gainNode);
    gainNode.connect(ctx.destination);

    // Different sounds for different notification types
    if (type === 'urgent') {
      // Urgent: higher pitched, multiple beeps
      oscillator.frequency.setValueAtTime(880, ctx.currentTime); // A5
      oscillator.type = 'sine';
      gainNode.gain.setValueAtTime(0.3, ctx.currentTime);
      gainNode.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.3);
      oscillator.start(ctx.currentTime);
      oscillator.stop(ctx.currentTime + 0.3);

      // Second beep
      setTimeout(() => {
        const osc2 = ctx.createOscillator();
        const gain2 = ctx.createGain();
        osc2.connect(gain2);
        gain2.connect(ctx.destination);
        osc2.frequency.setValueAtTime(880, ctx.currentTime);
        osc2.type = 'sine';
        gain2.gain.setValueAtTime(0.3, ctx.currentTime);
        gain2.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.3);
        osc2.start(ctx.currentTime);
        osc2.stop(ctx.currentTime + 0.3);
      }, 150);
    } else {
      // Default: soft single beep
      oscillator.frequency.setValueAtTime(523.25, ctx.currentTime); // C5
      oscillator.type = 'sine';
      gainNode.gain.setValueAtTime(0.2, ctx.currentTime);
      gainNode.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.2);
      oscillator.start(ctx.currentTime);
      oscillator.stop(ctx.currentTime + 0.2);
    }
  } catch (error) {
    console.warn('Failed to play notification sound:', error);
  }
}

// Vibration
export function vibrateDevice(pattern: number | number[] = 200): void {
  try {
    if ('vibrate' in navigator) {
      navigator.vibrate(pattern);
    }
  } catch (error) {
    console.warn('Vibration not supported:', error);
  }
}

// Combined notification alert
export function notifyUser(
  options: {
    sound?: boolean;
    vibrate?: boolean;
    urgent?: boolean;
  } = {}
): void {
  const { sound = true, vibrate = true, urgent = false } = options;

  if (sound) {
    playNotificationSound(urgent ? 'urgent' : 'default');
  }

  if (vibrate) {
    vibrateDevice(urgent ? [200, 100, 200] : 200);
  }
}

// Browser notification permission
export async function requestNotificationPermission(): Promise<boolean> {
  if (!('Notification' in window)) {
    console.warn('Browser does not support notifications');
    return false;
  }

  if (Notification.permission === 'granted') {
    return true;
  }

  if (Notification.permission !== 'denied') {
    const permission = await Notification.requestPermission();
    return permission === 'granted';
  }

  return false;
}

// Show browser notification
export function showBrowserNotification(
  title: string,
  options?: NotificationOptions
): void {
  if ('Notification' in window && Notification.permission === 'granted') {
    new Notification(title, {
      icon: '/favicon.ico',
      badge: '/favicon.ico',
      ...options,
    });
  }
}
