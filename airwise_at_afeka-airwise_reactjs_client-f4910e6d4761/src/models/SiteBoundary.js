import { ObjectBoundary } from './ObjectBoundary';

export class SiteBoundary extends ObjectBoundary {

  constructor(props) {
    super({ type: 'Site', ...props });
  }

}