import { TestBed } from '@angular/core/testing';
import { Cajero } from './cajero';

describe('Cajero', () => {
  let service: Cajero;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Cajero);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
