package com.patex.forever.mapper;

import com.patex.forever.entities.BookSequenceEntity;
import com.patex.forever.entities.SequenceEntity;
import com.patex.forever.model.Sequence;
import com.patex.forever.model.SequenceBook;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = SequenceMapper.SequenceBookMapper.class)
public interface SequenceMapper {

    @Mapping(target = "books", source ="bookSequences")
    @Named("entity")
    Sequence toDto(SequenceEntity entity);

    @Mapping(target = "books", ignore = true)
    Sequence toListDto(SequenceEntity entity);

    @Mapper(componentModel = "spring", uses = BookMapper.class)
    interface SequenceBookMapper {

        SequenceBook toDto(BookSequenceEntity entity);
    }
}
