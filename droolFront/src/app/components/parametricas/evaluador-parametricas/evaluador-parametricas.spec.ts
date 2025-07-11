import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EvaluadorParametricas } from './evaluador-parametricas';

describe('EvaluadorParametricas', () => {
  let component: EvaluadorParametricas;
  let fixture: ComponentFixture<EvaluadorParametricas>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EvaluadorParametricas]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EvaluadorParametricas);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
