package com.patex.forever.mapper;

import com.patex.forever.entities.AuthorBookEntity;
import com.patex.forever.entities.AuthorEntity;
import com.patex.forever.entities.BookEntity;
import com.patex.forever.model.Author;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.transaction.annotation.Transactional;

@Mapper(componentModel = "spring", uses = {BookMapper.class, SequenceMapper.class})
public interface AuthorMapper {

    @Transactional(readOnly = true)
    @Mapping(target = "books", ignore = true)
    Author toDto(AuthorEntity entity);


    default BookEntity toBookEntity(AuthorBookEntity authorBookEntity){
        return authorBookEntity.getBook();
    }

    @Mapping(target = "booksNoSequence", ignore = true)
    @Mapping(target = "sequences", ignore = true)
    @Mapping(target = "books", ignore = true)
    Author toListDto(AuthorEntity entity);
}
