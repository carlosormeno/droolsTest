import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UitManager } from './uit-manager';

describe('UitManager', () => {
  let component: UitManager;
  let fixture: ComponentFixture<UitManager>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UitManager]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UitManager);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
