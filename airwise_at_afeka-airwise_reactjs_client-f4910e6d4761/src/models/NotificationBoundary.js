import { ObjectBoundary } from './ObjectBoundary';

export class NotificationBoundary extends ObjectBoundary {
  constructor(props) {
    super({ type: 'Notification', ...props });
  }
}