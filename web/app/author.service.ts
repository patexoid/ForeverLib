import {Injectable} from "@angular/core";
import {Http} from "@angular/http";
import "rxjs/add/operator/toPromise";
import {Author} from "./Author";
import {Page} from "./Page";
import {HttpService} from "./HttpService";

@Injectable()
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
