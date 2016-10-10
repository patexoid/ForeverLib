/**
 * Created by Alexey on 9/5/2016.
 */
import {Component, Input, OnInit} from "@angular/core";
import {Book} from "./Book";

@Component({
    selector: 'lib-book',
    template: `
<div *ngIf="book" class="main">
    <h1 >{{book.title}}</h1>
    <div>
        <p *ngFor="let paragr of descr">
            {{paragr}}
        </p>
    </div>
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
export class BookComponent implements OnInit {

    @Input()
    book: Book;

    descr:string[];

    ngOnInit(): void {
        this.descr  = this.book.descr.split(/\r\n|\r|\n/g)
    }
}
