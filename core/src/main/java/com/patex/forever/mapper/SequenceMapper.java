package com.patex.forever.mapper;

import com.patex.forever.entities.BookSequenceEntity;
import com.patex.forever.entities.SequenceEntity;
import com.patex.forever.model.Sequence;
import com.patex.forever.model.SequenceBook;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = SequenceMapper.SequenceBookMapper.class)
public interface SequenceMapper {

    @Mapping(target = "books", source ="bookSequences")
    Sequence toDto(SequenceEntity entity);

    @Mapper(componentModel = "spring", uses = BookMapper.class)
    interface SequenceBookMapper {

        SequenceBook toDto(BookSequenceEntity entity);
    }
}
