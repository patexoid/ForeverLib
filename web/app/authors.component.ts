/**
 * Created by Alexey on 9/5/2016.
 */
import {Component, OnInit} from "@angular/core";
import {Author} from "./Author";
import {AuthorService} from "./author.service";

@Component({
    selector: 'lib-authors-list',
    template: `
<lib-upload></lib-upload>
<h1>Author List</h1>
<div class="lib">
    <div class="authors">
    <input class="filter" type="text" [(ngModel)]="filter" (ngModelChange)="refreshList()">
        <div *ngFor="let author of authors"  [class.selected]="author === selectedAuthor"
        (click)="onSelect(author)">
            {{author.name}}
        </div>
        <input class="filter" type="number" size="3" [attr.max]="pageCount" 
        [(ngModel)]="pageNumber" (ngModelChange)="refreshList()">/{{pageCount}}
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
    filter: string;
    pageNumber: number;
    pageCount: number;

    constructor(private authorsService: AuthorService) {
    }

    getAuthors(): void {
        this.authorsService.getAuthors(this.filter,this.pageNumber,20).then(page => {
            this.authors = page.content as Author[];
            this.pageCount = page.totalPages;

        });
    }

    refreshList(): void {
        this.getAuthors();
    }


    ngOnInit(): void {
        this.pageNumber=0;
        this.filter='';
        this.pageCount=0;
        this.getAuthors();
    }

    onSelect(author: Author): void {
        this.selectedAuthor = author;
    }

}
