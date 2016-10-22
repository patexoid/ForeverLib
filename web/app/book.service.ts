import { Injectable } from '@angular/core';
import {Http, Headers, RequestOptions} from '@angular/http';

import 'rxjs/add/operator/toPromise';
import {Book} from "./Book";

@Injectable()
export class BookService {

    url = "http://localhost:8080/book";

    constructor(private http: Http) { }

    getBook( id:number ): Promise<Book> {
        return this.http.get(this.url+'/'+id).toPromise()
            .then(response =>
                response.json() as Book)
            .catch(BookService.handleError);
    }


    saveBook(book:Book): Promise<Book> {
        var headers = new Headers({ 'content-type': 'application/json' });
        return this.http.post(this.url,JSON.stringify(book),{ headers: headers }).toPromise()
             .then(response => response.json().content as Book)
             .catch(BookService.handleError);
    }

    private static handleError(error: any): Promise<any> {
        console.error('An error occurred', error); // for demo purposes only
        return Promise.reject(error.message || error);
    }

}
