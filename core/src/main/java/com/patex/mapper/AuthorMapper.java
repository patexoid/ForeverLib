package com.patex.mapper;

import com.patex.entities.AuthorEntity;
import com.patex.zombie.model.Author;
import org.mapstruct.Mapper;
import org.springframework.transaction.annotation.Transactional;

@Mapper(componentModel = "spring", uses = {BookMapper.class, SequenceMapper.class})
public interface AuthorMapper {

    @Transactional(readOnly = true)
    Author toDto(AuthorEntity entity);

}
