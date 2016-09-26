/**
 * Created by Alexey on 9/5/2016.
 */
import {Component, Input} from "@angular/core";
import {Author} from "./Author";
import {AuthorService} from "./author.service";

@Component({
    selector: 'lib-author',
    template: `
<div *ngIf="author" class="main">
    <h1 >{{author.name}}</h1>
        <div>{{author.descr}}</div>
        <div class="sequence">
            <div *ngFor="let sequence of author.sequences" >
               {{sequence.name}}
                <div *ngFor="let bookSequence of sequence.bookSequences" >
                   {{bookSequence.seqOrder}} {{bookSequence.book.title}}
                </div>
        </div>
        <div *ngFor="let book of author.booksNoSequence" >
            {{book.title}}
        </div>

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


`],
    providers: [AuthorService]
})
export class AuthorComponent {

    @Input()
    author:Author;

}
