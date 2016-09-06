/**
 * Created by Alexey on 9/5/2016.
 */
import {Component, OnInit } from "@angular/core";
import {Author} from "./Author";
import {AuthorService} from "./author.service";

@Component({
    selector: 'lib-authors-list',
    template: `
<h1>Authors {{aa}}</h1>
<ul class="authors">
<!--<li *ngFor="let author of authors"-->
    <!--<span class="badge">{{author.id}}</span> {{author.name}}-->
  <!--</li>-->
  </ul>
`,
    providers: [AuthorService]
})
export class AuthorsComponent  implements OnInit {

    authors: Author[];
    aa:string;

    constructor(private authorsService: AuthorService) {
    }

    getAuthors(): void {
        this.authorsService.getAuthors().then(authors => {this.authors = authors;this.aa=JSON.stringify(authors)});
    }
    ngOnInit(): void {
        this.getAuthors();
    }
}