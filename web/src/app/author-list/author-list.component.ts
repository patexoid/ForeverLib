import {Component, OnInit} from '@angular/core';
import {Author} from "../author";
import {AuthorService} from "../author.service";

@Component({
  selector: 'app-author-list',
  templateUrl: './author-list.component.html',
  styleUrls: ['./author-list.component.css']
})
export class AuthorListComponent implements OnInit {

  authors: Author[];
  selectedAuthor: Author;
  filter: string;
  pageNumber: number;
  pageCount: number;


  constructor(private authorsService: AuthorService) {
  }

  getAuthors(): void {
    this.authorsService.getAuthors(this.filter, this.pageNumber, 15).then(page => {
      this.authors = page.content as Author[];
      this.pageCount = page.totalPages;

    });
  }

  refreshListFilter(): void {
    this.pageNumber = 0;
    this.refreshList()
  }

  refreshList(): void {
    this.getAuthors();
  }


  ngOnInit(): void {
    this.pageNumber = 0;
    this.filter = '';
    this.pageCount = 0;
    this.getAuthors();
  }

  onSelect(author: Author): void {
    this.selectedAuthor = author;
  }
}
