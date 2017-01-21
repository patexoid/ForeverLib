package com.patex.entities;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Alexey on 11/26/2016.
 */
@Repository
public interface ExtLibraryRepository extends CrudRepository<ExtLibrary, Long> {

}
