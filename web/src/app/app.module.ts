import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { CurrentUserComponent } from './current-user/current-user.component';
import { SettingsComponent } from './settings/settings.component';
import { AppRoutingModule } from './app-routing.module';
import { HttpClientModule }    from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { AuthorListComponent } from './author-list/author-list.component';
import { BookUploadComponent } from './book-upload/book-upload.component';
import { AuthorComponent } from './author/author.component';
import { BookComponent } from './book/book.component';
@NgModule({
  declarations: [
    AppComponent,
    CurrentUserComponent,
    SettingsComponent,
    AuthorListComponent,
    BookUploadComponent,
    AuthorComponent,
    BookComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }