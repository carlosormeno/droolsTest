import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TiposProcesoManager } from './tipos-proceso-manager';

describe('TiposProcesoManager', () => {
  let component: TiposProcesoManager;
  let fixture: ComponentFixture<TiposProcesoManager>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TiposProcesoManager]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TiposProcesoManager);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
