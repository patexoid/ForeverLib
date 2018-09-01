import { Injectable } from '@angular/core';
import {HttpService} from "./http-service.service";
import {Author} from "./author";
import {Page} from "./page";

@Injectable({
  providedIn: 'root'
})
export class AuthorService {

  url = "author";

  constructor(private http: HttpService) {
  }

  getAuthors(filter: string, page: number, size: number): Promise<Page> {
    return this.http.get(this.url + '?prefix=' + filter + '&page=' + page + '&size=' + size)
      .then(obj => obj as Page);
  }

  getAuthor(id: number): Promise<Author> {
    return this.http.get(this.url + '/' + id)
      .then(obj =>
        obj as Author);
  }
}
