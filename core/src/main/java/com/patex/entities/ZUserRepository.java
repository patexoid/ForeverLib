package com.patex.entities;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * Created by Alexey on 25.03.2017.
 */
@Repository
public interface ZUserRepository extends CrudRepository<ZUser, String> {

    @Query("select a.user from ZUserAuthority a where a.authority=?1")
    Collection<ZUser> findAllByAuthoritiesIs(String authority);

}
