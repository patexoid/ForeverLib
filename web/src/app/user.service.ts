import {Injectable, OnInit} from "@angular/core";
import {User} from "./User";
import {HttpService} from "./http-service.service";

@Injectable({
  providedIn: 'root'
})
export class UserService implements OnInit {

  constructor(private http: HttpService) {
  }

  private user: User;
  private _userPromise = this.getCurrentUser();

  ngOnInit() {
    this._userPromise = this.getCurrentUser();
  }

  private getCurrentUser(): Promise<User> {
    return this.http.get('user/current').then(obj => obj as User).then(value => this.user = value)
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

  login(user: User) {
    this.http.login(user.username, user.password);
    this.ngOnInit()
  }

  logout() {
    this.http.logout();
    this.ngOnInit()
  }
}
