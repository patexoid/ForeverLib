// Mirrors com.patex.forever.model DTOs field-for-field, as returned by
// AuthorController / SequenceController / BookController.
//
// java.time.Instant fields serialize as ISO-8601 strings (Spring Boot registers
// jackson-datatype-jsr310 by default), and byte[] fields (checksum) serialize
// as base64 strings.

export interface FileResource {
  id: number;
  filePath: string;
  type: string;
  size: number;
}

export interface Genre {
  id: number;
  name: string;
}

export interface BookAuthor {
  id: number;
  name: string;
}

export interface BookSequence {
  id: number;
  seqOrder: number;
  sequenceName: string;
}

export interface SimpleBook {
  id: number;
  duplicate: boolean;
  title: string;
  fileName: string;
  contentSize: number;
  created: string;
  checksum: string;
  descr: string;
  lang: string;
  fileResource: FileResource;
}

export interface Book extends SimpleBook {
  authors: BookAuthor[];
  sequences: BookSequence[];
  genres: Genre[];
  cover: FileResource;
}

export interface SequenceBook {
  seqOrder: number;
  book: Book;
}

export interface Sequence {
  id: number;
  name: string;
  updated: string;
  books: SequenceBook[];
}

export interface Author {
  id: number;
  name: string;
  booksNoSequence: Book[];
  sequences: Sequence[];
  descr: string;
  updated: string;
}

export interface User {
  username: string;
  userConfig?: unknown;
}

// Matches the JSON shape Spring Data's PageImpl serializes.
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  numberOfElements: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
