import {Component, OnInit} from '@angular/core';
import {UserService} from "../user.service";
import {User} from "../user";

@Component({
  selector: 'app-current-user',
  templateUrl: './current-user.component.html',
  styleUrls: ['./current-user.component.css']
})
export class CurrentUserComponent implements OnInit {

  constructor(private userService: UserService) {
  }

  username: string;
  password: string;
  loggedIn: boolean;

  private currentUser(user: User) {
    this.loggedIn = user.permissions.indexOf('ROLE_USER') != -1;
    if (this.loggedIn) {
      this.username = user.username
    } else {
      this.username = null;
    }
  }

  ngOnInit() {
    this.userService.userPromise.then(value => this.currentUser(value))
  }

  login() {
    this.userService.login(this.username, this.password);
    this.password = null;
    this.ngOnInit()
  }

  logout() {
    this.userService.logout();
    this.ngOnInit()
  }

  signup(){
    this.userService.signup(this.username, this.password).then(() => this.ngOnInit());
    this.password = null;
  }
}
