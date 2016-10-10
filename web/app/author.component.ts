/**
 * Created by Alexey on 9/5/2016.
 */
import {Component, Input} from "@angular/core";
import {Author} from "./Author";
import {Book} from "./Book";

@Component({
    selector: 'lib-author',
    template: `
<div *ngIf="author" class="main">
    <h1 >{{author.name}}</h1>
        <div>{{author.descr}}</div>
        <div class="sequence">
            <div *ngFor="let sequence of author.sequences" >
               {{sequence.name}}
                <div *ngFor="let bookSequence of sequence.bookSequences" (click)="onSelect(bookSequence.book)">
                   {{bookSequence.seqOrder}} {{bookSequence.book.title}}
                   
                </div>
            </div>
            <div *ngFor="let book of author.booksNoSequence" >
                {{book.title}}
            </div>
        </div>
<lib-book  *ngIf="selectedBook" [book] = "selectedBook"> </lib-book>
</div>
`,
    styles: [`
.main {
    border: 4px double black;
}
h1 {
    text-align: center;
}


`]
})
export class AuthorComponent {

    @Input()
    author:Author;

    selectedBook:Book;


    onSelect(book: Book): void {
        this.selectedBook = book;
    }
}
