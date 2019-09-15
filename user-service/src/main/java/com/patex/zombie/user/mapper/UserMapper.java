package com.patex.zombie.user.mapper;

import com.patex.model.User;
import com.patex.zombie.user.controller.UserCreateRequest;
import com.patex.zombie.user.entities.AuthorityEntity;
import com.patex.zombie.user.entities.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toDto(UserEntity entity);

    default String authorityString(AuthorityEntity entity) {
        return entity.getAuthority();
    }

    UserEntity toEntity(UserCreateRequest createRequest);

    default AuthorityEntity authorityObject( String authority){
        return new AuthorityEntity(authority);
    }
}

