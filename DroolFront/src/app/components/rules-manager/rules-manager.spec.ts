import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RulesManager } from './rules-manager';

describe('RulesManager', () => {
  let component: RulesManager;
  let fixture: ComponentFixture<RulesManager>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RulesManager]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RulesManager);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
