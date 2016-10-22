/**
 * Created by Alexey on 9/5/2016.
 */
import {Component, OnInit } from "@angular/core";
import {Author} from "./Author";
import {AuthorService} from "./author.service";

@Component({
    selector: 'lib-authors-list',
    template: `
<lib-upload></lib-upload>
<h1>Author List</h1>
<div class="lib">
    <div class="authors">
        <div *ngFor="let author of authors"  [class.selected]="author === selectedAuthor"
        (click)="onSelect(author)">
            {{author.name}}
        </div>
    </div>
    <div class="author">
    <lib-author  *ngIf="selectedAuthor" [author] = "selectedAuthor"> </lib-author>
</div>
</div>
`,
    styles: [`
.selected {
  background-color: #CFD8DC !important;
  color: white;
}

.lib {
width: 100%;
overflow:auto;
}
.authors {
    width:18%;
    float:left;
    
}
.author {
     margin-left: 19%
}
`],
    providers: [AuthorService]
})
export class AuthorsComponent implements OnInit {

    authors: Author[];
    selectedAuthor: Author;

    constructor(private authorsService: AuthorService) {
    }

    getAuthors(): void {
        this.authorsService.getAuthors().then(authors => {
            this.authors = authors;});
    }

    ngOnInit(): void {
        this.getAuthors();
    }

    onSelect(author: Author): void {
        this.selectedAuthor = author;
    }

}
