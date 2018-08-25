import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {CookieService} from "./cookie.service";

@Injectable({
  providedIn: 'root'
})
export class HttpService {

  url = 'http://localhost:8080';
  private COOKIE_NAME="basic_cookie";

  basic: string;

  constructor(private http: HttpClient, private cookie: CookieService) {
    this.basic=this.cookie.getCookie(this.COOKIE_NAME);
  }

  get(uri: string): Promise<any> {
    const headers = this.getHeaders();
    return this.http.get(this.getFullUrl(uri), {headers: headers}).toPromise()
      .catch(HttpService.handleError);
  }

  post(uri: string, object: any): Promise<any> {
    const headers = this.getHeaders();
    headers.append('content-type', 'application/json');
    return this.http.post(this.getFullUrl(uri), JSON.stringify(object), {
      headers: headers,
    }).toPromise()
      .then(response => response)
      .catch(HttpService.handleError);
  }

  postForm(uri: string, params: URLSearchParams): Promise<any> {
    const headers = this.getHeaders();
    headers.append('content-type', 'application/x-www-form-urlencoded');
    return this.http.post(this.getFullUrl(uri), params.toString(), {
      headers: headers
    }).toPromise()
      .then(response => response)
      .catch(HttpService.handleError);
  }

  private getFullUrl(uri: string) {
    return this.url + '/' + uri;
  }

  private static handleError(error: any): Promise<any> {
    console.error('An error occurred', error); // for demo purposes only
    return Promise.reject(error.message || error);
  }

  private getHeaders(): HttpHeaders {
    let headers = new HttpHeaders();
    if (this.basic != null) {
      headers = headers.append('Authorization', 'Basic ' + this.basic);
    }
    return headers
  }

  login(username: string, password: string) {
    this.basic = btoa(username + ':' + password);
    this.cookie.setCookie(this.COOKIE_NAME, this.basic,1)//TODO is it secure? of course not
  }

  uploadFiles(uri: string, files: Array<File>) {
    if (files.length > 0) {
      let formData: FormData = new FormData();
      for (let i = 0; i < files.length; i++) {
        formData.append("file", files[i], files[i].name);
      }
      let headers = this.getHeaders();
      headers.append('Content-Type', 'multipart/form-data');
      headers.append('Accept', 'application/json');
      this.http.post(this.getFullUrl(uri), formData, {headers: headers})
        .toPromise()
        .then(response => response)
        .catch(HttpService.handleError);
    }
  }

  logout() {
    this.cookie.deleteCookie(this.COOKIE_NAME);
    this.get("user/logout");
    this.basic = null;
  }

}
