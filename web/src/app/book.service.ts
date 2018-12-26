import { Injectable } from '@angular/core';
import {HttpService} from "./http-service.service";

@Injectable({
  providedIn: 'root'
})
export class BookService {

  constructor(private http: HttpService) { }

  uploadFiles(files: Array<File>) {
    this.http.uploadFiles("book/upload",files);
  }
}
