import {Sequence} from "./sequence";
import {Book} from "./book";

export class Author {

  id: number;
  name: string;
  descr: string;

  sequences: Array<Sequence>;
  booksNoSequence: Array<Book>;

}
