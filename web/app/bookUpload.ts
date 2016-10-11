/**
 * Created by Alexey on 10/10/2016.
 */
/**
 * Created by Alexey on 9/5/2016.
 */
import {Component} from "@angular/core";


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


    fileChangeEvent(fileInput: any): void {
        var files = fileInput.target.files;
        this.makeFileRequest("http://localhost:8080/book/upload",files);
         fileInput.srcElement.parentElement.reset();
    }

    makeFileRequest(url: string, files: Array<File>) {
         new Promise((resolve, reject) => {
            var formData = new FormData();
            var xhr = new XMLHttpRequest();
            for(var i = 0; i < files.length; i++) {
                formData.append("file", files[i], files[i].name);
            }
            xhr.onreadystatechange = function () {
                if (xhr.readyState == 4) {
                    if (xhr.status == 200) {
                        resolve(JSON.parse(xhr.response));
                    } else {
                        reject(xhr.response);
                    }
                }
            };
            xhr.open("POST", url, true);
            xhr.send(formData);
        });
    }
}
