import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule }   from '@angular/forms';

import { LibComponent }  from './lib.component';
import {AuthorsComponent} from "./authors.component";
import { HttpModule }    from '@angular/http';
import {AuthorService} from "./author.service";
@NgModule({
    imports:      [
        BrowserModule,
        FormsModule,
        HttpModule,],
    declarations: [
        LibComponent,
        AuthorsComponent
    ],
    providers: [
        AuthorService,
    ],
    bootstrap:    [ LibComponent ]
})
export class LibModule { }