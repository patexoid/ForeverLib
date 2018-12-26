import {Sequence} from "./sequence";
import {Book} from "./book";
import {AuthorBook} from "./author-book";

export class Author {

  id: number;
  name: string;
  descr: string;

  sequences: Array<Sequence>;
  booksNoSequence: Array<AuthorBook>;

}
