import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule }   from '@angular/forms';

import { LibComponent }  from './lib.component';
import {AuthorsComponent} from "./authors.component";
import {AuthorComponent} from "./author.component";
import { HttpModule }    from '@angular/http';
import {AuthorService} from "./author.service";
import {BookComponent} from "./book.component";
import {BookUpload} from "./bookUpload";
@NgModule({
    imports:      [
        BrowserModule,
        FormsModule,
        HttpModule,],
    declarations: [
        LibComponent,
        AuthorsComponent,
        AuthorComponent,
        BookComponent,
        BookUpload
    ],
    providers: [
        AuthorService,
    ],
    bootstrap:    [ LibComponent ]
})
export class LibModule { }
