import {Injectable, OnInit} from "@angular/core";
import {User} from "./user";
import {HttpService} from "./http-service.service";

@Injectable({
  providedIn: 'root'
})
export class UserService implements OnInit {

  constructor(private http: HttpService) {
  }

  private _userPromise = this.getCurrentUser();

  ngOnInit() {
    this._userPromise = this.getCurrentUser();
  }

  private getCurrentUser(): Promise<User> {
    return this.http.get('user/current').then(obj => obj as User);
  }

  get userPromise(): Promise<User> {
    return this._userPromise;
  }

// updatePassword(oldPassword: string, newPasssword: string) {
  //   let urlSearchParams = new URLSearchParams();
  //   urlSearchParams.append('old', oldPassword);
  //   urlSearchParams.append('new', newPasssword);
  //   this.http.postForm("user/updatePassword", urlSearchParams)
  // }

  login(username: string, password: string) {
    this.http.login(username, password);
    this.ngOnInit()
  }

  signup(username: string, password: string): Promise<User> {
    let user = new User();
    user.username = username;
    user.password = password;

    return this.http.postNoAuth('user/create', user).then(obj => this.loginU(obj, user));
  }

  private loginU(newUser: User, user: User): User {
    this.login(user.username, user.password);
    return newUser;
  }

  logout() {
    this.http.logout();
    this.ngOnInit()
  }
}
