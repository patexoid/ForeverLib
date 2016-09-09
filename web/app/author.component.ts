/**
 * Created by Alexey on 9/5/2016.
 */
import {Component, Input} from "@angular/core";
import {Author} from "./Author";
import {AuthorService} from "./author.service";

@Component({
    selector: 'lib-author',
    template: `
<div >
<h1 >{{author.name}}</h1>

</div>
`,
    styles:[` 
   
    }
    
`],
    providers: [AuthorService]
})
export class AuthorComponent  {

    @Input()
    author: Author;

}