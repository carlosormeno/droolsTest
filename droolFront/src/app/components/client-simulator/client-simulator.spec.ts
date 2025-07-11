import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClientSimulator } from './client-simulator';

describe('ClientSimulator', () => {
  let component: ClientSimulator;
  let fixture: ComponentFixture<ClientSimulator>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClientSimulator]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ClientSimulator);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
