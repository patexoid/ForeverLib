package com.patex.forever.entities;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * Created by Alexey on 25.03.2017.
 */
@Repository
public interface LibUserRepository extends CrudRepository<LibUser, String> {

    @Query("select a.user from LibUserAuthority a where a.authority=?1")
    Collection<LibUser> findAllByAuthoritiesIs(String authority);

}
