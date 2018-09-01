import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BookUploadComponent } from './book-upload.component';

describe('BookUploadComponent', () => {
  let component: BookUploadComponent;
  let fixture: ComponentFixture<BookUploadComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BookUploadComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BookUploadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
