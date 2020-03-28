package com.patex.mapper;

import com.patex.entities.AuthorEntity;
import com.patex.zombie.model.Author;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {BookMapper.class, SequenceMapper.class})
public interface AuthorMapper {

    Author toDto(AuthorEntity entity);

}
