import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";

import {LibComponent} from "./lib.component";
import {AuthorsComponent} from "./authors.component";
import {AuthorComponent} from "./author.component";
import {HttpModule} from "@angular/http";
import {AuthorService} from "./author.service";
import {BookComponent} from "./book.component";
import {BookUpload} from "./bookUpload";
import {BookService} from "./book.service";
import {HttpService} from "./HttpService";
import {SettingsComponent} from "./settings.component";
import {LibRoutingModule} from "./lib-routing.module";
import {UserSettingsComponent} from "./user.settings.component";
import {UserService} from "./user.service";
import {UserComponent} from "./user.component";
@NgModule({
    imports: [
        BrowserModule,
        FormsModule,
        HttpModule,
        LibRoutingModule],
    declarations: [
        LibComponent,
        AuthorsComponent,
        AuthorComponent,
        BookComponent,
        SettingsComponent,
        BookUpload,
        UserSettingsComponent,
        UserComponent
    ],
    providers: [
        AuthorService,
        BookService,
        UserService,
        HttpService
    ],
    bootstrap: [LibComponent]
})
export class LibModule {
}
