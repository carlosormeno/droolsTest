import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TopesManager } from './topes-manager';

describe('TopesManager', () => {
  let component: TopesManager;
  let fixture: ComponentFixture<TopesManager>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TopesManager]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TopesManager);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
