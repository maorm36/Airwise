import { ObjectBoundary } from './ObjectBoundary';

export class AirConditionerBoundary extends ObjectBoundary {
  constructor(props) {
    super({ type: 'AirConditioner', ...props });
  }
}