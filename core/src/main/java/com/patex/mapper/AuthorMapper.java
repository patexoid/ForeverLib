package com.patex.mapper;

import com.patex.entities.AuthorEntity;
import com.patex.zombie.model.Author;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.transaction.annotation.Transactional;

@Mapper(componentModel = "spring", uses = {BookMapper.class, SequenceMapper.class})
public interface AuthorMapper {

    @Transactional(readOnly = true)
    @Mapping(target = "books", ignore = true)
    Author toDto(AuthorEntity entity);


    @Mapping(target = "booksNoSequence", ignore = true)
    @Mapping(target = "sequences", ignore = true)
    @Mapping(target = "books", ignore = true)
    Author toListDto(AuthorEntity entity);
}
