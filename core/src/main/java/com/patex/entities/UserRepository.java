package com.patex.entities;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * Created by Alexey on 25.03.2017.
 */
@Repository
public interface UserRepository extends CrudRepository<UserEntity, String> {

    @Query("select u from UserEntity u, AuthorityEntity a where u.username=a.user AND a.authority=?1")
    Collection<UserEntity> findAllByAuthoritiesIs(String authority);
}
