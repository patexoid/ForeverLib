/**
 * Created by Alexey on 10/10/2016.
 */
/**
 * Created by Alexey on 9/5/2016.
 */
import {Component} from "@angular/core";
import {HttpService} from "./HttpService";
import {BookService} from "./book.service";


@Component({
    selector: 'lib-upload',
    template: `
<div>
<form>
    <input type="button"  value="Upload books..." (click)="fileupload.click()"/>
    <input #fileupload type="file" hidden name="book" multiple (change)="fileChangeEvent($event)" accept=".fb2, .fb2.zip"/>

</form>
</div>
`
})
export class BookUpload{

    constructor(private bookService: BookService) { }

    fileChangeEvent(fileInput: any): void {
        var files = fileInput.target.files;
        this.bookService.uploadFiles(files);
         fileInput.srcElement.parentElement.reset();
    }

}
