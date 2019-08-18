package com.patex.mapper;

import com.patex.entities.AuthorEntity;
import com.patex.entities.BookEntity;
import com.patex.entities.BookGenreEntity;
import com.patex.entities.BookSequenceEntity;
import com.patex.model.Book;
import com.patex.model.BookAuthor;
import com.patex.model.BookSequence;
import com.patex.model.Genre;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring")
public interface BookMapper {

    Book toDto(BookEntity bookEntity);

    default BookAuthor toBookAuthor(AuthorEntity authorEntity) {
        return new BookAuthor(authorEntity.getId(), authorEntity.getName());
    }

    default BookSequence toBookSequence(BookSequenceEntity entity) {
        return new BookSequence(entity.getSequence().getId(), entity.getSeqOrder(), entity.getSequence().getName());
    }

    default Genre toGenres(BookGenreEntity genreEntity) {
        return new Genre(genreEntity.getGenre().getId(), genreEntity.getGenre().getName());
    }

    @Mapping(target = "authors", ignore =  true)
    @Mapping(target = "sequences", ignore =  true)
    @Mapping(target = "genres", ignore =  true)
    BookEntity updateEntity(Book book, @MappingTarget BookEntity bookEntity);
}
