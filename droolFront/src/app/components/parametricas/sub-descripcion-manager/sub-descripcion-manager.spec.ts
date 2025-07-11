import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SubDescripcionManager } from './sub-descripcion-manager';

describe('SubDescripcionManager', () => {
  let component: SubDescripcionManager;
  let fixture: ComponentFixture<SubDescripcionManager>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SubDescripcionManager]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SubDescripcionManager);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
