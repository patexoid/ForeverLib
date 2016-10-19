/**
 * Created by Alexey on 9/5/2016.
 */
import {Component, Input, OnChanges, SimpleChanges} from "@angular/core";
import {Book} from "./Book";
import {BookService} from "./book.service";

@Component({
    selector: 'lib-book',
    template: `
<div *ngIf="_book" class="main">
    <div>
        <input class="title" type="text" [(ngModel)]="title" (input)="setEdited(true)">
    </div>
    <div>
        <textarea class="descr" [(ngModel)]="descr" rows="5" (input)="setEdited(true)" ></textarea>
    </div>
    <div *ngIf="changed" >
        <button    (click)="saveBook()">Save book</button>
    </div>
</div>
`,
    styles: [`
.main {
    border: 4px double black;
}
.title {
    width:100% ;
}

.descr {
    width: 100%;

}
h1 {
    text-align: center;
}


`],
    providers: [BookService]

})
export class BookComponent implements OnChanges {

    constructor(private bookService: BookService) {
    }

    changed: boolean;

    _book: Book;

    title: string;

    descr: string;

    @Input()
    set book(book: Book) {
        this.title=book.title;
        this.descr=book.descr;
        this._book = book
    }

    ngOnChanges(changes: SimpleChanges): void {
        this.setEdited(false)
    }


    setEdited(edited: boolean) {
        this.changed = edited;
    }

    saveBook() {
        this._book.title=this.title;
        this._book.descr=this.descr;
        this.bookService.saveBook(this._book);
        this.changed = false;
    }
}
