package com.patex.mapper;

import com.patex.entities.BookSequenceEntity;
import com.patex.entities.SequenceEntity;
import com.patex.model.Sequence;
import com.patex.model.SequenceBook;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = SequenceMapper.SequenceBookMapper.class)
public interface SequenceMapper {

    Sequence toDto(SequenceEntity entity);

    @Mapper(componentModel = "spring", uses = BookMapper.class)
    interface SequenceBookMapper {

        SequenceBook toDto(BookSequenceEntity entity);
    }
}
