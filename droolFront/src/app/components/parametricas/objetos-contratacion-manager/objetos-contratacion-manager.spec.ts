import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ObjetosContratacionManager } from './objetos-contratacion-manager';

describe('ObjetosContratacionManager', () => {
  let component: ObjetosContratacionManager;
  let fixture: ComponentFixture<ObjetosContratacionManager>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ObjetosContratacionManager]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ObjetosContratacionManager);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
