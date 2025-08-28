import { ObjectBoundary } from './ObjectBoundary';

export class RoomBoundary extends ObjectBoundary {
  constructor(props) {
    super({ type: 'Room', ...props });
  }
}