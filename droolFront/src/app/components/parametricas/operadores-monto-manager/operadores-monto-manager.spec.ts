import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OperadoresMontoManager } from './operadores-monto-manager';

describe('OperadoresMontoManager', () => {
  let component: OperadoresMontoManager;
  let fixture: ComponentFixture<OperadoresMontoManager>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OperadoresMontoManager]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OperadoresMontoManager);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
