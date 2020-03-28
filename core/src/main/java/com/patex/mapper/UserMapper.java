package com.patex.mapper;

import com.patex.entities.ZUser;
import com.patex.entities.ZUserConfigEntity;
import com.patex.zombie.model.User;
import com.patex.zombie.model.UserConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "password", ignore =  true)
    User toDto(ZUser user);

    UserConfig  toDtoConfig(ZUserConfigEntity entity);

    ZUser toEntity(User user);

    ZUser updateEntity(User book, @MappingTarget ZUser bookEntity);
}
