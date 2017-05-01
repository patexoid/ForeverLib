import { Injectable } from '@angular/core';
import {Http, Headers, RequestOptions} from '@angular/http';

import 'rxjs/add/operator/toPromise';
import {Book} from "./Book";
import {HttpService} from "./HttpService";

@Injectable()
export class BookService {

    constructor(private http: HttpService) { }

    getBook( id:number ): Promise<Book> {
        return this.http.get('book/'+id).then(obj => obj as Book)
    }


    saveBook(book:Book): Promise<Book> {
        return this.http.post("book",book)
             .then(obj => obj.content as Book)
    }

    uploadFiles(files: Array<File>) {
        this.http.uploadFiles("/book/upload",files);
    }
}
