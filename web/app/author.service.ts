import {Injectable} from "@angular/core";
import {Http} from "@angular/http";
import "rxjs/add/operator/toPromise";
import {Author} from "./Author";
import {Page} from "./Page";

@Injectable()
export class AuthorService {

    url = "http://localhost:8080/author";

    constructor(private http: Http) {
    }

    getAuthors(filter: string, page: number, size: number): Promise<Page> {
        return this.http.get(this.url + '?prefix=' + filter + '&page=' + page + '&size=' + size).toPromise()
            .then(response => response.json() as Page)
            .catch(AuthorService.handleError);
    }

    getAuthor(id: number): Promise<Author> {
        return this.http.get(this.url + '/' + id).toPromise()
            .then(response =>
                response.json() as Author)
            .catch(AuthorService.handleError);
    }


    private static handleError(error: any): Promise<any> {
        console.error('An error occurred', error); // for demo purposes only
        return Promise.reject(error.message || error);
    }

}
