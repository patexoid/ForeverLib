package com.patex.mapper;

import com.patex.entities.AuthorBookEntity;
import com.patex.entities.AuthorEntity;
import com.patex.entities.BookEntity;
import com.patex.zombie.model.Author;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = BookMapper.class)
public interface AuthorMapper {

    Author toDto(AuthorEntity entity);

    default BookEntity toBook(AuthorBookEntity authorBookEntity){
        return authorBookEntity.getBook();
    }
}
