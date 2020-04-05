package com.patex.zombie.opds.entity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * Created by Alexey on 11/26/2016.
 */
@Repository
public interface ExtLibraryRepository extends CrudRepository<ExtLibrary, Long> {

    Collection<ExtLibrary> findAll();

    Page<ExtLibrary> findAll(Pageable pageable);

}
