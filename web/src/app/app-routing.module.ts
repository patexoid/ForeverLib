import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {AuthorsComponent} from "./authors/authors.component";
import {SettingsComponent} from "./settings/settings.component";
import {AuthorListComponent} from "./author-list/author-list.component";

const routes: Routes = [
  {path: '', redirectTo: '/authors', pathMatch: 'full'},
  {path: 'authors', component: AuthorListComponent},
  {path: 'settings', component: SettingsComponent},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
