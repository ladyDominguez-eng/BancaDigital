import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PanelCajero } from './panel-cajero';

describe('PanelCajero', () => {
  let component: PanelCajero;
  let fixture: ComponentFixture<PanelCajero>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PanelCajero],
    }).compileComponents();

    fixture = TestBed.createComponent(PanelCajero);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
