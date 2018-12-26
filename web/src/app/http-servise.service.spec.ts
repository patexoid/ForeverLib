import { TestBed, inject } from '@angular/core/testing';

import { HttpServiseService } from './http-servise.service';

describe('HttpServiseService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [HttpServiseService]
    });
  });

  it('should be created', inject([HttpServiseService], (service: HttpServiseService) => {
    expect(service).toBeTruthy();
  }));
});
