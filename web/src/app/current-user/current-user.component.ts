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

  user: User;
  loggedIn: boolean;

  private currentUser(user: User) {
    this.user = user;
    this.loggedIn = user.permissions.indexOf('ROLE_USER') != -1;
  }

  ngOnInit() {
    this.userService.userPromise.then(value => this.currentUser(value))
  }

  login() {
    this.userService.login(this.user);
    this.ngOnInit()
  }

  logout() {
    this.userService.logout();
    this.ngOnInit()
  }

}
