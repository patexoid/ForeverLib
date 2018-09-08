import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {SettingsComponent} from "./settings/settings.component";
import {AuthorListComponent} from "./author-list/author-list.component";
import {AuthorComponent} from "./author/author.component";

const routes: Routes = [
  {path: '', redirectTo: '/authors', pathMatch: 'full'},
  {
    path: 'authors', component: AuthorListComponent,
    children:[
      {
        path:':id',
        component:AuthorComponent
      }
      ]
  },
  {path: 'settings', component: SettingsComponent},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
