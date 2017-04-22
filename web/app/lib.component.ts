import {Component} from "@angular/core";
@Component({
    selector: 'lib-main',
    template: `
        <nav>
            <div>
                <lib-user></lib-user>
            </div>
            <div>
                <a routerLink="/authors">Library</a>
                <a routerLink="/settings">Setting</a>
            </div>
        </nav>
        <router-outlet></router-outlet>
    `
})
export class LibComponent {
}
