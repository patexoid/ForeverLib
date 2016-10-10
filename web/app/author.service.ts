import { Injectable } from '@angular/core';
import { Http } from '@angular/http';

import 'rxjs/add/operator/toPromise';
import {Author} from "./Author";

@Injectable()
export class AuthorService {

    constructor(private http: Http) { }

    getAuthors(): Promise<Author[]> {
        var url = "http://localhost:8080/author";
        return this.http.get(url).toPromise()
             .then(response => response.json().content as Author[])
             .catch(AuthorService.handleError);
    }

    private static handleError(error: any): Promise<any> {
        console.error('An error occurred', error); // for demo purposes only
        return Promise.reject(error.message || error);
    }

}
