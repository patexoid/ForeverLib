import {Component, OnInit} from "@angular/core";

@Component({
    selector: 'lib-settings',
    template: `
        <h1>Settings</h1>
        <div class="main">
            <div class="menu">
            MENU
            </div>
            <user-settings></user-settings>
        </div>
    `,
    styles: [`
        .selected {
            background-color: #CFD8DC !important;
            color: white;
        }
    `]
})
export class SettingsComponent implements OnInit {

    ngOnInit(): void {

    }

}
