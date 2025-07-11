import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FormularioDinamico } from './formulario-dinamico';

describe('FormularioDinamico', () => {
  let component: FormularioDinamico;
  let fixture: ComponentFixture<FormularioDinamico>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormularioDinamico]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FormularioDinamico);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
