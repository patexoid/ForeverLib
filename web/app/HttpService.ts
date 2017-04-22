import "rxjs/add/operator/toPromise";
import {Injectable} from "@angular/core";
import {Headers, Http, URLSearchParams} from "@angular/http";

@Injectable()
export class HttpService {

    url = process.env.API_URL;

    basic:string;

    constructor(private http: Http) {
    }

    get(uri: string): Promise<any> {
        const headers = this.getHeaders();
        return this.http.get(this.url + '/' + uri, {headers: headers}).toPromise()
            .then(response => response.json())
            .catch(HttpService.handleError);
    }

    post(uri: string, object: any): Promise<any> {
        const headers = this.getHeaders();
        headers.append('content-type', 'application/json');
        return this.http.post(this.url + '/' + uri, JSON.stringify(object), {
            headers: headers,
        }).toPromise()
            .then(response => response.json())
            .catch(HttpService.handleError);
    }

    postForm(uri: string, params: URLSearchParams): Promise<any> {
        const headers = this.getHeaders();
        headers.append('content-type', 'application/x-www-form-urlencoded');
        return this.http.post(this.url + '/' + uri, params.toString(), {
            headers: headers
        }).toPromise()
            .then(response => response.json())
            .catch(HttpService.handleError);
    }

    private static handleError(error: any): Promise<any> {
        console.error('An error occurred', error); // for demo purposes only
        return Promise.reject(error.message || error);
    }

    private getHeaders(): Headers {
        const headers = new Headers();
        if(this.basic!=null){
            headers.append('Authorization', 'Basic ' + this.basic);
        }
        return headers
    }

    login(username: string, password: string) {
        this.basic = btoa(username + ':' + password);
    }

    logout() {
        this.get("user/logout");
        this.basic = null;
    }
}