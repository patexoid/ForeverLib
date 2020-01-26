package com.patex.zombie.core.mapper;

import com.patex.zombie.core.entities.AuthorBookEntity;
import com.patex.zombie.core.entities.AuthorEntity;
import com.patex.zombie.core.entities.BookEntity;
import com.patex.model.Author;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = BookMapper.class)
public interface AuthorMapper {

    Author toDto(AuthorEntity entity);

    default BookEntity toBook(AuthorBookEntity authorBookEntity){
        return authorBookEntity.getBook();
    }
}
