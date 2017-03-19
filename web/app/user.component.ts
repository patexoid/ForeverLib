import {Component, OnInit} from "@angular/core";
import {UserService} from "./user.service";
import {User} from "./User";
import {HttpService} from "./HttpService";

@Component({
    selector: 'lib-user',
    template: `
        <div>
            <div class="main" *ngIf="user; else loginDiv">
                <input type="text" contenteditable="false" [(ngModel)]="user.username"/>
                <button id="logoutButton" (click)="logout()">Logout</button>
            </div>
            <ng-template #loginDiv>
                    <label for="loginInput">Login:</label>
                    <input type="text" id="loginInput" [(ngModel)]="username"/>
                    <label for="passwordInput">Password:</label>
                    <input type="password" id="passwordInput" [(ngModel)]="password"/>
                <button (click)="login()">Login</button>
            </ng-template>
        </div>
    `,
    styles: [`
        .selected {
            background-color: #CFD8DC !important;
            color: white;
        }
    `],
})
export class UserComponent implements OnInit {

    user: User;

    username:string;
    password:string;

    constructor(private http: HttpService, private userService: UserService) {
    }

    ngOnInit(): void {
        this.check();
    }

    private check() {
        this.userService.getCurrentUser().then(value => this.user = value).catch(
            this.user = null
        )
    }

    login(): void {
        this.http.login(this.username,this.password)
        this.password=null;
        this.check()
    }

    logout(): void {
        this.http.logout();
        this.check()
    }

}
