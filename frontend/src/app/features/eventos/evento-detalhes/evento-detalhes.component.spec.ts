import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EventoDetalhesComponent } from './evento-detalhes.component';

describe('EventoDetalhesComponent', () => {
  let component: EventoDetalhesComponent;
  let fixture: ComponentFixture<EventoDetalhesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EventoDetalhesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EventoDetalhesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
