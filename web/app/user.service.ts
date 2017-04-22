import {Injectable} from "@angular/core";
import {URLSearchParams} from "@angular/http";

import "rxjs/add/operator/toPromise";
import {HttpService} from "./HttpService";
import {User} from "./User";

@Injectable()
export class UserService {

    constructor(private http: HttpService) {
    }

    getCurrentUser(): Promise<User> {
        return this.http.get('user/current').then(obj => obj as User)
    }

    updatePassword(oldPassword: string, newPasssword: string) {
        let urlSearchParams = new URLSearchParams();
        urlSearchParams.append('old', oldPassword);
        urlSearchParams.append('new', newPasssword);
        this.http.postForm("user/updatePassword", urlSearchParams)
    }

}
