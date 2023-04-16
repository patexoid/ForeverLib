package com.patex.mapper;

import com.patex.entities.AuthorEntity;
import com.patex.entities.BookEntity;
import com.patex.entities.BookGenreEntity;
import com.patex.entities.BookSequenceEntity;
import com.patex.entities.FileResourceEntity;
import com.patex.zombie.model.Book;
import com.patex.zombie.model.BookAuthor;
import com.patex.zombie.model.BookSequence;
import com.patex.zombie.model.FileResource;
import com.patex.zombie.model.Genre;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.transaction.annotation.Transactional;


@Mapper(componentModel = "spring")
public interface BookMapper {

    @Transactional(readOnly = true)
    Book toDto(BookEntity bookEntity);

    BookEntity toEntity(Book bookEntity);

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
    @Mapping(target = "fileResource", expression = "java(updateFileResource(book.getFileResource(), bookEntity.getFileResource()))")
    @Mapping(target = "cover", expression = "java(updateFileResource(book.getCover(), bookEntity.getCover()))")
    BookEntity updateEntity(Book book, @MappingTarget BookEntity bookEntity);

    default FileResourceEntity updateFileResource(FileResource source, FileResourceEntity target) {
        if ( source != null ) {
            if(target==null){
                target=new FileResourceEntity();
            }
            target.setFilePath( source.getFilePath() );
            target.setSize( source.getSize() );
            target.setType( source.getType() );
        }
        return target;
    }
}
