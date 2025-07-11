import { TestBed } from '@angular/core/testing';

import { Parametricas } from './parametricas';

describe('Parametricas', () => {
  let service: Parametricas;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Parametricas);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
