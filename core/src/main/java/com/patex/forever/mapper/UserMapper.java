package com.patex.forever.mapper;

import com.patex.forever.entities.LibUser;
import com.patex.forever.entities.LibUserConfigEntity;
import com.patex.forever.model.User;
import com.patex.forever.model.UserConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "password", ignore =  true)
    User toDto(LibUser user);

    UserConfig  toDtoConfig(LibUserConfigEntity entity);

    LibUser toEntity(User user);

    LibUser updateEntity(User book, @MappingTarget LibUser bookEntity);
}
