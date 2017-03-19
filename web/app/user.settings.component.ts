import {Component, OnInit} from "@angular/core";
import {UserService} from "./user.service";
import {User} from "./User";

@Component({
    selector: 'user-settings',
    template: `
        <div>
            <h1>User settings</h1>
            <div class="main" *ngIf="user">
                <div>
                    <label for="loginInput">Login:</label>
                    <input type="text" id="loginInput" [(ngModel)]="user.username"/>
                </div>
                <div>
                    <label for="password">Password:</label>
                    <input type="password" id="passwordInput" [(ngModel)]="user.password"/>
                </div>
                <div>
                    <label for="NewPasswordInput">New Password:</label>
                    <input type="password" id="NewPasswordInput" [(ngModel)]="newPassword"/>
                </div>
                <button (click)="updatePassword()">Update Password</button>
            </div>
        </div>
    `,
    styles: [`
        .selected {
            background-color: #CFD8DC !important;
            color: white;
        }
    `],
    providers: [UserService]
})
export class UserSettingsComponent implements OnInit {

    user: User;
    newPassword: string;

    constructor(private userService: UserService) {
    }

    ngOnInit(): void {
        this.userService.getCurrentUser().then(value => this.user = value)
    }

    updatePassword(): void {
        this.userService.updatePassword(this.user.password, this.newPassword);
        this.user.password = '';
        this.newPassword = '';
    }


}
