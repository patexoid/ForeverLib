import "rxjs/add/operator/toPromise";
import {Injectable} from "@angular/core";
import {Http, Headers} from "@angular/http";

@Injectable()
export class HttpService {

    url = process.env.API_URL;

    constructor(private http: Http) {
    }

    get(uri: string): Promise<any> {
        return this.http.get(this.url + '/' + uri).toPromise()
            .then(response => response.json())
            .catch(HttpService.handleError);
    }

    post(uri: string, object: any): Promise<any> {
        var headers = new Headers({'content-type': 'application/json'});
        return this.http.post(this.url + '/' + uri, JSON.stringify(object), {headers: headers}).toPromise()
            .then(response => response.json())
    }

    private static handleError(error: any): Promise<any> {
        console.error('An error occurred', error); // for demo purposes only
        return Promise.reject(error.message || error);
    }
}