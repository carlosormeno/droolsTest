import { TestBed } from '@angular/core/testing';

import { Drools } from './drools';

describe('Drools', () => {
  let service: Drools;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Drools);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
