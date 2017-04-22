import { NgModule }             from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {SettingsComponent} from "./settings.component";
import {AuthorsComponent} from "./authors.component";

const routes: Routes = [
    // { path: process.env.public_path+'', redirectTo: process.env.public_path+'/authors', pathMatch: 'full' },
    { path: 'settings',  component: SettingsComponent },
    { path: 'authors', component: AuthorsComponent },
    { path: '',   redirectTo: '/authors', pathMatch: 'full' },
    { path: '**',   redirectTo: '/authors', pathMatch: 'full'},
];
@NgModule({
    imports: [ RouterModule.forRoot(routes) ],
    exports: [ RouterModule ]
})
export class LibRoutingModule {}