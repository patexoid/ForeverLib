import {Component, Input, OnInit} from '@angular/core';
import {Author} from "../author";
import {Book} from "../book";
import {AuthorService} from "../author.service";
import {ActivatedRoute, ActivatedRouteSnapshot, Resolve, RouterStateSnapshot} from "@angular/router";
import {Observable} from "rxjs/index";

@Component({
  selector: 'app-author',
  templateUrl: './author.component.html',
  styleUrls: ['./author.component.css']
})
export class AuthorComponent implements OnInit, Resolve<Author> {

  constructor(private route: ActivatedRoute,
              private authorsService: AuthorService) {
  }

  author: Author;

  selectedBook: Book;


  ngOnInit(): void {
    this.getHero();
  }


  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<Author> | Promise<Author> | Author {
    return null;
  }

  getHero(): void {
    const id = +this.route.snapshot.paramMap.get('id');
    this.route.paramMap.subscribe(params => {
      let id = params.get('id');
      if (id != null) {
        this.authorsService.getAuthor(parseInt(id)).then(a => this.author = a)
      }
    });
  }

  @Input()
  set selectedAuthor(author: Author) {
    this.selectedBook = null;
    this.author = author;
    this.authorsService.getAuthor(author.id).then(author => {
      this.author = author;
    })
  }

  onSelect(book: Book): void {

  }


}
