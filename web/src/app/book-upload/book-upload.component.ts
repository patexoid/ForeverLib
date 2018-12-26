import { Component, OnInit } from '@angular/core';
import {BookService} from "../book.service";

@Component({
  selector: 'book-upload',
  templateUrl: './book-upload.component.html',
  styleUrls: ['./book-upload.component.css']
})
export class BookUploadComponent implements OnInit {

  ngOnInit() {
  }


  constructor(private bookService: BookService) { }

  fileChangeEvent(fileInput: any): void {
    let files = fileInput.target.files;
    this.bookService.uploadFiles(files);
    fileInput.srcElement.parentElement.reset();
  }
}
