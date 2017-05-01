import "rxjs/add/operator/toPromise";
import {Injectable} from "@angular/core";
import {Headers, Http, RequestOptions, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs/Observable";

@Injectable()
export class HttpService {

    url = process.env.API_URL;

    basic:string;

    constructor(private http: Http) {
    }

    get(uri: string): Promise<any> {
        const headers = this.getHeaders();
        return this.http.get(this.getFullUrl(uri), {headers: headers}).toPromise()
            .then(response => response.json())
            .catch(HttpService.handleError);
    }

    post(uri: string, object: any): Promise<any> {
        const headers = this.getHeaders();
        headers.append('content-type', 'application/json');
        return this.http.post(this.getFullUrl(uri), JSON.stringify(object), {
            headers: headers,
        }).toPromise()
            .then(response => response.json())
            .catch(HttpService.handleError);
    }

    postForm(uri: string, params: URLSearchParams): Promise<any> {
        const headers = this.getHeaders();
        headers.append('content-type', 'application/x-www-form-urlencoded');
        return this.http.post(this.getFullUrl(uri), params.toString(), {
            headers: headers
        }).toPromise()
            .then(response => response.json())
            .catch(HttpService.handleError);
    }

    private getFullUrl(uri: string) {
        return this.url + '/' + uri;
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

    uploadFiles(uri: string, files: Array<File>) {
        if (files.length > 0) {
            let formData: FormData = new FormData();
            for(let i = 0; i < files.length; i++) {
                formData.append("file", files[i], files[i].name);
            }
            let headers = this.getHeaders();
            // headers.append('Content-Type', 'multipart/form-data');
            // headers.append('Accept', 'application/json');

            let options = new RequestOptions({headers: headers});
            this.http.post(this.getFullUrl(uri), formData, options)
                .map(res => res.json())
                .catch(error => Observable.throw(error))
                .subscribe(
                    data => console.log('success'),
                    error => console.log(error)
                )
        }
    }

    logout() {
        this.get("user/logout");
        this.basic = null;
    }
}