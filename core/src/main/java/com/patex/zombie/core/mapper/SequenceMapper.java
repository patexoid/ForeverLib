package com.patex.zombie.core.mapper;

import com.patex.zombie.core.entities.BookSequenceEntity;
import com.patex.zombie.core.entities.SequenceEntity;
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
